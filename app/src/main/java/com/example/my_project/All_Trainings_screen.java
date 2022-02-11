package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class All_Trainings_screen extends AppCompatActivity {
    ArrayList<Run> RunList;
    ListView lv;
    RunAdapter adapter;
    DatabaseReference run_ref;

    Button add;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_trainings_screen);
        getSupportActionBar().hide();

        add=findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(All_Trainings_screen.this, Add_Training_scr.class);
                startActivity(intent);
            }
        });

        RunList = new ArrayList<Run>();
        lv = findViewById(R.id.lv_trainings_all_trainings_screen);
        run_ref = FirebaseUtils.getCurrentUserRuns();

        retrieveData();
    }

        private void retrieveData() {
            run_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    RunList = new ArrayList<>();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Run r = data.getValue(Run.class);
                        RunList.add(r);
                    }
                    adapter = new RunAdapter(RunList,All_Trainings_screen.this, 0, 0);
                    lv.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
