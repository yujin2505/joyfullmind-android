package com.yh04.joyfulmindapp.api;

import com.yh04.joyfulmindapp.model.User;
import com.yh04.joyfulmindapp.model.UserRes;
import com.yh04.joyfulmindapp.model.UserChange;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface UserApi {

    @POST("user/login")
    Call<UserRes> login(@Body User user);

    @POST("user/register")
    Call<UserRes> register(@Body User user);

    // 비밀번호 변경 API
    @PUT("user/updatedpwd")
    Call<UserRes> changePassword(@Header("Authorization") String token, @Body UserChange request);

    // 닉네임 변경 API
    @PUT("user/updatednickname")
    Call<UserRes> changeNickname(@Header("Authorization") String token, @Body UserChange request);

    // 프로필 정보 가져오기 API
    @GET("user/profile")
    Call<UserRes> getUserProfile(@Header("Authorization") String token);
}
