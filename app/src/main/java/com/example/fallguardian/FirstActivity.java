package com.example.fallguardian;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirstActivity extends AppCompatActivity {

    FirebaseUser user;

    Button elderlyBTN,monitorBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        user = FirebaseAuth.getInstance().getCurrentUser();

        doesUserExist();

        elderlyBTN = findViewById(R.id.LoginElderly);
        monitorBTN = findViewById(R.id.LoginMonitor);

        elderlyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent =  new Intent(FirstActivity.this, LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        monitorBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent =  new Intent(FirstActivity.this, LogInActivity_Monitor.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });




    }

    private void doesUserExist() {
        if(user!=null){
            //user with email exists
            if(!user.getPhoneNumber().isEmpty()){
                Log.i("LOGIN","USER SEEMS TO EXIST IN THIS SYSTEM USING PHONE*******************");
                ///Monitor Activity
                finish();
                Intent intent =  new Intent(FirstActivity.this, MonitorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else if(!user.getEmail().isEmpty()){
                Log.i("LOGIN","USER SEEMS TO EXIST IN THIS SYSTEM ****USING EMAIL********************");
                finish();
                Intent intent =  new Intent(FirstActivity.this, LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }


        }
        else{
            Log.d("LOGIN","USER SEEMS TO BE NULL IN THIS SYSTEM WHICH IS NOT WEIRD XD*********************");
        }
    }


}