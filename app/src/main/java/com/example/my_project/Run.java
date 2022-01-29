package com.example.my_project;

public class Run {
    private String time;
    private String speed;
    private String distance;

    public Run(String time, String speed, String distance) {
        this.time = time;
        this.speed = speed;
        this.distance = distance;
    }
    public Run() {

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
