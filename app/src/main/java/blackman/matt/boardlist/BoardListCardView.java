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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import blackman.matt.infinitebrowser.R;


/**
 * A view for displaying a boards information taken from the boardList database.
 * It will update the database if a board is favorited.
 */
public class BoardListCardView extends RelativeLayout {
    private ToggleButton mFavButton;
    private TextView mBoardLinkTextView;
    private TextView mBoardNameTextView;
    private TextView mBoardValueTextView;

    /**
     * Public constructor used to get the context of the view being created.
     *
     * @param context Context of the parent to this view.
     */
    public BoardListCardView(Context context) {
        super(context);
        init();
    }

    /**
     * Initialization of the views basic components.
     * This is used for a general case, if multiple constructors are used.
     */
    private void init() {
        inflate(getContext(), R.layout.board_list_card_view, this);
        this.mFavButton = (ToggleButton)findViewById(R.id.tb_board_fav);
        this.mBoardLinkTextView = (TextView)findViewById(R.id.tv_board_link);
        this.mBoardNameTextView = (TextView)findViewById(R.id.tv_board_name);
        this.mBoardValueTextView = (TextView)findViewById(R.id.tv_board_value);
    }

    /**
     * Used to set the values of the card from whoever is creating it.
     *
     * @param boardLink Short link for the board. EX /v/, /ck/, /tech/,...etc
     * @param boardName Name of the board
     * @param nation Nationality and language of the board.
     * @param boardValue A stored value used to sort the boards.
     * @param isFavorited If the user follows the board or not.
     *
     * TODO: Add flags for nationality
     */
    public void setCardInfo(String boardLink, String boardName, String nation, String boardValue,
                            boolean isFavorited) {
        mBoardLinkTextView.setText(boardLink);
        mBoardNameTextView.setText(boardName);
        mBoardValueTextView.setText(boardValue);
        mFavButton.setChecked(isFavorited);
        invalidate();
        requestLayout();
    }
}