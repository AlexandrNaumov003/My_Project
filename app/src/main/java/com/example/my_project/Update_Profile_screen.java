package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class Update_Profile_screen extends AppCompatActivity {

    EditText et_name, et_surname, et_email;
    Button btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_screen);

        et_name=findViewById(R.id.et_name_update_profile_screen);
        et_surname=findViewById(R.id.et_surname_update_profile_screen);
        et_email=findViewById(R.id.et_email_update_profile_screen);
        btn_save=findViewById(R.id.btn_save_update_profile_screen);


        FirebaseUtils.getCurrentUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user=snapshot.getValue(User.class);
                    String uname=user.getName();
                    String usurname=user.getSurname();
                    String uemail=user.getEmail();

                    et_name.setText(uname);
                    et_surname.setText(usurname);
                    et_email.setText(uemail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}