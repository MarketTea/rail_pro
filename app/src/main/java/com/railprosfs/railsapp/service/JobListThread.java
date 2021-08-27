package com.railprosfs.railsapp.service;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data_layout.JobDao;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data.ScheduleDB;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.Constants;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;

import java.util.Calendar;

import static com.railprosfs.railsapp.utility.Constants.*;

public class JobListThread extends Thread {
    private Context ctx;
    private Messenger Callback;
    private ScheduleDB mDatabase;
    private IWebServices ApiService;

    public JobListThread(Context ctx, ScheduleDB mDatabase, Messenger message) {
        this.ctx = ctx;
        this.mDatabase = mDatabase;
        this.Callback = message;
        this.ApiService = new WebServices(new Gson());
    }

    /**
     * Gets a List of all the Jobs and puts each entry in the JobTbl
     */
    @Override
    public void run() {
        try {
            Actor actor = new Actor(ctx);
            Connection connection = Connection.getInstance();
            WebServiceModels.JobsRequest request = new WebServiceModels.JobsRequest();
            WebServiceModels.Jobs jobs = ApiService.CallPostApi(connection.getFullApiPath(Connection.API_POST_JOB_LIST), request, WebServiceModels.Jobs.class, actor.ticket);

            if (jobs != null) {
                updateJobDB(jobs);
                ReturnOK();
            }
        } catch (ExpClass kx) {
            ExpClass.LogEXP(kx, "JobTbl Refresh");
            ReturnErr();
        } catch (Exception e) {
            ReturnErr();
        }
    }

    // The API will return nulls pretty much anytime for anything, so be cautious.
    private void updateJobDB(WebServiceModels.Jobs jobs) {
        JobDao jDao = mDatabase.jobDao();
        JobTbl jobTbl;
        for(WebServiceModels.Results results : jobs.results) {
            jobTbl = new JobTbl();
            jobTbl.Id = results.id;
            jobTbl.JobNumber = Functions.DefaultForNull(results.number);
            jobTbl.Description = Functions.DefaultForNull(results.description);
            jobTbl.Status = Functions.DefaultForNull(results.status);
            jobTbl.CustomerId = results.customer != null ? results.customer.id : 0;
            jobTbl.CustomerName = results.customer != null ? Functions.DefaultForNull(results.customer.name) : "";
            jobTbl.RailRoadId = results.railroad != null ? results.railroad.id : 0;
            jobTbl.RailRoadCode = results.railroad != null ? Functions.DefaultForNull(results.railroad.code) : "";
            jobTbl.RailRoadName = results.railroad != null ? Functions.DefaultForNull(results.railroad.name) : "";
            jobTbl.StartTime = Functions.DefaultForNull(results.start);
            jobTbl.EndTime = Functions.DefaultForNull(results.end);
            try {
                // We depend on there being an end date, so lets make sure there is one.
                Calendar holdTS = KTime.ParseToCalendar(jobTbl.EndTime, KTime.KT_fmtDateOnlyRPFS);
            } catch (ExpParseToCalendar expParseToCalendar) {
                jobTbl.EndTime = KTime.ParseNow(KTime.KT_fmtDate3339).toString();
            }

            // Make sure the job is worth keeping around.
            if(jobValid(jobTbl)){
                JobTbl localJob = jDao.GetJob(jobTbl.Id);
                if(localJob != null) {
                    // Only update if the job has changed.
                    if(!jobMatch(jobTbl, localJob)){
                        jDao.Update(jobTbl);
                    }
                } else {
                    jDao.Insert(jobTbl);
                }
            }
        }
    }

    // Check that this is a job worth keeping on the list.
    private boolean jobValid(JobTbl job){
        return (job.JobNumber.length() > 0 && job.RailRoadCode.length() > 0
                && (job.Status.equalsIgnoreCase(Constants.JOB_STATUS_APPROVED)
                || job.Status.equalsIgnoreCase(Constants.JOB_STATUS_COMPLETED)
                || job.Status.equalsIgnoreCase(Constants.JOB_STATUS_INPRROGRESS)));
    }

    // Check that fields we care about match.
    private boolean jobMatch(JobTbl job, JobTbl other){
        if(job == null) return false;
        if(other == null) return false;
        return(job.JobNumber.equalsIgnoreCase(other.JobNumber)
            && job.Description.equalsIgnoreCase(other.Description)
            && job.Status.equalsIgnoreCase(other.Status)
            && job.EndTime.equalsIgnoreCase(other.EndTime)
            && job.RailRoadCode.equalsIgnoreCase(other.RailRoadCode));
    }

    private void ReturnOK() {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_JOB_LIST;
            if (Callback != null) {
                Callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

    private void ReturnErr() {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_JOB_LIST_ERR;
            if (Callback != null) {
                Callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

}
