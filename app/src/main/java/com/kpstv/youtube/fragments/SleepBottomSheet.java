package com.kpstv.youtube.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kpstv.youtube.R;

import org.w3c.dom.Text;

public class SleepBottomSheet extends BottomSheetDialogFragment {

    ItemClickListener listener; Context context;
    TextView mins5,mins10,mins15,mins30,mins45,mins60,eot,snone;
    View v; LinearLayout linearLayout; Fragment parentFrag;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.sleep_bottom_sheet,container);
        getAllViews();

        context = getContext();

        mins5.setOnClickListener(view -> click(mins5));
        mins10.setOnClickListener(view -> click(mins10));
        mins15.setOnClickListener(view -> click(mins15));
        mins30.setOnClickListener(view -> click(mins30));
        mins45.setOnClickListener(view -> click(mins45));
        mins60.setOnClickListener(view -> click(mins60));
        eot.setOnClickListener(view -> click(eot));
        snone.setOnClickListener(view -> click(snone));

        if (getTag()!=null && !getTag().isEmpty()) {
            for (int i=0;i<linearLayout.getChildCount();i++) {
                View v = linearLayout.getChildAt(i);
                if (v instanceof TextView && ((TextView) v).getText().toString().equals(getTag())) {
                    ((TextView) v).setTextColor(context.getResources().getColor(R.color.colorAccent));
                    snone.setVisibility(View.VISIBLE);
                }
            }
        }

        return v;
    }

    void click(TextView v) {
        listener.onItemClick(v);
        dismiss();
    }

    void getAllViews() {
        linearLayout = v.findViewById(R.id.linearLayout);
        mins5 = v.findViewById(R.id.s5mins);
        snone = v.findViewById(R.id.snone);
        mins10 = v.findViewById(R.id.s10mins);
        mins15 = v.findViewById(R.id.s15mins);
        mins30 = v.findViewById(R.id.s30mins);
        mins45 = v.findViewById(R.id.s45mins);
        mins60 = v.findViewById(R.id.s60mins);
        eot = v.findViewById(R.id.eot);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

           try {
            listener = (ItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }

    public interface ItemClickListener {
        void onItemClick(TextView view);
    }
}
