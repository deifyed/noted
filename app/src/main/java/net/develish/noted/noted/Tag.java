package net.develish.noted.noted;

import android.provider.BaseColumns;

/**
 * Created by Julius Pedersen <deifyed@gmail.com> on 09.03.18.
 */

public class Tag {
    long id;
    public String title;

    public Tag(String title) {
        this(-1, title);
    }
    public Tag(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public long getId() {
        return id;
    }
    public boolean setId(long id) {
        if(id == -1) {
            this.id = id;

            return true;
        }

        return false;
    }

    public static class db {
        public static final String TABLE_NAME = "tags";
        public static final String TABLE_CREATE =
                "CREATE TABLE " + Tag.db.TABLE_NAME + " (" +
                        Tag.db.column._ID + " INTEGER PRIMARY KEY," +
                        Tag.db.column.TITLE + " TEXT" +
                        ")";
        public static final String TABLE_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static class column implements BaseColumns {
            public static final String TITLE = "title";
        }
    }
}
