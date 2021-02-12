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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity  {


    private static final Object TAG = "SignUpActivity";
    EditText userFirstName,userLastName,userEmail,userPassword,confirmuserPassword,userNumber,monitorFirstName,monitorLastName,monitorNumber, Birthday;
    Button signupButton;

    ProgressBar progressBar;

    private DatabaseReference databaseReference;



    private String user_first_name;
    private String user_last_name;
    private String user_email;
    private String user_password;
    private String confirmed_user_password;
    private String user_phone_number;
    private String date_of_birth;
    private String monitor_first_name;
    private String monitor_last_name;
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
        userNumber = (EditText) findViewById(R.id.SignupMobile);
        Birthday = findViewById(R.id.Birthday);

        monitorFirstName = (EditText) findViewById(R.id.SignupMonitorFirstName);
        monitorLastName = (EditText) findViewById(R.id.SignupMonitorLastName) ;
        monitorNumber = (EditText) findViewById(R.id.SignupMonitorMobile);

        signupButton = (Button) findViewById(R.id.SignupButton);

        progressBar = findViewById(R.id.progressBarId);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

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
                new DatePickerDialog(SignUpActivity.this, date, myCalendar
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
        user_phone_number = userNumber.getText().toString().trim();
        date_of_birth = Birthday.getText().toString().trim();
        monitor_first_name = monitorFirstName.getText().toString().trim();
        monitor_last_name = monitorLastName.getText().toString().trim();
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
        if(user_phone_number.isEmpty()){
            userNumber.setError("Please Enter a valid Number");
            userNumber.requestFocus();
            return;
        }

        if(date_of_birth.isEmpty()){
            Birthday.setError("Please enter your date of birth");
            Birthday.requestFocus();
            return;
        }

        if(monitor_first_name.isEmpty()){
            monitorFirstName.setError("Please Fill this field");
            monitorFirstName.requestFocus();
            return;
        }
        if(monitor_last_name.isEmpty()){
            monitorLastName.setError("Please Fill this field");
            monitorLastName.requestFocus();
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

                                 Elderly elderly = new Elderly(user_first_name, user_last_name, user_email, user_phone_number,monitor_first_name,monitor_last_name, monitor_phone_number,date_of_birth);
                                 FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                 String userID = user.getUid();
                                 databaseReference.child(userID).setValue(elderly);
                                 Toast.makeText(getApplicationContext(),"SUCCESSFULLY REGISTERED",Toast.LENGTH_SHORT).show();

                                ///send email verification
                                 sendVerificationEmail();


                                 finish();
                                 Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
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






    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,"Verification email send",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(SignUpActivity.this,"Error! Couldn't send verification email",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}

