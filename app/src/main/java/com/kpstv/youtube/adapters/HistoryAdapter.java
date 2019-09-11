package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kpstv.youtube.HistoryBottomSheet;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTLength;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTStatistics;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

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
        }
    }

    public HistoryAdapter(ArrayList<String> data,Context context, View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
        this.dataSet = data;
        this.con = context;
        Dateset = new ArrayList<>();
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

        new getData(holder,urlset,listPosition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
            viewHolder.titleText.setText(model.getTitle());
            viewHolder.authorText.setText(model.getAuthor());
            viewHolder.rate_percent.setText(percent);

            String toput = DateString;
            String yesterday = String.format("%s-%s-%s",dateOnly-1,monthOnly,yearOnly);
            if (DateString.contains(formattedDate)) {
                toput="Today";
            } else if (DateString.contains(yesterday)) {
                toput = "Yesterday";
            }
            Object[] objects = new Object[3];
            objects[0]=pos; objects[1]=model.getTitle();objects[2]=ytUrl;
            viewHolder.mainCard.setTag(objects);
            viewHolder.mainCard.setOnLongClickListener(longClickListener);

            viewHolder.mainCard.setOnClickListener(v -> {

                Activity activity = (Activity) con;

                Intent intent = new Intent(con,PlayerActivity.class);
                intent.putExtra("youtubelink",new String[]{ ytUrl });
                con.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            });

            viewHolder.dateText.setText(toput);

            if (!containsDateItem(DateString)) {
                viewHolder.dateLayout.setVisibility(View.VISIBLE);
                Dateset.add(DateString);
                Log.e("ShownDataLayout",pos+"");
            }else viewHolder.dateLayout.setVisibility(View.GONE);

            viewHolder.addPlaylist.setOnClickListener(v -> {
                Activity activity = (Activity) con;
                new addToPlay(activity,ytUrl).executeOnExecutor(THREAD_POOL_EXECUTOR);
            });
            super.onPostExecute(aVoid);
        }

        class addToPlay extends AsyncTask<Void,Void,Void> {
            String yturl;
            long seconds; Activity activity; ProgressDialog dialog;
            public addToPlay(Activity activity,String yturl) {
                this.activity = activity;
                this.yturl = yturl;
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
                YTutils.addToPlayList(activity,yturl,seconds);
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                YTLength ytLength = new YTLength(YTutils.getVideoID(yturl));
                seconds = ytLength.getSeconds();
                return null;
            }
        }


        @Override
        protected Void doInBackground(String... strings) {

            String id = YTutils.getVideoID(ytUrl);
            HttpHandler handler = new HttpHandler();
            String json = handler.makeServiceCall("https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="+id+"&format=json");
            try {
                JSONObject object = new JSONObject(json);
                model = new MetaModel(
                        object.getString("title"),
                        object.getString("author_name"),
                        "https://i.ytimg.com/vi/"+id+"/mqdefault.jpg"
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

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
