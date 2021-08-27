package com.railprosfs.railsapp.ui_support;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.data_layout.WorkflowTbl;
import com.railprosfs.railsapp.dialog.SimpleDisplayDialog;
import com.railprosfs.railsapp.service.WebServices;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;
import com.railprosfs.railsapp.utility.Triplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static com.railprosfs.railsapp.utility.Constants.*;

/***
 *  This adaptor manages the data behind the Job Schedule View.
 */
public class WorkAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<AssignmentTbl> data;
    private List<AssignmentTbl> noData;
    private List<DwrTbl> subData;
    private List<JobSetupTbl> jsData;
    private HashMap<Integer, WorkflowTbl> dwrWFMap = new HashMap<>();
    private HashMap<Integer, WorkflowTbl> jobWFMap = new HashMap<>();

    private static final int VIEW_TYPE_PAST = 1;            // Jobs for which user had been scheduled and worked recently, but no no loner.
    private static final int VIEW_TYPE_PRESENT = 2;         // Jobs for which user is scheduled to work on today (and maybe in the past/future).
    private static final int VIEW_TYPE_FUTURE = 3;          // Jobs for which user is scheduled to work on after today.
    private static final int VIEW_TYPE_LBL_TODAY_EMPTY = 4;
    private static final int VIEW_TYPE_EX = 20;             // The "EX" means that the job has been (should be) expanded.
    private static final int VIEW_TYPE_PAST_EX = 21;
    private static final int VIEW_TYPE_PRESENT_EX = 22;
    private static final int VIEW_TYPE_FUTURE_EX = 23;
    private static final int VIEW_TYPE_LBL_NODATA = 100;    // No data found for this user, need to inform user with delimiter.
    private static final int VIEW_TYPE_LBL_TODAY = 101;     // Data below this delimiter row is for Today.
    private static final int VIEW_TYPE_LBL_UPCOMING = 102;  // Data below this delimiter row is for the Future.
    private static final int VIEW_TYPE_LBL_HIDE = 103;      // Something odd happened with this data, so just display an empty delimiter.
    private static final int MAX_DWRS = 5;                  // Up to 5 past DWRs can be displayed in expanded mode.
    private static final int MAX_JSRS = 5;                  // Up to 5 Job Setups can be displayed in expanded mode.
    private int todayPosition;

    public WorkAdapter(Context ctx, List<AssignmentTbl> rows, List<DwrTbl> subrows, List<JobSetupTbl> jsrows) {
        context = ctx;
        data = getJobDisplayList(rows);
        noData = new ArrayList<>(1);
        AssignmentTbl holdNoData = new AssignmentTbl();
        holdNoData.TimeLine = 100;
        noData.add(holdNoData);
        subData = subrows;
        jsData = jsrows;
    }

    /* This will redraw the list on the screen after getting some new data. */
    public void RefreshData(List<AssignmentTbl> rows) {
        if (rows != null && rows.size() > 0) {
            data = getJobDisplayList(rows);
        } else
            data = noData;
        notifyDataSetChanged();
    }

    /* This will redraw the list on the screen after getting some new data. */
    public void RefreshSubData(List<DwrTbl> rows) {
        if (rows != null && rows.size() > 0) {
            subData = rows;
        }
        else
            subData = new ArrayList<>();
        notifyDataSetChanged();
    }

    /* This will redraw the list on the screen after getting some new data. */
    public void RefreshJsData(List<JobSetupTbl> rows) {
        if (rows != null && rows.size() > 0)
            jsData = rows;
        else
            jsData = new ArrayList<>();
        notifyDataSetChanged();
    }

    private void ExpandRow(int key, int pos) {
        for (AssignmentTbl item : data) {
            if (item.AssignmentId == key)
                item.TimeLine += VIEW_TYPE_EX;
        }
        notifyItemChanged(pos);
    }

    private void CollapseRow(int key, int pos) {
        for (AssignmentTbl item : data) {
            if (item.AssignmentId == key)
                item.TimeLine -= VIEW_TYPE_EX;
        }
        notifyItemChanged(pos);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).TimeLine;
    }

    @Override
    public @NonNull
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LBL_TODAY_EMPTY) {
            return new EmptyJobViewHolder(parent);
        }

        return viewType < VIEW_TYPE_LBL_NODATA ? new JobViewHolder(parent) : new DelimiterHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        AssignmentTbl item = data.get(position);

        // See if this is a special row.
        if (holder.getItemViewType() >= VIEW_TYPE_LBL_NODATA) {
            DelimiterHolder rowTitle = (DelimiterHolder) holder;
            rowTitle.timeTitle.setText(GetTitle(holder.getItemViewType()));
            return;
        }

        if (holder.getItemViewType() == VIEW_TYPE_LBL_TODAY_EMPTY) {
            return;
        }

        // Normal item processing.
        JobViewHolder rowJob = (JobViewHolder) holder;

        // Common fields.
        rowJob.assignmentId.setText(String.valueOf(item.AssignmentId));

        if (item.JobSetup) {
            rowJob.dateRange.setText(context.getResources().getString(R.string.jsetup_required));
        } else {
            rowJob.dateRange.setText(item.ShiftNotes);
        }

        rowJob.imgLocation.setTag(item.JobId);
        rowJob.imgLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaunchGMaps((int) v.getTag());
            }
        });

        rowJob.jobAssignment.setText(context.getResources().getString(R.string.jobnumber));
        rowJob.jobNumber.setText(item.JobNumber);
        //rowJob.jobNumber.setText(String.valueOf(item.ServiceType));
        rowJob.jobDescription.setText(item.JobDescription);

        // This gives the buttons/links easy access to job id.
        rowJob.imgJobDetail.setTag(item.JobId);
        rowJob.btnJobDetail1.setTag(item.JobId);
        rowJob.btnJobDetail2.setTag(item.JobId);

        // This gives the buttons easy access to the row key.
        rowJob.btnExpand.setTag(item.AssignmentId);
        rowJob.btnExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpandRow((int) v.getTag(), holder.getAdapterPosition());
            }
        });
        rowJob.btnCollapse.setTag(item.AssignmentId);
        rowJob.btnCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollapseRow((int) v.getTag(), holder.getAdapterPosition());
            }
        });

        // If Today (for a proper version, this will use a proper background color).
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (item.TimeLine == VIEW_TYPE_PRESENT || item.TimeLine == VIEW_TYPE_PRESENT_EX) {
                rowJob.fullView.setBackgroundColor(context.getColor(R.color.rpWater));
            } else {
                rowJob.fullView.setBackgroundColor(context.getColor(R.color.rpWhite));
            }
        }

        // Getting the Expand/Collapsed display set up.
        for (RelativeLayout row : rowJob.dwrPast) {
            row.setVisibility(View.GONE);
        }
        int holdRRid = item.RailroadId; // protect against bad RailroadId from user entry.
        if (holdRRid < 0 || item.RailroadId >= Railroads.TotalProperties(context)) {
            holdRRid = 0;
        }
        if (item.TimeLine < VIEW_TYPE_EX) {
            rowJob.smallView.setVisibility(View.VISIBLE);
            rowJob.largeView.setVisibility(View.GONE);
            rowJob.buttonsEX.setVisibility(View.GONE);
            rowJob.jobSetup.setVisibility(View.GONE);
            rowJob.dwrStart.setVisibility(View.GONE);
        } else {    // This row is expanded.
            rowJob.smallView.setVisibility(View.GONE);
            rowJob.largeView.setVisibility(View.VISIBLE);
            rowJob.buttonsEX.setVisibility(View.VISIBLE);
            Triplet<Integer, Integer, Integer> jobInfo = new Triplet<>(item.JobId, holdRRid, item.ServiceType);
            rowJob.jobSetupLink.setTag(jobInfo);
            if(item.ServiceType > RP_FLAGGING_SERVICE) { rowJob.jobSetupLink.setText(R.string.start_job_utility); }
            rowJob.jobSetup.setVisibility(JobSetupVisible(Railroads.PropertyName(context, holdRRid), item.ServiceType));
            rowJob.coverSheet.setVisibility(item.ServiceType == RP_FLAGGING_SERVICE ? View.GONE : JobSetupVisible(Railroads.PropertyName(context, holdRRid), item.ServiceType));
            rowJob.coverSheetLink.setTag(jobInfo);
            rowJob.dwrStart.setVisibility(View.VISIBLE);
            rowJob.dwrStartLink.setTag(jobInfo);
            // Add in past Job Setups
            WebServices ApiService = new WebServices(null);
            boolean networkUnavailable = !ApiService.IsNetwork(context);
            String holdFmt;
            int jsCnt = 0;
            int jsNdx = 0;
            while (jsNdx < jsData.size() && jsCnt < MAX_JSRS) {
                JobSetupTbl jobSetupTbl = jsData.get(jsNdx);

                if (jobSetupTbl.JobId == item.JobId) {
                    rowJob.jsPast.get(jsCnt).setVisibility(View.VISIBLE);

                    Triplet<Integer, Integer, Integer> jsInfo = new Triplet<>(jobSetupTbl.Id, holdRRid, item.ServiceType);
                    rowJob.jsLabel.get(jsCnt).setTag(jsInfo);

                    holdFmt = context.getResources().getString(R.string.js_classification);
                    String[] classificationNameList = context
                            .getResources()
                            .getStringArray(R.array.job_setup_types);
                    String jsLabelTitle = Arrays
                            .asList(classificationNameList)
                            .get(jobSetupTbl.AssignmentId);
                    String jsLabelText = String.format(holdFmt, jsLabelTitle);
                    rowJob.jsLabel.get(jsCnt).setText(jsLabelText);

                    try {
                        rowJob.jsDate.get(jsCnt).setText(KTime.ParseToFormat(jobSetupTbl.CreateDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, TimeZone.getDefault().getID()));
                        if (Functions.GetDwrIcon(jobSetupTbl.Status) > 0)
                            rowJob.jsStatus.get(jsCnt).setImageResource(Functions.GetDwrIcon(jobSetupTbl.Status));
                    } catch (ExpParseToCalendar expParseToCalendar) {
                        rowJob.jsDate.get(jsCnt).setText(R.string.unknown);
                    }

                    /* Checks if the current job is on the WorkFlowTbl Queue, if it is then display the status of it on the adapter */
                    WorkflowTbl tbl = jobWFMap.get(jobSetupTbl.Id);
                    // Set Fail Information
                    if (tbl != null) {
                        rowJob.jobFail.get(jsCnt).setVisibility(View.VISIBLE);
                        if (tbl.Uploading == UPLOADING_FALSE && tbl.Retry <= tbl.RetryMax) {
                            try {
                                if(networkUnavailable){
                                    holdFmt = context.getString(R.string.sync_trying_nonetwork);
                                    rowJob.jobFail.get(jsCnt).setText(String.format(holdFmt, tbl.Retry, tbl.RetryMax));
                                }else if (KTime.IsPast(tbl.Pending, KTime.KT_fmtDate3339k, false)){
                                    holdFmt = context.getString(R.string.sync_trying_refresh);
                                    rowJob.jobFail.get(jsCnt).setText(String.format(holdFmt, tbl.Retry, tbl.RetryMax));
                                }else {
                                    holdFmt = context.getString(R.string.sync_trying_again);
                                    rowJob.jobFail.get(jsCnt).setText(String.format(holdFmt, tbl.Retry, tbl.RetryMax, KTime.CalcDateDifference(tbl.Pending, KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString(), KTime.KT_fmtDate3339k, KTime.KT_SECONDS)));
                                }
                            } catch (ExpParseToCalendar expParseToCalendar) {
                                holdFmt = context.getString(R.string.sync_trying);
                                rowJob.jobFail.get(jsCnt).setText(String.format(holdFmt, tbl.Retry, tbl.RetryMax));
                            }
                        } else if (tbl.Uploading == UPLOADING_FALSE) {
                            rowJob.jobFail.get(jsCnt).setText(jobSetupTbl.StatusMessage + ": " + context.getResources().getString(R.string.sync_failed));
                        } else if (tbl.Uploading == UPLOADING_TRUE) {
                            rowJob.jobFail.get(jsCnt).setText(R.string.sync_uploading);
                        } else {
                            rowJob.jobFail.get(jsCnt).setVisibility(View.GONE);
                        }
                    } else {
                        rowJob.jobFail.get(jsCnt).setVisibility(View.GONE);
                    }
                    ++jsCnt;
                }
                ++jsNdx;
            }
            // Add in past DWRs
            int dwrCnt = 0;
            int dwrNdx = 0;
            while (dwrNdx < subData.size() && dwrCnt < MAX_DWRS) {
                DwrTbl dwrTbl = subData.get(dwrNdx);

                if (dwrTbl.JobId == item.JobId) {
                    rowJob.dwrPast.get(dwrCnt).setVisibility(View.VISIBLE);
                    rowJob.dwrLabel.get(dwrCnt).setTag(dwrTbl.DwrId);
                    holdFmt = context.getResources().getString(R.string.dwr_classification);

                    String[] classificationNameList = context
                                                        .getResources()
                                                        .getStringArray(R.array.classification_name);
                    String dwrLabelTitle = Arrays
                                            .asList(classificationNameList)
                                            .get(dwrTbl.Classification);
                    String dwrLabelText = String.format(holdFmt, dwrLabelTitle);

                    rowJob.dwrLabel.get(dwrCnt).setText(dwrLabelText);
                    try {
                        rowJob.dwrDate.get(dwrCnt).setText(
                                KTime.ParseToFormat(
                                        dwrTbl.WorkDate,
                                        KTime.KT_fmtDate3339k,
                                        KTime.UTC_TIMEZONE,
                                        KTime.KT_fmtDateShrtMiddle,
                                        TimeZone.getDefault().getID()
                                )
                        );
                        if (Functions.GetDwrIcon(dwrTbl.Status) > 0)
                            rowJob.dwrStatus.get(dwrCnt).setImageResource(Functions.GetDwrIcon(dwrTbl.Status));
                    } catch (ExpParseToCalendar expParseToCalendar) {
                        rowJob.dwrDate.get(dwrCnt).setText(R.string.unknown);
                    }

                    /* Checks if the current job is on the WorkFlowTbl Queue, if it is then display the status of it on the adapter */
                    WorkflowTbl tbl = dwrWFMap.get(dwrTbl.DwrId);
                    // Set Fail Information
                    if (tbl != null) {
                        rowJob.dwrFail.get(dwrCnt).setVisibility(View.VISIBLE);
                        if (tbl.Uploading == UPLOADING_FALSE && tbl.Retry <= tbl.RetryMax) {
                            try {
                                if(networkUnavailable){
                                    holdFmt = context.getString(R.string.sync_trying_nonetwork);
                                    rowJob.dwrFail.get(dwrCnt).setText(String.format(holdFmt, tbl.Retry, tbl.RetryMax));
                                }else if (KTime.IsPast(tbl.Pending, KTime.KT_fmtDate3339k, false)){
                                    holdFmt = context.getString(R.string.sync_trying_refresh);
                                    rowJob.dwrFail.get(dwrCnt).setText(String.format(holdFmt, tbl.Retry, tbl.RetryMax));
                                }else {
                                    holdFmt = context.getString(R.string.sync_trying_again);
                                    rowJob.dwrFail.get(dwrCnt).setText(String.format(holdFmt, tbl.Retry, tbl.RetryMax, KTime.CalcDateDifference(tbl.Pending, KTime.ParseNow(KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE).toString(), KTime.KT_fmtDate3339k, KTime.KT_SECONDS)));
                                }
                            } catch (ExpParseToCalendar expParseToCalendar) {
                                holdFmt = context.getString(R.string.sync_trying);
                                rowJob.dwrFail.get(dwrCnt).setText(String.format(holdFmt, tbl.Retry, tbl.RetryMax));
                            }
                        } else if (tbl.Uploading == UPLOADING_FALSE) {
                            rowJob.dwrFail.get(dwrCnt).setText(dwrTbl.StatusMessage + ": " + context.getResources().getString(R.string.sync_failed));
                        } else if (tbl.Uploading == UPLOADING_TRUE) {
                            rowJob.dwrFail.get(dwrCnt).setText(R.string.sync_uploading);
                        } else {
                            rowJob.dwrFail.get(dwrCnt).setVisibility(View.GONE);
                        }
                    } else {
                        rowJob.dwrFail.get(dwrCnt).setVisibility(View.GONE);
                    }

                    ++dwrCnt;
                }
                ++dwrNdx;
            }
        }

    }

    static class EmptyJobViewHolder extends RecyclerView.ViewHolder {
        EmptyJobViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_schedule_row, parent, false));

        }
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView assignmentId;          // This is helpful for interacting with the screen.
        TextView dateRange;
        ImageView imgLocation;
        ImageView imgJobDetail;
        TextView jobAssignment;
        TextView jobNumber;
        TextView jobDescription;
        Button btnJobDetail1;
        Button btnJobDetail2;
        Button btnExpand;
        Button btnCollapse;
        RelativeLayout fullView;        // Sometimes the whole card color change.
        LinearLayout smallView;         // This is visible when DWRs collapsed.
        LinearLayout largeView;         // This is visible when DWRs expanded.
        LinearLayout buttonsEX;         // This is visible when DWRs expanded.
        RelativeLayout jobSetup;        // This is visible if Job Setup is valid for the property
        TextView jobSetupLink;
        RelativeLayout coverSheet;        // This is visible if Job Setup is valid for the property
        TextView coverSheetLink;
        RelativeLayout dwrStart;        // This is is always the first if Job Setup is done.
        TextView dwrStartLink;
        List<RelativeLayout> dwrPast;
        List<ImageView> dwrStatus;
        List<TextView> dwrDate;
        List<TextView> dwrLabel;
        List<RelativeLayout> jsPast;
        List<ImageView> jsStatus;
        List<TextView> jsDate;
        List<TextView> jsLabel;
        List<TextView> dwrFail;
        List<TextView> jobFail;

        JobViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_row, parent, false));
            assignmentId = itemView.findViewById(R.id.assignmentIdJI);
            dateRange = itemView.findViewById(R.id.dateRangeJI);
            imgLocation = itemView.findViewById(R.id.imgHomeMapLinkJT);
            imgJobDetail = itemView.findViewById(R.id.imgJobDetaiLink);
            jobAssignment = itemView.findViewById(R.id.lblJobNbrJI);
            jobNumber = itemView.findViewById(R.id.jobIdJI);
            jobDescription = itemView.findViewById(R.id.jobDescJI);
            btnJobDetail1 = itemView.findViewById(R.id.btnMoreJI);
            btnJobDetail2 = itemView.findViewById(R.id.btnMore2JI);

            fullView = itemView.findViewById(R.id.layoutJI);
            smallView = itemView.findViewById(R.id.layoutCollapsedJI);
            largeView = itemView.findViewById(R.id.layoutDWRsJI);
            buttonsEX = itemView.findViewById(R.id.layoutExpandedJI);
            btnExpand = itemView.findViewById(R.id.btnExpandJI);
            btnCollapse = itemView.findViewById(R.id.btnCollapseJI);

            jobSetup = itemView.findViewById(R.id.extraJobSetup);
            jobSetupLink = itemView.findViewById(R.id.lblJobSetupLink);
            coverSheet = itemView.findViewById(R.id.extraCoverSheet);
            coverSheetLink = itemView.findViewById(R.id.lblCoverSheetLink);
            dwrStart = itemView.findViewById(R.id.extraDwrStart);
            dwrStartLink = itemView.findViewById(R.id.lblDwrStartLink);

            dwrPast = new ArrayList<>(MAX_DWRS);
            dwrPast.add((RelativeLayout) itemView.findViewById(R.id.extraDwrRow1));
            dwrPast.add((RelativeLayout) itemView.findViewById(R.id.extraDwrRow2));
            dwrPast.add((RelativeLayout) itemView.findViewById(R.id.extraDwrRow3));
            dwrPast.add((RelativeLayout) itemView.findViewById(R.id.extraDwrRow4));
            dwrPast.add((RelativeLayout) itemView.findViewById(R.id.extraDwrRow5));

            dwrStatus = new ArrayList<>(MAX_DWRS);
            dwrStatus.add((ImageView) itemView.findViewById(R.id.imgStatusDwrRow1));
            dwrStatus.add((ImageView) itemView.findViewById(R.id.imgStatusDwrRow2));
            dwrStatus.add((ImageView) itemView.findViewById(R.id.imgStatusDwrRow3));
            dwrStatus.add((ImageView) itemView.findViewById(R.id.imgStatusDwrRow4));
            dwrStatus.add((ImageView) itemView.findViewById(R.id.imgStatusDwrRow5));

            dwrLabel = new ArrayList<>(MAX_DWRS);
            dwrLabel.add((TextView) itemView.findViewById(R.id.lblDwrRow1));
            dwrLabel.add((TextView) itemView.findViewById(R.id.lblDwrRow2));
            dwrLabel.add((TextView) itemView.findViewById(R.id.lblDwrRow3));
            dwrLabel.add((TextView) itemView.findViewById(R.id.lblDwrRow4));
            dwrLabel.add((TextView) itemView.findViewById(R.id.lblDwrRow5));

            dwrDate = new ArrayList<>(MAX_DWRS);
            dwrDate.add((TextView) itemView.findViewById(R.id.lblDwrDateRow1));
            dwrDate.add((TextView) itemView.findViewById(R.id.lblDwrDateRow2));
            dwrDate.add((TextView) itemView.findViewById(R.id.lblDwrDateRow3));
            dwrDate.add((TextView) itemView.findViewById(R.id.lblDwrDateRow4));
            dwrDate.add((TextView) itemView.findViewById(R.id.lblDwrDateRow5));

            dwrFail = new ArrayList<>(MAX_DWRS);
            dwrFail.add((TextView) itemView.findViewById(R.id.lblFailDwrRow1));
            dwrFail.add((TextView) itemView.findViewById(R.id.lblFailDwrRow2));
            dwrFail.add((TextView) itemView.findViewById(R.id.lblFailDwrRow3));
            dwrFail.add((TextView) itemView.findViewById(R.id.lblFailDwrRow4));
            dwrFail.add((TextView) itemView.findViewById(R.id.lblFailDwrRow5));

            jsPast = new ArrayList<>(MAX_JSRS);
            jsPast.add((RelativeLayout) itemView.findViewById(R.id.extraJsRow1));
            jsPast.add((RelativeLayout) itemView.findViewById(R.id.extraJsRow2));
            jsPast.add((RelativeLayout) itemView.findViewById(R.id.extraJsRow3));
            jsPast.add((RelativeLayout) itemView.findViewById(R.id.extraJsRow4));
            jsPast.add((RelativeLayout) itemView.findViewById(R.id.extraJsRow5));

            jsStatus = new ArrayList<>(MAX_JSRS);
            jsStatus.add((ImageView) itemView.findViewById(R.id.imgStatusJsRow1));
            jsStatus.add((ImageView) itemView.findViewById(R.id.imgStatusJsRow2));
            jsStatus.add((ImageView) itemView.findViewById(R.id.imgStatusJsRow3));
            jsStatus.add((ImageView) itemView.findViewById(R.id.imgStatusJsRow4));
            jsStatus.add((ImageView) itemView.findViewById(R.id.imgStatusJsRow5));

            jsLabel = new ArrayList<>(MAX_JSRS);
            jsLabel.add((TextView) itemView.findViewById(R.id.lblJsRow1));
            jsLabel.add((TextView) itemView.findViewById(R.id.lblJsRow2));
            jsLabel.add((TextView) itemView.findViewById(R.id.lblJsRow3));
            jsLabel.add((TextView) itemView.findViewById(R.id.lblJsRow4));
            jsLabel.add((TextView) itemView.findViewById(R.id.lblJsRow5));

            jsDate = new ArrayList<>(MAX_JSRS);
            jsDate.add((TextView) itemView.findViewById(R.id.lblJsDateRow1));
            jsDate.add((TextView) itemView.findViewById(R.id.lblJsDateRow2));
            jsDate.add((TextView) itemView.findViewById(R.id.lblJsDateRow3));
            jsDate.add((TextView) itemView.findViewById(R.id.lblJsDateRow4));
            jsDate.add((TextView) itemView.findViewById(R.id.lblJsDateRow5));

            jobFail = new ArrayList<>(MAX_JSRS);
            jobFail.add((TextView) itemView.findViewById(R.id.lblFailJSRow1));
            jobFail.add((TextView) itemView.findViewById(R.id.lblFailJSRow2));
            jobFail.add((TextView) itemView.findViewById(R.id.lblFailJSRow3));
            jobFail.add((TextView) itemView.findViewById(R.id.lblFailJSRow4));
            jobFail.add((TextView) itemView.findViewById(R.id.lblFailJSRow5));

        }
    }

    static class DelimiterHolder extends RecyclerView.ViewHolder {
        TextView timeTitle;

        DelimiterHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_title, parent, false));
            timeTitle = itemView.findViewById(R.id.scheduleTitleDspl);
        }
    }

    /* Rely on the database to order, but flag Past/Present/Future and delimiters for display. */
    private List<AssignmentTbl> MassageJobDspl(List<AssignmentTbl> raw) {
        if (raw == null) return null;
        long rightNow = KTime.GetEpochNow(false);
        int lastViewType = VIEW_TYPE_PAST;
        List<AssignmentTbl> ordered = new ArrayList<>(raw.size());
        boolean future = true;

        for (AssignmentTbl item : raw) {
            try {
                // Check if this is in the past(0) or future(1).
                item.TimeLine = KTime.IsPast(item.ShiftDate, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), true) ? VIEW_TYPE_PAST : VIEW_TYPE_FUTURE;
                // If in the future and the start time is earlier than now, job must be Today.
                if (item.TimeLine == VIEW_TYPE_FUTURE) {
                    Calendar startWork = KTime.ParseToCalendar(item.ShiftDate, KTime.KT_fmtDateOnlyRPFS, KTime.UTC_TIMEZONE);
                    startWork.set(Calendar.HOUR_OF_DAY, 0);
                    startWork.set(Calendar.MINUTE, 1);
                    startWork.set(Calendar.SECOND, 1);
                    if (rightNow > startWork.getTimeInMillis()) {
                        item.TimeLine = VIEW_TYPE_PRESENT;  // default is to show today's jobs expanded.
                    }
                }
            } catch (ExpParseToCalendar expParseToCalendar) {
                item.TimeLine = VIEW_TYPE_LBL_HIDE; // Hiding errors from users for now.
            }

            // May need to generate a delimiter.
            if (item.TimeLine != lastViewType) {
                if (item.TimeLine == VIEW_TYPE_LBL_HIDE) continue;
                if (item.TimeLine == VIEW_TYPE_PRESENT || item.TimeLine == VIEW_TYPE_PRESENT_EX) {
                    // Add Today Delimiter
                    AssignmentTbl holdDelimit = new AssignmentTbl();
                    holdDelimit.TimeLine = VIEW_TYPE_LBL_TODAY;
                    holdDelimit.AssignmentId = -1;
                    holdDelimit.JobId = -1;
                    todayPosition = ordered.size();
                    ordered.add(holdDelimit);
                }
                if (item.TimeLine == VIEW_TYPE_FUTURE || item.TimeLine == VIEW_TYPE_FUTURE_EX) {
                    if (future) {
                        future = false;
                        insertEmpty(ordered);
                    }
                    // Add Today Delimiter
                    AssignmentTbl holdDelimit = new AssignmentTbl();
                    holdDelimit.TimeLine = VIEW_TYPE_LBL_UPCOMING;
                    holdDelimit.AssignmentId = -1;
                    holdDelimit.JobId = -1;
                    ordered.add(holdDelimit);
                }
            }
            ordered.add(item);
            lastViewType = item.TimeLine;
        }

        insertEmpty(ordered);

        return ordered;
    }

    /**
     * This Function Takes removes all Duplicates of the data array in 3 passes from present > future > past
     *
     * @param data passed in is all the shifts corresponding to jobnumber
     * @return a list of non Duplicate AssignmentTbls with one jobnumber for each day
     */
    private List<AssignmentTbl> filterOutDuplicates(List<AssignmentTbl> data) {
        HashMap<String, AssignmentTbl> items = new HashMap<>();
        List<AssignmentTbl> past = new ArrayList<>();
        List<AssignmentTbl> present = new ArrayList<>();
        List<AssignmentTbl> future = new ArrayList<>();

        if (data == null) {
            return data;
        }

        //Store Present if duplicate remove
        for (AssignmentTbl item : data) {
            if (item.TimeLine == VIEW_TYPE_LBL_TODAY) {
                present.add(item);
            } else if (item.TimeLine == VIEW_TYPE_LBL_TODAY_EMPTY) {
                present.add(item);
                break;
            } else if (item.TimeLine == VIEW_TYPE_PRESENT || item.TimeLine == VIEW_TYPE_PRESENT_EX) {
                if (items.put(item.JobNumber, item) == null) {
                    present.add(item);
                }
            }
        }

        //Store Future if duplicate remove
        for (AssignmentTbl item : data) {
            if (item.TimeLine == VIEW_TYPE_LBL_UPCOMING) {
                future.add(item);
            } else if (item.TimeLine == VIEW_TYPE_FUTURE || item.TimeLine == VIEW_TYPE_FUTURE_EX) {
                if (items.put(item.JobNumber, item) == null) {
                    future.add(item);
                }
            }
        }

        //Store Past if duplicate remove
        for (AssignmentTbl item : data) {
            if (item.TimeLine == VIEW_TYPE_PAST || item.TimeLine == VIEW_TYPE_PAST_EX) {
                if (items.put(item.JobNumber, item) == null) {
                    past.add(item);
                }
            }
        }

        //Check if Header is the only item in future if so remove all data
        if (future.size() <= 1) {
            future.clear();
        }

        past.addAll(present);
        past.addAll(future);

        return past;
    }

    /**
     * Gets Job List and then Hides Duplicates
     **/
    private List<AssignmentTbl> getJobDisplayList(List<AssignmentTbl> raw) {
        List<AssignmentTbl> data = MassageJobDspl(raw);
        data = filterOutDuplicates(data);
        return data;
    }

/*     /** The DAO only returns end date sorted, however it does not sort the start dates past present so therefore
     * We have to sort the rest of the dates based off of start time to make sure that the end dates are either in
     * present or future.
    private List<AssignmentTbl> sortArrayForDatesLogically(List<AssignmentTbl> raw) throws ExpParseToCalendar {
        List<AssignmentTbl> sorted = new ArrayList<>();
        for (AssignmentTbl item : raw) {
            if (KTime.IsPast(item.EndDate, KTime.KT_fmtDateOnlyRPFS, true) ? true : false) {
                sorted.add(item);
            }
        }

        //Check for Start to be Empty
        int start = 0;
        if(sorted.size() != 0) {
            start = sorted.size();
        }


        List<AssignmentTbl> unsorted = new ArrayList<>();

        //Fill out Unsorted Array
        for(int i = start; i < raw.size(); i++) {
            unsorted.add(raw.get(i));
        }

        //Sort Array
        unsorted.sort(new Comparator<AssignmentTbl>() {
            @Override
            public int compare(AssignmentTbl o1, AssignmentTbl o2) {
                try {

                    int value = (int) KTime.CalcDateDifferenceNoAbs(o1.StartDate, o2.StartDate, KTime.KT_fmtDateOnlyRPFS);
                    return value;
                } catch (ExpParseToCalendar expParseToCalendar) {
                    return 0;
                }

            }
        });

        //Repopulate Sorted Tbl
        for(AssignmentTbl item : unsorted)
        {
            sorted.add(item);
        }

        return sorted;
    }
*/

    /**
     * Checks for previous assignmentTbl value, if previous assignmentTbl is in the past
     * or doesn't exist then the condition for today to have no assignment is true. Appends
     * today right after.
     */
    private void insertEmpty(List<AssignmentTbl> ordered) {
        if (ordered != null && (ordered.size() - 1 < 0 || ordered.get(ordered.size() - 1).TimeLine == VIEW_TYPE_PAST)) {
            // Create Today Delimiter
            AssignmentTbl titleDelimit = new AssignmentTbl();
            titleDelimit.TimeLine = VIEW_TYPE_LBL_TODAY;
            titleDelimit.AssignmentId = -1;
            titleDelimit.JobId = -1;
            todayPosition = ordered.size();
            ordered.add(titleDelimit);
            AssignmentTbl noTodayJob = new AssignmentTbl();
            noTodayJob.TimeLine = VIEW_TYPE_LBL_TODAY_EMPTY;
            noTodayJob.AssignmentId = -1;
            noTodayJob.JobId = -1;
            ordered.add(noTodayJob);
        }

    }

    /* Delimiter title lookup. */
    private int GetTitle(int key) {
        switch (key) {
            case VIEW_TYPE_LBL_TODAY:
                return R.string.today;
            case VIEW_TYPE_LBL_UPCOMING:
                return R.string.upcoming;
            case VIEW_TYPE_LBL_NODATA:
                return R.string.nojobs;
            case VIEW_TYPE_LBL_HIDE:
                return R.string.blank;
            default:
                return R.string.blank;
        }
    }

    // Look up the job and get the coordinates.
    private void LaunchGMaps(int jobid) {
        String link = "";
        for (AssignmentTbl item : data) {
            if (item.JobId == jobid) {
                link = item.LocationLink;
                break;
            }
        }
        // Launch Maps.
        if (link.length() > 0) {
            Uri mapUri = Uri.parse(link);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        } else {
            simpleDisplayRequest(0, context.getResources().getString(R.string.msg_inform_no_coordinates));
        }
    }

    // Does this railroad have a Job Setup Form
    private int JobSetupVisible(String name, int type) {
        return (Railroads.UseJobSetup(name, type) ? View.VISIBLE : View.GONE);
    }

    public int getTodayPosition() {
        return todayPosition;
    }

    public void updateFailInformation(List<WorkflowTbl> wfTbl) {
        // Clear Tables to Update
        dwrWFMap.clear();
        jobWFMap.clear();

        for (WorkflowTbl tbl : wfTbl) {
            if (tbl.EventType == Q_TYPE_DWR_SAVE || tbl.EventType == Q_TYPE_DWR_ROADWAY_FLAGGING_SAVE) {
                dwrWFMap.put(tbl.EventKey, tbl);
            } else if (tbl.EventType == Q_TYPE_JOB_SAVE) {
                jobWFMap.put(tbl.EventKey, tbl);
            }
        }

        notifyDataSetChanged();
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleDisplayRequest(int title, String message){
        // Set up the fragment.
        FragmentManager mgr = ((AppCompatActivity) context).getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_DISPLAY_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the confirmation.
        DialogFragment submitFrag = new SimpleDisplayDialog(title, message);
        submitFrag.show(mgr, KY_SIMPLE_DISPLAY_FRAG);
    }
}
