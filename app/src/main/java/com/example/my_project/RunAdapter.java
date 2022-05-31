package com.example.my_project;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

public class RunAdapter extends ArrayAdapter<Run>  {

    ArrayList<Run> runs;
    Context context;

    public RunAdapter(ArrayList<Run> runs, Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId, runs);
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
        view=LayoutInflater.from(context).inflate(R.layout.row_list_run, null);

        ImageView imageView=view.findViewById(R.id.iv_row_run);
        TextView distance=view.findViewById(R.id.tv_distance_row_run);
        TextView time=view.findViewById(R.id.tv_time_row_run);
        TextView speed=view.findViewById(R.id.tv_speed_row_run);

        int hours = (int) (tmp.getTime() / 3600);
        int minutes = (int) ((tmp.getTime() % 3600) / 60);
        int secs = (int) (tmp.getTime() % 60);

        // Format the totalTime into hours, minutes,
        // and totalTime.
        String text = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

        time.setText(text);
        distance.setText(String.valueOf(tmp.getDistance()));
        speed.setText(String.valueOf(tmp.getSpeed()));

        return view;
    }
}
