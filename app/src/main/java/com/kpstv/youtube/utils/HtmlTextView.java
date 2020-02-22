package com.kpstv.youtube.utils;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

public class HtmlTextView extends android.support.v7.widget.AppCompatTextView {
    public HtmlTextView(Context context) {
        super(context);
        this.setText(Html.fromHtml(getText().toString()));
    }
}
