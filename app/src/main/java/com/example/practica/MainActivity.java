package com.example.practica;

import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 101;

    private ImageView btnToMain, btnToNotifications, btnToAddReviews, btnToSettings;
    private int currentSelectedItem;
    private ImageView userIcon;
    private FrameLayout frameLayout;
    private MainFragment mainFragment = new MainFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
    private AddReviewsFragment addReviewsFragment = new AddReviewsFragment();
    private NotificationFragment notificationFragment = new NotificationFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();
    private InstitutionProfileFragment institutionProfileFragment = new InstitutionProfileFragment();

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация БД только один раз
        DataBaseOfInstitutions dataBaseOfInstitutions = new DataBaseOfInstitutions(this);
        dataBaseOfInstitutions.getWritableDatabase().close(); // Закрываем соединение сразу

        initializeViews(); // Вызывается ОДИН раз
        currentSelectedItem = btnToMain.getId();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupNavigation();
        checkLoginFromIntent();
        setupNavigationIcons();

        // Первоначальная загрузка фрагмента без анимации
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, mainFragment)
                .commit();

        new Thread(() -> {
            DataBaseOfInstitutions db = new DataBaseOfInstitutions(this);
            if(db.getAllInstitutions().isEmpty()) {
                Institution institution = new Institution();
                institution.setName("Тестовое заведение");
                institution.setAddressText("ул. Тестовая, 1");
                db.addInstitution(institution);
            }
        }).start();
    }

    private void initializeViews() {
        frameLayout = findViewById(R.id.frameLayout);
        btnToMain = findViewById(R.id.house_icon);
        btnToNotifications = findViewById(R.id.notification_icon);
        userIcon = findViewById(R.id.user_icon);
        btnToAddReviews = findViewById(R.id.add_reviews_icon);
        btnToSettings = findViewById(R.id.settings_icon);
    }

    private void checkLoginFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("user_login")) {
            String login = intent.getStringExtra("user_login");
            String username = intent.getStringExtra("username");
            handleLoginSuccess(login, username);
        }
    }

    private void updateUserState() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        String role = prefs.getString("user_role", "guest");

        // Обновляем иконку в зависимости от роли
        if (role.equals("institution")) {
            userIcon.setImageResource(R.drawable.ic_profile_in_circle);
        } else {
            userIcon.setImageResource(isLoggedIn ?
                    R.drawable.ic_known_user :
                    R.drawable.ic_unknown_user);
        }

        // Дополнительная логика для заведений
        if (role.equals("institution")) {
            btnToAddReviews.setVisibility(View.VISIBLE);
        }
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

    private void setupNavigationIcons() {
        resetAllIcons();
        int activeColor = ContextCompat.getColor(this, R.color.color_1);

        if (currentSelectedItem == btnToMain.getId()) {
            btnToMain.setColorFilter(activeColor);
        } else if (currentSelectedItem == btnToNotifications.getId()) {
            btnToNotifications.setColorFilter(activeColor);
        } else if (currentSelectedItem == userIcon.getId()) {
            userIcon.setColorFilter(activeColor);
        } else if (currentSelectedItem == btnToAddReviews.getId()) {
            btnToAddReviews.setColorFilter(activeColor);
        }else if (currentSelectedItem == btnToSettings.getId()) {
            btnToSettings.setColorFilter(activeColor);
        }
    }

    private void resetAllIcons() {
        int defaultColor = ContextCompat.getColor(this, R.color.color_2);
        btnToMain.setColorFilter(defaultColor);
        btnToNotifications.setColorFilter(defaultColor);
        userIcon.setColorFilter(defaultColor);
        btnToAddReviews.setColorFilter(defaultColor);
        btnToSettings.setColorFilter(defaultColor);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserState();
        // Обновление данных при возвращении из редактирования
        if (currentSelectedItem == userIcon.getId()) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String role = prefs.getString("user_role", "guest");

            if (role.equals("institution")) {
                setFragment(institutionProfileFragment, userIcon.getId());
            }
        }
    }

    private void setFragment(Fragment fragment, int selectedItemId) {
        currentSelectedItem = selectedItemId;
        setupNavigationIcons();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_left
        );
        ft.replace(R.id.frameLayout, fragment);
        ft.commit();
    }

    private void setupNavigation() {
        btnToMain.setOnClickListener(v -> setFragment(mainFragment, btnToMain.getId()));
        btnToNotifications.setOnClickListener(v -> setFragment(notificationFragment, btnToNotifications.getId()));

        // Модифицируем обработчик для userIcon
        userIcon.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String role = prefs.getString("user_role", "guest");

            if (role.equals("institution")) {
                setFragment(institutionProfileFragment, userIcon.getId());
            } else {
                setFragment(profileFragment, userIcon.getId());
            }
        });

        btnToAddReviews.setOnClickListener(v -> setFragment(addReviewsFragment, btnToAddReviews.getId()));
        btnToSettings.setOnClickListener(v -> setFragment(settingsFragment, btnToSettings.getId()));
    }
}