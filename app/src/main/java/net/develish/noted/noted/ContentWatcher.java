package net.develish.noted.noted;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by deifyed on 03.03.18.
 */

public class ContentWatcher implements TextWatcher {
    Activity mActivity;
    Note mNote;

    ContentWatcher(Activity activity, Note note) {
        mActivity = activity;
        mNote = note;
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        mNote.touch();

        mNote.setTitle(charSequence.toString());

        mActivity.setTitle(mNote.getTitle());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }
}
