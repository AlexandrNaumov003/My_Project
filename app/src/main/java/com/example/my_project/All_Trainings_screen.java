package com.example.my_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class All_Trainings_screen extends AppCompatActivity {
    ArrayList<Run> RunList;
    ListView lv;
    RunAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_trainings_screen);

        RunList=new ArrayList<Run>();
        lv=findViewById(R.id.lv_trainings_all_trainings_screen);

        adapter=new RunAdapter(RunList,getApplicationContext());
        lv.setAdapter(adapter);

    }
}