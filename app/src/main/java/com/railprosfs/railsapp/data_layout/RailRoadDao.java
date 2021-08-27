package com.railprosfs.railsapp.data_layout;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RailRoadDao {
    @Insert
    long Insert(RailRoadTbl field);

    @Update
    void Update(RailRoadTbl field);

    @Delete
    void Delete(RailRoadTbl field);

    @Query("Select * from RailRoadTbl")
    List<RailRoadTbl> GetAll();

    @Query("Select * from RailRoadTbl where code == :railroadName")
    List<RailRoadTbl> GetAll(String railroadName);

    @Query("select * from railroadtbl where code == :railroadName and subdivisionName not null order by subdivisionname;")
    LiveData<List<RailRoadTbl>> GetDivisionForRailRoad(String railroadName);

    @Query("select subdivisionName from railroadtbl where code == :railroadName and subdivisionName not null order by subdivisionname;")
    LiveData<List<String>> GetSubdivisionByRailRoad(String railroadName);
}
