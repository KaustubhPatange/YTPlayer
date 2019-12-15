package com.kpstv.youtube.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kpstv.youtube.AppInterface;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.RingdroidEditActivity;
import com.kpstv.youtube.SettingsActivity;
import com.kpstv.youtube.adapters.SearchAdapter;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class LibraryFragment extends Fragment implements AppInterface {

    public LibraryFragment() {}

    View v;
    Toolbar toolbar; FragmentActivity activity;
    LinearLayout playlistLayout, settingsLayout, sleepLayout, audioCutterLayout,
            localMusicLayout,favLayout,SOW,SOF;
    ImageView githubView,pulseView,myWebView,moonId;
    RecyclerView recyclerView; LinearLayoutManager manager;
    String region; LinearLayout commonLayout; RelativeLayout progressLayout; SearchAdapter adapter;
    SharedPreferences preferences; ArrayList<SearchModel> models;
    NestedScrollView nestedScrollView; boolean networkCheck=false;
    TextView sleepTimerTextview;

    private static final String TAG = "LibraryFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (v==null) {
            v = inflater.inflate(R.layout.fragment_library, container, false);
            activity = getActivity();

            getAllViews();
            toolbar.setTitle("Library");
            recyclerView.setLayoutManager(manager);
            models = new ArrayList<>();

            preferences = activity.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
            region = preferences.getString("pref_select_region","global");

            preClicks();


            /** Add subsequent methods here*/



            if (YTutils.isInternetAvailable())
            {
                networkCheck=false;
                setRecyclerView();
            }
            else {
                internetHandler.postDelayed(internetTask,1000);
                commonLayout.setVisibility(View.GONE);
            }

        }

        return v;
    }

    Handler internetHandler = new Handler();
    Runnable internetTask = new Runnable() {
        @Override
        public void run() {
            if (YTutils.isInternetAvailable())
            {
                setRecyclerView();
                return;
            }
            internetHandler.postDelayed(this,1000);
        }
    };

    void getAllViews() {
        moonId = v.findViewById(R.id.moonId);
        sleepTimerTextview = v.findViewById(R.id.sleepTimer_textview);
        commonLayout = v.findViewById(R.id.common_recycler_layout);
        sleepLayout = v.findViewById(R.id.sleepTimer_layout);
        audioCutterLayout = v.findViewById(R.id.audio_cutter_layout);
        localMusicLayout = v.findViewById(R.id.local_music_layout);
        favLayout = v.findViewById(R.id.favourite_layout);
        progressLayout = v.findViewById(R.id.progressLayout);
        nestedScrollView = v.findViewById(R.id.nestedScrollView);
        manager = new LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,true);
        recyclerView = v.findViewById(R.id.my_recycler_view);
        toolbar = v.findViewById(R.id.toolbar);
        githubView = v.findViewById(R.id.githubImage);
        pulseView = v.findViewById(R.id.pulseWebImage);
        myWebView = v.findViewById(R.id.myWebImage);
        SOW = v.findViewById(R.id.SOW_layout);
        SOF = v.findViewById(R.id.SOF_layout);
        settingsLayout = v.findViewById(R.id.settingsLayout);
        playlistLayout = v.findViewById(R.id.playlist_layout);
    }



    @SuppressLint("StaticFieldLeak")
    void setRecyclerView() {
            new AsyncTask<Void,Void,Void>() {

                String json;

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (json.contains("\"error\":")) {
                        commonLayout.setVisibility(View.GONE);
                        return;
                    }

                    adapter = new SearchAdapter(models,activity);
                    recyclerView.setAdapter(adapter);
                    recyclerView.getLayoutManager().scrollToPosition(models.size()-1);
                    progressLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    super.onPostExecute(aVoid);
                }

                String jsonResponse(int apinumber) {
                    HttpHandler httpHandler = new HttpHandler();
                    String link;
                    if (region.equals("global")) {
                        link = "https://www.googleapis.com/youtube/v3/videos?part=id%2Csnippet&chart=mostPopular&maxResults=10" +
                                "&videoCategoryId=10&key="+API_KEYS[apinumber];
                    }else {
                        link = "https://www.googleapis.com/youtube/v3/videos?part=id%2Csnippet&chart=mostPopular&maxResults=10&regionCode="+
                                region+"&videoCategoryId=10&key="+API_KEYS[apinumber];
                    }

                    return httpHandler.makeServiceCall(link);
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    String data = YTutils.readContent(activity,"mostPopular_"+region+".csv");
                    if (data!=null && !data.isEmpty()) {
                        String line = data.split("\n|\r")[0];
                        if (!line.equals(YTutils.getTodayDate()))
                        {
                            Log.e(TAG, "doInBackground: Making data null");
                            data=null;
                        }
                    }
                    if (data==null || data.isEmpty()) {

                        Log.e(TAG, "doInBackground: Creating new data" );

                        int i=0;
                        int apiLength = API_KEYS.length;

                        do {
                            json = jsonResponse(i);
                            i++;
                        }while (json.contains("\"error\":") && i<apiLength);

                        if (!json.contains("\"error\":")) {
                            try {
                                JSONObject object = new JSONObject(json);
                                JSONArray array = object.getJSONArray("items");

                                data = YTutils.getTodayDate();

                                models.clear();

                                for (i=0;i<array.length();i++) {
                                    String videoID = array.getJSONObject(i).getString("id");
                                    JSONObject snippets = array.getJSONObject(i).getJSONObject("snippet");
                                    String title = snippets.getString("title");
                                    String channelTitle = snippets.getString("channelTitle");

                                    data += "\n"+videoID+"|"+title+"|"+channelTitle;

                                    models.add(0,new SearchModel(
                                            YTutils.getVideoTitle(title),YTutils.getImageUrlID(videoID),YTutils.getYtUrl(videoID)
                                    ));
                                }

                                YTutils.writeContent(activity,"mostPopular_"+region+".csv",data);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }else {
                        if (data.contains("|")) {
                            json = "";
                            Log.e(TAG, "doInBackground: Loading data locally");
                            models.clear();
                            String[] lines = data.split("\n|\r");
                            for (int i=1;i<11;i++) {
                                String videoID = lines[i].split("\\|")[0];
                                String title = YTutils.getVideoTitle(lines[i].split("\\|")[1]);
                                models.add(0,new SearchModel(
                                        title,
                                        YTutils.getImageUrlID(videoID),
                                        YTutils.getYtUrl(videoID)
                                ));
                            }
                        }
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onResume() {
        String newregion = preferences.getString("pref_select_region","global");
        if (!newregion.contains(region)) {
            nestedScrollView.scrollTo(0, 0);

            models.clear();
            adapter.notifyDataSetChanged();

            progressLayout.setVisibility(View.VISIBLE);
            region = newregion;

            setRecyclerView();
        }
        super.onResume();
    }

    Handler mHandler = new Handler();

    public Runnable sleepTimerTask = new Runnable() {
        @Override
        public void run() {
            if (MainActivity.isplaying && MainActivity.sleepSeconds!=0) {
                Log.e(TAG, "SleepTimerTask run: "+MainActivity.sleepSeconds);

                if (MainActivity.sleepSeconds == -2) {
                    MainActivity.sleepEndTrack=true;
                    return;
                }else MainActivity.sleepEndTrack=false;
                MainActivity.sleepSeconds = MainActivity.sleepSeconds-1;

                try {
                    if (MainActivity.sleepSeconds%60==0) {
                        int time = (MainActivity.sleepSeconds/60);
                        if (time==1)
                            sleepTimerTextview.setText("Sleep Timer - " + time +" minute");
                        else   sleepTimerTextview.setText("Sleep Timer - " + time +" minutes");
                    }
                }catch (Exception e) {
                    Log.e(TAG, "run: Failed to calculate seconds" );
                }

                if (MainActivity.sleepSeconds == 0)
                {
                    MainActivity.changePlayBack(false);
                    timerWentOff();
                }
            }else if (MainActivity.sleepSeconds==0) return;
            mHandler.postDelayed(this,1000);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // Request code for audio cutter
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();

                    Log.e(TAG, "onActivityResult: "+uri.toString() );

                    String path = YTutils.getPath(activity,uri);

                    Log.e(TAG, "onActivityResult: "+path);
                    startEditor("file:/"+path);
                }
                break;
            case 100: //Request code for sleep Timer
                if (!MainActivity.selectedItemText.isEmpty()) {
                    moonId.setColorFilter(getResources().getColor(R.color.colorAccent));
                    sleepTimerTextview.setText("Sleep Timer - " + MainActivity.selectedItemText);

                    if (MainActivity.sleepSeconds==-2)
                        Toast.makeText(activity, "Pausing at End of track!", Toast.LENGTH_SHORT).show();
                    else if (MainActivity.sleepSeconds!=0)
                        Toast.makeText(activity, "Pausing in "+MainActivity.selectedItemText+"!", Toast.LENGTH_SHORT).show();

                    mHandler.postDelayed(sleepTimerTask,1000);
                }else {
                    timerWentOff();
                }
                break;
            case 101: //Request code to change sleep Timer Text
                timerWentOff();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void timerWentOff() {
        moonId.clearColorFilter();
        sleepTimerTextview.setText("Sleep Timer");
        MainActivity.selectedItemText="";
        Toast.makeText(activity, "Timer went off!", Toast.LENGTH_SHORT).show();
    }

    private void startEditor(String filePathUri) {
        Intent intent = new Intent(activity, RingdroidEditActivity.class);
        intent.putExtra("FILE_PATH", filePathUri);
        startActivity(intent);
    }

    void preClicks() {

        localMusicLayout.setOnClickListener(view -> {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);
            ft.replace(R.id.fragment_container, MainActivity.localMusicFrag);
            ft.commit();
        });

        sleepLayout.setOnClickListener(view -> {
            SleepBottomSheet bottomSheet = new SleepBottomSheet();
            bottomSheet.show(activity.getSupportFragmentManager(),MainActivity.selectedItemText);
        });

        audioCutterLayout.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            try {
                startActivityForResult(
                        Intent.createChooser(intent, "Select an audio file"),
                        1);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(activity, "No file manager installed", Toast.LENGTH_SHORT).show();
            }
        });

        favLayout.setOnClickListener(view -> {

            String data = YTutils.readContent(activity,"favourite.csv");
            if (data==null || data.isEmpty()) {
                Toast.makeText(activity, "No favourites yet!", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] items = data.split("\n|\r");
            if (items.length==0) {
                items = new String[1];
                items[0] = data;
            }

            PlaylistModel playlistModel = new PlaylistModel(
                    YTutils.getTodayDate(),
                    "Favourites",
                    YTutils.convertArrayToArrayList(items)
            );

            MainActivity.FavouriteFrag = new OPlaylistFragment();
            Bundle args = new Bundle();
            args.putSerializable("model",playlistModel);
            MainActivity.FavouriteFrag.setArguments(args);
            MainActivity.loadedFavFrag=true;
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);
            ft.replace(R.id.fragment_container, MainActivity.FavouriteFrag);
            ft.commit();
        });

        playlistLayout.setOnClickListener(view -> {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);
            ft.replace(R.id.fragment_container, MainActivity.PlaylistFrag);
            ft.commit();
        });

        settingsLayout.setOnClickListener(v->
                startActivity(new Intent(activity, SettingsActivity.class)));
        githubView.setOnClickListener(v1 -> {
            YTutils.StartURL("https://github.com/KaustubhPatange/YTPlayer",activity);
        });
        pulseView.setOnClickListener(v1 -> {
            YTutils.StartURL("https://kaustubhpatange.github.io/YTPlayer",activity);
        });
        myWebView.setOnClickListener(v1 -> {
            YTutils.StartURL("https://kaustubhpatange.github.io",activity);
        });
        SOW.setOnClickListener(v1 -> {
            String shareText = "If you are a music lover and wants to download Spotify, YouTube music for free try this app https://kaustubhpatange.github.io/YTPlayer";
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            try {
                activity.startActivity(whatsappIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(activity, "WhatsApp is not installed!", Toast.LENGTH_SHORT).show();
                whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                activity.startActivity(whatsappIntent);
            }
        });
        SOF.setOnClickListener(v1 -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share this app");
            String shareMessage = "https://kaustubhpatange.github.io/YTPlayer";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Choose the messenger to share this AppNotify"));
        });
    }


}
