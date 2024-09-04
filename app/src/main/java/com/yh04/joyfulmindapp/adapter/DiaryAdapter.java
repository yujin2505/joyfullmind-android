package com.yh04.joyfulmindapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.yh04.joyfulmindapp.EditDiaryActivity;
import com.yh04.joyfulmindapp.R;
import com.yh04.joyfulmindapp.api.DiaryApi;
import com.yh04.joyfulmindapp.model.Diary;
import com.yh04.joyfulmindapp.adapter.NetworkClient;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    private List<Diary> diaryList;
    private Context context;
    private String token;

    public DiaryAdapter(List<Diary> diaryList, String token) {
        this.diaryList = diaryList;
        this.token = token;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diary_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Diary diary = diaryList.get(position);
        holder.txtTitle.setText(diary.getTitle());
        holder.txtDescription.setText(diary.getContent());
        holder.txtCreatedAt.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(diary.getCreatedAt()));

        holder.imgDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(diary.getId(), position);
        });

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditDiaryActivity.class);
            intent.putExtra("diary", diary);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    public void setDiaryList(List<Diary> diaryList) {
        this.diaryList = diaryList;
        notifyDataSetChanged();
    }

    private void showDeleteConfirmationDialog(int diaryId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("일기 삭제")
                .setMessage("일기를 삭제하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> deleteDiary(diaryId, position))
                .setNegativeButton("아니오", null)
                .show();
    }

    private void deleteDiary(int diaryId, int position) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(context);
        DiaryApi diaryApi = retrofit.create(DiaryApi.class);
        Call<Void> call = diaryApi.deleteDiary("Bearer " + token, diaryId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    diaryList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "일기가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "일기 삭제를 실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtCreatedAt;
        ImageView imgDelete;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtCreatedAt = itemView.findViewById(R.id.txtCreatedAt);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
