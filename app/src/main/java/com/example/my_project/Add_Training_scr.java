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

public class Add_Training_scr extends AppCompatActivity implements View.OnClickListener {

    TextView time, speed, distance;
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
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

            Run r = new Run(time.getText().toString(), speed.getText().toString(), distance.getText().toString(),uid, "" );

            runRef = firebaseDatabase.getReference("Runs").push();
            r.setKey(runRef.getKey());
            runRef.setValue(r);

        }
    }


}