package com.railprosfs.railsapp.service;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;

import static com.railprosfs.railsapp.utility.Constants.WHAT_FLASHAUDIT_ERR;
import static com.railprosfs.railsapp.utility.Constants.WHAT_FLASH_AUDIT;

public class FlashAuditSendThread extends Thread {
    private Context ctx;
    private Actor user;
    private Gson parser;
    private Messenger Callback;
    private WebServiceModels.FlashAuditRequest request;

    public FlashAuditSendThread(Context ctx, Gson parser, WebServiceModels.FlashAuditRequest request, Messenger callback) {
        this.ctx = ctx;
        this.parser = parser;
        this.request = request;
        this.user = new Actor(ctx);
        this.Callback = callback;
    }

    @Override
    public void run() {
            IWebServices ws = new WebServices(parser);
            if (ws.IsNetwork(ctx)) {
                try {
                    Connection connection = Connection.getInstance();
                    connection.getBuild(ctx);
                    ws.CallPostApi(Connection.getInstance().getFullApiPath(ctx, Connection.API_POST_SAVE_FLASHAUDIT), request, user.ticket);
                    ReturnOk();
                } catch (ExpClass kx) {
                    ExpClass.LogEXP(kx, user.unique + ":FlashAuditSendThread");
                    ReturnErr(kx.Status);
                } catch (Exception ex) {
                    ExpClass.LogEX(ex, user.unique + ":FlashAuditSendThread-Exception");
                    ReturnErr(7000);
                }
            } else {
                ReturnErr(ExpClass.STATUS_CODE_NETWORK_DOWN);
            }
    }

    // Return the information.
    private void ReturnOk() {
        try {
            Message msg = Message.obtain();
            msg.what = WHAT_FLASH_AUDIT;
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
            msg.what = WHAT_FLASHAUDIT_ERR;
            msg.arg1 = status;
            if (Callback != null) {
                Callback.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }
}
