package com.example.my_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class RunAdapter extends BaseAdapter {

    ArrayList<Run> runs;
    Context context;

    public RunAdapter(ArrayList<Run> runs, Context context) {
        this.runs = runs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return runs.size();
    }

    @Override
    public Run getItem(int i) {
        return runs.get(i);

    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Run tmp=runs.get(i);
        view= LayoutInflater.from(context).inflate(R.layout.row_list_run, null);
        TextView distance=view.findViewById(R.id.tv_distance_row_run);
        TextView time=view.findViewById(R.id.tv_time_row_run);
        distance.setText(tmp.getDistance());
        time.setText(tmp.getTime());
        return view;
    }
}
