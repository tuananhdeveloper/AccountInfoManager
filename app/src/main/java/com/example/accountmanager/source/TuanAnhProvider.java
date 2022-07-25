package com.example.accountmanager.source;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accountmanager.source.helper.MyDBHelper;

import java.util.HashMap;

/**
 * Created by Nguyen Tuan Anh on 19/07/2022.
 * FPT Software
 * tuananhprogrammer@gmail.com
 */
public class TuanAnhProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher;
    private static final int ALL_ITEMS = 0;
    private static final int ONE_ITEM = 1;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AccountInfoData.AUTHORITY, "accounts", ALL_ITEMS);
        sUriMatcher.addURI(AccountInfoData.AUTHORITY, "accounts/#", ONE_ITEM);
    }

    private static final HashMap<String, String> accountInfoMap;

    static {
        accountInfoMap = new HashMap<>();
        accountInfoMap.put(AccountInfoData.Account._ID, AccountInfoData.Account._ID);
        accountInfoMap.put(AccountInfoData.Account._TITLE, AccountInfoData.Account._TITLE);
        accountInfoMap.put(AccountInfoData.Account._CONTENT, AccountInfoData.Account._CONTENT);
        accountInfoMap.put(AccountInfoData.Account._PASSWORD, AccountInfoData.Account._PASSWORD);
    }

    private MyDBHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new MyDBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(AccountInfoData.Account.TABLE_NAME);
        builder.setProjectionMap(accountInfoMap);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor queryCursor = builder.query(db, projection, selection,
                selectionArgs, null, null, null);
        queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return queryCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(AccountInfoData.Account.TABLE_NAME, null, values);
        if (rowId> 0) {
            Uri accountInfoUri = ContentUris.withAppendedId(AccountInfoData.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(accountInfoUri, null);
            return accountInfoUri;
        }
        throw new IllegalArgumentException("<Illegal>Unknown URI: " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
