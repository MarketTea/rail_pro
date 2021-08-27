package com.railprosfs.railsapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;

/**
 * The SimplePickerDialog is a radio button based dialog picker used to display
 * a list of values based off of an array.  The title, array to use and current
 * position are supplied by the caller.  The new position (selection) and array
 * are returned by the callback interface to the caller.  It is expected that
 * the array (resource key) doubles as a key on the return to allow the callback
 * to figure out what to do.
 */
public class SimplePickerDialog extends DialogFragment {
    private FragmentTalkBack Activity;
    private int Source;
    private int Selection;
    private int Title;
    private boolean Compromised;
    private static final String SOURCE = "source";
    private static final String SELECTION = "selection";
    private static final String TITLE = "title";
    private static final String COMPROMISED = "compromised";

    /**
     *  Set up the initial conditions for the dialog picker.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param source    The resource id of an array of values used to provide selection options.
     * @param selection The default, zero based selection.  If negative one (-1), nothing selected.
     * @param require   When true, the user must explicitly make a selection to have an effect.
     */
    public SimplePickerDialog(@StringRes int title, @ArrayRes int source, int selection, boolean require) {
        if(title == 0) {
            Title = R.string.title_pick_default;
        } else {
            Title = title;
        }
        Source = source;
        Selection = selection < 0 ? -1 : selection;
        Compromised = !require;
    }

    /**
     * A fragment must implement a default constructor. The state should be saved
     * somehow and made available to the onCreateDialog().
     */
    public SimplePickerDialog() { }

    /**
     * These low overhead fragments do not have much state, but need to save it off
     * here in case of screen rotation and such.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SOURCE, Source);
        outState.putInt(SELECTION, Selection);
        outState.putInt(TITLE, Title);
        outState.putBoolean(COMPROMISED, Compromised);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            Source = savedInstanceState.getInt(SOURCE);
            Selection = savedInstanceState.getInt(SELECTION);
            Title = savedInstanceState.getInt(TITLE);
            Compromised = savedInstanceState.getBoolean(COMPROMISED);
        }
        // Check for positive out of bounds condition.
        if(getActivity() != null) {
            if (getActivity().getResources().getStringArray(Source).length < Selection) {
                Selection = 0;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialog);
        builder.setTitle(Title)
                .setSingleChoiceItems(Source, Selection, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int picked) {
                        Selection = picked;
                        Compromised = true;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Compromised && Selection >= 0) {
                            if (Activity == null) Activity = (FragmentTalkBack) getActivity();
                            if(Activity != null) {
                                Activity.simplePickerResponse(Source, Selection);
                            }
                        }
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User cancelled dialog
                    }
                });
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
