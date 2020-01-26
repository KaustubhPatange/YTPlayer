package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kpstv.youtube.R;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.services.IntentDownloadService;
import com.kpstv.youtube.utils.YTutils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DownloadAdpater2 extends RecyclerView.Adapter<DownloadAdpater2.DownloadHolder> {

    private static final String TAG = "DownloadAdpater2";
    private ArrayList<YTConfig> configs;
    private Context context;
    private DownloadClickListener listener;

    public DownloadAdpater2(ArrayList<YTConfig> configs, Context context) {
        this.configs = configs;
        this.context = context;
    }

    @NonNull
    @Override
    public DownloadHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_download,viewGroup,false);
        return new DownloadHolder(v);
    }

    @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull DownloadHolder holder, int i) {
        final YTConfig config = configs.get(i);

        String title = config.getText();
        title = title.replace("Video ","");
        title = title.replace("Audio ","");

        holder.btn_format.setText(config.getExt());
        holder.txt_bits.setText(title);

        holder.cardView.setOnClickListener(view -> {
            listener.onClick(config,i);
        });

        holder.btn_format.setOnClickListener(view -> {
            String text = holder.btn_format.getText().toString().toLowerCase();
            if (text.equals("mp3")) {
                holder.btn_format.setText("m4a");
            }else if (text.equals("m4a")) {
                holder.btn_format.setText("mp3");
            }
            setButtonColor(holder.btn_format);
        });

        setButtonColor(holder.btn_format);

        new AsyncTask<Void,Void,Void>(){
            long size=0;

            @Override
            protected void onPostExecute(Void aVoid) {
                holder.txt_size.setText(YTutils.getSize(size));
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    size = YTutils.getFileSize(new URL(config.getUrl()));
                } catch (MalformedURLException e) {
                    Log.e(TAG, "doInBackground: "+e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public interface DownloadClickListener {
        void onClick(YTConfig model, int pos);
    }

    public void setListener(DownloadClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return configs.size();
    }

    private void setButtonColor(Button v) {
        switch (v.getText().toString()) {
            case "mp3":
                v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(v.getContext(),R.color.colorAccent)));
                break;

            case "m4a":
                v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(v.getContext(),R.color.blue)));
                break;

            case "mp4":
                v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(v.getContext(),R.color.playback_indicator)));
                v.setTextColor(ContextCompat.getColor(v.getContext(),R.color.background));
                break;
        }
    }

    public class DownloadHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView txt_bits, txt_size; Button btn_format;
        public DownloadHolder(@NonNull View itemView) {
            super(itemView);
            btn_format = itemView.findViewById(R.id.download_format);
            cardView = itemView.findViewById(R.id.cardView);
            txt_bits = itemView.findViewById(R.id.download_title);
            txt_size = itemView.findViewById(R.id.download_size);
        }
    }
}
