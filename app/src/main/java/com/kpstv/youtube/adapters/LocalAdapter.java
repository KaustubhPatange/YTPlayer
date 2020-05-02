package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.LocalModel;
import com.kpstv.youtube.utils.ArtistImage;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;
import java.util.ArrayList;

public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.LocalHolder> {
    Activity context; boolean isAlbumAdapter;
    ArrayList<LocalModel> models;
    private static final String TAG = "LocalAdapter";
    SingleClickListener singleClickListener;
    LongClickListener longClickListener;

    public LocalAdapter(Activity context, ArrayList<LocalModel> models,boolean albumAdapter) {
        this.context = context;
        this.models = models;
        this.isAlbumAdapter = albumAdapter;
    }

    @NonNull
    @Override
    public LocalHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.artist_item, viewGroup, false);
        return new LocalHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(@NonNull LocalHolder localHolder, int i) {
        final LocalModel model = models.get(i);
        if (isAlbumAdapter)
            localHolder.aAlbumText.setVisibility(View.GONE);
        else {
            int s = model.getAlbumCount();
            if (s==1)
                localHolder.aAlbumText.setText("1 album");
            else localHolder.aAlbumText.setText(s+" albums");
        }

        localHolder.aTitle.setText(model.getTitle());

        if (model.getSongList().size()==1)
            localHolder.aSongText.setText("1 song");
        else  localHolder.aSongText.setText(model.getSongList().size()+" songs");


        localHolder.cardView.setOnClickListener(view -> singleClickListener.onSingleClick(view,model,i));

        localHolder.moreButton.setOnClickListener(view -> longClickListener.onLongClick(view,model,i));

        if (model.getTitle().equals("Unknown artist")||model.getTitle().equals("Unknown album")) return;
        new AsyncTask<Void,Void,Void>() {
           String imageUri;
            @Override
            protected void onPostExecute(Void aVoid) {
                if (isAlbumAdapter) {
                    if (model.getSongList().size()>0) {
                        try {
                            String line = model.getSongList().get(0);
                            String filePath = line.split("\\|")[0];
                            File f = new File(filePath);
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(context, Uri.fromFile(f));

                            byte[] data = mmr.getEmbeddedPicture();

                            if (data!=null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                localHolder.aImage.setImageBitmap(bitmap);
                                Palette.from(bitmap).generate(palette -> {
                                    int color = palette.getVibrantColor(context.getResources().getColor(R.color.background));
                                    localHolder.cardView.setBackgroundTintList(ColorStateList.valueOf(color));
                                });
                            }
                        }catch (Exception e) {
                            Log.e(TAG, "onPostExecute: "+e.getMessage());
                        }

                    }
                }else if (imageUri!=null) {
                    Glide.with(context.getApplicationContext()).asBitmap().load(imageUri).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            localHolder.aImage.setImageBitmap(resource);

                            Palette.from(resource).generate(palette -> {
                                int color = palette.getVibrantColor(context.getResources().getColor(R.color.background));
                                localHolder.cardView.setBackgroundTintList(ColorStateList.valueOf(color));
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });

                }
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (isAlbumAdapter) {
                    return null;
                }
                imageUri = YTutils.getLocalArtworkImage(context,model);
                return null;
            }
        }.execute();
    }

    public interface LongClickListener {
        void onLongClick(View view, LocalModel model, int position);
    }

    public interface SingleClickListener {
        void onSingleClick(View view, LocalModel model, int position);
    }

    public void setSingleClickListener(SingleClickListener singleClickListener) {
        this.singleClickListener = singleClickListener;
    }

    public void setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class LocalHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView aTitle, aSongText,aAlbumText;
        ImageView moreButton, aImage;
        public LocalHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            aTitle = itemView.findViewById(R.id.aTitle);
            aSongText = itemView.findViewById(R.id.aSongCount);
            aAlbumText = itemView.findViewById(R.id.aAlbumText);
            moreButton = itemView.findViewById(R.id.aMoreButton);
            aImage = itemView.findViewById(R.id.aImage);
        }
    }
}
