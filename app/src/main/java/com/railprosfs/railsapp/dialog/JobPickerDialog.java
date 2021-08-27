package com.railprosfs.railsapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data.ScheduleDB;
import com.railprosfs.railsapp.service.JobListThread;
import com.railprosfs.railsapp.ui_support.JobAdapter;
import com.railprosfs.railsapp.utility.Constants;
import com.railprosfs.railsapp.utility.KTime;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * This class is used to display a list of jobs. Typical use is to pass in
 * a property name, such that only jobs associated with that property are
 * displayed. By calling this dialog, the thread to update the jobs is also
 * triggered to get the latest added to the local data store. Consider that
 * the thread to load job information ignores jobs that are not valid or
 * have too old an end date.  The property filter is done manually in code.
 * If there are not Jobs, display a message indicating as much.
 */
public class JobPickerDialog extends DialogFragment {
    private JobAdapter mAdapter;
    private JobPickerListener mListener;
    private String railroad = "";
    private SearchView searchView;

    public interface JobPickerListener {
        void setJobNumber(JobTbl tbl);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JobListThread work = new JobListThread(getContext(), ScheduleDB.getDatabase(getContext()), null);
        if(getArguments() != null) {
            railroad = getArguments().getString(Constants.RAILROAD);
            if(railroad != null){
                // The job description data includes the server side railroad code, so we need to get that value.
                railroad = Railroads.PropertyNameServer(getContext(), Railroads.PropertyKey(getContext(), railroad));
            } else {
                railroad = "";
            }
        }
        work.start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.job_dialog_fragment, container, false);
        RecyclerView mRecyclerView = rv.findViewById(R.id.rv_job_dialog);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new JobAdapter(getContext(), mListener, this, railroad);
        mRecyclerView.setAdapter(mAdapter);
        searchView = rv.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                    mAdapter.getFilter().filter(newText);
                return false;
            }
        });

        // Few layers of filtering here.  First is during load (see Refresh) when all the jobs are
        // downloaded.  Then in the SQL, only jobs that have end dates not too far in the past.
        // Finally, only show the jobs of concern for the desired railroad.
        ScheduleDB.getDatabase(getContext()).jobDao().GetJobsList(KTime.ParseHoursPast(KTime.KT_fmtDate3339k,1440).toString()).observe(getViewLifecycleOwner(), new Observer<List<JobTbl>>() {
            @Override
            public void onChanged(List<JobTbl> jobTbls) {
                if(jobTbls != null) {
                    List<JobTbl> filterJobs = new ArrayList<>();
                    for (JobTbl item:jobTbls) {
                        if(railroad.length() == 0 || item.RailRoadCode.equalsIgnoreCase(railroad)){
                            filterJobs.add(item);
                        }
                    }
                    mAdapter.RefreshJobData(filterJobs);
                }
            }
        });

        // This will only show the no railroads text if the railroad corresponding to it has no jobs corresponding to that railroad
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(searchView.getQuery().toString().length() == 0) {
                    toggleVisibility(!(mAdapter.getItemCount() > 0));
                }
            }
        });
        return rv;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof JobPickerListener) {
            mListener = (JobPickerListener) context;
        } else {
            throw new ClassCastException(getDialog().toString() + " must implement JobPickerListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void toggleVisibility(boolean visbility) {
        if(getDialog() != null) {
            TextView tv = getDialog().findViewById(R.id.no_job_text);
            if (visbility) {
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

}
