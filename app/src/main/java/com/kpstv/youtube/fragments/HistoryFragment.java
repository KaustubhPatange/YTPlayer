package com.kpstv.youtube.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kpstv.youtube.MainActivity;
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
    View v; TimerTask task;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_history, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar);

        toolbar.setTitle("History");

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);

        LoadMainMethod();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadMainMethod();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        LoadMainMethod();
        super.onResume();
    }

    void LoadMainMethod() {
        swipeRefreshLayout.setEnabled(false);
        SharedPreferences pref = getContext().getSharedPreferences("history",Context.MODE_PRIVATE);
        final ArrayList<String> urls = new ArrayList<>();
        String items = pref.getString("urls","");
        if (!items.isEmpty()) {
            urls.addAll(Arrays.asList(items.split(",")));
        }
        if (urls.size()>0) {
            swipeRefreshLayout.setRefreshing(true);

            recyclerView = v.findViewById(R.id.my_recycler_view);

            layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            adapter = new HistoryAdapter(urls,getActivity());
            recyclerView.setAdapter(adapter);

         /*   final Timer timer = new Timer();

            task = new TimerTask() {
                @Override
                public void run() {
                    if (adapter.getItemCount()==urls.size()) {
                        View view = layoutManager.findViewByPosition(0);
                        TextView b = view.findViewById(R.id.hAuthor);
                        b.setText("Hello");
                        timer.cancel();
                    }
                }
            };

           timer.schedule(task,1000,1000);*/

            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);

        }else {
            // It is empty
            LinearLayout hlayout = v.findViewById(R.id.history_linear);
            hlayout.setVisibility(View.VISIBLE);
        }
    }
}
