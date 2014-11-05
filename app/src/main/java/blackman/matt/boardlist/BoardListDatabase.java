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
import android.provider.BaseColumns;
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
    private Context mContext;
    private SQLiteOpenHelper SQLiteHelper;

    // Query to create the table.
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            DatabaseDef.Boards.TABLE_NAME + " (" +
            DatabaseDef.Boards.BOARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DatabaseDef.Boards.BOARD_LINK + " TEXT," +
            DatabaseDef.Boards.NATIONALITY + " TEXT," +
            DatabaseDef.Boards.POSTS_LAST_HOUR + " INTEGER," +
            DatabaseDef.Boards.TOTAL_POSTS + " INTEGER," +
            DatabaseDef.Boards.UNIQUE_IPS + " INTEGER," +
            DatabaseDef.Boards.DATE_CREATED + " TEXT," +
            DatabaseDef.Boards.FAVORITED + " INTEGER," +
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
        mContext = context;
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
     * @return The id of the table row.
     */
    public long insertBoard(String boardName, String nation, String boardlink, String postsLastHour,
                            String totalPosts, String uniqueIps, String dateCreated) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseDef.Boards.BOARD_NAME, boardName);
        values.put(DatabaseDef.Boards.NATIONALITY, nation);
        values.put(DatabaseDef.Boards.BOARD_LINK, boardlink);
        values.put(DatabaseDef.Boards.POSTS_LAST_HOUR, postsLastHour);
        values.put(DatabaseDef.Boards.TOTAL_POSTS, totalPosts);
        values.put(DatabaseDef.Boards.UNIQUE_IPS, uniqueIps);
        values.put(DatabaseDef.Boards.DATE_CREATED, dateCreated);
        values.put(DatabaseDef.Boards.FAVORITED, 0);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseDef.Boards.TABLE_NAME,
                null,
                values);

        return newRowId;
    }

    /**
     * Check to see if a board is already in the database.
     *
     * @param boardLink Board link EG /v/
     * @return True if the board is in the database, false otherwise.
     */
    public Boolean boardExists(String boardLink) {
        SQLiteDatabase db = getReadableDatabase();
        Boolean boardExists;
        String selection = DatabaseDef.Boards.BOARD_LINK + "=?";
        String[] boardLinks = new String[] { boardLink };

        Cursor c = db.query(
                DatabaseDef.Boards.TABLE_NAME,        // The table to query
                null,                        // The columns to return
                selection,                   // The columns for the WHERE clause
                boardLinks,                  // The values for the WHERE clause
                null,                        // don't group the rows
                null,                        // don't filter by row groups
                null                         // The sort order
        );

        c.moveToFirst();
        boardExists = c.getCount() != 0;
        c.close();

        return boardExists; // if mContext.getCount() == 0 then board doesn't exists
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

        Cursor c = db.query(
                DatabaseDef.Boards.TABLE_NAME,        // The table to query
                projection,                  // The columns to return
                SQL_SELECT_FAVORITED_BOARDS, // The columns for the WHERE clause
                null,                        // The values for the WHERE clause
                null,                        // don't group the rows
                null,                        // don't filter by row groups
                sortOrder                    // The sort order
        );

        return c;
    }

    public Cursor getSortedSearch(CharSequence search, String sortBy, String order) {
        SQLiteDatabase db = getReadableDatabase();
        String selection;
        String[] projection;
        String[] selectionArgs;
        String sortOrder;

        projection = new String[] {
                DatabaseDef.Boards.BOARD_ID,
                DatabaseDef.Boards.NATIONALITY,
                DatabaseDef.Boards.BOARD_LINK,
                DatabaseDef.Boards.BOARD_NAME,
                DatabaseDef.Boards.FAVORITED,
                sortBy
        };

        selection = DatabaseDef.Boards.BOARD_LINK + " LIKE ? OR " +
                    DatabaseDef.Boards.BOARD_NAME + " LIKE ?";
        if(search == null || search.equals("")) {
            selectionArgs = new String[]{"%", "%"};
        } else {
            selectionArgs = new String[]{"%" + search.toString() + "%", "%" + search.toString() + "%"};
        }
        sortOrder = sortBy + " " + order;

        Cursor c = db.query(
                DatabaseDef.Boards.TABLE_NAME,       // The table to query
                projection,                 // The columns to return
                selection,                  // The columns for the WHERE clause
                selectionArgs,              // The values for the WHERE clause
                null,                       // don't group the rows
                null,                       // don't filter by row groups
                sortOrder                   // The sort order
        );
        return c;
    }

    /**
     * Opens up the database so a cursor can point to tables in the database to easily
     * update the UI with database changed.
     *
     * @return A BoardListDatabase with an open cursor.
     * @throws android.database.SQLException
     */
    public BoardListDatabase openToRead() throws android.database.SQLException {
        SQLiteHelper = new SQLiteOpenHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(SQL_CREATE_ENTRIES);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }
        };
        return this;
    }

    /**
     * Gets a cursor pointing to all the entries sorted by the parameters specified by the user.
     * Will use default sortBy of posts_last_hour if set to null.
     * @param sortBy the column to sort the db.
     * @param order the order of the sort. desc or asc.
     * @return the cursor pointing to the returned db
     */
    public Cursor getBoardsInSortedOrder(String sortBy, String order) {
        SQLiteDatabase db = getReadableDatabase();
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

        sortOrder = sortBy + " " + order;

        Cursor c = db.query(
                DatabaseDef.Boards.TABLE_NAME,       // The table to query
                projection,                 // The columns to return
                null,                       // The columns for the WHERE clause
                null,                       // The values for the WHERE clause
                null,                       // don't group the rows
                null,                       // don't filter by row groups
                sortOrder                   // The sort order
        );
        return c;
    }

    /**
     * Updates an existing board with new data.
     *
     * @param boardName The named of the board to change.
     * @param postsLastHour The new posts in the last hour.
     * @param totalPosts The new total posts to the board.
     * @param uniqueIps The total unique IPs that have visited the board.
     * @return A int that determines if the row was updated. -1 for false.
     */
    public int updateBoard(String boardName, String postsLastHour, String totalPosts,
                           String uniqueIps) {
        SQLiteDatabase db = this.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseDef.Boards.POSTS_LAST_HOUR, postsLastHour);
        values.put(DatabaseDef.Boards.TOTAL_POSTS, totalPosts);
        values.put(DatabaseDef.Boards.UNIQUE_IPS, uniqueIps);

        // Which row to update, based on the ID
        String selection = DatabaseDef.Boards.BOARD_NAME + " =?";
        String[] boards = new String[] { boardName };

        int count = db.update(
                DatabaseDef.Boards.TABLE_NAME,
                values,
                selection,
                boards);
        return count;
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

        int count = db.update(
                DatabaseDef.Boards.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        return count;
    }
}
