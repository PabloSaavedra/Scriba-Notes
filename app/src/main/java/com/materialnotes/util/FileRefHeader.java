package com.materialnotes.util;

import com.materialnotes.activity.Cfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pablo Saavedra on 18/05/2017.
 */

public class FileRefHeader {
    private static final int COLOR_MEANING_SIZE = 30; //Length of pYellowHint, pBlueHint, pGreenHint
    private static final int EMPTY_SPACE = FileRef.REG_SIZE - 8 - (2+COLOR_MEANING_SIZE)*3; //REG_SIZE - date - color

    //Header values (The first register in the file will be a header with project settings)
    private long creationDate; //project creation date
    private String yellowMeaning;
    private String blueMeaning;
    private String greenMeaning;

    public FileRefHeader() {
        creationDate = new Date().getTime();
        yellowMeaning="";
        blueMeaning="";
        greenMeaning="";
    }

    /**
     * Write a header in fi
     * @param fi must be opened
     */
    public static void writeEmptyHeader(RandomAccessFile fi){
        FileRefHeader fh = new FileRefHeader();
        fh.write(fi);
    }

    public int write(RandomAccessFile fi){
        byte zero = 0;
        try {
            fi.seek(0);
            fi.writeLong(creationDate);
            fi.writeUTF(FileRef.fixedSizeString(yellowMeaning,COLOR_MEANING_SIZE));
            fi.writeUTF(FileRef.fixedSizeString(blueMeaning,COLOR_MEANING_SIZE));
            fi.writeUTF(FileRef.fixedSizeString(greenMeaning,COLOR_MEANING_SIZE));
            for (int i = 0; i < EMPTY_SPACE; i++) {
                fi.writeByte(zero);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public static void writeCurrentProjectHeader(FileRefHeader fh){
        try {
            RandomAccessFile fi = new RandomAccessFile(new File(Cfg.currentProjectFilename),"rw");
            fh.write(fi);
            fi.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads header from fi
     * @param fi must be opened
     */
    public FileRefHeader read(RandomAccessFile fi){
        FileRefHeader fh=null;
        try {
            fh = new FileRefHeader();
            fi.seek(0);
            fh.creationDate=fi.readLong();
            fh.yellowMeaning=fi.readUTF().trim();
            fh.blueMeaning=fi.readUTF().trim();
            fh.greenMeaning=fi.readUTF().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fh;
    }

    public static FileRefHeader getCurrentProjectHeader() {
        try {
            RandomAccessFile fi = new RandomAccessFile(new File(Cfg.currentProjectFilename),"r");
            FileRefHeader fh = new FileRefHeader();
            fh.read(fi);
            fi.close();
            return fh;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCurrentProjectYellowMeaning(){
        FileRefHeader fh = getCurrentProjectHeader();
        return fh.yellowMeaning;
    }

    public static String getCurrentProjectBlueMeaning(){
        FileRefHeader fh = getCurrentProjectHeader();
        return fh.yellowMeaning;
    }

    public static String getCurrentProjectGreenMeaning(){
        FileRefHeader fh = getCurrentProjectHeader();
        return fh.yellowMeaning;
    }

    public static void setCurrentProjectYellowMeaning(String value){
        FileRefHeader fh = getCurrentProjectHeader();
        fh.yellowMeaning=value;
        FileRefHeader.writeCurrentProjectHeader(fh);
    }

    public static void setCurrentProjectBlueMeaning(String value){
        FileRefHeader fh = getCurrentProjectHeader();
        fh.blueMeaning=value;
        FileRefHeader.writeCurrentProjectHeader(fh);

    }

    public static void setCurrentProjectGreenMeaning(String value){
        FileRefHeader fh = getCurrentProjectHeader();
        fh.greenMeaning=value;
        FileRefHeader.writeCurrentProjectHeader(fh);
    }

    public static String getCurrentProjectDate(){
        FileRefHeader fh = getCurrentProjectHeader();
        return textDate(fh.creationDate);
    }

    private static String textDate(long date) {
        Date tmpDate = new Date(date);
        String formatDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(tmpDate);
        return formatDate;
    }
}
