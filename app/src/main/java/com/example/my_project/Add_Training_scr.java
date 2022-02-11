package com.example.my_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Add_Training_scr extends AppCompatActivity implements View.OnClickListener {

    EditText time, speed, distance;
    Button btn_save;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference runRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training_scr);
        getSupportActionBar().hide();

        time = findViewById(R.id.tv_time_add_training);
        distance = findViewById(R.id.tv_distance_add_training);
        speed=findViewById(R.id.tv_speed_add_training);
        btn_save = findViewById(R.id.btn_save_add_training);

        btn_save.setOnClickListener(this);


        firebaseDatabase = FirebaseDatabase.getInstance();
    }



    @Override
    public void onClick(View view) {
        if(view == btn_save){
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            LocalDate today=LocalDate.now();

            Run r = new Run(Integer.parseInt(time.getText().toString()), Integer.parseInt(speed.getText().toString()), Integer.parseInt(distance.getText().toString()),uid, "", today );

           /* Run r1=new Run(1, "speed1", "dist1", uid, "", 2022, 11,1);
            Run r2=new Run(2, "speed2", "dist2", uid, "", 2022, 11,23);
            Run r3=new Run(3, "speed3", "dist3", uid, "", 2022, 11,30);
            Run r4=new Run(4, "speed4", "dist4", uid, "", 2022, 11,4);
            Run r5=new Run(5, "speed5", "dist5", uid, "", 2022, 11,8);

            ArrayList<Run> arrayList=new ArrayList<>();
            arrayList.add(r1);
            arrayList.add(r2);
            arrayList.add(r3);
            arrayList.add(r4);
            arrayList.add(r5);*/


            runRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Runs").push();
            r.setKey(runRef.getKey());
            runRef.setValue(r);

        }
    }


}