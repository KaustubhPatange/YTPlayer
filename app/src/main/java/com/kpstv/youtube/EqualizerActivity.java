package com.kpstv.youtube;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.kpstv.youtube.utils.YTutils;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import static com.kpstv.youtube.MainActivity.bassBoost;
import static com.kpstv.youtube.MainActivity.loudnessEnhancer;
import static com.kpstv.youtube.MainActivity.mEqualizer;
import static com.kpstv.youtube.MainActivity.presetReverb;
import static com.kpstv.youtube.MainActivity.settingPref;
import static com.kpstv.youtube.MainActivity.virtualizer;

public class EqualizerActivity extends AppCompatActivity {

    LinearLayout mLinearLayout;
    Spinner equalizerSpinner, reverbSpinner;
    SharedPreferences preferences;
    private static final String TAG = "EqualizerActivity";
    private IndicatorSeekBar bassBoastSeekbar;
    private IndicatorSeekBar virtualizerSeekbar;
    private IndicatorSeekBar loudnessSeekbar;
    private Button mPurchasebutton;
    private RelativeLayout mPurchaselayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
        initViews();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferences = getSharedPreferences("settings", MODE_PRIVATE);

        setTitle("Equalizer");

        equalizerSpinner = findViewById(R.id.preset_spinner);
        reverbSpinner = findViewById(R.id.reverb_spinner);

        short m = mEqualizer.getNumberOfPresets();
        String[] styles = new String[m];
        for (int i = 0; i < m; i++) {
            styles[i] = mEqualizer.getPresetName((short) i);
        }

        ArrayAdapter<String> presetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, styles);
        presetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equalizerSpinner.setAdapter(presetAdapter);
        int default_preset = preferences.getInt("selected_preset", 0);
        if (default_preset != 0)
            equalizerSpinner.setSelection(default_preset);
        equalizerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mEqualizer.usePreset((short) i);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("selected_preset", i);
                editor.apply();
                setLayout(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (AppSettings.enableEqualizer) {
            ArrayAdapter<CharSequence> reverbAdapter = ArrayAdapter.createFromResource(this
                    , R.array.reverb_presets, android.R.layout.simple_spinner_item);
            reverbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            reverbSpinner.setAdapter(reverbAdapter);
            int default_reverb = preferences.getInt("selected_reverb", 0);
            if (default_reverb != 0) {
                reverbSpinner.setSelection(default_reverb);
                presetReverb.setPreset((short) default_reverb);
            }
            reverbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    presetReverb.setPreset((short) i);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("selected_reverb", i);
                    editor.apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            bassBoastSeekbar.setMax(1000);
            bassBoastSeekbar.setProgress(bassBoost.getRoundedStrength());
            bassBoastSeekbar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {
                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                    bassBoost.setStrength((short) seekBar.getProgress());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("selected_bass", seekBar.getProgress());
                    editor.apply();
                }
            });

            virtualizerSeekbar.setMax(1000);
            virtualizerSeekbar.setProgress(virtualizer.getRoundedStrength());
            virtualizerSeekbar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {
                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                    virtualizer.setStrength((short) seekBar.getProgress());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("selected_virtualizer", seekBar.getProgress());
                    editor.apply();
                }
            });

            loudnessSeekbar.setMax(100);
            loudnessSeekbar.setProgress(loudnessEnhancer.getTargetGain());
            loudnessSeekbar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {

                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                    loudnessEnhancer.setTargetGain(seekBar.getProgress());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("selected_loudness", seekBar.getProgress());
                    editor.apply();
                }
            });
        }

        mLinearLayout = findViewById(R.id.linearLayout);

        if (mEqualizer == null)
            return;

        setLayout(true);

        if (AppSettings.enableEqualizer) {
            mPurchaselayout.setVisibility(View.GONE);
        }else {
            mPurchasebutton.setOnClickListener(view -> {
                Intent intent = new Intent(this,PurchaseActivity.class);
                startActivity(intent);
            });
        }
    }

    void setLayout(boolean loadOrSave) {
        mLinearLayout.removeAllViews();
        short numberFrequencyBands = mEqualizer.getNumberOfBands();

        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];

        final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];
        for (short i = 0; i < numberFrequencyBands; i++) {
            final short equalizerBandIndex = i;

            Log.e(TAG, "onCreate: Adding: " + i);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View equalizerLayout = inflater.inflate(R.layout.equilizer_item, null);

            TextView freqencyBand = equalizerLayout.findViewById(R.id.txt_frequencyBand);
            int bandFreq = (mEqualizer.getCenterFreq(equalizerBandIndex) / 1000);
            String texttoPut = bandFreq + " Hz";
            if (bandFreq > 1000) {
                texttoPut = (YTutils.dividePattern(bandFreq, 1000, "0.0"));
                if (!texttoPut.contains(".")) {
                    texttoPut += " kHz";
                } else if (texttoPut.split("\\.")[1].equals("0")) {
                    texttoPut = texttoPut.split("\\.")[0] + " kHz";
                } else texttoPut += " kHz";
            } else if (bandFreq > 10 && bandFreq < 100) {
                texttoPut = " " + bandFreq + " Hz";
            }

            freqencyBand.setText(texttoPut);

            TextView lowerBandLevel = equalizerLayout.findViewById(R.id.txt_lowerBandLevel);
            lowerBandLevel.setText((lowerEqualizerBandLevel / 100) + " dB");

            TextView upperBandLevel = equalizerLayout.findViewById(R.id.txt_upperBandLevel);
            upperBandLevel.setText((upperEqualizerBandLevel / 100) + " dB");

            IndicatorSeekBar seekBar = equalizerLayout.findViewById(R.id.seekBar);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

            final int seek_id = i;
            int progressBar = 1500;
            if (!loadOrSave) {
                //int centerFrequency = mEqualizer.getCenterFreq(equalizerBandIndex)/1000;
                Log.e(TAG, "setLayout: Band: " + equalizerBandIndex + ", BandLevel: "
                        + (mEqualizer.getBandLevel(equalizerBandIndex) + upperEqualizerBandLevel));

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("seek_" + seek_id, (mEqualizer.getBandLevel(equalizerBandIndex) + upperEqualizerBandLevel));
                editor.apply();
                progressBar = preferences.getInt("seek_" + seek_id, 1500);
                seekBar.setProgress(progressBar);
            } else {
                progressBar = preferences.getInt("seek_" + seek_id, 1500);
                if (progressBar != 1500) {
                    seekBar.setProgress(progressBar);
                    mEqualizer.setBandLevel(equalizerBandIndex,
                            (short) (progressBar + lowerEqualizerBandLevel));
                } else {
                    seekBar.setProgress(mEqualizer.getBandLevel(equalizerBandIndex));
                    mEqualizer.setBandLevel(equalizerBandIndex,
                            (short) (progressBar + lowerEqualizerBandLevel));
                }
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
                    // editor.putInt("position", 0);
                    editor.apply();
                }
            });

            mLinearLayout.addView(equalizerLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        setEqualizerLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_volume:
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
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
        Log.e(TAG, "setEqualizerLayout: Enabled" + MainActivity.isEqualizerEnabled);
        RelativeLayout layout = findViewById(R.id.mainlayout);
        if (MainActivity.isEqualizerEnabled) {
            Log.e(TAG, "setEqualizerLayout: And I got here too");
            layout.setClickable(false);
            equalizerSpinner.setEnabled(true);
            for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
                View view = mLinearLayout.getChildAt(i);
                IndicatorSeekBar seekBar = view.findViewById(R.id.seekBar);
                seekBar.setEnabled(true);
            }
            if (AppSettings.enableEqualizer) {
                reverbSpinner.setEnabled(true);
                bassBoastSeekbar.setEnabled(true);
                virtualizerSeekbar.setEnabled(true);
                loudnessSeekbar.setEnabled(true);
                bassBoost.setStrength((short) bassBoastSeekbar.getProgress());
                virtualizer.setStrength((short) virtualizerSeekbar.getProgress());
                loudnessEnhancer.setTargetGain(loudnessSeekbar.getProgress());
            }
        } else {
            Log.e(TAG, "setEqualizerLayout: Got here...");
            layout.setClickable(true);
            equalizerSpinner.setEnabled(false);
            reverbSpinner.setEnabled(false);
            bassBoastSeekbar.setEnabled(false);
            virtualizerSeekbar.setEnabled(false);
            loudnessSeekbar.setEnabled(false);
            bassBoost.setStrength((short) 0);
            virtualizer.setStrength((short) 0);
            loudnessEnhancer.setTargetGain(0);
            for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
                View view = mLinearLayout.getChildAt(i);
                IndicatorSeekBar seekBar = view.findViewById(R.id.seekBar);
                seekBar.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.equalizer_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.equalizer_enabled);
        menuItem.setActionView(R.layout.switch_item);
        final Switch sw = menuItem.getActionView().findViewById(R.id.action_switch);
        sw.setChecked(settingPref.getBoolean("equalizer_enabled", false));
        sw.setOnCheckedChangeListener((compoundButton, b) -> {
            MainActivity.isEqualizerEnabled = b;
            setEqualizerLayout();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("equalizer_enabled", MainActivity.isEqualizerEnabled);
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

    private void initViews() {
        bassBoastSeekbar = findViewById(R.id.bass_boast_seekBar);
        virtualizerSeekbar = findViewById(R.id.virtualizer_seekBar);
        loudnessSeekbar = findViewById(R.id.loudness_seekBar);
        mPurchasebutton = findViewById(R.id.purchaseButton);
        mPurchaselayout = findViewById(R.id.purchaseLayout);
    }
}
