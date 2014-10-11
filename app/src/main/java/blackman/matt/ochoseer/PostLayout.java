package blackman.matt.ochoseer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostLayout.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostLayout#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PostLayout extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_URL = "post_url";
    private static final String ARG_USERNAME = "post_username";
    private static final String ARG_POSTDATE = "post_post_date";
    private static final String ARG_POSTNUMBER = "post_post_number";
    private static final String ARG_TOPIC = "post_topic";
    private static final String ARG_POSTTEXT = "post_text";
    private static final String ARG_IMAGETHUMBS = "post_image_file_thumbs";
    private static final String ARG_IMAGEFULL = "post_image_file_full_size";
    private static final String ARG_POSTREPLIES = "post_replies_ids";

    private String url;
    private String username;
    private String postdate;
    private String postNumber;
    private String topic;
    private String postText;
    private String[] imageThumbs;
    private String[] imageFull;
    private String[] postReplies;

    private Boolean isThumbnail;
    private ImageButton postImageButton;

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types and number of parameters
    public static PostLayout newInstance(String url, String username, String postdate,
                                         String postNumber, String topic, String postText,
                                         String[]imageThumbs, String[] imageFull,
                                         String[] postReplies) {
        PostLayout fragment = new PostLayout();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_POSTDATE, postdate);
        args.putString(ARG_POSTNUMBER, postNumber);
        args.putString(ARG_TOPIC, topic);
        args.putString(ARG_POSTTEXT, postText);
        args.putStringArray(ARG_IMAGETHUMBS, imageThumbs);
        args.putStringArray(ARG_IMAGEFULL, imageFull);
        args.putStringArray(ARG_POSTREPLIES, postReplies);
        fragment.setArguments(args);
        return fragment;
    }
    public PostLayout() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            url = getArguments().getString(ARG_URL);
            username = getArguments().getString(ARG_USERNAME);
            postdate = getArguments().getString(ARG_POSTDATE);
            postNumber = getArguments().getString(ARG_POSTNUMBER);
            topic = getArguments().getString(ARG_TOPIC);
            postText = getArguments().getString(ARG_POSTTEXT);
            imageThumbs = getArguments().getStringArray(ARG_IMAGETHUMBS);
            imageFull = getArguments().getStringArray(ARG_IMAGEFULL);
            postReplies = getArguments().getStringArray(ARG_POSTREPLIES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myInflatedView = inflater.inflate(R.layout.fragment_post_layout, container, false);

        // Setup
        isThumbnail = Boolean.TRUE;
        addListenerOnButton(myInflatedView);

        // Init variables
        TextView ttopic = (TextView) myInflatedView.findViewById(R.id.tv_topic);
        TextView tuser = (TextView) myInflatedView.findViewById(R.id.tv_username);
        TextView tdate = (TextView) myInflatedView.findViewById(R.id.tv_datetime);
        TextView tpostno = (TextView) myInflatedView.findViewById(R.id.tv_postno);
        TextView tposttext = (TextView) myInflatedView.findViewById(R.id.tv_posttext);

        ttopic.setText(topic);
        tuser.setText(username);
        tdate.setText(postdate);
        tpostno.setText("No." + postNumber);
        tposttext.setText(postText);

        if(imageThumbs.length > 0) {
            String imgURL = "http://8chan.co" + imageThumbs[0];
            new postImage(postImageButton).execute(imgURL);
        }
        return myInflatedView;
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

    public void addListenerOnButton(View curView) {
        postImageButton = (ImageButton) curView.findViewById(R.id.post_thumbnail);

        postImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View btn) {
                // Swap big and little pick + swap settings
                if(isThumbnail) {
                    String oldUrl = btn.toString();
                    String imgURL = "http://8chan.co" + imageThumbs[0];
                    new postImage((ImageButton) btn).execute(imgURL);
                }
                else {

                }
            }
        });
    }

    public class postImage extends AsyncTask<String, Void, Bitmap> {
        private ImageButton imb;

        public postImage(ImageButton imgbutton) {
            this.imb = imgbutton;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap img = null;
            try {
                URL url = new URL(urls[0]);
                img = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return img;
        }

        @Override
        protected void onPostExecute(Bitmap img) {
            if(img != null) {
                imb.setVisibility(View.VISIBLE);
                imb.setImageBitmap(img);
            }
            else {
                imb.setVisibility(View.GONE);
            }
        }
    }
}
