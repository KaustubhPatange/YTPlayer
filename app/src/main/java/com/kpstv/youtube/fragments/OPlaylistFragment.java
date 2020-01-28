package com.kpstv.youtube.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.MenuCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.AppSettings;
import com.kpstv.youtube.DPlaylistActivity;
import com.kpstv.youtube.EditTagActivity;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.LocalAdapter;
import com.kpstv.youtube.adapters.OFAdapter;
import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.LocalModel;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.models.OFModel;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.AppBarStateChangeListener;
import com.kpstv.youtube.utils.SortOrder;
import com.kpstv.youtube.utils.SortType;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;
import java.net.URLDecoder;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.COLLAPSED;
import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.EXPANDED;
import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.IDLE;

public class OPlaylistFragment extends Fragment {

    View v;
    boolean isnetworkCreated;
    Toolbar toolbar;
    RecyclerView recyclerView, albumRecyclerView;
    ArrayList<String> yturls;
    SongAdapter adapter;
    ArrayList<DiscoverModel> models;
    FragmentActivity activity;
    RecyclerView.LayoutManager layoutManager;
    ImageView oImageView;
    String title = "Playlist";
    TextView TitleText, SongCountText, TimeText, albumText, songText, pathLocation;
    PlaylistModel playlistModel;
    OFModel ofModel;
    FloatingActionButton playFab;
    boolean localMusic = false, searchMusic = false;
    SharedPreferences preferences;
    File mainFile;
    OFAdapter ofadapter;
    LocalModel localModel;
    boolean scanAlbum = false;
    ArrayList<OFModel> ofModels;
    ArrayList<LocalModel> albumModels;
    LocalAdapter albumAdaper;
    GridLayoutManager gridLayoutManager;
    private static final String TAG = "OPlayListFragment";
    boolean loadComplete = false;
    ProgressBar circularProgressBar;

    public OPlaylistFragment() {
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isnetworkCreated) {
            Log.e(TAG, "onCreateView: Calling Parent");
            v = inflater.inflate(R.layout.fragment_oplaylist, container, false);
            activity = getActivity();

            toolbar = v.findViewById(R.id.toolbar);

            models = new ArrayList<>();
            albumModels = new ArrayList<>();
            ofModels = new ArrayList<>();
            yturls = new ArrayList<>();
            recyclerView = v.findViewById(R.id.my_recycler_view);
            TitleText = v.findViewById(R.id.oTitle);
            albumText = v.findViewById(R.id.albumText);
            albumRecyclerView = v.findViewById(R.id.album_recyclerView);
            oImageView = v.findViewById(R.id.oImageView);
            circularProgressBar = v.findViewById(R.id.circularProgress);
            pathLocation = v.findViewById(R.id.oPathLocation);
            SongCountText = v.findViewById(R.id.oSongText);
            TimeText = v.findViewById(R.id.oTime);
            songText = v.findViewById(R.id.songText);
            playFab = v.findViewById(R.id.oPlayFAB);
            layoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(layoutManager);

            gridLayoutManager = new GridLayoutManager(activity, 3);
            albumRecyclerView.setLayoutManager(gridLayoutManager);

            preferences = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);

            AppSettings.sortOrder = SortOrder.values()[preferences.getInt("sort_order", 0)];
            AppSettings.sortType = SortType.values()[preferences.getInt("sort_type", 2)];

            final CollapsingToolbarLayout collapsingToolbarLayout = v.findViewById(R.id.toolbar_layout);
            AppBarLayout appBarLayout = v.findViewById(R.id.app_bar);

            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    appBarLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if (state == COLLAPSED) {
                                toolbar.setTitle(title);
                                collapsingToolbarLayout.setTitle(title);
                            } else if (state == EXPANDED || state == IDLE) {
                                collapsingToolbarLayout.setTitle(" ");
                                toolbar.setTitle(" ");
                            }
                        }
                    });
                }
            });
            /*appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = true;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0 && !isShow) {
                        toolbarTextView.setText(title);
                        isShow = true;
                    } else if(isShow) {
                        toolbarTextView.setText("");
                        isShow = false;
                    }
                }
            });
*/
            Log.e(TAG, "onCreateView: Above Bundles");

            Bundle args = getArguments();
            String isLocal = args.getString("isLocalMusic");

            if (isLocal != null && isLocal.equals("true")) {
                Log.e(TAG, "onCreateView: Okay in localMusic");
                localMusic = true;
                ofModel = (OFModel) args.getSerializable("model");
                title = ofModel.getTitle().trim();
                String location = ofModel.getPath();
                mainFile = new File(activity.getFilesDir(), "locals/" + location.replace("/", "_") + ".csv");
                Log.e(TAG, "onCreateView: " + mainFile.getPath());
                if (mainFile.exists()) {
                    Log.e(TAG, "onCreateView: In File Exist bro");
                    oImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_folder));
                    int song = ofModel.getSongCount();
                    TitleText.setText(ofModel.getTitle());
                    if (song == 1)
                        SongCountText.setText(song + " song");
                    else
                        SongCountText.setText(song + " songs");
                    pathLocation.setText(new File(ofModel.getPath()).getParent());
                    pathLocation.setVisibility(View.VISIBLE);
                    Log.e(TAG, "onCreateView: " + ofModel.getDuration());
                    TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(ofModel.getDuration() * 1000)));
                    Log.e(TAG, "onCreateView: In localMusic");

                    //  new getData_Offline(mainFile.getPath()).execute();

                } else {
                    localMusic = false;
                    Log.e(TAG, "onCreateView: Not File Exist bro");
                    Toast.makeText(MainActivity.activity, "Rescan library to scan songs", Toast.LENGTH_SHORT).show();
                }

            } else if (isLocal != null && isLocal.equals("search")) {
                localMusic = true;
                searchMusic = true;
                localModel = (LocalModel) args.getSerializable("model");
                title = localModel.getTitle().trim();
                oImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_user));

                if (localModel != null) {
                    scanAlbum = false;
                    TitleText.setText(localModel.getTitle());
                    if (localModel.getSongList().size() > 1) {
                        SongCountText.setText(localModel.getSongList().size() + " songs");
                    } else SongCountText.setText("1 song");

                    Log.e(TAG, "onCreateView: AlbumCount: " + localModel.getAlbumCount());

                    if (localModel.getAlbumCount() > 0) {
                        scanAlbum = true;
                    }
                    //new searchTask(scanAlbum,localModel).execute();
                }
            } else {
                Log.e(TAG, "onCreateView: In normal OPlayFrag");
                playlistModel = (PlaylistModel) args.getSerializable("model");
                title = playlistModel.getTitle().trim();
                TitleText.setText(playlistModel.getTitle());
                SongCountText.setText(String.format("%s songs", playlistModel.getData().size()));
                TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(
                        playlistModel.getTimeseconds() * 1000)));


                if (playlistModel.getData().size() > 0) {
                    new getData().execute();
                } else {
                    circularProgressBar.setVisibility(View.GONE);
                    songText.setText("NO SONG DATA");
                    songText.setVisibility(View.VISIBLE);
                }
            }

            toolbar.setNavigationOnClickListener(v1 -> {
                if (searchMusic) {
                    activity.onBackPressed();
                    return;
                }
                if (localMusic) {
                    MainActivity.loadLocalMusicFrag();
                    return;
                }
                if (MainActivity.loadedFavFrag) {
                    MainActivity.loadedFavFrag = false;
                    MainActivity.loadLibraryFrag();
                } else
                    MainActivity.loadPlayFrag();
            });

            if (localMusic) {
                toolbar.inflateMenu(R.menu.sort_menu);
                toolbar.getMenu().getItem(0).getSubMenu()
                        .getItem(AppSettings.sortType.ordinal()).setChecked(true);
                toolbar.getMenu().getItem(0).getSubMenu()
                        .getItem(4 + AppSettings.sortOrder.ordinal()).setChecked(true);

                Log.e(TAG, "onCreateView: Sort Order: " + AppSettings.sortType.ordinal() + toolbar.getMenu().getItem(0).getSubMenu()
                        .getItem(4 + AppSettings.sortOrder.ordinal()).getTitle());
                MenuCompat.setGroupDividerEnabled(toolbar.getMenu(), true);
                toolbar.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.action_sort) return true;
                    menuItem.setChecked(true);
                    switch (menuItem.getItemId()) {
                        case R.id.action_alphabet:
                            AppSettings.sortType = SortType.alphabetical;
                            break;
                        case R.id.action_dateAdded:
                            AppSettings.sortType = SortType.date_added;
                            break;
                        case R.id.action_fileName:
                            AppSettings.sortType = SortType.file_name;
                            break;
                        case R.id.action_duration:
                            AppSettings.sortType = SortType.duration;
                            break;
                        case R.id.action_ascending:
                            AppSettings.sortOrder = SortOrder.ascending;
                            break;
                        case R.id.action_descending:
                            AppSettings.sortOrder = SortOrder.descending;
                            break;
                    }
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("sort_order", AppSettings.sortOrder.ordinal());
                    editor.putInt("sort_type", AppSettings.sortType.ordinal());
                    Log.e(TAG, "onCreateView: " + AppSettings.sortType.ordinal());
                    editor.apply();
                    if (searchMusic)
                        new searchTask(scanAlbum, localModel).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        new getData_Offline(mainFile.getPath()).execute();
                    return true;
                });
            }else {
                /** Online Playlist or Favourites */
                toolbar.inflateMenu(R.menu.playlist_popup);
                toolbar.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.action_download:
                            Intent intent = new Intent(activity, DPlaylistActivity.class);
                            intent.putExtra("list",playlistModel.getData());
                            startActivity(intent);


                            break;
                    }
                    return true;
                });
            }

            //   collapsingToolbarLayout.setTitle(title);
            isnetworkCreated = true;
        }
        return v;
    }

    class searchTask extends AsyncTask<Void, Void, Void> {
        int seconds = 0;
        boolean scanAlbum;
        LocalModel model;
        String imageUri;
        ArrayList<String> albumKey;
        ArrayList<ArrayList<String>> albumValueList;


        public searchTask(boolean scanAlbum, LocalModel model) {
            Log.e(TAG, "searchTask: Created Task");
            this.scanAlbum = scanAlbum;
            this.model = model;
            albumKey = new ArrayList<>();
            albumValueList = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            circularProgressBar.setVisibility(View.GONE);
            songText.setText("TRACKS");
            songText.setVisibility(View.VISIBLE);

            TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(seconds * 1000)));

            ofadapter = new OFAdapter(activity, ofModels, true);
            recyclerView.setAdapter(ofadapter);
            recyclerView.setVisibility(View.VISIBLE);

            if (scanAlbum) {
                pathLocation.setVisibility(View.VISIBLE);
                int s = albumKey.size();
                if (s == 1)
                    pathLocation.setText("1 album");
                else pathLocation.setText(s + " albums");

                if (imageUri != null) {
                    Log.e(TAG, "onPostExecute: Loading data locally: " + imageUri);
                    Glide.with(activity).asBitmap().load(imageUri).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            oImageView.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
                }
            } else {
                String file = ofModels.get(0).getPath();
                Log.e(TAG, "onPostExecute: File: " + file);
                if (file != null) {
                    File f = new File(file);
                    Bitmap bmp = YTutils.getArtworkFromFile(activity, f);
                    if (bmp != null) {
                        oImageView.setImageBitmap(bmp);
                        oImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }
            }

            playFab.setOnClickListener(v -> PlayMusic_Offline(0));

            /** Set Adapter Clicks */

            ofadapter.setSingleClickListener((v1, model1, position) -> PlayMusic_Offline(position));

            ofadapter.setLongClickListener((v1, model1, position) -> {
                PopupMenu popupMenu = new PopupMenu(activity, v1);
                popupMenu.inflate(R.menu.local_popup_menu2);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.action_play:
                            PlayMusic_Offline(position);
                            break;
                        case R.id.action_ringtone:
                            YTutils.setDefaultRingtone(activity, new File(model1.getPath()));
                            break;
                        case R.id.action_details:
                            Bundle args = new Bundle();
                            args.putString("filepath", model1.getPath());
                            DetailsBottomSheet sheet = new DetailsBottomSheet();
                            sheet.setArguments(args);
                            sheet.show(activity.getSupportFragmentManager(), "");
                            break;
                        case R.id.action_play_next:
                            if (MainActivity.yturls.size() == 0) {
                                PlayMusic_Offline(position);
                            } else {
                                insertPosition(model1, position, false);
                            }
                            break;
                        case R.id.action_add_queue:
                            if (MainActivity.yturls.size() == 0) {
                                PlayMusic_Offline(position);
                            } else {
                                insertPosition(model1, position, true);
                            }
                            break;
                        case R.id.action_share:
                            File f = new File(model1.getPath());
                            YTutils.shareFile(MainActivity.activity, f);
                            break;
                    }
                    return true;
                });
                popupMenu.show();
            });

            if (scanAlbum && albumModels.size() > 0) {
                albumText.setVisibility(View.VISIBLE);

                albumAdaper = new LocalAdapter(activity, albumModels, true);
                albumRecyclerView.setVisibility(View.VISIBLE);
                albumRecyclerView.setAdapter(albumAdaper);

                albumAdaper.setSingleClickListener((view, model1, position) -> {
                    Bundle args = new Bundle();
                    args.putSerializable("model", model1);
                    args.putString("isLocalMusic", "search");

                    OPlaylistFragment fragment = new OPlaylistFragment();
                    fragment.setArguments(args);
                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.anim.fade_in,
                            R.anim.fade_out);
                    ft.addToBackStack(null).replace(R.id.fragment_container, fragment, "localMusic");
                    ft.commit();
                });

                albumAdaper.setLongClickListener((view, model1, position) -> {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.inflate(R.menu.local_popup_menu3);
                    popupMenu.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case R.id.action_play:
                                albumPlay(localModel);
                                break;
                            case R.id.action_add_queue:
                                if (MainActivity.yturls.isEmpty()) {
                                    albumPlay(localModel);
                                } else {
                                    AddItems(localModel);
                                }
                                break;
                        }
                        return true;
                    });
                    popupMenu.show();
                });
            }
            super.onPostExecute(aVoid);
        }

        void AddItems(LocalModel localModel) {
            boolean someThingAdded = false;
            for (String line : localModel.getSongList()) {
                if (line.isEmpty()) continue;
                String filePath = line.split("\\|")[0];
                if (!MainActivity.videoID.equals(filePath)) {
                    if (!MainActivity.yturls.contains(filePath)) {
                        someThingAdded = true;
                        MainActivity.yturls.add(filePath);
                    }
                }
            }
            if (someThingAdded)
                Toast.makeText(activity, "Current playlist updated!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(activity, "No new song to add!", Toast.LENGTH_SHORT).show();
        }

        void albumPlay(LocalModel localModel) {
            ArrayList<String> urls = new ArrayList<>();
            for (String path : localModel.getSongList()) {
                if (path.isEmpty()) continue;
                urls.add(path.split("\\|")[0]);
            }
            MainActivity.PlayVideo_Local(YTutils.convertListToArrayMethod(urls));
        }

        void insertPosition(OFModel model, int position, boolean addToLast) {
            if (MainActivity.videoID.equals(model.getPath())) {
                Toast.makeText(activity, "Song is already playing!", Toast.LENGTH_SHORT).show();
            } else if (MainActivity.localPlayBack) {
                if (addToLast) {
                    MainActivity.yturls.remove(model.getPath());
                    MainActivity.yturls.add(model.getPath());
                } else {
                    int index = MainActivity.yturls.indexOf(MainActivity.videoID);
                    MainActivity.yturls.remove(model.getPath());
                    MainActivity.yturls.add(index + 1, model.getPath());
                }
                Toast.makeText(activity, "Song added to queue", Toast.LENGTH_SHORT).show();
            } else {
                PlayMusic_Offline(position);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            /** Normal Displaying models */
            for (String line : model.getSongList()) {
                String[] childs = line.split("\\|");
                int s = Integer.parseInt(childs[3]);
                seconds += s;
                OFModel model = new OFModel(
                        childs[1], childs[0], s
                );
                yturls.add(childs[0]);
                model.setDuration(s);
                model.setDate(Long.parseLong(childs[4]));
                ofModels.add(model);
            }

            /** Sorting data */
            automateSorting();

            int comparision = 0;
            /** Search for albums*/
            if (scanAlbum) {
                Log.e(TAG, "doInBackground: Title: " + model.getTitle().trim());
                imageUri = YTutils.getLocalArtworkImage(activity, model);
            } else return null;
            File local = new File(activity.getFilesDir(), "locals");
            for (File file : local.listFiles()) {
                String data = YTutils.readContent(activity, file.getPath());
                if (data.isEmpty()) continue;
                if (!data.contains(localModel.getTitle())) continue;
                comparision++;
                Log.e(TAG, "doInBackground: Album: data");
                String[] lines = data.split("\n|\r");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.isEmpty()) continue;
                    if (line.contains("|" + localModel.getTitle().trim() + "|")) {
                        comparision++;
                        String[] childs = line.split("\\|");
                        String album = childs[2];
                        if (albumKey.contains(album)) {
                            int index = albumKey.indexOf(album);
                            if (!albumValueList.get(index).contains(line))
                                albumValueList.get(index).add(line);
                        } else {
                            albumKey.add(album);
                            ArrayList<String> strings = new ArrayList<>();
                            strings.add(line);
                            albumValueList.add(strings);
                        }

                        /** This loop is for adding respective songs to album */
                        for (int j = 0; j < albumKey.size(); j++) {
                            comparision++;
                            album = albumKey.get(j);
                            for (int c = 0; c < i; c++) {
                                String l = lines[c];
                                if (l.contains("|" + album + "|") && !albumValueList.get(j).contains(l)) {
                                    comparision++;
                                    albumValueList.get(j).add(l);
                                }
                            }
                        }
                    }

                }
            }
            Log.e(TAG, "doInBackground: AlbumScan: " + albumKey.size() + ", Total Comparision: " + comparision);
            if (albumKey.size() > 0) {
                for (int i = 0; i < albumKey.size(); i++) {
                    albumModels.add(new LocalModel(albumKey.get(i), albumValueList.get(i), 0));
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            circularProgressBar.setVisibility(View.VISIBLE);
            albumModels.clear();
            if (albumAdaper != null) albumAdaper.notifyDataSetChanged();
            ofModels.clear();
            if (ofadapter != null) ofadapter.notifyDataSetChanged();
            super.onPreExecute();
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {

        Log.e(TAG, "onCreateAnimation: Enter: " + enter);

        if (enter) {
            final int animatorId = R.anim.fade_in;
            final Animation anim = AnimationUtils.loadAnimation(getActivity(), animatorId);
            if (anim == null) return null;
            anim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    Log.d(TAG, "Animation started.");
                    // additional functionality
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    Log.d(TAG, "Animation repeating.");
                    // additional functionality
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Log.d(TAG, "Animation ended.");
                    if (loadComplete) return;
                    if (localMusic && !searchMusic)
                        new getData_Offline(mainFile.getPath()).execute();
                    else if (searchMusic)
                        new searchTask(scanAlbum, localModel).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    loadComplete = true;
                }
            });

            return anim;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: Triggered onResume");
        super.onResume();
    }

   /* private View.OnClickListener recyclerItemListener = view -> {
        int position = (int)view.getTag();
        PlayMusic(position);
    };*/

    void PlayMusic(int position) {
        if (yturls.size() == 0) return;
        String[] videos = YTutils.ConvertToStringArray(yturls);
        MainActivity.PlayVideo(videos, position);
    }

    void PlayMusic_Offline(int position) {
        if (yturls.size() == 0) return;
        String[] files = YTutils.ConvertToStringArray(yturls);
        MainActivity.PlayVideo_Local(files, position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42) {
            Uri treeUri = data.getData();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(activity, treeUri);
            activity.grantUriPermission(activity.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ext_sdcard", treeUri.toString());
            editor.apply();

            Toast.makeText(activity, "Permission granted, try to delete file again!", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class getData_Offline extends AsyncTask<Void, Void, Void> {
        String filePath;
        int seconds = 0;

        public getData_Offline(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected void onPreExecute() {
            ofModels.clear();
            circularProgressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (ofModels.size() > 0) {
                circularProgressBar.setVisibility(View.GONE);
                songText.setText("TRACKS");
                songText.setVisibility(View.VISIBLE);

                TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(seconds * 1000)));

                ofadapter = new OFAdapter(activity, ofModels, true);
                recyclerView.setAdapter(ofadapter);
                recyclerView.setVisibility(View.VISIBLE);

                playFab.setOnClickListener(v -> PlayMusic_Offline(0));

                ofadapter.setSingleClickListener((v1, model, position) -> {
                    PlayMusic_Offline(position);
                });

                ofadapter.setLongClickListener((v1, model, position) -> {
                    File f = new File(model.getPath());
                    Log.e(TAG, "onPostExecute: " + model.getPath());
                    PopupMenu popupMenu = new PopupMenu(activity, v1);
                    popupMenu.inflate(R.menu.local_popup_menu1);
                    popupMenu.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case R.id.action_play:
                                PlayMusic_Offline(position);
                                break;
                            case R.id.action_play_next:
                                if (MainActivity.yturls.size() == 0) {
                                    PlayMusic_Offline(position);
                                    return true;
                                }
                                insertPosition(model, MainActivity.ytIndex + 1, false);
                                break;
                            case R.id.action_ringtone:
                                YTutils.setDefaultRingtone(activity, f);
                                break;
                            case R.id.action_details:
                                Bundle args = new Bundle();
                                args.putString("filepath", model.getPath());
                                DetailsBottomSheet sheet = new DetailsBottomSheet();
                                sheet.setArguments(args);
                                sheet.show(activity.getSupportFragmentManager(), "");
                                break;
                            case R.id.action_add_queue:
                                if (MainActivity.yturls.size() == 0) {
                                    PlayMusic_Offline(position);
                                    return true;
                                }
                                insertPosition(model, MainActivity.yturls.size(), true);
                                break;
                            case R.id.action_edit_tag:
                                if (MainActivity.videoID != null && MainActivity.videoID.equals(model.getPath())) {
                                    Toast.makeText(activity, "Cannot edit tags when playing", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                                String extension = model.getPath().split("\\.")[1].toLowerCase();
                                if (extension.equals("mp3")) {
                                    Intent intent = new Intent(activity, EditTagActivity.class);
                                    intent.putExtra("model", model);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(activity, "Could not edit tags for ." + extension + " file type", Toast.LENGTH_SHORT).show();
                                }

                                break;
                            case R.id.action_share:
                                YTutils.shareFile(MainActivity.activity, f);
                                break;
                            case R.id.action_delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setTitle("Confirm deletion")
                                        .setMessage("Are you sure you want to delete?")
                                        .setPositiveButton("OK", (dialog, which) -> {

                                            boolean isDeleted = f.delete();
                                            if (!isDeleted) {

                                                // try delete using Document file...
                                                String[] volumes = YTutils.getExternalStorageDirectories(activity);
                                                if (volumes.length > 0) {
                                                    Log.e(TAG, "onPostExecute: Volumes: " + volumes[0]);
                                                    if (f.getPath().contains(volumes[0])) {

                                                        /* String ext_storage = "content://com.android.externalstorage.documents/tree/"
                                                                +f.getPath().replace("/storage/","");

                                                        String ext_storage = "content://com.android.externalstorage.documents/tree/"
                                                                +f.getPath().replace("/storage/","");

                                                        Uri treeUri = Uri.parse(ext_storage);
                                                        DocumentFile file = DocumentFile.fromTreeUri(activity, treeUri);
                                                        activity.grantUriPermission(activity.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                                        activity.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                                        */
                                                        Log.e(TAG, "onPostExecute: Using SAF");
                                                        String ext_sdCard = preferences.getString("ext_sdcard", null);
                                                        String storageName = f.getPath().split("/")[2];
                                                        Log.e(TAG, "onPostExecute: Storage: " + storageName);
                                                        if (ext_sdCard == null) {

                                                            View v = activity.getLayoutInflater().inflate(R.layout.alert_saf_info, null);
                                                            new AlertDialog.Builder(activity)
                                                                    .setTitle("Permission Required")
                                                                    .setView(v)
                                                                    .setNegativeButton("Cancel", null)
                                                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                                                        /** Use SAF to get uri */
                                                                        Intent n = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                                                        n.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                                                        n.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                                      /*  String contentUri = "content://com.android.externalstorage.documents/tree/"+storageName+"%3ACoolpad";
                                                                        Log.e(TAG, "onPostExecute: Content: "+contentUri );
                                                                        n.putExtra(DocumentsContract.EXTRA_INITIAL_URI, contentUri);*/
                                                                        //   n.setData();
                                                                        startActivityForResult(n, 42);
                                                                    })
                                                                    .show();

                                                            return;
                                                        } else {
                                                            Log.e(TAG, "onPostExecute: Decoded Path: " + ext_sdCard);
                                                            if (ext_sdCard.contains(volumes[0].replace("/storage/", ""))) {
                                                             /*   String fpath = f.getPath().replace("/storage/"+storageName+"/","")
                                                                        .replace("/"+f.getName(),"");*/
                                                                Uri treeUri = Uri.parse(ext_sdCard);

                                                                DocumentFile file = DocumentFile.fromTreeUri(activity, treeUri);

                                                                String deepFile = f.getPath().replace("/storage/" + storageName + "/", "");
                                                                String[] folders = deepFile.split("/");

                                                                activity.grantUriPermission(activity.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                                                activity.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


                                                                for (int i = 0; i < folders.length; i++) {
                                                                     file = file.findFile(folders[i]);
                                                                }


                                                                Log.e(TAG, "onPostExecute: treeUri:" + file.getUri().toString());

                                                                if (file != null && file.exists()) {
                                                                    isDeleted = file.delete();
                                                                } else sayWrongPath();

                                                                //+fpath.replace("/","%2F")

                                                               /* if (file!=null && file.exists()) {
                                                                    isDeleted = file.delete();
                                                                }else sayWrongPath();*/


                                                            } else {
                                                                sayWrongPath();
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!isDeleted) {
                                                    Toast.makeText(activity, "Unable to delete media!", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            }

                                            boolean wasOnlyFile = false;

                                            if (MainActivity.yturls.size() > 0) {
                                                int index = MainActivity.yturls.indexOf(model.getPath());
                                                if (index < position)
                                                    MainActivity.ytIndex--;
                                                MainActivity.yturls.remove(model.getPath());
                                                if (MainActivity.nPlayModels.size() > 0)
                                                    for (NPlayModel nPlayModel : MainActivity.nPlayModels) {
                                                        if (nPlayModel.getUrl().equals(model.getPath())) {
                                                            MainActivity.nPlayModels.remove(nPlayModel);
                                                            break;
                                                        }
                                                    }
                                            }
                                            yturls.remove(position);
                                            ofModels.remove(position);
                                            if (MainActivity.videoID != null && MainActivity.videoID.equals(model.getPath())) {
                                                if (yturls.size() > 0) {
                                                    PlayMusic_Offline(position);
                                                } else {
                                                    MainActivity.onClear();
                                                    MainActivity.bottom_player.setVisibility(View.GONE);
                                                }
                                            }
                                            String name = ofModel.getPath().replace("/", "_") + ".csv";
                                            File toSave = new File(activity.getFilesDir(), "locals/" + name);
                                            String data = YTutils.readContent(activity, toSave.getPath());
                                            String[] items = data.trim().split("\n|\r");
                                            Log.e(TAG, "onPostExecute: " + items.length);
                                            if (items.length == 1) {
                                                Log.e(TAG, "onPostExecute: This is only file in dir");
                                                wasOnlyFile = true;
                                            }
                                            if (items.length > 0 && !wasOnlyFile) {
                                                StringBuilder builder1 = new StringBuilder();
                                                for (int i = 0; i < items.length; i++) {
                                                    if (!items[i].contains(model.getPath()))
                                                        builder1.append("\n").append(items[i]);
                                                }
                                                YTutils.writeContent(activity, toSave.getPath(), builder1.toString().trim());
                                            } else if (wasOnlyFile) {
                                                {
                                                    Log.e(TAG, "onPostExecute: OnFile deleted");
                                                    YTutils.writeContent(activity, toSave.getPath(), "");
                                                }
                                            } else
                                                Toast.makeText(activity, "Something went wrong!", Toast.LENGTH_SHORT).show();

                                            // Modify fileList csv file...
                                            String parent = new File(model.getPath()).getParent();
                                            Log.e(TAG, "onPostExecute: File modify: " + parent);

                                            data = YTutils.readContent(activity, "fileList.csv");
                                            if (items.length > 0) {
                                                StringBuilder builder1 = new StringBuilder();
                                                items = data.split("\n|\r");
                                                for (String line : items) {
                                                    if (line.isEmpty()) continue;

                                                    if (line.contains(parent)) {
                                                        Log.e(TAG, "onPostExecute: Found parent");
                                                        long dur = model.getDuration();
                                                        long total = ofModel.getDuration();

                                                        ofModel.setDuration(total - dur);
                                                        ofModel.setSongCount(ofModel.getSongCount() - 1);

                                                        if (!wasOnlyFile) {
                                                            builder1.append("\n").append(parent)
                                                                    .append("|").append(ofModel.getSongCount())
                                                                    .append("|").append(ofModel.getDuration());
                                                        }

                                                    } else {
                                                        builder1.append("\n").append(line);
                                                    }
                                                }
                                                YTutils.writeContent(activity, "fileList.csv", builder1.toString().trim());
                                            }

                                            ofadapter.notifyDataSetChanged();

                                            MainActivity.localMusicFrag.onActivityResult(110, 8, null);

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

            } else {
                Toast.makeText(MainActivity.activity, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }


        void sayWrongPath() {
            Toast.makeText(activity, "Wrong path selected, try again!", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ext_sdcard", null);
            editor.apply();
        }

        void insertPosition(OFModel model, int position, boolean addLast) {
            if (MainActivity.videoID.equals(model.getPath())) {
                Toast.makeText(activity, "Song is already playing!", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.e(TAG, "insertPosition: Path: " + model.getPath());
            int insertAt = position;
            if (MainActivity.yturls.contains(model.getPath())) {
                if (addLast) {
                    Toast.makeText(activity, "Song already in queue", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e(TAG, "insertPosition: Item already exist");
                int pos = MainActivity.yturls.indexOf(model.getPath());
                if (pos < position) {
                    MainActivity.ytIndex--;
                    insertAt = position - 1;
                }
                MainActivity.yturls.remove(model.getPath());
            }
            if (MainActivity.nPlayModels.size() == MainActivity.yturls.size() && MainActivity.yturls.size() > 0) {
                File f = new File(model.getPath());
                Uri uri = Uri.fromFile(f);
                byte[] data = null;
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(activity, uri);
                    data = mmr.getEmbeddedPicture();
                } catch (Exception e) {
                    Log.e(TAG, "insertPosition: Not a mp3 file");
                }
                Bitmap icon;
                if (data != null)
                    icon = BitmapFactory.decodeByteArray(data, 0, data.length);
                else
                    icon = YTutils.drawableToBitmap(ContextCompat.getDrawable(activity, R.drawable.ic_pulse));
                MetaModel model1 = new MetaModel(
                        model.getPath(), model.getTitle(), model.getPath(), null
                );
                NPlayModel nPlayModel = new NPlayModel(model.getPath(), new YTMeta(model1), false);
                nPlayModel.setIcon(icon);
                MainActivity.nPlayModels.add(insertAt, nPlayModel);
            }
            Log.e(TAG, "insertPosition: Inserted position" + insertAt);
            MainActivity.yturls.add(insertAt, model.getPath());
            Toast.makeText(activity, "Song added to queue", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String data = YTutils.readContent(activity, filePath);
            String[] items = data.split("\n|\r");
            if (items.length > 0) {
                for (String line : items) {
                    if (line.isEmpty()) continue;
                    String fullLocaltion = line.split("\\|")[0];
                    if (!new File(fullLocaltion).exists()) continue;
                    String artist = line.split("\\|")[1];
                    int s = Integer.parseInt(line.split("\\|")[3]);
                    long date = Long.parseLong(line.split("\\|")[4]);
                    seconds += s;
                    yturls.add(fullLocaltion);
                    OFModel model = new OFModel(artist, fullLocaltion, s);
                    model.setDuration(s);
                    model.setDate(date);
                    ofModels.add(model);
                }

                /** Sorting of list will be done here*/

                automateSorting();
            }
            return null;
        }
    }

    void automateSorting() {
        switch (AppSettings.sortType) {
            case alphabetical:
                Collections.sort(ofModels, (t1, t2) -> {
                    String t1_string = new File(t1.getPath()).getName();
                    String t2_string = new File(t2.getPath()).getName();
                    return t1_string.compareToIgnoreCase(t2_string);
                });
                break;
            case date_added:
                Collections.sort(ofModels, (t1, t2) -> Long.compare(t2.getDate(), t1.getDate()));
                break;
            case duration:
                Collections.sort(ofModels, (t1, t2) -> Long.compare(t1.getDuration(), t2.getDuration()));
                break;
        }
        if (AppSettings.sortOrder == SortOrder.descending) {
            Collections.reverse(ofModels);
        }
        yturls.clear();
        for (OFModel model : ofModels) {
            yturls.add(model.getPath());
        }
    }

    class getData extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPostExecute(Void aVoid) {
            adapter = new SongAdapter(models, activity, true, null);
            recyclerView.setAdapter(adapter);
            circularProgressBar.setVisibility(View.GONE);
            songText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            playFab.setOnClickListener(v -> PlayMusic(0));

            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String data = YTutils.readContent(activity, "removedList.csv");
            ArrayList<String> strings = new ArrayList<>();
            if (data != null && !data.isEmpty()) {
                if (data.contains(","))
                    strings = new ArrayList<>(Arrays.asList(data.split(",")));
                else strings.add(data.trim());
            }
            for (String line : playlistModel.getData()) {
                if (!line.contains("|")) continue;
                String[] childs = line.split("\\|");
                String videoId = childs[0];
                yturls.add(YTutils.getYtUrl(videoId));

                DiscoverModel discoverModel = new DiscoverModel(
                        childs[2],
                        childs[3],
                        childs[4],
                        YTutils.getYtUrl(videoId)
                );

                if (strings.contains("ytID:" + videoId) || strings.contains("sd:" + videoId))
                    discoverModel.setDisabled(true);

                models.add(discoverModel);
            }
           /* for (String yturl : yturls) {
                YTMeta ytMeta = new YTMeta(YTutils.getVideoID(yturl));
                if (ytMeta.getVideMeta()!=null) {
                    DiscoverModel discoverModel = new DiscoverModel(
                            ytMeta.getVideMeta().getTitle(),
                            ytMeta.getVideMeta().getAuthor(),
                            ytMeta.getVideMeta().getImgUrl(),
                            yturl
                    );

                    if (strings.contains("ytID:"+YTutils.getVideoID(yturl)))
                        discoverModel.setDisabled(true);

                    models.add(discoverModel);
                }
            }*/
            return null;
        }

    }


}
