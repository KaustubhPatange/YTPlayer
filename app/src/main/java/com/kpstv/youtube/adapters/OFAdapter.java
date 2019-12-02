package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kpstv.youtube.R;
import com.kpstv.youtube.models.OFModel;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class OFAdapter extends RecyclerView.Adapter<OFAdapter.OFHolder> {

    Context context;
    ArrayList<OFModel> models;
    LongClickListener longClickListener;
    SingleClickListener singleClickListener;
    int accentcolor; boolean localMusic=false;

    public OFAdapter(Context context, ArrayList<OFModel> models) {
        this.context = context;
        this.models = models;
        accentcolor = ContextCompat.getColor(context,R.color.colorAccent);
    }

    public OFAdapter(Context context, ArrayList<OFModel> models,boolean localMusic) {
        this.context = context;
        this.models = models;
        this.localMusic = localMusic;
        accentcolor = ContextCompat.getColor(context,R.color.colorAccent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull OFHolder ofHolder, int i) {
        final OFModel ofModel = models.get(i);

        File f = new File(ofModel.getPath());

        ofHolder.constraintLayout.setOnClickListener(view -> {
            singleClickListener.onSingleClick(view,ofModel,i);
        });

        ofHolder.MoreButton.setOnTouchListener((v, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView ) v;
                    view.setColorFilter(accentcolor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    longClickListener.onLongClick(v,ofModel,i);

                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        ofHolder.constraintLayout.setOnLongClickListener(view -> {
            longClickListener.onLongClick(view,ofModel,i);
            return true;
        });

        if (localMusic) {
            ofHolder.mainImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play));
            ofHolder.Title.setText(f.getName().split("\\.")[0]);
            ofHolder.Path.setText(ofModel.getTitle());
            ofHolder.SongText.setText(YTutils.getDuration(ofModel.getSongCount()*1000));
        }else {
            ofHolder.Title.setText(ofModel.getTitle());
            ofHolder.Path.setText(f.getParent());
            int count = ofModel.getSongCount();

            if (count==1) {
                ofHolder.SongText.setText("1 song");
            }else ofHolder.SongText.setText(count+" songs");
        }

    }


    public void setLongClickListener(LongClickListener listener) {
        longClickListener = listener;
    }

    public void setSingleClickListener(SingleClickListener listener) {
        singleClickListener = listener;
    }

    public interface LongClickListener {
        void onLongClick(View v, OFModel model, int position);
    }

    public interface SingleClickListener {
        void onSingleClick(View v, OFModel model, int position);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @NonNull
    @Override
    public OFHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.folder_item, viewGroup, false);

        return new OFHolder(view);
    }

    public static class OFHolder extends RecyclerView.ViewHolder {

        TextView Title,Path,SongText;
        ImageView MoreButton,mainImage;
        ConstraintLayout constraintLayout;

        public OFHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.fConstraintLayout);
            Title = itemView.findViewById(R.id.fTitle);
            Path = itemView.findViewById(R.id.fPath);
            SongText = itemView.findViewById(R.id.fSongText);
            mainImage = itemView.findViewById(R.id.imageView1);
            MoreButton = itemView.findViewById(R.id.fMoreButton);
            Title = itemView.findViewById(R.id.fTitle);
        }
    }
}
