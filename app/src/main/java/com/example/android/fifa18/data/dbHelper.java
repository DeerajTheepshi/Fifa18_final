package com.example.android.fifa18.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.fifa18.data.contractClass.fixtureTable;

public class dbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fifa.db";
    private static final int DATABASE_VERSION = 1;

    public dbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    //CREATES TABLE ON CALL FROM PROVIDER
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_QUERY =  "CREATE TABLE " + fixtureTable.TABLE_NAME + " ("
                + fixtureTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + fixtureTable.TEAM1 + " TEXT NOT NULL, "
                + fixtureTable.TEAM2 + " TEXT NOT NULL, "
                + fixtureTable.DATE + " TEXT NOT NULL, "
                + fixtureTable.TIME + " TEXT NOT NULL DEFAULT '22:00', "
                + fixtureTable.VENUE+ " TEXT NOT NULL, "
                + fixtureTable.IMAGE1 + " TEXT NOT NULL, "
                + fixtureTable.IMAGE2 + " TEXT NOT NULL, "
                + fixtureTable.DATEFORMAT+ " DATE NOT NULL );";

        db.execSQL(CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
