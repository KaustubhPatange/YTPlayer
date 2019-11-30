package com.kpstv.youtube.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity2;
import com.kpstv.youtube.R;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;
import java.util.Arrays;

public class PlayerAdapter extends PagerAdapter {

    Context context;
    ArrayList<String> yturls;
    LayoutInflater mLayoutInflater;

    public PlayerAdapter(Context context, ArrayList<String> yturls) {
        this.context = context;
        this.yturls = yturls;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return yturls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.player_item, container, false);

        ImageView imageView = itemView.findViewById(R.id.mainImage);

        Glide.with(context)
                .asBitmap()
                .load(YTutils.getImageUrl(yturls.get(position)))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Palette.generateAsync(resource, palette -> {
                            /*MainActivity.bitmapIcon = resource;
                            MainActivity.nColor = palette.getVibrantColor(context.getResources().getColor(R.color.light_white));*/
                            imageView.setImageBitmap(resource);

                        });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        container.addView(itemView);

        return itemView;
    }

    public void getImage() {

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}
