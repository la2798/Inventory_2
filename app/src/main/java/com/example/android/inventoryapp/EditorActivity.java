package com.example.android.inventoryapp;

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
    private boolean saveSuccess = true;
    private int quantity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        orderButton = findViewById(R.id.order_button);
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        if (mCurrentBookUri == null) {
            orderButton.setEnabled(false);
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

        mIncreaseButton = findViewById(R.id.increase_quantity);
        mDecreaseButton = findViewById(R.id.decrease_quantity);


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
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierContactString)) {
            return;
        }
        if (TextUtils.isEmpty(nameString)) {
            //set error accordingly
            mbookName.requestFocus();
            mbookName.setError(getString(R.string.title_empty_error));
            Toast.makeText(this, R.string.title_empty_error, Toast.LENGTH_SHORT).show();

            //indicate save wasn't successful
            saveSuccess = false;
            return;
        } else {
            //update save success flag if validation succeeds
            saveSuccess = true;
        }
        //validate price field isn't empty
        if (TextUtils.isEmpty(priceString)) {
            //set error accordingly
            mbookPrice.requestFocus();
            mbookPrice.setError("price cannot be empty");
            Toast.makeText(EditorActivity.this, R.string.price_invalid_error, Toast.LENGTH_SHORT).show();
            //indicate save wasn't successful
            saveSuccess = false;
            return;
        } else {
            int price = Integer.parseInt(priceString.replaceAll("\\D", ""));
            if (price < 0) {
                saveSuccess = false;
                mbookPrice.requestFocus();
                mbookPrice.setError("price cannot be negative");
                Toast.makeText(EditorActivity.this, R.string.price_invalid_error, Toast.LENGTH_SHORT).show();
                return;
            }
            //update save success flag if validation succeeds
            saveSuccess = true;
        }

        if (TextUtils.isEmpty(quantityString)) {
            //set error accordingly
            mQuantity.requestFocus();
            mQuantity.setError("quantity cannot be empty");
            Toast.makeText(EditorActivity.this, R.string.quantity_missing_error, Toast.LENGTH_SHORT).show();
            //indicate save wasn't successful
            saveSuccess = false;
            return;
        } else {
            int quantity = Integer.parseInt(quantityString);
            if (quantity < 0 || quantity >= 100) {
                mQuantity.requestFocus();
                mQuantity.setError("quantity cannot be negative or more than 100");
                Toast.makeText(EditorActivity.this, R.string.quantity_missing_error, Toast.LENGTH_SHORT).show();
                saveSuccess = false;
                return;
            }
            //update save success flag if validation succeeds
            saveSuccess = true;
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            //set error accordingly
            mSupplierName.requestFocus();
            mSupplierName.setError("Supplier Name cannot be empty");
            Toast.makeText(EditorActivity.this, R.string.supplier_name_error, Toast.LENGTH_SHORT).show();
            //indicate save wasn't successful
            saveSuccess = false;
            return;
        } else {
            //update save success flag if validation succeeds
            saveSuccess = true;
        }

        if (TextUtils.isEmpty(supplierContactString)) {
            //display error accordingly
            mSupplierNumber.requestFocus();
            mSupplierNumber.setError("Supplier Number cannot be empty");
            Toast.makeText(EditorActivity.this, R.string.supplier_details_missing_error, Toast.LENGTH_SHORT).show();
            //indicate save wasn't successful
            saveSuccess = false;
            return;
        } else {
            int snum = Integer.parseInt(supplierContactString.replaceAll("\\D", ""));
            if (snum < 0) {
                mSupplierNumber.requestFocus();
                mSupplierNumber.setError("Supplier Number cannot be negative");
                Toast.makeText(EditorActivity.this, R.string.supplier_details_missing_error, Toast.LENGTH_SHORT).show();
                //indicate save wasn't successful
                saveSuccess = false;
                return;
            }
            saveSuccess = true;
        }
        ContentValues values = new ContentValues();
        values.put(ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ItemsContract.ItemsEntry.COLUMN_PRICE, priceString);
        values.put(ItemsContract.ItemsEntry.COLUMN_QUANTITY, quantityString);
        values.put(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierContactString);

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
            double price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityNameColumnIndex);
            String supplierName = cursor.getString(supplierColumnIndex);
            String supplierPhoneNum = cursor.getString(supplierPhoneNumColumnIndex);
            mbookName.setText(name);
            mbookPrice.setText(String.valueOf(price));
            mQuantity.setText(String.valueOf((quantity)));
            mSupplierName.setText(supplierName);
            mSupplierNumber.setText(supplierName);
            mSupplierNumber.setText(supplierPhoneNum);
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
                if (saveSuccess) {
                    finish();
                    return true;
                }
                return false;
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
                deleteBook();
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

    private void deleteBook() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    public void increment(View view) {
        if (mQuantity.getText().length() > 0) {
            quantity = Integer.parseInt(mQuantity.getText().toString());
        }
        if (quantity >= 100) {
            Toast.makeText(this, getResources().getString(R.string.toast_max), Toast.LENGTH_SHORT).show();
            return;
        }
        quantity = quantity + 1;
        displayQuantity(quantity);
    }

    public void decrement(View view) {
        if (mQuantity.getText().length() > 0) {
            quantity = Integer.parseInt(mQuantity.getText().toString());
        }
        if (quantity > 0) {
            quantity = quantity - 1;
            displayQuantity(quantity);
        } else {
            Toast.makeText(this, getResources().getString(R.string.toast_min), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayQuantity(int number) {
        mQuantity.setText(String.valueOf(number));
        mQuantity.setSelection(mQuantity.getText().length());
        mQuantity.setError(null);
    }

    public void submitButton(View view) {
        String numString = mSupplierNumber.getText().toString().trim();
        if (numString.length() > 0) {
            submitOrder(numString);
        }
    }

    private void submitOrder(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        String numberString = "tel:" + number;
        intent.setData(Uri.parse(numberString));
        startActivity(intent);
    }
}
