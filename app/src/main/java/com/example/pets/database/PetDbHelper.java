package com.example.pets.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.pets.database.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pets.db";
    private static final int DATABASE_VERSION = 1;


    public PetDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_QUERY = "CREATE TABLE " + PetEntry.TABLE_NAME + " (" +
                PetEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PetEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                PetEntry.COLUMN_BREED + " TEXT, " +
                PetEntry.COLUMN_GENDER + " INTEGER NOT NULL, " +
                PetEntry.COLUMN_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
