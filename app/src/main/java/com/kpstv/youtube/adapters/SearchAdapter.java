package com.kpstv.youtube.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;
import java.util.Collections;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private ArrayList<SearchModel> dataSet;
    private ArrayList<String> yturls;
    Context con; boolean isLibraryFrag=false;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;
        ImageView imageView;
        CardView mainCard;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(R.id.sTitle);
            this.imageView = itemView.findViewById(R.id.sImage);
            this.mainCard = itemView.findViewById(R.id.cardView);
        }

    }

    public SearchAdapter(ArrayList<SearchModel> data, Context context) {
        this.dataSet = data;
        this.con = context;
        yturls = new ArrayList<>();
        for (SearchModel model: data)
            yturls.add(model.getYturl());
    }


    public SearchAdapter(ArrayList<SearchModel> data, Context context, boolean isLibraryFrag) {
        this.dataSet = data;
        this.con = context;
        yturls = new ArrayList<>();
        for (SearchModel model: data)
            yturls.add(0,model.getYturl());
        this.isLibraryFrag = isLibraryFrag;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final SearchModel searchModel = dataSet.get(listPosition);

        holder.titleText.setText(searchModel.getTitle());

        Glide.with(con)
                .asBitmap()
                .load(searchModel.getImageUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        holder.imageView.setImageBitmap(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });


        if (isLibraryFrag) {
            holder.mainCard.setOnClickListener(v -> {
                if (YTutils.isInternetAvailable()) {
                    ArrayList<String> urls = (ArrayList<String>) yturls.clone();
                    Collections.reverse(urls);
                    MainActivity.PlayVideo(YTutils.ConvertToStringArray(urls), listPosition);
                }else Toast.makeText(con, "No active internet connection!", Toast.LENGTH_SHORT).show();
            });
        }else {
            holder.mainCard.setOnClickListener(v -> {
                if (YTutils.isInternetAvailable())
                MainActivity.PlayVideo(YTutils.ConvertToStringArray(yturls),9-listPosition);
                else
                    Toast.makeText(con, "No active internet connection!", Toast.LENGTH_SHORT).show();
            });
        }


    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

