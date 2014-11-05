package blackman.matt.boardlist;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Matt on 11/5/2014.
 */
public class DatabaseDef {

    public DatabaseDef(){
    }

    public static final class Boards implements BaseColumns {
        private Boards(){
        }

        public static final String TABLE_NAME = "boards";
        public static final String BOARD_ID = "_id";
        public static final String BOARD_NAME = "boardname";
        public static final String NATIONALITY = "nation";
        public static final String BOARD_LINK = "boardlink";
        public static final String POSTS_LAST_HOUR = "postslasthour";
        public static final String TOTAL_POSTS = "totalposts";
        public static final String UNIQUE_IPS = "uniqueips";
        public static final String DATE_CREATED = "datecreated";
        public static final String FAVORITED = "favorited"; // 1 means favorited 0 means no
    }
}
