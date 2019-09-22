package com.kpstv.youtube;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.SpotifyPlaylist;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTLength;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import at.huber.youtubeExtractor.YouTubeUriExtractor;

public class CPlaylistActivity extends AppCompatActivity {
    RecyclerView recyclerView; ArrayList<DiscoverModel> models;
    SongAdapter adapter; EditText playlistText;
    static RecyclerView.LayoutManager layoutManager;
    String playlist_csv; String date; ProgressBar progressBar;
    LinearLayout mainLayout; int current_to_save=-1;
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

        adapter = new SongAdapter(models,this,true,false,recyclerItemListener);
        recyclerView.setAdapter(adapter);

        playlist_csv = YTutils.readContent(this,"playlist.csv");
        if (playlist_csv==null) playlist_csv="";

        if (pline!=null&&!pline.isEmpty()) {
            // Get data of playlist line
            mainLayout.setVisibility(View.GONE);
            new getIntentData(pline).execute();
        }
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
            playlistText.setText(pline.split(",")[1]);
            adapter = new SongAdapter(models,CPlaylistActivity.this,true,false,recyclerItemListener);
            recyclerView.setAdapter(adapter);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            date = pline.split(",")[0];
            String[] plines = playlist_csv.split("[\n\r]");
            for(int i=0;i<plines.length;i++) {
                if (pline.equals(plines[i])) {
                    current_to_save = i;
                    Log.e("CurrentToSave",i+"");
                }
            }
            String[] lines = pline.split(",");
            for(int i=2;i<lines.length;i++) {
                String videoID = lines[i].split("\\|")[0];
                YTMeta ytMeta = new YTMeta(videoID);
                if (ytMeta.getVideMeta()!=null) {
                    DiscoverModel model = new DiscoverModel(
                            ytMeta.getVideMeta().getTitle(),
                            ytMeta.getVideMeta().getAuthor(),
                            ytMeta.getVideMeta().getImgUrl(),
                            YTutils.getYtUrl(videoID)
                    );
                    model.setSeconds(Integer.parseInt(lines[i].split("\\|")[1]));
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
        }else if (item.getItemId()==R.id.action_url) {
            showAlertWithEditText1();
        }else if (item.getItemId()==R.id.action_save) {
            String title = playlistText.getText().toString();
            if (!title.isEmpty()) {
                String[] lines = playlist_csv.split("\n|\r");
                if (playlist_csv!=null && playlist_csv.contains(","+title)) {
                    // Update playList
                    StringBuilder builder = new StringBuilder();
                    for (int i=0;i<lines.length;i++) {
                        if (lines[i].contains(","+title)) {
                            lines[i] = createPlayListLine(title);
                        }
                        builder.append(lines[i]).append("\n");
                    }
                    YTutils.writeContent(this,"playlist.csv",builder.toString());
                }else {
                    // Add to new playlist
                    if (current_to_save!=-1) {
                        Log.e("UpdatingList","true");
                       lines[current_to_save] = createPlayListLine(title);
                       playlist_csv = YTutils.convertArrayToStringMethod(lines);
                    }else playlist_csv+=createPlayListLine(title);
                    YTutils.writeContent(this,"playlist.csv",playlist_csv);
                }
                finish();
            }else Toast.makeText(this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener recyclerItemListener = view -> {
        int position = (int)view.getTag();
        Log.e("PositionTORemove",position+"");
        int icon = android.R.drawable.ic_dialog_alert;
        new AlertDialog.Builder(CPlaylistActivity.this)
                .setTitle("Delete")
                .setMessage("Are you sure to delete selected item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    models.remove(position);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("No",null)
                .setIcon(icon)
                .show();
    };


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

    void showAlertWithEditText1() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edittextalert, null);
        final EditText edittext = dialogView.findViewById(R.id.editText);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Enter YouTube or Spotify playlist url in the field.");
        alert.setTitle("Add Playlist");
        alert.setView(dialogView);
        alert.setPositiveButton("Add", (dialog, whichButton) -> {
            String urltosearch = edittext.getText().toString();
            if (urltosearch.contains("open.spotify.com") && urltosearch.contains("/playlist/")) {
                new spotifyplaylist(urltosearch).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else if (urltosearch.contains("youtube.com")||urltosearch.contains("youtu.be")) {
                new youtubeplaylist(urltosearch).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else Toast.makeText(this, "Bad search code!", Toast.LENGTH_SHORT).show();
        });
        alert.show();
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
            }else Toast.makeText(this, "Bad search code!", Toast.LENGTH_SHORT).show();
        });
        alert.show();
    }

    class youtubeplaylist extends AsyncTask<Void,String,Void> {

        String url;
        ProgressDialog dialog;
        public youtubeplaylist(String url) {
            this.url = url;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            adapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String id = YTutils.getVideoID(url);
                JSONObject mainJson = new JSONObject(new HttpHandler().makeServiceCall(
                        "https://www.googleapis.com/youtube/v3/playlistItems?playlistId=\"+id+\"&part=snippet&maxResults=50&key=AIzaSyCA2Py9snHNdp4Y4Dkyq-z7gUfxLqdPhtQ"));
                int current = 1;
                parseStructure(id,mainJson,current);
             }catch (Exception e){}
            return null;
        }

        void parseStructure(String id, JSONObject mainJson, int current) {
            try {
                JSONArray mainArrays = mainJson.getJSONArray("items");
                int total = Integer.parseInt(mainJson.getJSONObject("pageInfo")
                        .getString("totalResults"));
                if (mainArrays.length()<total) {
                    for(int i=0;i<mainArrays.length();i++) {
                        publishProgress("Parsing video "+current+"/"+total+"...");
                        models.add(parseVideo(mainArrays.getJSONObject(i)));
                        current++;
                    }
                    if (mainJson.getJSONObject("etag").has("nextPageToken")) {
                        String Nexttoken = mainJson.getJSONObject("etag").getString("nextPageToken");
                        JSONObject newJSON = new JSONObject(new HttpHandler().makeServiceCall(
                                "https://www.googleapis.com/youtube/v3/playlistItems?playlistId="+id+"pageToken="+Nexttoken+"&part=snippet&maxResults=50&key=AIzaSyCA2Py9snHNdp4Y4Dkyq-z7gUfxLqdPhtQ"));
                        parseStructure(id,newJSON,current);
                    }
                }
            }catch (Exception e){}
        }

        DiscoverModel parseVideo(JSONObject object) {
            try {
                String videoID =  object.getJSONObject("snippet").getJSONObject("resourceId")
                        .getString("videoId");
                YTLength ytLength = new YTLength(videoID);
                DiscoverModel model = new DiscoverModel(
                        object.getJSONObject("snippet").getString("title"),
                        object.getJSONObject("snippet").getString("channelTitle"),
                        object.getJSONObject("snippet").getJSONObject("thumbnails")
                        .getJSONObject("medium").getString("url"),
                        YTutils.getYtUrl(videoID)
                        );
                model.setSeconds(ytLength.getSeconds());
                return model;
            }catch (Exception e){}
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setMessage(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setMessage("Parsing youtube playlist");
            dialog.show();
            super.onPreExecute();
        }
    }

    class spotifyplaylist extends AsyncTask<Void,String,Void> {
        String url;
        ProgressDialog dialog;
        public spotifyplaylist(String url) {
            this.url = url;
            dialog = new ProgressDialog(CPlaylistActivity.this);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            adapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SpotifyPlaylist playlist = new SpotifyPlaylist(YTutils.getSpotifyID(url));
            ArrayList<String> songs = playlist.getSpotifyUrls();
            if (songs!=null) {
                for (int i=0;i<songs.size();i++) {
                    publishProgress("Parsing song "+(i+1)+"/"+songs.size()+"...");
                    SpotifyTrack track = new SpotifyTrack(YTutils.getSpotifyID(songs.get(i)));
                    YTLength ytLength = new YTLength(YTutils.getVideoID(track.getYtUrl()));
                    DiscoverModel model = new DiscoverModel(
                            track.getTitle(),track.getAuthor(),track.getImageUrl(),track.getYtUrl()
                    );
                    model.setSeconds(ytLength.getSeconds());
                    models.add(model);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setMessage(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setMessage("Parsing playlist songs...");
            dialog.show();
            super.onPreExecute();
        }
    }

    class youtubeSearch extends AsyncTask<Void,Void,Void> {
        String yturl;
        ProgressDialog dialog; boolean isadded;

        public youtubeSearch(String textToSearch) {
            this.yturl = textToSearch;
            dialog = new ProgressDialog(CPlaylistActivity.this);
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
            if (checkifItemExist(yturl))
                return null;
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
    boolean checkifItemExist(String ytUrl) {
        for(DiscoverModel model:  models) {
            if (model.getYtUrl().contains(ytUrl))
                return true;
        }
        return false;
    }

    class spotifySearch extends AsyncTask<Void,Void,Void> {
        SpotifyTrack track;
        String textToSearch;
        ProgressDialog dialog; boolean isadded;
        public spotifySearch(String textToSearch) {
            this.textToSearch = textToSearch;
            dialog = new ProgressDialog(CPlaylistActivity.this);
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
                if (checkifItemExist(track.getYtUrl()))
                    return null;
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
