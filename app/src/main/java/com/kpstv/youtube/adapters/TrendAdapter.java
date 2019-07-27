package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.OnLoadMoreListener;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONObject;

import java.util.List;

public class TrendAdapter extends RecyclerView.Adapter {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<DiscoverModel> discoverModels;
    Context con;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public TrendAdapter(Context context,List<DiscoverModel> students, RecyclerView recyclerView) {
        discoverModels = students;
        this.con = context;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading
                                    && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                // End has been reached
                                // Do something
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

            final DiscoverModel model = discoverModels.get(position);

            final MyViewHolder viewHolder = ((MyViewHolder) holder);

          /*  new getData(((MyViewHolder) holder),model,position).execute();*/

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

            viewHolder.addPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Add to playlist
                }
            });

            viewHolder.mainCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = (Activity) con;

                    Intent intent = new Intent(con,PlayerActivity.class);
                    intent.putExtra("youtubelink",new String[]{ model.getYtUrl() });
                    con.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                }
            });
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

   /* class getData extends AsyncTask<Void,Void,Void> {

        private MyViewHolder viewHolder;
        private DiscoverModel model;
        String json; int pos;

        public getData(MyViewHolder viewHolder, DiscoverModel model, int position) {
            this.viewHolder = viewHolder;
            this.model = model;
            this.pos = position;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void aVoid) {
            if (json!=null) {
                try {

                    JSONObject jsonObject = new JSONObject(json);


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
          *//*  HttpHandler handler = new HttpHandler();
            json = handler.makeServiceCall(
                    "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="+YTutils.getVideoID(model.getYtUrl()) +"&format=json");
         *//*   return null;
        }
    }*/

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