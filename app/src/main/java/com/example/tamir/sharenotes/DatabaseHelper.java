package com.example.tamir.sharenotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "notes.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Creates a table with the ID, name of the notes, date and the notes content
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE_NOTES = "CREATE TABLE " + Notes.TABLE + "("
                + Notes.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Notes.KEY_name + " TEXT, "
                + Notes.KEY_date + " TEXT, "
                + Notes.KEY_content + " TEXT )";

        sqLiteDatabase.execSQL(CREATE_TABLE_NOTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Notes.TABLE);
        onCreate(sqLiteDatabase);
    }
}
