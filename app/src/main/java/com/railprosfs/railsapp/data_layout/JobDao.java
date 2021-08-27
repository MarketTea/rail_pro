package com.railprosfs.railsapp.data_layout;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface JobDao {
    @Insert
    void Insert(JobTbl field);

    @Update
    void Update(JobTbl field);

    @Update
    void Delete(JobTbl field);

    @Query("Delete from JobTbl")
    void DeleteAll();

    @Query("Select * from JobTbl where Id == :jobsId")
    JobTbl GetJob(int jobsId);

    @Query("Select * from JobTbl where EndTime > :range")
    LiveData<List<JobTbl>> GetJobsList(String range);

    @Query("Select * from JobTbl where Id == :jobsId")
    LiveData<JobTbl> GetJobDetail(int jobsId);
}
