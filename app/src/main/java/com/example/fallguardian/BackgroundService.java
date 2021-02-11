package com.example.fallguardian;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

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
import com.squareup.okhttp.OkHttpClient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.fallguardian.NotifApp.CHANNEL_1_ID;
import static com.example.fallguardian.NotifApp.CHANNEL_2_ID;

public class BackgroundService extends Service implements SensorEventListener{


    Notification notification;

    ///Initializing sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    ///initializing gravity vector
    private double accelerometer_values[] = {0.0, 0.0, 0.0};
    private double gravity[] = {0.0, 0.0, 0.0};
    private boolean collect_data;
    double start_time, curr_time;

    //Datastructures
    List<Data_ACC> list_ACC;
    List<Data> list_both;

    ///current user data

    Elderly current_elderly_user;

    ///display informations
    TextView userName, userPhone, monitorName, monitorPhone;

    private Communicator communicator;
    LocationAndSMS locationAndSMS;

    int prev_response;
    boolean fall_detected;
    double fall_detection_time;
    private Vibrator v;



    ///dialogue box
    private FallDialogue fallDialogue;
    ///Notificaiton
    public NotificationManagerCompat notificationManager;
    boolean isNotificationEnabled;




    @Override
    public void onCreate() {
        super.onCreate();


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

        ///Initializing Sensor Services
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        ///registering Accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(BackgroundService.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);  //SensorManager.SENSOR_DELAY_NORMAL
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(BackgroundService.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);  //SensorManager.SENSOR_DELAY_NORMAL
        }
        collect_data = true;
        list_ACC = new ArrayList<Data_ACC>();
        list_both = new ArrayList<Data>();


        locationAndSMS = new LocationAndSMS(this);

        prev_response = 0;
        fall_detected = false;
        fall_detection_time = 100000000000000.0;
        ///initialize communicator instance
        communicator =  new Communicator();
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        isNotificationEnabled = false;
        notificationManager = NotificationManagerCompat.from(this);



        ///Database
        DatabaseReference databaseReference;
        FirebaseUser user;

        user = FirebaseAuth.getInstance().getCurrentUser();



        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    current_elderly_user = snapshot.child(user.getUid()).getValue(Elderly.class);
                    locationAndSMS.setElderly(current_elderly_user);
                }
                else{
                    Toast.makeText(BackgroundService.this,"Couldn't retrieve user data",Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
            countFallTimer();
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




        if ((System.currentTimeMillis() / 1000.0) - start_time > 6.0) {
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


        if ((System.currentTimeMillis() / 1000.0) - start_time > 6.0) {
            int index = binary_both(start_time, list_both, list_both.size());


            List<Data> temp;
            temp = list_both;
            list_both = list_both.subList(index + 1, list_both.size());
            start_time = (list_both.get(0).getTimestamp()) / 1000.0;

           createPost_both(temp);

        }

    }





    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if((intent.getExtras()!=null)){
            String value = intent.getStringExtra("Response");
            if(value!=null){
                if(value.equals("YES")){
                    disableFallDetection();
                    locationAndSMS.getLocationAndSendSMS();
                    isNotificationEnabled =false;
                    //Toast.makeText(this,"",Toast.LENGTH_SHORT).show();
                }
                else if(value.equals("NO")){
                    disableFallDetection();
                    isNotificationEnabled = false;
                    Toast.makeText(this,"Better safe than sorry!",Toast.LENGTH_SHORT).show();
                }
                else if(value.equals("YESNO")){
                    disableFallDetection();
                    isNotificationEnabled = false;
                    Toast.makeText(this,"Incase of injury click 'Emergency Distress Call' from the options menu.",Toast.LENGTH_SHORT).show();
                }

            }
            else{

            }
        }

        Intent activityIntent = new Intent(this,SensorActivity.class);
        activityIntent.putExtra("flag","not_fall");
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);

        notification = new NotificationCompat.Builder(this,CHANNEL_2_ID)
                .setContentTitle("Fall Gudarian")
                .setContentText("Running on Background.")
                .setSmallIcon(R.drawable.ic_service)
                .setContentIntent(contentIntent)
                .build();
        startForeground(2,notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

        Intent activityIntent = new Intent(this,SensorActivity.class);
        activityIntent.putExtra("flag","fall");
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);


        Intent broadCastIntentYES = new Intent(this,NotificationReceiverYES.class);
        broadCastIntentYES.putExtra("Selected","Yes");
        PendingIntent actionIntentYES = PendingIntent.getBroadcast(this,0,broadCastIntentYES,PendingIntent.FLAG_CANCEL_CURRENT);

        Intent broadCastIntentNO = new Intent(this,NotificationReceiverNO.class);
        broadCastIntentNO.putExtra("Selected","No");
        PendingIntent actionIntentNO = PendingIntent.getBroadcast(this,0,broadCastIntentNO,PendingIntent.FLAG_CANCEL_CURRENT);

        Intent broadCastIntentYESNO = new Intent(this,NotificationReceiverYESNO.class);
        broadCastIntentYES.putExtra("Selected","YesNo");
        PendingIntent actionIntentYESNO = PendingIntent.getBroadcast(this,0,broadCastIntentYESNO,PendingIntent.FLAG_CANCEL_CURRENT);



        Notification notification = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_fall)
                .setContentTitle("Fall Detected!")
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Did you fell down?")
                        .addLine("Do you want to send SMS?")
                        .addLine("SMS will be sent if you don't respond in 20 seconds!")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setWhen(System.currentTimeMillis())
                .setUsesChronometer(true)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_yes,"Yes, SMS!",actionIntentYES)
                .addAction(R.drawable.ic_no,"No didn't fall!",actionIntentNO)
                .addAction(R.drawable.ic_no,"Yes, don't SMS .",actionIntentYESNO)
                .build();
        notificationManager.notify(1,notification);
        isNotificationEnabled = true;

    }

    public void cancelNotification(int id){
        notificationManager.cancel(id);
    }

    public void createPost_ACC(List<Data_ACC> list) {

        Call<Fall> call = communicator.getClient_acc().GetPostValue_ACC(list);
        call.enqueue(new Callback<Fall>() {
            @Override
            public void onResponse(Call<Fall> call, Response<Fall> response) {
                Fall hasFall = response.body();
                try {
                    if (hasFall.getFall() == 1) {

                        if (prev_response == 0) {
                            Toast.makeText(BackgroundService.this, "Fall Detected!", Toast.LENGTH_LONG).show();
                            enableFallDetection();
                            sendNotification();

                            v.vibrate(1000);

                        }

                    }
                    prev_response = hasFall.getFall();
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

    public void createPost_both(List<Data> list) {

        Call<Fall> call = communicator.getClient_both().GetPostValue_both(list);
        call.enqueue(new Callback<Fall>() {
            @Override
            public void onResponse(Call<Fall> call, Response<Fall> response) {
                Fall hasFall = response.body();
                if (hasFall.getFall() == 1) {
                    if (prev_response == 0) {
                        Toast.makeText(BackgroundService.this, "Fall Detected!", Toast.LENGTH_LONG).show();

                              enableFallDetection();
                              sendNotification();

                        v.vibrate(1000);

                    }
                }
                prev_response = hasFall.getFall();
            }

            @Override
            public void onFailure(Call<Fall> call, Throwable t) {
                Log.d("SensorActivity", "___________________________________ERROR FOR GYRO+_________________" + String.valueOf(t));
            }
        });
    }


    public void countFallTimer(){
        if (fall_detected) {
            Log.d("Sensor Activity","User fell down and time of falling is _________________________________________________________:           "+fall_detection_time);
            ///check if 20 seconds have passed since user hasn't responded to the fall dialogue
            //user might be injured
            if (( (System.currentTimeMillis() / 1000.0) - fall_detection_time) >= 20.0) {
                locationAndSMS.getLocationAndSendSMS();
                disableFallDetection();
                    if(isNotificationEnabled){
                        ///dismiss the notification
                        notificationManager.cancel(1);
                        isNotificationEnabled = false;
                    }

            }
        }

    }
}
