package com.railprosfs.railsapp.ui_support;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.View;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.utility.ExpClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  To support a variety of high definition DWR forms without actually having
 *  to build layouts for every possible DWR form, hybrid layout/code approach
 *  has been implemented.  They layout consists of all possible input widgets
 *  laid out in a single form.  The code consists of a declarative language
 *  used to show only those widgets of concern for a specific DWR Form.
 *
 *  This class is a set of helpers to provide access to the declarative data
 *  found in the dynamics.xml resource.
*/
public class LayoutHelperDwr {

    public static final int VIEW_TYPE_VIEW = 0;
    public static final int VIEW_TYPE_INPUT = 1;
    public static final int VIEW_TYPE_EDIT = 2;
    public static final int VIEW_TYPE_TEXT = 3;
    public static final int VIEW_TYPE_CHECK = 5;
    public static final int VIEW_TYPE_PHONE = 6;

    public static final int CONDITION_HOLDOVER = 1;
    public static final int CONDITION_SUPERVISOR_JOB = 2;

    private final Map<String, Qualities> mDwrQualities;
    private final List<Qualities> mAltQualities;
    private final SparseIntArray mTemplatesIds;
    private static final int TEMPLATE_WILDCARD = -1;

    /**
     *  We are getting the dynamics data from a local resource and want it be both
     *  quick to access and global in nature. The singleton pattern allows wrapping
     *  global data as an in-memory data structure. To do this we create a static
     *  storage area (sLayoutHelperDwr), provide a method to get that storage area
     *  (getInstance) and hide the normal constructor (private LayoutHelperDwr).
     *  NOTE: If we wanted to switch to using an API someday for the dynamics data,
     *  it could be placed in the DB and presented with this same wrapper.
    */
    private static volatile LayoutHelperDwr sLayoutHelperDwr;

    public static LayoutHelperDwr getInstance(Context ctx) {

        if (sLayoutHelperDwr == null) {
            synchronized (LayoutHelperDwr.class) {
                if (sLayoutHelperDwr == null) sLayoutHelperDwr = new LayoutHelperDwr(ctx);
            }
        }
        return sLayoutHelperDwr;
    }

    private LayoutHelperDwr(Context ctx) {
        mTemplatesIds = InitializeTemplateIds(ctx);
        mDwrQualities = InitializeDwrQualities(ctx);
        mAltQualities = InitializeAltQualities(ctx);
    }

    /**
     *  The idea here is that there is a set of base field qualities for any form.
     *  There are also alternative qualities for certain fields.  These alternatives
     *  are superimposed on the base list. The consolidated map or list is returned
     *  for use in layout and validation of a specific form.
     * @param formType This is the form, based on the day classification.
     * @param railroad This is the specific property the form is getting filled out for.
     * @return Returns a list or hash map of qualities for a given form and template.
    */
    public List<Qualities> GetDwrQualities(int formType, int railroad){
        return new ArrayList<>(GetDwrQualityMap(formType,  railroad).values());
    }
    public Map<String, Qualities> GetDwrQualityMap(int formType, int railroad){

        int templateId;
        Map<String, Qualities> formData = new HashMap<>();

        try { templateId = mTemplatesIds.get(railroad);}
        catch (Exception ex){ templateId = 0; }

        if(mAltQualities != null) {

            formData = RealDwrCopy();

            // Step through alternative Qualities and replace as needed.
            for (Qualities item : mAltQualities) {
                if (item.Classification == formType && (item.TemplateId == templateId || item.TemplateId == TEMPLATE_WILDCARD)) {
                    formData.put(item.FieldName, item);
                }
            }
        }
        return formData;
    }

    /**
     *  After acquiring the base settings for the DWR layout from the resource, we do not want to
     *  alter it directly.  Instead we want to use a copy of it each time we need to refresh the
     *  set of qualities used for a form.  Since this is a static class any use of the data, even
     *  outside of the class, will change it and we do not want to give up our hard won base list.
     * @return  A copy of the base map of DWR Qualities by field name.
     */
    private Map<String, Qualities> RealDwrCopy(){
        Map<String, Qualities> tempDwrQualities = new HashMap<>();

        List<Qualities> realDwrQualities = new ArrayList<>(mDwrQualities.values());

        for (Qualities item : realDwrQualities){
            Qualities holdQ = new Qualities(
                    item.Classification,
                    item.TemplateId,
                    item.FieldName,
                    item.FieldType,
                    item.Visibility,
                    item.FieldHint.replace(SYMBOL_REQUIRED, ""),
                    item.FieldHintError,
                    item.FieldHintHelp,
                    item.DefaultValue,
                    item.Required,
                    SYMBOL_REQUIRED,
                    item.UsageCode
            );
            tempDwrQualities.put(item.FieldName, holdQ);
        }
        return tempDwrQualities;
    }


    /**
     *  Sometimes the dynamics declaration cannot express the expected logic.  For example,
     *  if a quality of a widget is dependent upon user entered data.  In that case, this
     *  method allows specific changes to be applied (kind of an override) directly to a
     *  generated list.
     * @param list A previously generated list of qualities from GetDwrQualities().
     * @param condition A key to let the method know what specific qualities changes are needed.
     * @return An updated list of widget qualities.
    */
    public List<Qualities> adjustCondition(List<Qualities> list, int condition) {
        // This is a case of turning off the required flag for a specific input.
        switch (condition){
            case CONDITION_HOLDOVER:
            for (Qualities item : list) {
                if (item.FieldName.equals("fieldWorkHoursRounded") || item.FieldName.equals("inputWorkHoursRounded")) {
                    item.Required = false;
                }
            }
            break;
            case CONDITION_SUPERVISOR_JOB:
                for (Qualities item : list) {
                    if (item.FieldName.equals("displayClientTimeSignature") || item.FieldName.equals("layoutClientSignatureInfo")) {
                        item.Visibility = View.GONE;
                        item.Required = false;
                    }
                }
               break;
        }
        return list;
    }

    // Returns a map of all qualities by Usage Code (skipping those without useage codes).

    /**
     *  The Usage Code is the link that ties answers in the App to questions on the backend.
     *  While some data is handled more traditionally by the API (i.e. named parameters in a
     *  JSON object), most of the DWR data is conveyed to the API as a generic layout called
     *  a "field".  Every "field" looks the same, so each need a unique name that allows the
     *  client and server to know what data is what.
     *  This specific method allows the lookup of a widget quality by using the Usage Code.
     *  NOTE: For this method to work properly, a set of widget qualities should NEVER have
     *  duplicate Usage Codes.  That will only lead to madness.
     * @param formType This is the form, based on the day classification.
     * @param railroad This is the specific property the form is getting filled out for.
     * @return Returns map of Qualities by Usage Code (not including those without usage codes).
    */
    public Map<String, Qualities> GetApiQualityMap(int formType, int railroad){
        List<Qualities> allQualities = GetDwrQualities(formType, railroad);
        Map<String, Qualities> usageData = new HashMap<>();

        for (Qualities item : allQualities) {
            if (item.UsageCode.length() > 0){
                usageData.put(item.UsageCode, item);
            }
        }
        return usageData;
    }

    /**
     *  Each railroad might want their own personalized DWR Form, but in reality, most
     *  are alright using one that is similar to another railroad.  This allow a reduction
     *  in complexity by grouping railroads into specific templates.
     *  We want to do save this data in-memory, so we can allow later methods to use it
     *  without requiring a Application Context.
     * @param context Application Context needed for accessing local resources.
     * @return An array of key/value pairs that identity the template used for a railroad.
    */
    private SparseIntArray InitializeTemplateIds(Context context) {
        List<String> railroads = Railroads.GetPropertiesRaw(context);
        SparseIntArray holdTemplateIds = new SparseIntArray(railroads.size());

        for (String item: railroads) {
            holdTemplateIds.append(Railroads.PropertyKey(context, item), Railroads.GetTemplateKey(item));
        }
        return holdTemplateIds;
    }

    /**
     *  Create the baseline structure. This holds all the possible widget qualities that
     *  may need to be used to create the forms.
     *  We want to do save this data in-memory, so we can allow later methods to use it
     *  without requiring a Application Context.
     * @param context Application Context needed for accessing local resources.
     * @return All the baseline form data indexed by field (widget) name.
    */
    private Map<String, Qualities> InitializeDwrQualities(Context context) {
        List<String> formRaw = Arrays.asList(context.getResources().getStringArray(R.array.baseline));
        Map<String, Qualities> formMap = new HashMap<>(formRaw.size());
        for (String item: formRaw) {
            Qualities hold = ParseFromString(item);
            if (hold != null) {
                formMap.put(hold.FieldName, hold);
            }
        }
        return formMap;
    }

    /**
     *  Create a list of alternative widget qualities. This holds all the modifications based
     *  on variations in requirements across templates and such.  Since it will be used to
     *  replace specific entries in the baseline structure, it can be stored as a list.
     *  We want to do save this data in-memory, so we can allow later methods to use it
     *  without requiring a Application Context.
     * @param context Application Context needed for accessing local resources.
     * @return A list of all alternative form widget qualities.
     */
    private List<Qualities> InitializeAltQualities(Context context) {
        String [] holdAlternates = context.getResources().getStringArray(R.array.alternate_qualities);
        List<Qualities> altQualities = new ArrayList<>();
        for (String item : holdAlternates) {
            Qualities hold = ParseFromString(item);
            if (hold != null)
                altQualities.add(hold);
        }
        return altQualities;
    }

    /**
     *  A simple (de)serialization technique.
     * @param raw A row of the string-array found in dynamics.xml.
     * @return An instance of the (widget) Qualities class.
     */
    private Qualities ParseFromString(String raw) {
        int EXPECTED_DYNAMIC_PARMS = 11;
        String[] holdParts = raw.split("\\?", EXPECTED_DYNAMIC_PARMS);
        if(holdParts.length == EXPECTED_DYNAMIC_PARMS) { // Nbr of class properties
            return new Qualities(
                    Integer.parseInt(holdParts[0]),     // Classification
                    Integer.parseInt(holdParts[1]),     // Template
                    holdParts[2],                       // FieldName
                    Integer.parseInt(holdParts[3]),     // FieldType
                    Integer.parseInt(holdParts[4]),     // Visibility (0=there, 8=gone)
                    holdParts[5],                       // FieldHint
                    holdParts[6],                       // FieldHintError
                    holdParts[7],                       // FieldHintHelp
                    holdParts[8],                       // DefaultValue
                    Boolean.parseBoolean(holdParts[9]), // Required
                    SYMBOL_REQUIRED,                    // Symbol concat on FieldHint if Required
                    holdParts[10]);                     // Usage Code link to backend data item
        } else {
            ExpClass kx = new ExpClass(18001, "Dynamics Parser", "The expected number of parameters is not correct.  Expected: " + EXPECTED_DYNAMIC_PARMS + " Found:" + holdParts.length + " raw:" + raw , (new IndexOutOfBoundsException()));
            ExpClass.LogEX(kx, "Dynamics Parser Failure");
            return null;
        }
    }

    /**
     *  This class holds the data we care about for each widget on a form.
     *  It is primarily UX information, but also used to hold validation
     *  and API information as well.
     *  Each row of the dynamics.xml resource is one instance of this class.
     */
    public final static class Qualities {
        public int Classification;
        public int TemplateId;
        public String FieldName;
        public int FieldType;
        public int Visibility;
        public String FieldHint;
        public String FieldHintError;
        public String FieldHintHelp;
        public String DefaultValue;
        public boolean Required;
        public String UsageCode;

        private Qualities(int classification, int templateId, String fieldName, int fieldType, int visibility, String fieldHint, String fieldHintError, String fieldHintHelp, String defaultValue, boolean required, String requiredSym, String usageCode){
            Classification = classification;
            TemplateId = templateId;
            FieldName = fieldName;
            FieldType = fieldType;
            Visibility = visibility;
            FieldHint = fieldHint + (required ? requiredSym : "");
            FieldHintError = fieldHintError;
            FieldHintHelp = fieldHintHelp;
            DefaultValue = defaultValue;
            Required = required;
            UsageCode = usageCode;
        }
    }
}
