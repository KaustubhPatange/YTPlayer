package com.kpstv.youtube;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class PurchaseActivity extends AppCompatActivity {

    private Button mBuybutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        initViews();

        mBuybutton.setOnClickListener(view -> {

        });
    }

    private void initViews() {
        mBuybutton = findViewById(R.id.buyButton);
    }
}
