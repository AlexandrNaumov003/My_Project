package com.example.my_project;

import androidx.annotation.NonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public final static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String TimeToText(@NonNull LocalTime time){
        return time.format(timeFormat);
    }

    public static LocalTime TextToTime(String time){
        return LocalTime.parse(time, timeFormat);
    }
}