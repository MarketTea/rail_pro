package com.railprosfs.railsapp.service;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.Functions;

import java.util.Arrays;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 * This class is a thread that can retrieve and return a set of cost center data
 * for a specfic job.  The data is compacted into a single string per a common
 * parser for use by the caller.
 */
public class CostCenterThread extends Thread {
    private Messenger Callback;
    private int JobId;
    private Context Ctx;
    private IWebServices ApiService;

    public CostCenterThread(Context ctx, int jobid, Messenger message) {
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
            String costPath = connection.getFullApiPath(Connection.API_GET_JOB_COSTCENTERS).replace(SUB_ZZZ, String.valueOf(JobId));
            WebServiceModels.JobCostCenter[] costResponse = ApiService.CallGetApi(costPath, WebServiceModels.JobCostCenter[].class, actor.ticket);
            String holdCostCenters = Functions.CompactCostCenters(Arrays.asList(costResponse));
            ReturnOK(holdCostCenters);
        } catch (Exception e) {
            ReturnErr();
        }
    }

    private void ReturnOK(String cargo) {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_COSTCENTERS;
            msg.obj = cargo;
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
            msg.what = WHAT_COSTCENTER_ERR;
            if (Callback != null) {
                Callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

}
