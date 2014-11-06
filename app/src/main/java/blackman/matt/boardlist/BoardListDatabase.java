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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * A database to store boards on 8chan.
 * An extension of the SQLite open helper with the design of the database to store the list of
 * boards. Can read the DB and add items to it.
 * Created by Matt on 10/12/2014.
 */
public class BoardListDatabase extends SQLiteOpenHelper  {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BoardList.db";

    // Query to create the table.
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            DatabaseDef.Boards.TABLE_NAME + " (" +
            DatabaseDef.Boards.BOARD_ID + " INTEGER PRIMARY KEY," +
            DatabaseDef.Boards.BOARD_LINK + " TEXT," +
            DatabaseDef.Boards.NATIONALITY + " TEXT," +
            DatabaseDef.Boards.POSTS_LAST_HOUR + " INTEGER," +
            DatabaseDef.Boards.TOTAL_POSTS + " INTEGER," +
            DatabaseDef.Boards.UNIQUE_IPS + " INTEGER," +
            DatabaseDef.Boards.DATE_CREATED + " TEXT," +
            DatabaseDef.Boards.FAVORITED + " INTEGER DEFAULT 0," +
            DatabaseDef.Boards.BOARD_NAME + " TEXT" +
            " )";

    // Query to delete all the entries.
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseDef.Boards.TABLE_NAME;

    // Query to get all the boards that have been favorited.
    private static final String SQL_SELECT_FAVORITED_BOARDS = DatabaseDef.Boards.FAVORITED + ">0";

    /**
     * Constructor to set up the database for the caller.
     *
     * @param context Context of the caller
     */
    public BoardListDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the table when the class is initialized as an object.
     *
     * @param database The database being created.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Whenever the database is being updated to a new version, the tables is cleared out and
     * remade with the new database version.
     *
     * @param db The database being upgraded.
     * @param oldVersion The version the current database is.
     * @param newVersion The version the database is being updated to.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(BoardListDatabase.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Inserts a board into the database as defined by the information being sent in.
     *
     * @param boardName Name of the board.
     * @param nation Nationality of the board.
     * @param boardlink Link to the board. EG /v/
     * @param postsLastHour Posts per hour as last updated.
     * @param totalPosts Total posts to board as last updated.
     * @param uniqueIps Unique IPs visiting the board as last updated.
     * @param dateCreated Date the board was created.
     */
    public void insertBoard(String boardName, String nation, String boardlink, String postsLastHour,
                            String totalPosts, String uniqueIps, String dateCreated) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        int boardId = boardlink.hashCode();
        String cleanedName = boardName.replace("'", "''");

        String UPSERT = "INSERT OR REPLACE INTO " + DatabaseDef.Boards.TABLE_NAME + " (" +
                DatabaseDef.Boards.BOARD_ID + ", " + DatabaseDef.Boards.BOARD_LINK + ", " +
                DatabaseDef.Boards.BOARD_NAME + ", " + DatabaseDef.Boards.NATIONALITY + ", " +
                DatabaseDef.Boards.POSTS_LAST_HOUR + ", " + DatabaseDef.Boards.TOTAL_POSTS + ", " +
                DatabaseDef.Boards.UNIQUE_IPS + ", " + DatabaseDef.Boards.DATE_CREATED + ", " +
                DatabaseDef.Boards.FAVORITED + ") " +
                "VALUES ( " + boardId + ", '" + boardlink + "', '" + cleanedName + "', '" +
                nation +  "', '" + postsLastHour + "', '" + totalPosts + "', '" +
                uniqueIps + "', '" + dateCreated + "', " +
                "(SELECT " + DatabaseDef.Boards.FAVORITED + " FROM " +
                DatabaseDef.Boards.TABLE_NAME + " WHERE " + DatabaseDef.Boards.BOARD_ID + " = " +
                boardId + "));";

        db.execSQL(UPSERT);

    }

    /**
     * Returns a cursor pointing to all the boards the user has favorited.
     *
     * @return A cursor to the boards the user has favorited.
     */
    public Cursor getFavoritedBoards() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection;
        String sortOrder;

        projection = new String[] {
                DatabaseDef.Boards.BOARD_ID,
                DatabaseDef.Boards.NATIONALITY,
                DatabaseDef.Boards.BOARD_LINK,
                DatabaseDef.Boards.BOARD_NAME
        };

        sortOrder = DatabaseDef.Boards.BOARD_LINK + " ASC";

        return db.query(
                DatabaseDef.Boards.TABLE_NAME,        // The table to query
                projection,                  // The columns to return
                SQL_SELECT_FAVORITED_BOARDS, // The columns for the WHERE clause
                null,                        // The values for the WHERE clause
                null,                        // don't group the rows
                null,                        // don't filter by row groups
                sortOrder                    // The sort order
        );
    }

    /**
     * Searches through the database for boards that match a pattern and sorts the boards
     * depending on variables set by users.
     *
     * @param search What you are searching for by board link or name.
     * @param sortBy The value you are sorting on.
     * @param order The order to be sort. EG ASC or DESC
     * @return The cursor to query.
     */
    public Cursor getSortedSearch(CharSequence search, String sortBy, String order) {
        SQLiteDatabase db = getReadableDatabase();
        String selection;
        String select;
        String[] projection;
        String sortOrder;

        projection = new String[] {
                DatabaseDef.Boards.BOARD_ID,
                DatabaseDef.Boards.NATIONALITY,
                DatabaseDef.Boards.BOARD_LINK,
                DatabaseDef.Boards.BOARD_NAME,
                DatabaseDef.Boards.FAVORITED,
                sortBy
        };

        if(search == null || search.toString().trim().equals("")){
            selection = null;
        } else {
            select = "'%" + search.toString() + "%'";

            selection = DatabaseDef.Boards.BOARD_LINK + " LIKE " + select + " OR " +
                    DatabaseDef.Boards.BOARD_NAME + " LIKE " + select;
        }

        sortOrder = sortBy + " " + order;

        return db.query(
                DatabaseDef.Boards.TABLE_NAME,       // The table to query
                projection,                 // The columns to return
                selection,                  // The columns for the WHERE clause
                null,              // The values for the WHERE clause
                null,                       // don't group the rows
                null,                       // don't filter by row groups
                sortOrder                   // The sort order
        );
    }

    /**
     * Toggles the favorited status of a board in the database.
     *
     * @param boardLink The board to update.
     * @param follow Whether to favorite the board or now.
     * @return If the operation was a success. -1 for failure.
     */
    public int favoriteBoard(String boardLink, Boolean follow) {
        SQLiteDatabase db = this.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseDef.Boards.FAVORITED, follow ? 1 : 0);

        // Which row to update, based on the ID
        String selection = DatabaseDef.Boards.BOARD_LINK + "=?";
        String[] selectionArgs = { boardLink };

        return db.update(
                DatabaseDef.Boards.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }
}
