package com.example.my_project;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrainingService extends LifecycleService {

    public static final String ACTION_START_TRACKING_SERVICE = BuildConfig.APPLICATION_ID + "action_start_tracking";
    public static final String ACTION_STOP_TRACKING_SERVICE = BuildConfig.APPLICATION_ID + "action_stop_tracking";
    public static final String ACTION_FINISH_TRACKING_SERVICE = BuildConfig.APPLICATION_ID + "action_finish_tracking";

    public static final String ACTION_MOVE_TO_TRAINING_FRAGMENT = BuildConfig.APPLICATION_ID + "action_move_to_tracking_fragment";


    public static final String TAG = "tracking";
    
    public final static int NOTIFICATION_ID = 333;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private static List<LatLng> locations;
    private static long time;
    private static long totalTime;

    private Handler handler = new Handler();

    private NotificationManager notificationManager;

    private NotificationCompat.Builder notificationBuilder;

    private long start;
    private long end;

    private LocationCallback locationCallback = new LocationCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (!FirebaseUtils.isUserLoggedIn()){
                stopTraining();
                return;
            }

//            double latitude = locationResult.getLastLocation().getLatitude();
//            double longitude = locationResult.getLastLocation().getLongitude();

            double latitude = round(locationResult.getLastLocation().getLatitude(), 3);
            double longitude = round(locationResult.getLastLocation().getLongitude(), 3);

            Log.d(TAG, "latitude = " + latitude);
            Log.d(TAG, "longitude = " + longitude);

            locations.add(new LatLng(latitude, longitude));

            TrainingViewModel.locations.setValue(locations);

            int current_position = locations.size()-1;
            int previous_position = current_position-1;

            double absoluteDistance = SphericalUtil.computeLength(TrainingViewModel.locations.getValue());

            Log.d(TAG, "absoluteDistance = " + absoluteDistance);

            if (previous_position >= 0){
                calculateDistance(locations.get(previous_position), locations.get(current_position));
            }

//            Toast.makeText(TrainingService.this, "Latitude: " + latitude
//                    + "\nLongitude: " + longitude, Toast.LENGTH_SHORT).show();
        }
    };

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private LocationRequest locationRequest = new LocationRequest()
            .setWaitForAccurateLocation(true)
            .setInterval(2000)
            .setFastestInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setSmallestDisplacement(1);
    

    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, @NonNull IntentFilter filter) {
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);

        return super.registerReceiver(receiver, filter);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locations = new ArrayList<>();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {

        String action = intent.getAction();

        if (action.equals(ACTION_START_TRACKING_SERVICE)) {
            Toast.makeText(this, "Training Started", Toast.LENGTH_SHORT).show();
            Log.d("murad", "Training Started");

            if (TrainingViewModel.isNewTraining.getValue() != null) {
                if (TrainingViewModel.isNewTraining.getValue()){
                    startTraining();
                }
                else {
                    resumeTraining();
                }
            }
        }
        else if (action.equals(ACTION_STOP_TRACKING_SERVICE)) {
            stopTraining();
        }
        else if (action.equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

            Toast.makeText(this, "Power mode changed", Toast.LENGTH_SHORT).show();

            if (powerManager.isPowerSaveMode()) {
                createTurnOffPowerSavingDialog();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void createTurnOffPowerSavingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @SuppressLint("MissingPermission")
    private void startTraining() {

        TrainingViewModel.clearData();

        TrainingViewModel.isNewTraining.setValue(false);

        Toast.makeText(this, "Training started", Toast.LENGTH_SHORT).show();
        Log.d("tracking", "STARTED");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_MOVE_TO_TRAINING_FRAGMENT);
//        intent_stop_alarm.setAction(ACTION_STOP_VIBRATION);

        /*PendingIntent pintent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);*/

        PendingIntent pintent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = new NotificationCompat.Builder(this, "CHANNEL_ID");
        notificationBuilder.setSmallIcon(R.drawable.ic_baseline_directions_run)
                .setContentTitle("Training started")
//                .setContentText("0:00:00")
                .setAutoCancel(false)
                .setContentIntent(pintent)
                .setUsesChronometer(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);


        String channelId = "CHANNEL_ID";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH);

        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);

        notificationBuilder.setChannelId(channelId);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        start = System.currentTimeMillis()/1000;

        handler.post(new Runnable() {
            @Override

            public void run() {

                // If running is true, increment the
                // time variable.
                if (TrainingViewModel.isRunning.getValue()) {
                    Log.d("tracking", "time = " + time);
                    TrainingViewModel.time.setValue(time);
                    time++;
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });

        TrainingViewModel.isRunning.setValue(true);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void resumeTraining(){
        Log.d("tracking", "RESUMED");

        TrainingViewModel.isRunning.setValue(true);

        notificationBuilder.setContentText("Training started");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void stopTraining() {
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        TrainingViewModel.isRunning.setValue(false);

        notificationBuilder.setContentText("Training stopped");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void finishTraining(){
        Log.d("tracking", "FINISHED");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        TrainingViewModel.isRunning.setValue(false);
        TrainingViewModel.isNewTraining.setValue(true);

//        addTraining(locations);

        stopForeground(true);
        stopSelf();

        notificationManager.cancel(NOTIFICATION_ID);

        end = System.currentTimeMillis()/1000;

//        end = Calendar.getInstance().getTimeInMillis();

    }

    //ToDo adjust training data

   /* public Task<Void> uploadTraining(long start, long end){

        String trainingId = FirebaseUtils.getCurrentUserTrainingsRef().push().getKey();

        long time = TrainingViewModel.time.getValue();
        long totalTime = TrainingViewModel.totalTime.getValue();
        double speed = TrainingViewModel.avgSpeed.getValue();
        double maxSpeed = TrainingViewModel.maxSpeed.getValue();
        double totalDistance = TrainingViewModel.totalDistance.getValue();
        HashMap<String, Double> speeds = TrainingViewModel.speeds.getValue();


        Training training = new Training(trainingId, start, end, time, totalTime, speed, maxSpeed, speeds, totalDistance);
        Log.d("tracking", training.toString());


        return FirebaseUtils.getCurrentUserTrainingsRef().child(trainingId).setValue(training);
    }*/

    public void calculateDistance(@NonNull LatLng previous, @NonNull LatLng current){
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

//        double distance = round(startPoint.distanceTo(endPoint), 3);

        double distance = SphericalUtil.computeDistanceBetween(previous, current);

        double absoluteDistance = SphericalUtil.computeLength(TrainingViewModel.locations.getValue());

        TrainingViewModel.totalDistance.setValue(TrainingViewModel.totalDistance.getValue() + distance);

        TrainingViewModel.avgSpeed.setValue(round((double) TrainingViewModel.totalDistance.getValue()/ TrainingViewModel.time.getValue(), 3));
        HashMap<String, Double> addedSpeeds = TrainingViewModel.speeds.getValue();
        addedSpeeds.put(String.valueOf(TrainingViewModel.time.getValue()), TrainingViewModel.avgSpeed.getValue());
        TrainingViewModel.speeds.setValue(addedSpeeds);

        TrainingViewModel.maxSpeed.setValue(Math.max(TrainingViewModel.avgSpeed.getValue(), TrainingViewModel.maxSpeed.getValue()));

        Log.d(TAG, "-----------------------------------tracking-----------------------------");
        Log.d(TAG, "");
        Log.d(TAG, "distance = " + distance);
        Log.d(TAG, "absoluteDistance = " + absoluteDistance);
        Log.d(TAG, "totalDistance = " + TrainingViewModel.totalDistance.getValue());
        Log.d(TAG, "avgSpeed = " + TrainingViewModel.avgSpeed.getValue());
        Log.d(TAG, "maxSpeed = " + TrainingViewModel.maxSpeed.getValue());
        Log.d(TAG, "speeds = " + TrainingViewModel.speeds.getValue().toString());
        Log.d(TAG, "");
        Log.d(TAG, "-------------------------------tracking---------------------------------");

    }

    @Override
    public boolean stopService(@NonNull Intent name) {

        if (name.getAction() != null && name.getAction().equals(ACTION_FINISH_TRACKING_SERVICE)){
        }
            finishTraining();

            Toast.makeText(this, "Training Stopped", Toast.LENGTH_SHORT).show();
            Log.d("murad", "Training Stopped");

        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        stopLocationUpdates();


        finishTraining();

        Toast.makeText(this, "Training Finished", Toast.LENGTH_SHORT).show();
        Log.d("murad", "Training Finished");

    }
}
