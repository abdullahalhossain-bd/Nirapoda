package com.example.nirapod.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirapod.R;
import com.example.nirapod.adapters.HealthcareProviderAdapter;
import com.example.nirapod.adapters.ReviewAdapter;
import com.example.nirapod.models.HealthcareProvider;
import com.example.nirapod.models.Review;
import com.example.nirapod.network.HealthcareApiService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HealthcareFragment extends Fragment {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    // Views
    private ImageButton backButton;
    private EditText searchEditText;
    private ChipGroup filterChipGroup;
    private RecyclerView providersRecyclerView;
    private View loadingView;
    private TextView emptyStateTextView;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    // Adapters
    private HealthcareProviderAdapter providerAdapter;
    private ReviewAdapter reviewAdapter;

    // API Service
    private HealthcareApiService apiService;

    // Data
    private List<HealthcareProvider> healthcareProviders = new ArrayList<>();
    private String currentPhoneNumber; // For call permission callback

    // Dialog views
    private AlertDialog providerDetailsDialog;
    private CardView reviewFormCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_healthcare, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Initialize location services
        initLocation();

        // Setup API service
        apiService = new HealthcareApiService(requireContext());

        // Setup RecyclerView
        setupRecyclerView();

        // Setup filter chips
        setupFilterChips();

        // Setup search functionality
        setupSearch();

        // Fetch healthcare providers
        fetchHealthcareProviders();

        // Set up back button
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void initViews(View view) {
        backButton = view.findViewById(R.id.back_button);
        searchEditText = view.findViewById(R.id.search_edit_text);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        providersRecyclerView = view.findViewById(R.id.providers_recycler_view);

        // Add loading view and empty state if needed
        // loadingView = view.findViewById(R.id.loading_view);
        // emptyStateTextView = view.findViewById(R.id.empty_state_text);
    }

    private void initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLocation = location;
                                apiService.setUserLocation(location);

                                // If we already have providers, update their distances
                                if (!healthcareProviders.isEmpty()) {
                                    fetchHealthcareProviders();
                                }
                            }
                        }
                    });
        }
    }

    private void setupRecyclerView() {
        providersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        providerAdapter = new HealthcareProviderAdapter(requireContext(), healthcareProviders);
        providersRecyclerView.setAdapter(providerAdapter);

        // Set click listeners
        providerAdapter.setOnProviderClickListener(new HealthcareProviderAdapter.OnProviderClickListener() {
            @Override
            public void onProviderClick(HealthcareProvider provider) {
                showProviderDetailsDialog(provider);
            }

            @Override
            public void onCallButtonClick(String phoneNumber) {
                initiateCall(phoneNumber);
            }
        });
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Find which chip was selected
            if (checkedId != View.NO_ID) {
                Chip selectedChip = group.findViewById(checkedId);
                String specialty = selectedChip.getText().toString();

                // Filter the provider list
                providerAdapter.filterBySpecialty(specialty);
            } else {
                // If no chip is selected, show all providers
                providerAdapter.resetFilters();
            }
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter list based on search text
                // Note: This is a basic implementation. You might want to enhance it.
                filterProvidersByName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void filterProvidersByName(String query) {
        if (providerAdapter != null) {
            List<HealthcareProvider> filteredList = new ArrayList<>();

            for (HealthcareProvider provider : healthcareProviders) {
                if (provider.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(provider);
                }
            }

            providerAdapter.updateList(filteredList);
        }
    }

    private void fetchHealthcareProviders() {
        // Show loading state
        // loadingView.setVisibility(View.VISIBLE);
        // emptyStateTextView.setVisibility(View.GONE);

        apiService.fetchHealthcareProviders(new HealthcareApiService.ApiCallback<List<HealthcareProvider>>() {
            @Override
            public void onSuccess(List<HealthcareProvider> result) {
                // Hide loading state
                // loadingView.setVisibility(View.GONE);

                if (result.isEmpty()) {
                    // Show empty state
                    // emptyStateTextView.setVisibility(View.VISIBLE);
                } else {
                    healthcareProviders.clear();
                    healthcareProviders.addAll(result);
                    providerAdapter.updateList(result);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading state
                // loadingView.setVisibility(View.GONE);
                // Show error state
                // emptyStateTextView.setVisibility(View.VISIBLE);
                // emptyStateTextView.setText("Error: " + errorMessage);

                Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showProviderDetailsDialog(HealthcareProvider provider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_healthcare, null);
        builder.setView(dialogView);

        // Initialize dialog views
        TextView tvDoctorName = dialogView.findViewById(R.id.tvDoctorName);
        TextView tvSpecialty = dialogView.findViewById(R.id.tvSpecialty);
        TextView tvDistance = dialogView.findViewById(R.id.tvDistance);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        TextView tvProfileInitial = dialogView.findViewById(R.id.tvProfileInitial);
        TextView tvAddress = dialogView.findViewById(R.id.tvAddress);
        TextView tvPhone = dialogView.findViewById(R.id.tvPhone);
        TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
        TextView tvWebsite = dialogView.findViewById(R.id.tvWebsite);
        TextView tvHours = dialogView.findViewById(R.id.tvHours);
        TextView tvAboutContent = dialogView.findViewById(R.id.tvAboutContent);
        ChipGroup chipGroupSpecialties = dialogView.findViewById(R.id.chipGroupSpecialties);
        Button btnBookAppointment = dialogView.findViewById(R.id.btnBookAppointment);
        Button btnWriteReview = dialogView.findViewById(R.id.btnWriteReview);
        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);
        RecyclerView rvReviews = dialogView.findViewById(R.id.rvReviews);
        TextView tvReviewCount = dialogView.findViewById(R.id.tvReviewCount);

        // Review form
        reviewFormCard = dialogView.findViewById(R.id.review_form);
        TextInputEditText etName = dialogView.findViewById(R.id.et_name);
        RatingBar rbRating = dialogView.findViewById(R.id.rb_rating);
        TextInputEditText etReview = dialogView.findViewById(R.id.et_review);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);

        // Set provider details
        tvDoctorName.setText(provider.getName());
        tvSpecialty.setText(provider.getSpecialty());
        tvDistance.setText(String.format("%.1f miles", provider.getDistance()));
        ratingBar.setRating(provider.getRating());
        tvProfileInitial.setText(provider.getInitial());
        tvAddress.setText("Address: " + provider.getAddress());
        tvPhone.setText("Phone: " + provider.getPhone());

        // Set optional fields if available
        if (provider.getEmail() != null && !provider.getEmail().isEmpty()) {
            tvEmail.setText("Email: " + provider.getEmail());
            tvEmail.setVisibility(View.VISIBLE);
        } else {
            tvEmail.setVisibility(View.GONE);
        }

        if (provider.getWebsite() != null && !provider.getWebsite().isEmpty()) {
            tvWebsite.setText("Website: " + provider.getWebsite());
            tvWebsite.setVisibility(View.VISIBLE);
        } else {
            tvWebsite.setVisibility(View.GONE);
        }

        if (provider.getHours() != null && !provider.getHours().isEmpty()) {
            tvHours.setText("Hours: " + provider.getHours());
            tvHours.setVisibility(View.VISIBLE);
        } else {
            tvHours.setVisibility(View.GONE);
        }

        if (provider.getAbout() != null && !provider.getAbout().isEmpty()) {
            tvAboutContent.setText(provider.getAbout());
        } else {
            tvAboutContent.setText("No information available.");
        }

        // Clear existing chips and add specialties
        chipGroupSpecialties.removeAllViews();
        for (String specialty : provider.getSpecialties()) {
            Chip chip = new Chip(requireContext());
            chip.setText(specialty);
            chipGroupSpecialties.addView(chip);
        }

        // Set up review list
        rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Review> reviews = provider.getReviews();

        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        reviewAdapter = new ReviewAdapter(requireContext(), reviews);
        rvReviews.setAdapter(reviewAdapter);

        // Set review count
        int reviewCount = reviews.size();
        tvReviewCount.setText(reviewCount + " " + (reviewCount == 1 ? "review" : "reviews"));

        // Initialize the like/dislike callbacks for reviews
        reviewAdapter.setOnReviewInteractionListener(new ReviewAdapter.OnReviewInteractionListener() {
            @Override
            public void onLikeClicked(Review review) {
                Toast.makeText(requireContext(), "You liked a review", Toast.LENGTH_SHORT).show();
                // Here you would typically call API to update the like count
            }

            @Override
            public void onDislikeClicked(Review review) {
                Toast.makeText(requireContext(), "You disliked a review", Toast.LENGTH_SHORT).show();
                // Here you would typically call API to update the dislike count
            }
        });

        // Set up action buttons
        btnBookAppointment.setOnClickListener(v -> initiateCall(provider.getPhone()));

        btnWriteReview.setOnClickListener(v -> {
            // Show review form
            reviewFormCard.setVisibility(View.VISIBLE);
        });

        // Review form buttons
        btnCancel.setOnClickListener(v -> {
            reviewFormCard.setVisibility(View.GONE);
            clearReviewForm(etName, rbRating, etReview);
        });

        btnSubmit.setOnClickListener(v -> {
            submitReview(provider.getId(), etName, rbRating, etReview, provider);
        });

        btnClose.setOnClickListener(v -> {
            if (providerDetailsDialog != null) {
                providerDetailsDialog.dismiss();
            }
        });

        // Build and show dialog
        providerDetailsDialog = builder.create();
        providerDetailsDialog.show();

        // Fetch the latest reviews for this provider
        fetchProviderReviews(provider);
    }

    private void fetchProviderReviews(HealthcareProvider provider) {
        apiService.fetchProviderDetails(provider.getId(), new HealthcareApiService.ApiCallback<HealthcareProvider>() {
            @Override
            public void onSuccess(HealthcareProvider updatedProvider) {
                if (providerDetailsDialog != null && providerDetailsDialog.isShowing()) {
                    RecyclerView rvReviews = providerDetailsDialog.findViewById(R.id.rvReviews);
                    TextView tvReviewCount = providerDetailsDialog.findViewById(R.id.tvReviewCount);

                    if (rvReviews != null && reviewAdapter != null && tvReviewCount != null) {
                        List<Review> reviews = updatedProvider.getReviews();

                        if (reviews != null) {
                            reviewAdapter.updateReviews(reviews);
                            int reviewCount = reviews.size();
                            tvReviewCount.setText(reviewCount + " " + (reviewCount == 1 ? "review" : "reviews"));
                        }
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "Error loading reviews: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReview(int providerId, TextInputEditText etName, RatingBar rbRating, TextInputEditText etReview, HealthcareProvider provider) {
        String name = etName.getText().toString().trim();
        float rating = rbRating.getRating();
        String content = etReview.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Please enter your name");
            return;
        }

        if (rating == 0) {
            Toast.makeText(requireContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            etReview.setError("Please enter your review");
            return;
        }

        // Create review object
        Review review = new Review();
        review.setProviderId(providerId);
        review.setAuthorName(name);
        review.setRating(rating);
        review.setContent(content);
        review.setDate(new Date());

        // Submit to API
        apiService.submitReview(providerId, review, new HealthcareApiService.ApiCallback<Review>() {
            @Override
            public void onSuccess(Review result) {
                Toast.makeText(requireContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show();

                // Add review to provider and update UI
                provider.addReview(result);

                if (reviewAdapter != null) {
                    List<Review> updatedReviews = new ArrayList<>(provider.getReviews());
                    reviewAdapter.updateReviews(updatedReviews);

                    // Update review count in dialog
                    if (providerDetailsDialog != null && providerDetailsDialog.isShowing()) {
                        TextView tvReviewCount = providerDetailsDialog.findViewById(R.id.tvReviewCount);
                        if (tvReviewCount != null) {
                            int reviewCount = updatedReviews.size();
                            tvReviewCount.setText(reviewCount + " " + (reviewCount == 1 ? "review" : "reviews"));
                        }

                        // Update provider rating in dialog
                        RatingBar ratingBar = providerDetailsDialog.findViewById(R.id.ratingBar);
                        if (ratingBar != null) {
                            ratingBar.setRating(provider.getRating());
                        }
                    }
                }

                // Hide review form
                reviewFormCard.setVisibility(View.GONE);
                clearReviewForm(etName, rbRating, etReview);

                // Update the provider in the main list to reflect new rating
                int index = healthcareProviders.indexOf(provider);
                if (index != -1) {
                    healthcareProviders.set(index, provider);
                    providerAdapter.notifyItemChanged(index);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "Error submitting review: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearReviewForm(TextInputEditText etName, RatingBar rbRating, TextInputEditText etReview) {
        etName.setText("");
        rbRating.setRating(0);
        etReview.setText("");
    }

    private void initiateCall(String phoneNumber) {
        currentPhoneNumber = phoneNumber;

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION);
        } else {
            makePhoneCall(phoneNumber);
        }
    }

    private void makePhoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CALL_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (currentPhoneNumber != null) {
                        makePhoneCall(currentPhoneNumber);
                    }
                } else {
                    Toast.makeText(requireContext(), "Call permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation();
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        if (apiService != null) {
            fetchHealthcareProviders();
        }
    }
}