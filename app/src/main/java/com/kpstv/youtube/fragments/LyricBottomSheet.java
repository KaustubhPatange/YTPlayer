package com.kpstv.youtube.fragments;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.LyricModel;
import com.kpstv.youtube.services.MusicService;

import top.defaults.drawabletoolbox.DrawableBuilder;

public class LyricBottomSheet extends BottomSheetDialogFragment {
    private TextView mTitletextview;
    private TextView mTexttextview;
    private LinearLayout mColorview;
    private static final String TAG = "LyricBottomSheet";

    public LyricBottomSheet() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_lyric, container, false);
        initViews(v);

        Bundle args = getArguments();
        if (args != null) {

            Palette.from(MusicService.bitmapIcon).generate(palette -> {
                int black_color = ContextCompat.getColor(getActivity(), R.color.black);
                int vibrant_color = palette.getVibrantColor(black_color);

                GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM
                ,new int[]{manipulateColor(vibrant_color,0.8f),black_color,black_color});


              /*  Drawable drawable = new DrawableBuilder()
                        .angle(90)
                        .startColor(vibrant_color)
                        .endColor(black_color)
                        .build();*/
                mColorview.setBackground(drawable);
            });

            LyricModel model = (LyricModel) args.getSerializable("model");
            mTitletextview.setText(model.getTitle());
            mTexttextview.setText(model.getText());
        }
        return v;
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    private void initViews(View view) {
        mTitletextview = view.findViewById(R.id.titleTextView);
        mTexttextview = view.findViewById(R.id.textTextView);
        mColorview = view.findViewById(R.id.colorView);
    }
}
