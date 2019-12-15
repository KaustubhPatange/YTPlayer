package com.kpstv.youtube.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kpstv.youtube.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocalSearchFragment extends Fragment {
    public LocalSearchFragment() {}

    View v;
    @BindView(R.id.searchEditText) AutoCompleteTextView searchEditText;
    @BindView(R.id.removeText) ImageView closeButton;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.songText) TextView songText;
    @BindView(R.id.my_recycler_view) RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v==null) {
            v = inflater.inflate(R.layout.fragment_local_search, container, false);
            ButterKnife.bind(v);


        }
        return v;
    }

}
