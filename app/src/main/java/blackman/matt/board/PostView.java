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

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import blackman.matt.infinitebrowser.R;

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
    private TextView mReplyView;
    private ImageLoader mImageLoader;

    private Board.OnReplyClickedListener mListener;

    private String mPostImageThumb;
    private String mPostImageFull;

    /**
     * Constructor to take in custom attribute set.
     *
     * @param context Context of the caller.
     * @param attrs Custom attribute set.
     */
    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Public constructor used to get the context of the view being created.
     *
     * @param context Context of the parent to this view.
     * @param listener A On reply clicked listener for the board fragment.
     */
    public PostView(Context context, Board.OnReplyClickedListener listener) {
        super(context);
        mListener = listener;
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
        mReplyView = (TextView) findViewById(R.id.tv_number_replies);
    }

    /**
     * Used to set the values of the card from whoever is creating it.
     *
     * @param post Post used to populate view.
     */
    public void setUpPost(Post post) {
        mUserNameTextView.setText(post.mUserName);
        mTopicTextView.setText(post.mTopic);
        mPostDateTextView.setText(post.mPostDate);
        mPostNumberTextView.setText("No." + post.mPostNo);
        if(post.mIsRootBoard) {
            setUpReplyButton(post.mBoardLink, post.mPostNo);
        } else {
            mReplyView.setVisibility(GONE);
        }
        mPostTextView.setText(Html.fromHtml(post.mPostText));
        mPostTextView.setMovementMethod(LinkMovementMethod.getInstance());
        if(!post.mFullURLS.isEmpty()) {
            mPostImageFull = "http://8chan.co/" + post.mFullURLS.get(0);
        }
        if(!post.mThumbURLS.isEmpty()) {
            mPostImageThumb = "http://8chan.co/" + post.mThumbURLS.get(0);
        }
        if(mPostImageFull != null && mPostImageThumb != null) {
            mImageLoader = new ImageLoader(getContext(), mImage, mPostImageThumb, mPostImageFull);
            mImageLoader.draw();
        }
        addListenerOnButton();
    }

    /**
     * Adds an on click listener to the reply button to open up a new thread.
     *
     * @param boardLink Link of the board the post is on.
     * @param postNo Number of the thread to open.
     */
    private void setUpReplyButton(String boardLink, String postNo) {
        final String newUrl = boardLink + "res/" + postNo + ".html";

        mReplyView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onReplyClicked(newUrl);
            }
        });
    }

    /**
     * Adds a listener to the image button for the posts images.
     */
    public void addListenerOnButton() {
        final LinearLayout postInfoView = (LinearLayout) findViewById(R.id.ll_post_layout);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View btn) {
                // Swap big and little pick + swap settings
                if(mImageLoader.isThumbnail()) { // Little Mode -> Big mode
                    mImage.setMaxWidth(Integer.MAX_VALUE); // A big number
                    mImageLoader.renderThumbnail(false);
                    mImageLoader.draw();
                    postInfoView.setOrientation(LinearLayout.VERTICAL);
                }
                else { // Big mode -> Little Mode
                    mImage.setMaxWidth(getResources().getDimensionPixelOffset(
                            R.dimen.post_bar_image_size_small));
                    mImageLoader.renderThumbnail(true);
                    mImageLoader.draw();
                    postInfoView.setOrientation(LinearLayout.HORIZONTAL);
                }
            }
        });
    }
}
