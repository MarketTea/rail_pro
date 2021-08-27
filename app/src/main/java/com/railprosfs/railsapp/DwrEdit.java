package com.railprosfs.railsapp;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.databinding.DwreditBinding;
import com.railprosfs.railsapp.dialog.JobPickerDialog;
import com.railprosfs.railsapp.dialog.SignatureDialog;
import com.railprosfs.railsapp.dialog.SimpleConfirmDialog;
import com.railprosfs.railsapp.dialog.SimpleDisplayDialog;
import com.railprosfs.railsapp.dialog.SimpleListDialog;
import com.railprosfs.railsapp.dialog.SimplePickerDialog;
import com.railprosfs.railsapp.dialog.SimpleHoursDialog;
import com.railprosfs.railsapp.service.CostCenterThread;
import com.railprosfs.railsapp.service.DeleteItemThread;
import com.railprosfs.railsapp.service.JobOneThread;
import com.railprosfs.railsapp.service.SaveDwrThread;
import com.railprosfs.railsapp.service.WebServiceModels;
import com.railprosfs.railsapp.data.observable.DwrItem;
import com.railprosfs.railsapp.ui_support.DwrViewModel;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;
import com.railprosfs.railsapp.ui_support.LayoutHelperDwr;
import com.railprosfs.railsapp.ui_support.PictureAdapter;
import com.railprosfs.railsapp.ui_support.PictureField;
import com.railprosfs.railsapp.utility.Constants;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;
import com.railprosfs.railsapp.utility.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.railprosfs.railsapp.utility.Constants.*;

public class DwrEdit extends AppCompatActivity implements
        View.OnClickListener,
        FragmentTalkBack,
        SpellCheckerSession.SpellCheckerSessionListener {

    private final int MY_PERMISSIONS_REQUEST_STORAGE = 1;
    private final int MY_PERMISSIONS_CAMERA = 2;
    private final int CAMERA_REQUEST = 1888;
    private final int CAMERA_REQUEST_ADAPTER = 1999;
    private final int IMAGE_REQUEST = 1777;

    private int mDwrId = 0;
    private int mJobId = 0;
    private int mPropertyId = 0;
    private long timeEnter = 0;
    private Actor mUser;
    private DwrViewModel mDwrViewModel;
    private List<TextWatcher> mWatchListeners;
    private boolean fabMenuToggle = false;
    private DatePickerDialog datePickerDialog;
    private AppCompatImageView lblStatusDwrIcon;
    private RecyclerView photoRV;
    private PictureAdapter mPicAdapter;
    private DwreditBinding binding;
    private Map<String, Integer> PerDiemList;
    private Observer<List<String>> mSubDivisionObserver;
    private Observer<AssignmentTbl> mAssignmentObserver;
    private List<WebServiceModels.OfferedService> offeredServices;
    int serviceType = -1;
    /*
     *  Most of the on screen widgets are managed using the Android binding
     *  library.  For widgets that are not directly linked to saved data or
     *  ones we need to do a more fancy manipulation, it is usually easier
     *  to just do it directly than w/ a custom binding.
     */
    private EditText mileageToJob;
    private EditText mileageFromJob;
    private EditText mileageJob;
    private EditText workHoursRounded;
    private EditText pictureComment;
    private EditText costCenters;
    private LinearLayout costCentersLayout;
    private LinearLayout layoutSigninSheet;
    private LinearLayout layoutProtection;

    private Animation fabSpinOff, fabSpinOn, fabTapOff, fabTapOn, recyDark, recyLight;
    private TextView lblSave, lblSubmit, lblDelete;
    private FloatingActionButton fabSave, fabMenu, fabSubmit, fabDelete;

    // Spellchecker overrides.
    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) { }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {    }

    // Allow temporary orientation lock.
    @Override
    public void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void lockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    /*
        This handler is used to as a callback mechanism, such that the Thread
        can alert the Activity when the data has been retrieved.
    */
    private static class MsgHandler extends Handler {
        private final WeakReference<DwrEdit> mActivity;

        MsgHandler(DwrEdit activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            DwrEdit activity = mActivity.get();
            if (activity != null) {
                Log.d("MARKET_TEA", "msg dwr edit: " + msg.what);
                switch (msg.what) {
                    case WHAT_DWR_SAVE: // 102
                        activity.ExitDwr();
                        break;
                    case WHAT_DWR_DETAIL_ERR:
                        activity.DwrRefreshErr(msg.arg1);
                        break;
                    case WHAT_DWR_DUPLICATE_SUBMISSION:
                        activity.DwrDuplicateErr(msg.arg1);
                        break;
                    case WHAT_DWR_DELETE:
                        activity.DeleteSuccess();
                        break;
                    case WHAT_DWR_DELETE_ERR:
                        activity.DeleteFailure();
                        break;
                    case WHAT_COSTCENTERS: // 109
                        activity.ManageCostCenter((String) msg.obj);
                        activity.mDwrViewModel.dwrItem.setRestrictionsJob((String) msg.obj);
                        break;
                    case WHAT_JOB_ONE: // 112
                        WebServiceModels.JobDetailResponse jobResponse = (WebServiceModels.JobDetailResponse)msg.obj;
                        String holdRestrictions = "";
                        if(jobResponse.isNonBillable) {
                            holdRestrictions = JOB_RESTRIC_BILLABLE;
                        }
                        activity.mDwrViewModel.dwrItem.setRestrictionsJob(holdRestrictions);

                        activity.jobChangedHandle(jobResponse);
                    case WHAT_JOB_ONE_ERR:
                    case WHAT_COSTCENTER_ERR:
                        // if the call fails, just move on.
                        break;
                }
            }
        }
    }

    private final MsgHandler mHandler = new MsgHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        setupMainViewAndMenu(extras);
        initializeDwrViewModel();
        hookUpBindings();
        setupMainViewAndMenu(savedInstanceState);

        // These are call backs to avoid DB used on main thread.
        getReportObservable();
        getByJobIdObersvable();
        getAllPerDiemsObservable();
        getImagesObservable();

        // This is named (not anonymous) to allow for manual refresh.
        setupManualRefreshObserver();

        // Who is this.
        mUser = new Actor(this);

        // Declare Widgets
        initializeAndSetupWidgets();

        // Animation Setup
        fabAnimationSetup();

        //Persistence through rotation
        persistenceLoad();
    }

    /**
     * Set up main view and menu.  Getting the railroad (IN_PROPERTYID) is helpful as it
     * is used (along with classification) to determine the exact layout.  While it could
     * be picked up when we get the Job info, getting it early reduces screen jitter.
     * @param extras Bundle to null check
     */
    private void setupMainViewAndMenu(Bundle extras) {
        if (extras != null) {
            mDwrId = extras.getInt(IN_DWRID, 0);
            mJobId = extras.getInt(IN_JOBID, 0);
            mPropertyId = extras.getInt(IN_PROPERTYID, 0);
        }
    }

    /**
     * Use a factory to pass in the database key to ViewModel. Could also use a public parameter.
     */
    private void initializeDwrViewModel() {
        DwrViewModel.Factory factory = new DwrViewModel.Factory(getApplication(), mDwrId, mJobId, Railroads.PropertyNameServer(getApplicationContext(),  mPropertyId));
        mDwrViewModel = ViewModelProviders.of(this, factory).get(DwrViewModel.class);
    }

    private void hookUpBindings() {
        binding = DataBindingUtil.setContentView(this, R.layout.dwredit);
        binding.setDwr(mDwrViewModel.dwrItem);
    }

    private void getReportObservable() {
        mDwrViewModel.GetReport().observe(this, new Observer<DwrTbl>() {
            @Override
            public void onChanged(@Nullable final DwrTbl dwr) {
                LogUtil.debug(LogUtil.LOAD_DWR_ITEM + " mDwrViewModel.dwrItem = " + mDwrViewModel.dwrItem.toJSON());
                if (dwr != null) {  // No DWR locally, so just skip this and let the onStart() initialize stuff.
                    if(mDwrViewModel.isReloadOK) { // Avoid over-writing stuff the user might have entered.
                        FillDwrForm(dwr);
                        // The idea is to not accidentally overwrite user input and once the DWR
                        // is submitted, it should not be changed unless rejected.
                        if (dwr.Status == DWR_STATUS_NEW || dwr.Status == DWR_STATUS_DRAFT || dwr.Status == DWR_STATUS_BOUNCED) {
                            mDwrViewModel.isReloadOK = false;
                            FabMenuEnable();
                        } else { // Disable Everything: Adapters and Views
                            mPicAdapter.toggleAdapterClickablility(false);
                            FabMenuDisable();
                            disableEnableControls(false, (ViewGroup) binding.dwrContents.getRoot());
                            lblStatusDwrIcon.setEnabled(true); // ok if this field is enabled, just used for troubleshooting.
                        }
                    } else {
                        ShowHideForms(mDwrViewModel.CurrentFormType, mPropertyId);
                    }
                }
            }
        });
    }

    private boolean hasRoadwayFlaggingFromResponse() {
        if(offeredServices != null) {
            for (WebServiceModels.OfferedService offeredService: offeredServices) {
                if(offeredService.type.id == RP_ROADWAY_FLAGGING_SERVICE) {
                    mDwrViewModel.dwrItem.setHasRoadwayFlagging(true);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPropertyCsx(String property) {
        return Railroads.GetTemplateKey(property) == Railroads.DWR_TEMPLATE_CSX;

    }

    private boolean isPropertyScraa(String property) {
        return Railroads.GetTemplateKey(property) == Railroads.DWR_TEMPLATE_SCRRA;
    }

    private boolean isBillableDay() {
        String billableDayUtility = Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(DWR_TYPE_BILLABLE_DAY);
        return billableDayUtility.equals(mDwrViewModel.dwrItem.getClassification());
    }

    private boolean isBillableDayUtility() {
        String billableDayUtility = Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(DWR_TYPE_BILLABLE_DAY_UTILITY);
        return billableDayUtility.equals(mDwrViewModel.dwrItem.getClassification());
    }

    private boolean hasRoadwayFlagging() {
        if(serviceType == RP_ROADWAY_FLAGGING_SERVICE || mDwrViewModel.dwrItem.hasRoadwayFlagging) {
            mDwrViewModel.dwrItem.setHasRoadwayFlagging(true);
            return true;
        }
        return false;
    }

    private void jobChangedHandle(WebServiceModels.JobDetailResponse jobResponse) {
        if(jobResponse.id != mDwrViewModel.getJobId()) {
            mDwrViewModel.dwrItem.setEightyTwoT("");
            mDwrViewModel.dwrItem.setStreetName("");
            mDwrViewModel.dwrItem.setMilePostsForStreet("");
        }
        mDwrViewModel.setJobId(jobResponse.id);
        offeredServices = jobResponse.offeredServices;
        mDwrViewModel.dwrItem.hasRoadwayFlagging = false;
        ShowHideRoadwayFlagging(-1, mDwrViewModel.dwrItem.hasRoadwayFlagging);
        mDwrViewModel.GetJobAssignment().observe(this, new Observer<AssignmentTbl>() {
            @Override
            public void onChanged(@Nullable final AssignmentTbl job) {
                serviceType = -1;

                if (job != null) {
                    serviceType = job.ServiceType;
                    if(mDwrViewModel.isReloadOK) { // Avoid over-writing stuff the user might have entered.  Not huge risk, as this mostly triggers only on new form.
                        mDwrViewModel.dwrItem.setJobNumber(job.JobNumber);
                        mDwrViewModel.dwrItem.setRoadMaster(job.TrackSupervisor);
                        if (Functions.DefaultForNull(job.SupervisorRP).equalsIgnoreCase(SUPERVISOR_JOB)) {
                            mDwrViewModel.dwrItem.setSupervisorJob(true);
                        } else {
                            mDwrViewModel.dwrItem.setSupervisorJob(false);
                        }
                        if (job.ServiceType == RP_UTILITY_SERVICE) {
                            mDwrViewModel.dwrItem.setContractName(job.PermitNumber);
                            mDwrViewModel.CurrentFormType = DWR_TYPE_BILLABLE_DAY_UTILITY;
                            mDwrViewModel.dwrItem.setClassification(Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(DWR_TYPE_BILLABLE_DAY_UTILITY));
                            ShowHideForms(mDwrViewModel.CurrentFormType, mPropertyId);
                        }
                    }
                }

                if(!isBillableDayUtility()) {
                    if(serviceType != RP_ROADWAY_FLAGGING_SERVICE && hasRoadwayFlaggingFromResponse()) {
                        ConfirmJobServiceDialog();
                    } else {
                        ShowHideRoadwayFlagging(serviceType, mDwrViewModel.dwrItem.hasRoadwayFlagging);
                    }
                }
            }
        });
    }

    private void getByJobIdObersvable() {
        mDwrViewModel.GetJobAssignment().observe(this, new Observer<AssignmentTbl>() {
            @Override
            public void onChanged(@Nullable final AssignmentTbl job) {
                serviceType = -1;
                if (job != null) {
                    serviceType = job.ServiceType;
                    if(mDwrViewModel.isReloadOK) { // Avoid over-writing stuff the user might have entered.  Not huge risk, as this mostly triggers only on new form.
                        mDwrViewModel.dwrItem.setJobNumber(job.JobNumber);
                        mDwrViewModel.dwrItem.setRoadMaster(job.TrackSupervisor);
                        if (Functions.DefaultForNull(job.SupervisorRP).equalsIgnoreCase(SUPERVISOR_JOB)) {
                            mDwrViewModel.dwrItem.setSupervisorJob(true);
                        } else {
                            mDwrViewModel.dwrItem.setSupervisorJob(false);
                        }
                        if (job.ServiceType == RP_UTILITY_SERVICE) {
                            mDwrViewModel.dwrItem.setContractName(job.PermitNumber);
                            mDwrViewModel.CurrentFormType = DWR_TYPE_BILLABLE_DAY_UTILITY;
                            mDwrViewModel.dwrItem.setClassification(Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(DWR_TYPE_BILLABLE_DAY_UTILITY));
                            ShowHideForms(mDwrViewModel.CurrentFormType, mPropertyId);
                        }
                    }
                }
                ShowHideRoadwayFlagging(serviceType, mDwrViewModel.dwrItem.hasRoadwayFlagging);
            }
        });
    }

    private void getAllPerDiemsObservable() {
        mDwrViewModel.GetAllPerDiems().observe(this, new Observer<List<DwrTbl>>() {
            @Override
            public void onChanged(List<DwrTbl> dwrTbls) {
                PerDiemList = new HashMap<>();
                for (DwrTbl dwr:dwrTbls) {
                    if (mDwrId != dwr.DwrId) {
                        try {
                            String perDiemDate = KTime.ParseToFormat(dwr.WorkDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()).toString();
                            PerDiemList.put(perDiemDate, dwr.DwrId);
                        } catch (Exception ex) { /* Just skip it if data not good. */ }
                    }
                }
            }
        });
    }

    private void getImagesObservable() {
        mDwrViewModel.GetImages().observe(this, new Observer<List<DocumentTbl>>() {
            @Override
            public void onChanged(List<DocumentTbl> documentTbls) {
                if (documentTbls != null && documentTbls.size() > 0) {
                    if(mDwrViewModel.isReloadImageOK) { // Avoid over-writting stuff the user might have entered.
                        mDwrViewModel.isReloadImageOK = false;
                        mPicAdapter.generatePictureFields(documentTbls);
                        for (DocumentTbl t : documentTbls) {
                            if (t.DocumentType == 4) {
                                mDwrViewModel.dwrItem.setPictureSignInUri(mPicAdapter.getUri(t.FileName));
                            }
                        }
                    }
                }
            }
        });
    }

    private void setupManualRefreshObserver() {
        mSubDivisionObserver = new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> subdivs) {
                mDwrViewModel.dwrItem.SubDivisionList = subdivs;
            }
        };
        mDwrViewModel.GetSubdivisions().observe(this, mSubDivisionObserver);

        mAssignmentObserver = new Observer<AssignmentTbl>() {
            @Override
            public void onChanged(AssignmentTbl assignmentTbl) {
                if(assignmentTbl != null) {
                    ManageCostCenter(assignmentTbl.CostCenters);
                    mDwrViewModel.dwrItem.setRestrictionsJob(assignmentTbl.Restrictions);
                }
            }
        };
        mDwrViewModel.GetJobAssignment().observe(this, mAssignmentObserver);
    }

    private void initializeAndSetupWidgets() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        fabMenu = findViewById(R.id.fabExtras);
        fabSave = findViewById(R.id.fabSave);
        fabSubmit = findViewById(R.id.fabSubmit);
        fabDelete = findViewById(R.id.fabDelete);
        lblSave = findViewById(R.id.lblSave);
        lblSubmit = findViewById(R.id.lblSubmit);
        lblDelete = findViewById(R.id.lblDelete);
        lblStatusDwrIcon = findViewById(R.id.lblStatusDwrIcon);
        mileageFromJob = findViewById(R.id.inputMileageFrom);
        mileageToJob = findViewById(R.id.inputMileageTo);
        mileageJob = findViewById(R.id.inputMileageOn);
        workHoursRounded = findViewById(R.id.inputWorkHoursRounded);
        costCenters = findViewById(R.id.inputSpecialCostCenter);
        costCentersLayout = findViewById(R.id.layoutSpecialCostCenter);
        layoutSigninSheet = findViewById(R.id.layoutSigninSheet);
        layoutProtection = findViewById(R.id.layoutProtection);

        //Picture Adapter
        photoRV = findViewById(R.id.recyclePictures);
        pictureComment = findViewById(R.id.inputPictureComment);

        // Special number formatting - miles
        setupMileageInit();
        // Initialize Widgets
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Initialize Watcher Listeners
        mWatchListeners = new ArrayList<>();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Need the onClick listener for TextInputEditText in code for older Android devices.
    // The onClick for the icon (AppCompatImageView) works as expected using the onClick in XML.
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.inputClassType:
                ClassificationPicker(view);
                break;
            case R.id.inputWmataYard:
                WmataYardPicker(view);
                break;
            case R.id.inputFirstStation:
                FirstStationPicker(view);
                break;
            case R.id.inputLastStation:
                LastStationPicker(view);
                break;
            case R.id.inputDistrict:
                DistrictPicker(view);
                break;
            case R.id.inputWorkOnTrack:
                TrackFoulPicker(view);
                break;
            case R.id.inputWorkDate:
                DayPicker(view);
                break;
            case R.id.inputNonBilledReason:
                NonBilledReasonPicker(view);
                break;
            case R.id.inputRailroad:
                PropertyPicker(view);
                break;
            case R.id.inputJobNbr:
                JobPicker(view);
                break;
            case R.id.inputLocationState:
                LocationStatePicker(view);
                break;
            case R.id.inputCSXDataRegion:
                RegionPicker(view);
                break;
            case R.id.inputWmataLine:
                WmataLinePicker(view);
                break;
            case R.id.inputWmataStation:
                WmataStationNbrPicker(view);
                break;
            case R.id.inputWmataTrack:
                WmataTrackPicker(view);
                break;
            case R.id.inputWmataWeather:
                WeatherPicker(view);
                break;
            case R.id.inputTypeOfVehicle:
                TypeOfVehiclePicker(view);
                break;
            case R.id.inputWorkLunchTime:
                LunchHoursPicker(view);
                break;
            case R.id.inputWorkBriefTime:
                BriefingHoursPicker(view);
                break;
            case R.id.inputTravelToJob:
                TravelToHoursPicker(view);
                break;
            case R.id.inputTravelFromJob:
                TravelFromHoursPicker(view);
                break;
            case R.id.inputPerDiem:
                PerDiemPicker(view);
                break;
            case R.id.inputWorkHoursRounded:
                WorkHoursRoundedPicker(view);
                break;
            case R.id.inputSpecialCostCenter:
                SpecialCostCenterPicker(view);
                break;
            case R.id.inputShiftSide:
                UtilityShiftPicker(view);
                break;
        }
    }

    private void persistenceLoad() {
        //Load Picture RecyclerView

        List<PictureField> mPictureList;
        if (mDwrViewModel.PictureList != null) {
            mPictureList = mDwrViewModel.PictureList;
        } else {
            mPictureList = new ArrayList<>();
        }
        mPicAdapter = new PictureAdapter(mPictureList, this, this);
        photoRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoRV.setAdapter(mPicAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDwrViewModel.PictureList != null && mDwrViewModel.PictureList.size() > 0) {
            mPicAdapter.setPictureList(mDwrViewModel.PictureList);
            mPicAdapter.notifyDataSetChanged();
        }
        timeEnter = Calendar.getInstance().getTimeInMillis(); // keeps track of time on screen for Austin

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the raw data needs to be back filled.
        if (mDwrId == 0) {
            FillDwrFormNew();
            ShowHideForms(mDwrViewModel.CurrentFormType, mPropertyId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDwrViewModel.PictureList = mPicAdapter.getPictureList();
        if(timeEnter > 0) // keeps track of time on screen for Austin
            mDwrViewModel.dwrItem.timeLogged += Calendar.getInstance().getTimeInMillis() - timeEnter;
    }

    /*
     *  Keeping these keys local for now.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(IN_DWRID, mDwrId);
        outState.putInt(IN_JOBID, mJobId);
        outState.putInt(IN_PROPERTYID, mPropertyId);
        super.onSaveInstanceState(outState);
    }

    /*
     *  Restore the state, compliments of onSaveInstanceState().
     *  NOTE: If you do not see something here, probably because
     *  we need to restore data in the OnCreate().
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /* Add Listeners for Mileage Edit Text to update Total Mileage */
    private void setupMileageInit() {
        View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
            int mileageTo;
            int mileageFrom;
            int jobMileage;

            @Override
            public void onFocusChange(View view, boolean b) {
                try {
                    mileageFrom = Integer.parseInt(mileageFromJob.getText().toString());
                } catch (Exception e) {
                    mileageFrom = 0;
                }
                try {
                    mileageTo = Integer.parseInt(mileageToJob.getText().toString());
                } catch (Exception e) {
                    mileageTo = 0;
                }
                try {
                    jobMileage = Integer.parseInt(mileageJob.getText().toString());
                } catch (Exception e) {
                    jobMileage = 0;
                }
                mDwrViewModel.dwrItem.setTotalMileage(mileageFrom + mileageTo + jobMileage);
            }
        };
        mileageToJob.setOnFocusChangeListener(mFocusChangeListener);
        mileageFromJob.setOnFocusChangeListener(mFocusChangeListener);
        mileageJob.setOnFocusChangeListener(mFocusChangeListener);
    }

    /* Handle the callback when a picture has been clicked in the adapter */
    @Override
    public void setPictureOnClick(final int position) {
        if (photoRV.getLayoutManager() != null)
            photoRV.getLayoutManager().scrollToPosition(position);
        final PictureField mField = mPicAdapter.getPictureList().get(position);
        //Remove All Previous Text Listeners
        for (TextWatcher mTextWatcher : mWatchListeners) {
            pictureComment.removeTextChangedListener(mTextWatcher);
        }
        //Create New Text Watcher
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mPicAdapter.getItemCount() > position) {
                    mPicAdapter.getPictureList().get(position).description = pictureComment.getText().toString();
                    mPicAdapter.notifyItemChanged(position);
                }
            }
        };


        pictureComment.setText(mField.description);

        //Add the new listener to the array to be removed later
        mWatchListeners.add(mTextWatcher);
        pictureComment.addTextChangedListener(mTextWatcher);


    }

    public void SetHoursWorked(View view){
        mDwrViewModel.dwrItem.setWorkHoursRounded("0.0 hours");
        mDwrViewModel.dwrItem.setWorkStartTime("");
        mDwrViewModel.dwrItem.setWorkEndTime("");
        ((TextInputLayout) (workHoursRounded.getParent()).getParent()).setHelperText("");
    }

    /*
     *  Cost centers are an odd feature in that they are supplied by the server and if
     *  they do not exist (as part of the job), then the selection widgets on the form
     *  are not to be shown.  If they exist, their selection is required. Unless the
     *  form is not a billable day, then do not show the widget.  It would be hard to
     *  come up with an ask that was  better at breaking the form design. lol
     *
     *  To make this work, first the dynamics defines the cost center field to only
     *  be seen/required for billable days.  Then when a job does not have any cost
     *  centers, it hides the layout and input text (to avoid validation).  This fails
     *  to reverse the process if they start switching around jobs, but they can always
     *  exit and return to the DWR to see the field.
     */
    private void ManageCostCenter(String costcenters){
        if(costcenters.length() > 0) {
            mDwrViewModel.dwrItem.setAllCostCenters(costcenters);
        } else {
            costCentersLayout.setVisibility(View.GONE);
            costCenters.setVisibility(View.GONE);
        }
    }

    /*
     *  The ValidationOk is simply a check to make sure required fields have something in them.  For
     *  EditText fields, it tries to use the enclosing TextInputLayout to supply the error message.
     */
    private boolean ValidationOk(int form, int property) {
        if (!Settings.getUseValidation(this)) return true;
        boolean noFails = true;
        LayoutHelperDwr layoutHelperDwr = LayoutHelperDwr.getInstance(this);
        List<LayoutHelperDwr.Qualities> qualities = layoutHelperDwr.GetDwrQualities(form, property);


        // The dynamics data (i.e. the qualities) is a preprocessor and cannot on its own consider cross
        // field dependencies based on user input. When we have user entered data in a field changing the
        // validation on another field we need to manage that explicitly.  One way to do that is by changing
        // the data in the qualities list using the adjustCondition() method.

        /* For Not-Present-On-Track OR Other-Non-Billable and Holdover, allow for zero hours worked. */
        if (mDwrViewModel.dwrItem.getNonBilledReason() != null && mDwrViewModel.dwrItem.getClassification() != null){
            if ((mDwrViewModel.dwrItem.getNotPresentOnTrack())
                || (mDwrViewModel.dwrItem.getClassification().equals(Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(DWR_TYPE_OTHER_NONBILLABLE))
                    && mDwrViewModel.dwrItem.getNonBilledReason().equals(Arrays.asList(getResources().getStringArray(R.array.nonbilledreason_name_max)).get(0)))) {
                qualities = layoutHelperDwr.adjustCondition(qualities, LayoutHelperDwr.CONDITION_HOLDOVER);
            }
        }

        /* If a time range is entered, we do not want to allow zero (0) hours worked.  By clearing out the hours worked, it will be flagged by normal validation. */
        if(mDwrViewModel.dwrItem.getWorkHoursRounded() != null && Functions.GetFloatFromString(mDwrViewModel.dwrItem.getWorkHoursRounded()) == 0){
            if((mDwrViewModel.dwrItem.getWorkStartTime() != null && mDwrViewModel.dwrItem.getWorkStartTime().length() > 0)
                    ||(mDwrViewModel.dwrItem.getWorkEndTime() != null && mDwrViewModel.dwrItem.getWorkEndTime().length() > 0)){
                mDwrViewModel.dwrItem.setWorkHoursRounded("");
                workHoursRounded.setText("");
            }
        }

        /* If there are hours worked greater than zero, but no time range is entered, that means something when wrong.
           Here we will set a flag to make sure we fail validation and add the proper error text to the hours worked.
         */
        boolean workRangeFailed = false;
        if(mDwrViewModel.dwrItem.getWorkHoursRounded() != null && Functions.GetFloatFromString(mDwrViewModel.dwrItem.getWorkHoursRounded()) > 0){
            if(mDwrViewModel.dwrItem.getWorkStartTime() == null) { workRangeFailed = true; }
            if(mDwrViewModel.dwrItem.getWorkStartTime() != null && mDwrViewModel.dwrItem.getWorkStartTime().length() == 0) { workRangeFailed = true; }
            if(mDwrViewModel.dwrItem.getWorkEndTime() == null) { workRangeFailed = true; }
            if(mDwrViewModel.dwrItem.getWorkEndTime() != null && mDwrViewModel.dwrItem.getWorkEndTime().length() == 0) { workRangeFailed = true; }
        }

        /* If a particular job has a restriction, verify that restriction has not been compromised. */
        if(mDwrViewModel.dwrItem.getRestrictionsJob().length() > 0) {
            // This is a check that the selected classification is allowed for this job.
            List<String> localDay = Arrays.asList(getResources().getStringArray(R.array.classification_name));
            int holdClassNdx = -1;
            for (int i = 0; i < localDay.size(); ++i) {
                if (localDay.get(i).equalsIgnoreCase(mDwrViewModel.dwrItem.getClassification()))
                    holdClassNdx = i;
            }
            if (holdClassNdx >= 0) {
                String classSrvName = Arrays.asList(getResources().getStringArray(R.array.classification_code_name)).get(holdClassNdx);
                if (mDwrViewModel.dwrItem.getRestrictionsJob().contains(classSrvName)){
                    EditText input = findViewById(R.id.inputClassType);
                    if (input != null) {
                        noFails = false;
                        TextInputLayout fieldParent = (TextInputLayout)input.getParent().getParent();
                        fieldParent.setError(getResources().getText(R.string.err_classification_not_allowed));
                    }
                }
            }
        }


        // Cycle through the dynamics data to see if there are any required fields without data.
        for (LayoutHelperDwr.Qualities item : qualities) {
            if (item.Required && (item.FieldType == LayoutHelperDwr.VIEW_TYPE_EDIT || item.FieldType == LayoutHelperDwr.VIEW_TYPE_PHONE)) {
                int id = getResources().getIdentifier(item.FieldName, RESOURCE_TYPE_ID, getPackageName());
                if (id > 0) {
                    TextInputEditText input = findViewById(id);
                    if (input != null) {
                        if (input.getVisibility() == View.VISIBLE) {
                            if (input.getText() != null) {
                                if (input.getText().length() < 1) {
                                    noFails = false;
                                    ViewParent holdParent = input.getParent().getParent();
                                    TextInputLayout fieldParent;
                                    if (holdParent instanceof TextInputLayout) {
                                        fieldParent = (TextInputLayout) holdParent;
                                        fieldParent.setError(item.FieldHintError);
                                    } else {
                                        input.setError(item.FieldHintError);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // More complicated validations can be applied here.
            /* Check Per Diem */
            if (item.FieldName.equals("fieldPerDiem")) {
                EditText input = findViewById(R.id.inputPerDiem);
                if (input != null) {
                    String inputText = input.getText().toString();
                    if (!inputText.equals(Arrays.asList(getResources().getStringArray(R.array.per_diem)).get(0))
                            && CheckOtherPerDiem(mDwrViewModel.dwrItem.workDate)) {
                        noFails = false;
                        TextInputLayout fieldParent = (TextInputLayout)input.getParent().getParent();
                        fieldParent.setError(getResources().getText(R.string.err_perdiem_dup));
                    }
                }
            }
            /* Fail if hours worked but no time range.  Use a non-default error message */
            if((workRangeFailed) && item.FieldName.equalsIgnoreCase("inputWorkHoursRounded")) {
                EditText input = findViewById(R.id.inputWorkHoursRounded);
                if (input != null) {
                    noFails = false;
                    TextInputLayout fieldParent = (TextInputLayout)input.getParent().getParent();
                    fieldParent.setError(getResources().getText(R.string.err_workhours_range));
                }
            }
        }

        // Check if the require images are present.
        boolean csc = CheckSignature(R.id.layoutClientSignatureInfo, R.id.layoutClientTimeSignatureError, R.id.cardviewClientSignatureboarder, mDwrViewModel.dwrItem.clientSignaturePhotoName);
        boolean csr = CheckSignature(R.id.layoutRwicTimeSignature, R.id.layoutRwicTimeSignatureError, R.id.cardviewRwicSignatureboarder, mDwrViewModel.dwrItem.flagmanSignaturePhotoName);
        boolean cse = CheckSignature(R.id.layoutRailSignatureInfo, R.id.layoutRailTimeSignatureError, R.id.cardviewRailSignatureboarder, mDwrViewModel.dwrItem.railSignaturePhotoName);
        boolean csx = CheckCSX();
        boolean scrra = CheckSCRRA();
        boolean scraaVehicle = checkScrraVehicle();
        noFails = noFails && csc && csr && csx && cse && scrra && scraaVehicle;

        return noFails;
    }

    public boolean CheckSCRRA() {
        DwrItem item = mDwrViewModel.dwrItem;
        if (layoutProtection != null && (layoutProtection.getVisibility() == View.GONE || layoutProtection.getVisibility() == View.INVISIBLE)) {
            return true;
        } else if (Railroads.GetTemplateKey(item.getProperty()) == Railroads.DWR_TEMPLATE_SCRRA) {
            return item.p707 || item.p1102 || item.p1107 || item.pEC1
                    || item.pForm23 || item.pForm23Y || item.pFormB
                    || item.pFormC || item.pFormW || item.pTrackTime
                    || item.pDerails || item.pTrackWarrant || item.pObserver
                    || item.pTrackAuthority || item.pNoProtection
                    || item.pLookout || item.pLiveFlagman || item.pVerbalPermission;
        } else {
            return true;    // true = OK
        }
    }


    // Limit Per Diem expense to 1 DWR per day by checking for another on same date.
    private boolean CheckOtherPerDiem(String dwrDate) {
        try {
            return PerDiemList.containsKey(dwrDate);
        } catch (Exception ex) {
            return false; /* Not a huge deal if we skip this check on exception. */
        }
    }

    private void handleSCRRAVehicleError(String error) {
        TextInputLayout input = findViewById(R.id.fieldTypeOfVehicle);
        input.setError(error);
    }

    private boolean handleCxsImageError() {
        // Minimum IMAGE_DWR_CXS_MIN_PHOTOS Daily Picture Check or more than max photos
        TextView dailyTv = findViewById(R.id.layoutMultiPictureError);
        ConstraintLayout dailyCv = findViewById(R.id.cardviewMultipictureboarder);
        if (mDwrViewModel.PictureList == null
                || mDwrViewModel.PictureList.size() < IMAGE_DWR_CXS_MIN_PHOTOS
                ||  mDwrViewModel.PictureList.size() > getDwrMaxPhotos()) {
            dailyTv.setVisibility(View.VISIBLE);
            dailyCv.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.card_required_error));
            return false;
        } else {
            dailyTv.setVisibility(View.INVISIBLE);
            dailyCv.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.card_normal));
            return true;
        }
    }

    // The CSX has a few extra required items that happen to be images.
    public boolean CheckCSX() {
        boolean value = true;
        if (isPropertyCsx(mDwrViewModel.dwrItem.property)) {
            // Sometimes pictures are not required.
            LinearLayout takePicsLayout = findViewById(R.id.layoutPictures);
            if (takePicsLayout == null) return true;
            if (takePicsLayout.getVisibility() == View.GONE) return true;

            // Minimum IMAGE_DWR_CXS_MIN_PHOTOS Daily Picture Check
            if (!handleCxsImageError()) {
                value = false;
            }
        }

        return value;
    }

    // Check if required signature is present.  This makes some assumptions about the
    // layout of the signature collection dialog.
    public boolean CheckSignature(int layoutTop, int layoutErr, int cardSign, String fileName) {
        // Is the signature needed.
        LinearLayout signedLayout = findViewById(layoutTop);
        if (signedLayout == null) return true;
        if (signedLayout.getVisibility() == View.GONE) return true;

        TextView errorTV = findViewById(layoutErr);
        ConstraintLayout signedCV = findViewById(cardSign);
        if (fileName == null) {
            errorTV.setVisibility(View.VISIBLE);
            signedCV.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.card_required_error));
            return false;
        } else if (Functions.EncodeImagePictureBase64(DOC_SIGNATURES, fileName, 80).length() == 0) {
            errorTV.setVisibility(View.VISIBLE);
            signedCV.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.card_required_error));
            return false;
        } else {
            errorTV.setVisibility(View.INVISIBLE);
            signedCV.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.card_normal));
            return true;
        }
    }

    /*
     *  This is the case where the DWR is to be created from scratch.  No point in running through
     *  all the empty data from the DB.
     *  This also prevents the Picture Callback from Calling Onstart and Overriding the previous default values.
     */
    private void FillDwrFormNew() {

        // Fill out labels
        String holdFmt;
        holdFmt = getResources().getString(R.string.format_name);
        mDwrViewModel.dwrItem.setWorkerName(String.format(holdFmt, mUser.display, mUser.employeeCode));
        holdFmt = getResources().getString(R.string.format_status_dwr);
        mDwrViewModel.dwrItem.setLabelStatus(String.format(holdFmt, Arrays.asList(getResources().getStringArray(R.array.dwr_status_name)).get(DWR_STATUS_NEW)));
        mDwrViewModel.dwrItem.statusIcon = DWR_STATUS_NEW;

        // Various defaults

        if(mDwrViewModel.dwrItem.workDate == null) mDwrViewModel.dwrItem.setWorkDate(KTime.ParseNow(KTime.KT_fmtDateShrtMiddle).toString());
        if(mDwrViewModel.dwrItem.classification == null) mDwrViewModel.dwrItem.setClassification(Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(DWR_TYPE_BILLABLE_DAY));
        if(mDwrViewModel.dwrItem.getJobNumberId() == 0) mDwrViewModel.dwrItem.setJobNumberId(mJobId);
        if(mDwrViewModel.dwrItem.property == null) mDwrViewModel.dwrItem.setProperty(Railroads.PropertyName(getApplicationContext(),mPropertyId));
        // Little trick to force a new database load for subdivisions.
        if(!mDwrViewModel.getRailRoadCode().equalsIgnoreCase(Railroads.PropertyNameServer(getApplicationContext(), mPropertyId))) {
            mDwrViewModel.setRailRoadCode(Railroads.PropertyNameServer(getApplicationContext(), mPropertyId));
            mDwrViewModel.GetSubdivisions().removeObserver(mSubDivisionObserver);
            mDwrViewModel.GetSubdivisions().observe(this, mSubDivisionObserver);
        }
    }

    /*
     *  This method is used to fill the form with existing data. To do that it places data in the
     *  object bound to the widgets on the screen.  As the data is applied, the screen is updated.
     */
    private void FillDwrForm(DwrTbl dwr) {
        // Save off some key that are useful to know.  These are used internally to help determine
        // some interactive elements of the screen.
        mDwrViewModel.CurrentFormType = dwr.Classification;
        mPropertyId = dwr.Property;
        // Little trick to force a new database load for subdivisions.
        if(!mDwrViewModel.getRailRoadCode().equalsIgnoreCase(Railroads.PropertyNameServer(getApplicationContext(), mPropertyId))) {
            mDwrViewModel.setRailRoadCode(Railroads.PropertyNameServer(getApplicationContext(), mPropertyId));
            mDwrViewModel.GetSubdivisions().removeObserver(mSubDivisionObserver);
            mDwrViewModel.GetSubdivisions().observe(this, mSubDivisionObserver);
        }
        // Little trick to force a new database Load for cost center (assignment data)
        if(mDwrViewModel.getJobId() != dwr.JobId){
            mDwrViewModel.setJobId(dwr.JobId);
            mDwrViewModel.GetJobAssignment().removeObserver(mAssignmentObserver);
            mDwrViewModel.GetJobAssignment().observe(this, mAssignmentObserver);
            getByJobIdObersvable();
        }

        // Fill out labels
        String holdFmt;
        holdFmt = getResources().getString(R.string.format_name);
        mDwrViewModel.dwrItem.setWorkerName(String.format(holdFmt, mUser.display, mUser.employeeCode));
        holdFmt = getResources().getString(R.string.format_status_dwr);
        mDwrViewModel.dwrItem.setLabelStatus(String.format(holdFmt, Arrays.asList(getResources().getStringArray(R.array.dwr_status_name)).get(dwr.Status)));
        mDwrViewModel.dwrItem.statusIcon = dwr.Status;

        // Project Information
        if (dwr.WorkDate.length() == 0) {
            // This will default to the local time zone.
            mDwrViewModel.dwrItem.setWorkDate(KTime.ParseNow(KTime.KT_fmtDateShrtMiddle).toString());
        } else {
            try {
                mDwrViewModel.dwrItem.setWorkDate(KTime.ParseToFormat(dwr.WorkDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()).toString());
            } catch (ExpParseToCalendar expParseToCalendar) {
                mDwrViewModel.dwrItem.setWorkDate(KTime.ParseNow(KTime.KT_fmtDateShrtMiddle).toString());  // Today is a good default.
            }
        }
        mDwrViewModel.dwrItem.dwrSrvrId = dwr.DwrSrvrId;
        mDwrViewModel.dwrItem.setClassification(Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(dwr.Classification));
        mDwrViewModel.dwrItem.setLocationCity(dwr.LocationCity);
        mDwrViewModel.dwrItem.setLocationState(dwr.LocationState);
        mDwrViewModel.dwrItem.setConstructionDay(dwr.ConstructionDay);
        mDwrViewModel.dwrItem.setInputTotalWorkDays(dwr.InputTotalWorkDays);
        mDwrViewModel.dwrItem.setInputDescWeatherWind(dwr.InputDescWeatherWind);
        mDwrViewModel.dwrItem.setInputDescWeatherRain(dwr.InputDescWeatherRain);
        mDwrViewModel.dwrItem.setContractName(dwr.ContractorName);
        String propertyName = Railroads.PropertyName(getApplicationContext(), mPropertyId);
        mDwrViewModel.dwrItem.setProperty(propertyName);
        mDwrViewModel.dwrItem.setTypeOfVehicle(dwr.TypeOfVehicle);
        checkProperty();
        mDwrViewModel.dwrItem.setJobNumber(dwr.JobNumber);  // for display
        mDwrViewModel.dwrItem.setJobNumberId(dwr.JobId);    // for API
        mDwrViewModel.dwrItem.setNonBilledReason(dwr.NonBilledReason);
        // Railroad specific fields
        mDwrViewModel.dwrItem.setCnDataN_SO_CC(dwr.CNDataNSOCC);
        mDwrViewModel.dwrItem.setCnDataNumber(dwr.CNDataNetwork);
        mDwrViewModel.dwrItem.setCnDataCounty(dwr.CNDataCounty);
        mDwrViewModel.dwrItem.setKcsDataContractorCnt(String.valueOf(dwr.KCSDataContractorCnt));
        mDwrViewModel.dwrItem.setKctDataTaskOrder(dwr.KCTDataTaskOrder);
        mDwrViewModel.dwrItem.setUpDataDotXing(dwr.UPDataDotXing);
        mDwrViewModel.dwrItem.setUpDataFolderNbr(dwr.UPDataFolderNbr);
        mDwrViewModel.dwrItem.setUpDataServiceUnit(dwr.UPDataServiceUnit);
        mDwrViewModel.dwrItem.setCsxDataRegionId(dwr.CSXDataRegion);
        mDwrViewModel.dwrItem.setCsxDataRegionDspl(Arrays.asList(getResources().getStringArray(R.array.csx_region_name)).get(dwr.CSXDataRegion));
        mDwrViewModel.dwrItem.setCsxDataOpNbr(dwr.CSXDataOpNbr);
        // Work Zone data
        mDwrViewModel.dwrItem.setOnGoing(dwr.IsOngoing);
        mDwrViewModel.dwrItem.setRoadMaster(dwr.RoadMaster);
        mDwrViewModel.dwrItem.setDistrict(dwr.District);
        mDwrViewModel.dwrItem.setSubdivision(dwr.Subdivision);
        mDwrViewModel.dwrItem.setMpStart(dwr.MpStart);
        mDwrViewModel.dwrItem.setMpEnd(dwr.MpEnd);
        mDwrViewModel.dwrItem.setWorkingTrack(dwr.WorkingTrack);
        mDwrViewModel.dwrItem.setWorkOnTrackId(dwr.WorkOnTrack);
        mDwrViewModel.dwrItem.setWorkOnTrackDspl(Arrays.asList(getResources().getStringArray(R.array.track_foul_name)).get(dwr.WorkOnTrack));
        // Protections
        mDwrViewModel.dwrItem.setP707(dwr.Is707);
        mDwrViewModel.dwrItem.setP1102(dwr.Is1102);
        mDwrViewModel.dwrItem.setP1107(dwr.Is1107);
        mDwrViewModel.dwrItem.setPEc1(dwr.IsEC1);
        mDwrViewModel.dwrItem.setPFormB(dwr.IsFormB);
        mDwrViewModel.dwrItem.setPFormC(dwr.IsFormC);
        mDwrViewModel.dwrItem.setPFormW(dwr.IsFormW);
        mDwrViewModel.dwrItem.setPForm23(dwr.IsForm23);
        mDwrViewModel.dwrItem.setPForm23Y(dwr.IsForm23Y);
        mDwrViewModel.dwrItem.setPDerails(dwr.IsDerails);
        mDwrViewModel.dwrItem.setPTrackTime(dwr.IsTrackTime);
        mDwrViewModel.dwrItem.setPWarrant(dwr.IsTrackWarrant);
        mDwrViewModel.dwrItem.setPAuthority(dwr.IsTrackAuthority);
        mDwrViewModel.dwrItem.setPObserver(dwr.IsObserver);
        mDwrViewModel.dwrItem.setPNoProtect(dwr.IsNoProtection);
        mDwrViewModel.dwrItem.setPWatchman(dwr.IsLookout);
        mDwrViewModel.dwrItem.setPLiveFlagman(dwr.IsLiveFlagman);
        mDwrViewModel.dwrItem.setPVerbalPermission(dwr.IsVerbalPermission);
        mDwrViewModel.dwrItem.setInputWMLine(dwr.InputWMLine);
        mDwrViewModel.dwrItem.setInputWMStation(dwr.InputWMStation);
        mDwrViewModel.dwrItem.setInputWMStationName(dwr.InputWMStationName);
        mDwrViewModel.dwrItem.setInputWMTrack(dwr.InputWMTrack == -1 ? "X" : String.valueOf(dwr.InputWMTrack));
        // Work Details
        mDwrViewModel.dwrItem.setDescription(dwr.Description);
        // Need to parse the description into 15 comments for WMATA.
        if(Railroads.GetTemplateKey(mDwrViewModel.dwrItem.getProperty()) == Railroads.DWR_TEMPLATE_WMATA){
            String[] wmComments = dwr.Description.split("#_");
            mDwrViewModel.dwrItem.setComment01(wmComments.length > 1 ? wmComments[1].substring(3) : "");
            mDwrViewModel.dwrItem.setComment02(wmComments.length > 2 ? wmComments[2].substring(3) : "");
            mDwrViewModel.dwrItem.setComment03(wmComments.length > 3 ? wmComments[3].substring(3) : "");
            mDwrViewModel.dwrItem.setComment04(wmComments.length > 4 ? wmComments[4].substring(3) : "");
            mDwrViewModel.dwrItem.setComment05(wmComments.length > 5 ? wmComments[5].substring(3) : "");
            mDwrViewModel.dwrItem.setComment06(wmComments.length > 6 ? wmComments[6].substring(3) : "");
            mDwrViewModel.dwrItem.setComment07(wmComments.length > 7 ? wmComments[7].substring(3) : "");
            mDwrViewModel.dwrItem.setComment08(wmComments.length > 8 ? wmComments[8].substring(3) : "");
            mDwrViewModel.dwrItem.setComment09(wmComments.length > 9 ? wmComments[9].substring(3) : "");
            mDwrViewModel.dwrItem.setComment10(wmComments.length > 10 ? wmComments[10].substring(3) : "");
            mDwrViewModel.dwrItem.setComment11(wmComments.length > 11 ? wmComments[11].substring(3) : "");
            mDwrViewModel.dwrItem.setComment12(wmComments.length > 12 ? wmComments[12].substring(3) : "");
            mDwrViewModel.dwrItem.setComment13(wmComments.length > 13 ? wmComments[13].substring(3) : "");
            mDwrViewModel.dwrItem.setComment14(wmComments.length > 14 ? wmComments[14].substring(3) : "");
            mDwrViewModel.dwrItem.setComment15(wmComments.length > 15 ? wmComments[15].substring(3) : "");
            mDwrViewModel.dwrItem.setWmataCallNumber(dwr.ClientPhone);
        }
        mDwrViewModel.dwrItem.setDescWeatherConditions(dwr.DescWeatherConditions);
        mDwrViewModel.dwrItem.setDescTypeOfWork(dwr.DescTypeOfWork);
        mDwrViewModel.dwrItem.setDescInsideRow(dwr.DescInsideRoW);
        mDwrViewModel.dwrItem.setDescOutsideRow(dwr.DescOutsideRoW);
        mDwrViewModel.dwrItem.setDescUnusual(dwr.DescUnusual);
        mDwrViewModel.dwrItem.setDescLocationStart(dwr.DescLocationStart);
        // Time
        String hoursFmt = getResources().getString(R.string.display_hours_dec);
        mDwrViewModel.dwrItem.setWorkStartTime(GetLocalFromUTC(dwr.WorkStartTime));
        mDwrViewModel.dwrItem.setWorkEndTime(GetLocalFromUTC(dwr.WorkEndTime));
        if(dwr.WorkHoursRounded > 0) {  // Since validation is looking for blank data, and zero is like blank.
            mDwrViewModel.dwrItem.setWorkHoursRounded(String.format(Locale.getDefault(), hoursFmt, dwr.WorkHoursRounded));
        }
        mDwrViewModel.dwrItem.setNotPresentOnTrack(dwr.NotPresentOnTrack);
        // It is ok for zero work hours if NotPresentOnTrack true, but to allow that requires some value in WorkHoursRounded.
        if(dwr.NotPresentOnTrack && dwr.WorkHoursRounded == 0){ SetHoursWorked(null); }
        mDwrViewModel.dwrItem.setTravelToJobStartTime(GetLocalFromUTC(dwr.TravelToJobStartTime));
        mDwrViewModel.dwrItem.setTravelToJobEndTime(GetLocalFromUTC(dwr.TravelToJobEndTime));
        mDwrViewModel.dwrItem.setTravelToJobHours(String.format(Locale.getDefault(), hoursFmt, dwr.TravelToHoursRounded));
        mDwrViewModel.dwrItem.setTravelFromJobStartTime(GetLocalFromUTC(dwr.TravelFromJobStartTime));
        mDwrViewModel.dwrItem.setTravelFromJobEndTime(GetLocalFromUTC(dwr.TravelFromJobEndTime));
        mDwrViewModel.dwrItem.setTravelFromJobHours(String.format(Locale.getDefault(), hoursFmt, dwr.TravelFromHoursRounded));
        mDwrViewModel.dwrItem.setPerformedTraining(dwr.PerformedTraining);
        mDwrViewModel.dwrItem.setSpecialCostCenterReal(dwr.SpecialCostCenter);
        if(Functions.DisplayCostCenters(getApplicationContext(), dwr.SpecialCostCenter).size() > 1) {
            mDwrViewModel.dwrItem.setSpecialCostCenterDspl(Functions.DisplayCostCenters(getApplicationContext(), dwr.SpecialCostCenter).get(1));
        } else {
            mDwrViewModel.dwrItem.setSpecialCostCenterDspl(Functions.DisplayCostCenters(getApplicationContext(), dwr.SpecialCostCenter).get(0));
        }

        // Time - show the from/to values if available.
        String startTime, endTime;
        String hoursDisplayFmt = getResources().getString(R.string.display_work_times);
        String tz = TimeZone.getDefault().getID();
        try {
            startTime = KTime.ParseToFormat(dwr.WorkStartTime, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateTime24, tz).toString();
            endTime = KTime.ParseToFormat(dwr.WorkEndTime, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateTime24, tz).toString();
        } catch (ExpParseToCalendar expParseToCalendar) {
            startTime = ""; endTime = "";
        }
        if(startTime.length() > 0 || endTime.length() > 0) {
            TextView mTextView = findViewById(R.id.inputWorkHoursRounded);
            ((TextInputLayout) (mTextView.getParent()).getParent()).setHelperText(String.format(Locale.getDefault(), hoursDisplayFmt, startTime, endTime));
        }
        try {
            startTime = KTime.ParseToFormat(dwr.TravelToJobStartTime, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateTime24, tz).toString();
            endTime = KTime.ParseToFormat(dwr.TravelToJobEndTime, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateTime24, tz).toString();
        } catch (ExpParseToCalendar expParseToCalendar) {
            startTime = ""; endTime = "";
        }
        if(startTime.length() > 0 || endTime.length() > 0) {
            TextView mTextView = findViewById(R.id.inputTravelToJob);
            ((TextInputLayout) (mTextView.getParent()).getParent()).setHelperText(String.format(Locale.getDefault(), hoursDisplayFmt, startTime, endTime));
        }
        try {
            startTime = KTime.ParseToFormat(dwr.TravelFromJobStartTime, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateTime24, tz).toString();
            endTime = KTime.ParseToFormat(dwr.TravelFromJobEndTime, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateTime24, tz).toString();
        } catch (ExpParseToCalendar expParseToCalendar) {
            startTime = ""; endTime = "";
        }
        if(startTime.length() > 0 || endTime.length() > 0) {
            TextView mTextView = findViewById(R.id.inputTravelFromJob);
            ((TextInputLayout) (mTextView.getParent()).getParent()).setHelperText(String.format(Locale.getDefault(), hoursDisplayFmt, startTime, endTime));
        }

        // Travel
        mDwrViewModel.dwrItem.setMilesToJob(Integer.toString(dwr.TravelToJobMiles));
        mDwrViewModel.dwrItem.setMilesFromJob(Integer.toString(dwr.TravelFromJobMiles));
        mDwrViewModel.dwrItem.setJobMileage(Integer.toString(dwr.TravelOnJobMiles));
        mDwrViewModel.dwrItem.setTotalMileage(dwr.TravelToJobMiles + dwr.TravelFromJobMiles + dwr.TravelOnJobMiles);
        mDwrViewModel.dwrItem.setPerdiemId(dwr.PerDiem);
        mDwrViewModel.dwrItem.setPerdiemDspl(Arrays.asList(getResources().getStringArray(R.array.per_diem)).get(dwr.PerDiem == -1 ? 0 : dwr.PerDiem));
//        mDwrViewModel.dwrItem.setPerdiemDspl(Arrays.asList(getResources().getStringArray(R.array.per_diem)).get(dwr.PerDiem));

        // Roadway Flagging
        mDwrViewModel.dwrItem.setHasRoadwayFlagging(serviceType == RP_ROADWAY_FLAGGING_SERVICE || dwr.HasRoadwayFlagging);
        mDwrViewModel.dwrItem.setEightyTwoT(dwr.EightyTwoT);
        mDwrViewModel.dwrItem.setStreetName(dwr.StreetName);
        mDwrViewModel.dwrItem.setMilePostsForStreet(dwr.MilePostsForStreet);

        // Signatures
        mDwrViewModel.dwrItem.setFlagmanSignaturePhotoName(dwr.RwicSignatureName);
        mDwrViewModel.dwrItem.setFlagmanSignaturePhotoDate(dwr.RwicSignatureDate);
        mDwrViewModel.dwrItem.setClientSignaturePhotoName(dwr.ClientSignatureName);
        mDwrViewModel.dwrItem.setClientSignaturePhotoDate(dwr.ClientSignatureDate);
        mDwrViewModel.dwrItem.setClientName(dwr.ClientName);
        mDwrViewModel.dwrItem.setClientPhone(dwr.ClientPhone);
        mDwrViewModel.dwrItem.setClientEmail(dwr.ClientEmail);
        mDwrViewModel.dwrItem.setRailSignaturePhotoName(dwr.RailSignatureName);
        mDwrViewModel.dwrItem.setRailSignaturePhotoDate(dwr.RailSignatureDate);
        mDwrViewModel.dwrItem.setRailroadContact(dwr.RailroadContact);

        //CSX Forms
        mDwrViewModel.dwrItem.setRwicPhone(dwr.RwicPhone);
        mDwrViewModel.dwrItem.setCsxShiftNew(dwr.CSXShiftNew);
        mDwrViewModel.dwrItem.setCsxShiftRelief(dwr.CSXShiftRelief);
        mDwrViewModel.dwrItem.setCsxShiftRelieved(dwr.CSXShiftRelieved);
        mDwrViewModel.dwrItem.setWorkLunchTime(dwr.WorkLunchTime);
        mDwrViewModel.dwrItem.setCsxPeopleRow(String.valueOf(dwr.CSXPeopleRow));
        mDwrViewModel.dwrItem.setCsxEquipmentRow(String.valueOf(dwr.CSXEquipmentRow));
        mDwrViewModel.dwrItem.setDescWeatherHigh(String.valueOf(dwr.DescWeatherHigh));
        mDwrViewModel.dwrItem.setDescWeatherLow(String.valueOf(dwr.DescWeatherLow));
        mDwrViewModel.dwrItem.setWorkBriefTime(dwr.WorkBriefTime);
        mDwrViewModel.dwrItem.setRoadMasterPhone(dwr.RoadMasterPhone);
        mDwrViewModel.dwrItem.setDescWorkPlanned(dwr.DescWorkPlanned);
        mDwrViewModel.dwrItem.setDescSafety(dwr.DescSafety);

        // Reviewer info
        mDwrViewModel.dwrItem.reviewerNotes = dwr.ReviewerNotes;
        mDwrViewModel.dwrItem.reviewerName = dwr.ReviewerName;
        mDwrViewModel.dwrItem.reviewerOn = dwr.ReviewerOn;

        // After all the new data is here, build the form.
        ShowHideForms(mDwrViewModel.CurrentFormType, mPropertyId);
    }

    private String GetLocalFromUTC(String inTime) {
        try {
            return KTime.ParseToFormat(inTime, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // Build the card RoadwayFlagging based on the job(service type)
    private void ShowHideRoadwayFlagging(int type, boolean hasRoadwayFlagging) {
        CardView cardRoadwayFlagging = findViewById(R.id.cardRoadwayFlagging);
        if(!isBillableDayUtility() && (type == RP_ROADWAY_FLAGGING_SERVICE || hasRoadwayFlagging)) { // Show
            mDwrViewModel.dwrItem.setHasRoadwayFlagging(true);
            cardRoadwayFlagging.setVisibility(View.VISIBLE);
        } else { // Hide
            cardRoadwayFlagging.setVisibility(View.GONE);
        }
    }

    /*
        Check some special cases of property to update
     */
    private void checkProperty() {
        LayoutHelperDwr layoutHelperDwr = LayoutHelperDwr.getInstance(this);
        Map<String, LayoutHelperDwr.Qualities> qualities = layoutHelperDwr.GetDwrQualityMap(mDwrViewModel.CurrentFormType, mPropertyId);

        String propertyName = mDwrViewModel.dwrItem.property;
        if (isPropertyCsx(propertyName)) {
            layoutSigninSheet.setVisibility(View.GONE);
            mDwrViewModel.dwrItem.setIsCsx(true);
        } else {
            layoutSigninSheet.setVisibility(qualities.get("layoutSigninSheet").Visibility);
            mDwrViewModel.dwrItem.setIsCsx(false);
        }
    }

    // Build the layout based on the template.
    private void ShowHideForms(int form, int property) {
        findViewById(R.id.protectionErrorMessage).setVisibility(View.GONE);

        String propertyName = Railroads.PropertyName(getApplicationContext(), mPropertyId);
        mDwrViewModel.dwrItem.setProperty(propertyName);
        mDwrViewModel.dwrItem.setTypeOfVehicle(mDwrViewModel.dwrItem.typeOfVehicle);

        // DWRs that "require changes" will usually have reviewer notes that need to be displayed at top.
        if(mDwrViewModel.dwrItem.reviewerNotes != null && mDwrViewModel.dwrItem.reviewerNotes.trim().length() > 0) {
            CardView statusCardView = findViewById(R.id.cardStatus);
            TextView lblReviewNote = findViewById(R.id.lblReviewNote);
            TextView lblReviewerName = findViewById(R.id.lblReviewName);
            TextView lblReviewdOn = findViewById(R.id.lblReviewDate);

            statusCardView.setVisibility(View.VISIBLE);

            lblReviewNote.setText(mDwrViewModel.dwrItem.reviewerNotes);
            lblReviewerName.setText(mDwrViewModel.dwrItem.reviewerName);
            try {
                lblReviewdOn.setText(KTime.ParseToFormat(mDwrViewModel.dwrItem.reviewerOn, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()).toString());
            } catch (ExpParseToCalendar expParseToCalendar) {
                lblReviewdOn.setText(KTime.ParseNow(KTime.KT_fmtDateShrtMiddle).toString());
            }
        }

        lblStatusDwrIcon.setImageResource(Functions.GetDwrIcon(mDwrViewModel.dwrItem.statusIcon));
        LayoutHelperDwr layoutHelperDwr = LayoutHelperDwr.getInstance(this);
        List<LayoutHelperDwr.Qualities> qualities = layoutHelperDwr.GetDwrQualities(form, property);
        // WMATA Supervisor Jobs should not have a RWIC (client) signature.
        if(mDwrViewModel.dwrItem.getSupervisorJob()
           && Railroads.GetTemplateKey(mDwrViewModel.dwrItem.getProperty()) == Railroads.DWR_TEMPLATE_WMATA) {
            qualities = layoutHelperDwr.adjustCondition(qualities, LayoutHelperDwr.CONDITION_SUPERVISOR_JOB);
        }

        for (LayoutHelperDwr.Qualities item : qualities) {
            int id = getResources().getIdentifier(item.FieldName, RESOURCE_TYPE_ID, getPackageName());
            if (id > 0) {  // Unknown field names (id=0) can be skipped.
                switch (item.FieldType) {
                    case LayoutHelperDwr.VIEW_TYPE_VIEW:    // Only used for visibility.
                        View view = findViewById(id);
                        if (view != null)
                            view.setVisibility(item.Visibility);
                        break;
                    case LayoutHelperDwr.VIEW_TYPE_INPUT:
                        final TextInputLayout input = findViewById(id);
                        if (input != null) {
                            input.setVisibility(item.Visibility);
                            if (item.FieldHint.length() > 0) input.setHint(item.FieldHint);
                            if (item.FieldHintError.length() > 0)
                                input.setError(item.FieldHintError);
                            if (item.FieldHintHelp.length() > 0)
                                if (input.getEditText() != null && item.Required) { // Used to clear the "required" error if they type something in.
                                    input.getEditText().addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                            if (s.length() > 0) {
                                                input.setError(null);
                                                input.setErrorEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {
                                        }
                                    });
                                }
                        }
                        break;
                    case LayoutHelperDwr.VIEW_TYPE_TEXT:
                        TextView text = findViewById(id);
                        if (text != null) {
                            text.setVisibility(item.Visibility);
                            if (item.DefaultValue.length() > 0) text.setText(item.DefaultValue);
                        }
                        break;
                    case LayoutHelperDwr.VIEW_TYPE_CHECK:
                        CheckBox box = findViewById(id);
                        if (box != null) {
                            box.setVisibility(item.Visibility);
                            box.setText(item.FieldHint);
                            //box.setChecked(item.DefaultValue.toLowerCase().contentEquals("true"));  // with bound controls, this probably is not going to work.
                        }
                        break;
                    case LayoutHelperDwr.VIEW_TYPE_PHONE:
                        final TextInputEditText phone = findViewById(id);

                        if (phone != null) {
                            phone.setVisibility(item.Visibility);
                            phone.setInputType(InputType.TYPE_CLASS_PHONE);
                            if(phone.getTag() == null) { // Only want to add 1 listener or things go sideways.
                                phone.setTag("PHONE");
                                phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                            }
                        }
                        break;
                    case LayoutHelperDwr.VIEW_TYPE_EDIT:
                        EditText editText = findViewById(id);
                        editText.setOnClickListener(this);
                        break;
                }
            }
        }

        checkProperty();
        ShowHideRoadwayFlagging(this.serviceType, mDwrViewModel.dwrItem.hasRoadwayFlagging);
    }

    private boolean checkCsxImage() {
        if (isPropertyCsx(mDwrViewModel.dwrItem.property)) {
            if (mDwrViewModel.PictureList == null || mDwrViewModel.PictureList.size() < IMAGE_DWR_CXS_MIN_PHOTOS
                    || mDwrViewModel.PictureList.size() > IMAGE_DWR_CXS_MAX_PHOTOS) {
                handleCxsImageError();
                return false;
            }
        }
        return true;
    }

    private boolean checkScrraVehicle() {
        if (isPropertyScraa(mDwrViewModel.dwrItem.property)) {
            if (isBillableDay() && mDwrViewModel.dwrItem.typeOfVehicle.isEmpty()) {
                handleSCRRAVehicleError(getString(R.string.err_required_field));
                return false;
            }
        }
        return true;
    }
    /**
     *  The save button will mostly just save the current data and exit back to the scheduler screen.
     *  The one thing required to save is the Job Number, and an error dialog is displayed if no job
     *  has been selected.
     */
    public void saveDwrData(View view) {
        //Save State of Adapters
        mDwrViewModel.PictureList = mPicAdapter.getPictureList();
        // DWRs are tightly linked to assignments in this app, and if there is no job
        // number, there is no way to find an assignment.  Without it the DWR is orphaned.
        if(mDwrViewModel.dwrItem.getJobNumberId() == 0) {
            ValidationJobNumberDialog();
        } else {
            mDwrViewModel.dwrItem.timeLogged += Calendar.getInstance().getTimeInMillis() - timeEnter;
            timeEnter = 0; // keeps track of time on screen for Austin
            SaveDwrThread work = new SaveDwrThread(this, mDwrViewModel, new Messenger(mHandler), false);
            work.start();
        }
    }

    //*******************************************************************************************/
    /*  All the dialog pickers are located here.  Each picker gets a method to kick it off,    */
    /*  and then all call back into the same method for processing of the result.               */
    /*  It is suggested that for fields used with a picker, they have their EditText android    */
    /*  focus attribute turned off, e.g. android:focusable="false".  This will only allow data  */
    /*  from the picker to be used.                                                             */
    //*******************************************************************************************/

    /*
     *  This Job Picker allows a different job to be selected, but does not allow random
     *  job numbers to be input.  The backend can only process valid job numbers.
     *  The Job Picker Dialog is custom built just for the Picking of Jobs.
     *  **This could be made more general if needed**
     */
    public void JobPicker(View view) {
        FragmentManager mgr = getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_JOB_PICKER);
        if (fragment != null) {
            mgr.beginTransaction().remove(fragment).commit();
        }
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RAILROAD, mDwrViewModel.dwrItem.property);
        JobPickerDialog picker = new JobPickerDialog();
        picker.setArguments(bundle);
        picker.show(mgr, KY_JOB_PICKER);
    }

    /* JobPicker Callback.
    *   Update the job id and display number, and check if there are any cost centers linked
    *   to this Job.  The Job Picker is railroad specific, so changing jobs should not change
    *   the form layout.
     */
    @Override
    public void setJobNumber(JobTbl tbl) {
        mDwrViewModel.dwrItem.setJobNumber(tbl.JobNumber);
        mDwrViewModel.dwrItem.setJobNumberId((tbl.Id));
        CostCenterThread cost = new CostCenterThread(this, mDwrViewModel.dwrItem.getJobNumberId(), new Messenger(mHandler));
        cost.start();
        JobOneThread jobo = new JobOneThread(this, mDwrViewModel.dwrItem.getJobNumberId(), new Messenger(mHandler));
        jobo.start();
    }

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
            case R.array.state_name:
                mDwrViewModel.dwrItem.setLocationState(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.KCS_Districts:
                mDwrViewModel.dwrItem.setDistrict(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.property_name:
                mPropertyId = Railroads.PropertyKey(getApplicationContext(), Railroads.GetPropertiesSorted(getApplicationContext()).get(selection));
                mDwrViewModel.dwrItem.setProperty(Railroads.PropertyName(getApplicationContext(), mPropertyId));
                if(!mDwrViewModel.getRailRoadCode().equalsIgnoreCase(Railroads.PropertyNameServer(getApplicationContext(), mPropertyId))) {
                    // Little trick to force a new database load for subdivisions.
                    mDwrViewModel.setRailRoadCode(Railroads.PropertyNameServer(getApplicationContext(), mPropertyId));
                    mDwrViewModel.GetSubdivisions().removeObserver(mSubDivisionObserver);
                    mDwrViewModel.GetSubdivisions().observe(this, mSubDivisionObserver);
                }
                ShowHideForms(mDwrViewModel.CurrentFormType, mPropertyId);
                break;
            case R.string.msg_inform_no_subdivisions:
                mDwrViewModel.dwrItem.setSubdivision(mDwrViewModel.dwrItem.SubDivisionList.get(selection));
                break;
            case R.array.temp_special_cost:
                List<String> holdCostCenters = Functions.DisplayCostCenters(getApplicationContext(), mDwrViewModel.dwrItem.getAllCostCenters());
                if(holdCostCenters != null){
                    mDwrViewModel.dwrItem.setSpecialCostCenterDspl(holdCostCenters.get(selection));
                } else {
                    mDwrViewModel.dwrItem.setSpecialCostCenterDspl(Arrays.asList(getResources().getStringArray(source)).get(selection));
                }
                if(holdCostCenters != null && selection > 0) {
                    mDwrViewModel.dwrItem.setSpecialCostCenterReal(Functions.RawCostCenters(mDwrViewModel.dwrItem.getAllCostCenters()).get(selection - 1));
                } else {
                    mDwrViewModel.dwrItem.setSpecialCostCenterReal("");
                }
        }
    }

    /**
     *  The basic fragment support for showing a simple listbox picker.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param source    The resource id of an array of values used to provide selection options.
     * @param values    An optional list of values to use instead of the one hardcoded in source.
     */
    private void simpleListRequest(int title, int source, List<String> values) {
        // Set up the fragment.
        FragmentManager mgr = getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_LIST_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the picker.
        SimpleListDialog picker = new SimpleListDialog(title, source);
        if(values != null){
            picker.ChangeListArray(values);
        }
        picker.show(mgr, KY_SIMPLE_LIST_FRAG);
    }
    private void simpleListRequest(int title, int source) { simpleListRequest(title, source, null); }

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
            case R.string.msg_confirm_submitDWR:
                // Save State of pictures.
                mDwrViewModel.PictureList = mPicAdapter.getPictureList();
                // Perform validation.  If ok, let the SaveDwrThread know this is a save/submit.
                if (ValidationOk(mDwrViewModel.CurrentFormType, mPropertyId)) {
                    mDwrViewModel.dwrItem.timeLogged += Calendar.getInstance().getTimeInMillis() - timeEnter;
                    timeEnter = 0; // keeps track of time on screen for Austin
                    SaveDwrThread work = new SaveDwrThread(this, mDwrViewModel, new Messenger(mHandler), true);
                    work.start();
                } else {
                    if(!CheckSCRRA()) {
                        findViewById(R.id.protectionErrorMessage).setVisibility(View.VISIBLE);
                    }
                    ShowFabMenuOpen();       // Toggle Fab menu off.
                    ValidationDwrDialog(); // Show informational dialog.
                }
                break;
            case R.string.msg_confirm_exitDWR:
                DwrEdit.super.onBackPressed();
                break;
            case R.string.msg_confirm_delteDWR:
                if(mDwrId != 0) {
                    DeleteItemThread work = new DeleteItemThread(this, FORM_DWR_TYPE, mDwrId, new Messenger(mHandler));
                    work.start();
                } else {
                    // No harm in trying to delete something that has not yet been saved.
                    DeleteSuccess();
                }
                break;
            case R.string.msg_inform_validation_dwr:
                // Informational Only
                break;
            case R.string.msg_inform_validation_jobnbr:
                // Informational Only
                break;
            case R.string.msg_inform_pictures_max:
                // Informational Only
                break;
            case R.string.msg_confirm_job_service:
                mDwrViewModel.dwrItem.hasRoadwayFlagging = true;
                ShowHideRoadwayFlagging(-1, mDwrViewModel.dwrItem.hasRoadwayFlagging);
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

    /**
     *  Simple Picker Dialog callback. The simple radio button picker is used by a number of
     *  methods.  This is where the selections are properly assigned.  The source array is
     *  used as the key to determine what code needs to deal with the resulting selection.
     * @param source    The selection index, e.g chosen value.
     * @param selection The resource key of the original array provided as the list.
    */
    @Override
    public void simplePickerResponse(int source, int selection) {

        switch (source) {
            case R.array.classification_name:   // See ClassificationPicker
                mDwrViewModel.CurrentFormType = selection;
                mDwrViewModel.dwrItem.setClassification(Arrays.asList(getResources().getStringArray(source)).get(selection));
                ShowHideForms(mDwrViewModel.CurrentFormType, mPropertyId);
                break;
            case R.array.nonbilledreason_name_max: // See NonBilledReasonPicker
            case R.array.nonbilledreason_name_scrra:
            case R.array.nonbilledreason_name_base:
                int NON_BILL_HOLDOVER = 0;
                mDwrViewModel.dwrItem.setNonBilledReason(Arrays.asList(getResources().getStringArray(source)).get(selection));
                if (selection == NON_BILL_HOLDOVER) { //  Validation not required, so remove asterisk.
                    TextInputLayout totalHoursIL = findViewById(R.id.fieldWorkHoursRounded);
                    if (totalHoursIL != null && totalHoursIL.getHint() != null) {
                        String holdHint = totalHoursIL.getHint().toString();
                        totalHoursIL.setHint(holdHint.replaceAll("\\*", ""));
                    }
                }
                break;
            case R.array.track_foul_name:   // TrackFoulPicker
                mDwrViewModel.dwrItem.setWorkOnTrackId(selection);
                mDwrViewModel.dwrItem.setWorkOnTrackDspl(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.csx_region_name:   // See RegionPicker
                mDwrViewModel.dwrItem.setCsxDataRegionId(selection);
                mDwrViewModel.dwrItem.setCsxDataRegionDspl(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.type_of_vehicle:   // See TypeOfVehiclePicker
                mDwrViewModel.dwrItem.setTypeOfVehicle(Arrays.asList(getResources().getStringArray(source)).get(selection));
                handleSCRRAVehicleError(null);
                break;
            case R.array.wmata_lines:   // See WmataLinePicker
                mDwrViewModel.dwrItem.setInputWMLine(Arrays.asList(getResources().getStringArray(source)).get(selection));
                // The Line dictates the station, so if line changes probably need to reselect station.
                mDwrViewModel.dwrItem.setInputWMStation("");
                mDwrViewModel.dwrItem.setInputWMStationName("");
                break;
            case R.array.wmata_stations_blue:   // See WmataStationNbrPicker
            case R.array.wmata_stations_green:
            case R.array.wmata_stations_orange:
            case R.array.wmata_stations_red:
            case R.array.wmata_stations_yellow:
            case R.array.wmata_stations_silver:
                mDwrViewModel.dwrItem.setInputWMStation(Arrays.asList(getResources().getStringArray(source)).get(selection));
                // The Station Name is part of the station number.  Break it out here for the backend where the name and number are split.
                mDwrViewModel.dwrItem.setInputWMStationName(Arrays.asList(getResources().getStringArray(source)).get(selection).substring(5).replace(")", ""));
                break;
            case R.array.wmata_tracks:
                mDwrViewModel.dwrItem.setInputWMTrack(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.wmata_yards:
                mDwrViewModel.dwrItem.setSubdivision(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.conditions:
                mDwrViewModel.dwrItem.setDescWeatherConditions(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.first_stations:
                mDwrViewModel.dwrItem.setMpStart(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.last_stations:
                mDwrViewModel.dwrItem.setMpEnd(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.per_diem:
                mDwrViewModel.dwrItem.setPerdiemDspl(Arrays.asList(getResources().getStringArray(source)).get(selection));
                mDwrViewModel.dwrItem.setPerdiemId(selection);
                break;
            case R.array.shifts:
                mDwrViewModel.dwrItem.setDescTypeOfWork(Arrays.asList(getResources().getStringArray(source)).get(selection));
                break;
            case R.array.per_diem_wmata:
                mDwrViewModel.dwrItem.setPerdiemDspl(Arrays.asList(getResources().getStringArray(source)).get(selection));
                // WMATA uses a subset of the per diem selections.  Here we map back to the real index.
                int cnt = 0; int holdSelection = selection;
                for (String item : getResources().getStringArray(R.array.per_diem)) {
                    if (getResources().getStringArray(source)[holdSelection].equalsIgnoreCase(item)) {
                        selection = cnt;
                    }
                    cnt++;
                }
                mDwrViewModel.dwrItem.setPerdiemId(selection);
                break;
        }
    }

    /**
     *  The basic fragment support for showing a simple radio button picker.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param source    The resource id of an array of values used to provide selection options.
     * @param selection The default, zero based selection.  If negative one (-1), nothing selected.
    */
    private void simplePickerRequest(int title, int source, int selection) {
        // Set up the fragment.
        FragmentManager mgr = getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_PICKER_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the picker.
        SimplePickerDialog picker = new SimplePickerDialog(title, source, selection, false);
        picker.show(mgr, KY_SIMPLE_PICKER_FRAG);
    }

    /**
     * This manages time returned from the SimpleHoursDialog.
     *
     * @param totalTime Formatted number of hours between start and end time.
     * @param startTime The start time. Sample(10am): 1970-01-01T10:00:00-0800
     * @param endTime   The end time. Sample(1:30pm): 1970-01-01T13:30:00-0800.
     * @param source    The resource id of an array of values used to provide selection options.
     */
    @Override
    public void setTimePicker(String totalTime, String startTime, String endTime, int source) {
        TextView mTextView = findViewById(source);
        boolean showHours = true;
        switch (source) {
            case R.id.inputTravelToJob:
                mDwrViewModel.dwrItem.setTravelToJobStartTime(startTime);
                mDwrViewModel.dwrItem.setTravelToJobEndTime(endTime);
                mDwrViewModel.dwrItem.setTravelToJobHours(totalTime);
                if(totalTime.length() == 0) showHours = false;
                break;
            case R.id.inputWorkHoursRounded:
                mDwrViewModel.dwrItem.setWorkStartTime(startTime);
                mDwrViewModel.dwrItem.setWorkEndTime(endTime);
                mDwrViewModel.dwrItem.setWorkHoursRounded(totalTime);
                if(totalTime.length() == 0) showHours = false;
                break;
            case R.id.inputTravelFromJob:
                mDwrViewModel.dwrItem.setTravelFromJobStartTime(startTime);
                mDwrViewModel.dwrItem.setTravelFromJobEndTime(endTime);
                mDwrViewModel.dwrItem.setTravelFromJobHours(totalTime);
                if(totalTime.length() == 0) showHours = false;
                break;
            case R.id.inputWorkLunchTime:
                try {
                    mDwrViewModel.dwrItem.setWorkLunchTime(KTime.ParseToFormat(startTime, KTime.KT_fmtDate3339k, "", KTime.KT_fmtDateTime24, "").toString());
                    mDwrViewModel.dwrItem.setLunchHoursStartTime(startTime);
                    mDwrViewModel.dwrItem.setLunchHoursEndTime(endTime);
                    showHours = false;
                } catch (ExpParseToCalendar expParseToCalendar) { mDwrViewModel.dwrItem.setWorkLunchTime(""); showHours = false; }
                break;
            case R.id.inputWorkBriefTime:
                try {
                    mDwrViewModel.dwrItem.setWorkBriefTime(KTime.ParseToFormat(startTime, KTime.KT_fmtDate3339k, "", KTime.KT_fmtDateTime24, "").toString());
                    mDwrViewModel.dwrItem.setBriefHoursStartTime(startTime);
                    mDwrViewModel.dwrItem.setBriefHoursEndTime(endTime);
                    showHours = false;
                } catch (ExpParseToCalendar expParseToCalendar) { mDwrViewModel.dwrItem.setWorkBriefTime(""); showHours = false; }
        }

        if (source != 0 && showHours) {
            String sTime, eTime;
            try {
                sTime = KTime.ParseToFormat(startTime, KTime.KT_fmtDate3339k, "", KTime.KT_fmtDateTime24, "").toString();
                eTime = KTime.ParseToFormat(endTime, KTime.KT_fmtDate3339k, "", KTime.KT_fmtDateTime24, "").toString();
            } catch (ExpParseToCalendar expParseToCalendar) {
                sTime = "";
                eTime = "";
            }
            String hoursDisplayFmt = getResources().getString(R.string.display_work_times);
            ((TextInputLayout) (mTextView.getParent()).getParent()).setHelperText(String.format(Locale.getDefault(), hoursDisplayFmt, sTime, eTime));
        } else {
            if(source != 0){
                ((TextInputLayout) (mTextView.getParent()).getParent()).setHelperText("");
            }
        }
    }

    /**
     *  The Hours Picker dialog lets a user select a starting and ending hour from
     *  a list in 1/2 hour increments from a 24 hour clock.  Note its use of bundled
     *  arguments instead of a constructor.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param source    The resource id of an array of values used to provide selection options.
     * @param start     The start time (generally this is only populated after first use).
     * @param finish    The finish time (generally this is only populated after first use).
     */
    public void simpleHoursRequest(int title, int source, String start, String finish){
        // Set up the fragment.
        FragmentManager mgr = getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_TIME_PICKER);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }

        // Launch the picker.

        SimpleHoursDialog picker = new SimpleHoursDialog(mDwrViewModel.dwrItem.getWorkDate());
        Bundle args = new Bundle();
        args.putInt(IN_PICKER_ID, source);
        args.putString(IN_START_TIME, start);
        args.putString(IN_END_TIME, finish);
        args.putString(IN_TITLE_ID, getResources().getString(title));

        picker.setArguments(args);
        picker.show(mgr, KY_TIME_PICKER);
    }

    /*
     *  This radio button dialog is used to pick the Day Classification.
    */
    public void ClassificationPicker(View view){
        // Initialization.
        int pickerList = R.array.classification_name;
        int pickerTitle = R.string.title_pick_class;
        int selection = mDwrViewModel.CurrentFormType;

        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the non-billable reason.
    */
    public void NonBilledReasonPicker(View view) {
        // Initialization.
        int pickerList = R.array.nonbilledreason_name_base;
        if (Railroads.GetTemplateKey(mDwrViewModel.dwrItem.getProperty()) == Railroads.DWR_TEMPLATE_SCRRA) {
            pickerList = R.array.nonbilledreason_name_scrra;
        }
        int pickerTitle = R.string.title_pick_nonbilledreason;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getNonBilledReason() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getNonBilledReason().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Track Condition.
    */
    public void TrackFoulPicker(View view) {
        // Initialization.
        int pickerList = R.array.track_foul_name;
        int pickerTitle = R.string.title_pick_track_foul;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getWorkOnTrackDspl() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getWorkOnTrackDspl().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the CSX Region.
    */
    public void RegionPicker(View view) {
        // Initialization.
        int pickerList = R.array.csx_region_name;
        int pickerTitle = R.string.title_pick_csx_region;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getCsxDataRegionDspl() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getCsxDataRegionDspl().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Type of Vehicle (SCRRA).
     */
    public void TypeOfVehiclePicker(View view){
        // Initialization.
        int pickerList = R.array.type_of_vehicle;
        int pickerTitle = R.string.title_pick_vehicle;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getTypeOfVehicle() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getTypeOfVehicle().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Line for WMATA.
    */
    public void WmataLinePicker(View view){
        // Initialization.
        int pickerList = R.array.wmata_lines;
        int pickerTitle = R.string.title_pick_wmata_line;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getInputWMLine() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getInputWMLine().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Station Number for WMATA.
    */
    public void WmataStationNbrPicker(View view){
        // Initialization.
        int pickerList = R.array.wmata_stations_blue;
        int pickerTitle = R.string.title_pick_wmata_station;
        int selection = 0;  // Can set to -1 if do not want any default.

        // The Station list depends on the Line
        if(mDwrViewModel.dwrItem.getInputWMLine() != null) {
            switch (mDwrViewModel.dwrItem.getInputWMLine()) {
                case "Blue":
                    pickerList = R.array.wmata_stations_blue;
                    break;
                case "Green":
                    pickerList = R.array.wmata_stations_green;
                    break;
                case "Orange":
                    pickerList = R.array.wmata_stations_orange;
                    break;
                case "Red":
                    pickerList = R.array.wmata_stations_red;
                    break;
                case "Yellow":
                    pickerList = R.array.wmata_stations_yellow;
                    break;
                case "Silver":
                    pickerList = R.array.wmata_stations_silver;
                    break;
            }
        }
        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getInputWMStation() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getInputWMStation().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Track for WMATA.
    */
    public void WmataTrackPicker(View view) {
        // Initialization.
        int pickerList = R.array.wmata_tracks;
        int pickerTitle = R.string.title_pick_wmata_track;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getInputWMTrack() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getInputWMTrack().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Yard for WMATA.
    */
    public void WmataYardPicker(View view) {
        // Initialization.
        int pickerList = R.array.wmata_yards;
        int pickerTitle = R.string.title_pick_wmata_yard;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getSubdivision() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getSubdivision().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Weather Condition.
     */
    public void WeatherPicker(View view) {
        // Initialization.
        int pickerList = R.array.conditions;
        int pickerTitle = R.string.title_pick_wmata_weather;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getDescWeatherConditions() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getDescWeatherConditions().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Starting Station for WMATA.
     */
    public void FirstStationPicker(View view) {
        // Initialization.
        int pickerList = R.array.first_stations;
        int pickerTitle = R.string.title_pick_wmata_station;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getMpStart() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getMpStart().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Ending Station for WMATA.
     */
    public void LastStationPicker(View view) {
        // Initialization.
        int pickerList = R.array.last_stations;
        int pickerTitle = R.string.title_pick_wmata_station;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getMpEnd() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getMpEnd().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This radio button dialog is used to pick the Per Diem (or no Per Diem).
    */
    public void PerDiemPicker(View view) {
        // Initialization.
        int pickerList = R.array.per_diem;
        int pickerTitle = R.string.title_pick_per_diem;
        int selection = 0;

        // The WMATA Property has less options.
        if(Railroads.GetTemplateKey(mDwrViewModel.dwrItem.getProperty()) == Railroads.DWR_TEMPLATE_WMATA){
            pickerList = R.array.per_diem_wmata;
        } else if(hasRoadwayFlagging() && !isBillableDayUtility()) {
            pickerList = R.array.per_diem_roadway_flagging;
        }
        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getPerdiemDspl() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getPerdiemDspl().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }

    /*
     *  This confirmation dialog is used to submit the DWR to the backend.
    */
    public void SubmitDwrPicker(View view) {
       simpleConfirmRequest(R.string.title_confirm_submitDWR, R.string.msg_confirm_submitDWR, true);
    }

    /*
     *  This confirmation dialog is used to confirm that they want to delete the DWR.
    */
    public void DeleteDwrPicker(View view) {
        simpleConfirmRequest(R.string.title_confirm_deleteDWR, R.string.msg_confirm_delteDWR, true);
    }

    /*
     *  This confirmation dialog is used to warn the user they will lose any changes.
    */
    @Override
    public void onBackPressed() {
        simpleConfirmRequest(R.string.title_confirm_exitDWR, R.string.msg_confirm_exitDWR, true);
    }

    /*
     *  This informational dialog is used to inform the user that the validation failed.
    */
    public void ValidationDwrDialog() {
        simpleConfirmRequest(R.string.title_inform_validation, R.string.msg_inform_validation_dwr, false);
    }

    /*
     * This informational dialog is used to inform the user that there is no Job Number entered.
     */
    public void ValidationJobNumberDialog() {
        simpleConfirmRequest(R.string.title_inform_validation, R.string.msg_inform_validation_jobnbr, false);
    }

    /*
     * This informational dialog is used to inform the user that they have reached the max number of photos to upload.
    */
    public void MaxPhotoUploadDialog() {
        simpleConfirmRequest(R.string.title_inform_pictures_max, R.string.msg_inform_pictures_max, false);
    }

    public void ConfirmJobServiceDialog() {
        simpleConfirmRequest(R.string.title_confirm_job_service, R.string.msg_confirm_job_service, true);
    }

    /*
     * This listbox dialog is used to pick the state code of where the work was performed.
    */
    public void LocationStatePicker(View view) {
        simpleListRequest(R.string.title_pick_state, R.array.state_name);
    }

    /*
     *  This listbox dialog is used to pick the property (railroad) associated with the work.
     *  Note: This uses a dynamic array instead of the classic hard coded resource.
    */
    public void PropertyPicker(View view) {
        simpleListRequest(R.string.title_pick_property, R.array.property_name, Railroads.GetPropertiesSorted(getApplicationContext()));
    }

    /*
     *  This listbox dialog is used to pick the district of where the work was performed for KCS.
     */
    public void DistrictPicker(View view) {
        simpleListRequest(R.string.title_pick_district, R.array.KCS_Districts);
    }

    /*
     *  This listbox dialog is used to pick the subdivision of where the work was performed. If
     *  there are no subdivisions, an information message is shown.
     */
    public void DivisionPicker(View view) {

        if(mDwrViewModel.dwrItem.SubDivisionList.size() > 0) {
            simpleListRequest(R.string.title_pick_subdivision, R.string.msg_inform_no_subdivisions, mDwrViewModel.dwrItem.SubDivisionList);
        }else {
            simpleConfirmRequest(R.string.title_pick_subdivision, R.string.msg_inform_no_subdivisions, false);
        }
    }

    /*
     *  This lets the user know there was a problem trying to delete their form and they should try again or give up.
    */
    public void DeleteFailure(){
        simpleConfirmRequest(R.string.title_inform_delete, R.string.msg_inform_deletion_fail, false);
    }

    /*
     *  This allows entry of hours worked.
     */
    public void WorkHoursRoundedPicker(View view){
        simpleHoursRequest(R.string.title_hours_worked, R.id.inputWorkHoursRounded, mDwrViewModel.dwrItem.getWorkStartTime(), mDwrViewModel.dwrItem.getWorkEndTime());
    }

    /*
     *  This allows entry of a special cost center for hours worked.
     */
    public void SpecialCostCenterPicker(View view){
        simpleListRequest(R.string.title_cost_center, R.array.temp_special_cost, Functions.DisplayCostCenters(getApplicationContext(), mDwrViewModel.dwrItem.getAllCostCenters()));
    }

    /*
     *  This allows entry of hours traveled to a job site.
    */
    public void TravelToHoursPicker(View view){
        simpleHoursRequest(R.string.title_hours_traveled_to, R.id.inputTravelToJob, mDwrViewModel.dwrItem.getTravelToJobStartTime(), mDwrViewModel.dwrItem.getTravelToJobEndTime());
    }

    /*
     *  This allows entry of hours traveled from a job site.
    */
    public void TravelFromHoursPicker(View view){
        simpleHoursRequest(R.string.title_hours_traveled_from, R.id.inputTravelFromJob, mDwrViewModel.dwrItem.getTravelFromJobStartTime(), mDwrViewModel.dwrItem.getTravelFromJobEndTime());
    }

    /*
     *  This allows entry of hours spent in a briefing.
    */
    public void BriefingHoursPicker(View view){
        simpleHoursRequest(R.string.title_hours_briefed, R.id.inputWorkBriefTime, mDwrViewModel.dwrItem.getBriefHoursStartTime(), mDwrViewModel.dwrItem.getBriefHoursEndTime());
    }

    /*
     *  This allows entry of hours spent at a lunch.
    */
    public void LunchHoursPicker(View view){
        simpleHoursRequest(R.string.title_hours_lunched, R.id.inputWorkLunchTime, mDwrViewModel.dwrItem.getLunchHoursStartTime(), mDwrViewModel.dwrItem.getLunchHoursEndTime());
    }

    /*
     *  Helpful dialog to show the DWR id as stored on the server.  This can aid in trouble shooting.
     */
    public void ShowDwrServerId(View view){
        simpleDisplayRequest(0, String.format(getResources().getString(R.string.msg_dwr_server_id), mDwrViewModel.dwrItem.dwrSrvrId));
    }

    /*
     *  This provides a selection for the shift (day/night).
    */
    public void UtilityShiftPicker(View view){
        // Initialization.
        int pickerList = R.array.shifts;
        int pickerTitle = R.string.title_pick_shift;
        int selection = 0;

        // Nice to have the picker show the existing value.
        int cntSelection = 0;
        if(mDwrViewModel.dwrItem.getDescTypeOfWork() != null) {
            for (String item : getResources().getStringArray(pickerList)) {
                if (mDwrViewModel.dwrItem.getDescTypeOfWork().equalsIgnoreCase(item)) {
                    selection = cntSelection;
                }
                cntSelection++;
            }
        }
        simplePickerRequest(pickerTitle, pickerList, selection);
    }
    //*******************************************************************************************/
    /* End of Dialog Pickers
    //*******************************************************************************************/

    /*
     *  This is used to pick the day of work.  It is a self-contained use of the date picker
     *  dialog.  There is something odd with the theme/buttons, but trying to override the
     *  color shifts the screen layout around, so just adjusting programmatically.
     *  NOTE: The callback is just integrated into the code, as it is using a system dialog.
     */
    public void DayPicker(View view) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {    // use APIv1 constructor for older versions.
            datePickerDialog = new DatePickerDialog(this, null, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        } else {
            datePickerDialog = new DatePickerDialog(this);
        }
        datePickerDialog.setCancelable(true);
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Calendar work = Calendar.getInstance();
                work.set(datePickerDialog.getDatePicker().getYear(), datePickerDialog.getDatePicker().getMonth(), datePickerDialog.getDatePicker().getDayOfMonth(), 0, 0, 0);
                mDwrViewModel.dwrItem.setWorkDate(DateFormat.format(KTime.KT_fmtDateShrtMiddle, work).toString());
            }
        });
        // Somehow the buttons default to the background color and so are invisible unless we force them to the desired color.
        // Tried styles, but was hard to find something that would work, so just manually set the color here.
        datePickerDialog.create();
        datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        if (mDwrViewModel.CurrentDwr() != null) {
            try {
                Calendar work = KTime.ParseToCalendar(mDwrViewModel.CurrentDwr().WorkDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE);
                work = KTime.ConvertTimezone(work, TimeZone.getDefault().getID());
                datePickerDialog.getDatePicker().updateDate(work.get(Calendar.YEAR), work.get(Calendar.MONTH), work.get(Calendar.DAY_OF_MONTH));
            } catch (ExpParseToCalendar expParseToCalendar) {
                /* The default is today, which is fine. */
            }
        }
        datePickerDialog.show();
    }

    public void launchSignature(View view) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(KY_SIGN_PICKER);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment sigFrag = new SignatureDialog();
        Bundle args = new Bundle();
        args.putInt(IN_SIGN_ID, view.getId());
        sigFrag.setArguments(args);
        sigFrag.show(ft, KY_SIGN_PICKER);
    }

    /* Signatures dialog. */
    @Override
    public void setSignatureImage(String imageName, int id) {
        try {
            switch (id) {
                case R.id.fieldRwicTimeSignatureIcon:
                case R.id.imageRwicTimeSignature:
                    mDwrViewModel.dwrItem.setFlagmanSignaturePhotoName(imageName);
                    mDwrViewModel.dwrItem.setFlagmanSignaturePhotoDate(KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString());
                    CheckSignature(R.id.layoutRwicTimeSignature, R.id.layoutRwicTimeSignatureError, R.id.cardviewRwicSignatureboarder, mDwrViewModel.dwrItem.flagmanSignaturePhotoName);
                    if(imageName == null) { // When there is no file, clear out the visible image too.
                        AppCompatImageView imageView = findViewById(R.id.imageRwicTimeSignature);
                        imageView.setImageDrawable(null);
                    }
                    break;
                case R.id.fieldClientTimeSignatureIcon:
                case R.id.imageClientTimeSignature:
                    mDwrViewModel.dwrItem.setClientSignaturePhotoName(imageName);
                    mDwrViewModel.dwrItem.setClientSignaturePhotoDate(KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString());
                    CheckSignature(R.id.layoutClientSignatureInfo, R.id.layoutClientTimeSignatureError, R.id.cardviewClientSignatureboarder, mDwrViewModel.dwrItem.clientSignaturePhotoName);
                    if(imageName == null) { // When there is no file, clear out the visible image too.
                        AppCompatImageView imageView = findViewById(R.id.imageClientTimeSignature);
                        imageView.setImageDrawable(null);
                    }
                    break;
                case R.id.fieldRailSignatureIcon:
                case R.id.imageRailTimeSignature:
                    mDwrViewModel.dwrItem.setRailSignaturePhotoName(imageName);
                    mDwrViewModel.dwrItem.setRailSignaturePhotoDate(KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString());
                    CheckSignature(R.id.layoutRailSignatureInfo, R.id.layoutRailTimeSignatureError, R.id.cardviewRailSignatureboarder, mDwrViewModel.dwrItem.railSignaturePhotoName);
                    if(imageName == null) { // When there is no file, clear out the visible image too.
                        AppCompatImageView imageView = findViewById(R.id.imageRailTimeSignature);
                        imageView.setImageDrawable(null);
                    }
                default:
                    break;
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, imageName);
            simpleDisplayRequest(0, getResources().getString(R.string.err_signature_reg));
        }
    }

    /* Image Picker for Image Library.  Currently Not Used by request of business. */
    public void ImagePicker(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    /*
        Get Max Photos based on property
     */
    private int getDwrMaxPhotos() {
        if (isPropertyCsx(mDwrViewModel.dwrItem.property)) {
            return IMAGE_DWR_CXS_MAX_PHOTOS;
        }
        return IMAGE_DWR_MAX_PHOTOS;
    }

    /*
        Picker for Adapter and Today Sign in to Launch Camera. Allows for a max of N pictures
     */
    public void PhotoPicker(View view) {
        if (mDwrViewModel.PictureList == null || mDwrViewModel.PictureList.size() < getDwrMaxPhotos()) {
            switch (view.getId()) {
                case R.id.fieldSigninSheetIcon:
                    launchPicturePicker(CAMERA_REQUEST, IMAGE_SIGNUP, DOC_IMAGE_SIGNIN);
                    break;
                case R.id.fieldPicturesIcon:
                    launchPicturePicker(CAMERA_REQUEST_ADAPTER, IMAGE_WORKSITE, DOC_IMAGE_DWR);
                    break;
            }
        } else {
            MaxPhotoUploadDialog();
        }
    }

    public void launchPicturePicker(int REQUEST_ID, String prefix, int document) {
        try {
            String[] PERMISSIONS = {android.Manifest.permission.CAMERA};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, MY_PERMISSIONS_CAMERA);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = Functions.GetTempImageFile(document, prefix);
                    mDwrViewModel.dwrItem.pictureAnyUri = FileProvider.getUriForFile(this, getPackageName() + ".share", photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mDwrViewModel.dwrItem.pictureAnyUri);
                    startActivityForResult(cameraIntent, REQUEST_ID);
                } else {
                    throw new ExpClass(17001, "INFORMATIONAL", "The cameraIntent.resolveActivity failed to find a default camera app to load.", "");
                }
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mUser.unique + " :" + "launchPicturePicker - General error in method.");
            simpleDisplayRequest(R.string.title_inform_display, getResources().getString(R.string.msg_inform_error_info));
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    simpleDisplayRequest(0, getResources().getString(R.string.msg_no_permission_storage));
                }
            }
            case MY_PERMISSIONS_CAMERA: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    simpleDisplayRequest(0, getResources().getString(R.string.msg_no_permission_camera));
                }
            }
        }
    }

    public void SignatureLauncher(View view) {
        String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, MY_PERMISSIONS_REQUEST_STORAGE);
        } else {
            launchSignature(view);
        }
    }

    // There is no cached data and no network (or some other catastrophe).
    private void DwrRefreshErr(int status) {
        simpleDisplayRequest(0, String.format(Locale.US, getResources().getString(R.string.err_api_dwr_refresh), status));
    }

    // There is no cached data and no network (or some other catastrophe).
    private void DwrDuplicateErr(int status) {
        simpleDisplayRequest(0, String.format(Locale.US, getResources().getString(R.string.err_api_dwr_dup), status));
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    public void ShowFABmenu(View view) {
        View current = getCurrentFocus();
        if (current != null) current.clearFocus();

        if (fabMenuToggle) {
            ShowFabMenuOpen();
        } else {
            ShowFabMenuClose();
        }
    }

    public void ShowFabMenuOpen() {
        fabMenu.startAnimation(fabSpinOn);
        fabSave.startAnimation(fabTapOff);
        fabSubmit.startAnimation(fabTapOff);
        binding.dwrContents.dwrScrollview.startAnimation(recyLight);
        lblSave.setVisibility(View.INVISIBLE);
        lblSubmit.setVisibility(View.INVISIBLE);
        fabSubmit.setClickable(false);
        fabSave.setClickable(false);
        fabDelete.startAnimation(fabTapOff);
        lblDelete.setVisibility(View.GONE);
        fabDelete.setClickable(false);
        fabMenuToggle = false;
    }

    public void ShowFabMenuClose() {
        fabMenu.startAnimation(fabSpinOff);
        fabSave.startAnimation(fabTapOn);
        fabSubmit.startAnimation(fabTapOn);
        binding.dwrContents.dwrScrollview.startAnimation(recyDark);
        lblSave.setVisibility(View.VISIBLE);
        lblSubmit.setVisibility(View.VISIBLE);
        fabSave.setClickable(true);
        fabSubmit.setClickable(true);
        fabDelete.startAnimation(fabTapOn);
        lblDelete.setVisibility(View.VISIBLE);
        fabDelete.setClickable(true);
        fabMenuToggle = true;
    }

    public void FabMenuDisable() {
        fabMenu.hide();
        fabMenu.setClickable(false);
    }

    public void FabMenuEnable() {
        fabMenu.show();
        fabMenu.setClickable(true);
    }

    public void ExitDwr() {
        finish();
    }

    public void DeleteSuccess() {
        Intent intent = new Intent(getApplicationContext(), Schedule.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
    }

    private void disableEnableControls(boolean enable, ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                disableEnableControls(enable, (ViewGroup) child);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            mDwrViewModel.dwrItem.setPictureSignInUri(mDwrViewModel.dwrItem.pictureAnyUri);
            CheckCSX();
        } else if (requestCode == CAMERA_REQUEST_ADAPTER && resultCode == Activity.RESULT_OK) {
            PictureField mNew = new PictureField(mDwrViewModel.dwrItem.pictureAnyUri, "", 90);
            mPicAdapter.addItem(mNew);
            if (photoRV.getLayoutManager() != null)
                photoRV.getLayoutManager().scrollToPosition(mPicAdapter.getItemCount() - 1);
            photoRV.getParent().requestChildFocus(photoRV, photoRV);
            CheckCSX();
        } else if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            PictureField mNew = new PictureField(data.getData(), "", 90);
            mPicAdapter.addItem(mNew);
            if (photoRV.getLayoutManager() != null)
                photoRV.getLayoutManager().scrollToPosition(mPicAdapter.getItemCount() - 1);
            photoRV.getParent().requestChildFocus(photoRV, photoRV);
        }
    }
}