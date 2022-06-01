package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class Add_Training_scr extends AppCompatActivity implements View.OnClickListener {

    TextView time, speed, distance;
    Button btn_save;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference runRef;
    Run run;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training_scr);
        getSupportActionBar().hide();

        time=findViewById(R.id.tv_time_add_training);
        distance=findViewById(R.id.tv_distance_add_training);
        speed=findViewById(R.id.tv_speed_add_training);
        btn_save = findViewById(R.id.btn_save_add_training);

        btn_save.setOnClickListener(this);

        runRef=Utils.getUserRuns();

        run=(Run) getIntent().getSerializableExtra("run");

        long seconds = run.getTime();

        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);

        String runTime = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

        time.setText(runTime);
        distance.setText(String.valueOf(run.getDistance()));
        speed.setText(String.valueOf(run.getSpeed()));
    }


    @Override
    public void onClick(View view) {
        if(view == btn_save){

            DatabaseReference ref = Utils.getUserRuns();
            String key = ref.push().getKey();

            run.setKey(key);

            ref.child(key).setValue(run).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(Add_Training_scr.this, "Run was successfully saved", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(Add_Training_scr.this, "Saving your run failed", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }

}