package com.railprosfs.railsapp.data;

import android.content.Context;
import android.text.format.DateFormat;

import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.service.WebServiceModels;
import com.railprosfs.railsapp.utility.KTime;

import java.util.Calendar;

public class JobSetupDwrUpdateMgr {
    private Context ctx;

    public JobSetupDwrUpdateMgr(Context ctx) {
        this.ctx = ctx;
    }

    public WebServiceModels.ListRequest getRequest() {
        Actor user = new Actor(ctx);
        WebServiceModels.ListRequest dlr = new WebServiceModels.ListRequest();
        dlr.fieldWorkerId = user.workId;
        Calendar timeNow = KTime.ConvertTimezone(Calendar.getInstance(), KTime.UTC_TIMEZONE);
        timeNow.add(Calendar.DATE, 30);
        String futureTime = DateFormat.format(KTime.KT_fmtDate3339k, timeNow).toString();
        timeNow.add(Calendar.DATE, -301);
        String pastTime = DateFormat.format(KTime.KT_fmtDate3339k, timeNow).toString();
        dlr.end = futureTime;
        dlr.start = pastTime;
        return dlr;
    }
}
