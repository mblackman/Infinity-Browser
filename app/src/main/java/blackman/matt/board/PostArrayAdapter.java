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
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.List;

import blackman.matt.infinitebrowser.R;

/**
 * A custom adapter to handle loading posts on a board page. Sets up the views and recycles them.
 *
 * Created by Matt on 10/26/2014.
 */
class PostArrayAdapter extends BaseAdapter {
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
        return mPosts != null ? mPosts.size() : 0;
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
        final Post post = getItem(position);
        final ViewHolder holder;
        //int maxWidth = mContext.getResources().getInteger(R.integer.post_thumbnail_size);
        //final ImageSize targetSize = new ImageSize(maxWidth, maxWidth);

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.post_view, parent, false);

            holder = new ViewHolder();

            holder.image = (ImageButton) convertView.findViewById(R.id.post_thumbnail);
            holder.filename = (TextView) convertView.findViewById(R.id.tv_post_image_filename);
            holder.username = (TextView) convertView.findViewById(R.id.tv_username);
            holder.postDate = (TextView) convertView.findViewById(R.id.tv_datetime);
            holder.postNo = (TextView) convertView.findViewById(R.id.tv_postno);
            holder.topic = (TextView) convertView.findViewById(R.id.tv_topic);
            holder.postBody = (TextView) convertView.findViewById(R.id.tv_postText);
            holder.replies = (TextView) convertView.findViewById(R.id.tv_number_replies);
            holder.layout = (LinearLayout) convertView.findViewById(R.id.ll_post_body);
            holder.progressImage = (ProgressBar) convertView.findViewById(R.id.progress_post_image);

            holder.replies.setTag(holder);
            holder.image.setTag(holder);

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
            if(post.numReplies.equals("")) {
                holder.replies.setText("Click to reply >>>");
            } else {
                holder.replies.setText(post.numReplies + " >>>");
            }
            holder.replies.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReplyClicked(post.boardLink, post.postNo);
                }
            });
        } else {
            holder.replies.setVisibility(View.GONE);
        }

        // Set up image button
        if(post.hasImages) {
            holder.filename.setText(post.fileNames.get(0) + " " + post.fileNumbers.get(0));
            holder.filename.setVisibility(View.VISIBLE);

            holder.image.setImageBitmap(null);

            if(post.isThumbnail) {
                ImageAware imageAware = new ImageViewAware(holder.image, false);
                ImageLoader.getInstance().displayImage(post.thumbURLS.get(0), imageAware,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                holder.progressImage.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view,
                                                        FailReason failReason) {
                                holder.progressImage.setVisibility(View.GONE);
                                Drawable error = mContext.getResources().getDrawable(R.drawable.deadico);
                                view.setVisibility(View.VISIBLE);
                                ((ImageButton) view).setImageDrawable(error);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view,
                                                          Bitmap loadedImage) {
                                holder.progressImage.setVisibility(View.GONE);
                                holder.image.setVisibility(View.VISIBLE);
                            }
                        });
                holder.layout.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                ImageAware imageAware = new ImageViewAware(holder.image, false);
                ImageLoader.getInstance().displayImage(post.fullURLS.get(0), imageAware,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                holder.progressImage.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view,
                                                        FailReason failReason) {
                                holder.progressImage.setVisibility(View.GONE);
                                Drawable error = mContext.getResources().getDrawable(R.drawable.deadico);
                                view.setVisibility(View.VISIBLE);
                                ((ImageButton) view).setImageDrawable(error);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view,
                                                          Bitmap loadedImage) {
                                holder.progressImage.setVisibility(View.GONE);
                                holder.image.setVisibility(View.VISIBLE);
                            }
                });
                holder.layout.setOrientation(LinearLayout.VERTICAL);
            }
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ViewHolder myHolder = (ViewHolder) v.getTag();
                    final Post myPost = getItem(position);
                    // If it is on big picture
                    if(myHolder.layout.getOrientation() == LinearLayout.VERTICAL) {
                        String url = myPost.thumbURLS.get(0);
                        myPost.isThumbnail = true;
                        ImageLoader.getInstance().displayImage(url, myHolder.image,
                                new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {
                                        myHolder.image.setVisibility(View.GONE);
                                        myHolder.progressImage.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view,
                                                                FailReason failReason) {
                                        myHolder.progressImage.setVisibility(View.GONE);
                                        Drawable error = mContext.getResources().getDrawable(R.drawable.deadico);
                                        view.setVisibility(View.VISIBLE);
                                        ((ImageButton) view).setImageDrawable(error);
                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                                                  Bitmap loadedImage) {
                                        myHolder.progressImage.setVisibility(View.GONE);
                                        ((ImageButton) view).setImageBitmap(loadedImage);
                                        myHolder.layout.setOrientation(LinearLayout.HORIZONTAL);
                                        myHolder.image.setVisibility(View.VISIBLE);
                                    }
                                });
                    } else {
                        String url = myPost.fullURLS.get(0);
                        myPost.isThumbnail = false;
                        ImageLoader.getInstance().displayImage(url, myHolder.image,
                                new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {
                                        myHolder.image.setVisibility(View.GONE);
                                        myHolder.progressImage.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view,
                                                                FailReason failReason) {
                                        myHolder.progressImage.setVisibility(View.GONE);
                                        Drawable error = mContext.getResources().getDrawable(R.drawable.deadico);
                                        view.setVisibility(View.VISIBLE);
                                        ((ImageButton) view).setImageDrawable(error);
                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                                                  Bitmap loadedImage) {
                                        myHolder.progressImage.setVisibility(View.GONE);
                                        ((ImageButton) view).setImageBitmap(loadedImage);
                                        myHolder.layout.setOrientation(LinearLayout.VERTICAL);
                                        myHolder.image.setVisibility(View.VISIBLE);
                                    }
                                });
                    }
                }
            });
            holder.image.setVisibility(View.VISIBLE);
        } else {
            holder.image.setVisibility(View.GONE);
            holder.filename.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageButton image;
        TextView username, postDate, postNo, topic, postBody, replies, filename;
        LinearLayout layout;
        ProgressBar progressImage;
    }
}
