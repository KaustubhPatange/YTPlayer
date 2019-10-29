package com.kpstv.youtube.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
    String pline; View.OnClickListener listener;

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

    public PlaylistAdapter(ArrayList<PlaylistModel> data, Context context, View.OnClickListener listener) {
        this.dataSet = data;
        this.con = context;
        this.listener = listener;
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

        holder.mainCard.setOnClickListener(listener);

        String playlist_csv = YTutils.readContent((Activity) con,"playlist.csv");

        holder.imageMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(con,v);
            popupMenu.inflate(R.menu.playlist_context);
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.action_open:
                        listener.onClick(holder.mainCard);
                        break;
                    case R.id.action_modify:
                        if (playlist_csv!=null&&!playlist_csv.isEmpty()) {
                            String[] lines = playlist_csv.split("\r|\n");
                            for(int i=0;i<lines.length;i++) {
                                if (lines[i].contains(","+playlistModel.getTitle())) {
                                    Intent intent = new Intent(con,CPlaylistActivity.class);
                                    intent.putExtra("line",lines[i]);
                                    con.startActivity(intent);
                                }
                            }
                        }

                        break;
                    case R.id.action_delete:
                        if (playlist_csv!=null&&!playlist_csv.isEmpty()) {
                            ArrayList<String> lines = new ArrayList<>(Arrays.asList(playlist_csv.split("\r|\n")));
                            for(int i=0;i<lines.size();i++) {
                                if (lines.get(i).contains(","+playlistModel.getTitle())) {
                                    dataSet.remove(i);
                                    lines.remove(i);
                                    YTutils.writeContent((Activity)con,"playlist.csv",
                                            YTutils.convertListToStringMethod(lines));
                                    PlaylistFragment.loadRecyclerAgain();
                                }
                            }
                        }
                        break;
                }
                return false;
            });
        });
    }

    @Override
    public int getItemCount() {

        return dataSet.size();
    }
}
