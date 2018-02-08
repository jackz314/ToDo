package com.jackz314.todo;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by zhang on 2017/9/14.
 */

final class AppContract {
    /** The string authority */
    static final String AUTHORITY = MainActivity.getThisPackageName();
    /** A content:// style URI authority */
    private static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Class Item.
     */
    static final class Item implements BaseColumns {

        /** The content:// style URI */
        static final Uri TODO_URI = Uri.withAppendedPath(AUTHORITY_URI, dtb.TODO_TABLE);
        static final Uri HISTORY_URI = Uri.withAppendedPath(AUTHORITY_URI, dtb.HISTORY_TABLE);
        static final Uri TAGS_URI = Uri.withAppendedPath(AUTHORITY_URI, dtb.TAGS_TABLE);



        /**
         * MIME TYPES
         */
        static final String DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + dtb.TODO_TABLE;
        static final String ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + dtb.TODO_TABLE;
        static final String HISTORY_DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + dtb.HISTORY_TABLE;
        static final String HISTORY_ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + dtb.HISTORY_TABLE;
        static final String TAGS_DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + dtb.TODO_TABLE;
        static final String TAGS_ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + dtb.TODO_TABLE;
    }
}

