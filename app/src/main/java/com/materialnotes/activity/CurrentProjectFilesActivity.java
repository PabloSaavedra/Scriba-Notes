package com.materialnotes.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.materialnotes.R;
import com.materialnotes.util.FileRef;
import com.materialnotes.util.FilenameUtils;
import com.materialnotes.widget.AboutNoticeDialog;

import java.io.IOException;
import java.util.ArrayList;

import no.nordicsemi.android.scriba.hrs.HRSActivity;

/**
 * Activity to manage all files and settings of a notes project
 *
 * Created by Pablo Saavedra on 12/05/2017.
 *
 */


public class CurrentProjectFilesActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public final static String FILENAME = "com.materialnotes.activity.CurrentProjectFilesActivity.FILENAME";

    static final int OPEN_FILE_REQUEST = 1;

    private ArrayList<String> usedFiles; //Files used in current project
    ArrayAdapter<String> adapter;
    ListView lvProjectFiles;
    ImageView addFilesImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_project_files);
        addFilesImg = (ImageView) findViewById(R.id.addFilesImage);
        usedFiles = new ArrayList<String>();
        lvProjectFiles = (ListView) findViewById(R.id.LvProjectFiles);
        lvProjectFiles.setOnItemClickListener(this);
        lvProjectFiles.setOnItemLongClickListener(this);
        //Header Subtitle and back
        getSupportActionBar().setTitle(FilenameUtils.getShortFilenameWithoutExtension(Cfg.currentProjectFilename));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        usedFiles.clear();
        usedFiles = FileRef.getCurrentProjectFiles();

        //Convert elements on the ArrayList to items on the ListView using an ArrayAdapter
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, usedFiles);
        adapter.notifyDataSetChanged();

        lvProjectFiles.setAdapter(adapter);

        if (usedFiles.size()>0){
            addFilesImg.setVisibility(View.INVISIBLE);
        }else{
            addFilesImg.setVisibility(View.VISIBLE);
        }

        //TODO remove next line, is only for testing
        //FileRef.showAllRefsInLog();
    }


//    /** {@inheritDoc} */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    /** {@inheritDoc} */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_about_info:
//                new AboutNoticeDialog().show(getSupportFragmentManager(), "dialog_about_notice");
//                return true;
//            case R.id.bluetooth:
//                return true;
//            default: return super.onOptionsItemSelected(item);
//        }
//    }

//    public void bluetooth(MenuItem item) {
//        Intent intent= new Intent(this, HRSActivity.class);
//        startActivity(intent);
//    }

    public void showSettings(View v) {
        Intent intent = new Intent(this, SpinnerActivity.class);
        startActivity(intent);
    }

    public void showNotes(View view) {
        Intent intent = new Intent(this, ShowNotesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String entryName = (String) parent.getItemAtPosition(position);

        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(FILENAME, Environment.getExternalStorageDirectory() + "/" + entryName);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id) {
        final String entryName = (String)parent.getItemAtPosition(position);

        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        //dlgAlert.setTitle("App Title");
        dlgAlert.setMessage("Remove file from project?");
        dlgAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FileRef.removeFileFromCurrentProject(Environment.getExternalStorageDirectory()+"/"+entryName);
                onResume();
            }
        });
        dlgAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //close dialog
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

        return true;
    }


    public void addNewFile(View view) {
        Intent openFileIntent = new Intent(this, OpenFileActivity.class);
        startActivityForResult(openFileIntent, OPEN_FILE_REQUEST);
    }

    //Check activity result to load new file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OPEN_FILE_REQUEST:
                if (resultCode == RESULT_OK) {
                    String fileName = data.getStringExtra("fileName");
                    //String shortFileName = data.getStringExtra("shortFileName");
                    Toast.makeText(this, "Selected File: " + fileName, Toast.LENGTH_SHORT).show();

                    //TODO add file to current project (=add ref with id=-1)
                    try {
                        FileRef.addNewFileToProject(fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(this, EditNoteActivity.class);
                    intent.putExtra(FILENAME, fileName);
                    startActivity(intent);
                } else {
                    //Do nothing!
                    Toast.makeText(this, "No File Selected"/*, Cancel Or Back Pressed"*/, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}