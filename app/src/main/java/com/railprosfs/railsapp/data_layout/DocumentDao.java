package com.railprosfs.railsapp.data_layout;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DocumentDao {
    @Insert
    void Insert(DocumentTbl doc);

    @Update
    void Update(DocumentTbl doc);

    @Delete
    void Delete(DocumentTbl doc);

    @Query("Delete from DocumentTbl where DwrId == :dwrId and DocumentType == 1")
    void DeleteDwrImages(int dwrId);

    @Query("Delete from DocumentTbl where DwrId == :dwrId and DocumentType == 4")
    void DeleteDwrSigninSheet(int dwrId);

    @Query("Delete from DocumentTbl where jobid == :job")
    void ClearJobDocs(int job);

    @Query("Select * from DocumentTbl where jobid == :job")
    List<DocumentTbl> GetJobDocs(int job);

    @Query("Select * from DocumentTbl where documenttype == :type")
    List<DocumentTbl> GetDocsByType(int type);

    @Query("Select * from DocumentTbl where DwrId == :dwrId and DocumentType == 1")
    List<DocumentTbl> GetDwrImages(int dwrId);

    @Query("Select * from DocumentTbl where DwrId == :dwrId and DocumentType == 4")
    List<DocumentTbl> GetDwrDailyImage(int dwrId);

    @Query("Select * from DocumentTbl where DwrId == :dwrId")
    LiveData<List<DocumentTbl>> GetAllDwrImages(int dwrId);

    @Query("Select * from DocumentTbl where jobid == :job")
    LiveData<List<DocumentTbl>> GetDocsByJob(int job);

    @Query("Select * from DocumentTbl where DwrId == :dwrId")
    List<DocumentTbl> GetDocumentsByDwr(int dwrId);

    @Query("Select * from DocumentTbl where JobId == :jsfId")
    List<DocumentTbl> GetDocumentsByJsf(int jsfId);
}
