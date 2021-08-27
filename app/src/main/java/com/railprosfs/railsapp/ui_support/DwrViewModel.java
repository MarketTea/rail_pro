package com.railprosfs.railsapp.ui_support;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.railprosfs.railsapp.data.observable.DwrItem;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data.ScheduleDB;

import java.util.List;

public class DwrViewModel extends AndroidViewModel {
    private int mCurrentReport;
    private String mRailRoadCode;
    private int mJobKey;
    private LiveData<AssignmentTbl> mAssignment;
    private LiveData<DwrTbl> mCurrentDwr;
    private ScheduleDB mDatabase;
    public boolean isReloadOK = true;
    public boolean isReloadImageOK = true;
    public int CurrentFormType = 0;
    public List<PictureField> PictureList;
    public DwrItem dwrItem;

    private DwrViewModel(@NonNull Application application, final int dwrId, final int jobId, String property) {
        super(application);
        mDatabase = ScheduleDB.getDatabase(application);
        mCurrentReport = dwrId;
        mCurrentDwr =  mDatabase.dwrDao().GetReport(mCurrentReport);
        mAssignment = mDatabase.assignmentDao().GetAssignmentByJobId(jobId);
        mJobKey = jobId;
        this.dwrItem = new DwrItem();
        mRailRoadCode = property;
    }

    // The LiveData will trigger a screen refresh on first load and when the database changes.
    public LiveData<DwrTbl> GetReport() { return mCurrentDwr; }
    public LiveData<AssignmentTbl> GetByJobId() { return mAssignment; }
    public LiveData<List<DocumentTbl>> GetImages() {return mDatabase.documentDao().GetAllDwrImages(getDwrId()); }
    public LiveData<List<DwrTbl>> GetAllPerDiems(){ return mDatabase.dwrDao().GetPerDiemDwrs(); }
    public LiveData<List<String>> GetSubdivisions() { return mDatabase.railRoadDao().GetSubdivisionByRailRoad(mRailRoadCode); }
    public LiveData<AssignmentTbl> GetJobAssignment() { return mDatabase.assignmentDao().GetAssignmentByJobId(mJobKey); }

    // In case there is data access outside the refresh flow, accessed cached dwr.
    // Note we need to dig into the LiveData value.
    public DwrTbl CurrentDwr() { return mCurrentDwr.getValue(); }

    // A factory is needed to pass in initialization data as part of constructor.
    public static class Factory extends ViewModelProvider.NewInstanceFactory{
        @NonNull
        private final Application mApplication;
        private final int mDwrId;
        private final int mJobId;
        private final String mProperty;

        public Factory(@NonNull Application application, int dwrId, int jobId, String property) {
            mApplication = application;
            mDwrId = dwrId;
            mJobId = jobId;
            mProperty = property;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T  create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new DwrViewModel(mApplication, mDwrId, mJobId, mProperty);
        }
    }

    public int getDwrId() {
        return mCurrentReport;
    }

    public void setDwrId(int id) {
        mCurrentReport = id;
    }

    public String getRailRoadCode() { return mRailRoadCode; }

    public void setRailRoadCode(String property) { mRailRoadCode = property; }

    public int getJobId() { return mJobKey; }

    public void setJobId(int jobId) { mJobKey = jobId; }
}
