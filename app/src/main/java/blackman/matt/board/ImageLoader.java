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
import android.util.Log;
import android.widget.ImageButton;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import blackman.matt.infinitebrowser.R;

/**
 * Handles the loading of an images thumbnail and full size version.
 * Can open any format still image and animated gifs.
 *
 * Created by Matt on 10/31/2014.
 */
public class ImageLoader {
    private final Context mContext;
    private final ImageButton mButton;
    private final String mThumb, mFull, mExtension;
    private Boolean mIsThumbnail;

    /**
     * Basic constructor that takes in the context and image view along with a thumbnail and
     * full sized version of an image.
     *
     * @param context Context of view calling.
     * @param button Image button that will display the pictures.
     * @param thumbURL URL of the thumbnail.
     * @param fullURL URL of the full sized image.
     */
    public ImageLoader(Context context, ImageButton button, String thumbURL, String fullURL) {
        String[] filenameArray = fullURL.split("\\.");
        mExtension =  filenameArray[filenameArray.length-1];
        mContext = context;
        mButton = button;
        mThumb = thumbURL;
        mFull = fullURL;
        mIsThumbnail = true;
    }

    public void draw() {
        if(mIsThumbnail) {
            loadThumb();
        } else {
            loadFull();
        }
    }

    /**
     * Loads the thumbnail into the image button.
     */
    public void loadThumb(){
        Ion.with(mContext)
                .load(mThumb)
                .setLogging("MyLogs", Log.DEBUG)
                .withBitmap()
                .placeholder(R.drawable.ic_launcher)
                .intoImageView(mButton);
    }

    /**
     * Loads the full sized image into the button.
     */
    public void loadFull() {
        Ion.with(mContext)
                .load(mFull)
                .setLogging("MyLogs", Log.DEBUG)
                .withBitmap()
                .placeholder(R.drawable.ic_launcher)
                .intoImageView(mButton);
    }

    public void renderThumbnail(Boolean isThumb) {
        mIsThumbnail = isThumb;
    }

    /**
     * Returns if the image button is displaying a thumbnail or not.
     *
     * @return If it is a thumbnail.
     */
    public Boolean isThumbnail() {
        return mIsThumbnail;
    }
}
