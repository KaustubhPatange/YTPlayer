package com.naveed.ytextractor;
/*
import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.naveed.ytextractor.model.YTMedia;
import com.naveed.ytextractor.model.YoutubeMeta;
import com.naveed.ytextractor.utils.ContextUtils;
import com.naveed.ytextractor.utils.LogUtils;
import com.universalvideoview.UniversalMediaController;
import com.universalvideoview.UniversalVideoView;
import java.util.List;
import android.widget.ListAdapter;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Adapter;
import com.naveed.ytextractor.utils.Utils;

public class MainActivity extends Activity {






	private EditText edit;

	private Button btn;

	private UniversalVideoView mVideoView;

	private UniversalMediaController mMediaController;

	private ListView list;

	private ArrayAdapter<String> adapter;

	private ArrayList<String> urls_li;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		ContextUtils.init(this);
		mVideoView = (UniversalVideoView) findViewById(R.id.videoView);
		TextView loading=(TextView)findViewById(R.id.loading_text);
		list = (ListView)findViewById(R.id.mainListView1);
		urls_li = new ArrayList<>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, urls_li);
		list.setAdapter(adapter);
		View bg=findViewById(R.id.loading_layout);
		bg.setBackgroundColor(Color.TRANSPARENT);
		loading.setText("LOADING");
		mMediaController = (UniversalMediaController) findViewById(R.id.media_controller);
		mVideoView.setMediaController(mMediaController);
		mVideoView.setAutoRotation(false);
		mVideoView.setVideoViewCallback(new UniversalVideoView.VideoViewCallback() {

				//private boolean isFullscreen;
                @Override
                public void onScaleChange(boolean isFullscreen) {
                    
				}

                @Override
                public void onPause(MediaPlayer mediaPlayer) { // Video pause

				}

                @Override
                public void onStart(MediaPlayer mediaPlayer) { // Video start/resume to play

				}

                @Override
                public void onBufferingStart(MediaPlayer mediaPlayer) {// steam start loading
                    Toast.makeText(getApplicationContext(), "Bufferinh", Toast.LENGTH_LONG).show();

				}

                @Override
                public void onBufferingEnd(MediaPlayer mediaPlayer) {// steam end loading

				}

            });
		edit = (EditText)findViewById(R.id.mainEditText1);
		btn = (Button)findViewById(R.id.mainButton1);
		edit.setText("https://youtu.be/4GuqB1BQVr4");
		edit.setHint("id or url");
		btn.setOnClickListener((new OnClickListener(){

								   @Override
								   public void onClick(View p1) {
									   Toast.makeText(getApplicationContext(), "Extracting", Toast.LENGTH_LONG).show();

									   new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner(){

											   @Override
											   public void onExtractionDone(List<YTMedia> adativeStream, final List<YTMedia> muxedStream, YoutubeMeta meta) {

												   urls_li.clear();
												   for (YTMedia c:muxedStream) {
													   urls_li.add(c.getUrl());
													   adapter.notifyDataSetChanged();
												   }
												   for (YTMedia c:adativeStream) {
													   urls_li.add(c.getUrl());
													   adapter.notifyDataSetChanged();
												   }
												   //Toast.makeText(getApplicationContext(), meta.getTitle(), Toast.LENGTH_LONG).show();
												  Toast.makeText(getApplicationContext(), meta.getAuthor(), Toast.LENGTH_LONG).show();
												   if (adativeStream.isEmpty()) {
													   LogUtils.log("null ha");
													   return;
												   }
												   if (muxedStream.isEmpty()) {
													   LogUtils.log("null ha");
													   return;
												   }
												   String url = muxedStream.get(0).getUrl();
												   PlayVideo(url);


											   }


											   @Override
											   public void onExtractionGoesWrong(final ExtractorException e) {

												   Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();


											   }
										   }).useDefaultLogin().Extract(edit.getText().toString());

								   }
							   }));


		list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
					Utils.copyToBoard(urls_li.get(p3));
					Toast.makeText(getApplicationContext(), "copied", Toast.LENGTH_LONG).show();

				}
			});

    }








	private void PlayVideo(String url) {
		mVideoView.setVideoPath(url);
		mVideoView.requestFocus();
		mVideoView.start();








	}




}
*/