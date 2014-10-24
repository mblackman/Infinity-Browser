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

package blackman.matt.infinitebrowser;

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

    /**
     *  Inner class that defines the table contents for the database.
     */
    public static abstract class FeedEntry implements BaseColumns {
        private static final String TABLE_NAME = "boards";
        private static final String KEY_BOARD_NAME = "boardname";
        private static final String KEY_NATIONALITY = "nation";
        private static final String KEY_BOARD_LINK = "boardlink";
        private static final String KEY_POSTS_LAST_HOUR = "postslasthour";
        private static final String KEY_TOTAL_POSTS = "totalposts";
        private static final String KEY_UNIQUE_IPS = "uniqueips";
        private static final String KEY_DATE_CREATED = "datecreated";
        private static final String KEY_FAVORITED = "favorited"; // 1 means favorited 0 means no
    }

    // Query to create the table.
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" + FeedEntry.KEY_BOARD_LINK +
                    " TEXT PRIMARY KEY," + FeedEntry.KEY_NATIONALITY + " TEXT," + FeedEntry._ID + " INTEGER," +
                    FeedEntry.KEY_POSTS_LAST_HOUR + " INTEGER," + FeedEntry.KEY_TOTAL_POSTS + " INTEGER," +
                    FeedEntry.KEY_UNIQUE_IPS + " INTEGER," + FeedEntry.KEY_DATE_CREATED + " TEXT," +
                    FeedEntry.KEY_FAVORITED + " INTEGER," + FeedEntry.KEY_BOARD_NAME + " TEXT" +
            " )";

    // Query to delete all the entries.
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    // Query to get all the boards that have been favorited.
    private static final String SQL_SELECT_FAVORITED_BOARDS = FeedEntry.KEY_FAVORITED + ">0";

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
        values.put(FeedEntry.KEY_BOARD_NAME, boardName);
        values.put(FeedEntry.KEY_NATIONALITY, nation);
        values.put(FeedEntry.KEY_BOARD_LINK, boardlink);
        values.put(FeedEntry.KEY_POSTS_LAST_HOUR, postsLastHour);
        values.put(FeedEntry.KEY_TOTAL_POSTS, totalPosts);
        values.put(FeedEntry.KEY_UNIQUE_IPS, uniqueIps);
        values.put(FeedEntry.KEY_DATE_CREATED, dateCreated);
        values.put(FeedEntry.KEY_FAVORITED, 0);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                FeedEntry.TABLE_NAME,
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
        Boolean boardExists = false;
        String selection = FeedEntry.KEY_BOARD_LINK + "=?";
        String[] boardLinks = new String[] { boardLink };

        Cursor c = db.query(
                FeedEntry.TABLE_NAME,        // The table to query
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
            FeedEntry._ID,
            FeedEntry.KEY_NATIONALITY,
            FeedEntry.KEY_BOARD_LINK,
            FeedEntry.KEY_BOARD_NAME
        };

        sortOrder = FeedEntry.KEY_BOARD_LINK + " DESC";

        Cursor c = db.query(
                FeedEntry.TABLE_NAME,        // The table to query
                projection,                  // The columns to return
                SQL_SELECT_FAVORITED_BOARDS, // The columns for the WHERE clause
                null,                        // The values for the WHERE clause
                null,                        // don't group the rows
                null,                        // don't filter by row groups
                sortOrder                    // The sort order
        );

        return c;
    }

    /**
     * Checks if the table is empty of any rows.
     *
     * @return If the table is empty or not.
     */
    public Boolean isEmpty() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + FeedEntry.TABLE_NAME, null);
        Boolean isEmpty;

        if (mCursor.moveToFirst()) {
            isEmpty = false;
        }
        else {
            isEmpty = true;
        }
        return isEmpty;
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
                FeedEntry._ID,
                FeedEntry.KEY_NATIONALITY,
                FeedEntry.KEY_BOARD_LINK,
                FeedEntry.KEY_BOARD_NAME,
                FeedEntry.KEY_FAVORITED,
                sortBy
        };

        sortOrder = sortBy + " " + order;

        Cursor c = db.query(
                FeedEntry.TABLE_NAME,       // The table to query
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
        values.put(FeedEntry.KEY_POSTS_LAST_HOUR, postsLastHour);
        values.put(FeedEntry.KEY_TOTAL_POSTS, totalPosts);
        values.put(FeedEntry.KEY_UNIQUE_IPS, uniqueIps);

        // Which row to update, based on the ID
        String selection = FeedEntry.KEY_BOARD_NAME + " =?";
        String[] boards = new String[] { boardName };

        int count = db.update(
                FeedEntry.TABLE_NAME,
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
        values.put(FeedEntry.KEY_FAVORITED, follow ? 1 : 0);

        // Which row to update, based on the ID
        String selection = FeedEntry.KEY_BOARD_LINK + "=?";
        String[] selectionArgs = { boardLink };

        int count = db.update(
                FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        return count;
    }
}
