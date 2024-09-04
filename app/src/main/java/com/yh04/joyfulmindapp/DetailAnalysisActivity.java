package com.yh04.joyfulmindapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.api.SentimentAnalysisService;
import com.yh04.joyfulmindapp.model.SentimentRequest;
import com.yh04.joyfulmindapp.model.SentimentResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class DetailAnalysisActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private PieChart pieChart;
    private HorizontalBarChart barChart;
    private Map<String, Integer> emotionCountMap = new HashMap<>(); // 감정 카운트를 위한 맵
    private Map<String, String> emotionMapping = new HashMap<>(); // 감정 매핑

    // 색상 변수 설정
    private final int colorFear = Color.parseColor("#ff9999"); // Light pastel red
    private final int colorDisgust = Color.parseColor("#ffba85"); // Lighter pastel yellow
    private final int colorSurprise = Color.parseColor("#8fd9b6"); // Light pastel green
    private final int colorSadness = Color.parseColor("#d395d0"); // Light pastel purple
    private final int colorAnger = Color.parseColor("#ffcccb"); // Light pastel pink
    private final int colorHappiness = Color.parseColor("#c0d6e4"); // Light pastel blue

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_analysis);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        LinearLayout song = findViewById(R.id.song);

        // 감정 매핑 테이블 초기화
        initializeEmotionMapping();

        // imgSong 클릭 리스너 설정
        song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 가장 높은 감정을 찾기
                String highestEmotion = getHighestEmotion();
                String mappedEmotion = emotionMapping.get(highestEmotion);

                if (mappedEmotion != null) {
                    Log.i("DetailAnalysisActivity", "Mapped Emotion: " + mappedEmotion);
                    // SongRecActivity로 이동
                    Intent intent = new Intent(DetailAnalysisActivity.this, SongRecActivity.class);
                    // 감정 분석 결과를 SongRecActivity로 전달
                    intent.putExtra("emotion", mappedEmotion);
                    startActivity(intent);
                } else {
                    Log.e("DetailAnalysisActivity", "No valid emotion found. Highest Emotion: " + highestEmotion);
                }
            }
        });

        String chatData = getIntent().getStringExtra("chatData");

        if (chatData != null) {
            showProgress(); // 여기서 프로그레스바를 보여줌
            analyzeSentiments(chatData.split("\n"));
        }
    }

    private void initializeEmotionMapping() {
        emotionMapping.put("fear", "공포");
        emotionMapping.put("disgust", "극혐");
        emotionMapping.put("surprise", "놀람");
        emotionMapping.put("sadness", "슬픔");
        emotionMapping.put("angry", "분노");
        emotionMapping.put("happiness", "기쁨");
    }

    private String getHighestEmotion() {
        return emotionCountMap.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("neutral"))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null); // 기본값으로 null
    }

    private void analyzeSentiments(String[] messages) {
        for (String message : messages) {
            analyzeSentiment(message);
        }
    }

    private void analyzeSentiment(String text) {
        if (text == null || text.isEmpty()) {
            Log.e("DetailAnalysisActivity", "Text is null or empty");
            return;
        }

        SentimentRequest request = new SentimentRequest(text);
        SentimentAnalysisService service = NetworkClient.getRetrofitInstance().create(SentimentAnalysisService.class);
        Call<SentimentResponse> call = service.getSentimentAnalysis(request);

        call.enqueue(new Callback<SentimentResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<SentimentResponse> call, Response<SentimentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Float> predictions = response.body().getPrediction();
                    if (predictions != null) {
                        for (Map.Entry<String, Float> entry : predictions.entrySet()) {
                            emotionCountMap.put(entry.getKey(), emotionCountMap.getOrDefault(entry.getKey(), 0) + entry.getValue().intValue());
                        }
                        displayCharts();
                    } else {
                        Log.e("DetailAnalysisActivity", "Predictions map is null");
                    }
                } else {
                    Log.e("DetailAnalysisActivity", "Error: " + response.message());
                    Log.e("DetailAnalysisActivity", "Error code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("DetailAnalysisActivity", "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e("DetailAnalysisActivity", "Error parsing error body", e);
                        }
                    }
                }
                dismissProgress(); // 여기서 프로그레스바를 닫음
            }

            @Override
            public void onFailure(Call<SentimentResponse> call, Throwable t) {
                Log.e("DetailAnalysisActivity", "API call failed: " + t.getMessage(), t);
                dismissProgress(); // 실패 시에도 프로그레스바를 닫음
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void displayCharts() {
        if (emotionCountMap.isEmpty()) {
            Log.e("DetailAnalysisActivity", "No emotion data available to display in charts.");
            return;
        }

        // 감정 수치 총합 계산 (neutral 포함)
        int totalCount = emotionCountMap.values().stream().mapToInt(Integer::intValue).sum();

        List<PieEntry> pieEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        String[] emotions = {"fear", "disgust", "surprise", "sadness", "angry", "happiness"};

        // 감정 수치를 퍼센트로 변환 (neutral 제외하고 나머지 감정들로 100% 비율 맞추기)
        Map<String, Float> emotionPercentageMap = new HashMap<>();
        int totalNonNeutralCount = emotionCountMap.entrySet().stream()
                .filter(entry -> !"neutral".equals(entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();

        for (Map.Entry<String, Integer> entry : emotionCountMap.entrySet()) {
            if (!"neutral".equals(entry.getKey())) {
                emotionPercentageMap.put(entry.getKey(), (entry.getValue() / (float) totalNonNeutralCount) * 100);
            }
        }

        // 상위 3개의 감정을 파이 차트에 추가 (neutral 제외)
        emotionPercentageMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // 0% 제외
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
                    colors.add(getColorForEmotion(entry.getKey()));
                });

        // 감정이 1개 또는 2개인 경우에도 해당 감정만 표시
        if (pieEntries.isEmpty()) {
            emotionPercentageMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0) // 0% 제외
                    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                    .forEach(entry -> {
                        pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
                        colors.add(getColorForEmotion(entry.getKey()));
                    });
        }

        // 모든 감정을 바 차트에 추가
        for (int i = 0; i < emotions.length; i++) {
            float value = emotionPercentageMap.getOrDefault(emotions[i], 0f);
            barEntries.add(new BarEntry(i, value));
        }

        // Pie Chart
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(15f); // 글자 크기 키우기
        pieDataSet.setValueFormatter(new PercentFormatter()); // 퍼센트 표시
        pieChart.setData(new PieData(pieDataSet));
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("감정 분석");
        pieChart.setCenterTextSize(20f); // 센터 텍스트 크기 키우기
        pieChart.animate();
        pieChart.setEntryLabelTextSize(15f); // 항목 레이블 텍스트 크기 키우기
        pieChart.invalidate(); // 차트 강제 업데이트

        // Bar Chart
        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(getBarChartColors());
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f); // 글자 크기 키우기
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f); // 바 굵기 줄이기
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(emotions));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(15f); // x축 텍스트 크기 키우기
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(2000);
        barChart.getLegend().setTextSize(15f); // 범례 텍스트 크기 키우기
        barChart.invalidate(); // 차트 강제 업데이트
    }

    private int getColorForEmotion(String emotion) {
        switch (emotion) {
            case "fear":
                return colorFear;
            case "disgust":
                return colorDisgust;
            case "surprise":
                return colorSurprise;
            case "sadness":
                return colorSadness;
            case "angry":
                return colorAnger;
            case "happiness":
                return colorHappiness;
            default:
                return Color.GRAY; // Default color if emotion not found
        }
    }

    private int[] getBarChartColors() {
        return new int[]{colorFear, colorDisgust, colorSurprise, colorSadness, colorAnger, colorHappiness};
    }

    // 퍼센트 포맷터 클래스
    public static class PercentFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format("%.1f%%", value);
        }
    }

    // 서버에 데이터를 저장하거나, 수정하거나, 삭제하는 경우에 사용한다!
    Dialog dialog;
    void showProgress(){
        dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(new ProgressBar(this));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    void dismissProgress(){
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
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