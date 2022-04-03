package com.example.my_project;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;

public class TrainingViewModel extends AndroidViewModel {



    public TrainingViewModel(@NonNull Application application) {
        super(application);
    }
}
