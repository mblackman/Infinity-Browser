/*
 * Infinity Browser 2014  Matt Blackman
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


package blackman.matt.board;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.List;

import blackman.matt.utils.ImageLongPressDialog;
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
    private replyClickListener mPostReplyClicked;

    public interface replyClickListener {
        public void gotoPost(int position);
    }

    /**
     * Public constructor to handle taking in the list of views.
     * @param context Context of the caller.
     */
    public PostArrayAdapter(Context context) {
        mContext = context;

    }

    public void updatePosts(List<Post> posts, Board.OnReplyClickedListener listener,
                            replyClickListener replyListener) {
        mPosts = posts;
        mListener = listener;
        mPostReplyClicked = replyListener;
        notifyDataSetChanged();
    }

    public Boolean gotoPost(String postNo) {
        int i = 0;
        for (Post post : mPosts) {
            if (post.postNo.equals(postNo)) {
                mPostReplyClicked.gotoPost(i);
                return true;
            }
            i++;
        }
        return false;
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
        return Long.parseLong(mPosts.get(position).postNo);
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
            holder.postLayout = (LinearLayout) convertView.findViewById(R.id.ll_post_body);
            holder.menu = (ImageButton) convertView.findViewById(R.id.btn_post_menu);
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

        // Add replies to post
        if(post.repliedBy.size() > 0) {
            holder.menu.setBackgroundColor(mContext.getResources().getColor(R.color.post_menu_active));
        } else {
            holder.menu.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                popup.getMenuInflater().inflate(R.menu.post_dropdown_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_replies:
                               new AlertDialog.Builder(mContext).setTitle("Replies")
                                        .setAdapter(new ArrayAdapter<String>(mContext,
                                                        android.R.layout.simple_list_item_1,
                                                        post.repliedBy),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        gotoPost(post.repliedBy.get(which));
                                                    }
                                                }
                                        ).create().show();
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        // Set up reply button
        if(!post.numReplies.equals("")) {
            if(!post.numReplies.equals("0")) {
                holder.replies.setText("Post has " + post.numReplies + " replies");
            } else {
                holder.replies.setText("Post has no replies :'(");
            }
            View.OnClickListener replyClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReplyClicked(post.rootBoard, post.postNo);
                }
            };
            convertView.setOnClickListener(replyClick);
            holder.postBody.setOnClickListener(replyClick);
            holder.replies.setOnClickListener(replyClick);
        } else {
            holder.replies.setVisibility(View.GONE);
        }

        // Set up image button
        if(!post.images.isEmpty()) {
            holder.filename.setText(post.images.get(0).getFileInfo());
            holder.filename.setVisibility(View.VISIBLE);

            holder.image.setImageBitmap(null);
            String imageUrl;
            ImageAware imageAware = new ImageViewAware(holder.image, false);

            if(post.isThumbnail) {
                imageUrl = post.images.get(0).getThumbnailUrl();
                holder.postLayout.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                imageUrl = post.images.get(0).getFullUrl();
                holder.postLayout.setOrientation(LinearLayout.VERTICAL);
            }

            holder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new ImageLongPressDialog(mContext, post.images.get(0));
                    return true;
                }
            });

            ImageLoader.getInstance().displayImage(imageUrl, imageAware,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.progressImage.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                                    FailReason failReason) {
                            holder.progressImage.setVisibility(View.GONE);
                            Drawable error = mContext.getResources()
                                    .getDrawable(R.drawable.deadico);
                            view.setVisibility(View.VISIBLE);
                            ((ImageButton) view).setImageDrawable(error);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view,
                                                      Bitmap loadedImage) {
                            holder.progressImage.setVisibility(View.GONE);
                            view.setVisibility(View.VISIBLE);
                        }
                    });
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ViewHolder myHolder = (ViewHolder) v.getTag();
                    final Post myPost = getItem(position);
                    ImageAware imageAware = new ImageViewAware(myHolder.image, false);
                    String imageUrl;

                    if(myPost.isThumbnail) {
                        myPost.isThumbnail = false;
                        imageUrl = post.images.get(0).getFullUrl();

                    } else {
                        myPost.isThumbnail = true;
                        imageUrl = post.images.get(0).getThumbnailUrl();
                    }
                    ImageLoader.getInstance().displayImage(imageUrl, imageAware,
                            new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                    view.setVisibility(View.GONE);
                                    myHolder.progressImage.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view,
                                                            FailReason failReason) {
                                    myHolder.progressImage.setVisibility(View.GONE);
                                    Drawable error = mContext.getResources()
                                            .getDrawable(R.drawable.deadico);
                                    view.setVisibility(View.VISIBLE);
                                    ((ImageButton) view).setImageDrawable(error);
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view,
                                                              Bitmap loadedImage) {
                                    myHolder.progressImage.setVisibility(View.GONE);
                                    ((ImageButton) view).setImageBitmap(loadedImage);
                                    view.setVisibility(View.VISIBLE);
                                    if(!myPost.isThumbnail) {
                                        myHolder.postLayout.setOrientation(LinearLayout.VERTICAL);

                                    } else {
                                        myHolder.postLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    }
                                }
                            });
                }
            });
        } else {
            holder.image.setVisibility(View.GONE);
            holder.filename.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged () {
        super.notifyDataSetChanged();

        // Start from last post and go up
        for(int i = mPosts.size() - 1; i >= 0; i--) {
            Post replyPost = mPosts.get(i);
            for(String replied : replyPost.repliedTo) {
                for(int j = i; j >= 0; j--) {
                    Post post = mPosts.get(j);
                    if(post.postNo.equals(replied) && !post.repliedBy.contains(replyPost.postNo)) {
                        post.repliedBy.add(replyPost.postNo);
                        break;
                    }
                }
            }
        }
    }

    static class ViewHolder {
        ImageButton image, menu;
        TextView username, postDate, postNo, topic, postBody, replies, filename;
        LinearLayout postLayout;
        ProgressBar progressImage;
    }
}
