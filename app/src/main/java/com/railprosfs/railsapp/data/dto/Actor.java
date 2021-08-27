package com.railprosfs.railsapp.data.dto;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.microsoft.aad.adal.AuthenticationResult;
import com.railprosfs.railsapp.About;
import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.service.IWebServices;
import com.railprosfs.railsapp.service.WebServiceModels.FieldWorkerWS;
import com.railprosfs.railsapp.service.WebServiceModels.UserResponse;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.KTime;

import java.util.Arrays;
import java.util.List;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 * The Actor is the person using the app.  They have many of the same
 * properties as other people that might get referenced in the app, e.g.
 * name, but have some special functionality as well.  Specifically,
 * their data is retained in the preference DB for easy and quick access.
 */
public class Actor extends Worker {
    // These are items specific to the person using the app.
    public String ticket = "";      // The key used to access web services.
    public String device = "";      // The unique device identifier.
    public String secret = "";      // The login secret.
    public String employeeCode = "";
    public String jobSetupVer = "";
    public long refresh = 0;        // The last (epoch) time this data was refreshed from server.
    public boolean refreshStatus = false;

    /*
     *  Constructors
     */
    public Actor() {
    }

    public Actor(Context ctx) {
        LoadPrime(ctx, null);
    }

    // Helpers

    /*
     * For the primary user, this method loads the relevant data from the local
     * store (shared preferences).  Additionally, it can go to the server, although
     * that should not be used to change local users settings.
     * NOTE: Any use of the network would require the calling party to not be on
     * the main thread when used.
     */
    public void LoadPrime(Context context, IWebServices ws) {

        // LoadPrime the local data.
        SharedPreferences registered = context.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        ticket = registered.getString(SP_REG_TICKET, "");       // Token
        device = registered.getString(SP_REG_DEVICE, "");       //
        secret = registered.getString(SP_REG_SECRET, "");       // Temporary Password (No Longer Used)
        refresh = registered.getLong(SP_REG_UPDATED, 0);         // Refresh Token
        workId = registered.getInt(SP_REG_ID, 0);                // FieldWorker ID Database (Do Not Use to Display)
        userId = registered.getString(SP_REG_USER, "");         // UniqueKey for Login
        unique = registered.getString(SP_REG_UNIQUE, "");       // Email
        display = registered.getString(SP_REG_DISPLAY, "");     // Name
        role = registered.getInt(SP_REG_ROLE, 0);                // Roles
        railroad = registered.getInt(SP_REG_RAIL_PRI, 0);        //
        properties = registered.getString(SP_REG_RAIL_ALL, ""); //
        employeeCode = registered.getString(SP_REG_EMPLOYEE_CODE, "");
        primary = true;
        refreshStatus = false;

        if (ws != null) {
            // LoadPrime the server data, too.
            if (ws.IsNetwork(context)) {
                try {
                    // Basic User Data
                    Connection connection = Connection.getInstance();
                    String holdApiUrl = !connection.isKevin(context) ? connection.getFullApiPath(Connection.API_GET_ADMIN_USER_BY_ID) : connection.getFullApiPath(Connection.API_GET_USER_BY_ID);
                    UserResponse user = ws.CallGetApi(holdApiUrl.replace(SUB_ZZZ, userId), UserResponse.class, ticket);

                    if(user.fieldWorker != null) {
                        workId = CheckFieldId(user.fieldWorker.id, context);
                        employeeCode = user.fieldWorker.employeeCode;
                        display = user.fieldWorker.firstName + " " + user.fieldWorker.lastName;
                        role = ParseRoles(user.roles);

                        // Check the field worker for a team.
                        if (user.fieldWorker.id > 0) {
                            holdApiUrl = connection.getFullApiPath(Connection.API_GET_FIELDWORKER_BY_ID).replace(SUB_ZZZ, String.valueOf(user.fieldWorker.id));
                            FieldWorkerWS fieldworker = ws.CallGetApi(holdApiUrl, FieldWorkerWS.class, ticket);
                            if (fieldworker != null && fieldworker.team != null && fieldworker.team.railroad != null) {
                                int holdRR = Railroads.PropertyKeyServer(context, fieldworker.team.railroad.code);
                                if (holdRR >= 0) railroad = holdRR;
                            }
                        }
                    } else {
                        CheckFieldId(0, context);   // Inform the user something is wrong with their account.
                    }
                    refresh = KTime.GetEpochNow();
                    refreshStatus = true;
                } catch (ExpClass ex) {
                    ExpClass.LogIN(KEVIN_SPEAKS, "STATUS = " + ex.Name + " : " + ex.getMessage());
                }
            } else
                ExpClass.LogIN(KEVIN_SPEAKS, "Actor.LoadPrime Network Unavailable");
        }
    }

    /*
     * For the primary user, this method saves the relevant data to the
     * local store (shared preferences).  Additionally, we put it to the
     * server as well if full=true.
     * NOTE: Any use of the network would require the calling party to not be on
     * the main thread when used. This method is synchronized in case it gets
     * called in multiple places in the code.
     */
    public synchronized void SyncPrime(Context context, boolean authOnly) {

        if (refreshStatus) {
            SharedPreferences registered = context.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = registered.edit();

            // The authOnly flag helps avoid some race conditions.
            if(authOnly) {
                editor.putString(SP_REG_TICKET, ticket);
                editor.putString(SP_REG_USER, userId);
                editor.putString(SP_REG_UNIQUE, cleanUnique());
                editor.apply();
                return;
            }
            // Non-security related data.
            editor.putString(SP_REG_DEVICE, device);
            editor.putString(SP_REG_SECRET, secret);
            editor.putLong(SP_REG_UPDATED, refresh);
            editor.putInt(SP_REG_ID, workId);
            editor.putString(SP_REG_DISPLAY, display);
            editor.putInt(SP_REG_ROLE, role);
            editor.putInt(SP_REG_RAIL_PRI, railroad);
            editor.putString(SP_REG_RAIL_ALL, properties);
            editor.putString(SP_REG_EMPLOYEE_CODE, employeeCode);
            editor.apply();
        }

    }

    // We only care about the highest permission.
    private int ParseRoles(String[] roles) {
        List<String> realRoles = Arrays.asList(roles);
        int bestGuess = -1;
        for (String role : realRoles) {
            int holdRole = 0;
            if (role.equalsIgnoreCase("Admin")) holdRole = 4;
            if (role.equalsIgnoreCase("Mobile-Manager")) holdRole = 3;
            if (role.equalsIgnoreCase("Mobile-RWIC")) holdRole = 1;
            if (holdRole > bestGuess) bestGuess = holdRole;
        }
        return bestGuess;
    }

    private int CheckFieldId(int fieldWorkerId, Context ctx) {
        if (fieldWorkerId == 0) {
            // Notify the user about their weak account.
            Intent resultIntent = new Intent(ctx, About.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, "default")
                    .setSmallIcon(R.drawable.pending)
                    .setContentTitle(ctx.getResources().getString(R.string.title_acct_issue))
                    .setContentText(ctx.getResources().getString(R.string.msg_bad_fieldworker))
                    .setContentIntent(resultPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);


            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
            notificationManager.notify(0, mBuilder.build());
        }

        return fieldWorkerId;
    }

    public void Login(Context ctx, AuthenticationResult authenticationResult) {

        refreshStatus = true;
        ticket = authenticationResult.getAccessToken();
        userId = authenticationResult.getUserInfo().getUserId();
        unique = authenticationResult.getUserInfo().getDisplayableId();
        SyncPrime(ctx, true);
    }

    public void Logout(Context context) {
        SharedPreferences registered = context.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = registered.edit();
        editor.putString(SP_REG_TICKET, "");
        editor.apply();
    }
}
