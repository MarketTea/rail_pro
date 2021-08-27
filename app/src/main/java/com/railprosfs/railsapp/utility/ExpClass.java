package com.railprosfs.railsapp.utility;

import android.util.Log;
import android.util.Pair;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.List;

/**
 * This can be used when internally throwing exceptions.  Also, the static Logging methods are here.
 * The DEBUG_RUN should be turned to false when releasing to production.
 */
public class ExpClass extends Exception {
    public static final long serialVersionUID = -6463927996462971950L;  // required by superclass
    public static final int GENERAL_EXP = 18000;
    public static final int HTTP_STATUS = 18001;
    public static final int PARSE_EXP = 18002;
    public static final int FILE_ISSUES = 18003;
    public static final int NETWORK_EXP = 18004;
    public static final int AUDIT_LOG = 17001;
    public static final String DEFAULT_DESC = "No Error Detail.";
    public static final int STATUS_CODE_NETWORK_DOWN = 99;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_NOTFOUND = 404;
    public static final int STATUS_CODE_SERVER_ERR = 500;
    public static final int STATUS_CODE_UNKNOWN = 98;
    private static final String ERR_TAG = "Exception Sink";
    private static final boolean DEBUG_RUN = false;

    public int Number;
    public int Status;
    public String Name;
    public String Extra;

    public ExpClass(){
        super("General Exception Thrown.");
        Number = 18000;
        Name = "GeneralException";
        Extra = "";
    }

    // Traditional encapsulation of exception, providing information on the source exception.
    public ExpClass(int number, String name, String desc, Throwable source){
        super(desc, source);
        Number = number;
        Name = name;
        Status = 0;
        Extra = "";
    }

    // Informational exception.
    public ExpClass(int number, String name, String desc, String extra) {
        super(desc);
        Number = number;
        Name = name;
        Status = 0;
        Extra = extra;
    }

    // HTTP error encapsulation for propagation of a request failure.
    public ExpClass(int status, String desc, String extra){
        super(desc);
        Number = HTTP_STATUS;
        Name = HttpStatusName(status);
        Status = status;
        Extra = extra;
    }

    public static void Audit(String title, String msg, List<Pair<String,String>> keys) {
        for (Pair<String, String> tuple: keys) {
            FirebaseCrashlytics.getInstance().setCustomKey(tuple.first, tuple.second);
        }
        FirebaseCrashlytics.getInstance().log(msg);
        FirebaseCrashlytics.getInstance().recordException(new ExpClass(AUDIT_LOG, "Audit Message", title, "Custom Keys:" + String.valueOf(keys.size())));
    }

    public static void LogEXP(ExpClass ex,  String key){
        if(ex.Status == STATUS_UNAUTHORIZED) return;    // So common there is no point to logging it.
        FirebaseCrashlytics.getInstance().log(String.valueOf(ex.Number) + " Exception Name: " + ex.Name + "() :: (status=" + HttpStatusName(ex.Status) + ") key=" + key + " :: " + ex.getMessage() + " -extra- " + ex.Extra);
        FirebaseCrashlytics.getInstance().recordException(ex);
    }

    public static void LogEX(Exception ex, String key){
        FirebaseCrashlytics.getInstance().log(ERR_TAG + ex.getStackTrace()[0].getClassName() + "." + ex.getStackTrace()[0].getMethodName() + "() :: (key=" + key + ") " + ex.getMessage());
        FirebaseCrashlytics.getInstance().recordException(ex);
    }

    public static void LogIN(String tag, String message){
        if(DEBUG_RUN) Log.i(tag, message);
    }

    private static String HttpStatusName(int code){
        if(code==99) return "NetworkDown";  // This one is personal.
        if(code==100) return "Continue";
        if(code==101) return "SwitchingProtocols";
        if(code==300) return "MultipleChoices";
        if(code==301) return "MovedPermanently";
        if(code==302) return "Found";
        if(code==303) return "SeeOther";
        if(code==304) return "NotModified";
        if(code==307) return "TemporaryRedirect";
        if(code==308) return "PermanentRedirect";
        if(code==400) return "BadRequest";
        if(code==401) return "Unauthorized";
        if(code==402) return "PaymentRequired";
        if(code==403) return "Forbidden";
        if(code==404) return "NotFound";
        if(code==405) return "MethodNotAllowed";
        if(code==406) return "NotAcceptable";
        if(code==407) return "ProxyAuthenticationRequired";
        if(code==408) return "RequestTimeout";
        if(code==409) return "Conflict";
        if(code==410) return "Gone";
        if(code==411) return "LengthRequired";
        if(code==412) return "PreconditionFailed";
        if(code==413) return "PayloadTooLarge";
        if(code==414) return "URITooLong";
        if(code==415) return "UnsupportedMediaType";
        if(code==416) return "RangeNotSatisfiable";
        if(code==417) return "ExpectationFailed";
        if(code==426) return "UpgradeRequired";
        if(code==428) return "PreconditionRequired";
        if(code==429) return "TooManyRequests";
        if(code==431) return "RequestHeaderFieldsTooLarge";
        if(code==451) return "UnavailableForLegalReasons";
        if(code==500) return "InternalServerError";
        if(code==501) return "NotImplemented";
        if(code==502) return "BadGateway";
        if(code==503) return "ServiceUnavailable";
        if(code==504) return "GatewayTimeout";
        if(code==505) return "HTTPVersionNotSupported";
        if(code==511) return "NetworkAuthenticationRequired";
        return "UnknownError";
    }

    public String ExceptionName(int code){
        if(code==GENERAL_EXP) return "General Failure";
        if(code==HTTP_STATUS) return "Networking Failure";
        if(code==PARSE_EXP) return "Parse Failure";
        if(code==FILE_ISSUES) return "File Failure";
        if(code==NETWORK_EXP) return "Connection Failure";
        if(code==AUDIT_LOG) return "Audit Message";
        return "Unknown Failure";
    }
}
