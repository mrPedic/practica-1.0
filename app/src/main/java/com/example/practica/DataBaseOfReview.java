package com.example.practica;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseOfReview extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DataBaseOdInstitutions.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + ReviewTable.TABLE_NAME + " (" +
                    ReviewTable._ID + " INTEGER PRIMARY KEY," +
                    ReviewTable.LOGIN + " TEXT UNIQUE," +
                    ReviewTable.PASSWORD + " TEXT," +
                    ReviewTable.ROLE + " TEXT CHECK(" + DataBaseOfUsers.UserTable.ROLE + " IN ('institution','human','admin'))," +
                    ReviewTable.LIKES_INSTITUTION + " INTEGER DEFAULT 0," +
                    ReviewTable.FOLLOWS + " INTEGER DEFAULT 0," +
                    ReviewTable.USERNAME + " TEXT," +
                    ReviewTable.LIKES_THEMES + " INTEGER DEFAULT 0)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + DataBaseOfInstitutions.InstitutionTable.TABLE_NAME;

    public DataBaseOfReview(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + ReviewTable.TABLE_NAME +
                    " ADD COLUMN " + ReviewTable.ROLE + " TEXT");
            db.execSQL("ALTER TABLE " + ReviewTable.TABLE_NAME +
                    " ADD COLUMN " + ReviewTable.LIKES_INSTITUTION + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + ReviewTable.TABLE_NAME +
                    " ADD COLUMN " + ReviewTable.FOLLOWS + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + ReviewTable.TABLE_NAME +
                    " ADD COLUMN " + ReviewTable.USERNAME + " TEXT");
            db.execSQL("ALTER TABLE " + ReviewTable.TABLE_NAME +
                    " ADD COLUMN " + ReviewTable.LIKES_THEMES + " INTEGER DEFAULT 0");
        } else {
            db.execSQL(SQL_DELETE_TABLE);
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static class ReviewTable {
        public static final String TABLE_NAME = "users";
        public static final String _ID = "id";
        public static final String LOGIN = "login";
        public static final String PASSWORD = "password";
        public static final String ROLE = "role";
        public static final String LIKES_INSTITUTION = "likesInstitution";
        public static final String FOLLOWS = "follows";
        public static final String USERNAME = "username";
        public static final String LIKES_THEMES = "likesThemes";
    }
}