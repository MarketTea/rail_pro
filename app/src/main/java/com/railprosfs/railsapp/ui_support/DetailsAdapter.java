package com.railprosfs.railsapp.ui_support;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import static com.railprosfs.railsapp.utility.Constants.RP_FLAGGING_SERVICE;

/***
 *  This adaptor manages the data behind the Job Detail View.
 */
public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DwrTbl> mDwrData;
    private List<JobSetupTbl> mJsData;
    private List<DocumentTbl> mDocData;
    private int mServiceType;   // This is the way we distinguish the Flagging from Utility
    private final List<Pair<Integer, LocalKeys>> mFullData;
    private static final int VIEW_TYPE_JOB_SETUP = 1;
    private static final int VIEW_TYPE_DWR_START = 2;
    private static final int VIEW_TYPE_DWR_PAST = 3;
    private static final int VIEW_TYPE_DOCUMENT = 4;
    private static final int VIEW_TYPE_LABLE = 5;
    private static final int VIEW_TYPE_JS_PAST = 6;
    private static final int VIEW_TYPE_CS_START = 7;
    private final Context ctx;
    public DetailsAdapter(Context ctx){
        mFullData = new ArrayList<>();
        mDwrData = new ArrayList<>();
        mJsData = new ArrayList<>();
        mDocData = new ArrayList<>();
        ReorgData();
        this.ctx = ctx;
    }

    /* This will redraw the list on the screen after getting some new data. */
    public void RefreshDwrData(List<DwrTbl> rows, int serviceType) {
        mServiceType = serviceType;
        if(rows != null && rows.size() > 0)
            mDwrData = rows;
        else
            mDwrData = new ArrayList<>();

        ReorgData();
        notifyDataSetChanged();
    }
    /* This will redraw the list on the screen after getting some new data. */
    public void RefreshJsData(List<JobSetupTbl> rows, int serviceType) {
        mServiceType = serviceType;
        if(rows != null && rows.size() > 0)
            mJsData = rows;
        else
            mJsData = new ArrayList<>();

        ReorgData();
        notifyDataSetChanged();
    }
    /* This will redraw the list on the screen after getting some new data. */
    public void RefreshDocData(List<DocumentTbl> rows, int serviceType) {
        mServiceType = serviceType;
        if(rows != null && rows.size() > 0)
            mDocData = rows;
        else
            mDocData = new ArrayList<>();

        ReorgData();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFullData != null ? mFullData.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mFullData.get(position).first;
    }

    @Override
    public @NonNull
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_DOCUMENT:
                return new DocumentHolder(parent);
            case VIEW_TYPE_JS_PAST:
            case VIEW_TYPE_DWR_PAST:
                return new PastWorkHolder(parent);
            case VIEW_TYPE_DWR_START:
                return new DwrStartHolder(parent);
            case VIEW_TYPE_JOB_SETUP:
                return new JobSetupStartHolder(parent);
            case VIEW_TYPE_CS_START:
                return new CoverSheetStartHolder(parent);
            default:    // Should not see this type, but will not break if do.
                return new DelimiterHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int VIEW_TYPE_NOTHING = 0;

        // These types of views do not require any customization.
        if(holder.getItemViewType() == VIEW_TYPE_DWR_START ||
           holder.getItemViewType() == VIEW_TYPE_CS_START ||
           holder.getItemViewType() == VIEW_TYPE_NOTHING){

            return;
        }

        if(holder.getItemViewType() == VIEW_TYPE_JOB_SETUP){
            JobSetupStartHolder rowLabel = (JobSetupStartHolder) holder;
            if(mServiceType > RP_FLAGGING_SERVICE) { rowLabel.jobSetupLink.setText(R.string.start_job_utility); }
            rowLabel.jobSetupLink.setTag(mServiceType);
            return;
        }

        if(holder.getItemViewType() == VIEW_TYPE_LABLE){
            DelimiterHolder rowLabel = (DelimiterHolder) holder;
            rowLabel.timeTitle.setText(R.string.documents);
            return;
        }

        // List the JobSetups.
        if(holder.getItemViewType() == VIEW_TYPE_JS_PAST){
            // Normal item processing.
            PastWorkHolder rowPast = (PastWorkHolder) holder;
            JobSetupTbl dataJs = GetJsRow(mFullData.get(position).second.PrimeKey);
            if(dataJs != null){

                String holdFmt = ctx.getResources().getString(R.string.js_classification);
                String[] classificationNameList = ctx.getResources().getStringArray(R.array.job_setup_types);
                int jstype = dataJs.AssignmentId < classificationNameList.length ? dataJs.AssignmentId : 0;
                String jsLabelTitle = Arrays.asList(classificationNameList).get(jstype);
                String jsLabelText = String.format(holdFmt, jsLabelTitle);
                rowPast.hotspotJS.setText(jsLabelText);
                rowPast.hotspotJS.setTag(dataJs.Id);
                rowPast.hotspotDWR.setVisibility(View.GONE);
                try {
                    rowPast.workdate.setText(KTime.ParseToFormat(dataJs.CreateDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()));

                    if(Functions.GetDwrIcon(dataJs.Status) > 0)
                        rowPast.status.setImageResource(Functions.GetDwrIcon(dataJs.Status));
                } catch (ExpParseToCalendar expParseToCalendar) {
                    rowPast.workdate.setText(R.string.unknown);
                }
            }
            return;
        }

        // List the DWR.
        if(holder.getItemViewType() == VIEW_TYPE_DWR_PAST){
            // Normal item processing.
            PastWorkHolder rowPast = (PastWorkHolder) holder;
            DwrTbl dataDwr = GetDwrRow(mFullData.get(position).second.PrimeKey);
            if(dataDwr != null){

                String holdFmt = ctx.getResources().getString(R.string.dwr_classification);
                String[] classificationNameList = ctx.getResources().getStringArray(R.array.classification_name);
                String dwrLabelTitle = Arrays.asList(classificationNameList).get(dataDwr.Classification);
                String dwrLabelText = String.format(holdFmt, dwrLabelTitle);
                rowPast.hotspotDWR.setText(dwrLabelText);
                rowPast.hotspotDWR.setTag(dataDwr.DwrId);
                rowPast.hotspotJS.setVisibility(View.GONE);

                try {
                    rowPast.workdate.setText(KTime.ParseToFormat(dataDwr.WorkDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()));

                    if(Functions.GetDwrIcon(dataDwr.Status) > 0)
                        rowPast.status.setImageResource(Functions.GetDwrIcon(dataDwr.Status));
                } catch (ExpParseToCalendar expParseToCalendar) {
                    rowPast.workdate.setText(R.string.unknown);
                }
            }
            return;
        }

        // Fill out any document tiles, 3 at a time.
        if(holder.getItemViewType() == VIEW_TYPE_DOCUMENT) {
            // May want to adjust width of grid based on width of screen.
//            Point size = new Point();
//            getWindowManager().getDefaultDisplay().getSize(size);
//            int screenWidth = size.x;
//            int screenHeight = size.y;

            DocumentHolder rowDoc = (DocumentHolder) holder;
            DocumentTbl dataDoc = GetDocRow(mFullData.get(position).second.PrimeKey);
            if (dataDoc != null) {
                rowDoc.oneCard.setTag(getUriFromDocument(dataDoc));
                rowDoc.oneFilename.setText(dataDoc.description);
                try {
                    rowDoc.oneUpdateDate.setText(KTime.ParseToFormat(dataDoc.LastUpdate, KTime.KT_fmtDate3339fk, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()));
                } catch (ExpParseToCalendar expParseToCalendar) {
                    rowDoc.oneUpdateDate.setText(R.string.unknown);
                }
            } else {
                rowDoc.oneCard.setVisibility(View.INVISIBLE);
            }
            dataDoc = GetDocRow(mFullData.get(position).second.SecondKey);
            if (dataDoc != null) {
                rowDoc.twoCard.setTag(getUriFromDocument(dataDoc));
                rowDoc.twoFilename.setText(dataDoc.description);
                try {
                    rowDoc.twoUpdateDate.setText(KTime.ParseToFormat(dataDoc.LastUpdate, KTime.KT_fmtDate3339fk, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()));
                } catch (ExpParseToCalendar expParseToCalendar) {
                    rowDoc.twoUpdateDate.setText(R.string.unknown);
                }
            } else {
                rowDoc.twoCard.setVisibility(View.INVISIBLE);
            }
            dataDoc = GetDocRow(mFullData.get(position).second.ThirdKey);
            if (dataDoc != null) {
                rowDoc.treCard.setTag(getUriFromDocument(dataDoc));
                rowDoc.treFilename.setText(dataDoc.description);
                try {
                    rowDoc.treUpdateDate.setText(KTime.ParseToFormat(dataDoc.LastUpdate, KTime.KT_fmtDate3339fk, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()));
                } catch (ExpParseToCalendar expParseToCalendar) {
                    rowDoc.treUpdateDate.setText(R.string.unknown);
                }
            } else {
                rowDoc.treCard.setVisibility(View.INVISIBLE);
            }
        }
    }

    // This will populate the intersection of all the data displayed in the listview.
    // The first two items are fixed, the Job Setup & Dwr history lines up 1:1,
    // and the documents are grouped in 3's.
    private void ReorgData(){
        mFullData.clear();
        // these are fixed rows
        mFullData.add(new Pair<>(VIEW_TYPE_JOB_SETUP, new LocalKeys()));
        if(mServiceType > 0) { mFullData.add(new Pair<>(VIEW_TYPE_CS_START, new LocalKeys())); }
        mFullData.add(new Pair<>(VIEW_TYPE_DWR_START, new LocalKeys()));
        for (JobSetupTbl item : mJsData) {
            mFullData.add(new Pair<>(VIEW_TYPE_JS_PAST, new LocalKeys(item.Id)));
        }
        for (DwrTbl item : mDwrData) {
            mFullData.add(new Pair<>(VIEW_TYPE_DWR_PAST, new LocalKeys(item.DwrId)));
        }
        mFullData.add(new Pair<>(VIEW_TYPE_LABLE, new LocalKeys()));
        for (int i = 0; i < mDocData.size(); i += 3){
            LocalKeys holdKeys = new LocalKeys(mDocData.get(i).DocumentId);
            if(i+1 < mDocData.size()) holdKeys.SecondKey = mDocData.get(i+1).DocumentId;
            if(i+2 < mDocData.size()) holdKeys.ThirdKey = mDocData.get(i+2).DocumentId;
            mFullData.add(new Pair<>(VIEW_TYPE_DOCUMENT, holdKeys));
        }
    }

    // Quick lookup of the specific DWR.
    private DwrTbl GetDwrRow(int dwrid){
        for (DwrTbl item : mDwrData) {
            if(item.DwrId == dwrid) return item;
        }
        return null;
    }

    // Quick lookup of the specific JS.
    private JobSetupTbl GetJsRow(int jsid){
        for (JobSetupTbl item : mJsData) {
            if(item.Id == jsid) return item;
        }
        return null;
    }

    // Quick lookup of the specific Doc.
    private DocumentTbl GetDocRow(int docid){
        for (DocumentTbl item : mDocData) {
            if(item.DocumentId == docid) return item;
        }
        return null;
    }

    static class PastWorkHolder extends RecyclerView.ViewHolder {
        ImageView status;
        TextView workdate;
        TextView hotspotDWR;
        TextView hotspotJS;

        PastWorkHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.project_row_dwr, parent, false));
            status = itemView.findViewById(R.id.imgStatusDwrRow);
            workdate = itemView.findViewById(R.id.txtDwrDateRow);
            hotspotDWR = itemView.findViewById(R.id.lblDwrRow);
            hotspotJS = itemView.findViewById(R.id.lblJsRow);
        }
    }

    static class DocumentHolder extends RecyclerView.ViewHolder {
        CardView oneCard;
        CardView twoCard;
        CardView treCard;
        TextView oneUpdateDate;
        TextView twoUpdateDate;
        TextView treUpdateDate;
        TextView oneFilename;
        TextView twoFilename;
        TextView treFilename;

        DocumentHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.project_row_doc, parent, false));

            oneCard = itemView.findViewById(R.id.oneCardView);
            twoCard = itemView.findViewById(R.id.twoCardView);
            treCard = itemView.findViewById(R.id.treCardView);

            oneUpdateDate = itemView.findViewById(R.id.oneUpdateDate);
            twoUpdateDate = itemView.findViewById(R.id.twoUpdateDate);
            treUpdateDate = itemView.findViewById(R.id.treUpdateDate);

            oneFilename = itemView.findViewById(R.id.oneFilename);
            twoFilename = itemView.findViewById(R.id.twoFilename);
            treFilename = itemView.findViewById(R.id.treFilename);
        }
    }

    static class JobSetupStartHolder extends RecyclerView.ViewHolder {
        TextView jobSetupLink;

        JobSetupStartHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.project_row_setup, parent, false));
            jobSetupLink = itemView.findViewById(R.id.lblJobSetupLink);
        }
    }

    static class CoverSheetStartHolder extends RecyclerView.ViewHolder {
        CoverSheetStartHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.project_row_cover, parent, false));
        }
    }

    static class DwrStartHolder extends RecyclerView.ViewHolder {
        DwrStartHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.project_row_dwr_start, parent, false));
        }
    }

    static class DelimiterHolder extends RecyclerView.ViewHolder {
        TextView timeTitle;

        DelimiterHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_title, parent, false));
            timeTitle = itemView.findViewById(R.id.scheduleTitleDspl);
        }
    }

    static class LocalKeys {
        int PrimeKey;
        int SecondKey;
        int ThirdKey;
        LocalKeys(){}
        LocalKeys(int primeKey){ PrimeKey = primeKey; SecondKey = 0; ThirdKey = 0; }
    }

    private String getUriFromDocument(DocumentTbl doc) {
        try {
            return Functions.GetStorageName(doc.DocumentType, doc.FileName, ctx);
        } catch (ExpClass expClass) {
            return "";
        }
    }
}
