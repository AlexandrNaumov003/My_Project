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
    GraphView graphView_distance,graphView_time, graphView_speed;
    LineGraphSeries<DataPoint> series_distance, series_time, series_speed;
    double max_distance=0;
    int max_time=0;
    double max_speed=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_screen);

        graphView_distance = findViewById(R.id.graph_distance_statistic);
        graphView_time=findViewById(R.id.graph_time_statistic);
        graphView_speed=findViewById(R.id.graph_speed_statistic);

        LocalDate today = LocalDate.now();
        int current_month_length = today.lengthOfMonth();
        int current_month_value = today.getMonthValue();

        DataPoint[] dataPoints_distance = new DataPoint[current_month_length+1];
        DataPoint[] dataPoints_time = new DataPoint[current_month_length+1];
        DataPoint[] dataPoints_speed = new DataPoint[current_month_length+1];

        double[] points_distance = new double[current_month_length+1];
        int[] points_time = new int[current_month_length+1];
        double[] points_speed = new double[current_month_length+1];



        points_distance[0] = 0;
        points_time[0] = 0;
        points_speed[0] = 0;

        DatabaseReference run_data = FirebaseUtils.getCurrentUserRuns();

        Query query = run_data.orderByChild("month").equalTo(current_month_value);
        //Log.d("naumov", "Month is " + current_month_value);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    //Log.d("naumov", "------------------------------------------------------");
                    int day = data.child("day").getValue(int.class);
                    //Log.d("naumov", "Day is " + day);
                    double distance = data.child("distance").getValue(int.class);
                    //Log.d("naumov", "Distance is " + distance);
                    int time=data.child("time").getValue(int.class);
                    //Log.d("naumov", "Time is " + time);
                    double speed=data.child("speed").getValue(int.class);
                    //Log.d("naumov", "Speed is " + speed);

                    max_distance=Math.max(max_distance, distance);
                    max_time=Math.max(max_time, time);
                    max_speed=Math.max(max_speed, speed);

                    //// TODO: 12.02.2022 average speed + average distance 
                    points_distance[day] += distance;
                    points_time[day] +=time;
                    points_speed[day] +=speed;

                    dataPoints_distance[day] = new DataPoint(day, points_distance[day]);
                    dataPoints_time[day] = new DataPoint(day, points_time[day]);
                    dataPoints_speed[day] = new DataPoint(day, points_speed[day]);

                    series_distance = new LineGraphSeries<>(dataPoints_distance);
                    series_time = new LineGraphSeries<>(dataPoints_time);
                    series_speed = new LineGraphSeries<>(dataPoints_speed);

                    graphView_distance.removeAllSeries();
                    graphView_time.removeAllSeries();
                    graphView_speed.removeAllSeries();

                    graphView_distance.addSeries(series_distance);
                    graphView_time.addSeries(series_time);
                    graphView_speed.addSeries(series_speed);


                    //set manual Y bounds
                    graphView_distance.getViewport().setYAxisBoundsManual(true);
                    //graphView_distance.getViewport().setMinY(0);
                    graphView_distance.getViewport().setMaxY(max_distance+10);
                    // set manual X bounds
                    graphView_distance.getViewport().setXAxisBoundsManual(true);
                    //graphView_distance.getViewport().setMinX(0);
                    graphView_distance.getViewport().setMaxX(today.lengthOfMonth());
                    graphView_distance.onDataChanged(false, false);
                    // enable scrolling
                    graphView_distance.getViewport().setScrollable(true);


                    //set manual Y bounds
                    graphView_time.getViewport().setYAxisBoundsManual(true);
                    //graphView_time.getViewport().setMinY(0);
                    graphView_time.getViewport().setMaxY(max_time+10);
                    // set manual X bounds
                    graphView_time.getViewport().setXAxisBoundsManual(true);
                    // graphView_time.getViewport().setMinX(0);
                    graphView_time.getViewport().setMaxX(today.lengthOfMonth());
                    graphView_time.onDataChanged(false, false);
                    // enable scrolling
                    graphView_time.getViewport().setScrollable(true);


                    //set manual Y bounds
                    graphView_speed.getViewport().setYAxisBoundsManual(true);
                    //graphView_speed.getViewport().setMinY(0);
                    graphView_speed.getViewport().setMaxY(max_speed+10);
                    // set manual X bounds
                    graphView_speed.getViewport().setXAxisBoundsManual(true);
                    //graphView_speed.getViewport().setMinX(0);
                    graphView_speed.getViewport().setMaxX(today.lengthOfMonth());
                    graphView_speed.onDataChanged(false, false);
                    // enable scrolling
                    graphView_speed.getViewport().setScrollable(true);



                    graphView_distance.setTitle("Distance");
                    graphView_distance.setTitleTextSize(20);
                    graphView_distance.getGridLabelRenderer().setVerticalAxisTitle("km");
                    graphView_distance.getGridLabelRenderer().setHorizontalAxisTitle("day");

                    graphView_time.setTitle("Time");
                    graphView_time.setTitleTextSize(20);
                    graphView_time.getGridLabelRenderer().setVerticalAxisTitle("hours");
                    graphView_time.getGridLabelRenderer().setHorizontalAxisTitle("day");

                    graphView_speed.setTitle("Speed");
                    graphView_speed.setTitleTextSize(20);
                    graphView_speed.getGridLabelRenderer().setVerticalAxisTitle("km/h");
                    graphView_speed.getGridLabelRenderer().setHorizontalAxisTitle("day");
                  /*
                    // optional styles
                    //graph.setTitleTextSize(40);
                    //graph.setTitleColor(Color.BLUE);
                    //graph.getGridLabelRenderer().setVerticalAxisTitleTextSize(40);
                    graphView_distance.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLUE);
                    //graph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(40);
                    graphView_distance.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLUE);

*/
  /*                  // set manual X bounds
                    graphView_distance.getViewport().setYAxisBoundsManual(true);
                    //graphView_distance.getViewport().setMinY(0);
                    graphView_distance.getViewport().setMaxY(max_distance+10);
                    graphView_distance.getViewport().setXAxisBoundsManual(true);
                    //graphView_distance.getViewport().setMinX(0);
                    graphView_distance.getViewport().setMaxX(today.lengthOfMonth());
                    // enable scaling
                    graphView_distance.getViewport().setScalable(true);

*/
/*
                    Log.d("naumov", "Day " + day + " , Distance " + points_distance[day]);
                    Log.d("naumov", "Day " + day + " , Time " + points_time[day]);
                    Log.d("naumov", "Day " + day + " , Speed " + points_speed[day]);
                    Log.d("naumov", "------------------------------------------------------");*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
/*

        for (int i = 0; i < points_distance.length; i++) {
            Log.d("naumov", "Day " + i + " , Distance " + points_distance[i]);
            Log.d("naumov", "Day " + i + " , Time " + points_time[i]);
            Log.d("naumov", "Day " + i + " , Speed " + points_speed[i]);
        }
*/

        for (int i = 0; i < dataPoints_distance.length; i++) {
            double distance = points_distance[i];
            int time = points_time[i];
            double speed = points_speed[i];

            dataPoints_distance[i] = new DataPoint(i, distance);
            dataPoints_time[i] = new DataPoint(i, time);
            dataPoints_speed[i] = new DataPoint(i, speed);
        }
/*

        // after adding data to our line graph series.
        // on below line we are setting
        // title for our graph view.
        graphView_distance.setTitle("Distance");

        // on below line we are setting
        // text color to our graph view.
        graphView_distance.setTitleColor(R.color.purple_200);

        // on below line we are setting
        // our title text size.

*/

    }

}