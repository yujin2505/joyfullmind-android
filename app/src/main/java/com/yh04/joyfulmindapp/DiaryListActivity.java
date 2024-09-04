package com.yh04.joyfulmindapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.yh04.joyfulmindapp.adapter.DiaryAdapter;
import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.api.DiaryApi;
import com.yh04.joyfulmindapp.config.Config;
import com.yh04.joyfulmindapp.model.Diary;
import com.yh04.joyfulmindapp.model.DiaryResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DiaryListActivity extends AppCompatActivity {
    private static final String TAG = "DiaryListActivity";

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private DiaryAdapter adapter;
    private List<Diary> diaryList = new ArrayList<>();
    private String token;
    private TextView txtDateRange;
    private Button btnCalendar;
    private FloatingActionButton btnAdd;
    private View expandableLayout;

    private static final int REQUEST_CODE_ADD_DIARY = 1;
    private static final int REQUEST_CODE_EDIT_DIARY = 2; // 일기 수정에 대한 요청 코드 추가
    private CalendarDay selectedStartDate;
    private CalendarDay selectedEndDate;
    private CalendarDay previousSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        calendarView = findViewById(R.id.cvCalendar);
        recyclerView = findViewById(R.id.recyclerView);
        txtDateRange = findViewById(R.id.txtDateRange);
        btnCalendar = findViewById(R.id.btnCalendar);
        btnAdd = findViewById(R.id.btnAdd);
        expandableLayout = findViewById(R.id.ExpandableLayout);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        token = getTokenFromSharedPreferences();
        adapter = new DiaryAdapter(diaryList, token);
        recyclerView.setAdapter(adapter);

        calendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView widget, @NonNull List<CalendarDay> dates) {
                if (!dates.isEmpty()) {
                    selectedStartDate = dates.get(0);
                    selectedEndDate = dates.get(dates.size() - 1);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String start = sdf.format(new Date(selectedStartDate.getYear() - 1900, selectedStartDate.getMonth() - 1, selectedStartDate.getDay()));
                    String end = sdf.format(new Date(selectedEndDate.getYear() - 1900, selectedEndDate.getMonth() - 1, selectedEndDate.getDay()));

                    txtDateRange.setText(start + " ~ " + end);
                    fetchDiariesForDateRange(selectedStartDate, selectedEndDate);

                    // 날짜 범위 선택 후 캘린더 접기
                    expandableLayout.setVisibility(View.GONE);
                }
            }
        });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (previousSelectedDate != null && previousSelectedDate.equals(date)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String selectedDate = sdf.format(new Date(date.getYear() - 1900, date.getMonth() - 1, date.getDay()));

                    txtDateRange.setText(selectedDate);
                    fetchDiariesForDateRange(date, date);

                    // 같은 날짜를 두 번 선택하면 캘린더 접기
                    expandableLayout.setVisibility(View.GONE);
                }
                previousSelectedDate = date;
            }
        });

        btnCalendar.setOnClickListener(v -> {
            expandableLayout.setVisibility(expandableLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(DiaryListActivity.this, EditDiaryActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_DIARY);
        });

        // 처음에 오늘 날짜의 일기를 가져옵니다.
        fetchDiariesForToday();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 날짜에 맞는 일기를 가져오고, 날짜가 선택되어 있지 않으면 전체 일기를 가져옵니다
        if (selectedStartDate != null || selectedEndDate != null) {
            fetchDiariesForDateRange(selectedStartDate, selectedEndDate);
        } else {
            fetchAllDiaries();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_DIARY && resultCode == RESULT_OK) {
            // 일기 작성 후에는 오늘 날짜의 일기를 가져옵니다.
            fetchDiariesForToday();
        } else if (requestCode == REQUEST_CODE_EDIT_DIARY && resultCode == RESULT_OK) {
            // 일기 수정 후에는 날짜에 맞는 일기를 가져오고, 날짜가 선택되어 있지 않으면 전체 일기를 가져옵니다
            if (selectedStartDate != null || selectedEndDate != null) {
                fetchDiariesForDateRange(selectedStartDate, selectedEndDate);
            } else {
                fetchAllDiaries();
            }
        }
    }

    private void fetchDiariesForToday() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH는 0부터 시작하므로 1을 더합니다.
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDay todayDate = CalendarDay.from(year, month, day);
        fetchDiariesForDateRange(todayDate, todayDate);
    }

    private void fetchAllDiaries() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        DiaryApi diaryApi = retrofit.create(DiaryApi.class);
        Call<DiaryResponse> call = diaryApi.getAllDiaries("Bearer " + token);

        call.enqueue(new Callback<DiaryResponse>() {
            @Override
            public void onResponse(Call<DiaryResponse> call, Response<DiaryResponse> response) {
                if (response.isSuccessful()) {
                    diaryList = response.body().getItems();
                    sortAndDisplayDiaries();
                } else {
                    Log.e(TAG, "Failed to fetch diaries: " + response.code());
                    Toast.makeText(DiaryListActivity.this, "Failed to fetch diaries", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DiaryResponse> call, Throwable t) {
                Log.e(TAG, "An error occurred: " + t.getMessage(), t);
                Toast.makeText(DiaryListActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDiariesForDateRange(CalendarDay startDate, CalendarDay endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 시작 날짜와 끝 날짜가 지정된 경우 해당 날짜를 사용
        Calendar startCal = Calendar.getInstance();
        startCal.set(startDate.getYear(), startDate.getMonth() - 1, startDate.getDay());
        String start = sdf.format(startCal.getTime());

        Calendar endCal = Calendar.getInstance();
        endCal.set(endDate.getYear(), endDate.getMonth() - 1, endDate.getDay());
        // 날짜 범위의 끝을 포함하려면 하루를 더해야 합니다
        endCal.add(Calendar.DAY_OF_MONTH, 1);
        String end = sdf.format(endCal.getTime());

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        DiaryApi diaryApi = retrofit.create(DiaryApi.class);
        Call<DiaryResponse> call = diaryApi.getDiariesForRange("Bearer " + token, start, end);

        call.enqueue(new Callback<DiaryResponse>() {
            @Override
            public void onResponse(Call<DiaryResponse> call, Response<DiaryResponse> response) {
                if (response.isSuccessful()) {
                    diaryList = response.body().getItems();
                    sortAndDisplayDiaries();
                } else {
                    Log.e(TAG, "Failed to fetch diaries for date range: " + response.code());
                    Toast.makeText(DiaryListActivity.this, "Failed to fetch diaries for selected date range", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DiaryResponse> call, Throwable t) {
                Log.e(TAG, "An error occurred: " + t.getMessage(), t);
                Toast.makeText(DiaryListActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortAndDisplayDiaries() {
        Collections.sort(diaryList, new Comparator<Diary>() {
            @Override
            public int compare(Diary o1, Diary o2) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
        });
        adapter.setDiaryList(diaryList);
        adapter.notifyDataSetChanged();
    }

    private String getTokenFromSharedPreferences() {
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        return sp.getString("token", "");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}