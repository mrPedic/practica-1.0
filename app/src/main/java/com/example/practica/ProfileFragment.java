package com.example.practica;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    private DataBaseOfUsers dbHelper;
    private EditText loginInput, passwordInput;
    private TextView dataView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DataBaseOfUsers(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        loginInput = view.findViewById(R.id.login_input);
        passwordInput = view.findViewById(R.id.password_input);
        dataView = view.findViewById(R.id.profile_data);

        Button saveButton = view.findViewById(R.id.save_button);
        Button loadButton = view.findViewById(R.id.load_button);

        saveButton.setOnClickListener(v -> saveProfileData());
        loadButton.setOnClickListener(v -> loadProfileData());

        return view;
    }

    private void saveProfileData() {
        String login = loginInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInputs(login, password)) return;

        new AsyncTask<Void, Void, Integer>() {
            private Long rowId;
            private boolean loginExists;

            @Override
            protected Integer doInBackground(Void... voids) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                // Проверка существования логина
                Cursor cursor = db.query(
                        DataBaseOfUsers.UserTable.TABLE_NAME,
                        new String[]{DataBaseOfUsers.UserTable._ID},
                        DataBaseOfUsers.UserTable.LOGIN + " = ?",
                        new String[]{login},
                        null, null, null
                );

                loginExists = cursor.getCount() > 0;
                cursor.close();

                if (!loginExists) {
                    ContentValues values = new ContentValues();
                    values.put(DataBaseOfUsers.UserTable.LOGIN, login);
                    values.put(DataBaseOfUsers.UserTable.PASSWORD, password);

                    // Автоматическое заполнение остальных полей
                    values.put(DataBaseOfUsers.UserTable.USERNAME, generateUsername(login));
                    values.put(DataBaseOfUsers.UserTable.ROLE, "human"); // По умолчанию
                    values.put(DataBaseOfUsers.UserTable.LIKES_INSTITUTION, 0);
                    values.put(DataBaseOfUsers.UserTable.FOLLOWS, 0);
                    values.put(DataBaseOfUsers.UserTable.LIKES_THEMES, 0);

                    rowId = db.insert(
                            DataBaseOfUsers.UserTable.TABLE_NAME,
                            null,
                            values
                    );
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer result) {
                handleSaveResult(loginExists, rowId);
            }
        }.execute();
    }

    private String generateUsername(String login) {
        return "user_" + login.toLowerCase();
    }

    private boolean validateInputs(String login, String password) {
        boolean hasError = false;

        if (login.isEmpty()) {
            loginInput.setError("Введите логин");
            hasError = true;
        } else if (login.length() < 4) {
            loginInput.setError("Логин должен содержать минимум 4 символа");
            hasError = true;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Введите пароль");
            hasError = true;
        } else if (password.length() < 6) {
            passwordInput.setError("Пароль должен содержать минимум 6 символов");
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(getContext(), "Исправьте ошибки ввода", Toast.LENGTH_SHORT).show();
        }
        return hasError;
    }

    private void handleSaveResult(boolean loginExists, Long rowId) {
        if (loginExists) {
            loginInput.setError("Логин уже занят");
            Toast.makeText(getContext(), "Пользователь с таким логином уже существует", Toast.LENGTH_SHORT).show();
        } else if (rowId == -1) {
            Toast.makeText(getContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Данные сохранены (ID: " + rowId + ")", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadProfileData();
        }
    }

    private void loadProfileData() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                StringBuilder result = new StringBuilder("Сохраненные данные:\n\n");
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                try (Cursor cursor = db.query(
                        DataBaseOfUsers.UserTable.TABLE_NAME,
                        null, null, null, null, null, null
                )) {
                    while (cursor.moveToNext()) {
                        result.append("ID: ").append(cursor.getLong(0)).append("\n")
                                .append("Логин: ").append(cursor.getString(1)).append("\n")
                                .append("Пароль: ").append(cursor.getString(2)).append("\n")
                                .append("Роль: ").append(cursor.getString(3)).append("\n")
                                .append("Имя: ").append(cursor.getString(6)).append("\n")
                                .append("Лайков учреждений: ").append(cursor.getInt(4)).append("\n")
                                .append("Подписок: ").append(cursor.getInt(5)).append("\n")
                                .append("Лайков тем: ").append(cursor.getInt(7)).append("\n\n");
                    }
                }
                return result.toString();
            }

            @Override
            protected void onPostExecute(String result) {
                dataView.setText(result);
            }
        }.execute();
    }

    private void clearInputs() {
        loginInput.setText("");
        passwordInput.setText("");
        loginInput.setError(null);
        passwordInput.setError(null);
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}