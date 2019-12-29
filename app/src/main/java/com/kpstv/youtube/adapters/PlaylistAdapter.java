package com.kpstv.youtube.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kpstv.youtube.CPlaylistActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.fragments.PlaylistFragment;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;
import java.util.Arrays;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {

    private ArrayList<PlaylistModel> dataSet;
    private ArrayList<String> Dateset;
    Context con;
    LongClickListener longClickListener;
    SingleClickListener singleClickListener;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;
        TextView SongText;
        TextView FrameText;
        TextView DateText;
        CardView mainCard;
        ImageView imageMore;
        FrameLayout frameLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(R.id.pTitle);
            this.SongText = itemView.findViewById(R.id.pSongText);
            this.FrameText = itemView.findViewById(R.id.pFrameText);
            this.DateText = itemView.findViewById(R.id.pDate);
            this.mainCard = itemView.findViewById(R.id.mainCardView);
            this.imageMore = itemView.findViewById(R.id.pimageMore);
            this.frameLayout = itemView.findViewById(R.id.pFrame);
        }
    }

    public PlaylistAdapter(ArrayList<PlaylistModel> data, Context context) {
        this.dataSet = data;
        this.con = context;
        Dateset = new ArrayList<>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final PlaylistModel playlistModel = dataSet.get(listPosition);
        holder.titleText.setText(playlistModel.getTitle());
        holder.SongText.setText(playlistModel.getSongCountText() + "  "+ Html.fromHtml("&#8226;") + "  " +
                YTutils.milliSecondsToTimer(playlistModel.getTimeseconds()*1000));
        String letter = Character.toUpperCase(playlistModel.getTitle().charAt(0))+"";
        holder.FrameText.setText(letter);

        String toput = playlistModel.getDate();
        String yesterdayDate = YTutils.getYesterdayDate();
        String todayDate = YTutils.getTodayDate();

        if (toput.contains(yesterdayDate))
            toput = "Yesterday";
        else if (toput.contains(todayDate))
            toput = "Today";
        holder.DateText.setText(toput);

        if (!Dateset.contains(playlistModel.getDate()))
        {
            holder.DateText.setVisibility(View.VISIBLE);
            Dateset.add(playlistModel.getDate());
        }


        holder.mainCard.setTag(playlistModel);

        holder.mainCard.setOnClickListener(view -> singleClickListener.onSingleClick(view,playlistModel,listPosition));

        holder.imageMore.setOnClickListener(v -> longClickListener.onLongClick(v,playlistModel,listPosition));

        holder.mainCard.setOnLongClickListener(v -> {
            longClickListener.onLongClick(v,playlistModel,listPosition);
            return true;
        });
    }

    public void setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setSingleClickListener(SingleClickListener singleClickListener) {
        this.singleClickListener = singleClickListener;
    }

    public void performSingleClick(View v,PlaylistModel playlistModel, int position) {
        singleClickListener.onSingleClick(v,playlistModel,position);
    }

    private static final String TAG = "PlaylistAdapter";

    public interface LongClickListener {
        void onLongClick(View v, PlaylistModel model, int position);
    }

    public interface SingleClickListener {
        void onSingleClick(View v, PlaylistModel model, int position);
    }


    @Override
    public int getItemCount() {

        return dataSet.size();
    }
}
