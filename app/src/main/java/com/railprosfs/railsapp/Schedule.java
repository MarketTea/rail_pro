package com.railprosfs.railsapp;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.data_layout.WorkflowTbl;
import com.railprosfs.railsapp.dialog.JobSetupDialog;
import com.railprosfs.railsapp.dialog.JobPickerDialog;
import com.railprosfs.railsapp.service.NetworkChangeReceiver;
import com.railprosfs.railsapp.service.Refresh;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;
import com.railprosfs.railsapp.ui_support.ScheduleViewModel;
import com.railprosfs.railsapp.ui_support.WorkAdapter;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.KTime;
import com.railprosfs.railsapp.utility.Triplet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 * The Schedule screen is the entry point into the application.  Upon first
 * use it will notice the user needs to proceed to the signin process.  If
 * the user is returning, this screen provides a list of all scheduled jobs
 * for an RWIC, or a set of jobs on specific properties for a supervisor.
 * This screen is the base of all navigation.
 */
public class Schedule extends LoginAzure implements JobSetupDialog.DwrJobSetupListener, JobPickerDialog.JobPickerListener, FragmentTalkBack {

    private RecyclerView recyclerView;
    private ViewGroup layoutSchedule;
    private boolean fabMenuActive = false;
    private FloatingActionButton fabMenu;
    private FloatingActionButton fabFlash;
    private FloatingActionButton fabJobAdd;
    private FloatingActionButton fabDwrAdd;
    private Animation fabSpinOff, fabSpinOn, fabTapOff, fabTapOn, recyDark, recyLight;
    private TextView tvFlash;
    private TextView tvJobAdd;
    private TextView tvDwrAdd;
    private NetworkChangeReceiver mNetworkReceiver;
    private WorkAdapter adapter;
    private Actor user;
    private ProgressBar progressBar;

    private List<DwrTbl> dwrs;
    private List<JobSetupTbl> jobSetups;
    private List<AssignmentTbl> assignments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up main view, model and menus.
        setContentView(R.layout.schedule);

        // Lets see what we know about this person.
        user = new Actor(this);

        initializeWidgets();
        setupBroadcastReceiverForNetworkChange();
        setBackgroundBuild();
        fabAnimationSetup();
        ScheduleViewModel mScheduleViewModel = setupScheduleViewModel();
        setupRecyclerView(mScheduleViewModel);

        // These are call backs to avoid DB use on main thread.
        observeGetAllJobs(mScheduleViewModel);
        observerGetAllDwrs(mScheduleViewModel);
        observeGetAllJobSetups(mScheduleViewModel);
        observeGetAllWorkflowTbl(mScheduleViewModel);
    }

    private void initializeWidgets() {
        progressBar = findViewById(R.id.progressbar);

        fabFlash = findViewById(R.id.fabFlash);
        fabJobAdd = findViewById(R.id.fabJobAdd);
        fabMenu = findViewById(R.id.fabExtras);
        fabDwrAdd = findViewById(R.id.fabDwrAdd);
        tvFlash = findViewById(R.id.lblFlash);
        tvJobAdd = findViewById(R.id.lblJobAdd);
        tvDwrAdd = findViewById(R.id.lblDwrAdd);
        layoutSchedule = findViewById(R.id.layoutSchedule);
    }

    // Likely that this setup is not going to work on later versions of Android.
    // Set up the network change broadcast receiver to trigger NetworkChangeReceiver
    // and see if can upload any pending forms.
    private void setupBroadcastReceiverForNetworkChange() {
        mNetworkReceiver = new NetworkChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    // Set the background color based on the build environment.
    public void setBackgroundBuild() {
        RelativeLayout relativeLayout = findViewById(R.id.layoutSchedule);
        Connection connection = Connection.getInstance();
        Log.d("MARKET_TEA", "CHECK CONNECTION: " + connection.getBuild(this));
        switch (connection.getBuild(this)) {
            case Connection.BUILD_DEV:
                relativeLayout.setBackgroundColor(Color.parseColor("#F8bbd0"));
                break;
            case Connection.BUILD_QA:
                relativeLayout.setBackgroundColor(Color.parseColor("#E6EE9C"));
                break;
            case Connection.BUILD_PROD_QA:
                relativeLayout.setBackgroundColor(Color.parseColor("#FF9800"));// yellow: FFFF8B
                break;
            case Connection.BUILD_JESSE:
                relativeLayout.setBackgroundColor(Color.parseColor("#3EE0AF"));
                break;
        }
    }

    private void fabAnimationSetup() {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    fabMenu.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.rpBlack, Schedule.this.getTheme())));
                }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    fabMenu.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent, Schedule.this.getTheme())));
                }
            }
        });

        fabTapOn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_tap_on);
        fabTapOff = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_tap_off);
        recyDark = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.recycle_darken);
        recyLight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.recycle_lighten);
    }

    // Set up the view model via a factory.
    @NotNull
    private ScheduleViewModel setupScheduleViewModel() {
        // Use a factory to pass in the database key to ViewModel. Could also use a public parameter.
        ScheduleViewModel.Factory factory = new ScheduleViewModel.Factory(getApplication(), user.unique);
        return new ViewModelProvider(this, factory).get(ScheduleViewModel.class);
    }

    // Get the raw data for all the cards to display.
    private void setupRecyclerView(ScheduleViewModel mScheduleViewModel) {
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new WorkAdapter(this, mScheduleViewModel.GetAllJobs().getValue(), mScheduleViewModel.GetDwrsByJob(0), mScheduleViewModel.GetJobSetupByJob(0));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void observeGetAllJobs(ScheduleViewModel mScheduleViewModel) {
        mScheduleViewModel.GetAllJobs().observe(this, new Observer<List<AssignmentTbl>>() {
            @Override
            public void onChanged(@Nullable final List<AssignmentTbl> jobs) {
                assignments = jobs;
                updateAssignments();
                adapter.RefreshData(assignments);
                if (jobs != null) {
                    recyclerView.scrollToPosition(adapter.getTodayPosition());
                }
            }
        });
    }

    private void observerGetAllDwrs(ScheduleViewModel mScheduleViewModel) {
        mScheduleViewModel.GetmAllDwrs().observe(this, new Observer<List<DwrTbl>>() {
            @Override
            public void onChanged(@Nullable List<DwrTbl> dwrTbls) {
                dwrs = dwrTbls;
                updateAssignments();
                adapter.RefreshSubData(dwrs);
            }
        });
    }

    private void observeGetAllJobSetups(ScheduleViewModel mScheduleViewModel) {
        mScheduleViewModel.GetAllJobSetups().observe(this, new Observer<List<JobSetupTbl>>() {
            @Override
            public void onChanged(@Nullable List<JobSetupTbl> jsetupTbls) {
                jobSetups = jsetupTbls;
                updateAssignments();
                adapter.RefreshJsData(jobSetups);
            }
        });
    }

    private void observeGetAllWorkflowTbl(@NotNull ScheduleViewModel mScheduleViewModel) {
        mScheduleViewModel.GetAllWorkflowTbl().observe(this, new Observer<List<WorkflowTbl>>() {
            @Override
            public void onChanged(List<WorkflowTbl> workflowTbls) {
                adapter.updateFailInformation(workflowTbls);
            }
        });
    }

    private void updateAssignments() {
        try {
            if(assignments != null && jobSetups != null && dwrs != null) {
                long ONE_WEEK_AGO = -168; // in hours

                List<AssignmentTbl> duplicatedAssignmentTblData = new ArrayList<>(assignments);
                for (AssignmentTbl assignment: assignments) {
                    int count = 0;
                    for(DwrTbl dwr: dwrs)
                        if(assignment.JobId == dwr.JobId) { count++;  }

                    for(JobSetupTbl job : jobSetups)
                        if(assignment.JobId == job.JobId) { count++;  }

                    boolean shiftOld = KTime.IsPast(assignment.ShiftDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE) < ONE_WEEK_AGO;

                    if(count == 0 && shiftOld) {
                        duplicatedAssignmentTblData.remove(assignment);
                    }
                }
                assignments = duplicatedAssignmentTblData;
                adapter.RefreshData(assignments);
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "Schedule.updateAssignments");
        }
    }

    /* The Options Menu works closely with the ActionBar.  It can show useful menu items on the bar
     * while hiding less used ones on the traditional menu.  The xml configuration determines how they
     * are shown. The system will call the onCreate when the user presses the menu button.
     * Note: Android refuses to show icon+text on the ActionBar in portrait, so deal with it. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuScheduleSettings:
                startActivity(new Intent(this, Settings.class));
                return true;
            case R.id.mnuScheduleRefresh:
                callLogin();
                Intent sIntent = new Intent(this, Refresh.class);
                sIntent.setData(getIntent().getData()); // Passes the new user data if there.
                sIntent.putExtra(REFRESH_LOOP, true);
                sIntent.putExtra(REFRESH_RECIEVER, new RefreshReceiver(new Handler()));
                startService(sIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void ShowJobDetail(View view) {
        try {
            switch (view.getId()) {
                case R.id.btnMoreJI:
                case R.id.btnMore2JI:
                case R.id.imgJobDetaiLink:
                    if (view.getTag() != null) {
                        Intent intent = new Intent(this, Project.class);
                        Bundle mBundle = new Bundle();
                        mBundle.putInt(IN_JOBID, (int) view.getTag());
                        intent.putExtras(mBundle);
                        startActivity(intent);
                    }
                    break;
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".ShowJobInfo");
        }
    }

    // This could be either a flagging or utility Job Setup, although if it exist already, it will sort itself out.
    public void ShowJsDetail(View view) {
        try {
            switch (view.getId()) {
                case R.id.lblJsRow1:
                case R.id.lblJsRow2:
                case R.id.lblJsRow3:
                case R.id.lblJsRow4:
                case R.id.lblJsRow5:
                    if (view.getTag() != null) {
                        @SuppressWarnings("unchecked")
                        Triplet<Integer, Integer, Integer> holdJsetup = (Triplet<Integer, Integer, Integer>) view.getTag();
                        startJobSetup(holdJsetup.getSecond(), Railroads.PropertyName(getApplicationContext(), holdJsetup.getSecond()), 0, holdJsetup.getFirst(), holdJsetup.getThird());
                    }
                    break;
                case R.id.lblJobSetupLink:
                    @SuppressWarnings("unchecked")
                    Triplet<Integer, Integer, Integer> holdJobSetupKeys = (Triplet<Integer, Integer, Integer>) view.getTag();
                    startJobSetup(holdJobSetupKeys.getSecond(), Railroads.PropertyName(getApplicationContext(), holdJobSetupKeys.getSecond()), holdJobSetupKeys.getFirst(), 0, holdJobSetupKeys.getThird());
                    break;
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".ShowJsDetail");
        }
    }

    // We know this is exactly one type of form, we force it to be that.
    public void ShowCsDetail(View view) {
        try {
            switch (view.getId()) {
                case R.id.lblJsRow1:
                case R.id.lblJsRow2:
                case R.id.lblJsRow3:
                case R.id.lblJsRow4:
                case R.id.lblJsRow5:
                    if (view.getTag() != null) {
                        @SuppressWarnings("unchecked")
                        Triplet<Integer, Integer, Integer> holdJsetup = (Triplet<Integer, Integer, Integer>) view.getTag();
                        startJobSetup(holdJsetup.getSecond(), Railroads.PropertyName(getApplicationContext(), holdJsetup.getSecond()), 0, holdJsetup.getFirst(), RP_COVER_SERVICE);
                    }
                    break;
                case R.id.lblCoverSheetLink:
                    @SuppressWarnings("unchecked")
                    Triplet<Integer, Integer, Integer> holdJobSetupKeys = (Triplet<Integer, Integer, Integer>) view.getTag();
                    startJobSetup(holdJobSetupKeys.getSecond(), Railroads.PropertyName(getApplicationContext(), holdJobSetupKeys.getSecond()), holdJobSetupKeys.getFirst(), 0, RP_COVER_SERVICE);
                    break;
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".ShowJsDetail");
        }
    }

    public void ShowDwrDetail(View view) {
        try {
            switch (view.getId()) {
                case R.id.lblDwrRow1:
                case R.id.lblDwrRow2:
                case R.id.lblDwrRow3:
                case R.id.lblDwrRow4:
                case R.id.lblDwrRow5:
                    if (view.getTag() != null) {
                        Intent intent = new Intent(this, DwrEdit.class);
                        Bundle mBundle = new Bundle();
                        mBundle.putInt(IN_DWRID, (int) view.getTag());
                        intent.putExtras(mBundle);
                        startActivity(intent);
                    }
                    break;
                case R.id.lblDwrStartLink:
                    Intent intent = new Intent(this, DwrEdit.class);
                    Bundle mBundle = new Bundle();
                    @SuppressWarnings("unchecked")
                    Triplet<Integer, Integer, Integer> holdJob = (Triplet<Integer, Integer, Integer>) view.getTag();
                    mBundle.putInt(IN_JOBID, holdJob.getFirst());
                    mBundle.putInt(IN_PROPERTYID, holdJob.getSecond());
                    intent.putExtras(mBundle);
                    startActivity(intent);
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".ShowDwrDetail");
        }
    }

    public void ShowFABmenu(View view) {

        if (fabMenuActive) {

            fabMenu.startAnimation(fabSpinOn);
            fabJobAdd.startAnimation(fabTapOff);
            fabFlash.startAnimation(fabTapOff);
            fabDwrAdd.startAnimation(fabTapOff);
            recyclerView.startAnimation(recyLight);
            TransitionManager.beginDelayedTransition(layoutSchedule);
            tvJobAdd.setVisibility(View.INVISIBLE);
            tvFlash.setVisibility(View.INVISIBLE);
            tvDwrAdd.setVisibility(View.INVISIBLE);
            fabJobAdd.setClickable(false);
            fabFlash.setClickable(false);
            fabDwrAdd.setClickable(false);
            fabMenuActive = false;

        } else {

            fabMenu.startAnimation(fabSpinOff);
            fabJobAdd.startAnimation(fabTapOn);
            fabFlash.startAnimation(fabTapOn);
            fabDwrAdd.startAnimation(fabTapOn);
            recyclerView.startAnimation(recyDark);
            TransitionManager.beginDelayedTransition(layoutSchedule);
            tvJobAdd.setVisibility(View.VISIBLE);
            tvFlash.setVisibility(View.VISIBLE);
            tvDwrAdd.setVisibility(View.VISIBLE);
            fabJobAdd.setClickable(true);
            fabFlash.setClickable(true);
            fabDwrAdd.setClickable(true);
            fabMenuActive = true;
        }
    }

    public void LaunchFlashAudit(View view) {
        Intent intent = new Intent(this, FlashAudit.class);
        startActivity(intent);
    }

    public void LaunchNewJobSetup(View view) {
        DialogFragment newFragment = JobSetupDialog.newInstance();
        newFragment.show(getSupportFragmentManager(), KY_JOBSETUP_NEW_FRAG);
    }

    public void LaunchNewDwr(View view) {
        Intent intent = new Intent(this, DwrEdit.class);
        Bundle mBundle = new Bundle();
        mBundle.putInt(IN_PROPERTYID, user.railroad);
        intent.putExtras(mBundle);
        startActivity(intent);
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /*
        Use this method to launch the Job Setup Form.
     */
    @Override
    public void startJobSetup(int railroadKey, String railroadCode, int jobId, int jobSetupId, int jobSetupFormType) {
        Intent intent = new Intent(getApplicationContext(), JobSetup.class);
        Bundle mBundle = new Bundle();
        mBundle.putInt(IN_PROPERTYID, railroadKey);
        mBundle.putString(IN_PROPERTY_CODE, railroadCode);
        mBundle.putInt(IN_JOBID, jobId);
        mBundle.putInt(IN_JSID, jobSetupId);
        mBundle.putInt(IN_JOBSETUP_FORM, jobSetupFormType);
        intent.putExtras(mBundle);
        startActivity(intent);
    }

    /*
        The dialogs on fragments end up sending their data back to the Activity.
     */
    @Override
    public void setJobNumber(JobTbl tbl) {
        FragmentManager mgr = getSupportFragmentManager();
        JobSetupDialog dialog = (JobSetupDialog) mgr.findFragmentByTag(KY_JOBSETUP_NEW_FRAG);
        if (dialog != null) {
            dialog.setJobNumber(tbl);
        }
    }

    /*
        The dialogs on fragments end up sending their data back to the Activity.
     */
    @Override
    public void simpleListResponse(int source, int selection) {
        if(source == R.array.property_name || source == R.array.job_setup_types) {
            FragmentManager mgr = getSupportFragmentManager();
            JobSetupDialog dialog = (JobSetupDialog) mgr.findFragmentByTag(KY_JOBSETUP_NEW_FRAG);
            if (dialog != null) {
                dialog.simpleListResponse(source, selection);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
    }

    private class RefreshReceiver extends ResultReceiver {

        public RefreshReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == REFRESH_CONST) {
                int progress = resultData.getInt(REFRESH_PROGRESS);

                progressBar.setIndeterminate(true);
                // pd variable represents your ProgressDialog
                progressBar.setVisibility(View.VISIBLE);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    /***********************************
     * The methods below are not in use.  Need to be here to allow use of FragmentTalkBack
     */
    @Override
    public void setTimePicker(String totalTime, String startTime, String endTime, int id) {    }

    @Override
    public void setSignatureImage(String imageName, int id) {    }

    @Override
    public void setPictureOnClick(int position) {    }

    @Override
    public void unlockOrientation() {    }

    @Override
    public void lockOrientation() {    }

    @Override
    public void simplePickerResponse(int source, int selection) {    }

    @Override
    public void simpleConfirmResponse(int message) {    }

}
