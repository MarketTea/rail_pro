package com.railprosfs.railsapp.service;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.Schedule;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.KTime;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.railprosfs.railsapp.utility.Constants.SP_REG_LAST_ONLINE;
import static com.railprosfs.railsapp.utility.Constants.SP_REG_STORE;

/***
 * This Service Checks Whether or not Internet is available.
 * Whenever the state is changed the service will try to call RefreshIntent
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            if (isOnline(context)) {
                // Try to Run IntentService
                Intent sIntent = new Intent(context, Refresh.class);
                try {
                    context.startService(sIntent);
                    editor.putString(SP_REG_LAST_ONLINE, KTime.ParseNow(KTime.KT_fmtDate3339fk).toString());
                    editor.apply();
                    return;
                } catch (Exception ex) { /* If this fails due to being in background, go with Plan B. */ }
            }
            // Unable to run IntentService
            String past = sp.getString(SP_REG_LAST_ONLINE, "");
            int hours = (int) KTime.CalcDateDifference(past, KTime.ParseNow(KTime.KT_fmtDate3339fk).toString(), KTime.KT_fmtDate3339fk, KTime.KT_HOURS);
            if (hours > 24) {
                Intent resultIntent = new Intent(context, Schedule.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntentWithParentStack(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "default")
                        .setSmallIcon(R.drawable.disconnected)
                        .setContentTitle("Offline for 24+ hours!")
                        .setContentText("Please find an internet source to sync data.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(0, mBuilder.build());
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "Background Task Failure.");
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

}
