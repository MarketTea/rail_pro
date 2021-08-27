package com.railprosfs.railsapp.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.DateFormat;

import com.google.gson.Gson;
import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.data.ScheduleDB;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrDao;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data_layout.Metrics;
import com.railprosfs.railsapp.data_layout.WorkflowDao;
import com.railprosfs.railsapp.data_layout.WorkflowTbl;
import com.railprosfs.railsapp.data.observable.DwrItem;
import com.railprosfs.railsapp.ui_support.DwrViewModel;
import com.railprosfs.railsapp.ui_support.PhoneNumberSupport;
import com.railprosfs.railsapp.ui_support.PictureField;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.railprosfs.railsapp.service.WebServiceModels.*;
import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  Save the DWR form into the local database.  Optionally
 *  add a request to the event queue, to have the DWR sent
 *  to the backend.  If this DWR was created without care
 *  of an assignment, a placeholder assignment is created.
 */
public class SaveDwrThread extends Thread {

    private final DwrViewModel dvm;
    private final ScheduleDB db;
    private final Messenger callback;
    private final Actor user;
    private final Context context;
    private final boolean submit;
    private static final String WAMATA_DESC_FILLER = "#_02 #_03 #_04 #_05 #_06 #_07 #_08 #_09 #_10 #_11 #_12 #_13 #_14 #_15 ";
    private int classificationNdx = 0;

    public SaveDwrThread(Context ctx, DwrViewModel dvm, Messenger callback, boolean submitToggle) {
        this.dvm = dvm;
        this.db = ScheduleDB.getDatabase(ctx);
        this.callback = callback;
        this.user = new Actor(ctx);
        this.submit = submitToggle;
        context = ctx;
    }

    @Override
    public void run() {
        try {
            updateDatabase();
            if(submit) {
                AssignmentTbl assignment = this.db.assignmentDao().GetByJobId(dvm.getJobId());
                queueUpload(assignment.ServiceType == RP_ROADWAY_FLAGGING_SERVICE ? Q_TYPE_DWR_ROADWAY_FLAGGING_SAVE : Q_TYPE_DWR_SAVE);
            }
            linkAssignment(dvm.dwrItem.getJobNumberId(), classificationNdx == DWR_TYPE_BILLABLE_DAY_UTILITY ? RP_UTILITY_SERVICE : RP_FLAGGING_SERVICE);
            ReturnOk();
        }
        catch (Exception e) {
            ReturnErr();
        }
    }

    private void queueUpload(int eventType) {

        // Add to event queue
        WorkflowDao wfDao = db.workflowDao();
        WorkflowTbl wfTbl = wfDao.GetWorkflowTblUniqueKeyAndType(dvm.getDwrId(), eventType);
        if(wfTbl == null) {
            wfTbl = new WorkflowTbl();
            wfTbl.Pending = KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
            wfTbl.Priority = HIGH_PRIORITY_EVENT;
            wfTbl.EventType = eventType;
            wfTbl.EventKey = dvm.getDwrId();
            wfTbl.Retry = 0;
            wfTbl.RetryMax = 6;
            wfTbl.CreateDate = KTime.ParseNow(KTime.KT_fmtDate3339k).toString();
            wfDao.Insert(wfTbl);
        }
        else {
            ReturnErrDuplicateSubmission();
        }
        // Adjust status
        DwrDao mDwrDao = db.dwrDao();
        DwrTbl mTbl = mDwrDao.GetDwr(dvm.getDwrId());
        mTbl.Status = DWR_STATUS_QUEUED;
        mDwrDao.Update(mTbl);
        // Kick off upload service
        Intent sIntent = new Intent(context, Refresh.class);
        context.startService(sIntent);
    }

    private void updateDatabase() {
        // Initialize Data
        DwrDao mDwrDao = db.dwrDao();
        DwrTbl mTbl = mDwrDao.GetDwr(dvm.getDwrId());
        if(mTbl == null) {      // Check if DWR exist in DB, if not create one locally.
            mTbl = new DwrTbl();
            long id = mDwrDao.Insert(mTbl);
            mTbl.DwrId = (int) id;
            mTbl.DwrSrvrId = -1;
            dvm.setDwrId((int) id);
        }
        DwrItem dwrView = dvm.dwrItem;
        Calendar workday = Calendar.getInstance();
        Calendar workTime;

        // Prep the database record.
        // Figure out the day classification index.
        List<String> localDay = Arrays.asList(context.getResources().getStringArray(R.array.classification_name));
        for (int i = 0; i < localDay.size(); ++i){
            if(localDay.get(i).equalsIgnoreCase(dwrView.getClassification()))
                mTbl.Classification = i;
        }
        classificationNdx = mTbl.Classification;    // Helps out later to know this value.
        // Important to get the right Railroad key.
        mTbl.Property = Railroads.PropertyKey(context, dwrView.getProperty());
        /*  The management of time is a little tricky because we collect the data and time separately.
         *  Here we will bring them back together, in the local time zone, and convert them to UTC
         *  as we save them to the database.
         *  First set the work day to match up with the starting work time.  Then use that result
         *  in all the other time calculations.
         */
        try {
            workday = KTime.ParseToCalendar(dwrView.getWorkDate(), KTime.KT_fmtDateShrtMiddle);
        } catch (ExpParseToCalendar expParseToCalendar) {
            /* Just go with the default date/time */
        }
        try {
            workTime = KTime.ParseToCalendar(dwrView.getWorkStartTime(), KTime.KT_fmtDate3339k);
            workday.set(Calendar.HOUR_OF_DAY, workTime.get(Calendar.HOUR_OF_DAY));
            workday.set(Calendar.MINUTE, workTime.get(Calendar.MINUTE));
            workday.set(Calendar.SECOND, 0);
            mTbl.WorkDate = DateFormat.format(KTime.KT_fmtDate3339k, KTime.ConvertTimezone(workday, KTime.UTC_TIMEZONE)).toString();
        } catch (ExpParseToCalendar expParseToCalendar) {
            mTbl.WorkDate = DateFormat.format(KTime.KT_fmtDate3339k, KTime.ConvertTimezone(workday, KTime.UTC_TIMEZONE)).toString();
        }

        mTbl.UserId = user.unique;
        mTbl.WorkId = user.workId;

        mTbl.JobId = dwrView.getJobNumberId();
        mTbl.JobNumber = dwrView.jobNumberDspl;
        mTbl.LocationCity = dwrView.getLocationCity();
        if(dwrView.getLocationState() != null) {
            mTbl.LocationState = dwrView.getLocationState().trim().length() > 2 ? dwrView.getLocationState().trim().substring(0, 2) : dwrView.getLocationState().trim();
        }
        mTbl.NonBilledReason = dwrView.nonBilledReason;
        mTbl.ContractorName = dwrView.getContractName();
        mTbl.CNDataNSOCC = dwrView.cnDataN_SO_CC;
        mTbl.CNDataNetwork = dwrView.cnDataNumber;
        mTbl.CNDataCounty = dwrView.cnDataCounty;
        mTbl.CSXDataOpNbr = dwrView.csxDataOpNbr;
        mTbl.CSXDataRegion = dwrView.csxDataRegionId;
        mTbl.KCSDataContractorCnt = parseInt(dwrView.kcsDataContractorCnt);
        mTbl.UPDataDotXing = dwrView.upDataDotXing;
        mTbl.UPDataFolderNbr = dwrView.upDataFolderNbr;
        mTbl.UPDataServiceUnit = dwrView.upDataServiceUnit;
        mTbl.KCTDataTaskOrder = dwrView.kctDataTaskOrder;
        mTbl.TypeOfVehicle = dwrView.typeOfVehicle;

        mTbl.RwicPhone = PhoneNumberSwitchToNumber(dwrView.rwicPhone);
        mTbl.CSXShiftNew = dwrView.csxShiftNew;
        mTbl.CSXShiftRelieved = dwrView.csxShiftRelieved;
        mTbl.CSXShiftRelief = dwrView.csxShiftRelief;
        mTbl.WorkLunchTime = dwrView.workLunchTime;
        mTbl.CSXPeopleRow = parseInt(dwrView.csxPeopleRow);
        mTbl.CSXEquipmentRow = parseInt(dwrView.csxEquipmentRow);
        mTbl.DescWeatherHigh = parseInt(dwrView.descWeatherHigh);
        mTbl.DescWeatherLow = parseInt(dwrView.descWeatherLow);
        mTbl.WorkBriefTime = dwrView.workBriefTime;
        mTbl.RoadMasterPhone = PhoneNumberSwitchToNumber(dwrView.roadMasterPhone);
        mTbl.DescWorkPlanned = dwrView.descWorkPlanned;
        mTbl.DescSafety = dwrView.descSafety;

        mTbl.RoadMaster = dwrView.roadMaster;
        mTbl.District = dwrView.district;
        mTbl.Subdivision = dwrView.subdivision;
        mTbl.MpStart = dwrView.mpStart;
        mTbl.MpEnd = dwrView.mpEnd;
        mTbl.WorkingTrack = dwrView.workingTrack;

        mTbl.Is707 = dwrView.p707;
        mTbl.Is1102 = dwrView.p1102;
        mTbl.Is1107 = dwrView.p1107;
        mTbl.IsEC1 = dwrView.pEC1;
        mTbl.IsFormB = dwrView.pFormB;
        mTbl.IsFormC = dwrView.pFormC;
        mTbl.IsFormW = dwrView.pFormW;
        mTbl.IsForm23 = dwrView.pForm23;
        mTbl.IsForm23Y = dwrView.pForm23Y;
        mTbl.IsDerails = dwrView.pDerails;
        mTbl.IsTrackTime = dwrView.pTrackTime;
        mTbl.IsTrackWarrant = dwrView.pTrackWarrant;
        mTbl.IsTrackAuthority = dwrView.pTrackAuthority;
        mTbl.IsObserver = dwrView.pObserver;
        mTbl.IsNoProtection = dwrView.pNoProtection;
        mTbl.IsLookout = dwrView.pLookout;
        mTbl.IsLiveFlagman = dwrView.pLiveFlagman;
        mTbl.IsVerbalPermission = dwrView.pVerbalPermission;
        mTbl.WorkOnTrack = dwrView.workOnTrackId;
        mTbl.InputWMLine = dwrView.InputWMLine == null ? "" : dwrView.InputWMLine;
        mTbl.InputWMStation = dwrView.InputWMStation == null ? "" : dwrView.InputWMStation;
        mTbl.InputWMStationName = dwrView.InputWMStationName == null ? "" : dwrView.InputWMStationName;
        mTbl.InputWMTrack = Integer.parseInt(dwrView.InputWMTrack == null ? "0" : dwrView.InputWMTrack.equalsIgnoreCase("X")?"-1":dwrView.InputWMTrack);
        mTbl.ConstructionDay = dwrView.constructionDay;
        mTbl.InputTotalWorkDays = dwrView.inputTotalWorkDays;
        mTbl.InputDescWeatherWind = dwrView.inputDescWeatherWind;
        mTbl.InputDescWeatherRain = dwrView.inputDescWeatherRain;
        //Photos
        saveImagestoDocuments();

        // ClientPhone is used for wmataCallNumber, so make sure this happens before wmataCallNumber is assgined.
        mTbl.ClientPhone = PhoneNumberSwitchToNumber(dwrView.clientPhone);
        // The Description for WMATA is special sometimes, but make sure the default is assigned before that happens.
        mTbl.Description = dwrView.description;
        // The WMATA description is split into 15 lines for Billable days.
        if(Railroads.GetTemplateKey(dwrView.getProperty()) == Railroads.DWR_TEMPLATE_WMATA) {
            if (mTbl.Classification == 0 || mTbl.Classification == 6) {
                mTbl.Description = "#_01 " + dwrView.getComment01() +
                        "#_02 " + dwrView.getComment02() +
                        "#_03 " + dwrView.getComment03() +
                        "#_04 " + dwrView.getComment04() +
                        "#_05 " + dwrView.getComment05() +
                        "#_06 " + dwrView.getComment06() +
                        "#_07 " + dwrView.getComment07() +
                        "#_08 " + dwrView.getComment08() +
                        "#_09 " + dwrView.getComment09() +
                        "#_10 " + dwrView.getComment10() +
                        "#_11 " + dwrView.getComment11() +
                        "#_12 " + dwrView.getComment12() +
                        "#_13 " + dwrView.getComment13() +
                        "#_14 " + dwrView.getComment14() +
                        "#_15 " + dwrView.getComment15();
                mTbl.ClientPhone = dwrView.wmataCallNumber;
            } else {
                // Backend reporting really wants the #_X.  We just fake it for not-billable-day forms.
                mTbl.Description = mTbl.Description.replace(WAMATA_DESC_FILLER, "");
                mTbl.Description = "#_01 " + mTbl.Description;
                mTbl.Description += WAMATA_DESC_FILLER;
            }
        }
        mTbl.DescWeatherConditions = dwrView.descWeatherConditions;
        mTbl.DescTypeOfWork = dwrView.descTypeOfWork;
        mTbl.DescInsideRoW = dwrView.descInsideRow;
        mTbl.DescOutsideRoW = dwrView.descOutsideRow;
        mTbl.DescUnusual = dwrView.descUnusual;
        mTbl.DescLocationStart = dwrView.descLocationStart;
        mTbl.NotPresentOnTrack = dwrView.notPresentOnTrack;
        mTbl.PerformedTraining = dwrView.performedTraining;
        mTbl.WorkStartTime = GetTimeFor(workday, dwrView.workStartTime, KTime.KT_fmtDate3339k);
        mTbl.WorkEndTime = AdjustForward(mTbl.WorkStartTime, GetTimeFor(workday, dwrView.workEndTime, KTime.KT_fmtDate3339k), KTime.KT_fmtDate3339k);
        mTbl.WorkHoursRounded = Functions.GetFloatFromString(dwrView.workHoursRounded);
        mTbl.SpecialCostCenter = dwrView.getSpecialCostCenterReal();
        mTbl.RwicSignatureName = dwrView.getFlagmanSignaturePhotoName();
        mTbl.RwicSignatureDate = dwrView.getFlagmanSignaturePhotoDate();
        mTbl.ClientSignatureName = dwrView.getClientSignaturePhotoName();
        mTbl.ClientSignatureDate = dwrView.getClientSignaturePhotoDate();
        mTbl.ClientName = dwrView.clientName;
        mTbl.ClientEmail = dwrView.clientEmail;
        mTbl.RailSignatureName = dwrView.getRailSignaturePhotoName();
        mTbl.RailSignatureDate = dwrView.getRailSignaturePhotoDate();
        mTbl.RailroadContact = dwrView.railroadContact;

        mTbl.TravelToJobStartTime = GetTimeFor(workday, dwrView.travelToJobStartTime, KTime.KT_fmtDate3339k);
        mTbl.TravelToJobEndTime = AdjustForward(mTbl.TravelToJobStartTime, GetTimeFor(workday, dwrView.travelToJobEndTime, KTime.KT_fmtDate3339k), KTime.KT_fmtDate3339k);
        mTbl.TravelToHoursRounded = Functions.GetFloatFromString(dwrView.travelToJobHours);
        mTbl.TravelFromJobStartTime = GetTimeFor(workday, dwrView.travelFromJobStartTime, KTime.KT_fmtDate3339k);
        mTbl.TravelFromJobEndTime = AdjustForward(mTbl.TravelFromJobStartTime, GetTimeFor(workday, dwrView.travelFromJobEndTime, KTime.KT_fmtDate3339k), KTime.KT_fmtDate3339k);
        mTbl.TravelFromHoursRounded = Functions.GetFloatFromString(dwrView.travelFromJobHours);
        mTbl.TravelToJobMiles = parseInt(dwrView.milesToJob);
        mTbl.TravelFromJobMiles = parseInt(dwrView.milesFromJob);
        mTbl.TravelOnJobMiles = parseInt(dwrView.jobMileage);
        mTbl.PerDiem = dwrView.perdiemId;

        mTbl.HasRoadwayFlagging = dwrView.hasRoadwayFlagging;
        mTbl.EightyTwoT = dwrView.eightyTwoT;
        mTbl.StreetName = dwrView.streetName;
        mTbl.MilePostsForStreet = dwrView.milePostsForStreet;

        mTbl.IsOngoing = dwrView.onGoing;
        mTbl.VersionInformation = MetricsForAustin(mTbl.VersionInformation, dwrView.timeLogged);
        mTbl.Status = DWR_STATUS_DRAFT;

        mDwrDao.Update(mTbl);
    }

    // Unpack the stored data, add new data, repack.
    private String MetricsForAustin(String oldData, long moreTime){
        String [] parts = oldData.split("_", 2);
        String ver = context.getResources().getString(R.string.unknown);
        Metrics metrics;
        try { ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName; }
        catch (PackageManager.NameNotFoundException e) { /* skip it */ }
        if(parts.length < 2 || parts[1].length() == 0) { // New Data
            metrics = new Metrics(1, moreTime);
        } else {
            metrics = new Gson().fromJson(parts[1], Metrics.class);
            metrics.AddSave();
            metrics.AddScreenTime(moreTime);
        }
        return ver + "_" + new Gson().toJson(metrics);
    }

    private int parseInt(String value) {
        if(value == null) return 0;
        try { return Integer.parseInt(value); }
        catch (Exception e) { return 0; }
    }

    private void saveImagestoDocuments() {
        db.documentDao().DeleteDwrImages(dvm.getDwrId());
        db.documentDao().DeleteDwrSigninSheet(dvm.getDwrId());
        //Failure Don't Save Images if It's DWRID is bad
        if(dvm.getDwrId() == 0) {
            return;
        }

        if(dvm.dwrItem != null && dvm.dwrItem.pictureSignInUri != null) {
            PictureField signIn = new PictureField(dvm.dwrItem.pictureSignInUri, context.getResources().getString(R.string.lbl_signup_sheet), 90);
            saveImageToDocument(signIn, 4);
        }

        for(int i = 0; i < dvm.PictureList.size(); i++) {
            PictureField temp = dvm.PictureList.get(i);
            saveImageToDocument(temp, 1);
        }
    }

    private void saveImageToDocument(PictureField temp, int type) {
        DocumentTbl tbl = new DocumentTbl();
        tbl.DocumentId = 0;
        tbl.DocumentType = type;
        tbl.JobId = dvm.dwrItem.getJobNumberId();
        tbl.FileName = Functions.UriFileName(context, temp.pictureURI);
        tbl.UriLocal = temp.pictureURI.getPath();
        tbl.Mimetype = "image/jpeg";
        tbl.description = temp.description;
        tbl.DwrId = dvm.getDwrId();
        tbl.LastUpdate = KTime.ParseNow(KTime.KT_fmtDate3339fk).toString();
        db.documentDao().Insert(tbl);
    }

    // The various times collected are concatenated onto the current workday date
    // and converted to UTC for storage.
    private String GetTimeFor(Calendar workday, String workTime, String format){
        try {
            if (workTime == null) { return ""; }
            if (workTime.length() == 0) { return ""; }
            Calendar holdtime = KTime.ParseToCalendar(workTime, format);
            workday.set(Calendar.HOUR_OF_DAY, holdtime.get(Calendar.HOUR_OF_DAY));
            workday.set(Calendar.MINUTE, holdtime.get(Calendar.MINUTE));
            workday.set(Calendar.SECOND, 0);
            return DateFormat.format(format, KTime.ConvertTimezone(workday, KTime.UTC_TIMEZONE)).toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // If the end is earlier than the start, add 24 hours and return the end.
    private String AdjustForward(String start, String end, String format) {
        try {
            if(start == null || end == null) { return ""; }
            if(start.length() == 0 || end.length() == 0) { return ""; }
            Calendar timeEarly = KTime.ParseToCalendar(start, format, KTime.UTC_TIMEZONE);
            Calendar timeLate = KTime.ParseToCalendar(end, format, KTime.UTC_TIMEZONE);
            if((timeEarly.getTimeInMillis() - timeLate.getTimeInMillis()) > 0){
                timeLate.add(Calendar.HOUR, 24);
            }
            return DateFormat.format(format, timeLate).toString();
        } catch (Exception ex) {
            return end;
        }
    }

    private void ReturnOk() {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_DWR_SAVE;
            if(callback != null) {
                callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

    private void ReturnErr() {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_DWR_SAVE_ERR;
            if(callback != null) {
                callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

    private void ReturnErrDuplicateSubmission() {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_DWR_DUPLICATE_SUBMISSION;
            if(callback != null) {
                callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

    private String PhoneNumberSwitchToNumber(String number) {
        PhoneNumberSupport mSupport = new PhoneNumberSupport();
        try {
            return mSupport.switchToNumber(number);
        }
        catch (Exception e) {
            return "";
        }
    }

    // If there is no assignment yet, create a placeholder.
    private void linkAssignment(int jobid, int serviceType) {
        try {
            AssignmentTbl tbl = db.assignmentDao().GetByJobId(jobid);
            if (tbl == null) {
                // Get the basic information needed to build an Assignment.
                JobDetailResponse jobResponse;
                JobCostCenter[] costResponse = new JobCostCenter[0];
                try {
                    IWebServices ApiService = new WebServices(API_TIMEOUT_SHORT, new Gson());
                    String jobPath = Connection.getInstance().getFullApiPath(Connection.API_GET_JOB_BY_ID).replace(SUB_ZZZ, String.valueOf(jobid));
                    jobResponse = ApiService.CallGetApi(jobPath, JobDetailResponse.class, user.ticket);
                    // Job cost center information
                    String costPath = Connection.getInstance().getFullApiPath(Connection.API_GET_JOB_COSTCENTERS).replace(SUB_ZZZ, String.valueOf(jobid));
                    costResponse = ApiService.CallGetApi(costPath, JobCostCenter[].class, user.ticket);
                } catch (Exception ex) {
                    // If cannot get data online, see if we have any locally.
                    JobTbl smallJob = db.jobDao().GetJob(jobid);
                    jobResponse = new JobDetailResponse();
                    jobResponse.id = jobid;
                    if(smallJob != null) {
                        jobResponse.start = smallJob.StartTime;
                        jobResponse.end = smallJob.EndTime;
                        jobResponse.description = smallJob.Description;
                        jobResponse.customer = new CustomerShortWS();
                        jobResponse.customer.name = smallJob.CustomerName;
                        jobResponse.number = smallJob.JobNumber;
                    }
                    ExpClass.LogEX(ex, "linktoAssignment try local job lookup.");
                }
                AssignmentWS workItem = new AssignmentWS();
                AssignmentShiftWS shift = new AssignmentShiftWS();
                shift.id = 0;   // Zero (0) indicates this is not a server created assignment.
                shift.serviceType = new BaseIdWS();
                shift.serviceType.id = serviceType;

                // Make the dates match the workday for this DWR.
                String workDate = KTime.ParseToFormat(dvm.dwrItem.getWorkDate(), KTime.KT_fmtDateShrtMiddle, KTime.UTC_TIMEZONE, KTime.KT_fmtDateOnlyRPFS, KTime.UTC_TIMEZONE).toString();
                workDate = workDate.substring(0,11) + MIDNIGHT + "Z"; // Use the backend format.
                shift.day = workDate;
                workItem.start = workDate;
                workItem.end = workDate;

                AssignmentTbl action = Functions.BuildAssignment(user.unique, Railroads.PropertyKey(context, dvm.dwrItem.property), jobResponse, Arrays.asList(costResponse), workItem, shift);
                ScheduleDB.getDatabase(context).assignmentDao().Insert(action);
            }
        } catch (Exception ex) { ExpClass.LogEX(ex, "linkToAssignment"); }
    }
}