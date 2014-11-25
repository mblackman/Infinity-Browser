package blackman.matt.catalog;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import blackman.matt.board.ImageFile;
import blackman.matt.board.Post;

/**
 * Created by Matt on 11/24/2014.
 */
public class CatalogLoader  extends AsyncTask<URL, Void, Boolean> {
    private List<Post> mPosts;
    private String mRootBoard;

    private List<String> postIds = new ArrayList<String>();

    private static final String postNo = "no";
    private static final String postSubject = "sub";
    private static final String postComment = "com";
    private static final String postReplies = "replies";
    private static final String postOmittedReplies = "omitted_posts";
    private static final String postOmittedImages = "omitted_images";
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

    private CatalogLoadedNotifier mNotifier;

    public interface CatalogLoadedNotifier {
        public void pageLoaded();
        public void addPost(Post post);
    }

    public CatalogLoader(List<Post> posts) {
        mPosts = posts;
    }

    public void setNotifier(CatalogLoadedNotifier loaded) {
        mNotifier = loaded;
    }

    /**
     * Shows the progress bar and its text to the user.
     */
    @Override
    protected void onPreExecute() {
    }

    /**
     * Reads in urls sent by user to download the html from.
     *
     * @param urls the urls sent by the user.
     * @return returns the html document
     */
    @Override
    protected Boolean doInBackground(URL... urls) {
        JSONArray ochPage = null;
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
            ochPage = new JSONArray(str.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (ochPage != null) {
            try {
                for (int i = 0; i < ochPage.length(); i++) {
                    JSONArray threadsArray = ochPage.getJSONObject(i).getJSONArray("threads");
                    for (int j = 0; j < threadsArray.length(); j++) {
                        mNotifier.addPost(createPost(threadsArray.getJSONObject(j)));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pageLoaded = true;
        } else {
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
        mNotifier.pageLoaded();
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
                        postJson.optInt(postFileWidth),
                        postJson.optInt(postFileHeight),
                        postJson.optInt(postFileThumbWidth),
                        postJson.optInt(postFileThumbHeight),
                        postJson.getInt(postFileSize)));
            }

            if(postJson.has("extra_files")) {
                JSONArray multiFiles = postJson.getJSONArray("extra_files");
                for (int i = 0; i < multiFiles.length(); i++) {
                    JSONObject imageJson = multiFiles.getJSONObject(i);
                    images.add(new ImageFile(mRootBoard,
                            imageJson.getString(postFileName),
                            imageJson.getString(postFileExt),
                            imageJson.getString(postFileSiteName),
                            imageJson.optInt(postFileWidth),
                            imageJson.optInt(postFileHeight),
                            imageJson.optInt(postFileThumbWidth),
                            imageJson.optInt(postFileThumbHeight),
                            imageJson.getInt(postFileSize)));
                }
            }

            opPost = new Post(postJson.optString(postName),
                    postJson.getString(postTime),
                    postJson.getString(postNo),
                    postJson.optString(postSubject),
                    postJson.optString(postComment),
                    postJson.optString(postReplies),
                    postJson.optString(postOmittedReplies),
                    postJson.optString(postOmittedImages),
                    images,
                    mRootBoard);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return opPost;
    }
}
