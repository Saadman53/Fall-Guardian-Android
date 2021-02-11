package com.example.fallguardian;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiverYES extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager;
        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(1);

        String x = intent.getStringExtra("Selected");

        intent= new Intent(context, BackgroundService.class);
        intent.putExtra("Response","YES");
        context.startService(intent);
    }
}
