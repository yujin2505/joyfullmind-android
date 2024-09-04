
package com.yh04.joyfulmindapp.api;

import com.yh04.joyfulmindapp.model.ChatMessage;
import com.yh04.joyfulmindapp.model.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApi {
    @POST("chat")
    Call<ChatResponse> chat(@Body ChatMessage chatMessage);
}