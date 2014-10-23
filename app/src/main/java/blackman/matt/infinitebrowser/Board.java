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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;


/**
 * The board on 8chan in relation to a link you initialize this class with.
 * A main board or thread on 8chan. This will load and handle all of the posts on the board.
 *
 */
public class Board extends Fragment {
    // ARG for the board link to be sent in
    private static final String ARG_BOARD_LINK = "boardlink";

    private String mBoardLink;

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
            mBoardLink = getArguments().getString(ARG_BOARD_LINK);
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
        View rootView = inflater.inflate(R.layout.fragment_board, container, false);

        if(mBoardLink != null){
            new OchBoard().execute(mBoardLink);
        }

        return rootView;
    }

    /**
     * Hook method used to help the fragment interact with the parent activity.
     *
     * @param uri the uri of the activity being called from.
     */
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        public void onFragmentInteraction(Uri uri);
    }

    public String getBoardLink() {
        return mBoardLink;
    }

    /**
     * Takes in a list of urls and gets the html from them. Then it takes the html doc and
     * turns it into post cards to display.
     * This assumes you send it a link and doesn't check for null.
     */
    public class OchBoard extends AsyncTask<String, Void, Document> {
        /**
         * Reads in urls sent by user to download the html from.
         * @param urls the urls sent by the user.
         * @return returns the html document
         */
        @Override
        protected Document doInBackground(String... urls) {
            Document ochPage ;
            String url;

            ochPage = null;
            url = urls[0];

            try {
                ochPage = Jsoup.connect(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ochPage;
        }

        /**
         * After the page is read in, the pages are turned into fragments and are put on the
         * screen.
         * @param html the document to process.
         */
        @Override
        protected void onPostExecute(Document html) {
            LinearLayout postView;
            Elements threads;

            postView = (LinearLayout) getActivity().findViewById(R.id.posts_view);
            postView.removeAllViews();

            // Gets all the parent posts on page
            threads = html.select("[id*=thread_]");

            // Looks through all the master posts
            for (Element thread : threads) {
                // Create main elements and post
                Elements postReplies;
                PostView opPost;

                try {
                   opPost = createPostOP(thread);
                    postView.addView(opPost);
                }
                catch (Exception e) {
                    TextView errorView;
                    errorView = new TextView(getActivity());
                    errorView.setText(e.toString());
                    postView.addView(errorView);
                }

                postReplies = thread.getElementsByClass("post reply");

                // Looks through all the replies to an OP post
                for (Element postReply : postReplies) {

                }
            }
        }

        /**
         * Takes information from HTML elements and creates the OP post of a board
         * or a thread and returns the newly created post.
         *
         * @param postElement the post's HTML elements.
         * @return the newly created post.
         */
        private PostView createPostOP(Element postElement) {
            PostView opPost;
            Elements singleFile;
            Elements multiFiles;
            Elements omitted;
            Elements subjects;
            Element postOp;
            Element imageFiles;
            Element postLink;
            String postNumber;
            String userName;
            String postDate;
            String postTopic;
            String postText ;
            String numReplies;
            List<String> postImageThumbs;
            List<String> postImageFull;

            // Start filtering
            postOp = postElement.select("[class*=post op]").first();
            imageFiles = postElement.getElementsByClass("files").first();

            singleFile = imageFiles.select("[class=file");
            multiFiles = imageFiles.select("[class=file multifile");

            // Read through op post and get information
            postLink = postOp.getElementsByClass("post_no").first();
            postNumber = postLink.attr("id").replace("post_no_", "");
            userName = postOp.getElementsByClass("name").first().text();
            postDate = postOp.select("time").first().text();
            postText = postOp.getElementsByClass("body").first().html();

            subjects = postOp.getElementsByClass("subject");
            if(!subjects.isEmpty()) {
                postTopic = subjects.first().text();
            }
            else {
                postTopic = "";
            }

            omitted = postOp.getElementsByClass("omitted");
            if(!omitted.isEmpty()) {
                numReplies = omitted.first().text().replaceAll("Click reply to view.", "");
            }
            else {
                numReplies = "";
            }

            // Get images and thumbnails into arrays
            postImageThumbs = new ArrayList<String>();
            postImageFull = new ArrayList<String>();

            if (!singleFile.isEmpty()) {
                Element image = singleFile.first();
                String imageUrl = image.select("a").first().attr("href");
                String imageThumbnail = image.select("img").first().attr("src");

                postImageThumbs.add(imageThumbnail);
                postImageFull.add(imageUrl);
            } else if (!multiFiles.isEmpty()) {
                for(Element image : multiFiles) {
                    String imageUrl = image.select("a").first().attr("href");
                    String imageThumbnail = image.select("img").first().attr("src");

                    postImageThumbs.add(imageThumbnail);
                    postImageFull.add(imageUrl);
                }
            }

            // Create new instance of post with elements
            opPost = new PostView(getActivity());
            opPost.setUpPost(userName, postDate, postNumber, postTopic, postText, numReplies,
                    postImageThumbs, postImageFull, true);

            return opPost;
        }
    }
}
