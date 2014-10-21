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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;


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

                createPostOP(thread, postView);

                postReplies = thread.getElementsByClass("post reply");

                // Looks through all the replies to an OP post
                for (Element postReply : postReplies) {

                }
            }
        }

        private void createPostOP(Element postElement, LinearLayout postView) {
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

            postView.addView(opPost);
        }
    }
}
