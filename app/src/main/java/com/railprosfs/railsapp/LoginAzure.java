package com.railprosfs.railsapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.service.WebServices;
import com.railprosfs.railsapp.utility.Connection;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoginAzure extends AppCompatActivity {    /* Azure AD Variables */
    private AuthenticationContext mAuthContext;
    private AuthenticationResult mAuthResult;
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
        connection = Connection.getInstance();
        // Turning off the sharted preference storage in this example.  Doc recommend always setting validateAuthority to false.
        if(!connection.isKevin(this)) {
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if Expired
        if(!connection.isKevin(this)) {
            callLogin();
        }

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
                /* Successfully got a token, call graph now */
                getPermissions();


                //TODO: TOKEN HERE authenticationResult.geteAccessToken();
                Actor user = new Actor(getApplicationContext());
                Actor userOld = new Actor(getApplicationContext());

                user.refreshStatus = true;
                user.ticket = authenticationResult.getAccessToken();
                user.userId = authenticationResult.getUserInfo().getUserId();
                user.unique = authenticationResult.getUserInfo().getDisplayableId();
                user.SyncPrime(getApplicationContext(), true);

                if(userOld.unique == null || userOld.unique.equals("")) {
                    //Relaunches on first launch
                    Intent intent = new Intent(getApplicationContext(), Schedule.class);
                    intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }

            }

            @Override
            public void onError(Exception exception) {
                /* Failed to acquireToken */
                Log.e(Login.class.getSimpleName(), "Authentication failed: " + exception.toString());
                if (exception instanceof AuthenticationException) {
                    ADALError error = ((AuthenticationException) exception).getCode();
                    if (error == ADALError.AUTH_FAILED_CANCELLED) {

                        Log.e(Login.class.getSimpleName(), "The user cancelled the authorization request");
                        Intent intent = new Intent(getApplicationContext(), Schedule.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
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

    public void callLogin() {
        //Runs Either Azure or Kevin's Box
        if(new WebServices(new Gson()).IsNetwork(this)) {
            if (!connection.isKevin(this)) {
                //mAcquireTokenHandler.sendEmptyMessage(MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO);
                mAuthContext.acquireToken(getActivity(), connection.getOauthResourceId(getActivity()), connection.getOauthClientId(getActivity()), connection.getOauthRedirectUri(getActivity()), PromptBehavior.Auto, getAuthInteractiveCallback());
            } else {
                Actor user = new Actor(this);
                    if (user.ticket.isEmpty() || user.ticket.length() == 0) {
                    Intent intent = new Intent(this, AuthenticationSplashScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        }
    }
}
