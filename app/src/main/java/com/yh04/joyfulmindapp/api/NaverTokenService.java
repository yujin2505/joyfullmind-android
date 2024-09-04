package com.yh04.joyfulmindapp.api;

import com.yh04.joyfulmindapp.model.NaverTokenResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NaverTokenService {
    @GET("oauth2.0/token")
    Call<NaverTokenResponse> getAccessToken(
            @Query("grant_type") String grantType,
            @Query("client_id") String clientId,
            @Query("client_secret") String clientSecret,
            @Query("code") String code,
            @Query("state") String state
    );
}
