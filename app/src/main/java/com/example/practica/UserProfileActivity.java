package com.example.practica;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserProfileActivity extends AppCompatActivity {

    private EditText etLogin, etPassword, etUsername;
    private ImageButton togglePasswordVisibility; // Добавьте это поле
    private Button btnSave;
    private DataBaseOfUsers dbHelper;
    private SharedPreferences prefs;
    private int userId;
    private String currentLogin, currentUsername, currentPassword;
    private final AtomicBoolean isSaving = new AtomicBoolean(false);

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initDatabase();
        initUIElements();
        loadUserData();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onPause() {
        hideKeyboard(); // Скрытие клавиатуры при сворачивании приложения
        super.onPause();
    }

    private void initUIElements() {
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        togglePasswordVisibility = findViewById(R.id.toggle_password_visibility_user_profile_activity);
        etUsername = findViewById(R.id.etUsername);
        btnSave = findViewById(R.id.btnSave);

        // Добавляем установку обработчика для кнопки
        setupPasswordToggle();

        btnSave.setOnClickListener(v -> handleSaveClick());
    }
    private void setupPasswordToggle() {
        togglePasswordVisibility.setOnClickListener(v -> {
            // Переключаем видимость пароля
            if (etPassword.getTransformationMethod() == null) {
                // Если пароль виден - скрываем
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
            } else {
                // Если пароль скрыт - показываем
                etPassword.setTransformationMethod(null);
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);

                // Автоматическое скрытие через 2.5 секунды
                new android.os.Handler().postDelayed(() -> {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
                },2500000);
            }

            // Перемещаем курсор в конец текста
            etPassword.setSelection(etPassword.getText().length());
        });
    }


    @SuppressLint("Range")
    private void loadUserData() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DataBaseOfUsers.UserTable.TABLE_NAME,
                     new String[]{
                             DataBaseOfUsers.UserTable.LOGIN,
                             DataBaseOfUsers.UserTable.PASSWORD,
                             DataBaseOfUsers.UserTable.USERNAME
                     },
                     DataBaseOfUsers.UserTable._ID + " = ?",
                     new String[]{String.valueOf(userId)},
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                currentLogin = cursor.getString(cursor.getColumnIndex(DataBaseOfUsers.UserTable.LOGIN));
                currentPassword = cursor.getString(cursor.getColumnIndex(DataBaseOfUsers.UserTable.PASSWORD));
                currentUsername = cursor.getString(cursor.getColumnIndex(DataBaseOfUsers.UserTable.USERNAME));

                runOnUiThread(() -> {
                    etLogin.setText(currentLogin);
                    etPassword.setText(currentPassword);
                    etUsername.setText(currentUsername);
                });
            } else {
                showToastAndClose("Профиль не найден");
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка загрузки данных: " + e.getMessage());
            showToast("Ошибка загрузки профиля");
        }
    }

    private void handleSaveClick() {
        if (isSaving.get()) return;

        isSaving.set(true);
        btnSave.setEnabled(false);
        new Thread(this::processSaveOperation).start();
    }

    private void processSaveOperation() {
        executor.execute(() -> {
        try {
            ContentValues values = prepareContentValues();
            if (values == null) {
                showToast("Нет изменений для сохранения");
                return;
            }

            if (updateDatabase(values)) {
                updateSharedPreferences(values);
                showToastAndClose("Данные успешно обновлены");
            } else {
                showToast("Ошибка сохранения данных");
            }
        } catch (Exception e) {
            Log.e("ThreadError", "Background task failed", e);
        } finally {
            resetSaveState();
        }
        });
    }

    private ContentValues prepareContentValues() {
        String newLogin = etLogin.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();

        ContentValues values = new ContentValues();
        boolean hasChanges = false;

        if (!newLogin.equals(currentLogin)) {
            if (isLoginExists(newLogin)) {
                showError(etLogin, "Логин уже занят");
                return null;
            }
            values.put(DataBaseOfUsers.UserTable.LOGIN, newLogin);
            hasChanges = true;
        }

        if (!newPassword.isEmpty() && !newPassword.equals(currentPassword)) {
            values.put(DataBaseOfUsers.UserTable.PASSWORD, newPassword);
            hasChanges = true;
        }

        if (!newUsername.equals(currentUsername)) {
            values.put(DataBaseOfUsers.UserTable.USERNAME, newUsername);
            hasChanges = true;
        }

        return hasChanges ? values : null;
    }

    private boolean isLoginExists(String login) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DataBaseOfUsers.UserTable.TABLE_NAME,
                     new String[]{DataBaseOfUsers.UserTable._ID},
                     DataBaseOfUsers.UserTable.LOGIN + " = ? AND " +
                             DataBaseOfUsers.UserTable._ID + " != ?",
                     new String[]{login, String.valueOf(userId)},
                     null, null, null)) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка проверки логина: " + e.getMessage());
            return true; // Блокируем сохранение при ошибке
        }
    }

    private boolean updateDatabase(ContentValues values) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            int rows = db.update(
                    DataBaseOfUsers.UserTable.TABLE_NAME,
                    values,
                    DataBaseOfUsers.UserTable._ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rows > 0;
        }
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    private void updateSharedPreferences(ContentValues values) {
        SharedPreferences.Editor editor = prefs.edit();
        if (values.containsKey(DataBaseOfUsers.UserTable.LOGIN)) {
            String newLogin = values.getAsString(DataBaseOfUsers.UserTable.LOGIN);
            editor.putString("user_login", newLogin);

            // Обновление логина во всех связанных активностях
            Intent broadcastIntent = new Intent("LOGIN_UPDATED");
            broadcastIntent.putExtra("new_login", newLogin);
            sendBroadcast(broadcastIntent);
        }
        if (values.containsKey(DataBaseOfUsers.UserTable.USERNAME)) {
            String newUsername = values.getAsString(DataBaseOfUsers.UserTable.USERNAME);
            editor.putString("username", newUsername);

            // Обновление имени пользователя в реальном времени
            Intent broadcastIntent = new Intent("USERNAME_UPDATED");
            broadcastIntent.putExtra("new_username", newUsername);
            sendBroadcast(broadcastIntent);
        }
        editor.apply();
    }

    private void resetSaveState() {
        runOnUiThread(() -> {
            btnSave.setEnabled(true);
            isSaving.set(false);
        });
    }

    private void initDatabase() {
        dbHelper = new DataBaseOfUsers(this);
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Проверка авторизации
        if (!prefs.getBoolean("is_logged_in", false)) {
            showToastAndClose("Требуется авторизация");
            return;
        }

        // Получение user_id с проверкой
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Log.e("USER_ID", "Неверный ID пользователя");
            showToastAndClose("Ошибка авторизации");
            finish();
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void showToastAndClose(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showError(EditText field, String message) {
        runOnUiThread(() -> {
            field.setError(message);
            field.requestFocus();
        });
    }
}