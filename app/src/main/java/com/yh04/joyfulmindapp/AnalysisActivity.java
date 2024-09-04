package com.yh04.joyfulmindapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.yh04.joyfulmindapp.config.Config;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalysisActivity extends AppCompatActivity {

    private static final String TAG = "AnalysisActivity";
    private FirebaseFirestore db;
    private String email;
    private MaterialCalendarView calendarView;
    private CalendarDay selectedStartDate;
    private CalendarDay selectedEndDate;
    private String chatData;
    private ImageView imgDetail;
    private TextView txtDateRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();

        // SharedPreferences에서 이메일 가져오기
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        email = sp.getString("email", null);

        calendarView = findViewById(R.id.cvCalendar);
        imgDetail = findViewById(R.id.imgDetail);
        txtDateRange = findViewById(R.id.txtDateRange);

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
                    fetchChatData(start, end, email);
                }
            }
        });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String selectedDate = sdf.format(new Date(date.getYear() - 1900, date.getMonth() - 1, date.getDay()));

                txtDateRange.setText(selectedDate);
                fetchChatData(selectedDate, selectedDate, email);
            }
        });

        imgDetail.setOnClickListener(v -> {
            if (chatData != null && !chatData.isEmpty()) {
                Intent intent = new Intent(AnalysisActivity.this, DetailAnalysisActivity.class);
                intent.putExtra("chatData", chatData);
                startActivity(intent);
            } else {
                showSnackbar("데이터를 가져오는 중 오류가 발생했습니다.");
            }
        });
    }

    private void fetchChatData(String startDate, String endDate, String email) {
        Calendar startCalendar = Calendar.getInstance();
        String[] startDateParts = startDate.split("-");
        startCalendar.set(Calendar.YEAR, Integer.parseInt(startDateParts[0]));
        startCalendar.set(Calendar.MONTH, Integer.parseInt(startDateParts[1]) - 1);
        startCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startDateParts[2]));
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        String[] endDateParts = endDate.split("-");
        endCalendar.set(Calendar.YEAR, Integer.parseInt(endDateParts[0]));
        endCalendar.set(Calendar.MONTH, Integer.parseInt(endDateParts[1]) - 1);
        endCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(endDateParts[2]));
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);

        db.collection("UserChattingTest")
                .whereEqualTo("email", email)
                .whereGreaterThanOrEqualTo("timestamp", startCalendar.getTime())
                .whereLessThanOrEqualTo("timestamp", endCalendar.getTime())
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            chatData = null;
                        } else {
                            StringBuilder chatDataBuilder = new StringBuilder();
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                String message = document.getString("message");
                                chatDataBuilder.append(message).append("\n");
                            }
                            chatData = chatDataBuilder.toString();
                        }
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showSnackbar("데이터를 가져오는 중 오류가 발생했습니다.");
                        chatData = null;
                    }
                });
    }

    private void showSnackbar(String message) {
        try {
            View view = findViewById(android.R.id.content);
            if (view != null) {
                Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "No suitable parent found from the given view. Please provide a valid view.");
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Snackbar 오류: " + e.getMessage());
        }
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
