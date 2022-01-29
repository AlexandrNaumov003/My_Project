package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.profile_scr){
            getSupportFragmentManager().beginTransaction().replace(R.id.bottomNavigationView, new Profile_scr());
        }
        if(item.getItemId() == R.id.progress_scr){
            getSupportFragmentManager().beginTransaction().replace(R.id.bottomNavigationView, new Progress_scr());
        }
        if(item.getItemId() == R.id.current_Training_screen){
            getSupportFragmentManager().beginTransaction().replace(R.id.bottomNavigationView, new Current_Training_screen());
        }
        return false;
    }
}