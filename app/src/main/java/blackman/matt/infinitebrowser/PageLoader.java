package blackman.matt.infinitebrowser;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes in a list of urls and gets the html from them. Then it takes the html doc and
 * turns it into post cards to display.
 * This assumes you send it a link and doesn't check for null.
 */
public class PageLoader extends AsyncTask<String, Void, Document> {
    private Context mContext;
    private View mParent;
    private ProgressBar mProgress;
    private TextView mProgressText;

    public PageLoader(Context context, View parent) {
        mContext = context;
        mParent = parent;
        mProgress = (ProgressBar) mParent.findViewById(R.id.progress_board_load);
        mProgressText = (TextView) mParent.findViewById(R.id.tv_loading_page);
    }

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
        ListView postView;
        Elements threads;

        postView = (ListView) mParent.findViewById(R.id.lv_board_posts);
        //postView.removeAllViews();

        // Gets all the parent posts on page
        threads = html.select("[id*=thread_]");

        // Looks through all the master posts
        for (Element thread : threads) {
            // Create main elements and post
            Elements postReplies;
            PostView opPost;

            try {
                opPost = createPostOP(thread);
                postView.addFooterView(opPost);
            }
            catch (Exception e) {
                TextView errorView;
                errorView = new TextView(mContext);
                errorView.setText(e.toString());
                if(errorView != null) {
                    postView.addFooterView(errorView);
                }
            }

            postReplies = thread.getElementsByClass("post reply");

            // Looks through all the replies to an OP post
            for (Element postReply : postReplies) {

            }
        }
        postView.invalidate();
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
        opPost = new PostView(mContext);
        opPost.setUpPost(userName, postDate, postNumber, postTopic, postText, numReplies,
                postImageThumbs, postImageFull, true);

        return opPost;
    }
}
