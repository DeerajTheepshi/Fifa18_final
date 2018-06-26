package com.example.android.fifa18;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.example.android.fifa18.data.contractClass;

import java.nio.channels.FileChannel;
import java.util.List;

public class TeamFixtures extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    //VARIABLE DECLARATIONS
    FixturesAdapter adapter ;
    private final static int LOAD_LIST=1;
    String teamName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fixtures_activity);

        //GET INTENT ALONG WITH IT THE DATA THAT WAS PASSED
        teamName = getIntent().getExtras().getString("TeamName");

        //ATTACH ADAPTER TO LISTVIEW
        ListView matches = (ListView) findViewById(R.id.teamFixtureList);
        matches.setEmptyView(findViewById(R.id.empty1));
        adapter = new FixturesAdapter(this, null);
        getLoaderManager().initLoader(LOAD_LIST,null,this);
        matches.setAdapter(adapter);
    }

    //ABSTRACT METHOD IMPLEMENTATIONS
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //USING WHERE CLAUSE TO FILTER OUT RESULTS
        String[] projection1 = {contractClass.fixtureTable._ID, contractClass.fixtureTable.TEAM1, contractClass.fixtureTable.TEAM2, contractClass.fixtureTable.DATE, contractClass.fixtureTable.TIME
                        , contractClass.fixtureTable.VENUE, contractClass.fixtureTable.IMAGE1, contractClass.fixtureTable.IMAGE2,contractClass.fixtureTable.DATEFORMAT};
        String selection = contractClass.fixtureTable.TEAM1 + "=? OR "+ contractClass.fixtureTable.TEAM2+"=?";
        String[] selectionArgs = new String[]{teamName,teamName};
        return new CursorLoader(this, contractClass.fixtureTable.CONTENT_URI,projection1,selection,selectionArgs,
                        contractClass.fixtureTable.DATEFORMAT+" ASC, "+ contractClass.fixtureTable.TIME+" ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //ATTACH ADAPTER TO CURSOR
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
