package com.railprosfs.railsapp.data_layout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AssignmentTbl {

    // Information specific to the App
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int AssignmentId;        // Local DB Key.
    public int TimeLine;            // Helpful value when displaying data, not part of RP data.
    @NonNull
    public String UserId;           // Friendly user identifier.

    // Assignment specific data
    @NonNull
    public int ShiftId;             // Backend key for an assignment.
    @NonNull
    public String ShiftDate;        // The day the shift takes place.
    public String StartDate;        // Probably the date of the earliest shift.
    public String EndDate;          // Probably the date of the last shift.
    public String ShiftNotes;       // Notes about the shift.
    public boolean JobSetup;        // True flags assignment as needing a job setup form.

    @NonNull
    public String FieldContactName; // fieldContact Name
    @NonNull
    public String FieldContactPhone; // fieldContact Phone
    @NonNull
    public String FieldContactEmail; // fieldContact Email

    // Job Specific data
    @NonNull
    public int JobId;               // Backend DB Key of Job.  Used as input to Api to get more data.
    public int RailroadId;          // Index into local railroad collection.
    public String JobStartDate;     // Start date of overall job.
    public String JobEndDate;       // End date of overall job
    public String JobDescription;   // Description of the job.
    public String CustomerName;     // Customer requesting Job.
    public String CustomerPhone;
    public String CustomerEmail;
    public String LocationName;     // A human readable location.
    public String LocationLink;     // Job location URL.
    public String Subdivision;      // Subdivision name of where the Job takes place.
    public String MilePostStart;    // Starting mile post of the Job.
    public String SupervisorRP;     // Used to indicate if this is a "Supervisor" job.
    public String JobNumber;        // Display Number for Job
    public String Notes;
    public String EquipmentDescription;
    public String DistanceFromTracks;
    public String PermitNumber;
    public String TrackSupervisor;
    public String Restrictions;     // Restrictions on Job (can be more than one).
    public int ServiceType;         // The suggested service type for the shift (Flagging = 0, Utility = 1, ???)
    public String CostCenters;      // Hold optional cost centers that could be used


    @Override
    public boolean equals(@Nullable Object input) {
        if (input == this) { return true; }
        if (input == null) { return false; }
        if (!(input instanceof AssignmentTbl)) { return false; }
        AssignmentTbl other = (AssignmentTbl) input;
        return (SafeCheckEqual(this.ShiftDate, other.ShiftDate) &&
                SafeCheckEqual(this.StartDate, other.StartDate) &&
                SafeCheckEqual(this.EndDate, other.EndDate) &&
                SafeCheckEqual(this.ShiftNotes, other.ShiftNotes) &&
                this.JobSetup == other.JobSetup &&
                SafeCheckEqual(this.FieldContactName, other.FieldContactName) &&
                SafeCheckEqual(this.FieldContactPhone, other.FieldContactPhone) &&
                SafeCheckEqual(this.FieldContactEmail, other.FieldContactEmail) &&
                this.ShiftId == other.ShiftId &&
                this.JobId == other.JobId &&
                this.RailroadId == other.RailroadId &&
                this.ServiceType == other.ServiceType &&
                SafeCheckEqual(this.JobStartDate, other.JobStartDate) &&
                SafeCheckEqual(this.JobEndDate, other.JobEndDate) &&
                SafeCheckEqual(this.JobDescription, other.JobDescription) &&
                SafeCheckEqual(this.CustomerName, other.CustomerName) &&
                SafeCheckEqual(this.LocationName, other.LocationName) &&
                SafeCheckEqual(this.LocationLink, other.LocationLink) &&
                SafeCheckEqual(this.Subdivision, other.Subdivision) &&
                SafeCheckEqual(this.MilePostStart, other.MilePostStart) &&
                SafeCheckEqual(this.SupervisorRP, other.SupervisorRP) &&
                SafeCheckEqual(this.JobNumber, other.JobNumber) &&
                SafeCheckEqual(this.Notes, other.Notes) &&
                SafeCheckEqual(this.EquipmentDescription, other.EquipmentDescription) &&
                SafeCheckEqual(this.DistanceFromTracks, other.DistanceFromTracks) &&
                SafeCheckEqual(this.PermitNumber, other.PermitNumber) &&
                SafeCheckEqual(this.TrackSupervisor, other.TrackSupervisor) &&
                SafeCheckEqual(this.Restrictions, other.Restrictions) &&
                SafeCheckEqual(this.CostCenters, other.CostCenters)
        );
    }

    private boolean SafeCheckEqual(String s1, String s2) {
        if((s1 == null && s2 == null) || (s1 != null && s2 != null)) {
            return ((s1 == null) || s1.equalsIgnoreCase(s2));
        } else {
            return false;
        }
    }
}

