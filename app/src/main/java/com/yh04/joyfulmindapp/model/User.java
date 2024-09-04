package com.yh04.joyfulmindapp.model;

public class User {

    public String email;
    public String password;
    public String nickname;
    public int gender;
    public String birthDate;

    public User(String email, String password, String nickname, String birthDate, int gender) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public User(String nickname){
        this.nickname = nickname;
    }

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
