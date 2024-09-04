package com.yh04.joyfulmindapp.model;

import com.google.gson.annotations.SerializedName;

public class NidProfileResponse {
    @SerializedName("response")
    private Profile profile;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
