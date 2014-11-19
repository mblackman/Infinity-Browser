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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
        //int maxWidth = mContext.getResources().getInteger(R.integer.post_thumbnail_size);
        //final ImageSize targetSize = new ImageSize(maxWidth, maxWidth);
        Float replyPadding = mContext.getResources().getDimension(R.dimen.post_header_word_spacing);


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
            holder.menu = (Button) convertView.findViewById(R.id.btn_post_menu);
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

     /*   // Add replies to post
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                popup.getMenuInflater().inflate();
            }
        });
        ArrayAdapter<String> replies = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_dropdown_item, post.repliedBy);
        holder.spinner.setAdapter(replies);
        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String postNo = ((TextView) view).getText().toString();
                int i = 0;
                for (Post post : mPosts) {
                    if (post.postNo.equals(postNo)) {
                        mPostReplyClicked.gotoPost(i);
                    }
                    i++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        // Set up reply button
        if(!post.numReplies.equals("")) {
            if(post.numReplies.equals("0")) {
                holder.replies.setText("Click to reply >>>");
            } else {
                holder.replies.setText("Post has " + post.numReplies + " replies");
            }
            holder.replies.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReplyClicked(post.rootBoard, post.postNo);
                }
            });
            holder.postBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReplyClicked(post.rootBoard, post.postNo);
                }
            });
        } else {
            holder.replies.setVisibility(View.GONE);
        }

        // Set up image button
        if(!post.images.isEmpty()) {
            holder.filename.setText(post.images.get(0).getFileInfo());
            holder.filename.setVisibility(View.VISIBLE);

            holder.image.setImageBitmap(null);

            if(post.isThumbnail) {
                ImageAware imageAware = new ImageViewAware(holder.image, false);
                ImageLoader.getInstance().displayImage(post.images.get(0).getThumbnailUrl(), imageAware,
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
                holder.postLayout.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                ImageAware imageAware = new ImageViewAware(holder.image, false);
                ImageLoader.getInstance().displayImage(post.images.get(0).getFullUrl(), imageAware,
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
                holder.postLayout.setOrientation(LinearLayout.VERTICAL);
            }
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ViewHolder myHolder = (ViewHolder) v.getTag();
                    final Post myPost = getItem(position);
                    // If it is on big picture
                    if(myHolder.postLayout.getOrientation() == LinearLayout.VERTICAL) {
                        myPost.isThumbnail = true;
                        ImageLoader.getInstance().displayImage(post.images.get(0).getThumbnailUrl(),
                                myHolder.image,
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
                                        myHolder.postLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        myHolder.image.setVisibility(View.VISIBLE);
                                    }
                                });
                    } else {
                        myPost.isThumbnail = false;
                        ImageLoader.getInstance().displayImage(post.images.get(0).getFullUrl(),
                                myHolder.image,
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
                                        myHolder.postLayout.setOrientation(LinearLayout.VERTICAL);
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
        ImageButton image;
        TextView username, postDate, postNo, topic, postBody, replies, filename;
        LinearLayout postLayout;
        Button menu;
        ProgressBar progressImage;
    }
}
