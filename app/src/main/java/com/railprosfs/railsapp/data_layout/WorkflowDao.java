package com.railprosfs.railsapp.data_layout;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface WorkflowDao {

    @Insert
    long Insert(WorkflowTbl wfTbl);

    @Update
    void Update(WorkflowTbl wfTbl);

    @Delete
    void Delete(WorkflowTbl wfTbl);

    @Query("Select * from WorkflowTbl where EventType == :eventType")
    List<WorkflowTbl> GetWorkflowTblByEventType(int eventType);

    @Query("Select * from WorkflowTbl where EventID == :eventID")
    LiveData<WorkflowTbl> GetWorkflowTblByEventID(int eventID);

    @Query("Select * from WorkflowTbl ORDER BY Priority")
    List<WorkflowTbl> GetWorkflowTblByPriorityEventType();

    @Query("Select * from WorkflowTbl")
    LiveData<List<WorkflowTbl>> GetAllWorkflowTbl();

    //Gets Workflow Table from it's primary key
    @Query("Select * from WorkflowTbl where EventID == :eventID")
    WorkflowTbl GetWorkflowTblPrimaryKey(int eventID);

    //Gets the Unique Workflow table associated to EventKey
    @Query("Select * from WorkflowTbl where EventKey == :eventKey")
    WorkflowTbl GetWorkflowTblUniqueKey(int eventKey);

    //Gets the Unique Workflow table associated to EventKey AND EventType
    @Query("Select * from WorkflowTbl where EventKey == :eventKey AND EventType == :eventType")
    WorkflowTbl GetWorkflowTblUniqueKeyAndType(int eventKey, int eventType);

    //Get the old record off the stack. Make sure "now" is the same format as was saved in the db.
    @Query("Select * from WorkflowTbl where Pending <= :now order by Pending asc limit 1")
    WorkflowTbl Pop(String now);

    //Hides a record by pushing it into the future
    @Query("Update WorkflowTbl set Pending = :future, Retry = Retry+1, Uploading=1 where EventID == :eventId")
    void Push(int eventId, String future);
}
