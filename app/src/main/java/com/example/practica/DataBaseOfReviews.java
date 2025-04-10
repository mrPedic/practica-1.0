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

public class DataBaseOfReviews extends SQLiteOpenHelper {
    // Унифицированное имя базы данных
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "AppDatabase.db";

    // SQL для создания таблицы отзывов
    private static final String SQL_CREATE_REVIEWS =
            "CREATE TABLE " + ReviewTable.TABLE_NAME + " (" +
                    ReviewTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ReviewTable.INSTITUTION_ID + " INTEGER NOT NULL," +
                    ReviewTable.IMAGE_URL + " TEXT," +
                    ReviewTable.RATING + " REAL NOT NULL," +
                    ReviewTable.TEXT_REVIEW + " TEXT NOT NULL," +
                    ReviewTable.ADDRESS_TEXT + " TEXT," +
                    ReviewTable.ADDRESS_LINK + " TEXT," +
                    ReviewTable.USERNAME + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + ReviewTable.INSTITUTION_ID + ") REFERENCES " +
                    DataBaseOfInstitutions.InstitutionTable.TABLE_NAME +
                    "(" + DataBaseOfInstitutions.InstitutionTable._ID + ") ON DELETE CASCADE);";

    // SQL для создания таблицы заведений
    private static final String SQL_CREATE_INSTITUTIONS =
            "CREATE TABLE " + DataBaseOfInstitutions.InstitutionTable.TABLE_NAME + " (" +
                    DataBaseOfInstitutions.InstitutionTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DataBaseOfInstitutions.InstitutionTable.NAME + " TEXT NOT NULL," +
                    DataBaseOfInstitutions.InstitutionTable.ADDRESS_TEXT + " TEXT NOT NULL," +
                    DataBaseOfInstitutions.InstitutionTable.ADDRESS_LINK + " TEXT);";

    public DataBaseOfReviews(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        // Создаем обе таблицы
        db.execSQL(SQL_CREATE_INSTITUTIONS);
        db.execSQL(SQL_CREATE_REVIEWS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ReviewTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataBaseOfInstitutions.InstitutionTable.TABLE_NAME);
        onCreate(db);
    }


    @SuppressLint("Range")
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            String query = "SELECT " +
                    "r." + ReviewTable._ID + ", " +
                    "r." + ReviewTable.INSTITUTION_ID + ", " +
                    "r." + ReviewTable.RATING + ", " +
                    "r." + ReviewTable.TEXT_REVIEW + ", " +
                    "r." + ReviewTable.ADDRESS_TEXT + ", " +
                    "r." + ReviewTable.ADDRESS_LINK + ", " +
                    "r." + ReviewTable.USERNAME + ", " +
                    "r." + ReviewTable.IMAGE_URL + ", " + // Добавлено получение image_url
                    "i." + DataBaseOfInstitutions.InstitutionTable.NAME + " AS institution_name " +
                    "FROM " + ReviewTable.TABLE_NAME + " r " +
                    "LEFT JOIN " + DataBaseOfInstitutions.InstitutionTable.TABLE_NAME + " i " + // Изменено на LEFT JOIN
                    "ON r." + ReviewTable.INSTITUTION_ID + " = i." + DataBaseOfInstitutions.InstitutionTable._ID;

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Review review = new Review(
                            cursor.getInt(cursor.getColumnIndex(ReviewTable._ID)),
                            cursor.getInt(cursor.getColumnIndex(ReviewTable.INSTITUTION_ID)),
                            cursor.getFloat(cursor.getColumnIndex(ReviewTable.RATING)),
                            getStringOrEmpty(cursor, ReviewTable.TEXT_REVIEW),
                            getStringOrEmpty(cursor, ReviewTable.ADDRESS_TEXT),
                            getStringOrEmpty(cursor, ReviewTable.ADDRESS_LINK),
                            getStringOrEmpty(cursor, ReviewTable.USERNAME)
                    );
                    review.setInstitutionName(getStringOrEmpty(cursor, "institution_name"));
                    review.setImageUrl(getStringOrEmpty(cursor, ReviewTable.IMAGE_URL));
                    reviews.add(review);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка получения отзывов: ", e); // Добавлено логирование исключения
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return reviews;
    }

    // Вспомогательный метод для безопасного получения строк
    private String getStringOrEmpty(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index != -1 ? cursor.getString(index) : "";
    }

    public void updateReviewAction(int reviewId, boolean isLiked) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ReviewTable.RATING, isLiked ? 5 : 1);
            db.update(ReviewTable.TABLE_NAME, values, ReviewTable._ID + " = ?", new String[]{String.valueOf(reviewId)});
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка при обновлении отзыва", e);
        } finally {
            if (db != null) db.close();
        }
    }

    public boolean addReview(Review review) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            // Обязательные поля
            values.put(ReviewTable.INSTITUTION_ID, review.getInstitutionId());
            values.put(ReviewTable.RATING, review.getRating());
            values.put(ReviewTable.TEXT_REVIEW, review.getTextReview());
            values.put(ReviewTable.ADDRESS_TEXT, review.getAddressText());
            values.put(ReviewTable.ADDRESS_LINK, review.getAddressLink());
            values.put(ReviewTable.USERNAME, review.getUsername());
            values.put(ReviewTable.IMAGE_URL, review.getImageUrl()); // добавить эту строку

            // Вставка в БД
            long result = db.insert(ReviewTable.TABLE_NAME, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка записи: " + e.getMessage());
            return false;
        } finally {
            if (db != null) db.close();
        }
    }


    public static class ReviewTable {
        public static final String TABLE_NAME = "reviews";
        public static final String _ID = "id";
        public static final String INSTITUTION_ID = "institution_id";
        public static final String IMAGE_URL = "image_url";
        public static final String RATING = "rating";
        public static final String TEXT_REVIEW = "text_review";
        public static final String ADDRESS_TEXT = "address_text";
        public static final String ADDRESS_LINK = "address_link";
        public static final String USERNAME = "username";
    }
}
