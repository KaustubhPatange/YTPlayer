package com.kpstv.youtube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.kpstv.youtube.adapters.DiscoverAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.OnLoadMoreListener;
import com.kpstv.youtube.utils.YTSearch;

import java.util.ArrayList;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {

    private String[] apikeys = new String[] {"AIzaSyCA2Py9snHNdp4Y4Dkyq-z7gUfxLqdPhtQ","AIzaSyBH8szUCt1ctKQabVeQuvWgowaKxHVjn8E"};

    private RecyclerView mRecyclerView;
    private DiscoverAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private List<DiscoverModel> discoverModels;

    ArrayList<String> csvlines;

    ProgressBar progressBar;

    AsyncTask<Void,Void,Void> loadTask;

    protected Handler handler; String intentTitle,csvString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_trend);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        csvlines = new ArrayList<>();

        Intent intent = getIntent();
        csvString = intent.getStringExtra("data_csv");
        intentTitle = intent.getStringExtra("title");

        setTitle(intentTitle+" ("+ csvlines.size() +")");
        discoverModels = new ArrayList<>();
        handler = new Handler();
        mRecyclerView = findViewById(R.id.my_recycler_view);
        progressBar = findViewById(R.id.progressBar);

        if (csvString!=null) {
            setTitle(intentTitle+" ("+ csvlines.size() +")");
        }
        new loadInitialData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void setInitial() {
        String[] lines = csvString.split("\n|\r");
        int startnumber = 2;
        if (intentTitle.contains("Viral")) startnumber = 1;
        for(int i=startnumber;i<lines.length;i++) {
            csvlines.add(lines[i]);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    class loadInitialData extends AsyncTask<Void,Void,Void> {


        @Override
        protected void onPostExecute(Void aVoid) {

            setTitle(intentTitle+" ("+ (csvlines.size()+10) +")");

            progressBar.setVisibility(View.GONE);
            mRecyclerView.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(DiscoverActivity.this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new DiscoverAdapter(DiscoverActivity.this, discoverModels, mRecyclerView);
            mRecyclerView.setAdapter(mAdapter);

            
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                   try {
                       Log.e("SizeofArray",csvlines.size()+"");
                       if (csvlines.isEmpty())
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
                }
            });
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler handler = new HttpHandler();
            if (intentTitle.contains("Viral")) {
                csvString = handler.makeServiceCall("https://spotifycharts.com/viral/global/daily/latest/download");
            }else
                csvString = handler.makeServiceCall("https://spotifycharts.com/regional/global/daily/latest/download");
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
            CommonLoad();
            return null;
        }
    }

    void CommonLoad() {
        for (int i = 0; i < 10; i++) {
            String line = csvlines.get(i);
            String title = line.split(",")[1].replace("\"","");
            String author = line.split(",")[2].replace("\"","");

            String search_text = title.replace(" ","+")
                    + "+by+" + author.replace(" ","+");

            YTSearch ytSearch = new YTSearch(search_text);

            final String videoId = ytSearch.getVideoIDs().get(0);
            String imgurl = "https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg";
            discoverModels.add(new DiscoverModel(
                    title,author,imgurl,"https://www.youtube.com/watch?v="+videoId
            ));
        }
        csvlines.subList(0,10).clear();
    }
}
