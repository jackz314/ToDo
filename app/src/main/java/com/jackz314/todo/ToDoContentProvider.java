/*
 * Copyright (C) 2017 Yordan P. Dieguez <ypdieguez@tuta.io>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jackz314.todo;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import static com.jackz314.todo.DatabaseContract.AUTHORITY;
/**
 * Content provider. The contract between this provider and applications
 * is defined in {@link DatabaseContract}.
 */
public class ToDoContentProvider extends ContentProvider {

    private static final int ITEMS = 1;
    private static final int ITEMS_ID = 2;

    private static final String ACTION_UPDATE = "UPDATE";
    private static final String ACTION_DELETE = "DELETE";

    private DatabaseManager mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, DatabaseManager.TODO_TABLE, ITEMS);
        sUriMatcher.addURI(AUTHORITY, DatabaseManager.TODO_TABLE + "/#", ITEMS_ID);
        sUriMatcher.addURI(AUTHORITY, DatabaseManager.HISTORY_TABLE, ITEMS);
        sUriMatcher.addURI(AUTHORITY, DatabaseManager.HISTORY_TABLE + "/#", ITEMS_ID);
        sUriMatcher.addURI(AUTHORITY, DatabaseManager.TAGS_TABLE, ITEMS);
        sUriMatcher.addURI(AUTHORITY, DatabaseManager.TAGS_TABLE + "/#", ITEMS_ID);
    }

    private ContentResolver mResolver;

    public ToDoContentProvider() {
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        //System.out.println("AppProviderCalled");
        if (context == null) {
            //System.out.println("context == null");

            return false;
        }

        mResolver = context.getContentResolver();
        if (mResolver == null) {
            //System.out.println("contentResolver == null");

            return false;
        }


    mDbHelper = new DatabaseManager(context);

        return true;
}
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)) {
            case ITEMS:
                break;
            case ITEMS_ID:
                selection = appendIdToSelection(uri.getLastPathSegment(), selection);
                break;
            default:
                return null;
        }
        if(uri.getPath().contains(DatabaseManager.TODO_TABLE)){
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseManager.TODO_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(mResolver, DatabaseContract.Item.TODO_URI);
            return cursor;

        }
        if(uri.getPath().contains(DatabaseManager.HISTORY_TABLE)){
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseManager.HISTORY_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(mResolver, DatabaseContract.Item.HISTORY_URI);
            return cursor;
        }
        if(uri.getPath().contains(DatabaseManager.TAGS_TABLE)){
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseManager.TAGS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(mResolver, DatabaseContract.Item.TAGS_URI);
            return cursor;
        }
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case ITEMS:
                break;
            default:
        return null;
    }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if(uri.getPath().contains(DatabaseManager.TODO_TABLE)){
            long newRowId = db.insert(DatabaseManager.TODO_TABLE, null, values);
            if (newRowId != -1) {
                Uri newUri = ContentUris.withAppendedId(DatabaseContract.Item.TODO_URI, newRowId);
                mResolver.notifyChange(newUri, null);
                return newUri;
            }
        }else if(uri.getPath().contains(DatabaseManager.HISTORY_TABLE)){
            long newRowId = db.insert(DatabaseManager.HISTORY_TABLE, null, values);
            if (newRowId != -1) {
                Uri newUri = ContentUris.withAppendedId(DatabaseContract.Item.HISTORY_URI, newRowId);
                mResolver.notifyChange(newUri, null);
                return newUri;
            }
        }else if(uri.getPath().contains(DatabaseManager.TAGS_TABLE)){
            long newRowId = db.insert(DatabaseManager.TAGS_TABLE, null, values);
            if (newRowId != -1) {
                Uri newUri = ContentUris.withAppendedId(DatabaseContract.Item.TAGS_URI, newRowId);
                mResolver.notifyChange(newUri, null);
                return newUri;
            }
        }
        mResolver.notifyChange(uri, null);
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return doAction(uri, values, selection, selectionArgs, ACTION_UPDATE);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return doAction(uri, null, selection, selectionArgs, ACTION_DELETE);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        if(uri.getPath().contains(DatabaseManager.TODO_TABLE)){
            switch (sUriMatcher.match(uri)) {
                case ITEMS:
                    return DatabaseContract.Item.DIR_MIME_TYPE;
                case ITEMS_ID:
                    return DatabaseContract.Item.ITEM_MIME_TYPE;
                default:
                    return null;
            }
        }else if (uri.getPath().contains(DatabaseManager.HISTORY_TABLE)){
            switch (sUriMatcher.match(uri)) {
                case ITEMS:
                    return DatabaseContract.Item.HISTORY_DIR_MIME_TYPE;
                case ITEMS_ID:
                    return DatabaseContract.Item.HISTORY_ITEM_MIME_TYPE;
                default:
                    return null;
            }
        }else if(uri.getPath().contains(DatabaseManager.TAGS_TABLE)){
            switch (sUriMatcher.match(uri)) {
                case ITEMS:
                    return DatabaseContract.Item.TAGS_DIR_MIME_TYPE;
                case ITEMS_ID:
                    return DatabaseContract.Item.TAGS_ITEM_MIME_TYPE;
                default:
                    return null;
            }
        }
        return null;
    }

    private String appendIdToSelection(String id, String selection) {
        return DatabaseManager.ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
    }

    private int doAction(Uri uri, ContentValues values, String selection,
                         String[] selectionArgs, String action) {
        switch (sUriMatcher.match(uri)) {
            case ITEMS:
                break;
            case ITEMS_ID:
                selection = appendIdToSelection(uri.getLastPathSegment(), selection);
                break;
            default:
                return 0;
        }
        ////System.out.println("selection = " + selection); // debug info
       // //System.out.println("uri.getpath = " + uri.getPath());
        ////System.out.println("matcher = " + sUriMatcher.match(uri));

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int count;
        if (uri.getPath().contains(DatabaseManager.TODO_TABLE)){
            switch (action) {
                case ACTION_UPDATE:
                    count = db.update(DatabaseManager.TODO_TABLE, values, selection, selectionArgs);
                    break;
                case ACTION_DELETE:
                    count = db.delete(DatabaseManager.TODO_TABLE, selection, selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Action not supported: " + action);
            }
            mResolver.notifyChange(uri, null);
            return count;

        }else if (uri.getPath().contains(DatabaseManager.HISTORY_TABLE)){
            switch (action) {
                case ACTION_UPDATE:
                    count = db.update(DatabaseManager.HISTORY_TABLE, values, selection, selectionArgs);
                    break;
                case ACTION_DELETE:
                    count = db.delete(DatabaseManager.HISTORY_TABLE, selection, selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Action not supported: " + action);
            }
            mResolver.notifyChange(uri, null);
            return count;

        }else if (uri.getPath().contains(DatabaseManager.TAGS_TABLE)){
            switch (action) {
                case ACTION_UPDATE:
                    count = db.update(DatabaseManager.TAGS_TABLE, values, selection, selectionArgs);
                    break;
                case ACTION_DELETE:
                    count = db.delete(DatabaseManager.TAGS_TABLE, selection, selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Action not supported: " + action);
            }
            mResolver.notifyChange(uri, null);
            return count;

        }
        mResolver.notifyChange(uri, null);
        return -1;
    }
}
