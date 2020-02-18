package com.kpstv.youtube.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kpstv.youtube.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PSBottomSheet extends BottomSheetDialogFragment {

    View view;
    PSListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.bottom_share_playlist,container);
        ButterKnife.bind(this,view);
        return view;
    }


    @OnClick(R.id.txt_saveToLocal) void saveToLocal(View v) {
        listener.onClick(0);
    }

    @OnClick(R.id.txt_shareTo) void shareTo(View v) {
        listener.onClick(1);
    }

    @OnClick(R.id.button) void dismissClick(View v) {
        this.dismiss();
    }

    public interface PSListener {
        void onClick(int id);
    }

    public void setListener(PSListener listener) {
        this.listener = listener;
    }

}
