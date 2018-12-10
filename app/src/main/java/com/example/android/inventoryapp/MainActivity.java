package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.Data.ItemsContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private  static  final int BOOK_LOADER = 0;
    BookCursorAdapter mCursorAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Setup FAB to open EditorActivity
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                    startActivity(intent);
                }
            });
            // Find the ListView which will be populated with the pet data
            ListView bookListView = (ListView) findViewById(R.id.list);

            // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
            View emptyView = findViewById(R.id.empty_view);
            bookListView.setEmptyView(emptyView);

            mCursorAdapter = new BookCursorAdapter(this,null,0);
            bookListView.setAdapter(mCursorAdapter);

            bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this,EditorActivity.class);
                    Uri currentBookUri = ContentUris.withAppendedId(ItemsContract.ItemsEntry.CONTENT_URI,id);
                    intent.setData(currentBookUri);
                    startActivity(intent);
                }
            });

            getSupportLoaderManager().initLoader(BOOK_LOADER,null,this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(ItemsContract.ItemsEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                ItemsContract.ItemsEntry._ID,
                ItemsContract.ItemsEntry.COLUMN_PRODUCT_NAME,
                ItemsContract.ItemsEntry.COLUMN_PRICE,
                ItemsContract.ItemsEntry.COLUMN_QUANTITY
        };
            return new CursorLoader(this,
                    ItemsContract.ItemsEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
    }



    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

            mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

            mCursorAdapter.swapCursor(null);
    }
}
