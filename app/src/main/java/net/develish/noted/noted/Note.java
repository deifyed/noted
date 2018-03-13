package net.develish.noted.noted;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.BaseColumns;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by Julius Pedersen <deifyed@gmail.com> on 04.03.18.
 */

public class Note {
    private static final Pattern tagPattern = Pattern.compile("#\\w+");
    private static DatabaseManager mDatabaseManager = null;
    private static SharedPreferences mPrefs = null;

    static Note load(Context context, String uuid) {
        verifyDatabaseManager(context);

        return mDatabaseManager.getNote(uuid);
    }
    static List<Note> getAllNotes(Context context) {
        verifyDatabaseManager(context);

        return mDatabaseManager.getAllNotes();
    }

    private static void verifyPrefs(Context context) {
        if(mPrefs == null)
            mPrefs = context.getSharedPreferences(OpenNoteActivity.DEFAULT_PREFS, Context.MODE_PRIVATE);
    }
    private static void verifyDatabaseManager(Context context) {
        if(mDatabaseManager == null)
            mDatabaseManager = DatabaseManager.getInstance(context);
    }

    public long id;
    public String uuid;
    public String last_change;
    private String title;
    private boolean changed = false;

    Note() {
        this(-1, UUID.randomUUID().toString(), "Unnamed");
    }
    public Note(long id, String uuid, String title) {
        this.id = id;
        this.uuid = uuid;
        this.title = title;
    }

    void save(Context context, String content) throws IOException {
        /*
            Saving a note.

            Saving consists of three actions:
               1. Saving uuid, title and tags to the db
               2. Saving content to file
               3. Saving the most recent UUID to prefs for autoloading most recent note

         */
        verifyPrefs(context);
        verifyDatabaseManager(context);

        // Saving uuid, title and tags to database
        mDatabaseManager.saveNote(this);

        // Saving content to file
        FileOutputStream file = context.openFileOutput(this.uuid, Context.MODE_PRIVATE);
        file.write(content.getBytes());
        file.close();

        // Saving the most recent UUID to prefs
        setLatestPrefs();
    }
    void delete(Context context) {
        verifyPrefs(context);
        verifyDatabaseManager(context);

        mDatabaseManager.deleteNote(this);

        if(mPrefs.getString(OpenNoteActivity.EXTRA_UUID, "").equals(this.uuid))
            mPrefs.edit().remove(OpenNoteActivity.EXTRA_UUID).apply();

        context.deleteFile(this.uuid);
    }

    void touch() { this.changed = true; }
    boolean hasChanged() {
        return changed;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String content) {
        this.title = extractTitle(content);
    }

    String getContent(Context context) throws IOException {
        FileInputStream fis = context.openFileInput(uuid);
        DataInputStream in = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        StringBuilder content = new StringBuilder();
        String line;
        while((line = br.readLine()) != null)
            content.append(String.format("%s\n", line));

        return (content.length() > 0) ? content.substring(0, content.length() - 1) : "";
    }

    private String extractTitle(String content) {
        /*
            Returns a title created from the first phrase of the content
         */
        int title_end = (content.length() < 160) ? content.length() : 160;
        int possible_end = content.indexOf('\n');

        String title = "Unnamed";
        if(title_end != 0) {
            if(possible_end != -1)
                title_end = possible_end;

            title = content.substring(0, title_end);
        }

        return title;
    }
    private void setLatestPrefs() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(OpenNoteActivity.EXTRA_UUID, this.uuid);
        editor.apply();
    }

    public static class db {
        public static final String TABLE_NAME = "notes";
        public static final String TABLE_CREATE =
                "CREATE TABLE " + Note.db.TABLE_NAME + " (" +
                        column._ID + " INTEGER PRIMARY KEY," +
                        column.UUID + " TEXT," +
                        column.TITLE + " TEXT," +
                        column.LAST_CHANGE + " TEXT" +

                ")";
        public static final String TABLE_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static class column implements BaseColumns {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String LAST_CHANGE = "touched";
        }
    }
}

