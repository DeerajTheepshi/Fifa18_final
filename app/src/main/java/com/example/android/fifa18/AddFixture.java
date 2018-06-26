package com.example.android.fifa18;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.fifa18.data.contractClass.fixtureTable;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class AddFixture extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //ALL DECLARATIONS
    TextView Time,Date;
    Spinner Venue, Team1, Team2;
    String team_name1,team_name2,venueString,date_formatted, mCurrentPhotoPath;
    Uri dataUri;
    ImageView img1,img2;
    private final static int LOADER_ID=1000,LOADER_TEAM1_ICON=1002,LOADER_TEAM2_ICON=1004,REQUEST_IMAGE_CAPTURE = 1,REQUEST_IMAGE_PICKER=4;
    String image1_to_save,image2_to_save; // PATH VARIABLES
    int cameraTeamStatus; //TELLS ABOUT WHICH PICTURE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);
        Venue = (Spinner) findViewById(R.id.venue);
        Date = (TextView) findViewById(R.id.date);
        Time = (TextView) findViewById(R.id.time);
        Team1 = (Spinner) findViewById(R.id.team1);
        Team2 = (Spinner) findViewById(R.id.team2);
        img1 = (ImageView) findViewById(R.id.img1);
        img2 = (ImageView) findViewById(R.id.img2);

        //GET INTENT DATA, IF THE REQUEST IS FOR A UPDATE
        Intent intent = getIntent();
        dataUri = intent.getData();

        if(dataUri==null)
            setTitle("Add Fixture");
        else {
            //INITIALIZE UPDATE LOADER, AND POPULATE THE FIELDS USING onLoadFinished()
            getLoaderManager().initLoader(LOADER_ID,null,this);
            setTitle("Update Existing Fixture");
        }

        //CALENDAR AND TIME PICKER DIALOG POP UPS
        final Calendar cal = Calendar.getInstance();
        //DATE PICKER
        final DatePickerDialog.OnDateSetListener DateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                String FormatMonth=""+monthOfYear, FormatDay=""+dayOfMonth;
                //FORMAT INTEGERS < 10 TO DD:MM:YYYY FORMAT
                if(monthOfYear<10)
                    FormatMonth = "0"+monthOfYear;
                if(dayOfMonth<10)
                    FormatDay = "0"+dayOfMonth;
                date_formatted = year+"-"+FormatMonth+"-"+FormatDay; //THIS IS FOR SQL DATE ENTRY
                Date.setText(FormatDay+"/"+FormatMonth+"/"+year);
            }

        };
        Date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddFixture.this, android.R.style.Theme_DeviceDefault_Dialog,DateListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //SIMILARLY TIME PICKER
        final TimePickerDialog.OnTimeSetListener TimeListener =  new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String FormatHour=""+hourOfDay, FormatMinute=""+minute;
                if(hourOfDay<10)
                    FormatHour = "0"+hourOfDay;
                if(minute<10)
                    FormatMinute = "0"+minute;
                Time.setText(FormatHour+":"+FormatMinute);
            }
        };

        Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(AddFixture.this,android.R.style.Theme_DeviceDefault_Dialog,TimeListener,cal.get(Calendar.HOUR_OF_DAY)
                        ,cal.get(Calendar.MINUTE), DateFormat.is24HourFormat(getApplicationContext())).show();
            }
        });

        //START SPINNER, SETUP ON ITEM CLICK LISTENERS
        intializeSpiner();
    }

    public void intializeSpiner(){
        //INITIALIZE AND ATTACH ARRAY ADAPTERS TO SPINNER
        final ArrayList<String> teams1 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.teams)));
        final ArrayList<String> teams2= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.teams)));
        final ArrayAdapter team1Adapter= new ArrayAdapter(AddFixture.this,android.R.layout.simple_spinner_item,teams1);
        final ArrayAdapter team2Adapter= new ArrayAdapter(AddFixture.this,android.R.layout.simple_spinner_item,teams2);
        ArrayAdapter venueAdapter = ArrayAdapter.createFromResource(this,R.array.venues,android.R.layout.simple_spinner_item);
        team1Adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        team2Adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        venueAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        Team1.setAdapter(team1Adapter);
        Team2.setAdapter(team2Adapter);
        Venue.setAdapter(venueAdapter);


        //SET UP LISTENERS
        Team1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                team_name1=(String) parent.getItemAtPosition(position);
                String team2_selected = Team2.getSelectedItem().toString();
                Log.v("kaala",(team_name1.equals("Select Team"))+" + "+team_name1);
                if(team_name1.equals("Select Team"))
                   team_name1=null;
                team2Adapter.clear();
                team2Adapter.addAll(getResources().getStringArray(R.array.teams));
                if(team_name1!=null) {
                    team2Adapter.remove(team_name1);
                    Team2.setSelection(team2Adapter.getPosition(team2_selected));
                    getLoaderManager().initLoader(LOADER_TEAM1_ICON,null,AddFixture.this);
                    getLoaderManager().restartLoader(LOADER_TEAM1_ICON, null,AddFixture.this);
                }
                //GET THE ICON IMAGE OF THE TEAM IF IT ALREADY EXISTS, START THE LOADER FIRST TIME, THEN RESTART THE LOADER

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                team_name1 = null;
            }
        });

        Team2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                team_name2=(String) parent.getItemAtPosition(position);
                String team1_selected = Team1.getSelectedItem().toString();
                if(team_name2.equals("Select Team"))
                    team_name2=null;
                team1Adapter.clear();
               team1Adapter.addAll(getResources().getStringArray(R.array.teams));
                if(team_name2!=null) {
                    team1Adapter.remove(team_name2);
                    Team1.setSelection(team1Adapter.getPosition(team1_selected));
                    getLoaderManager().initLoader(LOADER_TEAM2_ICON, null,AddFixture.this);
                    getLoaderManager().restartLoader(LOADER_TEAM2_ICON, null,AddFixture.this);
               }
                //SIMILARLY GET THE TEAM ICON IMAGE IF IT EXISTS ALREADY

        }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                team_name2 = null;
            }
        });

        Venue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                venueString = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                venueString = null;
            }
        });
    }

    //THIS METHOD WILL BE CALLED WHEN SAVE MENU OPTION IS PRESSED, IT SAVES ALL DATA
    public void save(){
        String time = Time.getText().toString().trim();
        String date =  Date.getText().toString().trim();
        //CONTENT VALUES FOR DIFFERENT NEEDS
        ContentValues val = new ContentValues(); //COMPLETE CONTENT VALUE FOR INSERTION AND UPDATION
        ContentValues updateImage1Val = new ContentValues(); //FOR IMAGE CHANGE ie..CHANGING AND EXISTING TEAM ICON IMAGE
        ContentValues updateImage2Val = new ContentValues(); //FOR IMAGE CHANGE IE...CHANGING AND EXISTING TEAM ICON IMAGE
        ContentValues updateImage1Val1 = new ContentValues(); //FOR IMAGE CHANGE ie..CHANGING AND EXISTING TEAM ICON IMAGE
        ContentValues updateImage2Val1 = new ContentValues();  //FOR IMAGE CHANGE ie..CHANGING AND EXISTING TEAM ICON IMAGE
        //VALUE INSERTIONS
        val.put(fixtureTable.VENUE,venueString);
        val.put(fixtureTable.TIME,time);
        val.put(fixtureTable.DATE,date);
        val.put(fixtureTable.TEAM1,team_name1);
        val.put(fixtureTable.TEAM2,team_name2);
        val.put(fixtureTable.IMAGE1,image1_to_save.toString());
        val.put(fixtureTable.IMAGE2,image2_to_save.toString());
        val.put(fixtureTable.DATEFORMAT,date_formatted);
        updateImage1Val.put(fixtureTable.IMAGE1,image1_to_save.toString());
        updateImage2Val.put(fixtureTable.IMAGE2,image1_to_save.toString());
        updateImage1Val1.put(fixtureTable.IMAGE1,image2_to_save.toString());
        updateImage2Val1.put(fixtureTable.IMAGE2,image2_to_save.toString());

        //UPDATE THE IMAGE ICON OF THE TEAM IF IT IS CHANGED (4 CASES)
        getContentResolver().update(fixtureTable.CONTENT_URI,updateImage1Val ,fixtureTable.TEAM1+"=?",
                new String[]{team_name1});
        getContentResolver().update(fixtureTable.CONTENT_URI,updateImage2Val,fixtureTable.TEAM2+"=?",
                new String[]{team_name1});
        getContentResolver().update(fixtureTable.CONTENT_URI,updateImage1Val1,fixtureTable.TEAM1+"=?",
                new String[]{team_name2});
        getContentResolver().update(fixtureTable.CONTENT_URI,updateImage2Val1,fixtureTable.TEAM2+"=?",
                new String[]{team_name2});
        Uri newURI;
        int updatedRows = 0;
        //IF ITS AN INSERT REQUEST:
        if(dataUri==null) {
            newURI = getContentResolver().insert(fixtureTable.CONTENT_URI, val);
            if(newURI==null)//ACT TO RETURN VALUES FROM PROVIDER CLASS
            {
                Toast.makeText(this, "PLEASE CHECK YOUR ENTRY , OR CHECK OUT REQUIRMENTS", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "SAVE SUCCESS", Toast.LENGTH_SHORT).show();
                finish();//CLOSE THE ACTIVITY, IF SUCCESS
            }
        }
        //IF IT IS AN UPDATE REQUEST
        else {
            updatedRows = getContentResolver().update(dataUri, val, null, null);
            if(updatedRows==0)
                Toast.makeText(this, "PLEASE CHECK YOUR ENTRY , OR CHECK OUT REQUIRMENTS", Toast.LENGTH_SHORT).show();
            else{
                Toast.makeText(this, "UPDATE SUCCESS", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    //CHOOSE TEAM ICON 1 BUTTON
    public void loadpic1(View view){
        startCamera();
        cameraTeamStatus = 1; //INDICATES WHICH IMAGEVIEW
    }

    //CHOOSE TEAM ICON 2 BUTTON
    public void loadpic2(View view){
        startCamera();
        cameraTeamStatus = 2; //INDIACTES WHICH IMAGEVIEW
    }

    //INTENT CALL BACK
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST CODE IS FRO IMAGE CAPTURE
        if (requestCode == REQUEST_IMAGE_CAPTURE &&
                resultCode == RESULT_OK) {
                Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);

                if(cameraTeamStatus==1) {
                    img1.setImageBitmap(bmp); //SET IMAGE AFTER SCALING
                    image1_to_save = mCurrentPhotoPath; //SET PATH
                    //Picasso.with(AddFixture.this).load(new File(image1_to_save)).into(img1);
                }
                else if(cameraTeamStatus==2){
                    img2.setImageBitmap(bmp); //SET IMAGE AFTER SCALING
                    image2_to_save = mCurrentPhotoPath; //SET TO PATH
                    //Picasso.with(AddFixture.this).load(new File(image2_to_save)).into(img2);
                }

        }
        //REQUEST CODE IS FOR IMAGE PICKER
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String path = getRealPathFromURI(selectedImage);

            if(cameraTeamStatus==1) {
                //scalePic(img1,path); //SET IMAGE AFTER SCALING
                image1_to_save = path;//SET PATH
                Picasso.with(AddFixture.this).load(new File(image1_to_save)).into(img1);
            }
            else if(cameraTeamStatus==2){
                //scalePic(img2,path); //SET IMAGE AFTER SCALING
                image2_to_save = path; //SET PATH
                Picasso.with(AddFixture.this).load(new File(image2_to_save)).into(img2);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu,menu);
        return super.onCreateOptionsMenu(menu);
    } //INFLATE THE MENU

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selected = item.getItemId();
        if(selected==R.id.save){
            save();
        }
        else if(selected==R.id.rulesMenu){
            Intent intent = new Intent(AddFixture.this,Rules.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    } //SET UP LISTENERS FOR MENU CLICKS

    //ABSTRACT METHOD IMPLEMENTATIONS
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID: //UPDATE LOADER REQUEST
                String[] projection1 = {fixtureTable._ID, fixtureTable.TEAM1, fixtureTable.TEAM2, fixtureTable.DATE, fixtureTable.TIME
                        , fixtureTable.VENUE, fixtureTable.IMAGE1, fixtureTable.IMAGE2,fixtureTable.DATEFORMAT};
                return new CursorLoader(this, dataUri, projection1, null, null, null);

            case LOADER_TEAM1_ICON: //TEAM1 ICON IMAGE REQUEST LOADER , LOADS CURSOR WHERE TEAM NAME MATCHES
                String[] projection2 = {fixtureTable._ID, fixtureTable.TEAM1, fixtureTable.TEAM2, fixtureTable.IMAGE1,fixtureTable.IMAGE2};
                String selection2 = fixtureTable.TEAM1 + "=? OR "+fixtureTable.TEAM2+"=?";
                String[] selectionArgs2 = new String[]{team_name1,team_name1};
                return new CursorLoader(this, fixtureTable.CONTENT_URI, projection2, selection2, selectionArgs2, null);

            case LOADER_TEAM2_ICON: //TEAM2 ICON IMAGE REQUEST LOADER WHERE TEAM NAME MATCHES
                String[] projection3 = {fixtureTable._ID, fixtureTable.TEAM1, fixtureTable.TEAM2, fixtureTable.IMAGE1,fixtureTable.IMAGE2};
                String selection3 = fixtureTable.TEAM1 + "=? OR "+fixtureTable.TEAM2+"=?";
                String[] selectionArgs3 = new String[]{team_name2,team_name2};
                return new CursorLoader(this, fixtureTable.CONTENT_URI, projection3, selection3, selectionArgs3, null);
            }
         return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //IF THERE IS NO PREVIOUS IMAGE ICON FOR TEAM1
        if(data.getCount()==0 && loader.getId()==LOADER_TEAM1_ICON)
            img1.setImageDrawable(null);
        //IF THERE IS NO PREVIOUS IMAGE ICON FOR TEAM2
        else if(data.getCount()==0 && loader.getId()==LOADER_TEAM2_ICON)
            img2.setImageDrawable(null);
        else{
            switch (loader.getId()){
                case LOADER_ID: //UPDATE - POPULATE FIELDS
                    if(data.moveToFirst()) {
                        int team1Index = data.getColumnIndex(fixtureTable.TEAM1);
                        int team2Index = data.getColumnIndex(fixtureTable.TEAM2);
                        int dateIndex = data.getColumnIndex(fixtureTable.DATE);
                        int timeIndex = data.getColumnIndex(fixtureTable.TIME);
                        int VenueIndex = data.getColumnIndex(fixtureTable.VENUE);

                        //USING ARRAY LIST FOR GETTING INDEX OF SELECTED ITEM
                        ArrayList<String> venuesArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.venues)));
                        ArrayList<String> teamsArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.teams)));

                        //POPULATE THE FILEDS
                        Date.setText(data.getString(dateIndex));
                        Time.setText(data.getString(timeIndex));
                        Venue.setSelection(venuesArray.indexOf(data.getString(VenueIndex)));
                        Team1.setSelection(teamsArray.indexOf(data.getString(team1Index)));
                        Team2.setSelection(teamsArray.indexOf(data.getString(team2Index)));
                        date_formatted = data.getString(data.getColumnIndex(fixtureTable.DATEFORMAT));
                        break;
                    }
                case LOADER_TEAM1_ICON:
                    if(data.moveToFirst()){ //RETRIEVE TEAM ICON IF EXISTS, TWO CASES ARE POSSIBLE
                        if(data.getString(data.getColumnIndex(fixtureTable.TEAM1)).equals(team_name1))
                            image1_to_save = data.getString(data.getColumnIndex(fixtureTable.IMAGE1));
                        else if(data.getString(data.getColumnIndex(fixtureTable.TEAM2)).equals(team_name1))
                            image1_to_save = (data.getString(data.getColumnIndex(fixtureTable.IMAGE2)));


                        //scalePic(img1,image1_to_save);//SCALE AND SET IMAGE
                        Picasso.with(AddFixture.this).load(new File(image1_to_save)).into(img1);



                        break;
                    }
                case LOADER_TEAM2_ICON:
                    if(data.moveToFirst()){ //RETRIEVE TEAM ICON IF EXISTS, TWO CASES ARE POSSIBLE
                        if(data.getString(data.getColumnIndex(fixtureTable.TEAM1)).equals(team_name2))
                            image2_to_save = (data.getString(data.getColumnIndex(fixtureTable.IMAGE1)));
                        else if(data.getString(data.getColumnIndex(fixtureTable.TEAM2)).equals(team_name2))
                            image2_to_save = (data.getString(data.getColumnIndex(fixtureTable.IMAGE2)));

                       //scalePic(img2,image2_to_save);//SCALE AND SET IMAGE

                        Picasso.with(AddFixture.this).load(new File(image2_to_save)).into(img2);

                        break;

                    }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



    //HOST A CAMERA INTENT AND PICKER INTENT BASED ON ALERT DIALOG SELECTION
    public void startCamera(){
        new AlertDialog.Builder(this).setTitle("Take or Pick?").setMessage("Choose to click a new picture or choose from existing").
                setPositiveButton("TAKE PHOTO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), //FILE PROVIDER TO CONVERT FILE URI TO CONTENT URI
                                        "com.example.android.fileprovider",
                                        photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                            }
                        }
                    }
                }).setNegativeButton("CHOOSE PHOTO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (ActivityCompat.checkSelfPermission(AddFixture.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(AddFixture.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_PICKER);
                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICKER);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case REQUEST_IMAGE_PICKER:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickIntent, REQUEST_IMAGE_PICKER);
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                    Toast.makeText(this, "Permission not Granted",Toast.LENGTH_SHORT);
                }
                break;
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String Stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String FileName = "JPEG_" + Stamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(FileName, ".jpg", storageDir);

        // SET IMAGE PATH TO GLOBAL VAR
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    public String getRealPathFromURI(Uri contentUri) {
        //GET PATH FROM URI FOR GALLERY IMAGE
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null,null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


}
