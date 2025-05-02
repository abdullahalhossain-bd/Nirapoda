package com.example.nirapod.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirapod.R;
import com.example.nirapod.adapters.MedicationAdapter;
import com.example.nirapod.database.MedicationDBHelper;
import com.example.nirapod.dialogs.AddMedicationDialog;
import com.example.nirapod.models.Medication;
import com.example.nirapod.utils.AlarmHelper;
import com.example.nirapod.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedicationFragment extends Fragment implements
        MedicationAdapter.OnMedicationInteractionListener,
        AddMedicationDialog.OnMedicationAddedListener {

    private RecyclerView rvMedications;
    private TextView tvMedicationsCount;
    private MaterialButton btnAddMedication;
    private List<Medication> medicationList;
    private MedicationAdapter adapter;
    private MedicationDBHelper dbHelper;

    private BroadcastReceiver statusUpdateReceiver;

    public MedicationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_medication, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database helper
        dbHelper = new MedicationDBHelper(requireContext());

        // Initialize UI components
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnAddMedication = view.findViewById(R.id.btn_add_medication);
        tvMedicationsCount = view.findViewById(R.id.tv_medications_count);
        rvMedications = view.findViewById(R.id.rv_medications);

        // Set up RecyclerView
        medicationList = new ArrayList<>();
        adapter = new MedicationAdapter(requireContext(), medicationList, this);
        rvMedications.setAdapter(adapter);
        rvMedications.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up click listeners
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnAddMedication.setOnClickListener(v -> {
            AddMedicationDialog dialog = new AddMedicationDialog(requireContext(), this);
            dialog.show();
        });

        // Create notification channel for Android O and above
        NotificationHelper.createNotificationChannel(requireContext());

        // Register broadcast receiver for status updates
        statusUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadMedications();
            }
        };

        // Load medications from database
        loadMedications();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register for status updates
        requireContext().registerReceiver(
                statusUpdateReceiver,
                new IntentFilter("com.example.nirapod.MEDICATION_STATUS_UPDATED"),
                Context.RECEIVER_NOT_EXPORTED);

        // Refresh medications when fragment resumes
        loadMedications();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister receiver
        try {
            requireContext().unregisterReceiver(statusUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }

    private void loadMedications() {
        // Get today's day of week
        String today = getCurrentDayOfWeek();

        // Get medications due today
        List<Medication> todaysMedications = dbHelper.getMedicationsDueToday(today);
        medicationList.clear();
        medicationList.addAll(todaysMedications);
        adapter.notifyDataSetChanged();

        // Update the medications count
        int count = todaysMedications.size();
        tvMedicationsCount.setText(String.format(Locale.getDefault(),
                "You have %d medication%s due today",
                count,
                count == 1 ? "" : "s"));
    }

    private String getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Mon";
            case Calendar.TUESDAY:
                return "Tue";
            case Calendar.WEDNESDAY:
                return "Wed";
            case Calendar.THURSDAY:
                return "Thu";
            case Calendar.FRIDAY:
                return "Fri";
            case Calendar.SATURDAY:
                return "Sat";
            case Calendar.SUNDAY:
                return "Sun";
            default:
                return "";
        }
    }

    @Override
    public void onMedicationClick(Medication medication) {
        // Show the edit dialog when a medication item is clicked
        AddMedicationDialog dialog = new AddMedicationDialog(requireContext(), this, medication);
        dialog.show();
    }

    @Override
    public void onStatusChanged() {
        // Reload medications when status changes
        loadMedications();
    }

    @Override
    public void onMedicationAdded(Medication medication) {
        // Insert medication into database
        long id = dbHelper.insertMedication(medication);
        medication.setId((int) id);

        // Schedule alarm for the medication
        AlarmHelper.scheduleAlarm(requireContext(), medication);

        // Reload medications
        loadMedications();
    }

    @Override
    public void onMedicationUpdated(Medication medication) {
        // Update medication in database
        dbHelper.updateMedication(medication);

        // Cancel previous alarm and schedule new one
        AlarmHelper.cancelAlarm(requireContext(), medication.getId());
        AlarmHelper.scheduleAlarm(requireContext(), medication);

        // Reload medications
        loadMedications();
    }
}