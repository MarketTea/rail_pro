package com.railprosfs.railsapp.data_layout;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class JobSetupTbl {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int Id;                  // The local database id
    @NonNull
    public int AssignmentId;        // This holds the Form Type: RP_FLAGGING_SERVICE, RP_UTILITY_SERVICE, RP_COVER_SERVICE
    public int JobSetupSvrId;       // The backend database id.
    public int JobId;               // The backend database key of the job this job setup is for.
    public int RailRoadId;          // The local property key of the railroad.
    public String CreateDate;       // When the Job Setup was first created.
    public int Status;              // Same as the DWR status values. (see DWR_STATUS_NEW)
    public String StatusMessage;
    public String UserId;           // The Actor.unique (email address of the RWIC user).
    // Reviewer Information from backend.
    public String ReviewerNotes;
    public String ReviewerOn;
    public String ReviewerName;
    public int ReviewerId;
}
