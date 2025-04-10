package com.example.practica;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataBaseOfInstitutions extends SQLiteOpenHelper {
    // Увеличьте версию базы данных
    public static final int DATABASE_VERSION = 3; // Было 2
    public static final String DATABASE_NAME = "DataBaseOfInstitutions.db";

    // Исправленный SQL-запрос (добавлены недостающие запятые)
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + InstitutionTable.TABLE_NAME + " (" +
                    InstitutionTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    InstitutionTable.NAME + " TEXT NOT NULL," +
                    InstitutionTable.ADDRESS_TEXT + " TEXT NOT NULL," +
                    InstitutionTable.ADDRESS_LINK + " TEXT," +
                    InstitutionTable.SHORT_DESCRIPTION + " TEXT," +
                    InstitutionTable.FULL_DESCRIPTION + " TEXT," +
                    InstitutionTable.AVG_RATING + " REAL DEFAULT 0," +
                    InstitutionTable.REVIEW_IDS + " TEXT" +
                    ");"; // Добавлена точка с запятой

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_TABLE);
            // Тестовые данные (опционально)
            ContentValues values = new ContentValues();
            values.put(InstitutionTable.NAME, "Тестовое заведение");
            values.put(InstitutionTable.ADDRESS_TEXT, "ул. Тестовая, 1");
            db.insert(InstitutionTable.TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e("DB", "Error creating table: " + e.getMessage());
        }
    }

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + InstitutionTable.TABLE_NAME;

    public DataBaseOfInstitutions(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db); // Полная пересоздание таблицы
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @SuppressLint("Range")
    public List<Institution> getAllInstitutions() {
        List<Institution> institutions = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    InstitutionTable.TABLE_NAME,
                    new String[]{
                            InstitutionTable._ID,
                            InstitutionTable.NAME,
                            InstitutionTable.ADDRESS_TEXT,
                            InstitutionTable.ADDRESS_LINK
                    },
                    null, null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Institution institution = new Institution();
                    institution.setId(cursor.getInt(cursor.getColumnIndex(InstitutionTable._ID)));
                    institution.setName(cursor.getString(cursor.getColumnIndex(InstitutionTable.NAME)));
                    institution.setAddress_link(cursor.getString(cursor.getColumnIndex(InstitutionTable.ADDRESS_LINK)));
                    institution.setAddress_text(cursor.getString(cursor.getColumnIndex(InstitutionTable.ADDRESS_TEXT)));
                    institutions.add(institution);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB", "Error getting institutions: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return institutions;
    }

    public boolean addInstitution(Institution institution) {
        SQLiteDatabase db = null;
        boolean transactionStarted = false;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            transactionStarted = true;

            ContentValues values = new ContentValues();
            values.put(InstitutionTable._ID, institution.getId());
            values.put(InstitutionTable.NAME,institution.getName());
            values.put(InstitutionTable.ADDRESS_LINK,institution.getAddress_link());
            values.put(InstitutionTable.ADDRESS_TEXT,institution.getAddress_text());
            values.put(InstitutionTable.SHORT_DESCRIPTION,institution.getShortDescription());
            values.put(InstitutionTable.FULL_DESCRIPTION,institution.getFullDescription());
            values.put(InstitutionTable.REVIEW_IDS,institution.getReviewsIds());

            long result = db.insert(InstitutionTable.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                if (transactionStarted) {
                    db.endTransaction();
                }
                db.close();
            }
        }
    }

    public long insertInstitution(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(InstitutionTable.TABLE_NAME, null, values);
        db.close();
        return result;
    }


    public static class InstitutionTable {
        public static final String TABLE_NAME = "institutions";
        public static final String _ID = "id";
        public static final String NAME = "name";
        public static final String ADDRESS_TEXT = "address_text";
        public static final String ADDRESS_LINK = "address_link";
        public static final String SHORT_DESCRIPTION = "short_description";
        public static final String FULL_DESCRIPTION = "full_description";
        public static final String AVG_RATING = "avg_rating";
        public static final String REVIEW_IDS = "review_ids";
    }
}