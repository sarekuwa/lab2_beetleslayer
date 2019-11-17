package com.example.lab2_beetlegame;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class SoundServiceStartMenu extends Service {
    MediaPlayer mp;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        mp = MediaPlayer.create(this, R.raw.menu); //select music file
        mp.setLooping(true); //set looping
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        mp.start();
        return Service.START_NOT_STICKY;
    }

    public void onDestroy() {
        mp.stop();
        mp.release();
        stopSelf();
        super.onDestroy();
    }

}