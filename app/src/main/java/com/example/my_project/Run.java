package com.example.my_project;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.time.LocalDate;

public class Run implements Serializable {

    private long time;
    private double speed;
    private double distance;
    private String uid;
    private String key;
    private int year;
    private int month;
    private int day;
    private long finishTime;

    public Run(){

    }

    public Run(long time, double speed, double distance, String uid, @NonNull LocalDate localDate, long finishTime) {
        this.time = time;
        this.speed = speed;
        this.distance = distance;
        this.uid = uid;
        this.year = localDate.getYear();
        this.month = localDate.getMonthValue();
        this.day=localDate.getDayOfMonth();
        this.finishTime=finishTime;
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

    @Override
    public String toString() {
        return "Run{" +
                "totalTime='" + time + '\'' +
                ", speed='" + speed + '\'' +
                ", distance='" + distance + '\'' +
                ", uid='" + uid + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }
}
