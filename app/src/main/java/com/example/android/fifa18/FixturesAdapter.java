package com.example.android.fifa18;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.fifa18.data.contractClass.fixtureTable;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

//CUSTOM CLASS FOR ADAPTER

public class FixturesAdapter extends CursorAdapter {


    public  FixturesAdapter(Context context,Cursor cursor){
        super(context,cursor,0);
    }
    @Override

    //INFLATE VIEW IF DOES NOT EXIST
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_view,parent,false);
    }

    //POPULATE VIEW, IF IT EXISTS
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        //VIEW FIELDS INITIALIZATION
        TextView team1 = (TextView) view.findViewById(R.id.teamName1);
        TextView team2 = (TextView) view.findViewById(R.id.teamName2);
        TextView date = (TextView) view.findViewById(R.id.dateList);
        TextView time = (TextView) view.findViewById(R.id.timeList);
        TextView venue = (TextView) view.findViewById(R.id.venueList);

        final ImageView img1= (ImageView) view.findViewById(R.id.img1List);
        final ImageView img2 = (ImageView) view.findViewById(R.id.img2List);

        //POPULATE THE VIEWS FROM CURSOR
        team1.setText(cursor.getString(cursor.getColumnIndex(fixtureTable.TEAM1)));
        team2.setText(cursor.getString(cursor.getColumnIndex(fixtureTable.TEAM2)));
        date.setText(cursor.getString(cursor.getColumnIndex(fixtureTable.DATE)));
        time.setText(cursor.getString(cursor.getColumnIndex(fixtureTable.TIME)));
        venue.setText(cursor.getString(cursor.getColumnIndex(fixtureTable.VENUE)));

        //BLOB TO BITMAP CONVERSION
        String im1uri = (cursor.getString(cursor.getColumnIndex(fixtureTable.IMAGE1)));
        String im2uri = (cursor.getString(cursor.getColumnIndex(fixtureTable.IMAGE2)));

        //scalePic(img1,im1uri,context);
        Picasso.with(context).load(new File(im1uri)).into(img1);

        img1.setTag(R.id.tag1,cursor.getString(cursor.getColumnIndex(fixtureTable.TEAM1)));
        //scalePic(img2,im2uri,context);
        Picasso.with(context).load(new File(im2uri)).into(img2);
        img2.setTag(R.id.tag1,cursor.getString(cursor.getColumnIndex(fixtureTable.TEAM2)));

        //SET UP ONCLICK LISTENER FOR IMAGEVIEWS - THEY TAKE THE TAG ID ALONG WITH THEM TO PARSE DATA TEAM NAME
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,TeamFixtures.class);
                intent.putExtra("TeamName",(String) img1.getTag(R.id.tag1) );
                context.startActivity(intent);
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,TeamFixtures.class);
                intent.putExtra("TeamName",(String) img2.getTag(R.id.tag1));
                context.startActivity(intent);
            }
        });
    }
}
