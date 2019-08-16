package com.kpstv.youtube;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kpstv.youtube.utils.YTLength;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class HistoryBottomSheet extends BottomSheetDialogFragment {

    LinearLayout addToPlaylist, removeList, copyLink, shareButton, mainLayout, watchOnYouTube;
    String ytUrl, Title; BottomSheetListener mListener; ProgressBar progressBar;
    Activity activity; int position;
    long ytseconds; View v; TextView title_TextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.history_bottom_sheet,container,false);

        activity = getActivity();

        addToPlaylist = v.findViewById(R.id.baddToPlaylist);
        removeList = v.findViewById(R.id.bremoveFromHistory);
        copyLink = v.findViewById(R.id.bcopyLink);
        shareButton = v.findViewById(R.id.bshare);
        mainLayout = v.findViewById(R.id.mainlayout);
        progressBar = v.findViewById(R.id.progressBar);
        title_TextView = v.findViewById(R.id.btitle);
        watchOnYouTube = v.findViewById(R.id.bwatch);

        if (getTag().equals("discover"))
            removeList.setVisibility(View.GONE);

        ChangetoDefaultColor(R.id.baddToPlaylist_textView);
        ChangetoDefaultColor(R.id.bwatch_textView);
        ChangetoDefaultColor(R.id.bcopyLink_textView);
        ChangetoDefaultColor(R.id.bshare_textView);
        ChangetoDefaultColor(R.id.bremoveFromHistory_textView);

        position = getArguments().getInt("pos",0);
        Title = getArguments().getString("title");
        ytUrl = getArguments().getString("yturl");
        title_TextView.setText(Title);

        new getLength(ytUrl).execute();

        return v;
    }

    // Since BottomSheetDialog is not able to call default app theme
    // I need to manually set it here
    void ChangetoDefaultColor(int id) {
        TextView textView = v.findViewById(id);
        textView.setTextColor(getResources().getColor(R.color.light_white));
    }


    public interface BottomSheetListener {
        void onRemoveFromHistory(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }

    class getLength extends AsyncTask<Void,Void,Void> {

        String ytUrl;

        public getLength(String ytUrl) {
            this.ytUrl = ytUrl;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            copyLink.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(ytUrl);
                Toast.makeText(activity, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                dismiss();
            });

            watchOnYouTube.setOnClickListener(v1 -> {
                YTutils.StartURLIntent(ytUrl,activity);
                dismiss();
            });

            shareButton.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,ytUrl);
                startActivity(Intent.createChooser(shareIntent, "Share using..."));
                dismiss();
            });

            addToPlaylist.setOnClickListener(v1 -> {
                YTutils.addToPlayList(activity,ytUrl,ytseconds);
                dismiss();
            });

            removeList.setOnClickListener(v1 -> {
                mListener.onRemoveFromHistory(position);
                dismiss();
            });

            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            YTLength ytLength = new YTLength(YTutils.getVideoID(ytUrl));
            ytseconds = ytLength.getSeconds();
            return null;
        }
    }

    /*void addToPlayList() {
        String playlist_csv = YTutils.readContent(activity,"playlist.csv");
        if (playlist_csv==null&&playlist_csv.isEmpty()) return;
        ArrayList<String> configs = new ArrayList<>();

        String[] allPlaylist = playlist_csv.split("\n|\r");

        for(String line : allPlaylist) {
            String[] playlist = line.split(",");
            configs.add(playlist[1]);
        }
        final String[] arrays = new String[configs.size()];
        for(int i=0;i<configs.size();i++) {
            arrays[i]=configs.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add to playlist");

        builder.setItems(arrays, (dialog, which) -> {
            String name = configs.get(which);
            for (int i=0;i<configs.size();i++) {
                String videoID = YTutils.getVideoID(ytUrl);
                if (allPlaylist[i].contains(","+name) && !allPlaylist[i].contains(videoID+"|")) {

                    YTutils.writeContent(activity,"playlist.csv",
                            YTutils.join(allPlaylist,'\n'));
                    Toast.makeText(activity, "Added to playlist", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }*/
}
