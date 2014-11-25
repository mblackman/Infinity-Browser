package blackman.matt.board;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import blackman.matt.utils.PageLoader;
import blackman.matt.infinitebrowser.R;

/**
 * Created by Matt on 11/20/2014.
 */
public class BoardPageLoader extends PageLoader {
    private final ProgressBar mProgress;
    private final TextView mProgressText;
    private final PostArrayAdapter mAdapter;

    private List<String> postIds = new ArrayList<String>();

    public PageLoaderResponse mResponse;

    /**
     * Basic constructor to initialize the class.
     *
     * @param parent  Parent view who needs a loading.
     * @param posts   The posts container.
     * @param adapter Adapter for the list view that holds the posts.
     */
    public BoardPageLoader(View parent, List<Post> posts, PostArrayAdapter adapter, Boolean isRootBoard) {
        super(posts, isRootBoard);
        mProgress = (ProgressBar) parent.findViewById(R.id.progress_page_load);
        mProgressText = (TextView) parent.findViewById(R.id.tv_progress_page_load);
        mAdapter = adapter;
    }

    public interface PageLoaderResponse {
        public void setPageLoaded(Boolean isLoaded);

        public void sendErrorMessage(CharSequence error);
    }

    /**
     * Shows the progress bar and its text to the user.
     */
    @Override
    protected void onPreExecute() {
        mProgress.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
        mResponse.setPageLoaded(false);
    }

    /**
     * After the page is read in, the pages are turned into fragments and are put on the
     * screen.
     */
    @Override
    protected void onPostExecute(Boolean loadSuccess) {
        if (loadSuccess) {
            mResponse.setPageLoaded(true);
        } else {
            mResponse.setPageLoaded(false);
        }
        mAdapter.notifyDataSetChanged();
        mProgress.setVisibility(View.GONE);
        mProgressText.setVisibility(View.GONE);
    }
}
