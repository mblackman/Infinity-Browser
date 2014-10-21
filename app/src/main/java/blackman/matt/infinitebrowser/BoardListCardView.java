package blackman.matt.infinitebrowser;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;


/**
 * TODO: document your custom view class.
 */
public class BoardListCardView extends RelativeLayout {
    private ToggleButton mFavButton;
    private TextView mBoardLinkTextView;
    private TextView mBoardNameTextView;
    private TextView mBoardValueTextView;
    private String mBoardLink;

    public BoardListCardView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.board_list_card_view, this);
        this.mFavButton = (ToggleButton)findViewById(R.id.tb_board_fav);
        this.mBoardLinkTextView = (TextView)findViewById(R.id.tv_board_link);
        this.mBoardNameTextView = (TextView)findViewById(R.id.tv_board_name);
        this.mBoardValueTextView = (TextView)findViewById(R.id.tv_board_value);

        mFavButton.setOnCheckedChangeListener(new MyClickClass());
    }

    public void setCardInfo(String boardLink, String boardName, String nation, String boardValue,
                            boolean isFavorited) {
        mBoardLink = boardLink;
        mBoardLinkTextView.setText(boardLink);
        mBoardNameTextView.setText(boardName);
        mBoardValueTextView.setText(boardValue);
        mFavButton.setChecked(isFavorited);
        invalidate();
        requestLayout();
    }

    class MyClickClass implements CompoundButton.OnCheckedChangeListener {
        public MyClickClass() {

        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BoardListDatabase list_db = new BoardListDatabase(getContext());

            list_db.favoriteBoard(mBoardLink, isChecked);
        }
    }
}
