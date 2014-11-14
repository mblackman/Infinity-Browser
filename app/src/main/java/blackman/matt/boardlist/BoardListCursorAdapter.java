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
    public static class ViewHolder {
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

        String htmlBoardLink = "<a href=\"http://8chan.co" +
                boardLink.toLowerCase() +
                "\">" +
                boardLink +
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
