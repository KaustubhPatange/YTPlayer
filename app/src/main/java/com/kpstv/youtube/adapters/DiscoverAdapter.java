package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.OnLoadMoreListener;
import com.kpstv.youtube.utils.YTLength;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.*;

public class DiscoverAdapter extends RecyclerView.Adapter {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<DiscoverModel> discoverModels;
    Context con; String csvString,intentTitle;
    private int visibleThreshold = 5; View.OnLongClickListener longClickListener;
    private int lastVisibleItem, totalItems;
    ArrayList<Integer> adNumbers = new ArrayList<>();
    private boolean loading,isScrolling; int scrollOutItems,currentItems;
    private OnLoadMoreListener onLoadMoreListener;

    public DiscoverAdapter(Context context, List<DiscoverModel> students, RecyclerView recyclerView, View.OnLongClickListener longClickListener,
                           String data,String title) {
        discoverModels = students;
        this.con = context;
        this.csvString = data;
        this.intentTitle = title;
        this.longClickListener = longClickListener;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    {
                        isScrolling = true;
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    currentItems = linearLayoutManager.getChildCount();
                    scrollOutItems = linearLayoutManager.findFirstVisibleItemPosition();
                    totalItems = linearLayoutManager.getItemCount();
                    if(!loading && isScrolling && (currentItems + scrollOutItems == totalItems))
                    {
                        isScrolling = false;
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return discoverModels.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.history_item, parent, false);

            vh = new MyViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            int total = discoverModels.size();
            final DiscoverModel model = discoverModels.get(position);

            final MyViewHolder viewHolder = ((MyViewHolder) holder);

            viewHolder.authorText.setText(model.getAuthor());
            viewHolder.titleText.setText(model.getTitle());
            viewHolder.rate_percent.setText("#"+(position+1));

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

            viewHolder.addPlaylist.setOnClickListener(v -> {
                Activity activity = (Activity) con;
                new getData(activity,model.getYtUrl()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            });

            Object[] objects = new Object[3];
            objects[0]=position; objects[1]=model.getTitle();objects[2]=model.getYtUrl();
            viewHolder.mainCard.setTag(objects);
            viewHolder.mainCard.setOnLongClickListener(longClickListener);

            viewHolder.mainCard.setOnClickListener(v -> {
                Activity activity = (Activity) con;

                Intent intent = new Intent(con,PlayerActivity.class);
                intent.putExtra("youtubelink",new String[]{ model.getYtUrl() });
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                con.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            });

            if (position%5==0 && position!=0 && position%10!=0) {
                // Load ads on 5,15,25...
                Log.e("ShowingAds","pos: "+position);
                viewHolder.adLayout.setVisibility(VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().build();
                viewHolder.adView.loadAd(adRequest);
            }else {
                viewHolder.adLayout.setVisibility(GONE);
            }
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }



    class getData extends AsyncTask<Void,Void,Void> {
        String yturl;
        long seconds; Activity activity; ProgressDialog dialog;
        public getData(Activity activity,String yturl) {
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

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return discoverModels.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

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

        public MyViewHolder(View v) {
            super(v);
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

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar1);
        }
    }
}