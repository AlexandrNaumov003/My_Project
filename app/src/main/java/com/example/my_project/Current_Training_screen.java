package com.example.my_project;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

            if (savedInstanceState != null) {

                // Get the previous state of the stopwatch
                // if the activity has been
                // destroyed and recreated.
                seconds = savedInstanceState.getInt("seconds");
                running = savedInstanceState.getBoolean("running");
                wasRunning = savedInstanceState.getBoolean("wasRunning");
            }
            runTimer();

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

        }

    // Save the state of the stopwatch
    // if it's about to be destroyed.
    @Override
    public void onSaveInstanceState(
            Bundle savedInstanceState)
    {
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }

    // If the activity is paused,
    // stop the stopwatch.
    @Override
    public void onPause()
    {
        super.onPause();
        wasRunning = running;
        running = false;


        stopLocationUpdates();
    }

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously.
    @Override
    public void onResume()
    {
        super.onResume();
        if (wasRunning) {
            running = true;
        }

        if (googleApiClient.isConnected()) {
            startLocationUpdates();
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
        googleApiClient.disconnect();

    }

    public void onClickPause(View view)
    {
        running = false;
        googleApiClient.disconnect();

    }

    public void onClickResume(View view)
    {
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

            btn_start=view.findViewById(R.id.start_button);
            btn_stop=view.findViewById(R.id.stop_button);
            btn_reset=view.findViewById(R.id.reset_button);
            btn_pause=view.findViewById(R.id.pause_button);
            btn_resume=view.findViewById(R.id.resume_button);

            btn_start.setOnClickListener(this::onClickStart);
            btn_stop.setOnClickListener(this::onClickStop);
            btn_reset.setOnClickListener(this::onClickReset);
            btn_pause.setOnClickListener(this::onClickPause);
            btn_resume.setOnClickListener(this::onClickResume);




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

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Asking for the permission");
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog...");
            }

            requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10000);

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
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(4);
        gpsTrack = map.addPolyline(polylineOptions);
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
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
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

    double totalDistance;

    public void showDistance(@NonNull LatLng previous, @NonNull LatLng current){
        double lat = Math.abs(previous.latitude - current.latitude);
        double lng = Math.abs(previous.longitude - current.longitude);

        double d = Math.sqrt(Math.pow(lat, 2) + Math.pow(lng, 2));
        distanceView.setText(""+d);
    }
}

