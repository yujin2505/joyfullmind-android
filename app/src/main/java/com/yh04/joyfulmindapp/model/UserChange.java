package com.yh04.joyfulmindapp.model;

public class UserChange {
    // 비밀번호 변경 필드
    private String oldPassword;
    private String newPassword;

    // 닉네임 변경 필드
    private String newNickname;

    // 기본 생성자
    public UserChange() {}

    // 비밀번호 변경을 위한 생성자
    public UserChange(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // 닉네임 변경을 위한 생성자
    public UserChange(String newNickname) {
        this.newNickname = newNickname;
    }

    // Getters and setters
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewNickname() {
        return newNickname;
    }

    public void setNewNickname(String newNickname) {
        this.newNickname = newNickname;
    }
}
