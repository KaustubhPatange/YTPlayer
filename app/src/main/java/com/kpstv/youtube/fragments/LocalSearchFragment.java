package com.kpstv.youtube.fragments;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.LocalAdapter;
import com.kpstv.youtube.adapters.OFAdapter;
import com.kpstv.youtube.models.LocalModel;
import com.kpstv.youtube.models.LocalSearchModel;
import com.kpstv.youtube.models.OFModel;
import com.kpstv.youtube.utils.YTutils;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.File;
import java.util.ArrayList;

public class LocalSearchFragment extends Fragment {
    public LocalSearchFragment() {}

    View v;
    AutoCompleteTextView searchEditText;
    ImageView closeButton;
    Toolbar toolbar;
    ProgressBar progressBar;
    TextView songText,albumText;
    TextView tooManyText, noResultText;
    TextView artistText;
    RecyclerView recyclerView;
    RecyclerView artistRecyclerView,albumRecyclerView;

    FragmentActivity activity;
    ArrayList<OFModel> ofModels;
    ArrayList<LocalModel> artistModels,albumModels; ArrayList<String> yturls;
    LinearLayoutManager linearLayoutManager;
    GridLayoutManager gridLayoutManager,gridLayoutManager1;
    AsyncTask<Void,Void,Void> searchTask;
    OFAdapter ofAdapter; LocalAdapter localAdapter,albumAdapter;

    private static final String TAG = "LocalSearchFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v==null) {
            v = inflater.inflate(R.layout.fragment_local_search, container, false);

            activity = getActivity();

            getAllViews();

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> {
                activity.onBackPressed();
            });

            ofModels = new ArrayList<>();
            albumModels = new ArrayList<>();
            artistModels = new ArrayList<>();
            yturls = new ArrayList<>();
            linearLayoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(linearLayoutManager);

            gridLayoutManager = new GridLayoutManager(activity,3);
            gridLayoutManager1 = new GridLayoutManager(activity,3);
            artistRecyclerView.setLayoutManager(gridLayoutManager);
            albumRecyclerView.setLayoutManager(gridLayoutManager1);

            closeButton.setOnClickListener(view -> {
                searchEditText.setText("");
            });

            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.toString().trim().isEmpty()) {
                        closeButton.setVisibility(View.GONE);
                        preExeuteMethod();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }else {
                        closeButton.setVisibility(View.VISIBLE);
                    }
                    if (searchTask!=null && searchTask.getStatus() == AsyncTask.Status.RUNNING) {
                        searchTask.cancel(true);
                    }
                    searchTask = new SearchTask(editable.toString());
                    searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }
        searchEditText.requestFocus();
        UIUtil.showKeyboard(activity,searchEditText);
        return v;
    }

    void PlayMusic_Offline(int position) {
        if (yturls.size()==0) return;
        String[] files =YTutils.ConvertToStringArray(yturls);
        MainActivity.PlayVideo_Local(files,position);
    }

    class SearchTask extends AsyncTask<Void,Void,Void> {
        String text;
        ArrayList<ArrayList<String>> albumValueCountList;
        ArrayList<String> artistKey;
        ArrayList<String> albumKey;
        ArrayList<ArrayList<String>> artistValueList;
        ArrayList<ArrayList<String>> albumValueList;
        public SearchTask(String text) {
            this.text = text;
            albumKey = new ArrayList<>();
            albumValueList = new ArrayList<>();
            artistKey = new ArrayList<>();
            artistValueList = new ArrayList<>();
            albumValueCountList = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (ofModels.size()>0 || artistModels.size()>0 || albumModels.size()>0) {
                if (ofModels.size()>0) {
                    if (ofModels.size()>100) {
                        tooManyText.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }else {
                        ofAdapter = new OFAdapter(activity, ofModels, true);
                        /** Set onclicks for adapter */
                        setAdapterClicks();
                        songText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(ofAdapter);
                    }
                }
                if (albumModels.size()>0) {
                    albumAdapter = new LocalAdapter(activity,albumModels,true);
                    setAdapterClicks2();
                    albumText.setVisibility(View.VISIBLE);
                    albumRecyclerView.setVisibility(View.VISIBLE);
                    albumRecyclerView.setAdapter(albumAdapter);
                }

                if (artistModels.size()>0) {
                    localAdapter = new LocalAdapter(activity,artistModels,false);
                    setAdapterClicks1();
                    artistText.setVisibility(View.VISIBLE);
                    artistRecyclerView.setVisibility(View.VISIBLE);
                    artistRecyclerView.setAdapter(localAdapter);
                }
            }else {
                noResultText.setVisibility(View.VISIBLE);
            }

            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            /** Get all the files from local folder */

            File local = new File(activity.getFilesDir(),"locals");
            if (local.list().length>0) {
                for (File file : local.listFiles()) {
                    String data = YTutils.readContent(activity,file.getPath());
                    if (data.isEmpty()) continue;
                    String[] items = data.split("\n|\r");
                    if (items.length>0) {
                        for (String line : items) {
                            if (line.isEmpty()) continue;

                            /** Filter file with the list */

                            /** Filter for song recyclerView */
                            if (line.split("\\|")[0].toLowerCase().contains(text.toLowerCase())) {
                                String[] childs = line.split("\\|");
                                OFModel model = new OFModel(childs[1],childs[0],Integer.parseInt(childs[3]));
                                model.setDuration(Long.parseLong(childs[3]));
                                model.setDate(Long.parseLong(childs[4]));
                                yturls.add(childs[0]);
                                ofModels.add(model);
                            }

                            /** Filter for artist recyclerView */
                            String artist = line.split("\\|")[1];
                            String album = line.split("\\|")[2];
                            if (artist.toLowerCase().contains(text.toLowerCase())) {
                               /* if (!albumCount.contains(album))
                                    albumCount.add(album);*/
                                if (artistKey.contains(artist)) {
                                    int index = artistKey.indexOf(artist);
                                    artistValueList.get(index).add(line);
                                    if (!albumValueCountList.get(index).contains(album))
                                        albumValueCountList.get(index).add(album);
                                }else {
                                    artistKey.add(artist);
                                    ArrayList<String> strings = new ArrayList<>();
                                    strings.add(line);
                                    artistValueList.add(strings);
                                    ArrayList<String> albums = new ArrayList<>();
                                    albums.add(album);
                                    albumValueCountList.add(albums);
                                }
                            }

                            /** Filter for album recyclerView */
                            if (album.toLowerCase().contains(text.toLowerCase())) {
                                if (albumKey.contains(album)) {
                                    int index = albumKey.indexOf(album);
                                    albumValueList.get(index).add(line);
                                }else {
                                    albumKey.add(album);
                                    ArrayList<String> strings = new ArrayList<>();
                                    strings.add(line);
                                    albumValueList.add(strings);
                                }
                            }
                        }
                    }
                }
                if (albumKey.size()>0) {
                    for (int i=0;i<albumKey.size();i++) {
                        albumModels.add(new LocalModel(albumKey.get(i),albumValueList.get(i),0));
                    }
                }
                Log.e("LocalSearchFragment", "doInBackground: "+albumModels.size() );
                if (artistKey.size()>0) {
                    for (int i=0;i<artistKey.size();i++) {
                        artistModels.add(new LocalModel(artistKey.get(i),artistValueList.get(i), albumValueCountList.get(i).size()));
                    }
                }
            }
            return null;
        }

        void setAdapterClicks2() {
            albumAdapter.setSingleClickListener((view, model, position) -> {
                openClick(model);
            });


            albumAdapter.setLongClickListener((view, model, position) -> {
                PopupMenu popupMenu = new PopupMenu(activity,view);
                popupMenu.inflate(R.menu.local_popup_menu3);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()){
                        case R.id.action_play:
                            albumPlay(model);
                            break;
                        case R.id.action_add_queue:
                            if (MainActivity.yturls.isEmpty()) {
                                albumPlay(model);
                            }else {
                                AddItems(model);
                            }
                            break;
                    }
                    return true;
                });
                popupMenu.show();
            });
        }

        void AddItems(LocalModel localModel) {
            boolean someThingAdded=false;
            for (String line : localModel.getSongList()) {
                if (line.isEmpty()) continue;
                String filePath = line.split("\\|")[0];
                if (!MainActivity.videoID.equals(filePath)) {
                    if (!MainActivity.yturls.contains(filePath))
                    {
                        someThingAdded=true;
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

        void setAdapterClicks1() {
            localAdapter.setSingleClickListener((view, model,position) -> {
                openClick(model);
            });

            localAdapter.setLongClickListener((view, model, position) -> {
                PopupMenu popupMenu = new PopupMenu(activity,view);
                popupMenu.inflate(R.menu.local_popup_menu3);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()){
                        case R.id.action_play:
                            albumPlay(model);
                            break;
                        case R.id.action_add_queue:
                            if (MainActivity.yturls.isEmpty()) {
                                albumPlay(model);
                            }else {
                                AddItems(model);
                            }
                            break;
                    }
                    return true;
                });
                popupMenu.show();
            });
        }

        void openClick(LocalModel model) {
            Bundle args = new Bundle();
            args.putSerializable("model",model);
            args.putString("isLocalMusic","search");

            OPlaylistFragment fragment = new OPlaylistFragment();
            fragment.setArguments(args);
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in,
                    R.anim.fade_out);
            ft.addToBackStack(null).replace(R.id.fragment_container, fragment,"localMusic");
            ft.commit();
        }

        void setAdapterClicks() {
            ofAdapter.setSingleClickListener((v1, model, position) -> {
                PlayMusic_Offline(position);
            });

            ofAdapter.setLongClickListener((v1, model, position) -> {
                PopupMenu popupMenu = new PopupMenu(activity,v1);
                popupMenu.inflate(R.menu.local_popup_menu2);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.action_play:
                            PlayMusic_Offline(position);
                            break;
                        case R.id.action_ringtone:
                            YTutils.setDefaultRingtone(activity,new File(model.getPath()));
                            break;
                        case R.id.action_details:
                            Bundle args = new Bundle();
                            args.putString("filepath",model.getPath());
                            DetailsBottomSheet sheet = new DetailsBottomSheet();
                            sheet.setArguments(args);
                            sheet.show(activity.getSupportFragmentManager(),"");
                            break;
                        case R.id.action_play_next:
                            if (MainActivity.yturls.size()==0) {
                                PlayMusic_Offline(position);
                            }else {
                                insertPosition(model,position,false);
                            }
                            break;
                        case R.id.action_add_queue:
                            if (MainActivity.yturls.size()==0) {
                                PlayMusic_Offline(position);
                            }else {
                                insertPosition(model,position,true);
                            }
                            break;
                        case R.id.action_share:
                            File f = new File(model.getPath());
                            YTutils.shareFile(MainActivity.activity,f);
                            break;
                    }
                    return true;
                });
                popupMenu.show();
            });
        }

        void insertPosition(OFModel model, int position,boolean addToLast) {
            if (MainActivity.videoID.equals(model.getPath())) {
                Toast.makeText(activity, "Song is already playing!", Toast.LENGTH_SHORT).show();
            }else if (MainActivity.localPlayBack) {
                if (addToLast) {
                    MainActivity.yturls.remove(model.getPath());
                    MainActivity.yturls.add(model.getPath());
                }else {
                    int index = MainActivity.yturls.indexOf(MainActivity.videoID);
                    MainActivity.yturls.remove(model.getPath());
                    MainActivity.yturls.add(index+1,model.getPath());
                }
                Toast.makeText(activity, "Song added to queue", Toast.LENGTH_SHORT).show();
            }else {
                PlayMusic_Offline(position);
            }
        }

        @Override
        protected void onPreExecute() {
            preExeuteMethod();
            super.onPreExecute();
        }
    }

    void preExeuteMethod() {
        noResultText.setVisibility(View.GONE);
        tooManyText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        songText.setVisibility(View.GONE);
        albumText.setVisibility(View.GONE);
        artistRecyclerView.setVisibility(View.GONE);
        albumRecyclerView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        artistText.setVisibility(View.GONE);
        ofModels.clear();
        artistModels.clear();
        albumModels.clear();
        yturls.clear();
        if (albumAdapter!=null)
            albumAdapter.notifyDataSetChanged();
        if (localAdapter!=null)
            localAdapter.notifyDataSetChanged();
        if (ofAdapter!=null)
            ofAdapter.notifyDataSetChanged();
    }

    void getAllViews() {
        albumText = v.findViewById(R.id.albumText);
        albumRecyclerView = v.findViewById(R.id.album_recyclerView);
        searchEditText = v.findViewById(R.id.searchEditText);
        closeButton = v.findViewById(R.id.removeText);
        toolbar = v.findViewById(R.id.toolbar);
        progressBar = v.findViewById(R.id.progressBar);
        noResultText = v.findViewById(R.id.noResultText);
        songText = v.findViewById(R.id.songText);
        tooManyText = v.findViewById(R.id.tooManyText);
        artistText = v.findViewById(R.id.artistText);
        recyclerView = v.findViewById(R.id.my_recycler_view);
        artistRecyclerView = v.findViewById(R.id.artist_recyclerView);
    }

}
