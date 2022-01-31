package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registration_screen extends AppCompatActivity implements TextWatcher {

    EditText name, username, email, password;
    Button register;
    FirebaseAuth mAuth;

    @Override
    public void onBackPressed() {
        Intent intent=new Intent(Registration_screen.this, Log_In_scr.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_screen);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        name = findViewById(R.id.et_name_registration);
        username = findViewById(R.id.et_username_registration);
        email = findViewById(R.id.et_email_registration);
        password = findViewById(R.id.et_password_registration);
        password.addTextChangedListener(this);
        register = findViewById(R.id.btn_register_registration);
        mAuth = FirebaseAuth.getInstance();



        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEmailValid(email.getText().toString())){
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Registration_screen.this, "User registered", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    email.setError("E-mail is not valid");
                }
            }
        });
    }

    public boolean isEmailValid(String email){
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(password.getText().toString().length()<6){
            password.setError("Password has to include at least 6 symbols");
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}