package blackman.matt.Gallery;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Matt on 11/21/2014.
 */
public class GalleryImageItem extends ImageView {
    public GalleryImageItem(Context context) {
        super(context);
    }

    public GalleryImageItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryImageItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
