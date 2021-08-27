package com.railprosfs.railsapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data.ScheduleDB;
import com.railprosfs.railsapp.dialog.YesNoConfirmDialog;
import com.railprosfs.railsapp.service.Refresh;
import com.railprosfs.railsapp.service.WebServices;
import com.railprosfs.railsapp.ui_support.IYesNoConfirmResponse;
import com.railprosfs.railsapp.utility.Connection;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static com.railprosfs.railsapp.utility.Constants.*;

public class AuthenticationSplashScreen extends AppCompatActivity implements IYesNoConfirmResponse {

    private ProgressBar progressBar;
    private TextView progressText;

    /* Azure AD Variables */
    private AuthenticationContext mAuthContext;
    private AuthenticationResult mAuthResult;
    private Actor user;
    private Connection connection;
    /* Handler to do an interactive sign in and acquire token */
    private Handler mAcquireTokenHandler;
    /* Boolean variable to ensure invocation of interactive sign-in only once in case of multiple  acquireTokenSilent call failures */
    private AtomicBoolean sIntSignInInvoked = new AtomicBoolean();
    /* Constant to send message to the mAcquireTokenHandler to do acquire token with Prompt.Auto*/
    private static final int MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO = 1;
    /* Constant to send message to the mAcquireTokenHandler to do acquire token with Prompt.Always */
    private static final int MSG_INTERACTIVE_SIGN_IN_PROMPT_ALWAYS = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        user = new Actor(this);
        progressText = findViewById(R.id.progresstext);
        progressBar = findViewById(R.id.progressbar);

        CheckBuildEnvironmentChange();
        // LogoutCheck();
        // Login();
    }

    private void LogoutCheck() {
        // If this is a logout request, clear the ticket.
        if (getIntent().getData() != null) {
            if (getIntent().getData().getPath() != null) {
                if (getIntent().getData().getPath().contains(REQUEST_LOGOUT)) {
                    Logout();

                }
            }
        }
    }

    private void Logout() {
        //Delete Database
        new deleteDatabaseInfoAsyncTask(ScheduleDB.getDatabase(getApplicationContext())).execute();

        AuthenticationContext authContext = new AuthenticationContext(this, connection.getOauthTenet(this), false);
        authContext.getCache().removeAll();
        user.Logout(this);

    }

    /**
     * Checks For What Type of Build To Use
     * QA and Dev can be toggled on and off
     * PROD is final and should not be changed when it is on this build.
     */
    private void CheckBuildEnvironmentChange() {
        connection = Connection.getInstance();
        connection.setBuildEnvironment(this, connection.getBuild(this));
        Intent intent = getIntent();
        Uri data = intent.getData();

        //Check null and Other Non-Togglable Builds
        if (data == null) {
            if(!connection.getBuild(this).equals(connection.getDefaultBuild())) {
                confirmLoginRequest(R.string.title_confirm_Sandbox, R.string.msg_confirm_sandbox_mode, true);
            } else {
                LogoutCheck();
                Login();
            }
            return;
        }

        //Check Other Togglable Builds
        String build = data.getPath().toUpperCase();
        if (build.contains("PROD_QA") && !connection.getBuild(this).equals(Connection.BUILD_PROD_QA)) {
            connection.setBuildEnvironment(this, Connection.BUILD_PROD_QA);
            Logout();
        } else if (build.contains("PROD") && !connection.getBuild(this).equals(Connection.BUILD_PROD)) {
            connection.setBuildEnvironment(this, Connection.BUILD_PROD);
            Logout();
        } else if (build.contains("QA") && !connection.getBuild(this).equals(Connection.BUILD_QA)) {
            connection.setBuildEnvironment(this, Connection.BUILD_QA);
            Logout();
        } else if (build.contains("DEV") && !connection.getBuild(this).equals(Connection.BUILD_DEV)) {
            connection.setBuildEnvironment(this, Connection.BUILD_DEV);
            Logout();
        } else if (build.contains("KEVIN") && !connection.getBuild(this).equals(Connection.BUILD_KEVIN)) {
            connection.setBuildEnvironment(this, Connection.BUILD_KEVIN);
        } else if (build.contains("JESSE") && !connection.getBuild(this).equals(Connection.BUILD_JESSE)) {
            connection.setBuildEnvironment(this, Connection.BUILD_JESSE);
        }
        LogoutCheck();
        Login();
    }

    private void Login() {
        if(!connection.isKevin(this)) {
            // Turning off the sharted preference storage in this example.  Doc recommend always setting validateAuthority to false.
            mAuthContext = new AuthenticationContext(getApplicationContext(), connection.getOauthTenet(this), false);
            mAcquireTokenHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (sIntSignInInvoked.compareAndSet(false, true)) {
                        Actor user = new Actor(getActivity());
                        if (msg.what == MSG_INTERACTIVE_SIGN_IN_PROMPT_ALWAYS || user.ticket == null || user.ticket.length() == 0) {
                            mAuthContext.acquireToken(getActivity(), connection.getOauthResourceId(getActivity()), connection.getOauthClientId(getActivity()), connection.getOauthRedirectUri(getActivity()), PromptBehavior.Always, getAuthInteractiveCallback());
                        } else if (msg.what == MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO) {
                            mAuthContext.acquireToken(getActivity(), connection.getOauthResourceId(getActivity()), connection.getOauthClientId(getActivity()), connection.getOauthRedirectUri(getActivity()), PromptBehavior.Auto, getAuthInteractiveCallback());
                        }
                    }
                }
            };
        }

        //Runs Either Azure or Kevin's Box
        if (new WebServices(new Gson()).IsNetwork(this)) {
            if (!connection.isKevin(this)) {
                mAcquireTokenHandler.sendEmptyMessage(MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO);
            } else {
                Intent intent = new Intent(this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        } else {
            launchSchedule();
        }
    }

    /* Callback used for interactive request.  If succeeds we use the access
     * token to call the Microsoft Graph. Does not check cache
     */
    private AuthenticationCallback<AuthenticationResult> getAuthInteractiveCallback() {
        return new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(final AuthenticationResult authenticationResult) {
                if (authenticationResult == null || TextUtils.isEmpty(authenticationResult.getAccessToken())
                        || authenticationResult.getStatus() != AuthenticationResult.AuthenticationStatus.Succeeded) {
                    Log.e(Login.class.getSimpleName(), "Authentication Result is invalid");
                    return;
                }

                //Get Permissions
                getPermissions();

                //Set New Info
                Actor userOld = new Actor(getApplicationContext());
                userOld.Login(getApplication(), authenticationResult);

                Intent refresh = new Intent(getActivity(), Refresh.class);
                refresh.setData(getIntent().getData());
                refresh.putExtra(REFRESH_MAIN, true);
                refresh.putExtra(REFRESH_LOOP, false);
                refresh.putExtra(REFRESH_RECIEVER, new RefreshReceiver(new Handler()));
                startService(refresh);
            }

            @Override
            public void onError(Exception exception) {
                /* Failed to acquireToken */
                Log.e(Login.class.getSimpleName(), "Authentication failed: " + exception.toString());
                if (exception instanceof AuthenticationException) {
                    ADALError error = ((AuthenticationException) exception).getCode();
                    if (error == ADALError.AUTH_FAILED_CANCELLED) {
                        Log.e(Login.class.getSimpleName(), "The user cancelled the authorization request");
                    } else if (error == ADALError.NO_NETWORK_CONNECTION_POWER_OPTIMIZATION) {
                        /* Device is in Doze mode or App is in stand by mode.
                           Wake up the app or show an appropriate prompt for the user to take action
                           More information on this : https://github.com/AzureAD/azure-activedirectory-library-for-android/wiki/Handle-Doze-and-App-Standby */
                        Log.e(Login.class.getSimpleName(), "Device is in doze mode or the app is in standby mode");
                    }
                }

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthContext.onActivityResult(requestCode, resultCode, data);
    }

    public Activity getActivity() {
        return this;
    }

    public void getPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED | ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    0);
        }

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        }
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void confirmLoginRequest(int title, int message, boolean showCancel){
        // Set up the fragment.
        FragmentManager mgr = null;
        try {
            mgr = this.getSupportFragmentManager();
        } catch (Exception ex) { return; }
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_CONFIRM_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the confirmation.
        DialogFragment submitFrag = new YesNoConfirmDialog(title, message, !showCancel);
        submitFrag.show(mgr, KY_SIMPLE_CONFIRM_FRAG);
    }

    @Override
    public void yesResponse(int message) {
        switch (message) {
            case R.string.msg_confirm_sandbox_mode:
                // Change Environment
                Connection connection = Connection.getInstance();
                connection.setBuildEnvironment(getApplicationContext(), connection.getDefaultBuild());
                LogoutCheck();
                Login();
                break;
        }
    }

    @Override
    public void noResponse(int message) {
        switch (message) {
            case R.string.msg_confirm_sandbox_mode:
                LogoutCheck();
                Login();
                break;
        }
    }

    private class RefreshReceiver extends ResultReceiver {

        public RefreshReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == REFRESH_CONST) {
                int progress = resultData.getInt(REFRESH_PROGRESS);
                String description = resultData.getString(REFRESH_PROGRESS_DESCRIPTION);

                // pd variable represents your ProgressDialog
                progressBar.setProgress(progress);
                if (description != null) {
                    progressText.setText(description);
                }
                if (progress == 100) {
                    launchSchedule();
                }
            }
        }
    }

    private void launchSchedule() {
        Intent intent = new Intent(getApplicationContext(), Schedule.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private static class deleteDatabaseInfoAsyncTask extends AsyncTask<Void, Void, Void> {
        private ScheduleDB db;

        deleteDatabaseInfoAsyncTask(ScheduleDB db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            db.clearAllTables();
            return null;
        }
    }
}
