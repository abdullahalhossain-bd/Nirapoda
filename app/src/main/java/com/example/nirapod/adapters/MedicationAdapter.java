package com.example.nirapod.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirapod.R;
import com.example.nirapod.database.MedicationDBHelper;
import com.example.nirapod.models.Medication;
import com.example.nirapod.utils.AlarmHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private final List<Medication> medicationList;
    private final Context context;
    private final OnMedicationInteractionListener listener;

    public interface OnMedicationInteractionListener {
        void onMedicationClick(Medication medication);
        void onStatusChanged();
    }

    public MedicationAdapter(Context context, List<Medication> medicationList, OnMedicationInteractionListener listener) {
        this.context = context;
        this.medicationList = medicationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);

        holder.tvMedicationName.setText(medication.getName());
        holder.tvMedicationDosage.setText(medication.getDosage());

        if (medication.getTime() != null && !medication.getTime().isEmpty()) {
            holder.tvMedicationTime.setText(medication.getTime());
            holder.tvMedicationTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvMedicationTime.setVisibility(View.GONE);
        }

        if (medication.getInstructions() != null && !medication.getInstructions().isEmpty()) {
            holder.tvMedicationInstructions.setText(medication.getInstructions());
            holder.tvMedicationInstructions.setVisibility(View.VISIBLE);
        } else {
            holder.tvMedicationInstructions.setVisibility(View.GONE);
        }

        // Set status and color
        updateStatus(holder, medication);

        // Set button visibility based on status
        if ("Taken".equals(medication.getStatus()) || "Skipped".equals(medication.getStatus())) {
            holder.btnMarkTaken.setVisibility(View.GONE);
            holder.btnSkip.setVisibility(View.GONE);
        } else {
            holder.btnMarkTaken.setVisibility(View.VISIBLE);
            holder.btnSkip.setVisibility(View.VISIBLE);

            // Check if medication time has passed
            if (medication.getTime() != null && !medication.getTime().isEmpty()) {
                boolean isPastDue = isTimePassed(medication.getTime());
                if (isPastDue) {
                    holder.btnMarkTaken.setText("Mark as Taken");
                    holder.btnMarkTaken.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                    holder.btnMarkTaken.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.medication_action_button)));
                } else {
                    holder.btnMarkTaken.setText("Take Early");
                }
            }
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMedicationClick(medication);
            }
        });

        holder.btnMarkTaken.setOnClickListener(v -> {
            MedicationDBHelper dbHelper = new MedicationDBHelper(context);
            medication.setStatus("Taken");
            dbHelper.updateMedicationStatus(medication.getId(), "Taken");

            // Cancel the alarm since medication has been taken
            AlarmHelper.cancelAlarm(context, medication.getId());

            // Update UI
            updateStatus(holder, medication);
            holder.btnMarkTaken.setVisibility(View.GONE);
            holder.btnSkip.setVisibility(View.GONE);

            if (listener != null) {
                listener.onStatusChanged();
            }
        });

        holder.btnSkip.setOnClickListener(v -> {
            MedicationDBHelper dbHelper = new MedicationDBHelper(context);
            medication.setStatus("Skipped");
            dbHelper.updateMedicationStatus(medication.getId(), "Skipped");

            // Cancel the alarm since medication has been skipped
            AlarmHelper.cancelAlarm(context, medication.getId());

            // Update UI
            updateStatus(holder, medication);
            holder.btnMarkTaken.setVisibility(View.GONE);
            holder.btnSkip.setVisibility(View.GONE);

            if (listener != null) {
                listener.onStatusChanged();
            }
        });
    }

    private void updateStatus(MedicationViewHolder holder, Medication medication) {
        holder.tvMedicationStatus.setText(medication.getStatus());

        // Get the background drawable
        GradientDrawable statusBackground = (GradientDrawable) holder.tvMedicationStatus.getBackground();

        // Set color based on status
        int color;
        switch (medication.getStatus()) {
            case "Taken":
                color = ContextCompat.getColor(context, R.color.status_taken);
                break;
            case "Skipped":
                color = ContextCompat.getColor(context, R.color.status_skipped);
                break;
            case "Upcoming":
            default:
                color = ContextCompat.getColor(context, R.color.status_upcoming);
                break;
        }

        holder.tvMedicationStatus.setTextColor(color);
        if (statusBackground != null) {
            statusBackground.setColor(color);
        }
    }

    private boolean isTimePassed(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date medicationTime = sdf.parse(timeString);

            Calendar now = Calendar.getInstance();
            Calendar medTime = Calendar.getInstance();

            if (medicationTime != null) {
                medTime.setTime(medicationTime);
                medTime.set(Calendar.YEAR, now.get(Calendar.YEAR));
                medTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
                medTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

                return now.after(medTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public void updateData(List<Medication> newMedicationList) {
        medicationList.clear();
        medicationList.addAll(newMedicationList);
        notifyDataSetChanged();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicationName;
        TextView tvMedicationDosage;
        TextView tvMedicationStatus;
        TextView tvMedicationTime;
        TextView tvMedicationInstructions;
        Button btnMarkTaken;
        Button btnSkip;

        MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvMedicationDosage = itemView.findViewById(R.id.tv_medication_dosage);
            tvMedicationStatus = itemView.findViewById(R.id.tv_medication_status);
            tvMedicationTime = itemView.findViewById(R.id.tv_medication_time);
            tvMedicationInstructions = itemView.findViewById(R.id.tv_medication_instructions);
            btnMarkTaken = itemView.findViewById(R.id.btn_mark_taken);
            btnSkip = itemView.findViewById(R.id.btn_skip);
        }
    }
}