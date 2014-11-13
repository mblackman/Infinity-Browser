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
import android.util.Log;
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
public class PageLoader extends AsyncTask<URL, Void, Boolean> {
    private final Context mContext;
    private final ProgressBar mProgress;
    private final TextView mProgressText;
    private final List<Post> mPosts;
    private final PostArrayAdapter mAdapter;

    private Boolean mIsOnRootPage;
    private String mPageUrl;

    public PageLoaderResponse mResponse;

    public interface PageLoaderResponse {
        public void setPageLoaded(Boolean isLoaded);
        public void sendErrorMessage(CharSequence error);
    }

    /**
     * Basic constructor to initialize the class.
     *
     * @param context Context of caller.
     * @param parent Parent view who needs a loading.
     * @param posts The posts container.
     * @param adapter Adapter for the list view that holds the posts.
     */
    public PageLoader(Activity context, View parent, List<Post> posts,
                      PostArrayAdapter adapter, Boolean isRootBoard) {
        mContext = context;
        mProgress = (ProgressBar) parent.findViewById(R.id.progress_page_load);
        mProgressText = (TextView) parent.findViewById(R.id.tv_progress_page_load);
        mPosts = posts;
        mAdapter = adapter;
        mIsOnRootPage = isRootBoard;
    }

    /**
     * Shows the progress bar and its text to the user.
     */
    @Override
    protected void onPreExecute() {
        mProgress.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
        mResponse.setPageLoaded(false);
    }

    /**
     * Reads in urls sent by user to download the html from.
     * @param urls the urls sent by the user.
     * @return returns the html document
     */
    @Override
    protected Boolean doInBackground(URL... urls) {
        URL url = urls[0];
        Document ochPage = null;
        Boolean pageLoaded;
        mPageUrl = url.toString();

        try {
            ochPage = Jsoup.connect(url.toString()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(ochPage != null) {
            // Gets all the parent posts on page
            Elements threads = ochPage.select("[class=thread]");

            // Looks through all the master posts
            for (Element thread : threads) {
                // Create main elements and post
                Elements postReplies;
                Post opPost;

                try {
                    opPost = createPost(thread);

                    if(!boardExists(opPost.Id)) {
                        mPosts.add(opPost);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // If you are in a thread it will load the replies
                if (!mIsOnRootPage) {
                    postReplies = thread.select("[class=post reply]");

                    // Looks through all the replies to an OP post
                    for (Element postReply : postReplies) {
                        Post replyPost;

                        try {
                            replyPost = createPost(postReply);

                            if(!boardExists(replyPost.Id)) {
                                mPosts.add(replyPost);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            pageLoaded = true;
        } else {
            CharSequence text = "Error loading page...Try reloading the page.";
            mResponse.sendErrorMessage(text);
            pageLoaded =false;
        }
        return pageLoaded;
    }

    /**
     * After the page is read in, the pages are turned into fragments and are put on the
     * screen.
     */
    @Override
    protected void onPostExecute(Boolean loadSuccess) {
        if(loadSuccess) {
            mResponse.setPageLoaded(true);
        } else {
            mResponse.setPageLoaded(false);
        }
        mAdapter.notifyDataSetChanged();
        mProgress.setVisibility(View.GONE);
        mProgressText.setVisibility(View.GONE);
    }

    private Boolean boardExists(final long newPost) {
        Boolean exists = false;

        for(Post post : mPosts) {
            if(post.Id.equals(newPost)) {
                exists = true;
            }
        }

        return exists;
    }

    /**
     * Takes information from HTML elements and creates the OP post of a board
     * or a thread and returns the newly created post.
     *
     * @param postElement the post's HTML elements.
     * @return the newly created post.
     */
    private Post createPost(Element postElement) {
        Post opPost;
        Elements singleFile;
        Elements multiFiles;
        Elements omitted;
        Elements subjects;
        Element post;
        Element imageFiles;
        Element postLink;
        String postNumber;
        String userName;
        String postDate;
        String postTopic;
        String postText;
        String numReplies;
        List<String> postImageThumbs = new ArrayList<String>();
        List<String> postImageFull = new ArrayList<String>();
        List<String> fileNumbers = new ArrayList<String>();
        List<String> fileInfos = new ArrayList<String>();

        // Start filtering
        post = postElement.select("div[class^=post]").first();

        // Read through op post and get information
        postLink = post.getElementsByClass("post_no").first();
        postNumber = postLink.attr("id").replace("post_no_", "");
        userName = post.getElementsByClass("name").first().text();
        postDate = post.select("time").first().text();
        postText = post.getElementsByClass("body").first().html();

        subjects = post.getElementsByClass("subject");
        if (!subjects.isEmpty()) {
            postTopic = subjects.first().text();
        } else {
            postTopic = "";
        }

        omitted = post.getElementsByClass("omitted");
        if (!omitted.isEmpty()) {
            numReplies = omitted.first().text().replaceAll("Click reply to view.", "");
        } else {
            numReplies = "";
        }

        imageFiles = postElement.getElementsByClass("files").first();

        if(imageFiles.hasText()) {
            singleFile = imageFiles.getElementsByClass("file");
            multiFiles = imageFiles.select("[class=file multifile]");

            if (!multiFiles.isEmpty()) {
                for (Element image : multiFiles) {
                    String imageUrl = image.select("a").first().attr("href");
                    String imageThumbnail = image.select("img").first().attr("src");
                    String fileNumber = image.select("a").first().text();
                    String fileInfo = image.getElementsByClass("unimportant").first().text();

                    postImageThumbs.add(imageThumbnail);
                    postImageFull.add(imageUrl);
                    fileNumbers.add(fileNumber);
                    fileInfos.add(fileInfo);
                }
            } else if (!singleFile.isEmpty()) {
                Element image = singleFile.first();
                String imageUrl = image.select("a").first().attr("href");
                String imageThumbnail = image.select("img").first().attr("src");
                String fileNumber = image.select("a").first().text();
                String fileInfo = image.getElementsByClass("unimportant").first().text();

                postImageThumbs.add(imageThumbnail);
                postImageFull.add(imageUrl);
                fileNumbers.add(fileNumber);
                fileInfos.add(fileInfo);
            }
        }

        // Create new instance of post with elements
        opPost = new Post(userName, postDate, postNumber, postTopic, postText, numReplies,
                postImageThumbs, postImageFull, fileInfos, fileNumbers, mPageUrl, mIsOnRootPage);

        return opPost;
    }
}
