package net.develish.noted.noted.net.develish.db;

import android.provider.BaseColumns;

/**
 * Created by Julius Pedersen <deifyed@gmail.com> on 09.03.18.
 */

public class NoteTag {
    int id;

    int note_id;
    int tag_id;

    public static class db {
        public static final String TABLE_NAME = "notetag";
        public static final String TABLE_CREATE =
                "CREATE TABLE " + NoteTag.db.TABLE_NAME + " (" +
                        column._ID + " INTEGER PRIMARY KEY," +
                        column.NID + " INTEGER," +
                        column.TID + " INTEGER" +
                        ")";
        public static final String TABLE_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static class column implements BaseColumns {
            public static final String NID = "note_id";
            public static final String TID = "tag_id";
        }
    }
}
