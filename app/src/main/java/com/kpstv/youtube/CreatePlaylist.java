package com.kpstv.youtube;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTLength;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

public class CreatePlaylist extends AppCompatActivity {
    RecyclerView recyclerView; ArrayList<DiscoverModel> models;
    SongAdapter adapter; EditText playlistText;
    static RecyclerView.LayoutManager layoutManager;
    String playlist_csv; String date; ProgressBar progressBar;
    LinearLayout mainLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        setTitle("Create");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        models = new ArrayList<>();
        playlistText = findViewById(R.id.cPlaylistText);
        mainLayout = findViewById(R.id.createLayout);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        String pline =  intent.getStringExtra("line");
        if (pline!=null&&!pline.isEmpty()) {
            // Get data of playlist line
            mainLayout.setVisibility(View.GONE);
            new getIntentData(pline).execute();
        }

        adapter = new SongAdapter(models,this,true);
        recyclerView.setAdapter(adapter);

        playlist_csv = YTutils.readContent(this,"playlist.csv");
    }

    class getIntentData extends AsyncTask<Void,Void,Void> {

        String pline;
        public getIntentData(String line) {
            pline = line;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mainLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            adapter = new SongAdapter(models,CreatePlaylist.this,true);
            recyclerView.setAdapter(adapter);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            date = pline.split(",")[0];
            playlistText.setText(pline.split(",")[1]);
            String[] lines = pline.split(",");
            for(int i=2;i<lines.length;i++) {
                String videoID = pline.split("\\|")[0];
                YTMeta ytMeta = new YTMeta(videoID);
                YTLength ytLength = new YTLength(videoID);
                if (ytMeta.getVideMeta()!=null) {
                    DiscoverModel model = new DiscoverModel(
                            ytMeta.getVideMeta().getTitle(),
                            ytMeta.getVideMeta().getAuthor(),
                            ytMeta.getVideMeta().getImgUrl(),
                            YTutils.getYtUrl(videoID)
                    );
                    model.setSeconds(ytLength.getSeconds());
                    models.add(model);
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_playlist_menu,menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_new) {
            showAlertWithEditText();
        }else if (item.getItemId()==R.id.action_save) {
            String title = playlistText.getText().toString();
            if (!title.isEmpty()) {
                if (playlist_csv!=null && playlist_csv.contains(","+title+",")) {
                    // Update playList
                    String[] lines = playlist_csv.split("\n|\r");
                    StringBuilder builder = new StringBuilder();
                    for (int i=0;i<lines.length;i++) {
                        if (lines[i].contains(","+title+",")) {
                            lines[i] = createPlayListLine(title);
                        }
                        builder.append(lines[i]).append("\n");
                    }
                    YTutils.writeContent(this,"playlist.csv",builder.toString());
                }else {
                    // Add to new playlist
                    playlist_csv+=createPlayListLine(title);
                    YTutils.writeContent(this,"playlist.csv",playlist_csv);
                }
                finish();
            }else Toast.makeText(this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    String createPlayListLine(String title) {
        if (date==null) {
            date = YTutils.getTodayDate();
        }
        String line = date+","+
                title;
        for (DiscoverModel model : models) {
            line+=","+YTutils.getVideoID(model.getYtUrl())+"|"+model.getSeconds();
        }
        return line;
    }

    void showAlertWithEditText() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edittextalert, null);
        final EditText edittext = dialogView.findViewById(R.id.editText);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Enter YouTube url or Spotify song url in the field.");
        alert.setTitle("Add Song");
        alert.setView(dialogView);
        alert.setPositiveButton("Add", (dialog, whichButton) -> {
            String textToSearch = edittext.getText().toString();
            if (textToSearch.contains("open.spotify.com") && textToSearch.contains("/track/")) {
                new spotifySearch(textToSearch).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else if (textToSearch.contains("youtube.com")||textToSearch.contains("youtu.be")) {
                new youtubeSearch(textToSearch).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        alert.show();
    }

    class youtubeSearch extends AsyncTask<Void,Void,Void> {
        String yturl;
        ProgressDialog dialog; boolean isadded;

        public youtubeSearch(String textToSearch) {
            this.yturl = textToSearch;
            dialog = new ProgressDialog(CreatePlaylist.this);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            if (isadded)
                adapter.notifyItemInserted(models.size()-1);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String videoID = YTutils.getVideoID(yturl);
            YTMeta ytMeta = new YTMeta(videoID);
            YTLength ytLength = new YTLength(videoID);
            if (ytMeta.getVideMeta() != null) {
                DiscoverModel model = new DiscoverModel(
                        ytMeta.getVideMeta().getTitle(),
                        ytMeta.getVideMeta().getAuthor(),
                        ytMeta.getVideMeta().getImgUrl(),
                        "https://www.youtube.com/watch?v=" + videoID
                );
                Log.e("YTSearch_Text",model.getTitle()+"");
                model.setSeconds(ytLength.getSeconds());
                models.add(model);
                isadded = true;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Parsing youtube url...");
            dialog.show();
            super.onPreExecute();
        }
    }


    class spotifySearch extends AsyncTask<Void,Void,Void> {
        SpotifyTrack track;
        String textToSearch;
        ProgressDialog dialog; boolean isadded;
        public spotifySearch(String textToSearch) {
            this.textToSearch = textToSearch;
            dialog = new ProgressDialog(CreatePlaylist.this);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            if (isadded)
                adapter.notifyItemInserted(models.size()-1);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String id = YTutils.getSpotifyID(textToSearch);
            if (id!=null) {
                track = new SpotifyTrack(id);
                YTLength ytLength = new YTLength(YTutils.getVideoID(track.getYtUrl()));
                if (track.getTitle()!=null) {
                    DiscoverModel model = new DiscoverModel(
                            track.getTitle(),track.getAuthor(),track.getImageUrl(),track.getYtUrl()
                    );
                    model.setSeconds(ytLength.getSeconds());
                    models.add(model);
                    isadded = true;
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Parsing spotify url...");
            dialog.show();
            super.onPreExecute();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
