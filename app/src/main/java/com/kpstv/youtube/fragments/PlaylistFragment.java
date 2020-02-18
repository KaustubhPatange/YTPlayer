package com.kpstv.youtube.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kpstv.youtube.BuildConfig;
import com.kpstv.youtube.CPlaylistActivity;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.PlaylistAdapter;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistFragment extends Fragment {

    View v;
    FragmentActivity activity;
    ProgressBar progressBar;
    boolean networkCreated;
    static String playlist_csv;
    ArrayList<PlaylistModel> data;
    FloatingActionButton fabCreate;
    RecyclerView recyclerView;
    PlaylistAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    Fragment OPlayFrag;
    NestedScrollView nestedScrollView;
    FragmentManager fragmentManager;
    private static final String TAG = "PlaylistFragment";

    public PlaylistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!networkCreated) {
            v = inflater.inflate(R.layout.fragment_playlist, container, false);

            data = new ArrayList<>();

            Toolbar toolbar = v.findViewById(R.id.toolbar);

            toolbar.setTitle("Playlist");
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(view -> MainActivity.loadLibraryFrag());

            activity = getActivity();
            fragmentManager = activity.getSupportFragmentManager();

            recyclerView = v.findViewById(R.id.my_recycler_view);
            progressBar = v.findViewById(R.id.progressBar);
            nestedScrollView = v.findViewById(R.id.nestedScrollView);
            fabCreate = v.findViewById(R.id.fab_create);
            layoutManager = new LinearLayoutManager(activity.getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);

            fabCreate.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CPlaylistActivity.class);
                activity.startActivity(intent);
            });

            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                if (scrollY > oldScrollY) {
                    fabCreate.hide();
                }
                if (scrollY < oldScrollY) {
                    fabCreate.show();
                }
            });

            toolbar.inflateMenu(R.menu.player_menu);
            toolbar.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.action_import:

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("text/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        try {
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select an audio file"),
                                    1);
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(activity, "No file manager installed", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case R.id.action_export:

                        YTutils.parseDataForPlaylist(activity, new File(activity.getFilesDir(), "playlist.csv").getPath(), true, true);

                        break;
                }
                return true;
            });

            networkCreated = true;
        }
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = YTutils.getPath(activity, uri);
            Log.e(TAG, "onActivityResult: " + path);

            YTutils.parseDataForPlaylist(activity,path);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onResume() {
        Log.e("onResume", "true");
        if (YTutils.isInternetAvailable())
            new getData(activity).execute();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e("onPause", "true");
        super.onPause();
    }


    void setAdapterClicks() {
        adapter.setSingleClickListener((v1, model, position) -> {
            OPlayFrag = new OPlaylistFragment();
            Bundle args = new Bundle();
            args.putSerializable("model", model);
            if (model.getData().size() > 0) {
                OPlayFrag.setArguments(args);
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in,
                        R.anim.fade_out);
                ft.replace(R.id.fragment_container, OPlayFrag);
                ft.commit();
            } else Toast.makeText(activity, "Playlist is empty!", Toast.LENGTH_SHORT).show();
        });

        adapter.setLongClickListener((v1, model, position) -> {
            String playlist_csv = YTutils.readContent(activity, "playlist.csv");
            PopupMenu popupMenu = new PopupMenu(activity, v1);
            popupMenu.inflate(R.menu.playlist_context);
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.action_open:
                        adapter.performSingleClick(v, model, position);
                        break;
                    case R.id.action_modify:
                        if (playlist_csv != null && !playlist_csv.isEmpty()) {
                            String[] lines = playlist_csv.split("\r|\n");
                            for (int i = 0; i < lines.length; i++) {
                                if (lines[i].contains("," + model.getTitle())) {
                                    Intent intent = new Intent(activity, CPlaylistActivity.class);
                                    intent.putExtra("line", lines[i]);
                                    activity.startActivity(intent);
                                }
                            }
                        }

                        break;
                    case R.id.action_delete:
                        if (playlist_csv != null && !playlist_csv.isEmpty()) {
                            if (model.getTimeseconds() > 0) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Delete")
                                        .setMessage("Are you sure? This cannot be undone.")
                                        .setNegativeButton("Cancel", null)
                                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                                            deleteFunc(playlist_csv, model);
                                        })
                                        .show();
                            } else
                                deleteFunc(playlist_csv, model);
                        }
                        break;
                    case R.id.action_export:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED)
                                commonExport(playlist_csv, model);
                            else
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                        } else commonExport(playlist_csv, model);
                        break;
                }
                return false;
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0) {
                Toast.makeText(activity, "Permission granted, try again exporting", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(activity, "Permission denied!", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void commonExport(String playlist_csv, PlaylistModel model) {
        if (playlist_csv != null && !playlist_csv.isEmpty()) {
            PSBottomSheet bottomSheet = new PSBottomSheet();
            bottomSheet.setListener(id -> {
                if (id == 0) {
                    savePlayListLocal(model);
                } else if (id == 1) {
                    String fileStore = savePlayListLocal(model);
                    if (fileStore != null) {
                                       /* ShareCompat.IntentBuilder.from(activity)
                                                .setStream(Uri.fromFile(new File(fileStore)))
                                                .setType("text/html")
                                                .startChooser();*/
                        File f = new File(fileStore);

                        //  Uri contentUri = YTutils.getImageContentUri(activity, f);
                        Uri uriForFile = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", f);

                        Log.e(TAG, "setAdapterClicks: uriForFile" + uriForFile);

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, uriForFile);
                        intent.setType("application/octet-stream");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, "Share"));
                    }
                }
                bottomSheet.dismiss();
            });
            bottomSheet.show(activity.getSupportFragmentManager(), "tag");
        }
    }

    private String savePlayListLocal(PlaylistModel model) {
        String[] items = playlist_csv.split("\n|\r");
        if (items.length > 0 && model.getData().size() > 0) {
            for (String line : items) {
                if (line.contains("," + model.getTitle() + ",")) {
                    File playlist_dir = YTutils.getFile("Playlists");
                    if (!playlist_dir.exists())
                        playlist_dir.mkdirs();
                    String fileName = YTutils.getFile("Playlists/" + model.getTitle() + ".txt").getPath();
                    YTutils.writeContent(activity,
                            fileName,
                            activity.getResources().getString(R.string.header_playlist) + "\n" +
                                    line);
                    Toast.makeText(activity, "Exported to " + fileName, Toast.LENGTH_SHORT).show();
                    return fileName;
                }
            }
            return null;
        } else {
            Toast.makeText(activity, "Warning: Playlist is empty!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void deleteFunc(String playlist_csv, PlaylistModel model) {
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(playlist_csv.split("\r|\n")));
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("," + model.getTitle())) {
                data.remove(data.size() - 1 - i);
                lines.remove(i);
                YTutils.writeContent(activity, "playlist.csv",
                        YTutils.convertListToStringMethod(lines));
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    class getData extends AsyncTask<Void, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        Activity activity;

        public getData(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (data.size() > 0) {
                adapter = new PlaylistAdapter(data, activity);
                setAdapterClicks();
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            playlist_csv = YTutils.readContent(activity, "playlist.csv");
            if (playlist_csv != null && !playlist_csv.isEmpty()) {
                data.clear();
                for (String line : playlist_csv.split("\n|\r")) {
                    if (line != null && !line.isEmpty()) {
                        ArrayList<String> list = new ArrayList<>();
                        String[] lines = line.split(",");
                        for (int i = 2; i < lines.length; i++) {
                            //  Log.e(TAG, "doInBackground: "+lines[i] );
                            list.add(lines[i]);
                        }
                        //Log.e(TAG, "doInBackground: "+lines[1] );
                        data.add(0, new PlaylistModel(
                                lines[0],
                                lines[1],
                                list
                        ));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            super.onPreExecute();
        }
    }

}
