package net.develish.noted.noted.net.develish.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.develish.noted.noted.Note;
import net.develish.noted.noted.Tag;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Julius Pedersen <deifyed@gmail.com> on 09.03.18.
 */

public class DatabaseManager extends SQLiteOpenHelper {
    private static DatabaseManager mInstance = null;
    private static final int VERSION = 1;
    private static final String NAME = "notes.db";
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static DatabaseManager getInstance(Context context) {
        if(mInstance == null)
            mInstance = new DatabaseManager(context);

        return mInstance;
    }

    private DatabaseManager(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Note.db.TABLE_CREATE);
        sqLiteDatabase.execSQL(Tag.db.TABLE_CREATE);
        sqLiteDatabase.execSQL(NoteTag.db.TABLE_CREATE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(Note.db.TABLE_DELETE);
        sqLiteDatabase.execSQL(Tag.db.TABLE_DELETE);
        sqLiteDatabase.execSQL(NoteTag.db.TABLE_DELETE);

        onCreate(sqLiteDatabase);
    }

    public void saveNote(Note note) {
        SQLiteDatabase wdb = getWritableDatabase();

        if(!updateNote(wdb, note))
            note.id = createNote(wdb, note);

        wdb.close();
    }

    public Note getNote(String uuid) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = new String[] { Note.db.column._ID, Note.db.column.TITLE };
        String selection = Note.db.column.UUID + " = ?";
        String[] selectionArgs = new String[] { uuid };

        Cursor c = db.query(Note.db.TABLE_NAME, projection, selection, selectionArgs, null,
                            null, null);

        Note result = null;
        if(c.moveToFirst()) {
            long id = c.getLong(c.getColumnIndex(Note.db.column._ID));
            String title = c.getString(c.getColumnIndex(Note.db.column.TITLE));

            result = new Note(id, uuid, title);
        }
        c.close();
        db.close();

        return result;
    }
    public List<Note> getAllNotes() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = new String[] { Note.db.column._ID, Note.db.column.UUID,
                                             Note.db.column.TITLE, Note.db.column.LAST_CHANGE };

        Cursor c = db.query(Note.db.TABLE_NAME, projection, null, null,
                                null, null, Note.db.column.LAST_CHANGE);

        int COL_ID = c.getColumnIndex(Note.db.column._ID);
        int COL_UUID = c.getColumnIndex(Note.db.column.UUID);
        int COL_TITLE = c.getColumnIndex(Note.db.column.TITLE);
        int COL_LAST_CHANGE = c.getColumnIndex(Note.db.column.LAST_CHANGE);

        List<Note> result = new ArrayList<>(c.getCount());
        while(c.moveToNext()) {
            Note n = new Note(c.getLong(COL_ID), c.getString(COL_UUID), c.getString(COL_TITLE));
            n.last_change = c.getString(COL_LAST_CHANGE);

            result.add(n);
        }

        c.close();
        db.close();

        return result;
    }

    private long createNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();

        long result = createNote(db, note);
        db.close();

        return result;
    }
    private long createNote(SQLiteDatabase db, Note note) {
        ContentValues values = new ContentValues();

        values.put(Note.db.column.TITLE, note.getTitle());
        values.put(Note.db.column.UUID, note.uuid);
        values.put(Note.db.column.LAST_CHANGE, getNow());

        return db.insert(Note.db.TABLE_NAME, null, values);
    }

    private boolean updateNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();

        boolean result = updateNote(db, note);

        db.close();

        return result;
    }
    private boolean updateNote(SQLiteDatabase db, Note note) {
        String selection = Note.db.column.UUID + " = ?";
        String[] selectionArgs = new String[] { note.uuid };

        ContentValues values = new ContentValues();
        values.put(Note.db.column.TITLE, note.getTitle());
        values.put(Note.db.column.LAST_CHANGE, getNow());

        int affected = db.update(Note.db.TABLE_NAME, values, selection, selectionArgs);

        return affected == 1;
    }

    public boolean deleteNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();

        boolean result = deleteNote(db, note);
        db.close();

        return result;
    }
    private boolean deleteNote(SQLiteDatabase db, Note note) {
        String selection = Note.db.column.UUID + " = ?";
        String[] selectionArgs = new String[] { note.uuid };

        int affected = db.delete(Note.db.TABLE_NAME, selection, selectionArgs);

        return affected == 1;
    }

    private String getNow() {
        return DATE_FORMATTER.format(new Date());
    }
}