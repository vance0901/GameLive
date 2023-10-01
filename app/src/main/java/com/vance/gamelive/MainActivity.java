package com.vance.gamelive;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity {

    private ScreenLive mScreenLive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, 10);

        Handler handler = new Handler();
//        Message message1 = new Message();
        Message message = handler.obtainMessage(); // what = 1, what =2 ,what = 2
//        message.what = 2;
        handler.sendMessage(message);


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            mScreenLive.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startLive(View view) {
        mScreenLive = new ScreenLive();
        mScreenLive.startLive(this,
                "rtmp://sendtc3a.douyu.com/live/6918788rd3WwQNSV?wsSecret=e4661e772c05bc58c8f08b13eb6df517&wsTime=6319e048&wsSeek=off&wm=0&tw=0&roirecognition=0&record=flv&origin=tct");
    }

    public void stopLive(View view) {
        mScreenLive.stoptLive();
    }

}
