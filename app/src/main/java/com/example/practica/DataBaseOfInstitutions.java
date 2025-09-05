package com.example.practica;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataBaseOfInstitutions extends SQLiteOpenHelper {
    // Константы базы данных
    private static final String TAG = "InstitutionDB";
    public static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "InstitutionsDatabase.db";

    // Константы таблицы
    public static final String TABLE_INSTITUTIONS = "institutions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LOGIN = "login";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address_text";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_LNG = "longitude";
    public static final String COLUMN_SHORT_DESC = "short_description";
    public static final String COLUMN_FULL_DESC = "full_description";
    public static final String COLUMN_IMAGES = "image_uris";
    public static final String COLUMN_RATING = "avg_rating";
    public  static final String COLUMN_REVIEWS = "review_ids";

    private final Gson gson = new Gson();

    public DataBaseOfInstitutions(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_INSTITUTIONS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_LOGIN + " TEXT UNIQUE NOT NULL, " +
                        COLUMN_PASSWORD + " TEXT NOT NULL, " +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_ADDRESS + " TEXT NOT NULL, " +
                        COLUMN_LAT + " REAL DEFAULT 0, " +
                        COLUMN_LNG + " REAL DEFAULT 0, " +
                        COLUMN_SHORT_DESC + " TEXT, " +
                        COLUMN_FULL_DESC + " TEXT, " +
                        COLUMN_IMAGES + " TEXT, " +
                        COLUMN_RATING + " REAL DEFAULT 0, " +
                        COLUMN_REVIEWS + " TEXT" +
                        ");";

        try {
            db.execSQL(SQL_CREATE_TABLE);
            Log.i(TAG, "Table created successfully");
        } catch (SQLiteException e) {
            Log.e(TAG, "Error creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 9) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE " + TABLE_INSTITUTIONS + " RENAME TO temp_table");
                onCreate(db);

                String columns = String.join(", ",
                        COLUMN_ID,
                        COLUMN_LOGIN,
                        COLUMN_PASSWORD,
                        COLUMN_NAME,
                        COLUMN_ADDRESS,
                        COLUMN_LAT,
                        COLUMN_LNG,
                        COLUMN_SHORT_DESC,
                        COLUMN_FULL_DESC,
                        COLUMN_IMAGES
                );

                db.execSQL("INSERT INTO " + TABLE_INSTITUTIONS + " (" + columns + ") " +
                        "SELECT " + columns + " FROM temp_table");

                db.execSQL("DROP TABLE temp_table");
                db.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e(TAG, "Migration error: " + e.getMessage());
            } finally {
                db.endTransaction();
            }
        }
    }

    public boolean addOrUpdateInstitution(Institution institution) {
        if (institution.getId() > 0) {
            return updateInstitution(institution);
        } else {
            return addInstitution(institution);
        }
    }

    public boolean addInstitution(Institution institution) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_LOGIN, institution.getLogin());
        values.put(COLUMN_PASSWORD, institution.getPassword());
        values.put(COLUMN_NAME, institution.getName());
        values.put(COLUMN_ADDRESS, institution.getAddressText());
        values.put(COLUMN_LAT, institution.getLatitude());
        values.put(COLUMN_LNG, institution.getLongitude());
        values.put(COLUMN_SHORT_DESC, institution.getShortDescription());
        values.put(COLUMN_FULL_DESC, institution.getFullDescription());
        values.put(COLUMN_IMAGES, serializeList(institution.getImageUris()));

        try {
            long result = db.insert(TABLE_INSTITUTIONS, null, values);
            return result != -1;
        } catch (SQLiteException e) {
            Log.e(TAG, "Insert error: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    @SuppressLint("Range")
    public Institution getInstitutionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_INSTITUTIONS,
                null,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Institution institution = null;
        if (cursor.moveToFirst()) {
            institution = new Institution();
            institution.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            institution.setLogin(cursor.getString(cursor.getColumnIndex(COLUMN_LOGIN)));
            institution.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
            institution.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            institution.setAddressText(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
            institution.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT)));
            institution.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG)));
            institution.setShortDescription(cursor.getString(cursor.getColumnIndex(COLUMN_SHORT_DESC)));
            institution.setFullDescription(cursor.getString(cursor.getColumnIndex(COLUMN_FULL_DESC)));
            institution.setImageUris(deserializeList(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGES))));
        }

        cursor.close();
        db.close();
        return institution;
    }

    public boolean updateInstitution(Institution institution) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_LOGIN, institution.getLogin());
        values.put(COLUMN_PASSWORD, institution.getPassword());
        values.put(COLUMN_NAME, institution.getName());
        values.put(COLUMN_ADDRESS, institution.getAddressText());
        values.put(COLUMN_LAT, institution.getLatitude());
        values.put(COLUMN_LNG, institution.getLongitude());
        values.put(COLUMN_SHORT_DESC, institution.getShortDescription());
        values.put(COLUMN_FULL_DESC, institution.getFullDescription());
        values.put(COLUMN_IMAGES, serializeList(institution.getImageUris()));

        try {
            int rows = db.update(
                    TABLE_INSTITUTIONS,
                    values,
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(institution.getId())}
            );
            return rows > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Update error: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // SQL для создания таблицы
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_INSTITUTIONS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LOGIN + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_ADDRESS + " TEXT NOT NULL, " +
                    COLUMN_LAT + " REAL DEFAULT 0, " +
                    COLUMN_LNG + " REAL DEFAULT 0, " +
                    COLUMN_SHORT_DESC + " TEXT, " +
                    COLUMN_FULL_DESC + " TEXT, " +
                    COLUMN_IMAGES + " TEXT, " +
                    COLUMN_RATING + " REAL DEFAULT 0, " +
                    COLUMN_REVIEWS + " TEXT" +
                    ");";



    @SuppressLint("Range")
    private Institution cursorToInstitution(Cursor cursor) {
        Institution institution = new Institution();
        try {
            institution.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            institution.setLogin(cursor.getString(cursor.getColumnIndex(COLUMN_LOGIN)));
            institution.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
            institution.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            institution.setAddressText(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
            institution.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT)));
            institution.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG)));
            institution.setShortDescription(cursor.getString(cursor.getColumnIndex(COLUMN_SHORT_DESC)));
            institution.setFullDescription(cursor.getString(cursor.getColumnIndex(COLUMN_FULL_DESC)));
            institution.setImageUris(deserializeList(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGES))));
            institution.setAvgRating(cursor.getFloat(cursor.getColumnIndex(COLUMN_RATING)));
            institution.setReviewIds(deserializeReviews(cursor.getString(cursor.getColumnIndex(COLUMN_REVIEWS))));
        } catch (Exception e) {
            Log.e(TAG, "Error converting cursor: " + e.getMessage());
        }
        return institution;
    }



    // Удаление учреждения
    public boolean deleteInstitution(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rows = db.delete(
                    TABLE_INSTITUTIONS,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            return rows > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Delete error: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Проверка существования логина
    public boolean isLoginExists(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_INSTITUTIONS,
                new String[]{COLUMN_ID},
                COLUMN_LOGIN + " = ?",
                new String[]{login},
                null, null, null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Получение всех учреждений
    @SuppressLint("Range")
    public List<Institution> getAllInstitutions() {
        List<Institution> institutions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INSTITUTIONS, null);

        try {
            while (cursor.moveToNext()) {
                Institution institution = new Institution();
                institution.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                institution.setLogin(cursor.getString(cursor.getColumnIndex(COLUMN_LOGIN)));
                institution.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                institution.setAddressText(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
                institution.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT)));
                institution.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG)));
                institutions.add(institution);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting institutions: " + e.getMessage());
        } finally {
            cursor.close();
            db.close();
        }
        return institutions;
    }

    // Сериализация отзывов
    private String serializeReviews(List<Integer> reviews) {
        return reviews != null ? gson.toJson(reviews) : "[]";
    }

    // Десериализация отзывов
    private List<Integer> deserializeReviews(String json) {
        Type type = new TypeToken<ArrayList<Integer>>(){}.getType();
        return json != null ? gson.fromJson(json, type) : new ArrayList<>();
    }

    // Обновление рейтинга
    public boolean updateRating(int institutionId, float newRating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RATING, newRating);

        try {
            int rows = db.update(
                    TABLE_INSTITUTIONS,
                    values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(institutionId)}
            );
            return rows > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Rating update error: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Добавление отзыва
    public boolean addReview(int institutionId, int reviewId) {
        Institution institution = getInstitutionById(institutionId);
        if (institution == null) return false;

        List<Integer> reviews = institution.getReviewIds();
        reviews.add(reviewId);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEWS, serializeReviews(reviews));

        try {
            int rows = db.update(
                    TABLE_INSTITUTIONS,
                    values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(institutionId)}
            );
            return rows > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Add review error: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    @SuppressLint("Range")
    public Institution getInstitutionByLogin(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        Institution institution = null;

        Cursor cursor = db.query(
                TABLE_INSTITUTIONS,
                null,
                COLUMN_LOGIN + " = ?",
                new String[]{login},
                null, null, null
        );

        try {
            if (cursor.moveToFirst()) {
                institution = new Institution();
                institution.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                institution.setLogin(cursor.getString(cursor.getColumnIndex(COLUMN_LOGIN)));
                institution.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
                institution.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                institution.setAddressText(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
                institution.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT)));
                institution.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG)));
                institution.setShortDescription(cursor.getString(cursor.getColumnIndex(COLUMN_SHORT_DESC)));
                institution.setFullDescription(cursor.getString(cursor.getColumnIndex(COLUMN_FULL_DESC)));
                institution.setImageUris(deserializeList(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGES))));
            }
        } finally {
            cursor.close();
            db.close();
        }
        return institution;
    }

    private String serializeList(List<String> list) {
        return gson.toJson(list);
    }

    private List<String> deserializeList(String json) {
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        return gson.fromJson(json, type);
    }
}