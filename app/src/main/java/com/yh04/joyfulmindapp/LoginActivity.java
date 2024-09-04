    package com.yh04.joyfulmindapp;

    import android.app.Dialog;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.graphics.Color;
    import android.graphics.drawable.ColorDrawable;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.material.snackbar.Snackbar;
    import com.navercorp.nid.NaverIdLoginSDK;
    import com.navercorp.nid.oauth.OAuthLoginCallback;
    import com.navercorp.nid.oauth.view.NidOAuthLoginButton;
    import com.yh04.joyfulmindapp.adapter.NetworkClient;
    import com.yh04.joyfulmindapp.api.UserApi;
    import com.yh04.joyfulmindapp.config.Config;
    import com.yh04.joyfulmindapp.model.User;
    import com.yh04.joyfulmindapp.model.UserRes;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;
    import retrofit2.Retrofit;

    import java.util.regex.Pattern;

    public class LoginActivity extends AppCompatActivity {

        EditText editEmail;
        EditText editPassword;
        ImageView ImgLogin;
        TextView txtRegister;
        Dialog dialog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            // 액션바 이름 변경
            getSupportActionBar().setTitle(" ");

            //뷰 객체 연결
            NidOAuthLoginButton btnNaverLogin = findViewById(R.id.btn_login);

            //네아로 객체 초기화
            NaverIdLoginSDK.INSTANCE.initialize(this, getString(R.string.naver_client_id),
                    getString(R.string.naver_client_secret), getString(R.string.app_name));

            editEmail = findViewById(R.id.editEmail);
            editPassword = findViewById(R.id.editPassword);
            ImgLogin = findViewById(R.id.ImgLogin);
            txtRegister = findViewById(R.id.txtRegister);

            // 로그인 버튼 클릭 리스너 설정
            btnNaverLogin.setOAuthLogin(new OAuthLoginCallback() {
                @Override
                public void onSuccess() {
                    // 로그인 성공 시
                    String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken();
                    Log.d("LoginActivity", "Received Access Token: " + accessToken);

                    // 액세스 토큰을 SharedPreferences에 저장
                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("naverAccessToken", accessToken);
                    editor.apply();

                    // 메인 액티비티로 이동
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("naverAccessToken", accessToken);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(int httpStatus, @NonNull String message) {
                    // 로그인 실패 시
                    Log.e("LoginActivity", "Login failed: " + message);
                    Toast.makeText(LoginActivity.this, "로그인 실패: " + message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int errorCode, @NonNull String message) {
                    // 로그인 에러 발생 시
                    Log.e("LoginActivity", "Login error: " + message);
                    Toast.makeText(LoginActivity.this, "로그인 에러: " + message, Toast.LENGTH_SHORT).show();
                }
            });

            ImgLogin.setOnClickListener(v -> {
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                // 이메일과 패스워드는 필수다.
                if (email.isEmpty() || password.isEmpty()) {
                    Snackbar.make(ImgLogin,
                            "필수 항목입니다. 모두 입력하세요.",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식 체크
                Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
                if (!pattern.matcher(email).matches()) {
                    Snackbar.make(ImgLogin,
                            "이메일 형식을 바르게 작성하세요.",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                showProgress();

                // 레트로핏 라이브러리를 이용해서 네트워크 호출한다.
                Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);
                UserApi api = retrofit.create(UserApi.class);
                User user = new User(email, password);

                Call<UserRes> call = api.login(user);
                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        dismissProgress();

                        if (response.isSuccessful()) {
                            UserRes userRes = response.body();

                            SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", userRes.accessToken);
                            editor.putString("email", email); // 이메일 저장
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("token", userRes.accessToken);
                            startActivity(intent);

                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, "로그인 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable throwable) {
                        dismissProgress();
                    }
                });
            });

            txtRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            });
        }

        private void dismissProgress() {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        void showProgress() {
            dialog = new Dialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(new ProgressBar(this));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }
