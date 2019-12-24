package com.kpstv.youtube.fragments.basedOnApi;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.utils.AppBarStateChangeListener;
import com.kpstv.youtube.utils.YTutils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.COLLAPSED;
import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.EXPANDED;
import static com.kpstv.youtube.utils.AppBarStateChangeListener.State.IDLE;

public class YTFragment extends Fragment {
    private LinearLayout mLinearlayout;
    private RelativeLayout mRelativelayout;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mToolbarLayout;
    private AppBarLayout mAppBar;
    private Button mOplayfab;
    private ProgressBar mProgressbar;
    private RecyclerView mRecyclerview;
    String title;
    FragmentActivity activity;
    private ImageView mImageview;
    private TextView mTextview1;
    private TextView mYear;

    public YTFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_popular, container, false);
        initViews(v);

        activity = getActivity();

        mAppBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                appBarLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if (state == COLLAPSED) {
                            mToolbar.setTitle(title);
                            mToolbarLayout.setTitle(title);
                            mOplayfab.setVisibility(View.GONE);
                        } else if (state == EXPANDED || state == IDLE) {
                            mToolbarLayout.setTitle(" ");
                            mToolbar.setTitle(" ");
                            mOplayfab.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        mToolbar.setNavigationOnClickListener(view -> MainActivity.loadSearchFrag());


        String tag = getTag();
        if (tag != null) {
            View view = getLayoutInflater().inflate(R.layout.yt_item1, mLinearlayout);
            mImageview = view.findViewById(R.id.imageView);
            mTextview1 = view.findViewById(R.id.textView1);
            mYear = view.findViewById(R.id.year);
            Date c = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") String year =  new SimpleDateFormat("yyyy").format(c);
            mYear.setText(year);
            Bitmap bitmap=null;
            switch (tag) {
                case "most_viewed":
                    bitmap = YTutils.drawableToBitmap(getResources().getDrawable(R.drawable.material_back1));
                    mImageview.setImageDrawable(getResources().getDrawable(R.drawable.material_back1));
                    mTextview1.setText("Most Viewed");
                    break;
            }
            int yellow = activity.getResources().getColor(R.color.playback_indicator);
            int background = activity.getResources().getColor(R.color.background);
            new Palette.Builder(bitmap).generate(palette -> {
                mTextview1.setTextColor(palette.getMutedColor(background));
                mYear.setTextColor(palette.getDarkVibrantColor(yellow));
                GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{palette.getDarkVibrantColor(yellow),background});
                mRelativelayout.setBackground(drawable);
            });
        }

        return v;
    }

    private void initViews(View view) {
        mLinearlayout = view.findViewById(R.id.linearLayout);
        mRelativelayout = view.findViewById(R.id.relativeLayout);
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbarLayout = view.findViewById(R.id.toolbar_layout);
        mAppBar = view.findViewById(R.id.app_bar);
        mOplayfab = view.findViewById(R.id.oPlayFAB);
        mProgressbar = view.findViewById(R.id.progressBar);
        mRecyclerview = view.findViewById(R.id.recyclerView);
    }
}
