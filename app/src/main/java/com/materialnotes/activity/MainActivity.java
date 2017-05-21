package com.materialnotes.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.materialnotes.R;
import com.materialnotes.util.FileRefHeader;
import com.materialnotes.util.FilenameUtils;
import com.materialnotes.widget.AboutNoticeDialog;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import no.nordicsemi.android.scriba.hrs.HRSActivity;
import roboguice.activity.RoboActionBarActivity;

/**
 * Main activity where the user can manage all existing projects and change application settings
 *
 * Created by Pablo Saavedra on 11/05/2017.
 *
 */


public class MainActivity extends RoboActionBarActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public final static String FILENAME = "com.materialnotes.activity.MainActivity.FILENAME";

    static final int OPEN_FILE_REQUEST = 1;

    private String newProjectName ="";
//    private File newProjectFile; //used for new projects

    private ArrayList<String> projects; //Project files in app folder
    ArrayAdapter<String> adapter;
    ListView lvMainFiles;

    ImageView createNewProjectImg;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNewProjectImg = (ImageView) findViewById(R.id.createNewProjectImage);

        //Create application folders
        File pathAppDataFolder = new File(Cfg.APP_DATA_FOLDER);
        if (!pathAppDataFolder.exists())
            pathAppDataFolder.mkdirs();

        projects = new ArrayList<String>();
        lvMainFiles = (ListView) findViewById(R.id.LvMainFiles);
        lvMainFiles.setOnItemClickListener(this);
        lvMainFiles.setOnItemLongClickListener(this);

        //prompt the user to connect their Scriba device via dialog
        Intent intent = new Intent(MainActivity.this, HRSActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        projects.clear();

        projects = getProjects();

        //Convert elements on the ArrayList to items on the ListView using an ArrayAdapter
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, projects);
        adapter.notifyDataSetChanged();

        lvMainFiles.setAdapter(adapter);

        if (projects.size()>0){
            createNewProjectImg.setVisibility(View.INVISIBLE);
        }else{
            createNewProjectImg.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<String> getProjects(){
        //TODO Load all ref files

        ArrayList<String> folders = new ArrayList<String>();

        ArrayList<String> files = new ArrayList<String>();

        File[] allEntries = new File(Cfg.APP_DATA_FOLDER).listFiles();

        for (int i = 0; i < allEntries.length; i++) {
            if (allEntries[i].isDirectory()) {
                folders.add(allEntries[i].getName());
            } else if (allEntries[i].isFile() &&
                    FilenameUtils.getExtension(allEntries[i].getName()).equalsIgnoreCase(Cfg.PROJECT_EXTENSION)) {
                files.add(FilenameUtils.getShortFilenameWithoutExtension(allEntries[i].getName()));
            }
        }

        return files;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about_info:
                new AboutNoticeDialog().show(getSupportFragmentManager(), "dialog_about_notice");
                return true;
            case R.id.bluetooth:
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    public void bluetooth(MenuItem item) {
        Intent intent= new Intent(this, HRSActivity.class);
        startActivity(intent);
    }

    public void showSettings(View v) {
        Intent intent = new Intent(this, SpinnerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String entryName = (String)parent.getItemAtPosition(position);

        Cfg.currentProjectFilename = Cfg.APP_DATA_FOLDER+"/"+entryName+Cfg.PROJECT_EXTENSION;

        Intent intent = new Intent(this, CurrentProjectFilesActivity.class);
        //intent.putExtra(FILENAME, Cfg.currentProjectFilename);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final String entryName = (String)parent.getItemAtPosition(position);

        Log.d("PSL:LongClick","Long click done!");

        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        //dlgAlert.setTitle("App Title");
        dlgAlert.setMessage("Delete project?");
        dlgAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(Cfg.APP_DATA_FOLDER+"/"+entryName+Cfg.PROJECT_EXTENSION);
                file.delete();
                onResume();
//                finish();
//                System.exit(0);
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


    //TODO
    public void addNewProject (View view){
//        Intent openFileIntent = new Intent(this, OpenFileActivity.class);
//        startActivityForResult(openFileIntent, OPEN_FILE_REQUEST);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter new project name");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT /* | InputType.TYPE_TEXT_VARIATION_PASSWORD*/);
        input.setHint("Type name here");
        input.setBackgroundColor(Color.parseColor("#DDDDDD"));
        input.setPaddingRelative(10,10,10,10);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newProjectName = input.getText().toString();
                newProjectName = Cfg.APP_DATA_FOLDER+"/"+newProjectName+Cfg.PROJECT_EXTENSION;
                File newProjectFile = new File(newProjectName);

                if (newProjectFile.exists()){
                    Toast.makeText(MainActivity.this, "Project already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    try{
                        RandomAccessFile newFile = new RandomAccessFile(newProjectFile, "rw");
                        FileRefHeader fh = new FileRefHeader();
                        FileRefHeader.writeEmptyHeader(newFile);
                        newFile.close();
                    }catch(Exception e){
                        Toast.makeText(MainActivity.this, "Error creating project.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    if (newProjectFile.exists()){
                        Cfg.currentProjectFilename = newProjectName;
                        Intent intent = new Intent(MainActivity.this, CurrentProjectFilesActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }
}