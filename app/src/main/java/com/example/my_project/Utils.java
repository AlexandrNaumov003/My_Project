package com.example.my_project;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.AnyRes;
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

    /**
     * get uri to drawable or any other resource type if u wish
     * @param context - context
     * @param drawableId - drawable res id
     * @return - uri
     */
    public static Uri getUriToDrawable(@NonNull Context context, @AnyRes int drawableId) {

        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId) );
    }
}