package com.example.fallguardian;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NotifApp extends Application {

    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    public static final String CHANNEL_3_ID = "channel3";

    FirebaseUser user;



    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d("TOAST","notification________________________________channel___________________________created");

        user = FirebaseAuth.getInstance().getCurrentUser();


        jumpToNewActivity();

    }

    private void jumpToNewActivity() {
        if(user!=null){
            //user with email exists
            if(!user.getPhoneNumber().isEmpty()){
                Log.i("NOTIF","USER SEEMS TO EXIST IN THIS SYSTEM USING PHONE*******************");
                ///Monitor Activity
                Intent intent =  new Intent(NotifApp.this, MonitorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else if(!user.getEmail().isEmpty()){
                Log.i("NOTIF","USER SEEMS TO EXIST IN THIS SYSTEM ****USING EMAIL********************");
                Intent intent =  new Intent(NotifApp.this, LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }


        }
        else{
            Log.d("NOTIF","USER SEEMS TO BE NULL IN THIS SYSTEM WHICH IS NOT WEIRD XD*********************");
            Intent intent =  new Intent(NotifApp.this, FirstActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }


    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Chanel 1");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel2.setDescription("This is Chanel 2");


            NotificationChannel channel3 = new NotificationChannel(
                    CHANNEL_3_ID,
                    "Channel 3",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel3.setDescription("This is Chanel 3");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            manager.createNotificationChannel(channel3);
        }
    }
}
