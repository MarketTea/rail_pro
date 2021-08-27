package com.railprosfs.railsapp.data_layout;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class JobTbl extends BaseObservable {
    @PrimaryKey
    @NonNull
    public int Id;
    public String JobNumber;
    public String Description;
    public String Status;
    public int CustomerId;
    public String CustomerName;
    public int RailRoadId;
    public String RailRoadCode;
    public String RailRoadName;
    public String StartTime;
    public String EndTime;

    @Bindable
    public String getJobNumber() {
        return JobNumber;
    }


    @Bindable
    public String getDescription() {
        if(Description == null) {
            return "";
        }
        return Description;
    }
}
