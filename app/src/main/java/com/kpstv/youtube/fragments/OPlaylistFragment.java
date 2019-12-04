package com.kpstv.youtube.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.Transition;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kpstv.youtube.EditTagActivity;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.OFAdapter;
import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.models.OFModel;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import org.mozilla.javascript.tools.jsc.Main;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;

public class OPlaylistFragment extends Fragment {

    View v;
    boolean isnetworkCreated; Toolbar toolbar;
    RecyclerView recyclerView; ArrayList<String> yturls;
    SongAdapter adapter; ArrayList<DiscoverModel> models;
    FragmentActivity activity;
    RecyclerView.LayoutManager layoutManager;
    ImageView oImageView;
    TextView TitleText,SongCountText,TimeText,songText,pathLocation;
    ProgressBar progressBar; PlaylistModel playlistModel; OFModel ofModel;
    FloatingActionButton playFab; boolean localMusic=false;
    OFAdapter ofadapter;
    ArrayList<OFModel> ofModels;
    private static final String TAG = "OPlayListFragment";

    public OPlaylistFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isnetworkCreated) {
            Log.e(TAG, "onCreateView: Calling Parent");
            v = inflater.inflate(R.layout.fragment_oplaylist, container, false);
            activity = getActivity();

            toolbar = v.findViewById(R.id.toolbar);

            models = new ArrayList<>();
            ofModels = new ArrayList<>();
            yturls = new ArrayList<>();
            recyclerView = v.findViewById(R.id.my_recycler_view);
            TitleText = v.findViewById(R.id.oTitle);
            oImageView = v.findViewById(R.id.oImageView);
            pathLocation = v.findViewById(R.id.oPathLocation);
            SongCountText = v.findViewById(R.id.oSongText);
            TimeText = v.findViewById(R.id.oTime);
            songText = v.findViewById(R.id.songText);
            playFab = v.findViewById(R.id.oPlayFAB);
            progressBar = v.findViewById(R.id.progressBar);
            layoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(layoutManager);

            final CollapsingToolbarLayout collapsingToolbarLayout = v.findViewById(R.id.toolbar_layout);
            AppBarLayout appBarLayout = v.findViewById(R.id.app_bar);
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = true;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0) {
                        if (playlistModel!=null) {
                            toolbar.setTitle(playlistModel.getTitle());
                            collapsingToolbarLayout.setTitle(playlistModel.getTitle());
                        }else {
                            pathLocation.setVisibility(View.GONE);
                            oImageView.setVisibility(View.INVISIBLE);
                            toolbar.setTitle(ofModel.getTitle());
                            collapsingToolbarLayout.setTitle(ofModel.getTitle());
                        }
                        isShow = true;
                    } else if(isShow) {
                        if (ofModel!=null)
                        {
                            oImageView.setVisibility(View.VISIBLE);
                            pathLocation.setVisibility(View.VISIBLE);
                        }
                        toolbar.setTitle(" "); //careful there should a space between double quote otherwise it wont work
                        collapsingToolbarLayout.setTitle(" "); //careful there should a space between double quote otherwise it wont work
                        isShow = false;
                    }
                }
            });

            Log.e(TAG, "onCreateView: Above Bundles" );

            Bundle args = getArguments();
            String isLocal = args.getString("isLocalMusic");
            if (isLocal!=null && isLocal.equals("true"))
            {
                Log.e(TAG, "onCreateView: Okay in localMusic" );
                localMusic = true;
                ofModel = (OFModel) args.getSerializable("model");
                String location = ofModel.getPath();
                File f = new File(activity.getFilesDir(),"locals/"+location.replace("/","_")+".csv");
                Log.e(TAG, "onCreateView: "+f.getPath());
                if (f.exists()) {
                    Log.e(TAG, "onCreateView: In File Exist bro" );
                    oImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_folder));
                    int song = ofModel.getSongCount();
                    TitleText.setText(ofModel.getTitle());
                    if (song==1)
                        SongCountText.setText(song+ " song");
                    else
                        SongCountText.setText(song+ " songs");
                    pathLocation.setText(new File(ofModel.getPath()).getParent());
                    pathLocation.setVisibility(View.VISIBLE);
                    Log.e(TAG, "onCreateView: "+ofModel.getDuration());
                    TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(ofModel.getDuration() * 1000)));
                    Log.e(TAG, "onCreateView: In localMusic" );
                    new getData_Offline(f.getPath()).execute();
                }else{
                    Log.e(TAG, "onCreateView: Not File Exist bro" );
                    Toast.makeText(MainActivity.activity, "Rescan library to scan songs", Toast.LENGTH_SHORT).show();
                }

            }else {
                Log.e(TAG, "onCreateView: In normal OPlayFrag" );
                playlistModel = (PlaylistModel) args.getSerializable("model");
                ArrayList<String> videos = playlistModel.getData();
                TitleText.setText(playlistModel.getTitle());
                SongCountText.setText(String.format("%s songs", videos.size()));
                TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(
                        playlistModel.getTimeseconds()*1000)));


                if (videos.size()>0) {
                    for (int i = 0; i < videos.size(); i++) {
                        String videoID = videos.get(i).split("\\|")[0];
                        yturls.add( YTutils.getYtUrl(videoID));
                    }

                    new getData().execute();
                }else {
                    progressBar.setVisibility(View.GONE);
                    songText.setText("NO SONG DATA");
                    songText.setVisibility(View.VISIBLE);
                }
            }

            toolbar.setNavigationOnClickListener(v1 -> {
                if (localMusic) {
                    MainActivity.loadLocalMusicFrag();
                    return;
                }
                if (MainActivity.loadedFavFrag)
                {
                    MainActivity.loadedFavFrag=false;
                    MainActivity.loadLibraryFrag();
                }
                else
                    MainActivity.loadPlayFrag();
            });
            
            isnetworkCreated = true;
        }
        return v;
    }


    private View.OnClickListener recyclerItemListener = view -> {
        int position = (int)view.getTag();
        PlayMusic(position);
    };

    void PlayMusic(int position) {
        if (yturls.size()==0) return;
        String[] videos =YTutils.ConvertToStringArray(yturls);
        MainActivity.PlayVideo(videos,position);
    }

    void PlayMusic_Offline(int position) {
        if (yturls.size()==0) return;
        String[] files =YTutils.ConvertToStringArray(yturls);
        MainActivity.PlayVideo_Local(files,position);
    }

    class getData_Offline extends AsyncTask<Void,Void,Void> {
        String filePath;
        int seconds=0;

        public getData_Offline(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (ofModels.size()>0) {
                progressBar.setVisibility(View.GONE);
                songText.setText("TRACKS");
                songText.setVisibility(View.VISIBLE);

                TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(seconds * 1000)));

                ofadapter = new OFAdapter(activity,ofModels,true);
                recyclerView.setAdapter(ofadapter);
                recyclerView.setVisibility(View.VISIBLE);

                playFab.setOnClickListener(v -> PlayMusic_Offline(0));

                ofadapter.setSingleClickListener((v1, model, position) -> {
                    PlayMusic_Offline(position);
                });

                ofadapter.setLongClickListener((v1, model, position) -> {
                    File f = new File(model.getPath());
                    Log.e(TAG, "onPostExecute: "+model.getPath());
                    PopupMenu popupMenu = new PopupMenu(activity,v1);
                    popupMenu.inflate(R.menu.local_popup_menu1);
                    popupMenu.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case R.id.action_play:
                                PlayMusic_Offline(position);
                                break;
                            case R.id.action_play_next:
                                if (MainActivity.yturls.size()==0)
                                {
                                    PlayMusic_Offline(position);
                                    return true;
                                }
                               insertPosition(model,MainActivity.ytIndex+1,false);
                                break;
                            case R.id.action_add_queue:
                                if (MainActivity.yturls.size()==0)
                                {
                                    PlayMusic_Offline(position);
                                    return true;
                                }
                                insertPosition(model,MainActivity.yturls.size(),true);
                                break;
                            case R.id.action_edit_tag:
                                if (MainActivity.videoID!=null && MainActivity.videoID.equals(model.getPath())) {
                                    Toast.makeText(activity, "Cannot edit tags when playing", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                                String extension = model.getPath().split("\\.")[1].toLowerCase();
                                if (extension.equals("mp3")) {
                                    Intent intent = new Intent(activity, EditTagActivity.class);
                                    intent.putExtra("model",model);
                                    startActivity(intent);
                                }else {
                                    Toast.makeText(activity, "Could not edit tags for ."+extension+" file type", Toast.LENGTH_SHORT).show();
                                }

                                break;
                            case R.id.action_share:
                                try {
                                  Uri uri = Uri.fromFile(f);
                                    ShareCompat.IntentBuilder.from(MainActivity.activity)
                                            .setStream(uri)
                                            .setType(URLConnection.guessContentTypeFromName(f.getName()))
                                            .startChooser();
                                }catch (Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(activity, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.action_delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setTitle("Confirm deletion")
                                        .setMessage("Are you sure you want to delete?")
                                        .setPositiveButton("OK", (dialog, which) -> {

                                            boolean isDeleted = f.delete();
                                            if (!isDeleted) {
                                                Toast.makeText(activity, "Unable to delete media!", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            boolean wasOnlyFile=false;

                                            if (MainActivity.yturls.size()>0) {
                                                int index = MainActivity.yturls.indexOf(model.getPath());
                                                if (index<position)
                                                    MainActivity.ytIndex--;
                                                MainActivity.yturls.remove(model.getPath());
                                                if (MainActivity.nPlayModels.size()>0)
                                                    for (NPlayModel nPlayModel: MainActivity.nPlayModels) {
                                                        if (nPlayModel.getUrl().equals(model.getPath()))
                                                        {
                                                            MainActivity.nPlayModels.remove(nPlayModel);
                                                            break;
                                                        }
                                                    }
                                            }
                                            yturls.remove(position);
                                            ofModels.remove(position);
                                            if (MainActivity.videoID!=null&&MainActivity.videoID.equals(model.getPath()))
                                            {
                                                if (yturls.size()>0) {
                                                    PlayMusic_Offline(position);
                                                }else {
                                                    MainActivity.onClear();
                                                    MainActivity.bottom_player.setVisibility(View.GONE);
                                                }
                                            }
                                            String name=ofModel.getPath().replace("/","_")+".csv";
                                            File toSave = new File(activity.getFilesDir(),"locals/"+name);
                                            String data = YTutils.readContent(activity,toSave.getPath());
                                            String[] items = data.trim().split("\n|\r");
                                            Log.e(TAG, "onPostExecute: "+items.length);
                                            if (items.length==1) {
                                                Log.e(TAG, "onPostExecute: This is only file in dir");
                                                wasOnlyFile=true;
                                            }
                                            if (items.length>0 && !wasOnlyFile) {
                                                StringBuilder builder1 = new StringBuilder();
                                                for (int i=0;i<items.length;i++) {
                                                    if (!items[i].contains(model.getPath()))
                                                        builder1.append("\n").append(items[i]);
                                                }
                                                YTutils.writeContent(activity,toSave.getPath(),builder1.toString().trim());
                                            }else if (wasOnlyFile) {
                                                {
                                                    Log.e(TAG, "onPostExecute: OnFile deleted" );
                                                    YTutils.writeContent(activity,toSave.getPath(),"");
                                                }
                                            }else
                                                Toast.makeText(activity, "Something went wrong!", Toast.LENGTH_SHORT).show();

                                            // Modify fileList csv file...
                                            String parent = new File(model.getPath()).getParent();
                                            Log.e(TAG, "onPostExecute: File modify: "+parent);

                                            data = YTutils.readContent(activity,"fileList.csv");
                                            if (items.length>0) {
                                                StringBuilder builder1 = new StringBuilder();
                                                items = data.split("\n|\r");
                                                for (String line: items) {
                                                    if (line.isEmpty()) continue;

                                                    if (line.contains(parent)) {
                                                        Log.e(TAG, "onPostExecute: Found parent");
                                                        long dur = model.getDuration();
                                                        long total = ofModel.getDuration();

                                                        ofModel.setDuration(total-dur);
                                                        ofModel.setSongCount(ofModel.getSongCount()-1);

                                                        if (!wasOnlyFile) {
                                                            builder1.append("\n").append(parent)
                                                                    .append("|").append(ofModel.getSongCount())
                                                                    .append("|").append(ofModel.getDuration());
                                                        }

                                                    }else {
                                                        builder1.append("\n").append(line);
                                                    }
                                                }
                                                YTutils.writeContent(activity,"fileList.csv",builder1.toString().trim());
                                            }

                                            ofadapter.notifyDataSetChanged();

                                            if (wasOnlyFile)
                                                activity.onBackPressed();

                                        }).setNegativeButton("Cancel", (dialog, which) -> {
                                }).show();
                                break;
                        }
                        return true;
                    });
                    popupMenu.show();
                });

            }else {
                Toast.makeText(MainActivity.activity, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }

        void insertPosition(OFModel model,int position,boolean addLast) {
            if (MainActivity.videoID.equals(model.getPath()))
            {
                Toast.makeText(activity, "Song is already playing!", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.e(TAG, "insertPosition: Path: "+model.getPath());
            int insertAt=position;
            if (MainActivity.yturls.contains(model.getPath())) {
                if (addLast) {
                    Toast.makeText(activity, "Song already in queue", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e(TAG, "insertPosition: Item already exist");
                int pos = MainActivity.yturls.indexOf(model.getPath());
                if (pos < position)
                {
                    MainActivity.ytIndex--;
                    insertAt = position-1;
                }
                MainActivity.yturls.remove(model.getPath());
            }
            if (MainActivity.nPlayModels.size()==MainActivity.yturls.size()&&MainActivity.yturls.size()>0) {
                File f = new File(model.getPath());
                Uri uri = Uri.fromFile(f);
                byte [] data=null;
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(activity,uri);
                    data = mmr.getEmbeddedPicture();
                }catch (Exception e){
                    Log.e(TAG, "insertPosition: Not a mp3 file");
                }
                Bitmap icon;
                if(data != null)
                    icon = BitmapFactory.decodeByteArray(data, 0, data.length);
                else
                    icon = YTutils.drawableToBitmap(ContextCompat.getDrawable(activity,R.drawable.ic_pulse));
                MetaModel model1 = new MetaModel(
                        model.getTitle(),model.getPath(),null
                );
                NPlayModel nPlayModel = new NPlayModel(model.getPath(),new YTMeta(model1),false);
                nPlayModel.setIcon(icon);
                MainActivity.nPlayModels.add(insertAt,nPlayModel);
            }
            Log.e(TAG, "insertPosition: Inserted position"+insertAt);
            MainActivity.yturls.add(insertAt,model.getPath());
            Toast.makeText(activity, "Song added to queue", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String data = YTutils.readContent(activity,filePath);
            String[] items = data.split("\n|\r");
            if (items.length>0) {
                for (String line: items) {
                    if (line.isEmpty()) continue;
                    String fullLocaltion = line.split("\\|")[0];
                    String artist = line.split("\\|")[1];
                    int s = Integer.parseInt(line.split("\\|")[3]);
                    seconds += s;
                    yturls.add(fullLocaltion);
                    ofModels.add(new OFModel(
                            artist,fullLocaltion,s
                    ));
                }
            }
            return null;
        }
    }

    class getData extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter = new SongAdapter(models,activity,true,recyclerItemListener);
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            songText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            playFab.setOnClickListener(v -> PlayMusic(0));

            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String yturl : yturls) {
                YTMeta ytMeta = new YTMeta(YTutils.getVideoID(yturl));
                if (ytMeta.getVideMeta()!=null) {
                    models.add(new DiscoverModel(
                            ytMeta.getVideMeta().getTitle(),
                            ytMeta.getVideMeta().getAuthor(),
                            ytMeta.getVideMeta().getImgUrl(),
                            yturl
                    ));
                }
            }
            return null;
        }

    }


}
