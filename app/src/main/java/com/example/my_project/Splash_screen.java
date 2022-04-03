package com.example.my_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Splash_screen extends AppCompatActivity {
    Button btn;
    TextView tv_welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        btn=findViewById(R.id.btn_start_splash);
        tv_welcome=findViewById(R.id.tv_welcome_splash);
        btn.setVisibility(View.INVISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(FirebaseUtils.isUserLoggedIn()){
                   intent = new Intent(Splash_screen.this, MainActivity.class);
                }
                else {
                    intent = new Intent(Splash_screen.this, Log_In_scr.class);
                }

                startActivity(intent);
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn.setVisibility(View.VISIBLE);
                tv_welcome.setVisibility(View.INVISIBLE);
            }
        }, 5000);


    }
}