package com.example.practica;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MainFragment extends Fragment implements UserStateListener {

    private TextView welcomeText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        return view;
    }

    @Override
    public void onUserStateChanged(boolean isLoggedIn) {
        if (welcomeText != null) {
            welcomeText.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);

            // Можно добавить дополнительные изменения UI
            if (isLoggedIn) {
                String username = ((MainActivity) requireActivity()).getCurrentUsername();
                welcomeText.setText("Добро пожаловать, " + username + "!");
            }
        }
    }
}