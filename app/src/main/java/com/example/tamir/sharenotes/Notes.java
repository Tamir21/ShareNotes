package com.example.tamir.sharenotes;

public class Notes {

    //The values that will be saved to the SQLite database

    public static final String TABLE = "Notes";

    public static final String KEY_ID = "id";
    public static final String KEY_name = "name";
    public static final String KEY_content = "content";
    public static final String KEY_date = "date";

    public int notes_ID;
    public String name;
    public String content;
    public String date;
}

