package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
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
    int trainings_amount=0;

    TextInputEditText spinner_period_statistic_screen;

    LocalDate selectedDate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_screen);

        LocalDate today = LocalDate.now();
        selectedDate = today;

        spinner_period_statistic_screen = findViewById(R.id.spinner_period_statistic_screen);
        spinner_period_statistic_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(Statistic_screen.this, AlertDialog.THEME_HOLO_LIGHT);
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
                        spinner_period_statistic_screen.setText(text);

                        showGraphs(selectedDate);
                    }
                });
                datePickerDialog.show();
            }
        });

        graphView_distance = findViewById(R.id.graph_distance_statistic);
        graphView_time = findViewById(R.id.graph_time_statistic);
        graphView_speed = findViewById(R.id.graph_speed_statistic);

        // enable scrolling
        graphView_distance.getViewport().setScrollable(true);

        //set manual Y bounds
        graphView_distance.getViewport().setYAxisBoundsManual(true);

        // set manual X bounds
        graphView_distance.getViewport().setXAxisBoundsManual(true);

        // enable scrolling
        graphView_time.getViewport().setScrollable(true);

        //set manual Y bounds
        graphView_time.getViewport().setYAxisBoundsManual(true);

        // set manual X bounds
        graphView_time.getViewport().setXAxisBoundsManual(true);

        // enable scrolling
        graphView_time.getViewport().setScrollable(true);

        //set manual Y bounds
        graphView_speed.getViewport().setYAxisBoundsManual(true);

        // set manual X bounds
        graphView_speed.getViewport().setXAxisBoundsManual(true);

        // enable scrolling
        graphView_speed.getViewport().setScrollable(true);

        graphView_distance.setTitle("Distance");
        graphView_distance.setTitleTextSize(50);
        graphView_distance.getGridLabelRenderer().setVerticalAxisTitle("km");
        graphView_distance.getGridLabelRenderer().setHorizontalAxisTitle("day");

        graphView_time.setTitle("Time");
        graphView_time.setTitleTextSize(50);
        graphView_time.getGridLabelRenderer().setVerticalAxisTitle("minutes");
        graphView_time.getGridLabelRenderer().setHorizontalAxisTitle("day");

        graphView_speed.setTitle("Speed");
        graphView_speed.setTitleTextSize(50);
        graphView_speed.getGridLabelRenderer().setVerticalAxisTitle("km/h");
        graphView_speed.getGridLabelRenderer().setHorizontalAxisTitle("day");

        String today_text = Utils.DateToText(today);
        spinner_period_statistic_screen.setText(today_text);
        showGraphs(today);

    }

    public void showGraphs(@NonNull LocalDate localDate){

        int current_month_length = localDate.lengthOfMonth();
        int current_month_value = localDate.getMonthValue();
        int current_year_value = localDate.getYear();

        Toast.makeText(this, "current_month_length = " + current_month_length +
                "\n current_month_value = " + current_month_value +
                "\n current_year_value = " + current_year_value, Toast.LENGTH_LONG).show();

        DataPoint[] dataPoints_distance = new DataPoint[current_month_length+1];
        DataPoint[] dataPoints_time = new DataPoint[current_month_length+1];
        DataPoint[] dataPoints_speed = new DataPoint[current_month_length+1];

        double[] points_distance = new double[current_month_length+1];
        int[] points_time = new int[current_month_length+1];
        double[] points_speed = new double[current_month_length+1];

        double[] sum_speed = new double[current_month_length+1];
        int[] count_speed = new int[current_month_length+1];

        points_distance[0] = 0;
        points_time[0] = 0;
        points_speed[0] = 0;

        DatabaseReference run_data = FirebaseUtils.getCurrentUserRuns();

        Query query_year = run_data.orderByChild("year").equalTo(current_year_value);
        DatabaseReference ref = query_year.getRef();
        Query query_month = ref.orderByChild("month").equalTo(current_month_value);

        //Log.d("naumov", "Month is " + current_month_value);

        graphView_distance.removeAllSeries();
        graphView_time.removeAllSeries();
        graphView_speed.removeAllSeries();

        query_month.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    //Log.d("naumov", "------------------------------------------------------");
                    int day = data.child("day").getValue(int.class);
                    //Log.d("naumov", "Day is " + day);
                    double distance = data.child("distance").getValue(double.class);
                    //Log.d("naumov", "Distance is " + distance);
                    int time = data.child("totalTime").getValue(int.class);
                    //Log.d("naumov", "Time is " + totalTime);
                    double speed = data.child("speed").getValue(double.class);
                    //Log.d("naumov", "Speed is " + speed);

                    max_distance = Math.max(max_distance, distance);
                    max_time = Math.max(max_time, time);
                    max_speed = Math.max(max_speed, speed);

                    //// TODO: 13.02.2022 average speed
                    points_distance[day] += distance;
                    points_time[day] += time;

                    sum_speed[day] += speed;
                    count_speed[day]++;

                    points_speed[day] = (double) sum_speed[day]/count_speed[day];

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



                    //graphView_distance.getViewport().setMinY(0);
                    graphView_distance.getViewport().setMaxY(max_distance+10);

                    //graphView_distance.getViewport().setMinX(0);
                    graphView_distance.getViewport().setMaxX(localDate.lengthOfMonth());
                    graphView_distance.onDataChanged(false, false);



                    //graphView_time.getViewport().setMinY(0);
                    graphView_time.getViewport().setMaxY(max_time+10);

                    // graphView_time.getViewport().setMinX(0);
                    graphView_time.getViewport().setMaxX(localDate.lengthOfMonth());
                    graphView_time.onDataChanged(false, false);




                    //graphView_speed.getViewport().setMinY(0);
                    graphView_speed.getViewport().setMaxY(max_speed+10);

                    //graphView_speed.getViewport().setMinX(0);
                    graphView_speed.getViewport().setMaxX(localDate.lengthOfMonth());
                    graphView_speed.onDataChanged(false, false);





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
                    graphView_distance.getViewport().setMaxX(localDate.lengthOfMonth());
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