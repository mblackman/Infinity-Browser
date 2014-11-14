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
import android.os.Bundle;
import android.app.Fragment;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

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
public class Board extends Fragment implements PageLoader.PageLoaderResponse {
    // ARG for the board link to be sent in
    private static final String ARG_BOARD_LINK = "boardlink";
    private PageLoader mPageGetter;
    private boolean mIsRootBoard;
    private List<Post> mPosts;
    private PostArrayAdapter mAdapter;
    private URL mBoardLink;
    private View mRootView;
    private ListView mListView;

    private Boolean mPageLoaded = false;

    private EndlessScrollListener mScrollListener;
    private OnReplyClickedListener mListener;

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
     * Creates a new instance of a board based on a given board and thread
     *
     * @param boardRoot Root of board EG /v/, /tech/, etc..
     * @param threadNo The thread being initiated
     * @return A new instance of a fragment board.
     */
    public static Board newInstance(String boardRoot, String threadNo) {
        String boardLink = boardRoot + "res/" + threadNo + ".html";
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true); // Do not forget this!!!
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.board_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshBoard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        mRootView = inflater.inflate(R.layout.fragment_board, container, false);
        View progress = inflater.inflate(R.layout.board_progress_message, mListView, false);

        mListView = (ListView) mRootView.findViewById(R.id.lv_board_posts);
        mListView.addFooterView(progress);

        mPosts = new ArrayList<Post>();
        mAdapter = new PostArrayAdapter(getActivity());
        mAdapter.updatePosts(mPosts, mListener);

        if(mBoardLink != null){
            mIsRootBoard = !mBoardLink.getPath().contains(".html");
            mPageGetter = new PageLoader(getActivity(), mRootView, mPosts, mAdapter, mIsRootBoard);
            mPageGetter.mResponse = this;
            mPageGetter.execute(mBoardLink);
        }

        mListView.setAdapter(mAdapter);

        mScrollListener = new EndlessScrollListener(ImageLoader.getInstance(), true, true);
        mScrollListener.setParentView(mRootView);
        mListView.setOnScrollListener(mScrollListener);

        getActivity().getActionBar().setTitle(mBoardLink.toString()
                .replace("http://8chan.co", "")
                .replace("res/", "")
                .replace(".html", ""));

        return mRootView;
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
            mListener = (OnReplyClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnReplyClickedListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(mBoardLink.toString()
                .replace("http://8chan.co", "")
                .replace("res/", "")
                .replace(".html", ""));
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
     * Called when the fragment is being destroyed
     */
    @Override
    public void onDestroy() {
        mPosts.clear();
        mAdapter.notifyDataSetChanged();
        mPosts = null;
        mAdapter = null;
        System.gc();
        super.onDestroy();
    }

    /**
     * Called when a page is loaded or fails to load.
     *
     * @param isLoaded If the page successfully loaded or not.
     */
    @Override
    public void setPageLoaded(Boolean isLoaded) {
        mPageLoaded = isLoaded;
    }

    /**
     * Makes a toast indicating an error loading the board.
     *
     * @param error The error message.
     */
    @Override
    public void sendErrorMessage(final CharSequence error) {
        Thread thread = new Thread() {
            public void run() {

                Looper.prepare();
                MessageQueue queue = Looper.myQueue();

                queue.addIdleHandler(new MessageQueue.IdleHandler() {
                    int mReqCount = 0;

                    @Override
                    public boolean queueIdle() {
                        if (++mReqCount == 2) {
                            // Quit looper
                            Looper.myLooper().quit();
                            return false;
                        } else
                            return true;
                    }
                });

                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getActivity(), error, duration);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
                Looper.loop();
            }
        };
        thread.start();
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
    public interface OnReplyClickedListener {
        public void onReplyClicked(String boardRoot, String threadNo);
    }

    private void refreshBoard(){
        if(mScrollListener != null) {
            mScrollListener.resetBoardPage();
        }

        mPosts.clear();
        mAdapter.notifyDataSetChanged();

        mPageGetter = new PageLoader(getActivity(), mRootView, mPosts, mAdapter, mIsRootBoard);
        mPageGetter.mResponse = this;
        mPageGetter.execute(mBoardLink);
    }

    /**
     * This class is used to set the on scroll listener for the list view on the board.
     * Now the page will load the next page on a board when the bottom is met.
     */
    public class EndlessScrollListener extends PauseOnScrollListener {
        private int currentPage = 1;
        private View mParent;

        public EndlessScrollListener(ImageLoader imageLoader, boolean pauseOnScroll,
                                     boolean pauseOnFling) {
            super(imageLoader, pauseOnScroll, pauseOnFling);
        }

        public void setParentView(View parentView) {
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
            if (mIsRootBoard && mPageLoaded &&
                    (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleItemCount)) {
                URL newPage;
                try {
                    newPage = new URL(mBoardLink.toString() + (++currentPage) + ".html");
                    mPageGetter = new PageLoader(getActivity(), mParent, mPosts, mAdapter,
                            mIsRootBoard);
                    mPageGetter.mResponse = Board.this;
                    mPageGetter.execute(newPage);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        public void resetBoardPage() {
            currentPage = 1;
        }
    }
}
