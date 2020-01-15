package com.kpstv.youtube.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kpstv.youtube.AppInterface;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.utils.APIResponse;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTSearch;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class SFragment extends Fragment implements AppInterface {

    public SFragment() {}

    View v; boolean networkCreated;
    RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    CardView recyclerCard;
    TextView trendingText;
    AutoCompleteTextView searchEdit;
    ProgressBar progressBar;
    static String SongList;
    private SongAdapter adapter;
    private ArrayList<DiscoverModel> discoverModels;
    private Activity activity; boolean showTrend;
    private AsyncTask<Void, Void, Void> task;
    ImageView removeText;
    AsyncTask<Void,Void,String[]> suggestionTask;
    SharedPreferences preferences; String region="global";
    private static final String TAG = "SFragment"; boolean suppressAction=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if (!networkCreated) {
            v = inflater.inflate(R.layout.activity_search, container, false);
            activity = getActivity();

            Toolbar toolbar = v.findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(view -> {
                ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
                MainActivity.loadSearchFrag();
            });

            Bundle args = getArguments();
            SongList = args.getString("data_csv");

            preferences = activity.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
            if (preferences!=null) {
                region = preferences.getString("pref_select_region","global");
            }

            suggestionTask = new getAdapter();
            discoverModels = new ArrayList<>();
            progressBar = v.findViewById(R.id.progressBar);
            removeText = v.findViewById(R.id.removeText);
            trendingText = v.findViewById(R.id.trendingNow);
            searchEdit = v.findViewById(R.id.searchEditText);
            recyclerCard = v.findViewById(R.id.recyclerCard);
            recyclerView = v.findViewById(R.id.my_recycler_view);
            mLayoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(mLayoutManager);


        /*    ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,
                    new String[]{"trap","trap Nation","trap Music","trap city","trap country"});
            searchEdit.setAdapter(adapter);
            searchEdit.showDropDown();*/

            searchEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.toString().isEmpty())
                        removeText.setVisibility(View.GONE);
                    else removeText.setVisibility(View.VISIBLE);
                    Log.e(TAG, "afterTextChanged: Working" );
                    if (suppressAction) {
                        suppressAction = false;
                        return;
                    }
                    if (suggestionTask.getStatus()==AsyncTask.Status.RUNNING)
                        suggestionTask.cancel(true);
                    suggestionTask = new getAdapter();
                    suggestionTask.execute();
                }
            });

            removeText.setOnClickListener(view -> {
                searchEdit.setText("");
            });

            searchEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.e(TAG, "onItemClick: true" );
                    suppressAction=true;
                }
            });

            searchEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (!YTutils.isInternetAvailable()) {
                            Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        String textToSearch = searchEdit.getText().toString();
                        if (textToSearch.isEmpty())
                            return false;
                        searchEdit.setEnabled(false);
                        if (task.getStatus()== AsyncTask.Status.RUNNING)
                            task.cancel(true);
                        trendingText.setText("SEARCHING...");
                        trendingText.setVisibility(View.VISIBLE);
                        discoverModels.clear();
                        if (textToSearch.contains("open.spotify.com")) {
                            // Spotify url here
                            if (textToSearch.contains("/track/"))
                            {
                                String id = YTutils.getSpotifyID(textToSearch);
                                if (id!=null)
                                    new spotifySearch(textToSearch).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                else Toast.makeText(activity, "Could not extract track ID!", Toast.LENGTH_SHORT).show();
                            }
                            else if (textToSearch.contains("/playlist/")) {
                                showAlert("Note","Current spotify link seems to be a playlist.\n\n" +
                                        "It is recommend to go to the playlist menu from the app where you can manage this url!",true);
                            }else {
                                Toast.makeText(activity, "Seems to invalid spotify url", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            // Normal searching goes here
                            new normalSearch(textToSearch).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        return true;
                    }
                    return false;
                }
            });

            task = new getVirals();
            task.execute();
        }
        searchEdit.requestFocus();
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
       /* InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);*/
        return v;
    }

    @Override
    public void onPause() {
        if (task.getStatus()== AsyncTask.Status.RUNNING)
            task.cancel(true);
        super.onPause();
    }

    class getAdapter extends AsyncTask<Void,Void,String[]> {

        @Override
        protected void onPostExecute(String[] strings) {
            if (suppressAction) {
                suppressAction = false;
                return;
            }
            Log.e(TAG, "onPostExecute: Strings Size "+strings.length);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, strings);
            searchEdit.setAdapter(adapter);
            searchEdit.showDropDown();
            super.onPostExecute(strings);
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            if (suppressAction) {
                suppressAction = false;
                return new String[0];
            }
            HttpHandler handler = new HttpHandler();
            String json = handler.makeServiceCall("https://suggestqueries.google.com/complete/search?client=youtube&ds=yt&client=firefox&q="+
                    URLEncoder.encode(searchEdit.getText().toString()));
            if (json==null || json.isEmpty())
                return new String[0];
            Log.e(TAG, "doInBackground: "+json);
            try {
                JSONArray array = new JSONArray(json);
                JSONArray jsonArray = array.getJSONArray(1);
                String[] elements = new String[jsonArray.length()];
                for (int i=0; i<jsonArray.length();i++) {
                    elements[i] = jsonArray.getString(i);
                }
                return elements;
            }catch (Exception e){
                Log.e(TAG, "doInBackground: Error retrieving suggestion json" );
                e.printStackTrace();
            }
            return new String[0];
        }
    }


    class spotifySearch extends AsyncTask<Void,Void,Void> {

        SpotifyTrack track;
        String textToSearch;
        public spotifySearch(String textToSearch) {
            this.textToSearch = textToSearch;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            trendingText.setText("SEARCH RESULTS");
            if (track.getTitle()==null)
                trendingText.setText("NO RESULTS FOUND");
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            adapter = new SongAdapter(true,discoverModels,activity);
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            searchEdit.setEnabled(true);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String id = YTutils.getSpotifyID(textToSearch);
            if (id!=null) {
                track = new SpotifyTrack(id);
                if (track.getTitle()!=null) {
                    discoverModels.add(new DiscoverModel(
                            track.getTitle(),
                            track.getAuthor(),
                            track.getImageUrl(),
                            track.getYtUrl()
                    ));
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

    class normalSearch extends AsyncTask<Void,Void,Void> {
        String textToSearch;
        YTSearch ytSearch;
        public normalSearch(String textToSearch) {
            this.textToSearch = textToSearch;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            trendingText.setText("SEARCH RESULTS");
            if (ytSearch!=null && ytSearch.getVideoIDs().size()<=0)
                trendingText.setText("NO RESULTS FOUND");
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            adapter = new SongAdapter(true,discoverModels,activity);
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            searchEdit.setEnabled(true);
            super.onPostExecute(aVoid);
        }

   /*     String jsonResponse(int apinumber) {
            HttpHandler httpHandler = new HttpHandler();
            String link = "https://www.googleapis.com/youtube/v3/search?part=id%2Csnippet&type=video&maxResults=20&q=+"+URLEncoder.encode(textToSearch)+"&key="+ API_KEYS[apinumber];
            return httpHandler.makeServiceCall(link);
        }*/

        @Override
        protected Void doInBackground(Void... voids) {

            //https://www.googleapis.com/youtube/v3/search?part=id%2Csnippet&maxResults=20&q=trap&key=[YOUR_API_KEY]'

            String link ="https://www.googleapis.com/youtube/v3/search?part=id%2Csnippet&type=video&maxResults=20&q=+"+URLEncoder.encode(textToSearch);
            APIResponse response = new APIResponse(link);
            String json = response.getJson();
           /* int i=0;
            int apiLength = API_KEYS.length;
            String json = response.getJson();
            do {
                json = jsonResponse(i);
                i++;
            }while (json.contains("\"error\":") && i<apiLength);*/

            if (json.contains("\"error\"")) {
                ytSearch = new YTSearch(textToSearch);
                if (ytSearch.getVideoIDs().size()<=0) return null;
                for (String videoID: ytSearch.getVideoIDs()) {
                    YTMeta ytMeta = new YTMeta(videoID);
                    if (ytMeta.getVideMeta()!=null) {
                        discoverModels.add(new DiscoverModel(
                                ytMeta.getVideMeta().getTitle(),
                                ytMeta.getVideMeta().getAuthor(),
                                ytMeta.getVideMeta().getImgUrl(),
                                "https://www.youtube.com/watch?v="+videoID
                        ));
                    }
                }
            }else {
                /** Using YouTube Data Api... */
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONArray array = obj.getJSONArray("items");
                    for (int i=0;i<array.length();i++) {
                        JSONObject object = array.getJSONObject(i);

                        String videoID = object.getJSONObject("id").getString("videoId");

                        JSONObject snippet = object.getJSONObject("snippet");
                        discoverModels.add(new DiscoverModel(
                                snippet.getString("title"),
                                snippet.getString("channelTitle"),
                                YTutils.getImageUrlID(videoID),
                                YTutils.getYtUrl(videoID)
                        ));
                    }
                }catch (Exception e){
                    Log.e(TAG, "doInBackground: JSON Object error" );
                    e.printStackTrace();
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

    class getVirals extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            if (!YTutils.isInternetAvailable()) {
                task.cancel(true);
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (showTrend)
                trendingText.setText("TOP HIT ON SPOTIFY");
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            adapter = new SongAdapter(true,discoverModels,activity);
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            trendingText.setVisibility(View.VISIBLE);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String list = YTutils.readContent(activity,"history.csv");
            if (list!=null) {
                if (list.isEmpty()) {
                    MakeSpotifyList();
                    showTrend=true;
                    return null;
                }
                String[] songList = list.split("\n|\r");
                if (songList.length>5) {
                    MakeList(songList,5);
                }else if (songList.length<=0) {
                    MakeSpotifyList();
                }else MakeList(songList,songList.length);
                return null;
            }else showTrend=true;

            MakeSpotifyList();
            return null;
        }
    }

    void MakeSpotifyList() {
        if (SongList ==null) {
            String discoverViral = YTutils.readContent(activity,"discover_"+region+".csv");
            if (discoverViral!=null && !discoverViral.isEmpty()) {
                String[] csvlines = discoverViral.split("\r|\n");
                for(int i=1;i<csvlines.length;i++) {
                    String videoID = csvlines[i].split("/")[4];
                    YTMeta ytMeta = new YTMeta(videoID);
                    if (ytMeta.getVideMeta()!=null) {
                        discoverModels.add(new DiscoverModel(
                                ytMeta.getVideMeta().getTitle(),
                                ytMeta.getVideMeta().getAuthor(),
                                ytMeta.getVideMeta().getImgUrl(),
                                YTutils.getYtUrl(videoID)
                        ));
                    }
                    return;
                }
            }
            HttpHandler handler = new HttpHandler();
            SongList = handler.makeServiceCall("https://spotifycharts.com/viral/"+region+"/daily/latest/download");
        }

        String[] csvlines = SongList.split("\r|\n");
        for(int i=1;i<5;i++) {
            String line = csvlines[i];
            String title = line.split(",")[1].replace("\"","");
            String author = line.split(",")[2].replace("\"","");

            SpotifyTrack track = new SpotifyTrack(title,author);
            discoverModels.add(new DiscoverModel(
                    title,author,track.getImageUrl(),track.getYtUrl()
            ));
        }
    }

    void MakeList(String[] songList, int length) {
        for (int i=0;i<length;i++) {
            String[] childs = songList[i].split("\\|");
            discoverModels.add(new DiscoverModel(
                    childs[2],
                    childs[3],
                    childs[4],
                    YTutils.getYtUrl( childs[0])
            ));
        }
    }

    void showAlert(String title, String message, boolean isalert) {
        int icon = android.R.drawable.ic_dialog_info;
        if (isalert) icon = android.R.drawable.ic_dialog_alert;
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(icon)
                .show();
    }

}
