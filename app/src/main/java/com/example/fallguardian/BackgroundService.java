package com.example.fallguardian;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

class Pair<L,R> {
    private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public L getL(){ return l; }
    public R getR(){ return r; }
    public void setL(L l){ this.l = l; }
    public void setR(R r){ this.r = r; }
}

public class BackgroundService extends Service implements SensorEventListener{

    ///Initializing sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    ///initializing gravity vector
    private double accelerometer_values[] = {0.0, 0.0, 0.0};
    private double gravity[] = {0.0, 0.0, 0.0};
    private boolean collect_data = false;
    double start_time;

    //Datastructures
    List<Data_ACC> list_ACC;
    List<Data> list_both;

    List<Pair<Double,Double>> maxAccList;

    ///current user data

    Elderly current_elderly_user;

    int age;

    int timeLimit = 25;

    private Communicator communicator;
    LocationAndSMS locationAndSMS;

    int prev_response;
    boolean fall_detected;
    boolean post_fall_movement_detected;
    double fall_detection_time;
    double post_fall_movement_time = 0.0;
    private Vibrator v;

    ///Notificaiton
    public NotificationManagerCompat notificationManager;
    boolean isNotificationEnabled;

    ///Database
    DatabaseReference databaseReference;
    FirebaseUser user;





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
        sensorManager.registerListener(BackgroundService.this, accelerometer, 10000);  //SensorManager.SENSOR_DELAY_NORMAL 10000
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(BackgroundService.this, gyroscope,  10000);  //SensorManager.SENSOR_DELAY_NORMAL
        }
        collect_data = false;
        list_ACC = new ArrayList<Data_ACC>();
        list_both = new ArrayList<Data>();

        maxAccList = new ArrayList<Pair<Double,Double>>();

        maxAccList.add(new Pair(0.0,0.0));


        locationAndSMS = new LocationAndSMS(this);

        prev_response = 0;
        fall_detected = false;
        fall_detection_time = 100000000000000.0;
        ///initialize communicator instance
        communicator =  new Communicator();
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        isNotificationEnabled = false;
        notificationManager = NotificationManagerCompat.from(this);
        getDataFromDataBase();
    }

    private int getAge(String dobString){

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null) return 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int year = dob.get(Calendar.YEAR);
        int month = dob.get(Calendar.MONTH);
        int day = dob.get(Calendar.DAY_OF_MONTH);

        dob.set(year, month+1, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }



        return age;
    }

    void setTimeLimit(){
        if(age<=40){
            timeLimit = 25;
        }
        else{
            timeLimit = 25+((age-40)/10)*5;
        }
    }

    void getDataFromDataBase(){
        user = FirebaseAuth.getInstance().getCurrentUser();



        databaseReference = FirebaseDatabase.getInstance().getReference("users");


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    enableDataCollection();
                    current_elderly_user = snapshot.child(user.getUid()).getValue(Elderly.class);
                    age = getAge(current_elderly_user.getDob());
                    setTimeLimit();

                    Log.i("TAG", "onDataChange: ____________________________age :"+age);
                    locationAndSMS.setElderly(current_elderly_user);
                    Toast.makeText(BackgroundService.this,"User Data Updated In Service.",Toast.LENGTH_LONG).show();
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
    void enableDataCollection(){
        collect_data = true;
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


        Data_ACC data = new Data_ACC(AX, AY, AZ, ts);
        list_ACC.add(data);

        double maxVal = AX*AX+AY*AY+AZ*AZ;

        if( (int)(System.currentTimeMillis() / 1000.0) -  maxAccList.get(maxAccList.size()-1).getR().intValue()>=1 ){
            maxAccList.add(new Pair(maxVal,System.currentTimeMillis() / 1000.0));
        }
        else{
            maxAccList.get(maxAccList.size()-1).setL( Math.max(maxAccList.get(maxAccList.size()-1).getL(),maxVal) );
        }

        if ((System.currentTimeMillis() / 1000.0) - start_time >= 6.0) {
            int index = binary_ACC(start_time, list_ACC, list_ACC.size());
            List<Data_ACC> temp;
            temp = list_ACC;

            list_ACC = list_ACC.subList(index + 1, list_ACC.size());
            start_time = (list_ACC.get(0).getTimestamp()) / 1000.0;

            ///new code
            if(checkIfMax(maxAccList)){
              createPost_ACC(temp);
                Log.d("MAX POS VAL:","THE MAXIMUM VALUE IS ----------------------> 3 "+Integer.toString(maxAccList.size()));
            }
            maxAccList.remove(0);

            //createPost_ACC(temp);
        }
    }

    private boolean checkIfMax(List<Pair<Double, Double>> List) {
        int size = List.size();
        double maxVal = 0.0;
        int maxpos = 0;
       // Log.d("MAX POS LOOPZ:","LOOP STARTS HERE:");
        for(int i = 0;i<size-1;i++){
           // Log.d("MAX POS LOOPZ:","------------------------------------> "+Double.toString(List.get(i).getL()));
            if(List.get(i).getL()>maxVal){
                maxVal = List.get(i).getL();
                maxpos = i;
            }

        }
        if(fall_detected && !post_fall_movement_detected){
            for(int i = 3;i<size-1;i++){
                if(List.get(i).getL()>196){ ///14 m/s^2
                    Log.d("POST FALL MOVEMENT ", "POST FALL MOVEMENT DETECTED _-------------------------_ POST FALL MOVEMENT DETECTED "+List.get(i).getL());
                    post_fall_movement_detected = true;
                    post_fall_movement_time =  (System.currentTimeMillis()/1000.0)-fall_detection_time;
                    break;
                }
            }
        }
       // Log.d("MAX POS LOOPZ:","LOOP ENDS HERE:");

        maxpos = (int)(List.get(maxpos).getR() - List.get(0).getR());
        Log.d("MAX POS IZZZZZ:","------------------------------------> "+Integer.toString(maxpos));

        if(maxpos ==3 && maxVal>196) return true;
        else return false;
    }

    public void addData_both(double AX, double AY, double AZ, double GX, double GY, double GZ) {
        //String key = databaseReference.push().getKey();
        Long ts = System.currentTimeMillis();


        Data data = new Data(AX, AY, AZ, GX, GY, GZ, ts);
        list_both.add(data);

        double maxVal = AX*AX+AY*AY+AZ*AZ;

        if( (int)(System.currentTimeMillis() / 1000.0) -  maxAccList.get(maxAccList.size()-1).getR().intValue()>=1 ){
            maxAccList.add(new Pair(maxVal,System.currentTimeMillis() / 1000.0));
        }
        else{
            maxAccList.get(maxAccList.size()-1).setL( Math.max(maxAccList.get(maxAccList.size()-1).getL(),maxVal) );
        }


        if ((System.currentTimeMillis() / 1000.0) - start_time >= 6.0) {
            int index = binary_both(start_time, list_both, list_both.size());

            Log.i("HAHAHHAHAH","DATA IS BEING LOADED++++++++++++++++++++");
            List<Data> temp;
            temp = list_both;
            list_both = list_both.subList(index + 1, list_both.size());
            start_time = (list_both.get(0).getTimestamp()) / 1000.0;

            ///new code
            if(checkIfMax(maxAccList)){
                createPost_both(temp);
                Log.d("MAX POS VAL:","THE MAXIMUM VALUE IS ----------------------> 3 "+Integer.toString(maxAccList.size()));
            }
            maxAccList.remove(0);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if((intent.getExtras()!=null)){
            String value = intent.getStringExtra("Response");
            String any_updates = intent.getStringExtra("info");
            if(value!=null){
                if(value.equals("YES")){
                    locationAndSMS.getLocationAndSendSMS(false,post_fall_movement_detected,fall_detection_time,post_fall_movement_time);
                    disableFallDetection();
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
                    Toast.makeText(this,"Incase of injury click 'Emergency Distress SMS' button.",Toast.LENGTH_SHORT).show();
                }
                else if(value.equals("EMERGENCY")){
                    Toast.makeText(this,"Emergency text is send to monitor.",Toast.LENGTH_SHORT).show();
                    locationAndSMS.getLocationAndSendSMS(true,false,0.0,0.0);
                }

            }
            if(any_updates!=null){
                if(any_updates.equals("updated")){
                    getDataFromDataBase();

                }
            }
        }

        Intent activityIntent = new Intent(this,SensorActivity.class);
        activityIntent.putExtra("flag","fall");
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);

        Intent broadCastIntentEmergency = new Intent(this,NotificationReceiverEmergency.class);
        broadCastIntentEmergency.putExtra("Selected","Emergency");
        PendingIntent actionIntentEmergency = PendingIntent.getBroadcast(this,0,broadCastIntentEmergency,PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(this,CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_service)
                .setContentTitle("Fall Gudarian")
                .setColor(Color.BLUE)
                .setContentText("Running on Background.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.ic_emergency,"SMS EMERGENCY",actionIntentEmergency)
                .build();
        notificationManager.notify(2,notification);
        startForeground(2,notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        collect_data = false;
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
        post_fall_movement_detected = false;
    }

    public void disableFallDetection(){
        fall_detected = false;
        fall_detection_time = 100000000000000.0;
        post_fall_movement_detected = false;
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
        broadCastIntentYESNO.putExtra("Selected","YesNo");
        PendingIntent actionIntentYESNO = PendingIntent.getBroadcast(this,0,broadCastIntentYESNO,PendingIntent.FLAG_CANCEL_CURRENT);



        Notification notification = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_fall)
                .setContentTitle("Fall Detected!")
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Did you fell down?")
                        .addLine("Do you want to send SMS?")
                        .addLine("SMS will be sent if you don't respond in "+String.valueOf(timeLimit)+" seconds!")
                )
                .setColor(Color.RED)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setWhen(System.currentTimeMillis())
                .setUsesChronometer(true)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_yes,"YES SMS!",actionIntentYES)
                .addAction(R.drawable.ic_no,"NO",actionIntentNO)
                .addAction(R.drawable.ic_no,"YES,BUT I'M OK.",actionIntentYESNO)
                .build();
        notificationManager.notify(1,notification);
        isNotificationEnabled = true;

    }

    public void createPost_ACC(List<Data_ACC> list) {

        Call<Fall> call = communicator.getClient_acc().GetPostValue_ACC(list);
        call.enqueue(new Callback<Fall>() {
            @Override
            public void onResponse(Call<Fall> call, Response<Fall> response) {
                Fall hasFall = response.body();
                try {
                    Log.i("SERVICE", "onResponse: ++++++++++++++++++++++++++++ "+hasFall.getFall());
                    //fall = hasFall;
                    if (hasFall.getFall()==1) {

                        if (prev_response == 0) {
                            Toast.makeText(BackgroundService.this, "Fall Detected!", Toast.LENGTH_LONG).show();
                            enableFallDetection();
                            sendNotification();

                            v.vibrate(1000);

                        }
                        prev_response = 1;

                    }
                    else{
                        prev_response = 0;
                    }

                } catch (NullPointerException e) {
                    Log.i("SensorActivity", "CAUGHT++++++++++++++++++++EXCEPTION++++++++++============" + e);

                }

            }


            @Override
            public void onFailure(Call<Fall> call, Throwable t) {
                Log.i("SensorActivity", "_____________________________________________FAILURE_____________" + String.valueOf(t));
            }
        });
    }

    public void createPost_both(List<Data> list){

        Call<Fall> call = communicator.getClient_both().GetPostValue_both(list);
        call.enqueue(new Callback<Fall>() {
            @Override
            public void onResponse(Call<Fall> call, Response<Fall> response) {
                Fall hasFall = response.body();
                try {
                    Log.i("SERVICE", "onResponse: ++++++++++++++++++++++++++++ "+hasFall.getFall());
                    //fall = hasFall;
                    if (hasFall.getFall()==1) {

                        if (prev_response == 0) {
                            Toast.makeText(BackgroundService.this, "Fall Detected!", Toast.LENGTH_LONG).show();
                            enableFallDetection();
                            sendNotification();

                            v.vibrate(1000);

                        }
                        prev_response = 1;

                    }
                    else{
                        prev_response = 0;
                    }

                } catch (NullPointerException e) {
                    Log.i("SensorActivity", "CAUGHT++++++++++++++++++++EXCEPTION++++++++++============" + e);

                }

            }


            @Override
            public void onFailure(Call<Fall> call, Throwable t) {
                Log.i("SensorActivity", "_____________________________________________FAILURE_____________" + String.valueOf(t));
            }
        });
    }

    private void countFallTimer(){
        if (fall_detected) {
            //Log.d("Sensor Activity","User fell down and time of falling is _________________________________________________________:           "+fall_detection_time);
            ///check if 20 seconds have passed since user hasn't responded to the fall dialogue
            //user might be injured
            if (( (System.currentTimeMillis() / 1000.0) - fall_detection_time) >= timeLimit) {
                locationAndSMS.getLocationAndSendSMS(false,post_fall_movement_detected,fall_detection_time,post_fall_movement_time);
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
