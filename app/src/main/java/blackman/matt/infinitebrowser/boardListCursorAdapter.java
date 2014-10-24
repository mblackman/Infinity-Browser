package blackman.matt.infinitebrowser;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Reads in all the rows from the SQL query and addresses them to listCardViews and sets up
 * the adapter.
 *
 * Created by Matt on 10/24/2014.
 */
public class boardListCursorAdapter extends CursorAdapter {
    private String selectedValue;

    /**
     * Basic constructor for the class. Runs the parent constructor and assigns values.
     *
     * @param context Context of caller.
     * @param c Cursor to be adapted.
     * @param selectedValue The currently selected query column.
     */
    public boardListCursorAdapter(Context context, Cursor c, String selectedValue) {
        super(context, c, 0);
        this.selectedValue = selectedValue;
    }

    /**
     * Called when a new view is going to be created, and it creates the new CardListView.
     *
     * @param context context of the caller.
     * @param cursor Cursor for the query.
     * @param parent Parent view group to the caller.
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        BoardListCardView cardView = new BoardListCardView(context);

        return cardView;
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

        ((BoardListCardView) view).setCardInfo(boardLink, boardName, nationality, displayColumn, isFavorited);
    }
}
