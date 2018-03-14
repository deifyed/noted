package net.develish.noted.noted;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class EditNoteActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText txtContent;

    ShareActionProvider mShareActionProvider;

    Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        mToolbar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mToolbar);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        txtContent = findViewById(R.id.txtContent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_normal, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_share:
                Intent intent = createShareIntent();

                startActivity(Intent.createChooser(intent, "Share note with"));

                return true;
            default:
                return false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if(currentNote == null) {
            String uuid = getIntent().getStringExtra(OpenNoteActivity.EXTRA_UUID);

            if(uuid != null) {
                currentNote = Note.load(this, uuid);
                
                try {
                    txtContent.setText(currentNote.getContent(this));
                }
                catch (IOException e) {
                    Toast.makeText(this, "Error reading content", Toast.LENGTH_SHORT).show();
                }

                System.out.println("Found current note: " + uuid);
            }
            else {
                currentNote = new Note();
                System.out.println("Could not find a current note");
            }

            txtContent.addTextChangedListener(new ContentWatcher(this, currentNote));

            setTitle(currentNote.getTitle());
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);

        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(currentNote.hasChanged()) {
            try {
                currentNote.save(this, txtContent.getText().toString());
            } catch (IOException e) {
                Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Intent createShareIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, currentNote.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, txtContent.getText());
        intent.setType("text/plain");

        return intent;
    }
}
