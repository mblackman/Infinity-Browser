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

package blackman.matt.infinitebrowser;

import android.app.Activity;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blackman.matt.board.Board;
import blackman.matt.boardlist.BoardListActivity;
import blackman.matt.boardlist.BoardListDatabase;
import blackman.matt.boardlist.DatabaseDef;


/**
 * The main method of Infinity Browser.
 * Starts when the program is launched and is in charge of starting and managing activities
 * within the program.
 */
public class InfinityBrowser extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        Board.OnReplyClickedListener {

    private CharSequence mTitle;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Generic onCreate method generated by the IDE. Called when the application is first
     * initialized.
     * @param savedInstanceState ??
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = getResources().getString(R.string.app_name);

        if(!ImageLoader.getInstance().isInited()) {
            // Set up configuration for the universal image loader
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    //.imageScaleType(ImageScaleType.EXACTLY)
                    .build();

            ImageLoaderConfiguration config =
                    new ImageLoaderConfiguration.Builder(getApplicationContext())
                            .defaultDisplayImageOptions(defaultOptions)
                            .memoryCacheSize(20 * 1024 * 1024) // 20MB
                            .diskCacheSize(50 * 1024 * 1024) // 50MB
                            .threadPoolSize(10)
                            .build();

            ImageLoader.getInstance().init(config);
        }

        // Load preferences for activity
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean ageAccept = preferences.getBoolean("age_guard_accept", false);
        String defaultBoard = preferences.getString("default_board", "").toLowerCase();
        LinearLayout helpText = (LinearLayout) findViewById(R.id.ll_help_add_boards);

        // Checks if age guard has been accepted
        if(!ageAccept){
            DialogFragment ageGuardDialog = new AgeGuardDialogFragment();
            ageGuardDialog.show(getFragmentManager(), "ageGuardDialog");
        } else {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Intent intent = getIntent();
            Board newBoard = null;

            // TODO: Ensure intent loads properly
            if(intent != null && intent.getData() != null) {
                Pattern patternRoot = Pattern.compile("(?<=8chan.co\\/)\\w*");
                Pattern patternThread = Pattern.compile("(?<=\\/res\\/)\\w*");
                Matcher rootMatch = patternRoot.matcher(intent.getDataString());
                Matcher threadMatch = patternThread.matcher(intent.getDataString());
                rootMatch.find();
                String boardRoot = rootMatch.group(0);

                if(threadMatch.find()) {
                    newBoard = Board.newInstance(boardRoot, threadMatch.group(0));
                } else {
                    newBoard = Board.newInstance(boardRoot);
                }

                mTitle = intent.getDataString().replace("https://8chan.co", "")
                        .replace("http://8chan.co", "")
                        .replace("index.html", "");
            } else if(!defaultBoard.equals("")) {
                newBoard = Board.newInstance(defaultBoard);
                mTitle = "/" + defaultBoard.toLowerCase() + "/";
            } else {
                BoardListDatabase db = new BoardListDatabase(this);
                Cursor cursor =  db.getFavoritedBoards();

                if(cursor.moveToNext()) {
                    String boardLink = cursor.getString(
                            cursor.getColumnIndex(DatabaseDef.Boards.BOARD_LINK)).toLowerCase();
                    newBoard = Board.newInstance(boardLink);
                    mTitle = boardLink;
                } else {
                    helpText.setVisibility(View.VISIBLE);
                }
                cursor.close();
            }
            if(newBoard != null) {
                fragmentTransaction.replace(R.id.container, newBoard, mTitle.toString());
                helpText.setVisibility(View.GONE);
                setTitle(mTitle);
            }
            fragmentTransaction.commit();
        }

        // Set up navigation drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer with new onClickListener
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    /**
     * Replaces the item fragments in the navigation drawer when a new item is selected.
     * @param position the position of the newly selected item
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
        setTitle(mTitle);
    }

    /**
     * Called when the navigation drawer is opened and the position is changed.
     *
     * @param number the position of the navigation drawer.
     */
    void onSectionAttached(int number) {
        //mTitle = boardLink;
        setTitle(mTitle);
    }

    /**
     * Override to set the local title variable.
     *
     * @param title New title.
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        super.setTitle(mTitle);
    }


    /**
     * Restores the action bar when called.
     */
    void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }


    /**
     * Adds the options menu to the main window
     * @param menu the menu to add
     * @return Returns a bool if the menu was created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.board_browser, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * When an option menu item is selected this handles the case.
     * @param item the item selected in the menu
     * @return True if the item already exists or it is created
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_boards:
                Intent boardList = new Intent(this, BoardListActivity.class);
                startActivity(boardList);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Creates a new board for a post when reply button is hit.
     *
     * @param boardRoot Link to the thread to open up
     * @param threadNo Thread no being opened
     */
    @Override
    public void onReplyClicked(String boardRoot, String threadNo) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean ageAccept = preferences.getBoolean("age_guard_accept", false);

        if(ageAccept) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            LinearLayout helpText = (LinearLayout) findViewById(R.id.ll_help_add_boards);

            Board newThread = Board.newInstance(boardRoot, threadNo);

            fragmentTransaction.replace(R.id.container, newThread, threadNo);
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();

            helpText.setVisibility(View.GONE);
            mTitle = boardRoot.replace("https://8chan.co", "") + threadNo;
            setTitle(mTitle);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The menu being created
     * @return If the action was a success
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                InfinityBrowser.this.startActivity(new Intent(InfinityBrowser.this,
                        SettingsActivity.class));
                return true;
            }
        });
        return result;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        /**
         * Empty constructor generated by the class
         */
        public PlaceholderFragment() {
        }

        /**
         * Called when the view is created
         * @param inflater The fragment inflator and context
         * @param container The container for the menu items
         * @param savedInstanceState Last state of the instance
         * @return Returns the view just created
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_board, container, false);
        }

        /**
         * Called when the user or app focuses the fragment
         * @param activity the activity which is a parent to this
         */
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((InfinityBrowser) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
