package blackman.matt.infinitebrowser;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Matt on 10/24/2014.
 */
public class boardListCursorAdapter extends SimpleCursorAdapter {
    private Context appContext;
    private String selectedValue;

    public boardListCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        appContext = context;
        selectedValue = from[from.length - 1];
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String boardName;
        String nationality;
        String displayColumn;
        final String boardLink;
        int favoritedInt;
        boolean isFavorited;

        boardName = cursor.getString(cursor.getColumnIndexOrThrow("boardname"));
        boardLink = cursor.getString(cursor.getColumnIndexOrThrow("boardlink"));
        nationality = cursor.getString(cursor.getColumnIndexOrThrow("nation"));
        displayColumn = cursor.getString(cursor.getColumnIndexOrThrow(selectedValue));
        favoritedInt = cursor.getInt(cursor.getColumnIndexOrThrow("favorited"));

        isFavorited = favoritedInt == 1;

        TextView viewBoardName = (TextView) view.findViewById(R.id.tv_board_name);
        viewBoardName.setText(boardName);

        TextView viewBoardLink = (TextView) view.findViewById(R.id.tv_board_link);
        viewBoardLink.setText(boardLink);

        TextView viewBoardValue = (TextView) view.findViewById(R.id.tv_board_value);
        viewBoardValue.setText(displayColumn);

        ToggleButton viewToggler = (ToggleButton) view.findViewById(R.id.tb_board_fav);
        viewToggler.setChecked(isFavorited);
        viewToggler.setOnCheckedChangeListener(new MyClickClass(boardLink));
    }

    /**
     * A listener for how the card reacts to the favorite button being clicked.
     */
    class MyClickClass implements CompoundButton.OnCheckedChangeListener {
        private String mBoardLink;
        /**
         * Default, empty constructor.
         */
        public MyClickClass(String boardLink) {
            mBoardLink = boardLink;
        }

        /**
         * Find the board from the database and sets if the board is to be favorited or not.
         *
         * @param buttonView The button that was clicked.
         * @param isChecked If the button just got checked or not.
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BoardListDatabase list_db = new BoardListDatabase(appContext);

            list_db.favoriteBoard(mBoardLink, isChecked);
        }
    }
}
