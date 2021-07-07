package com.example.fallguardian;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.fallguardian.databinding.ActivitySignUpMonitorBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


public class LogInActivity_Monitor extends AppCompatActivity {

    ///binding
    private ActivitySignUpMonitorBinding binding;


    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private FirebaseAuth firebaseAuth;

    private static final String TAG = "MAIN_TAG";

    private ProgressDialog pd;

    private String mVerificationCode;

    DatabaseReference monitorDatabaseRefenence;

    Boolean monitor_exists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpMonitorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.phoneL1.setVisibility(View.VISIBLE);
        binding.codeL1.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);

        monitorDatabaseRefenence = FirebaseDatabase.getInstance().getReference("monitor");




        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                registerMonitor(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                pd.dismiss();
                Toast.makeText(LogInActivity_Monitor.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                Log.d(TAG, "onVerificationFailed: "+e.getMessage());

            }

            @Override
            public void onCodeSent(@NonNull String verificationCode, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationCode, forceResendingToken);
                Log.d(TAG, "onCodeSent: "+verificationCode);
                mVerificationCode = verificationCode;
                forceResendingToken =token;
                pd.dismiss();

                ///hide phone layout
                binding.phoneL1.setVisibility(View.GONE);
                binding.codeL1.setVisibility(View.VISIBLE);

                Toast.makeText(LogInActivity_Monitor.this, "Verification code sent...", Toast.LENGTH_SHORT).show();

                binding.VerificationCodeMobileTextID.setText("Enter the verification code sent to\n"+binding.SignupPhone.getText().toString());
            }
        };

        binding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = binding.SignupPhone.getText().toString();

                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(LogInActivity_Monitor.this,"Please enter phone number",Toast.LENGTH_SHORT).show();
                }
                else{
                    startPhoneVerification(phone);
                }
            }
        });

        binding.ResendCodeTextViewID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = binding.SignupPhone.getText().toString();

                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(LogInActivity_Monitor.this,"Please enter phone number",Toast.LENGTH_SHORT).show();
                }
                else{
                    resendVerificationCode(phone, forceResendingToken);
                }
            }
        });

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = binding.verifyCodeID.getText().toString();

                if(TextUtils.isEmpty(code)){
                    Toast.makeText(LogInActivity_Monitor.this,"Please enter verification code",Toast.LENGTH_SHORT).show();
                }
                else{
                    verifyPhoneWithCode(mVerificationCode,code);
                }
            }
        });


    }

    private void startPhoneVerification(String phone) {
        pd.setMessage("Verifying Phone Number");
        pd.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L,TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        monitorDatabaseRefenence.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

               if(snapshot.hasChild(phone)){
                   monitor_exists = true;
               }
               else{
                   monitor_exists = false;

               }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }




        });
    }

    private void resendVerificationCode(String phone , PhoneAuthProvider.ForceResendingToken token) {
        pd.setMessage("Resending Code");
        pd.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L,TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }

    private void verifyPhoneWithCode(String verificationCode, String code) {
        pd.setMessage("Verifying Code");
        pd.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode,code);

        if(monitor_exists) registerMonitor(credential);
        else{
            Toast.makeText(this,"No elderly account registered for this number",Toast.LENGTH_LONG).show();
            pd.dismiss();
        }
    }

    private void registerMonitor(PhoneAuthCredential credential) {
        pd.setMessage("Registering Monitor");

        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                pd.dismiss();
                Toast.makeText(LogInActivity_Monitor.this,"Monitor Successfully Logged in",Toast.LENGTH_LONG).show();

                finish();
                Intent intent = new Intent(LogInActivity_Monitor.this, MonitorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LogInActivity_Monitor.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                Log.d(TAG, "on Failure Listener: "+e.getMessage());
            }
        });
    }


}