package net.develish.noted.noted;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deifyed on 04.03.18.
 */

public class OpenNoteActivity extends AppCompatActivity implements NoteAdapter.ItemClickListener {
    private static final String TAG = "OpenNoteActivity";
    static final int REQUEST_EDIT = 0;

    static final String DEFAULT_PREFS = "note_prefs";
    static final String EXTRA_UUID = "nuuid";

    private SharedPreferences mPrefs;
    private NoteAdapter mAdapter;

    private Toolbar cabSelection;

    private List<Note> current_notes;
    private List<Tag> current_tags;

    // Action mode
    public static boolean inActionMode = false;
    public static ArrayList<Note> selectionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_note);
        cabSelection = (Toolbar) findViewById(R.id.cabSelection);
        setSupportActionBar(cabSelection);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        RecyclerView recyclerNotes = findViewById(R.id.recyclerNotes);
        recyclerNotes.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerNotes.setLayoutManager(new GridLayoutManager(this, 1));

        current_notes = new ArrayList<>();
        mAdapter = new NoteAdapter(this, current_notes);
        mAdapter.setClickListener(this);

        recyclerNotes.setAdapter(mAdapter);

        FloatingActionButton fabNew = findViewById(R.id.fabNew);
        fabNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNote(null);
            }
        });

        mPrefs = getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE);
        openNote(mPrefs.getString(EXTRA_UUID, null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }
    @Override
    public void onBackPressed() {
        if(inActionMode) {
            clearActionMode();

            mAdapter.notifyDataSetChanged();
        }
        else
            super.onBackPressed();
    }

    public void prepareToolbar(int position) {
        cabSelection.getMenu().clear();
        cabSelection.inflateMenu(R.menu.menu_action_mode);

        inActionMode = true;

        mAdapter.notifyDataSetChanged();

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prepareSelection(position);
    }
    public void prepareSelection(int position) {
        Note selected = current_notes.get(position);

        if(!selectionList.contains(selected))
            selectionList.add(selected);
        else
            selectionList.remove(selected);

        if(selectionList.size() > 0)
            updateViewCounter();
        else
            clearActionMode();
    }
    private void updateViewCounter() {
        int counter = selectionList.size();

        cabSelection.setTitle(counter + " item(s) selected");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_delete:
                String latest = mPrefs.getString(EXTRA_UUID, "");

                for(Note note : selectionList) {
                    note.delete(this);

                    if(note.uuid.equals(latest))
                        mPrefs.edit().remove(EXTRA_UUID).apply();

                    current_notes.remove(note);
                }

                mAdapter.notifyDataSetChanged();
                clearActionMode();

                return true;
            default:
                return false;
        }
    }
    private void clearActionMode() {
        inActionMode = false;

        cabSelection.getMenu().clear();
        cabSelection.inflateMenu(R.menu.menu_main);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        cabSelection.setTitle(R.string.app_name);

        selectionList.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            current_notes.clear();

            current_notes.addAll(Note.getAllNotes(this));

            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        openNote(current_notes.get(position).uuid);
    }

    void openNote(String uuid) {
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.putExtra(EXTRA_UUID, uuid);

        startActivityForResult(intent, REQUEST_EDIT);
    }
}
