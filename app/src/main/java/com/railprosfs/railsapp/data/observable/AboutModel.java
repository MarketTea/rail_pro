package com.railprosfs.railsapp.data.observable;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.railprosfs.railsapp.BR;
import com.railprosfs.railsapp.BuildConfig;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.service.IWebServices;
import com.railprosfs.railsapp.service.WebServices;
import com.railprosfs.railsapp.utility.Connection;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import static com.railprosfs.railsapp.utility.Constants.*;

public class AboutModel extends RRBaseObservable {
    public String internetConnected;
    public String date;
    public String version;
    public String buildType;
    public String user;
    public String fieldWorkerId;
    public String apiStatus;
    public String apiUrlBase;
    public String name;
    public String lastSyncTemplate;
    public String lastSyncJobForm;
    public String lastSyncDWRQuestion;
    public String lastSyncRailRoad;
    public String lastSyncAssignment;
    public String lastSyncDocument;
    public String lastSyncEventQueDwr;
    public String lastSyncEventQueJob;
    public String lastSyncDwrStatus;
    public String lastSyncJobStatus;


    private class CheckAPIStatus extends AsyncTask<Void, Void, Void> {
        Context ctx;
        public CheckAPIStatus(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            IWebServices ws = new WebServices(API_TIMEOUT_SHORT, new Gson());
            try {
                Actor user = new Actor(ctx);
                user.LoadPrime(ctx, ws);
                if(user.refreshStatus)  {
                    fieldWorkerId = "" + user.employeeCode;
                    setName(user.display);
                    setApiStatus("Available");
                } else {
                    setApiStatus("Unavailable");
                }
            } catch(Exception e) {
                setApiStatus("Unavailable");
            }

            return null;
        }
    }

    public AboutModel(Context ctx) {
        updateInternetConnected(ctx);
        updateVersion(ctx);
        updateBuildType(ctx);
        updateUser(ctx);
        updateAPIStatus(ctx);
        updateSyncStatus(ctx);
    }

    private void updateSyncStatus(Context ctx) {
        SharedPreferences registered = ctx.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        setLastSyncTemplate(registered.getString(SP_LS_TEMPLATE, "UNKNOWN"));
        setLastSyncJobForm(registered.getString(SP_LS_JOBFORM, "UNKNOWN"));
        setLastSyncDWRQuestion(registered.getString(SP_LS_DWRQUESTION, "UNKNOWN"));
        setLastSyncRailRoad(registered.getString(SP_LS_RAILROAD, "UNKNOWN"));
        setLastSyncAssignment(registered.getString(SP_LS_ASSIGNMENT, "UNKNOWN"));
        setLastSyncDocument(registered.getString(SP_LS_DOCUMENT, "UNKNOWN"));
        setLastSyncEventQueDwr(registered.getString(SP_LS_EVENTQUE_DWR, "UNKNOWN"));
        setLastSyncEventQueJob(registered.getString(SP_LS_EVENTQUE_JOB, "UNKNOWN"));
        setLastSyncDwrStatus(registered.getString(SP_LS_DWRSTATUS, "UNKNOWN"));
        setLastSyncJobStatus(registered.getString(SP_LS_JOBSTATUS, "UNKNOWN"));
    }

    @Bindable
    public String getName() {
        if(name == null) {
            return "";
        }
        return "(" + name + ")";
    }

    public AboutModel setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
        return this;
    }

    @Bindable
    public String getApiStatus() {
        if(apiStatus == null) {
            apiStatus = "Connecting...";
        }
        return apiStatus;
    }

    public AboutModel setApiStatus(String apiStatus) {
        this.apiStatus = apiStatus;
        notifyPropertyChanged(BR.apiStatus);
        return this;
    }

    @Bindable
    public String getApiUrlBase() {
        return Connection.getInstance().build == null ? "" : Connection.getInstance().build;
    }

    public AboutModel setApiUrlBase(String apiUrlBase) {
        this.apiUrlBase = apiUrlBase;
        notifyPropertyChanged(BR.apiUrlBase);
        return this;
    }

    private void updateInternetConnected(Context ctx) {
        internetConnected = isNetworkConnected(ctx) ? "Connected" : "Disconnected";
    }

    private void updateUser(Context ctx) {
        Actor actor = new Actor(ctx);
        user = actor.userId;
        fieldWorkerId = actor.employeeCode;
    }

    private void updateBuildType(Context ctx) {
        buildType = Connection.getInstance().getBuildType(ctx);
    }

    private void updateVersion(Context ctx) {
        try {
            version = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateAPIStatus(Context ctx) {
        new CheckAPIStatus(ctx).execute();
    }

    private boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Bindable
    public String getInternetConnected() {
        return internetConnected;
    }

    public AboutModel setInternetConnected(String internetConnected) {
        this.internetConnected = internetConnected;
        notifyPropertyChanged(BR.internetConnected);
        return this;
    }

    @Bindable
    public String getDate() {
        return BuildConfig.BUILDDATE;
    }

    public AboutModel setDate(String date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
        return this;
    }

    @Bindable
    public String getVersion() {
        return version;
    }

    public AboutModel setVersion(String version) {
        this.version = version;
        notifyPropertyChanged(BR.version);
        return this;
    }

    @Bindable
    public String getBuildType() {
        return buildType;
    }

    public AboutModel setBuildType(String buildType) {
        this.buildType = buildType;
        notifyPropertyChanged(BR.buildType);
        return this;
    }

    @Bindable
    public String getUser() {
        return user;
    }

    public AboutModel setUser(String user) {
        this.user = user;
        notifyPropertyChanged(BR.user);
        return this;
    }

    @Bindable
    public String getFieldWorkerId() {
        return fieldWorkerId;
    }

    public AboutModel setFieldWorkerId(String fieldWorkerId) {
        this.fieldWorkerId = fieldWorkerId;
        notifyPropertyChanged(BR.fieldWorkerId);
        return this;
    }

    @Bindable
    public String getLastSyncTemplate() {
        return lastSyncTemplate;
    }

    public AboutModel setLastSyncTemplate(String lastSyncTemplate) {
        this.lastSyncTemplate = lastSyncTemplate;
        notifyPropertyChanged(BR.lastSyncTemplate);
        return this;
    }

    @Bindable
    public String getLastSyncJobForm() {
        return lastSyncJobForm;
    }

    public AboutModel setLastSyncJobForm(String lastSyncJobForm) {
        this.lastSyncJobForm = lastSyncJobForm;
        notifyPropertyChanged(BR.lastSyncJobForm);
        return this;
    }

    @Bindable
    public String getLastSyncDWRQuestion() {
        return lastSyncDWRQuestion;
    }

    public AboutModel setLastSyncDWRQuestion(String lastSyncDWRQuestion) {
        this.lastSyncDWRQuestion = lastSyncDWRQuestion;
        notifyPropertyChanged(BR.lastSyncDWRQuestion);
        return this;
    }

    @Bindable
    public String getLastSyncRailRoad() {
        return lastSyncRailRoad;
    }

    public AboutModel setLastSyncRailRoad(String lastSyncRailRoad) {
        this.lastSyncRailRoad = lastSyncRailRoad;
        notifyPropertyChanged(BR.lastSyncRailRoad);
        return this;
    }

    @Bindable
    public String getLastSyncAssignment() {
        return lastSyncAssignment;
    }

    public AboutModel setLastSyncAssignment(String lastSyncAssignment) {
        this.lastSyncAssignment = lastSyncAssignment;
        notifyPropertyChanged(BR.lastSyncAssignment);
        return this;
    }

    @Bindable
    public String getLastSyncDocument() {
        return lastSyncDocument;
    }

    public AboutModel setLastSyncDocument(String lastSyncDocument) {
        this.lastSyncDocument = lastSyncDocument;
        notifyPropertyChanged(BR.lastSyncDocument);
        return this;
    }

    @Bindable
    public String getLastSyncEventQueDwr() {
        return lastSyncEventQueDwr;
    }

    public AboutModel setLastSyncEventQueDwr(String lastSyncEventQueDwr) {
        this.lastSyncEventQueDwr = lastSyncEventQueDwr;
        notifyPropertyChanged(BR.lastSyncEventQueDwr);
        return this;
    }

    @Bindable
    public String getLastSyncEventQueJob() {
        return lastSyncEventQueJob;
    }

    public AboutModel setLastSyncEventQueJob(String lastSyncEventQueJob) {
        this.lastSyncEventQueJob = lastSyncEventQueJob;
        notifyPropertyChanged(BR.lastSyncEventQueJob);
        return this;
    }

    @Bindable
    public String getLastSyncDwrStatus() {
        return lastSyncDwrStatus;
    }

    public AboutModel setLastSyncDwrStatus(String lastSyncDwrStatus) {
        this.lastSyncDwrStatus = lastSyncDwrStatus;
        notifyPropertyChanged(BR.lastSyncDwrStatus);
        return this;
    }

    @Bindable
    public String getLastSyncJobStatus() {
        return lastSyncJobStatus;
    }

    public AboutModel setLastSyncJobStatus(String lastSyncJobStatus) {
        this.lastSyncJobStatus = lastSyncJobStatus;
        notifyPropertyChanged(BR.lastSyncJobStatus);
        return this;
    }
}

