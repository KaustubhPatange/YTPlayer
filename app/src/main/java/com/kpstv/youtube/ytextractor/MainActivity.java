
package com.kpstv.youtube.ytextractor;
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
import com.kpstv.youtube.ytextractor.model.YoutubeMedia;
import com.kpstv.youtube.ytextractor.model.YoutubeMeta;
import com.kpstv.youtube.ytextractor.utils.ContextUtils;
import com.kpstv.youtube.ytextractor.utils.LogUtils;
import com.universalvideoview.UniversalMediaController;
import com.universalvideoview.UniversalVideoView;
import java.util.List;

public class MainActivity extends Activity {

	private EditText edit;

	private Button btn;

	private UniversalVideoView mVideoView;

	private UniversalMediaController mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		ContextUtils.init(this);
		mVideoView = (UniversalVideoView) findViewById(R.id.videoView);
		TextView loading=(TextView)findViewById(R.id.loading_text);
		View bg=findViewById(R.id.loading_layout);
		bg.setBackgroundColor(Color.TRANSPARENT);
		loading.setText("LOADING");
		mMediaController = (UniversalMediaController) findViewById(R.id.media_controller);
		mVideoView.setMediaController(mMediaController);
		mVideoView.setVideoViewCallback(new UniversalVideoView.VideoViewCallback() {

				private boolean isFullscreen;
                @Override
                public void onScaleChange(boolean isFullscreen) {
                    this.isFullscreen = isFullscreen;

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
		edit.setText("4H4Oizo7oOE");
		edit.setHint("id or url");
		btn.setOnClickListener((new OnClickListener(){

								   @Override
								   public void onClick(View p1) {
									   Toast.makeText(getApplicationContext(), "Extracting", Toast.LENGTH_LONG).show();
									  
									   new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner(){

											   @Override
											   public void onExtractionDone(List<YoutubeMedia> adativeStream, final List<YoutubeMedia> muxedStream, YoutubeMeta meta) {

												   Toast.makeText(getApplicationContext(), meta.getTitle(), Toast.LENGTH_LONG).show();
												   Toast.makeText(getApplicationContext(), meta.getAuthor(), Toast.LENGTH_LONG).show();
												   

												   if (muxedStream.isEmpty()) {LogUtils.log("null ha");
													   return;}
												   String url = muxedStream.get(0).getUrl();
												   LogUtils.log(url);
												   PlayVideo(url);


											   }


											   @Override
											   public void onExtractionGoesWrong(final ExtractorException e) {

												   Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();


											   }
										   }).Extract(edit.getText().toString());

								   }
							   }));

    }








	private void PlayVideo(String url) {
		mVideoView.setVideoPath(url);
		mVideoView.requestFocus();
		mVideoView.start();


		
	}




}
*/
