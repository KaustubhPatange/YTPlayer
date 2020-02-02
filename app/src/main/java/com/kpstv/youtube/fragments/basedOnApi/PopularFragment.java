package com.kpstv.youtube.fragments.basedOnApi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kpstv.youtube.AppInterface;
import com.kpstv.youtube.DPlaylistActivity;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.services.MusicService;
import com.kpstv.youtube.utils.APIResponse;
import com.kpstv.youtube.utils.AppBarStateChangeListener;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.COLLAPSED;
import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.EXPANDED;
import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.IDLE;

public class PopularFragment extends Fragment {
    private Toolbar toolbar;
    FragmentActivity activity;
    private CollapsingToolbarLayout mToolbarLayout;
    private AppBarLayout mAppBar;
    private Button mOplayfab;
    private RecyclerView mRecyclerview;
    private LinearLayout mLinearlayout;
    private RelativeLayout mRelativelayout;
    ProgressBar progressBar; ArrayList<String> strings;
    String fileName="ytrend.csv";
    LinearLayoutManager manager;
    String ref = "top_100"; boolean isOther,fallinWeek;
    ArrayList<DiscoverModel> models;
    String title="Top 100 songs";
    public PopularFragment() {}

    private static final String TAG = "PopularFragment";

    View v; SongAdapter adapter;

    boolean error; boolean processAsCSV;boolean downloadNew=false;
    boolean getitFromFirebase=false; boolean passed=false,isSoundCloud;
    String json; boolean wait=false; String writeData;
    StringBuilder builder = new StringBuilder();
    int number=0; SharedPreferences preferences; String region;
    AsyncTask<Void,Float,Void> asyncTask;
    String url;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_popular, container, false);
        initViews(v);
        activity = getActivity();

        preferences = activity.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        region = preferences.getString("pref_select_region","global");
        if (region.equals("global")) region="us";
        mAppBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                appBarLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if (state == COLLAPSED) {
                            toolbar.setTitle(title);
                            mToolbarLayout.setTitle(title);
                            mOplayfab.setVisibility(View.GONE);
                        } else if (state == EXPANDED || state == IDLE) {
                            mToolbarLayout.setTitle(" ");
                            toolbar.setTitle(" ");
                            mOplayfab.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        toolbar.setNavigationOnClickListener(view -> MainActivity.loadSearchFrag());
        toolbar.inflateMenu(R.menu.playlist_popup);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_download) {
                if (models.size() > 0) {
                    ArrayList<String> list = new ArrayList<>();
                    for (DiscoverModel model : models) {
                        String item = new StringBuilder().append(YTutils.getVideoID(model.getYtUrl())).append(">").append("0")
                                .append(">").append(model.getTitle()).append(">").append(model.getAuthor()).append(">")
                                .append(model.getImgUrl()).toString();
                        list.add(item);
                    }

                    Intent i = new Intent(activity, DPlaylistActivity.class);
                    i.putExtra("list", list);
                    startActivityForResult(i, 101);

                } else
                    Toast.makeText(activity, "Item list is empty!", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        models = new ArrayList<>();
        strings = new ArrayList<>();

        String data = YTutils.readContent(activity,"removedList.csv");
        if (data!=null && !data.isEmpty()) {
            if (data.contains(","))
                strings = new ArrayList<>(Arrays.asList(data.split(",")));
            else
                strings.add(data.trim());
        }

        manager = new LinearLayoutManager(activity);
        mRecyclerview.setLayoutManager(manager);
        String tag = getTag();
        if (tag != null) {
            Date c = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") String year =  new SimpleDateFormat("yyyy").format(c);
            switch (tag) {
                case "viral":
                    fileName = "yviral.csv";
                    ref="viral_100";
                    title = "Viral 100 songs";
                    mLinearlayout.removeAllViews();
                    mOplayfab.setBackground(activity.getResources().getDrawable(R.drawable.button_background1));
                    getLayoutInflater().inflate(R.layout.trend_item2,mLinearlayout);

                    mRelativelayout.setBackground(activity.getResources().getDrawable(R.drawable.trend_background2));
                    url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=PL4fGSI1pDJn5kI81J1fYWK5eZRl1zJ5kM";
                    break;
                case "sound20":
                    fileName = "sound20.csv";
                    isSoundCloud = true;
                    ref="sound_20";
                    title = "Top 20 songs";
                    mLinearlayout.removeAllViews();
                    mOplayfab.setBackground(activity.getResources().getDrawable(R.drawable.button_background4));
                    mOplayfab.setTextColor(ContextCompat.getColor(activity,R.color.background));
                    getLayoutInflater().inflate(R.layout.trend_item3,mLinearlayout);

                    mRelativelayout.setBackground(activity.getResources().getDrawable(R.drawable.trend_background3));
                    url = "https://api-v2.soundcloud.com/charts?&kind=top&client_id="+ AppInterface.SOUNDCLOUD_API +"&genre=soundcloud:genres:all-music&offset=0&limit=50&linked_partitioning=1";
                    break;
                case "most_viewed":
                    isOther=true;
                    fileName = "yt_mv_"+region+".csv";
                    ref = "most_viewed_"+region;
                    title = "Most Viewed";
                    mLinearlayout.removeAllViews();
                    mOplayfab.setBackground(activity.getResources().getDrawable(R.drawable.button_background2));
                    mOplayfab.setTextColor(ContextCompat.getColor(activity,R.color.background));
                    View view = getLayoutInflater().inflate(R.layout.yt_item1,mLinearlayout);
                    TextView yr = view.findViewById(R.id.year);
                    yr.setText(year);

                    mRelativelayout.setBackground(activity.getResources().getDrawable(R.drawable.yt_background1));
                    url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&region="+region+"&order=viewCount&type=video&videoCategoryId=10";
                    break;
                case "most_popular":
                    isOther=true;
                    fileName = "yt_mp_"+region+".csv";
                    ref = "most_popular_"+region;
                    title = "Most Popular";
                    mLinearlayout.removeAllViews();
                    mOplayfab.setBackground(activity.getResources().getDrawable(R.drawable.button_background1));
                    view = getLayoutInflater().inflate(R.layout.yt_item2,mLinearlayout);
                    yr = view.findViewById(R.id.year);
                    yr.setText(year);

                    mRelativelayout.setBackground(activity.getResources().getDrawable(R.drawable.trend_background1));
                    url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&region="+region+"&chart=mostPopular&maxResults=50&videoCategoryId=10";
                    break;
                case "weekly_popular":
                    fallinWeek=true;
                    isOther=true;
                    fileName = "yt_wp_"+region+".csv";
                    ref = "week_popular_"+region;
                    title = "Weekly Popular";
                    mLinearlayout.removeAllViews();
                    mOplayfab.setBackground(activity.getResources().getDrawable(R.drawable.button_background3));
                    view = getLayoutInflater().inflate(R.layout.yt_item3,mLinearlayout);
                    yr = view.findViewById(R.id.year);
                    yr.setText(year);

                    Date d = Calendar.getInstance().getTime();
                    @SuppressLint("SimpleDateFormat") int month =  Integer.parseInt(new SimpleDateFormat("MM").format(d));
                    @SuppressLint("SimpleDateFormat") int yearInt =  Integer.parseInt(new SimpleDateFormat("yyyy").format(d));
                    @SuppressLint("SimpleDateFormat") int date =  Integer.parseInt(new SimpleDateFormat("dd").format(d));
                    @SuppressLint("SimpleDateFormat") String today =  new SimpleDateFormat("yyyy-MM-dd").format(d);

                    Log.e(TAG, "onCreateView: Today:"+today+", Day: "+date+", Month: "+month+", Year: "+yearInt );

                    if (date>=7) {
                        date = date-7;
                    }else {
                        date = 30 + date - 7;
                        if (month==1) {
                            yearInt--;
                            month=12;
                        }else
                            month--;
                    }
                    Log.e(TAG, "onCreateView: Month: "+month );
                    mRelativelayout.setBackground(activity.getResources().getDrawable(R.drawable.yt_background3));
                    url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&order=viewCount&publishedAfter="+yearInt+"-"+month+"-"+date+"T00:00:00Z&publishedBefore="+today+"T00:00:00Z&region="+region+"&type=video&videoCategoryId=10";
                    break;
                default:
                    url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=PL4fGSI1pDJn6puJdseH2Rt9sMvt9E2M4i";
                    break;
            }
            Log.e(TAG, "onCreateView: Url to calculate: "+url);
        }
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==101 && resultCode == Activity.RESULT_OK)
            YTutils.showInterstitialAd(activity);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {

        Log.e(TAG, "onCreateAnimation: Enter: "+enter );

        if (enter) {
            final int animatorId = R.anim.fade_in;
            final Animation anim = AnimationUtils.loadAnimation(getActivity(), animatorId);
            if (anim==null) return null;
            anim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    Log.d(TAG, "Animation started.");
                    // additional functionality
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    Log.d(TAG, "Animation repeating.");
                    // additional functionality
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    Log.d(TAG, "Animation ended.");
                    hide();
                    String data = YTutils.readContent(activity,fileName);
                    String todayDate = YTutils.getTodayDate_nogaps();
                    String tomorrowDate = YTutils.getTomorrowDate_nogap();
                    String yesterdayDate = YTutils.getYesterday_nogap();
                    int day = Calendar.getInstance().getFirstDayOfWeek();
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                    if (data!=null) {
                        String date = data.trim().split("\\$")[0];
                        if (fallinWeek) {

                            Calendar c = Calendar.getInstance();
                            c.setTime(new Date());
                            c.add(Calendar.DATE, -7);

                            int oldDate = Integer.parseInt(date);
                            int previousDate = Integer.parseInt(df.format(c.getTime()));

                            Log.e(TAG, "onAnimationEnd: oldDate: "+oldDate+", previousDate: "+previousDate );

                            if (oldDate>=previousDate) {
                                json = data.substring(data.indexOf('$')+1); // Remove first line from string
                                processAsCSV = true;
                                wait = true;
                                asyncTask = new getData();
                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                return;
                            }
                        }else if (date.equals(todayDate)||data.equals(tomorrowDate)||date.equals(yesterdayDate)) {
                            Log.e(TAG, "onPreExecute: Load data locally...");
                            json = data.substring(data.indexOf('$')+1); // Remove first line from string
                            processAsCSV=true;
                            wait=true;
                            asyncTask = new getData();
                            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            return;
                        }
                    }
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference(ref);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild("timeString")) {
                                reference.child("timeString").setValue("empty");
                                reference.child("data").setValue("empty");
                                downloadNew=true;
                                wait=true;
                                asyncTask = new getData();
                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                return;
                            }else {
                                String val = (String) dataSnapshot.child("timeString").getValue();
                                if (val!=null) {
                                    if (fallinWeek) {
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(new Date());
                                        c.add(Calendar.DATE, -7);
                                        int oldDate = Integer.parseInt(val);
                                        int previousDate = Integer.parseInt(df.format(c.getTime()));
                                        if (oldDate>=previousDate) {
                                            json = (String) dataSnapshot.child("data").getValue();
                                            YTutils.writeContent(activity,fileName,val+"$"+json);
                                            processAsCSV=true;
                                            wait=true;
                                        }
                                        else {
                                            downloadNew=true;
                                            wait=true;
                                        }
                                    }else {
                                        Log.e(TAG, "onDataChange: TimerString Val="+val+", Today Date: "+todayDate+", Tomorrow Date: "+tomorrowDate);
                                        if (!val.contains("empty") && ( val.equals(todayDate)||val.equals(tomorrowDate))) {
                                            Log.e(TAG, "onDataChange: Getting data from Firebase" );
                                            json = (String) dataSnapshot.child("data").getValue();
                                            YTutils.writeContent(activity,fileName,YTutils.getTodayDate_nogaps()+"$"+json);
                                            processAsCSV=true;
                                            wait=true;
                                            Log.e(TAG, "onDataChange: Got it");
                                        }else {
                                            // We will use api response here...
                                            Log.e(TAG, "onDataChange: We will download new data!" );
                                            downloadNew=true;
                                            wait=true;
                                        }
                                    }
                                    asyncTask = new getData();
                                    asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }else
                                    Toast.makeText(activity, "Error: Node does not exist!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(activity, "Could not retrieve data from snap2", Toast.LENGTH_SHORT).show();
                            activity.onBackPressed();
                        }
                    });
                }
            });

            return anim;
        }
        return super.onCreateAnimation(transit,enter,nextAnim);
    }

    @SuppressLint("StaticFieldLeak")
    class getData extends AsyncTask<Void,Float,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            show();
            if (writeData!=null) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference(ref);
                reference.child("timeString").setValue(YTutils.getTodayDate_nogaps());
                reference.child("data").setValue(writeData);
                Toast.makeText(activity, "Updated server list!", Toast.LENGTH_SHORT).show();
            }
            if (models.size()>0) {
                mOplayfab.setOnClickListener(view -> {
                    String[] yturls = new String[models.size()];
                    MusicService.nPlayModels.clear();
                    for (int i=0;i<models.size();i++) {
                        MetaModel metaModel = new MetaModel(
                                YTutils.getVideoID(models.get(i).getYtUrl()),
                                models.get(i).getTitle(),
                                models.get(i).getAuthor(),
                                models.get(i).getImgUrl()
                        );
                        NPlayModel model = new NPlayModel(models.get(i).getYtUrl(),new YTMeta(metaModel),false);

                        MusicService.nPlayModels.add(model);
                        yturls[i] = models.get(i).getYtUrl();
                    }
                    MainActivity.PlayVideo(yturls);
                });
                adapter = new SongAdapter(models,activity);
                mRecyclerview.setAdapter(adapter);
            }else
                Toast.makeText(activity, "Unable to retrieve data!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onPostExecute: Process ended" );
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.e(TAG, "doInBackground: Still waiting" );
            while(!wait);
            passed=true;
            Log.e(TAG, "doInBackground: The beginning of the end");
            if (downloadNew) {
                Log.e(TAG, "doInBackground: Downloading data now..." );
                APIResponse response;
                if (isSoundCloud) {
                    HttpHandler handler = new HttpHandler();
                    json = handler.makeServiceCall(url);
                }else {
                    response = new APIResponse(url);
                    json = response.getJson();
                }
                if (json == null) {
                    error=true;
                    return null;
                }
                try {
                    JSONObject object = new JSONObject(json);
                    builder.append(YTutils.getTodayDate_nogaps());
                    processJSON(object);
                    if (!isOther && !isSoundCloud) {
                        String nextToken = object.getString("nextPageToken");
                        response = new APIResponse(url+"&pageToken="+nextToken);
                        json = response.getJson();
                        if (json==null) {
                            error=true;
                            return null;
                        }
                        object = new JSONObject(json);
                        processJSON(object);
                    }
                    writeData = builder.toString().trim();
                    YTutils.writeContent(activity,fileName,writeData);
                    writeData = writeData.substring(writeData.indexOf('$')+1);
                    return null;
                }catch (Exception e){ error=true; e.printStackTrace();}
                processAsCSV=true;
            }

            if (json!=null && processAsCSV) {
                Log.e(TAG, "doInBackground: Loading data now..." );
                String[] lines = json.split("\\$");
                for (String line: lines) {
                    if (line.isEmpty()) continue;
                    if (isSoundCloud) {
                        if (!line.contains(">")) continue;
                    }
                    else if (!line.contains("|")) continue;
                    line = line.trim();
                   try {
                       String[] childs;
                       if (isSoundCloud)
                           childs = line.split(">");
                       else
                           childs = line.split("\\|");
                       String title = childs[0];
                       String channelTitle = childs[1];
                       String videoId = childs[2];
                       DiscoverModel discoverModel;
                       if (isSoundCloud) {
                           discoverModel = new DiscoverModel(
                                   title,channelTitle,childs[3],videoId
                           );
                       }else {
                           discoverModel = new DiscoverModel(
                                   title,channelTitle,YTutils.getImageUrlID(videoId),
                                   YTutils.getYtUrl(videoId));
                       }

                       if (strings.contains("ytID:"+videoId)||strings.contains("sd:"+videoId))
                           discoverModel.setDisabled(true);

                       models.add(discoverModel);
                       number++;
                   }catch (Exception e){
                       Log.e(TAG, "doInBackground: "+line+", "+e.getMessage() );
                   }
                }
            }
            Log.e(TAG, "doInBackground: The end has begun" );
            return null;
        }

        void processJSON(JSONObject object) {
            if (isSoundCloud) {
                try {
                    JSONArray array = object.getJSONArray("collection");
                    Log.e(TAG, "processJSON: Total Size: "+array.length());
                    for (int i=0;i<array.length();i++) {
                        JSONObject obj = array.getJSONObject(i).getJSONObject("track");
                        String title = obj.getString("title");
                        String channelTitle = obj.getJSONObject("user").getString("username");

                        String imageUrl = obj.getString("artwork_url");
                        String videoId = obj.getString("permalink_url");

                        DiscoverModel discoverModel = new DiscoverModel(
                                title,channelTitle,imageUrl,videoId
                        );

                        if (strings.contains("ytID:"+videoId)||strings.contains("sd:"+videoId))
                            discoverModel.setDisabled(true);

                        models.add(discoverModel);
                        number++;
                        builder.append("$").append(title).append(">").append(channelTitle).append(">")
                                .append(videoId).append(">").append(imageUrl);
                    }
                }catch (Exception e){e.printStackTrace();}
                return;
            }
           try {
               JSONArray array = object.getJSONArray("items");
               for (int i=0;i<array.length();i++) {
                   JSONObject obj = array.getJSONObject(i);
                   JSONObject snippet = obj.getJSONObject("snippet");
                   String imageUrl = snippet.getJSONObject("thumbnails")
                           .getJSONObject("medium").getString("url");
                   String videoId = YTutils.getVideoID_ImageUri(imageUrl);
                   if (videoId.contains("%"))
                       videoId = videoId.split("%")[0];
                   String title = snippet.getString("title").replace("|","");
                   String channelTitle = snippet.getString("channelTitle").replace("|","");

                   DiscoverModel discoverModel = new DiscoverModel(title,channelTitle
                           ,YTutils.getImageUrlID(videoId)
                           ,YTutils.getYtUrl(videoId));

                   if (strings.contains("ytID:"+videoId)||strings.contains("sd:"+videoId))
                       discoverModel.setDisabled(true);

                   models.add(discoverModel);
                   number++;
                   builder.append("$").append(title).append("|").append(channelTitle).append("|").append(videoId);
               }
           }catch (Exception e){e.printStackTrace();}
        }

        @Override
        protected void onPreExecute() {
         /*   if (getitFromFirebase) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference(ref);
                DatabaseReference dataRef = reference.child("data");
                dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "run: Launching handler" );
                                if (!passed) {
                                    Log.e(TAG, "run: Refreshing async task" );
                                    if (asyncTask.getStatus()==Status.RUNNING)
                                        asyncTask.cancel(true);
                                    asyncTask = new getData();
                                    asyncTask.executeOnExecutor(THREAD_POOL_EXECUTOR);
                                }

                            }
                        },5000);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(activity, "Could not retrieve data from snap2", Toast.LENGTH_SHORT).show();
                        activity.onBackPressed();
                    }
                });
            }*/
            super.onPreExecute();
        }
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        progressBar = view.findViewById(R.id.progressBar);
        mToolbarLayout = view.findViewById(R.id.toolbar_layout);
        mAppBar = view.findViewById(R.id.app_bar);
        mOplayfab = view.findViewById(R.id.oPlayFAB);
        mRecyclerview = view.findViewById(R.id.recyclerView);
        mLinearlayout = view.findViewById(R.id.linearLayout);
        mRelativelayout = view.findViewById(R.id.relativeLayout);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    void show() {
        progressBar.setVisibility(View.GONE);
        mRecyclerview.setVisibility(View.VISIBLE);
    }

    void hide() {
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        mRecyclerview.setVisibility(View.GONE);
    }
}
