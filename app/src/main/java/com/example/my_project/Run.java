package com.example.my_project;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class Run {
    private long time;
    private double speed;
    private double distance;
    private String uid;
    private String key;
    private int image;
    private int year;
    private int month;
    private int day;
    private long finishTime;

    public Run(long time, double speed, double distance, String uid, String key, int year, int month, int day) {
        this.image = R.drawable.ic_baseline_directions_run;
        this.time = time;
        this.speed = speed;
        this.distance = distance;
        this.uid = uid;
        this.key = key;
        this.year=year;
        this.month=month;
        this.day=day;

    }

    public Run(long time, double speed, double distance, String uid, String key, @NonNull LocalDate localDate, long finishTime) {
        this.image = R.drawable.ic_baseline_directions_run;
        this.time = time;
        this.speed = speed;
        this.distance = distance;
        this.uid = uid;
        this.key = key;
        this.year = localDate.getYear();
        this.month = localDate.getMonthValue();
        this.day=localDate.getDayOfMonth();
        this.finishTime=finishTime;

    }
    public Run() {

    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getSpeed() {
        return speed;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Run{" +
                "totalTime='" + time + '\'' +
                ", speed='" + speed + '\'' +
                ", distance='" + distance + '\'' +
                ", uid='" + uid + '\'' +
                ", key='" + key + '\'' +
                ", image=" + image +
                '}';
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }
}
