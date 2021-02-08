package com.example.fallguardian;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.fallguardian.NotifApp.CHANNEL_1_ID;


class FallDetector {
    private Context context;
    private Communicator communicator;
    private Elderly elderly;
    int prev_response;
    boolean fall_detected;
    double fall_detection_time;
    private Vibrator v;

    public LocationAndSMS locationAndSMS;

    ///dialogue box
    private FallDialogue fallDialogue;
    ///Notificaiton
    public NotificationManagerCompat notificationManager;
    boolean isNotificationEnabled;

    public FallDetector(Context c) {
        context = c;
        prev_response = 0;
        fall_detected = false;
        fall_detection_time = 100000000000000.0;
        ///initialize communicator instance
        communicator =  new Communicator();
        v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        isNotificationEnabled = false;
        notificationManager = NotificationManagerCompat.from(context);

        ///Initialize locationAndSMS
        locationAndSMS = new LocationAndSMS(c);
        ///Ask permissions for location and SMS
        locationAndSMS.askPermissions();
    }

    public void setElderly(Elderly elderly) {
        this.elderly = elderly;
        locationAndSMS.setElderly(elderly);
    }

    public void enableFallDetection(){
        fall_detected = true;
        fall_detection_time = System.currentTimeMillis() / 1000.0;
    }

    public void disableFallDetection(){
        fall_detected = false;
        fall_detection_time = 100000000000000.0;
    }

    public boolean isFall_detected() {
        return fall_detected;
    }

    public boolean getNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
    }



    public void sendNotification(){

        Intent activityIntent = new Intent(context,SensorActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context,0,activityIntent,0);



        Notification notification = new NotificationCompat.Builder(context,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_fall)
                .setContentTitle("Fall Detected!")
                .setContentText("You seem to have fallen. Click to open app.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(1,notification);
        isNotificationEnabled = true;

    }

    public void cancelNotification(int id){
        notificationManager.cancel(id);
    }

    public void createPost_ACC(List<Data_ACC> list, boolean isOnPause) {

        Call<Fall> call = communicator.getClient_acc().GetPostValue_ACC(list);
        call.enqueue(new Callback<Fall>() {
            @Override
            public void onResponse(Call<Fall> call, Response<Fall> response) {
                Fall hasFall = response.body();
                try {
                    if (hasFall.getFall() == 1) {

                        if (prev_response == 0) {
                            Toast.makeText(context, "Fall Detected!", Toast.LENGTH_LONG).show();

                            enableFallDetection();

                            if (!isOnPause) {
                                initiateFallDialogue();
                            } else {
                                ///show notification
                                sendNotification();
                            }

                            Log.d("SensorActivity", "User has________________________________________________________________fallen_________" + elderly.getMonitor_phone_number());

                            ///initiate timer

                            v.vibrate(1000);

                        }

                    }
                    prev_response = hasFall.getFall();
                    //Log.d(TAG, "Response__________________________________________________SUCCESS______" + hasFall.getFall());
                } catch (NullPointerException e) {
                    Log.d("SensorActivity", "CAUGHT++++++++++++++++++++EXCEPTION++++++++++============" + e);

                }

            }

            @Override
            public void onFailure(Call<Fall> call, Throwable t) {
                Log.d("SensorActivity", "_____________________________________________FAILURE_____________" + String.valueOf(t));
            }
        });
    }

    public void createPost_both(List<Data> list,boolean isOnPause) {

        Call<Fall> call = communicator.getClient_both().GetPostValue_both(list);
        call.enqueue(new Callback<Fall>() {
            @Override
            public void onResponse(Call<Fall> call, Response<Fall> response) {
                Fall hasFall = response.body();
                if (hasFall.getFall() == 1) {
                    if (prev_response == 0) {
                        Toast.makeText(context, "Fall Detected!", Toast.LENGTH_LONG).show();

                        enableFallDetection();

                        if(!isOnPause){
                            initiateFallDialogue();
                        }
                        else{
                            ///show notification
                            sendNotification();
                        }
                        Log.d("SensorActivity", "User has________________________________________________________________fallen_________" + elderly.getMonitor_phone_number());


                        ///initiate timer


                        v.vibrate(1000);

                    }
                }
                prev_response = hasFall.getFall();
                // Log.d(TAG, "Response__________________________________________________SUCCESS______" + hasFall.getFall());
            }

            @Override
            public void onFailure(Call<Fall> call, Throwable t) {
                Log.d("SensorActivity", "___________________________________ERROR FOR GYRO+_________________" + String.valueOf(t));
            }
        });
    }

    public void initiateFallDialogue(){
        try{
            fallDialogue = new FallDialogue("Fall Detected!","Oh no! Are you injured?!");
            fallDialogue.show(((FragmentActivity)context).getSupportFragmentManager(), "fall dialogue");
            Log.d("TAG","WORKING________________________________________________________");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void dismissFallDialogue(){
        fallDialogue.dismiss();
    }

    public void countFallTimer(boolean isOnPause){
        if (fall_detected) {
            Log.d("Sensor Activity","User fell down and time of falling is _________________________________________________________:           "+fall_detection_time);
            ///check if 20 seconds have passed since user hasn't responded to the fall dialogue
            //user might be injured
            if (( (System.currentTimeMillis() / 1000.0) - fall_detection_time) >= 20.0) {
                locationAndSMS.getLocationAndSendSMS();
                disableFallDetection();
                //Toast.makeText(this,"SMS SENT",Toast.LENGTH_SHORT).show();

                ///app screen is showing
                if(!isOnPause){
                    if (fallDialogue != null) {
                        try{
                            fallDialogue.dismiss();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    if(isNotificationEnabled){
                        ///dismiss the notification
                        notificationManager.cancel(1);
                        isNotificationEnabled = false;
                    }
                }
            }
        }
        else{
           // Log.d("SensorActivity","User has not fallen down _________________________________________________________:           "+fall_detection_time);
        }
    }




}
