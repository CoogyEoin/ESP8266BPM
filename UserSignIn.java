package com.example.owner.heartdetect;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;


public class UserSignIn extends AppCompatActivity implements View.OnClickListener{

    private Button signInButton;
    private EditText email;
    private EditText password;

    private Button registerButton;
    private EditText regEmail;
    private EditText regPassword;
    private String regToken;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference usersRef;

    //final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_in);

        signInButton = (Button) findViewById(R.id.buttonSignIn);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        registerButton = (Button) findViewById(R.id.buttonRegister);
        regEmail = (EditText) findViewById(R.id.regEmail);
        regPassword = (EditText) findViewById(R.id.regPassword);
        regToken =  FirebaseInstanceId.getInstance().getToken();

        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    Intent intent = new Intent(UserSignIn.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        registerButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);

    }



    @Override
    public void onClick(View view) {
        if(view == signInButton){
            signInUser();
        }
        if(view == registerButton){
            registerUser();
        }
    }

    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    private void registerUser() {
        final String email = regEmail.getText().toString().trim();
        final String password = regPassword.getText().toString().trim();
        final String token = regToken;
        final String heartrate = "70.0";

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter an Email", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter a Password", Toast.LENGTH_SHORT).show();
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    UserData userData = new UserData(email, password);
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    usersRef.child(user.getUid()).setValue(userData);

                    Map<String,Object> taskMap = new HashMap<>();
                    taskMap.put("Token", token);
                    taskMap.put("Heartrate", heartrate);
                    usersRef.child(user.getUid()).updateChildren(taskMap);


                   Intent intent = new Intent(UserSignIn.this, MainActivity.class);
                   startActivity(intent);
                }
            }
        });




    }

    private void signInUser() {
        String signInEmail = email.getText().toString().trim();
        String signInPassword = password.getText().toString().trim();

        if(TextUtils.isEmpty(signInEmail)){
            Toast.makeText(this, "Please Enter an Email", Toast.LENGTH_SHORT).show();

            return;
        }

        if(TextUtils.isEmpty(signInPassword)){
            Toast.makeText(this, "Please Enter a Password", Toast.LENGTH_SHORT).show();

            return;
        }

        firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(!task.isSuccessful()){
                    Toast.makeText(UserSignIn.this, "Error Signing In", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




}
