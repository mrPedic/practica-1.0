package com.example.practica;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class SettingsFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private static final String THEME_PREF = "theme_pref";
    private static final String THEME_KEY = "selected_theme";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sharedPreferences = requireContext().getSharedPreferences(THEME_PREF, Context.MODE_PRIVATE);

        Spinner themeSpinner = view.findViewById(R.id.theme_spinner);

        // Ждем завершения отрисовки Spinner
        themeSpinner.post(() -> {
            // Устанавливаем смещение равное высоте Spinner
            themeSpinner.setDropDownVerticalOffset(themeSpinner.getHeight());
        });

        // Настройка адаптера
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.theme_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(adapter);

        // Установка текущей темы
        setCurrentThemeSelection(themeSpinner);

        // Обработчик выбора
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

                switch (position) {
                    case 0:
                        selectedTheme = AppCompatDelegate.MODE_NIGHT_NO;
                        break;
                    case 1:
                        selectedTheme = AppCompatDelegate.MODE_NIGHT_YES;
                        break;
                }

                saveAndApplyTheme(selectedTheme);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ничего не делаем
            }
        });


        return view;

    }

    private void setCurrentThemeSelection(Spinner spinner) {
        int savedTheme = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        switch (savedTheme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                spinner.setSelection(0);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                spinner.setSelection(1);
                break;
            default:
                spinner.setSelection(2);
        }
    }

    private void saveAndApplyTheme(int themeMode) {
        // Сохраняем только если тема изменилась
        if (sharedPreferences.getInt(THEME_KEY, -1) != themeMode) {
            sharedPreferences.edit()
                    .putInt(THEME_KEY, themeMode)
                    .apply();

            AppCompatDelegate.setDefaultNightMode(themeMode);
            if (getActivity() != null) {
                getActivity().recreate();
            }
        }
    }
}