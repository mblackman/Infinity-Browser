package blackman.matt.infinitebrowser;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Boardlist.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Boardlist#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class Boardlist extends Fragment implements BoardListCard.OnFragmentInteractionListener {
    private BoardListDatabase list_db;
    private static final String DEFAULT_SELECTED_COLUMN = "uniqueips";

    private LinearLayout mLLBoards;
    private String mDBOrderBy;
    private String mDBSortBy;

    private OnFragmentInteractionListener mListener;

    class MyClickClass implements CompoundButton.OnCheckedChangeListener {
        String boardLink;
        public MyClickClass(String board) {
            this.boardLink = board;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            list_db.favoriteBoard(boardLink, isChecked);
        }
    }

    /**
     * TODO: make the assigns use strings.xml
     */
    class SpinnerActivity extends Activity implements Spinner.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if(parent.getId() == R.id.spinner_sort_order) {
                mDBOrderBy = ((TextView) view).getText().toString();
            }
            else if(parent.getId() == R.id.spinner_sort_by) {
                String sortByName = parent.getItemAtPosition(pos).toString();

                if(sortByName.toLowerCase().equals("board name")) {
                    mDBSortBy = "boardname";
                }
                else if(sortByName.toLowerCase().equals("nationality")) {
                    mDBSortBy = "nation";
                }
                else if(sortByName.toLowerCase().equals("board tag")) {
                    mDBSortBy = "boardlink";
                }
                else if(sortByName.toLowerCase().equals("total posts")) {
                    mDBSortBy = "totalposts";
                }
                else if(sortByName.toLowerCase().equals("posts/hour")) {
                    mDBSortBy = "postslasthour";
                }
                else if(sortByName.toLowerCase().equals("unique ips")) {
                    mDBSortBy = "uniqueips";
                }
                else if(sortByName.toLowerCase().equals("date created")) {
                    mDBSortBy = "datecreated";
                }
                else if(sortByName.toLowerCase().equals("following")) {
                    mDBSortBy = "favorited";
                }
                else {
                    mDBSortBy = DEFAULT_SELECTED_COLUMN;
                }
            }
            if(mDBSortBy != null && mDBOrderBy != null) {
                updateDatabaseView();
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Boardlist.
     */
    // TODO: Rename and change types and number of parameters
    public static Boardlist newInstance() {
        Boardlist fragment = new Boardlist();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public Boardlist() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_board_list, container, false);
        list_db = new BoardListDatabase(rootView.getContext());
        mLLBoards = (LinearLayout) rootView.findViewById(R.id.ll_board_list);

        // Set up the spinners
        Spinner spinnerSort = (Spinner)rootView.findViewById(R.id.spinner_sort_by);
        Spinner spinnerOrder = (Spinner)rootView.findViewById(R.id.spinner_sort_order);
        ArrayAdapter<CharSequence> sortAdapter;
        ArrayAdapter<CharSequence> orderAdapter;
        sortAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sql_columns_array, android.R.layout.simple_spinner_item);
        orderAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sql_sort_order_array, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerOrder.setAdapter(orderAdapter);

        spinnerOrder.setOnItemSelectedListener(new SpinnerActivity());
        spinnerSort.setOnItemSelectedListener(new SpinnerActivity());

        // Gets an updated list of boards
        if(list_db.isEmpty()) {
            new getBoardList().execute();
            updateDatabaseView();
        }

        return rootView;
    }

    private static final int MAX_CARDS = 10;

    private void updateDatabaseView() {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        Cursor qBoards;
        int i = 0;

        if(mDBSortBy == null){
            mDBSortBy = DEFAULT_SELECTED_COLUMN;
        }
        if(mDBOrderBy == null){
            mDBOrderBy = "DESC";
        }
        if(mLLBoards.getChildCount() != 0) {
            mLLBoards.removeAllViews();
        }

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        qBoards = list_db.getBoardsInSortedOrder(mDBSortBy, mDBOrderBy);
        qBoards.moveToFirst();
        while(!qBoards.isAfterLast() && i < MAX_CARDS) {
            String boardName;
            final String boardLink;
            String nationality;
            String displayColumn;
            int favorited;
            BoardListCard boardCard;

            boardName = qBoards.getString(qBoards.getColumnIndexOrThrow("boardname"));
            boardLink = qBoards.getString(qBoards.getColumnIndexOrThrow("boardlink"));
            nationality = qBoards.getString(qBoards.getColumnIndexOrThrow("nation"));
            displayColumn = qBoards.getString(qBoards.getColumnIndexOrThrow(mDBSortBy));
            favorited = qBoards.getInt(qBoards.getColumnIndexOrThrow("favorited"));

            boardCard = BoardListCard.newInstance(boardName, boardLink, displayColumn, nationality, favorited);

            boardCard.saveOnChangeListener(new MyClickClass(boardLink));

            if(fragmentManager.findFragmentByTag(boardLink) != null) {
                fragmentTransaction.remove(fragmentManager.findFragmentByTag(boardLink));
            }

            fragmentTransaction.add(R.id.ll_board_list, boardCard, boardLink);

            i++;
            qBoards.moveToNext();
        }
        fragmentTransaction.commit();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public class getBoardList extends AsyncTask<Void, Void, Document> {
        @Override
        protected Document doInBackground(Void... params) {
            Document boardPage = null;
            String url = "http://8chan.co/boards.html";

            try {
                boardPage = Jsoup.connect(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return boardPage;
        }

        @Override
        protected void onPostExecute(Document html) {
            if(html != null) {
                Elements boards = html.select("tbody").first().children();

                // Looks through all the boards
                for(Element board : boards) {
                    Elements boardItems = board.children();

                    String nationality = boardItems.get(0).select("img").attr("title");
                    String boardLink = boardItems.get(1).select("a").attr("href");
                    String boardName = boardItems.get(2).text();
                    String postsInLastHour = boardItems.get(3).text();
                    String totalPosts = boardItems.get(4).text();
                    String uniqueIps = boardItems.get(5).text();
                    String dateCreated = boardItems.get(6).text();

                    if(list_db.boardExists(boardLink)){
                        list_db.updateBoard(boardName, postsInLastHour, totalPosts, uniqueIps);
                    }
                    else {
                        list_db.insertBoard(boardName, nationality, boardLink, postsInLastHour,
                                totalPosts, uniqueIps, dateCreated);
                    }
                }
            }
        }
    }
}
