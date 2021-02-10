package com.example.fallguardian;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.example.fallguardian.NotifApp.CHANNEL_2_ID;

public class BackgroundService extends Service {


    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent activityIntent = new Intent(this,SensorActivity.class);
        activityIntent.putExtra("flag","not_fall");
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);

        notification = new NotificationCompat.Builder(this,CHANNEL_2_ID)
                .setContentTitle("Fall Gudarian")
                .setContentText("Running on Background")
                .setSmallIcon(R.drawable.ic_service)
                .setContentIntent(contentIntent)
                .build();
        startForeground(2,notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
