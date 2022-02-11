package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.time.LocalDate;

public class Statistic_screen extends AppCompatActivity {
    GraphView graphView;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_screen);
        graphView = findViewById(R.id.graph_km_statistic);



        LocalDate today = LocalDate.now();
        int current_month_length = today.lengthOfMonth();
        int current_month_value = today.getMonthValue();

        DataPoint[] dataPoints = new DataPoint[current_month_length+1];
        int[] points = new int[current_month_length+1];

        points[0] = 0;

        DatabaseReference run_data = FirebaseUtils.getCurrentUserRuns();

        Query query = run_data.orderByChild("month").equalTo(current_month_value);
        Log.d("naumov", "Month is " + current_month_value);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Log.d("naumov", "------------------------------------------------------");
                    int day = data.child("day").getValue(int.class);
                    Log.d("naumov", "Day is " + day);
                    int distance = data.child("distance").getValue(int.class);
                    Log.d("naumov", "Distance is " + distance);

                    points[day] += distance;
                    dataPoints[day] = new DataPoint(day, points[day]);
                    series = new LineGraphSeries<>(dataPoints);
                    graphView.removeAllSeries();
                    graphView.addSeries(series);

                    Log.d("naumov", "Day " + day + " , Distance " + points[day]);
                    Log.d("naumov", "------------------------------------------------------");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        for (int i = 0; i < points.length; i++) {
            Log.d("naumov", "Day " + i + " , Distance " + points[i]);
        }

        for (int i = 0; i < dataPoints.length; i++) {
            int distance = points[i];

            dataPoints[i] = new DataPoint(i, distance);
        }

        // on below line we are adding data to our graph view.
        series = new LineGraphSeries<>(dataPoints);

        // after adding data to our line graph series.
        // on below line we are setting
        // title for our graph view.
        graphView.setTitle("My Graph View");

        // on below line we are setting
        // text color to our graph view.
        graphView.setTitleColor(R.color.purple_200);

        // on below line we are setting
        // our title text size.
        graphView.setTitleTextSize(18);

        // on below line we are adding
        // data series to our graph view.
        graphView.addSeries(series);
        graphView.onDataChanged(true, true);
    }

}