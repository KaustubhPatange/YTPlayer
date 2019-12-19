package com.kpstv.youtube.fragments;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kpstv.youtube.R;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;

public class DetailsBottomSheet extends BottomSheetDialogFragment {

    private TextView mTxtfilepath;
    private TextView mTxtfilename;
    private TextView mTxtsize;
    private TextView mTxtformat;
    private TextView mTxtlength;
    private TextView mTxtbitrate;
    private TextView mTxtsamplingrate;
    private Button mButton;
    private static final String TAG = "DetailsBottomSheet";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_details, container, false);
        initViews(v);

        Bundle args = getArguments();
        String file = args.getString("filepath");
        if (file!=null) {
            File f = new File(file);
            if (f.exists()) {
                mTxtfilepath.setText(file);
                mTxtfilename.setText(f.getName());
                mTxtsize.setText(YTutils.getSize(f.length()));
                MediaExtractor mex = new MediaExtractor();
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(getActivity(), Uri.fromFile(f));
                    mex.setDataSource(file);

                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    Long seconds = Long.parseLong(duration);

                    MediaFormat mf = mex.getTrackFormat(0);
                    int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE);
                    int sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    mTxtbitrate.setText(YTutils.getSizeNoDecimal(bitRate)+"/s");
                    mTxtsamplingrate.setText(sampleRate+" Hz");
                    mTxtlength.setText(YTutils.milliSecondsToTimer(
                            seconds
                    ));
                    String format = YTutils.getAudioFormat(f);
                    if (format!=null)
                        mTxtformat.setText(format);
                }catch (Exception e) {
                    Log.e(TAG, "onCreateView: "+e.getMessage());
                }
            }
        }
        mButton.setOnClickListener(view -> dismiss());
        return v;
    }

    private void initViews(View view) {
        mTxtfilepath = view.findViewById(R.id.txtFilePath);
        mTxtfilename = view.findViewById(R.id.txtFileName);
        mTxtsize = view.findViewById(R.id.txtSize);
        mTxtformat = view.findViewById(R.id.txtFormat);
        mTxtlength = view.findViewById(R.id.txtLength);
        mTxtbitrate = view.findViewById(R.id.txtBitrate);
        mTxtsamplingrate = view.findViewById(R.id.txtSamplingRate);
        mButton = view.findViewById(R.id.button);
    }

}
