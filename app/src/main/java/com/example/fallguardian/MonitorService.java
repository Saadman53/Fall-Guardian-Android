package com.example.fallguardian;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static com.example.fallguardian.NotifApp.CHANNEL_1_ID;
import static com.example.fallguardian.NotifApp.CHANNEL_2_ID;

public class MonitorService extends Service {

    public NotificationManagerCompat notificationManager;

    DatabaseReference monitorDatabaseReference;
    FirebaseUser user;
    private Timer myTimer;
    
    String phone;

    String text;

    boolean stopRunning;
    ArrayList<String> list;


    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        phone = user.getPhoneNumber();
        monitorDatabaseReference = FirebaseDatabase.getInstance().getReference("monitor");
        stopRunning = false;



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(stopRunning) return;
                //Log.d("TIMERRRRRRRR","----------------TIMER IS RUNNING--------------");
                displayFromDatabase();
            }

        }, 0, 5000);
        Intent activityIntent = new Intent(MonitorService.this,MonitorActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_service)
                .setContentTitle("Fall Gudarian")
                .setColor(Color.BLUE)
                .setContentText("Running on Background.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setContentIntent(contentIntent)
                .build();
        notificationManager.notify(2,notification);
        startForeground(2,notification);

        return START_NOT_STICKY;
    }

    private void displayFromDatabase() { monitorDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            if(snapshot.exists()){

                try{
                    Map<String, String> map = (Map<String, String>) snapshot.child(phone).getValue();
                    TreeMap<String, String> treemap = new TreeMap<String, String>(map);
                    Map<String, String> newdata = new HashMap<>();
                    int iter = 0;
                    for (String key : treemap.descendingKeySet()){
                        if(iter==10) break;
                        newdata.put(key,treemap.get(key));
                        iter++;
                    }

                    if(map.size()>10){
                        monitorDatabaseReference.child(phone).removeValue();
                        Map<String,Object> updatedata = new HashMap<>(newdata);
                        monitorDatabaseReference.child(phone).updateChildren(updatedata);

                        Log.i("UPDATE DATABASE", "onDataChange: -----------------------------> Database Updated");

                        sendDataToActivity(newdata);

                    }
                }
                catch (Exception e){
                    Log.d("ERROR", "ERRRRRRRRRRRRRRRRRRRRRROR "+e.toString());
                }

            }
            else{

            }


        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    }

    private void sendDataToActivity(Map<String, String> newdata) {
        sendNotification();


        Intent intent = new Intent("newDataReceived");
        intent.putExtra("data", "yes"); // You can add additional data to the intent...
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendNotification() {

        Intent activityIntent = new Intent(this,MonitorActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);

        notificationManager.cancel(1);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_fall)
                .setContentTitle("Fall Detected!")
                .setContentText("User fell down! Click to view.")
                .setColor(Color.RED)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();
        notificationManager.notify(1,notification);
    }

    @Override
    public void onDestroy() {
        stopRunning = true;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}