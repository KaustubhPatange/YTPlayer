package com.kpstv.youtube;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kpstv.youtube.adapters.DiscoverAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.OnLoadMoreListener;
import com.kpstv.youtube.utils.YTSearch;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity implements HistoryBottomSheet.BottomSheetListener {

    private String[] apikeys = new String[] {"AIzaSyCA2Py9snHNdp4Y4Dkyq-z7gUfxLqdPhtQ","AIzaSyBH8szUCt1ctKQabVeQuvWgowaKxHVjn8E"};

    private RecyclerView mRecyclerView;
    private DiscoverAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private List<DiscoverModel> discoverModels;

    ArrayList<String> csvlines, directItems; int totalItems;

    ProgressBar progressBar; boolean isdirectData;

    AsyncTask<Void,Void,Void> loadTask;

    SharedPreferences preferences; String region="global";

    protected Handler handler; String fileName, intentTitle,csvString;

    LinearLayout bottom_player;
    ImageButton actionUp,actionPlay;
    TextView actionTitle; String bTitle,isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_trend);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferences = getSharedPreferences("appSettings",Context.MODE_PRIVATE);
        if (preferences!=null) {
            region = preferences.getString("pref_select_region","global");
        }

        // Get required views...
        bottom_player = findViewById(R.id.bottom_player);
        actionPlay = findViewById(R.id.action_play);
        actionUp = findViewById(R.id.action_maximize);
        actionTitle = findViewById(R.id.action_title);

        csvlines = new ArrayList<>();
        directItems = new ArrayList<>();

        Intent intent = getIntent();
        checkIntent(intent);
        csvString = intent.getStringExtra("data_csv");
        intentTitle = intent.getStringExtra("title");

        setTitle(intentTitle+" ("+ csvlines.size() +")");
        discoverModels = new ArrayList<>();
        handler = new Handler();
        mRecyclerView = findViewById(R.id.my_recycler_view);
        progressBar = findViewById(R.id.progressBar);

        if (intentTitle.contains("Viral")) {
            totalItems=50;
            fileName = "viral_"+region+".csv";
        }else {
            totalItems=200;
            fileName="trending_"+region+".csv";
        }

        if (csvString!=null) {
            setTitle(intentTitle+" ("+ csvlines.size() +")");
        }
        new loadInitialData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void openPlayer(boolean changePlayBack) {
        Intent i=new Intent(DiscoverActivity.this,PlayerActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra("data_csv",csvString);
        i.putExtra("title",intentTitle);
        Log.e("IntentTitle",intentTitle);
        i.putExtra("sendActivity","discover");
        if (changePlayBack)
            i.putExtra("changePlayback","true");
        startActivity(i);
    }

    void checkIntent(Intent intent) {
        csvString = intent.getStringExtra("data_csv");
        isPlaying = intent.getStringExtra("is_playing");
        Log.e("IsPlayingWork",isPlaying+"");
        if (isPlaying!=null && isPlaying.equals("true")) {
            bTitle = intent.getStringExtra("b_title");
            bottom_player.setVisibility(View.VISIBLE);
            actionTitle.setSelected(true);
            actionTitle.setText(bTitle);
            bottom_player.setOnClickListener(v -> openPlayer(false));
            actionUp.setOnClickListener(v -> openPlayer(false));
            actionPlay.setOnClickListener(v -> openPlayer(true));
        }else {
            bottom_player.setVisibility(View.GONE);
            actionTitle.setText(" ");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    void setInitial() {
        String[] lines = csvString.split("\n|\r");
        int startnumber = 2;
        if (intentTitle.contains("Viral")) startnumber = 1;
        for(int i=startnumber;i<lines.length;i++) {
            csvlines.add(lines[i]);
        }
    }


    private View.OnLongClickListener recyclerItemLongListener = v1 -> {
        Object[] objects = (Object[])v1.getTag();
        int position = (int) objects[0];
        String title = (String) objects[1];
        String yturl = (String) objects[2];
        HistoryBottomSheet bottomSheet = new HistoryBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putInt("pos",position);
        bundle.putString("title",title);
        bundle.putString("yturl",yturl);
        bottomSheet.setArguments(bundle);
        bottomSheet.show(getSupportFragmentManager(), "discover");
        return false;
    };

    @Override
    public boolean onSupportNavigateUp() {

        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("b_title",bTitle);
        i.putExtra("is_playing",isPlaying);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
        return true;
    }

    @Override
    public void onRemoveFromHistory(int position) {
        // Blank stuff...
    }

    class loadInitialData extends AsyncTask<Void,Void,Void> {


        @Override
        protected void onPostExecute(Void aVoid) {

            if (intentTitle.contains("Viral"))
            setTitle(intentTitle+" ("+ 50 +")");
            else
            setTitle(intentTitle+" ("+ 200 +")");

            progressBar.setVisibility(View.GONE);
            mRecyclerView.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(DiscoverActivity.this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new DiscoverAdapter(DiscoverActivity.this, discoverModels, mRecyclerView,
                    recyclerItemLongListener,csvString,intentTitle);
            mRecyclerView.setAdapter(mAdapter);


            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.setOnLoadMoreListener(() -> {
                try {
                    Log.e("SizeofArray",csvlines.size()+"");
                    if (csvlines.isEmpty()&&directItems.isEmpty())
                        return;
                    //add null , so the adapter will check view_type and show progress bar at bottom
                    discoverModels.add(null);
                    mAdapter.notifyItemInserted(discoverModels.size() - 1);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadTask = new loadFurtherData();
                            loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }, 2000);
                }catch (Exception ignored){}
            });
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler handler = new HttpHandler();
            if (intentTitle.contains("Viral")) {
                {
                    csvString = handler.makeServiceCall("https://spotifycharts.com/viral/"+region+"/daily/latest/download");
                }
            }else
            {
                csvString = handler.makeServiceCall("https://spotifycharts.com/regional/"+region+"/daily/latest/download");
            }
            String dataString = YTutils.readContent(DiscoverActivity.this,fileName);
            if (dataString!=null&&!dataString.isEmpty()) {
                String[] lines = dataString.split("\n|\r");
                if (lines[0].contains(YTutils.getTodayDate())) {
                    directItems.addAll(Arrays.asList(lines).subList(1, lines.length));
                    isdirectData = true;
                    int sizeofItems = directItems.size();
                    String[] newLines = csvString.split("\n|\r");
                    if (intentTitle.contains("Viral")) {
                        if (sizeofItems!=newLines.length-1) {
                            csvlines.addAll(Arrays.asList(newLines).subList(sizeofItems + 1, newLines.length));
                        }
                    }else {
                        if (sizeofItems!=newLines.length-2) {
                            csvlines.addAll(Arrays.asList(newLines).subList(sizeofItems + 2, newLines.length));
                        }
                    }
                    CommonLoad_direct();
                    return null;
                }
            }
            setInitial();
            CommonLoad();
            return null;
        }
    }

    class loadFurtherData extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
            mAdapter.setLoaded();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            discoverModels.remove(discoverModels.size() - 1);
            if (isdirectData)
                CommonLoad_direct();
            else
                CommonLoad();
            return null;
        }
    }

    void CommonLoad_direct() {
        if (isdirectData) {
            if (directItems.isEmpty()&&!csvlines.isEmpty()) {
                CommonLoad();
                return;
            }
            for (int i=0;i<10;i++) {
                String[] infos = directItems.get(i).split(",");
                String videoID =infos[2];
                discoverModels.add(new DiscoverModel(
                        infos[0],infos[1],YTutils.getImageUrlID(videoID),
                        YTutils.getYtUrl(videoID)
                ));
            }
            directItems.subList(0,10).clear();
        }else CommonLoad();
    }

    void CommonLoad() {
        String main = YTutils.readContent(DiscoverActivity.this,fileName);
        if (main==null||main.isEmpty()) {
            main=YTutils.getTodayDate();
        }else if (!main.split("\n|\r")[0].contains(YTutils.getTodayDate())) {
            main=YTutils.getTodayDate();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(main);
        for (int i = 0; i < 10; i++) {
            String line = csvlines.get(i);
            String title = line.split(",")[1].replace("\"","");
            String author = line.split(",")[2].replace("\"","");

            String search_text = title.replace(" ","+")
                    + "+by+" + author.replace(" ","+");

            YTSearch ytSearch = new YTSearch(search_text);

            final String videoId = ytSearch.getVideoIDs().get(0);
            String imgurl = "https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg";
            if (!main.contains(title+","))
            {
                builder.append("\n").append(title).append(",").append(author).append(",")
                        .append(videoId);
                Log.e("AddedItem",title);
            }
            discoverModels.add(new DiscoverModel(
                    title,author,imgurl,"https://www.youtube.com/watch?v="+videoId
            ));
        }
        YTutils.writeContent(DiscoverActivity.this,fileName,
                builder.toString().replaceAll("(?m)^[ \t]*\r?\n", ""));
        csvlines.subList(0,10).clear();
    }
}
