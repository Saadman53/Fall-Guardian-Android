package com.example.fallguardian;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
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


public class SensorActivity extends AppCompatActivity {

    private static final String TAG = "LogInActivity";


    ///Database
    DatabaseReference databaseReference;
    FirebaseUser user;

    ///display informations
    TextView userName, monitorPhone;

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



        user = FirebaseAuth.getInstance().getCurrentUser();



        databaseReference = FirebaseDatabase.getInstance().getReference("users");


        userName = findViewById(R.id.userNameId);

        monitorPhone = findViewById(R.id.monitorPhoneId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    Elderly elderly = snapshot.child(user.getUid()).getValue(Elderly.class);
                    locationAndSMS.setElderly(elderly);



                    String username = "User: " + elderly.getFirstName() + " " + elderly.getLastName();


                    String monitorphone = "Monitor's Phone: " + elderly.getMonitor_phone_number();

                    userName.setText(username);

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
                locationAndSMS.getLocationAndSendSMS(true);
            }
        });





        Log.i("Activity","USER IS ______________________________TURNED____________________________________ON CREATE");


        Bundle extras =  getIntent().getExtras();
        if(extras!=null){
            String sendSMS = extras.getString("information");

            if(sendSMS!=null){
                if(sendSMS.equals("updated") && isMyServiceRunning(BackgroundService.class)){
                    Intent intent = new Intent(this,BackgroundService.class);
                    intent.putExtra("info","updated");
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
           if(isMyServiceRunning(BackgroundService.class)){
               stopService();
           }
            FirebaseAuth.getInstance().signOut();
            finish();


            Intent intent = new Intent(SensorActivity.this, LogInActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.startSystemId){
           if(isMyServiceRunning(BackgroundService.class)){
               Toast.makeText(this,"System is already running",Toast.LENGTH_SHORT).show();
           }
           else{
               startService();
           }
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
        else  if(item.getItemId()==R.id.termsId){
           Intent intent = new Intent(this,About.class);
           //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
           startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onPause() {

        super.onPause();
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
