package com.railprosfs.railsapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  The SimpleListPicker is a listbox based dialog picker used to display
 *  a list of values based off an array.  The title and array are supplied
 *  by the caller.  The selection (position in the array) and the resource
 *  key of the array are returned.  The array doubles as a key on return to
 *  allow the callback to figure out what to do.
 */
public class SimpleListDialog extends DialogFragment {
    private FragmentTalkBack Activity;
    private FragmentTalkBack OverActivity;
    private int Source;
    private int Title;
    private List<String> DisplayList;
    private static final String SOURCE = "source";
    private static final String TITLE = "title";
    private static final String DISPLAYLIST = "displaylist";

    /**
     *  Set up the initial conditions for the dialog picker.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param source    The resource id of an array of values used to provide selection options.
     */
    public SimpleListDialog(@StringRes int title, @ArrayRes int source){
        if(title == 0) {
            Title = R.string.title_pick_default;
        } else {
            Title = title;
        }
        Source = source;
    }

    /**
     * A fragment must implement a default constructor. The state should be saved
     * somehow and made available to the onCreateDialog().
     */
    public SimpleListDialog() { }

    // In case the array to use as a list is not a hard coded array.
    public void ChangeListArray(List<String> values){
        DisplayList = values;
    }

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
        outState.putInt(SOURCE, Source);
        outState.putInt(TITLE, Title);
        outState.putStringArrayList(DISPLAYLIST, new ArrayList<>(DisplayList));
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            Source = savedInstanceState.getInt(SOURCE);
            Title = savedInstanceState.getInt(TITLE);
            DisplayList = savedInstanceState.getStringArrayList(DISPLAYLIST);
        }
        ArrayAdapter<String> adapter;
        Context ctx = getContext();
        if(ctx != null) {
            if (DisplayList == null){
                DisplayList = Arrays.asList(getContext().getResources().getStringArray(Source));
            }
            adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, DisplayList);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialog);
            builder.setTitle(Title)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int picked) {
                            if(OverActivity != null) { Activity = OverActivity; }
                            if (Activity == null) Activity = (FragmentTalkBack) getActivity();
                            if (Activity != null) {
                                Activity.simpleListResponse(Source, picked);
                            }
                            dismiss();
                        }
                    });
            return builder.create();
        } else {
            // Fall back informational dialog to try again.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialog);
            builder.setTitle(Title)
                    .setMessage(R.string.msg_inform_list_failure)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            // This is a failure condition, so no real response.
                            dismiss();
                        }
                    });
            return builder.create();
        }
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
