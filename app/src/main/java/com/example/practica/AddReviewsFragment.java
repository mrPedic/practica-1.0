package com.example.practica;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddReviewsFragment extends Fragment {

    private static final String TAG = "AddReviewsFragment";
    // Удаляем ненужные поля для адреса
    private EditText etReviewText;
    private Button btnSubmit, btnShowAllReviews;
    private Spinner spinnerInstitutions;
    private RatingBar ratingBar;
    private DataBaseOfInstitutions dbInstitutions;
    private SharedPreferences prefs;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private LinearLayout llReviewsContainer;
    private TextView tvExistingReviewsLabel;
    private EditText etImageUrl;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dbInstitutions = new DataBaseOfInstitutions(context);
    }

        private void initViews(View view) {
            try {
                spinnerInstitutions = view.findViewById(R.id.spinnerInstitutions);
                ratingBar = view.findViewById(R.id.ratingBar);
                ratingBar.setStepSize(0.5f); // Фиксированный шаг
                ratingBar.setRating(2.5f);
                etReviewText = view.findViewById(R.id.etReviewText);
                btnSubmit = view.findViewById(R.id.btnSubmit);
                btnShowAllReviews = view.findViewById(R.id.btnShowAllReviews);
                llReviewsContainer = view.findViewById(R.id.llReviewsContainer);
                etImageUrl = view.findViewById(R.id.etImageUrl);
                tvExistingReviewsLabel = view.findViewById(R.id.tvExistingReviewsLabel);
            } catch (Exception e) {
                handleInitError(e);
            }
        }

        private void handleInitError(Exception e) {
            Log.e(TAG, "View initialization error", e);
            showToast("Ошибка инициализации: " + e.getMessage());
        }


    private boolean validateForm() {
        // Проверка выбора заведения
        if (spinnerInstitutions.getSelectedItem() == null) {
            showToast("Выберите заведение из списка");
            return false;
        }

        // Проверка текста отзыва
        String reviewText = etReviewText.getText().toString().trim();
        if (reviewText.isEmpty()) {
            etReviewText.setError("Отзыв не может быть пустым");
            etReviewText.requestFocus();
            return false;
        }

        // Проверка рейтинга
        if (ratingBar.getRating() < 1) {
            showToast("Минимальный рейтинг - 1 звезда");
            return false;
        }

        return true;
    }

    private Review createReviewFromInput() {
        Institution institution = (Institution) spinnerInstitutions.getSelectedItem();
        String reviewText = etReviewText.getText() != null
                ? etReviewText.getText().toString().trim()
                : "";

        Review review = new Review(
                institution.getId(),
                ratingBar.getRating(),
                reviewText, // Гарантированно не null
                institution.getName(),
                prefs.getString("username", "Гость")
        );
        review.setImageUrl(etImageUrl.getText().toString().trim());
        return review;
    }

        private void processReviewSubmission() {
            try {
                Review review = createReviewFromInput();
                logReviewData(review); // Логирование данных
                boolean success = saveReviewToDatabase(review);
                mainHandler.post(() -> handleSubmissionResult(success));
            } catch (Exception e) {
                Log.e(TAG, "Submission error", e);
                mainHandler.post(() -> showToast("Ошибка: " + e.getMessage()));
            }
        }

        private void logReviewData(Review review) {
            Log.d(TAG, "Attempting to save review: "
                    + "\nInstitution ID: " + review.getInstitutionId()
                    + "\nRating: " + review.getRating()
                    + "\nText: " + review.getTextReview()
                    + "\nUsername: " + review.getUsername());
        }


        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_add_reviews, container, false);
        }
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            initViews(view);
            setupSpinner();
            setupSubmitButton();
            setupShowReviewsButton();
            setupTextWatchers();
        }

    private void loadExistingReviews() {
        llReviewsContainer.setVisibility(View.VISIBLE);
        tvExistingReviewsLabel.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try (DataBaseOfReviews db = new DataBaseOfReviews(requireContext())) {
                List<Review> reviews = db.getAllReviews();
                mainHandler.post(() -> {
                    Log.d(TAG, "Loaded reviews count: " + reviews.size()); // Логирование количества отзывов
                    llReviewsContainer.removeAllViews();

                    if (reviews.isEmpty()) {
                        tvExistingReviewsLabel.setText("Нет отзывов");
                        return;
                    }

                    tvExistingReviewsLabel.setText("Все отзывы (" + reviews.size() + "):");

                    for (Review review : reviews) {
                        View reviewItem = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_review, llReviewsContainer, false);

                        TextView tvInstitution = reviewItem.findViewById(R.id.tvInstitution);
                        RatingBar ratingBar = reviewItem.findViewById(R.id.ratingBar);
                        TextView tvText = reviewItem.findViewById(R.id.tvReviewText);
                        TextView tvAuthor = reviewItem.findViewById(R.id.tvAuthor);
                        TextView tvDate = reviewItem.findViewById(R.id.tvDate);

                        // Заполнение данных
                        tvInstitution.setText(review.getInstitutionName());
                        ratingBar.setRating(review.getRating());
                        tvText.setText(review.getTextReview());
                        tvAuthor.setText("Автор: " + review.getUsername());

                        llReviewsContainer.addView(reviewItem);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки отзывов: ", e); // Логирование ошибки
                mainHandler.post(() ->
                        Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

        private void handleSubmissionResult(boolean success) {
            if (success) {
                showToast("Отзыв сохранён!");
                resetForm();
                loadExistingReviews(); // Обновляем список
            } else {
                showToast("Ошибка!");
            }
        }

        private void setupSpinner() {
            new Thread(() -> {
                try {
                    final List<Institution> institutions = dbInstitutions.getAllInstitutions();
                    mainHandler.post(() -> updateSpinnerUI(institutions));
                } catch (Exception e) {
                    Log.e(TAG, "Database error: " + e.getMessage());
                    mainHandler.post(() -> showToast("Ошибка загрузки данных"));
                }
            }).start();
        }

        private void updateSpinnerUI(List<Institution> institutions) {
            if (institutions == null || institutions.isEmpty()) {
                showToast("Нет доступных заведений");
                return;
            }

            ArrayAdapter<Institution> adapter = new ArrayAdapter<Institution>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    institutions
            ) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setText(getItem(position).getAddress_text());
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setText(getItem(position).getName());
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerInstitutions.setAdapter(adapter);
        }

        private void setupSubmitButton() {
            btnSubmit.setOnClickListener(v -> {
                if (!isUserLoggedIn()) return;
                if (!validateForm()) return;

                new Thread(this::processReviewSubmission).start();
            });
        }

        private boolean isUserLoggedIn() {
            if (!prefs.getBoolean("is_logged_in", false)) {
                showToast("Требуется авторизация");
                return false;
            }
            return true;
        }

    private boolean saveReviewToDatabase(Review review) {
        try (DataBaseOfReviews db = new DataBaseOfReviews(requireContext())) {
            boolean result = db.addReview(review);
            Log.d(TAG, "Save result: " + result + " for review: " + review);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Database error: ", e);
            return false;
        }
    }

        private void resetForm() {
            etReviewText.getText().clear();
            if (spinnerInstitutions.getCount() > 0) {
                spinnerInstitutions.setSelection(0);
            }
            etImageUrl.setText("");
            ratingBar.setRating(2.5f);
        }

    private void setupShowReviewsButton() {
        btnShowAllReviews.setOnClickListener(v -> {
            // Переключаем видимость контейнера с отзывами
            boolean isVisible = llReviewsContainer.getVisibility() == View.VISIBLE;
            llReviewsContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            tvExistingReviewsLabel.setVisibility(isVisible ? View.GONE : View.VISIBLE);

            // Если контейнер стал видимым - загружаем отзывы
            if (!isVisible) {
                loadExistingReviews();
                btnShowAllReviews.setText("Скрыть отзывы");
            } else {
                btnShowAllReviews.setText("Показать все отзывы");
            }
        });
    }

        private void setupTextWatchers() {
            addTextWatcher(etReviewText);
        }

        private void addTextWatcher(EditText editText) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateFieldAppearance(editText, s.length() > 0);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        private void updateFieldAppearance(EditText field, boolean hasContent) {
            int color = hasContent ? R.color.color_2 : R.color.color_3;
            field.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), color));
        }

        private void showToast(String message) {
            mainHandler.post(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
            );
        }

        @Override
        public void onDetach() {
            dbInstitutions.close();
            super.onDetach();
        }
    }