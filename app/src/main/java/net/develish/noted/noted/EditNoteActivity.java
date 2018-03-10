package net.develish.noted.noted;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class EditNoteActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText txtContent;

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
}
