package com.example.my_project;

public class User {

    private String name;
    private String surname;
    private String email;
    private String uid;

    public User(String name, String surname, String email, String uid){
        this.name=name;
        this.surname=surname;
        this.email=email;
        this.uid=uid;
    }
    public User(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
