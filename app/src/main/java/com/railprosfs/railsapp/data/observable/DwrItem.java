package com.railprosfs.railsapp.data.observable;

import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.library.baseAdapters.BR;

import com.railprosfs.railsapp.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import static com.railprosfs.railsapp.utility.Constants.SIGNATURE_FILE_PATH;

public class DwrItem extends RRBaseObservable {

    public String workerName;
    public String labelStatus;
    public int statusIcon;
    public int dwrSrvrId;
    public long timeLogged = 0; // keeps track of time on screen for Austin
    // Project
    public String workDate;
    public String classification;
    public String nonBilledReason;
    public String locationCity;
    public String locationState;
    public String contractName;
    public String property;
    private int jobNumberId;
    public String jobNumberDspl;
    public String InputWMLine;
    public String InputWMStation;
    public String InputWMStationName;
    public String InputWMTrack;
    public boolean supervisorJob;
    public String restrictionsJob;
    // Railroad
    public String cnDataN_SO_CC;
    public String cnDataNumber;
    public String cnDataCounty;
    public String kcsDataContractorCnt;
    public String kctDataTaskOrder;
    public String upDataDotXing;
    public String upDataFolderNbr;
    public String upDataServiceUnit;
    public int csxDataRegionId;
    public String csxDataRegionDspl;
    public String csxDataOpNbr;
    public String typeOfVehicle = "";
    public String wmataCallNumber;
    // Workzone
    public boolean onGoing;
    public String roadMaster;
    public String district;
    public String subdivision;
    public List<String> SubDivisionList;
    public String mpStart;
    public String mpEnd;
    public String workingTrack;
    public int workOnTrackId;
    public String workOnTrackDspl;
    //Protection
    public boolean p707;
    public boolean p1102;
    public boolean p1107;
    public boolean pEC1;
    public boolean pFormB;
    public boolean pFormC;
    public boolean pFormW;
    public boolean pForm23;
    public boolean pForm23Y;
    public boolean pDerails;
    public boolean pTrackTime;
    public boolean pTrackWarrant;
    public boolean pTrackAuthority;
    public boolean pObserver;
    public boolean pNoProtection;
    public boolean pLookout;
    public boolean pLiveFlagman;
    public boolean pVerbalPermission;
    // Details
    public String description;
    public String comment01;
    public String comment02;
    public String comment03;
    public String comment04;
    public String comment05;
    public String comment06;
    public String comment07;
    public String comment08;
    public String comment09;
    public String comment10;
    public String comment11;
    public String comment12;
    public String comment13;
    public String comment14;
    public String comment15;
    public String descWeatherConditions;
    public String descTypeOfWork;
    public String descInsideRow;
    public String descOutsideRow;
    public String descUnusual;
    public String descLocationStart;
    // Time
    public String workStartTime;
    public String workEndTime;
    public String workHoursRounded;
    public boolean notPresentOnTrack;
    public boolean performedTraining;
    public String travelToJobStartTime;
    public String travelToJobEndTime;
    public String travelToJobHours;
    public String travelFromJobStartTime;
    public String travelFromJobEndTime;
    public String travelFromJobHours;
    public String lunchHoursStartTime;
    public String lunchHoursEndTime;
    public String workLunchTime = "";
    public String briefHoursStartTime;
    public String briefHoursEndTime;
    public String workBriefTime = "";
    public String specialCostCenterReal = "";
    public String specialCostCenterDspl = "";
    public String allCostCenters = "";
    // Miles
    public String milesToJob;
    public String milesFromJob;
    public String jobMileage;
    public int totalMileage;
    public int perdiemId = 0;
    public String perdiemDspl = "No Per Diem";
    // Signature
    public String railroadContact;
    public String clientName;
    public String clientPhone;
    public String clientEmail;
    public String address;
    public String flagmanSignaturePhotoName;
    public String flagmanSignaturePhotoDate;    // Date is applied when signature is added
    public String clientSignaturePhotoName;
    public String clientSignaturePhotoDate;     // Date is applied when signature is added
    public String railSignaturePhotoName;
    public String railSignaturePhotoDate;      // Date is applied when signature is added
    // Review
    public String reviewerNotes;
    public String reviewerName;
    public String reviewerOn;

    public Uri pictureSignInUri;
    public Uri pictureAnyUri;

    // For now we initialize these fields here, as the local database does not want them to be null.
    public String rwicPhone = "";
    public boolean csxShiftNew;
    public boolean csxShiftRelief;
    public String csxShiftRelieved = "";
    public String csxPeopleRow = "";
    public String csxEquipmentRow = "";
    public String descWeatherHigh = "";
    public String descWeatherLow = "";
    public String roadMasterPhone = "";
    public String descWorkPlanned = "";
    public String descSafety = "";
    public boolean isCsx = false;
    public boolean isScrra = false;

    // New Properties
    public String constructionDay = "";
    public String inputTotalWorkDays = "";
    public String inputDescWeatherWind = "";
    public String inputDescWeatherRain = "";

    // Dwr RoadwayFlagging
    public boolean hasRoadwayFlagging = false;
    public String eightyTwoT = "";
    public String streetName = "";
    public String milePostsForStreet = "";

    //Used for loading saved images
    @BindingAdapter({"android:picassoImage"})
    public static void loadImage(ImageView view, String imageName) {
        if(imageName == null) return;
        if(imageName.length() == 0) return;
        String photoPath = Environment.getExternalStorageDirectory() + SIGNATURE_FILE_PATH;
        try {
            File imageFile = new File(photoPath, imageName);
            Picasso.get()
                    .load(imageFile)
                    .fit()
                    .into(view);
            view.getParent().requestChildFocus(view,view);
        }
        catch (Exception e) {
        }
    }

    //Used for loading in Uri
    @BindingAdapter({"android:picassoImage90"})
    public static void loadImage(ImageView view, Uri imagePath) {
        try {
            Picasso.get()
                    .load(imagePath)
                    .placeholder(R.drawable.placeholder)
                    .fit()
                    .centerCrop()
                    .rotate(90)
                    .into(view);
        }
        catch (Exception e) {

        }
    }


    @Bindable
    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
        notifyPropertyChanged(BR.classification);
    }

    @Bindable
    public String getNonBilledReason() {
        return nonBilledReason;
    }

    public DwrItem setNonBilledReason(String nonBilledReason) {
        this.nonBilledReason = nonBilledReason;
        notifyPropertyChanged(BR.nonBilledReason);
        return this;
    }

    @Bindable
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
        notifyPropertyChanged(BR.property);
    }

    @Bindable
    public String getJobNumber() {
        return jobNumberDspl;
    }

    public void setJobNumber(String jobNumberDspl) {
        this.jobNumberDspl = jobNumberDspl;
        notifyPropertyChanged(BR.jobNumber);
    }

    @Bindable
    public int getJobNumberId() { return jobNumberId; }

    public void setJobNumberId(int jobNumberId) {
        this.jobNumberId = jobNumberId;
        notifyPropertyChanged(BR.jobNumber);
    }

    @Bindable
    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
        notifyPropertyChanged(BR.locationCity);
    }

    @Bindable
    public String getContractName() {
        return contractName;
    }

    public DwrItem setContractName(String contractName) {
        this.contractName = contractName;
        notifyPropertyChanged(BR.contractName);
        return this;
    }

    @Bindable
    public String getWorkDate() {
        return workDate;
    }

    public DwrItem setWorkDate(String workDate) {
        this.workDate = workDate;
        notifyPropertyChanged(BR.workDate);
        return this;
    }

    @Bindable
    public String getWorkerName() {
        return workerName;
    }

    public DwrItem setWorkerName(String workerName) {
        this.workerName = workerName;
        notifyPropertyChanged(BR.workerName);
        return this;
    }

    @Bindable
    public String getLabelStatus() {
        return labelStatus;
    }

    public DwrItem setLabelStatus(String labelStatus) {
        this.labelStatus = labelStatus;
        notifyPropertyChanged(BR.labelStatus);
        return this;
    }

    @Bindable
    public String getLocationState() {
        return locationState;
    }

    public DwrItem setLocationState(String locationState) {
        this.locationState = locationState;
        notifyPropertyChanged(BR.locationState);
        return this;
    }

    @Bindable
    public String getCnDataN_SO_CC() {
        return cnDataN_SO_CC;
    }

    public DwrItem setCnDataN_SO_CC(String cnDataN_SO_CC) {
        this.cnDataN_SO_CC = cnDataN_SO_CC;
        notifyPropertyChanged(BR.cnDataN_SO_CC);
        return this;
    }

    @Bindable
    public String getCnDataNumber() {
        return cnDataNumber;
    }

    public DwrItem setCnDataNumber(String cnDataNumber) {
        this.cnDataNumber = cnDataNumber;
        notifyPropertyChanged(BR.cnDataNumber);
        return this;
    }

    @Bindable
    public String getCnDataCounty() {
        return cnDataCounty;
    }

    public DwrItem setCnDataCounty(String cnDataCounty) {
        this.cnDataCounty = cnDataCounty;
        notifyPropertyChanged(BR.cnDataCounty);
        return this;
    }

    @Bindable
    public int getCsxDataRegionId() {
        return csxDataRegionId;
    }

    public void setCsxDataRegionId(int csxDataRegionId) {
        this.csxDataRegionId = csxDataRegionId;
        notifyPropertyChanged(BR.csxDataRegionId);
    }

    @Bindable
    public String getCsxDataRegionDspl() {
        return csxDataRegionDspl;
    }

    public void setCsxDataRegionDspl(String csxDataRegionDspl) {
        this.csxDataRegionDspl = csxDataRegionDspl;
        notifyPropertyChanged(BR.csxDataRegionDspl);
    }

    @Bindable
    public String getCsxDataOpNbr() {
        return csxDataOpNbr;
    }

    public DwrItem setCsxDataOpNbr(String csxDataOpNbr) {
        this.csxDataOpNbr = csxDataOpNbr;
        notifyPropertyChanged(BR.csxDataOpNbr);
        return this;
    }

    @Bindable
    public String getTypeOfVehicle() {
        return typeOfVehicle;
    }

    public DwrItem setTypeOfVehicle(String typeOfVehicle) {
        this.typeOfVehicle = typeOfVehicle;
        notifyPropertyChanged(BR.typeOfVehicle);
        return this;
    }

    @Bindable
    public String getWmataCallNumber() {
        return wmataCallNumber;
    }

    public DwrItem setWmataCallNumber(String wmataCallNumber) {
        this.wmataCallNumber = wmataCallNumber;
        notifyPropertyChanged(BR.wmataCallNumber);
        return this;
    }


    @Bindable
    public String getKcsDataContractorCnt() {
        return kcsDataContractorCnt;
    }

    public DwrItem setKcsDataContractorCnt(String kcsDataContractorCnt) {
        this.kcsDataContractorCnt = kcsDataContractorCnt;
        notifyPropertyChanged(BR.kcsDataContractorCnt);
        return this;
    }

    @Bindable
    public String getKctDataTaskOrder() {
        return kctDataTaskOrder;
    }

    public DwrItem setKctDataTaskOrder(String kctDataTaskOrder) {
        this.kctDataTaskOrder = kctDataTaskOrder;
        notifyPropertyChanged(BR.kctDataTaskOrder);
        return this;
    }

    @Bindable
    public String getUpDataDotXing() {
        return upDataDotXing;
    }

    public DwrItem setUpDataDotXing(String upDataDotXing) {
        this.upDataDotXing = upDataDotXing;
        notifyPropertyChanged(BR.upDataDotXing);
        return this;
    }

    @Bindable
    public String getUpDataFolderNbr() {
        return upDataFolderNbr;
    }

    public DwrItem setUpDataFolderNbr(String upDataFolderNbr) {
        this.upDataFolderNbr = upDataFolderNbr;
        notifyPropertyChanged(BR.upDataFolderNbr);
        return this;
    }

    @Bindable
    public String getUpDataServiceUnit() {
        return upDataServiceUnit;
    }

    public DwrItem setUpDataServiceUnit(String upDataServiceUnit) {
        this.upDataServiceUnit = upDataServiceUnit;
        notifyPropertyChanged(BR.upDataServiceUnit);
        return this;
    }

    @Bindable
    public String getFlagmanSignaturePhotoName() {
        return flagmanSignaturePhotoName;
    }

    public DwrItem setFlagmanSignaturePhotoName(String flagmanSignaturePhotoName) {
        this.flagmanSignaturePhotoName = flagmanSignaturePhotoName;
        notifyPropertyChanged(BR.flagmanSignaturePhotoName);
        return this;
    }

    @Bindable
    public String getFlagmanSignaturePhotoDate() { return flagmanSignaturePhotoDate; }

    public DwrItem setFlagmanSignaturePhotoDate(String flagmanSignaturePhotoDate) {
        this.flagmanSignaturePhotoDate = flagmanSignaturePhotoDate;
        notifyPropertyChanged(BR.flagmanSignaturePhotoDate);
        return this;
    }

    @Bindable
    public String getClientSignaturePhotoName() {
        return clientSignaturePhotoName;
    }

    public DwrItem setClientSignaturePhotoName(String clientSignaturePhotoName) {
        this.clientSignaturePhotoName = clientSignaturePhotoName;
        notifyPropertyChanged(BR.clientSignaturePhotoName);
        return this;
    }

    @Bindable
    public String getClientSignaturePhotoDate() { return clientSignaturePhotoDate; }

    public DwrItem setClientSignaturePhotoDate(String clientSignaturePhotoDate) {
        this.clientSignaturePhotoDate = clientSignaturePhotoDate;
        notifyPropertyChanged(BR.clientSignaturePhotoDate);
        return this;
    }

    @Bindable
    public String getRailSignaturePhotoName() {
        return railSignaturePhotoName;
    }

    public DwrItem setRailSignaturePhotoName(String railSignaturePhotoName) {
        this.railSignaturePhotoName = railSignaturePhotoName;
        notifyPropertyChanged(BR.railSignaturePhotoName);
        return this;
    }

    @Bindable
    public String getRailSignaturePhotoDate() { return railSignaturePhotoDate; }

    public DwrItem setRailSignaturePhotoDate(String railSignaturePhotoDate) {
        this.railSignaturePhotoDate = railSignaturePhotoDate;
        notifyPropertyChanged(BR.railSignaturePhotoDate);
        return this;
    }

    @Bindable
    public Uri getPictureSignInUri() {
        return pictureSignInUri;
    }

    public DwrItem setPictureSignInUri(Uri pictureSignInUri) {
        this.pictureSignInUri = pictureSignInUri;
        notifyPropertyChanged(BR.pictureSignInUri);
        return this;
    }

    @Bindable
    public String getDistrict() {
        return district;
    }

    public DwrItem setDistrict(String district) {
        this.district = district;
        notifyPropertyChanged(BR.district);
        return this;
    }

    @Bindable
    public String getSubdivision() {
        return subdivision;
    }

    public DwrItem setSubdivision(String subdivision) {
        this.subdivision = subdivision;
        notifyPropertyChanged(BR.subdivision);
        return this;
    }

    @Bindable
    public String getSpecialCostCenterReal() {
        return specialCostCenterReal;
    }

    public DwrItem setSpecialCostCenterReal(String specialCostCenterReal) {
        this.specialCostCenterReal = specialCostCenterReal;
        notifyPropertyChanged(BR.specialCostCenterReal);
        return this;
    }

    @Bindable
    public String getSpecialCostCenterDspl() {
        return specialCostCenterDspl;
    }

    public DwrItem setSpecialCostCenterDspl(String specialCostCenterDspl) {
        this.specialCostCenterDspl = specialCostCenterDspl;
        notifyPropertyChanged(BR.specialCostCenterDspl);
        return this;
    }

    @Bindable
    public String getAllCostCenters() {
        return allCostCenters;
    }

    public DwrItem setAllCostCenters(String allCostCenters) {
        this.allCostCenters = allCostCenters;
        notifyPropertyChanged(BR.allCostCenters);
        return this;
    }

    @Bindable
    public String getRoadMaster() {
        return roadMaster;
    }

    public DwrItem setRoadMaster(String roadMaster) {
        this.roadMaster = roadMaster;
        notifyPropertyChanged(BR.roadMaster);
        return this;
    }

    @Bindable
    public String getMpStart() {
        return mpStart;
    }

    public DwrItem setMpStart(String mpStart) {
        this.mpStart = mpStart;
        notifyPropertyChanged(BR.mpStart);
        return this;
    }

    @Bindable
    public String getWorkingTrack() {
        return workingTrack;
    }

    public DwrItem setWorkingTrack(String workingTrack) {
        this.workingTrack = workingTrack;
        notifyPropertyChanged(BR.workingTrack);
        return this;
    }

    @Bindable
    public String getMpEnd() {
        return mpEnd;
    }

    public DwrItem setMpEnd(String mpEnd) {
        this.mpEnd = mpEnd;
        notifyPropertyChanged(BR.mpEnd);
        return this;
    }


    @Bindable
    public int getWorkOnTrackId() {
        return workOnTrackId;
    }

    public void setWorkOnTrackId(int workOnTrackId) {
        this.workOnTrackId = workOnTrackId;
        notifyPropertyChanged(BR.workOnTrackId);
    }

    @Bindable
    public String getWorkOnTrackDspl() {
        return workOnTrackDspl;
    }

    public void setWorkOnTrackDspl(String workOnTrackDspl) {
        this.workOnTrackDspl = workOnTrackDspl;
        notifyPropertyChanged(BR.workOnTrackDspl);
    }

    @Bindable
    public String getComment01() {
        if(comment01 == null) return "";
        return comment01;
    }

    public DwrItem setComment01(String comment) {
        this.comment01 = comment;
        notifyPropertyChanged(BR.comment01);
        return this;
    }

    @Bindable
    public String getComment02() {
        if(comment02 == null ) return "";
        return comment02;
    }

    public DwrItem setComment02(String comment) {
        this.comment02 = comment;
        notifyPropertyChanged(BR.comment02);
        return this;
    }

    @Bindable
    public String getComment03() {
        if(comment03 == null ) return "";
        return comment03;
    }

    public DwrItem setComment03(String comment) {
        this.comment03 = comment;
        notifyPropertyChanged(BR.comment03);
        return this;
    }

    @Bindable
    public String getComment04() {
        if(comment04 == null ) return "";
        return comment04;
    }

    public DwrItem setComment04(String comment) {
        this.comment04 = comment;
        notifyPropertyChanged(BR.comment04);
        return this;
    }

    @Bindable
    public String getComment05() {
        if(comment05 == null ) return "";
        return comment05;
    }

    public DwrItem setComment05(String comment) {
        this.comment05 = comment;
        notifyPropertyChanged(BR.comment05);
        return this;
    }

    @Bindable
    public String getComment06() {
        if(comment06 == null ) return "";
        return comment06;
    }

    public DwrItem setComment06(String comment) {
        this.comment06 = comment;
        notifyPropertyChanged(BR.comment06);
        return this;
    }

    @Bindable
    public String getComment07() {
        if(comment07 == null ) return "";
        return comment07;
    }

    public DwrItem setComment07(String comment) {
        this.comment07 = comment;
        notifyPropertyChanged(BR.comment07);
        return this;
    }

    @Bindable
    public String getComment08() {
        if(comment08 == null ) return "";
        return comment08;
    }

    public DwrItem setComment08(String comment) {
        this.comment08 = comment;
        notifyPropertyChanged(BR.comment08);
        return this;
    }

    @Bindable
    public String getComment09() {
        if(comment09 == null ) return "";
        return comment09;
    }

    public DwrItem setComment09(String comment) {
        this.comment09 = comment;
        notifyPropertyChanged(BR.comment09);
        return this;
    }

    @Bindable
    public String getComment10() {
        if(comment10 == null ) return "";
        return comment10;
    }

    public DwrItem setComment10(String comment) {
        this.comment10 = comment;
        notifyPropertyChanged(BR.comment10);
        return this;
    }

    @Bindable
    public String getComment11() {
        if(comment11 == null ) return "";
        return comment11;
    }

    public DwrItem setComment11(String comment) {
        this.comment11 = comment;
        notifyPropertyChanged(BR.comment11);
        return this;
    }

    @Bindable
    public String getComment12() {
        if(comment12 == null ) return "";
        return comment12;
    }

    public DwrItem setComment12(String comment) {
        this.comment12 = comment;
        notifyPropertyChanged(BR.comment12);
        return this;
    }

    @Bindable
    public String getComment13() {
        if(comment13 == null ) return "";
        return comment13;
    }

    public DwrItem setComment13(String comment) {
        this.comment13 = comment;
        notifyPropertyChanged(BR.comment13);
        return this;
    }

    @Bindable
    public String getComment14() {
        if(comment14 == null ) return "";
        return comment14;
    }

    public DwrItem setComment14(String comment) {
        this.comment14 = comment;
        notifyPropertyChanged(BR.comment14);
        return this;
    }

    @Bindable
    public String getComment15() {
        if(comment15 == null ) return "";
        return comment15;
    }

    public DwrItem setComment15(String comment) {
        this.comment15 = comment;
        notifyPropertyChanged(BR.comment15);
        return this;
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public DwrItem setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
        return this;
    }

    @Bindable
    public String getDescWeatherConditions() {
        return descWeatherConditions;
    }

    public DwrItem setDescWeatherConditions(String descWeatherConditions) {
        this.descWeatherConditions = descWeatherConditions;
        notifyPropertyChanged(BR.descWeatherConditions);
        return this;
    }

    @Bindable
    public boolean getP707() {
        return p707;
    }

    public void setP707(boolean p707) {
        if(this.p707 != p707) {
            this.p707 = p707;

            notifyPropertyChanged(BR.p707);
        }
    }

    @Bindable
    public boolean getP1102() {
        return p1102;
    }

    public void setP1102(boolean p1102) {
        if (this.p1102 != p1102) {
            this.p1102 = p1102;
            notifyPropertyChanged(BR.p1102);
        }
    }

    @Bindable
    public boolean getP1107() {
        return p1107;
    }

    public void setP1107(boolean p1107) {
        if (this.p1107 != p1107) {
            this.p1107 = p1107;
            notifyPropertyChanged(BR.p1107);
        }
    }

    @Bindable
    public boolean getPEc1() {
        return pEC1;
    }

    public void setPEc1(boolean pEc1) {
        if (this.pEC1 != pEc1) {
            this.pEC1 = pEc1;
            notifyPropertyChanged(BR.pEc1);
        }
    }

    @Bindable
    public boolean getPFormB() {
        return pFormB;
    }

    public void setPFormB(boolean pFormB) {
        if (this.pFormB != pFormB) {
            this.pFormB = pFormB;
            notifyPropertyChanged(BR.pFormB);
        }
    }

    @Bindable
    public boolean getPFormC() {
        return pFormC;
    }

    public void setPFormC(boolean pFormC) {
        if (this.pFormC != pFormC) {
            this.pFormC = pFormC;
            notifyPropertyChanged(BR.pFormC);
        }
    }

    @Bindable
    public boolean getPFormW() {
        return pFormW;
    }

    public void setPFormW(boolean pFormW) {
        if (this.pFormW != pFormW) {
            this.pFormW = pFormW;
            notifyPropertyChanged(BR.pFormW);
        }
    }

    @Bindable
    public boolean getPForm23() {
        return pForm23;
    }

    public void setPForm23(boolean pForm23) {
        if (this.pForm23 != pForm23) {
            this.pForm23 = pForm23;
            notifyPropertyChanged(BR.pForm23);
        }
    }

    @Bindable
    public boolean getPForm23Y() {
        return pForm23Y;
    }

    public void setPForm23Y(boolean pForm23Y) {
        if (this.pForm23Y != pForm23Y) {
            this.pForm23Y = pForm23Y;
            notifyPropertyChanged(BR.pForm23Y);
        }
    }

    @Bindable
    public boolean getPDerails() {
        return pDerails;
    }

    public void setPDerails(boolean pDerails) {
        if (this.pDerails != pDerails) {
            this.pDerails = pDerails;
            notifyPropertyChanged(BR.pDerails);
        }
    }

    @Bindable
    public boolean getPTrackTime() {
        return pTrackTime;
    }

    public void setPTrackTime(boolean pTrackTime) {
        if (this.pTrackTime != pTrackTime) {
            this.pTrackTime = pTrackTime;
            notifyPropertyChanged(BR.pTrackTime);
        }
    }

    @Bindable
    public boolean getPWarrant() {
        return pTrackWarrant;
    }

    public void setPWarrant(boolean pWarrant) {
        if (this.pTrackWarrant != pWarrant) {
            this.pTrackWarrant = pWarrant;
            notifyPropertyChanged(BR.pWarrant);
        }
    }

    @Bindable
    public boolean getPAuthority() {
        return pTrackAuthority;
    }

    public void setPAuthority(boolean pAuthority) {
        if (this.pTrackAuthority != pAuthority) {
            this.pTrackAuthority = pAuthority;
            notifyPropertyChanged(BR.pAuthority);
        }
    }

    @Bindable
    public boolean getPObserver() {
        return pObserver;
    }

    public void setPObserver(boolean pObserver) {
        if (this.pObserver != pObserver) {
            this.pObserver = pObserver;
            notifyPropertyChanged(BR.pObserver);
        }
    }

    @Bindable
    public boolean getPNoProtect() {
        return pNoProtection;
    }

    public void setPNoProtect(boolean pNoProtect) {
        if (this.pNoProtection != pNoProtect) {
            this.pNoProtection = pNoProtect;
            notifyPropertyChanged(BR.pNoProtect);
        }
    }

    @Bindable
    public boolean getPWatchman() {
        return pLookout;
    }

    public void setPWatchman(boolean pWatchman) {
        if (this.pLookout != pWatchman) {
            this.pLookout = pWatchman;
            notifyPropertyChanged(BR.pWatchman);
        }
    }

    @Bindable
    public boolean getPVerbalPermission() {
        return pVerbalPermission;
    }

    public void setPVerbalPermission(boolean pVerbalPermission) {
        if (this.pVerbalPermission != pVerbalPermission) {
            this.pVerbalPermission = pVerbalPermission;
            notifyPropertyChanged(BR.pVerbalPermission);
        }
    }

    @Bindable
    public boolean getPLiveFlagman() {
        return pLiveFlagman;
    }

    public void setPLiveFlagman(boolean pLiveFlagman) {
        if (this.pLiveFlagman != pLiveFlagman) {
            this.pLiveFlagman = pLiveFlagman;
            notifyPropertyChanged(BR.pLiveFlagman);
        }
    }

    @Bindable
    public boolean getOnGoing() {
        return onGoing;
    }

    public void setOnGoing(boolean onGoing) {
        this.onGoing = onGoing;
        notifyPropertyChanged(BR.onGoing);
    }

    @Bindable
    public boolean getSupervisorJob() {
        return supervisorJob;
    }

    public void setSupervisorJob(boolean supervisorJob) {
        this.supervisorJob = supervisorJob;
    }

    @Bindable
    public String getRestrictionsJob() {
        return restrictionsJob != null ? restrictionsJob : "";
    }

    public void setRestrictionsJob(String restrictionsJob) {
        this.restrictionsJob = restrictionsJob;
    }

    @Bindable
    public String getDescTypeOfWork() {
        return descTypeOfWork;
    }

    public DwrItem setDescTypeOfWork(String descTypeOfWork) {
        this.descTypeOfWork = descTypeOfWork;
        notifyPropertyChanged(BR.descTypeOfWork);
        return this;
    }

    @Bindable
    public String getDescInsideRow() {
        return descInsideRow;
    }

    public DwrItem setDescInsideRow(String descInsideRow) {
        this.descInsideRow = descInsideRow;
        notifyPropertyChanged(BR.descInsideRow);
        return this;
    }

    @Bindable
    public String getDescOutsideRow() {
        return descOutsideRow;
    }

    public DwrItem setDescOutsideRow(String descOutsideRow) {
        this.descOutsideRow = descOutsideRow;
        notifyPropertyChanged(BR.descOutsideRow);
        return this;
    }

    @Bindable
    public String getDescUnusual() {
        return descUnusual;
    }

    public DwrItem setDescUnusual(String descUnusual) {
        this.descUnusual = descUnusual;
        notifyPropertyChanged(BR.descUnusual);
        return this;
    }

    @Bindable
    public String getDescLocationStart() {
        return descLocationStart;
    }

    public DwrItem setDescLocationStart(String descLocationStart) {
        this.descLocationStart = descLocationStart;
        notifyPropertyChanged(BR.descLocationStart);
        return this;
    }

    @Bindable
    public String getWorkStartTime() {
        return workStartTime;
    }

    public DwrItem setWorkStartTime(String workStartTime) {
        this.workStartTime = workStartTime;
        notifyPropertyChanged(BR.workStartTime);
        return this;
    }

    @Bindable
    public String getWorkEndTime() {
        return workEndTime;
    }

    public DwrItem setWorkEndTime(String workEndTime) {
        this.workEndTime = workEndTime;
        notifyPropertyChanged(BR.workEndTime);
        return this;
    }

    @Bindable
    public String getWorkHoursRounded() {
        return workHoursRounded;
    }

    public DwrItem setWorkHoursRounded(String workHoursRounded) {
        this.workHoursRounded = workHoursRounded;
        notifyPropertyChanged(BR.workHoursRounded);
        return this;
    }

    @Bindable
    public boolean getNotPresentOnTrack() {
        return notPresentOnTrack;
    }

    public void setNotPresentOnTrack(boolean notPresentOnTrack) {
        if (this.notPresentOnTrack != notPresentOnTrack) {
            this.notPresentOnTrack = notPresentOnTrack;
            notifyPropertyChanged(BR.notPresentOnTrack);
        }
    }

    @Bindable
    public boolean getPerformedTraining() {
        return performedTraining;
    }

    public void setPerformedTraining(boolean performedTraining) {
        if (this.performedTraining != performedTraining) {
            this.performedTraining = performedTraining;
            notifyPropertyChanged(BR.performedTraining);
        }
    }

    @Bindable
    public String getRailroadContact() {
        return railroadContact;
    }

    public DwrItem setRailroadContact(String railroadContact) {
        this.railroadContact = railroadContact;
        notifyPropertyChanged(BR.railroadContact);
        return this;
    }

    @Bindable
    public String getClientName() {
        return clientName;
    }

    public DwrItem setClientName(String clientName) {
        this.clientName = clientName;
        notifyPropertyChanged(BR.clientName);
        return this;
    }

    @Bindable
    public String getClientPhone() {
        return clientPhone;
    }

    public DwrItem setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
        notifyPropertyChanged(BR.clientPhone);
        return this;
    }

    @Bindable
    public String getClientEmail() {
        return clientEmail;
    }

    public DwrItem setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
        notifyPropertyChanged(BR.clientEmail);
        return this;
    }

    @Bindable
    public String getAddress() {
        return address;
    }

    public DwrItem setAddress(String address) {
        this.address = address;
        notifyPropertyChanged(BR.clientEmail);
        return this;
    }

    @Bindable
    public String getTravelToJobStartTime() {
        return travelToJobStartTime;
    }

    public DwrItem setTravelToJobStartTime(String travelToJobStartTime) {
        this.travelToJobStartTime = travelToJobStartTime;
        notifyPropertyChanged(BR.travelToJobStartTime);
        return this;
    }

    @Bindable
    public String getTravelToJobEndTime() {
        return travelToJobEndTime;
    }

    public DwrItem setTravelToJobEndTime(String travelToJobEndTime) {
        this.travelToJobEndTime = travelToJobEndTime;
        notifyPropertyChanged(BR.travelToJobEndTime);
        return this;
    }

    @Bindable
    public String getTravelToJobHours() {
        return travelToJobHours;
    }

    public DwrItem setTravelToJobHours(String travelToJobHours) {
        this.travelToJobHours = travelToJobHours;
        notifyPropertyChanged(BR.travelToJobHours);
        return this;
    }

    @Bindable
    public String getTravelFromJobStartTime() {
        return travelFromJobStartTime;
    }

    public DwrItem setTravelFromJobStartTime(String travelFromJobStartTime) {
        this.travelFromJobStartTime = travelFromJobStartTime;
        notifyPropertyChanged(BR.travelFromJobStartTime);
        return this;
    }

    @Bindable
    public String getTravelFromJobEndTime() {
        return travelFromJobEndTime;
    }

    public DwrItem setTravelFromJobEndTime(String travelFromJobEndTime) {
        this.travelFromJobEndTime = travelFromJobEndTime;
        notifyPropertyChanged(BR.travelFromJobEndTime);
        return this;
    }

    @Bindable
    public String getTravelFromJobHours() {
        return travelFromJobHours;
    }

    public DwrItem setTravelFromJobHours(String travelFromJobHours) {
        this.travelFromJobHours = travelFromJobHours;
        notifyPropertyChanged(BR.travelFromJobHours);
        return this;
    }

    @Bindable
    public String getMilesToJob() {
        return milesToJob;
    }

    public void setMilesToJob(String milesToJob) {
        this.milesToJob = milesToJob;
        notifyPropertyChanged(BR.milesToJob);
    }

    @Bindable
    public String getMilesFromJob() {
        return milesFromJob;
    }

    public void setMilesFromJob(String milesFromJob) {
        this.milesFromJob = milesFromJob;
        notifyPropertyChanged(BR.milesFromJob);
    }

    @Bindable
    public String getJobMileage() {
        return jobMileage;
    }

    public void setJobMileage(String jobMileage) {
        this.jobMileage = jobMileage;
        notifyPropertyChanged(BR.jobMileage);
    }


    @Bindable
    public int getTotalMileage() {
        return totalMileage;
    }

    public void setTotalMileage(int totalMileage) {
        this.totalMileage = totalMileage;
        notifyPropertyChanged(BR.totalMileage);
    }

    @Bindable
    public int getPerdiemId() {
        return perdiemId;
    }

    public void setPerdiemId(int perdiemId) {
        this.perdiemId = perdiemId;
        notifyPropertyChanged(BR.perdiemId);
    }

    @Bindable
    public String getPerdiemDspl() {
        return perdiemDspl;
    }

    public void setPerdiemDspl(String perdiemDspl) {
        this.perdiemDspl = perdiemDspl;
        notifyPropertyChanged(BR.perdiemDspl);
    }

    @Bindable
    public String getInputWMLine() {
        return InputWMLine;
    }

    public DwrItem setInputWMLine(String inputWMLine) {
        InputWMLine = inputWMLine;
        notifyPropertyChanged(BR.inputWMLine);
        return this;
    }

    @Bindable
    public String getInputWMStation() {
        return InputWMStation;
    }

    public DwrItem setInputWMStation(String inputWMStation) {
        InputWMStation = inputWMStation;
        notifyPropertyChanged(BR.inputWMStation);
        return this;
    }

    @Bindable
    public String getInputWMStationName() {
        return InputWMStationName;
    }

    public DwrItem setInputWMStationName(String inputWMStationName) {
        InputWMStationName = inputWMStationName;
        return this;
    }

    @Bindable
    public String getInputWMTrack() {
        return InputWMTrack;
    }

    public DwrItem setInputWMTrack(String inputWMTrack) {
        InputWMTrack = inputWMTrack;
        notifyPropertyChanged(BR.inputWMTrack);
        return this;
    }

    @Bindable
    public String getRwicPhone() {
        return rwicPhone;
    }

    public DwrItem setRwicPhone(String rwicPhone) {
        this.rwicPhone = rwicPhone;
        notifyPropertyChanged(BR.rwicPhone);
        return this;
    }

    @Bindable
    public boolean getIsCsx() {
        return isCsx;
    }

    public DwrItem setIsCsx(boolean isCsx) {
        this.isCsx = isCsx;
        notifyPropertyChanged(BR.isCsx);
        return this;
    }

    @Bindable
    public boolean getIsScrra() {
        return isScrra;
    }

    public DwrItem setIsScrra(boolean isScrra) {
        this.isScrra = isScrra;
        notifyPropertyChanged(BR.isScrra);
        return this;
    }

    @Bindable
    public boolean isCsxShiftNew() {
        return csxShiftNew;
    }

    public DwrItem setCsxShiftNew(boolean csxShiftNew) {
        this.csxShiftNew = csxShiftNew;
        notifyPropertyChanged(BR.csxShiftNew);
        return this;
    }

    @Bindable
    public boolean isCsxShiftRelief() {
        return csxShiftRelief;
    }

    public DwrItem setCsxShiftRelief(boolean csxShiftRelief) {
        this.csxShiftRelief = csxShiftRelief;
        notifyPropertyChanged(BR.csxShiftRelief);
        return this;
    }

    @Bindable
    public String getCsxShiftRelieved() {
        return csxShiftRelieved;
    }

    public DwrItem setCsxShiftRelieved(String csxShiftRelieved) {
        this.csxShiftRelieved = csxShiftRelieved;
        notifyPropertyChanged(BR.csxShiftRelieved);
        return this;
    }

    @Bindable
    public String getLunchHoursStartTime() {
        return lunchHoursStartTime;
    }

    public DwrItem setLunchHoursStartTime(String lunchHoursStartTime) {
        this.lunchHoursStartTime = lunchHoursStartTime;
        notifyPropertyChanged(BR.lunchHoursStartTime);
        return this;
    }

    @Bindable
    public String getLunchHoursEndTime() {
        return lunchHoursEndTime;
    }

    public DwrItem setLunchHoursEndTime(String lunchHoursEndTime) {
        this.lunchHoursEndTime = lunchHoursEndTime;
        notifyPropertyChanged(BR.lunchHoursEndTime);
        return this;
    }

    @Bindable
    public String getWorkLunchTime() {
        return workLunchTime;
    }

    public DwrItem setWorkLunchTime(String workLunchTime) {
        this.workLunchTime = workLunchTime;
        notifyPropertyChanged(BR.workLunchTime);
        return this;
    }

    @Bindable
    public String getCsxPeopleRow() {
        return csxPeopleRow;
    }

    public DwrItem setCsxPeopleRow(String csxPeopleRow) {
        this.csxPeopleRow = csxPeopleRow;
        notifyPropertyChanged(BR.csxPeopleRow);
        return this;
    }

    @Bindable
    public String getCsxEquipmentRow() {
        return csxEquipmentRow;
    }

    public DwrItem setCsxEquipmentRow(String csxEquipmentRow) {
        this.csxEquipmentRow = csxEquipmentRow;
        notifyPropertyChanged(BR.csxEquipmentRow);
        return this;
    }

    @Bindable
    public String getDescWeatherHigh() {
        return descWeatherHigh;
    }

    public DwrItem setDescWeatherHigh(String descWeatherHigh) {
        this.descWeatherHigh = descWeatherHigh;
        notifyPropertyChanged(BR.descWeatherHigh);
        return this;
    }

    @Bindable
    public String getDescWeatherLow() {
        return descWeatherLow;
    }

    public DwrItem setDescWeatherLow(String descWeatherLow) {
        this.descWeatherLow = descWeatherLow;
        notifyPropertyChanged(BR.descWeatherLow);
        return this;
    }

    @Bindable
    public String getBriefHoursStartTime() {
        return briefHoursStartTime;
    }

    public DwrItem setBriefHoursStartTime(String briefHoursStartTime) {
        this.briefHoursStartTime = briefHoursStartTime;
        notifyPropertyChanged(BR.briefHoursStartTime);
        return this;
    }

    @Bindable
    public String getBriefHoursEndTime() {
        return briefHoursEndTime;
    }

    public DwrItem setBriefHoursEndTime(String briefHoursEndTime) {
        this.briefHoursEndTime = briefHoursEndTime;
        notifyPropertyChanged(BR.briefHoursEndTime);
        return this;
    }

    @Bindable
    public String getWorkBriefTime() {
        return workBriefTime;
    }

    public DwrItem setWorkBriefTime(String workBriefTime) {
        this.workBriefTime = workBriefTime;
        notifyPropertyChanged(BR.workBriefTime);
        return this;
    }

    @Bindable
    public String getRoadMasterPhone() {
        return roadMasterPhone;
    }

    public DwrItem setRoadMasterPhone(String roadMasterPhone) {
        this.roadMasterPhone = roadMasterPhone;
        notifyPropertyChanged(BR.roadMasterPhone);
        return this;
    }

    @Bindable
    public String getDescWorkPlanned() {
        return descWorkPlanned;
    }

    public DwrItem setDescWorkPlanned(String descWorkPlanned) {
        this.descWorkPlanned = descWorkPlanned;
        notifyPropertyChanged(BR.descWorkPlanned);
        return this;
    }

    @Bindable
    public String getDescSafety() {
        return descSafety;
    }

    public DwrItem setDescSafety(String descSafety) {
        this.descSafety = descSafety;
        notifyPropertyChanged(BR.descSafety);
        return this;
    }

    @Bindable
    public String getConstructionDay() {
        return constructionDay;
    }

    public void setConstructionDay(String constructionDay) {
        this.constructionDay = constructionDay;
        notifyPropertyChanged(BR.constructionDay);
    }

    @Bindable
    public String getInputTotalWorkDays() {
        return inputTotalWorkDays;
    }

    public void setInputTotalWorkDays(String inputTotalWorkDays) {
        this.inputTotalWorkDays = inputTotalWorkDays;
        notifyPropertyChanged(BR.inputTotalWorkDays);
    }

    @Bindable
    public String getInputDescWeatherWind() {
        return inputDescWeatherWind;
    }

    public void setInputDescWeatherWind(String inputDescWeatherWind) {
        this.inputDescWeatherWind = inputDescWeatherWind;
        notifyPropertyChanged(BR.inputDescWeatherWind);
    }

    @Bindable
    public String getInputDescWeatherRain() {
        return inputDescWeatherRain;
    }

    public void setInputDescWeatherRain(String inputDescWeatherRain) {
        this.inputDescWeatherRain = inputDescWeatherRain;
        notifyPropertyChanged(BR.inputDescWeatherRain);
    }

    @Bindable
    public boolean getHasRoadwayFlagging() {
        return hasRoadwayFlagging;
    }

    public void setHasRoadwayFlagging(boolean hasRoadwayFlagging) {
        this.hasRoadwayFlagging = hasRoadwayFlagging;
    }

    @Bindable
    public String getEightyTwoT() {
        return eightyTwoT;
    }

    public void setEightyTwoT(String eightyTwoT) {
        this.eightyTwoT = eightyTwoT;
        notifyPropertyChanged(BR.eightyTwoT);
    }

    @Bindable
    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
        notifyPropertyChanged(BR.streetName);
    }

    @Bindable
    public String getMilePostsForStreet() {
        return milePostsForStreet;
    }

    public void setMilePostsForStreet(String milePostsForStreet) {
        this.milePostsForStreet = milePostsForStreet;
        notifyPropertyChanged(BR.milePostsForStreet);
    }

}
