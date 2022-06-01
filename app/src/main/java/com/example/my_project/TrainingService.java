package com.example.my_project;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.maps.android.SphericalUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class TrainingService extends LifecycleService {

    public static final String ACTION_START_TRACKING_SERVICE = BuildConfig.APPLICATION_ID + "action_start_tracking";
    public static final String ACTION_STOP_TRACKING_SERVICE = BuildConfig.APPLICATION_ID + "action_stop_tracking";
    public static final String ACTION_FINISH_TRACKING_SERVICE = BuildConfig.APPLICATION_ID + "action_finish_tracking";
    public static final String ACTION_RESET_TRACKING_SERVICE = BuildConfig.APPLICATION_ID + "action_reset_tracking";

    public static final String ACTION_MOVE_TO_TRAINING_FRAGMENT = BuildConfig.APPLICATION_ID + "action_move_to_tracking_fragment";


    public static final String TAG = "training";
    
    public final static int NOTIFICATION_ID = 333;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private static long time;

    private final Handler handler = new Handler();

    private NotificationManager notificationManager;

    private NotificationCompat.Builder notificationBuilder;

    public static MutableLiveData<Boolean> isRunning = new MutableLiveData<>();

    public static MutableLiveData<Boolean> isNewTraining = new MutableLiveData<>();

    public static MutableLiveData<List<LatLng>> locationsLiveData = new MutableLiveData<>();

    public static MutableLiveData<Long> totalTime = new MutableLiveData<>();
    public static MutableLiveData<Double> avgSpeed = new MutableLiveData<>();
    public static MutableLiveData<Double> totalDistance = new MutableLiveData<>();

    public static void clearData(){
        isRunning.setValue(false);
        isNewTraining.setValue(true);
        locationsLiveData = new MutableLiveData<>();
        totalTime.setValue(0L);
        avgSpeed.setValue(0D);
        totalDistance.setValue(0D);
    }

    private final Runnable runnable = new Runnable() {
        @Override

        public void run() {

            // If running is true, increment the
            // totalTime variable.
            if (TrainingService.isRunning.getValue()) {
                Log.d("training", "totalTime = " + time);
                TrainingService.totalTime.setValue(time);
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

            if (!Utils.isUserLoggedIn()){
                stopTraining();
                return;
            }

            double latitude = round(locationResult.getLastLocation().getLatitude(), 3);
            double longitude = round(locationResult.getLastLocation().getLongitude(), 3);

            Log.d(TAG, "latitude = " + latitude);
            Log.d(TAG, "longitude = " + longitude);

            List<LatLng> locations = TrainingService.locationsLiveData.getValue();

            locations.add(new LatLng(latitude, longitude));

            TrainingService.locationsLiveData.setValue(locations);

            calculateDistance();
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

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {

        String action = intent.getAction();

        switch (action) {
            case ACTION_START_TRACKING_SERVICE:
                Toast.makeText(this, "Training Started", Toast.LENGTH_SHORT).show();
                Log.d("naumov", "Training Started");

                if (TrainingService.isNewTraining.getValue() != null) {
                    if (TrainingService.isNewTraining.getValue()) {
                        startTraining();
                    } else {
                        resumeTraining();
                    }
                }
                break;
            case ACTION_STOP_TRACKING_SERVICE:
                stopTraining();
                break;
            case ACTION_RESET_TRACKING_SERVICE:
                finishTraining(false);
                break;
            case PowerManager.ACTION_POWER_SAVE_MODE_CHANGED:
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

                Toast.makeText(this, "Power mode changed", Toast.LENGTH_SHORT).show();

                if (powerManager.isPowerSaveMode()) {
                    createTurnOffPowerSavingDialog();
                }
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void createTurnOffPowerSavingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Start training");
        builder.setMessage("In order to continue you have to turn off power saving mode");

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.setNegativeButton("Back", (dialog, which) -> dialog.dismiss());

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
        Log.d("training", "STARTED");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_MOVE_TO_TRAINING_FRAGMENT);

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

        handler.post(runnable);

        TrainingService.isRunning.setValue(true);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void resumeTraining(){
        Log.d("training", "RESUMED");

        TrainingService.isRunning.setValue(true);

        notificationBuilder.setContentText("Training started");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void stopTraining() {

        TrainingService.isRunning.setValue(false);

        notificationBuilder.setContentText("Training stopped");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void finishTraining(boolean upload){
        Log.d("training", "FINISHED");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        TrainingService.isRunning.setValue(false);
        TrainingService.isNewTraining.setValue(true);

        handler.removeCallbacks(runnable);

        stopForeground(true);
        stopSelf();

        notificationManager.cancel(NOTIFICATION_ID);

        if (upload){
            createRun();
        }

        time = 0;

        TrainingService.clearData();
    }

    private void createRun() {

        double speed = avgSpeed.getValue();
        double distance = totalDistance.getValue();
        long totalTimeValue = totalTime.getValue();

        Run run = new Run(totalTimeValue, speed, distance, Utils.getUID(),
                LocalDate.now(), System.currentTimeMillis());

        Intent intent = new Intent(this, Add_Training_scr.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("run", run);
        startActivity(intent);
    }

    public void calculateDistance(){

        double absoluteDistance = SphericalUtil.computeLength(TrainingService.locationsLiveData.getValue())/1000;

        TrainingService.totalDistance.setValue(absoluteDistance);

        long seconds = TrainingService.totalTime.getValue();
        double hours = (double) seconds/3600;

        TrainingService.avgSpeed.setValue(round(TrainingService.totalDistance.getValue() / hours, 3));


        Log.d(TAG, "-----------------------------------tracking-----------------------------");
        Log.d(TAG, "");
        Log.d(TAG, "absoluteDistance = " + absoluteDistance);
        Log.d(TAG, "totalDistance = " + TrainingService.totalDistance.getValue());
        Log.d(TAG, "avgSpeed = " + TrainingService.avgSpeed.getValue());
        Log.d(TAG, "");
        Log.d(TAG, "-------------------------------tracking---------------------------------");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        finishTraining(true);

        Toast.makeText(this, "Training finished", Toast.LENGTH_SHORT).show();
        Log.d("naumov", "Training finished");

    }
}
