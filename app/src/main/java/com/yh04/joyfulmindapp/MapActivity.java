package com.yh04.joyfulmindapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.yh04.joyfulmindapp.model.Place;

public class MapActivity extends AppCompatActivity {

    TMapData tMapData = new TMapData();
    LocationListener locationListener;
    LocationManager locationManager;

    double latitude;
    double longitude;
    double Distance;

    Place place;
    double lat;
    double lng;

    TextView remainD;
    TextView remainT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 액션바 이름 변경
        getSupportActionBar().setTitle(" ");
        // 액션바에 화살표 백버튼을 표시하는 코드
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout linearLayoutTmap = findViewById(R.id.linearLayoutTmap);
        TMapView tMapView = new TMapView(this);
        remainD = findViewById(R.id.remainD);
        remainT = findViewById(R.id.remainT);

        tMapView.setSKTMapApiKey("iNOu6cW7Gl6BCfk6HI0GT90J9e3B4iqB85qXUiZ1");

        // 초기 중심점을 설정 (임의의 위치)
        tMapView.setCenterPoint(126.6772, 37.5428);
        tMapView.setZoomLevel(16);

        // 도착지 위도 경도 intent로 받기
        place = (Place) getIntent().getSerializableExtra("place");

        if (place == null) {
            Toast.makeText(this, "장소 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 위에서 받아온 place 객체에 저장되어 있는 위도, 경도 꺼내기
        lat = place.getGeometry().getLocation().getLat();
        lng = place.getGeometry().getLocation().getLng();

        linearLayoutTmap.addView(tMapView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Log.i("PLACES MAIN", "위도 : " + latitude + ", 경도 : " + longitude);

                // 위치가 변경되었으므로 지도 중심점을 변경합니다.
                tMapView.setCenterPoint(longitude, latitude);

                // 마커를 추가하는 코드는 여기에 위치 정보를 받은 후에 실행합니다.
                TMapMarkerItem markerItem1 = new TMapMarkerItem();
                TMapPoint tMapPoint1 = new TMapPoint(latitude, longitude);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pointdesign);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width / 25, height / 25, false);
                markerItem1.setIcon(resizedBitmap);
                markerItem1.setPosition(0.5f, 1.0f);
                markerItem1.setTMapPoint(tMapPoint1);
                markerItem1.setName("현재 위치");
                tMapView.addMarkerItem("markerItem1", markerItem1);

                tMapView.setCompassMode(true);
                tMapView.setCompassModeFix(true);
                tMapView.setPOIRotate(true);

                // 도착지 설정
                TMapPoint destination = new TMapPoint(lat, lng);

                // 도착지 마커 설정
                TMapMarkerItem markerItem2 = new TMapMarkerItem();
                TMapPoint tMapPoint2 = new TMapPoint(lat, lng);

                Bitmap bit = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
                int w = bit.getWidth();
                int h = bit.getHeight();
                Bitmap resized = Bitmap.createScaledBitmap(bit, w / 20, h / 20, false);
                markerItem2.setIcon(resized);
                markerItem2.setPosition(0.5f, 1.0f);
                markerItem2.setTMapPoint(tMapPoint2);
                markerItem2.setName("도착 위치");
                tMapView.addMarkerItem("markerItem2", markerItem2);

                // 경로 표시
                tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPoint1, destination, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine tMapPolyLine) {
                        if (tMapPolyLine == null) {
                            Log.e("PATH_ERROR", "Polyline is null");
                            return;
                        }

                        tMapPolyLine.setLineColor(Color.argb(10, 252, 194, 126));
                        tMapPolyLine.setOutLineColor(Color.argb(10, 252, 194, 126));
                        Distance = tMapPolyLine.getDistance();

                        Log.i("DISTANCE", "거리는 : " + Distance + "m");

                        tMapPolyLine.setOutLineWidth(13);
                        tMapView.addTMapPolyLine("walkPath", tMapPolyLine);

                        // 예상 보행 속도 기준 설정 (m/s 단위)
                        double walkingSpeed = 1.4; // 보행자 평균 속도 약 5km/h = 1.4m/s

                        // 예상 소요 시간 계산 (단위: 초)
                        double estimatedTimeSeconds = Distance / walkingSpeed;

                        // 거리와 시간 표시
                        runOnUiThread(() -> {
                            remainD.setText(String.format("남은 거리 :    %.2fkm", Distance / 1000));
                            remainT.setText(String.format("남은 시간 :    %.2f분", estimatedTimeSeconds / 60));
                        });

                        Log.d("ESTIMATED_TIME", "예상 소요 시간: " + estimatedTimeSeconds + " 초");
                    }
                });
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            // 권한이 이미 허용된 경우 위치 업데이트를 요청합니다.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    -1, 1, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) { // 요청 코드가 100일 때만 처리
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 승인된 경우 위치 업데이트를 요청합니다.
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            -1, 1, locationListener);
                }
            } else {
                // 권한이 거부된 경우 사용자에게 메시지를 표시하거나 다른 처리를 수행할 수 있습니다.
                Toast.makeText(this, "위치 권한을 허용해야 이 앱을 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(MapActivity.this, PlaceActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}