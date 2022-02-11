package com.example.my_project;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FirebaseUtils {

    public static FirebaseUser getCurrentFirebaseUser(){
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

    //ToDo change reference to "Events"
    public static final DatabaseReference usersDatabase = getDatabase().getReference("Users").getRef();

    @NonNull
    public static String getCurrentUID(){
        return getCurrentFirebaseUser().getUid();
    }

    public static boolean isUserLoggedIn(){
        return getCurrentFirebaseUser() != null;
    }

    @NonNull
    public static DatabaseReference getCurrentUserRuns(){
        return getDatabase().getReference("Users").child(getCurrentUID()).child("Runs").getRef();
    }
}
