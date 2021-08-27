package com.railprosfs.railsapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Base64;

import com.google.gson.Gson;
import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.FieldPlacementTbl;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.service.IWebServices;
import com.railprosfs.railsapp.service.WebServiceModels;
import com.railprosfs.railsapp.service.WebServices;
import com.railprosfs.railsapp.ui_support.LayoutHelperDwr;
import com.railprosfs.railsapp.utility.Connection;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.railprosfs.railsapp.service.WebServiceModels.*;
import static com.railprosfs.railsapp.utility.Constants.*;

public class DwrTemplateMgr {
    private final Context mContext;
    private final Map<String, Integer> mDwrQuestions;
    private ScheduleDB db;
    public DwrTemplateMgr(Context context, Map<String, Integer> questions) {
        mContext = context;
        mDwrQuestions = questions;
        this.db = ScheduleDB.getDatabase(context);
    }

    /**
     * Call to save key information used to get the DWR form.
     *
     * @param id - The id on the server of the dwr question form.
     */
    public void RegisterDwrForm(String key, int id) {
        SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = registered.edit();
        editor.putInt(key, id);
        editor.apply();
    }

    /**
     * Returns the server id of the dwr template questions.
     */
    public int GetDwrTemplateId(String key) {
        SharedPreferences registered = mContext.getSharedPreferences(SP_REG_STORE, Context.MODE_PRIVATE);
        return registered.getInt(key, 0);
    }

    public String GetDwrTemplateIdStr(String key) {
        return String.valueOf(GetDwrTemplateId(key));
    }


    /**
     * Transfer the API data into a set of table rows and return. This can
     * be used to save the questions locally, since they rarely change.
     *
     * @param questions These are the questions for the DWR.
     */
    public List<FieldPlacementTbl> MapDwrQuestions(List<QuestionWS> questions) {

        List<FieldPlacementTbl> allFields = new ArrayList<>();
        for (QuestionWS field : questions) {
            FieldPlacementTbl fieldPlacementTbl = new FieldPlacementTbl();
            fieldPlacementTbl.Id = 0;
            fieldPlacementTbl.TemplateId = field.template.id;
            fieldPlacementTbl.TemplateName = field.template.name;
            fieldPlacementTbl.FieldId = field.id;
            fieldPlacementTbl.FieldType = field.field.type;
            fieldPlacementTbl.FieldPrompt = field.field.prompt;
            fieldPlacementTbl.FieldInstructions = field.field.instructions;
            fieldPlacementTbl.FieldOptions = ""; //field.field.options;
            fieldPlacementTbl.Required = field.isRequired;
            fieldPlacementTbl.Group = field.group;
            fieldPlacementTbl.Note = field.note;
            fieldPlacementTbl.Code = field.usageCode;
            allFields.add(fieldPlacementTbl);
        }
        return allFields;
    }

    /*
        Similar to the UpdateFieldKeys, this will update the Cost Center Keys.  Although
        initially, there is only 1 per DWR.
     */
    public DwrTbl UpdateCostCenterKeys(DwrTbl dwrTbl,  List<DwrCostCenter> costCenters) {
        // If no cost center, no key.
        if (costCenters.size() == 0) {
            dwrTbl.SpecialCostCenterKey = 0;
        }

        for (DwrCostCenter cc : costCenters){
            dwrTbl.SpecialCostCenterKey = cc.id;
        }
        return dwrTbl;
    }

    /**
     * This will update all the answers and answer keys stored locally, from data on the
     * server.  Primarily this is needed for later updates to these fields.  For example,
     * if you want to update a field on the backend, you will need to supply the database
     * id (here called the "key").  We therefore need to get those keys after an add and
     * store them in our database.
     * Also, if doing a full DWR sync from the server, this can be used with the keyOnly
     * set to false.  It will fill in the response (actual data), too.
     *
     * @param dwr     A DWR table record that needs its variable field filled in.
     * @param fields  The data from the server.
     * @param keyOnly If set to true, only update the key values.
     */
    public DwrTbl UpdateFieldKeys(DwrTbl dwr, List<DwrAnswerWS> fields, boolean keyOnly) {
        boolean noResponse;

        for (DwrAnswerWS f : fields) {
            // The noResponse=true lets us skip copying data from API, which helps
            // avoid formatting exceptions and getting null values.
            noResponse = f.response == null || f.response.length() == 0;

            if(f.usageCode == null ){
                continue;
            }

            if (f.usageCode.equalsIgnoreCase(F_LocationCity)) {
                dwr.LocationCityKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.LocationCity = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_LocationState)) {
                dwr.LocationStateKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.LocationState = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_ContractorName)) {
                dwr.ContractorNameKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.ContractorName = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_CNDataNSOCC)) {
                dwr.CNDataNSOCCKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.CNDataNSOCC = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_CNDataNetwork)) {
                dwr.CNDataNetworkKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.CNDataNetwork = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_CNDataCounty)) {
                dwr.CNDataCountyKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.CNDataCounty = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_CSXDataRegion)) {
                dwr.CSXDataRegionKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.CSXDataRegion = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_CSXDataOpNbr)) {
                dwr.CSXDataOpNbrKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.CSXDataOpNbr = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_KCSDataContractorCnt)) {
                dwr.KCSDataContractorCntKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.KCSDataContractorCnt = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_UPDataDotXing)) {
                dwr.UPDataDotXingKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.UPDataDotXing = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_UPDataFolderNbr)) {
                dwr.UPDataFolderNbrKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.UPDataFolderNbr = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_UPDataServiceUnit)) {
                dwr.UPDataServiceUnitKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.UPDataServiceUnit = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_KCTDataTaskOrder)) {
                dwr.KCTDataTaskOrderKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.KCTDataTaskOrder = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TypeofVehicle)) {
                dwr.TypeOfVehicleKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.TypeOfVehicle = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_Property)) {
                dwr.PropertyKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.Property = Railroads.PropertyKeyServer(mContext, f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_RoadMaster)) {
                dwr.RoadMasterKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.RoadMaster = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_District)) {
                dwr.DistrictKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.District = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_Subdivision)) {
                dwr.SubdivisionKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.Subdivision = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_MpStart)) {
                dwr.MpStartKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.MpStart = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_MpEnd)) {
                dwr.MpEndKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.MpEnd = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_WorkingTrack)) {
                dwr.WorkingTrackKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.WorkingTrack = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_RailroadContact)) {
                dwr.RailroadContactKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.RailroadContact = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_Is707)) {
                dwr.Is707Key = f.id;
                if(keyOnly || noResponse) continue;
                dwr.Is707 = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_Is1102)) {
                dwr.Is1102Key = f.id;
                if(keyOnly || noResponse) continue;
                dwr.Is1102 = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_Is1107)) {
                dwr.Is1107Key = f.id;
                if(keyOnly || noResponse) continue;
                dwr.Is1107 = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsEC1)) {
                dwr.IsEC1Key = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsEC1 = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsFormB)) {
                dwr.IsFormBKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsFormB = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsFormC)) {
                dwr.IsFormCKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsFormC = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsFormW)) {
                dwr.IsFormWKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsFormW = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsForm23)) {
                dwr.IsForm23Key = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsForm23 = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsForm23Y)) {
                dwr.IsForm23YKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsForm23Y = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsDerails)) {
                dwr.IsDerailsKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsDerails = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsTrackTime)) {
                dwr.IsTrackTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsTrackTime = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsTrackWarrant)) {
                dwr.IsTrackWarrantKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsTrackWarrant = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsLookout)) {
                dwr.IsLookoutKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsLookout = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsObserver)) {
                dwr.IsObserverKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsObserver = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsTrackAuthority)) {
                dwr.IsTrackAuthorityKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsTrackAuthority = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsNoProtection)) {
                dwr.IsNoProtectionKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsNoProtection = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsLiveFlagman)) {
                dwr.IsLiveFlagmanKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsLiveFlagman = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsVerbalPermission)) {
                dwr.IsVerbalPermissionKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsVerbalPermission = Boolean.parseBoolean(f.response);
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_WorkOnTrack)) {
                dwr.WorkOnTrackKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.WorkOnTrack = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_Description)) {
                dwr.DescriptionKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.Description = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_DescWeatherConditions)) {
                dwr.DescWeatherConditionsKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.DescWeatherConditions = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_DescTypeOfWork)) {
                dwr.DescTypeOfWorkKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.DescTypeOfWork = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_DescInsideRoW)) {
                dwr.DescInsideRoWKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.DescInsideRoW = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_DescOutsideRoW)) {
                dwr.DescOutsideRoWKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.DescOutsideRoW = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_DescUnusual)) {
                dwr.DescUnusualKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.DescUnusual = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_DescLocationStart)) {
                dwr.DescLocationStartKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.DescLocationStart = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_WorkStartTime)) {
                dwr.WorkStartTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.substring(0, f.response.indexOf(" "));
                try {
                    holdR = KTime.ParseToFormat(holdR, KTime.KT_fmtDate3339fk, KTime.UTC_TIMEZONE, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
                } catch (ExpParseToCalendar expParseToCalendar) {
                    ExpClass.LogEX(expParseToCalendar, F_WorkStartTime);
                }
                dwr.WorkStartTime = holdR;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_WorkEndTime)) {
                dwr.WorkEndTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.substring(0, f.response.indexOf(" "));
                try {
                    holdR = KTime.ParseToFormat(holdR, KTime.KT_fmtDate3339fk, KTime.UTC_TIMEZONE, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
                } catch (ExpParseToCalendar expParseToCalendar) {
                    ExpClass.LogEX(expParseToCalendar, F_WorkEndTime);
                }
                dwr.WorkEndTime = holdR;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_WorkHoursRounded)) {
                dwr.WorkHoursRoundedKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.WorkHoursRounded = Double.parseDouble(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelToJobStartTime)) {
                dwr.TravelToJobStartTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.substring(0, f.response.indexOf(" "));
                try {
                    holdR = KTime.ParseToFormat(holdR, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
                } catch (ExpParseToCalendar expParseToCalendar) {
                    ExpClass.LogEX(expParseToCalendar, F_TravelToJobStartTime);
                }
                dwr.TravelToJobStartTime = holdR;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelToJobEndTime)) {
                dwr.TravelToJobEndTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.substring(0, f.response.indexOf(" "));
                try {
                    holdR = KTime.ParseToFormat(holdR, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
                } catch (ExpParseToCalendar expParseToCalendar) {
                    ExpClass.LogEX(expParseToCalendar, F_TravelToJobEndTime);
                }
                dwr.TravelToJobEndTime = holdR;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelFromJobHours)) {
                dwr.TravelFromHoursRoundedKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.TravelFromHoursRounded = Double.parseDouble(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelFromJobStartTime)) {
                dwr.TravelFromJobStartTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.substring(0, f.response.indexOf(" "));
                try {
                    holdR = KTime.ParseToFormat(holdR, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
                } catch (ExpParseToCalendar expParseToCalendar) {
                    ExpClass.LogEX(expParseToCalendar, F_TravelFromJobStartTime);
                }
                dwr.TravelFromJobStartTime= holdR;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelFromJobEndTime)) {
                dwr.TravelFromJobEndTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.substring(0, f.response.indexOf(" "));
                try {
                    holdR = KTime.ParseToFormat(holdR, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
                } catch (ExpParseToCalendar expParseToCalendar) {
                    ExpClass.LogEX(expParseToCalendar, F_TravelFromJobEndTime);
                }
                dwr.TravelFromJobEndTime= holdR;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelToJobHours)) {
                dwr.TravelToHoursRoundedKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.TravelToHoursRounded = Double.parseDouble(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelToJobMiles)) {
                dwr.TravelToJobMilesKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.TravelToJobMiles = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelOnJobMiles)) {
                dwr.TravelOnJobMilesKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.TravelOnJobMiles = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_TravelFromJobMiles)) {
                dwr.TravelFromJobMilesKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.TravelFromJobMiles = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_PerDiem)) {
                dwr.PerDiemKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.PerDiem = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_IsOngoing)) {
                dwr.IsOngoingKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.IsOngoing = Boolean.parseBoolean(f.response);
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SitePhotoI)) {
                dwr.SitePhotoIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SiteCommentI)) {
                dwr.SiteCommentIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SitePhotoII)) {
                dwr.SitePhotoIIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SiteCommentII)) {
                dwr.SiteCommentIIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SitePhotoIII)) {
                dwr.SitePhotoIIIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SiteCommentIII)) {
                dwr.SiteCommentIIIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SitePhotoIV)) {
                dwr.SitePhotoIVKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SiteCommentIV)) {
                dwr.SiteCommentIVKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SitePhotoV)) {
                dwr.SitePhotoVKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SiteCommentV)) {
                dwr.SiteCommentVKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SitePhotoVI)) {
                dwr.SitePhotoVIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SiteCommentVI)) {
                dwr.SiteCommentVIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SitePhotoVII)) {
                dwr.SitePhotoVIIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SiteCommentVII)) {
                dwr.SiteCommentVIIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SitePhotoVIII)) {
                dwr.SitePhotoVIIIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SiteCommentVIII)) {
                dwr.SiteCommentVIIIKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_SigninSheet)) {
                dwr.SignInKey = f.id;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_InputWMLine)) {
                dwr.InputWMLineKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.InputWMLine = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_InputWMStation)) {
                dwr.InputWMStationKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.InputWMStation = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_InputWMStationName)) {
                dwr.InputWMStationNameKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.InputWMStationName = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_WMTrack)) {
                dwr.InputWMTrackKey = f.id;
                if(keyOnly || noResponse) continue;
                if(f.response.equalsIgnoreCase("X")) {
                    dwr.InputWMTrack = -1;
                } else {
                    String holdR = f.response.replaceAll("[^0-9.]", "");
                    dwr.InputWMTrack = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                }
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_RWICPHONE)) {
                dwr.RwicPhoneKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.RwicPhone = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_CSXShiftNew)) {
                dwr.CSXShiftNewKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.CSXShiftNew = Boolean.parseBoolean(f.response);
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_CSXShiftRelief)) {
                dwr.CSXShiftReliefKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.CSXShiftRelief = Boolean.parseBoolean(f.response);
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_CSXShiftRelieved)) {
                dwr.CSXShiftRelievedKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.CSXShiftRelieved = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_WorkLunch)) {
                dwr.WorkLunchTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.WorkLunchTime = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_CSXPeopleRow)) {
                dwr.CSXPeopleRowKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.CSXPeopleRow = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_CSXEquipmentRow)) {
                dwr.CSXEquipmentRowKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.CSXEquipmentRow = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_WeatherHighDesc)) {
                dwr.DescWeatherHighKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.DescWeatherHigh = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_WeatherLowDesc)) {
                dwr.DescWeatherLowKey = f.id;
                if(keyOnly || noResponse) continue;
                String holdR = f.response.replaceAll("[^0-9.]", "");
                dwr.DescWeatherLow = Integer.parseInt(holdR.length() > 0 ? holdR : "0");
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_WorkBriefTime)) {
                dwr.WorkBriefTimeKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.WorkBriefTime = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_RoadMasterPhone)) {
                dwr.RoadMasterPhoneKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.RoadMasterPhone = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_WorkPlanned)) {
                dwr.DescWorkPlannedKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.DescWorkPlanned = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_DescSafety)) {
                dwr.DescSafetyKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.DescSafety = f.response;
                continue;
            }
            if (f.usageCode.equalsIgnoreCase(F_VersionInformation)) {
                dwr.VersionInformationKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.VersionInformation = f.response;
            }
            if (f.usageCode.equalsIgnoreCase(F_ConstructionDay)) {
                dwr.ConstructionDayKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.ConstructionDay = f.response;
            }
            if (f.usageCode.equalsIgnoreCase(F_InputTotalWorkDays)) {
                dwr.InputTotalWorkDaysKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.InputTotalWorkDays = f.response;
            }
            if (f.usageCode.equalsIgnoreCase(F_DescWeatherRain)) {
                dwr.InputDescWeatherRainKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.InputDescWeatherRain = f.response;
            }
            if (f.usageCode.equalsIgnoreCase(F_DescWeatherWind)) {
                dwr.InputDescWeatherWindKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.InputDescWeatherWind = f.response;
            }
            if (f.usageCode.equalsIgnoreCase(F_EightyTwoT)) {
                dwr.EightyTwoTKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.EightyTwoT = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_StreetName)) {
                dwr.StreetNameKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.StreetName = f.response;
                continue;
            }
            if(f.usageCode.equalsIgnoreCase(F_MilePostsForStreet)) {
                dwr.MilePostsForStreetKey = f.id;
                if(keyOnly || noResponse) continue;
                dwr.MilePostsForStreet = f.response;
                continue;
            }
        }

        // Need to wait until the end to put these together.
        if(!keyOnly) {
            dwr.InputWMStation = dwr.InputWMStation + " " + dwr.InputWMStationName;
        }

        return dwr;
    }

    /**
     * This will take the database record for a DWR and map it to the API contract.
     * The API currently has no validation, so be careful to put the correct data in.
     *
     * @param dwr The DWR to be sent to the server.
     */
    public DwrRequest MapDwrToRequest(DwrTbl dwr) {
        DwrRequest request = new DwrRequest();
        LayoutHelperDwr helperDwr = LayoutHelperDwr.getInstance(mContext);
        Map<String, LayoutHelperDwr.Qualities> usageMap = helperDwr.GetApiQualityMap(dwr.Classification, dwr.Property);

        // Fixed Entry
        request.id = dwr.DwrSrvrId;

        try {
            request.date = TrimDate(KTime.ParseToFormat(BlankForNull(dwr.WorkDate), KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID()).toString());
        } catch (ExpParseToCalendar expParseToCalendar) {
            request.date = TrimDate(KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS).toString());
        }
        request.typeOfDay = Arrays.asList(mContext.getResources().getStringArray(R.array.classification_code_name)).get(dwr.Classification);
        request.railroadSignature = EncodeImageBase64(BlankForNull(dwr.RailSignatureName));
        request.railroadSignatureDate = LocalForApi(BlankForNull(dwr.RailSignatureDate));
        request.rwicSignature = EncodeImageBase64(BlankForNull(dwr.RwicSignatureName));
        request.rwicSignatureDate = LocalForApi(BlankForNull(dwr.RwicSignatureDate));
        request.clientSignature = EncodeImageBase64(BlankForNull(dwr.ClientSignatureName));
        request.clientSignatureDate = LocalForApi(BlankForNull(dwr.ClientSignatureDate));
        request.clientSignerName = BlankForNull(dwr.ClientName);
        request.clientSignerPhone = BlankForNull(dwr.ClientPhone);
        request.clientSignerEmail = BlankForNull(dwr.ClientEmail);
        request.submittedOn = KTime.ParseNow(KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
        request.nonBillableDayReason = dwr.Classification == DWR_TYPE_OTHER_NONBILLABLE ? BlankForNull(nonBillableToAPI(dwr.NonBilledReason)) : "";
        request.source = new BaseIdWS();
        request.source.id = dwr.WorkId;
        request.job = new BaseIdWS();
        request.job.id = dwr.JobId;
        request.template = new BaseIdWS();

        AssignmentTbl assignment = this.db.assignmentDao().GetByJobId(dwr.JobId);
        boolean isRoadWayFlagging = dwr.HasRoadwayFlagging
                || assignment.ServiceType == RP_ROADWAY_FLAGGING_SERVICE;

        String prefKey = isRoadWayFlagging ? SP_REG_DWR_FORMID_ROADWAY_FLAGGING : SP_REG_DWR_FORMID;
        request.template.id = GetDwrTemplateId(prefKey);

        request.status = new BaseIdWS();
        if (dwr.Status == DWR_STATUS_BOUNCED) {
            request.status.id = DWR_STATUS_API_ResubmittedId;
        } else {
            request.status.id = DWR_STATUS_API_SubmittedId;
        }
        request.reviewNotes = dwr.ReviewerNotes;
        request.reviewedOn = dwr.ReviewerOn;
        if (dwr.ReviewerId != 0) {
            request.reviewer = new FieldWorkerLink();
            request.reviewer.id = dwr.ReviewerId;
            request.reviewer.name = dwr.ReviewerName;
        }
        request.costCenters = Functions.BuildCostCenter(BlankForNull(dwr.SpecialCostCenter), LocalForApi(dwr.WorkStartTime), LocalForApi(dwr.WorkEndTime), BlankForNull(dwr.SpecialCostCenterKey));

        // Other types of classifications do not late cancel (although NotPresentOnTrack is reused on Non-billable day).
        if (dwr.Classification == DWR_TYPE_BILLABLE_DAY || dwr.Classification == DWR_TYPE_FIELD_TRAINING){
            request.isLateCancellation = dwr.NotPresentOnTrack;
        }
        request.isTrainer = dwr.PerformedTraining;
        request.hasRoadwayFlagging = dwr.HasRoadwayFlagging;

        // ServiceType
        request.serviceType = new BaseIdWS();
        if(isRoadWayFlagging) {
            request.hasRoadwayFlagging = true;
            request.serviceType.id = RP_ROADWAY_FLAGGING_SERVICE;
        } else {
            request.serviceType.id = dwr.Classification == DWR_TYPE_BILLABLE_DAY_UTILITY ? RP_UTILITY_SERVICE : RP_FLAGGING_SERVICE;
        }

        // Question based entry
        request.fields = new ArrayList<>();
        DwrAnswerWS holdAnswer;
        // Header Work Project
        holdAnswer = GetAnswerByCode("", 0, QUESTION_TYPE_FormHeaderStr + 1, usageMap, false);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        // Answer Work Project
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.LocationCity), dwr.LocationCityKey, F_LocationCity, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.LocationState), dwr.LocationStateKey, F_LocationState, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(Railroads.PropertyNameServer(mContext, dwr.Property)), dwr.PropertyKey, F_Property, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.ContractorName), dwr.ContractorNameKey, F_ContractorName, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.RoadMaster), dwr.RoadMasterKey, F_RoadMaster, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.District), BlankForNull(dwr.DistrictKey), F_District, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.Subdivision), dwr.SubdivisionKey, F_Subdivision, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.MpStart), dwr.MpStartKey, F_MpStart, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.MpEnd), dwr.MpEndKey, F_MpEnd, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.WorkingTrack), BlankForNull(dwr.WorkingTrackKey), F_WorkingTrack, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.RailroadContact), BlankForNull(dwr.RailroadContactKey), F_RailroadContact, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        // Header Property
        holdAnswer = GetAnswerByCode("", 0, QUESTION_TYPE_FormHeaderStr + 2, usageMap, false);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        // Answer Property
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CNDataNSOCC), dwr.CNDataNSOCCKey, F_CNDataNSOCC, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CNDataNetwork), dwr.CNDataNetworkKey, F_CNDataNetwork, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CNDataCounty), dwr.CNDataCountyKey, F_CNDataCounty, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CSXDataRegion), dwr.CSXDataRegionKey, F_CSXDataRegion, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CSXDataOpNbr), dwr.CSXDataOpNbrKey, F_CSXDataOpNbr, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.KCSDataContractorCnt), dwr.KCSDataContractorCntKey, F_KCSDataContractorCnt, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.UPDataDotXing), dwr.UPDataDotXingKey, F_UPDataDotXing, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.UPDataFolderNbr), dwr.UPDataFolderNbrKey, F_UPDataFolderNbr, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.UPDataServiceUnit), dwr.UPDataServiceUnitKey, F_UPDataServiceUnit, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.KCTDataTaskOrder), dwr.KCTDataTaskOrderKey, F_KCTDataTaskOrder, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.InputWMLine), dwr.InputWMLineKey, F_InputWMLine, usageMap);
        if(holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.InputWMStation).length() > 3 ? BlankForNull(dwr.InputWMStation).substring(0,3) : "", dwr.InputWMStationKey, F_InputWMStation, usageMap);
        if(holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.InputWMStationName), dwr.InputWMStationNameKey, F_InputWMStationName, usageMap);
        if(holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.InputWMTrack == -1 ? "X" : String.valueOf(dwr.InputWMTrack)), dwr.InputWMTrackKey, F_WMTrack, usageMap);
        if(holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.TypeOfVehicle), dwr.TypeOfVehicleKey, F_TypeofVehicle, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        // Header Work Zone
        holdAnswer = GetAnswerByCode("", 0, QUESTION_TYPE_FormHeaderStr + 3, usageMap, false);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        // Answer Work Zone
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.Is707), dwr.Is707Key, F_Is707, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.Is1102), dwr.Is1102Key, F_Is1102, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.Is1107), dwr.Is1107Key, F_Is1107, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsEC1), dwr.IsEC1Key, F_IsEC1, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsFormB), dwr.IsFormBKey, F_IsFormB, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsFormC), dwr.IsFormCKey, F_IsFormC, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsFormW), dwr.IsFormWKey, F_IsFormW, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsForm23), dwr.IsForm23Key, F_IsForm23, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsForm23Y), dwr.IsForm23YKey, F_IsForm23Y, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsDerails), dwr.IsDerailsKey, F_IsDerails, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsTrackTime), dwr.IsTrackTimeKey, F_IsTrackTime, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsTrackWarrant), dwr.IsTrackWarrantKey, F_IsTrackWarrant, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsLookout), dwr.IsLookoutKey, F_IsLookout, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsObserver), dwr.IsObserverKey, F_IsObserver, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsTrackAuthority), dwr.IsTrackAuthorityKey, F_IsTrackAuthority, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsNoProtection), dwr.IsNoProtectionKey, F_IsNoProtection, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsLiveFlagman), dwr.IsLiveFlagmanKey, F_IsLiveFlagman, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsVerbalPermission), dwr.IsVerbalPermissionKey, F_IsVerbalPermission, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.WorkOnTrack), dwr.WorkOnTrackKey, F_WorkOnTrack, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        // Header Work Description
        holdAnswer = GetAnswerByCode("", 0, QUESTION_TYPE_FormHeaderStr + 4, usageMap, false);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        // Answer Work Description
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.Description), dwr.DescriptionKey, F_Description, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescWeatherConditions), dwr.DescWeatherConditionsKey, F_DescWeatherConditions, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescTypeOfWork), dwr.DescTypeOfWorkKey, F_DescTypeOfWork, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescInsideRoW), dwr.DescInsideRoWKey, F_DescInsideRoW, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescOutsideRoW), dwr.DescOutsideRoWKey, F_DescOutsideRoW, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescUnusual), dwr.DescUnusualKey, F_DescUnusual, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescLocationStart), dwr.DescLocationStartKey, F_DescLocationStart, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);


        int position = isRoadWayFlagging ? 7 : 6;
        // Header Work Time
        holdAnswer = GetAnswerByCode("", 0, QUESTION_TYPE_FormHeaderStr + position, usageMap, false);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        // Answer Work Time
        holdAnswer = GetAnswerByCode(MakeLocalTimeWithPrecisionFormat(dwr.WorkStartTime), dwr.WorkStartTimeKey, F_WorkStartTime, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(MakeLocalTimeWithPrecisionFormat(dwr.WorkEndTime), dwr.WorkEndTimeKey, F_WorkEndTime, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.WorkHoursRounded), dwr.WorkHoursRoundedKey, F_WorkHoursRounded, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(MakeLocalTime(dwr.TravelToJobStartTime), dwr.TravelToJobStartTimeKey, F_TravelToJobStartTime, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(MakeLocalTime(dwr.TravelToJobEndTime), dwr.TravelToJobEndTimeKey, F_TravelToJobEndTime, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.TravelToHoursRounded), dwr.TravelToHoursRoundedKey, F_TravelToJobHours, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(MakeLocalTime(dwr.TravelFromJobStartTime), dwr.TravelFromJobStartTimeKey, F_TravelFromJobStartTime, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(MakeLocalTime(dwr.TravelFromJobEndTime), dwr.TravelFromJobEndTimeKey, F_TravelFromJobEndTime, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.TravelFromHoursRounded), dwr.TravelFromHoursRoundedKey, F_TravelFromJobHours, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.TravelToJobMiles), dwr.TravelToJobMilesKey, F_TravelToJobMiles, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.TravelOnJobMiles), dwr.TravelOnJobMilesKey, F_TravelOnJobMiles, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.TravelFromJobMiles), dwr.TravelFromJobMilesKey, F_TravelFromJobMiles, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.PerDiem), dwr.PerDiemKey, F_PerDiem, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.IsOngoing), dwr.IsOngoingKey, F_IsOngoing, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.RwicPhone), dwr.RwicPhoneKey, F_RWICPHONE, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CSXShiftNew), dwr.CSXShiftNewKey, F_CSXShiftNew, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CSXShiftRelief), dwr.CSXShiftReliefKey, F_CSXShiftRelief, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.WorkLunchTime), dwr.WorkLunchTimeKey, F_WorkLunch, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CSXPeopleRow), dwr.CSXPeopleRowKey, F_CSXPeopleRow, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CSXEquipmentRow), dwr.CSXEquipmentRowKey, F_CSXEquipmentRow, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.CSXShiftRelieved), dwr.CSXShiftRelievedKey, F_CSXShiftRelieved, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescWeatherHigh), dwr.DescWeatherHighKey, F_WeatherHighDesc, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescWeatherLow), dwr.DescWeatherLowKey, F_WeatherLowDesc, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.WorkBriefTime), dwr.WorkBriefTimeKey, F_WorkBriefTime, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.RoadMasterPhone), dwr.RoadMasterPhoneKey, F_RoadMasterPhone, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescWorkPlanned), dwr.DescWorkPlannedKey, F_WorkPlanned, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.DescSafety), dwr.DescSafetyKey, F_DescSafety, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(ParseVersion(dwr.VersionInformation), dwr.VersionInformationKey, F_VersionInformation, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(ParseMetrics(dwr.VersionInformation), 0, F_Profiles, usageMap); // cheating on the server field key
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.ConstructionDay), BlankForNull(dwr.ConstructionDayKey), F_ConstructionDay, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.InputTotalWorkDays), BlankForNull(dwr.InputTotalWorkDaysKey), F_InputTotalWorkDays, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.InputDescWeatherWind), BlankForNull(dwr.InputDescWeatherWindKey), F_DescWeatherWind, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        holdAnswer = GetAnswerByCode(BlankForNull(dwr.InputDescWeatherRain), BlankForNull(dwr.InputDescWeatherRainKey), F_DescWeatherRain, usageMap);
        if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

        if (isRoadWayFlagging) {
            // Header Work Time
            holdAnswer = GetAnswerByCode("", 0, QUESTION_TYPE_FormHeaderStr + 6, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

            holdAnswer = GetAnswerByCode(BlankForNull(dwr.EightyTwoT), dwr.EightyTwoTKey, F_EightyTwoT, usageMap);
            if (holdAnswer.formFieldPlacementId > 0) {
                holdAnswer.isNotApplicable = false;
                request.fields.add(holdAnswer);
            }
            holdAnswer = GetAnswerByCode(BlankForNull(dwr.StreetName), dwr.StreetNameKey, F_StreetName, usageMap);
            if (holdAnswer.formFieldPlacementId > 0) {
                holdAnswer.isNotApplicable = false;
                request.fields.add(holdAnswer);
            }
            holdAnswer = GetAnswerByCode(BlankForNull(dwr.MilePostsForStreet), dwr.MilePostsForStreetKey, F_MilePostsForStreet, usageMap);
            if (holdAnswer.formFieldPlacementId > 0) {
                holdAnswer.isNotApplicable = false;
                request.fields.add(holdAnswer);
            }
        }

        //Loop Through the document
        //Encode the image
        //put in the proper request area
        //Shrink to 4mb (shrink dimension)
        ScheduleDB db = ScheduleDB.getDatabase(mContext);
        String encoded;
        String comments;
        List<DocumentTbl> docs = db.documentDao().GetDwrImages(dwr.DwrId);
        if (docs != null && docs.size() > 0) {
            // Header Pictures
            holdAnswer = GetAnswerByCode("", 0, QUESTION_TYPE_FormHeaderStr + 5, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            // Answer Pictures
            encoded = (docs.size() > 0) ? Functions.EncodeImagePictureBase64(DOC_IMAGE_DWR, docs.get(0).FileName, IMAGE_COMPRESSION) : "";
            holdAnswer = GetAnswerByCode(encoded, dwr.SitePhotoIKey, F_SitePhotoI, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            comments = (docs.size() > 0) ? docs.get(0).description : "";
            holdAnswer = GetAnswerByCode(comments, dwr.SiteCommentIKey, F_SiteCommentI, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

            encoded = (docs.size() > 1) ? Functions.EncodeImagePictureBase64(DOC_IMAGE_DWR, docs.get(1).FileName, IMAGE_COMPRESSION) : "";
            holdAnswer = GetAnswerByCode(encoded, dwr.SitePhotoIIKey, F_SitePhotoII, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            comments = (docs.size() > 1) ? docs.get(1).description : "";
            holdAnswer = GetAnswerByCode(comments, dwr.SiteCommentIIKey, F_SiteCommentII, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

            encoded = (docs.size() > 2) ? Functions.EncodeImagePictureBase64(DOC_IMAGE_DWR, docs.get(2).FileName, IMAGE_COMPRESSION) : "";
            holdAnswer = GetAnswerByCode(encoded, dwr.SitePhotoIIIKey, F_SitePhotoIII, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            comments = (docs.size() > 2) ? docs.get(2).description : "";
            holdAnswer = GetAnswerByCode(comments, dwr.SiteCommentIIIKey, F_SiteCommentIII, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

            encoded = (docs.size() > 3) ? Functions.EncodeImagePictureBase64(DOC_IMAGE_DWR, docs.get(3).FileName, IMAGE_COMPRESSION) : "";
            holdAnswer = GetAnswerByCode(encoded, dwr.SitePhotoIVKey, F_SitePhotoIV, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            comments = (docs.size() > 3) ? docs.get(3).description : "";
            holdAnswer = GetAnswerByCode(comments, dwr.SiteCommentIVKey, F_SiteCommentIV, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

            encoded = (docs.size() > 4) ? Functions.EncodeImagePictureBase64(DOC_IMAGE_DWR, docs.get(4).FileName, IMAGE_COMPRESSION) : "";
            holdAnswer = GetAnswerByCode(encoded, dwr.SitePhotoVKey, F_SitePhotoV, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            comments = (docs.size() > 4) ? docs.get(4).description : "";
            holdAnswer = GetAnswerByCode(comments, dwr.SiteCommentVKey, F_SiteCommentV, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

            encoded = (docs.size() > 5) ? Functions.EncodeImagePictureBase64(DOC_IMAGE_DWR, docs.get(5).FileName, IMAGE_COMPRESSION) : "";
            holdAnswer = GetAnswerByCode(encoded, dwr.SitePhotoVIKey, F_SitePhotoVI, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            comments = (docs.size() > 5) ? docs.get(5).description : "";
            holdAnswer = GetAnswerByCode(comments, dwr.SiteCommentVIKey, F_SiteCommentVI, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

            encoded = (docs.size() > 6) ? Functions.EncodeImagePictureBase64(DOC_IMAGE_DWR, docs.get(6).FileName, IMAGE_COMPRESSION) : "";
            holdAnswer = GetAnswerByCode(encoded, dwr.SitePhotoVIIKey, F_SitePhotoVII, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            comments = (docs.size() > 6) ? docs.get(6).description : "";
            holdAnswer = GetAnswerByCode(comments, dwr.SiteCommentVIIKey, F_SiteCommentVII, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);

            encoded = (docs.size() > 7) ? Functions.EncodeImagePictureBase64(DOC_IMAGE_DWR, docs.get(7).FileName, IMAGE_COMPRESSION) : "";
            holdAnswer = GetAnswerByCode(encoded, dwr.SitePhotoVIIIKey, F_SitePhotoVIII, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
            comments = (docs.size() > 7) ? docs.get(7).description : "";
            holdAnswer = GetAnswerByCode(comments, dwr.SiteCommentVIIIKey, F_SiteCommentVIII, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        }

        // Just grab the first one in the database as there should be only 1 there.
        List<DocumentTbl> daily = db.documentDao().GetDwrDailyImage(dwr.DwrId);
        if(daily != null && daily.size() > 0) {
            encoded = Functions.EncodeImagePictureBase64(DOC_IMAGE_SIGNIN, daily.get(0).FileName, IMAGE_COMPRESSION);
            holdAnswer = GetAnswerByCode(encoded, dwr.SignInKey, F_SigninSheet, usageMap, false);
            if (holdAnswer.formFieldPlacementId > 0) request.fields.add(holdAnswer);
        }

        return request;
    }

    /********************************************************************************************/
    /* This method tries to map the server representation of a DWR into the local database version.
     * - Retrieve and Save the signature images to the file system (file names are stored in the DWR).
     * - The fixed fields are copied from the server directly (for the most part).
     * - The variable fields come in from the server as an array.  Scan the array and map the data.
     * - Check that this DWR has a local assignment.  If not, create an assignment.
     * - Save the DWR to the database.
     * - Retrieve and Save the pictures associated with a DWR to the file system (create a database
     *   entry for them in the document table).
     * @param serverDWR
     * @param localDWR
     * @param ticket
     */
    public void MapDwrDetailResponseToDwr(DwrResponse serverDWR, DwrTbl localDWR, String ticket) {
        localDWR.DwrSrvrId = serverDWR.id;
        localDWR.Classification = getIndexFromXMLArray(R.array.classification_code_name, serverDWR.typeOfDay);
        if(serverDWR.serviceType != null) {
            if (serverDWR.serviceType.id == RP_UTILITY_SERVICE && localDWR.Classification == DWR_TYPE_BILLABLE_DAY) {
                localDWR.Classification = DWR_TYPE_BILLABLE_DAY_UTILITY;
            }
            if (serverDWR.serviceType.id == RP_ROADWAY_FLAGGING_SERVICE) {
                localDWR.HasRoadwayFlagging = true;
            }
        }
        localDWR.NonBilledReason = BlankForNull(nonBillableFromAPI(serverDWR.nonBillableDayReason));
        localDWR.RwicSignatureDate = LocalForApi(BlankForNull(serverDWR.rwicSignatureDate));
        localDWR.ClientSignatureDate = BlankForNull(serverDWR.clientSignatureDate);
        localDWR.ClientName=  BlankForNull(serverDWR.clientSignerName);
        localDWR.ClientPhone = BlankForNull(serverDWR.clientSignerPhone);
        localDWR.ClientEmail = BlankForNull(serverDWR.clientSignerEmail);
        localDWR.RailSignatureDate = BlankForNull(serverDWR.railroadSignatureDate);  // waiting for backend implementation, will also need to get signature file downloaded in Refresh.
        localDWR.JobId = serverDWR.job.id;
        localDWR.JobNumber = Integer.toString(serverDWR.job.number);
        localDWR.ReviewerNotes = BlankForNull(serverDWR.reviewNotes);
        localDWR.ReviewerOn = BlankForNull(serverDWR.reviewedOn);
        localDWR.PerformedTraining = BlankForNull(serverDWR.isTrainer);

        // Convert the cost center to local variation of data.
        if(serverDWR.costCenters != null) {
            // There should only be 0 or 1 cost centers per DWR.
            if(serverDWR.costCenters.size() > 0) {
                List<JobCostCenter> holdJobCostCenter = new ArrayList<>();
                holdJobCostCenter.add(serverDWR.costCenters.get(0).jobCostCenter);
                List<String> rawJobCostCenter = Functions.RawCostCenters(Functions.CompactCostCenters(holdJobCostCenter));
                if (rawJobCostCenter.size() > 0) {
                        localDWR.SpecialCostCenter = rawJobCostCenter.get(0);
                        localDWR.SpecialCostCenterKey = serverDWR.costCenters.get(0).id;
                }
            }
        }

        if(serverDWR.reviewer!= null) {
            localDWR.ReviewerId = serverDWR.reviewer.id;
            localDWR.ReviewerName = BlankForNull(serverDWR.reviewer.name);
        } else {
            localDWR.ReviewerName = "";
        }
        localDWR.WorkId = serverDWR.source.id;

        try {
            // WorkDate is tricky as server has their own format, be we store things as real dates.
            localDWR.WorkDate = KTime.ParseToFormat(BlankForNull(serverDWR.date.replace("00:00:00", "12:00:00")),
                    KTime.KT_fmtDateOnlyRPFS, KTime.UTC_TIMEZONE, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
        } catch (ExpParseToCalendar expParseToCalendar) {
            localDWR.WorkDate = KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString();
        }

        if (serverDWR.status.id == DWR_STATUS_API_ApprovedId) {
            localDWR.Status = DWR_STATUS_APPROVED;
        } else if (serverDWR.status.id == DWR_STATUS_API_SubmittedId) {
            localDWR.Status = DWR_STATUS_PENDING;
        }else  {
            localDWR.Status = DWR_STATUS_BOUNCED;
        }

        // Update all the variable fields.
        localDWR = UpdateFieldKeys(localDWR, serverDWR.fields, false);
        List<DocumentTbl> documents= CreateDocumentsListWithPicturesAndComments(localDWR, serverDWR.fields, ticket);


        ScheduleDB db = ScheduleDB.getDatabase(mContext);
        long dwrid = db.dwrDao().Insert(localDWR);

        AddDwrIdToDocuments(documents, dwrid, db);

        linkAssignment(localDWR.JobId, ticket, localDWR.WorkDate, localDWR.Property);
    }

    private void AddDwrIdToDocuments(List<DocumentTbl> documents, long dwrId, ScheduleDB db) {
        for(DocumentTbl document: documents) {
            document.DwrId = (int) dwrId;
            db.documentDao().Insert(document);
        }
    }

    private List<DocumentTbl> CreateDocumentsListWithPicturesAndComments(DwrTbl dwr, List<DwrAnswerWS> fields, String ticket) {
        HashMap<String, DocumentTbl> photoDocumentMap = new HashMap<>();
        List<DocumentTbl> documentTbls = new ArrayList<>();
        IWebServices Api = new WebServices(API_TIMEOUT_SHORT, new Gson());
        for (DwrAnswerWS f : fields) {
            if (f.usageCode != null
                    && f.usageCode.equals(F_SigninSheet)
                    && f.response != null) {
                documentTbls.add(SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_SIGNIN, IMAGE_SIGNUP, dwr));
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SitePhotoI)
                    && f.response != null) {
                photoDocumentMap.put(F_SitePhotoI, SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_DWR, F_SitePhotoI, dwr));
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SitePhotoII)
                    && f.response != null) {
                photoDocumentMap.put(F_SitePhotoII, SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_DWR, F_SitePhotoII, dwr));
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SitePhotoIII)
                    && f.response != null) {
                photoDocumentMap.put(F_SitePhotoIII, SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_DWR, F_SitePhotoIII, dwr));
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SitePhotoIV)
                    && f.response != null) {
                photoDocumentMap.put(F_SitePhotoIV, SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_DWR, F_SitePhotoIV, dwr));
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SitePhotoV)
                    && f.response != null) {
                photoDocumentMap.put(F_SitePhotoV, SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_DWR, F_SitePhotoV, dwr));
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SitePhotoVI)
                    && f.response != null) {
                photoDocumentMap.put(F_SitePhotoVI, SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_DWR, F_SitePhotoVI, dwr));
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SitePhotoVII)
                    && f.response != null) {
                photoDocumentMap.put(F_SitePhotoVII, SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_DWR, F_SitePhotoVII, dwr));
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SitePhotoVIII)
                    && f.response != null) {
                photoDocumentMap.put(F_SitePhotoVIII, SaveWorkPicture(Api, ticket, f.id, DOC_IMAGE_DWR, F_SitePhotoVIII, dwr));
                continue;
            }
        }

        for (DwrAnswerWS f : fields) {
            DocumentTbl documentTbl;
            if (f.usageCode != null
                    && f.usageCode.equals(F_SiteCommentI)
                    && f.response != null) {
                documentTbl = photoDocumentMap.get(F_SitePhotoI);
                if(documentTbl == null) continue;
                documentTbl.description = f.response;
                documentTbls.add(documentTbl);
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SiteCommentII)
                    && f.response != null) {
                documentTbl = photoDocumentMap.get(F_SitePhotoII);
                if(documentTbl == null) continue;
                documentTbl.description = f.response;
                documentTbls.add(documentTbl);
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SiteCommentIII)
                    && f.response != null) {
                documentTbl = photoDocumentMap.get(F_SitePhotoIII);
                if(documentTbl == null) continue;
                documentTbl.description = f.response;
                documentTbls.add(documentTbl);
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SiteCommentIV)
                    && f.response != null) {
                documentTbl = photoDocumentMap.get(F_SitePhotoIV);
                if(documentTbl == null) continue;
                documentTbl.description = f.response;
                documentTbls.add(documentTbl);
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SiteCommentV)
                    && f.response != null) {
                documentTbl = photoDocumentMap.get(F_SitePhotoV);
                if(documentTbl == null) continue;
                documentTbl.description = f.response;
                documentTbls.add(documentTbl);
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SiteCommentVI)
                    && f.response != null) {
                documentTbl = photoDocumentMap.get(F_SitePhotoVI);
                if(documentTbl == null) continue;
                documentTbl.description = f.response;
                documentTbls.add(documentTbl);
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SiteCommentVII)
                    && f.response != null) {
                documentTbl = photoDocumentMap.get(F_SitePhotoVII);
                if(documentTbl == null) continue;
                documentTbl.description = f.response;
                documentTbls.add(documentTbl);
                continue;
            }

            if (f.usageCode != null
                    && f.usageCode.equals(F_SiteCommentVIII)
                    && f.response != null) {
                documentTbl = photoDocumentMap.get(F_SitePhotoVIII);
                if(documentTbl == null) continue;
                documentTbl.description = f.response;
                documentTbls.add(documentTbl);
                continue;
            }
        }

        return documentTbls;
    }

    private DocumentTbl SaveWorkPicture(IWebServices apiService, String ticket, int fieldId, int document, String prefix, DwrTbl dwr) {
        DocumentTbl doc = new DocumentTbl();
        try {
            // Save the file to disk.
            String base64String = apiService.CallGetApi(
                    Connection.getInstance().getFullApiPath(Connection.API_GET_PHOTO_DATA).replace(SUB_ZZZ, String.valueOf(fieldId)),
                    String.class,
                    ticket);
            byte[] bites = Base64.decode(base64String, Base64.DEFAULT);
            String name = prefix
                    + KTime.ParseNow(KTime.KT_fmtFileNameFromTime).toString() + "_"
                    + fieldId  // adds some extra randomness
                    + IMGAGE_EXTENTION;
            Functions.SaveRawToFile(document, name, bites);

            // Create the Document record.
            doc.DocumentType = document;
            doc.JobId = dwr.JobId;
            doc.FileName = name;
            doc.UriLocal = Functions.GetStorageName(document, name);
            doc.Mimetype = "image/jpeg";
            doc.description = "";
            doc.DwrId = dwr.DwrId;
            doc.LastUpdate = KTime.ParseNow(KTime.KT_fmtDate3339fk).toString();

            return doc;
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "SaveWorkPicture");
            return null;
        }
    }

    private void linkAssignment(int jobid, String ticket, String dwrWorkDate, int property) {
        try {
            ScheduleDB db = ScheduleDB.getDatabase(mContext);
            AssignmentTbl tbl = db.assignmentDao().GetByJobId(jobid);
            if (tbl == null) {
                // Get the basic information needed to build an Assignment.
                JobDetailResponse jobResponse;
                JobCostCenter[] costResponse = new JobCostCenter[0];
                try {
                    IWebServices ApiService = new WebServices(API_TIMEOUT_SHORT, new Gson());
                    String jobPath = Connection.getInstance().getFullApiPath(Connection.API_GET_JOB_BY_ID).replace(SUB_ZZZ, String.valueOf(jobid));
                    jobResponse = ApiService.CallGetApi(jobPath, WebServiceModels.JobDetailResponse.class, ticket);
                    // Job cost center information
                    String costPath = Connection.getInstance().getFullApiPath(Connection.API_GET_JOB_COSTCENTERS).replace(SUB_ZZZ, String.valueOf(jobid));
                    costResponse = ApiService.CallGetApi(costPath, JobCostCenter[].class, ticket);
                } catch (Exception ex) {
                    // If cannot get data online, see if we have any locally.
                    JobTbl smallJob = db.jobDao().GetJob(jobid);
                    jobResponse = new WebServiceModels.JobDetailResponse();
                    jobResponse.id = jobid;
                    if(smallJob != null) {
                        jobResponse.start = smallJob.StartTime;
                        jobResponse.end = smallJob.EndTime;
                        jobResponse.description = smallJob.Description;
                        jobResponse.customer = new WebServiceModels.CustomerShortWS();
                        jobResponse.customer.name = smallJob.CustomerName;
                        jobResponse.number = smallJob.JobNumber;
                    }
                    ExpClass.LogEX(ex, "linktoAssignment try local job lookup.");
                }
                WebServiceModels.AssignmentWS workItem = new WebServiceModels.AssignmentWS();
                WebServiceModels.AssignmentShiftWS shift = new WebServiceModels.AssignmentShiftWS();
                shift.id = 0;   // Zero (0) indicates this is not a server created assignment.

                // Make the dates match the workday for this DWR.
                String workDate = KTime.ParseToFormat(dwrWorkDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateOnlyRPFS, KTime.UTC_TIMEZONE).toString();
                workDate = workDate.substring(0,11) + MIDNIGHT + "Z"; // Use the backend format.
                shift.day = workDate;
                workItem.start = workDate;
                workItem.end = workDate;

                Actor user = new Actor(mContext);
                AssignmentTbl action = Functions.BuildAssignment(user.unique, property, jobResponse, Arrays.asList(costResponse), workItem, shift);
                db.assignmentDao().Insert(action);
            }
        } catch (Exception ex) {
            // If unable to find job information, the non-null cause the insert to fail.
            ExpClass.LogEX(ex, "dwrtemplatemgr.linkToAssignment");
        }
    }


    // The backend did not have time to format these dates, so we do it for them.
    private String MakeLocalTime(String value) {
        if (value == null) return "";
        try {
            Calendar holdTime = KTime.ParseToCalendar(value, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE);
            Calendar holdLocal = KTime.ConvertTimezone(holdTime, TimeZone.getDefault().getID());
            return DateFormat.format(KTime.KT_fmtDateOnlyRPFS, holdLocal).toString() + " " + TimeZone.getDefault().getDisplayName();
        } catch (ExpParseToCalendar expParseToCalendar) {
            return "";
        }
    }

    private String MakeLocalTimeWithPrecisionFormat(String value) {
        if (value == null) return "";
        try {
            Calendar holdTime = KTime.ParseToCalendar(value, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE);
            Calendar holdLocal = KTime.ConvertTimezone(holdTime, TimeZone.getDefault().getID());
            return DateFormat.format(KTime.KT_fmtDate3339fk_xS, holdLocal).toString() + " " + TimeZone.getDefault().getDisplayName();
        } catch (ExpParseToCalendar expParseToCalendar) {
            return "";
        }
    }

    private int getIndexFromXMLArray(int p, String response) {
        return Arrays.asList(mContext.getResources().getStringArray(p)).indexOf(response);
    }

    // Convert the UTC time to Local for API
    private String LocalForApi(String date) {
        String reformed = "";
        if (date.length() == 0) return reformed;
        try {
            reformed = KTime.ParseToFormat(date, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
        } catch (ExpParseToCalendar expParseToCalendar) {
            reformed = KTime.ParseNow(KTime.KT_fmtDate3339k).toString();
        }
        return reformed;
    }

    private String BlankForNull(String value) {
        return value != null ? value : "";
    }
    private int BlankForNull(Integer value) {
        return value != null ? value : 0;
    }

    private int BlankForNull(int value) {
        return value;
    }

    private boolean BlankForNull(boolean value) {
        return value;
    }

    private double BlankForNull(double value) {
        return value;
    }

    // Since these files we need to encode are images, easiest to just use the bitmap class.
    private String EncodeImageBase64(String filename) {
        if (filename.length() == 0) {
            return "";
        }
        filename = Environment.getExternalStorageDirectory().toString() + SIGNATURE_FILE_PATH + "/" + filename;

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(filename);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bStream);
        byte[] imageBytes = bStream.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Build out the field from the disparate items.  The mDwrQuestions map needs to be
    // included with the constructor if this method is to work. When setting the checkUpload
    // to false, the default behavior is to upload the data unless explicitly restricted
    // by the dynamics.  Most items should use true, which means to default to requiring
    // dynamics explicitly allow uploading the data.
    // Turns out the web admin uses the "isNotApplicable" to display or not display the field.
    // This means we will need to be explicit in the dynamics about input fields (as visible
    // or not visible) to properly display them on the web.
    private DwrAnswerWS GetAnswerByCode(String value, int key, String code, Map<String, LayoutHelperDwr.Qualities> useageCodeMap) {
        return GetAnswerByCode(value, key, code, useageCodeMap, true);
    }
    private DwrAnswerWS GetAnswerByCode(String value, int key, String code, Map<String, LayoutHelperDwr.Qualities> useageCodeMap, boolean defaultNoUpload) {
        DwrAnswerWS field = new DwrAnswerWS();

        field.id = key;
        Integer holdPid = mDwrQuestions.get(code);
        field.formFieldPlacementId = holdPid != null ? holdPid : -1;
        field.response = value;
        field.usageCode = code;
        field.isNotApplicable = defaultNoUpload;
        if(useageCodeMap.containsKey(code)) {
            LayoutHelperDwr.Qualities holdQ = useageCodeMap.get(code);
            if (holdQ != null) {
                if (holdQ.Visibility != 8) {
                    field.isNotApplicable = false;
                }
            }
        }
        return field;
    }

    // All responses (values) are strings
    private DwrAnswerWS GetAnswerByCode(int value, int key, String code, Map<String, LayoutHelperDwr.Qualities> useageCodeMap) {
        return GetAnswerByCode(String.valueOf(value), key, code, useageCodeMap);
    }

    // All responses (values) are strings
    private DwrAnswerWS GetAnswerByCode(boolean value, int key, String code, Map<String, LayoutHelperDwr.Qualities> useageCodeMap) {
        return GetAnswerByCode(String.valueOf(value), key, code, useageCodeMap);
    }

    // All responses (values) are strings
    private DwrAnswerWS GetAnswerByCode(double value, int key, String code, Map<String, LayoutHelperDwr.Qualities> useageCodeMap) {
        return GetAnswerByCode(String.valueOf(value), key, code, useageCodeMap);
    }

    //Trims the hours away from the date and appends all 0s instead example 2018-12-06T22:19:32 -> 2018-12-06T00:00:00
    private String TrimDate(String date) {
        if(date == null) return "";
        if (date.length() > 0) {
            int endofday = date.indexOf("T") + 1;
            return date.substring(0, endofday) + "00:00:00+0000";
        }
        return date;
    }

    //  These two methods convert back and forth between the API and Local data for non billable day reason.
    private String nonBillableToAPI(String nonbillableday) {
        if(nonbillableday == null || nonbillableday.length() == 0) {
            return "";
        }
        List<String> list = Arrays.asList(mContext.getResources().getStringArray(R.array.nonbilledreason_name_max));
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(nonbillableday)) {
                return Arrays.asList(mContext.getResources().getStringArray(R.array.nonbilledreason_name_code)).get(i);
            }
        }
        return "";
    }
    private String nonBillableFromAPI(String nonbillableday) {
        if(nonbillableday == null || nonbillableday.length() == 0) {
            return "";
        }
        List<String> list = Arrays.asList(mContext.getResources().getStringArray(R.array.nonbilledreason_name_code));
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(nonbillableday)) {
                return Arrays.asList(mContext.getResources().getStringArray(R.array.nonbilledreason_name_max)).get(i);
            }
        }
        return "";
    }

    /* The version is the first part of the versioninformation field. */
    private String ParseVersion(String details){
        if(details == null) return mContext.getResources().getString(R.string.unknown);
        String [] parts = details.split("_", 2);
        if(parts.length == 0) return mContext.getResources().getString(R.string.unknown);
        return parts[0];
    }

    /* The metrics is the second part of the versioninformation field. */
    private String ParseMetrics(String details){
        if(details == null) return "";
        String [] parts = details.split("_", 2);
        if(parts.length != 2) return mContext.getResources().getString(R.string.unknown);
        return parts[1];
    }
}
