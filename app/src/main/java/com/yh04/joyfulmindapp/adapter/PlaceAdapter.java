package com.yh04.joyfulmindapp.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.yh04.joyfulmindapp.MainActivity;
import com.yh04.joyfulmindapp.MapActivity;
import com.yh04.joyfulmindapp.R;
import com.yh04.joyfulmindapp.model.Place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Place> placeArrayList;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public PlaceAdapter(Context context, ArrayList<Place> placeList) {
        this.context = context;
        this.placeArrayList = placeList;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        fetchUserLocationAndCalculateDistances(); // 사용자 위치를 가져와서 거리 계산
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = placeArrayList.get(position);

        // 장소 이름에 "공원"이 포함되어 있다면 "산책로"로 변경하여 표시
        String displayName = place.getName() != null ? place.getName().replace("공원", " 산책로") : "상점명 없음";
        holder.txtName.setText(displayName);

        holder.txtVicinity.setText(place.getVicinity()); // vicinity 설정

        // 카드뷰 클릭 리스너 설정
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭된 장소의 이름을 가져와서 MainActivity로 전달
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("place", place); // Place 객체를 직렬화하여 전달
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeArrayList.size();
    }

    private void fetchUserLocationAndCalculateDistances() {
        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }

        // 위치 정보 가져오기
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double currentLat = location.getLatitude();
                    double currentLng = location.getLongitude();

                    // 각 장소에 대해 거리 계산 및 소요 시간 계산
                    for (Iterator<Place> iterator = placeArrayList.iterator(); iterator.hasNext();) {
                        Place place = iterator.next();
                        double placeLat = place.getGeometry().getLocation().getLat();
                        double placeLng = place.getGeometry().getLocation().getLng();

                        float[] results = new float[1];
                        Location.distanceBetween(currentLat, currentLng, placeLat, placeLng, results);
                        place.setDistance(results[0]);

                        double walkingSpeed = 1.4; // 걷는 속도 (m/s)
                        double estimatedTimeMinutes = place.getDistance() / walkingSpeed / 60; // 소요 시간 계산

                        // 5분 미만으로 소요되는 장소는 리스트에서 제거
                        if (estimatedTimeMinutes < 5) {
                            iterator.remove();
                        }
                    }

                    // 거리 기준으로 장소 리스트 정렬
                    Collections.sort(placeArrayList, new Comparator<Place>() {
                        @Override
                        public int compare(Place p1, Place p2) {
                            return Float.compare(p1.getDistance(), p2.getDistance());
                        }
                    });

                    // 데이터 변경 알림
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "위치를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "위치를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        TextView txtVicinity; // vicinity 텍스트뷰 추가

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtTitle);
            txtVicinity = itemView.findViewById(R.id.txtDescription); // txtVicinity 설정
        }
    }
}