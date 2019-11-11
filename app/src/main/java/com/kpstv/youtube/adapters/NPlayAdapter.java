package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.kpstv.youtube.PlayerActivity2;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.EqualizerView;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import org.mozilla.javascript.tools.jsc.Main;

import java.util.ArrayList;

public class NPlayAdapter extends RecyclerView.Adapter<NPlayAdapter.MyViewHolder> {

    private ArrayList<NPlayModel> models;
    Context con;
    OnLongClickListener onlonglistener;
    OnMoreButtonClick onmoreclick;
    OnClickListener onClickListener;
    OnCheckBoxListener onCheckBoxListener;
    int accentColor;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, authorText;
        ImageButton moreButton, moveButton;
        ConstraintLayout layout;
        CheckBox checkBox; EqualizerView equalizerView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(R.id.aTitle);
            this.authorText = itemView.findViewById(R.id.aAuthor);
            this.moreButton = itemView.findViewById(R.id.aMoreButton);
            this.moveButton = itemView.findViewById(R.id.aMoveButton);
            this.layout = itemView.findViewById(R.id.mainlayout);
            this.checkBox = itemView.findViewById(R.id.aCheckBox);
            this.equalizerView = itemView.findViewById(R.id.equalizer);
        }

    }

    public NPlayAdapter(ArrayList<NPlayModel> data, Context context) {
        this.con = context;
        this.models = data;
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
            holder.titleText.setText(YTutils.getVideoTitle(meta.getVideMeta().getTitle()));
            holder.authorText.setText(YTutils.getChannelTitle(meta.getVideMeta().getTitle(),meta.getVideMeta().getAuthor()));

            holder.checkBox.setOnClickListener(view -> {
                onCheckBoxListener.OnSingleClicked(holder.checkBox,listPosition,nPlayModel,holder);
            });

            holder.layout.setOnLongClickListener(v1->{
                onlonglistener.onLongClicked(holder.layout,listPosition,nPlayModel,holder);
                return true;
            });

            holder.layout.setOnClickListener(v1->{
                onClickListener.OnSingleClicked(holder.layout,listPosition,nPlayModel,holder);
            });

            holder.moreButton.setOnTouchListener((v, motionEvent) -> {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageButton view = (ImageButton ) v;
                        view.setColorFilter(accentColor);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:

                        onmoreclick.OnMoreButtonClicked(holder.moreButton,listPosition,nPlayModel,holder);

                    case MotionEvent.ACTION_CANCEL: {
                        ImageButton view = (ImageButton) v;
                        view.clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }
                return true;
            });
        }

        if (listPosition+1==MainActivity.yturls.size()) {
            MainActivity.nPlayModels = models;
            Log.e("Datahasbeenset","true");
        }

       //  new getData(con,nPlayModel,holder,listPosition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public interface OnLongClickListener {
        void onLongClicked(View view, int position, NPlayModel model, NPlayAdapter.MyViewHolder holder);
    }

    public interface OnMoreButtonClick {
        void OnMoreButtonClicked(View view,  int position, NPlayModel model, NPlayAdapter.MyViewHolder holder);
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
    public void setOnLongClickListener(OnLongClickListener listener) {
        onlonglistener = listener;
    }
    public void setOnSingleClickListener(OnClickListener listener) {
        onClickListener = listener;
    }
    public void setOnMoreClickListener(OnMoreButtonClick listener) {
        onmoreclick = listener;
    }
}

