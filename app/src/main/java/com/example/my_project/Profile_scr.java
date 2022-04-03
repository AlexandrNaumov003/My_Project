package com.example.my_project;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile_scr#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile_scr extends Fragment implements View.OnClickListener {

    private CircleImageView iv_profile_screen;
    TextView name, surname, email;
    Button btn_update, btn_exit;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Profile_scr() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile_scr.
     */
    // TODO: Rename and change types and number of parameters
    @NonNull
    public static Profile_scr newInstance(String param1, String param2) {
        Profile_scr fragment = new Profile_scr();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_scr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        iv_profile_screen = view.findViewById(R.id.iv_profile_screen);
        iv_profile_screen.setOnClickListener(this);

        name=view.findViewById(R.id.tv_name_profile_screen);
        surname=view.findViewById(R.id.tv_surname_profile_screen);
        email=view.findViewById(R.id.tv_email_profile_screen);
        btn_update=view.findViewById(R.id.btn_update_profile_profile_screen);
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(requireContext(), Update_Profile_screen.class);
                startActivity(intent);
            }
        });

        btn_exit=view.findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(view1 -> createSignOutDialog());

        getUserData();
    }

    private void createSignOutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setCancelable(false);
        builder.setTitle("Sign out");
        builder.setMessage("Are you sure about signing out from account?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseUtils.getFirebaseAuth().signOut();
                startActivity(new Intent(requireContext(), Log_In_scr.class));
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public void getUserData(){
        FirebaseUtils.getCurrentUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    name.setText(user.getName());
                    email.setText(user.getEmail());
                    surname.setText(user.getSurname());

                    Uri pp = FirebaseUtils.getCurrentFirebaseUser().getPhotoUrl();
                    Uri sample_profile = Utils.getUriToDrawable(requireActivity(), R.drawable.sample_profile);

                    if (pp == null){
                        Glide.with(requireContext()).load(sample_profile).centerCrop().into(iv_profile_screen);
                    }
                    else {
                        Glide.with(Profile_scr.this).load(pp).centerCrop().into(iv_profile_screen);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View view) {

    }

}