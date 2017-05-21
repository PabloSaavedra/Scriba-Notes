package com.materialnotes.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.materialnotes.activity.Cfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is a Brute Force solution to make the app work. In the future, all work made directly in the
 * FileRef must be done in memory (using an ArrayList<FileRef>), and save changes on application exit.
 * This will be a lot faster than now.
 *
 * Current id field values:
 * -1 = file is inside the project
 *  0 = deleted reference
 * >0 = normal id
 *
 * Created by Pablo Saavedra & Jesús Sanz on 06/04/2017.
 */


//TODO Change register 0, now we have to save a FileRefHeader

public class FileRef {

    //FileRef values
    public int id;
    public int color;
    public int style;
    public int start, end;
    public String filename;
    public long date;

    private static final int FILENAME_LENGTH = 255;
    public static final int REG_SIZE = 5 * 4 + 2 + FILENAME_LENGTH + 8; //=285+8=293 //4 SIZE_INT'S, 8 SIZE_LONG'S, 2 for the UTF encoding.
    //public static final String extension = ".snprj";
    //public static final String extension = ".ref";

    //Fixed References filename, in case of working with projects, every project must have its own refFile.
    //public static String currentProjectFilename = Cfg.APP_DATA_FOLDER+"/refs.ref";

    //Highlight styles available (at the moment the app will only use highlight)
    public static final int NO_STYLE = 0,
            HIGHLIGHT = 1,
            UNDERLINE = 2,
            BOLD = 3,
            ITALIC = 4;

    /**
     * Different constructor overloaded
     */
    //Empty constructor that create a empty object FileRef.
    public FileRef() throws IOException {
        this.id = 0;
        this.start = 0;
        this.end = 0;
        this.color = RGBtoInt(0, 0, 0);
        this.style = 0;
        this.filename = "";
        this.date = new Date().getTime();
    }

    /**
     * Constructor in which we save a reference with using the RGB colors.
     *
     * @param startText Start of the selection of the text.
     * @param endText   End of the selection of the text.
     * @param R         Int value that it's the RED value of a RGB color.
     * @param G         Int value that it's the GREEN value of a RGB color.
     * @param B         Int value that it's the BLUE value of a RGB color.
     * @param style     Kind o
     * @param filename  ¿¿¿¿¿¿¿¿¿¿We will erase the style method? Because when the Ref haves color its because the style it's HIGHLIGHT.??????????
     */
    public FileRef(int startText, int endText, int R, int G, int B, int style, String filename) throws IOException {
        if (startText!=endText){
            if (startText>endText){
                int aux = startText;
                startText=endText;
                endText=aux;
            }
            addValues(startText, endText, RGBtoInt(R, G, B), style, filename);
            saveWithMergeAdvanced();
        }
    }

    /**
     * Constructor in which we save a reference with using a int Color.
     *
     * @param color Int value that it's a color codified in a INT.
     *              ¿¿¿¿¿¿¿¿¿¿We will erase the style method? Because when the Ref haves color its because the style it's HIGHLIGHT.??????????
     */
    public FileRef(int startText, int endText, int color, int style, String filename) throws IOException {
        if (startText!=endText){
            if (startText>endText){
                int aux = startText;
                startText=endText;
                endText=aux;
            }
            addValues(startText, endText, color, style, filename);
            saveWithMergeAdvanced();
        }
    }

//    /**
//     * Constructor in which we save a reference without color, we use that when we don't highlight.
//     */
//
//    public FileRef(int startText, int endText, int style, String filename) throws IOException {
//        addValues(startText, endText, 0, style, filename);
//        //save();
//        //saveWithMerge();
//        saveWithMergeAdvanced();
//    }

    public static void setRefFilename(String name){
        Cfg.currentProjectFilename = Cfg.APP_DATA_FOLDER + "/" + name + Cfg.PROJECT_EXTENSION;
    }

    /**
     * Method that erase the RefFile FOR TESTING.
     *
     * @return true if file was successfully deleted, false if wasn't.
     */
    public static boolean deleteRefsFile() {
        File file = new File(Cfg.currentProjectFilename);
        try {
            if (file.exists()) {
                if (file.delete()) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteRefsFile(Context context) {
        File file = new File(Cfg.currentProjectFilename);
        try {
            if (file.exists()) {
                if (file.delete()) {
                    Toast.makeText(context, "refFile deleted", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Toast.makeText(context, "Error: Can't delete refFile", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(context, "refFile does not exists", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * We give values to the variables of the class with the parameters below.
     */
    private void addValues(int startText, int endText, int color, int style, String filename) {
        this.start = startText;
        this.end = endText;
        this.color = color;
        this.style = style;
        this.filename = filename;
        this.date = new Date().getTime();
    }

//    /**
//     * Method for save in the first hole if exist one in the other hand, that's create a new reg at the bottom of the file.
//     */
//    private void save() throws IOException {
//        File file = new File(currentProjectFilename);
//
//        RandomAccessFile fi = new RandomAccessFile(file, "rw");
//        //If the file it's empty, we write a empty REG.
//        if (fi.length() == 0) {
//            FileRef emptyFr = new FileRef();
//            emptyFr.id = -1;
//            fi.writeInt(emptyFr.id);
//            fi.writeInt(emptyFr.start);
//            fi.writeInt(emptyFr.end);
//            fi.writeInt(emptyFr.color);
//            fi.writeInt(emptyFr.style);
//            //we have to change filename length to a fixed size
//            emptyFr.filename = fixedSizeString(emptyFr.filename);
//
//            fi.writeUTF(emptyFr.filename);
//            fi.writeLong(emptyFr.date);
//        }
//
//        boolean gapFound = false;
//        long pos;
//        long fileRegs = (fi.length() - REG_SIZE) / REG_SIZE;
//
//        for (int i = 1; i <= fileRegs; i++) {
//            FileRef fr = new FileRef();
//            pos = i * REG_SIZE;
//
//            //TODO Check
//            if (pos > fi.length()) {
//                fi.seek(pos);
//                break;
//            }
//            fr.readFileData(fi);
//            if (fr.id == 0) {
//                fi.seek(pos);
//                this.id = i;
//                gapFound = true;
//                break;
//            }
//        }
//        if (!gapFound) {
//            fi.seek(fi.length());
//            this.id = (int) fileRegs + 1;
//        }
//
//        fi.writeInt(id);
//        fi.writeInt(start);
//        fi.writeInt(end);
//        fi.writeInt(color);
//        fi.writeInt(style);
//
//        //we have to change filename length to a fixed size
//        filename = fixedSizeString(filename);
//        fi.writeUTF(filename);
//        fi.writeLong(date);
//        fi.close();
//    }
//
//    /**
//     * Method for save in the first hole if exist one in the other hand, that's create a new reg at the bottom of the file.
//     */
//    private void saveWithMerge() throws IOException {
//        File file = new File(currentProjectFilename);
//        boolean saved = false;
//        int firstGap = -1;
//        FileRef fr;
//
//        RandomAccessFile fi = new RandomAccessFile(file, "rw");
//        //If the file it's empty, we write a empty REG.
//        if (fi.length() == 0) {
//            FileRef emptyFr = new FileRef();
//            emptyFr.id = -1;
//            fi.writeInt(emptyFr.id);
//            fi.writeInt(emptyFr.start);
//            fi.writeInt(emptyFr.end);
//            fi.writeInt(emptyFr.color);
//            fi.writeInt(emptyFr.style);
//            //we have to change filename length to a fixed size
//            emptyFr.filename = fixedSizeString(emptyFr.filename);
//
//            fi.writeUTF(emptyFr.filename);
//            fi.writeLong(emptyFr.date);
//        }
//
//        boolean gapFound = false;
//        long pos;
//        long numRegs = (fi.length() - REG_SIZE) / REG_SIZE;
//
//        //this is the new FOR
//        //we look if the new ref merges with another one
//        for (int i = 1; i <= numRegs; i++) {
//            fr = new FileRef();
//            pos = i * REG_SIZE;
////            if (pos > fi.length()) {
////                fi.seek(pos);
////                break;
////            }
//            fi.seek(pos);
//            fr.readFileData(fi);
//
//            if (firstGap == -1 && fr.id == 0) {
//                firstGap = i;
//                continue;
//            }
//
//            if (color == fr.color && fr.id != 0) {
//                Log.d("FileRef:SaveMove", "Coincide color y fr.id != 0");
//                if (start >= fr.start && end <= fr.end) {
//                    //Don't save: The new ref is inside another one
//                    saved=true;
//                    Log.d("FileRef:SaveMove", "Ref inside.");
//                    break;
//                } else if (start <= fr.start && end >= fr.end) {
//                    //The new ref "eats" the old one, so we update the values of the old one
//                    fr.start = start;
//                    fr.end = end;
//
//                    //Falta reescribir fr al disco
//                    fi.seek(pos);
//                    saveRefToFile(fi,fr);
//                    saved = true;
//                    Log.d("FileRef:SaveMove", "Ref eats.");
//                    break;
////                } else if (start <= fr.end+1 && start>=fr.start && end>fr.end) {
//                } else if (start<=fr.end && start>=fr.start && end>fr.end) {
//                    //The ref overlaps on the right
//                    fr.end = end;
//                    fi.seek(pos);
//                    saveRefToFile(fi,fr);
//                    saved=true;
//                    Log.d("FileRef:SaveMove", "Ref right.");
//                    break;
//                } else if (end>=fr.start && end<=fr.end && start<fr.start) {
//                    //The ref overlaps on the right
//                    fr.start = start;
//                    fi.seek(pos);
//                    saveRefToFile(fi,fr);
//                    saved=true;
//                    Log.d("FileRef:SaveMove", "Ref left.");
//                    break;
//                }
//
//                //Log.d("FileRef:SaveMove", "NewStart: " + start + " frStart: " + fr.start + "\n" +
//                //        "NewEnd: " + end + " frEnd: " + fr.end);
//            }
//        }
//
//        if (!saved && firstGap != -1) {
//            fi.seek(firstGap * REG_SIZE);
//            this.id = firstGap;
//            saveRefToFile(fi);
//            saved = true;
//        }
//
//        if (!saved) {
//            fi.seek(fi.length());
//            this.id = (int) (numRegs + 1);
//            saveRefToFile(fi);
//            saved = true;
//        }
//        fi.close();
//        logRegs();
//    }

    private void saveWithMergeAdvanced() throws IOException {
        File file = new File(Cfg.currentProjectFilename);
        boolean saved = false;
        int firstGap = -1;
        FileRef fr;
        boolean optimized = false;
        long pos;
        long numRegs;

        RandomAccessFile fi = new RandomAccessFile(file, "rw");
        //If the file it's empty, we write a empty REG.
        if (fi.length() == 0) {
            //Write empty ref at pos 0
            FileRef emptyFr = new FileRef();
            emptyFr.id = -1;
            fi.writeInt(emptyFr.id);
            fi.writeInt(emptyFr.start);
            fi.writeInt(emptyFr.end);
            fi.writeInt(emptyFr.color);
            fi.writeInt(emptyFr.style);
            //we have to change filename length to a fixed size
            emptyFr.filename = fixedSizeString(emptyFr.filename);
            fi.writeUTF(emptyFr.filename);
            fi.writeLong(emptyFr.date);
        }

        do{
            firstGap = -1;
            saved=false;
            optimized=false;
            numRegs = (fi.length() - REG_SIZE) / REG_SIZE;

            //we look if the new ref merges with another one
            for (int i = 1; i <= numRegs; i++) {
                //read i reference
                fr = new FileRef();
                pos = i * REG_SIZE;
                fi.seek(pos);
                fr.readFileData(fi);

                if (firstGap == -1 && fr.id == 0) {
                    firstGap = i;
                    continue;
                }

                if (color == fr.color && fr.id != 0 && filename.equals(fr.filename)) {
                    Log.d("FileRef:SaveMove", "Coincide color y fr.id != 0");
                    if (start >= fr.start && end <= fr.end) {
                        Log.d("FileRef:SaveMove", "Ref inside.");
                        saved=true;
                        break;
                    } else if (start <= fr.start && end >= fr.end) {
                        deleteId(fi,i);
                        optimized=true;
                        Log.d("FileRef:SaveMove", "Ref eats.");
                        break;
                    } else if (start<=fr.end && start>=fr.start && end>fr.end) {
                        start = fr.start;
                        deleteId(fi,i);
                        optimized=true;
                        Log.d("FileRef:SaveMove", "Ref right.");
                        break;
                    } else if (end>=fr.start && end<=fr.end && start<fr.start) {
                        end=fr.end;
                        deleteId(fi,i);
                        optimized=true;
                        Log.d("FileRef:SaveMove", "Ref left.");
                        break;
                    }
                }
            }
        }while (optimized);

        //if there is a gap we write the new reference in the gap
        if (!saved && firstGap != -1) {
            fi.seek(firstGap * REG_SIZE);
            this.id = firstGap;
            saveRefToFile(fi);
            saved = true;
        }

        //if still not saved, we write the new reference at the end of the file
        if (!saved) {
            fi.seek(fi.length());
            this.id = (int) (numRegs + 1);
            saveRefToFile(fi);
        }
        fi.close();
        logRegs();
    }

    static public void logRegs() {
        if (count() != -1) {
            for (int i = 1; i <= count(); i++) {
                Log.d("FileRef:Test", "<Inicio del REG " + i);
                try {
                    FileRef fr = new FileRef();
                    fr = readId(i);
                    //Date myDate = new Date(fr.date);
                    Log.d("FileRef:Test", "  ID      : " + fr.id);
                    Log.d("FileRef:Test", "  Comienzo: " + fr.start);
                    Log.d("FileRef:Test", "  Final   : " + fr.end);
                    Log.d("FileRef:Test", "  Color   : " + fr.color);
                    Log.d("FileRef:Test", "  Estilo  : " + fr.style);
                    Log.d("FileRef:Test", "  Filename: " + fr.filename);
                    Log.d("FileRef:Test", "  Fecha   : " + fr.getDate());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("FileRef:Test", "Fin del REG " + i + "/>");
            }
        }
    }

    /**
     * Method for read a specific reg passed for parameters.
     *
     * @param ID we use the ID to move to the correct position to read the reg.
     * @return A FileRef if all it's success, null if something it's wrong.
     */
    public static FileRef readId(int ID) throws IOException {
        File file = new File(Cfg.currentProjectFilename);
        RandomAccessFile fi = new RandomAccessFile(file, "rw");
        FileRef fr = new FileRef();

        //CONTROL: If the file it's empty OR the ID in the param it's lower than 0 OR the ID it's longer than the file, return null.
        if (fi.length() == 0 || ID < 1 || fi.length() < ID * REG_SIZE) {
            fi.close();
            return null;
        }

        //Read the REG attributes and show them in the LOG.
        fr.readFileData(fi, ID);
        fr.showRegInLog();
        fi.close();
        return fr;
    }

    /**
     * This method its for "delete" a REG, but instead of overwrite all the REG, we put the ID with 0 value, and
     * when we search for saving a new REG, this will be a "hole" and this will be overwrite.
     *
     * @param id Number of the ID that is going to be delete.
     * @return -1 if error, 0 if success
     */
    public static int deleteId(int id) {
        //Calculate the position of the REG that it's going to be deleted.
        int position = id * REG_SIZE;

        File file = new File(Cfg.currentProjectFilename);

        try {
            FileRef fr = new FileRef();
            RandomAccessFile fi = new RandomAccessFile(file, "rw");

            //We positioning in the REG that we want to read, and the read that.
            fr.readFileData(fi, id);

            //Change the ID to 0
            fr.id = 0;

            //We repositioning for "modify" the values of the read reg.
            fi.seek(position);

            fi.writeInt(fr.id);
            fi.writeInt(fr.start);
            fi.writeInt(fr.end);
            fi.writeInt(fr.color);
            fi.writeInt(fr.style);
            fr.filename = fixedSizeString(fr.filename);
            fi.writeUTF(fr.filename);
            fi.writeLong(fr.date);
            fi.close();

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Deletes a reference in an open file
     * @param fi Must be opened
     * @param id Reference position to delete
     * @return -1 if error, 0 otherwise
     */
    public int deleteId(RandomAccessFile fi, int id){
        try {
            //search position
            fi.seek(id * REG_SIZE);
            //write a 0 on the Id field
            fi.writeInt(0);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Method for MODIFY values in a existing REFERENCE in the file.
     *
     * @param id ID of the REG that will be modify.
     * @return -1 if error, 0 if success
     * WARNING: If you not want to modify a value, put -1 in the number params, and if it's the String put null.
     */
    public static int modifyRef(int id, int startText, int endText, int color, int style, String filename) {
        //Calculate the position of the REG that it's going to be deleted.
        int position = id * REG_SIZE;

        File file = new File(Cfg.currentProjectFilename);

        try {
            FileRef fr = new FileRef();
            RandomAccessFile fi = new RandomAccessFile(file, "rw");

            //We positioning in the REG that we want to read, and the read that.
            fr.readFileData(fi, id);
            int aux;

            if (startText != -1) {
                aux = (int) fr.start;
                fr.start = startText;
                Log.d("FileRef:Modify", "Initial start value: " + aux + " , Modifyed: " + fr.start);
            }

            if (endText != -1) {
                aux = (int) fr.end;
                fr.end = endText;
                Log.d("FileRef:Modify", "Initial end value: " + aux + " , Modifyed: " + fr.end);
            }

            if (color != -1) {
                aux = (int) fr.color;
                fr.color = color;

                Log.d("FileRef:Modify", "Initial color value: " + aux + " , Modifyed: " + fr.color);
            }

            if (style != -1) {
                aux = (int) fr.style;
                Log.d("FileRef:Modify", "Initial style value: " + aux + " , Modifyed: " + fr.style);
                fr.style = style;
            }

            if (filename != null) {
                String auxS = fr.filename;
                Log.d("FileRef:Modify", "Initial filename value: " + auxS + " , Modifyed: " + fr.filename);
                fr.filename = filename;
            }

            fi.seek(position);

            fi.writeInt(fr.id);
            fi.writeInt(fr.start);
            fi.writeInt(fr.end);
            fi.writeInt(fr.color);
            fi.writeInt(fr.style);
            fr.filename = fixedSizeString(fr.filename);
            fi.writeUTF(fr.filename);
            fi.writeLong(fr.date);

            fi.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Method for reading reg in current pointer position
     *
     * @param fi must be opened
     * @return -1 if error, 0 if success
     * WARNING: Before the call for this method, you need to repositioned the pointer.
     */
    private int readFileData(RandomAccessFile fi) {
        try {
            id = fi.readInt();
            start = fi.readInt();
            end = fi.readInt();
            color = fi.readInt();
            style = fi.readInt();
            filename = fi.readUTF().trim();
            date = fi.readLong();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Method for reading reg of the id that you pass in parameters.
     *
     * @param fi must be opened
     * @return -1 if error, 0 if success
     */
    private int readFileData(RandomAccessFile fi, int idSearch) {

        int position = idSearch * REG_SIZE;

        try {
            fi.seek(position);
            id = fi.readInt();
            start = fi.readInt();
            end = fi.readInt();
            color = fi.readInt();
            style = fi.readInt();
            filename = fi.readUTF().trim();
            date = fi.readLong();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     *
     * @param fi Source data file. Must be opened
     * @param fr Destiny object to store data
     * @return -1 if error, 0 otherwise.
     */

    private static int readRefFromFile(RandomAccessFile fi, FileRef fr){
        try {
            fr.id = fi.readInt();
            fr.start = fi.readInt();
            fr.end = fi.readInt();
            fr.color = fi.readInt();
            fr.style = fi.readInt();
            fr.filename = fi.readUTF().trim();
            fr.date = fi.readLong();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Saves a reference to a file
     * @param fi File to write no. Must be opened.
     * @param fr FileRef to save
     * @return -1 if error, 0 otherwise.
     */
    private int saveRefToFile(RandomAccessFile fi, FileRef fr) {
        try {
            fi.writeInt(fr.id);
            fi.writeInt(fr.start);
            fi.writeInt(fr.end);
            fi.writeInt(fr.color);
            fi.writeInt(fr.style);
            //we have to change filename length to a fixed size
            fr.filename = fixedSizeString(fr.filename);
            fi.writeUTF(fr.filename);
            fi.writeLong(fr.date);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Saves a reference to a file
     * @param fi File to write no. Must be opened.
     * @return -1 if error, 0 otherwise.
     */
    private int saveRefToFile(RandomAccessFile fi) {
        try {
            fi.writeInt(id);
            fi.writeInt(start);
            fi.writeInt(end);
            fi.writeInt(color);
            fi.writeInt(style);
            //we have to change filename length to a fixed size
            filename = fixedSizeString(filename);
            fi.writeUTF(filename);
            fi.writeLong(date);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * @param arrayRefs in each iteraction we'll write one reference, in a new file.
     * @return 0 if somethings it's wrong, 1 if all it's fine.
     */
    public static String exportRefs(ArrayList<FileRef> arrayRefs) throws IOException {
        String exported = "";
        String noteNumber;
        String fileName;
        String fecha;
        String header;
        String contentNote = "";
        String table ="";

        Log.d("FileRef:Expor", "Estamos en exportRefs");
        try {

            Log.d("FileRef:Expor", "Entramos en el try de exportRefs");

            File fileExport = new File(Environment.getExternalStorageDirectory().getPath() + "/Scriba Notes/files/fileEx.txt");
            Log.d("FileRef:ExportContent", "File Exported created.");
            PrintWriter writerFile = new PrintWriter(fileExport, "UTF-8");

            int count = 1;
            for (FileRef fr : arrayRefs) {
                Log.d("FileRef:Expor", "Entramos en el for each que recorre los files del array.");
                noteNumber = "Note " + String.format("%03d", count) + " ";
                fileName = FilenameUtils.getShortFilename(fr.filename);
                fecha = textDate(fr.date);

                header = noteNumber + fileName + fecha;

                contentNote = getContentNote(fr);
                Log.d("FileRef:Expor", "Llamamos al metodo para conseguir el content de la note: " + contentNote);

                /**Now we aren't using the formating. */
//                String formatedNote = getFormatedContent(contentNote, header.length());/***/
//
//                Log.d("FileRef:ExportG", "Llamamos al metodo para formatear el content de la note: " + formatedNote);

                writerFile.println(header);
                //writerFile.print(lineFormat(header.length(), "_"));
                writerFile.println(contentNote);
                exported = exported + "\n" + "  <p><b>" + header + "</p></b>" + "\n" + /*lineFormat(header.length(), "_") + */ "  <p>" + contentNote + "</p>";
                count++;

                String color = "";
                if (fr.id > 0) {
                    if (fr.color == Cfg.COLOR_YELLOW) {
                        color = "      <tr>\n" +
                                "            <td bgcolor=\"ffff00\">" +
                                "<div style=\"height:6px\"></div></td>\n" +
                                "      </tr>\n";
                    } else if (fr.color == Cfg.COLOR_BLUE) {
                        color = "      <tr>\n" +
                                "            <td bgcolor=\"00ffff\"><div style=\"height:6px\"></div></td>\n" +
                                "      </tr>\n";
                    } else if (fr.color == Cfg.COLOR_GREEN) {
                        color = "      <tr>\n" +
                                "            <td bgcolor=\"00ff00\"><div style=\"height:6px\"></div></td>\n" +
                                "      </tr>\n";
                    }

                    table = table +
                            "<table width=\"100%\" border=1 cellspacing=0 cellpadding=2>\n"
                            + color +
                            "      <tr>\n" +
                            "      <td>\n" +
                            "        <table cellspacing=0 cellpadding=2  width=\"100%\">\n" +
                            "          <tr >\n" +
                            "           <td><p style=\"text-align:left\"><b>" + noteNumber + "</b></p></td>" +
                            "           <td><p style=\"text-align:center\"><b>" + fileName + "</b></p></td>" +
                            "           <td><p style=\"text-align:right\"><b>" + fecha + "</b></p></td>" +
                            "          </tr>\n" +
                            "        </table>\n" +
                            "        </td>\n" +
                            "      </tr>\n" +
                            "      <tr>\n" +
                            "       <td>\n" +
                            "        <p>" + contentNote + "</p>\n" +
                            "       </td>\n" +
                            "      </tr>\n" +
                            "    </table>\n<p></p>";
                }
            }

            String projectName = FilenameUtils.getShortFilenameWithoutExtension(Cfg.currentProjectFilename);
            String beginningHTML = "<!DOCTYPE html>\n <html>\n <h1>SCRIBA NOTES</h1><h3>Project: "+projectName+"</h3> <body>", endHTML = "  </body>\n </html>\n";
            exported = beginningHTML + table + "  </body>\n </html>\n";
            //Log.d("html", "HTML:" +exported);
            writerFile.close();
            return exported + "\n";
        } catch (FileNotFoundException e) {
            Log.d("FileRef:Error", "File not found: \n");
            e.printStackTrace();
            return "ERROR";
        }
    }



    /**
     * @param filename Path of a file
     * @return name of the file with extension.
     */
//    private static String getNameFromFilename(String filename) {
//        String aux = filename.substring(filename.lastIndexOf("/") + 1, filename.length() - 1);
//        return aux;
//    }

    /**
     * @return the number of REG's of the file that has the reference.
     */
    public static int count() {
        int count = 0;
        File file = new File(Cfg.currentProjectFilename);
        if (file.exists()) {
            if (file.length() <= REG_SIZE) {
                return 0;
            } else {
                count = (int) (file.length() / REG_SIZE);
                return count - 1;
            }
        } else {
            return -1;
        }
    }

    /**
     * @param fr a FileRef, to know the filename, start, and end. MAYBE we can change this param for 3 params.
     * @return a String with the content of the note.
     */
    private static String getContentNote(FileRef fr) {
        String content = "";
        String myFilename = fr.filename;
        myFilename = myFilename.trim();

        File temp = new File(myFilename);
        RandomAccessFile randomFile;

        try {
            randomFile = new RandomAccessFile(temp, "r");/***Something happens when I trim the filename.*/
            byte[] buffer = new byte[fr.end - fr.start + 1];
            randomFile.seek(fr.start);
            randomFile.read(buffer);
            content = new String(buffer);
            //content = content + "\n";/***/
        } catch (FileNotFoundException e) {
            Log.d("FileRef:ExportERROR", "File not found.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("FileRef:ExportERROR", "IOExcepion");
            e.printStackTrace();
        }
        //Log.d("FileRef:Export", "Return content: " + content);
        return content;
    }

    /**
     * @param contentNote Text of the note that we want to save in the exportFile
     * @return A formated Text that we'll save in the exportFile.
     */
    private static String getFormatedContent(String contentNote, int length) {
        Log.d("FileRef:Expor", "Estamos en getFormatedContent");
        final int MAX_CHARACTERS_CONTENT = length;
        String formatedContent = " ";
        String line = "";
        int endCut;
        int start = 0;
        do {
            line = contentNote.substring(start, MAX_CHARACTERS_CONTENT - 1);

            if (contentNote.charAt(MAX_CHARACTERS_CONTENT + 1) == ' ') {
                Log.d("FileRef:Expor", "Estamos en el if");

                //Itsn't necessaty to format the first
//                formatedContent = formatedContent + line;
//                contentNote = contentNote.substring(MAX_CHARACTERS_CONTENT, contentNote.length());
            } else {
                Log.d("FileRef:Expor", "Estamos en el else");
            }

            endCut = line.lastIndexOf(" ");
            //Log.d("FileRef:Expor", "Linea de LENGTH    carácteres: " + line);
            formatedContent = formatedContent + line.substring(0, endCut) + "\n";
            //Log.d("FileRef:Export", "Texto formateado: " + formatedContent);
            Log.d("FileRef:Export", "Content original: " + contentNote);
            contentNote = contentNote.substring(endCut, contentNote.length());
            Log.d("FileRef:Export", "Content cortado: " + contentNote);

            //start = endCut + 1;

            if (contentNote.length() <= MAX_CHARACTERS_CONTENT) {
                formatedContent = formatedContent + contentNote + "\n";
                Log.d("FileRef:ExportContent", "Finalizado del formateo: " + formatedContent);
            }
        } while (contentNote.length() >= MAX_CHARACTERS_CONTENT);
        return formatedContent;
    }

    /**
     * UtilMethod: for debug in the LOG the values in a REG.
     */
    public void showRegInLog() {
        Log.d("FileRef:Log", "id: " + id + ", start: " + start + ", end: " + end + ", color: " + color +
                ", style: " + style + ", filename: " + filename + ", date: " + textDate(date));
    }

    public static void showAllRefsInLog() {
        for (int i = 1; i <= count(); i++) {
            try {
                FileRef fr = new FileRef();
                fr = readId(i);
                Log.d("FileRef:AllRef", "id: " + fr.id + ", start: " + fr.start + ", end: " + fr.end + ", color: " + fr.color +
                        ", style: " + fr.style +
                        ", filename: " + FilenameUtils.getShortFilenameWithoutExtension(fr.filename) +
                        ", date: " + fr.getDate());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("FileRef:AllRef", "--------------------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * UtilMethod:
     *
     * @param size the number of '-' that we need
     * @return a String with '-' as the number it's.
     */
    private static String lineFormat(int size, String simbol) {
        String line = "";
        for (int i = 0; i <= size + 1; i++) {
            line = line + simbol;
        }
        line = line + "\n";
        return line;
    }

    /**
     * UtilMethod: For Coding an only one int value that it's the result of the coding of the other 3 values.
     *
     * @param R
     * @param G
     * @param B
     * @return value that it's the result of the coding of the other 3 values.
     */
    public static int RGBtoInt(int R, int G, int B) {
        //We have 3 ints, that we convert in to a int that its a color.
        int color = (255 & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
        return color;
    }

    /**
     * UtilMethod: For concat to a String for have a FIX SIZE in the Strings.
     *
     * @param longitud Rest size for the 255 size.
     * @return A string with WhiteSpaces for complete the 255 size.
     */
    private static String stringWhiteSpaces(int longitud) {
        char[] espacios = new char[longitud];
        for (int i = 0; i < longitud; i++) espacios[i] = ' ';
        return String.valueOf(espacios);
    }

    private static String fixedSizeString(String str) {
        String tempStr = str;
        if (str.length() > FILENAME_LENGTH) {
            tempStr = tempStr.substring(0, FILENAME_LENGTH);
        } else {
            tempStr = tempStr + stringWhiteSpaces(FILENAME_LENGTH - tempStr.length());
        }
        return tempStr;
    }

    protected static String fixedSizeString(String str, int size) {
        String tempStr = str;
        if (str.length() > size) {
            tempStr = tempStr.substring(0, size);
        } else {
            tempStr = tempStr + stringWhiteSpaces(size - tempStr.length());
        }
        return tempStr;
    }

    private static String textDate(long date) {
        Date tmpDate = new Date(date);

        String formatDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(tmpDate);
        return formatDate;//tmpDate.toString();
    }

    public String getDate(){
        String formatDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
        return formatDate;
    }

    //TODO Filename filtering!
    public static void eraseRef(int start, int end) throws IOException {
        if (start>end){
            int aux = start;
            start=end;
            end=aux;
        }

        Log.d("FileRef:Erase", "TOTAL REGS: "+count());
        //Loop that read and load each reference atributte in fr.
        for (int i = 1; i <= count(); i++){
            //We read the values of the fr in this iteraction.
            FileRef fr = readId(i);
            //showRefLog(fr, "Erase");
            Log.d("FileRef:Erase", "Inicio = "+start+", Fin = "+end);

            if (fr.id!=0) {
                if (start > fr.end || end < fr.start) {
                    Log.d("FileRef:Erase", "OUT of REG");
                } else if (start <= fr.start && end >= fr.end) {
                    Log.d("FileRef:Erase", "Erase by EATS");
                    deleteId(i);
                } else if (start >= fr.start && start < fr.end && end >= fr.end) {
                    Log.d("FileRef:Erase", "Erase by the RIGTH");
                    modifyRef(i, -1, start, -1, -1, null);
                } else if (end > fr.start && end <= fr.end && start <= fr.start) {
                    Log.d("FileRef:Erase", "Erase by the LEFT");
                    modifyRef(i, end, -1, -1, -1, null);
                } else if (start > fr.start && end < fr.end) {
                    Log.d("FileRef:Erase", "Erase by the HALF");
                    modifyRef(i, -1, start, -1, -1, null);
                    new FileRef(end, fr.end, fr.color, fr.style, fr.filename);
                }
            }
        }
    }

    /**
     *
     * @param start
     * @param end
     * @param colorToDelete
     * @return Returns an ArrayList of affected references in a different color to colorToDelete
     * @throws IOException
     */
    public static ArrayList<Integer> eraseRef(int start, int end, int colorToDelete, String filename) throws IOException {
        ArrayList<Integer> affectedId = new ArrayList<Integer>();

        if (start>end){
            int aux = start;
            start=end;
            end=aux;
        }

        //Log.d("FileRef:Erase", "TOTAL REGS: "+count());
        //Loop that read and load each reference atributte in fr.
        for (int i = 1; i <= count(); i++){
            //We read the values of the fr in this iteraction.
            FileRef fr = readId(i);
            //showRefLog(fr, "Erase");
            //Log.d("FileRef:Erase", "Inicio = "+start+", Fin = "+end);
            if (fr.id!=0 && fr.filename.equals(filename)) {
                if (fr.color == colorToDelete) {
                    if (start > fr.end || end < fr.start) {
                        Log.d("FileRef:Erase", "OUT of REG");
                    } else if (start <= fr.start && end >= fr.end) {
                        Log.d("FileRef:Erase", "Erase by EATS");
                        deleteId(i);
                    } else if (start >= fr.start && start < fr.end && end >= fr.end) {
                        Log.d("FileRef:Erase", "Erase by the RIGTH");
                        //fr.end = start;
                        modifyRef(i, -1, start, -1, -1, null);
                    } else if (end > fr.start && end <= fr.end && start <= fr.start) {
                        Log.d("FileRef:Erase", "Erase by the LEFT");
                        //fr.start = end;
                        modifyRef(i, end, -1, -1, -1, null);
                    } else if (start > fr.start && end < fr.end) {
                        Log.d("FileRef:Erase", "Erase by the HALF");
                        //int myEnd = fr.end;
                        modifyRef(i, -1, start, -1, -1, null);
                        new FileRef(end, fr.end, fr.color, fr.style, fr.filename);

                        //fr.end = start;
                    }
                } else {
                    //if the reference has a different color, we add it to the ArrayList if it is affected
                    if (start <= fr.end && end >= fr.start) {
                        if (!affectedId.contains(fr.id))
                            affectedId.add(fr.id);
                    }
                }
            }
        }
        return affectedId;
    }

    private static void showRefLog(FileRef fr, String filter) {
        Log.d("FileRef:"+filter, "--ID:"+fr.id+"--");
        Log.d("FileRef:"+filter, "  Start:"+fr.start);
        Log.d("FileRef:"+filter, "  End:"+fr.end);
//        Log.d("FileRef:"+filter, "    Color:"+fr.color);
//        Log.d("FileRef:"+filter, "    Style:"+fr.style);
//        Log.d("FileRef:"+filter, "    Filename:"+fr.filename);
//        Log.d("FileRef:"+filter, "    Date:"+fr.date);
    }

    /**
     * Method to kwow the previous color of the actual position.
     * @param index position for start searching the color
     * @return int of a color, -2147483648 if there isn't previous color (Integer.MIN_VALUE)
     */
    static public int getLastColor(int index, String currentFile) throws IOException {
        int  lastDiference = index, lastColor = Integer.MIN_VALUE;
        int numRefs = count();
        if (numRefs<1)
            return lastColor;

        File file = new File(Cfg.currentProjectFilename);
        RandomAccessFile fi = new RandomAccessFile(file, "r");
        fi.seek(REG_SIZE);

        for (int i = 1; i <= numRefs; i++){
            FileRef fr = new FileRef();
            readRefFromFile(fi, fr);
            if (fr.id!=0 && fr.filename.equals(currentFile)) {
                //We only want to know the PREVIOUS color, in this case the fr.start always will be smaller than the actual position.
                if (index > fr.start) {
                    if (/*index >= fr.start && */ index <= fr.end) {
                        //Actual position inside of the REG
                        lastColor = fr.color;
                        break;
                    } else {
                        //Actual position outside of a the REG

                        if (index - fr.end < lastDiference) {
                        /*In each iteraction we save the color of the REG
                        if the diference between index - fr.end it's lower
                        than the previous diference*/

                            lastDiference = index - fr.end;
                            lastColor = fr.color;
                        }
                    }
                }
            }
        }
        //Log.d("FileRef:LastColor", "Sended color: "+lastColor);
        fi.close();
        return lastColor;
    }

    public static ArrayList<String> getCurrentProjectFiles(){
        FileRef fr = null;
        ArrayList<String> filenames = new ArrayList<String>();
        int numRegs = count();
        for (int i = 1; i <= numRegs; i++){
            try {
                fr = new FileRef();
                fr = readId(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fr.id==-1) {
                if (!filenames.contains(fr.filename)){
                    filenames.add(FilenameUtils.getPathWithoutVirtualRoute(fr.filename));
                    Log.d("PSL:ProjectFiles","File: "+fr.filename);
                }
            }
        }
        return filenames;
    }

    /**
     * Adds a file to a project
     * @param filename name of the file to be added
     * @return 0 = success; -1 = error;
     * @throws IOException
     */
    public static int addNewFileToProject(String filename) throws IOException {
        FileRef fr;
        try {
            int numRegs = count();
            for (int i = 1; i <= numRegs; i++) {
                fr = new FileRef();
                fr = readId(i);
                if (fr.id == -1 && filename.equalsIgnoreCase(fr.filename))
                    return 0; //the file was already saved
            }

            //save ref at the end of the current FileRef
            fr = new FileRef();
            fr.id = -1;
            fr.filename = filename;

            File file = new File(Cfg.currentProjectFilename);
            RandomAccessFile fi = new RandomAccessFile(file, "rw");
            if (fi.length() == 0) {
                //Write empty ref at pos 0
                FileRef emptyFr = new FileRef();
                emptyFr.id = -1;
                fi.writeInt(emptyFr.id);
                fi.writeInt(emptyFr.start);
                fi.writeInt(emptyFr.end);
                fi.writeInt(emptyFr.color);
                fi.writeInt(emptyFr.style);
                //we have to change filename length to a fixed size
                emptyFr.filename = fixedSizeString(emptyFr.filename);
                fi.writeUTF(emptyFr.filename);
                fi.writeLong(emptyFr.date);
            }

            fi.seek(fi.length());

            fi.writeInt(fr.id);
            fi.writeInt(fr.start);
            fi.writeInt(fr.end);
            fi.writeInt(fr.color);
            fi.writeInt(fr.style);
            fr.filename = fixedSizeString(fr.filename);
            fi.writeUTF(fr.filename);
            fi.writeLong(fr.date);

            fi.close();
        } catch (Exception e){
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    public static void removeFileFromCurrentProject(String s) {
        for (int i = 1; i <= count(); i++) {
            try {
                FileRef fr = new FileRef();
                fr = readId(i);
                if (fr.filename.equalsIgnoreCase(s)){
                    deleteId(i);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

