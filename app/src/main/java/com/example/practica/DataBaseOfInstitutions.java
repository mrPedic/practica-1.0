package com.example.practica;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseOfInstitutions extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DataBaseOfInstitutions.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + InstitutionTable.TABLE_NAME + " (" +
                    InstitutionTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    InstitutionTable.NAME + " TEXT NOT NULL," +
                    InstitutionTable.ADDRESS_TEXT + " TEXT NOT NULL," +
                    InstitutionTable.ADDRESS_LINK + " TEXT," +
                    InstitutionTable.SHORT_DESCRIPTION + " TEXT," +
                    InstitutionTable.FULL_DESCRIPTION + " TEXT," +
                    InstitutionTable.AVG_RATING + " REAL DEFAULT 0," +
                    InstitutionTable.REVIEW_IDS + " TEXT" + // JSON array с ID отзывов
                    ")";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + InstitutionTable.TABLE_NAME;

    public DataBaseOfInstitutions(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
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