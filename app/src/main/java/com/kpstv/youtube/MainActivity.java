package com.kpstv.youtube;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // https://www.googleapis.com/youtube/v3/videos?id=BDocp-VpCwY&key=AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw&part=snippet,contentDetails,statistics,status

    // API KEY= AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Disabling URI exposure for lame android 8.0+ who can't do by themselves.
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        Button button = findViewById(R.id.newbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.edit_text);
                ArrayList<String> list = new ArrayList<>();
                if (editText.getText().toString().contains(",")) {
                    list = new ArrayList<>(Arrays.asList(editText.getText().toString().split(",")));
                }else
                list.add(editText.getText().toString());
                Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                String[] arr = new String[list.size()];
                arr = list.toArray(arr);
                intent.putExtra("youtubelink",arr);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            }
        });
    }

    @Override
    protected void onDestroy() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1);
        super.onDestroy();
    }
}
