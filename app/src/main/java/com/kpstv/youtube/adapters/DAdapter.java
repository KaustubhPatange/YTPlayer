package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.DModel;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

public class DAdapter extends RecyclerView.Adapter<DAdapter.DHolder> {

    private Context context;
    private ArrayList<DModel> models;
    private DAdpaterListener listener;

    public DAdapter(Context context, ArrayList<DModel> models) {
        this.context = context;
        this.models = models;
    }

    @NonNull
    @Override
    public DHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = (LayoutInflater) viewGroup.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_dplaylist,viewGroup,false);
        return new DHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DHolder holder, int i) {
        final DModel dModel = models.get(i);

        holder.checkBox.setOnCheckedChangeListener(null);

        if (dModel.isChecked()) {
            holder.checkBox.setChecked(true);
        }else holder.checkBox.setChecked(false);

        holder.title.setText(dModel.getTitle());

        if (dModel.getSeconds()>0) {
            holder.subtitle.setText(YTutils.milliSecondsToTimer(dModel.getSeconds()*1000)+" "+
                    Html.fromHtml("&#8226;")+" "+dModel.getSubtitle());
        }else
            holder.subtitle.setText(dModel.getSubtitle());

        Glide.with(context).asBitmap().load(dModel.getImageUrl()).into(new CustomTarget<Bitmap>(){
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                holder.imageView.setImageBitmap(resource);
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

        holder.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            listener.onClick(dModel,i);
        });

        holder.layout.setOnClickListener(view -> listener.onClick(dModel,i));
    }

    public interface DAdpaterListener {
        void onClick(DModel model,int pos);
    }

    public void setListener(DAdpaterListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class DHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout layout;
        private CheckBox checkBox;
        private ImageView imageView;
        private TextView title,subtitle;
        public DHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.mainLayout);
            checkBox = itemView.findViewById(R.id.checkBox);
            imageView = itemView.findViewById(R.id.imageView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
        }
    }
}
