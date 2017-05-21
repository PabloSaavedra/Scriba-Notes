package com.materialnotes.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.ColorInt;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.materialnotes.R;
import com.materialnotes.data.Note;
import com.materialnotes.util.FileRef;
import com.materialnotes.util.FilenameUtils;
import com.materialnotes.util.Strings;
import com.materialnotes.view.ShowHideOnScrollThree;
import com.materialnotes.view.VerticalScrollView;
import com.shamanland.fab.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import no.nordicsemi.android.scriba.hrs.HRSActivity;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Edit notes activity
 *
 * Created by Ian 2016
 * Lots of updates and fixes by Pablo Saavedra on 06/04/2017.
 *
 **/
@ContentView(R.layout.activity_edit_note)
public class EditNoteActivity extends RoboActionBarActivity{

    private static final String EXTRA_NOTE = "EXTRA_NOTE";
    private static final int FILTER_ID = 1;

    @InjectView(R.id.note_content) private EditText noteContentText;
    @InjectView(R.id.popup_button) private Button popupButton;

    private ActionMode mActionMode = null;
    private String mode = "Select"; //Starting mode is selection

    Vibrator v;

    private boolean activityStopped = false;
    private Thread squeezeMonitorThread;

    TextView hrmValueTextView;
    Intent openFileIntent;

    int count; //variable to keep track of number of times popup menu opens when condition is met i.e. when value is between 0 and 300.

    int setOne, setTwo, setThree, setFour;

    int touchPositionStart;
    int touchPositionEnd;

    public TextView selectStartTV=null, selectEndTV=null; //TextView's for testing

    boolean modeLocked = false; //Says if the operation mode is locked
    boolean readyForNewMode = true; //Says if the scriba can change the operation mode
    boolean dragModeOn = true; //Switch between selection and drag while unclicked
    int colorToDelete = 0;

    final int DRAG_NO_DIRECTION = 0;
    final int DRAG_UP_DIRECTION = 1;
    final int DRAG_DOWN_DIRECTION = 2;

    int dragDirection = DRAG_NO_DIRECTION;

    static final int OPEN_FILE_REQUEST = 1;

    float firstTouchX, firstTouchY;
    float lastTouchX, lastTouchY;

    int vibrationTime = 10; //vibration time when function change
    int lastStateVibration = 0; //last mode that made a phone vibration

    //ScaleGestureDetector mScaleDetector;

    private TextView noteTitleText;
    private VerticalScrollView scrollView;
    private View.OnTouchListener originalScrollListener;

    public static Context baseContext;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActionModeStarted(final ActionMode mode) {
        if (mActionMode == null) {
            mActionMode = mode;

            Menu menu = mode.getMenu();
            MenuItem item1 = menu.findItem(android.R.id.selectAll);
            item1.setVisible(false);
        }

        super.onActionModeStarted(mode);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onContextualMenuItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear1:
                unformatContent(item);
                break;
            default:
                break;
        }

        // This will likely always be true, but check it anyway, just in case
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        mActionMode = null;
        super.onActionModeFinished(mode);
    }

    /**
     * Makes the intent to call the activity with an existing note
     *
     * @param context the context
     * @param note    the note to edit
     * @return the Intent.
     */
    public static Intent buildIntent(Context context, Note note) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE, note);
        return intent;
    }

    /**
     * Makes the intent to call the activity for creating a note
     *
     * @param context the context that calls the activity
     * @return the Intent.
     */
    public static Intent buildIntent(Context context) {
        return buildIntent(context, null);
    }

    /**
     * Gets the edited note
     *
     * @param intent the intent from onActivityResult
     * @return the updated note
     */
    public static Note getExtraNote(Intent intent) {
        return (Note) intent.getExtras().get(EXTRA_NOTE);
    }

    public void showNotes(View view){
        Intent intent = new Intent(this, ShowNotesActivity.class);
        startActivity(intent);
    }

    private void lockScrollView(boolean val){
        if (val){
            //New listener to control scrollView
            View.OnTouchListener lockedScrollListener = new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //do nothing = lock scroll
                    return true;
                }
            };
            scrollView.setOnTouchListener(lockedScrollListener);
        }else{
            //restore original onTouchListener to scrollView
            scrollView.setOnTouchListener(originalScrollListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        activityStopped=true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        startSqueezeMonitorThread();
        activityStopped=false;
        openTextFileAndApplyRefs(Cfg.currentFileFilename);
    }

    /**
         * {@inheritDoc}
         */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the name of the file selected on MainActivity
        Intent intent = getIntent();
        Cfg.currentFileFilename = intent.getStringExtra(CurrentProjectFilesActivity.FILENAME);
        Log.d("PSL:FileToOpen","File to open: "+ Cfg.currentFileFilename);

        //Header Subtitle and back
        getSupportActionBar().setTitle(FilenameUtils.getShortFilenameWithoutExtension(Cfg.currentProjectFilename));
        getSupportActionBar().setSubtitle(FilenameUtils.getShortFilename(Cfg.currentFileFilename));

        hrmValueTextView = new TextView(this);

        baseContext=getBaseContext();
        selectStartTV = (TextView) findViewById(R.id.selection_start_position);
        selectEndTV = (TextView) findViewById(R.id.selection_end_position);
        noteTitleText = (TextView) findViewById(R.id.note_title);
        scrollView = (VerticalScrollView) findViewById(R.id.editNoteScrollView);

        //Capture original listener
        originalScrollListener = getOnTouchListener(scrollView);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Shows the go back arrow

        // Get instance of Vibrator from current Context
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setOne = SpinnerActivity.settingOne;
        setTwo = SpinnerActivity.settingTwo;
        setThree = SpinnerActivity.settingThree;
        setFour = SpinnerActivity.settingFour;

        Log.i("Log", setOne+": "+setTwo+": "+setThree+": "+setFour);

        //New listener to control scrollView
        scrollView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("PSL:onTouch:Action", "ScrollView: Action down");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("PSL:onTouch:Action", "ScrollView: Action move");
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        Log.d("PSL:onTouch:Action", "ScrollView: Action pointer up");
                    case MotionEvent.ACTION_CANCEL:
                        Log.d("PSL:onTouch:Action", "ScrollView: Action cancel");
                    case MotionEvent.ACTION_UP:
                        Log.d("PSL:onTouch:Action", "ScrollView: Action up");
                        break;
                    default:
                }
                return true;
            }
        });


        noteContentText.setOnTouchListener(new View.OnTouchListener() {
            //boolean moved = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("PSL:onTouch:Action","noteContentText: Action down");
                        dragDirection=DRAG_NO_DIRECTION;

                        if (mode.equals("Select")) {
                            //Do nothing
                            dragModeOn=true;
                        } else {
                            modeLocked = true; //Doing this the operation mode will remain active until release and press again
                            readyForNewMode = false;
                            firstTouchX = (int) event.getX();
                            firstTouchY = (int) event.getY();
                            lastTouchX = firstTouchX;
                            lastTouchY = firstTouchY;
                            touchPositionStart = noteContentText.getOffsetForPosition(firstTouchX, firstTouchY);
                            touchPositionEnd = touchPositionStart;
                            selectStartTV.setText("Start: " + String.valueOf(touchPositionStart));
                            selectEndTV.setText("End: "+String.valueOf(touchPositionEnd));
                            try {
                                colorToDelete=FileRef.getLastColor(touchPositionStart, Cfg.currentFileFilename);
                                Log.d("PSL:LastColor", "LastColor = " + String.valueOf(colorToDelete) + ", TouchPosStart = "+ String.valueOf(touchPositionStart));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.d("PSL:onTouch:Action","noteContentText: Action move");
                        if (dragModeOn){
                            //Drag only
                        }else{
                            //Capture selection
                            lastTouchX = (int) event.getX();
                            lastTouchY = (int) event.getY();

                            touchPositionEnd = noteContentText.getOffsetForPosition(lastTouchX, lastTouchY);
                            selectEndTV.setText("End: "+String.valueOf(touchPositionEnd));

                            //Check if the user is deleting
                            if (dragDirection == DRAG_NO_DIRECTION) {
                                if (touchPositionStart<touchPositionEnd){
                                    dragDirection = DRAG_DOWN_DIRECTION;
                                } else if (touchPositionStart>touchPositionEnd){
                                    dragDirection = DRAG_UP_DIRECTION;
                                }
                            }
                            formatSelectedText(touchPositionStart, touchPositionEnd);
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        Log.d("PSL:onTouch:Action","noteContentText: Action pointer up");
                    case MotionEvent.ACTION_CANCEL:
                        Log.d("PSL:onTouch:Action","noteContentText: Action cancel");
                    case MotionEvent.ACTION_UP:
                        Log.d("PSL:onTouch:Action","noteContentText: Action up");
                        if (!dragModeOn){
                            dragDirection=DRAG_NO_DIRECTION;
                            touchPositionStart=touchPositionEnd;
                            firstTouchX=lastTouchX;
                            firstTouchY=lastTouchY;
                            colorToDelete=0;
                        }

                        FileRef.showAllRefsInLog();

                        break;

                    default:;
                }

                //return true because we want to send the event
                return true;
            }
        });

//        //TODO 2017-04-28 new OnTouchListener, from a new point of view
//        noteContentText.setOnTouchListener(new View.OnTouchListener() {
//            // The ‘active pointer’ is the one currently moving our object.
//            private int mActivePointerId = INVALID_POINTER_ID;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent ev) {
//                // Let the ScaleGestureDetector inspect all events.
//                //mScaleDetector.onTouchEvent(ev);
//
//                final int action = MotionEventCompat.getActionMasked(ev);
//
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN: {
//                        Log.d("PSL:onTouch:Action","noteContentText: Action down");
//                        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
//                        final float x = MotionEventCompat.getX(ev, pointerIndex);
//                        final float y = MotionEventCompat.getY(ev, pointerIndex);
//
//                        // Remember where we started (for dragging)
//                        mLastTouchX = x;
//                        mLastTouchY = y;
//                        // Save the ID of this pointer (for dragging)
//                        mActivePointerId = MotionEventCompat.getPointerbId(ev, 0);
//
//                        //Set text start position
//                        touchPositionStart = noteContentText.getOffsetForPosition(x, y);
//                        //Log.d("PSL:onTouch","touchPositionStart:StartPosition:"+touchPositionStart);
//                        selectStartValue = noteContentText.getSelectionStart();
//                        selectEndValue = selectStartValue;
//                        selectStartTV.setText("Start: " + String.valueOf(selectStartValue));
//                        selectEndTV.setText("| End: " + String.valueOf(selectEndValue));
//
//                        break;
//                    }
//
//                    case MotionEvent.ACTION_MOVE: {
//                        Log.d("PSL:onTouch:Action","noteContentText: Action move");
//                        // Find the index of the active pointer and fetch its position
//                        final int pointerIndex =
//                                MotionEventCompat.findPointerIndex(ev, mActivePointerId);
//
//                        final float x = MotionEventCompat.getX(ev, pointerIndex);
//                        final float y = MotionEventCompat.getY(ev, pointerIndex);
//
//                        // Calculate the distance moved
//                        final float dx = x - mLastTouchX;
//                        final float dy = y - mLastTouchY;
//
//                        mPosX += dx;
//                        mPosY += dy;
//
//                        //invalidate();
//
//                        // Remember this touch position for the next move event
//                        mLastTouchX = x;
//                        mLastTouchY = y;
//
//                        break;
//                    }
//
//                    case MotionEvent.ACTION_UP: {
//                        Log.d("PSL:onTouch:Action","noteContentText: Action up");
//                        mActivePointerId = INVALID_POINTER_ID;
//                        break;
//                    }
//
//                    case MotionEvent.ACTION_CANCEL: {
//                        Log.d("PSL:onTouch:Action","noteContentText: Action cancel");
//                        mActivePointerId = INVALID_POINTER_ID;
//                        break;
//                    }
//
//                    case MotionEvent.ACTION_POINTER_UP: {
//                        Log.d("PSL:onTouch:Action","noteContentText: Action pointer up");
//
//                        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
//                        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
//
//                        if (pointerId == mActivePointerId) {
//                            // This was our active pointer going up. Choose a new
//                            // active pointer and adjust accordingly.
//                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//                            mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
//                            mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
//                            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
//                        }
//                        break;
//                    }
//                }
//                return true;
//            }
//        });

        openTextFileAndApplyRefs(Cfg.currentFileFilename);
        lockScrollView(false);

        //set transparent color for selected text background
        noteContentText.setHighlightColor(Color.parseColor("#00000000"));

        startSqueezeMonitorThread();

    }

    private void openTextFileAndApplyRefs(String filenameFullPath){
        String fileText = readFromTxtFile(filenameFullPath);
        if (!fileText.equals("")) {
            noteTitleText.setText(filenameFullPath);
            noteContentText.setText(fileText);
            applyStylesFromRefFile(filenameFullPath);
        } else {
            noteTitleText.setText(filenameFullPath + " [Error: File not found]");
            noteContentText.setText("");
        }
    }

    public void deleteRefFile(View view){
        FileRef.deleteRefsFile(baseContext);
        noteContentText.setText(readFromTxtFile(Cfg.currentFileFilename));
        noteContentText.clearFocus();//Clear selection?? Let's try;
    }

    public void openFile(View view){
        openFileIntent = new Intent(this, OpenFileActivity.class);
        startActivityForResult(openFileIntent, OPEN_FILE_REQUEST);
    }

    private String readFromTxtFile(String filename){
        File file = new File(filename);
        if (!file.exists()){
            //TODO Change error msg to @strings
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            return "";
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
        return fullText;
    }

    private void applyStylesFromRefFile(String filenameFullPath){
        try {
            int refCount = FileRef.count();
            for (int i=1; i<=refCount; i++){
                FileRef fr = new FileRef();
                fr = FileRef.readId(i);
                if (fr.filename.equals(filenameFullPath) && fr.id!=0){
                    applyRef(fr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Applies a file reference style to text
     * @param fr= reference to be applied
     * @return true if success, false if it fails
     */
    private boolean applyRef(FileRef fr){
        //TODO Range validation
        //Log.d("PSL:applyRef",String.valueOf(fr.start)+", "+String.valueOf(fr.end)+", "+String.valueOf(fr.color));
        switch (fr.style){
            case FileRef.HIGHLIGHT:
                highlightText(fr.start,fr.end,fr.color);
                break;
            case FileRef.UNDERLINE:
                underlineText(fr.start,fr.end);
                break;
            case FileRef.BOLD:
                boldText(fr.start,fr.end);
                break;
            case FileRef.ITALIC:
                italicText(fr.start,fr.end);
                break;
        }
        return true;
    }

    /**
     *
     * @param fr FileRef data source
     * @param start overrides fr.start
     * @param end overrides fr.end
     */
    private void applyRef(FileRef fr, int start, int end){
        //TODO Range validation
        switch (fr.style){
            case FileRef.HIGHLIGHT:
                highlightText(start,end,fr.color);
                break;
            case FileRef.UNDERLINE:
                underlineText(start,end);
                break;
            case FileRef.BOLD:
                boldText(start,end);
                break;
            case FileRef.ITALIC:
                italicText(start,end);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //This shows the Squeeze Value in the menu bar
        getMenuInflater().inflate(R.menu.edit_note, menu);
        hrmValueTextView.setText(String.valueOf(HRSActivity.mHrmValue));
        hrmValueTextView.setTextColor(getResources().getColor(R.color.white_circle));
        //hrmValueTextView.setOnClickListener((View.OnClickListener) this);
        hrmValueTextView.setPadding(5, 0, 5, 0);
        hrmValueTextView.setTypeface(null, Typeface.BOLD);
        hrmValueTextView.setTextSize(14);
        menu.add(0, FILTER_ID, 1, String.valueOf(HRSActivity.mHrmValue)).setActionView(hrmValueTextView).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                openFileIntent = new Intent(this, OpenFileActivity.class);
                startActivityForResult(openFileIntent, OPEN_FILE_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * @return {@code true} is the note has title and content; {@code false} every other case
     */
    private boolean isNoteFormOk() {
        return !Strings.isNullOrBlank(noteContentText.getText().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        // Note not created or updated
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    //method to get rid of all formats for specific selected text
    public void unformatContent(MenuItem item) {
        if (noteContentText.hasFocus()) {
            int startSelection = noteContentText.getSelectionStart();
            int endSelection = noteContentText.getSelectionEnd();

            Spannable str = noteContentText.getText();
            StyleSpan[] ss = str.getSpans(startSelection, endSelection, StyleSpan.class);

            for (int i = 0; i < ss.length; i++) {
                if (ss[i].getStyle() == Typeface.BOLD || ss[i].getStyle() == Typeface.ITALIC) {
                    str.removeSpan(ss[i]);
                }
            }

            UnderlineSpan[] ulSpan = str.getSpans(startSelection, endSelection, UnderlineSpan.class);
            for (int i = 0; i < ulSpan.length; i++) {
                str.removeSpan(ulSpan[i]);
            }

            BackgroundColorSpan[] bgSpan = str.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
            for (int i = 0; i < bgSpan.length; i++) {
                str.removeSpan(bgSpan[i]);
            }

            noteContentText.setText(str);
        }
    }

    public void selectionMode(int setMode){
        switch (setMode){
            case 0:
                mode = "Highlight";
                popupButton.setBackgroundResource(R.drawable.ic_yellow_highlighter);
                break;
            case 1:
                mode = "Highlight Blue";
                popupButton.setBackgroundResource(R.drawable.ic_blue_highlighter);

                break;
            case 2:
                mode = "Highlight Green";
                popupButton.setBackgroundResource(R.drawable.ic_green_highlighter);
                break;
            case 3:
                mode = "Bold";
                break;
            case 4:
                mode = "Italic";
                break;
            case 5:
                mode = "Delete";
                break;
            case 6:
                mode = "Select";
                popupButton.setBackgroundResource(R.drawable.ic_scroll);
                break;
            case 7:
                mode = "Underline";
                break;
        }
    }

    public void deviceVibration(int currentState){
        if (lastStateVibration!=currentState){
            v.vibrate(vibrationTime);
            lastStateVibration=currentState;
            Log.d("PSL:Vibrate","Vibrate!");
        }
    }
    //method that reads the current value from the Scriba device
    public void checkScribaValue() {
        float val = HRSActivity.mHrmValue;
        float percent = val / Cfg.MAX_SCRIBA_VALUE;

        if (percent >= Cfg.INTERVAL_UNCLICKED) {
            if (modeLocked) {
                readyForNewMode = true;
            }else{
                dragModeOn=true;
                lockScrollView(false);
                selectionMode(setOne);
                deviceVibration(1);
            }
        } else if (percent>=Cfg.INTERVAL_1){
            if (readyForNewMode){
                modeLocked=false;
                dragModeOn=false;
                lockScrollView(true);
                selectionMode(setTwo);
                deviceVibration(2);
            }
        } else if (percent>=Cfg.INTERVAL_2) {
            if (readyForNewMode) {
                modeLocked=false;
                dragModeOn=false;
                lockScrollView(true);
                selectionMode(setThree);
                deviceVibration(3);
            }
        } else {
            if (readyForNewMode) {
                modeLocked=false;
                dragModeOn=false;
                lockScrollView(true);
                selectionMode(setFour);
                deviceVibration(4);
            }
        }
    }

    //Thread Methods
    public void startSqueezeMonitorThread() {
            Log.d("PSL:Squeeze","New Squeeze Monitor Thread created.");

            squeezeMonitorThread = new Thread() {

                @Override
                public void run() {
                    try {
                        while (!isInterrupted() && !activityStopped) {
                            Thread.sleep(25);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Showing percent values
                                    float val = HRSActivity.mHrmValue;
                                    //if Scriba reaches higher values, we set a new maximum
                                    if (val > Cfg.MAX_SCRIBA_VALUE) {
                                        Cfg.MAX_SCRIBA_VALUE = val;
                                    }
                                    //Now we calculate percent values
                                    val = HRSActivity.mHrmValue / Cfg.MAX_SCRIBA_VALUE;
                                    hrmValueTextView.setText(String.valueOf(HRSActivity.mHrmValue) + " (" + String.format(Locale.US, "%.2f", val * 100) + "%)");
                                    checkScribaValue();

                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            squeezeMonitorThread.start();
    }

    //function methods
    public void formatSelectedText(int start, int end) {
        ArrayList<Integer> affectedId = new ArrayList<Integer>();
        int rrStart=0, rrEnd=0; //Rewrite color range

        if (start!=end) {
            if (start > end) {
                int aux = end;
                end = start;
                start = aux;
            }

            if (dragDirection == DRAG_UP_DIRECTION) {
                //is deleting the notes
                //TODO find the color to delete and send that as a parameter
                //deleteStyle(selectStartValue,selectEndValue);
                highlightText(start, end, Color.WHITE);//***
                try {
                    affectedId=FileRef.eraseRef(start, end, colorToDelete, Cfg.currentFileFilename);
                    //TODO Draw affected refs again
                    for (int i:affectedId){
                        FileRef fr = new FileRef();
                        fr = FileRef.readId(i);
                        //applyRef(fr, fr.start, fr.end);
                        applyRef(fr);
                    }
                    affectedId.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (dragDirection == DRAG_DOWN_DIRECTION) {
                try {
                    /* if (mode.equals("Delete")) {
                    } else if (mode.equals("Underline")) {
                        underlineText(start, end);
                    } else*/ if (mode.equals("Highlight")) {
                        highlightText(start, end, Color.YELLOW);
                        new FileRef(touchPositionStart, touchPositionEnd,255,255,0, FileRef.HIGHLIGHT, Cfg.currentFileFilename);
                    } else if (mode.equals("Highlight Blue")) {
                        highlightText(start, end, Color.CYAN);
                        new FileRef(touchPositionStart, touchPositionEnd,0,255,255, FileRef.HIGHLIGHT, Cfg.currentFileFilename);
                    } else if (mode.equals("Highlight Green")) {
                        highlightText(start, end, Color.GREEN);
                        new FileRef(touchPositionStart, touchPositionEnd,0,255,0, FileRef.HIGHLIGHT, Cfg.currentFileFilename);
                    }/* else if (mode.equals("Bold")) {
                        boldText(start, end);
                    } else if (mode.equals("Italic")) {
                        italicText(start, end);
                    }*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Underline text from start to end
     * @param start
     * @param end
     * by Pablo Saavedra
     */
    public void underlineText(int start, int end) {
        SpannableStringBuilder formattedText = (SpannableStringBuilder) noteContentText.getText();

        //removes previous style
        UnderlineSpan[] ulSpan = formattedText.getSpans(start, end, UnderlineSpan.class);
        for (int i = 0; i < ulSpan.length; i++) {
            formattedText.removeSpan(ulSpan[i]);
        }

        //apply underline format
        formattedText.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Highlights text from start to end with the given color
     * @param start first letter position
     * @param end last letter position
     * @param color color to be applied to text background
     *
     * by Pablo Saavedra
     */
    public void highlightText(int start, int end, @ColorInt int color) {
        if (start>end){
            int aux=end;
            end=start;
            start=aux;
        }
        if (start<end){
            SpannableStringBuilder text = new SpannableStringBuilder();
            text = (SpannableStringBuilder) noteContentText.getText();
            //text.setSpan(new BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(color), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    public void boldText(int start, int end){
        SpannableStringBuilder formattedText = (SpannableStringBuilder) noteContentText.getText();

        //removes previous style
        StyleSpan[] boldSpan = formattedText.getSpans(start, end, StyleSpan.class);
        for (int i = 0; i < boldSpan.length; i++) {
            formattedText.removeSpan(boldSpan[i]);
        }

        //apply bold format
        formattedText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void italicText(int start, int end){
        SpannableStringBuilder formattedText = (SpannableStringBuilder) noteContentText.getText();

        //removes previous style
        StyleSpan[] italicSpan = formattedText.getSpans(start, end, StyleSpan.class);
        for (int i = 0; i < italicSpan.length; i++) {
            formattedText.removeSpan(italicSpan[i]);
        }

        //apply italic format
        formattedText.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Returns the current View.OnClickListener for the given View
     * @param view the View whose click listener to retrieve
     * @return the View.OnClickListener attached to the view; null if it could not be retrieved
     */
    public View.OnTouchListener getOnTouchListener(View view) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getOnTouchListenerV14(view);
        } else {
            return getOnTouchListenerV(view);
        }
    }

    //Used for APIs lower than ICS (API 14)
    private View.OnTouchListener getOnTouchListenerV(View view) {
        View.OnTouchListener retrievedListener = null;
        String viewStr = "android.view.View";
        Field field;

        try {
            field = Class.forName(viewStr).getDeclaredField("mOnTouchListener");
            retrievedListener = (View.OnTouchListener) field.get(view);
        } catch (NoSuchFieldException ex) {
            Log.e("Reflection", "No Such Field.");
        } catch (IllegalAccessException ex) {
            Log.e("Reflection", "Illegal Access.");
        } catch (ClassNotFoundException ex) {
            Log.e("Reflection", "Class Not Found.");
        }

        return retrievedListener;
    }

    //Used for new ListenerInfo class structure used beginning with API 14 (ICS)
    private View.OnTouchListener getOnTouchListenerV14(View view) {
        View.OnTouchListener retrievedListener = null;
        String viewStr = "android.view.View";
        String lInfoStr = "android.view.View$ListenerInfo";

        try {
            Field listenerField = Class.forName(viewStr).getDeclaredField("mListenerInfo");
            Object listenerInfo = null;

            if (listenerField != null) {
                listenerField.setAccessible(true);
                listenerInfo = listenerField.get(view);
            }

            Field touchListenerField = Class.forName(lInfoStr).getDeclaredField("mOnTouchListener");

            if (touchListenerField != null && listenerInfo != null) {
                retrievedListener = (View.OnTouchListener) touchListenerField.get(listenerInfo);
            }
        } catch (NoSuchFieldException ex) {
            Log.e("Reflection", "No Such Field.");
        } catch (IllegalAccessException ex) {
            Log.e("Reflection", "Illegal Access.");
        } catch (ClassNotFoundException ex) {
            Log.e("Reflection", "Class Not Found.");
        }

        return retrievedListener;
    }

    //Check activity result to load new file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OPEN_FILE_REQUEST:
                if (resultCode == RESULT_OK) {
                    String fileName = data.getStringExtra("fileName");
                    String shortFileName = data.getStringExtra("shortFileName");
                    Toast.makeText(this, "Selected File: " + fileName, Toast.LENGTH_SHORT).show();
                    //currentTextFile=shortFileName;
                    Cfg.currentFileFilename =fileName;
                    noteTitleText.setText(shortFileName);
                    openTextFileAndApplyRefs(fileName);
                    lockScrollView(false);
                    Log.d("PSL:openFile","File loaded.");
                } else {
                    //Do nothing!
                    Toast.makeText(this,"No File Selected"/*, Cancel Or Back Pressed"*/,Toast.LENGTH_SHORT).show();
                    Log.d("PSL:openFile","File not loaded.");
                }
                break;
        }

    }
}
