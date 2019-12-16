package com.kpstv.youtube.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity2;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.OFAdapter;
import com.kpstv.youtube.models.OFModel;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;


public class LocalMusicFragment extends Fragment {
    public LocalMusicFragment() {}

    View v; FragmentActivity activity;
    RecyclerView recyclerView; Toolbar toolbar;
    LinearLayoutManager manager; LinearLayout noLayout;
    OFAdapter adapter; ArrayList<OFModel> models;
    AsyncTask<Void,String,Void> loadTask;
    Fragment localFrag;
    private static final String TAG = "LocalMusicFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v==null) {
            v = inflater.inflate(R.layout.fragment_local_music, container, false);
            activity = getActivity();

            toolbar = v.findViewById(R.id.toolbar);
            toolbar.setTitle("Local Folders");
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(view -> MainActivity.loadLibraryFrag());
            toolbar.inflateMenu(R.menu.of_menu);

            toolbar.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.action_rescan:
                        if (loadTask!=null && loadTask.getStatus()!= AsyncTask.Status.RUNNING)
                            loadTask.cancel(true);
                        loadTask = new loadSaveData(true);
                        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case R.id.action_search:
                        MainActivity.localSearchFrag = new LocalSearchFragment();
                        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.setCustomAnimations(android.R.anim.fade_in,
                                android.R.anim.fade_out);
                        ft.replace(R.id.fragment_container, MainActivity.localSearchFrag);
                        ft.commit();
                        break;
                }
                return true;
            });

            recyclerView = v.findViewById(R.id.recyclerView);
            models = new ArrayList<>();
            noLayout = v.findViewById(R.id.noLocalLayout);
            manager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(manager);

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            100);
                }else {
                    loadTask = new loadSaveData(false);
                    loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
        return v;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadTask = new loadSaveData(false);
                    loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Toast.makeText(activity, "Permission denied!", Toast.LENGTH_SHORT).show();
                    MainActivity.loadLibraryFrag();
                    v=null;
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode== 110) {
            loadTask = new loadSaveData(false);
            loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    void registerAdapterClicks() {
        adapter.setSingleClickListener(new OFAdapter.SingleClickListener() {
            @Override
            public void onSingleClick(View v, OFModel model, int position) {
                Bundle args = new Bundle();
                args.putSerializable("model",model);
                args.putString("isLocalMusic","true");
                localFrag = new OPlaylistFragment();
                localFrag.setArguments(args);
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                ft.replace(R.id.fragment_container, localFrag,"localMusic");
                ft.commit();
            }
        });

        adapter.setLongClickListener(new OFAdapter.LongClickListener() {
            @Override
            public void onLongClick(View v, OFModel model, int position) {

                PopupMenu popupMenu = new PopupMenu(activity,v);
                popupMenu.inflate(R.menu.local_popup_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.action_play:
                                File f = new File(model.getPath());
                                File[] files = f.listFiles(new FileFilter() {
                                    @Override
                                    public boolean accept(File file)
                                    {
                                        return (file.getPath().endsWith(".mp3")||file.getPath().endsWith(".m4a")
                                                ||file.getPath().endsWith(".wav")||file.getPath().endsWith(".aac")
                                                ||file.getPath().endsWith(".ogg")||file.getPath().endsWith(".flac"));
                                    }
                                });
                                if (files.length>0) {
                                    String[] urls = new String[files.length];
                                    for (int i=0;i<urls.length;i++)
                                        urls[i] = files[i].getPath();
                                    MainActivity.PlayVideo_Local(urls);
                                }else
                                    Toast.makeText(activity, "Could not play songs!", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.action_delete:
                                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setIcon(android.R.drawable.stat_sys_warning)
                                        .setTitle("Delete")
                                        .setMessage("Are you sure? This can't be undone.")
                                        .setNegativeButton("Cancel",null)
                                        .setPositiveButton("Yes",(dialogInterface, i) -> {
                                            /*File csv = new File(model.getPath()); /storage/emulated/0/Download
                                            Log.e(TAG, "onMenuItemClick: "+csv.getPath());*/
                                            File folder = new File(model.getPath());
                                            if (MainActivity.yturls.size()>0 && MainActivity.yturls.get(0).contains(model.getPath())) {
                                                Toast.makeText(activity, "Folder is in use for playing song!", Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            YTutils.deleteRecursive(folder);
                                            File csv = new File(activity.getFilesDir(),
                                                    "/locals/"+model.getPath().replace("/","_")+".csv");
                                            csv.delete();

                                            Log.e(TAG, "onMenuItemClick: "+csv.getPath());

                                            String data = YTutils.readContent(activity,"fileList.csv");
                                            String[] items = data.trim().split("\n|\r");
                                            StringBuilder sb = new StringBuilder();
                                            if (items.length>0) {
                                                for (i=0;i<items.length;i++) {
                                                    if (items[i].isEmpty()) continue;
                                                    if (items[i].contains(model.getPath())) continue;
                                                    sb.append("\n").append(items[i]);
                                                }
                                                YTutils.writeContent(activity,"fileList.csv",sb.toString());
                                                models.remove(position);
                                                adapter.notifyDataSetChanged();
                                            }else {
                                                Toast.makeText(activity, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                activity.onBackPressed();
                                            }

                                        });
                                builder.create().show();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();

            }
        });
    }

    class loadSaveData extends AsyncTask<Void,String,Void> {

        TextView dtxtView; boolean loadDataFromLocal;
        AlertDialog alertdialog; boolean reload;
        StringBuilder builder = new StringBuilder();
        String data;
        public loadSaveData(boolean reloadData) {
            reload = reloadData;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (alertdialog!=null)
            {
                alertdialog.dismiss();
                Toast.makeText(activity, "Scanning completed!", Toast.LENGTH_SHORT).show();
            }
            if (models.size()>0) {
                adapter = new OFAdapter(activity,models);
                registerAdapterClicks();
                recyclerView.setAdapter(adapter);
            }else {
                noLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            models.clear();
            data = YTutils.readContent(activity,"fileList.csv");
            if ((data==null || data.isEmpty()) || reload) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_scan_dialog, null);
                dtxtView = dialogView.findViewById(R.id.textView);
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setCancelable(false);
                alert.setView(dialogView);
                alert.setNegativeButton("Cancel", (dialogInterface, i) -> {
                    alertdialog.dismiss();
                    loadTask.cancel(true);
                    models.clear();
                    adapter.notifyDataSetChanged();
                    try {
                        File f = activity.getFilesDir();
                        f.delete();
                        f.mkdir();
                    }catch (Exception ignored){}
                    Toast.makeText(activity, "Scanning interrupted!", Toast.LENGTH_SHORT).show();
                });
                alertdialog = alert.create();
                alertdialog.show();
            }else {
                loadDataFromLocal=true;
            }
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            File f = new File(activity.getFilesDir(),"locals");
            if (!f.exists())
                f.mkdir();

            if (loadDataFromLocal) {
                String[] items = data.split("\n|\r");
                Log.e(TAG, "doInBackground: " + items.length);
                for (String line : items) {
                    if (line.isEmpty()) continue;
                    String path = line.split("\\|")[0];
                    String count = line.split("\\|")[1];
                    String duration = line.split("\\|")[2];
                    f = new File(path);

                    OFModel model = new OFModel(
                            f.getName(),f.getPath(),Integer.parseInt(count)
                    );
                    model.setDuration(Long.parseLong(duration));
                    models.add(model);
                }

                return null;
            }

            File root = Environment.getExternalStorageDirectory();
            searchRecursive(root);

            // Scan non-primary volume i.e Memory card

            String[] volumes = YTutils.getExternalStorageDirectories(activity);
            if (volumes.length>0) {
                File sd = new File(volumes[0]);
                searchRecursive(sd);
            }

            YTutils.writeContent(activity,"fileList.csv",builder.toString());
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dtxtView.setText(values[0]);
            super.onProgressUpdate(values);
        }

        void searchRecursive(File root) {
            publishProgress(root.getPath());
            File[] files = root.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file)
                {
                    return (file.getPath().endsWith(".mp3")||file.getPath().endsWith(".m4a")
                            ||file.getPath().endsWith(".wav")||file.getPath().endsWith(".aac")
                            ||file.getPath().endsWith(".ogg")||file.getPath().endsWith(".flac"));
                }
            });
            if (files.length>0) {

                /** Parsing Media Length... */

                StringBuilder localFileBuilder = new StringBuilder();
                long seconds = 0;
                for (File f : files) {
                    Uri uri = Uri.fromFile(f);
                   try {
                       MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                       mmr.setDataSource(activity,uri);
                       String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                       String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                       String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

                       if (artist==null) artist ="Unknown artist";
                       if (album==null) album = "Unknown album";

                       int s = Integer.parseInt(durationStr)/1000;
                       seconds+=s;
                       if (artist.contains("|"))
                           artist = artist.replace("|","");
                       if (album.contains("|")) album = album.replace("|","");
                       localFileBuilder.append("\n").append(f.getPath().trim()).append("|").append(artist.trim()).append("|")
                               .append(album.trim()).append("|").append(s).append("|").append(YTutils.getDate(new Date(f.lastModified())));
                   }catch (Exception e) {
                       Log.e(TAG, "searchRecursive: "+uri.toString());
                   }
                }
                String name=root.getPath().replace("/","_")+".csv";
                File toSave = new File(activity.getFilesDir(),"locals/"+name);
                YTutils.writeContent(activity,toSave.getPath(),localFileBuilder.toString());

                builder.append("\n").append(root.getPath().trim()).append("|").append(files.length).append("|").append(seconds);
                OFModel model = new OFModel(
                        root.getName(),
                        root.getPath(),
                        files.length
                );
                model.setDuration(seconds);
                models.add(model);
            }
            files = root.listFiles();
            for (File file : files) {
                if (file.isDirectory())
                    searchRecursive(file);
            }
        }


    }

}
