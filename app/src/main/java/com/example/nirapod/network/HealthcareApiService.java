package com.example.nirapod.network;


import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.nirapod.models.HealthcareProvider;
import com.example.nirapod.models.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HealthcareApiService {
    private static final String TAG = "HealthcareApiService";
    private static final String BASE_URL = "https://yourserver.com/api"; // Replace with your actual server URL

    private RequestQueue requestQueue;
    private Context context;
    private Location userLocation;

    public HealthcareApiService(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    // Set user's location for distance calculation
    public void setUserLocation(Location location) {
        this.userLocation = location;
    }

    // Calculate distance between two locations
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (userLocation == null) {
            return 0.0; // Default if location not available
        }

        Location locationA = new Location("point A");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lon1);

        Location locationB = new Location("point B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lon2);

        float distanceInMeters = locationA.distanceTo(locationB);
        // Convert to miles
        return distanceInMeters * 0.000621371;
    }

    // Fetch all healthcare providers
    public void fetchHealthcareProviders(final ApiCallback<List<HealthcareProvider>> callback) {
        String url = BASE_URL + "/healthcare_providers";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<HealthcareProvider> providers = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject providerJson = response.getJSONObject(i);
                                HealthcareProvider provider = parseProviderFromJson(providerJson);
                                providers.add(provider);
                            }
                            callback.onSuccess(providers);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing provider data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley error: " + error.getMessage());
                        callback.onError("Network error: " + error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    // Fetch provider details by ID
    public void fetchProviderDetails(int providerId, final ApiCallback<HealthcareProvider> callback) {
        String url = BASE_URL + "/healthcare_provider/" + providerId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            HealthcareProvider provider = parseProviderFromJson(response);

                            // Also fetch reviews for this provider
                            fetchProviderReviews(providerId, new ApiCallback<List<Review>>() {
                                @Override
                                public void onSuccess(List<Review> reviews) {
                                    provider.setReviews(reviews);
                                    callback.onSuccess(provider);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    // Continue even if reviews fail to load
                                    Log.e(TAG, "Error loading reviews: " + errorMessage);
                                    callback.onSuccess(provider);
                                }
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing provider details");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley error: " + error.getMessage());
                        callback.onError("Network error: " + error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    // Fetch reviews for a specific provider
    public void fetchProviderReviews(int providerId, final ApiCallback<List<Review>> callback) {
        String url = BASE_URL + "/healthcare_provider/" + providerId + "/reviews";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<Review> reviews = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject reviewJson = response.getJSONObject(i);
                                Review review = parseReviewFromJson(reviewJson);
                                reviews.add(review);
                            }
                            callback.onSuccess(reviews);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing review data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley error: " + error.getMessage());
                        callback.onError("Network error: " + error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    // Submit a new review
    public void submitReview(int providerId, Review review, final ApiCallback<Review> callback) {
        String url = BASE_URL + "/healthcare_provider/" + providerId + "/review";

        try {
            JSONObject reviewJson = new JSONObject();
            reviewJson.put("author_name", review.getAuthorName());
            reviewJson.put("content", review.getContent());
            reviewJson.put("rating", review.getRating());
            reviewJson.put("provider_id", providerId);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, reviewJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Review submittedReview = parseReviewFromJson(response);
                                callback.onSuccess(submittedReview);
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON parsing error: " + e.getMessage());
                                callback.onError("Error parsing review submission response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Volley error: " + error.getMessage());
                            callback.onError("Network error: " + error.getMessage());
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("Error creating review data");
        }
    }

    // Helper method to parse provider JSON
    private HealthcareProvider parseProviderFromJson(JSONObject json) throws JSONException {
        int id = json.getInt("id");
        String name = json.getString("name");
        String specialty = json.getString("specialty");
        String address = json.getString("address");
        String phone = json.getString("phone");

        // These fields may be optional in the API response
        double latitude = json.has("latitude") ? json.getDouble("latitude") : 0.0;
        double longitude = json.has("longitude") ? json.getDouble("longitude") : 0.0;
        float rating = json.has("rating") ? (float) json.getDouble("rating") : 0.0f;

        // Calculate distance if user location is available
        double distance = 0.0;
        if (userLocation != null) {
            distance = calculateDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    latitude, longitude);
        } else if (json.has("distance")) {
            distance = json.getDouble("distance");
        }

        // Create provider with required fields
        HealthcareProvider provider = new HealthcareProvider(id, name, specialty, address, phone, distance, rating);

        // Add optional fields if present
        if (json.has("email")) provider.setEmail(json.getString("email"));
        if (json.has("website")) provider.setWebsite(json.getString("website"));
        if (json.has("hours")) provider.setHours(json.getString("hours"));
        if (json.has("about")) provider.setAbout(json.getString("about"));
        if (json.has("image_url")) provider.setImageUrl(json.getString("image_url"));
        if (json.has("review_count")) provider.setReviewCount(json.getInt("review_count"));

        // Parse specialties if present
        if (json.has("specialties")) {
            JSONArray specialtiesArray = json.getJSONArray("specialties");
            List<String> specialties = new ArrayList<>();
            for (int i = 0; i < specialtiesArray.length(); i++) {
                specialties.add(specialtiesArray.getString(i));
            }
            provider.setSpecialties(specialties);
        } else {
            // Add main specialty as a default
            provider.addSpecialty(specialty);
        }

        return provider;
    }

    // Helper method to parse review JSON
    private Review parseReviewFromJson(JSONObject json) throws JSONException {
        Review review = new Review();

        review.setId(json.getInt("id"));
        review.setProviderId(json.getInt("provider_id"));
        review.setAuthorName(json.getString("author_name"));
        review.setContent(json.getString("content"));
        review.setRating((float) json.getDouble("rating"));
        review.setLikes(json.getInt("likes"));
        review.setDislikes(json.getInt("dislikes"));

        // Parse date
        if (json.has("date")) {
            try {
                String dateStr = json.getString("date");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = format.parse(dateStr);
                review.setDate(date);
            } catch (ParseException e) {
                Log.e(TAG, "Date parsing error: " + e.getMessage());
                // Keep default current date if parsing fails
            }
        }

        return review;
    }

    // Callback interface for API responses
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
}