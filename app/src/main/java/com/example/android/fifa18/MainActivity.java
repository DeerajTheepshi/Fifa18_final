package com.example.android.fifa18;

import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.android.fifa18.data.contractClass.fixtureTable;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //VARIABLE DECLARATIONS
    FloatingActionButton addNew;
    ListView homelist;

    FixturesAdapter adapter;

    private static final int LOADER_ID = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addNew = (FloatingActionButton) findViewById(R.id.addNew);

        //FLOATING BUTTON ACTION LISTENER
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddFixture.class);
                startActivity(intent);
            }
        });

        //BIND LIST WITH ADAPTER
        homelist = (ListView) findViewById(R.id.homelist);
        homelist.setEmptyView(findViewById(R.id.empty));
        adapter = new FixturesAdapter(this,null);
        homelist.setAdapter(adapter);

        //START UP ASYNC CURSOR LOADER
        getLoaderManager().initLoader(LOADER_ID,null,this);

        //FOR DELETE AND UPDATE, ON LONG CLICK OPEN ALERT DIALOG
        homelist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final long idFinal = id;
                new AlertDialog.Builder(MainActivity.this).setTitle("Pick an Action").setMessage("Do you want to perform a delete action or update action")
                        .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, AddFixture.class);
                                Uri dataUri = ContentUris.withAppendedId(fixtureTable.CONTENT_URI,idFinal);
                                intent.setData(dataUri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri dataUri = ContentUris.withAppendedId(fixtureTable.CONTENT_URI,idFinal);
                                getContentResolver().delete(dataUri,null,null);
                            }
                        }).create().show();
                return true;
            }
        });
    }



    //ABSTRACT METHODS OF IMPLEMENTATION
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {fixtureTable._ID,fixtureTable.TEAM1,fixtureTable.TEAM2,fixtureTable.DATE,fixtureTable.TIME
                                ,fixtureTable.VENUE,fixtureTable.IMAGE1,fixtureTable.IMAGE2};
        //START CURSOR LOADER AND SORT BY DATE AND TIME
        return new CursorLoader(this, fixtureTable.CONTENT_URI,projection,null,null,
                                fixtureTable.DATEFORMAT+" ASC, "+fixtureTable.TIME+" ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //ATTACH THE CURSOR WITH ADAPTER
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}