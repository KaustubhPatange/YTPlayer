package com.kpstv.youtube;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DiscoverTrend extends AppCompatActivity {

    private String[] apikeys = new String[] {"AIzaSyCA2Py9snHNdp4Y4Dkyq-z7gUfxLqdPhtQ","AIzaSyBH8szUCt1ctKQabVeQuvWgowaKxHVjn8E"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_trend);
    }
}
