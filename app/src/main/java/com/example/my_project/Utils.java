package com.example.my_project;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Utils {
    public final static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    public final static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMMM yyyy");

    public static String TimeToText(@NonNull LocalTime time){
        return time.format(timeFormat);
    }

    public static LocalTime TextToTime(String time){
        return LocalTime.parse(time, timeFormat);
    }

    public static String DateToText(@NonNull LocalDate date){
        return date.format(dateFormat);
    }

    public static LocalDate TextToDate(String date){
        return LocalDate.parse(date, dateFormat);
    }
}