
/**
 * New activity and uses
 *
 * by Pablo Sotelo
 */

package com.materialnotes.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
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

import com.materialnotes.R;
import com.materialnotes.util.FileRef;
import com.materialnotes.util.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ActivityColorFilterNotes extends AppCompatActivity {

    //Variables that we send to the next Activity
    public static final String FileText ="com.example.ScribaNotes.FileText";
    public static final String StartPosition ="com.example.ScribaNotes.StartPosition";
    public static final String EndtPosition ="com.example.ScribaNotes.EndtPosition";
    public static final String CardColor="com.example.ScribaNotes.Color";
    public static final String CardId = "com.example.ScribaNotes.CardId";

    String colorType;
    int idCard=0;
    int counterColorFilter = 1;
    private Context mContext;
    private ScrollView scrollView;
    private Button colorButton;
    TextView noNotes;

    LinearLayout lLinearLayout;
    ArrayList<CardView> list;
    ArrayList<FileRef> references= new ArrayList<FileRef>();


    /**
     * New activity and uses
     *
     * by Pablo Sotelo
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_filter_notes);


        //Here begins our code
        scrollView = (ScrollView) findViewById(R.id.scrollViewColorFilter);

        // Get the application context
        mContext = getApplicationContext();

        // Get the widgets reference from XML layout
        lLinearLayout = (LinearLayout) findViewById(R.id.cardSelectedLayout);

        //Get the NoNotes reference
        noNotes=(TextView) findViewById(R.id.noNotes);

        //Create a new cardView Array
        list = new ArrayList<CardView>();

        createCardsFromFile();
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
        createCardsFromFile();

    }
    // Method that load every cardview
    private void createCardsFromFile(){
        try {
            int refCount = FileRef.count();

            //Test
            Log.d("PSL:applyStyles...","Numero de registros: "+String.valueOf(refCount));
            for (int i=1; i<=refCount; i++) {
                FileRef fr = new FileRef();
                fr = FileRef.readId(i);
                fr.showRegInLog();
                if (fr.id != 0) {
                    //Filter (only merge colored selections, delete if you want bold, italic...... selections)
                    if ((fr.style == FileRef.HIGHLIGHT) && (fr.color == ShowNotesActivity.typeColor)) {
                        createCardFromReference(fr);
                        references.add(fr);
                        if (fr.color == -256) {
                            colorType="Yellow";
                        } else if (fr.color == -16711936) {
                            colorType="Green";
                        } else if (fr.color == -16711681) {
                            colorType = "Blue";

                        }else{
                            colorButton.setBackgroundColor(Color.WHITE);
                        }
                        getSupportActionBar().setTitle(colorType+" Notes");
                        colorButton = (Button) findViewById(R.id.colorButton);
                        colorButton.setBackgroundColor((fr.color));

                    }
                }
            }
            if (list.size()==0) {
                noNotes.setVisibility(View.VISIBLE);
                colorButton.setVisibility(View.INVISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Method to create cardViews from references
    private void createCardFromReference(final FileRef fr) {
        // Initialize a new CardView
        final CardView card = new CardView(mContext);


        // Set the CardView layoutParams
        CardView.LayoutParams params = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );

        //Turn on Padding setting
        card.setUseCompatPadding(true);

        //Margin values
        params.setMargins(25,30,25,30);
        params.height=410;


        card.setLayoutParams(params);

        //New parameters
        card.setPaddingRelative(8,8,8,8);

        // Set CardView corner radius
        card.setRadius(9);

        // Set the CardView maximum elevation
        card.setMaxCardElevation(15);

        // Set CardView elevation
        card.setCardElevation(13);

        //load new color Layout
        //CardView Header params
        RelativeLayout cabecera = new RelativeLayout(getApplicationContext());

        RelativeLayout.LayoutParams ParamsCabecera=
                (new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT)

                );
        ParamsCabecera.height=45;

        //Switch color from FileRef
        switch (fr.style){
            case FileRef.HIGHLIGHT:
                cabecera.setBackgroundColor(fr.color);
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
        cabecera.setLayoutParams(ParamsCabecera);

        //Add the layout into the cardview
        card.addView(cabecera);

        // TODO resize with dp
        // Initialize a card number in the CardView
        TextView cardNumber = new TextView(mContext);
        cardNumber.setLayoutParams(params);
        //tv.setTypeface(null, Typeface.BOLD);
        cardNumber.setText("Note " + String.valueOf(counterColorFilter));
        cardNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        cardNumber.setPadding(50,20,0,0);
        cardNumber.setTextColor(Color.BLACK);
        // Put the TextView in CardView
        card.addView(cardNumber);

        //Substring the last "/" and take File name
        int nameIndex= fr.filename.lastIndexOf("/");
        String str= "";

        if (nameIndex == -1){
            str=fr.filename;
        }else{
            str= fr.filename.substring(nameIndex+1);
        }

        // Initialize a card Title (from file) in the CardView
        TextView cardTitle = new TextView(mContext);
        cardTitle.setLayoutParams(params);
        cardTitle.setText(str);
        cardTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        cardTitle.setPadding(235,25,0,0);
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
        date.setPadding(790,30,0,0);
        date.setTextColor(Color.GRAY);
        // Put the TextView in CardView
        card.addView(date);

        //Initialize the text Selection
        TextView textPrueba = new TextView(mContext);
        textPrueba.setLayoutParams(params);
        String textNote = readTextRangeFromFile(fr.start, fr.end, fr.filename.trim());
        textPrueba.setText(textNote);
        // textPrueba.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15); //Freeze app
        textPrueba.setPadding(50,100,40,70);
        textPrueba.setTextColor(Color.GRAY);

        // Put the TextView in CardView
        card.addView(textPrueba);

        //Set an own Id to every single cardView
        idCard++;
        card.setId(idCard);

        //---------------------==WORK IN PROGRESS==---------------------------------------------
        //Convert a cardview into a clickable object
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardView c = (CardView)view;
                Log.i("TAG", "The index is " + c.getId());

                //send the "messages" with the variables who we need to set the text (color, start position, end position and text)
                Intent intent=new Intent(mContext.getApplicationContext(), ShowSpecificNoteActivity.class);
                String message=(fr.filename.trim());
                int message02=(fr.start);
                int message03=(fr.end);
                int message04=(fr.color);
                int message05 = (fr.id);
                //String message02= toIntfr.start);
                Log.i("TAG","Nombre del archivo " + message.toString());
                // String message= textPrueba.getText().toString();
                //String message =("que pasa tiu").toString();
                intent.putExtra(FileText, message);
                intent.putExtra(StartPosition, message02);
                intent.putExtra(EndtPosition, message03);
                intent.putExtra(CardColor,message04);
                intent.putExtra(CardId, message05);
                startActivity(intent);
            }

        });

        // Finally, add the CardView in root layout
        lLinearLayout.addView(card);

        //Add the card into the array list and plus counter
        list.add(card);
        counterColorFilter++;


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

                        if (list.size()==0) {
                            noNotes.setVisibility(View.VISIBLE);
                            colorButton.setVisibility(View.INVISIBLE);
                        }
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

    //Clickable usages.../-----IF YOU WANT TO PUt ANY BUTTON OR USAGE MORE JUST PUT MORE CASES----
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Export into an email
            case R.id.export:
                try {
                    sendEmail(FileRef.exportRefs(references));
                    //Toast.makeText(getApplicationContext(), "File Export Done!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
            //delete function, only colored selection cards
            case R.id.delete:
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure to delete permanently all the " + colorType + " references?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                lLinearLayout.removeAllViews();
                for (int i=0; i<references.size();i++){
                    FileRef.deleteId(references.get(i).id);
                }
                //FileRef.deleteRefsFile(fr.id);
                                noNotes.setVisibility(View.VISIBLE);
                                colorButton.setVisibility(View.INVISIBLE);
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
        if (!file.exists()){
            return "File not found: "+ filename;
        }
        FileInputStream inputStream = null;
        byte[] buffer = new byte[(int)file.length()];

        try {
            inputStream = new FileInputStream(file);
            inputStream.read(buffer);
            inputStream.close();
        } catch (Exception e) {
            Log.d("PSL:createTxtFileFor...","Error loading test file");
            e.printStackTrace();
        }
        String fullText = new String(buffer);
        //TODO range validation
        fullText=fullText.substring(start,end);
        return fullText;
    }
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


