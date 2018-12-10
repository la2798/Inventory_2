package com.example.android.inventoryapp;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.Data.ItemsContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_BOOK_LOADER = 0;
    EditText mbookName;
    EditText mbookPrice;
    EditText mQuantity;
    EditText mSupplierName;
    EditText mSupplierNumber;
    Button mIncreaseButton;
    Button mDecreaseButton;
    Button orderButton;
    private boolean mBookHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };
    private Uri mCurrentBookUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.addbook));
            invalidateOptionsMenu();
        } else {

            Log.e("CHECK", "Setting the title");
            setTitle(getString(R.string.aboutbook));
        }

        mbookName = findViewById(R.id.bookName);
        mbookPrice = findViewById(R.id.bookPrice);
        mQuantity = findViewById(R.id.quantity);
        mSupplierName = findViewById(R.id.supplierName);
        mSupplierNumber = findViewById(R.id.supplierPhoneNumber);

        mbookName.setOnTouchListener(mTouchListener);
        mbookPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierNumber.setOnTouchListener(mTouchListener);

        mIncreaseButton=findViewById(R.id.increase_quantity);
        mDecreaseButton=findViewById(R.id.decrease_quantity);
        orderButton=findViewById(R.id.order_button);


        if (mCurrentBookUri != null)
            getSupportLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    private void saveBook() {
        Log.e("insertBook", "insertBook: ");
        String nameString = mbookName.getText().toString().trim();
        String priceString = mbookPrice.getText().toString().trim();
        String quantityString = mQuantity.getText().toString().trim();
        String supplierNameString = mSupplierName.getText().toString().trim();
        String supplierContactString = mSupplierNumber.getText().toString().trim();
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierNameString) ||
                TextUtils.isEmpty(supplierContactString)) {
            return;
        }
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        int number = 0;
        if (!TextUtils.isEmpty(supplierContactString)) {
            number = Integer.parseInt(supplierContactString);
        }
        ContentValues values = new ContentValues();
        values.put(ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ItemsContract.ItemsEntry.COLUMN_PRICE, price);
        values.put(ItemsContract.ItemsEntry.COLUMN_QUANTITY, quantity);
        values.put(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER, number);

        if (mCurrentBookUri == null) {
            Uri newUri = getContentResolver().insert(ItemsContract.ItemsEntry.CONTENT_URI, values);
            if (newUri == null) {
                Log.e("NEWURI", "Inside if ");
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Log.e("NEWURI", "Inside else ");
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            Log.e("Data", "INSERTED BOOK ID: ");
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                ItemsContract.ItemsEntry._ID,
                ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME,
                ItemsContract.ItemsEntry.COLUMN_PRICE,
                ItemsContract.ItemsEntry.COLUMN_QUANTITY,
                ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME,
                ItemsContract.ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_PRICE);
            int quantityNameColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityNameColumnIndex);
            String supplierName = cursor.getString(supplierColumnIndex);
            int supplierPhoneNum = cursor.getInt(supplierPhoneNumColumnIndex);
            mbookName.setText(name);
            mbookPrice.setText(Integer.toString(price));
            mQuantity.setText(Integer.toString(quantity));
            mSupplierName.setText(supplierName);
            mSupplierNumber.setText(supplierName);
            mSupplierNumber.setText(Integer.toString(supplierPhoneNum));
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mbookName.setText("");
        mbookPrice.setText("");
        mQuantity.setText("");
        mSupplierName.setText("");
        mSupplierNumber.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBook();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
    public void increment(View view) {
        int quantity;
        String quantityString = mQuantity.getText().toString().trim();
        quantity = Integer.parseInt(quantityString);
        if (quantity == 100) {
            Toast.makeText(this, getResources().getString(R.string.toast_max), Toast.LENGTH_SHORT).show();
            return;
        }
        quantity = quantity + 1;
        displayQuantity(quantity);
    }
    public void decrement(View view) {
        int quantity;
        String quantityString = mQuantity.getText().toString().trim();
        quantity = Integer.parseInt(quantityString);
        if (quantity == 1) {
            Toast.makeText(this, getResources().getString(R.string.toast_min), Toast.LENGTH_SHORT).show();
            return;
        }
        quantity = quantity - 1;
        displayQuantity(quantity);

    }
    private void displayQuantity(int number) {
        TextView quantityTextView = (TextView) findViewById(R.id.quantity);
        quantityTextView.setText("" + number);
    }

    public void submitButton(View view){
        int number;
        String numString = mSupplierNumber.getText().toString().trim();
        number = Integer.parseInt(numString);
        submitOrder(number);
    }

    private void submitOrder(int number){
        Intent intent= new Intent(Intent.ACTION_DIAL);
        String numberString = "tel:"+number;
        intent.setData(Uri.parse(numberString));
        startActivity(intent);
    }

}
