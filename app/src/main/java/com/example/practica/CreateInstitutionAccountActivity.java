package com.example.practica;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.practica.adapter.ImageAdapter;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CreateInstitutionAccountActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_MAP = 200;
    private static final int PICK_IMAGES_REQUEST = 201;

    private EditText etName, etAddressText, etShortDescription, etFullDescription;
    private EditText etLogin, etPassword;
    private DataBaseOfInstitutions institutionsDb;
    private double selectedLatitude = -1;
    private double selectedLongitude = -1;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private TextInputLayout tilAddress;
    private boolean isManualAddressInput = false;
    private SharedPreferences prefs;

    // ebanie metodi
    private void setupImageRecycler() {
        RecyclerView recyclerView = findViewById(R.id.images_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(selectedImageUris);
        recyclerView.setAdapter(imageAdapter);
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK && data != null) {
            selectedLatitude = data.getDoubleExtra("latitude", 0);
            selectedLongitude = data.getDoubleExtra("longitude", 0);
            etAddressText.setText(data.getStringExtra("address"));
            disableAddressEditing();
            tilAddress.setEndIconTintList(ColorStateList.valueOf(getColor(R.color.color_2)));
        }

        else if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            handleImageSelection(data);
        }
    }

    private void handleImageSelection(Intent data) {
        try {
            // Очищаем предыдущий выбор, если нужно
            // selectedImageUris.clear();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && selectedImageUris.size() < 10; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    // Получаем постоянные права на доступ к файлу
                    getContentResolver().takePersistableUriPermission(
                            imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                getContentResolver().takePersistableUriPermission(
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                selectedImageUris.add(imageUri);
            }

            // Обновляем адаптер
            imageAdapter.notifyDataSetChanged();

            // Показываем сообщение, если достигнут лимит
            if (selectedImageUris.size() >= 10) {
                Toast.makeText(this, "Достигнут максимум 10 фотографий", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ImageSelection", "Ошибка выбора изображений", e);
            Toast.makeText(this, "Ошибка выбора изображений", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_institutions_accont);
        setupActionBar();
        setupPasswordToggle();
        initViews();
        setupSaveButton();
        setupImageRecycler();


        if (savedInstanceState != null) {
            selectedLatitude = savedInstanceState.getDouble("latitude");
            selectedLongitude = savedInstanceState.getDouble("longitude");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("latitude", selectedLatitude);
        outState.putDouble("longitude", selectedLongitude);
    }

    private void initViews() {
        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        etName = findViewById(R.id.etName);
        etAddressText = findViewById(R.id.etAddressText);
        etShortDescription = findViewById(R.id.etShortDescription);
        etFullDescription = findViewById(R.id.etFullDescription);
        institutionsDb = new DataBaseOfInstitutions(this);
        tilAddress = findViewById(R.id.tilAddress);

        Button btnPickLocation = findViewById(R.id.btnPickLocation);
        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MAP);
        });

        // Настройка редактирования адреса
        tilAddress.setEndIconOnClickListener(v -> toggleAddressEditing());
        setupAddressTextEditing();

        Button addPhotos = findViewById(R.id.btnAddPhotos);
        addPhotos.setOnClickListener(v -> pickImages());

    }

    private void toggleAddressEditing() {
        if (isManualAddressInput) {
            disableAddressEditing();
        } else {
            enableAddressEditing();
        }
    }

    private void enableAddressEditing() {
        isManualAddressInput = true;
        etAddressText.setEnabled(true);
        etAddressText.setFocusableInTouchMode(true);
        etAddressText.requestFocus();
        showKeyboard();
        tilAddress.setEndIconDrawable(R.drawable.ic_done);
        tilAddress.setEndIconContentDescription("Сохранить изменения");

        // Сброс координат при ручном редактировании
        selectedLatitude = 0;
        selectedLongitude = 0;
    }

    private void disableAddressEditing() {
        isManualAddressInput = false;
        etAddressText.setEnabled(false);
        etAddressText.setFocusable(false);
        hideKeyboard();
        tilAddress.setEndIconDrawable(R.drawable.ic_edit);
        tilAddress.setEndIconContentDescription("Редактировать адрес");
    }

    private void setupAddressTextEditing() {
        etAddressText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                disableAddressEditing();
                return true;
            }
            return false;
        });

        etAddressText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // При изменении текста сбрасываем координаты
                selectedLatitude = 0;
                selectedLongitude = 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isManualAddressInput && !TextUtils.isEmpty(s)) {
                    geocodeAddress(s.toString());
                }
            }
        });
    }

    private void geocodeAddress(String address) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.forLanguageTag("ru-RU"));
                List<Address> addresses = geocoder.getFromLocationName(address, 1);

                runOnUiThread(() -> {
                    if (!addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        selectedLatitude = addr.getLatitude();
                        selectedLongitude = addr.getLongitude();
                        tilAddress.setEndIconTintList(ColorStateList.valueOf(getColor(R.color.color_1)));
                    } else {
                        selectedLatitude = 0;
                        selectedLongitude = 0;
                        tilAddress.setEndIconTintList(ColorStateList.valueOf(getColor(R.color.colorForRatingBar)));
                        etAddressText.setError("Адрес не найден");
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Ошибка определения адреса. Проверьте подключение",
                        Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etAddressText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) view = new View(this);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void saveInstitution() {

        if (selectedLatitude == 0 || selectedLongitude == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Ошибка локации")
                    .setMessage("Для сохранения необходимо указать местоположение:\n\n" +
                            "• Нажмите 'Выбрать на карте' или\n" +
                            "• Введите корректный адрес вручную")
                    .setPositiveButton("Выбрать на карте", (d, w) -> openMapPicker())
                    .setNegativeButton("Ввести адрес", (d, w) -> enableAddressEditing())
                    .show();
            return;
        }

        Institution institution = new Institution();
        institution.setLogin(etLogin.getText().toString().trim());
        institution.setPassword(etPassword.getText().toString().trim());
        institution.setName(etName.getText().toString().trim());
        institution.setAddressText(etAddressText.getText().toString().trim());
        institution.setLatitude(selectedLatitude);
        institution.setLongitude(selectedLongitude);
        institution.setShortDescription(etShortDescription.getText().toString().trim());
        institution.setFullDescription(etFullDescription.getText().toString().trim());
        institution.setImageUris(selectedImageUris.stream().map(Uri::toString).collect(Collectors.toList()));

        if (validateInputs(institution)) {
            if (institutionsDb.addOrUpdateInstitution(institution)) { // Обновите метод в DB
                saveLoginData(institution.getLogin(), institution.getName(), "institution");
                updateInstitutionProfile(institution); // Новый метод
                startMainActivity(institution.getLogin(), institution.getName(), "institution");
            }
        }
    }

    private void openMapPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        intent.putExtra("latitude", selectedLatitude);
        intent.putExtra("longitude", selectedLongitude);
        startActivityForResult(intent, REQUEST_CODE_MAP);
    }

    private void updateInstitutionProfile(Institution institution) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE); // Добавлено
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("institution_id", institution.getId());
        editor.putString("institution_login", institution.getLogin());
        editor.apply();
    }

    private void showAddressInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ввод адреса");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(etAddressText.getText());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String address = input.getText().toString().trim();
            if (!address.isEmpty()) {
                etAddressText.setText(address);
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();


    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Новое заведение");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSaveButton() {
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            // Сначала скрываем клавиатуру
            hideKeyboard();

            // Проверяем заполнение обязательных полей
            if (!validateRequiredFields()) {
                return;
            }

            // Проверяем местоположение
            if (selectedLatitude == -1 || selectedLongitude == -1) {
                handleMissingLocation();
                return;
            }

            // Создаем и сохраняем заведение
            saveInstitutionWithFeedback();
        });
    }

    private boolean validateRequiredFields() {
        boolean isValid = true;

        if (TextUtils.isEmpty(etLogin.getText())) {
            etLogin.setError("Введите логин");
            isValid = false;
        }

        if (TextUtils.isEmpty(etPassword.getText())) {
            etPassword.setError("Введите пароль");
            isValid = false;
        }

        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("Введите название");
            isValid = false;
        }

        if (TextUtils.isEmpty(etAddressText.getText())) {
            etAddressText.setError("Введите адрес");
            isValid = false;
        }

        return isValid;
    }

    private void handleMissingLocation() {
        // Показываем красивый Toast вместо диалога
        Toast.makeText(this, "Укажите местоположение на карте", Toast.LENGTH_LONG).show();

        // Подсвечиваем поле адреса
        tilAddress.setBoxStrokeColor(getColor(R.color.colorForRatingBar));
        tilAddress.setHintTextColor(ColorStateList.valueOf(getColor(R.color.colorForRatingBar)));

        // Через 3 секунды возвращаем обычный цвет
        new Handler().postDelayed(() -> {
            tilAddress.setBoxStrokeColor(getColor(R.color.color_3));
            tilAddress.setHintTextColor(ColorStateList.valueOf(getColor(R.color.color_3)));
        }, 3000);
    }

    private void saveInstitutionWithFeedback() {
        // Создаем объект заведения
        Institution institution = new Institution();
        institution.setLogin(etLogin.getText().toString().trim());
        institution.setPassword(etPassword.getText().toString().trim());
        institution.setName(etName.getText().toString().trim());
        institution.setAddressText(etAddressText.getText().toString().trim());
        institution.setLatitude(selectedLatitude);
        institution.setLongitude(selectedLongitude);
        institution.setShortDescription(etShortDescription.getText().toString().trim());
        institution.setFullDescription(etFullDescription.getText().toString().trim());
        institution.setImageUris(selectedImageUris.stream()
                .map(Uri::toString)
                .collect(Collectors.toList()));

        // Показываем прогресс
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Сохранение заведения...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            boolean success = institutionsDb.addOrUpdateInstitution(institution);

            runOnUiThread(() -> {
                progressDialog.dismiss();

                if (success) {
                    // Успешное сохранение
                    saveLoginData(institution.getLogin(), institution.getName(), "institution");
                    updateInstitutionProfile(institution);

                    // Показываем анимированный Toast с иконкой
                    Toast successToast = Toast.makeText(this, "✓ Заведение успешно сохранено", Toast.LENGTH_SHORT);
                    ImageView toastImage = new ImageView(this);
                    toastImage.setImageResource(R.drawable.ic_done);
                    successToast.setView(toastImage);
                    successToast.setGravity(Gravity.CENTER, 0, 0);
                    successToast.show();

                    // Задержка перед переходом
                    new Handler().postDelayed(() -> {
                        startMainActivity(institution.getLogin(), institution.getName(), "institution");
                    }, 1500);
                } else {
                    // Ошибка сохранения
                    Toast.makeText(this, "Ошибка при сохранении. Попробуйте позже.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private boolean validateInputs(Institution institution) {
        boolean isValid = true;
        String login = institution.getLogin().trim();
        String password = institution.getPassword().trim();
        String name = institution.getName().trim();
        String addressText = institution.getAddressText().trim();

        // Сброс ошибок
        etLogin.setError(null);
        etPassword.setError(null);
        etName.setError(null);
        etAddressText.setError(null);

        // Проверка логина
        if (TextUtils.isEmpty(login)) {
            etLogin.setError("Введите логин");
            isValid = false;
        } else if (login.length() < 4) {
            etLogin.setError("Логин должен содержать минимум 4 символа");
            isValid = false;
        }

        // Проверка пароля
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Введите пароль");
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Пароль должен содержать минимум 6 символов");
            isValid = false;
        }

        // Проверка названия
        if (TextUtils.isEmpty(name)) {
            etName.setError("Введите название");
            isValid = false;
        }

        // Проверка адреса
        if (TextUtils.isEmpty(addressText)) {
            etAddressText.setError("Введите адрес");
            isValid = false;
        }

        return isValid;
    }

    private void saveLoginData(String login, String username, String role) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_login", login)
                .putString("username", username)
                .putString("user_role", role)
                .apply();
    }

    private void startMainActivity(String login, String username, String role) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_login", login);
        intent.putExtra("username", username);
        intent.putExtra("user_role", role);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupPasswordToggle() {
        ImageButton togglePasswordVisibility = findViewById(R.id.toggle_password_visibility_institution_create_account_activity);
        togglePasswordVisibility.setOnClickListener(v -> {
            if (etPassword.getTransformationMethod() == null) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ((ImageButton) v).setImageResource(R.drawable.ic_visibility_off);
            } else {
                etPassword.setTransformationMethod(null);
                ((ImageButton) v).setImageResource(R.drawable.ic_visibility);
                // Исправлено: уменьшена задержка до 5 секунд
                new Handler().postDelayed(() -> {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ((ImageButton) v).setImageResource(R.drawable.ic_visibility_off);
                }, 10000); // 5 секунд вместо 250000
            }
        });
    }

    @Override
    protected void onDestroy() {
        institutionsDb.close();
        super.onDestroy();
    }
}