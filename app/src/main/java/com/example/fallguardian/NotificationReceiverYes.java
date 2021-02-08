package com.example.fallguardian;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiverYes extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String Message = intent.getStringExtra("reply");
        Toast.makeText(context,Message,Toast.LENGTH_SHORT).show();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }
}
