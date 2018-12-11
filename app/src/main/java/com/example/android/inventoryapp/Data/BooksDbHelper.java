package com.example.android.inventoryapp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BooksDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "books.db";
    private static final int DATABASE_VERSION = 1;

    public BooksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("DbHelper", "onCreate: ");
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + ItemsContract.ItemsEntry.TABLE_NAME + " ("
                + ItemsContract.ItemsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ItemsContract.ItemsEntry.COLUMN_PRICE + " INTEGER, "
                + ItemsContract.ItemsEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                + ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL,"
                + ItemsContract.ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " INTEGER NOT NULL );";
        db.execSQL(SQL_CREATE_BOOKS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
