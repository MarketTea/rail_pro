package com.railprosfs.railsapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.railprosfs.railsapp.R;

/**
 * The SimpleDisplayDialog is a used to display dynamic informational messages
 * to the user.  This can be used to display information about errors or other
 * items that are interesting to the user. There is no callback, just dismiss.
 */
public class SimpleDisplayDialog extends DialogFragment {
    private String Message;
    private int Title;
    private int OK;
    private static final String MESSAGE = "message";
    private static final String TITLE = "title";
    private static final String BUTTON_OK = "buttonok";

    /**
     *  Set up the initial conditions for the dialog picker.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of a string to use as the confirmation message.
     */
    public SimpleDisplayDialog(@StringRes int title, String message) {
        if(title == 0) {
            Title = R.string.title_inform_display;
        } else {
            Title = title;
        }
        Message = message;
        OK = R.string.ok;
    }

    /**
     * A fragment must implement a default constructor. The state should be saved
     * somehow and made available to the onCreateDialog().
     */
    public SimpleDisplayDialog() { }

    /**
     * These low overhead fragments do not have much state, but need to save it off
     * here in case of screen rotation and such.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MESSAGE, Message);
        outState.putInt(TITLE, Title);
        outState.putInt(BUTTON_OK, OK);
    }

    /**
     * In case the button topics need to change.
     * @param btnOk     String displayed on the Affirmation button.
     */
    public void SetButtons(@StringRes int btnOk) {
        OK = btnOk;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            Message = savedInstanceState.getString(MESSAGE);
            Title = savedInstanceState.getInt(TITLE);
            OK = savedInstanceState.getInt(BUTTON_OK);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialog);
        builder.setTitle(Title)
                .setMessage(Message)
                .setPositiveButton(OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
