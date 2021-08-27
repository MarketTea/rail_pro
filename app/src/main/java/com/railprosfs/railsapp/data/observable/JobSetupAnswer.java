package com.railprosfs.railsapp.data.observable;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;
import com.railprosfs.railsapp.BR;
import com.railprosfs.railsapp.JobSetup;
import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.dialog.SignatureDialog;
import com.railprosfs.railsapp.dialog.SimpleConfirmDialog;
import com.railprosfs.railsapp.dialog.SimpleDisplayDialog;
import com.railprosfs.railsapp.dialog.SimpleListDialog;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.KTime;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static com.railprosfs.railsapp.utility.Constants.DELIMIT_GPS_REGEX;
import static com.railprosfs.railsapp.utility.Constants.KY_SIGN_PICKER;
import static com.railprosfs.railsapp.utility.Constants.KY_SIMPLE_CONFIRM_FRAG;
import static com.railprosfs.railsapp.utility.Constants.KY_SIMPLE_DISPLAY_FRAG;
import static com.railprosfs.railsapp.utility.Constants.KY_SIMPLE_LIST_FRAG;
import static com.railprosfs.railsapp.utility.Constants.MAP_NAME_COORDINATES;
import static com.railprosfs.railsapp.utility.Constants.MAP_WITH_COORDINATES;

public class JobSetupAnswer extends RRBaseObservable implements FragmentTalkBack {
    private int answerID;
    private String userInput;
    private boolean yesNo = false;
    private String date;
    private String signatureFileName;
    private boolean invalid;
    private List<String> GoodFairPoor;
    private List<String> MainOrSiding;
    private List<String> PipeOrWire;
    private List<String> FireWater;
    private List<String> StatesUSA;
    private View privateView;           // Keep some context around for later.

    public int specialQuestionID; //This ID is used Only For Manual Entry in JobSetupTbl
    public boolean ViewVisible;   //Determine if a view is grey/clickable or not


    @BindingAdapter("errorText")
    public static void setErrorMessage(TextInputLayout view, boolean invalid) {
        if (invalid) {
            view.setError("Invalid Input Data");
        } else {
            view.setError(null);
            view.clearFocus();
        }
    }

    @BindingAdapter("enableError")
    public static void setEnableError(TextView view, boolean invalid) {
        if (invalid) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @Bindable
    public String getUserInput() {
        return userInput != null ? userInput : "";
    }

    @Bindable
    public boolean isYesNo() {
        return yesNo;
    }

    @Bindable
    public boolean isInvalid() {
        return invalid;
    }

    @Bindable
    public String getDate() {
        return date;
    }

    @Bindable
    public String getSignatureFileName() {
        return signatureFileName;
    }

    @Bindable
    public boolean isViewVisible() {
        return ViewVisible;
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog. Note that there
     *  is no expected callback (and so NO Response method) from this dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleDisplayRequest(Context ctx, int title, String message){
        // Set up the fragment.
        FragmentManager mgr = ((FragmentActivity) ctx).getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_DISPLAY_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the confirmation.
        DialogFragment submitFrag = new SimpleDisplayDialog(title, message);
        submitFrag.show(mgr, KY_SIMPLE_DISPLAY_FRAG);
    }

    /**
     *  Simple Confirmation Dialog callback.  The simple confirmation dialog is used by
     *  a number of methods.  This is where the confirmation reply (OK or Cancel) is returned.
     *  The message passed in is used as the key to determine what code needs to run.
     *  NOTE: For confirmations that end up here it means the user pressed OK.
     * @param message   The unique message displayed on the confirmation dialog.
     */
    @Override
    public void simpleConfirmResponse(int message) {
        JobSetup jobSetup = null;
        if(message == R.string.msg_confirm_gps) {
            if(privateView != null)
                jobSetup = (JobSetup) privateView.getContext();
                this.setUserInput(jobSetup.GetCurrentCoordinates());
        }
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleConfirmRequest(Context ctx, int title, int message, boolean showCancel){
        // Set up the fragment.
        FragmentManager mgr = ((FragmentActivity) ctx).getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_CONFIRM_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the confirmation.
        SimpleConfirmDialog submitFrag = new SimpleConfirmDialog(title, message, !showCancel);
        submitFrag.ChangeListener(this);
        submitFrag.show(mgr, KY_SIMPLE_CONFIRM_FRAG);
    }


    /**
     *  Simple List Dialog callback. The simple listbox picker is used by a number of
     *  methods to pick from a long list with a single tap.  This method is where the
     *  selection is returned. The source array is used as the key to determine what
     *  code needs to deal with the resulting selection.
     * @param source    The selection index, e.g chosen value.
     * @param selection The resource key of the original array provided as the list.
     */
    @Override
    public void simpleListResponse(int source, int selection){

        switch (source) {
            case R.array.good_fair_poor:
                this.setUserInput(GoodFairPoor.get(selection));
                break;
            case R.array.main_or_siding:
                this.setUserInput(MainOrSiding.get(selection));
                break;
            case R.array.pipe_or_wire:
                this.setUserInput(PipeOrWire.get(selection));
                break;
            case R.array.fire_water:
                this.setUserInput(FireWater.get(selection));
                break;
            case R.array.state_name:
                this.setUserInput(StatesUSA.get(selection));
                break;
        }
    }

    /**
     *  The basic fragment support for showing a simple listbox picker.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param source    The resource id of an array of values used to provide selection options.
     * @param values    An optional list of values to use instead of the one hardcoded in source.
     */
    private void simpleListRequest(Context ctx, int title, int source, List<String> values) {
        // Set up the fragment.
        FragmentManager mgr = ((FragmentActivity) ctx).getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_LIST_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the picker.
        SimpleListDialog picker = new SimpleListDialog(title, source);
        if(values != null){
            picker.ChangeListArray(values);
        }
        picker.ChangeListener(this);
        picker.show(mgr, KY_SIMPLE_LIST_FRAG);
    }
    private void simpleListRequest(Context ctx, int title, int source) { simpleListRequest(ctx, title, source, null); }

    // In order to keep the action in this particular "answer" view, need to save off the array values while we have a context.
    public void onStatesUSAClicked(View view) {
        StatesUSA = Arrays.asList(view.getContext().getResources().getStringArray(R.array.state_name));
        simpleListRequest(view.getContext(), R.string.title_pick_state, R.array.state_name);
    }

    // In order to keep the action in this particular "answer" view, need to save off the array values while we have a context.
    public void onGoodFairPoorClicked(View view) {
        GoodFairPoor = Arrays.asList(view.getContext().getResources().getStringArray(R.array.good_fair_poor));
        simpleListRequest(view.getContext(), R.string.title_pick_goodfairpoor, R.array.good_fair_poor);
    }

    // In order to keep the action in this particular "answer" view, need to save off the array values while we have a context.
    public void onPipeOrWireClicked(View view) {
        PipeOrWire = Arrays.asList(view.getContext().getResources().getStringArray(R.array.pipe_or_wire));
        simpleListRequest(view.getContext(), R.string.title_pick_pipeorwire, R.array.pipe_or_wire);
    }

    // In order to keep the action in this particular "answer" view, need to save off the array values while we have a context.
    public void onFireWaterClicked(View view) {
        FireWater = Arrays.asList(view.getContext().getResources().getStringArray(R.array.fire_water));
        simpleListRequest(view.getContext(), R.string.title_pick_category, R.array.fire_water);
    }

    // In order to keep the action in this particular "answer" view, need to save off the array values while we have a context.
    public void onMainOrSidingClicked(View view) {
        MainOrSiding = Arrays.asList(view.getContext().getResources().getStringArray(R.array.main_or_siding));
        simpleListRequest(view.getContext(), R.string.title_pick_goodfairpoor, R.array.main_or_siding);
    }

    // To give us some time to make sure the GPS coordinates are available, we stall with a helpful dialog.
    public void onGpsCoordinatesClicked(View view) {
        privateView = view;
        simpleConfirmRequest(view.getContext(), R.string.title_pick_gps, R.string.msg_confirm_gps, true);
    }

    // To give us some time to make sure the GPS coordinates are available, we stall with a helpful dialog.
    public void onGpsMapClicked(View view) {
        String[] coordinates = getUserInput().split(DELIMIT_GPS_REGEX);
        if (coordinates.length == 2) {
            String latitude = coordinates[0];
            String longitude = coordinates[1];
            JobSetup jobSetup = (JobSetup) view.getContext();
            String link = String.format(MAP_WITH_COORDINATES, latitude, longitude,
                    Uri.encode(String.format(MAP_NAME_COORDINATES, jobSetup.GetJobNumber()))); // geo:0,0?q=<latitude>,<longitude>(Job #)
            Uri mapUri = Uri.parse(link);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(mapIntent);
        }
    }

    public void onDateClicked(View view) {
        Context ctx = view.getContext();
        //Todo: Fix Listener and yaer month dayofmonth
        final DatePickerDialog dpd;
        dpd = new DatePickerDialog(ctx, null, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        dpd.setCancelable(true);
        dpd.setButton(DialogInterface.BUTTON_POSITIVE, ctx.getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Calendar work = Calendar.getInstance();
                work.set(dpd.getDatePicker().getYear(), dpd.getDatePicker().getMonth(), dpd.getDatePicker().getDayOfMonth(), 0, 0, 0);
                date = DateFormat.format(KTime.KT_fmtDateShrtMiddle, work).toString();
                notifyPropertyChanged(BR.date);
            }
        });
        dpd.create();
        dpd.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(ctx, R.color.colorAccent));
        dpd.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(ctx, R.color.colorAccent));
        if (date != null) {
            try {
                Calendar work = KTime.ParseToCalendar(date, KTime.KT_fmtDate3339fk_xS, KTime.UTC_TIMEZONE);
                work = KTime.ConvertTimezone(work, TimeZone.getDefault().getID());
                dpd.getDatePicker().updateDate(work.get(Calendar.YEAR), work.get(Calendar.MONTH), work.get(Calendar.DAY_OF_MONTH));
            } catch (ExpParseToCalendar expParseToCalendar) {
                /* The default is today, which is find. */
            }
        }
        dpd.show();
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onSignatureClicked(View view) {
        setUserInput(KTime.ParseNow(KTime.KT_fmtDate3339fk_xS).toString());

        //Check Permission Before Allowing User to Use this Functionality, If they don't have then cancel
        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!hasPermissions(view.getContext(), PERMISSIONS)) {
            simpleDisplayRequest(view.getContext(), R.string.title_need_permission, view.getContext().getResources().getString(R.string.msg_need_permission_storage));
            return;
        }

        FragmentTransaction ft = ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction();
        Fragment prev = ((FragmentActivity) view.getContext()).getSupportFragmentManager().findFragmentByTag(KY_SIGN_PICKER);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment sigFrag = new SignatureDialog();
        privateView = view;
        ((SignatureDialog) sigFrag).setmCallback(this);
        sigFrag.show(ft, KY_SIGN_PICKER);
    }

    public JobSetupAnswer(int answerID) {
        this.answerID = answerID;
    }

    public void setAnswerID(int answerID) {
        this.answerID = answerID;
    }

    public int getAnswerID() {
        return answerID;
    }

    public JobSetupAnswer setUserInput(String userInput) {
        this.userInput = userInput;
        notifyPropertyChanged(BR.userInput);
        return this;
    }

    public void setUserInputNoOverride(String userInput) {
        if(userInput == null || userInput.length() == 0) { return; }
        if (this.userInput == null || this.userInput.length() == 0) {
            setUserInput(userInput);
        }
    }

    public void setYesNo(boolean yesNo) {
        this.yesNo = yesNo;
        notifyPropertyChanged(BR.yesNo);
    }

    public void setInvalid(boolean invalid) {
        if (this.invalid != invalid) {
            this.invalid = invalid;
            notifyPropertyChanged(BR.invalid);
        }
    }

    public JobSetupAnswer setDate(String date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
        return this;
    }

    public JobSetupAnswer setViewVisible(boolean viewVisible) {
        ViewVisible = viewVisible;
        notifyPropertyChanged(BR.viewVisible);
        return this;
    }

    @Override
    public void setTimePicker(String totalTime, String startTime, String endTime, int id) {

    }
    @Override
    public void simplePickerResponse(int source, int selection){

    }

    @Override
    public void setSignatureImage(String imageName, int id) {
        this.signatureFileName = imageName;
        notifyPropertyChanged(BR.signatureFileName);
    }

    @Override
    public void setPictureOnClick(int position) {

    }

    @Override
    public void setJobNumber(JobTbl tbl) {

    }

    @Override
    public void unlockOrientation() {
        ((FragmentActivity) privateView.getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void lockOrientation() {
        ((FragmentActivity) privateView.getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }
}
