package com.kpstv.youtube;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kpstv.youtube.utils.YTutils;

public class HiddenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden);

        CheckIntent(true);

    }

    void CheckIntent(boolean callFinish) {
        if (Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (YTutils.isValidID(ytLink)){
                Intent intent = new Intent(HiddenActivity.this,PlayerActivity.class);
                intent.putExtra("youtubelink",new String[] {ytLink});
                startActivityForResult(intent,200);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                finish();
            }else {
                showAlert(callFinish);
            }
        }
    }

    void showAlert(final boolean callFinish) {
        int icon =android.R.drawable.ic_dialog_alert;
        new AlertDialog.Builder(HiddenActivity.this)
                .setTitle("Callback Error")
                .setMessage("The requested url is not a valid YouTube url")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callFinish)
                            finish();
                    }
                })
                .setIcon(icon)
                .show();
    }
}
