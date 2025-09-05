package com.example.practica;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;

public class Login_SignUpActivity extends AppCompatActivity {
    private DataBaseOfUsers dbHelper;
    private DataBaseOfInstitutions institutionsDbHelper;
    private EditText loginInput, passwordInput;
    private Button loginButton, registerButton, showAccountsButton, createInstitutionBtn;
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
        institutionsDbHelper = new DataBaseOfInstitutions(this);

        initViews();
        setupPasswordToggle();
        updateUIForMode();
    }

    private void initViews() {
        loginInput = findViewById(R.id.login_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        switchModeText = findViewById(R.id.switch_mode_text);
        showAccountsButton = findViewById(R.id.show_accounts_button);
        accountsScroll = findViewById(R.id.accounts_scroll);
        accountsData = findViewById(R.id.accounts_data);
        createInstitutionBtn = findViewById(R.id.btn_create_institution);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());
        switchModeText.setOnClickListener(v -> switchAuthMode());
        showAccountsButton.setOnClickListener(v -> toggleAccountsVisibility());
        createInstitutionBtn.setOnClickListener(v -> startCreateInstitutionActivity());
    }

    private void startCreateInstitutionActivity() {
        Intent intent = new Intent(this, CreateInstitutionAccountActivity.class);
        startActivityForResult(intent, 1);
    }

    private void switchAuthMode() {
        isLoginMode = !isLoginMode;
        updateUIForMode();
    }

    private void setupPasswordToggle() {
        ImageButton togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);
        togglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (passwordInput.getTransformationMethod() == null) {
            passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ((ImageButton) findViewById(R.id.toggle_password_visibility)).setImageResource(R.drawable.ic_visibility_off);
        } else {
            passwordInput.setTransformationMethod(null);
            ((ImageButton) findViewById(R.id.toggle_password_visibility)).setImageResource(R.drawable.ic_visibility);
            new Handler().postDelayed(() -> {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ((ImageButton) findViewById(R.id.toggle_password_visibility)).setImageResource(R.drawable.ic_visibility_off);
            }, 2500);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            loginInput.setText(data.getStringExtra("login"));
            passwordInput.setText(data.getStringExtra("password"));
        }
    }

    private void updateUIForMode() {
        if (isLoginMode) {
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
            switchModeText.setText("Нет аккаунта? Зарегистрируйтесь");
            createInstitutionBtn.setVisibility(View.GONE);
        } else {
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            switchModeText.setText("Уже есть аккаунт? Войдите");
            createInstitutionBtn.setVisibility(View.VISIBLE);
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

            // Загрузка обычных пользователей
            accountsInfo.append("=== Обычные пользователи ===\n");
            accountsInfo.append(loadUsersFromDatabase(dbHelper.getReadableDatabase(),
                    DataBaseOfUsers.UserTable.TABLE_NAME));

            // Загрузка заведений
            accountsInfo.append("\n=== Заведения ===\n");
            accountsInfo.append(loadUsersFromDatabase(institutionsDbHelper.getReadableDatabase(),
                    DataBaseOfInstitutions.TABLE_INSTITUTIONS));

            runOnUiThread(() -> accountsData.setText(accountsInfo.toString()));
        }).start();
    }

    private String loadUsersFromDatabase(SQLiteDatabase db, String tableName) {
        StringBuilder result = new StringBuilder();
        try (Cursor cursor = db.query(tableName, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                result.append("ID: ").append(cursor.getLong(0)).append("\n")
                        .append("Логин: ").append(cursor.getString(1)).append("\n");

                if (tableName.equals(DataBaseOfUsers.UserTable.TABLE_NAME)) {
                    result.append("Имя: ").append(cursor.getString(6)).append("\n")
                            .append("Роль: ").append(cursor.getString(3)).append("\n");
                } else {
                    result.append("Название: ").append(cursor.getString(3)).append("\n")
                            .append("Адрес: ").append(cursor.getString(4)).append("\n");
                }
                result.append("----------------------------\n");
            }
        }
        return result.toString();
    }

    private void loginUser() {
        String login = loginInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInputs(login, password)) return;

        new Thread(() -> {
            if (!checkRegularUser(login, password)) {
                checkInstitutionUser(login, password);
            }
        }).start();
    }

    private boolean checkRegularUser(String login, String password) {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(
                DataBaseOfUsers.UserTable.TABLE_NAME,
                new String[]{
                        DataBaseOfUsers.UserTable._ID,
                        DataBaseOfUsers.UserTable.PASSWORD,
                        DataBaseOfUsers.UserTable.USERNAME,
                        DataBaseOfUsers.UserTable.ROLE
                },
                DataBaseOfUsers.UserTable.LOGIN + " = ?",
                new String[]{login},
                null, null, null
        )) {
            if (cursor.moveToFirst() && password.equals(cursor.getString(1))) {
                saveUserSession(
                        cursor.getLong(0),
                        login,
                        cursor.getString(2),
                        cursor.getString(3),
                        false
                );
                return true;
            }
        }
        return false;
    }

    private void checkInstitutionUser(String login, String password) {
        try (Cursor cursor = institutionsDbHelper.getReadableDatabase().query(
                DataBaseOfInstitutions.TABLE_INSTITUTIONS,
                new String[]{
                        DataBaseOfInstitutions.COLUMN_ID,
                        DataBaseOfInstitutions.COLUMN_PASSWORD,
                        DataBaseOfInstitutions.COLUMN_NAME
                },
                DataBaseOfInstitutions.COLUMN_LOGIN + " = ?",
                new String[]{login},
                null, null, null
        )) {
            if (cursor.moveToFirst() && password.equals(cursor.getString(1))) {
                saveUserSession(
                        cursor.getLong(0),
                        login,
                        cursor.getString(2),
                        "institution",
                        true
                );
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Неверные данные", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void saveUserSession(long id, String login, String name, String role, boolean isInstitution) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_login", login)
                .putString("username", name)
                .putString("user_role", role);

        if (isInstitution) {
            editor.putInt("institution_id", (int) id);
        } else {
            editor.putInt("user_id", (int) id);
        }
        editor.apply();

        startMainActivity(login, name);
    }

    private void registerUser() {
        String login = loginInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInputs(login, password) || isLoginTaken(login)) {
            return;
        }

        new Thread(() -> {
            ContentValues values = new ContentValues();
            values.put(DataBaseOfUsers.UserTable.LOGIN, login);
            values.put(DataBaseOfUsers.UserTable.PASSWORD, password);
            values.put(DataBaseOfUsers.UserTable.USERNAME, "user_" + System.currentTimeMillis());
            values.put(DataBaseOfUsers.UserTable.ROLE, "human");

            try {
                long rowId = dbHelper.getWritableDatabase()
                        .insertOrThrow(DataBaseOfUsers.UserTable.TABLE_NAME, null, values);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                    loginUser();
                    switchAuthMode(); // Переключаем на режим входа
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private boolean isLoginTaken(String login) {
        boolean isTaken = checkLoginInTable(dbHelper.getReadableDatabase(),
                DataBaseOfUsers.UserTable.TABLE_NAME, login);

        if (!isTaken) {
            isTaken = checkLoginInTable(institutionsDbHelper.getReadableDatabase(),
                    DataBaseOfInstitutions.TABLE_INSTITUTIONS, login);
        }

        if (isTaken) {
            runOnUiThread(() ->
                    Toast.makeText(this, "Логин уже занят", Toast.LENGTH_SHORT).show());
        }
        return isTaken;
    }

    private boolean checkLoginInTable(SQLiteDatabase db, String table, String login) {
        try (Cursor cursor = db.query(
                table,
                new String[]{"login"},
                "login = ?",
                new String[]{login},
                null, null, null
        )) {
            return cursor.getCount() > 0;
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

    private void startMainActivity(String login, String username) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_login", login);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        institutionsDbHelper.close();
        super.onDestroy();
    }
}