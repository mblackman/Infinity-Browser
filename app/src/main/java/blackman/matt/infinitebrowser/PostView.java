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

/**
 * A view for displaying a post on a board. Has built in variables to handle how the
 * post should appear depending on its context.
 */
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

    /**
     * Public constructor used to get the context of the view being created.
     *
     * @param context Context of the parent to this view.
     */
    public PostView(Context context) {
        super(context);
        init();
    }

    /**
     * Initialization of the views basic components.
     * This is used for a general case, if multiple constructors are used.
     */
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

    /**
     * Used to set the values of the card from whoever is creating it.
     *
     * @param userName Posters username.
     * @param postDate Date the post was made.
     * @param postNumber Number of the post on the board.
     * @param topic The topic the user has posted.
     * @param postText Body of the post the user made.
     * @param numReplies Replied string for long threads from site.
     * @param imageThumbs Container of links to image thumbnails.
     * @param imageFull Container of links to full sized images.
     * @param isCondensed If the post text should be condensed.
     */
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

    /**
     * Adds a listener to the image button for the posts images.
     */
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

    /**
     * Used to open a bit-stream to a image from the site to load on demand.
     */
    public class postImage extends AsyncTask<String, Void, Bitmap> {
        /**
         * Empty constructor.
         */
        public postImage() {

        }

        /**
         * Opens a bit stream and returns the image as a bitmap.
         *
         * @param urls All the urls of the images. Probably only 1 used.
         * @return A bitmap of the image.
         */
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

        /**
         * Sets the bitmap on the image button and adjusts the view.
         *
         * @param img The bitmap to set the image button to.
         */
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
