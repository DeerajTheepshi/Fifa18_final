package com.example.android.fifa18.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

//CLASS WITH ALL CONSTATNS
public class contractClass{
    public contractClass(){}

    //URI CONSTATNTS

    public static final String CONTENT_AUTHORITY = "com.example.android.fifa18";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final String PATH = "fixtures";

    public static final class fixtureTable implements BaseColumns{
        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH);
        //TABLE CONSTANTS
        public final static String TABLE_NAME = "fixtures";
        public final static String _ID = BaseColumns._ID;
        public final static String TEAM1 = "team1";
        public final static String TEAM2 = "team2";
        public final static String DATE = "date";
        public final static String TIME = "time";
        public final static String VENUE = "venue";
        public final static String IMAGE1="image1";
        public final static String IMAGE2="image2";
        public final static String DATEFORMAT = "date_format";

    }
}