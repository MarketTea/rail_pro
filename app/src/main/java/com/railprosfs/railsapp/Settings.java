package com.railprosfs.railsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import androidx.appcompat.app.AppCompatActivity;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.KTime;

import java.util.Calendar;

/**
 *  The Settings provide a UI to allow for the maintenance of certain locally
    stored data in a system defined Preference table. This is not the only
    data stored in Preferences, see the Actor class.
    The Settings mostly store general App data, e.g. how to format the
    date, and also act as a place to access certain special functions,
    like logout.  This class provides a simple wrapper to read them from
    the Preferences. It also alleviates any worries about initialization.

 */
public class Settings extends AppCompatActivity  implements FragmentTalkBack {

    public static final String PREF_LOGOUT = "1001";
    public static final String PREF_DEFAULT_RAILROAD = "1005";
    public static final String PREF_USEVALIDATION = "1006";
    public static final String PREF_DEFAULT_DAYS = "1111";
    public static final String PREF_SANDBOX_MODE = "1007";
    public static final String DEFAULT_FORM_DAYS = "16";


    public static final boolean DEFAULT_USEVALIDATION = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsBasic())
                .commit();
    }

    public static boolean getUseValidation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_USEVALIDATION, DEFAULT_USEVALIDATION);
    }

    public static String getPrefDefaultRailroad(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_DEFAULT_RAILROAD, "");
    }

    public static String getPrefOldestDwr(Context context){
        try {
            int daysPast = -1 * Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_DEFAULT_DAYS, DEFAULT_FORM_DAYS));
            Calendar pastDate = Calendar.getInstance();
            pastDate.add(Calendar.DATE, daysPast);
            return DateFormat.format(KTime.KT_fmtDate3339k, pastDate).toString();
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "getPrefOldestDwr");
            return "";
        }
    }

    public static int getPrefOldestDays(Context context){
        try {
            return  Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_DEFAULT_DAYS, DEFAULT_FORM_DAYS));
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "getPrefOldestDays");
            return 16;
        }
    }

    /**
     *  Simple Confirmation Dialog callback.  The simple confirmation dialog is used by
     *  a number of methods.  This is where the confirmation reply (OK or Cancel) is returned.
     *  The message passed in is used as the key to determine what code needs to run.
     *  NOTE: For confirmations that end up here it means the user pressed OK.
     * @param message   The unique message displayed on the confirmation dialog.
     */
    @Override
    public void simpleConfirmResponse(int message) {

        switch (message) {
            case R.string.msg_confirm_sandbox:
                // Change Environment
                Connection connection = Connection.getInstance();
                connection.setBuildEnvironment(getApplicationContext(), Connection.BUILD_PROD_QA);
                Intent intent = new Intent(getApplicationContext(), AuthenticationSplashScreen.class);
                intent.setData(Uri.parse("http://com.railprosfs.railsapp/logout"));
                startActivity(intent);
                break;
        }
    }

    @Override
    public void simpleListResponse(int source, int selection) {  }

    @Override
    public void setTimePicker(String totalTime, String startTime, String endTime, int id) {  }

    @Override
    public void setSignatureImage(String imageName, int id) {  }

    @Override
    public void setPictureOnClick(int position) {  }

    @Override
    public void unlockOrientation() {  }

    @Override
    public void lockOrientation() {  }

    @Override
    public void simplePickerResponse(int source, int selection) {  }

    @Override
    public void setJobNumber(JobTbl tbl) {  }
}
