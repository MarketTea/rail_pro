package com.railprosfs.railsapp.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.railprosfs.railsapp.BuildConfig;

import static com.railprosfs.railsapp.utility.Constants.SP_BUILD;
import static com.railprosfs.railsapp.utility.Constants.SP_REG_STORE;
import static com.railprosfs.railsapp.utility.Constants.SUB_ZZZ;

public class Connection {

    // API Calls
    public static final String API_GET_USER_BY_ID = "users/getuser?userId=" + SUB_ZZZ;
    public static final String API_GET_ADMIN_USER_BY_ID ="users/getadminuser?userId=" + SUB_ZZZ;
    public static final String API_GET_FIELDWORKER_BY_ID = "personnel/fieldworker/" + SUB_ZZZ;
    //public static final String API_GET_USER = BuildConfig.URL_BASE_API + "user/" + SUB_ZZZ;
    //public static final String API_GET_USER_RR = BuildConfig.URL_BASE_API + "user/" + SUB_ZZZ + "/railroad";
    public static final String API_POST_LOGIN = "token";
    // public static final String API_GET_SCHEDULE = BuildConfig.URL_BASE_API + "user/" + SUB_ZZZ + "/assignment";
    public static final String API_POST_SCHEDULE = "jobs/assignments/";
    //public static final String API_GET_JOB = BuildConfig.URL_BASE_API + "user/" + SUB_ZZZ + "/project/" + SUB_YYY;
    public static final String API_POST_JOB_LIST = "jobs";
    public static final String API_GET_JOB_BY_ID = "jobs/" + SUB_ZZZ;
    public static final String API_GET_JOB_COSTCENTERS = "jobs/costcenters/" + SUB_ZZZ;
    public static final String API_GET_CUST_BY_ID = "customer/" + SUB_ZZZ;
    public static final String API_GET_DWR = "forms/dwr/" + SUB_ZZZ;
    public static final String API_GET_DWR_RWIC_SIGNATURE = "forms/dwr/rwicsignature/" + SUB_ZZZ;
    public static final String API_GET_DWR_CLIENT_SIGNATURE = "forms/dwr/clientsignature/" + SUB_ZZZ;
    public static final String API_GET_DWR_RAIL_SIGNATURE = "forms/dwr/railroadSignature/" + SUB_ZZZ;
    public static final String API_GET_PHOTO_DATA = "forms/dwr/PhotoData/" + SUB_ZZZ;
    public static final String API_GET_DOC_BYTES = "docs/doc/data/" + SUB_ZZZ;
    public static final String API_POST_FORM_LIST = "forms/";
    public static final String API_GET_QUESTIONS =  "forms/" + SUB_ZZZ;
    public static final String API_POST_SAVE_DWR = "forms/dwr/save";
    public static final String API_POST_SAVE_JOBSETUP = "forms/jobsetup/save";
    public static final String API_POST_SAVE_COVERSHEET = "forms/utilityCoverSheet/save";
    public static final String API_POST_SAVE_UTILITYSETUP = "forms/utilityJobSetup/save";
    public static final String API_POST_DWR_LIST = "forms/dwr";
    public static final String API_POST_RAILRROAD = "railroads";
    public static final String API_POST_JOBSETUP_LIST = "forms/jobsetup";
    public static final String API_POST_UTILITYSETUP_LIST = "forms/utilityJobSetup";
    public static final String API_POST_COVERSHEET_LIST = "forms/utilityCoverSheet";
    public static final String API_GET_JOBSETUP =  "forms/jobsetup/" + SUB_ZZZ;
    public static final String API_GET_COVERSHEET =  "forms/utilityCoverSheet/" + SUB_ZZZ;
    public static final String API_GET_UTILITYSETUP = "forms/utilityJobSetup/" + SUB_ZZZ;
    public static final String API_POST_SAVE_FLASHAUDIT = "forms/flashaudit/save";

    /** ADAL INFORMATION **/

    // Authority (Azure Tenent):
    // Docs use form of https://login.windows.net/<tenant_domain_name>.onmicrosoft.com but does not work.
    // Instead using form of https://login.microsoftonline.com/<tenent GUID>/oauth2/authorize
    public static final String OAUTH_TENET_DEV = "https://login.microsoftonline.com/ae3bb901-38a7-4fcf-a2de-659e6de7efe3/oauth2/authorize";
    public static final String OAUTH_TENET_PROD = "https://login.microsoftonline.com/5384b56e-74f5-4534-b967-074c24d7d2f6/oauth2/authorize";

    // Altsource Azure
    /* The client ID of the application is called the Application ID in Azure (used to be Client ID in old portal.) */
    public static final String OAUTH_CLIENT_ID_DEV = "d7c7e595-c1ef-4d0e-ba12-b3734b99eb61";
    public static final String OAUTH_CLIENT_ID_PROD = "7da12ef2-1d86-4715-a3d5-35cea656dcfd";

    // Altsource Azure - RailProsNativeDev
    /* Resource URI is the endpoint which will be accessed.  This needs to be hooked up to the Client Id on Azure AD. */
    public static final String OAUTH_RESOURCE_ID_DEV = "55ee6067-562d-4a60-974d-16bb781ad029";
    public static final String OAUTH_RESOURCE_ID_PROD = "8a878cdd-dd45-4193-bce5-ee1d2001925f";

    // Altsource Azure - RailProsNativeDev
    /* The Redirect URI of the application. Not of much use on native, but may need it to be known to Azure AD. */
    public static final String OAUTH_REDIRECT_URI_DEV = "https://localhost";
    public static final String OAUTH_REDIRECT_URI_PROD = "https://rails.railpros.com";


    public static final String BUILD_QA = "https://rails-api-altsrc-qa.azurewebsites.net/api/";
    public static final String BUILD_DEV = "https://rails-api-altsrc.azurewebsites.net/api/";
    public static final String BUILD_PROD = "https://rails-api.railpros.com/api/";
    public static final String BUILD_PROD_QA = "https://rails-api-staging.railpros.com/api/";
    public static final String BUILD_KEVIN = "http://krogers-pc.altsrc.net/rpfs/api/";
    public static final String BUILD_JESSE = "http://hpainter-pc2.altsrc.net:61350/api/";


    private static Connection connection;
    public String build;

    public static Connection getInstance() {
        if(connection == null) {
            connection = new Connection();
        }
        return connection;
    }

    public void setBuildEnvironment(Context context, String string) {
        build = string;
        SharedPreferences registered = context.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = registered.edit();
        editor.putString(SP_BUILD, build);
        editor.apply();
    }


    public String getBuild(Context context) {
        SharedPreferences registered = context.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        return registered.getString(SP_BUILD, getDefaultBuild());
    }

    public boolean isKevin(Context context) {
        if(getBuild(context).equals(BUILD_KEVIN)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get Built Type Current App is Using String Form
     * @param context
     * @return
     */
    public String getBuildType(Context context) {
        String build = getBuild(context);
        switch (build) {
            case BUILD_QA:
                return "QA";
            case BUILD_DEV:
                return "DEV";
            case BUILD_PROD:
                return "PROD";
            case BUILD_PROD_QA:
                return "PROD_QA";
            case BUILD_KEVIN:
                return "KEVIN";
            case BUILD_JESSE:
                return "JESSE";
            default:
                return "";
        }
    }

    /**
     *  Gets Default of Initial Built
     *  Used At Launch
     **/
    public String getDefaultBuild() {
        switch (BuildConfig.BUILD) {
            case "DEV":
                return BUILD_DEV;
            case "QA":
                return BUILD_QA;
            case "PROD":
                return BUILD_PROD;
            case "PROD_QA":
                return BUILD_PROD_QA;
            case "KEVIN":
                return BUILD_KEVIN;
            case "JESSE":
                return BUILD_JESSE;
            default:
                return "";
        }
    }

    public String getFullApiPath(String path) {
        String holdBuild = build != null ? build : getDefaultBuild();
        switch(holdBuild) {
            case BUILD_QA:
                return BUILD_QA + path;
            case BUILD_DEV:
                return BUILD_DEV + path;
            case BUILD_PROD:
                return BUILD_PROD + path;
            case BUILD_PROD_QA:
                return BUILD_PROD_QA + path;
            case BUILD_KEVIN:
                return BUILD_KEVIN + path;
            case BUILD_JESSE:
                return BUILD_JESSE + path;
            default:
                return "";
        }
    }

    public String getFullApiPath(Context context, String path) {
        String holdBuild = build != null ? build : getDefaultBuild();
        switch(holdBuild) {
            case BUILD_QA:
                return BUILD_QA + path;
            case BUILD_DEV:
                return BUILD_DEV + path;
            case BUILD_PROD:
                return BUILD_PROD + path;
            case BUILD_PROD_QA:
                return BUILD_PROD_QA + path;
            case BUILD_KEVIN:
                return BUILD_KEVIN + path;
            case BUILD_JESSE:
                return BUILD_JESSE + path;
            default:
                return "";
        }
    }

    public String getOauthTenet(Context ctx) {
        switch (getBuild(ctx)) {
            case BUILD_DEV:
            case BUILD_QA:
            case BUILD_JESSE:
                return OAUTH_TENET_DEV;
            case BUILD_PROD:
            case BUILD_PROD_QA:
                return OAUTH_TENET_PROD;
            default:
                return "";
        }
    }

    public String getOauthClientId(Context ctx) {
        switch (getBuild(ctx)) {
            case BUILD_DEV:
            case BUILD_QA:
            case BUILD_JESSE:
                return OAUTH_CLIENT_ID_DEV;
            case BUILD_PROD:
            case BUILD_PROD_QA:
                return OAUTH_CLIENT_ID_PROD;
            default:
                return "";
        }
    }

    public String getOauthResourceId(Context ctx) {
        switch (getBuild(ctx)) {
            case BUILD_DEV:
            case BUILD_QA:
            case BUILD_JESSE:
                return OAUTH_RESOURCE_ID_DEV;
            case BUILD_PROD:
            case BUILD_PROD_QA:
                return OAUTH_RESOURCE_ID_PROD;
            default:
                return "";
        }
    }

    public String getOauthRedirectUri(Context ctx) {
        switch (getBuild(ctx)) {
            case BUILD_DEV:
            case BUILD_QA:
            case BUILD_JESSE:
                return OAUTH_REDIRECT_URI_DEV;
            case BUILD_PROD:
            case BUILD_PROD_QA:
                return OAUTH_REDIRECT_URI_PROD;
            default:
                return "";
        }
    }

}
