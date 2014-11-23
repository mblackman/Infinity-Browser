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

package blackman.matt.boardlist;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import blackman.matt.infinitebrowser.R;

/**
 * Reads in all the rows from the SQL query and addresses them to listCardViews and sets up
 * the adapter.
 *
 * Created by Matt on 10/24/2014.
 */
class BoardListCursorAdapter extends CursorAdapter {
    private BoardFavoritedListener mFavoritedListener;
    private static LayoutInflater mInflater=null;

    /**
     * Interface used to favorite a board when this is called.
     */
    public interface BoardFavoritedListener {
        public void favoriteBoard(String boardLink, Boolean isChecked);
    }

    /**
     * A nice place to hold your views in.
     */
    private static class ViewHolder {
        public ToggleButton favButton;
        public TextView boardLinkTextView, boardNameTextView, boardValueTextView;
    }

    /**
     * Basic constructor for the class. Runs the parent constructor and assigns values.
     *
     * @param context Context of caller.
     * @param c Cursor to be adapted.
     */
    public BoardListCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Sets the listener for the board favoring.
     *
     * @param listener Listener being sent in.
     */
    public void setListener(BoardFavoritedListener listener) {
        mFavoritedListener = listener;
    }

    /**
     * Called when a new view is going to be created, and it creates the new CardListView.
     *
     * @param context context of the caller.
     * @param cursor Cursor for the query.
     * @param parent Parent view group to the caller.
     * @return The newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.board_list_card_view, parent, false);
        ViewHolder holder = new ViewHolder();

        holder.boardNameTextView = (TextView) view.findViewById(R.id.tv_board_name);
        holder.boardValueTextView = (TextView) view.findViewById(R.id.tv_board_value);
        holder.boardLinkTextView = (TextView) view.findViewById(R.id.tv_board_link);
        holder.favButton = (ToggleButton) view.findViewById(R.id.tb_board_fav);

        view.setTag(holder);

        return view;
    }

    /**
     * Binds data to the view once the view has been inflated and data read.
     *
     * @param view View to be binded.
     * @param context Context of the caller.
     * @param cursor Cursor for the query.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        final String boardName = cursor.getString(3);
        final String boardLink = cursor.getString(2);
        //final String nationality = cursor.getString(1);
        final String displayColumn = cursor.getString(5);
        final int favoritedInt = cursor.getInt(4);
        final boolean isFavorited = favoritedInt > 0;

        String htmlBoardLink = "<a href=\"https://8chan.co/" +
                boardLink.toLowerCase() +
                "\">" +
                "/" + boardLink + "/" +
                "</a>";

        holder.boardNameTextView.setText(boardName);
        holder.boardLinkTextView.setText(Html.fromHtml(htmlBoardLink));
        holder.boardLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        holder.boardValueTextView.setText(displayColumn);
        holder.favButton.setChecked(isFavorited);
        holder.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((ToggleButton) v).isChecked();
                mFavoritedListener.favoriteBoard(boardLink, isChecked);
            }
        });
    }

    /**
     * If data was changed in the database, this notifies that adapter.
     */
    @Override
    protected void onContentChanged() {
        super.onContentChanged();
        notifyDataSetChanged();
    }

}
