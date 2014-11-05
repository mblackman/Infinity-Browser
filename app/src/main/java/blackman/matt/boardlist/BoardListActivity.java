package blackman.matt.boardlist;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import blackman.matt.infinitebrowser.R;

public class BoardListActivity extends Activity implements SearchView.OnQueryTextListener,
        SearchView.OnCloseListener, BoardListCursorAdapter.BoardFavoritedListener {

    private String mDBOrderBy;
    private String mDBSortBy;

    private BoardListCursorAdapter mAdapter;

    // Default selection to sort the value column by
    private static final String DEFAULT_SELECTED_COLUMN = DatabaseDef.Boards.UNIQUE_IPS;

    // Default selection to sort the value column by
    private static final String DEFAULT_SORT_ORDER = "DESC";

    private SearchView mSearchView;
    private String mCurFilter;

    /**
     * Interface to favorite a board when the user hits the favorite button.
     *
     * @param boardLink Link of the board to be favorited.
     * @param isChecked If the user just checked it true or false.
     */
    @Override
    public void favoriteBoard(String boardLink, Boolean isChecked) {
        BoardListDatabase list_db = new BoardListDatabase(this);
        CharSequence text;
        int duration = Toast.LENGTH_SHORT;

        if (isChecked) {
            text = "Added Board " + boardLink;
        } else {
            text = "Removed Board " + boardLink;
        }

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();

        list_db.favoriteBoard(boardLink, isChecked);


        Cursor mCursor = list_db.getSortedSearch(mCurFilter, mDBSortBy, mDBOrderBy);

        mAdapter.swapCursor(mCursor);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Search button on the activity bar.
     */
    public static class MySearchView extends SearchView {
        /**
         * Basic constructor which calls parent constructor.
         *
         * @param context context of the caller.
         */
        public MySearchView(Context context) {
            super(context);
        }

        /**
         * Clears the text when closed.
         */
        @Override
        public void onActionViewCollapsed() {
            setQuery("", false);
            super.onActionViewCollapsed();
        }
    }

    /**
     * Called when the search is closed.
     *
     * @return true
     */
    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
    }

    /**
     * Always true.
     *
     * @param query The query
     * @return true.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    /**
     * When the text in the search query is changed this updates the list.
     *
     * @param newText The newly inputted text.
     * @return If the change was a success.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

        if (mCurFilter == null && newFilter == null) {
            return true;
        }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) {
            return true;
        }
        mCurFilter = newFilter;
        updateDatabaseView();
        return true;
    }

    /**
     * When the activity is created this sets up the activity.
     *
     * @param savedInstanceState The saved state if this was created before.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);
        ListView boardsList = (ListView) findViewById(R.id.lv_board_list);

        BoardListDatabase list_db = new BoardListDatabase(this);
        Cursor cursor = list_db.getSortedSearch("", DEFAULT_SELECTED_COLUMN, DEFAULT_SORT_ORDER);
        mAdapter = new BoardListCursorAdapter(this, cursor);
        mAdapter.setListener(this);
        boardsList.setAdapter(mAdapter);
        initSpinners();
    }


    /**
     * When the option menu is created this handles the creation events.
     *
     * @param menu The menu being created.
     * @return If the creation was a success.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_board_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search_list);
        mSearchView = new MySearchView(this);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconified(true);
        searchItem.setActionView(mSearchView);
        return true;
    }

    /**
     * Called when an item is clicked on the activity bar. Mostly handles update being clicked.
     *
     * @param item Item that was clicked.
     * @return Returns if the click handle was successful.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_refresh_list:
                new GetBoardList().execute();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Updates the list of boards based on the SQL selected by the SpinnerViews on the
     * BoardList.
     */
    private void updateDatabaseView() {
        BoardListDatabase list_db = new BoardListDatabase(this);
        Cursor qBoards = list_db.getSortedSearch(mCurFilter, mDBSortBy, mDBOrderBy);

        mAdapter.swapCursor(qBoards);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Sets up the spinners on the view.
     */
    private void initSpinners() {
        Spinner spinnerSort;
        Spinner spinnerOrder;

        ArrayAdapter<CharSequence> sortAdapter;
        ArrayAdapter<CharSequence> orderAdapter;

        // Set up the spinners
        spinnerSort = (Spinner) findViewById(R.id.spinner_sort_by);
        spinnerOrder = (Spinner) findViewById(R.id.spinner_sort_order);

        sortAdapter = ArrayAdapter.createFromResource(this, R.array.sql_columns_array,
                android.R.layout.simple_spinner_item);

        orderAdapter = ArrayAdapter.createFromResource(this, R.array.sql_sort_order_array,
                android.R.layout.simple_spinner_item);

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSort.setAdapter(sortAdapter);
        spinnerOrder.setAdapter(orderAdapter);

        spinnerOrder.setOnItemSelectedListener(new SpinnerActivity());
        spinnerSort.setOnItemSelectedListener(new SpinnerActivity());
    }

    /**
     * Gets an updated list of all the boards on 8Chan and saves that list in the database, while
     * updating existing board entries.
     */
    public class GetBoardList extends AsyncTask<Void, Void, Document> {
        /**
         * Loads all the information from the boards index and returns the HTML doc.
         *
         * @param params nothing
         * @return The HTML doc of the boards lists.
         */
        @Override
        protected Document doInBackground(Void... params) {
            Document boardPage;
            String url;

            boardPage = null;
            url = "http://8chan.co/boards.html";

            try {
                boardPage = Jsoup.connect(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return boardPage;
        }

        /**
         * Takes the HTML doc and parses through all the board information and updates the
         * SQL database.
         *
         * @param html The page with the boards on it.
         */
        @Override
        protected void onPostExecute(Document html) {
            if (html != null) {
                BoardListDatabase list_db = new BoardListDatabase(getParent());
                Elements boards = html.select("tbody").first().children();

                // Looks through all the boards
                for (Element board : boards) {
                    Elements boardItems;
                    String nationality;
                    String boardLink;
                    String boardName;
                    String postsInLastHour;
                    String totalPosts;
                    String uniqueIps;
                    String dateCreated;

                    boardItems = board.children();

                    nationality = boardItems.get(0).select("img").attr("title");
                    boardLink = boardItems.get(1).select("a").attr("href");
                    boardName = boardItems.get(2).text();
                    postsInLastHour = boardItems.get(3).text();
                    totalPosts = boardItems.get(4).text();
                    uniqueIps = boardItems.get(5).text();
                    dateCreated = boardItems.get(6).text();

                    if (list_db.boardExists(boardLink)) {
                        list_db.updateBoard(boardName, postsInLastHour, totalPosts, uniqueIps);
                    } else {
                        list_db.insertBoard(boardName,
                                nationality,
                                boardLink,
                                postsInLastHour,
                                totalPosts,
                                uniqueIps,
                                dateCreated
                        );
                    }
                }
                updateDatabaseView();
            }
        }
    }

    /**
     * Used to control what happens when the spinners are used on the boardList.
     * Will update the search string for the SQL query.
     */
    class SpinnerActivity extends Activity implements Spinner.OnItemSelectedListener {
        /**
         * Called when an item is changed on a spinner. Used to changed the search terms of
         * the SQL query.
         *
         * @param parent Parent adapter.
         * @param view   SpinnerView that was clicked.
         * @param pos    Position of the newly selected item.
         * @param id     Id of the selected SpinnerView.
         */
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (view != null && parent.getId() == R.id.spinner_sort_order) {
                mDBOrderBy = ((TextView) view).getText().toString();
            } else if (view != null && parent.getId() == R.id.spinner_sort_by) {
                String sortByName;
                sortByName = parent.getItemAtPosition(pos).toString().toLowerCase();

                if (sortByName.equals("board name")) {
                    mDBSortBy = DatabaseDef.Boards.BOARD_NAME;
                } else if (sortByName.equals("nationality")) {
                    mDBSortBy = DatabaseDef.Boards.NATIONALITY;
                } else if (sortByName.equals("board tag")) {
                    mDBSortBy = DatabaseDef.Boards.BOARD_LINK;
                } else if (sortByName.equals("total posts")) {
                    mDBSortBy = DatabaseDef.Boards.TOTAL_POSTS;
                } else if (sortByName.equals("posts/hour")) {
                    mDBSortBy = DatabaseDef.Boards.POSTS_LAST_HOUR;
                } else if (sortByName.equals("unique ips")) {
                    mDBSortBy = DatabaseDef.Boards.UNIQUE_IPS;
                } else if (sortByName.equals("date created")) {
                    mDBSortBy = DatabaseDef.Boards.DATE_CREATED;
                } else if (sortByName.equals("following")) {
                    mDBSortBy = DatabaseDef.Boards.FAVORITED;
                } else {
                    mDBSortBy = DEFAULT_SELECTED_COLUMN;
                }
            }
            if (mDBSortBy != null && mDBOrderBy != null) {
                updateDatabaseView();
            }
        }

        /**
         * What happens when nothing is selected on a spinner.
         *
         * @param parent The SpinnerView with nothing.
         */
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }
}
