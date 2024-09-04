package com.yh04.joyfulmindapp.api;

import com.yh04.joyfulmindapp.model.Diary;
import com.yh04.joyfulmindapp.model.DiaryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DiaryApi {

    // 모든 다이어리를 가져오는 엔드포인트
    @GET("diary")
    Call<DiaryResponse> getAllDiaries(@Header("Authorization") String token);

    // 특정 날짜의 다이어리를 가져오는 엔드포인트
    @GET("diary")
    Call<DiaryResponse> getDiariesForDate(@Header("Authorization") String token, @Query("date") String date);

    // 특정 기간 동안의 다이어리를 가져오는 엔드포인트
    @GET("diary/range")
    Call<DiaryResponse> getDiariesForRange(@Header("Authorization") String token, @Query("start_date") String startDate, @Query("end_date") String endDate);

    // 새로운 다이어리를 생성하는 엔드포인트
    @POST("diary")
    Call<Void> createDiary(@Header("Authorization") String token, @Body Diary diary);

    // 특정 다이어리를 업데이트하는 엔드포인트
    @PUT("diary/{diaryId}")
    Call<Void> updateDiary(@Header("Authorization") String token, @Path("diaryId") int diaryId, @Body Diary diary);

    // 특정 다이어리를 삭제하는 엔드포인트
    @DELETE("diary/{diaryId}")
    Call<Void> deleteDiary(@Header("Authorization") String token, @Path("diaryId") int diaryId);
}
