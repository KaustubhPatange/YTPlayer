package com.kpstv.youtube.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.HistoryAdapter;
import com.kpstv.youtube.adapters.SearchAdapter;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTSearch;

import org.json.JSONObject;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    View v;
    RecyclerView recyclerView;
    static RecyclerView.LayoutManager layoutManager;
    SearchAdapter adapter; boolean networkCreated; ArrayList<String> images;
    ArrayList<SearchModel> models; RelativeLayout progresslayout;

    public SearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (!networkCreated) {
            v = inflater.inflate(R.layout.fragment_search, container, false);

            models = new ArrayList<>();
            images = new ArrayList<>();
            recyclerView = v.findViewById(R.id.my_recycler_view);
            progresslayout = v.findViewById(R.id.progressLayout);
            layoutManager = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL,true);
            recyclerView.setLayoutManager(layoutManager);

            networkCreated = true;

            new getTrending().execute();
        }

        return v;
    }

    class getTrending extends AsyncTask<Void,Void,Void> {

        String csvoutput;

        @Override
        protected void onPostExecute(Void aVoid) {
            String[] csvlines = csvoutput.split("\n|\r");
            for (int i=2;i<12;i++) {
                String line = csvlines[i];
                String title = line.split(",")[1].replace("\"","");
                String author = line.split(",")[2].replace("\"","");

                String search_text = title.replace(" ","+")
                        + "+by+" + author.replace(" ","+");

                YTSearch ytSearch = new YTSearch(search_text);

                final String videoId = ytSearch.getVideoIDs().get(0);
                String imgurl = "https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg";

                Log.e("TrendingLines",line.split(",")[1].replace("\"",""));
                models.add(0,new SearchModel(
                        title, imgurl, "https://www.youtube.com/watch?v="+videoId
                ));

                images.add(imgurl);
            }

            loadDiscoverImages(true);

            recyclerView.setItemAnimator(new DefaultItemAnimator());
            adapter = new SearchAdapter(models,getActivity());
            recyclerView.setAdapter(adapter);
            recyclerView.getLayoutManager().scrollToPosition(models.size()-1);
            progresslayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler handler = new HttpHandler();
            csvoutput = handler.makeServiceCall(
                    "https://spotifycharts.com/regional/global/daily/latest/download");
            return null;
        }
    }

    void loadDiscoverImages(boolean last4) {

        final ImageView imageView1 = v.findViewById(R.id.dImage1);
        final ImageView imageView2 = v.findViewById(R.id.dImage2);
        final ImageView imageView3 = v.findViewById(R.id.dImage3);
        final ImageView imageView4 = v.findViewById(R.id.dImage4);

        if (!last4) {
            Glide.with(v).load(images.get(0)).into(imageView1);
            Glide.with(v).load(images.get(1)).into(imageView2);
            Glide.with(v).load(images.get(2)).into(imageView3);
            Glide.with(v).load(images.get(3)).into(imageView4);
        }else {
            Glide.with(v).load(images.get(9)).into(imageView1);
            Glide.with(v).load(images.get(8)).into(imageView2);
            Glide.with(v).load(images.get(7)).into(imageView3);
            Glide.with(v).load(images.get(6)).into(imageView4);
        }

    }

}
