package com.railprosfs.railsapp.data.dto;

import android.content.Context;
import com.railprosfs.railsapp.R;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static com.railprosfs.railsapp.utility.Constants.*;

/**
 * The Railroads class is a wrapper for the list of railroads currently
 * stored as a local array.  The wrapper is necessary to help manage the
 * index and string name of the railroads (called properties by railpros).
 * For example, the railroad is stored as an integer in the dwrtbl and so
 * if a new railroad is added, we do not want that to invalidate the existing
 * stored data.  This would happen if we simply used the array index of the
 * list of railroads. There is also the need to map the list of railroads on
 * the server with our local list (see array.property_code_name).
 *
 * Finally, this class does not require the raw data be stored in an array.
 * It could be placed in the code (constants), stored in a local db table or
 * acquired from the backend.
 *
 * HINT - To add a new Railroad.
 * 1) Add a value to the end of array.property_name (this string is what is displayed to user).
 * 2) Add a value to the end of array.property_code_name (this must match backend code name
 *      while being the same order as the property_name array).
 * 3) If the property uses a different template than BASELINE, add it to GetTemplateKey().
 * 4) If the property needs a Job Setup form, need to add it to UseJobSetup(), but also
 *      it will require a new form built on the backend.
 */
public class Railroads extends BaseDTO {

    /* Railroads that have a Job Setup Form template on backend. */
    /* See Refresh.SyncTemplate() for code that downloads templates. */
    private static final String JS_PROP_BNSF = "BNSF";
    private static final String JS_PROP_UP = "UP";
    private static final String JS_PROP_CP = "CP";
    private static final String JS_PROP_CN = "CN";
    private static final String JS_PROP_KCS = "KCS";
    private static final String JS_PROP_FEC = "FEC";
    private static final String JS_PROP_AAF = "AAF";
    private static final String JS_PROP_AMTRAK = "AMTRAK";
    /* Railroads that have a Job Setup Form template on backend. */
    private static final String US_PROP_UP = "UP";
    private static final String US_PROP_NS = "NS";
    /* DWR TemplateIds - important to dynamics processing. */
    private static final int DWR_TEMPLATE_BASELINE = 1;
    private static final int DWR_TEMPLATE_CN = 2;
    private static final int DWR_TEMPLATE_UP = 3;
    private static final int DWR_TEMPLATE_KCS = 4;
    public static final int DWR_TEMPLATE_CSX = 5;
    private static final int DWR_TEMPLATE_KCT = 6;
    private static final int DWR_TEMPLATE_SECONDAY = 7; // The Florida RRs
    public static final int DWR_TEMPLATE_WMATA = 8;
    public static final int DWR_TEMPLATE_SCRRA = 9;

    public static int TotalProperties(Context context){
        return Arrays.asList(context.getResources().getStringArray(R.array.property_name)).size();
    }

    /*
     *  Simple lookup of railroad short name (also known as "code") from the local property list.
     */
    public static String PropertyName(Context context, int key){
        if(key >= 0 && key < TotalProperties(context)) {
            return Arrays.asList(context.getResources().getStringArray(R.array.property_name)).get(key);
        } else {
            return "";
        }
    }

    /*
     * It is possible that the property name that the backend uses might be different than
     * the name used locally.  The property_code_name holds the valid backend short names,
     * sometimes called the "code", of properties.  This method allows a mapping from the
     * index of the local list (property_code) to the matching backend value.
     */
    public static String PropertyNameServer(Context context, int key) {
        if(key >= 0 && key < TotalProperties(context)) {
            return Arrays.asList(context.getResources().getStringArray(R.array.property_code_name)).get(key);
        } else {
            return "";
        }
    }

    /*
     * If storing the index of a railroad in a table, you should always use this method
     * to get the index.  Just using the order of the array may not work if the array
     * order was manipulated by sorting or filtering.
     */
    public static int PropertyKey(Context context, String name){
        if(name == null) { return -1; }
        List<String> holdRR = GetPropertiesRaw(context);
        int cnt = 0;
        for (String knownAs : holdRR) {
            if (knownAs.equalsIgnoreCase(name)) { return cnt; }
            cnt++;
        }
        return -1;
    }

    /*
     * If getting the local property key when using a property code from the server,
     * this method should be used.  It uses the server version of property code names.
     * These initially all match, but they could drift over time.
     * TO BE CLEAR: This returns the local index, using the server property name.
     */
    public static int PropertyKeyServer(Context context, String name){
        if(name == null) { return -1; }
        List<String> holdRR = GetPropertiesServerRaw(context);
        int cnt = 0;
        for (String knownAs : holdRR) {
            if (knownAs.equalsIgnoreCase(name)) { return cnt; }
            cnt++;
        }
        return -1;
    }

    /*
     * Returns the list of railroads unsorted, as they are listed in the array.
     * This returns the railroads in an order that matches their keys, so this
     * is the safest way to use the data, as the key (index) can be implied from
     * the order.
     */
    public static List<String> GetPropertiesRaw(Context context) {
        return Arrays.asList(context.getResources().getStringArray(R.array.property_name));
    }
    // Just like the GetPropertiesRaw, returns the "server" code names.
    public static List<String> GetPropertiesServerRaw(Context context){
        return Arrays.asList(context.getResources().getStringArray(R.array.property_code_name));
    }

    /*
     * Returns the list of railroads sorted alphabetically.
     * If you use this data, it require one to later use PropertyKey() to get
     * the correct index of railroad (since this returns an ordering different
     * than what is stored in the source array).
     */
    public static List<String> GetPropertiesSorted(Context context) {
        List<String> holdRRs = Arrays.asList(context.getResources().getStringArray(R.array.property_name));
        Collections.sort(holdRRs);
        return holdRRs;
    }

    // The template code is associated to a property. Default is baseline.
    public static int GetTemplateKey(String property){
        if(property == null) { return DWR_TEMPLATE_BASELINE; }
        switch (property) {
            case "CSX":
                return DWR_TEMPLATE_CSX;
            case "CN":
                return DWR_TEMPLATE_CN;
            case "UP":
                return DWR_TEMPLATE_UP;
            case "KCS":
            case "FGA":
                return DWR_TEMPLATE_KCS;
            case "KC Terminal":
                return DWR_TEMPLATE_KCT;
            case "AAF":
            case "FEC":
            case "SFRTA":
            case "Sunrail":
                return DWR_TEMPLATE_SECONDAY;
            case "WMATA":
                return DWR_TEMPLATE_WMATA;
            case "SCRRA":
                return DWR_TEMPLATE_SCRRA;
            default:
                return DWR_TEMPLATE_BASELINE;
        }
    }


    /*
     *  Does this Railroad typically require a Job Setup/Utility Form.
     */
    public static boolean UseJobSetup(String name) { return UseJobSetup(name, RP_FLAGGING_SERVICE); }
    public static boolean UseJobSetup(String name, int type) {
        switch (type) {
            case RP_UTILITY_SERVICE:
            case RP_COVER_SERVICE:
                return name.equalsIgnoreCase(US_PROP_UP) || name.equalsIgnoreCase(US_PROP_NS);
            default:
                return (name.equalsIgnoreCase(JS_PROP_BNSF)
                        || name.equalsIgnoreCase(JS_PROP_CN)
                        || name.equalsIgnoreCase(JS_PROP_CP)
                        || name.equalsIgnoreCase(JS_PROP_UP)
                        || name.equalsIgnoreCase(JS_PROP_KCS)
                        || name.equalsIgnoreCase(JS_PROP_FEC)
                        || name.equalsIgnoreCase(JS_PROP_AAF)
                        || name.equalsIgnoreCase(JS_PROP_AMTRAK)
                );
        }
    }

}
