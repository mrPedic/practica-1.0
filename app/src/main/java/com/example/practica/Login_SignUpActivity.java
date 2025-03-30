package com.example.practica;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login_SignUpActivity extends AppCompatActivity {
    private DataBaseOfUsers dbHelper;
    private EditText loginInput, passwordInput;
    private Button loginButton, registerButton, showAccountsButton;
    private TextView switchModeText, accountsData;
    private ScrollView accountsScroll;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login_sign_up);

        dbHelper = new DataBaseOfUsers(this);

        // Инициализация элементов
        loginInput = findViewById(R.id.login_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        switchModeText = findViewById(R.id.switch_mode_text);
        showAccountsButton = findViewById(R.id.show_accounts_button);
        accountsScroll = findViewById(R.id.accounts_scroll);
        accountsData = findViewById(R.id.accounts_data);

        setupPasswordToggle();
        updateUIForMode();

        // Обработчики кликов
        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());

        switchModeText.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUIForMode();
        });

        showAccountsButton.setOnClickListener(v -> toggleAccountsVisibility());
    }

    private void setupPasswordToggle() {
        ImageButton togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);
        togglePasswordVisibility.setOnClickListener(v -> {
            if (passwordInput.getTransformationMethod() == null) {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ((ImageButton) v).setImageResource(R.drawable.ic_visibility_off);
            } else {
                passwordInput.setTransformationMethod(null);
                ((ImageButton) v).setImageResource(R.drawable.ic_visibility);
                new Handler().postDelayed(() -> {
                    passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ((ImageButton) v).setImageResource(R.drawable.ic_visibility_off);
                }, 2500);
            }
        });
    }

    private void updateUIForMode() {
        if (isLoginMode) {
            // Режим входа
            loginButton.setText("Войти");
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
            switchModeText.setText("Нет аккаунта? Зарегистрируйтесь");
        } else {
            // Режим регистрации
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            registerButton.setText("Зарегистрироваться");
            switchModeText.setText("Уже есть аккаунт? Войдите");
        }
    }

    private void toggleAccountsVisibility() {
        if (accountsScroll.getVisibility() == View.VISIBLE) {
            accountsScroll.setVisibility(View.GONE);
            showAccountsButton.setText("Показать все аккаунты");
        } else {
            loadAllAccounts();
            accountsScroll.setVisibility(View.VISIBLE);
            showAccountsButton.setText("Скрыть аккаунты");
        }
    }

    private void loadAllAccounts() {
        new Thread(() -> {
            StringBuilder accountsInfo = new StringBuilder();
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            try (Cursor cursor = db.query(
                    DataBaseOfUsers.UserTable.TABLE_NAME,
                    null, null, null, null, null, null)) {

                while (cursor.moveToNext()) {
                    accountsInfo.append("ID: ").append(cursor.getLong(0)).append("\n")
                            .append("Логин: ").append(cursor.getString(1)).append("\n")
                            .append("Пароль: ").append(cursor.getString(2)).append("\n")
                            .append("Имя: ").append(cursor.getString(6)).append("\n")
                            .append("Роль: ").append(cursor.getString(3)).append("\n")
                            .append("Лайков: ").append(cursor.getInt(4)).append("\n")
                            .append("Подписок: ").append(cursor.getInt(5)).append("\n")
                            .append("-----------------\n");
                }
            }

            runOnUiThread(() -> accountsData.setText(accountsInfo.toString()));
        }).start();
    }

    private void startMainActivity(String login, String username) {
        // Сохраняем данные пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_login", login)
                .putString("username", username)
                .apply();

        // Передаем данные в MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_login", login);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }

    private void loginUser() {
        String login = loginInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInputs(login, password)) {
            new Thread(() -> {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String[] columns = {
                        DataBaseOfUsers.UserTable._ID,
                        DataBaseOfUsers.UserTable.PASSWORD,
                        DataBaseOfUsers.UserTable.USERNAME,
                        DataBaseOfUsers.UserTable.ROLE
                };
                String selection = DataBaseOfUsers.UserTable.LOGIN + " = ?";
                String[] selectionArgs = {login};

                try (Cursor cursor = db.query(
                        DataBaseOfUsers.UserTable.TABLE_NAME,
                        columns,
                        selection,
                        selectionArgs,
                        null, null, null)) {

                    if (cursor.moveToFirst()) {
                        String storedPassword = cursor.getString(1);
                        if (password.equals(storedPassword)) {
                            String username = cursor.getString(2);
                            String role = cursor.getString(3);

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                                // Сохраняем все необходимые данные
                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                prefs.edit()
                                        .putBoolean("is_logged_in", true)
                                        .putString("user_login", login)
                                        .putString("username", username)
                                        .putString("user_role", role)
                                        .apply();

                                startMainActivity(login, username);
                            });
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show());
                    }
                }
            }).start();
        }
    }

    private void registerUser() {
        String login = loginInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInputs(login, password)) {
            new Thread(() -> {
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                try (Cursor cursor = db.query(
                        DataBaseOfUsers.UserTable.TABLE_NAME,
                        new String[]{DataBaseOfUsers.UserTable._ID},
                        DataBaseOfUsers.UserTable.LOGIN + " = ?",
                        new String[]{login},
                        null, null, null)) {

                    if (cursor.getCount() > 0) {
                        runOnUiThread(() ->
                                Toast.makeText(this, "Логин уже занят", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    ContentValues values = new ContentValues();
                    values.put(DataBaseOfUsers.UserTable.LOGIN, login);
                    values.put(DataBaseOfUsers.UserTable.PASSWORD, password);
                    values.put(DataBaseOfUsers.UserTable.USERNAME, generateUsername(login));
                    values.put(DataBaseOfUsers.UserTable.ROLE, "human");
                    values.put(DataBaseOfUsers.UserTable.LIKES_INSTITUTION, 0);
                    values.put(DataBaseOfUsers.UserTable.FOLLOWS, 0);
                    values.put(DataBaseOfUsers.UserTable.LIKES_THEMES, 0);

                    long rowId = db.insert(DataBaseOfUsers.UserTable.TABLE_NAME, null, values);

                    runOnUiThread(() -> {
                        if (rowId != -1) {
                            Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                            String username = generateUsername(login);
                            startMainActivity(login, username); // Явная передача имени пользователя
                        } else {
                            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }
    }


    private boolean validateInputs(String login, String password) {
        boolean isValid = true;

        if (login.isEmpty()) {
            loginInput.setError("Введите логин");
            isValid = false;
        } else if (login.length() < 4) {
            loginInput.setError("Логин должен содержать минимум 4 символа");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Введите пароль");
            isValid = false;
        } else if (password.length() < 6) {
            passwordInput.setError("Пароль должен содержать минимум 6 символов");
            isValid = false;
        }

        return isValid;
    }

    private String generateUsername(String login) {
        return "user_" + login.toLowerCase();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}