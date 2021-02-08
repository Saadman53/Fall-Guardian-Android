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

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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


public class SensorActivity extends AppCompatActivity implements SensorEventListener, FallDialogue.FallDialogueListener {

    private static final String TAG = "LogInActivity";

    ///Initializing sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    ///initializing gravity vector
    private double accelerometer_values[] = {0.0, 0.0, 0.0};
    private double gravity[] = {0.0, 0.0, 0.0};
    private boolean collect_data;
    double start_time, curr_time;



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

    FallDetector fallDetector;

    ///invoke when user is on pause
    private boolean isOnPause;




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

        ///Initializing Sensor Services
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        ///registering Accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(SensorActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);  //SensorManager.SENSOR_DELAY_NORMAL
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(SensorActivity.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);  //SensorManager.SENSOR_DELAY_NORMAL
        }


        collect_data = true;
        isOnPause = false;
        list_ACC = new ArrayList<Data_ACC>();
        list_both = new ArrayList<Data>();
        Toast.makeText(getApplicationContext(), "Data is being collected", Toast.LENGTH_SHORT).show();

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
                    //locationAndSMS.setElderly(current_elderly_user);
                    fallDetector.setElderly(current_elderly_user);

                    if(!current_elderly_user.getFirstLogin()){
                        databaseReference.child(user.getUid()).child("firstLogin").setValue(true);
                        isOnPause = false;
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

        fallDetector = new FallDetector(this);


        Log.d("Activity","USER IS ______________________________TURNED____________________________________ON CREATE");

    }




    @Override
    protected void onStart() {
        super.onStart();
        collect_data = true;
        isOnPause = false;

        fallDetector.disableFallDetection();
        //if(!fall_detected) Toast.makeText(this,"OFF____________________________________________________________________________OOOOOOOOOOOOOOOOOFFFFFFFFFFFFFFF",Toast.LENGTH_LONG).show();

        ///incase user doesnt tap on to pending notification
        if(fallDetector.getNotificationEnabled()){
            Toast.makeText(this,"Fall Detected!",Toast.LENGTH_SHORT).show();
            fallDetector.cancelNotification(1);
            fallDetector.enableFallDetection();
            fallDetector.initiateFallDialogue();
        }
        //isNotificationEnabled = false;
        fallDetector.setNotificationEnabled(false);
        Log.d("Activity","USER IS ______________________________TURNED____________________________________ON START");
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
            FirebaseAuth.getInstance().signOut();
            collect_data = false;
            finish();

            Intent intent = new Intent(SensorActivity.this, LogInActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.emergencyId){
            //locationAndSMS.getLocationAndSendSMS();
            fallDetector.locationAndSMS.getLocationAndSendSMS();
        }
        else if(item.getItemId()==R.id.updateId){
            collect_data = false;
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

        sensorManager.registerListener(SensorActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);  //SensorManager.SENSOR_DELAY_NORMAL
        if (gyroscope != null) {
            sensorManager.registerListener(SensorActivity.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);  //SensorManager.SENSOR_DELAY_NORMAL
        }
        Log.d("Activity","USER IS ______________________________TURNED____________________________________ON PAUSE");
    }




    private int binary_ACC(double time, List<Data_ACC> list, int length) {
        int l = -1;
        int h = length;


        while ((l + 1) < h) {
            int mid = (l + h) / 2;
            if ((list.get(mid).getTimestamp() / 1000.0) < ((int) time + 1)) {
                l = mid;
            } else {
                h = mid;
            }
        }
        return l;
    }

    private int binary_both(double time, List<Data> list, int length) {
        int l = -1;
        int h = length;


        while ((l + 1) < h) {
            int mid = (l + h) / 2;
            if ((list.get(mid).getTimestamp() / 1000.0) < ((int) time + 1)) {
                l = mid;
            } else {
                h = mid;
            }
        }
        return l;
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (collect_data) {
            ///if gyroscope is absent
            if (gyroscope == null) {
                ///invoke only for accelerometer

                accelerometer_values[0] = event.values[0];
                accelerometer_values[1] = event.values[1];
                accelerometer_values[2] = event.values[2];

                addData_ACC(accelerometer_values[0], accelerometer_values[1], accelerometer_values[2]);
            } else {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    accelerometer_values[0] = event.values[0];
                    accelerometer_values[1] = event.values[1];
                    accelerometer_values[2] = event.values[2];
                } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    addData_both(accelerometer_values[0], accelerometer_values[1], accelerometer_values[2], event.values[0], event.values[1], event.values[2]);
                }
            }

            fallDetector.countFallTimer(isOnPause);
        }


    }

    ///invoke for accelerometer only
    public void addData_ACC(double AX, double AY, double AZ) {
        //String key = databaseReference.push().getKey();
        Long ts = System.currentTimeMillis();

        gravity[0] = 0.8 * gravity[0] + 0.2 * AX;
        gravity[1] = 0.8 * gravity[1] + 0.2 * AY;
        gravity[2] = 0.8 * gravity[2] + 0.2 * AZ;
        Data_ACC data = new Data_ACC(AX, AY, AZ, gravity[0], gravity[1], gravity[2], ts);
        list_ACC.add(data);

        curr_time = System.currentTimeMillis() / 1000.0;



        if (curr_time - start_time > 6.0) {
            int index = binary_ACC(start_time, list_ACC, list_ACC.size());
            List<Data_ACC> temp;
            temp = list_ACC;

            list_ACC = list_ACC.subList(index + 1, list_ACC.size());
            start_time = (list_ACC.get(0).getTimestamp()) / 1000.0;
            fallDetector.createPost_ACC(temp,isOnPause);

        }
    }

    public void addData_both(double AX, double AY, double AZ, double GX, double GY, double GZ) {
        //String key = databaseReference.push().getKey();
        Long ts = System.currentTimeMillis();

        gravity[0] = 0.8 * gravity[0] + 0.2 * AX;
        gravity[1] = 0.8 * gravity[1] + 0.2 * AY;
        gravity[2] = 0.8 * gravity[2] + 0.2 * AZ;
        Data data = new Data(AX, AY, AZ, GX, GY, GZ, gravity[0], gravity[1], gravity[2], ts);
        list_both.add(data);


        curr_time = System.currentTimeMillis() / 1000.0;

        if (curr_time - start_time > 6.0) {
            int index = binary_both(start_time, list_both, list_both.size());


            List<Data> temp;
            temp = list_both;
            list_both = list_both.subList(index + 1, list_both.size());
            start_time = (list_both.get(0).getTimestamp()) / 1000.0;

            fallDetector.createPost_both(temp,isOnPause);

        }

    }


    @Override
    public void applyText(String fall) {
        if (fall.equals("2")) {
            ///Send sms
            if (current_elderly_user != null) {
                fallDetector.locationAndSMS.getLocationAndSendSMS();
            }
        } else if (fall.equals("1")) {
            Toast.makeText(this, "Incase of emergency press 'Emergency Distress Signal' option from the menu.", Toast.LENGTH_LONG).show();
        } else if (fall.equals("0")) {
            Toast.makeText(this, "Better safe than sorry!", Toast.LENGTH_SHORT).show();
            fallDetector.dismissFallDialogue();
        }

        fallDetector.disableFallDetection();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(fallDetector.isFall_detected()){
                        fallDetector.locationAndSMS.sendSMSMessage();
                    }
                } else {
                    Toast.makeText(this,
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

        }

    }



}
