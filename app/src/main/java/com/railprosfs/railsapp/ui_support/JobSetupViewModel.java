package com.railprosfs.railsapp.ui_support;

import android.app.Application;
import com.railprosfs.railsapp.data_layout.AnswerTbl;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.FieldPlacementTbl;
import com.railprosfs.railsapp.data.observable.JobSetupAnswer;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data.ScheduleDB;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class JobSetupViewModel extends AndroidViewModel {
    private final ScheduleDB mDatabase;
    private int mCurrentTemplate;
    private int mJobId;
    private final int mPropertyId;
    private int mSetupFormType;
    private String mJobNumber;
    public JobSetupTbl jobSetupTbl;
    public boolean editMode;
    public List<JobSetupAnswer> mJobSetupAnswers;

    private JobSetupViewModel(@NonNull Application application, int templateId, int jobId, int propertyId, int formType) {
        super(application);
        mDatabase = ScheduleDB.getDatabase(application);
        mCurrentTemplate = templateId;
        mJobId = jobId;
        mPropertyId = propertyId;
        mSetupFormType = formType;
    }

    // The LiveData will trigger a screen refresh on first load and when the database changes.
    public LiveData<List<FieldPlacementTbl>> getFieldTbl() { return mDatabase.fieldPlacementDao().GetLiveTemplateFields(mCurrentTemplate);}
    public LiveData<AssignmentTbl> getAssignmentTbl() { return mDatabase.assignmentDao().GetAssignmentByJobId(mJobId); }
    public LiveData<JobTbl> getJobListTbl() { return mDatabase.jobDao().GetJobDetail(mJobId); }
    public LiveData<JobSetupTbl> getJobSetupTbl(int mJobSetupId) { return mDatabase.jobSetupDao().GetJobSetupData(mJobSetupId); }
    public LiveData<List<AnswerTbl>> getAnswerListTbl(int fieldId, int fieldType) { return mDatabase.answerDao().GetLiveDataAnswerTbl(fieldId, fieldType); }

    public static class Factory extends ViewModelProvider.NewInstanceFactory{
        @NonNull
        private final Application mApplication;
        private final int mTemplateId;
        private final int mJobIdServer;
        private final int mRailRoadKey;
        private final int mFormType;

        public Factory(@NonNull Application application, int templateId, int jobId, int propertyId, int formType){
            mApplication = application;
            mTemplateId = templateId;
            mJobIdServer = jobId;
            mRailRoadKey = propertyId;
            mFormType = formType;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
        {
            //noinspection unchecked
            return(T) new JobSetupViewModel(mApplication,mTemplateId,mJobIdServer,mRailRoadKey,mFormType);
        }
    }

    public void setTemplateId(int id) {
        mCurrentTemplate = id;
    }

    public int getJobId() { return mJobId; }

    public void setJobId(int value) { mJobId = value;}

    public int getPropertyId() { return mPropertyId; }

    public int getSetupFormType() { return mSetupFormType; }

    public void setSetupFormType(int value) { mSetupFormType = value; }

    public int getJobSetupTblId() { return jobSetupTbl != null ? jobSetupTbl.Id : 0; }

    public void setmJobNumber(String value) { mJobNumber = value; }

    public String getmJobNumber() { return mJobNumber; }
}
