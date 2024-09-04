package com.yh04.joyfulmindapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.adapter.PlaceAdapter;
import com.yh04.joyfulmindapp.api.PlaceApi;
import com.yh04.joyfulmindapp.config.Config;
import com.yh04.joyfulmindapp.model.Place;
import com.yh04.joyfulmindapp.model.PlaceList;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PlaceActivity extends AppCompatActivity {

    LocationListener locationListener;
    LocationManager locationManager;

    ImageView imageView2;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    ArrayList<Place> placeArrayList = new ArrayList<>();
    PlaceAdapter adapter;

    double lat;
    double lng;

    String keyword = "공원"; // 공원 키워드로 초기화

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView2 = findViewById(R.id.imageView2);
        progressBar = findViewById(R.id.progressBar); // 프로그래스 바
        recyclerView = findViewById(R.id.recyclerView); // 리사이클러뷰
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(PlaceActivity.this));


        // 위치 관련 설정
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                Log.i("PLACES MAIN", "위도 : " + lat + ", 경도 : " + lng);
                getNetworkData(); // 위치 정보를 받은 후 데이터를 가져옴
            }
        };

        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PlaceActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    -1,
                    10,
                    locationListener); // 위치 업데이트 요청
        }

        progressBar.setVisibility(View.GONE); // 초기에는 프로그래스 바 숨김

        // 검색 키워드를 공원으로 초기화하고 데이터 가져오기
        getNetworkData();
    }

    private void getNetworkData() {
        progressBar.setVisibility(View.VISIBLE); // 데이터를 가져오는 동안 프로그래스 바 보이기

        placeArrayList.clear(); // 장소 리스트 초기화

        Retrofit retrofit = NetworkClient.getGoogleMapRetrofitClient(PlaceActivity.this); // Retrofit 인스턴스 생성

        PlaceApi api = retrofit.create(PlaceApi.class); // PlaceApi 인터페이스 생성

        // 키워드로 장소 목록을 가져오는 API 호출
        Call<PlaceList> call = api.getPlaceList("ko", lat+","+lng, 2000, Config.PLACE_API_KEY, keyword);

        call.enqueue(new Callback<PlaceList>() {
            @Override
            public void onResponse(Call<PlaceList> call, Response<PlaceList> response) {
                progressBar.setVisibility(View.GONE); // 데이터를 가져온 후 프로그래스 바 숨김

                if(response.isSuccessful()){
                    PlaceList placeList = response.body(); // 응답으로 받은 PlaceList 객체
                    placeArrayList.addAll(placeList.results); // 장소 목록에 추가

                    adapter = new PlaceAdapter(PlaceActivity.this, placeArrayList); // 어댑터 생성
                    recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 설정
                } else {
                    // 실패한 경우 처리
                }
            }

            @Override
            public void onFailure(Call<PlaceList> call, Throwable throwable) {
                progressBar.setVisibility(View.GONE); // 실패 시 프로그래스 바 숨김
                throwable.printStackTrace(); // 실패 원인 로그
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) { // 위치 권한 요청 코드
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한 승인 시
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            -1,
                            10,
                            locationListener); // 위치 업데이트 요청
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(PlaceActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}