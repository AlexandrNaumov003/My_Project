package com.example.my_project;

public class Run {
    private String time;
    private String speed;
    private String distance;
    private String uid;
    private String key;
    private int image;

    public Run(String time, String speed, String distance, String uid, String key) {
        this.image = R.drawable.ic_baseline_directions_run;
        this.time = time;
        this.speed = speed;
        this.distance = distance;
        this.uid = uid;
        this.key = key;

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
                "time='" + time + '\'' +
                ", speed='" + speed + '\'' +
                ", distance='" + distance + '\'' +
                ", uid='" + uid + '\'' +
                ", key='" + key + '\'' +
                ", image=" + image +
                '}';
    }
}
