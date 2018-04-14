package com.example.owner.heartdetect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button logOut;
    private DatabaseReference usersRef;
    private FirebaseAuth firebaseAuth;
    private TextView heartRate;
    private ValueEventListener heart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logOut = (Button) findViewById(R.id.logout);
        heartRate = (TextView) findViewById(R.id.heartrate);

        logOut.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = firebaseAuth.getCurrentUser();


        heart = usersRef.child(user.getUid()).child("Heartrate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();

                heartRate.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onClick(View view) {
        if(view == logOut){
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MainActivity.this, UserSignIn.class);
            startActivity(intent);
        }

    }
}
