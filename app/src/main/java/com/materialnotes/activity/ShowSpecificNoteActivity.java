

package com.materialnotes.activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.materialnotes.R;
import com.materialnotes.util.FileRef;
import com.materialnotes.util.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import roboguice.activity.RoboExpandableListActivity;

public class ShowSpecificNoteActivity extends AppCompatActivity {


    private static final String appDataFolder = Environment.getExternalStorageDirectory().getPath() + "/Scriba Notes/files";
    // @InjectView(R.id.textViewPrueba)

    //public String fileName;
    public int startPos = 0;
    public int endPos = 0;
    public int colorWay = 0;
    public int cardId = 0;

    private Context aContext;
    TextView textView;
    ScrollView scrollView;
    RelativeLayout relativeLayout;
    ArrayList<FileRef> references= new ArrayList<FileRef>();
    //Layout layout = textView.getLayout();
    private Button marc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_specific_note);

        aContext = getApplicationContext();
        relativeLayout = (RelativeLayout) findViewById(R.id.rl);
        scrollView = (ScrollView) findViewById(R.id.scrollViewSpecific);
        textView = (TextView) findViewById(R.id.textViewPrueba);



        //Capturing the text, start, end position and color for cardReference
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String fileName = intent.getStringExtra(ShowNotesActivity.FileText);
        Log.i("TAG", "Nombre del archivo " + fileName.toString());
        int startPosition = intent.getIntExtra(ShowNotesActivity.StartPosition, 0);
        final int endPosition = intent.getIntExtra(ShowNotesActivity.EndtPosition, 0);
        int color = intent.getIntExtra(ShowNotesActivity.CardColor, 0);
        int idCard = intent.getIntExtra(ShowNotesActivity.CardId, 0);

        startPos = startPosition;
        endPos = endPosition;
        colorWay = color;
        cardId = idCard;


        textView.setTextSize(18);
        //textView.setText(text);

//            TextView  textView1 = (TextView) findViewById(R.id.numerosRango);
//            textView1.setText(String.valueOf(startPosition) + " " + (String.valueOf(endPosition) + " " + (String.valueOf(color))));


        String fileText = "";
        fileText = readFromTxtFile(fileName);
        // Capture the layout's TextView and set the string as its text
        final TextView textView = (TextView) findViewById(R.id.textViewPrueba);
        textView.setText(fileText);


        applyStylesFromRefFile();

        //subtitles with the file folder
        //Substring the last "/" and take File name
        int nameIndex = fileName.lastIndexOf("/");
        String str = "";

        if (nameIndex == -1) {
            str = fileName;
        } else {
            str = fileName.substring(nameIndex + 1);
        }

        //Header Subtitle and back
        getSupportActionBar().setTitle(FilenameUtils.getShortFilenameWithoutExtension(Cfg.currentProjectFilename));
        getSupportActionBar().setSubtitle(FilenameUtils.getShortFilename(Cfg.currentFileFilename));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // ScrollView functionality.Scroll to highliht selection
        scrollView.post(new Runnable() {
            public void run() {
                Layout textlayout = textView.getLayout();
                scrollView.scrollTo(0, textlayout.getLineTop(textlayout.getLineForOffset(startPos)));
            }
        });
        //Log.i("TAG", "The index is " + layout.getLineForOffset(endPos));

        //ScrollView functionality
//        scrollView.post(new Runnable() {
//            public void run() {
//                scrollView.
//                scrollView.scrollTo(startPos,startPos);
//                //scrollView.fullScroll(ScrollView.FOCUS_FORWARD);
//            }
//        });

    }

    //Read From File method
    private String readFromTxtFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return "File not found.";
        }
        FileInputStream inputStream = null;
        byte[] buffer = new byte[(int) file.length()];

        try {
            inputStream = new FileInputStream(file);
            inputStream.read(buffer);
            inputStream.close();
        } catch (Exception e) {
            Log.d("PSL:createTxtFileFor...", "Error loading test file");
            e.printStackTrace();
        }
        String fullText = new String(buffer);
        return fullText;

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_notes, menu);
        return true;
    }

    //Clickable usages Export, back and Delete/-----IF YOU WANT TO PU ANY BUTTON OR USAGE MORE JUST PUT MORE CASES----
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.export:
                try {
                    FileRef fr=FileRef.readId(cardId);
                    references.add(fr);
                    sendEmail(FileRef.exportRefs(references));
                    //Toast.makeText(getApplicationContext(), "File Export Done!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.delete:
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure to delete permanently this reference?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                    FileRef.deleteId(cardId);

                                relativeLayout.removeAllViews();
                                onBackPressed();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
        }
       return true;
    }


    //Highlight Method
    public void highlightText(int start, int end, @ColorInt int color) {
        Spannable spanText = Spannable.Factory.getInstance().newSpannable(textView.getText());
        spanText.setSpan(new BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spanText);
    }

    private boolean applyRef(FileRef fr) {
        //TODO Range validation
        switch (fr.style) {
            case FileRef.HIGHLIGHT:
                highlightText(startPos, endPos, colorWay);
                break;
        }

        return true;
    }

    private void applyStylesFromRefFile() {
        try {
            int refCount = FileRef.count();
            Log.d("PSL:applyStyles...", "Numero de registros: " + String.valueOf(refCount));
            for (int i = 1; i <= refCount; i++) {
                FileRef fr = new FileRef();
                fr = FileRef.readId(i);
                fr.showRegInLog();
                applyRef(fr);
                //TODO Next code must be enabled in the future to apply not-deleted references only
//                if (fr.id!=0){
//                    applyRef(fr);
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendEmail(String body) {
        Log.d("eMail", "Coming sendEmail");
        if (body.equals("ERROR")) {
            Toast.makeText(aContext, "ERROR SENDING MESSAGE", Toast.LENGTH_SHORT).show();
            Log.d("eMail", "ERROR");
        } else {
            Log.d("eMail", "Body different to ERROR");
            //String[] TO = {"jesussanzlosa22@gmail.com","pablosaavedra75@gmail.com", "esguello@hotmail.com", "pscandorcia@outlook.es"};
            //String[] CC = {"david@dublindesignstudio.com"};
            String[] types = {
                    "message/rfc822",
                    "text/html",
                    "text/plain"};

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType(types[1]);
            File file = new File(Cfg.APP_DATA_FOLDER, "ScribaExportedNotes.html");
            try {
                FileWriter fw = new FileWriter(file);
                Log.d("html", "Escribiendo fichero: "+body);
                fw.write(body);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!file.exists() || !file.canRead()) {
                Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Uri uri = Uri.parse("file://" + file);
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

            Log.d("gMail", body);

            //emailIntent.putExtra(Intent.EXTRA_EMAIL, TO[0]);
//            emailIntent.putExtra(Intent.EXTRA_CC, CC);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Exported Notes");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
//            emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(body));
//            emailIntent.putExtra(Intent.EXTRA_STREAM,Html.fromHtml(body));

            try {
                Log.d("eMail", "Starting the new activity.");
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                finish();

            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
            }
            //file.delete();
        }
    }
}
