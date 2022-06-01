package com.example.my_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class Update_Profile_screen extends AppCompatActivity {

    private CircleImageView iv_update_profile_screen;
    EditText et_name, et_surname, et_email;
    Button btn_save;

    String uid;

    public static final int SELECT_PICTURE = 2500;

    Uri selectedImageUri;

    public static final String ACTION_GO_TO_PROFILE_SCREEN = BuildConfig.APPLICATION_ID + "action_go_to_profile_screen";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_screen);

        progressDialog = new ProgressDialog(this);

        iv_update_profile_screen = findViewById(R.id.iv_update_profile_screen);
        iv_update_profile_screen.setOnClickListener(v -> chooseProfilePicture());

        et_name=findViewById(R.id.et_name_update_profile_screen);
        et_surname=findViewById(R.id.et_surname_update_profile_screen);
        et_email=findViewById(R.id.et_email_update_profile_screen);
        btn_save=findViewById(R.id.btn_save_update_profile_screen);

        btn_save.setOnClickListener(view -> updateUserData());

        getUserData();
    }

    public void updateUserData(){
        progressDialog.setMessage("Updating profile data");
        progressDialog.show();

        String uname=et_name.getText().toString();
        String usurname=et_surname.getText().toString();
        String uemail=et_email.getText().toString();

        User user = new User(uname, usurname, uemail, uid);

        Utils.getUser().setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                UserProfileChangeRequest.Builder userProfileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(selectedImageUri);

                Utils.getFirebaseUser().updateProfile(userProfileChangeRequest.build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        Intent intent = new Intent(Update_Profile_screen.this, MainActivity.class);
                        intent.setAction(ACTION_GO_TO_PROFILE_SCREEN);

                        startActivity(intent);
                    }
                });
            }
        });
    }

    public void getUserData(){
        Utils.getUser().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    et_name.setText(user.getName());
                    et_email.setText(user.getEmail());
                    et_surname.setText(user.getSurname());

                    Uri pp = Utils.getFirebaseUser().getPhotoUrl();

                    if (pp == null){
                        Glide.with(Update_Profile_screen.this).load(R.drawable.profile).centerCrop().into(iv_update_profile_screen);
                    }
                    else {
                        Glide.with(Update_Profile_screen.this).load(pp).centerCrop().into(iv_update_profile_screen);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // this function is triggered when
    // the Select Image Button is clicked
    private void chooseProfilePicture() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);

    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                if (data.getData() != null) {
                    selectedImageUri = data.getData();
                    // update the preview image in the layout
                    iv_update_profile_screen.setImageURI(selectedImageUri);
                }
            }
        }
    }
}