package com.yh04.joyfulmindapp.api;

import com.yh04.joyfulmindapp.model.Song;
import com.yh04.joyfulmindapp.model.SongResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpotifyService {
    @GET("recommend")
    Call<SongResponse> getRecommendedSongs(@Query("emotion") String emotion, @Query("limit") int limit);
}