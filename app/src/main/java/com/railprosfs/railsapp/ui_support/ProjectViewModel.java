package com.railprosfs.railsapp.ui_support;

import android.app.Application;

import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data.ScheduleDB;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ProjectViewModel extends AndroidViewModel {
    private int mCurrentProject = 0;
    private ScheduleDB mDatabase;

    // We are only using LiveData, so just have the queries happend in the methods.
    public ProjectViewModel (@NonNull Application application, final int jobId) {
        super(application);
        mDatabase = ScheduleDB.getDatabase(application);
        mCurrentProject = jobId;
    }

    // The LiveData will trigger a screen refresh when the database changes.
    public LiveData<AssignmentTbl> GetJob() {
        return mDatabase.assignmentDao().GetAssignmentByJobId(mCurrentProject);
    }

    public LiveData<List<DwrTbl>> GetDwrs() {
        return mDatabase.dwrDao().GetProjectDwrs(mCurrentProject);
    }

    public LiveData<List<JobSetupTbl>> GetAllJobSetups() {
        return mDatabase.jobSetupDao().GetJobSetupByJobId(mCurrentProject);
    }

    public LiveData<List<DocumentTbl>> GetDocs() {
        return mDatabase.documentDao().GetDocsByJob(mCurrentProject);
    }

    // A factory is needed to pass in initialization data as part of constructor.
    public static class Factory extends ViewModelProvider.NewInstanceFactory{
        @NonNull
        private final Application mApplication;
        private final int mJobId;

        public Factory(@NonNull Application application,  int jobId) {
            mApplication = application;
            mJobId = jobId;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T  create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new ProjectViewModel(mApplication, mJobId);
        }
    }

}
