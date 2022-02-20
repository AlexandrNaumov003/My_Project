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

import java.time.LocalDate;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
                Intent intent=new Intent(requireContext(), Statistic_screen.class);
                startActivity(intent);
            }
        });
        RunList = new ArrayList<Run>();
        lv = view.findViewById(R.id.lv_last_trainings_statistic_screen);
        run_ref = FirebaseUtils.getCurrentUserRuns();

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        retrieveData(year, month);

    }

    private void retrieveData(int year, int month) {
        Query query1=run_ref.orderByChild("year").equalTo(year);
        DatabaseReference ref1=query1.getRef();

        Query query2=ref1.orderByChild("month").equalTo(month);
        DatabaseReference ref2=query2.getRef();

        Query query3=ref2.orderByChild("finishTime").limitToLast(3);

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
                adapter = new RunAdapter(RunList,getContext(), 0, 0);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}