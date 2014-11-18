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

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private final ProgressBar mProgress;
    private final TextView mProgressText;
    private final List<Post> mPosts;
    private final PostArrayAdapter mAdapter;
    private final Boolean mIsOnRootPage;
    private String mRootBoard;

    private List<String> postIds = new ArrayList<String>();

    private static final String postNo = "no";
    private static final String postSubject = "sub";
    private static final String postComment = "com";
    private static final String postReplies = "replies";
    private static final String postName = "name";
    private static final String postStickied = "sticky";
    private static final String postLocked = "locked";
    private static final String postTime = "time";
    private static final String postLastModified = "last_modified";
    private static final String postFileName = "filename";
    private static final String postFileSiteName = "tim";
    private static final String postFileExt = "ext";
    private static final String postFileSize = "fsize";
    private static final String postFileHeight = "h";
    private static final String postFileWidth = "w";
    private static final String postFileThumbHeight = "tn_h";
    private static final String postFileThumbWidth = "tn_w";

    public PageLoaderResponse mResponse;

    public interface PageLoaderResponse {
        public void setPageLoaded(Boolean isLoaded);

        public void sendErrorMessage(CharSequence error);
    }

    /**
     * Basic constructor to initialize the class.
     *
     * @param parent  Parent view who needs a loading.
     * @param posts   The posts container.
     * @param adapter Adapter for the list view that holds the posts.
     */
    public PageLoader(View parent, List<Post> posts, PostArrayAdapter adapter, Boolean isRootBoard) {
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
     *
     * @param urls the urls sent by the user.
     * @return returns the html document
     */
    @Override
    protected Boolean doInBackground(URL... urls) {
        JSONObject ochPage = null;
        Boolean pageLoaded;
        String pageUrl = urls[0].toString();
        mRootBoard = urls[0].getPath().split("/")[1];

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(pageUrl);
        try {
            HttpResponse response = client.execute(request);

            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            ochPage = new JSONObject(str.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (ochPage != null) {
            if(mIsOnRootPage) {
                try {
                    JSONArray threadsArray = ochPage.getJSONArray("threads");
                    for(int i = 0; i < threadsArray.length(); i++) {
                        mPosts.add(createPost(threadsArray.getJSONObject(i)
                                .getJSONArray("posts").getJSONObject(0)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    JSONArray postsArray = ochPage.getJSONArray("posts");
                    for(int i = 0; i < postsArray.length(); i++) {
                        mPosts.add(createPost(postsArray.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            pageLoaded = true;
        } else {
            CharSequence text = "Error loading page...Try reloading the page.";
            mResponse.sendErrorMessage(text);
            pageLoaded = false;
        }
        return pageLoaded;
    }

    /**
     * After the page is read in, the pages are turned into fragments and are put on the
     * screen.
     */
    @Override
    protected void onPostExecute(Boolean loadSuccess) {
        if (loadSuccess) {
            mResponse.setPageLoaded(true);
        } else {
            mResponse.setPageLoaded(false);
        }
        mAdapter.notifyDataSetChanged();
        mProgress.setVisibility(View.GONE);
        mProgressText.setVisibility(View.GONE);
    }

    /**
     * Takes information from HTML elements and creates the OP post of a board
     * or a thread and returns the newly created post.
     *
     * @param postJson the post's JSON object.
     * @return the newly created post.
     */
    private Post createPost(JSONObject postJson) {
        List<ImageFile> images = new ArrayList<ImageFile>();
        Post opPost = null;
        // Create new instance of post with elements
        try {
            String fileName = postJson.optString(postFileName);
            if(fileName != null && !fileName.equals("")) {
                images.add(new ImageFile(mRootBoard, fileName,
                        postJson.optString(postFileExt),
                        postJson.getString(postFileSiteName),
                        postJson.getInt(postFileWidth),
                        postJson.getInt(postFileHeight),
                        postJson.getInt(postFileThumbWidth),
                        postJson.getInt(postFileThumbHeight)));
            }

            if(postJson.has("extra_files")) {
                JSONArray multiFiles = postJson.getJSONArray("extra_files");
                for (int i = 0; i < multiFiles.length(); i++) {
                    JSONObject imageJson = multiFiles.getJSONObject(i);
                    images.add(new ImageFile(mRootBoard,
                            imageJson.getString(postFileName),
                            imageJson.getString(postFileExt),
                            imageJson.getString(postFileSiteName),
                            imageJson.getInt(postFileWidth),
                            imageJson.getInt(postFileHeight),
                            imageJson.getInt(postFileThumbWidth),
                            imageJson.getInt(postFileThumbHeight)));
                }
            }

            opPost = new Post(postJson.getString(postName),
                    postJson.getString(postTime),
                    postJson.getString(postNo),
                    postJson.optString(postSubject),
                    postJson.optString(postComment),
                    postJson.optString(postReplies),
                    images,
                    mRootBoard);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return opPost;
    }
}
