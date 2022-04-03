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
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
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
    public static final String ACTION_RESET_TRACKING_SERVICE = BuildConfig.APPLICATION_ID + "action_reset_tracking";

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

    public static MutableLiveData<Boolean> isRunning = new MutableLiveData<>();

    public static MutableLiveData<Boolean> isNewTraining = new MutableLiveData<>();

    public static MutableLiveData<List<LatLng>> locationsLiveData = new MutableLiveData<>();

    public static MutableLiveData<List<LatLng>> stops = new MutableLiveData<>();

    public static MutableLiveData<Long> timeLiveData = new MutableLiveData<>();
    public static MutableLiveData<Long> totalTimeLiveData = new MutableLiveData<>();
    public static MutableLiveData<Double> avgSpeed = new MutableLiveData<>();
    public static MutableLiveData<HashMap<String, Double>> speeds = new MutableLiveData<>();
    public static MutableLiveData<Double> maxSpeed = new MutableLiveData<>();
    public static MutableLiveData<Double> totalDistance = new MutableLiveData<>();

    public static void clearData(){
        isRunning.setValue(false);
        isNewTraining.setValue(true);
        locationsLiveData = new MutableLiveData<>();
        speeds.setValue(new HashMap<>());
        stops = new MutableLiveData<>();
        timeLiveData.setValue(0L);
        totalTimeLiveData.setValue(0L);
        avgSpeed.setValue(0D);
        maxSpeed.setValue(0D);
        totalDistance.setValue(0D);
    }

    private final Runnable runnable = new Runnable() {
        @Override

        public void run() {

            // If running is true, increment the
            // timeLiveData variable.
            if (TrainingService.isRunning.getValue()) {
                Log.d("tracking", "timeLiveData = " + time);
                TrainingService.timeLiveData.setValue(time);
                time++;
            }

            // Post the code again
            // with a delay of 1 second.
            handler.postDelayed(this, 1000);
        }
    };

    private final LocationCallback locationCallback = new LocationCallback() {
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

            TrainingService.locationsLiveData.setValue(locations);

            int current_position = locations.size()-1;
            int previous_position = current_position-1;

            double absoluteDistance = SphericalUtil.computeLength(TrainingService.locationsLiveData.getValue());

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

    private final LocationRequest locationRequest = new LocationRequest()
            .setWaitForAccurateLocation(true)
            .setInterval(4000)
            .setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            /*.setSmallestDisplacement(1)*/;

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

            if (TrainingService.isNewTraining.getValue() != null) {
                if (TrainingService.isNewTraining.getValue()){
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
        else if (action.equals(ACTION_RESET_TRACKING_SERVICE)){
            finishTraining(false);
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

        TrainingService.clearData();

        TrainingService.isNewTraining.setValue(false);

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

        handler.post(runnable);

        TrainingService.isRunning.setValue(true);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void resumeTraining(){
        Log.d("tracking", "RESUMED");

        TrainingService.isRunning.setValue(true);

        notificationBuilder.setContentText("Training started");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void stopTraining() {
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        TrainingService.isRunning.setValue(false);

        notificationBuilder.setContentText("Training stopped");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void finishTraining(boolean upload){
        Log.d("tracking", "FINISHED");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        TrainingService.isRunning.setValue(false);
        TrainingService.isNewTraining.setValue(true);

        handler.removeCallbacks(runnable);

        time = 0;

        TrainingService.clearData();


//        addTraining(locationsLiveData);

        stopForeground(true);
        stopSelf();

        notificationManager.cancel(NOTIFICATION_ID);

        end = System.currentTimeMillis()/1000;

        if (upload){

        }

//        end = Calendar.getInstance().getTimeInMillis();

    }

    //ToDo adjust training data

   /* public Task<Void> uploadTraining(long start, long end){

        String trainingId = FirebaseUtils.getCurrentUserTrainingsRef().push().getKey();

        long timeLiveData = TrainingService.timeLiveData.getValue();
        long totalTimeLiveData = TrainingService.totalTimeLiveData.getValue();
        double speed = TrainingService.avgSpeed.getValue();
        double maxSpeed = TrainingService.maxSpeed.getValue();
        double totalDistance = TrainingService.totalDistance.getValue();
        HashMap<String, Double> speeds = TrainingService.speeds.getValue();


        Training training = new Training(trainingId, start, end, timeLiveData, totalTimeLiveData, speed, maxSpeed, speeds, totalDistance);
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

        double absoluteDistance = SphericalUtil.computeLength(TrainingService.locationsLiveData.getValue());

        TrainingService.totalDistance.setValue(TrainingService.totalDistance.getValue() + distance);

        TrainingService.avgSpeed.setValue(round((double) TrainingService.totalDistance.getValue()/ TrainingService.timeLiveData.getValue(), 3));
        HashMap<String, Double> addedSpeeds = TrainingService.speeds.getValue();
        addedSpeeds.put(String.valueOf(TrainingService.timeLiveData.getValue()), TrainingService.avgSpeed.getValue());
        TrainingService.speeds.setValue(addedSpeeds);

        TrainingService.maxSpeed.setValue(Math.max(TrainingService.avgSpeed.getValue(), TrainingService.maxSpeed.getValue()));

        Log.d(TAG, "-----------------------------------tracking-----------------------------");
        Log.d(TAG, "");
        Log.d(TAG, "distance = " + distance);
        Log.d(TAG, "absoluteDistance = " + absoluteDistance);
        Log.d(TAG, "totalDistance = " + TrainingService.totalDistance.getValue());
        Log.d(TAG, "avgSpeed = " + TrainingService.avgSpeed.getValue());
        Log.d(TAG, "maxSpeed = " + TrainingService.maxSpeed.getValue());
        Log.d(TAG, "speeds = " + TrainingService.speeds.getValue().toString());
        Log.d(TAG, "");
        Log.d(TAG, "-------------------------------tracking---------------------------------");

    }

    @Override
    public boolean stopService(@NonNull Intent name) {

        if (name.getAction() != null && name.getAction().equals(ACTION_FINISH_TRACKING_SERVICE)){
        }
            finishTraining(false);

            Toast.makeText(this, "Training Stopped", Toast.LENGTH_SHORT).show();
            Log.d("murad", "Training Stopped");

        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        stopLocationUpdates();

        finishTraining(true);

        Toast.makeText(this, "Training Finished", Toast.LENGTH_SHORT).show();
        Log.d("murad", "Training Finished");

    }
}
