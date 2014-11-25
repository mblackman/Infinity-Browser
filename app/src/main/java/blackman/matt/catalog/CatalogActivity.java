package blackman.matt.catalog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import blackman.matt.board.Post;
import blackman.matt.infinitebrowser.R;

/**
 * Created by Matt on 11/24/2014.
 */
public class CatalogActivity extends Activity implements CatalogLoader.CatalogLoadedNotifier{
    public final static String ARG_CATALOG_BOARD = "catalog_board";

    private CatalogAdapter mAdapter;
    private List<Post> mPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Intent intent = getIntent();

        GridView grid = (GridView) findViewById(R.id.grid_gallery);

        String boardRoot = intent.getStringExtra(ARG_CATALOG_BOARD);
        String fileUrl = boardRoot + "/catalog.json";

        mPosts = new ArrayList<Post>();

        CatalogLoader loader = new CatalogLoader(mPosts);
        loader.setNotifier(this);

        //findViewById(R.id.progress_gallery).setVisibility(View.VISIBLE);

        mAdapter = new CatalogAdapter(this, mPosts, grid.getMeasuredWidth());
        grid.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        grid.setAdapter(mAdapter);

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

    }

    @Override
    public void addPost(Post post) {
        mPosts.add(post);
        mAdapter.notifyDataSetChanged();
    }
}
