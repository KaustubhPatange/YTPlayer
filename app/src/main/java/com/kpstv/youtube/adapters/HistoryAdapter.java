package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kpstv.youtube.AppInterface;
import com.kpstv.youtube.AppSettings;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTLength;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class HistoryAdapter  extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private ArrayList<String> dataSet;
    private ArrayList<String> Dateset;
    View.OnLongClickListener longClickListener;
    Context con; boolean checkForUpdates;


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView rate_percent;
        TextView titleText;
        TextView authorText;
        TextView dateText;
        ImageView imageView;
        LinearLayout dateLayout;
        LinearLayout addPlaylist;
        CardView mainCard;
        LinearLayout adLayout;
        AdView adView;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.rate_percent = itemView.findViewById(R.id.hRate_percent);
            this.titleText = itemView.findViewById(R.id.hTitle);
            this.authorText = itemView.findViewById(R.id.hAuthor);
            this.dateText = itemView.findViewById(R.id.hDate);
            this.imageView = itemView.findViewById(R.id.hImage);
            this.dateLayout = itemView.findViewById(R.id.hDate_layout);
            this.addPlaylist = itemView.findViewById(R.id.hAdd_playlist);
            this.mainCard = itemView.findViewById(R.id.cardView);
            this.adLayout = itemView.findViewById(R.id.adViewLayout);
            this.adView = itemView.findViewById(R.id.adView);
        }
    }

    public HistoryAdapter(ArrayList<String> data,Context context, View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
        this.dataSet = data;
        this.con = context;
        Dateset = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);

        SharedPreferences preferences = parent.getContext().getSharedPreferences("appSettings",MODE_PRIVATE);
        checkForUpdates = preferences.getBoolean("pref_update_check",true);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        String urlset = dataSet.get(listPosition);

        new getData(holder,urlset,listPosition).execute();

        if (checkForUpdates)
        {
            Activity activity = (Activity) con;
            new YTutils.CheckForUpdates(activity,true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    class getData extends AsyncTask<String,Void,Void> {

        MyViewHolder viewHolder; String DateString,ytUrl;
        MetaModel model;int pos; String percent;

        public getData(MyViewHolder holder, String url,int postion) {
            viewHolder = holder;
            pos = postion;
            ytUrl = url.split("\\|")[0];
            DateString = url.split("\\|")[1];
            percent = url.split("\\|")[2]+"%";
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (model ==null || model.getImgUrl() == null)
                return;
            Date c = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = df.format(c);
            @SuppressLint("SimpleDateFormat") int dateOnly = Integer.parseInt(new SimpleDateFormat("dd").format(c));
            @SuppressLint("SimpleDateFormat") String monthOnly = new SimpleDateFormat("MM").format(c);
            @SuppressLint("SimpleDateFormat") String yearOnly = new SimpleDateFormat("yyyy").format(c);


            Glide.with(con).load(model.getImgUrl()).addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    viewHolder.imageView.setImageDrawable(resource);
                    return true;
                }
            }).into(viewHolder.imageView);
            viewHolder.titleText.setText(YTutils.getVideoTitle(model.getTitle()));
            viewHolder.authorText.setText(YTutils.getChannelTitle(model.getTitle(),model.getAuthor()));
            viewHolder.rate_percent.setText(percent);

            String toput = DateString;
            String yesterday = String.format("%s-%s-%s",dateOnly-1,monthOnly,yearOnly);
            if (DateString.contains(formattedDate)) {
                toput="Today";
            } else if (DateString.contains(yesterday)) {
                toput = "Yesterday";
            }
            Object[] objects = new Object[5];
            objects[0]=pos; objects[1]=model.getTitle();objects[2]=ytUrl;objects[3]=model.getAuthor();
            objects[4]=model.getImgUrl();
            viewHolder.mainCard.setTag(objects);
            viewHolder.mainCard.setOnLongClickListener(longClickListener);

            viewHolder.mainCard.setOnClickListener(v -> {

               /* Activity activity = (Activity) con;

                Intent intent = new Intent(con,PlayerActivity.class);
                intent.putExtra("youtubelink",new String[]{ ytUrl });
                con.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);*/

               String[] arr = new String[dataSet.size()];
               for (int i = 0; i<arr.length;i++) {
                   arr[i] = dataSet.get(i).split("\\|")[0];
               }
               MainActivity.PlayVideo(arr,pos);
            });

            viewHolder.dateText.setText(toput);

            if (!containsDateItem(DateString)) {
                viewHolder.dateLayout.setVisibility(View.VISIBLE);
                Dateset.add(DateString);
                Log.e("ShownDataLayout",pos+"");
            }else viewHolder.dateLayout.setVisibility(View.GONE);

            viewHolder.addPlaylist.setOnClickListener(v -> {
                Activity activity = (Activity) con;
                new addToPlay(activity,model).executeOnExecutor(THREAD_POOL_EXECUTOR);
            });

            if (pos%5==0 && pos!=0 && pos%10!=0 && AppSettings.showAds) {
                // Load ads on 5,15,25...
                Log.e("ShowingAds","pos: "+pos);
                viewHolder.adLayout.setVisibility(VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().build();
                viewHolder.adView.loadAd(adRequest);
            }else {
                viewHolder.adLayout.setVisibility(GONE);
            }
            super.onPostExecute(aVoid);
        }

        class addToPlay extends AsyncTask<Void,Void,Void> {
            MetaModel model;
            long seconds=0; Activity activity; ProgressDialog dialog;
            public addToPlay(Activity activity,MetaModel model) {
                this.activity = activity;
                this.model = model;
                dialog = new ProgressDialog(activity);
            }

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Parsing your playlist...");
                dialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dialog.dismiss();
                YTutils.addToPlayList(activity,YTutils.getVideoID_ImageUri(model.getImgUrl()),
                        model.getTitle(),model.getAuthor(),model.getImgUrl(),seconds);
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (model.getImgUrl().contains("i.ytimg")) {
                    YTLength ytLength = new YTLength(YTutils.getVideoID_ImageUri(model.getImgUrl()));
                    seconds = ytLength.getSeconds();
                }else {
                    // TODO: Add support for soundCloud
                }
                return null;
            }
        }


        @Override
        protected Void doInBackground(String... strings) {
            YTMeta ytMeta = new YTMeta(YTutils.getVideoID(ytUrl));
            if (ytMeta.getVideMeta()!=null) {
                model = ytMeta.getVideMeta();
            }
            return null;
        }
    }

    private static final String TAG = "HistoryAdapter";
    boolean containsDateItem(String item) {
        for (int i=0;i<Dateset.size();i++) {
            if (Dateset.get(i).contains(item))
                return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {

        return dataSet.size();
    }
}
