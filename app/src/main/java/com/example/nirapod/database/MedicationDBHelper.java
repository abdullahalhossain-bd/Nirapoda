package com.example.nirapod.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nirapod.models.Medication;

import java.util.ArrayList;
import java.util.List;

public class MedicationDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medications.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_MEDICATIONS = "medications";

    // Table columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DOSAGE = "dosage";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_INSTRUCTIONS = "instructions";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_DAYS = "days";
    public static final String COLUMN_STATUS = "status";

    // Create table statement
    private static final String CREATE_TABLE_MEDICATIONS = "CREATE TABLE " + TABLE_MEDICATIONS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_DOSAGE + " TEXT NOT NULL, "
            + COLUMN_TIME + " TEXT, "
            + COLUMN_INSTRUCTIONS + " TEXT, "
            + COLUMN_FREQUENCY + " TEXT NOT NULL, "
            + COLUMN_DAYS + " TEXT, "
            + COLUMN_STATUS + " TEXT DEFAULT 'Upcoming'"
            + ");";

    public MedicationDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEDICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        onCreate(db);
    }

    // Insert a new medication
    public long insertMedication(Medication medication) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, medication.getName());
        values.put(COLUMN_DOSAGE, medication.getDosage());
        values.put(COLUMN_TIME, medication.getTime());
        values.put(COLUMN_INSTRUCTIONS, medication.getInstructions());
        values.put(COLUMN_FREQUENCY, medication.getFrequency());
        values.put(COLUMN_DAYS, medication.getDays());
        values.put(COLUMN_STATUS, medication.getStatus());

        long id = db.insert(TABLE_MEDICATIONS, null, values);
        db.close();
        return id;
    }

    // Get all medications
    public List<Medication> getAllMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDICATIONS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Medication medication = new Medication();
                medication.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                medication.setDosage(cursor.getString(cursor.getColumnIndex(COLUMN_DOSAGE)));
                medication.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
                medication.setInstructions(cursor.getString(cursor.getColumnIndex(COLUMN_INSTRUCTIONS)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndex(COLUMN_FREQUENCY)));
                medication.setDays(cursor.getString(cursor.getColumnIndex(COLUMN_DAYS)));
                medication.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));

                medicationList.add(medication);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return medicationList;
    }

    // Get medications due today
    public List<Medication> getMedicationsDueToday(String today) {
        List<Medication> medicationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // For daily medications
        String dailyQuery = "SELECT * FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_FREQUENCY + " = 'Daily'";

        // For specific days medications
        String specificQuery = "SELECT * FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_FREQUENCY + " = 'Specific days'" +
                " AND " + COLUMN_DAYS + " LIKE '%" + today + "%'";

        // For as needed medications (include all)
        String asNeededQuery = "SELECT * FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_FREQUENCY + " = 'As needed'";

        // Execute all queries and combine results
        Cursor dailyCursor = db.rawQuery(dailyQuery, null);
        addMedicationsFromCursor(medicationList, dailyCursor);

        Cursor specificCursor = db.rawQuery(specificQuery, null);
        addMedicationsFromCursor(medicationList, specificCursor);

        Cursor asNeededCursor = db.rawQuery(asNeededQuery, null);
        addMedicationsFromCursor(medicationList, asNeededCursor);

        db.close();
        return medicationList;
    }

    private void addMedicationsFromCursor(List<Medication> medicationList, Cursor cursor) {
        if (cursor.moveToFirst()) {
            do {
                Medication medication = new Medication();
                medication.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                medication.setDosage(cursor.getString(cursor.getColumnIndex(COLUMN_DOSAGE)));
                medication.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
                medication.setInstructions(cursor.getString(cursor.getColumnIndex(COLUMN_INSTRUCTIONS)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndex(COLUMN_FREQUENCY)));
                medication.setDays(cursor.getString(cursor.getColumnIndex(COLUMN_DAYS)));
                medication.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));

                medicationList.add(medication);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // Get medication by ID
    public Medication getMedicationById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MEDICATIONS,
                null,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) },
                null, null, null);

        Medication medication = null;

        if (cursor != null && cursor.moveToFirst()) {
            medication = new Medication();
            medication.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            medication.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            medication.setDosage(cursor.getString(cursor.getColumnIndex(COLUMN_DOSAGE)));
            medication.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
            medication.setInstructions(cursor.getString(cursor.getColumnIndex(COLUMN_INSTRUCTIONS)));
            medication.setFrequency(cursor.getString(cursor.getColumnIndex(COLUMN_FREQUENCY)));
            medication.setDays(cursor.getString(cursor.getColumnIndex(COLUMN_DAYS)));
            medication.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
            cursor.close();
        }

        db.close();
        return medication;
    }

    // Update medication
    public int updateMedication(Medication medication) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, medication.getName());
        values.put(COLUMN_DOSAGE, medication.getDosage());
        values.put(COLUMN_TIME, medication.getTime());
        values.put(COLUMN_INSTRUCTIONS, medication.getInstructions());
        values.put(COLUMN_FREQUENCY, medication.getFrequency());
        values.put(COLUMN_DAYS, medication.getDays());
        values.put(COLUMN_STATUS, medication.getStatus());

        int result = db.update(TABLE_MEDICATIONS,
                values,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(medication.getId()) });

        db.close();
        return result;
    }

    // Update medication status
    public int updateMedicationStatus(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_STATUS, status);

        int result = db.update(TABLE_MEDICATIONS,
                values,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) });

        db.close();
        return result;
    }

    // Delete medication
    public void deleteMedication(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDICATIONS,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    // Count medications due today
    public int countMedicationsDueToday(String today) {
        int dailyCount = 0;
        int specificDaysCount = 0;
        int asNeededCount = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        // Count daily medications
        String dailyQuery = "SELECT COUNT(*) FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_FREQUENCY + " = 'Daily'";
        Cursor dailyCursor = db.rawQuery(dailyQuery, null);
        if (dailyCursor.moveToFirst()) {
            dailyCount = dailyCursor.getInt(0);
        }
        dailyCursor.close();

        // Count specific days medications
        String specificQuery = "SELECT COUNT(*) FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_FREQUENCY + " = 'Specific days'" +
                " AND " + COLUMN_DAYS + " LIKE '%" + today + "%'";
        Cursor specificCursor = db.rawQuery(specificQuery, null);
        if (specificCursor.moveToFirst()) {
            specificDaysCount = specificCursor.getInt(0);
        }
        specificCursor.close();

        // Count as needed medications
        String asNeededQuery = "SELECT COUNT(*) FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_FREQUENCY + " = 'As needed'";
        Cursor asNeededCursor = db.rawQuery(asNeededQuery, null);
        if (asNeededCursor.moveToFirst()) {
            asNeededCount = asNeededCursor.getInt(0);
        }
        asNeededCursor.close();

        db.close();

        return dailyCount + specificDaysCount + asNeededCount;
    }
}