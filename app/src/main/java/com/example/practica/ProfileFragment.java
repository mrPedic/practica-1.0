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
    private FeedReaderDbHelper dbHelper;
    private EditText loginInput;
    private EditText passwordInput;
    private TextView dataView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new FeedReaderDbHelper(requireContext());
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

        // Проверка на пустые поля с выделением ошибок
        boolean hasError = false;

        if (login.isEmpty()) {
            loginInput.setError("Введите логин");
            hasError = true;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Введите пароль");
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(getContext(), "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(FeedReaderDbHelper.FeedEntry.COLUMN_NAME_LOGIN, login);
                values.put(FeedReaderDbHelper.FeedEntry.COLUMN_NAME_PASSWORD, password);

                return db.insert(
                        FeedReaderDbHelper.FeedEntry.TABLE_NAME,
                        null,
                        values
                );
            }

            @Override
            protected void onPostExecute(Long rowId) {
                if (rowId == -1) {
                    Toast.makeText(getContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Данные сохранены (ID: " + rowId + ")", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    loadProfileData();
                }
            }
        }.execute();
    }

    private void clearInputs() {
        loginInput.setText("");
        passwordInput.setText("");
        loginInput.setError(null);
        passwordInput.setError(null);
    }

    private void loadProfileData() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                StringBuilder result = new StringBuilder();
                result.append("Сохраненные данные:\n\n");

                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(
                        FeedReaderDbHelper.FeedEntry.TABLE_NAME,
                        null, null, null, null, null, null
                );

                try {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderDbHelper.FeedEntry._ID));
                        String login = cursor.getString(
                                cursor.getColumnIndexOrThrow(FeedReaderDbHelper.FeedEntry.COLUMN_NAME_LOGIN)
                        );
                        String password = cursor.getString(
                                cursor.getColumnIndexOrThrow(FeedReaderDbHelper.FeedEntry.COLUMN_NAME_PASSWORD)
                        );

                        result.append("ID: ").append(id).append("\n")
                                .append("Логин: ").append(login).append("\n")
                                .append("Пароль: ").append(password).append("\n\n");
                    }
                } finally {
                    cursor.close();
                }

                return result.toString();
            }

            @Override
            protected void onPostExecute(String result) {
                dataView.setText(result);
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}