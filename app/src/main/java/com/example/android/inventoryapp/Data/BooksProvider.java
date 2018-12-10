package com.example.android.inventoryapp.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.security.Provider;

public class BooksProvider extends ContentProvider {


    private static final int BOOKS = 100;
    private static final int BOOK_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY, ItemsContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY, ItemsContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    private BooksDbHelper mDbHelper;
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = BooksProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        mDbHelper = new BooksDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(ItemsContract.ItemsEntry.TABLE_NAME, projection, selection, selectionArgs, null,
                        null, sortOrder);
                break;
            case BOOK_ID:
                selection = ItemsContract.ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ItemsContract.ItemsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return ItemsContract.ItemsEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return ItemsContract.ItemsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        String name = values.getAsString(ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Book requires a name");
        }
        String suppliername = values.getAsString(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME);
        if (suppliername == null) {
            throw new IllegalArgumentException("Supplier requires a name");
        }
        Integer price = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_PRICE);
        if (price == null || !ItemsContract.ItemsEntry.isPositive(price)) {
            throw new IllegalArgumentException("Pet requires valid non negative price value");
        }
        Integer quantity = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_QUANTITY);
        if (quantity == null || !ItemsContract.ItemsEntry.isPositive(quantity)) {
            throw new IllegalArgumentException("Pet requires valid non negative quantity value");
        }
        Integer phone = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (phone == null || !ItemsContract.ItemsEntry.isPositive(phone)) {
            throw new IllegalArgumentException("Pet requires valid non negative phone num");
        }

        // Insert the new pet with the given values
        long id = database.insert(ItemsContract.ItemsEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ItemsContract.ItemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = ItemsContract.ItemsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ItemsContract.ItemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ItemsContract.ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        String name = values.getAsString(ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Book requires a name");
        }
        String suppliername = values.getAsString(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME);
        if (suppliername == null) {
            throw new IllegalArgumentException("Supplier requires a name");
        }
        Integer price = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_PRICE);
        if (price == null || !ItemsContract.ItemsEntry.isPositive(price)) {
            throw new IllegalArgumentException("Pet requires valid non negative price value");
        }
        Integer quantity = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_QUANTITY);
        if (quantity == null || !ItemsContract.ItemsEntry.isPositive(quantity)) {
            throw new IllegalArgumentException("Pet requires valid non negative quantity value");
        }
        Integer phone = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (phone == null || !ItemsContract.ItemsEntry.isPositive(phone)) {
            throw new IllegalArgumentException("Pet requires valid non negative phone num");
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;

        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ItemsContract.ItemsEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}




