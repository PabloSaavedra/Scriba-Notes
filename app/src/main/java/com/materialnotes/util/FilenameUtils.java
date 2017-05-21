package com.materialnotes.util;

import android.os.Environment;
import android.util.Log;

/**
 * Created by Pablo Saavedra on 12/05/2017.
 */

public class FilenameUtils {
    /**
     *
     * @param filename full path filename
     * @return extension of current filename, including the dot
     */
    public static String getExtension(String filename){
        String ext = "";
        int lastDotPos = filename.lastIndexOf(".");
        int lastBarPos = filename.lastIndexOf("/");
        if (lastBarPos>lastDotPos)
            return "";
        ext = filename.substring(lastDotPos);
        return ext;
    }

    public static String getShortFilename (String longFilename){
        if (longFilename.charAt(longFilename.length()-1)=='/')
            return ""; //is a directory
        return longFilename.substring(longFilename.lastIndexOf("/")+1);
    }

    public static String getShortFilenameWithoutExtension(String longFilename){
        String s ="";
        int dotPos;
        if (longFilename.charAt(longFilename.length()-1)=='/')
            return ""; //is a directory
        s = longFilename.substring(longFilename.lastIndexOf("/")+1);
        dotPos = s.lastIndexOf(".");
        if (dotPos>=0)
            return s.substring(0,dotPos);
        else
            return s;
    }

    public static String getPathWithoutVirtualRoute(String filename){
        String externalPath = Environment.getExternalStorageDirectory().getPath();
        String myPath = filename.substring(0,externalPath.length());
        if (externalPath.equalsIgnoreCase(myPath)){
            return filename.substring(myPath.length()+1);
        } else
            return filename;
    }
}
