/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package blackman.matt.infinitebrowser;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * The list of all the boards on 8Chan with the ability to sort through boards by value,
 * favorite boards, and search for boards. All the board information is stored in a database,
 * and the list can always be updated.
 *
 */
public class BoardList extends Fragment {
    private OnFragmentInteractionListener mListener;
    private String mDBOrderBy;
    private String mDBSortBy;
    private BoardListDatabase list_db;

    // Max board links to load each pass
    private static final int MAX_CARDS = 10;
    // Default selection to sort the value column by
    private static final String DEFAULT_SELECTED_COLUMN = "uniqueips";

    /**
     * Used to control what happens when the spinners are used on the boardList.
     * Will update the search string for the SQL query.
     *
     * TODO: make the assigns use strings.xml
     */
    class SpinnerActivity extends Activity implements Spinner.OnItemSelectedListener {
        /**
         * Called when an item is changed on a spinner. Used to changed the search terms of
         * the SQL query.
         *
         * @param parent Parent adapter.
         * @param view SpinnerView that was clicked.
         * @param pos Position of the newly selected item.
         * @param id Id of the selected SpinnerView.
         */
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if(parent.getId() == R.id.spinner_sort_order) {
                mDBOrderBy = ((TextView) view).getText().toString();
            }
            else if(parent.getId() == R.id.spinner_sort_by) {
                String sortByName;
                sortByName = parent.getItemAtPosition(pos).toString();

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

        /**
         * What happens when nothing is selected on a spinner.
         *
         * @param parent The SpinnerView with nothing.
         */
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BoardList.
     */
    public static BoardList newInstance() {
        BoardList fragment = new BoardList();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Auto-generated constructor full of nothing.
     */
    public BoardList() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created on the activity. Gets the arguments.
     *
     * @param savedInstanceState the instance state of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called when the fragments view is being created. Handled inflating the view and assigning
     * values.
     *
     * @param inflater inflater sent in from parent activity.
     * @param container The container that the fragment will go in.
     * @param savedInstanceState the instance state of the activity.
     * @return returns this fragments newly created view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        Spinner spinnerSort;
        Spinner spinnerOrder;
        ArrayAdapter<CharSequence> sortAdapter;
        ArrayAdapter<CharSequence> orderAdapter;

        rootView = inflater.inflate(R.layout.fragment_board_list, container, false);
        list_db = new BoardListDatabase(rootView.getContext());


        // Set up the spinners
        spinnerSort = (Spinner)rootView.findViewById(R.id.spinner_sort_by);
        spinnerOrder = (Spinner)rootView.findViewById(R.id.spinner_sort_order);

        sortAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sql_columns_array,
                android.R.layout.simple_spinner_item);

        orderAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sql_sort_order_array,
                android.R.layout.simple_spinner_item);

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSort.setAdapter(sortAdapter);
        spinnerOrder.setAdapter(orderAdapter);

        spinnerOrder.setOnItemSelectedListener(new SpinnerActivity());
        spinnerSort.setOnItemSelectedListener(new SpinnerActivity());

        // Gets an updated list of boards
        if(list_db.isEmpty()) {
            new getBoardList().execute();
        }

        return rootView;
    }

    /**
     * Updates the list of boards based on the SQL selected by the SpinnerViews on the
     * BoardList.
     */
    private void updateDatabaseView() {
        ListView mLLBoards;
        Cursor qBoards;
        String[] from;
        int[] to;

        mLLBoards = (ListView) getActivity().findViewById(R.id.lv_board_list);

        if(mDBSortBy == null){
            mDBSortBy = DEFAULT_SELECTED_COLUMN;
        }
        if(mDBOrderBy == null){
            mDBOrderBy = "DESC";
        }

        from = new String[] {"favorited", "boardlink", mDBSortBy};
        to = new int[] {R.id.tb_board_fav, R.id.tv_board_link, R.id.tv_board_value};

        qBoards = list_db.getBoardsInSortedOrder(mDBSortBy, mDBOrderBy);
        boardListCursorAdapter adapter = new boardListCursorAdapter(getActivity(), qBoards, mDBSortBy);

        if(!adapter.isEmpty()) {
            mLLBoards.setAdapter(adapter);
        }
    }

    /**
     * Called when the parent activity attached to this newly created fragment and checks
     * if the fragment interaction listener has be implemented by the activity.
     *
     * @param activity the parent activity of this fragment.
     */
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

    /**
     * After the fragment has run it's course and much be removed from the activity this
     * is called.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    /**
     * Gets an updated list of all the boards on 8Chan and saves that list in the database, while
     * updating existing board entries.
     */
    public class getBoardList extends AsyncTask<Void, Void, Document> {
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
            if(html != null) {
                Elements boards;

                boards = html.select("tbody").first().children();

                // Looks through all the boards
                for(Element board : boards) {
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

                    if(list_db.boardExists(boardLink)){
                        list_db.updateBoard(boardName, postsInLastHour, totalPosts, uniqueIps);
                    }
                    else {
                        list_db.insertBoard(boardName, nationality, boardLink, postsInLastHour,
                                totalPosts, uniqueIps, dateCreated);
                    }
                }
                updateDatabaseView();
            }
        }
    }
}
