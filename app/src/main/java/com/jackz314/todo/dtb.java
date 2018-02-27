package com.jackz314.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Canvas;
import android.graphics.Color;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhang on 2017/6/15.
 */

public class dtb extends SQLiteOpenHelper{
    public static String DATABASE_NAME = "todo.db";
    public static String TODO_TABLE = "todolist_table";
    public static String HISTORY_TABLE = "deleted_notes_table";
    public static String SAVED_FOR_LATER_TABLE = "saved_for_later";
    public static String TAGS_TABLE = "tags_table";
    public static String ID = "_id";
    //public static String UNIQUE_ID = "special_id";
    public static String TITLE = "title";
    public static String TAG = "tag";
    public static String TAG_COLOR = "color";
    public static String CONTENT = "content";
    public static String IMPORTANCE = "importance";
    public static String PINNED = "pinned";
    public static String PINNED_TIMESTAMP = "pinned_timestamp";
    public static String CREATED_TIMESTAMP = "created_timestamp";
    public static String DELETED_TIMESTAMP = "deleted_timestamp";
    public static String SAVED_FOR_LATER_TIMESTAMP = "saved_for_later_timestamp";
    public static String SAVED_TIME = "saved_time";



    public dtb(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();//remove this line after debug!
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ TODO_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + "" + CONTENT + " TEXT," + PINNED + " BOOLEAN," + PINNED_TIMESTAMP + " DATETIME, " + CREATED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        db.execSQL("create table "+ HISTORY_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + CONTENT + " TEXT," + PINNED + " BOOLEAN," + PINNED_TIMESTAMP + " DATETIME, " + DELETED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        //db.execSQL("create table "+ SAVED_FOR_LATER_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + CONTENT + " TEXT," + IMPORTANCE + " INTEGER," + SAVED_FOR_LATER_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        db.execSQL("create table "+ TAGS_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TAG + " TEXT," + TAG_COLOR + " TEXT," + CREATED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TODO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+HISTORY_TABLE);
        //db.execSQL("DROP TABLE IF EXISTS "+SAVED_FOR_LATER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+TAGS_TABLE);
        onCreate(db);
    }
    /*
    public boolean insertData(String title){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TITLE,title);
        long result = db.insert(TODO_TABLE,null,cv);
        return (result != -1);
    }*/

    public boolean insertDataToHistory(String title){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TITLE,title);
        long result = db.insert(HISTORY_TABLE,null,cv);
        return (result != -1);
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,* FROM " + TODO_TABLE ,null);
        return cs;
    }

    public  Cursor getDataDesc(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,* FROM " + TODO_TABLE + " order by _id desc",null);
        return cs;
    }

    public Cursor getSearchResults(String text){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.query(false,TODO_TABLE, new String[]{ID,TITLE},TITLE + " LIKE ?",new String[]{"%"+ text+ "%" },null,null,"_id desc",null );
        return cs;
    }

    public Cursor getHistorySearchResults(String text){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.query(false,HISTORY_TABLE, new String[]{ID,TITLE},TITLE + " LIKE ?",new String[]{"%"+ text+ "%" },null,null,"_id desc",null );
        return cs;
    }

    public void wipeHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(HISTORY_TABLE,null,null);
    }

    public void wipeTodoList(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TODO_TABLE,null,null);
    }

    public Cursor getHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,datetime(deleted_timestamp,'localtime'),* FROM " + HISTORY_TABLE,null);
        return cs;
    }

    public  Cursor getHistoryDesc(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,datetime(deleted_timestamp,'localtime'),* FROM " + HISTORY_TABLE + " order by _id desc",null);
        return cs;
    }

    public Integer finishData(Long id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
        ContentValues cv = new ContentValues();
        String data;
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
            cv.put(TITLE,data);
        }
        db.insert(HISTORY_TABLE,null,cv);
        Integer del = db.delete(TODO_TABLE,ID + " = ?",new String[] {Long.toString(id)});
        cs.close();
        return del;
    }

    public Integer deleteFromHistory(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Integer del = db.delete(HISTORY_TABLE,ID + " = ?",new String[] {id});
        return del;
    }

    public void deleteNote(Long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TODO_TABLE,ID + " = ?",new String[] {Long.toString(id)});
    }

    public void insertDataForSpecialMsgAction(String data){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TITLE,data);
        db.insert(TODO_TABLE,null,cv);
    }

    public void restoreDataHToM(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,* FROM "+ HISTORY_TABLE + " WHERE "+ ID + " = " + id, null);
        ContentValues cv = new ContentValues();
        String data;
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
            cv.put(TITLE,data);
        }
        db.insert(TODO_TABLE,null,cv);
        db.delete(HISTORY_TABLE,ID + " = ?",new String[] {id});
        cs.close();
    }

    public String getOneDataInTODO(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
        String data="";
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
        }
        cs.close();
        return data;
    }

    public String mergeBackup(String fileLocation) {
        SQLiteDatabase currentDatabase = null;
        boolean attached = false;;
        try {
            SQLiteDatabase backupDatabase = SQLiteDatabase.openDatabase(fileLocation, null, SQLiteDatabase.OPEN_READWRITE);
            currentDatabase = this.getWritableDatabase();
            File curPath = new File(currentDatabase.getPath());
            File bakPath = new File(backupDatabase.getPath());
            String backupPath = bakPath.getAbsolutePath();
            String currentName = curPath.getName().substring(0,curPath.getName().length()-3);
            String bakName = bakPath.getName().substring(0,bakPath.getName().length()-3);
            if(currentDatabase.inTransaction()){
                //currentDatabase.endTransaction();
            }
            currentDatabase.execSQL("ATTACH DATABASE '" + backupPath + "' AS backupDb");
            //System.out.println(currentDatabase.getAttachedDbs().toString());
            attached = true;
            currentDatabase.execSQL("INSERT INTO " + TODO_TABLE + " (" + TITLE + ", " + CONTENT + ", " + IMPORTANCE + ") SELECT " + TITLE + ", " + CONTENT + ", " + IMPORTANCE + " FROM " + "backupDb" + "." + TODO_TABLE);
            Cursor cursor = backupDatabase.rawQuery("SELECT name FROM " + "" + "sqlite_master WHERE type = 'table'", null);
            String combinedString = "";
            while (cursor.moveToNext()){
                combinedString += cursor.getString(cursor.getColumnIndex("name"));
            }
            if (combinedString.contains(HISTORY_TABLE)) {
                currentDatabase.execSQL("INSERT INTO " + HISTORY_TABLE + "(" + TITLE + "," + CONTENT + "," + IMPORTANCE + ") SELECT " + TITLE + "," + CONTENT + "," + IMPORTANCE + " FROM " + "backupDb" + "." + HISTORY_TABLE);
            }
            if (combinedString.contains(SAVED_FOR_LATER_TABLE)) {
                currentDatabase.execSQL("INSERT INTO " + SAVED_FOR_LATER_TABLE + "(" + TITLE + "," + CONTENT + "," + IMPORTANCE + ") SELECT " + TITLE + "," + CONTENT + "," + IMPORTANCE + " FROM " + "backupDb" + "." + SAVED_FOR_LATER_TABLE);
            }
            cursor.close();
            currentDatabase.execSQL("DETACH backupDb");
        } catch (Exception e) {
            e.printStackTrace();
            if(attached){
                currentDatabase.execSQL("DETACH backupDb");
            }
            //System.out.println(e.getLocalizedMessage());
            return e.getLocalizedMessage();
        }
        return null;
    }

    public boolean validateBackup(String fileLocation){
        SQLiteDatabase validateDatabase;
        try{
            File dbfile = new File(fileLocation);
            if(dbfile.exists()){
                validateDatabase = SQLiteDatabase.openDatabase(dbfile.getAbsolutePath(),null,SQLiteDatabase.OPEN_READWRITE);
                Cursor validateCursor = validateDatabase.rawQuery("SELECT name FROM " + "sqlite_master WHERE type = 'table'", null);
                String combinedString = "";
                while (validateCursor.moveToNext()){
                    combinedString += validateCursor.getString(validateCursor.getColumnIndex("name"));
                }
                //System.out.println(combinedString);
                if(combinedString.contains(TODO_TABLE)){
                    Cursor validateColumns = validateDatabase.rawQuery("SELECT * FROM " + TODO_TABLE + " ",null);
                    StringBuilder colBuilder = new StringBuilder();
                    combinedString = "";
                    for(String each : validateColumns.getColumnNames()){
                        colBuilder.append(",").append(each);
                    }
                    combinedString = colBuilder.deleteCharAt(0).toString();
                    //System.out.println(combinedString);
                    if(combinedString.contains(TITLE)){
                        return true;
                    }
                }
                /*//compare validate one with the current one with ".contains()"
                Cursor currentCusor = currentDatabase.rawQuery("SELECT name FROM " + currentDbName + ".sqlite_master WHERE type='table'", null);
                if(currentCursor.getColumnCount() == validateCursor.getColumnCount()||currentCursor.getColumnCount() > validateCursor.getColumnCount()){
                    String[] current_table_names = currentCursor.getColumnNames();
                    String[] validate_table_names = validateCursor.getColumnNames();
                    if(currentCursor.getColumnNames().equals(currentCursor.getColumnNames()) || validate_table_names.toString().contains(current_table_names.toString())){
                        Cursor currentColumns = currentDatabase.rawQuery("SELECT * FROM " + validateCursor.getColumnName(validate_table_names.length-1) + " ",null);
                        Cursor validateColumns = validateDatabase.rawQuery("SELECT * FROM " + validateCursor.getColumnName(validate_table_names.length-1) + " ",null);
                        if(currentColumns.getColumnNames().toString().equals(validateColumns.getColumnNames().toString())||currentColumns.getColumnNames().toString().contains(validateColumns.getColumnNames().toString())){
                            //compared that everything in the current setting is included in the backup
                        }
                    }
                }*/
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public String getOneDataInHISTORY(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,* FROM "+ HISTORY_TABLE + " WHERE "+ ID + " = " + id, null);
        String data="";
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
        }
        cs.close();
        return data;
    }

    public void restoreAllDataFromHistoryToTODO(){
        SQLiteDatabase db = this.getWritableDatabase();
        String cmd = "INSERT INTO " + TODO_TABLE + " SELECT * FROM " + HISTORY_TABLE;
        db.execSQL(cmd);
        wipeHistory();
    }

    public void finishAllInTodoList(){
        SQLiteDatabase db = this.getWritableDatabase();
        String cmd = "INSERT INTO " + HISTORY_TABLE + " SELECT * FROM " + TODO_TABLE;
        db.execSQL(cmd);
        wipeTodoList();
    }

    public String getTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        String t=format.format(new Date());
        return t;
    }

    public long getIdOfLatestDataInTODO(){
        SQLiteDatabase db = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + TODO_TABLE + " ORDER BY _id DESC LIMIT 1";
        long id = 0;
        Cursor cs = db.rawQuery(cmd,null);
        while(cs.moveToNext()){
            id = cs.getInt(cs.getColumnIndex(ID));
        }
        cs.close();
        return id;
    }

    public void deleteTheLastCoupleOnesFromHistory(int number){
        SQLiteDatabase db = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + HISTORY_TABLE + " ORDER BY _id DESC LIMIT " + Integer.toString(number);
        Cursor cs = db.rawQuery(cmd,null);
        while(cs.moveToNext()){
            db.delete(HISTORY_TABLE,ID + " = ?",new String[]{Integer.toString(cs.getInt(cs.getColumnIndex(ID)))});
        }
        cs.close();
        db.close();
    }

    public void deleteTheLastCoupleOnesFromToDo(int number){
        SQLiteDatabase db = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + TODO_TABLE + " ORDER BY _id DESC LIMIT " + Integer.toString(number);
        Cursor cs = db.rawQuery(cmd,null);
        while(cs.moveToNext()){
            db.delete(TODO_TABLE,ID + " = ?",new String[]{Integer.toString(cs.getInt(cs.getColumnIndex(ID)))});
        }
        cs.close();
        db.close();
    }

    public long getIdOfLatestDataInHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + HISTORY_TABLE + " ORDER BY _id DESC LIMIT 1";
        long id = 0;
        Cursor cs = db.rawQuery(cmd,null);
        while (cs.moveToNext()){
            id = cs.getInt(cs.getColumnIndex(ID));
        }
        cs.close();
        return id;
    }

    /*public String getDeletedTimeFromDB(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT rowid _id,* FROM "+ HISTORY_TABLE + " WHERE "+ ID + " = " + id, null);
        String time="";
        while(cs.moveToNext()){
            time = cs.getString(cs.getColumnIndex(DELETED_TIMESTAMP));
        }
        cs.close();
        return time;
    }*/

    long diff = 0;
    public long getTimeDifference(String timestampstr){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            Date delTime = df.parse(timestampstr);
            Date nowTime = new Date(System.currentTimeMillis());//get now time
            diff = ((nowTime.getTime() - delTime.getTime())/1000)/60;//get time difference in minutes
            //long days = diff / (1000 * 60 * 60 * 24);
            //long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
            //long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
            return diff;
        }
        catch (Exception e)
        {
            diff = -1;
            return diff;
        }
    }

    public ArrayList<Integer> returnAllTagColors(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT _id," + TAG_COLOR + " FROM " + TAGS_TABLE ,null);
        ArrayList<Integer> allColors = new ArrayList<Integer>();
        if(cs.getCount() != 0){
            while(cs.moveToNext()){
                allColors.add(Color.parseColor(cs.getString(cs.getColumnIndex(TAG_COLOR))));
            }
        }
        cs.close();
        return allColors;
    }

    public ArrayList<String> returnAllTags(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT _id," + TAG + " FROM " + TAGS_TABLE ,null);
        ArrayList<String> allTags = new ArrayList<String>();
        if(cs.getCount() != 0){
            while(cs.moveToNext()){
                allTags.add(cs.getString(cs.getColumnIndex(TAG)));
            }
        }
        cs.close();
        return allTags;
    }

    public ArrayList<String> returnTagsForNavMenu(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT _id," + TAG + " FROM " + TAGS_TABLE + " LIMIT 5",null);
        ArrayList<String> allTags = new ArrayList<String>();
        if(cs.getCount() != 0){
            while(cs.moveToNext()){
                allTags.add(cs.getString(cs.getColumnIndex(TAG)));
            }
        }
        cs.close();
        return allTags;
    }

    public ArrayList<String> returnTagColorsForNavMenu(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT _id," + TAG_COLOR + " FROM " + TAGS_TABLE + " LIMIT 5",null);
        ArrayList<String> allTagColors = new ArrayList<>();
        if(cs.getCount() != 0){
            while(cs.moveToNext()){
                allTagColors.add(cs.getString(cs.getColumnIndex(TAG_COLOR)));
            }
        }
        cs.close();
        return allTagColors;
    }

    public String returnTagColorIfExist(String tag){//see if tag exists in the tag database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.query(false,TAGS_TABLE, new String[]{TAG,TAG_COLOR},TAG + " LIKE ?",new String[]{""+ tag+ ""},null,null,"_id desc",null );//search for the tag
        if(cs.getCount() == 0) return "";
        else {
            try{
                cs.moveToNext();
                String tagColor = cs.getString(cs.getColumnIndex(TAG_COLOR));
                cs.close();
                return tagColor;
            }catch (Exception e){
                cs.close();
                return "";
            }
        }
    }

    public boolean determineIfTagInUse(String tag){//see if tag is in use in the displaying, active notes
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.query(false,TODO_TABLE, new String[]{ID,TITLE},TITLE + " LIKE ? OR ? OR ?",new String[]{"%"+ tag+ "", "%"+ tag+ " %", "%" + tag + "\n%"},null,null,"_id desc",null );
        if(!(cs.getCount() == 0)) {
            while(cs.moveToNext()){//confirm that the tag still is in use again
                String todoText = cs.getString(cs.getColumnIndex(TITLE));
                if(todoText.contains(tag)){
                    String charBeforeTagStart = String.valueOf(todoText.charAt(todoText.indexOf(tag) - 1));//determine if the character before the tag start is a space or enter (determine if it's a legal in use tag)
                    if(charBeforeTagStart.equals(" ") || charBeforeTagStart.equals("\n")){
                        cs.close();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public long returnTagID(String tag){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.query(false,TAGS_TABLE, new String[]{ID,TAG},TAG + " LIKE ?",new String[]{"" + tag + ""},null,null,"_id desc",null );
        cs.moveToNext();
        long id = cs.getInt(cs.getColumnIndex(ID));
        cs.close();
        return id;
    }

    public void createNewTag(String tag, String tagColor){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TAG,tag);
        cv.put(TAG_COLOR,tagColor);
        db.insert(TAGS_TABLE,null,cv);
    }

    public void pinNote(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(PINNED,true);
        Date nowTime = Calendar.getInstance().getTime();//get now time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cv.put(PINNED_TIMESTAMP,dateFormat.format(nowTime));
        db.update(TODO_TABLE, cv, ID + " = ?", new String[] { String.valueOf(id) });
    }

    public void unpinNote(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(PINNED,false);
        cv.put(PINNED_TIMESTAMP,(String)null);
        db.update(TODO_TABLE, cv, ID + " = ?", new String[] { String.valueOf(id) });
    }

    public int returnPinnedNotesNumber(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.query(false,TODO_TABLE, new String[]{ID,PINNED},PINNED + " = ?",new String[]{"true"},null,null,"_id desc",null );//filter for pinned tag
        int count = cs.getCount();
        cs.close();
        return count;
    }

    /* OLD METHOD
    public boolean updateData(String id, String title){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(TITLE,title);
        db.update(TODO_TABLE, cv, ID + " = ?", new String[] { id });
        return true;
    }*/

    public void stopService(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.close();
    }

    public boolean isOpen(){
        SQLiteDatabase db = this.getWritableDatabase();
        return (db.isOpen());
    }
}
