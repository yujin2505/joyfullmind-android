package com.yh04.joyfulmindapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.navercorp.nid.NaverIdLoginSDK;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.api.NaverApiService;
import com.yh04.joyfulmindapp.api.UserApi;
import com.yh04.joyfulmindapp.model.NidProfileResponse;
import com.yh04.joyfulmindapp.model.Profile;
import com.yh04.joyfulmindapp.model.User;
import com.yh04.joyfulmindapp.model.UserChange;
import com.yh04.joyfulmindapp.model.UserRes;
import com.yh04.joyfulmindapp.config.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private UserApi userApi;
    private EditText editText;
    private TextView textViewEmail;
    private TextView textViewGender;
    private TextView textViewAge;
    private ImageView imgChangeNickname;
    private String token;
    private String naverAccessToken; // JWT 토큰
    private Uri imageUri;
    private String imageUrl;
    private TextView txtChangePassword;
    private TextView txtLogout;
    private ImageView profileImage;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String DEFAULT_IMAGE = "https://firebasestorage.googleapis.com/v0/b/joyfulmindapp.appspot.com/o/profile_image%2Fdefaultprofileimg.png?alt=media&token=87768af9-03ef-4cc3-b801-ce17b9a1ece1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        userApi = retrofit.create(UserApi.class);

        // JWT 토큰 초기화 (예: 로그인 후 Intent를 통해 전달받은 토큰)
        token = getIntent().getStringExtra("token");
        naverAccessToken = getIntent().getStringExtra("naverAccessToken");

        if (token == null && naverAccessToken == null) {
            // SharedPreferences에서 토큰 가져오기
            SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
            token = sp.getString("token", null);
            naverAccessToken = sp.getString("naverAccessToken", null);
        }

        editText = findViewById(R.id.txtTitle);
        textViewEmail = findViewById(R.id.textViewemail);
        textViewGender = findViewById(R.id.textViewgender);
        textViewAge = findViewById(R.id.textViewage);
        imgChangeNickname = findViewById(R.id.imgChangeNickname);
        txtChangePassword = findViewById(R.id.txtChangePassword);
        txtLogout = findViewById(R.id.txtLogout);
        profileImage = findViewById(R.id.profileImage);

        if (naverAccessToken != null) {
            Log.d("ProfileActivity", "Naver Access Token: " + naverAccessToken);
            getNaverProfileInfo(naverAccessToken);
        } else if (token != null) {
            // 프로필 정보 가져오기
            getUserProfile();
        } else {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Firestore에서 저장된 이미지 URL 가져오기
        getProfileImageUrl();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // 클릭 이벤트 처리
        imgChangeNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 닉네임 수정 활성화
                enableNicknameEditing();
            }
        });

        // 비밀번호 변경 클릭 이벤트 처리
        txtChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                uploadImageAndProceedToChangePassword();
            }
        });

        // 로그아웃 클릭 이벤트 처리
        txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        // 초기에는 닉네임 수정 불가능하게 설정
        editText.setEnabled(false);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
            String email = sp.getString("email", null);

            if (email != null) {
                // 이메일의 특수 문자를 '_'로 변환하여 사용
                String sanitizedEmail = email.replaceAll("[^a-zA-Z0-9]", "_");
                String fileName = System.currentTimeMillis() + ".jpg";
                StorageReference fileReference = storage.getReference().child("profile_images/" + sanitizedEmail + "/" + fileName);

                fileReference.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        imageUrl = uri.toString();
                                        saveImageUrlToFirestore(imageUrl);
                                        loadProfileImage(imageUrl);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "이메일 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        String email = sp.getString("email", null);

        if (email != null) {
            DocumentReference docRef = db.collection("users").document(email);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // 문서가 존재하면 업데이트
                    docRef.update("profileImageUrl", imageUrl)
                            .addOnSuccessListener(aVoid -> Log.d("ProfileActivity", "프로필 이미지 URL이 Firestore에 성공적으로 저장되었습니다."))
                            .addOnFailureListener(e -> Log.e("ProfileActivity", "프로필 이미지 URL 저장 실패: ", e));
                } else {
                    // 문서가 존재하지 않으면 생성
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("profileImageUrl", imageUrl);
                    docRef.set(userData)
                            .addOnSuccessListener(aVoid -> Log.d("ProfileActivity", "프로필 이미지 URL이 Firestore에 성공적으로 저장되었습니다."))
                            .addOnFailureListener(e -> Log.e("ProfileActivity", "프로필 이미지 URL 저장 실패: ", e));
                }
            }).addOnFailureListener(e -> Log.e("ProfileActivity", "프로필 문서를 불러오는데 실패했습니다.", e));
        }
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

    private void uploadImageAndProceedToChangePassword() {
        if (imageUri != null) {
            StorageReference fileReference = storage.getReference().child("profile_images/" + System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl = uri.toString();
                                    saveImageUrlToFirestore(imageUrl);
                                    proceedToChangePasswordActivity(imageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            proceedToChangePasswordActivity(DEFAULT_IMAGE); // 업로드 실패 시 기본 이미지 사용
                        }
                    });
        } else {
            proceedToChangePasswordActivity(DEFAULT_IMAGE); // 이미지가 없을 경우 기본 이미지 사용
        }
    }

    private void proceedToChangePasswordActivity(String imageUrl) {
        Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
        intent.putExtra("imageUrl", imageUrl); // imageUrl 전달
        startActivity(intent);
    }

    private void getUserProfile() {
        if (token == null) {
            Toast.makeText(this, "토큰이 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ProfileActivity", "Token: " + token);
        Call<UserRes> call = userApi.getUserProfile("Bearer " + token);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                Log.d("ProfileActivity", "Response: " + response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().user;
                    editText.setText(user.nickname);
                    textViewEmail.setText(user.email);
                    textViewGender.setText(getGenderString(user.gender));
                    textViewAge.setText(calculateAge(user.birthDate));  // 나이 계산하여 표시

                    // SharedPreferences에 자체 회원 이메일 저장
                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("email", user.email);
                    editor.putString("profileImageUrl", DEFAULT_IMAGE); // 기본 이미지로 초기화
                    editor.apply();

                    // 프로필 이미지 로드
                    getProfileImageUrl();

                } else {
                    Log.d("ProfileActivity", "Response failed: " + response.message());
                    Toast.makeText(ProfileActivity.this, "프로필 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("ProfileActivity", "Request failed: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "프로필 정보를 가져오는 중 오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNaverProfileInfo(String accessToken) {
        Retrofit retrofit = NetworkClient.getNaverRetrofitClient(this);
        NaverApiService apiService = retrofit.create(NaverApiService.class);

        Call<NidProfileResponse> call = apiService.getProfile("Bearer " + accessToken);
        call.enqueue(new Callback<NidProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<NidProfileResponse> call, @NonNull Response<NidProfileResponse> response) {
                if (response.isSuccessful()) {
                    NidProfileResponse profileResponse = response.body();
                    Profile profile = profileResponse.getProfile();

                    String email = profile.getEmail();
                    String name = profile.getName();
                    String nickname = profile.getNickname();
                    String gender = profile.getGender();
                    String age = profile.getAge();
                    String profileImage = profile.getProfileImage();

                    Log.d("ProfileInfo", "Email: " + email);
                    Log.d("ProfileInfo", "Name: " + name);
                    Log.d("ProfileInfo", "Nickname: " + nickname);
                    Log.d("ProfileInfo", "Gender: " + gender);
                    Log.d("ProfileInfo", "Age: " + age);
                    Log.d("ProfileInfo", "Profile Image: " + profileImage);

                    // 프로필 정보를 UI에 설정
                    editText.setText(nickname);
                    textViewEmail.setText(email);
                    textViewGender.setText(gender);
                    textViewAge.setText(age);

                    // SharedPreferences에 이메일과 네이버 프로필 이미지 저장
                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("email", email);
                    editor.putString("profileImageUrl", profileImage);
                    editor.apply();

                    // 프로필 이미지 로드
                    getProfileImageUrl();

                } else {
                    Log.e("ProfileError", "Response message: " + response.message());
                    Log.e("ProfileError", "Response error body: " + response.errorBody().toString());
                    Toast.makeText(ProfileActivity.this, "프로필 가져오기 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NidProfileResponse> call, @NonNull Throwable t) {
                Log.e("ProfileError", "Error message: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "프로필 가져오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String calculateAge(String birthDateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date birthDate = sdf.parse(birthDateString);
            Calendar birthDay = Calendar.getInstance();
            birthDay.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);

            if (today.get(Calendar.MONTH) < birthDay.get(Calendar.MONTH)) {
                age--;
            } else if (today.get(Calendar.MONTH) == birthDay.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) < birthDay.get(Calendar.DAY_OF_MONTH)) {
                age--;
            }

            return String.valueOf(age);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getGenderString(int gender) {
        if (gender == 0) {
            return "남자";
        } else if (gender == 1) {
            return "여자";
        } else {
            return "기타";
        }
    }

    private void enableNicknameEditing() {
        // EditText를 수정 가능하게 설정
        editText.setEnabled(true);
        editText.requestFocus();

        // 닉네임 변경 버튼을 클릭할 때 변경 요청
        imgChangeNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNickname();
            }
        });
    }

    private void changeNickname() {
        String newNickname = editText.getText().toString().trim();

        if (newNickname.isEmpty()) {
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserChange request = new UserChange(newNickname);
        Call<UserRes> call = userApi.changeNickname("Bearer " + token, request);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ProfileActivity", "Nickname change successful: " + response.body().message);
                    Toast.makeText(ProfileActivity.this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    editText.setEnabled(false);  // 닉네임 변경 후 수정 불가능하게 설정

                    // SharedPreferences에 닉네임 저장
                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("userNickname", newNickname);
                    editor.apply();

                    // 닉네임 수정 후 다시 원래 클릭 리스너로 설정
                    imgChangeNickname.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            enableNicknameEditing();
                        }
                    });
                } else {
                    Log.d("ProfileActivity", "Nickname change failed: " + response.message());
                    Toast.makeText(ProfileActivity.this, "닉네임 변경 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("ProfileActivity", "Nickname change error: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "닉네임 변경 중 오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        // 토큰 삭제
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("token");
        editor.remove("naverAccessToken"); // 네이버 토큰도 삭제
        editor.apply();

        // 네이버 로그아웃 처리 (필요한 경우)
        if (naverAccessToken != null) {
            NaverIdLoginSDK.INSTANCE.logout();
        }

        // 로그인 화면으로 이동
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}