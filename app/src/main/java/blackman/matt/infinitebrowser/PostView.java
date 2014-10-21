package blackman.matt.infinitebrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class PostView extends RelativeLayout {
    private ImageButton mImage;
    private TextView mUserNameTextView;
    private TextView mPostDateTextView;
    private TextView mPostNumberTextView;
    private TextView mTopicTextView;
    private TextView mPostTextView;
    private TextView mNumberReplies;

    private String mPostImageThumb;
    private String mPostImageFull;
    private Boolean isThumbnail;

    public PostView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.post_view, this);

        mImage = (ImageButton) findViewById(R.id.post_thumbnail);
        mUserNameTextView = (TextView) findViewById(R.id.tv_username);
        mTopicTextView = (TextView) findViewById(R.id.tv_topic);
        mPostDateTextView = (TextView) findViewById(R.id.tv_datetime);
        mPostNumberTextView = (TextView) findViewById(R.id.tv_postno);
        mPostTextView = (TextView) findViewById(R.id.tv_postText);
        mNumberReplies = (TextView) findViewById(R.id.tv_number_replies);

        addListenerOnButton();
    }

    public void setUpPost(String userName, String postDate, String postNumber, String topic,
                          String postText, String numReplies, List<String> imageThumbs,
                          List<String> imageFull, boolean isCondensed) {
        mUserNameTextView.setText(userName);
        mTopicTextView.setText(topic);
        mPostDateTextView.setText(postDate);
        mPostNumberTextView.setText("No." + postNumber);
        mPostTextView.setText(Html.fromHtml(postText));
        mNumberReplies.setText(numReplies);

        if(!imageFull.isEmpty()) {
            mPostImageFull = "http://8chan.co/" + imageFull.get(0);
        }
        if(!imageThumbs.isEmpty()) {
            mPostImageThumb = "http://8chan.co/" + imageThumbs.get(0);
        }

        isThumbnail = true;
        new postImage().execute(mPostImageThumb);

        invalidate();
        requestLayout();

        int lineCount = mPostTextView.getLineCount();
        if(isCondensed && lineCount >= 10) {
            String[] newText = postText.split(System.getProperty("line.separator"));

            for(String line : newText) {

            }
        }
    }

    public void addListenerOnButton() {
        mImage = (ImageButton) findViewById(R.id.post_thumbnail);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View btn) {
                // Swap big and little pick + swap settings
                if(isThumbnail) { // Little Mode -> Big mode
                    mImage.setMaxWidth(Integer.MAX_VALUE); // A big number
                    new postImage().execute(mPostImageFull);
                    isThumbnail = Boolean.FALSE;
                }
                else { // Big mode -> Little Mode
                    mImage.setMaxWidth(getResources().getDimensionPixelOffset(
                            R.dimen.post_bar_image_size_small));
                    new postImage().execute(mPostImageThumb);
                    isThumbnail = Boolean.TRUE;
                }
            }
        });
    }

    public class postImage extends AsyncTask<String, Void, Bitmap> {
        public postImage() {

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
                mImage.setVisibility(View.VISIBLE);
                mImage.setImageBitmap(img);
                LinearLayout postInfoView = (LinearLayout) findViewById(R.id.ll_post_layout);
                if(isThumbnail) {
                    postInfoView.setOrientation(LinearLayout.HORIZONTAL);
                }
                else {
                    postInfoView.setOrientation(LinearLayout.VERTICAL);
                }
            }
            else {
                mImage.setVisibility(View.GONE);
            }
        }
    }
}
