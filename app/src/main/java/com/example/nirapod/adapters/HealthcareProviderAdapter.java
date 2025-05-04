package com.example.nirapod.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nirapod.R;
import com.example.nirapod.models.HealthcareProvider;
import com.example.nirapod.models.Review;

import java.util.ArrayList;
import java.util.List;

public class HealthcareProviderAdapter extends RecyclerView.Adapter<HealthcareProviderAdapter.ViewHolder> {

    private List<HealthcareProvider> providerList;
    private List<HealthcareProvider> originalList;
    private Context context;
    private OnProviderClickListener listener;

    public interface OnProviderClickListener {
        void onProviderClick(HealthcareProvider provider);
        void onCallButtonClick(String phoneNumber);
    }

    public HealthcareProviderAdapter(Context context, List<HealthcareProvider> providerList) {
        this.context = context;
        this.providerList = providerList;
        this.originalList = new ArrayList<>(providerList);
    }

    public void setOnProviderClickListener(OnProviderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_healthcare, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthcareProvider provider = providerList.get(position);

        holder.providerName.setText(provider.getName());
        holder.providerType.setText(provider.getSpecialty());
        holder.distance.setText(String.format("%.1f miles", provider.getDistance()));
        holder.address.setText(provider.getAddress());
        holder.ratingBar.setRating(provider.getRating());

        // Load image with Glide (handles caching, error fallback)
        if (provider.getImageUrl() != null && !provider.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(provider.getImageUrl())
                    .placeholder(R.drawable.ic_stethoscope)
                    .error(R.drawable.ic_stethoscope)
                    .into(holder.providerImage);
        } else {
            holder.providerImage.setImageResource(R.drawable.ic_stethoscope);
        }

        // Book appointment button - make a call
        holder.bookAppointmentBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallButtonClick(provider.getPhone());
            }
        });

        // Telemedicine button - show toast
        holder.telemedicineBtn.setOnClickListener(v -> {
            Toast.makeText(context, "Upcoming feature", Toast.LENGTH_SHORT).show();
        });

        // Set the entire card clickable
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProviderClick(provider);
            }
        });
    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    // Filter list by specialty
    public void filterBySpecialty(String specialty) {
        if (specialty == null || specialty.isEmpty() || specialty.equalsIgnoreCase("All Providers")) {
            providerList = new ArrayList<>(originalList);
        } else {
            List<HealthcareProvider> filteredList = new ArrayList<>();
            for (HealthcareProvider provider : originalList) {
                if (provider.getSpecialty().equalsIgnoreCase(specialty)) {
                    filteredList.add(provider);
                }
            }
            providerList = filteredList;
        }
        notifyDataSetChanged();
    }

    // Reset list to show all providers
    public void resetFilters() {
        providerList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }

    // Update the full list (e.g., when fresh data is fetched)
    public void updateList(List<HealthcareProvider> newList) {
        this.providerList = new ArrayList<>(newList);
        this.originalList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView providerImage;
        TextView providerName;
        TextView providerType;
        TextView distance;
        TextView address;
        RatingBar ratingBar;
        Button bookAppointmentBtn;
        Button telemedicineBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            providerImage = itemView.findViewById(R.id.iv_provider_image);
            providerName = itemView.findViewById(R.id.tv_provider_name);
            providerType = itemView.findViewById(R.id.tv_provider_type);
            distance = itemView.findViewById(R.id.tv_distance);
            address = itemView.findViewById(R.id.tv_provider_address);
            ratingBar = itemView.findViewById(R.id.rb_provider_rating);
            bookAppointmentBtn = itemView.findViewById(R.id.btn_book_appointment);
            telemedicineBtn = itemView.findViewById(R.id.btn_telemedicine);
        }
    }
}
