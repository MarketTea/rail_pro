package com.railprosfs.railsapp.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Pair;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.railprosfs.railsapp.DwrEdit;
import com.railprosfs.railsapp.JobSetup;
import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.Settings;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data.DwrTemplateMgr;
import com.railprosfs.railsapp.data.JobSetupDwrUpdateMgr;
import com.railprosfs.railsapp.data.JobSetupTemplateMgr;
import com.railprosfs.railsapp.data.JobTemplateMgr;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.data.ScheduleDB;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.FieldPlacementDao;
import com.railprosfs.railsapp.data_layout.FieldPlacementTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data_layout.RailRoadTbl;
import com.railprosfs.railsapp.data_layout.WorkflowTbl;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.Constants;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.railprosfs.railsapp.service.WebServiceModels.*;
import static com.railprosfs.railsapp.utility.Constants.*;
/**
 * This service is used to update various local data with any changed server data.
 * This primarily means checking on the DWRs, occasionally doing a token
 * refresh and updating job specific data.  It also performs document downloads.
 * <p>
 * NOTE: Android works to keep itself operating smoothly by aggressively cleaning
 * up tasks that do not seem to be getting used.  This means Services/Threads
 * cannot expect to be long running, even if they do not use up a lot of resources.
 * Services/Threads should do something and then exit.  If that thing needs to be
 * done periodically, use the approved method (AlarmManager, JobScheduler, etc.) to
 * schedule calls to it.
 */
public class Refresh extends IntentService {
    private static final String SRV_NAME = "RefreshService";  // Name can be used for debugging.
    private static final String JOBSETUP_DESC = "Job Setup";
    private static final String UTILITY_DESC = "Cover Sheet";
    private static final String DWRFORM_DESC = "Daily Work Report";
    private static final String ROADWAY_FLAGGING_DWRFORM_CHECK = "roadway flagging";
    public static final int REQUESTCODE_JOBSETUP = 552;
    private ResultReceiver receiver;
    IWebServices ApiService;
    ScheduleDB db;
    Connection connection;
    Actor mActor;

    public Refresh() {
        super(SRV_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            initVariables();
            clearData(intent);
            pushData(mActor);
            pullData(intent,mActor);


        } catch (Exception ex) {
            ExpClass.LogEX(ex, "Top Level Refresh IntentService failure.");
        }
    }

    private void pullData(Intent intent, Actor mActor) {


        if (intent != null) {
            receiver = intent.getParcelableExtra(REFRESH_RECIEVER);

            //Run Sync Assignment only when Refresh Button is Pressed
            if (intent.getBooleanExtra(REFRESH_MAIN, false) || intent.getBooleanExtra(REFRESH_LOOP, false)) {
                SyncAssignment(mActor);
                SyncDocument(mActor);
            }

            //Enable to Refresh Assignment Tbl/Doc Tbl/DWR Questions
            if (intent.getBooleanExtra(REFRESH_MAIN, false)) {
                SyncTemplate(mActor);
                SyncJobForms(mActor);
                SyncDWRQuestion(mActor);
                SyncRailRoad(mActor);
            }

            if (intent.getBooleanExtra(REFRESH_LOOP, true)) {
                SyncDWRStatus(mActor);
                SyncJobStatus(mActor);
                SyncUtilityJobStatus(mActor);
                SyncCoverStatus(mActor);
                SyncJobList(mActor);
            }

            //Finish Progress bar
            if (receiver != null) {
                // data that will be sent into ResultReceiver
                Bundle data = new Bundle();
                data.putInt(REFRESH_PROGRESS, 100);
                // here you are sending progress into ResultReceiver located in your Activity
                receiver.send(REFRESH_CONST, data);
            }
        }
    }

    private void clearData(Intent intent) {
          /*  If this is a new user, clear out data from any previous users. While
            the database is designed to work without this cleanup, it does make
            things less complex to not have a bunch of stale data laying around.
        */
        if (intent != null) {
            if (intent.getData() != null) {
                if (intent.getData().getPath() != null) {
                    if (intent.getData().getPath().contains(REQUEST_NEWACCT)) {
                        db.assignmentDao().ClearNonUsers(mActor.unique);
                        db.dwrDao().ClearNonUsers(mActor.unique);
                        db.jobSetupDao().ClearNonUsers(mActor.unique);
                    }
                }
            }
        }
    }

    private void initVariables() {
        mActor = new Actor(this);
        ApiService = new WebServices(new Gson());
        db = ScheduleDB.getDatabase(this);
        connection = Connection.getInstance();
        // Check if the user is signed in yet.
        if (mActor.ticket.length() == 0) return;
        FirebaseCrashlytics.getInstance().setCustomKey("unique", mActor.unique);
        // Check for user updates once a day (at most).
        if (Math.abs(KTime.GetEpochNow() - mActor.refresh) > 24 * 60 * 60) {
            try {
                mActor.LoadPrime(this, ApiService);
                mActor.SyncPrime(this, false);
            } catch (Exception e) {
                ExpClass.LogEX(e, "API Error cannot update Login Credentials");
            }
        }

    }

    /*
     *  Used to create a hash map of the server supplied questions to allow easy look up by code.
     *  key must be either: SP_REG_DWR_FORMID or SP_REG_DWR_FORMID_ROADWAY_FLAGGING
     */
    private Map<String, Integer> GenerateDwrQmap(String key) {
        DwrTemplateMgr hold = new DwrTemplateMgr(this, null);

        //Revert to default template if key is not in the expected list
        if (!key.equalsIgnoreCase(SP_REG_DWR_FORMID) && !key.equalsIgnoreCase(SP_REG_DWR_FORMID_ROADWAY_FLAGGING)) {
            key = SP_REG_DWR_FORMID;
        }

        List<FieldPlacementTbl> questions = db.fieldPlacementDao().GetTemplateFields(hold.GetDwrTemplateId(key));
        Map<String, Integer> qmap = new HashMap<>(questions.size());
        int headerCnt = 0;
        for (FieldPlacementTbl rec : questions) {
            if (rec.Code != null) {
                qmap.put(rec.Code, rec.FieldId);
            }
            // Headers have no usage code, so we generate a unique one.
            if (rec.FieldType.contentEquals(QUESTION_TYPE_FormHeaderStr)) {
                headerCnt++;
                qmap.put(QUESTION_TYPE_FormHeaderStr + headerCnt, rec.FieldId);
            }
        }
        return qmap;
    }

    /******************************************************************************
     *  The following set of methods is used to sync assigned jobs with the server.
     */

    /*
     *  Sync the Assignments.  This will grab data off the server and then clean
     *  up the local database with any Deletes, Changes, Adds.  In that order.
     */
    private void SyncAssignment(Actor mActor) {
        try {
            if (ApiService.IsNetwork(this)) {
                if (receiver != null) {
                    // data that will be sent into ResultReceiver
                    Bundle data = new Bundle();
                    data.putString(REFRESH_PROGRESS_DESCRIPTION, "Syncing Assignments");
                    data.putInt(REFRESH_PROGRESS, 1);
                    // here you are sending progress into ResultReceiver located in your Activity
                    receiver.send(REFRESH_CONST, data);
                }

                List<AssignmentTbl> response = GetAssignments(mActor.workId, mActor.unique, mActor.ticket);
                if (response.size() > 0) {
                    List<AssignmentTbl> scheduleStored = db.assignmentDao().GetUserAssignments(mActor.unique);
                    AssignmentDeletes(response, scheduleStored);
                    AssignmentUpdates(response, scheduleStored);
                    AssignmentAdditions(response, scheduleStored);
                    RefreshZeroShifts(scheduleStored, mActor.unique, mActor.ticket);
                }
                updateLastSync(SP_LS_ASSIGNMENT);
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    /*
     *  For each Job, there are a set of Docs associated with it.  The Docs are used
     *  only in the job detail display.  For ease of access the Docs are stored in
     *  their own table, using their own key, but with the added key of a job id.
     *  We'll use the same Deletes, Changes and Adds strategy for synchronization.
     *  Note: File meta data may also point to actual file bytes, so managing that
     *  data must also be considered, e.g. file deletes or change to last update.
     */
    private void SyncDocument(Actor mActor) {
        try {
            if (ApiService.IsNetwork(this)) {

                if (receiver != null) {
                    // data that will be sent into ResultReceiver
                    Bundle data = new Bundle();
                    data.putString(REFRESH_PROGRESS_DESCRIPTION, "Syncing Document");
                    data.putInt(REFRESH_PROGRESS, 2);
                    // here you are sending progress into ResultReceiver located in your Activity
                    receiver.send(REFRESH_CONST, data);
                }

                List<DocumentTbl> allServerDocs = new ArrayList<>();
                List<Integer> jobs = GetUniqueJobs(db.assignmentDao().GetUserAssignments(mActor.unique));
                for (Integer jobNbr : jobs) {
                    JobDetailResponse jobResponse;
                    try {
                        jobResponse = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_JOB_BY_ID).replace(SUB_ZZZ, String.valueOf(jobNbr)), JobDetailResponse.class, mActor.ticket);
                    } catch (ExpClass kx) { jobResponse = null; ExpClass.LogEXP(kx, "Job Number:" + jobNbr); }
                    if (jobResponse != null) {
                        if (jobResponse.documents != null && jobResponse.documents.size() > 0) {
                            for (DocumentWS docWS : jobResponse.documents) {
                                DocumentTbl doc = new DocumentTbl();
                                doc.DocumentType = DOC_OWNER_JOB;
                                doc.FileName = String.format(JOBDOC_FILE_PREFIX, docWS.id, docWS.fileName.toLowerCase());
                                doc.description = docWS.title.length() > 0 ? docWS.title : docWS.fileName;
                                doc.ServerId = docWS.id;
                                doc.JobId = jobNbr;
                                doc.LastUpdate = docWS.timeStamp;
                                doc.Mimetype = ""; // Windows Server Devs unfamiliar with this concept, fall back to file extension.
                                allServerDocs.add(doc);
                            }
                        }
                    }
                }
                List<DocumentTbl> docsStored = db.documentDao().GetDocsByType(DOC_OWNER_JOB);
                CheckDocDeletes(allServerDocs, docsStored);
                CheckDocUpdates(allServerDocs, docsStored, mActor.ticket);
                CheckDocAdditions(allServerDocs, docsStored, mActor.ticket);
                updateLastSync(SP_LS_DOCUMENT);
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    /*
     *  Get a de-duped list of the jobs based on AssignmentTbls.
     */
    private List<Integer> GetUniqueJobs(List<AssignmentTbl> assignmentTbls) {
        List<Integer> jobNbrs = new ArrayList<>();
        localLoop:
        for (AssignmentTbl item : assignmentTbls) {
            for (Integer key : jobNbrs) {
                if (item.JobId == key)
                    continue localLoop;
            }
            jobNbrs.add(item.JobId);
        }
        return jobNbrs;
    }

    /*
     *  This gets useful data about the Templates.  Specifically, find the
     *  server id needed to load the specific template and take note of the
     *  template name as a versioning strategy.
     *  NOTE: Optimally, we would read all the railroads and find the job setup
     *  forms associated with them.  At this time the API is not quite there and
     *  so we will rely on the name of the job template form to figure out the
     *  railroad it works with.  For example, if the description of the item type
     *  is returned as JOBSETUP_DESC, then we check if the name of the railroad (BNSF)
     *  is contained in the item name.  We also rely on the last item name provided
     *  being the template we want.  This works because the templates are sorted by name
     *  and we name them with an increasing version number.
     * There are now two forms of type.description we care about:
     * JOBSETUP_DESC and UTILITY_DESC
     */
    private void SyncTemplate(Actor mActor) {
        try {

            if (ApiService.IsNetwork(this)) {

                if (receiver != null) {
                    // data that will be sent into ResultReceiver
                    Bundle data = new Bundle();
                    data.putString(REFRESH_PROGRESS_DESCRIPTION, "Syncing Templates");
                    data.putInt(REFRESH_PROGRESS, 3);
                    // here you are sending progress into ResultReceiver located in your Activity
                    receiver.send(REFRESH_CONST, data);
                }

                JobTemplateMgr jtm = new JobTemplateMgr(this);
                FormsSummaryRequest request = new FormsSummaryRequest();
                FormsSummaryResponse formsResponse = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_FORM_LIST), request, FormsSummaryResponse.class, mActor.ticket);
                for (FormsSummaryWS item : formsResponse.results) {
                    if (item.type.description.equalsIgnoreCase(JOBSETUP_DESC)) {
                        jtm.RegisterJobSetupForm(item.name, item.id);
                    }
                    if (item.type.description.equalsIgnoreCase(UTILITY_DESC)) {
                        jtm.RegisterCoverSheetForm(item.name, item.id);
                    }
                }

                updateLastSync(SP_LS_TEMPLATE);
            }
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "Syncing Templates");
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    /*
     * Syncs JobSetup/UtilitySetup Forms and PreLoads them into the Database
     */
    private void SyncJobForms(Actor mActor) {
        try {
            if (ApiService.IsNetwork(this)) {

                if (receiver != null) {
                    // data that will be sent into ResultReceiver
                    Bundle data = new Bundle();
                    data.putString(REFRESH_PROGRESS_DESCRIPTION, "Syncing Job Forms");
                    data.putInt(REFRESH_PROGRESS, 4);
                    // here you are sending progress into ResultReceiver located in your Activity
                    receiver.send(REFRESH_CONST, data);
                }

                JobTemplateMgr jobTemplateMgr = new JobTemplateMgr(this);
                List<String> template = Railroads.GetPropertiesRaw(getApplicationContext());
                for (int i = 0; i < template.size(); i++) {
                    String JStemplateId = String.valueOf(jobTemplateMgr.GetJobSetupId(template.get(i)));
                    if (!JStemplateId.equals("0")) {
                        WebServiceModels.FormResponse formResponse = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_QUESTIONS).replace(SUB_ZZZ, JStemplateId), WebServiceModels.FormResponse.class, mActor.ticket);
                        JobUpdate(formResponse, Integer.parseInt(JStemplateId));
                    }
                    String UStemplateId = String.valueOf(jobTemplateMgr.GetUtilitySetupId(template.get(i)));
                    if (!UStemplateId.equals("0")) {
                        WebServiceModels.FormResponse formResponse = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_QUESTIONS).replace(SUB_ZZZ, UStemplateId), WebServiceModels.FormResponse.class, mActor.ticket);
                        JobUpdate(formResponse, Integer.parseInt(UStemplateId));
                    }
                    String CStemplateId = String.valueOf(jobTemplateMgr.GetCoverSheetId(template.get(i)));
                    if (!CStemplateId.equals("0")) {
                        WebServiceModels.FormResponse formResponse = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_QUESTIONS).replace(SUB_ZZZ, CStemplateId), WebServiceModels.FormResponse.class, mActor.ticket);
                        JobUpdate(formResponse, Integer.parseInt(CStemplateId));
                    }
                }
                updateLastSync(SP_LS_JOBFORM);
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    /*
     *  The DWR questions are mostly managed locally on the App, but knowing how to sync
     *  the answers collected to their corresponding server fields requires that linking
     *  everything up with the usageCode from the form questions.
     *  Step 1: Find the Dwr Form Template Id.
     *  Step 2: Update the questions by clearing out the table and re-adding them.
     *  It is important to find the right set of questions, as past templates are
     *  left in the system.  For now we just pray the db id of the newest template
     *  is always a larger number than the previous templates before it.
     */
    private void SyncDWRQuestion(Actor mActor) {
        try {
            if (ApiService.IsNetwork(this)) {
                if (receiver != null) {
                    // data that will be sent into ResultReceiver
                    Bundle data = new Bundle();
                    data.putString(REFRESH_PROGRESS_DESCRIPTION, "Syncing DWR Fields");
                    data.putInt(REFRESH_PROGRESS, 5);
                    // here you are sending progress into ResultReceiver located in your Activity
                    receiver.send(REFRESH_CONST, data);
                }

                // Find the Dwr Template (Be great to hardcode the db id, except it might vary across environments, so use string and cross fingers it does not change.)
                FormsSummaryRequest request = new FormsSummaryRequest();
                FormsSummaryResponse formsResponse = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_FORM_LIST), request, FormsSummaryResponse.class, mActor.ticket);
                int holdDWRtemplateId = 0;
                int holdDWRTemplateIdForRoadwayFlagging = 0;
                for (FormsSummaryWS item : formsResponse.results) {
                    if (item.type.description.equalsIgnoreCase(DWRFORM_DESC) && item.id > holdDWRtemplateId) {
                        if (!item.title.toLowerCase().contains(ROADWAY_FLAGGING_DWRFORM_CHECK)) {
                            holdDWRtemplateId = item.id;
                        }
                    }

                    if (item.type.description.equalsIgnoreCase(DWRFORM_DESC) && item.id > holdDWRTemplateIdForRoadwayFlagging) {
                        if (item.title.toLowerCase().contains(ROADWAY_FLAGGING_DWRFORM_CHECK)) {
                            holdDWRTemplateIdForRoadwayFlagging = item.id;
                        }
                    }
                }

                DwrTemplateMgr dtm = new DwrTemplateMgr(this, null);
                if (holdDWRtemplateId > 0) {
                    dtm.RegisterDwrForm(Constants.SP_REG_DWR_FORMID, holdDWRtemplateId);
                }

                if (holdDWRTemplateIdForRoadwayFlagging > 0) {
                    dtm.RegisterDwrForm(Constants.SP_REG_DWR_FORMID_ROADWAY_FLAGGING, holdDWRTemplateIdForRoadwayFlagging);
                }

                if (holdDWRtemplateId > 0) {
                    // At first, we only refreshed the questions if they changed, but the risk / reward was too great, so now we always refresh the questions.
                    FormResponse dwrQuestions = ApiService.CallGetApi(
                            connection.getFullApiPath(Connection.API_GET_QUESTIONS).replace(SUB_ZZZ, dtm.GetDwrTemplateIdStr(Constants.SP_REG_DWR_FORMID)),
                            FormResponse.class, mActor.ticket);

                    if (dwrQuestions != null && dwrQuestions.fields != null && dwrQuestions.fields.size() > 0) {
                        db.fieldPlacementDao().DeleteAllId(dtm.GetDwrTemplateId(Constants.SP_REG_DWR_FORMID));
                        List<FieldPlacementTbl> questions = dtm.MapDwrQuestions(dwrQuestions.fields);
                        for (FieldPlacementTbl rec : questions) {
                            //Here is where the question is injected to db
                            db.fieldPlacementDao().Insert(rec);
                        }
                    }
                }

                if (holdDWRTemplateIdForRoadwayFlagging > 0) {
                    //Temporarily do the same for roadway flagging.
                    FormResponse dwrRoadwayFlaggingQuestions = ApiService.CallGetApi(
                            connection.getFullApiPath(Connection.API_GET_QUESTIONS).replace(SUB_ZZZ, dtm.GetDwrTemplateIdStr(Constants.SP_REG_DWR_FORMID_ROADWAY_FLAGGING)),
                            FormResponse.class, mActor.ticket);

                    if (dwrRoadwayFlaggingQuestions != null && dwrRoadwayFlaggingQuestions.fields != null && dwrRoadwayFlaggingQuestions.fields.size() > 0) {
                        db.fieldPlacementDao().DeleteAllId(dtm.GetDwrTemplateId(Constants.SP_REG_DWR_FORMID_ROADWAY_FLAGGING));
                        List<FieldPlacementTbl> questions = dtm.MapDwrQuestions(dwrRoadwayFlaggingQuestions.fields);
                        for (FieldPlacementTbl rec : questions) {
                            //Here is where the question is injected to db
                            db.fieldPlacementDao().Insert(rec);
                        }
                    }
                }

                updateLastSync(SP_LS_DWRQUESTION);
            }
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "Syncing DWR Fields");
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    /*
        Sync RailRoads
     */
    private void SyncRailRoad(Actor mActor) {
        try {
            // Run Loading Task Async Call
            if (ApiService.IsNetwork(this)) {
                if (receiver != null) {
                    Bundle data = new Bundle();
                    data.putString(REFRESH_RAILROAD, "Syncing Railroads");
                    data.putInt(REFRESH_PROGRESS, 6);
                    receiver.send(REFRESH_CONST, data);
                }

                RailRoadRequest request = new RailRoadRequest();
                RailRoadResponse railRoadResponse = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_RAILRROAD), request, RailRoadResponse.class, mActor.ticket);

                List<RailRoadTbl> tbl = db.railRoadDao().GetAll();

                for (RailRoadItem railroad : railRoadResponse.results) {            // Railroad
                    boolean valid = true;

                    for (int i = 0; i < tbl.size(); i++) {
                        if (CheckContainRailRoad(tbl.get(i), railroad)) {
                            valid = false;
                            break;
                        }
                    }

                    if (valid) {
                        if (railroad.divisions == null || railroad.divisions.length == 0) {
                            InsertRailRoadTbl(railroad, null, null);
                        } else {
                            for (WebServiceModels.Division division : railroad.divisions) {                  // Division
                                if (division.subdivisions == null || division.subdivisions.length == 0) {
                                    InsertRailRoadTbl(railroad, division, null);
                                } else {
                                    for (WebServiceModels.Division subdivision : division.subdivisions) {        //Subdivision
                                        InsertRailRoadTbl(railroad, division, subdivision);
                                    }
                                }
                            }
                        }
                    }
                }
                updateLastSync(SP_LS_RAILROAD);
            }
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "Syncing Railroads");
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    /*
     * **EVENT QUEUE PROCESSING**
     * The event queue is how the data uploads are able to support an off-line network state.
     * Upon submission of data, it is saved in its regular table on the device, then a record
     * is added to the WorkflowTbl indicating it should be uploaded.  The WorkflowTbl is a
     * persistent queue that includes various fields that help in queue processing.  To use
     * the queue, follow these steps:
     * 1)  Get the oldest record that is also in the past. This is the Pop(now) command.
     * 2a) Hide the record from step #1, so it is not accidentally processed twice . This is
     *      the Push(future) command.
     * 2b) If the record Retry value is greater than the RetryMax value, there is a problem.
     *      The record should be considered "poison" and deleted. If this happens you should
     *      update the status of the target record.
     * 3) With a good record, use the EventType to know what type of data is getting uploaded
     *      and which table to get that information from.
     * 4) Use the EventKey to look up the specific record in a table that needs to be processed.
     * 5) Add/Save the data to the backend using the API.  Optionally update the record status.
     * 6) Upon success, remove the record from the queue with a delete.
     *
     * Note that if something goes wrong, just relax, as the record will reappear in a few minutes
     * and again be ready for processing.
     */
    private void pushData(Actor mActor) {
        String FAIL_UPDATE_KEYS = "Failed to Update Keys";
        String FAIL_UPLOAD_DATA = "Failed to Upload Record";
        String FAIL_POISON_RECD = "Failed too many times (poison)";
        String FAIL_INVALID_TYPE= "Failed with unrecognized Event Type";
        String FAIL_GENERAL_EXIT= "Failed at some point in the method";
        int eventType = -1;
        try {
            if (ApiService.IsNetwork(this)) {
                WorkflowTbl work = db.workflowDao().Pop(KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString());
                while (work != null) {
                    eventType = work.EventType;
                    Calendar future = KTime.ConvertTimezone(Calendar.getInstance(), KTime.UTC_TIMEZONE);
                    future.add(Calendar.SECOND, getRetrySeconds(work.Retry));
                    db.workflowDao().Push(work.EventID, DateFormat.format(KTime.KT_fmtDate3339k, future).toString());

                    boolean okToDelete = false;
                    boolean isPoison = work.Retry > work.RetryMax;
                    switch (eventType) {
                        //Temporarily keep all same code for Q_TYPE_DWR_ROADWAY_FLAGGING_SAVE, optimise with Q_TYPE_DWR_SAVE later when have enough information
                        case Q_TYPE_DWR_ROADWAY_FLAGGING_SAVE:
                            DwrTbl dwrRoadway = db.dwrDao().GetDwr(work.EventKey);
                            if (dwrRoadway != null) {
                                if (!isPoison) {
                                    String prefKey = SP_REG_DWR_FORMID_ROADWAY_FLAGGING;
                                    DwrTemplateMgr dtm = new DwrTemplateMgr(this, GenerateDwrQmap(prefKey));
                                    DwrRequest request = dtm.MapDwrToRequest(dwrRoadway);
                                    try {
                                        dwrRoadway.DwrSrvrId = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_SAVE_DWR), request, Integer.class, mActor.ticket);
                                        dwrRoadway.Status = DWR_STATUS_PENDING;
                                        okToDelete = true;
                                        updateLastSync(SP_LS_EVENTQUE_DWR);
                                        LogUploadToAudit(Q_TYPE_DWR_ROADWAY_FLAGGING_SAVE, dwrRoadway.DwrSrvrId, request.id, dwrRoadway.VersionInformation, request.submittedOn, dwrRoadway.Classification, dwrRoadway.Property);
                                        try {
                                            DwrResponse response = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_DWR).replace(SUB_ZZZ, String.valueOf(dwrRoadway.DwrSrvrId)), DwrResponse.class, mActor.ticket);
                                            dwrRoadway = dtm.UpdateCostCenterKeys(dwrRoadway, response.costCenters);
                                            dwrRoadway = dtm.UpdateFieldKeys(dwrRoadway, response.fields, true);  // needed in case future updates
                                        } catch (ExpClass ex) {
                                            /* If this fails, hope for the best. */
                                            ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPDATE_KEYS));
                                        }
                                    } catch (ExpClass ex) {
                                        // Failed to Upload it to Server
                                        dwrRoadway.StatusMessage = ex.ExceptionName(ex.Number) + " " + ex.getMessage();
                                        ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPLOAD_DATA));
                                    }
                                } else {
                                    dwrRoadway.Status = DWR_STATUS_DRAFT;
                                    ExpClass.LogEXP(new ExpClass(), String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_POISON_RECD));
                                }
                                db.dwrDao().Update(dwrRoadway);
                            }
                            break;
                        case Q_TYPE_DWR_SAVE:
                            DwrTbl dwr = db.dwrDao().GetDwr(work.EventKey);
                            if (dwr != null) {
                                if (!isPoison) {
                                    String prefKey = Constants.SP_REG_DWR_FORMID;
                                    DwrTemplateMgr dtm = new DwrTemplateMgr(this, GenerateDwrQmap(prefKey));
                                    DwrRequest request = dtm.MapDwrToRequest(dwr);
                                    try {
                                        dwr.DwrSrvrId = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_SAVE_DWR), request, Integer.class, mActor.ticket);
                                        dwr.Status = DWR_STATUS_PENDING;
                                        okToDelete = true;
                                        updateLastSync(SP_LS_EVENTQUE_DWR);
                                        LogUploadToAudit(Q_TYPE_DWR_ROADWAY_FLAGGING_SAVE, dwr.DwrSrvrId, request.id, dwr.VersionInformation, request.submittedOn, dwr.Classification, dwr.Property);
                                        try {
                                            DwrResponse response = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_DWR).replace(SUB_ZZZ, String.valueOf(dwr.DwrSrvrId)), DwrResponse.class, mActor.ticket);
                                            dwr = dtm.UpdateCostCenterKeys(dwr, response.costCenters);
                                            dwr = dtm.UpdateFieldKeys(dwr, response.fields, true);  // needed in case future updates
                                        } catch (ExpClass ex) {
                                            /* If this fails, hope for the best. */
                                            ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPDATE_KEYS));
                                        }
                                    } catch (ExpClass ex) {
                                        // Failed to Upload it to Server
                                        dwr.StatusMessage = ex.ExceptionName(ex.Number) + " " + ex.getMessage();
                                        ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPLOAD_DATA));
                                    }
                                } else {
                                    dwr.Status = DWR_STATUS_DRAFT;
                                    ExpClass.LogEXP(new ExpClass(), String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_POISON_RECD));
                                }
                                db.dwrDao().Update(dwr);
                            }
                            break;
                        case Q_TYPE_JOB_SAVE:
                            JobSetupTbl jobSetupTbl = db.jobSetupDao().GetJobSetup(work.EventKey);
                            if (jobSetupTbl != null) {
                                if (!isPoison) {
                                    JobSetupTemplateMgr jtm = new JobSetupTemplateMgr(this, jobSetupTbl);
                                    FormsRequest request = jtm.createRequest();
                                    try {
                                        jobSetupTbl.JobSetupSvrId = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_SAVE_JOBSETUP), request, Integer.class, mActor.ticket);
                                        jobSetupTbl.Status = DWR_STATUS_PENDING;
                                        okToDelete = true;
                                        updateLastSync(SP_LS_EVENTQUE_JOB);
                                        try {
                                            JobSetupResponse jobSetupResponse = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_JOBSETUP).replace(SUB_ZZZ, String.valueOf(jobSetupTbl.JobSetupSvrId)), JobSetupResponse.class, mActor.ticket);
                                            jtm.UpdateAnswerKeys(jobSetupResponse.fields);
                                        } catch (ExpClass ex) {
                                            /* If this fails, hope for the best. */
                                            ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPDATE_KEYS));
                                        }
                                    } catch (ExpClass ex) {
                                        jobSetupTbl.StatusMessage = ex.ExceptionName(ex.Number) + " " + ex.getMessage();
                                        ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPLOAD_DATA));
                                    }
                                } else {
                                    jobSetupTbl.Status = DWR_STATUS_DRAFT;
                                    ExpClass.LogEXP(new ExpClass(), String.format(getResources().getString(R.string.upload_failed),eventType, FAIL_POISON_RECD));
                                }
                                db.jobSetupDao().Update(jobSetupTbl);
                            }
                            break;
                        case Q_TYPE_UTIL_JOB_SAVE:
                            JobSetupTbl utilitySetupTbl = db.jobSetupDao().GetJobSetup(work.EventKey);
                            if (utilitySetupTbl != null) {
                                if (!isPoison) {
                                    JobSetupTemplateMgr jtm = new JobSetupTemplateMgr(this, utilitySetupTbl);
                                    FormsRequest request = jtm.createRequest();
                                    try {
                                        utilitySetupTbl.JobSetupSvrId = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_SAVE_UTILITYSETUP), request, Integer.class, mActor.ticket);
                                        utilitySetupTbl.Status = DWR_STATUS_PENDING;
                                        okToDelete = true;
                                        updateLastSync(SP_LS_EVENTQUE_JOB);
                                        try {
                                            JobSetupResponse utilitySetupResponse = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_UTILITYSETUP).replace(SUB_ZZZ, String.valueOf(utilitySetupTbl.JobSetupSvrId)), JobSetupResponse.class, mActor.ticket);
                                            jtm.UpdateAnswerKeys(utilitySetupResponse.fields);
                                        } catch (ExpClass ex) {
                                            /* If this fails, hope for the best. */
                                            ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPDATE_KEYS));
                                        }
                                    } catch (ExpClass ex) {
                                        utilitySetupTbl.StatusMessage = ex.ExceptionName(ex.Number) + " " + ex.getMessage();
                                        ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPLOAD_DATA));
                                    }
                                } else {
                                    utilitySetupTbl.Status = DWR_STATUS_DRAFT;
                                    ExpClass.LogEXP(new ExpClass(), String.format(getResources().getString(R.string.upload_failed),eventType, FAIL_POISON_RECD));
                                }
                                db.jobSetupDao().Update(utilitySetupTbl);
                            }
                            break;
                        case Q_TYPE_COVER_SAVE:
                            JobSetupTbl coverSheetTbl = db.jobSetupDao().GetJobSetup(work.EventKey);
                            if (coverSheetTbl != null) {
                                if (!isPoison) {
                                    JobSetupTemplateMgr jtm = new JobSetupTemplateMgr(this, coverSheetTbl);
                                    FormsRequest request = jtm.createRequest();
                                    try {
                                        coverSheetTbl.JobSetupSvrId = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_SAVE_COVERSHEET), request, Integer.class, mActor.ticket);
                                        coverSheetTbl.Status = DWR_STATUS_PENDING;
                                        okToDelete = true;
                                        updateLastSync(SP_LS_EVENTQUE_JOB);
                                        try {
                                            JobSetupResponse coverSheetResponse = ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_COVERSHEET).replace(SUB_ZZZ, String.valueOf(coverSheetTbl.JobSetupSvrId)), JobSetupResponse.class, mActor.ticket);
                                            jtm.UpdateAnswerKeys(coverSheetResponse.fields);
                                        } catch (ExpClass ex) {
                                            /* If this fails, hope for the best. */
                                            ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPDATE_KEYS));
                                        }
                                    } catch (ExpClass ex) {
                                        coverSheetTbl.StatusMessage = ex.ExceptionName(ex.Number) + " " + ex.getMessage();
                                        ExpClass.LogEXP(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_UPLOAD_DATA));
                                    }
                                } else {
                                    coverSheetTbl.Status = DWR_STATUS_DRAFT;
                                    ExpClass.LogEXP(new ExpClass(), String.format(getResources().getString(R.string.upload_failed),eventType, FAIL_POISON_RECD));
                                }
                                db.jobSetupDao().Update(coverSheetTbl);
                            }
                            break;
                        default:
                            // There is no default, should not get here.
                            ExpClass.LogEXP(new ExpClass(), String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_INVALID_TYPE));
                    }
                    // Remove from queue.
                    if (okToDelete || isPoison) {
                        db.workflowDao().Delete(work);
                    } else {
                        // If not deleting set the status of uploading to false
                        work = db.workflowDao().GetWorkflowTblUniqueKeyAndType(work.EventKey, eventType);
                        work.Uploading = UPLOADING_FALSE;
                        db.workflowDao().Update(work);
                    }
                    // Check for another record.
                    work = db.workflowDao().Pop(KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString());
                }
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, String.format(getResources().getString(R.string.upload_failed), eventType, FAIL_GENERAL_EXIT));
        }
    }

    private void LogUploadToAudit(int saveType, int serverId, int id, String ver, String submittedOn, int classifier, int property) {
        List<Pair<String, String>> values = new ArrayList<>();
        Pair<String, String> kvp;
        switch (saveType){
            case Q_TYPE_DWR_ROADWAY_FLAGGING_SAVE:
            case Q_TYPE_DWR_SAVE:
                 kvp = new Pair<String, String>("UploadType", "DWR");
                break;
            default:
                kvp = new Pair<String, String>("UploadType", "Unknown");
                break;
        }
        values.add(kvp);
        values.add(new Pair<String, String>("ServerId", String.valueOf(serverId)));
        values.add(new Pair<String, String>("NewUpload", id==0 ? "True" : "False"));
        values.add(new Pair<String, String>("Timestamp", submittedOn));
        values.add(new Pair<String, String>("Version", ver));
        values.add(new Pair<String, String>("classification", Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(classifier)));
        values.add(new Pair<String, String>("property", Railroads.PropertyName(getApplicationContext(), property)));
        ExpClass.Audit("LogUploadToAudit", "Audit of uploaded forms.", values);
    }

    /* Gets all the DWR and checks it with the server and compares it with the local
     * ones for change if it has been rejected, change the status and send a notification
     * This is a (now) a full synchronization of DWR data.  DWRs are sourced from the
     * app, so we are not going to treat the API as a source of truth like other data.
     * Instead the following rules apply:
     * Delete:  Any DWR over 120 days old. The user can also manually delete DWRs. Approved
     *          DWRs that are of a certain age are hidden via SQL, but not deleted.
     * Update:  DWRs have their status changed after review, e.g. Approved, Need Changes, etc.
     *          This status review data is the only info updated from the server.
     * Adding:  When a DWR exists on the backend but not in the app, that indicates something
     *          bad has happened.  In this case, the full DWR from the backend is downloaded
     *          to the app.  Only the last N days of DWRs are retrieved (based on default that
     *          determines how many can be seen).
     */
    private void SyncDWRStatus(Actor mActor) {
        try {
            if (ApiService.IsNetwork(this)) {
                List<DwrTbl> localDwrs = db.dwrDao().GetAllDwr();
                ListRequest request = (new JobSetupDwrUpdateMgr(this)).getRequest();
                WebServiceModels.DwrShortResponse serverDwrsResponse = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_DWR_LIST), request, WebServiceModels.DwrShortResponse.class, mActor.ticket);
                //Delete120DaysOldDwrs(localDwrs);
                UpdateDwr(localDwrs, serverDwrsResponse, mActor.ticket);
                AddDwrs(mActor, localDwrs, serverDwrsResponse);
                updateLastSync(SP_LS_DWRSTATUS);
            }
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "Sync DWR Status");
        } catch (Exception e) {
            ExpClass.LogEX(e, mActor.unique);
        }
    }

    private void AddDwrs(Actor mActor, List<DwrTbl> localDwrs, DwrShortResponse serverDwrsResponse) throws ExpClass {
        List<DwrShortResponseResult> mostRecentDWRsFromServer = new ArrayList<>();
        // Trim down the DWRs to only provide recent ones.
        for(DwrShortResponseResult shortResponse : serverDwrsResponse.results) {
            try {
                long days = KTime.CalcDateDifference(shortResponse.date, KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS).toString(), KTime.KT_fmtDateOnlyRPFS, KTime.KT_DAYS);
                int status = shortResponse.status != null ? shortResponse.status.id : 0;
                if(days < Settings.getPrefOldestDays(getApplicationContext()) &&  status != DWR_STATUS_API_RejectedId) {
                    mostRecentDWRsFromServer.add(shortResponse);
                }
            } catch (ExpParseToCalendar ep) {
                ExpClass.LogEXP(ep, mActor.unique);
            }
        }

        for (DwrShortResponseResult dwrShortResponseResult : mostRecentDWRsFromServer) {
            boolean isPresent = false;
            for (DwrTbl dwrFromTbl : localDwrs) {
                isPresent = dwrFromTbl.DwrSrvrId == dwrShortResponseResult.id;
                if(isPresent) { break; }
            }

            if( !isPresent ) {
                DwrResponse serverDWR = ApiService.CallGetApi(
                        connection.getFullApiPath(Connection.API_GET_DWR).replace(SUB_ZZZ, String.valueOf(dwrShortResponseResult.id)),
                        DwrResponse.class,
                        mActor.ticket);
                DwrTbl localdwr = new DwrTbl();
                localdwr.UserId = mActor.unique;

                try {
                    localdwr.RwicSignatureName = getSignatureFileName(Connection.API_GET_DWR_RWIC_SIGNATURE, mActor, dwrShortResponseResult);
                    localdwr.ClientSignatureName = getSignatureFileName(Connection.API_GET_DWR_CLIENT_SIGNATURE, mActor, dwrShortResponseResult);
                    localdwr.RailSignatureName = getSignatureFileName(Connection.API_GET_DWR_RAIL_SIGNATURE, mActor, dwrShortResponseResult);
                    String key = serverDWR.serviceType.id == Constants.RP_ROADWAY_FLAGGING_SERVICE ? SP_REG_DWR_FORMID_ROADWAY_FLAGGING : SP_REG_DWR_FORMID;
                    DwrTemplateMgr templateMgr = new DwrTemplateMgr(this, GenerateDwrQmap(key));
                    templateMgr.MapDwrDetailResponseToDwr(serverDWR, localdwr, mActor.ticket);
                } catch (IOException e) {
                    ExpClass.LogEX(e, mActor.unique);
                }
            }
        }
    }

    private String getSignatureFileName(String urlPath, Actor mActor, DwrShortResponseResult dwrShortResponseResult) throws ExpClass, IOException {
        String base64String;
        try {
            base64String = ApiService.CallGetApi(connection.getFullApiPath(urlPath).replace(SUB_ZZZ, String.valueOf(dwrShortResponseResult.id)), String.class, mActor.ticket);
        } catch (Exception ex) {  // API tends to fall down if no signature, but it is a normal thing.
            return "";
        }
        // Not every DWR has all the signatures, so do not bother if it is not there.
        if(base64String == null || base64String.length() == 0) {
            return "";
        }
        byte[] bites = Base64.decode(base64String, Base64.DEFAULT);
        String fileName = System.currentTimeMillis() + "";
        Functions.SaveRawToFile(DOC_SIGNATURES, fileName, bites);
        return fileName;
    }

    private void Delete120DaysOldDwrs(List<DwrTbl> allDwr) {
        long FOUR_MONTHS_AGO = -1440 * 2; // in hours
        long jobEnd;
        String basePath = Environment.getExternalStorageDirectory().toString() + SIGNATURE_FILE_PATH + "/";
        for(DwrTbl dwr : allDwr) {
            try {
                String clientSignatureFileName = basePath + dwr.ClientSignatureName;
                String rwicSignatureFilename = basePath + dwr.ClientSignatureName;

                jobEnd = KTime.IsPast(dwr.WorkDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE);
                if(jobEnd < FOUR_MONTHS_AGO)  {
                    DeleteFile(clientSignatureFileName);
                    DeleteFile(rwicSignatureFilename);
                    db.dwrDao().Delete(dwr);
                }
            } catch (ExpParseToCalendar expParseToCalendar) {/* continue */}
        }
    }

    private void DeleteFile(String filename) {
        File fdelete = new File(filename);
        if (fdelete.exists()) {
            fdelete.delete();
        }
    }

    /**
     *  Gets local JobSetup records and checks with the server to look for status changes.
     */
    private void SyncJobStatus(Actor mActor) {
        try {
            if (ApiService.IsNetwork(this)) {
                // Traditional Job Setups
                List<JobSetupTbl> flaggingJobSetups = db.jobSetupDao().GetAllJobSetupTbl(RP_FLAGGING_SERVICE);
                WebServiceModels.ListRequest request = (new JobSetupDwrUpdateMgr(this)).getRequest();
                WebServiceModels.JobSetupShortResponse serverJobSetups = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_JOBSETUP_LIST), request, WebServiceModels.JobSetupShortResponse.class, mActor.ticket);
                UpdateJobSetupTbl(flaggingJobSetups, serverJobSetups, mActor.ticket);
                updateLastSync(SP_LS_JOBSTATUS);
            }
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "Sync Job Status");
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    /**
     *  Gets local JobSetup records and checks with the server to look for status changes.
     */
    private void SyncUtilityJobStatus(Actor mActor) {
        try {
            if (ApiService.IsNetwork(this)) {
                // Utility Job Setups can have conflicting server ids with other records in the Job Setup table, so need to break them out.
                List<JobSetupTbl> utilityJobSetups = db.jobSetupDao().GetAllJobSetupTbl(RP_UTILITY_SERVICE);
                WebServiceModels.ListRequest request = (new JobSetupDwrUpdateMgr(this)).getRequest();
                WebServiceModels.JobSetupShortResponse serverJobSetups = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_UTILITYSETUP_LIST), request, WebServiceModels.JobSetupShortResponse.class, mActor.ticket);
                UpdateJobSetupTbl(utilityJobSetups, serverJobSetups, mActor.ticket);
                updateLastSync(SP_LS_JOBSTATUS);
            }
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "Sync Job Status");
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    /**
     *  Gets local Cover Sheet records and checks with the server to look for status changes.
     */
    private void SyncCoverStatus(Actor mActor) {
        try {
            if (ApiService.IsNetwork(this)) {
                // Cover Sheets can have conflicting server ids with other records in the Job Setup table, so need to break them out.
                List<JobSetupTbl> utilityCoverSheets = db.jobSetupDao().GetAllJobSetupTbl(RP_COVER_SERVICE);
                WebServiceModels.ListRequest request = (new JobSetupDwrUpdateMgr(this)).getRequest();
                WebServiceModels.JobSetupShortResponse serverJobSetups = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_COVERSHEET_LIST), request, WebServiceModels.JobSetupShortResponse.class, mActor.ticket);
                UpdateJobSetupTbl(utilityCoverSheets, serverJobSetups, mActor.ticket);
                updateLastSync(SP_LS_JOBSTATUS);
            }
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "Sync Job Status");
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    private void UpdateJobSetupTbl(List<JobSetupTbl> localJobSetups, JobSetupShortResponse serverJobSetups, String ticket) {

        Hashtable<Integer, JobSetupTbl> jsfHT = new Hashtable<>();
        for (JobSetupTbl temp : localJobSetups) {
            if (temp.JobSetupSvrId != 0 && temp.Status > DWR_STATUS_QUEUED) {
                jsfHT.put(temp.JobSetupSvrId, temp);
            }
        }
        for (JobSetupResponseResult serverJS : serverJobSetups.results) {

            JobSetupTbl localJS = jsfHT.get(serverJS.id);

            if (localJS != null && serverJS.status != null && serverJS.job != null) {
                switch (serverJS.status.id) {
                    case DWR_STATUS_API_ChangesRequiredId:
                        if (localJS.Status != DWR_STATUS_BOUNCED) {
                            RequireChangeJobSetupTbl(localJS, serverJS.job.number, ticket);
                        }
                        break;
                    case DWR_STATUS_API_ApprovedId:
                        if(localJS.Status != DWR_STATUS_APPROVED) {
                            ApproveJobSetupTbl(localJS, ticket);
                        }
                        break;
                    case DWR_STATUS_API_RejectedId:
                        if(localJS.Status != DWR_STATUS_BOUNCED) {
                            RejectJobSetupTbl(localJS, ticket);
                        }
                        break;
                }
            }
        }
    }

    private void ApproveJobSetupTbl(JobSetupTbl localJS, String ticket) {
        JobSetupResponse response = GetJobSetupFromAPI(localJS, ticket);
        if (response != null) {
            localJS.ReviewerNotes = response.reviewNotes;
            localJS.ReviewerOn = response.reviewedOn;
            if (response.reviewer != null) {
                localJS.ReviewerName = response.reviewer.name;
                localJS.ReviewerId = response.reviewer.id;
            }
        }
        localJS.Status = DWR_STATUS_APPROVED;
        ScheduleDB.getDatabase(getApplicationContext()).jobSetupDao().Update(localJS);
    }

    private void RejectJobSetupTbl(JobSetupTbl localJS, String ticket) {
        JobSetupResponse response = GetJobSetupFromAPI(localJS, ticket);
        if (response != null) {
            localJS.ReviewerNotes = response.reviewNotes;
            localJS.ReviewerOn = response.reviewedOn;
            if (response.reviewer != null) {
                localJS.ReviewerName = response.reviewer.name;
                localJS.ReviewerId = response.reviewer.id;
            }
        }
        localJS.Status = DWR_STATUS_BOUNCED;
        ScheduleDB.getDatabase(getApplicationContext()).jobSetupDao().Update(localJS);
    }

    public JobSetupResponse GetJobSetupFromAPI(JobSetupTbl temp, String ticket) {
        String path = "";
        try {
            switch (temp.AssignmentId){
                case RP_FLAGGING_SERVICE:
                    path = Connection.API_GET_JOBSETUP;
                    break;
                case RP_UTILITY_SERVICE:
                    path = Connection.API_GET_UTILITYSETUP;
                    break;
                case RP_COVER_SERVICE:
                    path = Connection.API_GET_COVERSHEET;
                    break;
            }
            return ApiService.CallGetApi(connection.getFullApiPath(path).replace(SUB_ZZZ, String.valueOf(temp.JobSetupSvrId)), JobSetupResponse.class, ticket);
        } catch (Exception e) {
            ExpClass.LogEX(e, "GetJobSetupFromAPI");
        }
        return null;
    }

    /**
     *  The Job List is used to allow ad-hoc creation of DWRs and Job Setup forms.  It provides
     *  a current list of jobs, typically limited by property.  While the database table is
     *  updated when a request to view the list is made, there is no capacity to delete records
     *  This method is here to periodically do that clean up.
     *  Only do this periodically and if connected to network.  After clean up, repopulate table.
     */
    private void SyncJobList(Actor mActor) {
        try {
            SharedPreferences registered = getApplication().getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
            long lastUpdate = registered.getLong(SP_REG_JOBLIST, 0);
            if ((Math.abs(KTime.GetEpochNow() - lastUpdate) > 30 * 24 * 60 * 60) // 1 Month
                && ApiService.IsNetwork(getApplicationContext())) {
                ScheduleDB.getDatabase(getApplicationContext()).jobDao().DeleteAll();
                JobListThread work = new JobListThread(getApplicationContext(), ScheduleDB.getDatabase(getApplicationContext()), null);
                work.start();
                // Update the next time to run.
                SharedPreferences.Editor editor = registered.edit();
                editor.putLong(SP_REG_JOBLIST, KTime.GetEpochNow());
                editor.apply();
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, mActor.unique);
        }
    }

    private void RequireChangeJobSetupTbl(JobSetupTbl localJS, int jobNumber, String ticket) {
        JobSetupResponse response = GetJobSetupFromAPI(localJS, ticket);
        if (response != null) {
            localJS.ReviewerNotes = response.reviewNotes;
            localJS.ReviewerOn = response.reviewedOn;
            if (response.reviewer != null) {
                localJS.ReviewerName = response.reviewer.name;
                localJS.ReviewerId = response.reviewer.id;
            }
        }
        localJS.Status = DWR_STATUS_BOUNCED;
        ScheduleDB.getDatabase(getApplicationContext()).jobSetupDao().Update(localJS);

        Intent resultIntent = new Intent(this, JobSetup.class);
        resultIntent.putExtra(IN_JSID, localJS.Id);
        resultIntent.putExtra(IN_PROPERTYID, localJS.RailRoadId);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(REQUESTCODE_JOBSETUP, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.declined)
                .setContentTitle(String.format(getResources().getString(R.string.job_setup_change), jobNumber))
                .setContentText((response != null) ? response.reviewNotes : "")
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(localJS.JobSetupSvrId, mBuilder.build());
    }

    /** The Assignments from the server come in as Jobs with 1 or more shifts (for a specific worker).
     *  RP has not really figured out the best way to display these.  To be as flexible as possible,
     *  the Assignments in the mobile app are the actual shifts, broken out separately.  Included
     *  with each shift is all the related job information they might need to stand on their own.
     *  This list is compared to what is stored in the local data base as part of the sync process.
     **/
    private List<AssignmentTbl> GetAssignments(int who, String unique, String token) {
        try {
            List<AssignmentTbl> assignments = new ArrayList<>();
            AssignmentRequest request = new AssignmentRequest();
            request.fieldWorkerId = who;
            AssignmentsResponse basicAssignments = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_SCHEDULE), request, AssignmentsResponse.class, token);

            for (AssignmentWS item : basicAssignments.results) {
                // Not all the job information comes down with the assignment.
                // Keep in mind the data from backend likes to include nulls. :(
                if (item.job == null) {
                    continue; /* Without a job, just skip it. */
                }
                // Job information
                String jobPath = connection.getFullApiPath(Connection.API_GET_JOB_BY_ID).replace(SUB_ZZZ, String.valueOf(item.job.id));
                JobDetailResponse jobResponse = ApiService.CallGetApi(jobPath, JobDetailResponse.class, token);
                // Job cost center information
                String costPath = connection.getFullApiPath(Connection.API_GET_JOB_COSTCENTERS).replace(SUB_ZZZ, String.valueOf(item.job.id));
                JobCostCenter[] costResponse = ApiService.CallGetApi(costPath, JobCostCenter[].class, token);

                for (AssignmentShiftWS shift : item.shifts) {
                    // Look up the property index based on the backend name.
                    int property = -1;
                    if (jobResponse.railroad != null) {
                        property = Railroads.PropertyKeyServer(getApplicationContext(), jobResponse.railroad.code);
                    }
                    // Build an Assignment.
                    AssignmentTbl action = Functions.BuildAssignment(unique, property, jobResponse, Arrays.asList(costResponse), item, shift);
                    assignments.add(action);
                }
            }
            return assignments;
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "Get Assignments");
            return new ArrayList<>();
        } catch (Exception ex) {
            ExpClass.LogEX(ex, unique);
            return new ArrayList<>();
        }
    }


    private void JobUpdate(WebServiceModels.FormResponse formResponse, int templateId) {
        FieldPlacementDao fieldPlacementDao = db.fieldPlacementDao();
        FieldPlacementTbl fieldPlacementTbl;

        // Do nothing if Error Response
        if (formResponse == null || formResponse.fields == null || formResponse.fields.size() == 0) {
            return;
        }

        //Delete All ID's Before Filling In
        fieldPlacementDao.DeleteAllId(templateId);

        for (WebServiceModels.QuestionWS field : formResponse.fields) {
            fieldPlacementTbl = new FieldPlacementTbl();
            fieldPlacementTbl.Id = 0;
            fieldPlacementTbl.TemplateId = field.template.id; // matches the templateId above
            fieldPlacementTbl.TemplateName = field.template.name;
            fieldPlacementTbl.FieldId = field.id;
            fieldPlacementTbl.FieldType = field.field.type;
            fieldPlacementTbl.FieldPrompt = field.field.prompt;
            fieldPlacementTbl.FieldInstructions = field.field.instructions;
            fieldPlacementTbl.FieldOptions = ""; //field.field.options;
            fieldPlacementTbl.Required = field.isRequired;
            fieldPlacementTbl.Group = field.group;
            fieldPlacementTbl.Note = field.note;
            fieldPlacementTbl.Code = field.usageCode;

            fieldPlacementDao.Insert(fieldPlacementTbl);
        }
    }

    /**
     * Assignments should be deleted locally on two occasions.
     * 1) The shift it is associated with has been deleted from the server.
     * Note that DWRs created ad-hoc never get a shift (shift=0) so those
     * are ignored for this first case.
     * 2) There are no longer current DWRs or JSFs associated with it and
     * the end date of the job is two months ago.
     */
    private void AssignmentDeletes(List<AssignmentTbl> server, List<AssignmentTbl> local) {
        try {
            long TWO_MONTHS_AGO = -1440; // in hours

            for (AssignmentTbl project : local) {

                boolean foundOnServer = false;
                for (AssignmentTbl job : server) {
                    if (project.ShiftId == job.ShiftId) {
                        foundOnServer = true;
                        break;
                    }
                }

                if (project.ShiftId > 0 && !foundOnServer) {
                    db.assignmentDao().Delete(project);
                } else {
                    int dwrCnt = db.assignmentDao().GetDwrCountOnAssignment(project.JobId, Settings.getPrefOldestDwr(this));
                    int jsfCnt = db.assignmentDao().GetJsfCountOnAssignment(project.JobId, Settings.getPrefOldestDwr(this));
                    boolean jobOver;
                    try {
                        jobOver = KTime.IsPast(project.JobEndDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE) < TWO_MONTHS_AGO;
                    } catch (ExpParseToCalendar kx) {
                        jobOver = true; // If there is no end date, safe to give it the boot if not in use.
                    }
                    if (dwrCnt == 0 && jsfCnt == 0 && jobOver) {
                        db.assignmentDao().Delete(project);
                    }
                }
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "AssignmentDeletes");
        }
    }

    /**
     * The Zero Shift assignments happen when a DWR or Job Setup is created without a known
     * shift.  For example, the Ad-hoc method or less frequently from a data sync.  As such
     * the normal update process will not work, since it depends on the server's Shift Id.
     * The data that goes stale is the job context, this method will refresh that information.
     */
    private void RefreshZeroShifts(List<AssignmentTbl> scheduleStored, String unique, String token) {
        try {
            for (AssignmentTbl item : scheduleStored) {
                if (item.ShiftId == 0 && item.JobId != 0){
                    // Kind of by definition, the AssignmentWS is meaningless, but good to preserve the original date.
                    AssignmentWS current = new AssignmentWS();
                    current.start = item.StartDate.substring(0, 19);
                    current.end = item.EndDate.substring(0, 19);
                    current.fieldContactName = item.FieldContactName;
                    current.fieldContactPhone = item.FieldContactPhone;
                    current.fieldContactEmail = item.FieldContactEmail;
                    // Same thing with the shift.
                    AssignmentShiftWS shift = new AssignmentShiftWS();
                    shift.id = item.ShiftId;
                    shift.serviceType = new BaseIdWS();
                    shift.serviceType.id = item.ServiceType;
                    shift.notes = item.ShiftNotes;
                    shift.needsJobSetupForm = item.JobSetup;
                    shift.day = item.ShiftDate.substring(0, 19);
                    // This is the data that can actually change (job).
                    String jobPath = connection.getFullApiPath(Connection.API_GET_JOB_BY_ID).replace(SUB_ZZZ, String.valueOf(item.JobId));
                    JobDetailResponse jobResponse = ApiService.CallGetApi(jobPath, JobDetailResponse.class, token);
                    // Job cost center information
                    String costPath = connection.getFullApiPath(Connection.API_GET_JOB_COSTCENTERS).replace(SUB_ZZZ, String.valueOf(item.JobId));
                    JobCostCenter[] costResponse = ApiService.CallGetApi(costPath, JobCostCenter[].class, token);

                    // Build an Assignment.
                    AssignmentTbl action = Functions.BuildAssignment(unique, item.RailroadId, jobResponse, Arrays.asList(costResponse), current, shift);
                    if(!item.equals(action)) {
                        action.AssignmentId = item.AssignmentId;
                        db.assignmentDao().Update(action);
                    }
                }
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "AssignmentZeroRefresh");
        }
    }

    private void AssignmentUpdates(List<AssignmentTbl> server, List<AssignmentTbl> local) {

        localLoop:
        for (AssignmentTbl project : local) {
            for (AssignmentTbl job : server) {
                if (project.ShiftId == job.ShiftId) {
                    if(!job.equals(project)) {
                        // AssignmentId local only value, need to augment the server data with it.
                        job.AssignmentId = project.AssignmentId;
                        db.assignmentDao().Update(job);
                    }
                    continue localLoop; // The localLoop will cut short the inner loop iterations.
                }
            }
        }
    }

    private void AssignmentAdditions(List<AssignmentTbl> server, List<AssignmentTbl> local) {
        long TWO_MONTHS_AGO = -1440; // in hours

        localLoop:
        for (AssignmentTbl job : server) {
            for (AssignmentTbl project : local) {
                if (job.ShiftId == project.ShiftId)
                    continue localLoop;
            }
            try {
                // Before adding an Assignment, make sure it has a reasonably recent shift.
                boolean tooOld = KTime.IsPast(job.ShiftDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE) < TWO_MONTHS_AGO;
                if (!tooOld) {
                    db.assignmentDao().Insert(job);
                }
            } catch (Exception ex) {
                ExpClass.LogEX(ex, "AssignmentAdditions");
            }
        }
    }

    /* *****************************************************************************
     *  The following set of methods is used to sync Documents with the server.
     */

    /**
     *  Loads all the known documents on the local device and makes sure that
     *  those documents are still valid.  A document is not longer valid when:
     *  1) Document has been deleted on the server. There will not be a matching
     *      ServerId since it has been delete on the backend.
     *  2) Assignment the document is linked to has been deleted from the device.
     *      This is a second order effect.  Only ServerId values from local jobs
     *      (assignments) are loaded into the list of server documents. So if an
     *      assignment no longer exists locally, the document will show up locally
     *      but not appear in the list of server side documents (and get deleted here).
     * @param server    List of server side docs linked to local assignments.
     * @param local     List of device side docs saved with type DOC_OWNER_JOB.
     */
    private void CheckDocDeletes(List<DocumentTbl> server, List<DocumentTbl> local) {
        localLoop:
        for (DocumentTbl document : local) {
            for (DocumentTbl jobdoc : server) {
                if (document.ServerId == jobdoc.ServerId)
                    continue localLoop;
            }
            if (Functions.DeleteFile(DOC_OWNER_JOB, document.FileName, this)) {
                db.documentDao().Delete(document);
            }
        }
    }

    /**
     *  Compares all the documents on the device to their corresponding server side
     *  version.  If the update date or filename has changed, delete the old file,
     *  save the new one and update the database.
     *  NOTE: The explicit delete of the file is due to the file name being just
     *  a field (that can change) on the server side and not a permanent key.
     * @param server    List of server side docs linked to local assignments.
     * @param local     List of device side docs saved with type DOC_OWNER_JOB.
     * @param token     Token used to download any files.
     */
    private void CheckDocUpdates(List<DocumentTbl> server, List<DocumentTbl> local, String token) {
        localLoop:
        for (DocumentTbl document : local) {
            for (DocumentTbl jobdoc : server) {
                if (document.ServerId == jobdoc.ServerId) {
                    if (jobdoc.LastUpdate.equalsIgnoreCase(document.LastUpdate)
                        && jobdoc.FileName.equalsIgnoreCase(document.FileName)) {
                        continue localLoop;
                    }
                    jobdoc.DocumentId = document.DocumentId;     // local db id stays the same on changes.
                    Functions.DeleteFile(DOC_OWNER_JOB, document.FileName, this);  // Explicit delete, since the file name might have changed.
                    if (DownloadDocument(jobdoc, token)) {
                        db.documentDao().Update(jobdoc);
                    }
                }
            }
        }
    }

    /**
     *  Final leg of the synchronization, download and add any files linked to jobs
     *  that have not been downloaded yet.
     * @param server    List of server side docs linked to local assignments.
     * @param local     List of device side docs saved with type DOC_OWNER_JOB.
     * @param token     Token used to download any files.
     */
    private void CheckDocAdditions(List<DocumentTbl> server, List<DocumentTbl> local, String token) {
        localLoop:
        for (DocumentTbl jobdoc : server) {
            for (DocumentTbl document : local) {
                if (document.ServerId == jobdoc.ServerId) {
                    continue localLoop;
                }
            }
            // Save the file to disk, then save file meta data to db.
            if (DownloadDocument(jobdoc, token)) {
                db.documentDao().Insert(jobdoc);
            }
        }
    }

    private boolean DownloadDocument(DocumentTbl meta, String token) {
        boolean success = false;
        FileOutputStream fos = null;
        try {
            // Step 1: Get the bits
            String docPath = connection.getFullApiPath(Connection.API_GET_DOC_BYTES).replace(SUB_ZZZ, String.valueOf(meta.ServerId));
            DocumentBitsResponse response = ApiService.CallGetApi(docPath, DocumentBitsResponse.class, token);

            // Step 2: Save the bits
            if (response != null) {
                if (response.item1.bytes.length() > 0) {
                    byte[] bits = Base64.decode(response.item1.bytes, Base64.DEFAULT);
                    fos = openFileOutput(meta.FileName, Context.MODE_PRIVATE);
                    fos.write(bits);
                    fos.flush();
                    fos.close();
                    success = true;
                }
            }
            return success;

        } catch (Exception ex) {
            ExpClass.LogEX(ex, meta.FileName);
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                return false;
            }
        }
    }

    private boolean CheckContainRailRoad(RailRoadTbl tbl, RailRoadItem item) {
        return tbl.railroadId == item.id;
    }

    private void InsertRailRoadTbl(RailRoadItem railroad, Division division, Division subdivision) {
        RailRoadTbl railRoadTbl = new RailRoadTbl();
        railRoadTbl.railroadId = (int) railroad.id;
        railRoadTbl.divisionId = division == null ? 0 : (int) division.id;
        railRoadTbl.subdivisionId = subdivision == null ? 0 : (int) subdivision.id;
        railRoadTbl.code = railroad.code;
        railRoadTbl.companyName = railroad.companyName;
        railRoadTbl.divisionName = division == null ? null : division.name;
        railRoadTbl.subdivisionName = subdivision == null ? null : subdivision.name;

        db.railRoadDao().Insert(railRoadTbl);
    }

    /* Normal Comparison with for loop would be n^2 so I stored everything in a Hashmap (n) and
     * then looped through result list and compared if they were there (m) O(n + m) if they were
     * then i'd update
     *
     * Update Status would Check for Status is Bounced, if Bounced then it would grab the DWR
     * from the sever and update the Notes and change of the current DWR to match the status
     * of the bounced DWR and then Send a Notification to the User that it has Been Updated
     * */
    private void UpdateDwr(List<DwrTbl> dwrTbls, WebServiceModels.DwrShortResponse result, String ticket) {
        //Key ServerIdNumber
        Hashtable<Integer, DwrTbl> dwrHT = new Hashtable<>();
        for (DwrTbl temp : dwrTbls) {

            dwrHT.put(temp.DwrSrvrId, temp);
        }
        for (DwrShortResponseResult resultTemp : result.results) {
            //Update Status
            DwrTbl localCopy = dwrHT.get(resultTemp.id);

            // If there is no local copy, skip the synchronization.
            if (localCopy != null) {
                // This is to help avoid unnecessary API calls
                if(DwrStatusChanged(localCopy.ReviewerOn, resultTemp.reviewedOn, localCopy.Status, resultTemp.status.id)) {
                    switch (resultTemp.status.id) {
                        case DWR_STATUS_API_ChangesRequiredId:
                            RequireChangeDwr(localCopy, ticket);
                            break;
                        case DWR_STATUS_API_ApprovedId:
                            ApproveDwr(localCopy, ticket);
                            break;
                        case DWR_STATUS_API_RejectedId:
                            RejectDwr(localCopy, ticket);
                            break;
                        case DWR_STATUS_API_SubmittedId:
                        case DWR_STATUS_API_ResubmittedId:
                            SubmitDwr(localCopy, ticket);
                            break;
                    }
                }
                // Check if any work dates have changed
                try {
                    String holdLocalWorkDate = KTime.ParseToFormat(localCopy.WorkDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDate3339Raw, TimeZone.getDefault().getID()).toString();
                    String holdLocalWorkTime = KTime.ParseToFormat(localCopy.WorkDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtTime3339Raw, TimeZone.getDefault().getID()).toString();
                    String holdServerWorkDate = resultTemp.date.substring(0, 10);
                if(!holdLocalWorkDate.equalsIgnoreCase(holdServerWorkDate)){
                    localCopy.WorkDate = KTime.ParseToFormat(holdServerWorkDate + "T" + holdLocalWorkTime, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE ).toString();
                    ScheduleDB.getDatabase(getApplicationContext()).dwrDao().Update(localCopy);
                }
                } catch (Exception ex) {
                    /* Skip but log, if error */
                    ExpClass.LogEX(ex, localCopy.WorkDate);
                }
            }
        }
    }

    private boolean DwrStatusChanged(String localReviewerDate, String serverReviewerDate, int localStatus, int serverStatus) {
        // If we have dates, we will let a difference determine that change has occurred.
        if(localReviewerDate != null && serverReviewerDate != null){
            if(!localReviewerDate.equalsIgnoreCase(serverReviewerDate)){
                return true;
            }
        }

        // Beyond review date, check status for determination of change.
        // Rejected DWRs on server are deleted locally, so there is no status for them.
        switch (localStatus){
            case DWR_STATUS_NEW:
            case DWR_STATUS_DRAFT:
            case DWR_STATUS_QUEUED:
            case DWR_STATUS_PROCESSED:
                return false;
            case DWR_STATUS_PENDING:
                return !(serverStatus == DWR_STATUS_API_SubmittedId || serverStatus == DWR_STATUS_API_ResubmittedId);
            case DWR_STATUS_BOUNCED:
                return !(serverStatus == DWR_STATUS_API_ChangesRequiredId);
            case DWR_STATUS_APPROVED:
                return !(serverStatus == DWR_STATUS_API_ApprovedId);
            default:
                return true;
        }
    }

    public DwrResponse GetDwrNotesFromAPI(DwrTbl temp, String ticket) {
        try {
            //Try to get Rejected Notes
            return ApiService.CallGetApi(connection.getFullApiPath(Connection.API_GET_DWR).replace(SUB_ZZZ, String.valueOf(temp.DwrSrvrId)), DwrResponse.class, ticket);
        } catch (Exception e) {
            ExpClass.LogEX(e, "GetDwrNotesFromAPI");
        }
        return null;
    }

    public void RequireChangeDwr(DwrTbl temp, String ticket) {
        DwrResponse response = GetDwrNotesFromAPI(temp, ticket);
        if (response != null) {
            temp.ReviewerNotes = response.reviewNotes;
            temp.ReviewerOn = response.reviewedOn;
            if (response.reviewer != null) {
                temp.ReviewerName = response.reviewer.name;
                temp.ReviewerId = response.reviewer.id;
            }
        }
        temp.Status = DWR_STATUS_BOUNCED;
        ScheduleDB.getDatabase(getApplicationContext()).dwrDao().Update(temp);

        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        Intent resultIntent = new Intent(this, DwrEdit.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra(IN_DWRID, temp.DwrId);
        resultIntent.putExtra(IN_JOBID, temp.JobId);
        resultIntent.putExtra(IN_PROPERTYID, temp.Property);

        int requestID = (int) System.currentTimeMillis();
        PendingIntent contentIntent = PendingIntent.getActivity(this, requestID,
                resultIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, "default");
        notification = builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.declined)
                .setWhen(temp.DwrSrvrId)
                .setContentTitle(String.format(getResources().getString(R.string.dwr_notification),
                        Arrays.asList(getResources().getStringArray(R.array.classification_name)).get(temp.Classification), temp.JobNumber))
                .setContentText((response != null) ? response.reviewNotes : "")
                .setContentIntent(contentIntent)
                .setAutoCancel(true).build();

        notificationManager.notify(temp.DwrSrvrId, notification);
    }

    public void ApproveDwr(DwrTbl temp, String ticket) {
        DwrResponse response = GetDwrNotesFromAPI(temp, ticket);
        if (response != null) {
            temp.ReviewerNotes = response.reviewNotes;
            temp.ReviewerOn = response.reviewedOn;
            if (response.reviewer != null) {
                temp.ReviewerName = response.reviewer.name;
                temp.ReviewerId = response.reviewer.id;
            }
        }
        temp.Status = DWR_STATUS_APPROVED;
        ScheduleDB.getDatabase(getApplicationContext()).dwrDao().Update(temp);
    }

    //Note Not doing anything for Rejected At the Moment
    public void RejectDwr(DwrTbl temp, String ticket) {
        DwrResponse response = GetDwrNotesFromAPI(temp, ticket);
        if (response != null) {
            temp.ReviewerNotes = response.reviewNotes;
            temp.ReviewerOn = response.reviewedOn;
            if (response.reviewer != null) {
                temp.ReviewerName = response.reviewer.name;
                temp.ReviewerId = response.reviewer.id;
            }
        }
        temp.Status = DWR_STATUS_BOUNCED;
        ScheduleDB.getDatabase(getApplicationContext()).dwrDao().Delete(temp);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.declined)
                .setContentTitle("DWR " + temp.JobNumber + " Rejected!")
                .setContentText((response != null) ? response.reviewNotes : "")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(temp.DwrSrvrId, mBuilder.build());

    }

    public void SubmitDwr(DwrTbl temp, String ticket) {
        DwrResponse response = GetDwrNotesFromAPI(temp, ticket);
        if (response != null) {
            temp.ReviewerNotes = response.reviewNotes;
            temp.ReviewerOn = response.reviewedOn;
            if (response.reviewer != null) {
                temp.ReviewerName = response.reviewer.name;
                temp.ReviewerId = response.reviewer.id;
            }
        }
        temp.Status = DWR_STATUS_PENDING;
        ScheduleDB.getDatabase(getApplicationContext()).dwrDao().Update(temp);
    }

    public void updateLastSync(String lastSyncItem) {
        SharedPreferences registered = getApplication().getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = registered.edit();
        editor.putString(lastSyncItem, KTime.ParseNow(KTime.KT_fmtDate3339fk).toString());
        editor.apply();
    }

    public int getRetrySeconds(int retry) {
        return Q_PENDING_SECONDS * retry;
    }

}
