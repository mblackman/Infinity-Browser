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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import blackman.matt.Utils.PageLoader;
import blackman.matt.board.Post;
import blackman.matt.infinitebrowser.R;

/**
 * Created by Matt on 11/20/2014.
 */
public class GalleryActivity extends Activity implements PageLoader.PageLoadedNotifier {
    public final static String ARG_GALLERY_BOARD = "gallery_board";
    public final static String ARG_GALLERY_THREAD = "gallery_thread";
    private List<Post> mPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Intent intent = getIntent();

        String board = intent.getStringExtra(ARG_GALLERY_BOARD);
        String thread = intent.getStringExtra(ARG_GALLERY_THREAD);

        String fileUrl;
        if(thread == null) {
            fileUrl = board + "/" + "0.json";
        } else {
            fileUrl = board + "/res/" + thread + ".json";
        }

        mPosts = new ArrayList<Post>();

        PageLoader loader = new PageLoader(mPosts, false);
        loader.setNotifier(this);

        URL url;
        try {
            url = new URL("https", "8chan.co", fileUrl);
            loader.execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void pageLoaded() {
        GridView grid = (GridView) findViewById(R.id.grid_gallery);
        RelativeLayout gallery = (RelativeLayout) findViewById(R.id.gallery_container);
        GalleryAdapter mAdapter = new GalleryAdapter(this, mPosts, gallery);
        grid.setAdapter(mAdapter);
    }


}
