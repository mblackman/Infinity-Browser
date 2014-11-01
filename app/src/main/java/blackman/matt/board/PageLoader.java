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
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import blackman.matt.infinitebrowser.R;

/**
 * Takes in a list of urls and gets the html from them. Then it takes the html doc and
 * turns it into post cards to display.
 * This assumes you send it a link and doesn't check for null.
 */
public class PageLoader extends AsyncTask<URL, Void, Document> {
    private final Context mContext;
    private final ProgressBar mProgress;
    private final TextView mProgressText;
    private final List<PostView> mPosts;
    private final PostArrayAdapter mAdapter;
    private final Object mListener;

    private Boolean mIsOnRootPage;
    private String mPageUrl;

    /**
     * Basic constructor to initialize the class.
     *
     * @param context Context of caller.
     * @param parent Parent view who needs a loading.
     * @param posts The posts container.
     * @param adapter Adapter for the list view that holds the posts.
     * @param listener A listener for the activity to interact with the fragment.
     */
    public PageLoader(Activity context, View parent, List<PostView> posts,
                      PostArrayAdapter adapter, Object listener) {
        mContext = context;
        mProgress = (ProgressBar) parent.findViewById(R.id.progress_page_load);
        mProgressText = (TextView) parent.findViewById(R.id.tv_progress_page_load);
        mPosts = posts;
        mAdapter = adapter;
        mListener = listener;
    }

    /**
     * Shows the progress bar and its text to the user.
     */
    @Override
    protected void onPreExecute() {
        mProgress.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
    }

    /**
     * Reads in urls sent by user to download the html from.
     * @param urls the urls sent by the user.
     * @return returns the html document
     */
    @Override
    protected Document doInBackground(URL... urls) {
        Document ochPage = null;
        URL url = urls[0];
        mPageUrl = url.toString();
        mIsOnRootPage = !mPageUrl.contains(".html");

        try {
            ochPage = Jsoup.connect(url.toString()).get();
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
        Elements threads;

        // Gets all the parent posts on page
        threads = html.select("[id*=thread_]");

        // Looks through all the master posts
        for (Element thread : threads) {
            // Create main elements and post
            Elements postReplies;
            PostView opPost;

            try {
                opPost = createPostOP(thread);
                mPosts.add(opPost);
            }
            catch (Exception e) {
                TextView errorView;
                errorView = new TextView(mContext);
                errorView.setText(e.toString());
                if(errorView != null) {
                    // TODO: Handle errors or something
                    //mListView.addFooterView(errorView);
                }
            }

            // If you are in a thread it will load the replies
            if(!mIsOnRootPage) {
                postReplies = thread.select("[class=post reply]");

                // Looks through all the replies to an OP post
                for (Element postReply : postReplies) {
                    PostView replyPost;

                    try {
                        replyPost = createPostReply(postReply);
                        mPosts.add(replyPost);
                    }
                    catch (Exception e) {
                        TextView errorView;
                        errorView = new TextView(mContext);
                        errorView.setText(e.toString());
                        if(errorView != null) {
                            // TODO: Handle errors or something
                            //mListView.addFooterView(errorView);
                        }
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
        mProgress.setVisibility(View.INVISIBLE);
        mProgressText.setVisibility(View.INVISIBLE);
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
        opPost = new PostView(mContext, mListener);
        opPost.setUpPost(userName, postDate, postNumber, postTopic, postText, numReplies,
                postImageThumbs, postImageFull, mPageUrl, mIsOnRootPage);

        return opPost;
    }

    /**
     * Takes information from HTML elements and creates a none OP post of a board
     * or a thread and returns the newly created post.
     *
     * @param postElement the post's HTML elements.
     * @return the newly created post.
     */
    private PostView createPostReply(Element postElement) {
        PostView newPost;
        Elements singleFile;
        Elements multiFiles;
        Element postReply;
        Element imageFiles;
        Element postLink;
        String postNumber;
        String userName;
        String postDate;
        String postText ;
        List<String> postImageThumbs;
        List<String> postImageFull;

        // Start filtering
        postReply = postElement.select("[class*=post reply]").first();
        imageFiles = postElement.getElementsByClass("files").first();

        singleFile = imageFiles.select("[class=file");
        multiFiles = imageFiles.select("[class=file multifile");

        // Read through op post and get information
        postLink = postReply.getElementsByClass("post_no").first();
        postNumber = postLink.attr("id").replace("post_no_", "");
        userName = postReply.getElementsByClass("name").first().text();
        postDate = postReply.select("time").first().text();
        postText = postReply.getElementsByClass("body").first().html();

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
        newPost = new PostView(mContext, mListener);
        newPost.setUpPost(userName, postDate, postNumber, "", postText, "",
                postImageThumbs, postImageFull, mPageUrl, mIsOnRootPage);

        return newPost;
    }
}
