package com.example.android.fifa18.data;

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

import com.example.android.fifa18.data.contractClass.fixtureTable;

public class dbProvider extends ContentProvider {

    private static final int MATCH_COMPLETE= 100;
    private static final int MATCH = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private dbHelper DbHelper;

    //ADD TWO PATTERNS OF URI
    static {
        sUriMatcher.addURI(contractClass.CONTENT_AUTHORITY,contractClass.PATH,MATCH_COMPLETE);
        sUriMatcher.addURI(contractClass.CONTENT_AUTHORITY,contractClass.PATH+"/#",MATCH);
    }

    //CREATE THE TABLE, IF NOT EXISTS
    @Override
    public boolean onCreate() {
        DbHelper = new dbHelper(getContext());
        return true;
    }

    //ABSTRACT METHOD IMPLEMENTATIONS FOR INSERT, QUERY, UPDATE AND DELETE
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case MATCH_COMPLETE:
                return insertFixture(uri,values);
            default:
                throw new IllegalArgumentException("Cannot insert into URI " + uri);
        }
    }

    private Uri insertFixture(Uri uri, ContentValues values){
        //CHECK IF ALL DETAILS ARE VALID ,ENTERED
        if(checkEntry(values)==0)
            return null;
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        long id = db.insert(fixtureTable.TABLE_NAME,null,values);
        if(id==-1){
            return null;
        }
        //NOTIFY ALL LISTENERS ABOUT INSERTION
        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs
            , @Nullable String sortOrder) {
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        int match= sUriMatcher.match(uri);
        Cursor cursor;
        switch(match){
            case MATCH_COMPLETE:
                cursor = db.query(fixtureTable.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case MATCH:
                selection = fixtureTable._ID +"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(fixtureTable.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //SET UP NOTIFICATION LISTENER ON THIS CURSOR
       cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int updatedRows=0;
        SQLiteDatabase db = DbHelper.getWritableDatabase();

        switch(match){
            case MATCH_COMPLETE:
                updatedRows=db.update(fixtureTable.TABLE_NAME,values,selection,selectionArgs);
                break;
            case MATCH:
                //CHECK UPDATE
                if(checkEntry(values)==0)
                    return 0;
                selection = fixtureTable._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                updatedRows=db.update(fixtureTable.TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        if(updatedRows!=0)
            //NOTIFY ALL LISTENERS ABOUT UPDATE
            getContext().getContentResolver().notifyChange(uri,null);
        return updatedRows;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int deletedRows = 0;
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        switch (match){
            case MATCH_COMPLETE:
                deletedRows = db.delete(fixtureTable.TABLE_NAME,selection,selectionArgs);
                break;
            case MATCH:
                selection = fixtureTable._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                deletedRows = db.delete(fixtureTable.TABLE_NAME,selection,selectionArgs);
        }
        if(deletedRows!=0)
            //NOTIFY ALL LISTENERS ABOUT DELETE
            getContext().getContentResolver().notifyChange(uri,null);

        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //NOT USED,SO LEFT EMPTY
        return null;
    }

    //CHECKS ENTRY FOR EMPTY VALUES
    public int checkEntry(ContentValues values){
        if(values.getAsString(fixtureTable.DATE).isEmpty())
            return 0;
        else if(values.getAsString(fixtureTable.TIME).isEmpty())
            return 0;
        else if(values.getAsString(fixtureTable.TEAM1)==values.getAsString(fixtureTable.TEAM2))
            return 0;
        return 1;
    }
}
