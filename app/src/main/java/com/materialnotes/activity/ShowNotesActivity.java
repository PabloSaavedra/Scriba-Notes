package com.materialnotes.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.wearable.DataApi;
import com.materialnotes.R;
import com.materialnotes.util.FileRef;
import com.materialnotes.util.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ShowNotesActivity extends AppCompatActivity {

    //Variables that we send to the next Activity
    public static final String FileText = "com.example.ScribaNotes.FileText";
    public static final String StartPosition = "com.example.ScribaNotes.StartPosition";
    public static final String EndtPosition = "com.example.ScribaNotes.EndtPosition";
    public static final String CardColor = "com.example.ScribaNotes.Color";
    public static final String CardId = "com.example.ScribaNotes.CardId";

    static int typeColor = 0;
    int yellowCounter = 0;
    int greenCounter = 0;
    int blueCounter = 0;
    int counter = 1;
    int idCard = 0;
    private Context mContext;
    private ScrollView scrollView;
    LinearLayout lLinearLayout;
    private TextView noNotes;

    //Create Buttons
    private Button yellowButton;
    private Button greenButton;
    private Button blueButton;
    public static Context baseContext;

    //CardView and fileExport Array
    ArrayList<CardView> list;
    ArrayList<FileRef> references= new ArrayList<FileRef>();


    /**
     * New activity and uses
     * <p>
     * by Pablo Sotelo
     */

    //Programattely cardview creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notes);

        // Get de ScrollView
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        // Get the application context
        mContext = getApplicationContext();

        // Get the widgets reference from XML layout
        lLinearLayout = (LinearLayout) findViewById(R.id.cardLayout);

        noNotes= (TextView) findViewById(R.id.noNotes);

        //Create a new cardView Array
        list = new ArrayList<CardView>();

        createCardsFromFile();

        //Header Subtitle and back
        getSupportActionBar().setSubtitle(FilenameUtils.getShortFilenameWithoutExtension(Cfg.currentProjectFilename));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ScrollView functionality
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        list.clear();
        references.clear();
        lLinearLayout.removeAllViews();
        yellowCounter=0;
        greenCounter=0;
        blueCounter=0;
        createCardsFromFile();
        yellowButton.setText(String.valueOf(yellowCounter));
        greenButton.setText(String.valueOf(greenCounter));
        blueButton.setText(String.valueOf(blueCounter));
    }

    // Method that load every cardview
    private void createCardsFromFile() {
        try {
            int refCount = FileRef.count();
            //test
            Log.d("PSL:applyStyles...", "Numero de registros: " + String.valueOf(refCount));
            idCard = 0;

            for (int i = 1; i <= refCount; i++) {
                FileRef fr = new FileRef();
                fr = FileRef.readId(i);
                references.add(fr);
                fr.showRegInLog();
                if (fr.id != 0) {
                    //Filter and colorCounter (only merge colored selections, delete if you want bold, italic...... selections)
                    if (fr.style == FileRef.HIGHLIGHT)
                        createCardFromReference(fr);
                    if (fr.color == -256) {
                        yellowCounter++;
                    } else if (fr.color == -16711936) {
                        greenCounter++;
                    } else if (fr.color == -16711681) {
                        blueCounter++;
                    }
                }
            }
            //Add color filter buttons
            yellowButton= (Button) findViewById(R.id.yellowButton);
            yellowButton.setText(String.valueOf(yellowCounter));
            yellowButton.setTextSize(16);
            greenButton = (Button) findViewById(R.id.greenButton);
            greenButton.setText(String.valueOf(greenCounter));
            greenButton.setTextSize(16);
            blueButton = (Button) findViewById(R.id.blueButton);
            blueButton.setText(String.valueOf(blueCounter));
            blueButton.setTextSize(16);

//            //            ----------Work in progress----------------------
            if (yellowCounter == 0)
                yellowButton.setEnabled(false);
             if (greenCounter==0)
                greenButton.setEnabled(false);
             if (blueCounter==0)
                blueButton.setEnabled(false);

            if (yellowCounter==0 && greenCounter==0 && blueCounter==0)
                noNotes.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Method that creates references
    private void createCardFromReference(final FileRef fr) {
        // Initialize a new CardView
        //for (int i=0;i<;i++){
        final CardView card = new CardView(mContext);

        // Set the CardView layoutParams
        CardView.LayoutParams params = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );

        //Turn on Padding settings
        card.setUseCompatPadding(true);

        //Cardview params (margins.....)
        params.setMargins(25, 30, 25, 30);
        params.height = 410;
        card.setLayoutParams(params);
        card.setPaddingRelative(8, 8, 8, 8);

        // Set CardView corner radius
        card.setRadius(9);

        // Set the CardView maximum elevation
        card.setMaxCardElevation(15);

        // Set CardView elevation
        card.setCardElevation(13);

        //load new color Layout
        //Header with the highlight color
        final RelativeLayout cardHeader = new RelativeLayout(getApplicationContext());

        //Le damos parametros
        RelativeLayout.LayoutParams ParamsCabecera =
                (new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT)

                );
        ParamsCabecera.height = 45;

        //Switch to merge color
        switch (fr.style) {
            case FileRef.HIGHLIGHT:
                cardHeader.setBackgroundColor(fr.color);
                break;
            case FileRef.BOLD:
                break;
            case FileRef.ITALIC:
                break;
            case FileRef.NO_STYLE:
                break;
            case FileRef.UNDERLINE:
                break;
        }
        //Upload parameters
        cardHeader.setLayoutParams(ParamsCabecera);

        //Add the layout into the cardview
        card.addView(cardHeader);

        // TODO resize with dp
        //Substring the last "/" and take File name
        int nameIndex = fr.filename.lastIndexOf("/");
        String str = "";

        if (nameIndex == -1) {
            str = fr.filename;
        } else {
            str = fr.filename.substring(nameIndex + 1);
        }

        // Initialize a card Title (from file) in the CardView
        TextView cardTitle = new TextView(mContext);
        cardTitle.setLayoutParams(params);
        cardTitle.setText(str);
        cardTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        cardTitle.setPadding(50, 20, 0, 0);
        cardTitle.setTextColor(Color.BLACK);
        // Put the TextView in CardView
        card.addView(cardTitle);

        // Initialize a card date (from file) in the CardView
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String newFormat = formatter.format(fr.date);

        TextView date = new TextView(mContext);
        date.setLayoutParams(params);
        date.setText(newFormat);
        date.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        date.setPadding(790, 30, 0, 0);
        date.setTextColor(Color.GRAY);
        // Put the date in CardView
        card.addView(date);


        //Initialize the text Selection
        final TextView selectedText = new TextView(mContext);
        selectedText.setLayoutParams(params);

        //TODO changue parameters
        //TODO erase TRIM when the  FileRef goes properly
        //TODO maybe we have a bug on the fileref (we need to insert trim)
        Log.i("TAG", fr.filename + "*");
        final String textNote = readTextRangeFromFile(fr.start, fr.end, fr.filename.trim());
        selectedText.setText(textNote);
        selectedText.setPadding(50, 100, 40, 70);
        selectedText.setTextColor(Color.GRAY);

        // Put the TextView in CardView
        card.addView(selectedText);

        //Set an own Id to every single cardView
        idCard++;
        card.setId(idCard);

        //Convert a cardview into a clickable object
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardView c = (CardView) view;
                Log.i("TAG", "The index is " + c.getId());


                int greenCounter = 0;
                int blueCounter = 0;
                //send the "messages" with the variables who we need to set the text (color, start position, end position and text) into the net activity
                Intent intent = new Intent(mContext.getApplicationContext(), ShowSpecificNoteActivity.class);
                String message = (fr.filename.trim());
                int message02 = (fr.start);
                int message03 = (fr.end);
                int message04 = (fr.color);
                int message05 = (fr.id);

                //checking
                Log.i("TAG", "Nombre del archivo " + message.toString());
                //send
                intent.putExtra(FileText, message);
                intent.putExtra(StartPosition, message02);
                intent.putExtra(EndtPosition, message03);
                intent.putExtra(CardColor, message04);
                intent.putExtra(CardId, message05);
                startActivity(intent);
            }

        });

        // Finally, add the CardView into the root layout
        lLinearLayout.addView(card);

        //Add the card into the array list and plus counter
        list.add(card);
        counter++;

        //Delete cardview by longclick
        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                card.setVisibility(View.VISIBLE);
                card.setAlpha(0.0f);

                // Start the animation
                card.animate()
                        .translationX(card.getWidth())
                        .alpha(1.0f);
                Handler h = new Handler();

                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lLinearLayout.removeView(card);
                        FileRef.deleteId(fr.id);
                        list.remove(list.get(list.size() - 1));

                        if (fr.color == -256) {
                            yellowCounter--;
                        } else if (fr.color == -16711936) {
                            greenCounter--;
                        } else if (fr.color == -16711681) {
                            blueCounter--;
                        }
                        yellowButton.setText(String.valueOf(yellowCounter));
                        greenButton.setText(String.valueOf(greenCounter));
                        blueButton.setText(String.valueOf(blueCounter));
                        if (yellowCounter == 0)
                            yellowButton.setEnabled(false);
                        if (greenCounter==0)
                            greenButton.setEnabled(false);
                        if (blueCounter==0)
                            blueButton.setEnabled(false);
                        if (yellowCounter==0 && greenCounter==0 && blueCounter==0)
                            noNotes.setVisibility(View.VISIBLE);

                    }

                }, 500);
                return true;
            }
        });
    }

    //ActionBar uses
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_notes, menu);
        return true;
    }

    //Clickable usages Export, back and Delete/-----IF YOU WANT TO PU ANY BUTTON OR USAGE MORE JUST PUT MORE CASES----
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                try {
                    //Toast.makeText(getApplicationContext(), "File Export Done!", Toast.LENGTH_SHORT).show();
                    sendEmail(FileRef.exportRefs(references));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case android.R.id.home:
                onBackPressed();
            break;

            case R.id.delete:

                new AlertDialog.Builder(this)
                        .setMessage("Are you sure to delete permanently all the references?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                lLinearLayout.removeAllViews();
//                list.remove(list);
                list.clear();
                FileRef.deleteRefsFile(baseContext);
                yellowButton.setText(String.valueOf(0));
                greenButton.setText(String.valueOf(0));
                blueButton.setText(String.valueOf(0));
                                            yellowButton.setEnabled(false);
                                            greenButton.setEnabled(false);
                                            blueButton.setEnabled(false);
                                            noNotes.setVisibility(View.VISIBLE);
                                    }
                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
        }

        return true;
    }


    //Read from File method
    private String readTextRangeFromFile(int start, int end, String filename) {

        File file = new File(filename);
        if (!file.exists()) {
            return "File not found: " + filename;
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
        //TODO range validation
        fullText = fullText.substring(start, end);
        return fullText;
    }


    //color cardView selection buttons
    public void showFilterNotesYellow(View view) {
        Intent intent = new Intent(this, ActivityColorFilterNotes.class);
        typeColor = -256;
        startActivity(intent);
    }

    public void showFilterNotesGreen(View view) {
        Intent intent = new Intent(this, ActivityColorFilterNotes.class);
        typeColor = -16711936;
        startActivity(intent);
    }

    public void showFilterNotesBlue(View view) {
        Intent intent = new Intent(this, ActivityColorFilterNotes.class);
        typeColor = -16711681;
        startActivity(intent);
    }

    //Method to find the text on fileText
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

    //Export to a email
    /**
     * That method receives a HTML code and attach to a gmail.
     * @param body
     */
    private void sendEmail(String body) {
        Log.d("eMail", "Coming sendEmail");
        if (body.equals("ERROR")) {
            Toast.makeText(mContext, "ERROR SENDING MESSAGE", Toast.LENGTH_SHORT).show();
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

