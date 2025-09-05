package com.example.practica;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.practica.fragments_institution_profile_fragment.DescriptionFragment;
import com.example.practica.fragments_institution_profile_fragment.MapFragment;
import com.example.practica.fragments_institution_profile_fragment.ReviewsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class InstitutionProfileFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;
    private TextView institutionNameTextView, shortDescriptionTextView;
    private Button editButton, logoutButton;
    private SharedPreferences prefs;
    private ImageView institutionImage;

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int EDIT_INSTITUTION_REQUEST = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_institution_profile, container, false);
        prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        initViews(view);
        setupViewPager();
        checkEditPermissions();
        loadInstitutionData();

        return view;
    }

    private void initViews(View view) {
        institutionImage = view.findViewById(R.id.institution_image);
        institutionNameTextView = view.findViewById(R.id.institution_name);
        shortDescriptionTextView = view.findViewById(R.id.short_description);
        editButton = view.findViewById(R.id.edit_button);
        logoutButton = view.findViewById(R.id.logout_button);
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        logoutButton.setOnClickListener(v -> onLogoutButtonClick());
        editButton.setOnClickListener(v -> openInstitutionEditor());
    }

    private void setupViewPager() {
        adapter = new ViewPagerAdapter(requireActivity());

        // Получаем данные заведения
        int institutionId = prefs.getInt("institution_id", -1);
        Institution institution = new DataBaseOfInstitutions(requireContext()).getInstitutionById(institutionId);

        // Создаем MapFragment с аргументами
        MapFragment mapFragment = new MapFragment();
        if (institution != null) {
            Bundle args = new Bundle();
            args.putDouble("latitude", institution.getLatitude());
            args.putDouble("longitude", institution.getLongitude());
            mapFragment.setArguments(args);
        }

        adapter.addFragment(new ReviewsFragment(), "Отзывы");
        adapter.addFragment(new DescriptionFragment(), "Описание");
        adapter.addFragment(mapFragment, "Карта"); // Передаем фрагмент с координатами

        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))).attach();
    }

    private void checkEditPermissions() {
        String currentLogin = prefs.getString("user_login", "");
        String institutionLogin = prefs.getString("institution_login", "");
        editButton.setVisibility(currentLogin.equals(institutionLogin) ? View.VISIBLE : View.GONE);
    }

    private void loadInstitutionData() {
        int institutionId = prefs.getInt("institution_id", -1);
        Institution institution = new DataBaseOfInstitutions(requireContext()).getInstitutionById(institutionId);

        if (institution != null) {
            institutionNameTextView.setText(institution.getName());
            shortDescriptionTextView.setText(institution.getShortDescription());

            List<String> imageUris = institution.getImageUris();
            if (!imageUris.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(imageUris.get(0)))
                        .into(institutionImage);
            } else {
                institutionImage.setImageResource(R.drawable.ic_roamly);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == EDIT_INSTITUTION_REQUEST) {
                loadInstitutionData();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.setAdapter(null);
        adapter = null;
    }

    private void onLogoutButtonClick() {
        prefs.edit().clear().apply();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        requireActivity().finish();
    }

    private void openInstitutionEditor() {
        int institutionId = prefs.getInt("institution_id", -1);
        if (institutionId != -1) {
            Intent intent = new Intent(getActivity(), CreateInstitutionAccountActivity.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("INSTITUTION_ID", institutionId);
            startActivityForResult(intent, EDIT_INSTITUTION_REQUEST);
        }
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> titles = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }

        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}