package com.railprosfs.railsapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;

import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data_layout.AnswerDao;
import com.railprosfs.railsapp.data_layout.AnswerTbl;
import com.railprosfs.railsapp.data_layout.AssignmentDao;
import com.railprosfs.railsapp.data_layout.FieldPlacementDao;
import com.railprosfs.railsapp.data_layout.FieldPlacementTbl;
import com.railprosfs.railsapp.data_layout.JobSetupDao;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.service.WebServiceModels;
import com.railprosfs.railsapp.ui_support.JobSetupAdapter;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.KTime;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.railprosfs.railsapp.utility.Constants.SIGNATURE_FILE_PATH;


public class JobSetupTemplateMgr {
    private Context ctx;
    private JobSetupTbl jsTbl;
    private ScheduleDB db;
    private Actor user;

    public JobSetupTemplateMgr(Context ctx, JobSetupTbl jstbl) {
        this.ctx = ctx;
        this.db = ScheduleDB.getDatabase(ctx);
        this.user = new Actor(ctx);
        this.jsTbl = jstbl;
    }

    public WebServiceModels.FormsRequest createRequest() throws ExpClass {
        //GetJobSetup
        JobSetupDao jsDao = db.jobSetupDao();
        AnswerDao aDao = db.answerDao();
        AssignmentDao assDao = db.assignmentDao();
        FieldPlacementDao fieldPlacementDao = db.fieldPlacementDao();

        //Get Set of Question Associated with Job
        List<AnswerTbl> listAnswer = aDao.GetAnswerTbl(jsTbl.Id, 1);

        if (listAnswer == null || listAnswer.size() == 0) {
            throw new ExpClass(ExpClass.GENERAL_EXP, this.getClass().getName() + ".createRequest", "No Answers", "");
        }

        // Check that we have some questions to work with.
        FieldPlacementTbl firstQuestion = fieldPlacementDao.GetFieldByFieldId(listAnswer.get(0).QuestionId);
        if(firstQuestion == null) {
            throw new ExpClass(ExpClass.GENERAL_EXP, this.getClass().getName() + ".createRequest", "No Questions", "");
        }

        WebServiceModels.FormsRequest rq = new WebServiceModels.FormsRequest();
        rq.id = jsTbl.JobSetupSvrId;
        try {
            rq.date = KTime.ParseToFormat(jsTbl.CreateDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID()).toString();
        } catch (ExpParseToCalendar ex) { rq.date = KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID()).toString(); }
        rq.submittedOn = KTime.ParseNow(KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();

        //Job
        rq.job = new WebServiceModels.BaseIdWS();
        rq.job.id = jsTbl.JobId;

        //RWIC
        rq.rwic = new WebServiceModels.FieldWorkerLink();
        rq.rwic.id = user.workId;

        //Template
        rq.template = new WebServiceModels.FormTemplateLink();
        rq.template.id = firstQuestion.TemplateId;

        //Status
        rq.status = new WebServiceModels.FilledOutFormStatus();
        rq.status.id = jsTbl.Status;
        rq.status.sortOrder = 0;

        //ReviewerStatus
        rq.reviewedOn = (jsTbl.ReviewerOn != null) ? jsTbl.ReviewerOn : "";
        rq.reviewNotes = (jsTbl.ReviewerNotes != null) ? jsTbl.ReviewerNotes : "";

        //Fields

        List<WebServiceModels.FormFieldResponse> fields = new ArrayList<>();

        /*
        * Loops through each Answer and gets associated ID
        * Finds Question Matching Question ID and Populates Fields
        * If Negative Associated ID then this means they belong to the Header and is a Custom Built UI Item in the Table
        * */
        for (int i = 0; i < listAnswer.size(); i++) {
            AnswerTbl answer = listAnswer.get(i);
            FieldPlacementTbl question = db.fieldPlacementDao().GetFieldByFieldId(answer.QuestionId);

            //Couldn't Find Question Move On
            if (question == null) {
                // Special Case if Found Question
                if (answer.QuestionId == -1) {
                    setHeader(rq, answer);
                } else if (answer.QuestionId == -2) {
                    try {
                        String date = KTime.ParseToFormat(answer.date, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339fk, TimeZone.getDefault().getID()).toString();
                        rq.rwicSignatureDate = date;
                    } catch (Exception e) {
                        // Failed to Save Date
                    }
                } else if (answer.QuestionId == -3) {
                    //JobNumber Ignore
                }
            } else {
                if(getField(answer, question) != null) {
                    fields.add(getField(answer, question));
                    //rq.fields[i - offset] = getField(answer, question);
                }
            }
        }

        rq.fields = new WebServiceModels.FormFieldResponse[fields.size()];
        rq.fields = fields.toArray(rq.fields);

        return rq;
    }

    // Apply the backend database record ids to the local answers.
    public void UpdateAnswerKeys(WebServiceModels.FormFieldResponse[] fields) {

        AnswerDao aDao = db.answerDao();
        List<AnswerTbl> listAnswers = aDao.GetAnswerTbl(jsTbl.Id, 1);

        for (AnswerTbl answer : listAnswers) {
            for (WebServiceModels.FormFieldResponse field : fields) {
                if (answer.QuestionId == field.formFieldPlacementId) {
                    answer.ServerId = field.id;
                    db.answerDao().Update(answer);
                }
            }
        }
    }

    //Gets Basic Field Information Associated with Question
    private WebServiceModels.FormFieldResponse getField(AnswerTbl answer, FieldPlacementTbl question) {
        WebServiceModels.FormFieldResponse temp = new WebServiceModels.FormFieldResponse();
        temp.id = answer.ServerId;
        temp.isNotApplicable = false;
        // TODO: This is the place. Check for FieldType and set the response accordingly
        if(question.FieldType.equals(JobSetupAdapter.DATE)) {
            temp.response = answer.date;
        } else if(question.FieldType.equals(JobSetupAdapter.CHECKBOX)) {
            temp.response = Boolean.toString(answer.yesNo);
        } else {
            temp.response = answer.CommentResponse;
        }

        temp.formFieldPlacementId = question.FieldId;
        temp.isRequired = question.Required;
        temp.group = question.Group;
        temp.prompt = question.FieldPrompt;
        temp.instructions = question.FieldInstructions;
        temp.type = question.FieldType;

        // Handle Date Picker
        if (temp.type.toUpperCase().equals("DATE") || temp.type.equals("DB_JobStartDate_editable")) {
            temp.response = answer.date;
        }

        return temp;
    }

    //Gets Basic Field Information Associated with Question
    private WebServiceModels.FormFieldResponse getField(FieldPlacementTbl question) {
        WebServiceModels.FormFieldResponse temp = new WebServiceModels.FormFieldResponse();
        temp.id = 0;
        temp.isNotApplicable = false;
        temp.formFieldPlacementId = question.FieldId;
        temp.isRequired = question.Required;
        temp.group = question.Group;
        temp.prompt = question.FieldPrompt;
        temp.instructions = question.FieldInstructions;
        temp.type = question.FieldType;

        return temp;
    }

    // Checks To determine which Header Field to put Field Information to
    private void setHeader(WebServiceModels.FormsRequest rq, AnswerTbl answer) {
        if (answer.signatureFileName != null) {
            setHeaderImage(rq, answer);
        }
    }

    // Uploads Image from Field to Header Field
    private void setHeaderImage(WebServiceModels.FormsRequest rq, AnswerTbl answer) {
        rq.rwicSignature = EncodeImageBase64(answer.signatureFileName);
        rq.rwicSignatureDate = answer.CommentResponse;
    }

    // Since these files we need to encode are images, easiest to just use the bitmap class.
    private String EncodeImageBase64(String filename) {
        if (filename.length() == 0) {
            return "";
        }
        filename = Environment.getExternalStorageDirectory().toString() + SIGNATURE_FILE_PATH + "/" + filename;

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(filename);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bStream);
        byte[] imageBytes = bStream.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
