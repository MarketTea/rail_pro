package com.railprosfs.railsapp.ui_support;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.railprosfs.railsapp.Settings;
import com.railprosfs.railsapp.data.ScheduleDB;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data_layout.WorkflowTbl;

import java.util.ArrayList;
import java.util.List;

public class ScheduleViewModel extends AndroidViewModel {
    private LiveData<List<WorkflowTbl>> mAllWFTbl;
    private LiveData<List<AssignmentTbl>> mAllJobs;
    private LiveData<List<DwrTbl>> mAllDwrs;
    private LiveData<List<JobSetupTbl>> mAllJobSetups;
    private ScheduleDB db;
    private String userId;

    // We need to access the data outside of LiveDate, so create local variables.
    public ScheduleViewModel (Application application, String userId) {
        super(application);
        String oldest = Settings.getPrefOldestDwr(getApplication().getApplicationContext());
        this.userId = userId;
        db = ScheduleDB.getDatabase(application);
        mAllJobs = db.assignmentDao().GetSchedule(userId);
        mAllDwrs = db.dwrDao().GetReports(userId, oldest);
        mAllJobSetups = db.jobSetupDao().GetJSetups(userId, oldest);
        mAllWFTbl = db.workflowDao().GetAllWorkflowTbl();
    }

    // The LiveData will trigger a screen refresh when the database changes.
    public LiveData<List<AssignmentTbl>> GetAllJobs() { return db.assignmentDao().GetSchedule(userId); }

    public LiveData<List<DwrTbl>> GetmAllDwrs() { return mAllDwrs; }

    public LiveData<List<JobSetupTbl>> GetAllJobSetups() { return mAllJobSetups; }

    public LiveData<List<WorkflowTbl>> GetAllWorkflowTbl() { return mAllWFTbl; }

    // We cache all the DWRs, but only display them in relation to a job.
    public List<DwrTbl> GetDwrsByJob(int jobId) {
        List<DwrTbl> reports = new ArrayList<>();
        if(mAllDwrs.getValue() != null) {
            for (DwrTbl item : mAllDwrs.getValue()) {
                if (item.JobId == jobId) {
                    reports.add(item);
                }
            }
        }
        return reports;
    }

    // We cache all the JobSetups, but only display them in relation to a job.
    public List<JobSetupTbl> GetJobSetupByJob(int jobId) {
        List<JobSetupTbl> reports = new ArrayList<>();
        if(mAllJobSetups.getValue() != null) {
            for (JobSetupTbl item : mAllJobSetups.getValue()) {
                if (item.JobId == jobId) {
                    reports.add(item);
                }
            }
        }
        return reports;
    }

    // A factory is needed to pass in initialization data as part of constructor.
    public static class Factory extends ViewModelProvider.NewInstanceFactory{
        @NonNull
        private final Application mApplication;
        private final String mUserId;

        public Factory(@NonNull Application application,  String userId) {
            mApplication = application;
            mUserId = userId;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T  create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new ScheduleViewModel(mApplication, mUserId);
        }
    }
}
