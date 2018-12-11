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
import android.text.TextUtils;
import android.util.Log;

import com.example.android.inventoryapp.Data.ItemsContract.ItemsEntry;

import java.security.Provider;

public class BooksProvider extends ContentProvider {
    public static final String LOG_TAG = BooksProvider.class.getSimpleName();
    private static final int BOOKS = 100;
    private static final int BOOK_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY, ItemsContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY, ItemsContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    private BooksDbHelper mDbHelper;

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
                cursor = database.query(ItemsEntry.TABLE_NAME, projection, selection, selectionArgs, null,
                        null, sortOrder);
                break;
            case BOOK_ID:
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ItemsEntry.TABLE_NAME, projection, selection, selectionArgs,
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
                return ItemsEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return ItemsEntry.CONTENT_ITEM_TYPE;
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

    private Uri insertBook(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Check that the name is not null
        String name = values.getAsString(ItemsEntry.COLUMN_PRODUCT_NAME);
        if (name == null || TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Book requires a name");
        }

        // Check that the price  cannot be -ve.
        String priceInString = values.getAsString(ItemsEntry.COLUMN_PRICE);
        double price = priceInString != null && !TextUtils.isEmpty(priceInString) ? Double.parseDouble(priceInString) : 0;
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative or zero");
        }
        // Check that the quantity cannot be -ve.
        String quantityInString = values.getAsString(ItemsEntry.COLUMN_QUANTITY);
        int quantity = quantityInString != null && !TextUtils.isEmpty(quantityInString) ? Integer.parseInt(quantityInString) : 0;
        if (quantity < 0) {
            throw new IllegalArgumentException("Book requires a quantity");
        }

        // Check that the name is not null
        String supplierName = values.getAsString(ItemsEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null || TextUtils.isEmpty(supplierName)) {
            throw new IllegalArgumentException("Book requires a supplier name");
        }
        // Check that the name is not null
        String supplierPhoneNumber = values.getAsString(ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (supplierPhoneNumber == null) {
            throw new IllegalArgumentException("Book requires a supplierPhoneNumber");
        }
        // Insert the new pet with the given values
        long id = database.insert(ItemsEntry.TABLE_NAME, null, values);
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
                rowsDeleted = database.delete(ItemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ItemsEntry.TABLE_NAME, selection, selectionArgs);
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
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link ItemsEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ItemsEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ItemsEntry.COLUMN_PRODUCT_NAME);
            if (name == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }

        // If the {@link ItemsEntry#COLUMN_PRODUCT_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(ItemsEntry.COLUMN_PRICE) && values.getAsString(ItemsEntry.COLUMN_PRICE).length() > 0) {
            double price = values.getAsDouble(ItemsEntry.COLUMN_PRICE);
            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be zero or negative");
            }
        }

        // If the {@link ItemsEntry#COLUMN_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(ItemsEntry.COLUMN_QUANTITY)) {
            // Check that the quantity cannot be -ve.
            int quantity = Integer.parseInt(values.getAsString(ItemsEntry.COLUMN_QUANTITY));
            if (quantity < 0) {
                throw new IllegalArgumentException("Book requires valid quantity");
            }
        }

        // If the {@link ItemsEntry#COLUMN_SUPPLIER_NAME} key is present,
        // check that the supplier name value is not null.
        if (values.containsKey(ItemsEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(ItemsEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null || TextUtils.isEmpty(supplierName)) {
                throw new IllegalArgumentException("Book requires a supplier name");
            }
        }
        // If the {@link ItemsEntry#COLUMN_SUPPLIER_PHONE_NUMBER} key is present,
        // check that the supplier phone number value is not null.
        if (values.containsKey(ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String supplierPhoneNumber = values.getAsString(ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (TextUtils.isEmpty(supplierPhoneNumber)) {
                throw new IllegalArgumentException("Book requires a supplier's valid phone Number");
            }
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ItemsEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}




