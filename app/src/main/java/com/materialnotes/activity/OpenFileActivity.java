package com.materialnotes.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.materialnotes.R;
import com.materialnotes.util.FilenameUtils;

public class OpenFileActivity extends AppCompatActivity implements OnClickListener, OnItemClickListener {

    ListView LvList;

    ArrayList<String> listItems = new ArrayList<String>();

    ArrayAdapter<String> adapter;

    Button BtnOK;
    Button BtnCancel;

    String currentPath = null;

    String selectedFilePath = null; /* Full path, i.e. /mnt/sdcard/folder/file.txt */
    String selectedFileName = null; /* File Name Only, i.e file.txt */

    public static String lastFolder = Cfg.APP_DATA_FOLDER+ "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        try {
			/* Initializing Widgets */
            LvList = (ListView) findViewById(R.id.LvList);
            BtnOK = (Button) findViewById(R.id.BtnOK);
            BtnCancel = (Button) findViewById(R.id.BtnCancel);

			/* Initializing Event Handlers */

            LvList.setOnItemClickListener(this);

            BtnOK.setOnClickListener(this);
            BtnCancel.setOnClickListener(this);

            //This is the standard behavior (Open on root folder)
            //setCurrentPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");

            //We are going to remember last visited folder
            File fi = new File(lastFolder);
            if (!fi.exists()){
                lastFolder=Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            }
            setCurrentPath(lastFolder);

        } catch (Exception ex) {
            Toast.makeText(this, "Error in OpenFileActivity.onCreate: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void setCurrentPath(String path) {
        ArrayList<String> folders = new ArrayList<String>();

        ArrayList<String> files = new ArrayList<String>();

        currentPath = path;
        Log.d("PSL:Path","Current path: "+currentPath);

        File[] allEntries = new File(path).listFiles();

        //Note that, at the moment, we are only adding .txt files (new formats would be added in the future)
        for (int i = 0; i < allEntries.length; i++) {
            if (allEntries[i].isDirectory()) {
                folders.add(allEntries[i].getName());
            } else if (allEntries[i].isFile() &&
                       FilenameUtils.getExtension(allEntries[i].getName()).equalsIgnoreCase(".txt")) {
                files.add(allEntries[i].getName());
            }
        }

        Collections.sort(folders, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        Collections.sort(files, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        listItems.clear();

        if (!currentPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"))
            listItems.add("../");

        for (int i = 0; i < folders.size(); i++) {
            listItems.add(folders.get(i) + "/");
        }

        for (int i = 0; i < files.size(); i++) {
            listItems.add(files.get(i));
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        adapter.notifyDataSetChanged();

        LvList.setAdapter(adapter);
    }

    @Override
    public void onBackPressed()
    {
//        if (!currentPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath() + "/")) {
//            setCurrentPath(new File(currentPath).getParent() + "/");
//        } else {
//            super.onBackPressed();
//        }

        Intent intent = new Intent();
        intent.putExtra("fileName", "");
        intent.putExtra("shortFileName", "");
        setResult(RESULT_CANCELED, intent);

        this.finish();
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.BtnOK:
                intent = new Intent();
                intent.putExtra("fileName", selectedFilePath);
                intent.putExtra("shortFileName", selectedFileName);
                setResult(RESULT_OK, intent);

                //store last visited folder
                lastFolder=currentPath;
                Log.d("PSL:OpenFileActivity","current Folder: "+currentPath);

                this.finish();

                break;
            case R.id.BtnCancel:

                intent = new Intent();
                intent.putExtra("fileName", "");
                intent.putExtra("shortFileName", "");
                setResult(RESULT_CANCELED, intent);

                this.finish();

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String entryName = (String)parent.getItemAtPosition(position);
        if (entryName.endsWith("../")){
            if (!currentPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath() + "/")) {
                setCurrentPath(new File(currentPath).getParent() + "/");
            }
        }else if (entryName.endsWith("/")) {
            setCurrentPath(currentPath + entryName);
        } else {
            selectedFilePath = currentPath + entryName;
            selectedFileName = entryName;
            this.setTitle(this.getResources().getString(R.string.title_activity_open_file)
                    + "[" + entryName + "]");
        }
    }
}