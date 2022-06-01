package com.example.my_project;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Progress_scr#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Progress_scr extends Fragment {

    ArrayList<Run> RunList;
    ListView lv;
    RunAdapter adapter;
    DatabaseReference run_ref;
    TextView tv_more_trainings, tv_more_statistics;
    GraphView barChart_distance;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Progress_scr() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Progress_scr.
     */
    // TODO: Rename and change types and number of parameters
    @NonNull
    public static Progress_scr newInstance(String param1, String param2) {
        Progress_scr fragment = new Progress_scr();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //// TODO: init Elements

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_progress_scr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_more_trainings=view.findViewById(R.id.tv_show_more_trainings_statistic_screen);
        tv_more_trainings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(requireContext(), All_Trainings_screen.class);
                startActivity(intent);
            }
        });

        tv_more_statistics=view.findViewById(R.id.tv_show_more_statistics_statistic_screen);
        tv_more_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(requireContext(), Statistics_screen.class);
                startActivity(intent);
            }
        });
        RunList = new ArrayList<>();
        lv = view.findViewById(R.id.lv_last_trainings_statistic_screen);
        barChart_distance = view.findViewById(R.id.barChart_distance);
        run_ref = Utils.getUserRuns();

        retrieveData();
        retrieveBarChartData();
    }

    private void retrieveBarChartData() {

        Query query = run_ref.orderByChild("finishTime");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    return;
                }

                double[] distances = new double[Month.values().length+1];

                for (DataSnapshot data : snapshot.getChildren()) {
                    Run run = data.getValue(Run.class);
                    int month = run.getMonth();
                    distances[month] += run.getDistance();
                }

                DataPoint[] dataPoints = new DataPoint[13];
                dataPoints[0] = new DataPoint(0, 0);

                for (Month month : Month.values()) {
                    int monthValue = month.getValue();
                    dataPoints[monthValue] = new DataPoint(monthValue, distances[monthValue]);
                }

                BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints);
                barChart_distance.addSeries(series);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void retrieveData() {
        Query query=run_ref.orderByChild("finishTime").limitToLast(3);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    return;
                }

                RunList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Run r = data.getValue(Run.class);
                    RunList.add(r);
                }

                Collections.reverse(RunList);
                adapter = new RunAdapter(RunList,getContext(), 0, 0);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}