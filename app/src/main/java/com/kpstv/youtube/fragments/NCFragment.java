package com.kpstv.youtube.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kpstv.youtube.HistoryBottomSheet;
import com.kpstv.youtube.R;

public class NCFragment extends Fragment {

    NoConnectionListener listener;

    public NCFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nc, container, false);
        Button button = v.findViewById(R.id.button);
        button.setOnClickListener(v1 -> listener.ReplaceFragment());
        return v;
    }

    public interface NoConnectionListener {
        void ReplaceFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (NoConnectionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoConnection Listener");
        }
    }

}
