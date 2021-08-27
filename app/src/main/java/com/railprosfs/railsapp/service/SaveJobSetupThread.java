package com.railprosfs.railsapp.service;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data_layout.AnswerDao;
import com.railprosfs.railsapp.data_layout.AnswerTbl;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data.observable.JobSetupAnswer;
import com.railprosfs.railsapp.data_layout.FieldPlacementTbl;
import com.railprosfs.railsapp.data_layout.JobSetupDao;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data.ScheduleDB;
import com.railprosfs.railsapp.data_layout.WorkflowDao;
import com.railprosfs.railsapp.data_layout.WorkflowTbl;
import com.railprosfs.railsapp.ui_support.JobSetupViewModel;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.railprosfs.railsapp.service.WebServiceModels.*;
import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  Save the Job Setup answers into the local database.  Optionally
 *  add a request to the event queue, to have have the Job Setup sent
 *  to the backend.  If this Job Setup was created without care of an
 *  assignment, a placeholder assignment is create (in the database).
 */
public class SaveJobSetupThread extends Thread {

    public static final boolean SUBMIT = true;
    public static final boolean SAVE = false;
    private static final String PHONE = "Phone";
    private static final String ODDTYPE_POC = "DB_PointOfContactPhone_editable";

    private final Context context;
    private final List<JobSetupAnswer> answers;
    private final boolean submit;
    private final ScheduleDB db;
    private final JobSetupViewModel model;
    private final Messenger callback;
    private final Actor user;

    public SaveJobSetupThread(Context ctx, List<JobSetupAnswer> answer, JobSetupViewModel model, boolean submitToggle, Messenger callback) {
        this.context = ctx;
        this.answers = answer;
        this.submit = submitToggle;
        this.model = model;
        db = ScheduleDB.getDatabase(ctx);
        this.callback = callback;
        this.user = new Actor(ctx);
    }

    /*** State For SaveSubmitJobsetupThread
     * 1. Create New JobSetupTbl for Job or Update Existing JobSetupTbl
     * 2. Create an AnswerResponse for each Question in the AnswerTbl
     * 3. Submit to Queue for Upload onto server if submit is true
     * 4. Start RefreshIntentService to Start Queue
     */
    @Override
    public void run() {
        try {
            updateDatabase();
            if (submit) {
                queueUpload();
            }
            linkAssignment(model.getJobId());
            ReturnOk();
        } catch (Exception ex) {
            ReturnErr();
        }
    }

    /*
        While the cover sheet is managed locally the same way as Job Setup forms,
        the backend has isolated them into a separate set of tables.  That means
        the API is different, so we have a different upload queue for them.
     */
    private void queueUpload() {

        // Add to event queue
        WorkflowDao wfDao = db.workflowDao();
        int formType = Q_TYPE_JOB_SAVE;
        if (model.getSetupFormType() == RP_COVER_SERVICE) { formType = Q_TYPE_COVER_SAVE; }
        if (model.getSetupFormType() == RP_UTILITY_SERVICE) { formType = Q_TYPE_UTIL_JOB_SAVE; }
        WorkflowTbl wfTbl = wfDao.GetWorkflowTblUniqueKeyAndType(model.jobSetupTbl.Id, formType);
        if (wfTbl == null) {
            wfTbl = new WorkflowTbl();
            wfTbl.Pending = KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
            wfTbl.Priority = HIGH_PRIORITY_EVENT;
            wfTbl.EventType = formType;
            wfTbl.EventKey = model.jobSetupTbl.Id;
            wfTbl.Retry = 0;
            wfTbl.RetryMax = 6;
            wfTbl.CreateDate = KTime.ParseNow(KTime.KT_fmtDate3339k).toString();
            long id = wfDao.Insert(wfTbl);
            /* Work Flow Table Upload Success, start Jobsetup display of queueing up */
            if(id > 0) {
                JobSetupTbl tbl = db.jobSetupDao().GetJobSetup(model.jobSetupTbl.Id);
                if (tbl != null) {
                    tbl.Status = DWR_STATUS_QUEUED;
                    db.jobSetupDao().Update(tbl);
                }
            }
        }

        // Update to Database
        Intent sIntent = new Intent(context, Refresh.class);
        sIntent.putExtra(REFRESH_LOOP, true);
        context.startService(sIntent);
    }


    /** 1/2 Creates New Jobsetup and Answer Response **/
    private void updateDatabase() {
        //Update Insert JobSetupTbl
        if (model.jobSetupTbl != null) {
            updateJobSetup();
        } else {
            createJobSetup();
        }
        createAnswerSetup();
    }

    /** 1 Update JobSetupTbl ***/
    private void updateJobSetup() {
        model.jobSetupTbl.Status = DWR_STATUS_DRAFT;
        db.jobSetupDao().Update(model.jobSetupTbl);
    }

    /*** 1 Create JobSetupTbl ***/
    private void createJobSetup() {
        JobSetupDao jsDao = db.jobSetupDao();
        model.jobSetupTbl = new JobSetupTbl();
        JobSetupTbl jsTbl = model.jobSetupTbl;
        jsTbl.AssignmentId = model.getSetupFormType(); // This value indicates the exact type of form, e.g. flagging js, utility js, utility cover sheet.
        jsTbl.JobId = model.getJobId();
        jsTbl.RailRoadId = model.getPropertyId();
        jsTbl.CreateDate = KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
        jsTbl.UserId = user.unique;

        //Check for Save or Submit to Update Status
        jsTbl.Status = DWR_STATUS_DRAFT;
        model.jobSetupTbl.Id = (int) jsDao.Insert(model.jobSetupTbl);
    }

    /** 2 Create Answer Response **/
    private void createAnswerSetup() {
        AnswerDao ansDao = db.answerDao();
        for (JobSetupAnswer response : answers) {
            AnswerTbl ans = new AnswerTbl();
            ans.Id = 0;
            ans.FieldId = model.jobSetupTbl.Id;
            ans.FieldType = FIELD_JSF_TYPE;
            ans.QuestionId = response.getAnswerID();
            ans.CommentResponse = Functions.DefaultForNull(response.getUserInput());
            ans.yesNo = response.isYesNo();
            ans.date = Functions.DefaultForNull(response.getDate());
            ans.signatureFileName = Functions.DefaultForNull(response.getSignatureFileName());
            FieldPlacementTbl fieldPlacementTbl = ScheduleDB.getDatabase(context).fieldPlacementDao().GetFieldByFieldId(ans.QuestionId);

            if(response.specialQuestionID < 0) {
                ans.QuestionId = response.specialQuestionID;
            }

            if (ansDao.GetAnswer(model.jobSetupTbl.Id, FIELD_JSF_TYPE, response.getAnswerID()) != null) {
                AnswerTbl old = ansDao.GetAnswer(model.jobSetupTbl.Id, FIELD_JSF_TYPE, response.getAnswerID());
                if(fieldPlacementTbl != null && (fieldPlacementTbl.FieldType.equals(PHONE) || fieldPlacementTbl.FieldType.equals(ODDTYPE_POC))) {
                    if(ans != null && ans.CommentResponse != null) {
                        ans.CommentResponse = ans.CommentResponse.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
                    }
                }
                ans.Id = old.Id;
                ans.ServerId = old.ServerId;
                ansDao.Update(ans);
            } else if(ansDao.GetAnswer(model.jobSetupTbl.Id, FIELD_JSF_TYPE, response.specialQuestionID) != null) {
                AnswerTbl old = ansDao.GetAnswer(model.jobSetupTbl.Id, FIELD_JSF_TYPE, response.specialQuestionID);
                if(fieldPlacementTbl != null && (fieldPlacementTbl.FieldType.equals(PHONE) || fieldPlacementTbl.FieldType.equals(ODDTYPE_POC))) {
                    if(ans != null && ans.CommentResponse != null) {
                        ans.CommentResponse = ans.CommentResponse.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
                    }
                }
                ans.Id = old.Id;
                ans.ServerId = old.ServerId;
                ansDao.Update(ans);
            } else {
                ansDao.Insert(ans);
            }
        }
    }

    // If there is no assignment yet, create a placeholder.
    private void linkAssignment(int jobid) {
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
                // If ad-hoc form is not default flagging job setup, make assignment a utility.
                if (model.getSetupFormType() != RP_FLAGGING_SERVICE) {
                    BaseIdWS holdST = new BaseIdWS();
                    holdST.id = RP_UTILITY_SERVICE;
                    shift.serviceType = holdST;
                }

                // Make the dates match today.
                String workDate = KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), true).toString();
                workDate = workDate.substring(0,11) + MIDNIGHT + "Z"; // Use the backend format.
                shift.day = workDate;
                workItem.start = workDate;
                workItem.end = workDate;

                AssignmentTbl action = Functions.BuildAssignment(user.unique, model.getPropertyId(), jobResponse, Arrays.asList(costResponse), workItem, shift);
                ScheduleDB.getDatabase(context).assignmentDao().Insert(action);
            }
        } catch (Exception ex) { ExpClass.LogEX(ex, "linkToAssignment");
        }
    }

    // Return an error status.
    private void ReturnOk() {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_JOB_SETUP;
            if (callback != null) {
                callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

    // Return an error status.
    private void ReturnErr() {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_JOB_SETUP_ERR;
            if (callback != null) {
                callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }
}
