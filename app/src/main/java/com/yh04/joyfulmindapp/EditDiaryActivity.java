package com.yh04.joyfulmindapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.api.DiaryApi;
import com.yh04.joyfulmindapp.config.Config;
import com.yh04.joyfulmindapp.model.Diary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditDiaryActivity extends AppCompatActivity {

    private TextView txtCreatedAt;
    private EditText txtTitle;
    private EditText txtContent;
    private ImageView imgSave;
    private Diary diary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_diary);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtCreatedAt = findViewById(R.id.txtCreatedAt);
        txtTitle = findViewById(R.id.txtTitle);
        txtContent = findViewById(R.id.txtContent);
        imgSave = findViewById(R.id.imgSave);

        // 다이어리 객체를 인텐트로부터 가져옴
        diary = (Diary) getIntent().getSerializableExtra("diary");

        if (diary != null) {
            // 기존 일기를 편집하는 경우
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(diary.getCreatedAt());

            txtCreatedAt.setText(formattedDate);
            txtTitle.setText(diary.getTitle());
            txtContent.setText(diary.getContent());
        } else {
            // 새로운 일기를 작성하는 경우
            txtCreatedAt.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date()));
            txtTitle.setText("");
            txtContent.setText("");
        }

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtTitle.getText().toString().isEmpty() || txtContent.getText().toString().isEmpty()) {
                    Toast.makeText(EditDiaryActivity.this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (diary == null) {
                    // 새로운 일기 생성
                    Diary newDiary = new Diary();
                    newDiary.setTitle(txtTitle.getText().toString());
                    newDiary.setContent(txtContent.getText().toString());
                    createDiary(newDiary);
                } else {
                    // 기존 일기 업데이트
                    diary.setTitle(txtTitle.getText().toString());
                    diary.setContent(txtContent.getText().toString());
                    updateDiary(diary);
                }
            }
        });
    }

    private void createDiary(Diary newDiary) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        DiaryApi diaryApi = retrofit.create(DiaryApi.class);
        String token = getTokenFromSharedPreferences();
        Call<Void> call = diaryApi.createDiary("Bearer " + token, newDiary);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditDiaryActivity.this, "새 일기가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditDiaryActivity.this, "일기 생성을 실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditDiaryActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDiary(Diary diary) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        DiaryApi diaryApi = retrofit.create(DiaryApi.class);
        String token = getTokenFromSharedPreferences();
        Call<Void> call = diaryApi.updateDiary("Bearer " + token, diary.getId(), diary);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditDiaryActivity.this, "일기가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditDiaryActivity.this, "수정을 실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditDiaryActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getTokenFromSharedPreferences() {
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        return sp.getString("token", "");
    }
}