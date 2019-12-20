package com.kpstv.youtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;
import com.kpstv.youtube.utils.YTutils;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.w3c.dom.Text;

import static com.kpstv.youtube.MainActivity.mEqualizer;
import static com.kpstv.youtube.MainActivity.settingPref;

public class EqualizerActivity extends AppCompatActivity {

    LinearLayout mLinearLayout;
    Spinner spinner;
    SharedPreferences preferences;
    private static final String TAG = "EqualizerActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Equalizer");

        spinner = findViewById(R.id.preset_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this
                , R.array.presets,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        preferences = getSharedPreferences("settings",MODE_PRIVATE);

        mLinearLayout = findViewById(R.id.linearLayout);

        setEqualizerLayout();

        if (mEqualizer==null)
            return;

        setLayout();

    }

    void setLayout() {
        short numberFrequencyBands = mEqualizer.getNumberOfBands();

        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];

        final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];
        for (short i = 0; i < numberFrequencyBands; i++) {
            final short equalizerBandIndex = i;

            Log.e(TAG, "onCreate: Adding: "+i );

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View equalizerLayout = inflater.inflate(R.layout.equilizer_item,null);

            TextView freqencyBand = equalizerLayout.findViewById(R.id.txt_frequencyBand);
            int bandFreq = (mEqualizer.getCenterFreq(equalizerBandIndex) / 1000);
            String texttoPut = bandFreq+" Hz";
            if (bandFreq>1000)
            {
                texttoPut = (YTutils.dividePattern(bandFreq,1000,"0.0"));
                if (texttoPut.split(".")[1].equals("0")) {
                   texttoPut = texttoPut.split(".")[0]+" kHz";
                }
            }else if (bandFreq>10 && bandFreq<100) {
                texttoPut = " "+bandFreq+" Hz";
            }

            freqencyBand.setText(texttoPut);

            TextView lowerBandLevel = equalizerLayout.findViewById(R.id.txt_lowerBandLevel);
            lowerBandLevel.setText((lowerEqualizerBandLevel / 100) + " dB");

            TextView upperBandLevel = equalizerLayout.findViewById(R.id.txt_upperBandLevel);
            upperBandLevel.setText((upperEqualizerBandLevel / 100) + " dB");

            IndicatorSeekBar seekBar = equalizerLayout.findViewById(R.id.seekBar);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

            final int seek_id = i;
            int progressBar = preferences.getInt("seek_" + seek_id, 1500);
            if (progressBar != 1500) {
                seekBar.setProgress(progressBar);
                mEqualizer.setBandLevel(equalizerBandIndex,
                        (short) (progressBar + lowerEqualizerBandLevel));
            } else {
                seekBar.setProgress(mEqualizer.getBandLevel(equalizerBandIndex));
                mEqualizer.setBandLevel(equalizerBandIndex,
                        (short) (progressBar + lowerEqualizerBandLevel));
            }

            seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {
                    mEqualizer.setBandLevel(equalizerBandIndex,
                            (short) (seekParams.progress + lowerEqualizerBandLevel));
                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("seek_" + seek_id, seekBar.getProgress());
                    editor.putInt("position", 0);
                    editor.apply();
                }
            });

            mLinearLayout.addView(equalizerLayout,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_volume:
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_SAME,AudioManager.FLAG_SHOW_UI);
                break;

            /*case R.id.equalizer_enabled:
                Switch switch_item = item.getActionView().findViewById(R.id.switch_item_view);
                MainActivity.isEqualizerEnabled = switch_item.isChecked();
                setEqualizerLayout();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("equalizer_enabled",MainActivity.isEqualizerEnabled);
                editor.apply();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    void setEqualizerLayout() {
        LinearLayout layout = findViewById(R.id.mainlayout);
        if (MainActivity.isEqualizerEnabled) {
            layout.setEnabled(true);
        }else layout.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.equalizer_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.equalizer_enabled);
        menuItem.setActionView(R.layout.switch_item);
        final Switch sw = menuItem.getActionView().findViewById(R.id.action_switch);
        sw.setOnCheckedChangeListener((compoundButton, b) -> {
            MainActivity.isEqualizerEnabled = b;
            setEqualizerLayout();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("equalizer_enabled",MainActivity.isEqualizerEnabled);
            mEqualizer.setEnabled(MainActivity.isEqualizerEnabled);
            editor.apply();
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
