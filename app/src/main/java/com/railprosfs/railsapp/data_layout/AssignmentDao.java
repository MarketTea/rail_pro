package com.railprosfs.railsapp.data_layout;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AssignmentDao {

    @Insert
    long Insert(AssignmentTbl job);

    @Update
    void Update(AssignmentTbl job);

    @Delete
    void Delete(AssignmentTbl job);

    @Query("Delete from AssignmentTbl where userid != :user")
    void ClearNonUsers(String user);

    @Query("Select * from AssignmentTbl where JobId == :jobId order by ShiftDate desc;")
    LiveData<AssignmentTbl> GetAssignmentByJobId(int jobId);


    @Query("Select * from AssignmentTbl where jobid == :jobId")
    AssignmentTbl GetByJobId(int jobId);

    @Query("Select count(*) from AssignmentTbl join DwrTbl on AssignmentTbl.jobid == DwrTbl.jobid where AssignmentTbl.jobid == :jobId and workdate > :range")
    int GetDwrCountOnAssignment(int jobId, String range);

    @Query("Select count(*) from AssignmentTbl join JobSetupTbl on AssignmentTbl.jobid == JobSetupTbl.jobid where AssignmentTbl.jobid == :jobId and createdate > :range")
    int GetJsfCountOnAssignment(int jobId, String range);

    @Query("Select * from AssignmentTbl where AssignmentId == :AssignmentId")
    AssignmentTbl GetAssignment(int AssignmentId);


    @Query("Select * from AssignmentTbl where userid == :user")
    List<AssignmentTbl> GetUserAssignments(String user);


    @Query("Select * from AssignmentTbl where AssignmentId == :assignmentId")
    LiveData<AssignmentTbl> GetLiveDataAssignmentByAssignmentId(int assignmentId);


    // The shift date is the day the work is expected to be done.
    @Query("Select * from AssignmentTbl where userid == :user Order By shiftdate")
    LiveData<List<AssignmentTbl>> GetSchedule(String user);
}
