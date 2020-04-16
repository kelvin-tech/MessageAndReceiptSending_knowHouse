package com.knowhouse.thereceiptbook;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



import com.knowhouse.thereceiptbook.Utils.PlayGifView;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
public class LoginActivity extends AppCompatActivity {


    private EditText mPhoneNumber, mCode ;
    private CountryCodePicker  mCountryCode;
    private  ProgressBar mProgressBar;
    private Button mSend;
    private ImageView mPreload;
    private  com.knowhouse.thereceiptbook.Utils.PlayGifView mPlaygifview;
    
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String mVerificationId;
    Boolean verifyClick = false ;
    boolean opt = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
       /*
        CountryCodePicker ccp;
        AppCompatEditText edtPhoneNumber;

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        edtPhoneNumber = findViewById(R.id.phone_number_edt);

        ccp.registerPhoneNumberTextView(edtPhoneNumber);
*/

        userIsLoggedIn();

        mPhoneNumber = findViewById(R.id.phoneNumber);
        mCode = findViewById(R.id.code);
        mCountryCode = findViewById(R.id.ccp);
        //mProgressBar = findViewById(R.id.progressBar);
        mSend = findViewById(R.id.send);
        mPlaygifview = findViewById(R.id.viewGif);


        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mPhoneNumber.getText().toString().isEmpty() && mPhoneNumber.getText().toString().length() == 10 ||!mPhoneNumber.getText().toString().isEmpty() && mPhoneNumber.getText().toString().length() == 9 )  {

                    String phoneNum = "+"+ mCountryCode.getSelectedCountryCode()+mPhoneNumber.getText().toString();
                    Log.d("TAG","onCLick: Phone number is "+phoneNum);

                    if (!verifyClick){

                     mPlaygifview.setVisibility(View.VISIBLE);
                     if(!opt) {

                         mSend.setText("Sending OTP..");
                     }else {
                         mSend.setText("Verifying Code");
                     }
                    PlayGifView pGif = mPlaygifview;
                    pGif.setImageResource(R.drawable.preload);
                    }


                    //mPreload.animate(R.drawable.preload);
                    //PlayGifView pGif = (PlayGifView) findViewById(R.id.viewGif);
                    //pGif.setImageResource(R.drawable.preload);

                    if(verifyClick && mCode.getText().toString().isEmpty() )

                    {
                        mCode.setError("Field is Empty");
                        mSend.setText("Verify Code");
                    }
                    else if(verifyClick && mCode.getText().toString().length() != 6){
                        mCode.setError("Enter Valid Code");

                        opt = true ;
                    }
                    else {
                        if (mVerificationId != null)
                            verifyPhoneNumberWithCode();

                        else
                            startPhoneNumberVerification(phoneNum);
                    }


                   // else if(verifyClick && mCode.getText().toString().isEmpty())


                    //{
                       // mCode.setError("Field is Empty");

                    //}
                    //else {
                      //  mCode.setError("Verification code is not valid");

                    //}
                }
                else{
                    mPhoneNumber.setError("Phone number is not valid");

                }
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }



            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(LoginActivity.this,"can not create an account"+e.getMessage(),Toast.LENGTH_LONG).show();

                mSend.setText("RESEND CODE");


                verifyClick = false;

            }




            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);

                mVerificationId = verificationId;

                        //setImageResource(R.drawable.preload);
                mSend.setVisibility(View.VISIBLE);
                mPlaygifview.setVisibility(View.INVISIBLE);
                mSend.setText("Verify Code");
                verifyClick = true ;


            }
        };

    }



    private void verifyPhoneNumberWithCode(){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mCode.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if(user != null){
                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()){
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name", user.getPhoneNumber());
                                    mUserDB.updateChildren(userMap);
                                }
                                userIsLoggedIn();
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });
                    }

                }

            }
        });
    }



    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
            return;
        }
    }





    private void startPhoneNumberVerification(String phoneNum) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNum,
                60L,
                TimeUnit.SECONDS,
                this,
                mCallbacks);


    }


}


