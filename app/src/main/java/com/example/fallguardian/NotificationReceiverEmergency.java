package com.example.fallguardian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiverEmergency extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager;
        notificationManager = NotificationManagerCompat.from(context);

        String x = intent.getStringExtra("Selected");

        Log.d("EMERGENCY","AN EMERGENCY HAS OCCURED!!!!!!!!!!!!!!");
        intent= new Intent(context, BackgroundService.class);
        intent.putExtra("Response","EMERGENCY");
        context.startService(intent);
    }
}
