package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kpstv.youtube.R;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.YTConfig;
//import com.kpstv.youtube.services.DownloadService;
import com.kpstv.youtube.services.IntentDownloadService;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.MyViewHolder> {

    ArrayList<YTConfig> models;
    Context context;
    int accentColor;

    public DownloadAdapter(ArrayList<YTConfig> models, Context context) {
        this.models = models;
        this.context = context;
        accentColor = ContextCompat.getColor(context,R.color.colorAccent);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.download_item,viewGroup,false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int pos) {
        final YTConfig model = models.get(pos);

        switch (model.getTaskExtra()) {
            case "mp3task":
                myViewHolder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_audio_download));
                break;
            case "mergetask":
                myViewHolder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_movie_download));
                break;
        }


        PopupMenu popupMenu = new PopupMenu(context,myViewHolder.imageMore);
        popupMenu.inflate(R.menu.service_menu);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.cancel_action:
                    IntentDownloadService.pendingJobs.remove(model);
                    notifyDataSetChanged();
                    break;
            }
            return true;
        });

        myViewHolder.imageMore.setOnTouchListener((v, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    popupMenu.show();

                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        myViewHolder.titleText.setText(model.getTitle());
        myViewHolder.sizeText.setText(model.getChannelTitle());
        myViewHolder.percentText.setVisibility(View.GONE);
        myViewHolder.progressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_RIGHT);
        myViewHolder.progressBar.setIndeterminateMode(true);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, sizeText, percentText;
        ImageView imageMore, imageView;
        CircularProgressBar progressBar;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.progressBar = itemView.findViewById(R.id.DProgress);
            this.imageView = itemView.findViewById(R.id.DImage);
            this.imageMore = itemView.findViewById(R.id.DMore);
            this.titleText = itemView.findViewById(R.id.DTitle);
            this.percentText = itemView.findViewById(R.id.DPercentText);
            this.sizeText = itemView.findViewById(R.id.DSizeText);

        }
    }
}
