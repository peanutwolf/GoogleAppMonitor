package com.peanutwolf.googleappmonitor.Database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by vigursky on 31.03.2016.
 */
public class ShakeDBContentProvider extends ContentProvider {
    private ShakeDatabase database;

    private static final int SHAKES = 10;
    private static final int SHAKE_ID = 20;
    private static final int ROUTE_ID = 30;

    private static final String AUTHORITY = "com.peanutwolf.googleappmonitor.Database";

    private static final String BASE_PATH = "shakes";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/shakes";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/shake";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, SHAKES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SHAKE_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ROUTE_ID);
    }

    @Override
    public boolean onCreate() {
        database = new ShakeDatabase(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(ShakeDatabase.TABLE_SHAKE);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case SHAKES:
                break;
            case SHAKE_ID:
                queryBuilder.appendWhere(ShakeDatabase.COLUMN_ID + "="
                        + uri.getLastPathSegment());
            case ROUTE_ID:
                queryBuilder.appendWhere(ShakeDatabase.COLUMN_ROUTEID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case SHAKES:
                id = sqlDB.insert(ShakeDatabase.TABLE_SHAKE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
