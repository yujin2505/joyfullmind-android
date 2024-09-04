package com.yh04.joyfulmindapp.api;

import com.yh04.joyfulmindapp.model.NidProfileResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface NaverApiService {
    @GET("v1/nid/me")
    Call<NidProfileResponse> getProfile(@Header("Authorization") String accessToken);
}

