package com.kpstv.youtube;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.utils.APIResponse;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTLength;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;


public class CPlaylistActivity extends AppCompatActivity {
    RecyclerView recyclerView; ArrayList<DiscoverModel> models;
    SongAdapter adapter; EditText playlistText;
    static RecyclerView.LayoutManager layoutManager;
    String playlist_csv; String date; ProgressBar progressBar;
    LinearLayout mainLayout; int current_to_save=-1;

    ArrayList<TrackModel> trackModels;
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "https://kaustubhpatange.github.io/YTPlayer";
    public static final String CLIENT_ID = "ff0d06a6f7c943d9bb0a0e2167efaa1d";
    private static final String TAG = "CPlaylistActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        setTitle("Create");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        models = new ArrayList<>();
        trackModels = new ArrayList<>();
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
                String[] childs = lines[i].split("\\|");
                String videoID = childs[0];
                int seconds = Integer.parseInt(childs[1]);
                DiscoverModel model = new DiscoverModel(
                        childs[2],
                        childs[3],
                        childs[4],
                        YTutils.getYtUrl(videoID)
                );
                model.setSeconds(seconds);
                models.add(model);
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
            line +=","+YTutils.getVideoID(model.getYtUrl())+"|"+model.getSeconds()+"|"+model.getTitle()+"|"+ model.getAuthor()+"|"+model.getImgUrl();
        }
        return line;
    }

    AsyncTask<Void,String,Void> spotifyPlayList; CircularProgressBar circularProgressBar;
    AlertDialog alertDialog; TextView titleAuthorText, currentTextView;
    boolean toastDisplayOnce=false;
    void showAlertWithEditText1() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edittextalert, null);
        final EditText edittext = dialogView.findViewById(R.id.editText);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String tocheck = editable.toString();
                if (!tocheck.isEmpty()) tocheck = URLDecoder.decode(tocheck);
                if (tocheck.contains("open.spotify.com") &&
                        tocheck.contains("/playlist/")) {
                    Log.e(TAG, "afterTextChanged: Contains" );
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                    if (!toastDisplayOnce) {
                        toastDisplayOnce=true;
                        Toast.makeText(CPlaylistActivity.this,
                                "You need to connect Spotify app first", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Log.e(TAG, "afterTextChanged: Not Contains" );
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
                }
            }
        });

        alert.setMessage("Enter YouTube or Spotify playlist url in the field.");
        alert.setTitle("Add Playlist");
        alert.setView(dialogView);
        alert.setPositiveButton("Add", (dialog, whichButton) -> {
            String urltosearch = edittext.getText().toString();
            if (urltosearch.contains("open.spotify.com") && urltosearch.contains("/playlist/")) {
                trackModels.clear();

                View v = getLayoutInflater().inflate(R.layout.alert_progress,null);
                titleAuthorText = v.findViewById(R.id.textView);
                currentTextView = v.findViewById(R.id.textView1);
                circularProgressBar = v.findViewById(R.id.progressBar);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(v);
                builder.setCancelable(false);
                builder.setPositiveButton("Cancel",(dialogInterface, i) -> {
                    if (spotifyPlayList!=null && spotifyPlayList.getStatus()== AsyncTask.Status.RUNNING) {
                        Log.e(TAG, "showAlertWithEditText1: Stopping it" );
                        do {spotifyPlayList.cancel(true);}while (!spotifyPlayList.isCancelled());
                        trackModels.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
                globalAlertDialog = builder.create();
                globalAlertDialog.show();
                circularProgressBar.setIndeterminateMode(true);

                parseData(0,0,YTutils.getSpotifyID(urltosearch));
            }else if (urltosearch.contains("youtube.com")||urltosearch.contains("youtu.be")) {
                if (spotifyPlayList!=null && spotifyPlayList.getStatus()== AsyncTask.Status.RUNNING)
                    spotifyPlayList.cancel(true);
                spotifyPlayList = new youtubeplaylist(urltosearch);
                spotifyPlayList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
               // new youtubeplaylist(urltosearch).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else Toast.makeText(this, "Bad search code!", Toast.LENGTH_SHORT).show();
        });
        alert.setNeutralButton("Connect", null);
        alertDialog = alert.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> {
            String urltosearch =edittext.getText().toString();
            commonSpotifyConnect(urltosearch);
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
    }

    void commonSpotifyConnect(String urltosearch) {

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

    }

    String formatData(String text) {
        return text.replace(",","").replace("|","");
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
    String accessToken=null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Log.e(TAG, "onActivityResult: "+response.getAccessToken() );
                    Log.e(TAG,"Expires in: "+response.getExpiresIn());

                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                    Toast.makeText(this, "Success: connected to Spotify app!", Toast.LENGTH_SHORT).show();

                    accessToken = response.getAccessToken();

                    break;

                case ERROR:
                    Log.e(TAG, "onActivityResult: "+response.getError() );
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Couldn't connect to Spotify!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class youtubeplaylist extends AsyncTask<Void,String,Void> {

        String url; int current,total;
        public youtubeplaylist(String url) {
            this.url = url;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            globalAlertDialog.dismiss();
            adapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String id = YTutils.getVideoID(url);
                APIResponse response = new APIResponse("https://www.googleapis.com/youtube/v3/playlistItems?playlistId="+id+"&part=snippet&maxResults=50");
                if (response.getJson()!=null) {
                    JSONObject mainJson = new JSONObject(response.getJson());
                    total = Integer.parseInt(mainJson.getJSONObject("pageInfo")
                            .getString("totalResults"));
                    Log.e(TAG, "doInBackground: Total:"+total);
                    parseStructure(id,mainJson);
                }
             }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        void parseStructure(String id, JSONObject mainJson) {
            try {
                JSONArray mainArrays = mainJson.getJSONArray("items");
                if (current<total) {
                    for(int i=0;i<mainArrays.length();i++) {
                       // publishProgress("Parsing video "+current+"/"+total+"...");
                        models.add(parseVideo(mainArrays.getJSONObject(i),i));
                        current++;
                    }
                    if (mainJson.has("nextPageToken")) {
                        String Nexttoken = mainJson.getString("nextPageToken");
                        APIResponse response = new APIResponse("https://www.googleapis.com/youtube/v3/playlistItems?playlistId="+id+"&pageToken="+Nexttoken+"&part=snippet&maxResults=50");
                        if (response.getJson()!=null) {
                            JSONObject newJSON = new JSONObject(response.getJson());
                            parseStructure(id, newJSON);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        DiscoverModel parseVideo(JSONObject object,int i) {
            try {
                String videoID =  object.getJSONObject("snippet").getJSONObject("resourceId")
                        .getString("videoId");
                YTLength ytLength = new YTLength(videoID);
                DiscoverModel model = new DiscoverModel(
                        formatData(object.getJSONObject("snippet").getString("title")),
                        formatData(object.getJSONObject("snippet").getString("channelTitle")),
                        object.getJSONObject("snippet").getJSONObject("thumbnails")
                        .getJSONObject("medium").getString("url"),
                        YTutils.getYtUrl(videoID)
                        );
                publishProgress(current+1+"",total+"",YTutils.getVideoTitle(model.getTitle()),
                        YTutils.getChannelTitle(model.getTitle(),model.getAuthor()));
                model.setSeconds(ytLength.getSeconds());
                return model;
            }catch (Exception e){}
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            currentTextView.setText("Current song ("+values[0]+"/"+values[1]+"):");
            titleAuthorText.setText(values[2]+" by "+values[3]);
            int curr = Integer.parseInt(values[0]);
            int total = Integer.parseInt(values[1]);

            circularProgressBar.setProgress((float)((float)curr*100.00/(float)total));
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            View v = getLayoutInflater().inflate(R.layout.alert_progress,null);
            titleAuthorText = v.findViewById(R.id.textView);
            currentTextView = v.findViewById(R.id.textView1);
            circularProgressBar = v.findViewById(R.id.progressBar);

            AlertDialog.Builder builder = new AlertDialog.Builder(CPlaylistActivity.this);
            builder.setView(v);
            builder.setCancelable(false);
            builder.setPositiveButton("Cancel",(dialogInterface, i) -> {
                if (spotifyPlayList!=null && spotifyPlayList.getStatus()== AsyncTask.Status.RUNNING) {
                    Log.e(TAG, "showAlertWithEditText1: Stopping it" );
                    do {
                        spotifyPlayList.cancel(true);
                    }while (!spotifyPlayList.isCancelled());
                    trackModels.clear();
                    adapter.notifyDataSetChanged();
                }
            });
            globalAlertDialog = builder.create();
            globalAlertDialog.show();
            super.onPreExecute();
        }
    }

    int current=0; int total;
    int offsetItem=0;
    void parseData(final int totalItems,int offset, String playlistId) {
        this.offsetItem = offset;
        RequestQueue queue = Volley.newRequestQueue(CPlaylistActivity.this);
        String url ="https://api.spotify.com/v1/playlists/"+playlistId+"/tracks?offset="+offset+"&limit=100&access_token="+
                accessToken;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.e(TAG, "Response success: ");
                    try {
                        JSONObject object = new JSONObject(response);

                        if (totalItems==0)
                            total = Integer.parseInt(object.getString("total"));
                        JSONArray array = object.getJSONArray("items");
                        for (int i=0;i<array.length();i++) {
                            current++;
                            JSONObject track = array.getJSONObject(i).getJSONObject("track");
                            JSONObject album = track.getJSONObject("album");
                            JSONObject artists = track.getJSONArray("artists").getJSONObject(0);

                            String title = album.getString("name");
                            String author = artists.getString("name");

                            trackModels.add(new TrackModel(title,author));
                        }
                        if (current<total-1) {
                            offsetItem = current;
                            parseData(totalItems,offsetItem+1,playlistId);
                        }else {
                            Log.e(TAG, "parseData: Analysis complete" );
                            if (spotifyPlayList!=null && spotifyPlayList.getStatus()== AsyncTask.Status.RUNNING)
                                spotifyPlayList.cancel(true);
                            spotifyPlayList = new spotifyplaylist();
                            spotifyPlayList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e(TAG, "Well that didn't work"));

        queue.add(stringRequest);
    }

    AlertDialog globalAlertDialog;
    class spotifyplaylist extends AsyncTask<Void,String,Void> {
        public spotifyplaylist() { }

        @Override
        protected void onPostExecute(Void aVoid) {
            globalAlertDialog.dismiss();
            adapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (trackModels.size()>0) {
                for (int i=0;i<trackModels.size();i++) {
                  //  publishProgress("Parsing song "+(i+1)+"/"+trackModels.size()+"...");
                    TrackModel trackModel = trackModels.get(i);
                    publishProgress(i+1+"",trackModels.size()+"",trackModel.getTitle(),trackModel.getAuthor());
                    try {
                        SpotifyTrack track = new SpotifyTrack(formatData(trackModel.getTitle()),formatData(trackModel.getAuthor()));
                        YTLength ytLength = new YTLength(YTutils.getVideoID(track.getYtUrl()));
                        DiscoverModel model = new DiscoverModel(
                                track.getTitle(),track.getAuthor(),track.getImageUrl(),track.getYtUrl()
                        );
                        model.setSeconds(ytLength.getSeconds());
                        models.add(model);
                    }catch (Exception e){
                        Log.e(TAG, "Failed: Title: "+trackModel.getTitle()+", Author: "+trackModel.getAuthor() );
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            currentTextView.setText("Current song ("+values[0]+"/"+values[1]+"):");
            titleAuthorText.setText(values[2]+" by "+values[3]);
            int curr = Integer.parseInt(values[0]);
            int total = Integer.parseInt(values[1]);

            circularProgressBar.setProgress((float)((float)curr*100.00/(float)total));

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            circularProgressBar.setIndeterminateMode(false);
            Log.e(TAG, "onPreExecute: Track Model Size: "+trackModels.size() );
            super.onPreExecute();
        }


    }

    class TrackModel {
        private String title,author;
        public TrackModel(String title, String author) {
            this.title = title;
            this.author = author;
        }
        public String getTitle() {
            return title;
        }
        public String getAuthor() {
            return author;
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
                        formatData( ytMeta.getVideMeta().getTitle()),
                        formatData(ytMeta.getVideMeta().getAuthor()),
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
                            formatData(track.getTitle()),formatData(track.getAuthor()),track.getImageUrl(),track.getYtUrl()
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
