package com.kpstv.youtube.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kpstv.youtube.AppSettings;
import com.kpstv.youtube.DPlaylistActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.HistoryAdapter;
import com.kpstv.youtube.models.HistoryModel;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class HistoryFragment extends Fragment {

    public static final String TAG = "HistoryFragment";
    SwipeRefreshLayout swipeRefreshLayout;
    static RecyclerView recyclerView;
    static RecyclerView.LayoutManager layoutManager;
    static HistoryAdapter adapter;
    private Toolbar toolbar;
    View v;
    boolean networkCreated, onCreateViewCalled;
    static SharedPreferences sharedPreferences, settingspref;
    static ArrayList<HistoryModel> urls;
    static LinearLayout hiddenLayout;
    ConstraintLayout tipLayout;
    LinearLayout historyButton;
    static FragmentActivity activity;
    boolean showedOnce = false, showedUpdateOnce = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        onCreateViewCalled = true;
        if (!networkCreated) {
            v = inflater.inflate(R.layout.fragment_history, container, false);

            activity = getActivity();

            sharedPreferences = getContext().getSharedPreferences("history", Context.MODE_PRIVATE);
            settingspref = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

            toolbar = v.findViewById(R.id.toolbar);
            toolbar.setTitle("History");

            urls = new ArrayList<>();

            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);
            hiddenLayout = v.findViewById(R.id.history_linear);
            tipLayout = v.findViewById(R.id.history_layout);
            historyButton = v.findViewById(R.id.history_gotButton);

            swipeRefreshLayout.setOnRefreshListener(() -> {
                LoadMainMethod();
                swipeRefreshLayout.setRefreshing(false);
            });

            swipeRefreshLayout.setOnLongClickListener(v -> false);

            networkCreated = true;

            LoadMainMethod();
        }

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 && resultCode == Activity.RESULT_OK)
            YTutils.showInterstitialAd(activity);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private View.OnLongClickListener recyclerItemLongListener = v1 -> {
        Object[] objects = (Object[]) v1.getTag();
        int position = (int) objects[0];
        String title = (String) objects[1];
        String yturl = (String) objects[2];
        String author = (String) objects[3];
        String imageUri = (String) objects[4];
        Log.e(TAG, "YTURL: " + yturl);
        HistoryBottomSheet bottomSheet = new HistoryBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        bundle.putString("title", title);
        bundle.putString("yturl", yturl);
        bundle.putString("channelTitle", author);
        bundle.putString("imageUri", imageUri);
        bundle.putString("yturl", yturl);
        bottomSheet.setArguments(bundle);
        bottomSheet.show(getActivity().getSupportFragmentManager(), "");
        return false;
    };

    public static void removeFromHistory(int position) {
        String history = YTutils.readContent(activity, "history.csv");
        if (history != null && !history.isEmpty()) {
            String[] items = history.split("\n|\r");
            StringBuilder builder = new StringBuilder();
            String videoId = YTutils.getVideoID(urls.get(position).getVideoId());
            for (String item : items) {
                if (!item.startsWith(videoId)) {
                    builder.append(item).append("\n");
                }
            }
            YTutils.writeContent(activity, "history.csv", builder.toString().trim());
        }
        urls.remove(position);
        adapter.notifyDataSetChanged();
        if (urls.isEmpty()) {
            hiddenLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        if (!onCreateViewCalled)
            LoadMainMethod();
        onCreateViewCalled = false;
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu, menu);
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
                        .setPositiveButton("Yes", (dialog, which) -> {
                            YTutils.writeContent(activity, "history.csv", "");
                            LoadMainMethod();
                        })
                        .setNegativeButton("No", null)
                        .setIcon(icon)
                        .show();
                break;
            case R.id.action_download:
                if (urls.size() > 0) {
                    ArrayList<String> list = new ArrayList<>();
                    for (HistoryModel model : urls) {
                        String item1 = new StringBuilder().append(model.getVideoId()).append(">").append("0")
                                .append(">").append(model.getTitle()).append(">").append(model.getChannelTitle()).append(">")
                                .append(model.getImageUrl()).toString();
                        list.add(item1);
                    }

                    Intent i = new Intent(activity, DPlaylistActivity.class);
                    i.putExtra("list", list);
                    startActivityForResult(i, 101);

                } else
                    Toast.makeText(activity, "Item list is empty!", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void LoadMainMethod() {
        if (!YTutils.isInternetAvailable()) {
            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            return;
        }
        urls.clear();
        swipeRefreshLayout.setEnabled(false);
        recyclerView = v.findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // String items = sharedPreferences.getString("urls","");
        String data = YTutils.readContent(activity, "history.csv");
        if (data != null && !data.isEmpty()) {
            String[] lines = data.split("\n|\r");
            for (String line : lines) {
                String[] childs = line.split("\\|");
                urls.add(new HistoryModel(
                        childs[0], childs[2], childs[3], childs[4], childs[5], childs[6]
                ));
            }
        }
        /*if (!items.isEmpty()) {
            String[] urlarray = items.split(",");
            urls.addAll(Arrays.asList(urlarray));
        }*/
        if (urls.size() > 0) {
            swipeRefreshLayout.setRefreshing(true);
            adapter = new HistoryAdapter(urls, getActivity(), recyclerItemLongListener);
            recyclerView.setAdapter(adapter);

            hiddenLayout.setVisibility(View.GONE);

            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);

            if (!settingspref.getBoolean("showHTip", false)) {
                tipLayout.setVisibility(View.VISIBLE);
                historyButton.setOnClickListener(view -> {
                    tipLayout.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = settingspref.edit();
                    editor.putBoolean("showHTip", true);
                    editor.apply();
                });
            }
        } else {
            // It is empty
            urls.clear();
            adapter = new HistoryAdapter(urls, getActivity(), recyclerItemLongListener);
            recyclerView.setAdapter(adapter);
            hiddenLayout.setVisibility(View.VISIBLE);
        }


        SharedPreferences preferences = activity.getSharedPreferences("appSettings", MODE_PRIVATE);
        boolean checkForUpdates = preferences.getBoolean("pref_update_check", true);
        if (checkForUpdates && !showedUpdateOnce) {
            Log.e(TAG, "LoadMainMethod: In show update...");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean canInstallpackage;
                canInstallpackage = activity.getPackageManager().canRequestPackageInstalls();
                if (canInstallpackage)
                    new YTutils.CheckForUpdates(activity, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else {
                    AlertDialog alertDialog = new AlertDialog.Builder(activity)
                            .setTitle("Permission")
                            .setCancelable(false)
                            .setMessage("You need to allow install unknown source permission from settings for installing updates!")
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                        .setData(Uri.parse(String.format("package:%s", activity.getPackageName()))));
                            })
                            .create();
                    alertDialog.show();
                }
            } else
                new YTutils.CheckForUpdates(activity, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            showedUpdateOnce = true;
        }
        if (preferences.getBoolean("pref_show_purchase", true) && !showedOnce && !AppSettings.contentActivated) {
            Log.e(TAG, "LoadMainMethod: Showing alert...");
            View view = getLayoutInflater().inflate(R.layout.alert_buy, null);
            AlertDialog alertDialog = new AlertDialog.Builder(activity)
                    .setView(view)
                    .setPositiveButton("Purchase", (dialogInterface, i) -> {
                        YTutils.openPurchaseActivity(activity);
                    })
                    .setNegativeButton("Later", null)
                    .setNeutralButton("No Thanks", (dialogInterface, i) -> {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("pref_show_purchase", false);
                        editor.apply();
                    })
                    .create();
            alertDialog.show();
            showedOnce = true;
        }
    }

}
