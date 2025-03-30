package com.example.practica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    private Button loginButton, logoutButton; // Добавьте logoutButton
    private ImageView userIcon;
    private TextView usernameText;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        loginButton = view.findViewById(R.id.login_button);
        logoutButton = view.findViewById(R.id.logout_button); // Инициализируйте кнопку
        usernameText = view.findViewById(R.id.username_text);
        userIcon = view.findViewById(R.id.user_icon);

        // Обработчик для кнопки выхода
        logoutButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout(); // Вызов метода выхода из MainActivity
            }
        });

        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), Login_SignUpActivity.class));
        });

        updateUI(isUserLoggedIn());
    }

    public void updateUI(boolean isLoggedIn) {
        if (getView() == null) return;

        loginButton.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        logoutButton.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE); // Управление видимостью
        usernameText.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);

        if (isLoggedIn) {
            userIcon.setImageResource(R.drawable.known_user_icon);
            usernameText.setText(prefs.getString("username", ""));
        } else {
            userIcon.setImageResource(R.drawable.unknown_user_icon);
        }
    }

    private boolean isUserLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }
}