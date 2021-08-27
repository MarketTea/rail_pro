package com.railprosfs.railsapp.utility;

public class Constants {

    /*
     *  No one is suppose to actually use this class.  To access the values
     *  import com.railprosfs.railsapp.utility.Constants.*;
     */
    private Constants(){}

    /*  Constants used in dealing with the Shared Preference Database.
     *  This is a different storage area than used by Settings, although
     *  a couple of the names are reused there.
     */
    public static final String SP_REG_STORE = "rpfs.registry";
    public static final String SP_REG_ID = "fieldworker.id";
    public static final String SP_REG_USER = "user.id";
    public static final String SP_REG_TICKET = "rpfs.ticket";
    public static final String SP_REG_UNIQUE = "rpfs.unique";
    public static final String SP_REG_DISPLAY = "rpfs.display";
    public static final String SP_REG_ROLE = "rpfs.role";
    public static final String SP_REG_RAIL_PRI = "rpfs.rail.primary";
    public static final String SP_REG_RAIL_ALL = "rpfs.rail.all";
    public static final String SP_REG_SECRET = "rpfs.secret";
    public static final String SP_REG_UPDATED = "rpfs.updated";
    public static final String SP_REG_DEVICE = "rpfs.device";
    public static final String SP_REG_DWR_FORMID = "rpfs.dwrformid";
    public static final String SP_REG_DWR_FORMID_ROADWAY_FLAGGING = "rpfs.dwrformid.roadwayflagging";
    public static final String SP_REG_LAST_ONLINE = "rpfs.lastonline";
    public static final String SP_REG_EMPLOYEE_CODE = "rpfs.employee.code";
    public static final String SP_BUILD = "rpfs.build";
    public static final String SP_REG_PERDIEM = "rpfs.perdiem";
    public static final String SP_REG_JOBLIST = "rpfs.job.list";
    public static final String SP_LS_TEMPLATE = "rpfs.lastsync.template";
    public static final String SP_LS_JOBFORM = "rpfs.lastsync.jobform";
    public static final String SP_LS_DWRQUESTION = "rpfs.lastsync.jobquestion";
    public static final String SP_LS_RAILROAD = "rpfs.lastsync.railroad";
    public static final String SP_LS_ASSIGNMENT = "rpfs.lastsync.assignments";
    public static final String SP_LS_DOCUMENT = "rpfs.lastsync.document";
    public static final String SP_LS_EVENTQUE = "rpfs.lastsync.eventqueue";
    public static final String SP_LS_EVENTQUE_DWR = "rpfs.lastsync.eventqueue.dwr";
    public static final String SP_LS_EVENTQUE_JOB = "rpfs.lastsync.eventqueue.job";
    public static final String SP_LS_EVENTQUE_AUDIT = "rpfs.lastsync.eventqueue.audit";
    public static final String SP_LS_DWRSTATUS = "rpfs.lastsync.dwrstatus";
    public static final String SP_LS_JOBSTATUS = "rpfs.lastsync.jobstatus";


    /* Constants used as codes for cross Activity communications. */
    public static final String KEVIN_SPEAKS = "Kevin Speaks";
    public static final int WHAT_POST_LOGIN = 100;
    public static final int WHAT_GET_DWR_DETAIL = 101;
    public static final int WHAT_DWR_SAVE = 102;
    public static final int WHAT_QUEUE_SAVE = 103;
    public static final int WHAT_JOB_FORMS = 104;
    public static final int WHAT_JOB_SETUP = 105;
    public static final int WHAT_JOB_LIST = 106;
    public static final int WHAT_FLASH_AUDIT = 107;
    public static final int WHAT_RAILROAD = 108;
    public static final int WHAT_COSTCENTERS = 109;
    public static final int WHAT_DWR_DELETE = 110;
    public static final int WHAT_JSF_DELETE = 111;
    public static final int WHAT_JOB_ONE = 112;

    public static final int WHAT_LOGIN_ERR = 200;
    public static final int WHAT_DWR_DETAIL_ERR = 201;
    public static final int WHAT_DWR_SAVE_ERR = 202;
    public static final int WHAT_QUEUE_ERR = 203;
    public static final int WHAT_DWR_DUPLICATE_SUBMISSION = 204;
    public static final int WHAT_JOB_FORMS_ERR = 205;
    public static final int WHAT_JOB_SETUP_ERR = 206;
    public static final int WHAT_JOB_LIST_ERR = 207;
    public static final int WHAT_HIDE_PROGRESS_BAR = 208;
    public static final int WHAT_FLASHAUDIT_ERR = 209;
    public static final int WHAT_RAILROAD_ERR = 210;
    public static final int WHAT_DWR_DELETE_ERR = 211;
    public static final int WHAT_JSF_DELETE_ERR = 212;
    public static final int WHAT_COSTCENTER_ERR = 213;
    public static final int WHAT_JOB_ONE_ERR = 214;

    public static final String REQUEST_LOGOUT = "logout";
    public static final String REQUEST_NEWACCT = "newaccount";
    public static final String IN_JOBID = "jobid";
    public static final String IN_DWRID = "dwrid";
    public static final String IN_JSID = "jobsetupid";
    public static final String IN_ASSIGNMENT_ID = "assignmentid";
    public static final String IN_PROPERTYID = "propertyid";
    public static final String IN_PROPERTY_CODE = "propertycode";
    public static final String IN_JOBSETUP_FORM = "jsformtype";
    public static final String IN_CSX_REGION = "csxregion";
    public static final String IN_TRACK_FOUL = "trackfoul";
    public static final String IN_START_TIME = "starttime";
    public static final String IN_END_TIME = "endtime";
    public static final String IN_PICKER_ID = "pickerid";
    public static final String IN_PICKER_INFO = "pickerInfo";
    public static final String IN_TITLE_ID = "titleid";
    public static final String KY_CLASS_FRAG = "classification.fragment";
    public static final String KY_STATION_FRAG ="station.fragment";
    public static final String KY_NONBILLEDREASON_FRAG = "nonbilledreason.fragment";
    public static final String KY_STATE_FRAG = "locationstate.fragment";
    public static final String KY_PROPERTY_FRAG = "property.fragment";
    public static final String KY_REGION_FRAG = "region.fragment";
    public static final String KY_TRACK_FOUL_FRAG = "trackfoul.fragment";
    public static final String KY_WEATHER_FRAG = "weather.fragment";
    public static final String KY_WMATA_TRACK_FRAG = "wmatatrack.fragment";
    public static final String KY_WMATA_LINE_FRAG = "wmataline.fragment";
    public static final String KY_WMATA_STATION_FRAG = "wmatastation.fragment";
    public static final String KY_WMATA_YARD_FRAG = "wmatayard.fragment";
    public static final String KY_TYPE_PROTECTION_FRAG = "trackprotect.fragment";
    public static final String KY_JOB_FRAG = "jobnumber.fragment";
    public static final String KY_SIMPLE_PICKER_FRAG = "simplepicker.fragment";
    public static final String KY_SIMPLE_LIST_FRAG = "simplelist.fragmnet";
    public static final String KY_SIMPLE_CONFIRM_FRAG = "simpleconfirm.fragment";
    public static final String KY_SIMPLE_DISPLAY_FRAG = "simpledisplay.fragment";
    public static final String KY_JOBSETUP_NEW_FRAG = "jobsetupnew.fragment";
    public static final String KY_PER_DIEM_FRAG = "perdiem.fragment";
    public static final String RAILROAD = "RAILROAD";
    public static final String KY_JOB_PICKER = "JobPicker";
    public static final String KY_DIVISION_PICKER = "DivisionPicker";
    public static final String KY_TIME_PICKER = "dialogTime";
    public static final String KY_SIGN_PICKER = "signature";
    public static final String IN_SIGN_ID = "signature.id";
    public static final String KY_DWR_SUBMIT  = "submit.dwr";
    public static final int KY_CAMERA_RESULT = 1999;
    public static final int MY_PERMISSIONS_CAMERA = 2001;
    public static final int MY_PERMISSIONS_LOCATION = 2002;


    /* Constants used in dealing with the web APIs. */
    public static final int API_TIMEOUT = 180000;       // Good default for this slow API.
    public static final int API_TIMEOUT_SHORT = 30000;  // Sometimes the people are not going to wait.
    public static final String SUB_ZZZ = "%ZZZ!";
    public static final String SUB_YYY = "%YYY!";
    public static final String API_HEADER_ACCEPT = "application/json";
    public static final String API_HEADER_CONTENT = "application/json";
    public static final String API_LOGIN_CONTENT = "application/x-www-form-urlencoded";
    public static final String API_ENCODING = "UTF-8";
    public static final String API_BEARER = "bearer "; // note the trailing space

    /* Refresh Constants */
    public static final String REFRESH_MAIN = "REFRESH_MAIN";
    public static final String REFRESH_LOOP = "REFRESH_LOOP";
    public static final String REFRESH_RECIEVER = "REFRESH_RECIEVER";
    public static final String REFRESH_PROGRESS_DESCRIPTION = "REFERSH_PROGRESS_DESCRIPTION";
    public static final String REFRESH_PROGRESS = "REFRESH_PROGRESS";
    public static final String REFRESH_RAILROAD = "REFRESH_RAILROAD";
    public static final int REFRESH_CONST = 123;

    /* Job */
    public static final String JOB_STATUS_APPROVED = "Approved";
    public static final String JOB_STATUS_INPRROGRESS = "InProgress";
    public static final String JOB_STATUS_COMPLETED = "Completed";
    public static final String JOB_RESTRIC_BILLABLE = "BillableDay";
    public static final String SUPERVISOR_JOB = "supervisor_job_true";

    /* Enum Replacements - because enums are corrupt. */
    /* DWR Status keys */
    public static final int DWR_STATUS_NEW = 0;
    public static final int DWR_STATUS_DRAFT = 1;
    public static final int DWR_STATUS_QUEUED = 2;
    public static final int DWR_STATUS_PENDING = 3;
    public static final int DWR_STATUS_BOUNCED = 4;
    public static final int DWR_STATUS_APPROVED = 5;
    public static final int DWR_STATUS_PROCESSED = 6;
    /* Server Side DWR Status values */
    public static final int DWR_STATUS_API_AssignedId = 0;
    public static final int DWR_STATUS_API_InProgressId = 1;
    public static final int DWR_STATUS_API_SubmittedId = 2;
    public static final int DWR_STATUS_API_ChangesRequiredId = 3;
    public static final int DWR_STATUS_API_ResubmittedId = 6;
    public static final int DWR_STATUS_API_ApprovedId = 4;
    public static final int DWR_STATUS_API_RejectedId = 5;
    /* DWR Classification key - matches the classification_name array order. */
    public static final int DWR_TYPE_BILLABLE_DAY = 0;
    public static final int DWR_TYPE_JOB_SETUP = 1;
    public static final int DWR_TYPE_TRAVEL = 2;
    public static final int DWR_TYPE_CONFERENCE_CALL = 3;
    public static final int DWR_TYPE_DRUG_TEST = 4;
    public static final int DWR_TYPE_RULES_CLASS = 5;
    public static final int DWR_TYPE_FIELD_TRAINING = 6;
    public static final int DWR_TYPE_OTHER_NONBILLABLE = 7;
    public static final int DWR_TYPE_TERRITORY_QUALIFICATION = 8;
    public static final int DWR_TYPE_BILLABLE_DAY_UTILITY = 9;
    /* DWR Flagging and Utility keys */
    public static final int RP_FLAGGING_SERVICE = 0;
    public static final int RP_UTILITY_SERVICE = 1;
    public static final int RP_COVER_SERVICE = 2;
    public static final int RP_ROADWAY_FLAGGING_SERVICE = 3;
    /* Event Queue Types and defaults */
    public static final int UPLOADING_FALSE = 0;
    public static final int UPLOADING_TRUE = 1;
    public static final int Q_TYPE_DWR_SAVE = 1;
    public static final int Q_TYPE_JOB_SAVE = 2;
    public static final int Q_TYPE_COVER_SAVE = 3;
    public static final int Q_TYPE_UTIL_JOB_SAVE = 4;
    public static final int Q_TYPE_DWR_ROADWAY_FLAGGING_SAVE = 5;
    public static final int Q_PENDING_MINUTES = 3;
    public static final int Q_PENDING_SECONDS = 60;
    public static final int HIGH_PRIORITY_EVENT = 0;
    public static final int MEDIUM_PRIORITY_EVENT = 5;
    public static final int LOW_PRIORITY_EVENT = 10;
    /* Document Types.  Documents are stored as files locally. */
    public static final int DOC_OWNER_JOB = 0;      // Any files provided by the backend and linked to a job.
    public static final int DOC_IMAGE_DWR = 1;      // Most of the pictures associated with a DWR.
    public static final int DOC_IMAGE_FLASH = 2;    // The pictures taken for flash audits.
    public static final int DOC_SIGNATURES = 3;     // The files that store images of the signatures.
    public static final int DOC_IMAGE_SIGNIN = 4;   // Special DWR image for CSX (sign in sheet).
    public static final String FILE_SYS_ISSUE = "FILE_SYSTEM";  // Used as the name of an exception.
    public static final String SIGNATURE_FILE_PATH = "/saved_signature";
    /* Form and Field Types */
    public static final int FORM_DWR_TYPE = 1;  // Daily Work Report
    public static final int FORM_JSF_TYPE = 2;  // Job Setup Form
    public static final int FIELD_JSF_TYPE = 1; // Job Setup Field
    public static final int FIELD_JSF_SIGN_RWIC = -1;   // Some JSF questions are not server controlled.
    public static final int FIELD_JSF_RWIC_DATE = -2;
    public static final int FIELD_JSF_JOBNBR = -3;
    //    /* Audit/Form Status */
    public static final int AUDIT_STATUS_ASSIGNED = 0;
    public static final int AUDIT_STATUS_IN_PROGRESS = 1;
    public static final int AUDIT_STATUS_SUBMITTED = 2;
    public static final int AUDIT_STATUS_CHANGES_REQUIRED = 3;
    public static final int AUDIT_STATUS_RESUBMITTED = 6;
    public static final int AUDIT_STATUS_APPROVED = 4;
    public static final int AUDIT_STATUS_REJECTED = 5;
    /* Question Types */
    public static final int QUESTION_TYPE_UserInput = 0;
    public static final int QUESTION_TYPE_PickOne = 1;
    public static final int QUESTION_TYPE_PickMany = 2;
    public static final int QUESTION_TYPE_FormHeader = 3;
    public static final String QUESTION_TYPE_FormHeaderStr = "FormHeader";
    public static final int QUESTION_TYPE_Audit = 4;
    public static final int QUESTION_TYPE_Date = 6;
    public static final int QUESTION_TYPE_Time = 7;
    public static final int QUESTION_TYPE_DateTime = 8;
    public static final int QUESTION_TYPE_Integer = 9;
    public static final int QUESTION_TYPE_Decimal2 = 10;
    public static final int QUESTION_TYPE_YesNo = 11;
    public static final int QUESTION_TYPE_Signature = 12;
    public static final int QUESTION_TYPE_Photo = 13;
    public static final int QUESTION_TYPE_DB_JobNumber_readonly = 14;
    public static final int QUESTION_TYPE_DB_RailroadCode_readonly = 15;
    public static final int QUESTION_TYPE_DB_RwicName_readonly = 16;
    public static final int QUESTION_TYPE_DB_RwicEmployeeCode_readonly = 17;
    public static final int QUESTION_TYPE_DB_CustomerCompanyName_readonly = 18;
    public static final int QUESTION_TYPE_DB_PointOfContactName_editable = 19;
    public static final int QUESTION_TYPE_DB_PointOfContactPhone_editable = 20;
    public static final int QUESTION_TYPE_DB_JobDivision_editable = 21;
    public static final int QUESTION_TYPE_DB_JobSubdivision_editable = 22;
    public static final int QUESTION_TYPE_DB_JobStartDate_editable = 23;
    public static final int QUESTION_TYPE_DB_JobDuration_editable = 24;
    public static final int QUESTION_TYPE_DB_JobDescription_editable = 25;
    public static final int QUESTION_TYPE_DB_JobMilePost_editable = 26;

    /* Constants that are hard to categorize. */
    public static final String DASHED_RANGE = "%s - %s";
    public static final int AUDIT_TEMPLATE_ID = 7;
    public static final String SYMBOL_REQUIRED = "*";
    public static final String MAP_WITH_COORDINATES = "geo:0,0?q=%s,%s(%s)";
    public static final String MAP_NAME_COORDINATES = "Job #%s";
    public static final String MIDNIGHT = "00:00:00";
    public static final String MIDMORNING = "06:00:00";
    public static final String RESOURCE_TYPE_ID = "id";
    public static final String IMGAGE_EXTENTION = ".jpg";
    public static final String IMAGE_FLASH = "flash_audit";
    public static final String IMAGE_SIGNUP = "signup_sheet";
    public static final String IMAGE_WORKSITE = "work_site";
    public static final String FILE_SEPARATOR = "/";
    public static final int IMAGE_COMPRESSION = 30;
    public static final int IMAGE_FLASH_MAX_PHOTOS = 5;
    public static final int IMAGE_DWR_MAX_PHOTOS = 5;
    public static final int IMAGE_DWR_CXS_MAX_PHOTOS = 8;
    public static final int IMAGE_DWR_CXS_MIN_PHOTOS = 6;
    public static final String JOBDOC_FILE_PREFIX = "jobdoc_%s_%s";
    public static final String DELIMIT_CONTACTS = "?";
    public static final String DELIMIT_CONTACTS_PH = "--";
    public static final String DELIMIT_CONTACTS_REG = "\\" + DELIMIT_CONTACTS;
    public static final String DELIMIT_COSTCENTER = "?";
    public static final String DELIMIT_COSTCENTER_REG = "\\" + DELIMIT_COSTCENTER;
    public static final String DELIMIT_COSTCENTER_ITEM = ":";
    public static final String COSTCENTER_LINE = "%d:%s:%s:%s" + DELIMIT_COSTCENTER;
    public static final int UI_GROUP_HIDDEN = 25;
    public static final String DELIMIT_GPS_COORDINATES = ";";
    public static final String DELIMIT_GPS_FORMATED = "%f" + DELIMIT_GPS_COORDINATES + "%f";
    public static final String DELIMIT_GPS_REGEX = "[" + DELIMIT_GPS_COORDINATES + "]";

    /* DWR Field Codes - These need to match what is on the server.
    *  It provides a way to map the local data to the server's
    *  collection of questions and answers. */
    public final static String F_Property = "inputProperty";
    public final static String F_RailroadContact = "inputRailroadContact";
    public final static String F_LocationCity = "inputLocationCity";
    public final static String F_LocationState = "inputLocationState";
    public final static String F_ContractorName = "inputContractorName";
    public final static String F_CNDataNSOCC = "inputCNDataNSOCC";
    public final static String F_CNDataNetwork = "inputCNDataNetwork";
    public final static String F_CNDataCounty = "inputCNDataCounty";
    public final static String F_CSXDataRegion = "inputCSXDataRegion";
    public final static String F_CSXDataOpNbr = "inputCSXDataOpNbr";
    public final static String F_RWICPHONE = "inputRwicPhone";
    public final static String F_CSXShiftNew = "inputCSXShiftNew";
    public final static String F_CSXShiftRelief = "inputCSXShiftRelief";
    public final static String F_CSXShiftRelieved = "inputCSXShiftRelieved";
    public final static String F_WorkLunch = "inputWorkLunchTime";
    public final static String F_WorkBriefTime = "inputWorkBriefTime";
    public final static String F_CSXPeopleRow = "inputCSXPeopleRow";
    public final static String F_CSXEquipmentRow = "inputCSXEquipmentRow";
    public final static String F_WeatherHighDesc = "inputDescWeatherHigh";
    public final static String F_WeatherLowDesc = "inputDescWeatherLow";
    public final static String F_RoadMasterPhone = "inputRoadMasterPhone";
    public final static String F_KCSDataContractorCnt = "inputKCSDataContractorCnt";
    public final static String F_UPDataDotXing = "inputUPDataDotXing";
    public final static String F_UPDataFolderNbr = "inputUPDataFolderNbr";
    public final static String F_UPDataServiceUnit = "inputUPDataServiceUnit";
    public final static String F_KCTDataTaskOrder = "inputKCTDataTaskOrder";
    public final static String F_InputWMLine = "inputWMLine";
    public final static String F_InputWMStation = "inputWMStation";
    public final static String F_InputWMStationName = "inputWMStationName";
    public final static String F_WMTrack = "inputWMTrack";
    public final static String F_TypeofVehicle = "inputTypeOfVehicle";

    public final static String F_IsOngoing = "inputIsOngoing";
    public final static String F_RoadMaster = "inputRoadMaster";
    public final static String F_District = "inputDistrict";
    public final static String F_Subdivision = "inputSubdivision";
    public final static String F_MpStart = "inputMpStart";
    public final static String F_MpEnd = "inputMpEnd";
    public final static String F_WorkingTrack = "inputWorkingTrack";
    public final static String F_WorkOnTrack = "inputWorkOnTrack";
    public final static String F_Is707 = "inputIs707";
    public final static String F_Is1102 = "inputIs1102";
    public final static String F_Is1107 = "inputIs1107";
    public final static String F_IsEC1 = "inputIsEC1";
    public final static String F_IsFormB = "inputIsFormB";
    public final static String F_IsFormC = "inputIsFormC";
    public final static String F_IsFormW = "inputIsFormW";
    public final static String F_IsForm23 = "inputIsForm23";
    public final static String F_IsForm23Y = "inputIsForm23Y";
    public final static String F_IsDerails = "inputIsDerails";
    public final static String F_IsTrackTime = "inputIsTrackTime";
    public final static String F_IsTrackWarrant = "inputIsTrackWarrant";
    public final static String F_IsObserver = "inputIsObserver";
    public final static String F_IsTrackAuthority = "inputIsTrackAuthority";
    public final static String F_IsNoProtection = "inputIsNoProtection";
    public final static String F_IsVerbalPermission = "inputIsVerbalPermission";
    public final static String F_IsLiveFlagman = "inputIsLiveFlagman";
    public final static String F_IsLookout = "inputIsLookout";

    public final static String F_Description = "inputDescription";
    public final static String F_DescWeatherConditions = "inputDescWeatherConditions";
    public final static String F_DescTypeOfWork = "inputDescTypeOfWork";
    public final static String F_DescInsideRoW = "inputDescInsideRoW";
    public final static String F_DescOutsideRoW = "inputDescOutsideRoW";
    public final static String F_DescUnusual = "inputDescUnusual";
    public final static String F_DescLocationStart = "inputDescLocationStart";
    public final static String F_WorkPlanned = "inputDescWorkPlanned";
    public final static String F_DescSafety = "inputDescSafety";

    public final static String F_SigninSheet = "inputSigninSheet";
    
    public final static String F_SitePhotoI = "inputSitePhotoI";
    public final static String F_SitePhotoII = "inputSitePhotoII";
    public final static String F_SitePhotoIII = "inputSitePhotoIII";
    public final static String F_SitePhotoIV = "inputSitePhotoIV";
    public final static String F_SitePhotoV = "inputSitePhotoV";
    public final static String F_SitePhotoVI = "inputSitePhotoVI";
    public final static String F_SitePhotoVII = "inputSitePhotoVII";
    public final static String F_SitePhotoVIII = "inputSitePhotoVIII";

    public final static String F_SiteCommentI = "inputSiteCommentI";
    public final static String F_SiteCommentII = "inputSiteCommentII";
    public final static String F_SiteCommentIII = "inputSiteCommentIII";
    public final static String F_SiteCommentIV = "inputSiteCommentIV";
    public final static String F_SiteCommentV = "inputSiteCommentV";
    public final static String F_SiteCommentVI = "inputSiteCommentVI";
    public final static String F_SiteCommentVII = "inputSiteCommentVII";
    public final static String F_SiteCommentVIII = "inputSiteCommentVIII";

    public final static String F_WorkHoursRounded = "inputWorkHoursRounded";
    public final static String F_WorkStartTime = "inputWorkStartTime";
    public final static String F_WorkEndTime = "inputWorkEndTime";
    public final static String F_SpecialCostCenter = "inputSpecialCostCenter";

    public final static String F_TravelToJobStartTime = "inputTravelToJobStartTime";
    public final static String F_TravelToJobEndTime = "inputTravelToJobEndTime";
    public final static String F_TravelToJobHours = "inputTravelToJobHours";
    public final static String F_TravelFromJobStartTime = "inputTravelFromJobStartTime";
    public final static String F_TravelFromJobEndTime = "inputTravelFromJobEndTime";
    public final static String F_TravelFromJobHours = "inputTravelFromJobHours";
    public final static String F_TravelToJobMiles = "inputTravelToJobMiles";
    public final static String F_TravelOnJobMiles = "inputTravelOnJobMiles";
    public final static String F_TravelFromJobMiles = "inputTravelFromJobMiles";
    public final static String F_PerDiem = "inputPerDiem";
    public final static String F_ConstructionDay = "inputConstructionDay";
    public final static String F_InputTotalWorkDays = "inputTotalWorkDays";
    public final static String F_DescWeatherWind = "inputDescWeatherWind";
    public final static String F_DescWeatherRain = "inputDescWeatherRain";

    public final static String F_VersionInformation = "inputVersionInformation";
    public final static String F_Profiles = "inputProfiles";

    public final static String F_EightyTwoT = "inputEightyTwoT";
    public final static String F_StreetName = "inputStreetName";
    public final static String F_MilePostsForStreet = "inputMilePostsForStreet";
}
