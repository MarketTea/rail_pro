package com.railprosfs.railsapp.ui_support;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.railprosfs.railsapp.dialog.JobPickerDialog;
import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.databinding.JobFragmentBinding;
import com.railprosfs.railsapp.utility.JobFilter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

public class JobAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private Context ctx;
    private LayoutInflater layoutInflater;
    private List<JobTbl> jobs;
    private JobFilter jobFilter;
    private JobPickerDialog.JobPickerListener mFrag;
    private DialogFragment mDialog;
    private int lastPosition = -1;
    private String railroad;

    public JobAdapter(Context ctx, JobPickerDialog.JobPickerListener mFrag, DialogFragment mDialog, String railroad) {
        this.ctx = ctx;
        jobs = new ArrayList<>();
        this.mFrag = mFrag;
        this.mDialog = mDialog;
        this.railroad = railroad;
    }

    public void RefreshJobData(List<JobTbl> data) {
        if(data != null) {
            jobs = data;
        }
        notifyDataSetChanged();
    }

    public void setJob(List<JobTbl> data) {
        if(data != null) {
            jobs = data;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        JobFragmentBinding jvb = DataBindingUtil.inflate(layoutInflater, R.layout.job_fragment, parent, false);
        return new JobViewHolder(jvb);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final JobTbl tbl = jobs.get(position);
        ((JobViewHolder) holder).binding.setField(tbl);
        holder.itemView.findViewById(R.id.job_cv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Clicked", "Clicked");
                mFrag.setJobNumber(tbl);
                mDialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    @Override
    public Filter getFilter() {
        if(jobFilter == null) {
            jobFilter = new JobFilter(jobs, this) ;
        }
        return jobFilter;
    }

    public class JobViewHolder extends  RecyclerView.ViewHolder {
        private final JobFragmentBinding binding;
        public JobViewHolder(final JobFragmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
