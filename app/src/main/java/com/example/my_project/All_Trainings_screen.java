package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class All_Trainings_screen extends AppCompatActivity {
    ArrayList<Run> RunList;
    ListView lv;
    RunAdapter adapter;
    DatabaseReference run_ref;
    TextInputEditText spinner_period_all_trainings_screen;

    LocalDate selectedDate;

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

        LocalDate today = LocalDate.now();
        selectedDate = today;

        spinner_period_all_trainings_screen = findViewById(R.id.spinner_period_all_trainings_screen);
        spinner_period_all_trainings_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(All_Trainings_screen.this, AlertDialog.THEME_HOLO_LIGHT);
                datePickerDialog.getDatePicker().setBackgroundColor(Color.TRANSPARENT);

                int selected_day = selectedDate.getDayOfMonth();
                int selected_month = selectedDate.getMonthValue();
                int selected_year = selectedDate.getYear();

                datePickerDialog.updateDate(selected_year, selected_month-1, selected_day);
                datePickerDialog.getDatePicker().findViewById(getResources().getIdentifier("day","id","android")).setVisibility(View.GONE);
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view1, int year, int month, int day) {
                        month = month + 1;

                        selectedDate = LocalDate.of(year, month, day);
                        String text = Utils.DateToText(selectedDate);
                        spinner_period_all_trainings_screen.setText(text);

                        retrieveData(selected_year,selected_month);
                    }
                });
                datePickerDialog.show();
            }
        });

        String today_text = Utils.DateToText(today);
        spinner_period_all_trainings_screen.setText(today_text);
        retrieveData(today.getYear(),today.getMonthValue());

    }

    private void retrieveData(int year, int month) {
        Query query1=run_ref.orderByChild("year").equalTo(year);
        DatabaseReference ref1=query1.getRef();

        Query query2=ref1.orderByChild("month").equalTo(month);
        DatabaseReference ref2=query2.getRef();

        Query query3=ref2.orderByChild("finishTime");
        query3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RunList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Run r = data.getValue(Run.class);
                    RunList.add(r);
                }
                /*int length=RunList.size()-1;
                for (int i = length; i >=0 ; i--) {
                    Run r= RunList.get(length-i);
                    RunList.set(i, r);
                }*/
                Collections.reverse(RunList);

                adapter = new RunAdapter(RunList,All_Trainings_screen.this, 0, 0);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    }
