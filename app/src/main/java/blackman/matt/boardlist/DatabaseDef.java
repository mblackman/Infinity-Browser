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

import android.provider.BaseColumns;

/**
 * Defines the columns of the database to hold the boards in.
 *
 * Created by Matt on 11/5/2014.
 */
public class DatabaseDef {

    /**
     * Column definition.
     */
    public static final class Boards implements BaseColumns {
        /**
         * Empty constructor.
         */
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
