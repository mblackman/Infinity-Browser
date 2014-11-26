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

package blackman.matt.catalog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.GridView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import blackman.matt.board.Post;
import blackman.matt.infinitebrowser.R;

/**
 * Activity to see the catalog of threads on a board.
 *
 * Created by Matt on 11/24/2014.
 */
public class CatalogActivity extends Activity implements CatalogLoader.CatalogLoadedNotifier{
    public final static String ARG_CATALOG_BOARD = "catalog_board";

    private String mBoardRoot;
    private CatalogAdapter mAdapter;
    private List<Post> mPosts;

    /**
     * Creates all the things on startup of the activity.
     *
     * @param savedInstanceState A saved state if this is being opened again.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Intent intent = getIntent();

        mBoardRoot = intent.getStringExtra(ARG_CATALOG_BOARD);
        String fileUrl = mBoardRoot + "/catalog.json";

        //noinspection ConstantConditions
        getActionBar().setTitle("Catalog - /" + mBoardRoot + "/");

        mPosts = new ArrayList<Post>();

        CatalogLoader loader = new CatalogLoader();
        loader.setNotifier(this);

        URL url;
        try {
            url = new URL("https", "8chan.co", fileUrl);
            loader.execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the catalog gets loaded from the web.
     *
     * @param jsonArray The array of threads from the catalog.
     */
    @Override
    public void pageLoaded(JSONArray jsonArray) {
        GridView grid = (GridView) findViewById(R.id.grid_gallery);
        mAdapter = new CatalogAdapter(this, mPosts, grid.getMeasuredWidth());
        grid.setOnScrollListener(new EndlessScrollListener(jsonArray));
        grid.setAdapter(mAdapter);
    }

    /**
     * This class is used to set the on scroll listener for the list view on the board.
     * Now the page will load the next page on a board when the bottom is met.
     */
    public class EndlessScrollListener extends PauseOnScrollListener {
        private int currentPage = 0;
        private JSONArray mPage;

        /**
         * Override constructor.
         */
        public EndlessScrollListener(JSONArray jsonArray) {
            super(ImageLoader.getInstance(), true, true);
            mPage = jsonArray;
        }

        /**
         * When the list view hits the bottom, the program gets the next page and displays it.
         *
         * @param view The list view being scrolled on.
         * @param firstVisibleItem First visible on the screen.
         * @param visibleItemCount Number of view on the screen.
         * @param totalItemCount How many views the list view can display.
         */
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (currentPage < mPage.length() &&
                    (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleItemCount)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mPosts.addAll(Post.fromJson(mPage.getJSONObject(currentPage)
                                    .getJSONArray("threads"), mBoardRoot));
                            mAdapter.notifyDataSetChanged();
                            currentPage++;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
