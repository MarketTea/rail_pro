package com.railprosfs.railsapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.Settings;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data.dto.Railroads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.railprosfs.railsapp.utility.Constants.*;

public class JobSetupDialog extends DialogFragment {
    private DwrJobSetupListener jobSetupListener;
    private Button positiveButton;
    private Button negativeButton;
    private EditText railroadText;
    private EditText jobNumberText;
    private EditText jobServiceType;
    private int jobListingId;
    private String jobNumber;
    private String railRoadCode;
    private int railRoadKey;
    private String jobServiceName;
    private int jobServiceKey;

    // To avoid more Classes Implementing a simple Listener Here
    public interface DwrJobSetupListener {
        void startJobSetup(int railroadKey, String railroadCode, int jobId, int jobSetupId, int jobSetupFormType);
    }

    public static JobSetupDialog newInstance() {
        return new JobSetupDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_job_setup, null);
        builder.setView(view);
        setLaunchJobSetup(builder); // Setup buttons

        jobNumberText = view.findViewById(R.id.inputJobNbr);
        railroadText = view.findViewById(R.id.inputRailroad);
        jobServiceType = view.findViewById(R.id.inputJobServiceType);

        jobNumberText.setText(jobNumber != null ? jobNumber : "");
        railroadText.setText(railRoadCode != null ? railRoadCode : "");
        jobServiceType.setText(jobServiceName != null ? jobServiceName : "");

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Sets Buttons to disabled for now
        positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        negativeButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE);
        setButtonEnabled(false, positiveButton);
        setButtonEnabled(false, negativeButton);

        if (checkValid()) {
            jobNumberText.setText(jobNumber);
            railroadText.setText(railRoadCode);
            jobServiceType.setText(jobServiceName);
        } else {
            railroadText.setText(Settings.getPrefDefaultRailroad(getContext()));
            this.railRoadCode = Settings.getPrefDefaultRailroad(getContext());
            jobServiceType.setText(R.string.hint_flagging);
            jobServiceKey = RP_FLAGGING_SERVICE;
        }

        // If there are not known forms for this job, do not let the user try to go there.
        checkValidButton();

        //Setup the pickers manually.
        ImageView imgJobNumber = getDialog().findViewById(R.id.jobPickerImg);
        ImageView imgFormType = getDialog().findViewById(R.id.fieldJobServiceTypeIcon);
        ImageView imgRailRoad = getDialog().findViewById(R.id.fieldRailroadIcon);

        EditText edtJobNumber = getDialog().findViewById(R.id.inputJobNbr);
        EditText edtFormType = getDialog().findViewById(R.id.inputJobServiceType);
        EditText edtRailRoad = getDialog().findViewById(R.id.inputRailroad);

        imgJobNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchJobPicker();
            }
        });

        edtJobNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchJobPicker();
            }
        });

        imgFormType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleListRequest(R.string.title_pick_job_type, R.array.job_setup_types);
            }
        });

        edtFormType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleListRequest(R.string.title_pick_job_type, R.array.job_setup_types);
            }
        });

        imgRailRoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleListRequest(R.string.title_pick_property, R.array.property_name, Railroads.GetPropertiesSorted(getContext()));
            }
        });

        edtRailRoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleListRequest(R.string.title_pick_property, R.array.property_name, Railroads.GetPropertiesSorted(getContext()));
            }
        });
    }


    private void setLaunchJobSetup(AlertDialog.Builder builder) {
        builder.setTitle(R.string.title_pick_job_setup)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int dwr) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.title_pick_job_setup, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int job) {
                        if (checkValid()) {
                            jobSetupListener.startJobSetup(railRoadKey, railRoadCode, jobListingId, 0, jobServiceKey);
                        }
                    }
                });
    }

    private boolean checkValid() {
        return (jobNumber != null && jobNumber.length() > 0 &&
                railRoadCode != null && railRoadCode.length() > 0 &&
                jobListingId != -1);
    }

    private void checkValidButton() {
        setButtonEnabled(Railroads.UseJobSetup(railRoadCode, jobServiceKey) && jobNumber != null && jobNumber.length() > 0, positiveButton);
        setButtonEnabled(true, negativeButton);
    }

    /*
     *  This Job Picker allows a different job to be selected, but does not allow random
     *  job numbers to be input.  The backend can only process valid job numbers.
     *  The Job Picker Dialog is custom built just for the Picking of Jobs.
     */
    private void launchJobPicker() {
        FragmentManager mgr = ((AppCompatActivity) jobSetupListener).getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_JOB_PICKER);
        if (fragment != null) {
            mgr.beginTransaction().remove(fragment).commit();
        }
        Bundle bundle = new Bundle();
        bundle.putString(RAILROAD, railroadText.getText().toString());
        JobPickerDialog picker = new JobPickerDialog();
        picker.setArguments(bundle);
        picker.show(mgr, KY_JOB_PICKER);

    }

    /* JobPicker Callback.
     *   Update the job id and display number, and check if there are any cost centers linked
     *   to this Job.  The Job Picker is railroad specific, so changing jobs should not change
     *   the form layout.
     */
    public void setJobNumber(JobTbl tbl) {
        jobNumber = tbl.JobNumber;
        jobNumberText.setText(jobNumber);
        jobListingId = tbl.Id;
        checkValidButton();
    }

    /**
     * The basic fragment support for showing a simple listbox picker.
     *
     * @param title  The resource id of a string to use as a title displayed on the dialog.
     * @param source The resource id of an array of values used to provide selection options.
     * @param values An optional list of values to use instead of the one hardcoded in source.
     */
    private void simpleListRequest(int title, int source, List<String> values) {
        // Set up the fragment.
        FragmentManager fragmentManager = ((AppCompatActivity) jobSetupListener).getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(KY_SIMPLE_LIST_FRAG);
        if (fragment != null) { // Clear out the previous use.
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        // Launch the picker.
        SimpleListDialog simpleListDialog = new SimpleListDialog(title, source);
        if (values != null) {
            simpleListDialog.ChangeListArray(values);
        }
        simpleListDialog.show(fragmentManager, KY_SIMPLE_LIST_FRAG);
    }

    private void simpleListRequest(int title, int source) {
        simpleListRequest(title, source, null);
    }

    /**
     * Simple List Dialog callback. The simple listbox picker is used by a number of
     * methods to pick from a long list with a single tap.  This method is where the
     * selection is returned. The source array is used as the key to determine what
     * code needs to deal with the resulting selection.
     *
     * @param source    The selection index, e.g chosen value.
     * @param selection The resource key of the original array provided as the list.
     */
    public void simpleListResponse(int source, int selection) {

        switch (source) {
            case R.array.job_setup_types:
                jobServiceName = Arrays.asList(getResources().getStringArray(source)).get(selection);
                jobServiceType.setText(jobServiceName);
                jobServiceKey = selection;
                checkValidButton();
                break;
            case R.array.property_name:
                railRoadKey = Railroads.PropertyKey(getContext(), Railroads.GetPropertiesSorted(getContext()).get(selection));
                railRoadCode = Railroads.PropertyName(getContext(), railRoadKey);
                if (!railroadText.getText().toString().equalsIgnoreCase(railRoadCode)) {
                    jobNumberText.setText("");
                    jobNumber = "";
                }
                railroadText.setText(railRoadCode);
                checkValidButton();
                break;
        }
    }

    // Set look and feel of enabled/disabled button.
    private void setButtonEnabled(boolean enable, Button button) {
        if (button != null) {
            if (enable) {
                button.setClickable(true);
                button.setTextColor(getResources().getColor(R.color.colorAccent));
                button.setAlpha(1f);
            } else {
                button.setClickable(false);
                button.setTextColor(Color.BLACK);
                button.setAlpha(.4f);
            }
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            jobSetupListener = (DwrJobSetupListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getDialog().toString() + " must implement DwrJobListener");
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
