package com.example.tamir.sharenotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class NotesRepo {
    private DatabaseHelper databaseHelper;

    public NotesRepo(Context context){
        databaseHelper = new DatabaseHelper(context);
    }


    //Insert statement for SQLite
    public int insert(Notes notes){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Notes.KEY_name, notes.name);
        values.put(Notes.KEY_date, notes.date);
        values.put(Notes.KEY_content, notes.content);

        long notes_Id = db.insert(Notes.TABLE, null, values);
        db.close();
        return (int) notes_Id;
    }

    //Delete statement for SQLite
    public void  delete(int notes_Id){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(Notes.TABLE, Notes.KEY_ID + "= ?", new String[]{ String.valueOf(notes_Id)});
        db.close();
    }

    //Update message statement for SQLite
    public void update(Notes notes){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Notes.KEY_name, notes.name);
        values.put(Notes.KEY_date, notes.date);
        values.put(Notes.KEY_content, notes.content);
        db.update(Notes.TABLE, values, Notes.KEY_ID + "= ?", new String[]{ String.valueOf(notes.notes_ID)});
    }


    public ArrayList<HashMap<String, String>> getNotesList(){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String selectQuery =  "SELECT  " +
                 Notes.KEY_ID + "," +
                 Notes.KEY_name + "," +
                 Notes.KEY_date + "," +
                 Notes.KEY_content +
                 " FROM " + Notes.TABLE;

        ArrayList<HashMap<String, String>> notesList = new ArrayList<HashMap<String, String>>();


        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do{
                HashMap<String, String> notes = new HashMap<String, String>();
                notes.put("id", cursor.getString(cursor.getColumnIndex(Notes.KEY_ID)));
                notes.put("name", cursor.getString(cursor.getColumnIndex(Notes.KEY_name)));
                notesList.add(notes);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notesList;
    }

    //Method used to select query
    public Notes getNotesById(int Id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String selectQuery = "SELECT  " +
                 Notes.KEY_ID + "," +
                 Notes.KEY_name + "," +
                 Notes.KEY_date + "," +
                 Notes.KEY_content +
                 " FROM " + Notes.TABLE
                 + " WHERE " +
                 Notes.KEY_ID + "=?";

        int iCount = 0;
        Notes notes = new Notes();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Id)});
        if (cursor.moveToFirst()) {
            do {
                notes.notes_ID = cursor.getInt(cursor.getColumnIndex(Notes.KEY_ID));
                notes.name = cursor.getString(cursor.getColumnIndex(Notes.KEY_name));
                notes.date = cursor.getString(cursor.getColumnIndex(Notes.KEY_date));
                notes.content = cursor.getString(cursor.getColumnIndex(Notes.KEY_content));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notes;
    }
}
