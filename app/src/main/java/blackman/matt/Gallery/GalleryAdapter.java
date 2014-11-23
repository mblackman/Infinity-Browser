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

package blackman.matt.Gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.util.ArrayList;
import java.util.List;

import blackman.matt.Utils.ImageLongPressDialog;
import blackman.matt.board.ImageFile;
import blackman.matt.board.Post;
import blackman.matt.infinitebrowser.R;

/**
 * Created by Matt on 11/20/2014.
 */
public class GalleryAdapter extends BaseAdapter {
    private Context mContext;
    private List<ImageFile> mImages;
    private static ImageSize mImageSize;
    private final ImageView mExpandedImage;
    private final View mGalleryContainer;
    private final View mDimmer;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;


    public GalleryAdapter(Context context, List<Post> posts, View galleryContainer) {
        mContext = context;
        this.mGalleryContainer = galleryContainer;
        mExpandedImage = (ImageView) galleryContainer.findViewById(R.id.gallery_expanded_image);
        mDimmer = galleryContainer.findViewById(R.id.gallery_dimmer);
        mShortAnimationDuration = mContext.getResources().getInteger(android.R.integer.config_shortAnimTime);
        mImages = new ArrayList<ImageFile>();
        int mBoundSize = Math.round(mContext.getResources().getDimension(R.dimen.gallery_column_width));
        mImageSize = new ImageSize(mBoundSize, mBoundSize);
        for(Post post : posts) {
            for(ImageFile image : post.images) {
                if(!image.getExtension().equals(".webm")) {
                    mImages.add(image);
                }
            }
        }
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public Object getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final ImageFile image = (ImageFile) getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gallery_image, parent, false);
            holder = new ViewHolder();
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(mImageSize.getWidth(), mImageSize.getHeight());

            holder.image = (GalleryImageItem) convertView.findViewById(R.id.gallery_image);
            holder.progress = (ProgressBar) convertView.findViewById(R.id.progress_gallery_image);

            holder.progress.setLayoutParams(params);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomImageFromThumb(v, holder.progress, image);
            }
        });

        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new ImageLongPressDialog(mContext, image);
                return true;
            }
        });

        ImageLoader.getInstance().loadImage(image.getThumbnailUrl(), mImageSize,
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

        return convertView;
    }

    private void zoomImageFromThumb(final View thumbView, final View progress,
                                    final ImageFile image) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        ImageAware imageAware = new ImageViewAware(mExpandedImage, false);

        ImageLoader.getInstance().displayImage(image.getFullUrl(), imageAware,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        thumbView.setVisibility(View.INVISIBLE);
                        progress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                                                FailReason failReason) {
                        Drawable error = mContext.getResources().getDrawable(R.drawable.deadico);
                        ((ImageView) view).setImageDrawable(error);
                        progress.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onLoadingComplete(final String imageUri, final View view,
                                                  Bitmap loadedImage) {
                        ((ImageView) view).setImageBitmap(loadedImage);
                        view.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                new ImageLongPressDialog(mContext, image);
                                return true;
                            }
                        });
                        progress.setVisibility(View.INVISIBLE);
                        // Calculate the starting and ending bounds for the zoomed-in image.
                        // This step involves lots of math. Yay, math.
                        final Rect startBounds = new Rect();
                        final Rect finalBounds = new Rect();
                        final Point globalOffset = new Point();

                        // The start bounds are the global visible rectangle of the thumbnail,
                        // and the final bounds are the global visible rectangle of the container
                        // view. Also set the container view's offset as the origin for the
                        // bounds, since that's the origin for the positioning animation
                        // properties (X, Y).
                        thumbView.getGlobalVisibleRect(startBounds);
                        mGalleryContainer.getGlobalVisibleRect(finalBounds, globalOffset);
                        startBounds.offset(-globalOffset.x, -globalOffset.y);
                        finalBounds.offset(-globalOffset.x, -globalOffset.y);

                        // Adjust the start bounds to be the same aspect ratio as the final
                        // bounds using the "center crop" technique. This prevents undesirable
                        // stretching during the animation. Also calculate the start scaling
                        // factor (the end scaling factor is always 1.0).
                        float startScale;
                        if ((float) finalBounds.width() / finalBounds.height()
                                > (float) startBounds.width() / startBounds.height()) {
                            // Extend start bounds horizontally
                            startScale = (float) startBounds.height() / finalBounds.height();
                            float startWidth = startScale * finalBounds.width();
                            float deltaWidth = (startWidth - startBounds.width()) / 2;
                            startBounds.left -= deltaWidth;
                            startBounds.right += deltaWidth;
                        } else {
                            // Extend start bounds vertically
                            startScale = (float) startBounds.width() / finalBounds.width();
                            float startHeight = startScale * finalBounds.height();
                            float deltaHeight = (startHeight - startBounds.height()) / 2;
                            startBounds.top -= deltaHeight;
                            startBounds.bottom += deltaHeight;
                        }

                        if(finalBounds.width() > loadedImage.getWidth()) {
                            finalBounds.left = Math.abs(finalBounds.width()
                                    - loadedImage.getWidth()) / 2;
                        }
                        if(finalBounds.height() > loadedImage.getHeight()) {
                            finalBounds.top = Math.abs(finalBounds.height()
                                    - loadedImage.getHeight()) / 2;
                        }

                        // Hide the thumbnail and show the zoomed-in view. When the animation
                        // begins, it will position the zoomed-in view in the place of the
                        // thumbnail.
                        thumbView.setAlpha(0f);
                        mDimmer.setVisibility(View.VISIBLE);
                        view.setVisibility(View.VISIBLE);

                        // Set the pivot point for SCALE_X and SCALE_Y transformations
                        // to the top-left corner of the zoomed-in view (the default
                        // is the center of the view).
                        view.setPivotX(0f);
                        view.setPivotY(0f);

                        // Construct and run the parallel animation of the four translation and
                        // scale properties (X, Y, SCALE_X, and SCALE_Y).
                        AnimatorSet set = new AnimatorSet();
                        set
                                .play(ObjectAnimator.ofFloat(view, View.X,
                                        startBounds.left, finalBounds.left))
                                .with(ObjectAnimator.ofFloat(view, View.Y,
                                        startBounds.top, finalBounds.top))
                                .with(ObjectAnimator.ofFloat(view, View.SCALE_X, startScale, 1f))
                                .with(ObjectAnimator.ofFloat(view, View.SCALE_Y, startScale, 1f));
                        set.setDuration(mShortAnimationDuration);
                        set.setInterpolator(new DecelerateInterpolator());
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mCurrentAnimator = null;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                mCurrentAnimator = null;
                            }
                        });
                        set.start();
                        mCurrentAnimator = set;

                        // Upon clicking the zoomed-in image, it should zoom back down
                        // to the original bounds and show the thumbnail instead of
                        // the expanded image.
                        final float startScaleFinal = startScale;
                        mDimmer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View animView) {
                                if (mCurrentAnimator != null) {
                                    mCurrentAnimator.cancel();
                                }

                                // Animate the four positioning/sizing properties in parallel,
                                // back to their original values.
                                AnimatorSet set = new AnimatorSet();
                                set.play(ObjectAnimator
                                        .ofFloat(view, View.X, startBounds.left))
                                        .with(ObjectAnimator
                                                .ofFloat(view,
                                                        View.Y,startBounds.top))
                                        .with(ObjectAnimator
                                                .ofFloat(view,
                                                        View.SCALE_X, startScaleFinal))
                                        .with(ObjectAnimator
                                                .ofFloat(view,
                                                        View.SCALE_Y, startScaleFinal));
                                set.setDuration(mShortAnimationDuration);
                                set.setInterpolator(new DecelerateInterpolator());
                                set.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        thumbView.setVisibility(View.VISIBLE);
                                        thumbView.setAlpha(1f);
                                        mDimmer.setVisibility(View.GONE);
                                        view.setVisibility(View.GONE);
                                        mCurrentAnimator = null;
                                        MemoryCacheUtils.removeFromCache(imageUri,
                                                ImageLoader.getInstance().getMemoryCache());
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        thumbView.setVisibility(View.VISIBLE);
                                        thumbView.setAlpha(1f);
                                        mDimmer.setVisibility(View.GONE);
                                        view.setVisibility(View.GONE);
                                        mCurrentAnimator = null;
                                        MemoryCacheUtils.removeFromCache(imageUri,
                                                ImageLoader.getInstance().getMemoryCache());
                                    }
                                });
                                set.start();
                                mCurrentAnimator = set;
                            }
                        });
                    }
                });
    }

    private static class ViewHolder {
        public GalleryImageItem image;
        public ProgressBar progress;
    }
}
