package com.example.my_project;


import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class Current_Training_screen extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

        private OnMapReadyCallback callback = new OnMapReadyCallback() {

            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;
                LatLng sydney = new LatLng(-34, 151);
                map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            }
        };

        private GoogleMap map;

        private FusedLocationProviderClient client;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if(getArguments() != null) {

            }

            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);


        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_current_training_screen, container, false);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            client = LocationServices.getFusedLocationProviderClient(requireContext());

        }


        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            map = googleMap;
            LatLng sydney = new LatLng(-34, 151);
            map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);

            map.setOnMyLocationClickListener(this);

            GoogleMapOptions options = new GoogleMapOptions();
            options.mapType(GoogleMap.MAP_TYPE_NORMAL);
        }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }
}

