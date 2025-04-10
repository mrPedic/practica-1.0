package com.example.practica;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.practica.DataBaseOfReviews;
import com.example.practica.R;
import com.example.practica.Review;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
    private List<Review> reviews = new ArrayList<>();
    private int currentIndex = 0;
    private DataBaseOfReviews dbHelper;

    private TextView tvInstitutionName, tvReviewText, tvAddress;
    private RatingBar ratingBar;
    private ImageView ivReviewImage;
    private View cardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        dbHelper = new DataBaseOfReviews(requireContext());

        initViews(view);
        setupButtons(view);
        loadReviews();

        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("currentIndex", 0);
        }

        return view;
    }

    private void initViews(View view) {
        tvInstitutionName = view.findViewById(R.id.tvInstitutionName);
        tvReviewText = view.findViewById(R.id.tvReviewText);
        tvAddress = view.findViewById(R.id.tvAddress);
        ratingBar = view.findViewById(R.id.ratingBar);
        ivReviewImage = view.findViewById(R.id.ivReviewImage);
        cardView = view.findViewById(R.id.cardView); // Убедитесь, что cardView инициализирован

        tvAddress.setOnClickListener(v -> openAddressLink());
    }

    private void loadReviews() {
        new Thread(() -> {
            try {
                reviews = dbHelper.getAllReviews();
                requireActivity().runOnUiThread(() -> {
                    if (reviews.isEmpty()) {
                        showNoReviewsMessage();
                    } else {
                        showCurrentReview();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showCurrentReview() {
        if (currentIndex >= 0 && currentIndex < reviews.size()) {
            Review current = reviews.get(currentIndex);

            tvInstitutionName.setText(current.getInstitutionName());
            ratingBar.setRating(current.getRating());
            tvReviewText.setText(current.getTextReview());
            tvAddress.setText(current.getAddressText());

            if (current.getImageUrl() != null && !current.getImageUrl().isEmpty()) {
                Glide.with(this).load(current.getImageUrl()).into(ivReviewImage);
                ivReviewImage.setVisibility(View.VISIBLE);
            } else {
                ivReviewImage.setVisibility(View.GONE);
            }
        }
    }

    private void openAddressLink() {
        if (currentIndex >= 0 && currentIndex < reviews.size()) {
            String url = reviews.get(currentIndex).getAddressLink();
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnUp).setOnClickListener(v -> showNextReview());
        view.findViewById(R.id.btnDown).setOnClickListener(v -> showPreviousReview());
        view.findViewById(R.id.btnRight).setOnClickListener(v -> likeReview());
        view.findViewById(R.id.btnLeft).setOnClickListener(v -> dislikeReview());
    }

    private void showNextReview() {
        if (reviews.isEmpty()) return;

        if (currentIndex < reviews.size() - 1) {
            currentIndex++;
            animateCardTransition(true);
            showCurrentReview();
        }
    }

    private void showPreviousReview() {
        if (currentIndex > 0) {
            currentIndex--;
            animateCardTransition(true);
            showCurrentReview();
        }
    }

    private void likeReview() {
        updateReview(true);
        showNextReview();  // Автоматически переходим к следующему отзыву после лайка
    }

    private void dislikeReview() {
        updateReview(false);
        showNextReview();  // Автоматически переходим к следующему отзыву после дизлайка
    }

    private void updateReview(boolean isLiked) {
        if (currentIndex >= 0 && currentIndex < reviews.size()) {
            Review current = reviews.get(currentIndex);
            dbHelper.updateReviewAction(current.getId(), isLiked);
        }
    }

    private void animateCardTransition(boolean next) {
        Animation exitAnim = AnimationUtils.loadAnimation(requireContext(),
                next ? R.anim.slide_out_down : R.anim.slide_out_up);
        Animation enterAnim = AnimationUtils.loadAnimation(requireContext(),
                next ? R.anim.slide_in_up : R.anim.slide_in_down);

        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                showCurrentReview();
                cardView.startAnimation(enterAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        cardView.startAnimation(exitAnim);
    }

    private void showNoReviewsMessage() {
        tvInstitutionName.setText("Нет отзывов");
        ratingBar.setVisibility(View.GONE);
        ivReviewImage.setVisibility(View.GONE);
        tvReviewText.setVisibility(View.GONE);
        tvAddress.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentIndex", currentIndex);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
