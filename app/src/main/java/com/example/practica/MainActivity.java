package com.example.practica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private ImageView userIcon;
    private FrameLayout frameLayout;
    private ImageView btnToMain, btnToNotifications;
    private MainFragment mainFragment = new MainFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
    private NotificationFragment notificationFragment = new NotificationFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Скрываем ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupNavigation();

        // Проверяем данные из Intent (после входа)
        checkLoginFromIntent();

        // Всегда открываем MainFragment при запуске
        setFragment(mainFragment);
    }

    private void initializeViews() {
        frameLayout = findViewById(R.id.frameLayout);
        btnToMain = findViewById(R.id.house);
        btnToNotifications = findViewById(R.id.notification);
        userIcon = findViewById(R.id.user_icon);
    }

    private void setupNavigation() {
        btnToMain.setOnClickListener(v -> setFragment(mainFragment));
        btnToNotifications.setOnClickListener(v -> setFragment(notificationFragment));

        // При клике на иконку всегда открываем ProfileFragment
        userIcon.setOnClickListener(v -> setFragment(profileFragment));
    }

    private void checkLoginFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("user_login")) {
            String login = intent.getStringExtra("user_login");
            String username = intent.getStringExtra("username");
            handleLoginSuccess(login, username);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserState();
    }

    private void updateUserState() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        userIcon.setImageResource(isLoggedIn ?
                R.drawable.known_user_icon :
                R.drawable.unknown_user_icon);

        // Обновляем текущий фрагмент если это ProfileFragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (currentFragment instanceof ProfileFragment) {
            ((ProfileFragment) currentFragment).updateUI(isLoggedIn);
        }
    }

    public String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("username", "");
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, fragment);
        ft.commit();
    }

    public boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getBoolean("is_logged_in", false);
    }

    public void handleLoginSuccess(String login, String username) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_login", login)
                .putString("username", username)
                .apply();

        updateUserState();
        Toast.makeText(this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
    }

    public void logout() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .clear()
                .apply();

        updateUserState();
        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
        setFragment(mainFragment); // Возвращаем на главный экран
    }
}