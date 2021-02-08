package com.example.fallguardian;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;



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


    public void sendSMSMessage() {
        try {
            phoneNo = elderly.getMonitor_phone_number();
            message = elderly.getFirstName()+" "+elderly.getLastName()+" fell down and might be injured"+" at location: "+user_map_location+" .";
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        PERMISSIONS);
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
                    "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            System.out.println("Exception Caught");
        }

    }

    public void getLocationAndSendSMS(){
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
                        sendSMSMessage();

                    }
                    else{
                        user_map_location = null;
                        Log.d("SensorActivity","Location is null ********************************************************************* ");

                        user_map_location = "Location unavailable";
                        sendSMSMessage();
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
