package com.example.practica;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseOfUsers extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "DataBaseOfUsers.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + UserTable.TABLE_NAME + " (" +
                    UserTable._ID + " INTEGER PRIMARY KEY," +
                    UserTable.LOGIN + " TEXT UNIQUE," +
                    UserTable.PASSWORD + " TEXT," +
                    UserTable.ROLE + " TEXT CHECK(" + UserTable.ROLE + " IN ('institution','human','admin'))," +
                    UserTable.LIKES_INSTITUTION + " INTEGER DEFAULT 0," +
                    UserTable.FOLLOWS + " INTEGER DEFAULT 0," +
                    UserTable.USERNAME + " TEXT," +
                    UserTable.LIKES_THEMES + " INTEGER DEFAULT 0)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + UserTable.TABLE_NAME;

    // SQL для добавления администратора
    private static final String SQL_INSERT_ADMIN =
            "INSERT INTO " + UserTable.TABLE_NAME + " (" +
                    UserTable._ID + ", " +
                    UserTable.LOGIN + ", " +
                    UserTable.PASSWORD + ", " +
                    UserTable.ROLE + ") " +
                    "VALUES (0, 'mrPedic', '5422f8aa', 'admin')";

    public DataBaseOfUsers(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
        db.execSQL(SQL_INSERT_ADMIN);  // Добавляем администратора при создании БД
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + UserTable.TABLE_NAME +
                    " ADD COLUMN " + UserTable.ROLE + " TEXT");
            db.execSQL("ALTER TABLE " + UserTable.TABLE_NAME +
                    " ADD COLUMN " + UserTable.LIKES_INSTITUTION + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + UserTable.TABLE_NAME +
                    " ADD COLUMN " + UserTable.FOLLOWS + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + UserTable.TABLE_NAME +
                    " ADD COLUMN " + UserTable.USERNAME + " TEXT");
            db.execSQL("ALTER TABLE " + UserTable.TABLE_NAME +
                    " ADD COLUMN " + UserTable.LIKES_THEMES + " INTEGER DEFAULT 0");

            // Если нужно, чтобы админ был и после обновления:
            db.execSQL(SQL_INSERT_ADMIN);
        } else {
            db.execSQL(SQL_DELETE_TABLE);
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static class UserTable {
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