package com.railprosfs.railsapp.data_layout;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RailRoadTbl {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;
    public int railroadId;
    public String companyName;
    public String code;
    public String divisionName;
    public int divisionId;
    public String subdivisionName;
    public int subdivisionId;

}
