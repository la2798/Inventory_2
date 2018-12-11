package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventoryapp.Data.ItemsContract;

public class BookCursorAdapter extends CursorAdapter {

    String bookQuantity;

    public BookCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, 0/*flags*/);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        Button saleButton = view.findViewById(R.id.sale_button);
        int nameColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_QUANTITY);
        String bookName = cursor.getString(nameColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        bookQuantity = cursor.getString(quantityColumnIndex);

        nameTextView.setText(bookName);
        priceTextView.setText("Rs. " + bookPrice);
        quantityTextView.setText("Quantity in stock = " + bookQuantity);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View parentRow = (View) view.getParent();
                ListView listView = (ListView) parentRow.getParent();
                int position = listView.getPositionForView(parentRow);
                int keyColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry._ID);
                try {
                    cursor.moveToFirst();
                } catch (IllegalStateException e) {
                    Log.e("sale Button", "Attempt failed to re-open an already-closed cursor object");
                    return;
                }
                long key = 0;
                for (int i = 1; i <= position + 1; i++) {
                    key = cursor.getLong(keyColumnIndex);
                    cursor.moveToNext();
                }
                Log.v("sale button", "Current row ID or Primary key of database is: " + key);
                cursor.moveToPosition(position);
                int quantityColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_QUANTITY);
                String currentQuantity = cursor.getString(quantityColumnIndex);
                int currentQuantityInt = Integer.parseInt(currentQuantity);
                if (currentQuantityInt > 0) {
                    currentQuantityInt -= 1;
                    Log.v("sale button", "Current quantity is: " + currentQuantityInt + " and changing to: " + currentQuantityInt);
                    ContentValues values = new ContentValues();
                    values.put(ItemsContract.ItemsEntry.COLUMN_QUANTITY, currentQuantityInt);
                    Uri currentProductUri = ContentUris.withAppendedId(ItemsContract.ItemsEntry.CONTENT_URI, key);
                    context.getContentResolver().update(currentProductUri, values, null, null);
                } else {
                    Log.v("sale button", "Quantity cannot be decreased further.");
                }
            }
        });
    }
}
