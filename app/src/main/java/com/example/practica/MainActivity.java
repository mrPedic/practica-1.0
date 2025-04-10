package com.example.practica;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private ImageView btnToMain, btnToNotifications, btnToAddReviews;
    private int currentSelectedItem;
    private ImageView userIcon;
    private FrameLayout frameLayout;
    private MainFragment mainFragment = new MainFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
    private AddReviewsFragment addReviewsFragment = new AddReviewsFragment();
    private NotificationFragment notificationFragment = new NotificationFragment();

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
                institution.setAddress_text("ул. Тестовая, 1");
                db.addInstitution(institution);
            }
        }).start();
    }

    public static String getCurrentUsername(Context context) {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("username", "Гость");
    }

    public void refreshReviews() {
        MainFragment newFragment = new MainFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, newFragment);
        transaction.commit();
    }

    private void initializeViews() {
        frameLayout = findViewById(R.id.frameLayout);
        btnToMain = findViewById(R.id.house_icon);
        btnToNotifications = findViewById(R.id.notification_icon);
        userIcon = findViewById(R.id.user_icon);
        btnToAddReviews = findViewById(R.id.add_reviews_icon);
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
        setupNavigationIcons();

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (currentFragment instanceof ProfileFragment) {
            ((ProfileFragment) currentFragment).updateUI(isLoggedIn);
        }
    }

    private void setFragment(Fragment fragment) {
        if (!isFinishing() && !isDestroyed()) {
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
        prefs.edit().clear().apply();
        updateUserState();
        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }

    private void setupNavigationIcons() {
        resetAllIcons();
        int activeColor = ContextCompat.getColor(this, R.color.color_2);

        if (currentSelectedItem == btnToMain.getId()) {
            btnToMain.setColorFilter(activeColor);
        } else if (currentSelectedItem == btnToNotifications.getId()) {
            btnToNotifications.setColorFilter(activeColor);
        } else if (currentSelectedItem == userIcon.getId()) {
            userIcon.setColorFilter(activeColor);
        } else if (currentSelectedItem == btnToAddReviews.getId()) {
            btnToAddReviews.setColorFilter(activeColor);
        }
    }

    private void resetAllIcons() {
        int defaultColor = ContextCompat.getColor(this, R.color.color_1);
        btnToMain.setColorFilter(defaultColor);
        btnToNotifications.setColorFilter(defaultColor);
        userIcon.setColorFilter(defaultColor);
        btnToAddReviews.setColorFilter(defaultColor);
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
        userIcon.setOnClickListener(v -> setFragment(profileFragment, userIcon.getId()));
        btnToAddReviews.setOnClickListener(v -> setFragment(addReviewsFragment, btnToAddReviews.getId()));
    }
}