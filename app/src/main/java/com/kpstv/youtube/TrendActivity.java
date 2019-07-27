package com.kpstv.youtube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.kpstv.youtube.adapters.TrendAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.OnLoadMoreListener;
import com.kpstv.youtube.utils.YTSearch;

import java.util.ArrayList;
import java.util.List;

public class TrendActivity extends AppCompatActivity {

    private String[] apikeys = new String[] {"AIzaSyCA2Py9snHNdp4Y4Dkyq-z7gUfxLqdPhtQ","AIzaSyBH8szUCt1ctKQabVeQuvWgowaKxHVjn8E"};

    private RecyclerView mRecyclerView;
    private TrendAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private List<DiscoverModel> discoverModels;

    ArrayList<String> csvlines;

    ProgressBar progressBar;

    protected Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_trend);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        csvlines = new ArrayList<>();

        Intent intent = getIntent();
        String csv = intent.getStringExtra("data_csv");
        String title = intent.getStringExtra("title");
        String[] lines = csv.split("\n|\r");
        int startnumber = 2;
        if (title.contains("Viral")) startnumber = 1;
        for(int i=startnumber;i<lines.length;i++) {
         csvlines.add(lines[i]);
        }

        setTitle(title+" ("+ csvlines.size() +")");

        discoverModels = new ArrayList<>();
        handler = new Handler();
        mRecyclerView = findViewById(R.id.my_recycler_view);
        progressBar = findViewById(R.id.progressBar);

        new loadInitialData().execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    class loadInitialData extends AsyncTask<Void,Void,Void> {


        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            mRecyclerView.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(TrendActivity.this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new TrendAdapter(TrendActivity.this, discoverModels, mRecyclerView);
            mRecyclerView.setAdapter(mAdapter);

            
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    if (csvlines.isEmpty())
                        return;
                    //add null , so the adapter will check view_type and show progress bar at bottom
                    discoverModels.add(null);
                    mAdapter.notifyItemInserted(discoverModels.size() - 1);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new loadFurtherData().execute();
                        }
                    }, 2000);

                }
            });
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
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
