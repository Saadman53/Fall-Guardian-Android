package com.example.fallguardian;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.os.Vibrator;

import static com.example.fallguardian.NotifApp.CHANNEL_1_ID;


public class SensorActivity extends AppCompatActivity implements SensorEventListener, FallDialogue.FallDialogueListener {

    private static final String TAG = "LogInActivity";
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

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
    FirebaseAuth mAuth;


    //Datastructures
    List<Data_ACC> list_ACC;
    List<Data> list_both;


    ///communication medium to client server
    Retrofit retrofit;
    Client_ACC client_acc;
    Client_both client_both;


    //Tarik wifi ipV6: "https://192.168.1.103:8000/"
    //"https://app.fakejson.com/"
    //my phone: "http://192.168.43.180:8000/"
    //Tarik wifi ipV4: "http://192.168.1.101:8000/"
    private final String BaseUrl = "http://192.168.1.101:8000/";



    ///current user data
    FirebaseUser user;
    Elderly current_elderly_user;

    ///display informations
    TextView userName, userPhone, monitorName, monitorPhone;


    ///regarding sms
    boolean got_permission_to_send_sms;
    String phoneNo, message;


    int prev_response;
    boolean fall_detected;
    double fall_detection_time;
    Vibrator v;
    ///invoke when user is on pause
    private boolean isOnPause;



    //location information
    String user_map_location = ""; //[latitude],[longitude]
    String google_map = "https://maps.google.com/maps?q=";
    FusedLocationProviderClient fusedLocationClient;

    ///permissions
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;








    ///dialogue box
    FallDialogue fallDialogue;
    ///Notificaiton
    NotificationManagerCompat notificationManager;
    Notification notification;
    boolean isNotificationEnabled;





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


        retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        client_acc = retrofit.create(Client_ACC.class);
        client_both = retrofit.create(Client_both.class);


        fall_detected = false;
        fall_detection_time = 10000000000000.0;
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

        mAuth = FirebaseAuth.getInstance();

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


        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        prev_response = 0;
        isNotificationEnabled = false;


        got_permission_to_send_sms = false;
        ///request permission for sending sms
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }


        notificationManager = NotificationManagerCompat.from(this);



        Bundle extra = getIntent().getExtras();

        fall_detected = false;
        fall_detection_time = 10000000000000.0;


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d("Activity","USER IS ______________________________TURNED____________________________________ON CREATE");

    }

    private void getLocationAndSendSMS(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        else{
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null){
                        user_map_location = google_map + location.getLatitude() + "," + location.getLongitude();
                        Log.d(TAG,user_map_location+" ********************************************************************* ");
                        sendSMSMessage();

                    }
                    else{
                        user_map_location = null;
                        Log.d(TAG,"Location is null ********************************************************************* ");

                        user_map_location = "Location unavailable";
                        sendSMSMessage();
                    }
                }
            });

            locationTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG,"Failure in location ********************************************************************* ");
                }
            });

            locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                }
            });
        }

    }



    @Override
    protected void onStart() {
        super.onStart();
        collect_data = true;
        isOnPause = false;
        fall_detected = false;
        fall_detection_time = 10000000000000.0;
        //if(!fall_detected) Toast.makeText(this,"OFF____________________________________________________________________________OOOOOOOOOOOOOOOOOFFFFFFFFFFFFFFF",Toast.LENGTH_LONG).show();

        ///incase user doesnt tap on to pending notification
        if(isNotificationEnabled){
            Toast.makeText(this,"Fall Detected!",Toast.LENGTH_SHORT).show();
            notificationManager.cancel(1);
            fall_detected = true;
            fall_detection_time = System.currentTimeMillis() / 1000.0;
            initiateFallDialogue();
        }
        isNotificationEnabled = false;
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

            getLocationAndSendSMS();
            //Toast.makeText(this,"SMS SENT",Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId()==R.id.updateId){
            collect_data = false;
            finish();
            Intent intent = new Intent(this,UpdateUserData.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

        //locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
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





    public void initiateFallDialogue(){
        try{
            fallDialogue = new FallDialogue("Fall Detected!","Oh no! Are you injured?!");
            fallDialogue.show(getSupportFragmentManager(), "fall dialogue");
        }
        catch (Exception e){
            e.printStackTrace();
        }
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

            if (fall_detected) {
                Log.d(TAG,"User fell down and time of falling is __________________________________:           "+fall_detection_time);
                ///check if 20 seconds have passed since user hasn't responded to the fall dialogue
                //user might be injured
                if (( (System.currentTimeMillis() / 1000.0) - fall_detection_time) >= 20.0) {
                    fall_detected = false;
                    fall_detection_time = 10000000000000.0;

                    getLocationAndSendSMS();
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

            if(isOnPause){
                Log.d("pause","USER IS__________________________________ON PAUSE");
            }
            else{
                Log.d("pause","USER IS__________________________________NOT ON PAUSE");
            }
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
            createPost_ACC(temp);

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

            createPost_both(temp);

        }

    }

    private void createPost_ACC(List<Data_ACC> list) {

        Call<Fall> call = client_acc.GetPostValue_ACC(list);
        call.enqueue(new Callback<Fall>() {
            @Override
            public void onResponse(Call<Fall> call, Response<Fall> response) {
                Fall hasFall = response.body();
                try {
                    if (hasFall.getFall() == 1) {

                        if (prev_response == 0) {
                            Toast.makeText(getApplicationContext(), "Fall Detected!", Toast.LENGTH_LONG).show();

                            fall_detected = true;
                            fall_detection_time = System.currentTimeMillis() / 1000.0;

                            if(!isOnPause){
                                initiateFallDialogue();
                            }
                            else{
                                ///show notification
                                sendNotification();
                            }

                            Log.d(TAG, "User has________________________________________________________________fallen_________" + current_elderly_user.getMonitor_phone_number());

                            ///initiate timer

                            v.vibrate(1000);

                        }

                    }
                    prev_response = hasFall.getFall();
                    //Log.d(TAG, "Response__________________________________________________SUCCESS______" + hasFall.getFall());
                } catch (NullPointerException e) {
                    Log.d(TAG, "CAUGHT++++++++++++++++++++EXCEPTION++++++++++============" + e);

                }

            }

            @Override
            public void onFailure(Call<Fall> call, Throwable t) {
                Log.d(TAG, "_____________________________________________FAILURE_____________" + String.valueOf(t));
            }
        });
    }

    private void createPost_both(List<Data> list) {

        Call<Fall> call = client_both.GetPostValue_both(list);
        call.enqueue(new Callback<Fall>() {
            @Override
            public void onResponse(Call<Fall> call, Response<Fall> response) {
                Fall hasFall = response.body();
                if (hasFall.getFall() == 1) {
                    if (prev_response == 0) {
                        Toast.makeText(getApplicationContext(), "Fall Detected!", Toast.LENGTH_LONG).show();

                        fall_detected = true;
                        fall_detection_time = System.currentTimeMillis() / 1000.0;

                        if(!isOnPause){
                            initiateFallDialogue();
                        }
                        else{
                            ///show notification
                            sendNotification();
                        }
                        Log.d(TAG, "User has________________________________________________________________fallen_________" + current_elderly_user.getMonitor_phone_number());


                        ///initiate timer


                        v.vibrate(1000);

                    }
                }
                prev_response = hasFall.getFall();
               // Log.d(TAG, "Response__________________________________________________SUCCESS______" + hasFall.getFall());
            }

            @Override
            public void onFailure(Call<Fall> call, Throwable t) {
                Log.d(TAG, "___________________________________ERROR FOR GYRO+_________________" + String.valueOf(t));
            }
        });
    }


    @Override
    public void applyText(String fall) {
        if (fall.equals("2")) {
            Toast.makeText(getApplicationContext(), "Selected Yes, send SMS", Toast.LENGTH_SHORT).show();
            ///Send sms
            if (current_elderly_user != null) {
                getLocationAndSendSMS();
                //Toast.makeText(this,"SMS SENT",Toast.LENGTH_SHORT).show();
            }
        } else if (fall.equals("1")) {
            Toast.makeText(getApplicationContext(), "Selected Yes, don't send SMS", Toast.LENGTH_SHORT).show();
        } else if (fall.equals("0")) {
            Toast.makeText(getApplicationContext(), "Selected No", Toast.LENGTH_SHORT).show();
            fallDialogue.dismiss();
        }

        fall_detected = false;
        fall_detection_time = 100000000000000.0;
    }

    public void sendNotification(){

//        Bundle notifBundle = new Bundle();
//        notifBundle.putString("fall_detected","yes");

        Intent activityIntent = new Intent(this,SensorActivity.class);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //activityIntent.putExtras(notifBundle);

        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);



        notification = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_fall)
                .setContentTitle("Fall Detected!")
                .setContentText("You seem to have fallen. Click to open app")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(1,notification);
        isNotificationEnabled = true;

    }

    public void initiateFallDetection(){
        fall_detected = true;
        fall_detection_time = System.currentTimeMillis() / 1000.0;
    }

    public void disableFallDetection(){
        fall_detected = false;
        fall_detection_time = 10000000000000.0;
    }



    protected void sendSMSMessage() {
        try {
            phoneNo = current_elderly_user.getMonitor_phone_number();
            message = current_elderly_user.getFirstName()+" "+current_elderly_user.getLastName()+" fell down and might be injured"+" at location: "+user_map_location+" .";
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
            else{
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                    Log.d(TAG,"SMS SENT ______________________________________________________________");
            }

        }
        catch(NullPointerException e){
            Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            System.out.println("Exception Caught");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ///when invoked at the start of app, donot send any sms just apply permissions
                    if(!got_permission_to_send_sms){
                        got_permission_to_send_sms = true;
                    }
                    else{
                        sendSMSMessage();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }

    }



}
