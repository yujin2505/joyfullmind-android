package com.yh04.joyfulmindapp.model;

public class UserRes {

    public String result;
    public String accessToken;
    public String message;
    public User user;

    // 기본 생성자
    public UserRes() {}

    public UserRes(String result, String accessToken, String message, User user) {
        this.result = result;
        this.accessToken = accessToken;
        this.message = message;
        this.user = user;
    }

    // 생성자 오버로딩
    public UserRes(String result, String accessToken, String message) {
        this.result = result;
        this.accessToken = accessToken;
        this.message = message;
    }

    // Getters and setters
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // 이메일 반환 메서드
    public String getEmail() {
        return user != null ? user.email : null;
    }

    // 닉네임 반환 메서드 (필요한 경우)
    public String getNickname() {
        return user != null ? user.nickname : null;
    }
}
