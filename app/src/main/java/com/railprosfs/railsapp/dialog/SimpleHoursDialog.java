package com.railprosfs.railsapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.KTime;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.railprosfs.railsapp.utility.Constants.*;

public class SimpleHoursDialog extends DialogFragment {
    String AnchorDate;
    FragmentTalkBack mCallback;
    int ID;
    String startTime;
    String endTime;
    String dialogTitle;
    Spinner sSpinner;
    Spinner eSpinner;
    ArrayAdapter<CharSequence> adapter;
    List<String> timeSlots;

    public SimpleHoursDialog() {
        // Dialogs need a default constructor. This loses the supplied AnchorDate, so may want to account for that in the callback.
        AnchorDate = KTime.ParseNow(KTime.KT_fmtDateShrtMiddle).toString();
    }
    public SimpleHoursDialog(String anchorDate) {
        // This is expected to be in the MM/dd/yyyy format (KTime.KT_fmtDateShrtMiddle)
        AnchorDate = anchorDate != null ? anchorDate : KTime.ParseNow(KTime.KT_fmtDateShrtMiddle).toString();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getContext() != null) {
            timeSlots = Arrays.asList(getContext().getResources().getStringArray(R.array.time_slots));
        }
        if (getArguments() != null) {
            ID = getArguments().getInt(IN_PICKER_ID);
            startTime = getArguments().getString(IN_START_TIME);
            endTime = getArguments().getString(IN_END_TIME);
            dialogTitle = getArguments().getString(IN_TITLE_ID);
            dialogTitle = dialogTitle != null ? dialogTitle : getContext().getResources().getString(R.string.title_pick_time);
        }
    }

    @Override
    @NotNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialog);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.spinner_picker_dialog, null);

        sSpinner = v.findViewById(R.id.start_time_spinner);
        eSpinner = v.findViewById(R.id.end_time_spinner);

        if(getContext() != null) {
            adapter = ArrayAdapter.createFromResource(getContext(), R.array.time_slots, android.R.layout.simple_spinner_item);
        }
        sSpinner.setAdapter(adapter);
        eSpinner.setAdapter(adapter);

        setSpinner(startTime, sSpinner);
        setSpinner(endTime, eSpinner);

        builder.setTitle(dialogTitle);
        builder.setView(v);
        // Setting the listener to null tricks the Dialog to not dismiss on press.
        builder.setPositiveButton(R.string.ok, null);
        builder.setNeutralButton(R.string.reset, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null) {
            Button buttonP = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
            buttonP.setOnClickListener(null);
            buttonP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sSpinner.getSelectedItemPosition() != 0 && eSpinner.getSelectedItemPosition() != 0) {
                        mCallback.setTimePicker(getHoursWorked(), getStartDate(), getEndDate(), ID);
                        dismiss();
                    }
                    if(sSpinner.getSelectedItemPosition() == 0 && eSpinner.getSelectedItemPosition() ==0) {
                        mCallback.setTimePicker("", "", "", ID);
                        dismiss();
                    }
                }
            });
            Button buttonN = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL);
            buttonN.setOnClickListener(null);
            buttonN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sSpinner.setSelection(0);
                    eSpinner.setSelection(0);
                }
            });
        }
    }

    // The start date is simple, just add the date and time together and covert to a date format (with offset).
    private String getStartDate() {
        String start = AnchorDate + "T" + timeSlots.get(sSpinner.getSelectedItemPosition()) + ":00";
        try {
            return KTime.ParseToFormat(start, KTime.KT_fmtDateMiddleTime, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "Start Date Failure: " + start);
            return "";
        }
    }

    // The end date might be the next day, so check for that before returning it.
    private String getEndDate() {
        String end = AnchorDate + "T" + timeSlots.get(eSpinner.getSelectedItemPosition()) + ":00";
        try {
            String endFormatted = KTime.ParseToFormat(end, KTime.KT_fmtDateMiddleTime, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
            if (KTime.CalcDateDifferenceNoAbs(getStartDate(), endFormatted, KTime.KT_fmtDate3339k) > 0) {
                Calendar endCal = KTime.ParseToCalendar(endFormatted, KTime.KT_fmtDate3339k, TimeZone.getDefault().getID());
                endCal.add(Calendar.DATE, 1);   // When ending time is before starting time, need to push it forward a day.
                endFormatted = DateFormat.format(KTime.KT_fmtDate3339k, endCal).toString();
            }
            return endFormatted;
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "End Date Failure: " + end);
            return "";
        }
    }
    
    private String getHoursWorked() {
        try {
            long totalminutes = KTime.CalcDateDifference(getStartDate(), getEndDate(), KTime.KT_fmtDateOnlyRPFS, KTime.KT_MINUTES);
            int SINGLE_HOUR = 60;
            int hours = (int) totalminutes / SINGLE_HOUR;
            int leftOverMinutes = (int) (totalminutes % SINGLE_HOUR);
            if (leftOverMinutes < 15 || leftOverMinutes >= 45) {
                leftOverMinutes = 0;
            } else { //  if (leftOverMinutes >= 15 && leftOverMinutes < 45)
                leftOverMinutes = 30;
            }
            String hoursIncluded = "";
            if (getContext() != null) {
                String hoursIncludedFmt = getContext().getResources().getString(R.string.display_hours);
                hoursIncluded = String.format(Locale.getDefault(), hoursIncludedFmt, hours, leftOverMinutes > 0 ? 5 : 0);
            }

            return hoursIncluded;
        } catch (Exception exp) {
            return "0.0 hours";
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            mCallback = (FragmentTalkBack) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TotalTimePickerListener");
        }
    }

    private void setSpinner(String input, Spinner spinner) {
        if (input == null || input.length() < 4) {
            return;
        }
        try {
            String tempValue = KTime.ParseToFormat(input, KTime.KT_fmtDate3339k, "", KTime.KT_fmtDateTime24, "").toString();
            if(tempValue.equalsIgnoreCase("00:00")) { tempValue = "24:00"; }
            for (int i = 0; i < timeSlots.size(); i++) {
                if (timeSlots.get(i).equals(tempValue)) {
                    spinner.setSelection(i);
                    return;
                }
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "input: " + input);
        }
    }
}