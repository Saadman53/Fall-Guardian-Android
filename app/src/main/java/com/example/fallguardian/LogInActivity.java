package com.example.fallguardian;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

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



public class LogInActivity extends AppCompatActivity  {

    EditText loginEmail,loginPassword;
    TextView signupTextView, forgotPasswordTextView;
    Button loginButton;

    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseUser user;

    String email ;
    String password ;


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
        signupTextView = (TextView) findViewById(R.id.signupTextView);
        forgotPasswordTextView = (TextView) findViewById(R.id.forgotPassTextView);

        progressBar = (ProgressBar) findViewById(R.id.progressBarId);

        mAuth = FirebaseAuth.getInstance();

        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///send to signup activity
                Intent intent = new Intent(LogInActivity.this,SignUpActivity.class);
                startActivity(intent);
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
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                loginSuccessful("no");
            }
            else{
                ///user exists but email is not verified
                Log.d("LogInActivity","User exists but email is not verified");
            }
        }
    }




    private void userLogin(){
        email = loginEmail.getText().toString().trim();
        password = loginPassword.getText().toString().trim();

        if(email.isEmpty()){
            loginEmail.setError("Enter an email Address");
            loginEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loginEmail.setError("Enter a valid email Address");
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
            //Toast.makeText(getApplicationContext(),"SUCCESSFULLY REGISTERED",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isSuccessful()){

                       FirebaseUser loggedinUser = FirebaseAuth.getInstance().getCurrentUser();
                       if(loggedinUser.isEmailVerified()){
                           loginSuccessful("yes");
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
        finish();


        Intent intent = new Intent(LogInActivity.this,SensorActivity.class);
        //intent.putExtra("flag",text);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

