package com.example.practica;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private ImageView userIcon;
    private SharedPreferences prefs;

    private Button editProfileButton; // Добавляем поле для кнопки

    private Button loginButton, logoutButton;
    private TextView usernameText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // Инициализация всех элементов
        userIcon = view.findViewById(R.id.user_icon);
        usernameText = view.findViewById(R.id.username_text);
        loginButton = view.findViewById(R.id.login_button);
        logoutButton = view.findViewById(R.id.logout_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button); // Инициализация здесь

        // Назначение обработчиков
        loginButton.setOnClickListener(v -> onLoginButtonClick());
        logoutButton.setOnClickListener(v -> onLogoutButtonClick());
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            startActivity(intent);
        });

        updateUI(); // Обновляем UI после инициализации всех элементов
        return view;
    }

    private void updateUI() {
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        String username = prefs.getString("username", "Гость");
        String role = prefs.getString("user_role", "guest");

        usernameText.setText(username);
        loginButton.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        logoutButton.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);

        // Проверяем инициализацию перед использованием
        if (editProfileButton != null) {
            editProfileButton.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
        }

        // Обновление иконки
        int iconRes = isLoggedIn ?
                (role.equals("institution") ? R.drawable.ic_profile_in_circle : R.drawable.ic_known_user)
                : R.drawable.ic_unknown_user;
        userIcon.setImageResource(iconRes);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        loadUserAvatar();
    }

    private void onLoginButtonClick() {
        // Переход на экран входа
        Intent intent;
        intent = new Intent(getActivity(), Login_SignUpActivity.class);
        startActivity(intent);
    }

    private void onLogoutButtonClick() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("is_logged_in");
        editor.remove("user_id"); // Удаляем ID
        editor.remove("user_login");
        editor.remove("username");
        editor.remove("user_role");
        editor.apply();

        updateUI();
        Toast.makeText(getContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }


    private void openImageChooser() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            startImagePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startImagePicker();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), imageUri);

                String savedPath = ImageUtils.saveImage(requireContext(), bitmap);

                if (savedPath != null) {
                    prefs.edit().putString("avatar_path", savedPath).apply();
                    userIcon.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST) {
            updateUI();
        }
    }

    private void loadUserAvatar() {
        String path = prefs.getString("avatar_path", null);
        if (path != null) {
            var bitmap = ImageUtils.loadImage(requireContext());
            if (bitmap != null) {
                userIcon.setImageBitmap(bitmap);
            }
        }
    }
}