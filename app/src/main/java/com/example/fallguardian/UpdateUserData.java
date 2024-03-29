package com.example.fallguardian;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateUserData extends AppCompatActivity {

    EditText userFirstName,userLastName,monitorPhoneNumber;
    Button updateButton;
    ProgressBar progressBar;

    private DatabaseReference databaseReference;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_data);

        userFirstName = findViewById(R.id.updateUserFirstName);
        userLastName = findViewById(R.id.updateUserLastName);

        monitorPhoneNumber = findViewById(R.id.updateMonitorMobile);
        updateButton = findViewById(R.id.updateButton);
        progressBar = findViewById(R.id.updateProgressBar);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        user = FirebaseAuth.getInstance().getCurrentUser();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });

    }

    private void updateData() {
        progressBar.setVisibility(View.VISIBLE);

        String u1 = userFirstName.getText().toString().trim();
        String u2 = userLastName.getText().toString().trim();

        String mPhone = monitorPhoneNumber.getText().toString().trim();

        String userID = user.getUid();
        if(!u1.equals("")){
            databaseReference.child(userID).child("firstName").setValue(u1);
        }
        if(!u2.equals("")){
            databaseReference.child(userID).child("lastName").setValue(u2);
        }


        if(!mPhone.equals("")){
            databaseReference.child(userID).child("monitor_phone_number").setValue(mPhone);
        }

        progressBar.setVisibility(View.GONE);

        Toast.makeText(this,"User Data Successfully Updated",Toast.LENGTH_SHORT).show();
        finish();
        Intent intent = new Intent(UpdateUserData.this,SensorActivity.class);
        intent.putExtra("information","updated");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

}