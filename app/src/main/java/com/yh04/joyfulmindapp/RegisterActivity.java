package com.yh04.joyfulmindapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.api.UserApi;
import com.yh04.joyfulmindapp.config.Config;
import com.yh04.joyfulmindapp.model.User;
import com.yh04.joyfulmindapp.model.UserRes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editPassword;
    EditText editNickname;
    EditText editBirthDate;
    ImageView imgMale;
    ImageView imgFemale;
    ImageView imgRegister;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editNickname = findViewById(R.id.editNickname);
        editBirthDate = findViewById(R.id.editBirthDate);
        imgMale = findViewById(R.id.imgMale);
        imgFemale = findViewById(R.id.imgFemale);
        imgRegister = findViewById(R.id.imgRegister);

        editBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        imgRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String nickname = editNickname.getText().toString().trim();
                String birthDate = editBirthDate.getText().toString().trim();

                // 필수 항목 체크
                if (email.isEmpty() || password.isEmpty() || nickname.isEmpty() || birthDate.isEmpty()) {
                    Snackbar.make(imgRegister, "필수 항목입니다. 모두 입력하세요.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식 체크
                Pattern pattern = Patterns.EMAIL_ADDRESS;
                if (!pattern.matcher(email).matches()) {
                    Snackbar.make(imgRegister, "이메일 형식을 바르게 작성하세요.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // 나이 계산
                int age = calculateAge(birthDate);
                if (age < 0) {
                    Snackbar.make(imgRegister, "생년월일을 올바르게 입력하세요.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // 성별 체크
                int gender = (imgMale.isSelected()) ? 0 : 1;

                showProgress();

                Retrofit retrofit = NetworkClient.getRetrofitClient(RegisterActivity.this);
                UserApi api = retrofit.create(UserApi.class);
                User user = new User(email, password, nickname, birthDate, gender);
                Call<UserRes> call = api.register(user);
                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        dismissProgress();

                        if (response.isSuccessful()) {
                            UserRes userRes = response.body();

                            SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", userRes.accessToken);
                            editor.apply();
                            editor.commit();

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Snackbar.make(imgRegister, "회원가입에 실패했습니다. 다시 시도하세요.", Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable throwable) {
                        dismissProgress();
                        Snackbar.make(imgRegister, "네트워크 오류가 발생했습니다. 다시 시도하세요.", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        imgMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgMale.setSelected(true);
                imgFemale.setSelected(false);
                imgMale.setImageResource(R.drawable.clickmale);
                imgFemale.setImageResource(R.drawable.female);
            }
        });

        imgFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgMale.setSelected(false);
                imgFemale.setSelected(true);
                imgMale.setImageResource(R.drawable.male);
                imgFemale.setImageResource(R.drawable.clickfemale);
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String birthDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                editBirthDate.setText(birthDate);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private int calculateAge(String birthDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Calendar birth = Calendar.getInstance();
            birth.setTime(sdf.parse(birthDate));
            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (ParseException e) {
            e.printStackTrace();
            Snackbar.make(editBirthDate, "생년월일 형식이 잘못되었습니다.", Snackbar.LENGTH_SHORT).show();
            return -1;
        }
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}