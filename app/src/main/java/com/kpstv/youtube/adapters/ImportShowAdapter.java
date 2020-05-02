package com.kpstv.youtube.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.ImportShowModel;

import java.util.ArrayList;

public class ImportShowAdapter extends RecyclerView.Adapter<ImportShowAdapter.ImportShowHolder> {

    Context context;
    ArrayList<ImportShowModel> models;

    public ImportShowAdapter(Context context, ArrayList<ImportShowModel> models) {
        this.context = context;
        this.models = models;
    }

    @NonNull
    @Override
    public ImportShowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_import_show,viewGroup,false);
        return new ImportShowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImportShowHolder holder, int i) {
        final ImportShowModel model = models.get(i);

        holder.title.setText(model.getTitle());
        holder.author.setText(model.getAuthor());

        Glide.with(context.getApplicationContext()).asBitmap().load(model.getImageUrl()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                holder.image.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    static class ImportShowHolder extends RecyclerView.ViewHolder {
        TextView title,author;
        ImageView image;
        public ImportShowHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            image = itemView.findViewById(R.id.imageView);
        }
    }
}
