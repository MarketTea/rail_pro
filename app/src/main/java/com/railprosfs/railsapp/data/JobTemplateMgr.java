package com.railprosfs.railsapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.railprosfs.railsapp.BuildConfig;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.utility.Connection;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  There was a good amount of code to added to support the dual use of the Job Setup
 *  infrastructure as a way of providing a Cover Sheet for the Utility jobs.  The
 *  backend folks were not happy about trying to reuse the Job Setup infrastructure
 *  for this form and probably will rewrite it as new Cover Sheet infrastructure.  If
 *  they just reproduce the form in the same manner (template of questions and fields
 *  of answers) we may be able to continue to reuse the code we have added here. There
 *  are some reference notes you might want to read and the duform samples (pdf) at
 *  subdirectory: K:\Applications\AltSource\Architecture\New Accounts\Railpros\RP Utility
 */
public class JobTemplateMgr {

    private final Context mContext;
    private static final String JOBSETUP_PREFIX = "JobSetup";
    private static final String UTILITYSETUP_PREFIX = "UtilitySetup";
    private static final String COVERSHEET_PREFIX = "CoverSheet";
    public JobTemplateMgr(Context context){
        mContext = context;
    }

    /**
     * This signals if the questions need to be updated because the version has changed.
     * Has the side effect of updating the version if it changes.
     * @param railroad - Property of concern.
     * @param current - The current version, which is the name of the form supplied by API.
     * @return - True means version has changed.
     */
    public boolean VersionChanged(String railroad, String current) {
        String nameVer = railroad + "ver" + BuildConfig.VERSION_NAME + Connection.getInstance().getBuild(mContext);
        // LoadPrime the local data.
        SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        String jobSetupVer = registered.getString(nameVer, "");

        if(!jobSetupVer.equals(current)) {
            SharedPreferences.Editor mEditor = registered.edit();
            mEditor.putString(nameVer, current);
            mEditor.apply();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Save key information used to get the Job and/or Utility Setup forms.
     * As it happens, the order of the data returned is what we want.  That
     * means the last call to this method is the newest value and overwrites
     * all the other entries.  If that ever changes, we will need to inspect
     * and drive the saved value based off the version embedded in the name.
     * @param name - Contains the embedded railroad name and version.
     * @param id - The id on the server of the job setup questions.
     */
    public void RegisterJobSetupForm(String name, int id){
        for (String railroad : Railroads.GetPropertiesRaw(mContext)) {
            String jsKey = JOBSETUP_PREFIX + railroad;
            if(name.contains(jsKey)){
                SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = registered.edit();
                editor.putInt(jsKey, id);
                editor.apply();
            }
            String utilKey = UTILITYSETUP_PREFIX + railroad;
            if(name.contains(utilKey)){
                SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = registered.edit();
                editor.putInt(utilKey, id);
                editor.apply();
            }
        }
    }

    public void RegisterCoverSheetForm(String name, int id){
        for (String railroad : Railroads.GetPropertiesRaw(mContext)) {
            String jsKey = COVERSHEET_PREFIX + railroad;
            if(name.contains(jsKey)){
                SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = registered.edit();
                editor.putInt(jsKey, id);
                editor.apply();
            }
        }
    }

    /**
     * Returns the server id of the job setup template questions for a specific railroad.
     * @param railroad - The name of the railroad form the property array.
     */
     public int GetJobSetupId(String railroad){
        SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
         String jsKey = JOBSETUP_PREFIX + railroad;
        return registered.getInt(jsKey, 0);
     }

    /**
     * Returns the server id of the utility setup template questions for a specific railroad.
     * @param railroad - The name of the railroad form the property array.
     */
    public int GetUtilitySetupId(String railroad) {
        SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        String utilKey = UTILITYSETUP_PREFIX + railroad;
        return registered.getInt(utilKey, 0);
    }

    /**
     * Returns the server id of the utility setup template questions for a specific railroad.
     * @param railroad - The name of the railroad form the property array.
     */
    public int GetCoverSheetId(String railroad) {
        SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        String utilKey = COVERSHEET_PREFIX + railroad;
        return registered.getInt(utilKey, 0);
    }
}
