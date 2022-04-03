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
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static String TAG = "naumov";

        private GoogleMap map;

        private FusedLocationProviderClient client;
        TextView timeView, distanceView, speedView;
        Button btn_start, btn_stop, btn_reset, btn_pause, btn_resume;


    private Polyline gpsTrack;
    private SupportMapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private LatLng lastKnownLatLng;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference runRef;


    // Use seconds, running and wasRunning respectively
    // to record the number of seconds passed,
    // whether the stopwatch is running and
    // whether the stopwatch was running
    // before the activity was paused.

    // Number of seconds displayed
    // on the stopwatch.
    private int seconds = 0;

    // Is the stopwatch running?
    private boolean running;

    private boolean wasRunning;

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

            /*if (savedInstanceState != null) {

                // Get the previous state of the stopwatch
                // if the activity has been
                // destroyed and recreated.
                seconds = savedInstanceState.getInt("seconds");
                running = savedInstanceState.getBoolean("running");
                wasRunning = savedInstanceState.getBoolean("wasRunning");
            }*/
//            runTimer();

            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(requireContext())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }

            firebaseDatabase = FirebaseDatabase.getInstance();

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
                        /*if (getContext() == null){
                            sendTurnOffPowerSavingNotification();
                        }
                        else {
                            createTurnOffPowerSavingDialog();
                        }*/

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

    // Save the state of the stopwatch
    // if it's about to be destroyed.
    /*@Override
    public void onSaveInstanceState(
            Bundle savedInstanceState)
    {
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }*/

    // If the activity is paused,
    // stop the stopwatch.
    /*@Override
    public void onPause()
    {
        super.onPause();
        wasRunning = running;
        running = true;


        stopLocationUpdates();
    }*/

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously.
    @Override
    public void onResume() {
        super.onResume();
        /*if (wasRunning) {
            running = true;
        }

        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }*/

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

    /* Start the stopwatch running
     when the Start button is clicked.
     Below method gets called
     when the Start button is clicked.*/
    public void onClickStart(View view)
    {
        running = true;
        googleApiClient.connect();
    }

    // Stop the stopwatch running
    // when the Stop button is clicked.
    // Below method gets called
    // when the Stop button is clicked.
    public void onClickStop(View view)
    {
        running = false;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        LocalDate today=LocalDate.now();

        Calendar calendar=Calendar.getInstance();
        long finishTime = calendar.getTimeInMillis();
        Run r = new Run(seconds, Double.parseDouble(speedView.getText().toString().replace(",", ".")),
                Double.parseDouble(distanceView.getText().toString().replace(",", ".")),uid, "", today, finishTime);


        runRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Runs").push();
        r.setKey(runRef.getKey());
        runRef.setValue(r);

        googleApiClient.disconnect();

    }

    public void onClickPause(View view)
    {
        running = false;
        googleApiClient.disconnect();

    }

    public void onClickResume(View view) {
        running = true;
        googleApiClient.connect();

    }

    // Reset the stopwatch when
    // the Reset button is clicked.
    // Below method gets called
    // when the Reset button is clicked.
    public void onClickReset(View view)
    {
        running = false;
        seconds = 0;
    }

    // Sets the NUmber of seconds on the timer.
    // The runTimer() method uses a Handler
    // to increment the seconds and
    // update the text view.
    private void runTimer()
    {

        // Get the text view.

        // Creates a new Handler
        final Handler handler = new Handler();

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

                // Set the text view text.
                timeView.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
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


//        btn_start.setOnClickListener(this::onClickStart);
//        btn_stop.setOnClickListener(this::onClickStop);
//        btn_reset.setOnClickListener(this::onClickReset);
//        btn_pause.setOnClickListener(this::onClickPause);
//        btn_resume.setOnClickListener(this::onClickResume);

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

        /*requireActivity().startService(new Intent(getContext(), TrainingService.class));
        // Build intent that displays the App settings screen.
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/

        }

    }

    public void createTurnOnPowerSavingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(true);
        builder.setTitle("Stop tracking");
        builder.setMessage("You can now turn on back power saving mode");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public void createTurnOffPowerSavingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        builder.setTitle("Start tracking");
        builder.setMessage("In order to continue you have to turn off power saving mode");


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

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

        TrainingService.timeLiveData.removeObservers(getViewLifecycleOwner());

        TrainingService.totalDistance.removeObservers(getViewLifecycleOwner());
        TrainingService.avgSpeed.removeObservers(getViewLifecycleOwner());
        TrainingService.maxSpeed.removeObservers(getViewLifecycleOwner());

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

        requireContext().registerReceiver(broadcastReceiver, intentFilter);

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

        TrainingService.timeLiveData.observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long seconds) {

                int hours = (int) (seconds / 3600);
                int minutes = (int) ((seconds % 3600) / 60);
                int secs = (int) (seconds % 60);

                // Format the timeLiveData into hours, minutes,
                // and timeLiveData.
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

                timeView.setText(time);
            }
        });

        TrainingService.totalDistance.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double totalDistance) {
                distanceView.setText(new DecimalFormat("####.##").format(totalDistance) + " m");
            }
        });

        TrainingService.avgSpeed.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double speed) {
                speedView.setText(new DecimalFormat("##.##").format(speed) + " m/s");
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

        TrainingService.timeLiveData.removeObservers(getViewLifecycleOwner());

        TrainingService.totalDistance.removeObservers(getViewLifecycleOwner());
        TrainingService.avgSpeed.removeObservers(getViewLifecycleOwner());
        TrainingService.maxSpeed.removeObservers(getViewLifecycleOwner());

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
//            triggerRebirth(requireContext());
        }
        else if (requestCode == 20000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "Permission " + permissions[i] + " is yet not granted");
                    }
                }

//                startTracking();

            }
            else {
                //Permission not granted
            }
//            triggerRebirth(requireContext());
        }
    }

   /* @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }*/

   /* @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }*/




    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateTrack();
    }


    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(4000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    private void updateTrack() {
        List<LatLng> points = gpsTrack.getPoints();
        points.add(lastKnownLatLng);
        gpsTrack.setPoints(points);

        int current_position = points.lastIndexOf(lastKnownLatLng);
        int previous_position = current_position-1;

        if (previous_position >= 0){
            showDistance(points.get(previous_position), points.get(current_position));
        }
    }

    public void getDistance(){
        List<LatLng> points = gpsTrack.getPoints();
        double distance = 0;
        for (int i = 1; i< points.size(); i++){
            LatLng previous = points.get(i-1);
            LatLng point = points.get(i);
            double lat = Math.abs(point.latitude - previous.latitude);
            double lng = Math.abs(point.longitude - previous.longitude);
        }
    }

    double totalDistance = 0;

    public void showDistance(@NonNull LatLng previous, @NonNull LatLng current){
       /* double lat = Math.abs(previous.latitude - current.latitude);
        double lng = Math.abs(previous.longitude - current.longitude);

        double d = Math.sqrt(Math.pow(lat, 2) + Math.pow(lng, 2));*/
        //Location startloc=Location.distanceBetween(previous.latitude, previous.latitude, current.latitude, current.longitude, );

        Location startPoint = new Location("previous");
        startPoint.setLatitude(previous.latitude);
        startPoint.setLongitude(previous.longitude);

        Location endPoint = new Location("current");
        endPoint.setLatitude(current.latitude);
        endPoint.setLongitude(current.longitude);

        float distance = startPoint.distanceTo(endPoint);
        totalDistance += (double) distance;
        Log.d("naumov", "distance  = " + distance);
        Log.d("naumov", "totalDistance  = " + totalDistance);
        Log.d("naumov", "----------------------------------");

        double speed = (double) totalDistance/seconds;


//        double distanceToShow = Double.parseDouble(new DecimalFormat("####.##").format(totalDistance));

        distanceView.setText(new DecimalFormat("####.##").format(totalDistance));
        speedView.setText(new DecimalFormat("####.##").format(speed));
    }
}

