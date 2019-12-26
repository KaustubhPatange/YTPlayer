package com.kpstv.youtube.fragments;


import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kpstv.youtube.R;
import com.kpstv.youtube.models.LyricModel;

public class LyricBottomSheet extends BottomSheetDialogFragment {
    private TextView mTitletextview;
    private TextView mTexttextview;

    public LyricBottomSheet() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_lyric, container, false);
        initViews(v);

        Bundle args = getArguments();
        if (args!=null) {
            LyricModel model = (LyricModel) args.getSerializable("model");
            mTitletextview.setText(model.getTitle());
            mTexttextview.setText(model.getText());
        }
        return v;
    }

    private void initViews(View view) {
        mTitletextview = view.findViewById(R.id.titleTextView);
        mTexttextview = view.findViewById(R.id.textTextView);
    }
}
