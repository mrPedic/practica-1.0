package com.example.practica;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NotificationFragment extends Fragment {
    private EditText etName, etAddressText, etAddressLink, etShortDescription, etFullDescription;
    private Button btnSave;
    private DataBaseOfInstitutions dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        dbHelper = new DataBaseOfInstitutions(requireContext());
        initViews(view);
        setupSaveButton();
        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etAddressText = view.findViewById(R.id.etAddressText);
        etAddressLink = view.findViewById(R.id.etAddressLink);
        etShortDescription = view.findViewById(R.id.etShortDescription);
        etFullDescription = view.findViewById(R.id.etFullDescription);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveInstitution());
    }

    private void saveInstitution() {
        try {
            String name = etName.getText().toString().trim();
            String addressText = etAddressText.getText().toString().trim();

            if (name.isEmpty() || addressText.isEmpty()) {
                Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(DataBaseOfInstitutions.InstitutionTable.NAME, name);
            values.put(DataBaseOfInstitutions.InstitutionTable.ADDRESS_TEXT, addressText);
            values.put(DataBaseOfInstitutions.InstitutionTable.ADDRESS_LINK, etAddressLink.getText().toString());
            values.put(DataBaseOfInstitutions.InstitutionTable.SHORT_DESCRIPTION, etShortDescription.getText().toString());
            values.put(DataBaseOfInstitutions.InstitutionTable.FULL_DESCRIPTION, etFullDescription.getText().toString());

            long result = dbHelper.insertInstitution(values);

            if (result != -1) {
                Toast.makeText(getContext(), "Сохранено!", Toast.LENGTH_SHORT).show();
                clearForm();
            } else {
                Toast.makeText(getContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("NotificationFragment", "Error saving", e);
            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void clearForm() {
        etName.setText("");
        etAddressText.setText("");
        etAddressLink.setText("");
        etShortDescription.setText("");
        etFullDescription.setText("");
    }

    @Override
    public void onDestroyView() {
        dbHelper.close();
        super.onDestroyView();
    }
}