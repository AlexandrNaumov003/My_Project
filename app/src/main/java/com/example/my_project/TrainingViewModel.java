package com.example.my_project;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;

public class TrainingViewModel extends AndroidViewModel {

    public static MutableLiveData<Boolean> isRunning = new MutableLiveData<>();

    public static MutableLiveData<Boolean> isNewTraining = new MutableLiveData<>();

    public static MutableLiveData<List<LatLng>> locations = new MutableLiveData<>();

    public static MutableLiveData<List<LatLng>> stops = new MutableLiveData<>();

    public static MutableLiveData<Long> time = new MutableLiveData<>();
    public static MutableLiveData<Long> totalTime = new MutableLiveData<>();
    public static MutableLiveData<Double> avgSpeed = new MutableLiveData<>();
    public static MutableLiveData<HashMap<String, Double>> speeds = new MutableLiveData<>();
    public static MutableLiveData<Double> maxSpeed = new MutableLiveData<>();
    public static MutableLiveData<Double> totalDistance = new MutableLiveData<>();

    public static void clearData(){
        isRunning.setValue(false);
        isNewTraining.setValue(true);
        locations = new MutableLiveData<>();
        speeds.setValue(new HashMap<>());
        stops = new MutableLiveData<>();
        time.setValue(0L);
        totalTime.setValue(0L);
        avgSpeed.setValue(0D);
        maxSpeed.setValue(0D);
        totalDistance.setValue(0D);
    }

    public TrainingViewModel(@NonNull Application application) {
        super(application);
    }
}
