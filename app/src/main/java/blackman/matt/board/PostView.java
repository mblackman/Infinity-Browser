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
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Board.OnFragmentInteractionListener mListener;

    private String mPostImageThumb;
    private String mPostImageFull;

    /**
     * Public constructor used to get the context of the view being created.
     *
     * @param context Context of the parent to this view.
     */
    public PostView(Context context, Object listener) {
        super(context);
        mListener = (Board.OnFragmentInteractionListener) listener;
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
     * @param onRootBoard If the post text should be condensed.
     */
    public void setUpPost(String userName, String postDate, String postNumber, String topic,
                          String postText, String numReplies, List<String> imageThumbs,
                          List<String> imageFull, String boardLink, boolean onRootBoard) {
        mUserNameTextView.setText(userName);
        mTopicTextView.setText(topic);
        mPostDateTextView.setText(postDate);
        mPostNumberTextView.setText("No." + postNumber);
        if(onRootBoard) {
            setUpReplyButton(boardLink, postNumber);
        } else {
            mReplyView.setVisibility(GONE);
        }
        mPostTextView.setText(Html.fromHtml(formatPostBody(postText)));
        mPostTextView.setMovementMethod(LinkMovementMethod.getInstance());

        if(!imageFull.isEmpty()) {
            mPostImageFull = "http://8chan.co/" + imageFull.get(0);
        }
        if(!imageThumbs.isEmpty()) {
            mPostImageThumb = "http://8chan.co/" + imageThumbs.get(0);
        }

        mImageLoader = new ImageLoader(getContext(), mImage, mPostImageThumb, mPostImageFull);
        mImageLoader.loadThumb();

        invalidate();
        requestLayout();
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
     * Formats the HTML on the post text to accurately display it on the post.
     *
     * @param post The unformatted text of the post.
     * @return A formatted version of the post.
     */
    private String formatPostBody(String post) {
        Document formattedText = Jsoup.parse(post);

        // Red Text
        Elements redTexts = formattedText.select("[class=heading]");
        for(Element text : redTexts) {
            text.wrap("<font color=\"#AF0A0F\"><strong></strong></font>");
        }

        // Board Links
        Elements boardLinks = formattedText.select("a");
        for(Element link : boardLinks) {
            String url = link.attr("href");
            Pattern p = Pattern.compile("^/.*/index\\.html");
            Matcher m = p.matcher(url);
            if(m.matches()) {
                link.attr("href", "http://8chan.co" + url);
            }
        }

        return formattedText.toString();
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
                    mImageLoader.loadFull();
                    postInfoView.setOrientation(LinearLayout.VERTICAL);
                }
                else { // Big mode -> Little Mode
                    mImage.setMaxWidth(getResources().getDimensionPixelOffset(
                            R.dimen.post_bar_image_size_small));
                    mImageLoader.loadThumb();
                    postInfoView.setOrientation(LinearLayout.HORIZONTAL);
                }
            }
        });
    }
}
