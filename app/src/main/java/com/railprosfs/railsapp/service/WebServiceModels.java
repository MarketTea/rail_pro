package com.railprosfs.railsapp.service;

import java.util.ArrayList;
import java.util.List;

/**
 * This file contains the various request and reply model classes used for parsing the
 * JSON used in the Web Services.
 */
public interface WebServiceModels {

    class LoginRequest {
        public String userId;
        public String password;
    }

    //************* REAL API FUNCTIONS ****************//
    class LoginResponse {
        public String access_token;
        public String id;
        public String token_type;
        public int expires_in;
    }

    // This works for both Get User and Get Admin User
    class UserResponse {
        public FieldWorkerWS fieldWorker;
        public String email;
        public String[] roles;
    }

    class FieldWorkerWS {
        public int id;
        public String firstName;
        public String lastName;
        public String employeeCode;
        public FieldTeamWS team;
    }

    class FieldTeamWS {
        public int id;
        public String name;
        public RailRoad railroad;
    }

    // Commenting out unused values to avoid accidental filtering.
    class AssignmentRequest {
        int fieldWorkerId;
//        int jobId;
//        String startDate;
//        String endDate;
//        String searchText;
//        String[] sortCriteria;
        int pageSize;
        int pageNumber;
    }

    class AssignmentsResponse {
        List<AssignmentWS> results;
    }

    public class AssignmentWS {
        public String start;
        public String end;
        public JobShortWS job;
        public String fieldContactName;
        public String fieldContactPhone;
        public String fieldContactEmail;
        public List<AssignmentShiftWS> shifts;
    }

    public class JobShortWS {
        public int id;
        public String description;
        public String start;
        public String end;
    }

    public class AssignmentShiftWS {
        public int id;
        public String day;
        public String startTime;
        public String notes;
        public boolean needsJobSetupForm;
        public BaseIdWS serviceType;
    }

    public class JobDetailResponse {
        public int id;
        public String number;
        public String description;
        public String location;
        public String startingMilePost;
        public String start;
        public String end;
        public double latitude;
        public double longitude;
        public CustomerShortWS customer;
        public PropertyShortWS railroad;
        public SubdivisionWS subdivision;
        public List<DocumentWS> documents;
        public String equipmentDescription;
        public String distanceFromTracks;
        public String permitNumber;
        public String notes;
        public String trackSupervisor;
        public boolean isSupervisorJob;
        public boolean isNonBillable;
        public List<ContactWS> fieldContacts;
        public List<OfferedService> offeredServices;
    }

    public class CustomerShortWS {
        public int id;
        public String name;
    }

    public class PropertyShortWS {
        public int id;
        public String code;
        public String name;
    }

    public class SubdivisionWS {
        public int id;
        public String name;
    }

    class DocumentWS {
        int id;
        String title;
        String fileName;
        String description;
        String uploadedOn;
        String timeStamp;
    }

    class DocumentBitsResponse {
        DocumentRawWS item1;
        String item2;
    }

    class DocumentRawWS {
        int id;
        String bytes;
    }

    class CustomerDetailResponse {
        int id;
        String companyName;
        List<ContactWS> contacts;
    }

    public class ContactWS {
        public int id;
        public String firstName;
        public String lastName;
        public List<PhoneWS> phoneNumbers;
        public List<EmailWS> emailAddresses;
    }

    public class PhoneWS {
        public int id;
        public String description;
        public String countryCode;
        public String areaCode;
        public String number;
        public String extension;
    }

    public class EmailWS {
        public int id;
        public String description;
        public String email;
    }

    // Commenting out unused values to avoid accidental filtering.
    class FormsSummaryRequest
    {
        //        public int fieldWorkerId;
//        public int jobId;
//        public int railroadId;
//        public int filledOutFormStatusId;
//        public String searchText;
        public int pageSize;
        public int pageNumber;
    }

    class FormsSummaryResponse
    {
        public int resultCount;
        public int pageNumber;
        public List<FormsSummaryWS> results;
    }

    class FormsSummaryWS
    {
        public int id;
        public String name;
        public String title;
        public TypeWS type;
        public int numberOfQuestions;
    }

    class FormResponse
    {
        public int id;
        public TypeWS type;
        public String name;
        public String title;
        public String instructions;
        public ArrayList<QuestionWS> fields;
    }

    class TypeWS
    {
        public int id;
        public String description;
        public int sortOrder;
    }

    class Template
    {
        public int id;
        public String name;
    }

    class QuestionWS
    {
        public int id;
        public Template template;
        public Field field;
        public Boolean isRequired;
        public int group;
        public String note;
        public String usageCode;
    }

    class Field
    {
        public int id;
        public String type;
        public String prompt;
        public String instructions;
        public String options[];
        public int radioResponse;
        public String commentResponse;
    }

    class SafetyAuditForm
    {
        public int id;
        public FieldWorkerLink rwic;
        public String date;

        // Form Template fields
        public String title; // i.e. "Daily Work Report"
        public String instructions;// Form-level instructions. i.e. "Please fill out this form."

        // filled out form fields
        public int filledOutFormId;
        public FormTemplateLink template;  // necessary for persisting new records
        public FilledOutFormStatus status;
        //public DateTimeOffset? SubmittedOn;
        public String reviewNotes;
        //public DateTimeOffset? ReviewedOn;
        public FieldWorkerLink reviewer;
        // questions/answers
        public FormFieldResponse[] fields;
    }

    class FieldWorkerLink
    {
        public int id;
        public String name;
        public String firstName;
        public String lastName;
        public String employeeCode;
        public String jobTitle;
    }

    class FormTemplateLink
    {
        public int id;
        public String name;
    }

    class FilledOutFormStatus
    {
        public int id;
        public String description;
        public int sortOrder;
    }

    class FormFieldResponse
    {
        public int id;
        public boolean isNotApplicable;
        public String response;
        public int formFieldPlacementId;
        public boolean isRequired;
        public int group;
        public String prompt;
        public String instructions; // Some forms have simple titles, others use more detailed instructions such as "TRASH: Is work site free of trash and debris?". Some have both.
        public String type;
    }

    enum FormFieldType{
        QUESTION_TYPE_UserInput,
        QUESTION_TYPE_PickOne,
        QUESTION_TYPE_PickMany,
        QUESTION_TYPE_FormHeader,
        QUESTION_TYPE_Audit,
        QUESTION_TYPE_Date,
        QUESTION_TYPE_Time,
        QUESTION_TYPE_DateTime,
        QUESTION_TYPE_Integer,
        QUESTION_TYPE_Decimal2,
        QUESTION_TYPE_YesNo,
        QUESTION_TYPE_Signature,
        QUESTION_TYPE_Photo,
        QUESTION_TYPE_DB_JobNumber_readonly,
        QUESTION_TYPE_DB_RailroadCode_readonly,
        QUESTION_TYPE_DB_RwicName_readonly,
        QUESTION_TYPE_DB_RwicEmployeeCode_readonly,
        QUESTION_TYPE_DB_CustomerCompanyName_readonly,
        QUESTION_TYPE_DB_PointOfContactName_editable,
        QUESTION_TYPE_DB_PointOfContactPhone_editable,
        QUESTION_TYPE_DB_JobDivision_editable,
        QUESTION_TYPE_DB_JobSubdivision_editable,
        QUESTION_TYPE_DB_JobStartDate_editable,
        QUESTION_TYPE_DB_JobDuration_editable,
        QUESTION_TYPE_DB_JobDescription_editable,
        QUESTION_TYPE_DB_JobMilePost_editable
    }

    class Jobs {
        public int resultCount;
        public int pageNumber;
        public ArrayList<Results> results;
    }

    class Results {
        public int id;
        public String number;
        public String description;
        public String status;
        public Customer customer;
        public RailRoad railroad;
        public String start;
        public String end;
    }

    class Customer {
        public int id;
        public String name;
    }

    class RailRoad {
        public int id;
        public String code;
        public String name;
    }

    class JobsRequest {
        public String railroidId;
        //public String[] jobStatuses;  // Turns out most jobs are "Approved" forever, so no point using this.
    }

    class ListRequest {
        public int fieldWorkerId;
        public String start;
        public String end;
    }

    class JobSetupShortResponse {
        public int resultCount;
        public int pageNumber;
        public List<JobSetupResponseResult> results;
    }

    class JobSetupResponseResult {
        public int id;
        public FieldWorkerLink rwic;
        public JobHeader job;
        public String date;
        public TypeWS status;
        public String submittedOn;
        public String reviewedOn;
    }


    class DwrShortResponse {
        public int resultCount;
        public int pageNumber;
        public List<DwrShortResponseResult> results;
    }

    class DwrShortResponseResult {
        public int id;
        public Customer source;
        public JobHeader job;
        public String date;
        public TypeWS status; // This status
        public String submittedOn;
        public String reviewedOn; // This reviewed
    }

    class DwrRequest {
        public int id;
        public String date;                 // Date
        public String typeOfDay;            // It is a string here and int in the response
        public String railroadSignature;    // Base64 encoded image
        public String railroadSignatureDate;// Date
        public String rwicSignature;        // Base64 encoded image
        public String rwicSignatureDate;    // Date
        public String clientSignature;      // Base64 encoded image
        public String clientSignatureDate;  // Date
        public String clientSignerName;
        public String clientSignerPhone;
        public String clientSignerEmail;
        public String nonBillableDayReason;
        public String submittedOn;          // Timestamp
        public String reviewNotes;
        public String reviewedOn;
        public boolean isLateCancellation;
        public FieldWorkerLink reviewer;
        public BaseIdWS serviceType;     // Flagging (0) or some other service like monitoring (1)
        public BaseIdWS source;             // Who is submitting
        public BaseIdWS job;                // Job work is for
        public BaseIdWS template;           // The DWR Template
        public BaseIdWS status;             // Default to zero
        public List<DwrAnswerWS> fields;
        public boolean isTrainer;
        public List<DwrCostCenter> costCenters;
        public boolean hasRoadwayFlagging;
    }

    class DwrResponse{
        public int id;
        public String date;                 // Date (if time is there, do not trust it)
        public String typeOfDay;               // It is an int here and a string in the request
        public String railroadSignature;    // Base64 encoded image no longer found here
        public String railroadSignatureDate;// Date (if time is there, do not trust it)
        public String rwicSignature;        // Base64 encoded image no longer found here
        public String rwicSignatureDate;    // Date (if time is there, do not trust it)
        public String clientSignature;      // Base64 encoded image no longer found here
        public String clientSignatureDate;  // Date (if time is there, do not trust it)
        public String clientSignerName;
        public String clientSignerPhone;
        public String clientSignerEmail;
        public String submittedOn;          // Timestamp
        public String reviewNotes;          // Important to the reader
        public String reviewedOn;           // Date
        public FieldWorkerLink reviewer;    // Probably a supervisor
        public BaseIdWS serviceType;
        public BaseIdWS source;             // Who is submitting
        public JobHeader job;               // Job work is for
        public BaseIdWS template;           // The DWR Template
        public BaseIdWS status;             // Default to zero
        public PropertyShortWS railroad;    // Property
        public List<DwrAnswerWS> fields;
        public boolean isTrainer;
        public String nonBillableDayReason; // Non Billable Day Reason
        public List<DwrCostCenter> costCenters;
        public boolean hasRoadwayFlagging;
    }

    class JobSetupResponse {
        public int id;
        public BaseIdWS job;
        public FieldWorkerLink rwic;
        public String date;
        public String reviewNotes;          // Important to the reader
        public String reviewedOn;           // Date
        public FieldWorkerLink reviewer;    // Probably a supervisor
        public BaseIdWS status;
        public FormFieldResponse[] fields;
    }

    // For updates, often only the id is of concern.
    class BaseIdWS{
        public int id;
    }

    class DwrCostCenter{
        public int id;
        public JobCostCenter jobCostCenter;
        public String startTime;
        public String endTime;
    }

    class JobCostCenter{
        public int id;
        public String code;
        public String description;
        public String milePostRange;
        public JobHeader job;
    }

    class DwrAnswerWS{
        public int id;
        public boolean isNotApplicable;
        public String response;
        public int formFieldPlacementId;
        public String usageCode;
    }

    /** Job Request **/
    class FormsRequest {
        public int id;
        public BaseIdWS job;                // Job work is for
        public FieldWorkerLink rwic;
        public String date;
        public String rwicSignature;
        public String rwicSignatureDate;
        public String title;
        public String instructions;
        public int filledOutFormId;
        public FormTemplateLink template;  // necessary for persisting new records
        public FilledOutFormStatus status;
        public String submittedOn;
        public String reviewNotes;
        public String reviewedOn;
        //public DateTimeOffset? ReviewedOn;
        public FieldWorkerLink reviewer;
        // questions/answers
        public FormFieldResponse[] fields;
    }

    class JobHeader {
        public int id;
        public int number;
        public String description;
    }

    class FlashAuditRequest {
        public int id;
        public FieldWorkerWS rwic;
        public String timestamp;
        public FieldWorkerWS reviewer;
        public String reviewNotes;
        public String reviewedOn;
        public String status;
        public FlashAuditImages[] images;
    }

    class FlashAuditImages {
        public int id;
        public String timestamp;
        public float latitude;
        public float longitude;
        public String data;
        public String description;
    }

    class RailRoadRequest {
        String customerId;
        String searchText;
        Object[] sortCriteria;
        int pageSize;
        int pageNumber;
    }

    class RailRoadResponse {
        public RailRoadItem[] results;
    }

    class RailRoadItem {
        public long id;
        public String companyName;
        public String code;
        public Object publicDwrStyle;
        public double nonflaggingHourlyRate;
        public long dailyCellPhoneReimbursementRate;
        public long hotelPerDiem;
        public long mealPerDiem;
        public boolean mealBonusApplies;
        public boolean mileageReimbursed;
        public boolean travelTimeApplies;
        public Long hourlyVehicleFee;
        public String divisionLabel;
        public String employerIdentificationNumber;
        public Object[] contacts;
        public Division[] divisions;
        public String billingEmail;
    }

    class Division {
        public long id;
        public String name;
        public String status;
        public boolean isAssignedToAJob;
        public Division[] subdivisions;
    }

    public class OfferedService {
        public int id;
        public String name;
        public BaseIdWS type;
    }

}
