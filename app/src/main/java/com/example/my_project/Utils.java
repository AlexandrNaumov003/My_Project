package com.example.my_project;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public final static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    public final static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMMM yyyy");

    public static String DateToText(@NonNull LocalDate date){
        return date.format(dateFormat);
    }

    public static FirebaseUser getFirebaseUser(){
        return getFirebaseAuth().getCurrentUser();
    }

    @NonNull
    public static FirebaseAuth getFirebaseAuth(){
        return FirebaseAuth.getInstance();
    }

    @NonNull
    public static FirebaseDatabase getDatabase(){
        return FirebaseDatabase.getInstance();
    }

    @NonNull
    public static String getCurrentUID(){
        return getFirebaseUser().getUid();
    }

    public static boolean isUserLoggedIn(){
        return getFirebaseUser() != null;
    }

    @NonNull
    public static DatabaseReference getCurrentUserRuns(){
        return getDatabase().getReference("Runs").child(getCurrentUID()).getRef();
    }

    @NonNull
    public static DatabaseReference getCurrentUserRef(){
        return getDatabase().getReference("Users").child(getCurrentUID()).getRef();
    }
}