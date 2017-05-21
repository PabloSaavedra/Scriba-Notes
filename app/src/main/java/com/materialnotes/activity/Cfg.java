package com.materialnotes.activity;

import android.os.Environment;

/**
 * App settings class.
 *
 * Created by Pablo Saavedra on 12/05/2017.
 *
 */

public class Cfg {
    public static final String APP_DATA_FOLDER = Environment.getExternalStorageDirectory().getPath()+"/Scriba Notes";
    public static final String PROJECT_EXTENSION = ".snprj";

    public static float MAX_SCRIBA_VALUE = 100.0f; //Max Scriba return value at app startup

    public static final float INTERVAL_UNCLICKED = 0.75f; //Percent value
    public static final float INTERVAL_1 = 0.50f; //Percent value
    public static final float INTERVAL_2 = 0.25f; //Percent value

    /* DO NOT TOUCH *****************************************************************************/
    public static String currentProjectFilename = APP_DATA_FOLDER+"/"+"default"+PROJECT_EXTENSION;
    public static String currentFileFilename = "";

    public static final int COLOR_YELLOW = -256;
    public static final int COLOR_BLUE = -16711681;
    public static final int COLOR_GREEN = -16711936;
}
