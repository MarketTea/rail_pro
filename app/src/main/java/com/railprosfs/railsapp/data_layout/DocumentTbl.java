package com.railprosfs.railsapp.data_layout;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DocumentTbl {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int DocumentId;
    @NonNull
    public int DocumentType;    // 0=Job Doc, 1=Dwr Picture, 2=FlashAuditModel Photo, 3=Signatures 4=DwrImageSignIn
    @NonNull
    public String FileName;
    public String description;
    public int Size;
    public String Mimetype;
    public String LastUpdate;
    public String UriServer;
    public String UriLocal;
    public int ServerId;
    public int JobId;
    public int DwrId;
}
