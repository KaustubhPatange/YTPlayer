package com.kpstv.youtube.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.HistoryAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class HistoryFragment extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    static RecyclerView.LayoutManager layoutManager;
    HistoryAdapter adapter;
    View v; boolean networkCreated,onCreateViewCalled;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        onCreateViewCalled=true;
        if (!networkCreated) {
            v = inflater.inflate(R.layout.fragment_history, container, false);

            sharedPreferences = getContext().getSharedPreferences("history",Context.MODE_PRIVATE);

            Toolbar toolbar = v.findViewById(R.id.toolbar);

            toolbar.setTitle("History");

            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);

            LoadMainMethod();

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    LoadMainMethod();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            networkCreated=true;
        }

        return v;
    }

    @Override
    public void onResume() {
        if (!onCreateViewCalled)
            LoadMainMethod();
        onCreateViewCalled=false;
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int Itemid = item.getItemId();
        switch (Itemid) {
            case R.id.action_remove:
                int icon = android.R.drawable.ic_dialog_alert;
                new AlertDialog.Builder(getContext())
                        .setTitle("Clear History")
                        .setMessage("Are you sure? This can't be undone.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("urls","");
                                editor.apply();
                                LoadMainMethod();
                            }
                        })
                        .setNegativeButton("No",null)
                        .setIcon(icon)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void LoadMainMethod() {
        swipeRefreshLayout.setEnabled(false);
        recyclerView = v.findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final ArrayList<String> urls = new ArrayList<>();
        String items = sharedPreferences.getString("urls","");
        if (!items.isEmpty()) {
            urls.addAll(Arrays.asList(items.split(",")));
        }
        if (urls.size()>0) {
            swipeRefreshLayout.setRefreshing(true);
            adapter = new HistoryAdapter(urls,getActivity());
            recyclerView.setAdapter(adapter);

            LinearLayout hlayout = v.findViewById(R.id.history_linear);
            hlayout.setVisibility(View.GONE);

            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);

        }else {
            // It is empty
            urls.clear();
            adapter = new HistoryAdapter(urls,getActivity());
            recyclerView.setAdapter(adapter);
            LinearLayout hlayout = v.findViewById(R.id.history_linear);
            hlayout.setVisibility(View.VISIBLE);
        }
    }
}
