package com.example.fallguardian;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static android.content.ContentValues.TAG;

public class MonitorActivity extends AppCompatActivity {

    ListView listView;

    DatabaseReference monitorDatabaseReference;
    FirebaseUser user;
    private Timer myTimer;

    String phone;

    String text;

    boolean stopRunning;
    boolean first = true;

    ArrayList<String> list;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        if(!isMyServiceRunning(MonitorService.class)) {
            startService();
        }
        listView = findViewById(R.id.listID);

        user = FirebaseAuth.getInstance().getCurrentUser();
        phone = user.getPhoneNumber();
        monitorDatabaseReference = FirebaseDatabase.getInstance().getReference("monitor");
        stopRunning = false;
        first = true;

        Log.i(TAG, "Called ON CREATE()");


        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getStringExtra("data"); // ... and retrieve that data here.
                if(data.equals("yes")){
                    Log.i(TAG, "Data received from service   ============================= "+data);
                    displayDataOnce();
                }
            }
        }, new IntentFilter("newDataReceived"));




    }






    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "Called ON START()");

        displayDataOnce();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("HUIA", "Called ON STOP(---------------------------------)");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("HUIA", "Called ON DESTROY(---------------------------------)");
        //stopRunning = true;
    }

    private void displayDataOnce() {

        list = new ArrayList<String>();


        monitorDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    try{
                        Map<String, String> map = (Map<String, String>) snapshot.child(phone).getValue();
                        TreeMap<String, String> treemap = new TreeMap<String, String>(map);
                        Map<String, String> newdata = new HashMap<>();
                        int iter = 0;
                        for (String key : treemap.descendingKeySet()){
                            if(iter==10) break;
                            newdata.put(key,treemap.get(key));
                            iter++;
                        }

                        if(map.size()>10){
                            monitorDatabaseReference.child(phone).removeValue();
                            Map<String,Object> updatedata = new HashMap<>(newdata);
                            monitorDatabaseReference.child(phone).updateChildren(updatedata);

                            Log.i("UPDATE DATABASE", "onDataChange: -----------------------------> Database Updated");



                        }
                        display(newdata);


                    }
                    catch (Exception e){
                        Log.d("ERROR", "ERRRRRRRRRRRRRRRRRRRRRROR "+e.toString());
                    }

                }
                else{

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void display(Map<String, String> newdata) {
        for (Map.Entry<String, String> entry : newdata.entrySet()){
            if(entry.getKey().charAt(0) != '+'){
                text = "Date: ";
                text+=entry.getKey();
                text+="\n";
                text+="Message: ";
                text+=entry.getValue();
                Log.d("SOMETHING", "onDataChange: --------------> key: "+entry.getKey()+" value: "+entry.getValue());
                list.add(text);
            }
        }
        adapter = new ArrayAdapter<String>(MonitorActivity.this,
                R.layout.list_views,
                R.id.ListTextViewID,
                list);

        Log.d("ON SERVICE", list.toString());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = list.get(position);
                String[] tokens = text.split(" ");

                Boolean locationUnavailable = true;
                for (String token : tokens)
                {
                    if(token.startsWith("https")){
                        locationUnavailable = false;
                        Toast.makeText(MonitorActivity.this,"Displaying Location in google maps",Toast.LENGTH_SHORT).show();
                        openWebPage(token);
                    }
                }
                if(locationUnavailable){
                    Toast.makeText(MonitorActivity.this,"Location Unavailable",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void startService(){
        Toast.makeText(this,"Service Started",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,MonitorService.class);
        startService(intent);
    }
    private void stopService(){
        Toast.makeText(this,"Service Stopped",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,MonitorService.class);
        stopService(intent);
    }

    ///handling the options menu here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.monitor_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOutMenuId) {
            stopService();
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent intent = new Intent(MonitorActivity.this, FirstActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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