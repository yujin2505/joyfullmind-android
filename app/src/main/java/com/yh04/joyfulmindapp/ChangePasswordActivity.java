package com.yh04.joyfulmindapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.api.UserApi;
import com.yh04.joyfulmindapp.model.UserChange;
import com.yh04.joyfulmindapp.model.UserRes;
import com.yh04.joyfulmindapp.config.Config;

import org.checkerframework.checker.nullness.qual.NonNull;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChangePasswordActivity extends AppCompatActivity {

    private UserApi userApi;
    private EditText currentPassword;
    private EditText newPassword;
    private EditText newPasswordCheck;
    private ImageView imgSave;
    private CircleImageView profileImage;

    private String token;  // JWT 토큰
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private static final String DEFAULT_IMAGE = "https://firebasestorage.googleapis.com/v0/b/joyfulmindapp.appspot.com/o/profile_image%2Fdefaultprofileimg.png?alt=media&token=87768af9-03ef-4cc3-b801-ce17b9a1ece1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        userApi = retrofit.create(UserApi.class);
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        // SharedPreferences에서 토큰 가져오기
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        token = sp.getString("token", null);

        currentPassword = findViewById(R.id.currentPassword);
        newPassword = findViewById(R.id.newPassword);
        newPasswordCheck = findViewById(R.id.newPasswordCheck);
        imgSave = findViewById(R.id.imgSave);

        profileImage = findViewById(R.id.profileImage);

        // Firestore에서 저장된 프로필 이미지 URL 가져오기
        getProfileImageUrl();

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void getProfileImageUrl() {
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        String email = sp.getString("email", null);

        if (email != null) {
            DocumentReference docRef = db.collection("users").document(email);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String savedImageUrl = documentSnapshot.getString("profileImageUrl");
                    if (savedImageUrl != null) {
                        loadProfileImage(savedImageUrl);
                    } else {
                        loadProfileImage(DEFAULT_IMAGE);
                    }
                } else {
                    loadProfileImage(DEFAULT_IMAGE);
                }
            }).addOnFailureListener(e -> {
                loadProfileImage(DEFAULT_IMAGE);
            });
        } else {
            loadProfileImage(DEFAULT_IMAGE);
        }
    }

    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.defaultprofileimg) // 이미지 로딩 중에 표시할 임시 이미지
                .error(R.drawable.defaultprofileimg) // 이미지 로딩 실패 시 표시할 이미지
                .centerCrop() // 이미지가 ImageView를 꽉 채우도록 설정
                .into(profileImage);
    }

    private void changePassword() {
        String currentPwd = currentPassword.getText().toString().trim();
        String newPwd = newPassword.getText().toString().trim();
        String newPwdCheck = newPasswordCheck.getText().toString().trim();

        if (currentPwd.isEmpty() || newPwd.isEmpty() || newPwdCheck.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPwd.equals(newPwdCheck)) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPwd.length() < 4 || newPwd.length() > 12) {
            Toast.makeText(this, "비밀번호는 4자 이상 12자 이하로 설정해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserChange request = new UserChange(currentPwd, newPwd);
        Call<UserRes> call = userApi.changePassword("Bearer " + token, request);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ChangePasswordActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();  // 비밀번호 변경 후 액티비티 종료
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "비밀번호 변경 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "비밀번호 변경 중 오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(ChangePasswordActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}