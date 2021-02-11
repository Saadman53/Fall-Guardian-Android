package com.example.fallguardian;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;


import com.google.android.gms.security.ProviderInstaller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;



import static com.example.fallguardian.LocationAndSMS.PERMISSIONS;


public class SensorActivity extends AppCompatActivity implements FallDialogue.FallDialogueListener {

    private static final String TAG = "LogInActivity";


    ///Database
    DatabaseReference databaseReference;
    FirebaseUser user;


    //Datastructures
    List<Data_ACC> list_ACC;
    List<Data> list_both;






    ///current user data

    Elderly current_elderly_user;

    ///display informations
    TextView userName, userPhone, monitorName, monitorPhone;


    ///invoke when user is on pause
    private boolean isOnPause;


    Button emergencyButton;


    LocationAndSMS locationAndSMS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        try {
            // Google Play will install latest OpenSSL
            ProviderInstaller.installIfNeeded(getApplicationContext());
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
                | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }


        isOnPause = false;


        isOnPause = false;

        user = FirebaseAuth.getInstance().getCurrentUser();



        databaseReference = FirebaseDatabase.getInstance().getReference("users");


        userName = findViewById(R.id.userNameId);
        userPhone = findViewById(R.id.userPhoneId);
        monitorName = findViewById(R.id.monitorNameId);
        monitorPhone = findViewById(R.id.monitorPhoneId);

        isOnPause = false;

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    current_elderly_user = snapshot.child(user.getUid()).getValue(Elderly.class);
                    locationAndSMS.setElderly(current_elderly_user);


                    if(!current_elderly_user.getFirstLogin()){
                        databaseReference.child(user.getUid()).child("firstLogin").setValue(true);
                        isOnPause = false;
                        PreferenceManager.getDefaultSharedPreferences(SensorActivity.this).edit().putBoolean("isActive", true).apply();
                    }


                    String username = "User: " + current_elderly_user.getFirstName() + " " + current_elderly_user.getLastName();

                    String userphone = "User no: " + current_elderly_user.getPhone_number();
                    String monitorname = "User's Monitor: " + current_elderly_user.getMonitor_first_name() + " " + current_elderly_user.getMonitor_last_name();
                    String monitorphone = "Monitor's Phone: " + current_elderly_user.getMonitor_phone_number();

                    userName.setText(username);
                    userPhone.setText(userphone);
                    monitorName.setText(monitorname);
                    monitorPhone.setText(monitorphone);
                    Toast.makeText(SensorActivity.this,"Retrieved user data",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SensorActivity.this,"Couldn't retrieve user data",Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }




        });

        locationAndSMS = new LocationAndSMS(this);
        locationAndSMS.askPermissions();


        emergencyButton = findViewById(R.id.emergencyID);

        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationAndSMS.getLocationAndSendSMS();
            }
        });





        Log.i("Activity","USER IS ______________________________TURNED____________________________________ON CREATE");


        if(getIntent().getExtras()!=null){
            String sendSMS = getIntent().getStringExtra("sendSMS");

            if(sendSMS!=null){
                Log.i("RECEIVER", "onReceive ACTIVITY: ______________________________"+sendSMS);
                if(sendSMS.equals("YES")){
                    Intent intent = new Intent(this,BackgroundService.class);
                    intent.putExtra("Response","yes");
                    Toast.makeText(this,"Send SMS",Toast.LENGTH_SHORT).show();
                    startService(intent);
                }
                else if(sendSMS.equals("NO")){
                    Intent intent = new Intent(this,BackgroundService.class);
                    intent.putExtra("Response","no");
                    Toast.makeText(this,"Dont Send SMS",Toast.LENGTH_SHORT).show();
                    startService(intent);
                }
            }
        }

    }




    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", true).apply();

        Log.i("ACTIVITY CYCLE", "________________________________________ON START");

        if(getIntent().getExtras()!=null){
            String sendSMS = getIntent().getStringExtra("sendSMS");

            if(sendSMS!=null){
                Log.i("RECEIVER", "onReceive ACTIVITY: ______________________________"+sendSMS);
                if(sendSMS.equals("YES")){
                    Intent intent = new Intent(this,BackgroundService.class);
                    intent.putExtra("Response","yes");
                    Toast.makeText(this,"Send SMS",Toast.LENGTH_SHORT).show();
                    startService(intent);
                }
                else if(sendSMS.equals("NO")){
                    Intent intent = new Intent(this,BackgroundService.class);
                    intent.putExtra("Response","no");
                    Toast.makeText(this,"Dont Send SMS",Toast.LENGTH_SHORT).show();
                    startService(intent);
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("ACTIVITY CYCLE", "________________________________________ON RESTART");

        locationAndSMS.askPermissions();
    }

    ///handling the options menu here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       if (item.getItemId() == R.id.signOutMenuId) {
            stopService();
            FirebaseAuth.getInstance().signOut();
            finish();


            Intent intent = new Intent(SensorActivity.this, LogInActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.startSystemId){
            startService();
        }
        else if(item.getItemId() == R.id.stopSystemId){
            stopService();
        }
        else if(item.getItemId()==R.id.updateId){
            finish();
            Intent intent = new Intent(this,UpdateUserData.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onPause() {

        super.onPause();

        isOnPause = true;

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", false).apply();


        Log.i("ACTIVITY CYCLE", "________________________________________ON PAUSE");

    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", true).apply();
        locationAndSMS.askPermissions();
        Log.i("ACTIVITY CYCLE", "________________________________________ON RESUME");

    }

    protected void onDestroy(){
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", false).apply();
        Log.i("ACTIVITY CYCLE", "________________________________________ON DESTROY");

    }

    private void startService(){
        Toast.makeText(this,"Service Started",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,BackgroundService.class);
        startService(intent);
    }
    private void stopService(){
        Toast.makeText(this,"Service Stopped",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,BackgroundService.class);
        stopService(intent);
    }

    @Override
    public void applyText(String fall) {
        Intent intent = new Intent(this,BackgroundService.class);
        intent.putExtra("Response",fall);
        startService(intent);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                }
            }

        }

    }



}
