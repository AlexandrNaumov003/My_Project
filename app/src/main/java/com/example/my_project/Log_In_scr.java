package com.example.my_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Log_In_scr extends AppCompatActivity implements TextWatcher {

    EditText et_email, et_password;
    TextView tv_sign_up;
    Button btn_login;
    FirebaseAuth firebaseAuth;
    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_screen);

        tv_sign_up=findViewById(R.id.tv_sign_up_log_in);
        tv_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Log_In_scr.this, Registration_screen.class);
                startActivity(intent);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        et_email=findViewById(R.id.et_enter_email_log_in);
        et_password=findViewById(R.id.et_enter_password_log_in);
        et_password.addTextChangedListener(this);
        btn_login = findViewById(R.id.btn_enter_log_in);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmailValid() | !validatePassword()){
                    et_email.setError("E-mail is not valid");
                }
                else {
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(Log_In_scr.this, All_Trainings_screen.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "You are not registered or E-mail is not valid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

    }


    private boolean validatePassword(){
        password = et_password.getText().toString();
        if(!password.isEmpty())
            return true;
        else{
            Toast.makeText(getApplicationContext(), "Password field is empty", Toast.LENGTH_SHORT).show();
            return false;
    }
}

    public boolean isEmailValid(){
        email=et_email.getText().toString();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(et_password.getText().toString().length()<6){
            et_password.setError("Password has to include at least 6 symbols");
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}

