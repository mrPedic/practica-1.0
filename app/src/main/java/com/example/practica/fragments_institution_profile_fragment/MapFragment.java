package com.example.practica.fragments_institution_profile_fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.practica.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private GoogleMap mMap;
    private static final LatLng MINSK = new LatLng(53.9023, 27.5618);
    private static final float DEFAULT_ZOOM = 12.0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMapFragment();
    }

    private void initMapFragment() {
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        try {
            applyMapStyle();
            configureMapUI();

            // Получаем координаты из аргументов
            LatLng location = getArguments() != null ?
                    new LatLng(
                            getArguments().getDouble("latitude"),
                            getArguments().getDouble("longitude")
                    ) : MINSK;

            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Местоположение")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            animateCameraToPosition(location);
        } catch (Exception e) {
            handleGeneralError(e);
        }
    }

    private void animateCameraToPosition(LatLng position) {
        if (mMap != null) {
            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM),
                    1500,
                    null
            );
        }
    }

    private void applyMapStyle() throws Resources.NotFoundException {
        boolean isStyleLoaded = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                )
        );

        if (!isStyleLoaded) {
            Log.w(TAG, "Map style parsing failed");
            Toast.makeText(getContext(), "Ошибка стиля карты", Toast.LENGTH_SHORT).show();
        }
    }

    private void configureMapUI() {
        if (mMap == null) return;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(18.0f);
    }

    private void handleResourceError(Exception e) {
        Log.e(TAG, "Resource error: " + e.getMessage());
        Toast.makeText(getContext(),
                "Ошибка загрузки ресурсов: " + e.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }

    private void handleGeneralError(Exception e) {
        Log.e(TAG, "Map error: ", e);
        Toast.makeText(getContext(),
                "Ошибка загрузки карты: " + e.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMap = null;
    }
}