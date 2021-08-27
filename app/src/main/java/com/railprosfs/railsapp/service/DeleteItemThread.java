package com.railprosfs.railsapp.service;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.railprosfs.railsapp.data.ScheduleDB;
import com.railprosfs.railsapp.data_layout.AnswerTbl;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.Functions;

import java.io.File;
import java.util.List;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  The DeleteItemThread is used to delete various items local database.
 *  There is currently no desire to allow the mobile app to delete data
 *  on the backend.  This is currently used only to clean up data living
 *  in the local database.
 */
public class DeleteItemThread extends Thread {

    private Context context;
    private Messenger source;
    private ScheduleDB db;
    private int type;
    private int id;


    public DeleteItemThread(Context ctx, int formType, int formId, Messenger callback){
        context = ctx;
        source = callback;
        db = ScheduleDB.getDatabase(ctx);
        type = formType;
        id = formId;
    }

    @Override
    public void run() {
        int what = 0;
        try {
            switch(type){
                case FORM_DWR_TYPE:
                    DeleteDwr();
                    what = WHAT_DWR_DELETE;
                    break;
                case FORM_JSF_TYPE:
                    DeleteJsf();
                    what = WHAT_JSF_DELETE;
                    break;
            }
            ReturnOk(what);
        }
        catch (Exception ex) {
            ReturnErr();
        }
    }

    private void DeleteDwr(){
        List<DocumentTbl> docs = db.documentDao().GetDocumentsByDwr(id);
        if(docs != null && docs.size() > 0){
            for (DocumentTbl item:docs) {
                RemoveDocument(item.DocumentType, item.FileName);
                db.documentDao().Delete(item);
            }
        }
        DwrTbl dwr = db.dwrDao().GetDwr(id);
        if(dwr != null) {
            if(dwr.ClientSignatureName != null && dwr.ClientSignatureName.length() > 0){
                RemoveDocument(DOC_SIGNATURES, dwr.ClientSignatureName);
            }
            if(dwr.RwicSignatureName != null && dwr.RwicSignatureName.length() > 0){
                RemoveDocument(DOC_SIGNATURES, dwr.RwicSignatureName);
            }
            db.dwrDao().Delete(dwr);
        }
    }

    private void DeleteJsf(){
        List<DocumentTbl> docs = db.documentDao().GetDocumentsByJsf(id);
        if(docs != null && docs.size() > 0){
            for (DocumentTbl item:docs) {
                if(item.FileName.length() > 0) {
                    RemoveDocument(item.DocumentType, item.FileName);
                }
                db.documentDao().Delete(item);
            }
        }
        JobSetupTbl jsf = db.jobSetupDao().GetJobSetup(id);
        if(jsf != null) {
            List<AnswerTbl> signatures = db.answerDao().GetAnswerByQuestion(jsf.Id, FIELD_JSF_SIGN_RWIC);
            for (AnswerTbl item:signatures) {
                if(item.signatureFileName != null && item.signatureFileName.length() > 0) {
                    RemoveDocument(DOC_SIGNATURES, item.signatureFileName);
                }
            }
            db.answerDao().ClearJobSetupAnswers(jsf.Id, FIELD_JSF_TYPE);
            db.jobSetupDao().Delete(jsf);
        }
    }

    private boolean RemoveDocument(int documentType, String filename){
        try {
            File file = new File(Functions.GetStorageName(documentType, filename, context));
            return file.delete();
        } catch (Exception ex) {
            ExpClass.LogEX(ex, filename + " Doc Type = " + documentType);
            return false;
        }
    }

    private void ReturnOk(int kind) {
        try {
            Message msg = Message.obtain();
            msg.what = kind;
            if(source != null) {
                source.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }

    private void ReturnErr() {
        try {
            Message msg = Message.obtain();
            switch (type){
                case FORM_DWR_TYPE:
                    msg.what = WHAT_DWR_DELETE_ERR;
                    break;
                case FORM_JSF_TYPE:
                    msg.what = WHAT_JSF_DELETE_ERR;
                    break;
            }
            if(source != null) {
                source.send(msg);
            }
        } catch (RemoteException ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".run");
        }
    }


}
