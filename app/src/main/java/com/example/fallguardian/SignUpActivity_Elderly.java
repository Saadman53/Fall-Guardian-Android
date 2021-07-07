package com.example.fallguardian;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;


import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUpActivity_Elderly extends AppCompatActivity  {


    private static final Object TAG = "SignUpActivity_Elderly";
    EditText userFirstName,userLastName,userEmail,userPassword,confirmuserPassword,monitorNumber, Birthday;
    Button signupButton;

    ProgressBar progressBar;

    private DatabaseReference databaseReference, monitorDatabaseReference;
    private static final String TABLE_NAME = "scale_values";



    private String user_first_name;
    private String user_last_name;
    private String user_email;
    private String user_password;
    private String confirmed_user_password;

    private String date_of_birth;

    private String monitor_phone_number;

    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("SIGN UP");

        setContentView(R.layout.activity_sign_up);

        userFirstName = (EditText) findViewById(R.id.SignupFirstName);
        userLastName = (EditText) findViewById(R.id.SignupLastName) ;

        userEmail = (EditText) findViewById(R.id.SignupEmail);
        userPassword = (EditText) findViewById(R.id.SignupPassword);
        confirmuserPassword = (EditText) findViewById(R.id.SignupConfirmPassword);

        Birthday = findViewById(R.id.Birthday);


        monitorNumber = (EditText) findViewById(R.id.SignupMonitorMobile);

        signupButton = (Button) findViewById(R.id.SignupButton);

        progressBar = findViewById(R.id.progressBarId);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        monitorDatabaseReference = FirebaseDatabase.getInstance().getReference("monitor");

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };


        Birthday.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SignUpActivity_Elderly.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });





        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Birthday.setText(sdf.format(myCalendar.getTime()));
    }




    private void registerUser(){
        user_first_name = userFirstName.getText().toString().trim();
        user_last_name = userLastName.getText().toString().trim();
        user_email = userEmail.getText().toString().trim();
        user_password = userPassword.getText().toString().trim();
        confirmed_user_password = confirmuserPassword.getText().toString().trim();

        date_of_birth = Birthday.getText().toString().trim();

        monitor_phone_number = monitorNumber.getText().toString().trim();



        if(user_email.isEmpty()){
            userEmail.setError("Enter an email Address");
            userEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(user_email).matches()){
            userEmail.setError("Enter a valid email Address");
            userEmail.requestFocus();
            return;
        }

        if(user_first_name.isEmpty()){
            userFirstName.setError("Please fill this field");
            userFirstName.requestFocus();
            return;
        }
        if(user_last_name.isEmpty()){
            userLastName.setError("Please fill this field");
            userLastName.requestFocus();
            return;
        }


        if(date_of_birth.isEmpty()){
            Birthday.setError("Please enter your date of birth");
            Birthday.requestFocus();
            return;
        }


        if(monitor_phone_number.isEmpty()){
            monitorNumber.setError("Please Enter a valid Number");
            monitorNumber.requestFocus();
            return;
        }

        if(user_password.length()<6){
            userPassword.setError("Password must atleast be 6 characters long");
            userPassword.requestFocus();
            return;
        }

        if(!user_password.equals(confirmed_user_password)){
            confirmuserPassword.setError("Passwords do not match");
            confirmuserPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        if(user_password.length()>=6 && user_password.equals(confirmed_user_password)){
            //Toast.makeText(getApplicationContext(),"SUCCESSFULLY REGISTERED",Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(user_email, user_password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Sign up is successful, update database

                                 Elderly elderly = new Elderly(user_first_name, user_last_name, user_email, monitor_phone_number,date_of_birth,true);
                                 FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                 String userID = user.getUid();
                                 databaseReference.child(userID).setValue(elderly);

                                 ///initializing monitor database
                                 Map<String, Object> map = new HashMap<>();

                                 for (int i=0;i<10;i++){
                                     map.put(String.valueOf(i), user_first_name+" "+user_last_name+" registered you as their monitor.");
                                 }

                                 checkAndSetMonitorDatabase(monitor_phone_number);
                                Toast.makeText(getApplicationContext(),"SUCCESSFULLY REGISTERED",Toast.LENGTH_SHORT).show();



//

                                ///send email verification
                                 sendVerificationEmail();


                                 finish();
                                 Intent intent = new Intent(SignUpActivity_Elderly.this, LogInActivity.class);
                                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                 startActivity(intent);

                            } else {
                                if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                    Toast.makeText(getApplicationContext(),"USER IS ALREADY REGISTER",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"ERROR IN REGISTRATION",Toast.LENGTH_SHORT).show();
                                }

                            }

                            // ...
                        }
                    });
            return;
        }

    }

    private void checkAndSetMonitorDatabase(String monitor_phone_number) {
        monitorDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Map<String, String> map = (Map<String, String>) snapshot.child(monitor_phone_number).getValue();

                    if(map.size()==0){
                        String x = "+";
                        Map<String,Object> updatedMap = new HashMap<String, Object>();
                        for(int i=0;i<10;i++){
                            updatedMap.put(x,"Null");
                            x+="+";
                        }
                        monitorDatabaseReference.child(monitor_phone_number).updateChildren(updatedMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity_Elderly.this,"Verification email send",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(SignUpActivity_Elderly.this,"Error! Couldn't send verification email",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    public void insertData(){


    }
}

