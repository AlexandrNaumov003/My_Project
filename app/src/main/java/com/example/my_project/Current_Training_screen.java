package com.example.my_project;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class Current_Training_screen extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    public static String TAG = "naumov";

        private GoogleMap map;

        private FusedLocationProviderClient client;
        TextView timeView, distanceView, speedView;
        Button btn_start, btn_stop, btn_reset, btn_pause, btn_resume;


    private Polyline gpsTrack;

    private BroadcastReceiver broadcastReceiver;
        
    private final static int TURN_OFF_POWER_SAVE_MODE_NOTIFICATION_ID = 111;

    private boolean mapReady = false;

    public void getMyLocation() {

        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try {
                    LatLng current_location  = new LatLng(location.getLatitude(), location.getLongitude());
//                    map.moveCamera(CameraUpdateFactory.newLatLng(current_location));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location,  18));
//                    map.moveCamera(CameraUpdateFactory.zoomBy(1f));
//                    map.clear();
//                    map.addMarker(new MarkerOptions().position(current_location).title("Current Location"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    getMyLocation();
//                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if(getArguments() != null) {

            }

            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            if (TrainingService.isRunning.getValue() == null){
                TrainingService.isRunning.setValue(false);
            }

            if (TrainingService.isNewTraining.getValue() == null){
                TrainingService.isNewTraining.setValue(true);
            }


            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, @NonNull Intent intent) {
                    if (TrainingService.isRunning.getValue() && intent.getAction().equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)){
                        PowerManager powerManager = (PowerManager) requireActivity().getSystemService(Context.POWER_SERVICE);

                        Toast.makeText(requireContext(), "Power mode changed", Toast.LENGTH_SHORT).show();

                        if (powerManager.isPowerSaveMode()){
                            sendTurnOffPowerSavingNotification();
                        }
                        else {
                            NotificationManager notificationManager =
                                    (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);

                            notificationManager.cancel(TURN_OFF_POWER_SAVE_MODE_NOTIFICATION_ID);
                        }
                    }
                }
            };

        }

    public void sendTurnOffPowerSavingNotification(){
        Intent i = new Intent(requireContext(), MainActivity.class);
//        intent_stop_alarm.setAction(ACTION_STOP_VIBRATION);

        PendingIntent pintent = PendingIntent.getBroadcast(requireContext(), 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "CHANNEL_ID");
        builder.setSmallIcon(R.drawable.ic_baseline_directions_run)
                .setContentTitle("Warning")
                .setContentText("Turn off power saving mode in order to continue tracking")
                .setColor(Color.RED)
                .setAutoCancel(true)
                .setContentIntent(pintent);

        NotificationManager notificationManager =
                (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);


        String channelId = "CHANNEL_ID";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);

        builder.setChannelId(channelId);

        notificationManager.notify(TURN_OFF_POWER_SAVE_MODE_NOTIFICATION_ID, builder.build());
    }

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously.
    @Override
    public void onResume() {
        super.onResume();

        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager.isPowerSaveMode()) {
            createTurnOffPowerSavingDialog();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, "Permission " + Manifest.permission.ACCESS_BACKGROUND_LOCATION + " is granted");
            if (!checkBackgroundLocationPermission()){
                return;
            }
        }

        if (TrainingService.isRunning.getValue() != null && TrainingService.isRunning.getValue()){
            startTracking();
        }

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

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        client = LocationServices.getFusedLocationProviderClient(requireContext());
        timeView = view.findViewById(R.id.time_view);
        distanceView = view.findViewById(R.id.distance_view);
        speedView = view.findViewById(R.id.speed_view);

        btn_start=view.findViewById(R.id.btn_start_training);
        btn_stop=view.findViewById(R.id.btn_stop_training);
        btn_reset=view.findViewById(R.id.btn_reset_training);
        btn_pause=view.findViewById(R.id.btn_pause_training);
        btn_resume=view.findViewById(R.id.btn_resume_training);

        btn_start.setOnClickListener(v -> startTracking());
        btn_stop.setOnClickListener(v -> stopTracking());
        btn_reset.setOnClickListener(v -> resetTracking());
        btn_pause.setOnClickListener(v -> pauseTracking());
        btn_resume.setOnClickListener(v -> resumeTracking());

        btn_stop.setVisibility(View.GONE);
        btn_reset.setVisibility(View.GONE);
        btn_pause.setVisibility(View.GONE);
        btn_resume.setVisibility(View.GONE);


        if (TrainingService.isRunning.getValue() && !isTrainingServiceRunning()){
            resumeTracking();
        }

    }

    public void createTurnOnPowerSavingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(true);
        builder.setTitle("Stop tracking");
        builder.setMessage("You can now turn on back power saving mode");

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public void createTurnOffPowerSavingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        builder.setTitle("Start tracking");
        builder.setMessage("In order to continue you have to turn off power saving mode");


        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.setNegativeButton("Back", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public boolean isTrainingServiceRunning(){
        ActivityManager activityManager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null){
            for (ActivityManager.RunningServiceInfo service : activityManager
                    .getRunningServices(Integer.MAX_VALUE)){
                if (TrainingService.class.getName().equals(service.service.getClassName())){
                    if (service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    public void pauseTracking() {
        btn_start.setVisibility(View.GONE);
        btn_pause.setVisibility(View.GONE);
        btn_reset.setVisibility(View.VISIBLE);
        btn_resume.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.VISIBLE);


        Intent intent = new Intent(getContext(), TrainingService.class);
        intent.setAction(TrainingService.ACTION_STOP_TRACKING_SERVICE);
        requireActivity().startService(intent);
    }

    public void stopTracking(){

        btn_start.setVisibility(View.VISIBLE);
        btn_pause.setVisibility(View.GONE);
        btn_reset.setVisibility(View.GONE);
        btn_resume.setVisibility(View.GONE);
        btn_stop.setVisibility(View.GONE);

        timeView.setText("0:00:00");

        Intent intent = new Intent(getContext(), TrainingService.class);
        intent.setAction(TrainingService.ACTION_FINISH_TRACKING_SERVICE);

        requireActivity().stopService(intent);
//        stopLocationUpdates();

        TrainingService.locationsLiveData.removeObservers(getViewLifecycleOwner());

        TrainingService.totalTime.removeObservers(getViewLifecycleOwner());

        TrainingService.totalDistance.removeObservers(getViewLifecycleOwner());
        TrainingService.avgSpeed.removeObservers(getViewLifecycleOwner());

        requireActivity().unregisterReceiver(broadcastReceiver);


        createTurnOnPowerSavingDialog();
    }

    public void startTracking(){

        btn_start.setVisibility(View.GONE);
        btn_pause.setVisibility(View.VISIBLE);
        btn_reset.setVisibility(View.GONE);
        btn_resume.setVisibility(View.GONE);
        btn_stop.setVisibility(View.VISIBLE);

        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager.isPowerSaveMode()) {
            createTurnOffPowerSavingDialog();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, "Permission " + Manifest.permission.ACCESS_BACKGROUND_LOCATION + " is granted");
            if (!checkBackgroundLocationPermission()){
                return;
            }
        }

        // Define the IntentFilter.
        IntentFilter intentFilter = new IntentFilter();

        // Adding system broadcast actions sent by the system when the power save mode is changed.
        intentFilter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);

        requireActivity().registerReceiver(broadcastReceiver, intentFilter);

//        startLocationUpdates();

        Intent intent = new Intent(getContext(), TrainingService.class);
        intent.setAction(TrainingService.ACTION_START_TRACKING_SERVICE);

        requireActivity().startService(intent);

        TrainingService.locationsLiveData.observe(getViewLifecycleOwner(), new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> latLngs) {
                if (mapReady){
                    LatLng lastLocation = latLngs.get(latLngs.size()-1);

                    gpsTrack.setPoints(latLngs);
                    Log.d("murad", "Latitude: " + lastLocation.latitude
                            + "\nLongitude: " + lastLocation.longitude);

//                map.animateCamera(CameraUpdateFactory.newLatLng(lastLocation));
                }

            }
        });

        TrainingService.totalTime.observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long seconds) {

                int hours = (int) (seconds / 3600);
                int minutes = (int) ((seconds % 3600) / 60);
                int secs = (int) (seconds % 60);

                // Format the totalTime into hours, minutes,
                // and totalTime.
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

                timeView.setText(time);
            }
        });

        TrainingService.totalDistance.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double totalDistance) {
                distanceView.setText(new DecimalFormat("####.##").format(totalDistance) + " km");
            }
        });

        TrainingService.avgSpeed.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double speed) {
                speedView.setText(new DecimalFormat("##.##").format(speed) + " km/h");
            }
        });
    }

    public void resumeTracking(){

        btn_start.setVisibility(View.GONE);
        btn_pause.setVisibility(View.VISIBLE);
        btn_reset.setVisibility(View.GONE);
        btn_resume.setVisibility(View.GONE);
        btn_stop.setVisibility(View.VISIBLE);

        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager.isPowerSaveMode()) {
            createTurnOffPowerSavingDialog();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, "Permission " + Manifest.permission.ACCESS_BACKGROUND_LOCATION + " is granted");
            if (!checkBackgroundLocationPermission()){
                return;
            }
        }

        Intent intent = new Intent(getContext(), TrainingService.class);
        intent.setAction(TrainingService.ACTION_START_TRACKING_SERVICE);

        requireActivity().startService(intent);
    }

    private void resetTracking() {
        btn_start.setVisibility(View.VISIBLE);
        btn_pause.setVisibility(View.GONE);
        btn_reset.setVisibility(View.GONE);
        btn_resume.setVisibility(View.GONE);
        btn_stop.setVisibility(View.GONE);

        Intent intent = new Intent(getContext(), TrainingService.class);
        intent.setAction(TrainingService.ACTION_RESET_TRACKING_SERVICE);

        requireActivity().startService(intent);

        timeView.setText("0:00:00");

        TrainingService.locationsLiveData.removeObservers(getViewLifecycleOwner());

        TrainingService.totalTime.removeObservers(getViewLifecycleOwner());

        TrainingService.totalDistance.removeObservers(getViewLifecycleOwner());
        TrainingService.avgSpeed.removeObservers(getViewLifecycleOwner());

    }

    public boolean checkPermissions(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission is granted");
            return true;
        } else {
            Log.d(TAG, "Permission is not granted");
            askLocationPermission();
        }

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission is granted");
            return true;
        } else {
            Log.d(TAG, "Permission is not granted");
            askLocationPermission();
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean checkBackgroundLocationPermission(){
        boolean hasPermissions = true;

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission " + Manifest.permission.ACCESS_BACKGROUND_LOCATION + " is granted");
        } else {
            Log.d(TAG, "Permission " + Manifest.permission.ACCESS_BACKGROUND_LOCATION + " is not granted");
            hasPermissions = false;
            askBackgroundLocationPermission();
        }

        return hasPermissions;
    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Asking for the permission");
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog...");
            }

            requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10000);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void askBackgroundLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Asking for the permission");
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog...");
            }

            requestPermissions( new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 33333);

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (checkPermissions()){
            map.setMyLocationEnabled(true);

            map.setOnMyLocationButtonClickListener(this);
            map.setOnMyLocationClickListener(this);
            getMyLocation();
        }
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL);


        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.width(25);
        gpsTrack = map.addPolyline(polylineOptions);
        mapReady = true;

    }

    @Override
    public boolean onMyLocationButtonClick() {
        getMyLocation();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 10000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d(TAG, "Permission granted");
                map.setMyLocationEnabled(true);

                map.setOnMyLocationButtonClickListener(this);
                map.setOnMyLocationClickListener(this);
                getMyLocation();
                onMapReady(map);
            }
            else {
                //Permission not granted
            }
        }
        else if (requestCode == 20000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "Permission " + permissions[i] + " is yet not granted");
                    }
                }

            }
            else {
                //Permission not granted
            }
        }
    }
}

