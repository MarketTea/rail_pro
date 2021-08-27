package com.railprosfs.railsapp.data_layout;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface JobSetupDao {
    @Insert
    long Insert(JobSetupTbl field);

    @Update
    void Update(JobSetupTbl field);

    @Delete
    void Delete(JobSetupTbl field);

    @Query("Delete from JobSetupTbl where userid != :user")
    void ClearNonUsers(String user);

    @Query("Select * from JobSetupTbl where Id == :jobSetupId")
    LiveData<JobSetupTbl> GetJobSetupData(int jobSetupId);

    @Query("Select * from JobSetupTbl where Id == :jobSetupId")
    JobSetupTbl GetJobSetup(int jobSetupId);

    @Query("Select * from JobSetupTbl where userid == :user AND createdate >= :range Order By createdate desc")
    LiveData<List<JobSetupTbl>> GetJSetups(String user, String range);

    @Query("Select * from JobSetupTbl where jobid == :job  Order By createdate desc")
    LiveData<List<JobSetupTbl>> GetJobSetupByJobId(int job);

    @Query("Select * from JobSetupTbl where AssignmentId == :type")
    List<JobSetupTbl> GetAllJobSetupTbl(int type);
}
