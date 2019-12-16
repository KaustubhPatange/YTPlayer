package com.kpstv.youtube.models;

import android.graphics.Bitmap;

import java.io.Serializable;

public class LocalSearchModel implements Serializable {
    Bitmap bitmap;

    public LocalSearchModel(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
