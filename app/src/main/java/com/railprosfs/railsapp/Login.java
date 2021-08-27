package com.railprosfs.railsapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.railprosfs.railsapp.service.LoginThread;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.service.Refresh;
import com.railprosfs.railsapp.service.WebServiceModels.*;
import com.railprosfs.railsapp.utility.ExpClass;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.railprosfs.railsapp.utility.Constants.*;

public class Login extends AppCompatActivity {

    EditText txtPassword;
    EditText txtEmailAddr;
    TextInputLayout tilEmailAddr;
    TextInputLayout tilPassword;
    CheckBox chkShowPassword;
    ProgressBar waitSignal;

    /*
        This handler is used to as a callback mechanism, such that the Thread
        can alert the Activity when the data has been retrieved.
    */
    private static class MsgHandler extends Handler {
        private final WeakReference<Login> mActivity;

        MsgHandler(Login activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Login activity = mActivity.get();
            if (activity != null) {
                try { activity.waitSignal.setVisibility(View.GONE); } catch (Exception ex) { /* Skip it */ }
                switch (msg.what){
                    case WHAT_POST_LOGIN:
                        activity.DisplaySuccess((LoginResponse)msg.obj);
                        break;
                    case WHAT_LOGIN_ERR:
                        activity.DisplayProblem(msg.arg1);
                        break;
                }
            }
        }
    }
    private final MsgHandler mHandler = new MsgHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        txtPassword = findViewById(R.id.passwordField);
        chkShowPassword = findViewById(R.id.cbShowPwd);
        txtEmailAddr = findViewById(R.id.emailField);
        waitSignal = findViewById(R.id.progressbar);
        tilEmailAddr = findViewById(R.id.emailFieldWrap);
        tilPassword = findViewById(R.id.passwordFieldWrap);

        Actor user = new Actor(getApplicationContext());
        if(user.unique.length() > 0) { txtEmailAddr.setText(user.unique); }


        chkShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    txtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }


    /*
     *  Checks validity of Email Address and Password before sending
     *  server login request
     */
    public void ValidateForm(View view){
        if(!isValidEmail(txtEmailAddr.getText().toString())){
            tilEmailAddr.setError("Invalid email address");

        }else{
            tilEmailAddr.setError(null);
            if(!isValidPassword(txtPassword.getText().toString())) {

                tilPassword.setError("Password cannot be empty");
            }else{
                tilPassword.setError(null);
                Authenticate(view);
            }
        }
    }


    /*
     *  Call the API to see if we can log in.  This is asynchronous, another method
     *  will finish the job.
     */
    public void Authenticate(View view){

        Gson parser = new Gson();
        LoginRequest request = new LoginRequest();
        request.userId = txtEmailAddr.getText().toString();
        request.password = txtPassword.getText().toString();
        LoginThread tLogin = new LoginThread(getApplicationContext(), request, parser, new Messenger(mHandler));
        tLogin.start();
        waitSignal.setVisibility(View.VISIBLE);

        // if you want to limit input while waiting, use the following code:
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        // don't forget to return control later on...
        // getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    /*
     *  If everything has gone well and we have a token, save the data and
     *  return to the main App.
     */
    public void DisplaySuccess(LoginResponse response){


        /* Successfully got a token, call graph now */
        getPermissions();

        //Grabs all the Information from the server since it's the first time loading
        Intent refresh = new Intent(this, Refresh.class);
        refresh.setData(getIntent().getData());
        refresh.putExtra(REFRESH_MAIN, true);
        refresh.putExtra(REFRESH_LOOP, false);
        startService(refresh);

        if(response.access_token.length() > 0) {
            Actor user = new Actor();
            Actor userOld = new Actor(getApplicationContext());
            boolean isNewLogin = true;

            // When the same person signs in, we skip some database cleanup stuff.
            if(userOld.unique.equalsIgnoreCase(txtEmailAddr.getText().toString())) {
                user = userOld;
                isNewLogin = false;
            }

            // user.unique = txtEmailAddr.getText().toString();  // now we get this in Actor
            user.refreshStatus = true;
            user.secret = txtPassword.getText().toString();
            user.ticket = response.access_token;
            user.userId = response.id;
            user.SyncPrime(getApplicationContext(), true);

            Intent intent = new Intent(this, Schedule.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if(isNewLogin) intent.setData(Uri.parse("http://com.railprosfs.railsapp/" + REQUEST_NEWACCT));
            startActivity(intent);
            finish();

        } else {
            DisplayProblem(ExpClass.STATUS_NOTFOUND);
        }

    }

    public void DisplayProblem(int status){
        String detail;
        switch (status){
            case 401:
                detail = getResources().getString(R.string.error_incorrect_credential);
                break;
            default:
                detail = getResources().getString(R.string.error_server_issues);
                break;
        }
        detail += " (" + status + ")";
        DisplayProblem(detail);
    }

    public void DisplayProblem(String extra){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setMessage(extra);
        mBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }

    /*
    *   Checks for valid email input client-side
    *
    * */
    public final boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    /*
     * Checks for empty password field on client-side
     */
    public final boolean isValidPassword(CharSequence target) {
        return !TextUtils.isEmpty(target);
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
}
