package com.yh04.joyfulmindapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.yh04.joyfulmindapp.adapter.ChatAdapter;
import com.yh04.joyfulmindapp.adapter.NetworkClient;
import com.yh04.joyfulmindapp.api.ChatApi;
import com.yh04.joyfulmindapp.config.Config;
import com.yh04.joyfulmindapp.model.ChatMessage;
import com.yh04.joyfulmindapp.model.ChatResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editChat;
    private Button btnSend;
    private List<ChatMessage> chatMessages;
    private FirebaseFirestore db;
    private ChatAdapter chatAdapter;
    private String nickname;
    private String token;
    private String naverAccessToken;
    private String profileImageUrl;
    private String email;
    private Handler handler;
    private Runnable typingRunnable;
    private LinearLayout typingIndicatorContainer;
    private View typingIndicatorView;

    private static final String DEFAULT_IMAGE = "https://firebasestorage.googleapis.com/v0/b/joyfulmindapp.appspot.com/o/profile_image%2Fdefaultprofileimg.png?alt=media&token=87768af9-03ef-4cc3-b801-ce17b9a1ece1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        token = sp.getString("token", null);
        naverAccessToken = sp.getString("naverAccessToken", null);
        profileImageUrl = sp.getString("profileImageUrl", DEFAULT_IMAGE);
        email = sp.getString("email", null);

        if (token == null && naverAccessToken == null) {
            Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        chatMessages = new ArrayList<>();
        editChat = findViewById(R.id.editChat);
        btnSend = findViewById(R.id.btnSend);
        typingIndicatorContainer = findViewById(R.id.typingIndicatorContainer);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        btnSend.setOnClickListener(v -> sendMessage());

        fetchUserProfile(); // 사용자 프로필 정보를 가져옴

        handler = new Handler(Looper.getMainLooper());

        // 타이핑 인디케이터 뷰 초기화
        LayoutInflater inflater = LayoutInflater.from(this);
        typingIndicatorView = inflater.inflate(R.layout.typing, typingIndicatorContainer, false);
        typingIndicatorContainer.addView(typingIndicatorView);
    }

    private void fetchUserProfile() {
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        nickname = sp.getString("userNickname", "YourNickname");
        profileImageUrl = sp.getString("profileImageUrl", DEFAULT_IMAGE);

        chatAdapter = new ChatAdapter(chatMessages, nickname, profileImageUrl);
        recyclerView.setAdapter(chatAdapter);

        loadChatMessages();
    }

    private void loadChatMessages() {
        List<ChatMessage> allMessages = new ArrayList<>();

        db.collection("UserChattingTest")
                .whereEqualTo("email", email)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            ChatMessage chatMessage = document.toObject(ChatMessage.class);
                            allMessages.add(chatMessage);
                        }
                        db.collection("JoyChattingTest")
                                .whereEqualTo("email", email)
                                .orderBy("timestamp", Query.Direction.ASCENDING)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        for (DocumentSnapshot document : task2.getResult()) {
                                            ChatMessage chatMessage = document.toObject(ChatMessage.class);
                                            allMessages.add(chatMessage);
                                        }
                                        Collections.sort(allMessages, (m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
                                        chatMessages.clear();
                                        chatMessages.addAll(allMessages);
                                        chatAdapter.notifyDataSetChanged();
                                        recyclerView.scrollToPosition(chatMessages.size() - 1);
                                    } else {
                                        Log.e("ChatActivity", "Error fetching JoyChattingTest messages", task2.getException());
                                    }
                                });
                    } else {
                        Log.e("ChatActivity", "Error fetching UserChattingTest messages", task.getException());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        nickname = sp.getString("userNickname", "YourNickname");
        profileImageUrl = sp.getString("profileImageUrl", DEFAULT_IMAGE);

        if (chatAdapter != null) {
            chatAdapter.setNickname(nickname);
            chatAdapter.setProfileImageUrl(profileImageUrl);
            chatAdapter.notifyDataSetChanged();
        }
    }

    private void sendMessage() {
        if (nickname == null || nickname.isEmpty()) {
            Log.e("ChatActivity", "닉네임이 설정되지 않았습니다.");
            return;
        }

        String message = editChat.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            ChatMessage chatMessage = new ChatMessage(nickname, message, email, Timestamp.now(), profileImageUrl);
            db.collection("UserChattingTest").add(chatMessage)
                    .addOnSuccessListener(documentReference -> Log.d("ChatActivity", "Message sent successfully"))
                    .addOnFailureListener(e -> Log.e("ChatActivity", "Error sending message", e));
            editChat.setText("");

            chatMessages.add(chatMessage);
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chatMessages.size() - 1);

            showTypingIndicator();
            sendToChatApi(chatMessage);
        }
    }

    private void sendToChatApi(ChatMessage chatMessage) {
        Retrofit retrofit = NetworkClient.getRetrofitClient2(ChatActivity.this);
        ChatApi api = retrofit.create(ChatApi.class);

        Call<ChatResponse> call = api.chat(chatMessage);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                hideTypingIndicator();
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    Log.d("ChatApi", "응답 메시지: " + chatResponse.getAnswer());
                    Log.d("ChatApi", "응답: " + chatResponse.toString());

                    ChatMessage responseMessage = new ChatMessage("조이", chatResponse.getAnswer(), email, Timestamp.now(), "조이 프로필 이미지 URL");

                    db.collection("JoyChattingTest").add(responseMessage);

                    chatMessages.add(responseMessage);
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                } else {
                    try {
                        Log.e("ChatApi", "응답 오류: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("ChatApi", "전체 응답: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable throwable) {
                hideTypingIndicator();
                Log.e("ChatApi", "API 호출 실패: " + throwable.getMessage());
            }
        });
    }

    private void showTypingIndicator() {
        typingIndicatorContainer.setVisibility(View.VISIBLE);
        handler.post(typingRunnable = new Runnable() {
            private int dotCount = 0;

            @Override
            public void run() {
                TextView dot1 = typingIndicatorView.findViewById(R.id.dot1);
                TextView dot2 = typingIndicatorView.findViewById(R.id.dot2);
                TextView dot3 = typingIndicatorView.findViewById(R.id.dot3);

                switch (dotCount) {
                    case 0:
                        dot1.setVisibility(View.VISIBLE);
                        dot2.setVisibility(View.INVISIBLE);
                        dot3.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        dot1.setVisibility(View.VISIBLE);
                        dot2.setVisibility(View.VISIBLE);
                        dot3.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        dot1.setVisibility(View.VISIBLE);
                        dot2.setVisibility(View.VISIBLE);
                        dot3.setVisibility(View.VISIBLE);
                        break;
                }
                dotCount = (dotCount + 1) % 3;
                handler.postDelayed(this, 500);
            }
        });
    }

    private void hideTypingIndicator() {
        handler.removeCallbacks(typingRunnable);
        typingIndicatorContainer.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}