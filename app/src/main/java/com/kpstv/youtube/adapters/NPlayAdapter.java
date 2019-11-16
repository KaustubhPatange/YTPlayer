package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.NPlaylistActivity;
import com.kpstv.youtube.PlayerActivity2;
import com.kpstv.youtube.R;
import com.kpstv.youtube.helper.ItemTouchHelperAdapter;
import com.kpstv.youtube.helper.ItemTouchHelperViewHolder;
import com.kpstv.youtube.helper.OnStartDragListener;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.EqualizerView;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import org.mozilla.javascript.tools.jsc.Main;

import java.util.ArrayList;
import java.util.Collections;

public class NPlayAdapter extends RecyclerView.Adapter<NPlayAdapter.MyViewHolder> implements ItemTouchHelperAdapter {

    private ArrayList<NPlayModel> models;
    Context con;
    OnClickListener onClickListener;
    OnCheckBoxListener onCheckBoxListener;
    private OnStartDragListener mDragStartListener;
    int accentColor;

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
       try {
           Collections.swap(models, fromPosition, toPosition);
           notifyItemMoved(fromPosition, toPosition);
       }catch (Exception e){e.printStackTrace();}
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public boolean onItemMoved(int fromPosition, int toPosition) {
        Log.e("onItemMoved","true");
        MainActivity.nPlayModels = models;
      //  Collections.swap(MainActivity.yturls,fromPosition,toPosition);
        MainActivity.yturls.clear();
        StringBuilder builder = new StringBuilder();
        for (int i=0;i<models.size();i++){
            MainActivity.yturls.add(models.get(i).getUrl());
            if (models.get(i).is_playing()) {
                MainActivity.ytIndex = i;
            }
            models.get(i).set_selected(false);
            builder.append(YTutils.getVideoTitle(models.get(i).getModel().getVideMeta().getTitle())).append("\n");
        }
        Log.e("ItemState","\n"+builder.toString());
        notifyDataSetChanged();
        return true;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        TextView titleText, authorText;
        ImageButton moveButton;
        ConstraintLayout layout;
        CheckBox checkBox; EqualizerView equalizerView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(R.id.aTitle);
            this.authorText = itemView.findViewById(R.id.aAuthor);
            this.moveButton = itemView.findViewById(R.id.aMoveButton);
            this.layout = itemView.findViewById(R.id.mainlayout);
            this.checkBox = itemView.findViewById(R.id.aCheckBox);
            this.equalizerView = itemView.findViewById(R.id.equalizer);

            itemView.setOnLongClickListener(view -> {
                // Blank listener
                return true;
            });
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onItemMoved(int from, int to) {
           // NPlaylistActivity.performSwap(from,to);
            Log.e("ItemSwappedComplete","from: "+from+", to: "+to);
        }

    }



    public NPlayAdapter(ArrayList<NPlayModel> data, Context context, OnStartDragListener dragStartListener) {
        this.con = context;
        this.models = data;
        this.mDragStartListener = dragStartListener;
        accentColor = ContextCompat.getColor(con,R.color.colorAccent);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nplaylist_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final NPlayModel nPlayModel = models.get(listPosition);

        if (nPlayModel.is_playing()) {
            holder.equalizerView.setVisibility(View.VISIBLE);
            holder.equalizerView.animateBars();
        }else {
            holder.equalizerView.setVisibility(View.INVISIBLE);
            holder.equalizerView.stopBars();
        }

        if (nPlayModel.is_selected()) {
            holder.checkBox.setChecked(true);
        }else holder.checkBox.setChecked(false);

        YTMeta meta = nPlayModel.getModel();
        if (meta.getVideMeta()!=null) {

            holder.moveButton.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                  try {
                      mDragStartListener.onStartDrag(holder);
                  }catch (Exception e){e.printStackTrace();}
                }
                return false;
            });

            holder.titleText.setText(YTutils.getVideoTitle(meta.getVideMeta().getTitle()));
            holder.authorText.setText(YTutils.getChannelTitle(meta.getVideMeta().getTitle(),meta.getVideMeta().getAuthor()));

            holder.checkBox.setOnClickListener(view -> {
                onCheckBoxListener.OnSingleClicked(holder.checkBox,listPosition,nPlayModel,holder);
            });

            holder.layout.setOnClickListener(v1->{
                onClickListener.OnSingleClicked(holder.layout,listPosition,nPlayModel,holder);
            });
        }

        if (listPosition+1==MainActivity.yturls.size()) {
            MainActivity.nPlayModels = models;
        }
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public interface OnClickListener {
        void OnSingleClicked(View view, int position, NPlayModel model, NPlayAdapter.MyViewHolder holder);
    }

    public interface OnCheckBoxListener {
        void OnSingleClicked(View view, int position, NPlayModel model, NPlayAdapter.MyViewHolder holder);
    }

    public void setOnCheckClickListener(OnCheckBoxListener listener) {
        onCheckBoxListener = listener;
    }

    public void setOnSingleClickListener(OnClickListener listener) {
        onClickListener = listener;
    }
}

