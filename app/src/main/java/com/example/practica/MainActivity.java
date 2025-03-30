package com.example.practica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private ImageView btnToMain, btnToNotifications;
    private int currentSelectedItem; // Не инициализируем здесь
    private ImageView userIcon;
    private FrameLayout frameLayout;
    private MainFragment mainFragment = new MainFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
    private NotificationFragment notificationFragment = new NotificationFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        currentSelectedItem = btnToMain.getId(); // Инициализируем после initializeViews()
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
        setupNavigationIcons();
    }

    private void initializeViews() {
        frameLayout = findViewById(R.id.frameLayout);
        btnToMain = findViewById(R.id.house_icon);
        btnToNotifications = findViewById(R.id.notification_icon);
        userIcon = findViewById(R.id.user_icon);
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
        // Обновляем иконку пользователя в MainActivity
        userIcon.setImageResource(isLoggedIn ?
                R.drawable.known_user_icon :
                R.drawable.unknown_user_icon);
        // Обновляем цвет иконок навигации
        setupNavigationIcons();
        // Обновляем ProfileFragment, если он активен
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

        ft.setCustomAnimations(
                R.anim.slide_in_right,  // Вход нового фрагмента
                R.anim.fade_out,        // Выход текущего фрагмента
                R.anim.fade_in,         // Вход при возврате (back stack)
                R.anim.slide_out_left   // Выход при возврате
        );

        ft.replace(R.id.frameLayout, fragment);
        ft.commit();
    }

    public void handleLoginSuccess(String login, String username) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_login", login)
                .putString("username", username)
                .apply();
        // Обновляем UI сразу
        updateUserState();
        Toast.makeText(this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
    }

    public void logout() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        updateUserState(); // Этот метод должен обновлять навигацию и иконки
        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }
    private void setupNavigationIcons() {
        // Сброс всех иконок к цвету по умолчанию
        resetAllIcons();

        // Установка цвета для активной иконки
        if (currentSelectedItem == btnToMain.getId()) {
            btnToMain.setColorFilter(ContextCompat.getColor(this, R.color.color_2));
        } else if (currentSelectedItem == btnToNotifications.getId()) {
            btnToNotifications.setColorFilter(ContextCompat.getColor(this, R.color.color_2));
        } else if (currentSelectedItem == userIcon.getId()) {
            userIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_2));
        }
    }

    private void resetAllIcons() {
        btnToMain.setColorFilter(ContextCompat.getColor(this, R.color.color_1));
        btnToNotifications.setColorFilter(ContextCompat.getColor(this, R.color.color_1));
        userIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_1));
    }

    private void setFragment(Fragment fragment, int selectedItemId) {
        currentSelectedItem = selectedItemId;
        setupNavigationIcons(); // Обновляем иконки

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, fragment);
        ft.commit();
    }

    private void setupNavigation() {
        btnToMain.setOnClickListener(v -> {
            setFragment(mainFragment, btnToMain.getId());
        });

        btnToNotifications.setOnClickListener(v -> {
            setFragment(notificationFragment, btnToNotifications.getId());
        });

        userIcon.setOnClickListener(v -> {
            setFragment(profileFragment, userIcon.getId());
        });
    }
}