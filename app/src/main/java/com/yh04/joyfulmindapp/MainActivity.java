package com.yh04.joyfulmindapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.navercorp.nid.NaverIdLoginSDK;
import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.adapter.ViewPager2Adapter;
import com.yh04.joyfulmindapp.api.NaverApiService;
import com.yh04.joyfulmindapp.api.UserApi;
import com.yh04.joyfulmindapp.config.Config;
import com.yh04.joyfulmindapp.databinding.ActivityMainBinding;
import com.yh04.joyfulmindapp.model.NidProfileResponse;
import com.yh04.joyfulmindapp.model.Profile;
import com.yh04.joyfulmindapp.model.User;
import com.yh04.joyfulmindapp.model.UserRes;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    // 뷰페이저2 인스턴스
    private ViewPager2 viewPager2;
    // 뷰페이저2를 보여줄 어댑터
    private ViewPager2Adapter adapter;

    TextView tvBtn1;
    TextView tvBtn2;
    TextView tvBtn3;
    TextView tvBtn4;
    TextView txtUserName;

    ImageView profileImage;

    LinearLayout profileLayout;

    private ActivityMainBinding binding;
    private UserApi userApi;
    private String token;  // JWT 토큰
    private String naverAccessToken;
    private String nickname;
    private FirebaseFirestore db;

    // 액션바의 로그아웃 버튼을 활성화시킴
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");

        // SharedPreferences에서 토큰과 네이버 액세스 토큰 가져오기
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        token = sp.getString("token", null);
        naverAccessToken = sp.getString("naverAccessToken", null);

        if (naverAccessToken == null && token == null) {
            // 토큰이 없는 경우 로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        tvBtn1 = findViewById(R.id.textView0);
        tvBtn2 = findViewById(R.id.textView1);
        tvBtn3 = findViewById(R.id.textView2);
        tvBtn4 = findViewById(R.id.textView3);
        txtUserName = findViewById(R.id.txtUserName);
        profileImage = findViewById(R.id.profileImage);

        profileLayout = findViewById(R.id.profileLayout);

        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // 뷰페이저2 어댑터 설정
        viewPager2 = findViewById(R.id.viewPager2);
        adapter = new ViewPager2Adapter(this); // 어댑터 생성
        viewPager2.setAdapter(adapter);

        // 페이지가 변경될 때마다 호출되는 콜백 함수
        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                CurrentPositionChk(position);
            }
        });

        // 첫번째 텍스트뷰 클릭 이벤트 : ViewPager2에서 Fragment1의 화면으로 이동
        tvBtn1.setOnClickListener(v -> viewPager2.setCurrentItem(0));

        // 두번째 텍스트뷰 클릭 이벤트 : ViewPager2에서 Fragment2의 화면으로 이동
        tvBtn2.setOnClickListener(v -> viewPager2.setCurrentItem(1));

        // 세번째 텍스트뷰 클릭 이벤트 : ViewPager2에서 Fragment3의 화면으로 이동
        tvBtn3.setOnClickListener(v -> viewPager2.setCurrentItem(2));

        // 네번째 텍스트뷰 클릭 이벤트 : ViewPager2에서 Fragment4의 화면으로 이동
        tvBtn4.setOnClickListener(v -> viewPager2.setCurrentItem(3));

        // Retrofit 인스턴스 초기화
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        userApi = retrofit.create(UserApi.class);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        if (naverAccessToken != null) {
            getProfileInfo(naverAccessToken);
        } else if (token != null) {
            getUserProfile(token);
        }
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.defaultprofileimg) // 이미지 로딩 중에 표시할 임시 이미지
                    .error(R.drawable.defaultprofileimg) // 이미지 로딩 실패 시 표시할 이미지
                    .centerCrop() // 이미지가 ImageView를 꽉 채우도록 설정
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.defaultprofileimg); // 저장된 이미지 URL이 없을 경우 기본 이미지 사용
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // SharedPreferences에서 닉네임 불러오기
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        String nickname = sp.getString("userNickname", "DefaultNickname");
        txtUserName.setText(nickname + "님");

        // Firestore에서 프로필 이미지 다시 가져오기
        String email = sp.getString("email", null);
        fetchProfileImageFromFirestore(email);
    }

    private void fetchProfileImageFromFirestore(String email) {
        if (email != null) {
            db.collection("users").document(email).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            loadProfileImage(imageUrl);

                            // SharedPreferences에 저장
                            SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("profileImageUrl", imageUrl);
                            editor.apply();
                        } else {
                            Log.e("ProfileError", "프로필 이미지를 불러오는데 실패했습니다.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ProfileError", "프로필 이미지를 불러오는데 실패했습니다.", e));
        }
    }

    private void CurrentPositionChk(int position) {
        // 프래그먼트 화면 인덱스 저장 함수
        int currentFragmentId = 0;

        // 페이지 인덱스 정보 저장
        if (currentFragmentId != position) {
            currentFragmentId = position;
        }

        // 텍스트뷰 버튼 객체 연결 : tvBtn1, tvBtn2, tvBtn3, tvBtn4
        tvBtn1 = findViewById(R.id.textView0);
        tvBtn2 = findViewById(R.id.textView1);
        tvBtn3 = findViewById(R.id.textView2);
        tvBtn4 = findViewById(R.id.textView3);

        // 텍스트뷰 버튼 색상 정의 : 현재 페이지의 위치를 구분하기 위해 선택된 페이지의 투명도를 더 높게 설정
        String BaseHexColor = "#F6BC78";
        int rgbColor = Color.parseColor(BaseHexColor);
        int selectedAlpha = 128;
        int deselectedAlpha = 25;

        // 첫번째 화면일 경우 왼쪽 버튼 활성화 표시 (배경색을 진하게 함)
        if (currentFragmentId == 0) {
            tvBtn1.setBackgroundColor(
                    Color.argb(selectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn2.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn3.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn4.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
        }
        // 두번째 화면일 경우 가운데 버튼 활성화 표시 (배경색을 진하게 함)
        else if (currentFragmentId == 1) {
            tvBtn1.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn2.setBackgroundColor(
                    Color.argb(selectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn3.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn4.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
        }
        // 세번째 화면일 경우 오른쪽 버튼 활성화 표시 (배경색을 진하게 함)
        else if (currentFragmentId == 2) {
            tvBtn1.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn2.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn3.setBackgroundColor(
                    Color.argb(selectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn4.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
        }
        // 네번째 화면일 경우 오른쪽 버튼 활성화 표시 (배경색을 진하게 함)
        else if (currentFragmentId == 3) {
            tvBtn1.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn2.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn3.setBackgroundColor(
                    Color.argb(deselectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
            tvBtn4.setBackgroundColor(
                    Color.argb(selectedAlpha, Color.red(rgbColor), Color.green(rgbColor), Color.blue(rgbColor))
            );
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("로그아웃하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        logout();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 아무일도 하지 않음
                    }
                });
        builder.create().show();
    }

    private void logout() {
        try {
            // NaverIdLoginSDK 초기화 확인
            if (!NaverIdLoginSDK.INSTANCE.isInitialized()) {
                NaverIdLoginSDK.INSTANCE.initialize(this, getString(R.string.naver_client_id),
                        getString(R.string.naver_client_secret), getString(R.string.app_name));
            }

            // 네이버 로그인 SDK 로그아웃
            NaverIdLoginSDK.INSTANCE.logout();

            // SharedPreferences에서 토큰 삭제
            SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("naverAccessToken");
            editor.remove("token");
            editor.apply();

            // 로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("MainActivity", "Error logging out", e);
        }
    }

    private void getProfileInfo(String accessToken) {
        Retrofit retrofit = NetworkClient.getNaverRetrofitClient(this);
        NaverApiService apiService = retrofit.create(NaverApiService.class);

        Call<NidProfileResponse> call = apiService.getProfile("Bearer " + accessToken);
        call.enqueue(new Callback<NidProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<NidProfileResponse> call, @NonNull Response<NidProfileResponse> response) {
                if (response.isSuccessful()) {
                    NidProfileResponse profileResponse = response.body();
                    Profile profile = profileResponse.getProfile();

                    nickname = profile.getNickname();

                    Log.d("ProfileInfo", "Nickname: " + nickname);

                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("userNickname", nickname);
                    editor.putString("email", profile.getEmail()); // 이메일 저장
                    editor.apply();

                    txtUserName.setText(nickname + "님");
                } else {
                    Log.e("ProfileError", "Response message: " + response.message());
                    Log.e("ProfileError", "Response error body: " + response.errorBody().toString());
                    Toast.makeText(MainActivity.this, "프로필 가져오기 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NidProfileResponse> call, @NonNull Throwable t) {
                Log.e("ProfileError", "Error message: " + t.getMessage());
                Toast.makeText(MainActivity.this, "프로필 가져오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserProfile(String token) {
        Call<UserRes> call = userApi.getUserProfile("Bearer " + token);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().user;
                    nickname = user.nickname;
                    Log.d("ProfileInfo", "User Nickname: " + nickname);

                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("userNickname", nickname);
                    editor.putString("email", user.email); // 이메일 저장
                    editor.apply();

                    txtUserName.setText(nickname + "님");
                } else {
                    Log.d("ProfileError", "Response failed: " + response.message());
                    Toast.makeText(MainActivity.this, "프로필 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("ProfileError", "Request failed: " + t.getMessage());
                Toast.makeText(MainActivity.this, "프로필 정보를 가져오는 중 오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 프로필 수정하기 클릭 리스너
    public void onProfileEditClick(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        if (naverAccessToken != null) {
            intent.putExtra("naverAccessToken", naverAccessToken);
        }
        if (token != null) {
            intent.putExtra("token", token);
        }
        startActivity(intent);
    }
}
