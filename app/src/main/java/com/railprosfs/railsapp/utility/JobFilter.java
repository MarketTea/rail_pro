package com.railprosfs.railsapp.utility;

import android.widget.Filter;

import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.ui_support.JobAdapter;

import java.util.ArrayList;
import java.util.List;

public class JobFilter extends Filter {
    private JobAdapter adapter;
    private List<JobTbl> filterList;

    public JobFilter(List<JobTbl> tbl, JobAdapter adapter) {
        filterList = tbl;
        this.adapter = adapter;
    }

    // Filters Adapter List so they contain the searched value in description.
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //CHANGE TO UPPER
        constraint = constraint.toString().toUpperCase();
        String[] split = ((String) constraint).split("\\s+");

        //STORE OUR FILTERED PLAYERS
        List<JobTbl> filteredList = new ArrayList<>();
        for (int i = 0; i < filterList.size(); i++) {
            String concat = filterList.get(i).getJobNumber() + " " + filterList.get(i).getDescription();
            boolean add = true;

            //Iterate through query
            for(String item : split) {
                concat = concat.toUpperCase();
                if (!concat.contains(item)) {
                    add = false;
                    break;
                }
            }

            if(add) {
                filteredList.add(filterList.get(i));
            }
        }
        results.count = filteredList.size();
        results.values = filteredList;
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.setJob((List<JobTbl>) results.values);
        adapter.notifyDataSetChanged();
    }
}
