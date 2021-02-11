package com.example.fallguardian;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiverNO extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager;
        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(1);

        String x = intent.getStringExtra("Selected");

        intent= new Intent(context, BackgroundService.class);
        intent.putExtra("Response","NO");
        context.startService(intent);
    }
}
