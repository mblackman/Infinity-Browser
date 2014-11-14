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
