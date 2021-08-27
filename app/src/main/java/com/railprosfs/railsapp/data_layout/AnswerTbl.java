package com.railprosfs.railsapp.data_layout;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AnswerTbl {

    @NonNull
    @PrimaryKey (autoGenerate = true)
    public int Id;
    public int ServerId;
    public int FieldId;
    public int FieldType;
    public int QuestionId;
    public int RadioResponse;
    public String CommentResponse;
    public String QuestionText;
    public boolean yesNo;
    public String date;
    public String signatureFileName; //File Name
}
