package blackman.matt.infinitebrowser;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Board.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Board#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class Board extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BOARD_LINK = "boardlink";

    // TODO: Rename and change types of parameters
    private String mBoardLink;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param boardLink Parameter 1.
     * @return A new instance of fragment Board.
     */
    // TODO: Rename and change types and number of parameters
    public static Board newInstance(String boardLink) {
        Board fragment = new Board();
        Bundle args = new Bundle();
        args.putString(ARG_BOARD_LINK, boardLink);
        fragment.setArguments(args);
        return fragment;
    }
    public Board() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBoardLink = getArguments().getString(ARG_BOARD_LINK);
        }
    }

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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

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
            Document ochPage = null;
            String url = urls[0];

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
            // Get the fragment manager
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Gets all the parent posts on page
            Elements threads = html.select("[id*=thread_]");

            // Looks through all the master posts
            for (Element thread : threads) {
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
                String postText = postOp.getElementsByClass("body").first().html();
                Integer MAXIMAGES = 10;

                // Work with data as needed
                if (postOp.getElementsByClass("topic").size() > 0) {
                    postTopic = postOp.getElementsByClass("topic").first().text();
                }

                // Get images and thumbnails into arrays
                String[] postImageThumbs = new String[MAXIMAGES];
                String[] postImageFull = new String[MAXIMAGES];
                String[] postRepliedToPost = new String[500];
                Elements singleFile = imageFiles.select("[class=file");
                Elements multiFiles = imageFiles.select("[class=file multifile");

                if (!singleFile.isEmpty()) {
                    Element image = singleFile.first();
                    String imageUrl = image.select("a").first().attr("href");
                    String imageThumbnail = image.select("img").first().attr("src");

                    postImageThumbs[0] = imageThumbnail;
                    postImageFull[0] = imageUrl;
                } else if (!multiFiles.isEmpty()) {

                }

                // Create new instance of post with elements
                opPost = PostLayout.newInstance(postUrl, userName, postDate, postNumber, postTopic, postText,
                        postImageThumbs, postImageFull, postRepliedToPost);

                // Add new fragment to browser activity
                fragmentTransaction.add(R.id.posts_view, opPost, postNumber);

                // Looks through all the replies to an OP post
                for (Element postReply : postReplies) {

                }
            }
            // Commit posts
            fragmentTransaction.commit();
        }
    }
}
