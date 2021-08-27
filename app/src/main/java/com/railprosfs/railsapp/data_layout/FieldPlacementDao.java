package com.railprosfs.railsapp.data_layout;


import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface FieldPlacementDao {

    @Insert
    void Insert(FieldPlacementTbl field);

    @Update
    void Update(FieldPlacementTbl field);

    @Delete
    void Delete(FieldPlacementTbl field);

    @Query("Delete from FieldPlacementTbl where TemplateId == :templateId")
    void DeleteAllId(int templateId);

    @Query("Select * from FieldPlacementTbl where Id == :id")
    FieldPlacementTbl GetField(int id);

    @Query("Select * from FieldPlacementTbl where FieldId == :fieldId")
    FieldPlacementTbl GetFieldByFieldId(int fieldId);

    @Query("Select * from FieldPlacementTbl where TemplateId == :templateId")
    List<FieldPlacementTbl> GetTemplateFields(int templateId);

    /*
        While all questions should exist in the backend, there are 3 that are inserted
        automatically and added to some forms (signature, signature date and job number).
        Since the App and backend are difficult to coordinate from a release perspective,
        we need to first add the questions and then change the code to expect the new
        questions.  Of course if we add the questions on the backend before changing the
        App code, we will see double questions.  To avoid this, the new questions will
        have a "Group" number of 25, which will allow them to be ignored.
     */
    @Query("Select * from FieldPlacementTbl where TemplateId == :templateId and `Group` != 25")
    LiveData<List<FieldPlacementTbl>> GetLiveTemplateFields(int templateId);
}