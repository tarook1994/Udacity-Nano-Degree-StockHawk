package com.udacity.stockhawk.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.DbHelper;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.ArrayList;

/**
 * Created by DELL on 2/26/2017.
 */

public class WidgetListViewAdapter extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            DbHelper dbHelper;
            private Cursor mCursor;
             SQLiteDatabase db;

            @Override
            public void onCreate() {
                 dbHelper = new DbHelper(getApplicationContext());
                 db = dbHelper.getWritableDatabase();


            }

            @Override
            public void onDataSetChanged() {

                mCursor = db.query("quotes",null,null,null,null,null,null);
                dbHelper = new DbHelper(getApplicationContext());
                db = dbHelper.getWritableDatabase();

            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return mCursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_item);
                int indexSymbol = mCursor.getColumnIndexOrThrow("symbol");
                int indexPrice = mCursor.getColumnIndexOrThrow("price");
                int indexPer = mCursor.getColumnIndexOrThrow("percentage_change");


                if (mCursor.moveToPosition(position)) {

                    views.setTextViewText(R.id.symbol1,mCursor.getString(indexSymbol));
                    views.setTextViewText(R.id.price1,mCursor.getString(indexPrice));
                    views.setTextViewText(R.id.change1,mCursor.getString(indexPer));

                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("stock",mCursor.getString(indexSymbol));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);



                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_item);

            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };

    }
}
