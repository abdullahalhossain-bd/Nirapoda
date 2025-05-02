package com.example.nirapod.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.example.nirapod.R;
import com.example.nirapod.models.Medication;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddMedicationDialog extends Dialog {

    private TextInputEditText etMedicationName;
    private TextInputEditText etDosage;
    private TextInputEditText etTime;
    private TextInputEditText etInstructions;
    private RadioGroup rgReminderFrequency;
    private RadioButton rbDaily;
    private RadioButton rbSpecificDays;
    private RadioButton rbAsNeeded;
    private LinearLayout llWeekdays;
    private Chip chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun;
    private Button btnCancel, btnSave;

    private final OnMedicationAddedListener listener;
    private Medication medicationToEdit;

    public interface OnMedicationAddedListener {
        void onMedicationAdded(Medication medication);
        void onMedicationUpdated(Medication medication);
    }

    public AddMedicationDialog(@NonNull Context context, OnMedicationAddedListener listener) {
        super(context);
        this.listener = listener;
    }

    public AddMedicationDialog(@NonNull Context context, OnMedicationAddedListener listener, Medication medicationToEdit) {
        super(context);
        this.listener = listener;
        this.medicationToEdit = medicationToEdit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_medication);

        // Initialize views
        etMedicationName = findViewById(R.id.et_medication_name);
        etDosage = findViewById(R.id.et_dosage);
        etTime = findViewById(R.id.et_time);
        etInstructions = findViewById(R.id.et_instructions);
        rgReminderFrequency = findViewById(R.id.rg_reminder_frequency);
        rbDaily = findViewById(R.id.rb_daily);
        rbSpecificDays = findViewById(R.id.rb_specific_days);
        rbAsNeeded = findViewById(R.id.rb_as_needed);
        llWeekdays = findViewById(R.id.ll_weekdays);
        chipMon = findViewById(R.id.chip_mon);
        chipTue = findViewById(R.id.chip_tue);
        chipWed = findViewById(R.id.chip_wed);
        chipThu = findViewById(R.id.chip_thu);
        chipFri = findViewById(R.id.chip_fri);
        chipSat = findViewById(R.id.chip_sat);
        chipSun = findViewById(R.id.chip_sun);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);

        // Set up time picker
        etTime.setOnClickListener(v -> showTimePicker());

        // Set up radio buttons for frequency
        rgReminderFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_specific_days) {
                llWeekdays.setVisibility(View.VISIBLE);
            } else {
                llWeekdays.setVisibility(View.GONE);
            }

            // Show/hide time field based on "As needed" selection
            if (checkedId == R.id.rb_as_needed) {
                etTime.setText("");
                etTime.setHint("Time (Optional)");
            } else {
                etTime.setHint("Time");
            }
        });

        // Set up cancel button
        btnCancel.setOnClickListener(v -> dismiss());

        // Set up save button
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                Medication medication = createMedicationFromInputs();

                if (medicationToEdit != null) {
                    // We're editing an existing medication
                    medication.setId(medicationToEdit.getId());
                    if (listener != null) {
                        listener.onMedicationUpdated(medication);
                    }
                } else {
                    // We're adding a new medication
                    if (listener != null) {
                        listener.onMedicationAdded(medication);
                    }
                }

                dismiss();
            }
        });

        // If we're editing an existing medication, populate the fields
        if (medicationToEdit != null) {
            populateFields();
        }
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, selectedMinute);

                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    etTime.setText(sdf.format(calendar.getTime()));
                },
                hour,
                minute,
                false);

        timePickerDialog.show();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = etMedicationName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (name.isEmpty()) {
            etMedicationName.setError("Medication name is required");
            isValid = false;
        }

        if (dosage.isEmpty()) {
            etDosage.setError("Dosage is required");
            isValid = false;
        }

        int selectedRadioButtonId = rgReminderFrequency.getCheckedRadioButtonId();
        if (selectedRadioButtonId == R.id.rb_specific_days) {
            // Check if at least one day is selected
            boolean anyDaySelected = chipMon.isChecked() || chipTue.isChecked() || chipWed.isChecked() ||
                    chipThu.isChecked() || chipFri.isChecked() || chipSat.isChecked() || chipSun.isChecked();

            if (!anyDaySelected) {
                // Show error message for specific days
                isValid = false;
                // You might want to show a toast or some kind of message here
            }
        }

        // Time is optional for "As needed" medications
        if (time.isEmpty() && selectedRadioButtonId != R.id.rb_as_needed) {
            etTime.setError("Time is required");
            isValid = false;
        }

        return isValid;
    }

    private Medication createMedicationFromInputs() {
        String name = etMedicationName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();

        String frequency;
        int selectedRadioButtonId = rgReminderFrequency.getCheckedRadioButtonId();
        if (selectedRadioButtonId == R.id.rb_daily) {
            frequency = "Daily";
        } else if (selectedRadioButtonId == R.id.rb_specific_days) {
            frequency = "Specific days";
        } else {
            frequency = "As needed";
        }

        String days = "";
        if (frequency.equals("Specific days")) {
            List<String> selectedDays = new ArrayList<>();
            if (chipMon.isChecked()) selectedDays.add("Mon");
            if (chipTue.isChecked()) selectedDays.add("Tue");
            if (chipWed.isChecked()) selectedDays.add("Wed");
            if (chipThu.isChecked()) selectedDays.add("Thu");
            if (chipFri.isChecked()) selectedDays.add("Fri");
            if (chipSat.isChecked()) selectedDays.add("Sat");
            if (chipSun.isChecked()) selectedDays.add("Sun");

            StringBuilder daysBuilder = new StringBuilder();
            for (int i = 0; i < selectedDays.size(); i++) {
                daysBuilder.append(selectedDays.get(i));
                if (i < selectedDays.size() - 1) {
                    daysBuilder.append(",");
                }
            }
            days = daysBuilder.toString();
        }

        return new Medication(name, dosage, time, instructions, frequency, days, "Upcoming");
    }

    private void populateFields() {
        if (medicationToEdit == null) return;

        etMedicationName.setText(medicationToEdit.getName());
        etDosage.setText(medicationToEdit.getDosage());
        etTime.setText(medicationToEdit.getTime());
        etInstructions.setText(medicationToEdit.getInstructions());

        // Set the frequency radio button
        switch (medicationToEdit.getFrequency()) {
            case "Daily":
                rbDaily.setChecked(true);
                break;
            case "Specific days":
                rbSpecificDays.setChecked(true);
                llWeekdays.setVisibility(View.VISIBLE);

                // Select the days
                String days = medicationToEdit.getDays();
                if (days != null && !days.isEmpty()) {
                    if (days.contains("Mon")) chipMon.setChecked(true);
                    if (days.contains("Tue")) chipTue.setChecked(true);
                    if (days.contains("Wed")) chipWed.setChecked(true);
                    if (days.contains("Thu")) chipThu.setChecked(true);
                    if (days.contains("Fri")) chipFri.setChecked(true);
                    if (days.contains("Sat")) chipSat.setChecked(true);
                    if (days.contains("Sun")) chipSun.setChecked(true);
                }
                break;
            case "As needed":
                rbAsNeeded.setChecked(true);
                etTime.setHint("Time (Optional)");
                break;
        }
    }
}