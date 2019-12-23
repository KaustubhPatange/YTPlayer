package com.kpstv.youtube.fragments.basedOnApi;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Toast;

import com.downloader.Progress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.fragments.OPlaylistFragment;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.utils.APIResponse;
import com.kpstv.youtube.utils.AppBarStateChangeListener;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.COLLAPSED;
import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.EXPANDED;
import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.IDLE;

public class PopularFragment extends Fragment {
    private Toolbar mToolbar;
    FragmentActivity activity;
    private CollapsingToolbarLayout mToolbarLayout;
    private AppBarLayout mAppBar;
    private Button mOplayfab;
    private RecyclerView mRecyclerview;
    private LinearLayout mLinearlayout;
    private RelativeLayout mRelativelayout;
    ProgressBar progressBar;
    String fileName="ytend.csv";
    LinearLayoutManager manager;
    String ref = "top_100";
    boolean isViral=false; ArrayList<DiscoverModel> models;
    String title="Top 100 songs";
    public PopularFragment() {}

    private static final String TAG = "PopularFragment";

    View v; SongAdapter adapter;

    boolean error; boolean processAsCSV;boolean downloadNew=false;
    boolean getitFromFirebase=false; boolean passed=false;
    String json; boolean wait=false; String writeData;
    StringBuilder builder = new StringBuilder();
    int number=0;
    AsyncTask<Void,Float,Void> asyncTask;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_popular, container, false);
        initViews(v);
        activity = getActivity();

        mAppBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                appBarLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if (state == COLLAPSED) {
                            mToolbar.setTitle(title);
                            mToolbarLayout.setTitle(title);
                            mOplayfab.setVisibility(View.GONE);
                        } else if (state == EXPANDED || state == IDLE) {
                            mToolbarLayout.setTitle(" ");
                            mToolbar.setTitle(" ");
                            mOplayfab.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        mToolbar.setNavigationOnClickListener(view -> MainActivity.loadSearchFrag());

        models = new ArrayList<>();
        manager = new LinearLayoutManager(activity);
        mRecyclerview.setLayoutManager(manager);
        String tag = getTag();
        if (tag != null) {
            if (tag.equals("viral")) {
                fileName = "yviral.csv";
                ref="viral_100";
                title = "Viral 100 songs";
                isViral=true;
                mLinearlayout.removeAllViews();
                mOplayfab.setBackground(activity.getResources().getDrawable(R.drawable.button_background1));
                View view = getLayoutInflater().inflate(R.layout.trend_item2,mLinearlayout);
               // mLinearlayout.addView(view);
                mRelativelayout.setBackground(activity.getResources().getDrawable(R.drawable.trend_background2));
            }
        }

      //  new getData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return v;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {

        Log.e(TAG, "onCreateAnimation: Enter: "+enter );

        if (enter) {
            final int animatorId = android.R.anim.fade_in;
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
                    if (data!=null) {
                        String date = data.trim().split("\\$")[0];
                        if (date.equals(todayDate)||data.equals(tomorrowDate)) {
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
                    DatabaseReference timeRef = reference.child("timeString");
                    timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String val = (String) dataSnapshot.getValue();
                            if (val!=null &&
                                    (!val.equals("empty")||val.equals(todayDate)||val.equals(tomorrowDate))) {
                               getitFromFirebase=true;
                            }else {
                                // We will use api response here...
                                downloadNew=true;
                                wait=true;
                            }
                            asyncTask = new getData();
                            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            }
            if (models.size()>0) {
                mOplayfab.setOnClickListener(view -> {
                    String[] yturls = new String[models.size()];
                    MainActivity.nPlayModels.clear();
                    for (int i=0;i<models.size();i++) {
                        MetaModel metaModel = new MetaModel(
                                models.get(i).getTitle(),
                                models.get(i).getAuthor(),
                                models.get(i).getImgUrl()
                        );
                        NPlayModel model = new NPlayModel(models.get(i).getYtUrl(),new YTMeta(metaModel),false);

                        MainActivity.nPlayModels.add(model);
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
                String url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=PL4fGSI1pDJn6puJdseH2Rt9sMvt9E2M4i";
                if (isViral) {
                    url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=PL4fGSI1pDJn5kI81J1fYWK5eZRl1zJ5kM";
                }
                APIResponse response = new APIResponse(url);
                json = response.getJson();
                if (json ==null) {
                    error=true;
                    return null;
                }
                try {
                    JSONObject object = new JSONObject(json);
                    builder.append(YTutils.getTodayDate_nogaps());
                    String nextToken = object.getString("nextPageToken");
                    processJSON(object);
                    response = new APIResponse(url+"&pageToken="+nextToken);
                    json = response.getJson();
                    if (json==null) {
                        error=true;
                        return null;
                    }
                    object = new JSONObject(json);
                    processJSON(object);
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
                    if (!line.contains("|")) continue;
                    String[] childs = line.split("\\|");
                    String title = childs[0];
                    String channelTitle = childs[1];
                    String videoId = childs[2];
                    models.add(new DiscoverModel(
                       title,channelTitle,YTutils.getImageUrlID(videoId),
                       YTutils.getYtUrl(videoId)
                    ));
                    number++;
                }
            }
            Log.e(TAG, "doInBackground: The end has begun" );
            return null;
        }

        void processJSON(JSONObject object) {
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
                   models.add(new DiscoverModel(title,channelTitle
                           ,YTutils.getImageUrlID(videoId)
                           ,YTutils.getYtUrl(videoId)));
                   number++;
                   builder.append("$").append(title).append("|").append(channelTitle).append("|").append(videoId);
               }
           }catch (Exception e){e.printStackTrace();}
        }

        @Override
        protected void onPreExecute() {
            if (getitFromFirebase) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference(ref);
                DatabaseReference dataRef = reference.child("data");
                dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.e(TAG, "onDataChange: Getting data from Firebase" );
                        json = (String) snapshot.getValue();
                        YTutils.writeContent(activity,fileName,YTutils.getTodayDate_nogaps()+"$"+json);
                        processAsCSV=true;
                        wait=true;
                        Log.e(TAG, "onDataChange: Got it");
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
            }
            super.onPreExecute();
        }
    }

    private void initViews(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
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
