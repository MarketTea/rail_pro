package com.railprosfs.railsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.railprosfs.railsapp.data_layout.AnswerTbl;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.FieldPlacementTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data.JobTemplateMgr;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.dialog.SimpleConfirmDialog;
import com.railprosfs.railsapp.dialog.SimpleDisplayDialog;
import com.railprosfs.railsapp.service.DeleteItemThread;
import com.railprosfs.railsapp.service.SaveJobSetupThread;
import com.railprosfs.railsapp.ui_support.JobSetupViewModel;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;
import com.railprosfs.railsapp.ui_support.JobSetupAdapter;
import com.railprosfs.railsapp.utility.LocationAid;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.railprosfs.railsapp.utility.Constants.*;

public class JobSetup extends AppCompatActivity implements FragmentTalkBack {

    private RecyclerView rv;
    private JobSetupAdapter adapter;
    private JobSetupViewModel model;

    private TextView lblSubmit, lblSave, lblDelete;
    private FloatingActionButton fabSubmit, fabMenu, fabSave, fabDelete;
    private Animation fabSpinOff, fabSpinOn, fabTapOff, fabTapOn, recyDark, recyLight;
    private boolean fabMenuActive = false;

    private int jobId;          // The server database id of the Job getting setup.
    private int jobSetupId;     // The database key of an existing Job Setup.
    private int propertyId;     // The local index key of the Property (railroad).
    private String railroad;    // The name (code) of the railroad, just for appearances.
    private int jobSetupForm;   // There are multiple types of Job Setup forms per Property.
                                // Flagging = 0, Utility = 1, Cover = 2
    private LocationAid tracker = null;
    private final int LOC_PERMISSION_KY = 10011;

    /*
        The MsgHandler is used to as a callback mechanism, such that the Thread
        can alert the Activity when the data has been retrieved.
    */
    private static class MsgHandler extends Handler {
        private final WeakReference<JobSetup> mActivity;
        MsgHandler(JobSetup activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            JobSetup activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case WHAT_JOB_SETUP:
                        activity.FormSuccess();
                        break;
                    case WHAT_JOB_SETUP_ERR:
                        activity.FormError();
                        break;
                    case WHAT_JSF_DELETE:
                        activity.DeleteSuccess();
                        break;
                    case WHAT_JSF_DELETE_ERR:
                        activity.DeleteFailure();
                        break;
                }
            }
        }
    }
    private final MsgHandler mHandler = new MsgHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobsetup);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            jobSetupId = bundle.getInt(IN_JSID);            // For existing job setups, here you go.
            propertyId = bundle.getInt(IN_PROPERTYID);      // This is used to get the questions.
            jobId = bundle.getInt(IN_JOBID);                // The Job Setup is linked to a Job.
            railroad = bundle.getString(IN_PROPERTY_CODE);  // Nice to have, but not critical.
            jobSetupForm = bundle.getInt(IN_JOBSETUP_FORM, RP_FLAGGING_SERVICE); // Default (0) is flagging, but can be others.
            if(railroad == null) railroad = "";
        }

        // Widgets
        Toolbar toolbar = findViewById(R.id.toolbar);
        lblSubmit = findViewById(R.id.lblSubmit);
        lblSave = findViewById(R.id.lblSave);
        lblDelete = findViewById(R.id.lblDelete);
        fabDelete = findViewById(R.id.fabDelete);
        fabSubmit = findViewById(R.id.fabSubmit);
        fabSave = findViewById(R.id.fabSave);
        fabMenu = findViewById(R.id.fabExtras);

        // Initialize Widgets
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Animation Setup
        fabSpinOff = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_spin_off);
        fabSpinOff.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                fabMenu.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.rpBlack)));
            }
        });
        fabSpinOn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_spin_on);
        fabSpinOn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                fabMenu.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
            }
        });
        fabTapOn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_tap_on);
        fabTapOff = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_tap_off);
        recyDark = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.recycle_darken);
        recyLight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.recycle_lighten);

        //Setup RecyclerView
        rv = findViewById(R.id.jobsetup_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobSetupAdapter(getApplicationContext());
        rv.setAdapter(adapter);

        // Setup Model
        String holdFmt = getResources().getString(R.string.title_activity_job_setup_extra);
        JobTemplateMgr jobTemplateMgr = new JobTemplateMgr(this);
        // Get what is probably the template, but this can change later if form already exists in db.
        // Default template is a Job Setup Form for Flagging.
        int templateId = 0;
        switch (jobSetupForm) {
            case RP_FLAGGING_SERVICE:
                templateId = jobTemplateMgr.GetJobSetupId(Railroads.PropertyName(getApplicationContext(), propertyId));
                holdFmt = getResources().getString(R.string.title_activity_job_setup_extra);
                break;
            case RP_UTILITY_SERVICE:
                templateId = jobTemplateMgr.GetUtilitySetupId(Railroads.PropertyName(getApplicationContext(), propertyId));
                holdFmt = getResources().getString(R.string.title_activity_utility_setup_extra);
                break;
            case RP_COVER_SERVICE:
                templateId = jobTemplateMgr.GetCoverSheetId(Railroads.PropertyName(getApplicationContext(), propertyId));
                holdFmt = getResources().getString(R.string.title_activity_cover_sheet_extra);
                break;
        }
        JobSetupViewModel.Factory factory = new JobSetupViewModel.Factory(getApplication(), templateId, jobId, propertyId, jobSetupForm);
        model = new ViewModelProvider(this, factory).get(JobSetupViewModel.class);
        if(jobSetupId == 0) { setTitle(String.format(holdFmt, railroad)); } // If there is a job to be loaded, set the title later.

        // Because we are layering answers on top of questions, we need to chain these together to
        // create a series of events.  Otherwise the answers might not have questions to fill in.
        loadAnswers();

        if(!LocationAid.Companion.checkPermissions(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            LocationAid.Companion.askPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, LOC_PERMISSION_KY);
        }

    }

    // Check if we can load an existing form's answers from the database
    public void loadAnswers() {
        model.getJobSetupTbl(jobSetupId).observe(this, new Observer<JobSetupTbl>() {
            @Override
            public void onChanged(JobSetupTbl tbl) {
                if (tbl != null) {  // Only have answers for existing Job Setup forms.
                    model.jobSetupTbl = tbl;
                    // Job Id might not come in the extras bundle of existing Job Setup Forms.
                    model.setJobId(tbl.JobId);
                    // Find the correct template
                    JobTemplateMgr jobTemplateMgr = new JobTemplateMgr(getApplicationContext());
                    String holdFmt = getResources().getString(R.string.title_activity_job_setup_extra);
                    switch (tbl.AssignmentId) {
                        case RP_FLAGGING_SERVICE:
                            model.setTemplateId(jobTemplateMgr.GetJobSetupId(Railroads.PropertyName(getApplicationContext(), propertyId)));
                            model.setSetupFormType(RP_FLAGGING_SERVICE);
                            holdFmt = getResources().getString(R.string.title_activity_job_setup_extra);
                            break;
                        case RP_UTILITY_SERVICE:
                            model.setTemplateId(jobTemplateMgr.GetUtilitySetupId(Railroads.PropertyName(getApplicationContext(), propertyId)));
                            model.setSetupFormType(RP_UTILITY_SERVICE);
                            holdFmt = getResources().getString(R.string.title_activity_utility_setup_extra);
                            break;
                        case RP_COVER_SERVICE:
                            model.setTemplateId(jobTemplateMgr.GetCoverSheetId(Railroads.PropertyName(getApplicationContext(), propertyId)));
                            model.setSetupFormType(RP_COVER_SERVICE);
                            holdFmt = getResources().getString(R.string.title_activity_cover_sheet_extra);
                            break;
                    }
                    setTitle(String.format(holdFmt, railroad));
                    // Load the Questions
                    loadTemplate();
                    //Determines Whether or Not to Hide FabMenu
                    if (model.jobSetupTbl.Status == DWR_STATUS_NEW
                            || model.jobSetupTbl.Status == DWR_STATUS_DRAFT
                            || model.jobSetupTbl.Status == DWR_STATUS_BOUNCED) {
                        ShowFabMenu();
                    } else {
                        HideFabMenu();
                        adapter.setEnabled(false);
                    }
                } else {
                    // Load the Questions
                    loadTemplate();
                }
            }
        });
    }

    // Get the Questions from the database.  Until we have the questions loaded, we do not know
    // where the answers (or defaults) should go, so wait until this is done before getting them.
    public void loadTemplate() {
        model.getFieldTbl().observe(this, new Observer<List<FieldPlacementTbl>>() {
            @Override
            public void onChanged(List<FieldPlacementTbl> fieldPlacementTbls) {
                //Animation for RV
                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation_fall_down);
                rv.setLayoutAnimation(animation);

                //Append Custom Fields to Adapter
                fieldPlacementTbls.add(appendSignatureField());
                fieldPlacementTbls.add(appendSignatureDateField());
                fieldPlacementTbls.add(1, appendJobNumberField());

                adapter.RefreshJobSetupData(fieldPlacementTbls);
                rv.scheduleLayoutAnimation();
                // Load the Answers
                getList();
            }
        });
    }

    // Load answers from database. The FIELD_JSF_TYPE at this point means it came from this
    // "JobSetup" code. Originally there was to be others types of forms that used the answer
    // table, but for now all forms using AnswerTbl are in "JobSetup".
    public void getList() {
        model.getAnswerListTbl(model.getJobSetupTblId(), FIELD_JSF_TYPE).observe(this, new Observer<List<AnswerTbl>>() {
            @Override
            public void onChanged(List<AnswerTbl> answers) {
                if(answers != null && answers.size() > 0) {
                    if (!model.editMode) {
                        model.editMode = true;
                        adapter.RefreshDataContent(answers);
                    }
                } else {
                    loadDefaultAssginment();
                }
            }
        });
    }

    // Populate some fields based on information already present in the Assignment.
    public void loadDefaultAssginment() {
        model.getAssignmentTbl().observe(this, new Observer<AssignmentTbl>() {
            @Override
            public void onChanged(AssignmentTbl assignmentTbl) {
                if(assignmentTbl != null) { // No Assignment if no shift yet.
                    adapter.setDefault(assignmentTbl);
                    model.setmJobNumber(assignmentTbl.JobNumber);
                } else { // fall back to job info
                    loadDefaultJob();
                }
            }
        });
    }

    // Populate some fields based on information already present in the Job.
    public void loadDefaultJob() {
        model.getJobListTbl().observe(this, new Observer<JobTbl>() {
            @Override
            public void onChanged(JobTbl jobTbl) {
                if(jobTbl != null) { // No guarantees in life.
                    // The JobTbl has a little data.  To get everything, will
                    // need to read whole job from server with a thread.
                    model.setmJobNumber(jobTbl.JobNumber);
                    AssignmentTbl holdAssignment = new AssignmentTbl();
                    holdAssignment.JobId = jobTbl.Id;
                    holdAssignment.JobNumber = jobTbl.JobNumber;
                    holdAssignment.CustomerName = jobTbl.CustomerName;
                    adapter.setDefault(holdAssignment);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.mJobSetupAnswers = adapter.getmJobSetupAnswers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setAnswers(model.mJobSetupAnswers);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() ==  android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(tracker == null && LocationAid.Companion.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            tracker = new LocationAid(this, LocationAid.RETRY_INTERVAL, LocationAid.RETRY_LIMIT);
        }
    }

    @Override
    public void onStop() {
        if(tracker != null){
            tracker.close();
            tracker = null;
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults){
        if(requestCode == LOC_PERMISSION_KY){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (tracker == null) {
                    tracker = new LocationAid(this, LocationAid.RETRY_INTERVAL, LocationAid.RETRY_LIMIT);
                }
            }
        }
    }

    //Open and Close Menu
    public void ShowMenu(View view) {
        if (fabMenuActive) {
            OpenMenu();
        } else {
            CloseMenu();
        }
    }

    public void OpenMenu() {
        fabMenu.startAnimation(fabSpinOn);
        fabSubmit.startAnimation(fabTapOff);
        fabSave.startAnimation(fabTapOff);
        rv.startAnimation(recyLight);
        lblSubmit.setVisibility(View.INVISIBLE);
        lblSave.setVisibility(View.INVISIBLE);
        fabSubmit.setClickable(false);
        fabSave.setClickable(false);
        fabDelete.startAnimation(fabTapOff);
        lblDelete.setVisibility(View.GONE);
        fabDelete.setClickable(false);
        fabMenuActive = false;
    }

    public void CloseMenu() {
        fabMenu.startAnimation(fabSpinOff);
        fabSubmit.startAnimation(fabTapOn);
        fabSave.startAnimation(fabTapOn);
        rv.startAnimation(recyDark);
        lblSubmit.setVisibility(View.VISIBLE);
        lblSave.setVisibility(View.VISIBLE);
        fabSubmit.setClickable(true);
        fabSave.setClickable(true);
        fabDelete.startAnimation(fabTapOn);
        lblDelete.setVisibility(View.VISIBLE);
        fabDelete.setClickable(true);
        fabMenuActive = true;
    }

    public void HideFabMenu() {
        fabMenu.setClickable(false);
        fabMenu.hide();
    }

    public void ShowFabMenu() {
        fabMenu.setClickable(true);
        fabMenu.show();
    }

    //*******************************************************************************************/
    /*  All the dialog pickers are located here.  Each picker gets a method to kick it off,    */
    /*  and then all call back into the same method for processing of the result.               */
    /*  It is suggested that for fields used with a picker, they have their EditText android    */
    /*  focus attribute turned off, e.g. android:focusable="false".  This will only allow data  */
    /*  from the picker to be used.                                                             */
    //*******************************************************************************************/

    /**
     *  The basic fragment support for showing a simple confirmation dialog. Note that there
     *  is no expected callback (and so NO Response method) from this dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleDisplayRequest(int title, String message){
        // Set up the fragment.
        FragmentManager mgr = getSupportFragmentManager();
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
        switch (message) {
            case R.string.msg_confirm_submitJobSetup:
                if (!Settings.getUseValidation(this) || (adapter.checkValidation() && Settings.getUseValidation(this))) {
                    SaveJobSetupThread work = new SaveJobSetupThread(this, adapter.getAnswers(), model, SaveJobSetupThread.SUBMIT, new Messenger(mHandler));
                    work.start();
                } else {
                    ValidationJobSetupDialog();
                }
                break;
            case R.string.msg_confirm_exitJobSetup:
                JobSetup.super.onBackPressed();
                break;
            case R.string.msg_confirm_delteJobSetup:
                if(jobSetupId != 0) {
                    DeleteItemThread work = new DeleteItemThread(this, FORM_JSF_TYPE, jobSetupId, new Messenger(mHandler));
                    work.start();
                } else {
                    // No harm in trying to delete something that has not yet been saved.
                    DeleteSuccess();
                }
                break;
            case R.string.msg_inform_validation_js:
                // Informational Only
                break;
        }
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleConfirmRequest(int title, int message, boolean showCancel){
        // Set up the fragment.
        FragmentManager mgr = getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_CONFIRM_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the confirmation.
        DialogFragment submitFrag = new SimpleConfirmDialog(title, message, !showCancel);
        submitFrag.show(mgr, KY_SIMPLE_CONFIRM_FRAG);
    }

    /*
     *  This confirmation dialog is used to submit the Job Setup to the backend.
     */
    public void SubmitJobSetupPicker(View view) {
        simpleConfirmRequest(R.string.title_confirm_submitJobSetup, R.string.msg_confirm_submitJobSetup, true);
    }

    /*
     *  This confirmation dialog is used to submit the DWR to the backend.
     */
    public void DeleteJobSetupPicker(View view) {
        simpleConfirmRequest(R.string.title_confirm_deleteJobSetup, R.string.msg_confirm_delteJobSetup, true);
    }

    /*
     *  This confirmation dialog is used to warn the user they will lose any changes.
     */
    @Override
    public void onBackPressed() {
        simpleConfirmRequest(R.string.title_confirm_exitJobSetup, R.string.msg_confirm_exitJobSetup, true);
    }

    /*
     *  This informational dialog is used to inform the user that the validation failed.
     */
    public void ValidationJobSetupDialog() {
        simpleConfirmRequest(R.string.title_inform_validation, R.string.msg_inform_validation_js, false);
    }

    /*
     *  This lets the user know there was a problem trying to delete their form and they should try again or give up.
     */
    public void DeleteFailure(){
        simpleConfirmRequest(R.string.title_inform_delete, R.string.msg_inform_deletion_fail, false);
    }
    /******************* End of Dialog Pickers *******************************/

    public void saveJobSetupData(View view) {
        SaveJobSetupThread work = new SaveJobSetupThread(this, adapter.getAnswers(), model, SaveJobSetupThread.SAVE, new Messenger(mHandler));
        work.start();
    }

    public FieldPlacementTbl appendSignatureField() {
        FieldPlacementTbl tbl = new FieldPlacementTbl();
        tbl.FieldType = JobSetupAdapter.SIGNATURE;
        tbl.Required = true;
        tbl.FieldPrompt = "Signature";
        switch (model.getSetupFormType()){
            case RP_UTILITY_SERVICE:
                tbl.FieldInstructions = "Contractor Signature*";
                break;
            case RP_COVER_SERVICE:
                tbl.FieldInstructions = "Observer Signature*";
                break;
            case RP_FLAGGING_SERVICE:
                tbl.FieldInstructions = "RWIC Signature*";
                break;
            default:
                tbl.FieldInstructions = "Signature*";
        }

        return tbl;
    }

    public FieldPlacementTbl appendSignatureDateField() {
        FieldPlacementTbl tbl = new FieldPlacementTbl();
        tbl.FieldType = JobSetupAdapter.DATE;
        tbl.Required = true;
        tbl.FieldPrompt = "Signature Date";
        tbl.FieldInstructions = "";
        return tbl;
    }

    public FieldPlacementTbl appendJobNumberField() {
        FieldPlacementTbl tbl = new FieldPlacementTbl();
        tbl.FieldType = JobSetupAdapter.INPUTTEXT;
        tbl.Required = true;
        tbl.FieldPrompt = "Job Number";
        tbl.FieldInstructions = "";
        return tbl;
    }

    public void FormSuccess() {
        finish();
    }

    public void FormError() {
        CloseMenu();
        simpleDisplayRequest(R.string.title_file_not_saved, getResources().getString(R.string.msg_not_saved_jobsetup));
    }

    public void DeleteSuccess() {
        Intent intent = new Intent(getApplicationContext(), Schedule.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
    }

    public String GetJobNumber() {
        return model.getmJobNumber();
    }

    public String GetCurrentCoordinates(){
        if(tracker != null){
            Location gps = tracker.getCachedLocation();
            return String.format(Locale.US, DELIMIT_GPS_FORMATED, gps.getLatitude(), gps.getLongitude());
        }
        return getResources().getString(R.string.err_location_not_found);
    }
    /***************************************
     * These are the used interface methods
     */
    @Override
    public void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
    @Override
    public void lockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    /****************************************
     * These are unused interface methods.
     */
    @Override
    public void setTimePicker(String totalTime, String startTime, String endTime, int id) { }
    @Override
    public void setSignatureImage(String imageName, int id) { }
    @Override
    public void setPictureOnClick(int position) { }
    @Override
    public void setJobNumber(JobTbl tbl) { }
    @Override
    public void simplePickerResponse(int source, int selection) { }
    @Override
    public void simpleListResponse(int source, int selection) { }

}