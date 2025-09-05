package com.example.practica;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker realMarker;
    private LatLng selectedLocation;
    private Geocoder geocoder;
    private LatLng initialLocation;
    private ImageView fakeMarker;
    private static final int MAP_STYLE_RES = R.raw.map_style;
    private static final String TAG = "MapPickerActivity";
    private volatile boolean isDestroyed = false;
    private static final int GEOCODE_RETRIES = 3;


    // Методы класса


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }

    private void getAddressFromLocation(LatLng latLng) {
        final LatLng finalLatLng = latLng;
        new Thread(() -> {
            int attempts = 0;
            boolean success = false;
            String localAddress = null;

            while (attempts < GEOCODE_RETRIES && !success && !isDestroyed) {
                attempts++;
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            finalLatLng.latitude,
                            finalLatLng.longitude,
                            1
                    );

                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        localAddress = formatAddress(address);
                        success = true;
                    }
                } catch (IOException | IllegalStateException e) {
                    Log.e("MapPicker", "Geocoder error: " + e.getMessage());
                    if (isDestroyed) break;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (!isDestroyed && !isFinishing()) {
                String finalLocalAddress = localAddress;
                boolean finalSuccess = success;
                runOnUiThread(() -> {
                    if (finalSuccess) {
                        returnResult(finalLatLng, finalLocalAddress);
                    } else {
                        showManualAddressInputDialog(finalLatLng);
                    }
                });
            }
        }).start();
    }

    private void showManualAddressInputDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Адрес не определен");
        builder.setMessage("Не удалось определить адрес для выбранной локации. Пожалуйста, введите адрес вручную:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setHint("Введите полный адрес");
        builder.setView(input);

        builder.setPositiveButton("Подтвердить", (dialog, which) -> {
            String address = input.getText().toString().trim();
            if (!address.isEmpty()) {
                returnResult(latLng, address);
            } else {
                Toast.makeText(this, "Адрес не может быть пустым", Toast.LENGTH_SHORT).show();
                showManualAddressInputDialog(latLng); // Повторно показываем диалог
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.cancel();
            Toast.makeText(this, "Выберите другое место на карте", Toast.LENGTH_SHORT).show();
        });

        builder.setCancelable(false);
        builder.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        double initialLat = getIntent().getDoubleExtra("latitude", 53.9045);
        double initialLng = getIntent().getDoubleExtra("longitude", 27.5615);
        initialLocation = new LatLng(initialLat, initialLng);
        geocoder = new Geocoder(this, new Locale("ru", "RU"));

        setupUI();
        setupMapFragment();

    }

    private void setupUI() {
        fakeMarker = findViewById(R.id.fake_marker);
        fakeMarker.setVisibility(View.GONE);

        findViewById(R.id.btnConfirmLocation).setOnClickListener(v -> {
            if (selectedLocation != null) {
                getAddressFromLocation(selectedLocation);
            } else {
                Toast.makeText(this, "Выберите место на карте", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        try {
            applyMapStyle();
            setupMap();
            setupMapListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка инициализации карты", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyMapStyle() {
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this,
                            MAP_STYLE_RES
                    )
            );

            if (!success) {
                Log.e(TAG, "Ошибка загрузки стиля карты");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Стиль карты не найден: " + e.getMessage());
        }
    }

    private void setupMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        addRealMarker(initialLocation);
    }

    private void addRealMarker(LatLng position) {
        if (realMarker != null) realMarker.remove();
        realMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Выбранное местоположение")
                .anchor(0.5f, 0.5f));
    }

    private void updateRealMarker(LatLng position) {
        realMarker.setPosition(position);
        realMarker.setVisible(true);
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getThoroughfare() != null) sb.append(address.getThoroughfare());
        if (address.getSubThoroughfare() != null) sb.append(", ").append(address.getSubThoroughfare());
        if (address.getLocality() != null) sb.append(", ").append(address.getLocality());
        return sb.toString();
    }

    private void returnResult(LatLng latLng, String address) {
        Intent result = new Intent();
        result.putExtra("latitude", latLng.latitude);
        result.putExtra("longitude", latLng.longitude);
        result.putExtra("address", address);
        setResult(RESULT_OK, result);
        finish();
    }

    private void handleGeocodeFailure(LatLng latLng, String address) {
        runOnUiThread(() -> {
            if (isDestroyed || isFinishing()) return;

            new AlertDialog.Builder(this)
                    .setTitle("Ошибка определения адреса")
                    .setMessage("Не удалось определить адрес для выбранной локации. Хотите использовать координаты?")
                    .setPositiveButton("Да", (dialog, which) -> returnResult(latLng, address))
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    private void setupMapListeners() {
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                animateFakeMarker();
            }
        });

        mMap.setOnCameraIdleListener(() -> {
            fakeMarker.setVisibility(View.GONE);
            LatLng center = mMap.getCameraPosition().target;
            updateRealMarker(center);
            selectedLocation = center;
        });
    }

    private void animateFakeMarker() {
        fakeMarker.setVisibility(View.VISIBLE);
        fakeMarker.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .start();
    }
}