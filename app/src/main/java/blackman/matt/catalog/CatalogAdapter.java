package blackman.matt.catalog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import blackman.matt.board.Post;
import blackman.matt.infinitebrowser.InfinityBrowser;
import blackman.matt.infinitebrowser.R;
import blackman.matt.utils.ImageLongPressDialog;

/**
 * Adapter for the array of posts in the catalog.
 *
 * Created by Matt on 11/24/2014.
 */
class CatalogAdapter extends BaseAdapter {
    private final ImageSize THUMBNAILSIZE;
    private final int THUMBSIZE;
    private Context mContext;
    private List<Post> mPosts;

    public CatalogAdapter(Context context, List<Post> posts, int gridWidth) {
        mContext = context;
        mPosts = posts;
        int NUMCOLUMNS = mContext.getResources().getInteger(R.integer.catalog_num_columns);
        THUMBSIZE = gridWidth / NUMCOLUMNS;
        THUMBNAILSIZE = new ImageSize(THUMBSIZE, THUMBSIZE);
    }

    @Override
    public int getCount() {
        return mPosts.size();
    }

    @Override
    public Object getItem(int position) {
        return mPosts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final Post post = (Post) getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.catalog_thread, parent, false);

            holder = new ViewHolder();

            holder.progress = (ProgressBar) convertView.findViewById(R.id.catalog_progress);
            holder.image = (ImageView) convertView.findViewById(R.id.catalog_image);
            holder.replies = (TextView) convertView.findViewById(R.id.tv_catalog_replies);
            holder.topic = (TextView) convertView.findViewById(R.id.tv_catalog_topic);
            holder.comment = (TextView) convertView.findViewById(R.id.tv_catalog_comment);

            holder.image.setLayoutParams(new LinearLayout.LayoutParams(THUMBSIZE, THUMBSIZE));
            holder.progress.setLayoutParams(new LinearLayout.LayoutParams(THUMBSIZE, THUMBSIZE));
            convertView.setLayoutParams(new AbsListView.LayoutParams(THUMBSIZE, THUMBSIZE * 2));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String numReplies = post.omittedReplies;
        String numImages = post.omittedImages;

        holder.replies.setText("R: " + numReplies + " / I: " + numImages);

        if(post.topic.equals("")) {
            holder.topic.setVisibility(View.GONE);
        } else {
            holder.topic.setVisibility(View.VISIBLE);
            holder.topic.setText(post.topic);
        }

        holder.comment.setText(Html.fromHtml(post.postBody));
        holder.image.setImageBitmap(null);


        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new ImageLongPressDialog(mContext, post.images.get(0));
                return true;
            }
        });

        ImageLoader.getInstance().loadImage(post.images.get(0).getThumbnailUrl(), THUMBNAILSIZE,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        holder.progress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                                                FailReason failReason) {
                        holder.progress.setVisibility(View.GONE);
                        Drawable error = mContext.getResources().getDrawable(R.drawable.deadico);
                        holder.image.setVisibility(View.VISIBLE);
                        holder.image.setImageDrawable(error);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                                                  Bitmap loadedImage) {
                        holder.progress.setVisibility(View.GONE);
                        holder.image.setImageBitmap(loadedImage);
                        holder.image.setVisibility(View.VISIBLE);
                    }
                });

        View.OnClickListener gotoThread = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, InfinityBrowser.class);
                intent.putExtra(InfinityBrowser.ARG_BOARD, post.rootBoard);
                intent.putExtra(InfinityBrowser.ARG_THREAD, post.postNo);
                mContext.startActivity(intent);
            }
        };

        holder.image.setOnClickListener(gotoThread);
        convertView.setOnClickListener(gotoThread);

        return convertView;
    }

    private static class ViewHolder {
        public ImageView image;
        public ProgressBar progress;
        public TextView replies, topic, comment;
    }
}
