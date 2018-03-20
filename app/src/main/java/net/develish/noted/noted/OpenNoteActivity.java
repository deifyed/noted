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
import android.widget.LinearLayout;

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
    private NoteAdapter mNoteAdapter;

    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;

    private Toolbar cabSelection;

    private List<Note> current_notes;

    // Action mode
    public static boolean inActionMode = false;
    public static ArrayList<Note> selectionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeUI();

        mPrefs = getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE);
        openNote(mPrefs.getString(EXTRA_UUID, null));
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_open_normal, menu);

        return true;
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

                refreshUI();

                mNoteAdapter.notifyDataSetChanged();
                clearActionMode();

                return true;
            default:
                return false;
        }
    }
    @Override
    public void onBackPressed() {
        if(inActionMode) {
            clearActionMode();

            mNoteAdapter.notifyDataSetChanged();
        }
        else
            super.onBackPressed();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            current_notes.clear();

            current_notes.addAll(Note.getAllNotes(this));

            mNoteAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onItemClick(View view, int position) {
        openNote(current_notes.get(position).uuid);
    }

    private void initializeUI() {
        // Sets the layout
        setContentView(R.layout.activity_open_note);

        // Actionbar
        cabSelection = (Toolbar) findViewById(R.id.cabSelection);
        setSupportActionBar(cabSelection);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        // Note list
        mRecyclerView = findViewById(R.id.recyclerNotes);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Empty view
        mEmptyView = findViewById(R.id.empty_view);

        current_notes = new ArrayList<>();
        mNoteAdapter = new NoteAdapter(this, current_notes);
        mNoteAdapter.setClickListener(this);

        mRecyclerView.setAdapter(mNoteAdapter);

        // New note FAB
        FloatingActionButton fabNew = findViewById(R.id.fabNew);
        fabNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNote(null);
            }
        });
    }

    private void refreshUI() {
        if(!current_notes.isEmpty()) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
        else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }
    /*
        Toolbar / Action mode
     */
    public void prepareToolbar(int position) {
        /*
            Actives the toolbar action mode. User can now select items and bulk delete them
         */
        cabSelection.getMenu().clear();
        cabSelection.inflateMenu(R.menu.menu_action_mode);

        inActionMode = true;

        mNoteAdapter.notifyDataSetChanged();

        //if(getSupportActionBar() != null)
        //    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prepareSelection(position);
    }
    public void prepareSelection(int position) {
        /*
            Adds/removes item to/from deletion list
         */
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
        /*
            Updates the selected items counter
         */
        int counter = selectionList.size();

        cabSelection.setTitle(counter + " item(s) selected");
    }
    private void clearActionMode() {
        /*
            Disables the action mode / resets toolbar to normal
         */
        inActionMode = false;

        cabSelection.getMenu().clear();
        cabSelection.inflateMenu(R.menu.menu_open_normal);

        //if(getSupportActionBar() != null)
        //    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        cabSelection.setTitle(R.string.app_name);

        selectionList.clear();
    }

    void openNote(String uuid) {
        /*
            Initiates a create/edit note intent
         */
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.putExtra(EXTRA_UUID, uuid);

        startActivityForResult(intent, REQUEST_EDIT);
    }
}
