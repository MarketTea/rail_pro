package com.railprosfs.railsapp.service;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 * This class is a thread that can retrieve and return job data.
 */
public class JobOneThread extends Thread {
    private Messenger Callback;
    private int JobId;
    private Context Ctx;
    private IWebServices ApiService;

    public JobOneThread(Context ctx, int jobid, Messenger message) {
        this.Ctx = ctx;
        this.JobId = jobid;
        this.Callback = message;
        this.ApiService = new WebServices(new Gson());
    }

    @Override
    public void run() {
        try {
            Actor actor = new Actor(Ctx);
            Connection connection = Connection.getInstance();
            String jobPath = connection.getFullApiPath(Connection.API_GET_JOB_BY_ID).replace(SUB_ZZZ, String.valueOf(JobId));
            WebServiceModels.JobDetailResponse jobResponse = ApiService.CallGetApi(jobPath, WebServiceModels.JobDetailResponse.class, actor.ticket);
            // The first thing we need is restrictions, if need more later (like supervisor), expand the returned data.
            ReturnOK(jobResponse);
        } catch (Exception e) {
            ReturnErr();
        }
    }

    private void ReturnOK(WebServiceModels.JobDetailResponse jobResponse) {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_JOB_ONE;
            msg.obj = jobResponse;
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
            msg.what = WHAT_JOB_ONE_ERR;
            if (Callback != null) {
                Callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

}
