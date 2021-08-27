package com.railprosfs.railsapp.data_layout;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DwrTbl {
    // All dates UTC in database.
    @PrimaryKey(autoGenerate = true)
    public int DwrId;                 // The local database id
    public int DwrSrvrId;             // The remote (backend) database id
    public int Classification;        // TypeWS of DWR (index into <string-array name="classification_name">)
    @NonNull
    public String WorkDate;           // Day work was performed (time can vary)
    @NonNull
    public String UserId;             // Friendly employee id
    public int WorkId;                // Server key of employee
    public int JobId;                 // Job id from server
    public String JobNumber;          // The display name of the job (typically a number)
    public int Status;                // Local status. Does not exactly match server status.
    public String StatusMessage;      // Used to relay some information to the user on failure conditions.
    public String RwicSignatureName;  // File name where the signature is stored (path not included.)
    public String RwicSignatureDate;  // Date of signature
    public String RailSignatureName;  // File name where the signature is stored (path not included.)
    public String RailSignatureDate;  // Date of signature
    public String ClientSignatureName;// File name where the signature is stored (path not included.)
    public String ClientSignatureDate;// Date of signature
    public String ClientName;         // Client = Whoever signed.
    public String ClientPhone;        // Formatted phone number
    public String ClientEmail;        // Email of the client
    public String NonBilledReason;    // Extra information added when Classification is "Non-billable".
    public boolean NotPresentOnTrack;   // RWIC not at work site, reduces liability
    // The Review information is only updated on the backend.
    public String ReviewerNotes;      // When a DWR is needs a change, the supervisor returns it with a note.
    public String ReviewerOn;         // This date is not locally generated, so it is not (re)formatted.
    public int ReviewerId;            // Server key of employee that reviewed DWR
    public String ReviewerName;       // Seems to be the first and last name of the reviewer.

    // The fields below all require explicit answer keys because they
    // are stored on the server as rows in a table and not just attributes
    // of a row.
    public String LocationCity;     // Response to question
    public int LocationCityKey;     // Server Answer Key
    public String LocationState;    // State (useful for calculating taxes)
    public int LocationStateKey;
    public String ContractorName;   // Customer name
    public int ContractorNameKey;
    public String CNDataNSOCC;      // CN  Specific
    public int CNDataNSOCCKey;
    public String CNDataNetwork;    // CN  Specific
    public int CNDataNetworkKey;
    public String CNDataCounty;     // CN  Specific
    public int CNDataCountyKey;
    public int CSXDataRegion;       // CSX Specific
    public int CSXDataRegionKey;
    public String CSXDataOpNbr;     // CSX Specific
    public int CSXDataOpNbrKey;
    public int KCSDataContractorCnt;// KCS Specific
    public int KCSDataContractorCntKey;
    public String UPDataDotXing;    // UP  Specific
    public int UPDataDotXingKey;
    public String UPDataFolderNbr;  // UP  Specific
    public int UPDataFolderNbrKey;
    public String UPDataServiceUnit;// UP  Specific
    public int UPDataServiceUnitKey;
    public String KCTDataTaskOrder; // KCT Specific
    public int KCTDataTaskOrderKey;
    public int Property;            // Railroad (see Railroads class)
    public int PropertyKey;
    public String RoadMaster;       // Railroad Boss
    public int RoadMasterKey;
    public String Subdivision;      // Section of Railroad
    public int SubdivisionKey;
    public String MpStart;          // Mp = mile post
    public int MpStartKey;
    public String MpEnd;
    public int MpEndKey;
    public boolean Is707;           // Protection
    public int Is707Key;
    public boolean Is1102;          // Protection
    public int Is1102Key;
    public boolean Is1107;          // Protection
    public int Is1107Key;
    public boolean IsEC1;           // Protection
    public int IsEC1Key;
    public boolean IsFormB;         // Protection
    public int IsFormBKey;
    public boolean IsFormC;         // Protection
    public int IsFormCKey;
    public boolean IsFormW;         // Protection
    public int IsFormWKey;
    public boolean IsForm23;        // Protection
    public int IsForm23Key;
    public boolean IsForm23Y;       // Protection
    public int IsForm23YKey;
    public boolean IsDerails;       // Protection
    public int IsDerailsKey;
    public boolean IsTrackTime;     // Protection
    public int IsTrackTimeKey;
    public boolean IsTrackWarrant;  // Protection
    public int IsTrackWarrantKey;
    public boolean IsLookout;       // Protection
    public int IsLookoutKey;
    public boolean IsObserver;      // Protection
    public int IsObserverKey;
    public boolean IsTrackAuthority;// Protection
    public int IsTrackAuthorityKey;
    public boolean IsNoProtection;  // Protection
    public int IsNoProtectionKey;
    public boolean IsLiveFlagman;   // Protection
    public int IsLiveFlagmanKey;
    public boolean IsVerbalPermission; // Protection
    public int IsVerbalPermissionKey;
    public int WorkOnTrack;         // Foul/PotentailFoul/NoFoul
    public int WorkOnTrackKey;
    public String Description;      // Description of work performed
    public int DescriptionKey;
    public String DescWeatherConditions;  // Weather
    public int DescWeatherConditionsKey;
    public String DescTypeOfWork;   // CSX specific
    public int DescTypeOfWorkKey;
    public String DescInsideRoW;    // CSX specific
    public int DescInsideRoWKey;
    public String DescOutsideRoW;   // CSX specific
    public int DescOutsideRoWKey;
    public String DescUnusual;      // CSX specific
    public int DescUnusualKey;
    public String DescLocationStart;// CSX specific
    public int DescLocationStartKey;
    public String WorkStartTime;    // Stored as UTC date
    public int WorkStartTimeKey;
    public String WorkEndTime;      // Stored as UTC date
    public int WorkEndTimeKey;
    public double WorkHoursRounded; // The total hours worked, rounded to the half hour
    public int WorkHoursRoundedKey;
    public int NotPresentOnTrackKey;        // This ended up being a fixed field, so we did not end up using this key.
    public String TravelToJobStartTime;     // Stored as UTC date
    public int TravelToJobStartTimeKey;
    public String TravelToJobEndTime;       // Stored as UTC date
    public int TravelToJobEndTimeKey;
    public double TravelToHoursRounded;     // The total hours traveled, rounded to the half hour
    public int TravelToHoursRoundedKey;
    public String TravelFromJobStartTime;   // Stored as UTC date
    public int TravelFromJobStartTimeKey;
    public String TravelFromJobEndTime;     // Stored as UTC date
    public int TravelFromJobEndTimeKey;
    public double TravelFromHoursRounded;   // The total hours traveled, rounded to the half hour
    public int TravelFromHoursRoundedKey;
    public int TravelToJobMiles;    // Miles to Job.
    public int TravelToJobMilesKey;
    public int TravelOnJobMiles;    // Miles on Job.
    public int TravelOnJobMilesKey;
    public int TravelFromJobMiles;  // Miles to Home.
    public int TravelFromJobMilesKey;
    public int PerDiem;             // None/Standard/Meals/Booking(Traveliance)
    public int PerDiemKey;
    public boolean IsOngoing;       // API decided to reverse the meaning of this flag, but renaming here would be hard.  Imagine the real name as "IsJobComplete".
    public int IsOngoingKey;
    // The photos are saved as files and kept track of in DocumentTbl.  Look them up using the DwrId.
    public int SitePhotoIKey;
    public int SiteCommentIKey;

    public int SitePhotoIIKey;
    public int SiteCommentIIKey;

    public int SitePhotoIIIKey;
    public int SiteCommentIIIKey;

    public int SitePhotoIVKey;
    public int SiteCommentIVKey;

    public int SitePhotoVKey;
    public int SiteCommentVKey;

    public int SitePhotoVIKey;
    public int SiteCommentVIKey;

    public int SitePhotoVIIKey;
    public int SiteCommentVIIKey;

    public int SitePhotoVIIIKey;
    public int SiteCommentVIIIKey;

    public int SignInKey;
    @NonNull
    public String InputWMLine;
    public int InputWMLineKey;
    @NonNull
    public String InputWMStation;
    public int InputWMStationKey;
    @NonNull
    public String InputWMStationName;
    public int InputWMStationNameKey;
    public int InputWMTrack;
    public int InputWMTrackKey;

    @NonNull
    public String  RwicPhone;
    public int     RwicPhoneKey;
    public boolean CSXShiftNew = false;
    public int    CSXShiftNewKey;
    public boolean CSXShiftRelief = false;
    public int    CSXShiftReliefKey;
    @NonNull
    public String CSXShiftRelieved;
    public int    CSXShiftRelievedKey;
    @NonNull
    public String WorkLunchTime;
    public int    WorkLunchTimeKey;
    public int    CSXPeopleRow;
    public int    CSXPeopleRowKey;
    public int    CSXEquipmentRow;
    public int    CSXEquipmentRowKey;
    public int    DescWeatherHigh;
    public int    DescWeatherHighKey;
    public int    DescWeatherLow;
    public int    DescWeatherLowKey;
    @NonNull
    public String WorkBriefTime;
    public int    WorkBriefTimeKey;
    @NonNull
    public String RoadMasterPhone;
    public int    RoadMasterPhoneKey;
    @NonNull
    public String DescWorkPlanned;
    public int    DescWorkPlannedKey;
    @NonNull
    public String DescSafety;
    public int    DescSafetyKey;
    @NonNull
    public String TypeOfVehicle;
    public int TypeOfVehicleKey;
    @NonNull
    public String VersionInformation;
    public int VersionInformationKey;
    public boolean PerformedTraining = false;

    public String ConstructionDay;
    public Integer ConstructionDayKey;
    public String InputTotalWorkDays;
    public Integer InputTotalWorkDaysKey;
    public String InputDescWeatherWind;
    public Integer InputDescWeatherWindKey;
    public String InputDescWeatherRain;
    public Integer InputDescWeatherRainKey;
    public String SpecialCostCenter;
    public Integer SpecialCostCenterKey;    // While not a field, cost centers do have an extra db id.
    public String RailroadContact;
    public Integer RailroadContactKey;
    public String WorkingTrack;
    public Integer WorkingTrackKey;
    public String District;
    public Integer DistrictKey;

    public boolean HasRoadwayFlagging = false;
    public Integer EightyTwoTKey;
    public String EightyTwoT;
    public Integer StreetNameKey;
    public String StreetName;
    public Integer MilePostsForStreetKey;
    public String MilePostsForStreet;

    /**
     *  The potential for null values in this app has been a constant headache.
     *  Every field that might be null (String) in this record should have a default value.
     *  -- If adding a new value that is a String, put it in the constructor. --
     */
    public DwrTbl() {
        WorkDate="";
        UserId="";
        JobNumber="";
        StatusMessage="";
        RwicSignatureName="";
        RwicSignatureDate="";
        RailSignatureName="";
        RailSignatureDate="";
        ClientSignatureName="";
        ClientSignatureDate="";
        ClientName="";
        ClientPhone="";
        ClientEmail="";
        NonBilledReason="";
        ReviewerNotes="";
        ReviewerOn="";
        ReviewerName="";
        LocationCity="";
        LocationState="";
        ContractorName="";
        CNDataNSOCC="";
        CNDataNetwork="";
        CNDataCounty="";
        CSXDataOpNbr="";
        UPDataDotXing="";
        UPDataFolderNbr="";
        UPDataServiceUnit="";
        KCTDataTaskOrder="";
        RoadMaster="";
        Subdivision="";
        MpStart="";
        MpEnd="";
        Description="";
        DescWeatherConditions="";
        DescTypeOfWork="";
        DescInsideRoW="";
        DescOutsideRoW="";
        DescUnusual="";
        DescLocationStart="";
        WorkStartTime="";
        WorkEndTime="";
        TravelToJobStartTime="";
        TravelToJobEndTime="";
        TravelFromJobStartTime="";
        TravelFromJobEndTime="";
        InputWMLine="";
        InputWMStation="";
        InputWMStationName="";
        RwicPhone="";
        CSXShiftRelieved="";
        WorkLunchTime="";
        WorkBriefTime="";
        RoadMasterPhone="";
        DescWorkPlanned="";
        DescSafety="";
        TypeOfVehicle="";
        VersionInformation="";
        ConstructionDay = "";
        InputTotalWorkDays = "";
        InputDescWeatherWind = "";
        InputDescWeatherRain = "";
        ConstructionDay = "";
        InputTotalWorkDays = "";
        InputDescWeatherWind = "";
        InputDescWeatherRain = "";
        SpecialCostCenter = "";
        RailroadContact = "";
        WorkingTrack = "";
        District = "";

        ConstructionDayKey = 0;
        InputTotalWorkDaysKey = 0;
        InputDescWeatherWindKey = 0;
        InputDescWeatherRainKey = 0;
        SpecialCostCenterKey = 0;
        RailroadContactKey = 0;
        WorkingTrackKey = 0;
        DistrictKey = 0;

        EightyTwoTKey = 0;
        EightyTwoT = "";
        StreetNameKey = 0;
        StreetName = "";
        MilePostsForStreetKey = 0;
        MilePostsForStreet = "";
    }
}

