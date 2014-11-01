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

package blackman.matt.board;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import blackman.matt.infinitebrowser.R;


/**
 * The board on 8chan in relation to a link you initialize this class with.
 * A main board or thread on 8chan. This will load and handle all of the posts on the board.
 *
 */
public class Board extends Fragment {
    // ARG for the board link to be sent in
    private static final String ARG_BOARD_LINK = "boardlink";
    private PageLoader mPageGetter;
    private boolean mIsRootBoard;
    private List<PostView> mPosts;
    private PostArrayAdapter mAdapter;
    private URL mBoardLink;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param boardLink the link to either the main board or a thread.
     * @return A new instance of fragment Board.
     */
    public static Board newInstance(String boardLink) {
        Board fragment = new Board();
        Bundle args = new Bundle();
        args.putString(ARG_BOARD_LINK, boardLink);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Auto-generated constructor full of nothing.
     */
    public Board() {
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
        if (getArguments() != null) {
            try {
                mBoardLink = new URL(getArguments().getString(ARG_BOARD_LINK));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public OnFragmentInteractionListener getListener() {
        return mListener;
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_board, container, false);

        ListView myListView = (ListView) rootView.findViewById(R.id.lv_board_posts);
        View progress = inflater.inflate(R.layout.board_progress_message, myListView, false);
        myListView.addFooterView(progress);

        mPosts = new ArrayList<PostView>();
        mAdapter = new PostArrayAdapter(mPosts);

        if(mBoardLink != null){
            mIsRootBoard = !mBoardLink.getPath().endsWith(".html");
            mPageGetter = new PageLoader(getActivity(), rootView, mPosts, mAdapter, mListener);
            mPageGetter.execute(mBoardLink);
        }

        myListView.setAdapter(mAdapter);
        myListView.setOnScrollListener(new EndlessScrollListener(rootView));

        String link = mBoardLink.getPath();
        if(link.contains(".html")) {
            link = link.replace(".html", "");
        }
        if(link.contains("/res/")){
            link = link.replace("res/", "");
        }

        getActivity().getActionBar().setTitle(link);

        return rootView;
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
        public void onReplyClicked(String postLink);
    }

    /**
     * This class is used to set the on scroll listener for the list view on the board.
     * Now the page will load the next page on a board when the bottom is met.
     */
    public class EndlessScrollListener implements AbsListView.OnScrollListener {
        private int currentPage = 1;
        private View mParent;

        /**
         * Default constructor to get the parent of who called him.
         *
         * @param parentView The view that called this class.
         */
        public EndlessScrollListener(View parentView) {
            mParent = parentView;
        }

        /**
         * When the list view hits the bottom, the program gets the next page and displays it.
         *
         * @param view The list view being scrolled on.
         * @param firstVisibleItem First visible on the screen.
         * @param visibleItemCount Number of view on the screen.
         * @param totalItemCount How many views the list view can display.
         */
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (mIsRootBoard && mPageGetter.getStatus() == AsyncTask.Status.FINISHED &&
                    (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleItemCount)) {
                URL newPage;
                try {
                    newPage = new URL(mBoardLink.toString() + (++currentPage) + ".html");
                    mPageGetter = new PageLoader(getActivity(), mParent, mPosts, mAdapter, mListener);
                    mPageGetter.execute(newPage);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Called when the scroll state is changed.
         * Currently empty.
         *
         * @param view List view being looked at.
         * @param scrollState The status of the scrolling.
         */
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
}
