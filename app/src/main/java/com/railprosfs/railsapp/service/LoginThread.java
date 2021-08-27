package com.railprosfs.railsapp.service;


import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.railprosfs.railsapp.service.WebServiceModels.*;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;
import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  This thread tries to authenticate and acquire a token. Once successful, the token
 *  is passed back to the caller in a message.  If unsuccessful, a return status code
 *  is returned.
 */
public class LoginThread extends Thread {
    private Context LocalContext;
    private LoginRequest Request;
    private Messenger Callback;
    private Gson Parser;

    public LoginThread(Context ctx, LoginRequest request, Gson parser, Messenger callback){
        LocalContext = ctx;
        Request = request;
        Parser = parser;
        Callback = callback;
    }

    @Override
    public void run() {
        try {
            IWebServices ws = new WebServices(Parser);
            if (ws.IsNetwork(LocalContext)) {
                try {
                    LoginResponse response = ws.CallLoginApi(Connection.getInstance().getFullApiPath(Connection.API_POST_LOGIN), Request.userId, Request.password);
                    if (response != null)
                        ReturnOk(response);
                    else
                        ReturnErr(ExpClass.STATUS_NOTFOUND);
                } catch (ExpClass ex) {
                    ReturnErr(ex.Status);
                }
            } else {
                ReturnErr(ExpClass.STATUS_CODE_NETWORK_DOWN);
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
            ReturnErr(ExpClass.STATUS_CODE_UNKNOWN);
        }
    }

    // Return the information.
    private void ReturnOk(LoginResponse data) {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_POST_LOGIN;
            msg.obj = data;
            if (Callback != null) {
                Callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

    // Return an error status.
    private void ReturnErr(int status) {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_LOGIN_ERR;
            msg.arg1 = status;
            if (Callback != null) {
                Callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }
}
