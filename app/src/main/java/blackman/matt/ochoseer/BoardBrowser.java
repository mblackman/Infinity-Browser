package blackman.matt.ochoseer;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class BoardBrowser extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, PostLayout.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_browser);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        String[] boardList;
        String boardUrl;

        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.shared_preference_file), this.MODE_PRIVATE);

        boardList = sharedPref.getString(getString(R.string.PREF_USER_BOARD_LIST), "").split(" ");

        if (number <= boardList.length) {
            mTitle = "/" + boardList[number - 1] + "/";
        }

        if (!mTitle.equals(getString(R.string.app_name))) {
            boardUrl = "http://8chan.co/" + mTitle.toString().replace("/", "");
            new OchBoard().execute(boardUrl);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.board_browser, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_board_browser, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((BoardBrowser) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public class OchBoard extends AsyncTask<String, Void, Document> {
        @Override
        protected Document doInBackground(String... urls) {
            Document ochPage = null;
            String url = urls[0];

            try {
                ochPage = Jsoup.connect(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ochPage;
        }

        @Override
        protected void onPostExecute(Document html) {
            // Get the fragment manager
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Gets all the parent posts on page
            Elements threads = html.select("[id*=thread_]");

            // Looks through all the master posts
            for(Element thread : threads) {
                // Create main elements and post
                PostLayout opPost;
                Element postOp = thread.select("[class*=post op]").first();
                Element imageFiles = thread.getElementsByClass("files").first();
                Elements postReplies = thread.getElementsByClass("post reply");

                // Read through op post and get information
                Element postLink = postOp.getElementsByClass("post_no").first();
                String postUrl = postLink.attr("href");
                String postNumber = postLink.attr("id").replace("post_no_", "");
                String userName = postOp.getElementsByClass("name").first().text();
                String postDate = postOp.select("time").first().text();
                String postTopic = null;
                String postText = postOp.getElementsByClass("body").first().text();
                Integer MAXIMAGES = getResources().getInteger(R.integer.max_images_per_post);

                // Work with data as needed
                if(postOp.getElementsByClass("topic").size() > 0) {
                    postTopic = postOp.getElementsByClass("topic").first().text();
                }

                // Get images and thumbnails into arrays
                String[] postImageThumbs = new String[MAXIMAGES];
                String[] postImageFull = new String[MAXIMAGES];
                String[] postRepliedToPost = new String[500];
                Elements singleFile = imageFiles.select("[class=file");
                Elements multiFiles = imageFiles.select("[class=file multifile");

                if(!singleFile.isEmpty()) {
                    Element image = singleFile.first();
                    String imageUrl = image.select("a").first().attr("href");
                    String imageThumbnail = image.select("img").first().attr("src");

                    postImageThumbs[0] = imageThumbnail;
                    postImageFull[0] = imageUrl;
                }
                else if(!multiFiles.isEmpty()) {

                }

                // Create new instance of post with elements
                opPost = PostLayout.newInstance(postUrl, userName, postDate, postNumber, postTopic, postText,
                        postImageThumbs, postImageFull, postRepliedToPost);

                // Add new fragment to browser activity
                fragmentTransaction.add(R.id.posts_view, opPost, postNumber.toString());

                // Looks through all the replies to an OP post
                for(Element postReply : postReplies) {

                }
            }
            // Commit posts
            fragmentTransaction.commit();
        }
    }

}
