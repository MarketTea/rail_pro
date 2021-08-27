package com.railprosfs.railsapp.data_layout;



import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AnswerDao {

    @Insert
    void Insert(AnswerTbl answer);

    @Update
    void Update(AnswerTbl answer);

    @Delete
    void Delete(AnswerTbl answer);

    @Query("Delete from AnswerTbl where FieldId == :fieldId and FieldType == :fieldType")
    void ClearJobSetupAnswers(int fieldId, int fieldType);

    @Query("Select * from AnswerTbl where FieldId == :fieldId and FieldType == :fieldType")
    List<AnswerTbl> GetAnswerTbl(int fieldId, int fieldType);

    @Query("Select * from AnswerTbl where FieldId == :fieldId and FieldType == :fieldType")
    LiveData<List<AnswerTbl>> GetLiveDataAnswerTbl(int fieldId, int fieldType);

    @Query("Select * from AnswerTbl where FieldId == :fieldId and FieldType == :fieldType and QuestionId == :questionId")
    AnswerTbl GetAnswer(int fieldId, int fieldType, int questionId);

    @Query("Select * from AnswerTbl where FieldId == :fieldId and QuestionId == :questionId")
    List<AnswerTbl> GetAnswerByQuestion(int fieldId, int questionId);

}
