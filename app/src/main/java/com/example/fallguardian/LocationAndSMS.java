package com.example.fallguardian;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;

import static com.example.fallguardian.NotifApp.CHANNEL_2_ID;
import static com.example.fallguardian.NotifApp.CHANNEL_3_ID;


class LocationAndSMS {
    Context context;

    ///regarding sms
    String phoneNo, message;


    //location information
    String user_map_location; //[latitude],[longitude]
    String google_map;
    FusedLocationProviderClient fusedLocationClient;
    Elderly elderly;

    ///permissions
    public static final int PERMISSIONS = 99;

    public LocationAndSMS(Context c) {
        user_map_location = "";
        google_map = "https://maps.google.com/maps?q=";
        context = c;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void askPermissions(){
        ///request permission for sending sms
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    PERMISSIONS);
        }
    }


    public void sendSMSMessage(boolean isEmergency) {
        try {
            phoneNo = elderly.getMonitor_phone_number();

            Calendar calendar = Calendar.getInstance();
            int hour24hrs = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            String time = hour24hrs+":"+minutes;


            if(isEmergency){
                message = elderly.getFirstName()+" "+elderly.getLastName()+" needs emergency assistance at "+time+" at location: "+user_map_location+" .";
            }
            else{
                message = elderly.getFirstName()+" "+elderly.getLastName()+" fell down at "+time+" and may be injured"+" at location: "+user_map_location+" .";
            }


            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (context instanceof Activity) {
                    // handle activity case
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSIONS);
                } else if (context instanceof Service){
                    // handle service case
                    Intent activityIntent = new Intent(context,SensorActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(context,0,activityIntent,0);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

                    Notification notification = new NotificationCompat.Builder(context,CHANNEL_3_ID)
                            .setContentTitle("ALLOW PERMISSIONS!")
                            .setContentText("Please allow permissions for accessing Location & sending SMS!")
                            .setSmallIcon(R.drawable.ic_service)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .build();

                    notificationManagerCompat.notify(3,notification);

                }

            }
            else{
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, message, null, null);
                Toast.makeText(context, "SMS sent.", Toast.LENGTH_LONG).show();
                Log.d("SensorActivty","SMS SENT ______________________________________________________________");
            }

        }
        catch(NullPointerException e){
            Toast.makeText(context,
                    "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            System.out.println("Exception Caught");
        }

    }

    public void getLocationAndSendSMS(boolean isEmergency){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSIONS);
        }
        else{
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null){
                        user_map_location = google_map + location.getLatitude() + "," + location.getLongitude();
                        Log.d("SensorActivity",user_map_location+" ********************************************************************* ");
                        if(isEmergency)
                        {
                            sendSMSMessage(true);
                        }
                        else{
                            sendSMSMessage(false);
                        }

                    }
                    else{
                        user_map_location = null;
                        Log.d("SensorActivity","Location is null ********************************************************************* ");

                        user_map_location = "Location unavailable";
                        if(isEmergency)
                        {
                            sendSMSMessage(true);
                        }
                        else{
                            sendSMSMessage(false);
                        }

                    }
                }
            });

            locationTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("SensorActivity","Failure in location ********************************************************************* ");
                }
            });

            locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                }
            });
        }

    }

    public void setElderly(Elderly elderly) {
        this.elderly = elderly;
    }

}
