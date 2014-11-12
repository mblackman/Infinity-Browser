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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final Post post = getItem(position);
        final ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.post_view, parent, false);

            holder = new ViewHolder();

            holder.image = (ImageButton) convertView.findViewById(R.id.post_thumbnail);
            holder.filename = (TextView) convertView.findViewById(R.id.tv_post_image_filename);
            holder.username = (TextView) convertView.findViewById(R.id.tv_username);
            holder.postDate = (TextView) convertView.findViewById(R.id.tv_datetime);
            holder.postNo = (TextView) convertView.findViewById(R.id.tv_postno);
            holder.topic = (TextView) convertView.findViewById(R.id.tv_topic);
            holder.postBody = (TextView) convertView.findViewById(R.id.tv_postText);
            holder.replies = (TextView) convertView.findViewById(R.id.tv_number_replies);
            holder.bodyLayout = (LinearLayout) convertView.findViewById(R.id.ll_post_body);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.username.setText(post.userName);
        holder.postDate.setText(post.postDate);
        holder.postNo.setText("Post No. " + post.postNo);
        holder.topic.setText(post.topic);
        holder.postBody.setText(Html.fromHtml(post.postBody));
        holder.postBody.setMovementMethod(LinkMovementMethod.getInstance());

        // Set up reply button
        if(post.isRootBoard) {
            final String newUrl = post.boardLink + "res/" + post.postNo + ".html";
            holder.replies.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReplyClicked(newUrl);
                }
            });
        } else {
            holder.replies.setVisibility(View.GONE);
        }

        // Set up image button
        if(post.hasImages) {
            int maxWidth = mContext.getResources().getInteger(R.integer.post_thumbnail_size);
            final ImageSize targetSize = new ImageSize(maxWidth, maxWidth);

            holder.filename.setText(post.fileNames.get(0) + " " + post.fileNumbers.get(0));
            holder.filename.setVisibility(View.VISIBLE);

            // Load image image
            if(holder.bodyLayout.getOrientation() == LinearLayout.HORIZONTAL) {
                ImageLoader.getInstance().loadImage(post.thumbURLS.get(0), targetSize,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                holder.image.setImageBitmap(loadedImage);
                            }
                        });
            } else {
                ImageLoader.getInstance().loadImage(post.fullURLS.get(0),
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                holder.image.setImageBitmap(loadedImage);
                            }
                        });
            }

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.bodyLayout.getOrientation() == LinearLayout.VERTICAL) {
                        ImageLoader.getInstance().loadImage(post.thumbURLS.get(0), targetSize,
                                new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        holder.image.setImageBitmap(loadedImage);
                                        holder.bodyLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    }
                                });
                    } else {
                        ImageLoader.getInstance().loadImage(post.fullURLS.get(0),
                                new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        holder.image.setImageBitmap(loadedImage);
                                        holder.bodyLayout.setOrientation(LinearLayout.VERTICAL);
                                    }
                                });
                    }
                }
            });
        } else {
            holder.image.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageButton image;
        TextView username, postDate, postNo, topic, postBody, replies, filename;
        LinearLayout bodyLayout;
    }
}
