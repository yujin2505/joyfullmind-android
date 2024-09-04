package com.yh04.joyfulmindapp.adapter;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yh04.joyfulmindapp.config.Config;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    public static Retrofit retrofit;
    private static Retrofit retrofit2;
    private static Retrofit googleMapRetrofit;
    private static Retrofit naverRetrofit;
    private static Retrofit emotionRetrofit;

    public static Retrofit getRetrofitClient(Context context){
        if(retrofit == null){
            // 커스텀 Gson 인스턴스 생성
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new CustomDateAdapter())
                    .create();

            // 통신 로그 확인할때 필요한 코드
            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 네트워크 연결관련 코드
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(loggingInterceptor)
                    .build();
            // 네트워크로 데이터를 보내고 받는
            // 레트로핏 라이브러리 관련 코드
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.DOMAIN)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // 커스텀 Gson 추가
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getRetrofitClient2(Context context){
        if(retrofit2 == null){
            // 커스텀 Gson 인스턴스 생성
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new CustomDateAdapter())
                    .create();

            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(loggingInterceptor)
                    .build();
            retrofit2 = new Retrofit.Builder()
                    .baseUrl(Config.CHATDOMAIN)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // 커스텀 Gson 추가
                    .build();
        }
        return retrofit2;
    }

    public static Retrofit getGoogleMapRetrofitClient(Context context){
        if(googleMapRetrofit == null){
            // 커스텀 Gson 인스턴스 생성
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new CustomDateAdapter())
                    .create();

            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(loggingInterceptor)
                    .build();
            googleMapRetrofit = new Retrofit.Builder()
                    .baseUrl(Config.PLACEDOMAIN)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // 커스텀 Gson 추가
                    .build();
        }
        return googleMapRetrofit;
    }

    public static Retrofit getNaverRetrofitClient(Context context){
        if(naverRetrofit == null){
            // 커스텀 Gson 인스턴스 생성
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new CustomDateAdapter())
                    .create();

            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(loggingInterceptor)
                    .build();
            naverRetrofit = new Retrofit.Builder()
                    .baseUrl("https://openapi.naver.com/")
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // 커스텀 Gson 추가
                    .build();
        }
        return naverRetrofit;
    }
    public static Retrofit getRetrofitInstance() {
        if (emotionRetrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            emotionRetrofit = new Retrofit.Builder()
                    .baseUrl(Config.EMOTIONDOMAIN)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return emotionRetrofit;
    }
}
