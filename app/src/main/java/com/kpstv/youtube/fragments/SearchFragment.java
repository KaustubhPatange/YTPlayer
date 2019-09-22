package com.kpstv.youtube.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kpstv.youtube.R;
import com.kpstv.youtube.SearchActivity;
import com.kpstv.youtube.SettingsActivity;
import com.kpstv.youtube.adapters.SearchAdapter;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTSearch;
import com.kpstv.youtube.utils.YTutils;
import com.spyhunter99.supertooltips.ToolTip;
import com.spyhunter99.supertooltips.ToolTipManager;

import java.util.ArrayList;


public class SearchFragment extends Fragment {

    View v;
    RecyclerView recyclerView; Fragment discoverFrag;
    static RecyclerView.LayoutManager layoutManager;
    SearchAdapter adapter; boolean networkCreated; ArrayList<String> images;
    ArrayList<SearchModel> models; RelativeLayout progresslayout;
    ArrayList<Drawable> drawables; Activity activity; TextView moreTrend;
    CardView discoverViral, searchCard; boolean istrendloaded,isdiscoverloaded;
    ImageView githubView,pulseView,myWebView; LinearLayout settingsLayout;
    NestedScrollView nestedScrollView;
    AsyncTask<Void,Void,Void> trendTask, discoverTask; boolean alertShown=false;
    LinearLayout SOW,SOF;  ConstraintLayout tipLayout; LinearLayout searchButton;

    SharedPreferences preferences,settingpref; String region="global";

    private static String SpotifyTrendsCSV, SpotifyViralCSV;
    ToolTipManager toolTipManager;

    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;

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

            activity = getActivity();
            settingpref = activity.getSharedPreferences("settings",Context.MODE_PRIVATE);
            preferences = activity.getSharedPreferences("appSettings",Context.MODE_PRIVATE);
            if (preferences!=null) {
                region = preferences.getString("pref_select_region","global");
            }

            Log.e("RegionSelected",region+"");

            toolTipManager = new ToolTipManager(activity);
            ToolTip toolTip = new ToolTip()
                    .withText("You can also enter Spotify or YouTube url.")
                    .withColor(getResources().getColor(R.color.colorAccent)) //or whatever you want
                    .withAnimationType(ToolTip.AnimationType.FROM_MASTER_VIEW)
                    .withShadow();

            models = new ArrayList<>();
            drawables = new ArrayList<>();
            images = new ArrayList<>();

            tipLayout = v.findViewById(R.id.search_layout);
            searchButton = v.findViewById(R.id.search_gotButton);
            nestedScrollView = v.findViewById(R.id.nestedScrollView);
            searchCard = v.findViewById(R.id.cardView_search);
            imageView1 = v.findViewById(R.id.dImage1);
            moreTrend = v.findViewById(R.id.moreTrending);
            imageView2 = v.findViewById(R.id.dImage2);
            imageView3 = v.findViewById(R.id.dImage3);
            imageView4 = v.findViewById(R.id.dImage4);
            githubView = v.findViewById(R.id.githubImage);
            pulseView = v.findViewById(R.id.pulseWebImage);
            myWebView = v.findViewById(R.id.myWebImage);
            settingsLayout = v.findViewById(R.id.settingsLayout);
            SOW = v.findViewById(R.id.SOW_layout);
            SOF = v.findViewById(R.id.SOF_layout);

            recyclerView = v.findViewById(R.id.my_recycler_view);
            discoverViral = v.findViewById(R.id.discoverViral);
            progresslayout = v.findViewById(R.id.progressLayout);
            layoutManager = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL,true);
            recyclerView.setLayoutManager(layoutManager);

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
                startActivity(Intent.createChooser(shareIntent, "Choose the messenger to share this App"));
            });

            searchCard.setOnClickListener(v -> {
                if (!YTutils.isInternetAvailable()) {
                    Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(activity,SearchActivity.class);
                intent.putExtra("data_csv",SpotifyViralCSV);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.right_enter,R.anim.left_exit);
            });

            moreTrend.setOnClickListener(v -> {
                if (!YTutils.isInternetAvailable()) {
                    Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    return;
                }
                FrameLayout layout = activity.findViewById(R.id.fragment_container);
                if (layout!=null) {
                    discoverFrag = new DiscoverFragment();
                    Bundle args = new Bundle();
                    args.putString("data_csv",SpotifyTrendsCSV);
                    args.putString("title","Discover Trends");
                    discoverFrag.setArguments(args);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = manager.beginTransaction();
                    ft.setCustomAnimations(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    ft.replace(R.id.fragment_container, discoverFrag);
                    ft.commit();
                }
            });

            discoverViral.setOnClickListener(v -> {
                if (!YTutils.isInternetAvailable()) {
                    Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    return;
                }
                FrameLayout layout = activity.findViewById(R.id.fragment_container);
                if (layout!=null) {
                    discoverFrag = new DiscoverFragment();
                    Bundle args = new Bundle();
                    args.putString("data_csv",SpotifyViralCSV);
                    args.putString("title","Discover Viral");
                    discoverFrag.setArguments(args);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = manager.beginTransaction();
                    ft.setCustomAnimations(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    ft.replace(R.id.fragment_container, discoverFrag);
                    ft.commit();
                }
                return;

            });

            settingsLayout.setOnClickListener(v->
                    startActivity(new Intent(activity,SettingsActivity.class)));

            if (YTutils.isInternetAvailable())
            networkCreated = true;

            trendTask = new getTrending();
            trendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            discoverTask = new loadDiscoverImages();
            discoverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            if (!settingpref.getBoolean("searchTip",false)) {
                toolTipManager.showToolTip(toolTip,searchCard);
                SharedPreferences.Editor editor = settingpref.edit();
                editor.putBoolean("searchTip",true);
                editor.apply();
            }
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        String newregion = preferences.getString("pref_select_region","global");
        Log.e("onResume","region: "+region+", newregion: "+newregion);
        if (!newregion.contains(region)) {

            nestedScrollView.scrollTo(0, 0);
            Toast.makeText(activity, "Reloading data from new region!", Toast.LENGTH_SHORT).show();
           
            models.clear();
            adapter.notifyDataSetChanged();
            drawables.clear();
            images.clear();
            progresslayout.setVisibility(View.VISIBLE);
            region = newregion;
            SpotifyTrendsCSV=null;
            SpotifyViralCSV=null;

            imageView1.setImageDrawable(null);
            imageView2.setImageDrawable(null);
            imageView3.setImageDrawable(null);
            imageView4.setImageDrawable(null);

            trendTask = new getTrending();
            trendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            discoverTask = new loadDiscoverImages();
            discoverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (drawables.size()>3) {
            imageView1.setImageDrawable(drawables.get(0));
            imageView2.setImageDrawable(drawables.get(1));
            imageView3.setImageDrawable(drawables.get(2));
            imageView4.setImageDrawable(drawables.get(3));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    class getTrending extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            if (!YTutils.isInternetAvailable())
                trendTask.cancel(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            recyclerView.setItemAnimator(new DefaultItemAnimator());
            adapter = new SearchAdapter(models,activity);
            recyclerView.setAdapter(adapter);
            recyclerView.getLayoutManager().scrollToPosition(models.size()-1);
            progresslayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            istrendloaded = true;

            if (!settingpref.getBoolean("showSTip",false)) {
                tipLayout.setVisibility(View.VISIBLE);
                searchButton.setOnClickListener(view -> {
                    tipLayout.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = settingpref.edit();
                    editor.putBoolean("showSTip",true);
                    editor.apply();
                });
            }

            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (SpotifyTrendsCSV==null) {
                HttpHandler handler = new HttpHandler();
                SpotifyTrendsCSV = handler.makeServiceCall(
                        "https://spotifycharts.com/regional/"+region+"/daily/latest/download");
            }

            String trendRead = YTutils.readContent(activity,"trend_"+region+".csv");
            if (trendRead!=null && !trendRead.isEmpty()) {
                String[] lines = trendRead.split("\n|\r");
                if (lines[0].contains(YTutils.getTodayDate())&&lines.length==11) {
                    for (int i=1;i<11;i++) {
                        String id = lines[i].split(",")[1];
                        models.add(new SearchModel(
                                lines[i].split(",")[0],
                                YTutils.getImageUrlID(id),
                                YTutils.getYtUrl(id)

                        ));
                    }
                    return null;
                }
            }

            if (models.size()<10) {
                models.clear();
                String[] csvlines = SpotifyTrendsCSV.split("\n|\r");
                for (int i=2;i<12;i++) {
                   try {
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
                   }catch (Exception ignored){ Log.e("ExceptionFragment",ignored.getMessage()); }
                }
            }
            // Save data to internal storage
            saveTrendToInternal();
            return null;
        }
    }

    void saveTrendToInternal() {
        String FILE_NAME = "trend_"+region+".csv";
        StringBuilder builder = new StringBuilder();
        builder.append(YTutils.getTodayDate()).append("\n");

        for(SearchModel model : models) {
            builder.append(model.getTitle()).append(",").append(YTutils.getVideoID(model.getYturl())).append("\n");
        }

        YTutils.writeContent(activity,FILE_NAME,builder.toString());
    }

    void saveDiscoverToInternal() {
        String FILE_NAME = "discover_"+region+".csv";
        StringBuilder builder = new StringBuilder();
        builder.append(YTutils.getTodayDate()+"\n");
        for (String image : images) {
            builder.append(image+"\n");
        }

        YTutils.writeContent(activity,FILE_NAME,builder.toString());
    }

    class loadDiscoverImages extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!YTutils.isInternetAvailable()) {
                discoverTask.cancel(true);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadImageGlide(images.get(0),imageView1);
            loadImageGlide(images.get(1),imageView2);
            loadImageGlide(images.get(2),imageView3);
            loadImageGlide(images.get(3),imageView4);

            isdiscoverloaded=true;
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (SpotifyViralCSV==null) {
                HttpHandler handler = new HttpHandler();
                SpotifyViralCSV = handler.makeServiceCall(
                        "https://spotifycharts.com/viral/"+region+"/daily/latest/download"
                );
            }

            String discoverRead = YTutils.readContent(activity,"discover_"+region+".csv");
            if (discoverRead!=null && !discoverRead.isEmpty()) {
                String[] lines = discoverRead.split("\n|\r");
                if (lines[0].contains(YTutils.getTodayDate())&&lines.length==5) {
                    for (int i=1;i<5;i++) {
                        images.add(lines[i]);
                    }
                    return null;
                }
            }

            if (images.size()<4) {
                images.clear();
                int length=5;
                String[] csvlines = SpotifyViralCSV.split("\n|\r");
                for (int i=1;i<length;i++) {
                    String line = csvlines[i];
                    String title = line.split(",")[1].replace("\"","");
                    String author = line.split(",")[2].replace("\"","");

                    String search_text = title.replace(" ","+")
                            + "+by+" + author.replace(" ","+");

                    YTSearch ytSearch = new YTSearch(search_text);

                    if (ytSearch.getVideoIDs().size()>0) {
                        final String videoId = ytSearch.getVideoIDs().get(0);
                        String imgurl = "https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg";
                        images.add(imgurl);
                    }else {
                        length++;
                    }
                }
            }
            saveDiscoverToInternal();
            return null;
        }
    }

    void loadImageGlide(String url,final ImageView imageView) {
        Glide.with(v).load(url).addListener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                drawables.add(resource);
                imageView.setImageDrawable(resource);
                return true;
            }
        }).into(imageView);
    }

}
