package com.example.fallguardian;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LogInActivity extends AppCompatActivity  {

    EditText loginEmail,loginPassword;
    TextView signupTextViewElderly, forgotPasswordTextView, aboutTextView;
    Button loginButton;

    ProgressBar progressBar;

    FirebaseUser user;

    String email ;
    String password ;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("LOG IN");
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();


        //if user is already signed in
        doesUserExists();

        loginEmail = (EditText) findViewById(R.id.LoginEmail);
        loginPassword = (EditText) findViewById(R.id.LoginPassword);
        loginButton = (Button) findViewById(R.id.LoginButton);
        signupTextViewElderly = (TextView) findViewById(R.id.signupTextViewElderly);
        forgotPasswordTextView = (TextView) findViewById(R.id.forgotPassTextView);
        aboutTextView = (TextView) findViewById(R.id.aboutID);

        progressBar = (ProgressBar) findViewById(R.id.progressBarId);



        signupTextViewElderly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///send to signup activity
                Intent intent = new Intent(LogInActivity.this, SignUpActivity_Elderly.class);
                startActivity(intent);
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference("users");


       aboutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder aboutDialog = new AlertDialog.Builder(v.getContext());
                aboutDialog.setTitle("About Fall Guardian");
                aboutDialog.setMessage(Html.fromHtml(getString(R.string.about_text_html)));
                aboutDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                aboutDialog.create().show();
            }
        });
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter Your Email.");
                passwordResetDialog.setView(resetMail);
                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ///extract the email and sent reset link
                        String mail = resetMail.getText().toString();
                        if(Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                            FirebaseAuth.getInstance().sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(LogInActivity.this,"Reset Link Sent To Your Email",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LogInActivity.this,"Error! Reset Link is Not Send "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            resetMail.setError("Enter a valid email Address");
                            resetMail.requestFocus();
                        }

                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordResetDialog.create().show();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });




    }


    private void doesUserExists(){
        if(user!=null){
            if(user.isEmailVerified()){
                Log.d("LOGIN","USER SEEMS TO EXIST IN THIS SYSTEM WEIRD");
                loginSuccessful("exists");
            }
            else{
                ///user exists but email is not verified
                Log.d("LogInActivity","User exists but email is not verified");
            }
        }
        else{
            Log.d("LOGIN","USER SEEMS TO BE NULL IN THIS SYSTEM WHICH IS NOT WEIRD XD");
        }
    }




    private void userLogin(){
        email = loginEmail.getText().toString().trim();
        password = loginPassword.getText().toString().trim();

        if(email.isEmpty()){
            loginEmail.setError("Enter Credentials");
            loginEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loginEmail.setError("Enter a valid email or ");
            loginEmail.requestFocus();
            return;
        }

        if(password.length()<6){
            loginPassword.setError("Password must atleast be 6 characters long");
            loginPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        if(password.length()>=6){
            progressBar.setVisibility(View.GONE);

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isSuccessful()){

                       FirebaseUser loggedinUser = FirebaseAuth.getInstance().getCurrentUser();
                       if(loggedinUser.isEmailVerified()){
                           user = loggedinUser;

                           loginSuccessful("no");
                           return;
                       }
                       else{
                           Toast.makeText(LogInActivity.this,"Please verify your email to login",Toast.LENGTH_LONG).show();
                       }

                   }
                   else{
                       Toast.makeText(getApplicationContext(),"LOG IN UNSUCCESSFUL",Toast.LENGTH_SHORT).show();
                   }
               }
           });
            return;
        }
    }
    private void loginSuccessful(String text){

        if(text.equals("no")){
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){
                        Elderly elderly = snapshot.child(user.getUid()).getValue(Elderly.class);
                        if(elderly.getFirstLogin()){
                            databaseReference.child(user.getUid()).child("firstLogin").setValue(false);
                            first_time("yes");
                        }
                        else{
                            first_time("no");
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
        else{
            first_time("no");
        }
    }

    void first_time(String x){
        if(x.equals("no")){
            finish();

            Intent intent = new Intent(LogInActivity.this,SensorActivity.class);
            //intent.putExtra("flag",text);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else{
            finish();

            Intent intent = new Intent(LogInActivity.this,Agreement.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}

