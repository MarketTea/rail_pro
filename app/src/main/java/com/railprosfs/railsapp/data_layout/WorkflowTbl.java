package com.railprosfs.railsapp.data_layout;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class WorkflowTbl {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int EventID;             // Unique ID used to access this record
    public String Pending;          // Time stamp used for pending of events
    public int Priority;            // 0 = High Priority, 5 = Normal Priority, 10 = Low Priority
    public int EventType;           // The type of record that needs to be processed
    @NonNull
    public int EventKey;            // The DB key where the data can be found.
    public int Retry;               // How may times this record attempted processing
    public int RetryMax;            // Defines when a record is "poision"
    public String CreateDate;       // Create date of the record
    public int Uploading;           // Upload Status 0 = Not In Currently Uploading, 1 = Uploading

    public WorkflowTbl() {
        this.EventID = 0;
        this.EventKey = 0;
        this.Retry = 0;
    }
}
