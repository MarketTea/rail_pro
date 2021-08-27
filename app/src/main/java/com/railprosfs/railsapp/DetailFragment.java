package com.railprosfs.railsapp;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.ui_support.DetailsAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *  This fragment holds the Job work history and the documents.
 */
public class DetailFragment extends Fragment {

    private RecyclerView mRecyclerViewPV;
    private DetailsAdapter mDetailsAdapter;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.project_fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerViewPV = view.findViewById(R.id.recyclerviewPV);
        mDetailsAdapter= new DetailsAdapter(getActivity());
        mRecyclerViewPV.setAdapter(mDetailsAdapter);
        mRecyclerViewPV.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    /*
        The Display methods are mostly called by the activity to update the description.
    */
    public void ShowJsDetail(List<JobSetupTbl> jss, int type){
        mDetailsAdapter.RefreshJsData(jss, type);
    }

    public void ShowDwrDetail(List<DwrTbl> dwrs, int type){
        mDetailsAdapter.RefreshDwrData(dwrs, type);
    }

    public void ShowDocDetail(List<DocumentTbl> docs, int type){
        mDetailsAdapter.RefreshDocData(docs, type);
    }


}
