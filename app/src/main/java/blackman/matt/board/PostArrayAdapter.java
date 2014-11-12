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
import android.graphics.Bitmap;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.List;

import blackman.matt.infinitebrowser.R;

/**
 * A custom adapter to handle loading posts on a board page. Sets up the views and recycles them.
 *
 * Created by Matt on 10/26/2014.
 */
public class PostArrayAdapter extends BaseAdapter {
    private List<Post> mPosts = Collections.emptyList();
    private final Context mContext;
    private Board.OnReplyClickedListener mListener;

    /**
     * Public constructor to handle taking in the list of views.
     * @param context Context of the caller.
     */
    public PostArrayAdapter(Context context) {
        mContext = context;
    }

    public void updatePosts(List<Post> posts, Board.OnReplyClickedListener listener) {
        mPosts = posts;
        mListener = listener;
        notifyDataSetChanged();
    }

    /**
     * Gets the number of posts being stored.
     *
     * @return The number of posts.
     */
    @Override
    public int getCount() {
        return mPosts.size();
    }

    /**
     * Gets a post view at a given position.
     *
     * @param position Where to get the post from.
     * @return The post view.
     */
    @Override
    public Post getItem(int position) {
        return mPosts.get(position);
    }

    /**
     * Gets the position.
     *
     * @param position The position
     * @return Returns the position.
     */
    @Override
    public long getItemId(int position) {
        return mPosts.get(position).Id;
    }

    /**
     * Gets a view from the list and returns it.
     *
     * @param position The selected position.
     * @param convertView The view that might be taking the place of the view.
     * @param parent The parent view group with all the stuffs.
     * @return The view you will see.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Post post = getItem(position);
        int maxWidth = mContext.getResources().getInteger(R.integer.post_thumbnail_size);
        final ImageSize targetSize = new ImageSize(maxWidth, maxWidth);

        ImageButton thumbnail, fullSize;
        TextView username, postDate, postNo, topic, postBodySmall, postBodyFull, replies, filename;
        ViewSwitcher switcher;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.post_view, parent, false);
        }

        thumbnail = ViewHolder.get(convertView, R.id.post_thumbnail);
        fullSize = ViewHolder.get(convertView, R.id.post_full_image);
        username = ViewHolder.get(convertView, R.id.tv_username);
        postNo = ViewHolder.get(convertView, R.id.tv_postno);
        postDate = ViewHolder.get(convertView, R.id.tv_datetime);
        topic = ViewHolder.get(convertView, R.id.tv_topic);
        postBodySmall = ViewHolder.get(convertView, R.id.tv_postText);
        postBodyFull = ViewHolder.get(convertView, R.id.tv_postText_full);
        replies = ViewHolder.get(convertView, R.id.tv_number_replies);
        filename = ViewHolder.get(convertView, R.id.tv_post_image_filename);
        switcher = ViewHolder.get(convertView, R.id.vs_post_body);

        username.setText(post.userName);
        postDate.setText(post.postDate);
        postNo.setText("Post No. " + post.postNo);
        topic.setText(post.topic);
        postBodySmall.setText(Html.fromHtml(post.postBody));
        postBodySmall.setMovementMethod(LinkMovementMethod.getInstance());
        postBodyFull.setText(Html.fromHtml(post.postBody));
        postBodyFull.setMovementMethod(LinkMovementMethod.getInstance());

        // Set up reply button
        if(post.isRootBoard) {
            final String newUrl = post.boardLink + "res/" + post.postNo + ".html";
            replies.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReplyClicked(newUrl);
                }
            });
        } else {
            replies.setVisibility(View.GONE);
        }

        // Set up image button
        if(post.hasImages) {
            filename.setText(post.fileNames.get(0) + " " + post.fileNumbers.get(0));
            filename.setVisibility(View.VISIBLE);

            ImageAware imageAware = new ImageViewAware(thumbnail, false);

            ImageLoader.getInstance().displayImage(post.thumbURLS.get(0), imageAware);

            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton fullImage = ViewHolder.get(v, R.id.post_full_image);

                    ImageLoader.getInstance().displayImage(getItem(position).fullURLS.get(0), fullImage,
                            new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    ViewSwitcher mswitch = ViewHolder.get(view, R.id.vs_post_body);
                                    ((ImageButton) view).setImageBitmap(null);
                                    ((ImageButton) view).setImageBitmap(loadedImage);
                                    view.setVisibility(View.VISIBLE);
                                    mswitch.showNext();
                                }
                            });
                }
            });
            fullSize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton thumb = ViewHolder.get(v, R.id.post_full_image);
                    ImageLoader.getInstance().displayImage(getItem(position).fullURLS.get(0), thumb,
                            new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    ViewSwitcher mswitch = ViewHolder.get(view, R.id.vs_post_body);
                                    ImageButton fullImage = ViewHolder.get(view, R.id.post_full_image);
                                    ((ImageButton) view).setImageBitmap(null);
                                    ((ImageButton) view).setImageBitmap(loadedImage);
                                    fullImage.setVisibility(View.GONE);
                                    mswitch.showNext();
                                }
                            });
                }
            });
        } else {
            //holder.thumbnail.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }
}
