package com.railprosfs.railsapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;

/**
 * The SimpleConfirmDialog is a Confirm or Cancel choice dialog used to get a
 * positive affirmation from a user before doing something that will be hard
 * to undo.  For example, sending data to the server or deleting local data.
 * The constructor requires the title and message to confirm.  The buttons
 * can be changed via method call.
 * The affirmation callback contains the message id, so multiple uses in the
 * same class can be distinguished.
 */
public class SimpleConfirmDialog extends DialogFragment {
    private FragmentTalkBack Activity;
    private FragmentTalkBack OverActivity;
    private int Message;
    private int Title;
    private int OK;
    private int CANCEL;
    private boolean OfferCancel;
    private static final String MESSAGE = "message";
    private static final String TITLE = "title";
    private static final String BUTTON_OK = "buttonok";
    private static final String BUTTON_NO = "buttonno";
    private static final String OFFERCANCEL = "offercancel";

    /**
     *  Set up the initial conditions for the dialog picker.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of a string to use as the confirmation message.
     * @param informational True is infomational only, so no cancel button (e.g. no choice).
     */
    public SimpleConfirmDialog(@StringRes int title, @StringRes int message, boolean informational) {
        if(title == 0) {
            Title = R.string.title_confirm_default;
        } else {
            Title = title;
        }
        Message = message;
        OK = R.string.ok;
        CANCEL = R.string.cancel;
        OfferCancel = !informational;
    }

    /**
     * A fragment must implement a default constructor. The state should be saved
     * somehow and made available to the onCreateDialog().
     */
    public SimpleConfirmDialog() { }

    // Override the callback activity.  Helpful if deep in a fragment stack.
    // **WARNING**  Keeping track of this secondary activity not going to
    //              happen on configuration change (rotation). Not a big
    //              for a simple dialog.  User can just try again.
    public void ChangeListener(FragmentTalkBack listener) {
        if (listener != null) {
            OverActivity = listener;
        }
    }

    /**
     * These low overhead fragments do not have much state, but need to save it off
     * here in case of screen rotation and such.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MESSAGE, Message);
        outState.putInt(TITLE, Title);
        outState.putInt(BUTTON_OK, OK);
        outState.putInt(BUTTON_NO, CANCEL);
        outState.putBoolean(OFFERCANCEL, OfferCancel);
    }

    /**
     * In case the button topics need to change.
     * @param btnOk     String displayed on the Affirmation button.
     * @param btnCancel String displayed on the Cancelation button.
     */
    public void SetButtons(@StringRes int btnOk, @StringRes int btnCancel) {
        OK = btnOk;
        CANCEL = btnCancel;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            Message = savedInstanceState.getInt(MESSAGE);
            Title = savedInstanceState.getInt(TITLE);
            OK = savedInstanceState.getInt(BUTTON_OK);
            CANCEL = savedInstanceState.getInt(BUTTON_NO);
            OfferCancel = savedInstanceState.getBoolean(OFFERCANCEL);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialog);
        builder.setTitle(Title)
                .setMessage(Message)
                .setPositiveButton(OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(OverActivity != null) { Activity = OverActivity; }
                        if (Activity == null) Activity = (FragmentTalkBack) getActivity();
                        if(Activity != null) {
                            Activity.simpleConfirmResponse(Message);
                        }
                        dismiss();
                    }
                });
        if(OfferCancel) {
            builder.setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) { /* User cancelled dialog */ }
            });
        }
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentTalkBack) {
            Activity = (FragmentTalkBack) context;
        } else {
            throw new RuntimeException(context.toString() + context.getResources().getString(R.string.err_no_fragmenttalkback));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Activity = null;
    }
}
