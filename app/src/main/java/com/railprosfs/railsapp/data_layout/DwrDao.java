package com.railprosfs.railsapp.data_layout;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DwrDao {

    @Insert
    long Insert(DwrTbl dwr);

    @Update
    void Update(DwrTbl dwr);

    @Delete
    void Delete(DwrTbl dwr);

    @Query("Delete from DwrTbl where userid != :user")
    void ClearNonUsers(String user);

    @Query("Select * from DwrTbl")
    List<DwrTbl> GetAllDwr();

    @Query("Select * from DwrTbl where jobid == :job and status > 2")
    List<DwrTbl> GetJobDwrs(int job);

    @Query("Select * from DwrTbl where jobid == :job Order By workdate desc")
    LiveData<List<DwrTbl>> GetProjectDwrs(int job);

    @Query("Select * from DwrTbl where dwrId == :dwrId ")
    DwrTbl GetDwr(int dwrId);

    @Query("Select * from DwrTbl where DwrSrvrId == :serverId ")
    DwrTbl GetDwrWithServerId(int serverId);

    @Query("Select * from DwrTbl where dwrId == :dwrId")
    LiveData<DwrTbl> GetReport(int dwrId);

    @Query("Select * from DwrTbl where userid == :user AND workdate >= :range Order By workdate desc")
    LiveData<List<DwrTbl>> GetReports(String user, String range);

    @Query("Select * from DwrTbl where Status > 1 and PerDiem > 0")
    LiveData<List<DwrTbl>> GetPerDiemDwrs();
}
