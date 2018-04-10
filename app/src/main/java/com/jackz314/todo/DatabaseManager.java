package com.jackz314.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zhang on 2017/6/15.
 */

public class DatabaseManager extends SQLiteOpenHelper{
    public static String DATABASE_NAME = "todo.database";
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
    public static String REMIND_TIMES = "remind_times";
    public static String RECENT_REMIND_TIME = "most_recent_remind_time";
    public static String IMPORTANCE_TIMESTAMP = "importance_timestamp";
    public static String RECURRENCE_STATS = "recurring_stats";
    public static String CREATED_TIMESTAMP = "created_timestamp";
    public static String DELETED_TIMESTAMP = "deleted_timestamp";
    public static String SAVED_FOR_LATER_TIMESTAMP = "saved_for_later_timestamp";
    public static String SAVED_TIME = "saved_time";
    public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, 1);//remove this line after debug!
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ TODO_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + "" + CONTENT + " TEXT," + IMPORTANCE + " INTEGER," + IMPORTANCE_TIMESTAMP + " DATETIME, " + REMIND_TIMES + " TEXT, " + RECENT_REMIND_TIME + " DATETIME, " + RECURRENCE_STATS + " TEXT, " + CREATED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        db.execSQL("create table "+ HISTORY_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + CONTENT + " TEXT," + IMPORTANCE + " INTEGER," + IMPORTANCE_TIMESTAMP + " DATETIME, " + REMIND_TIMES + " TEXT, " + RECENT_REMIND_TIME + " DATETIME, " + RECURRENCE_STATS + " TEXT, " + DELETED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        //database.execSQL("create table "+ SAVED_FOR_LATER_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + CONTENT + " TEXT," + IMPORTANCE + " INTEGER," + SAVED_FOR_LATER_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        db.execSQL("create table "+ TAGS_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TAG + " TEXT," + TAG_COLOR + " TEXT," + CREATED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TODO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+HISTORY_TABLE);
        //database.execSQL("DROP TABLE IF EXISTS "+SAVED_FOR_LATER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+TAGS_TABLE);
        onCreate(db);
    }
    /*
    public boolean insertData(String title){

        ContentValues cv = new ContentValues();
        cv.put(TITLE,title);
        long result = database.insert(TODO_TABLE,null,cv);
        return (result != -1);
    }*/

    public boolean insertDataToHistory(String title){
        ContentValues cv = new ContentValues();
        cv.put(TITLE,title);
        SQLiteDatabase database = this.getWritableDatabase();
        long result = database.insert(HISTORY_TABLE,null,cv);
        return (result != -1);
    }

    public Cursor getAllData(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT rowid _id,* FROM " + TODO_TABLE ,null);
    }

    public Cursor getTagData(String tagName){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.query(TODO_TABLE,new String[]{"*"},"REPLACE (title, '*', '')" + " LIKE ? OR title LIKE ? OR title LIKE ?",new String[]{"%" + tagName + "", "%" + tagName + " %", "%" + tagName + "\n%"},null,null,null);
    }

    public Cursor getImportantData(){
        SQLiteDatabase database = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.US);
        String selectionAddOn = "";
        String[] selectionArgs = null;
        String currentTimeStr = MainActivity.getCurrentTimeString();
        Calendar recentTime = Calendar.getInstance();
        selectionAddOn = " OR " + REMIND_TIMES + " BETWEEN ? AND ?)";
        if(getRecentReminderCount() > 0){//has recent reminder
            recentTime.add(Calendar.WEEK_OF_YEAR,1);
            String recentTimeStr = dateFormat.format(recentTime.getTime());
            selectionArgs = new String[]{currentTimeStr,recentTimeStr};
        }else {
            if(getPinnedNotesCount() <= 0){//if there are no pinned notes, add other notes that contains reminders to important fragment
                //selectionAddOn = " OR " + REMIND_TIMES + " IS NOT NULL)";
                recentTime.add(Calendar.MONTH,1);
                String recentTimeStr = dateFormat.format(recentTime.getTime());
                selectionArgs = new String[]{currentTimeStr,recentTimeStr};
                //sort = sort + " LIMIT 5";
            }else{
                selectionAddOn = ")";//complete the parentheses
            }
            //" UNION SELECT * FROM " + TODO_TABLE + " WHERE " + REMIND_TIMES + " IS NOT NULL ORDER BY " + REMIND_TIMES + " ASC LIMIT 5";//add other notes with reminder in important if nothing includes recent reminders is present
        }
        return database.query(TODO_TABLE, new String[]{"*"}, "REPLACE (title, '*', '')" + " (" + IMPORTANCE + " = 1" + selectionAddOn, selectionArgs, null, null, null);
    }

    public  Cursor getDataDesc(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT rowid _id,* FROM " + TODO_TABLE + " order by _id desc",null);
    }

    public Cursor getSearchResults(String text){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.query(false,TODO_TABLE, new String[]{ID,TITLE},TITLE + " LIKE ?",new String[]{"%"+ text+ "%" },null,null,"_id desc",null );
    }

    public Cursor getHistorySearchResults(String text){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.query(false,HISTORY_TABLE, new String[]{ID,TITLE},TITLE + " LIKE ?",new String[]{"%"+ text+ "%" },null,null,"_id desc",null );
    }

    public void wipeHistory(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(HISTORY_TABLE,null,null);
    }

    public void wipeTodoList(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TODO_TABLE,null,null);
    }

    public Cursor getHistory(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT rowid _id,datetime(deleted_timestamp,'localtime'),* FROM " + HISTORY_TABLE,null);
        return cs;
    }

    public  Cursor getHistoryDesc(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT rowid _id,datetime(deleted_timestamp,'localtime'),* FROM " + HISTORY_TABLE + " order by _id desc",null);
        return cs;
    }

    public Integer finishData(Long id){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT rowid _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
        ContentValues cv = new ContentValues();
        String data;
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
            cv.put(TITLE,data);
        }
        database.insert(HISTORY_TABLE,null,cv);
        Integer del = database.delete(TODO_TABLE,ID + " = ?",new String[] {Long.toString(id)});
        cs.close();
        return del;
    }

    public Integer deleteFromHistory(String id){
        SQLiteDatabase database = this.getWritableDatabase();
        Integer del = database.delete(HISTORY_TABLE,ID + " = ?",new String[] {id});
        return del;
    }

    public void deleteNote(Long id){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TODO_TABLE,ID + " = ?",new String[] {Long.toString(id)});
    }

    public void insertDataForSpecialMsgAction(String data){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TITLE,data);
        database.insert(TODO_TABLE,null,cv);
    }

    public void restoreDataHToM(String id){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT rowid _id,* FROM "+ HISTORY_TABLE + " WHERE "+ ID + " = " + id, null);
        ContentValues cv = new ContentValues();
        String data;
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
            cv.put(TITLE,data);
        }
        database.insert(TODO_TABLE,null,cv);
        database.delete(HISTORY_TABLE,ID + " = ?",new String[] {id});
        cs.close();
    }

    public String getOneTitleInTODO(long id){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT rowid _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
        String data="";
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
        }
        cs.close();
        return data;
    }

    public Cursor getOneDataInTODO(long id){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT rowid _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
    }

    public String mergeBackup(String fileLocation) {//todo fix later
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
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT rowid _id,* FROM "+ HISTORY_TABLE + " WHERE "+ ID + " = " + id, null);
        String data="";
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
        }
        cs.close();
        return data;
    }

    public void restoreAllDataFromHistoryToTODO(){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "INSERT INTO " + TODO_TABLE + " SELECT * FROM " + HISTORY_TABLE;
        database.execSQL(cmd);
        wipeHistory();
    }

    public void finishAllInTodoList(){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "INSERT INTO " + HISTORY_TABLE + " SELECT * FROM " + TODO_TABLE;
        database.execSQL(cmd);
        wipeTodoList();
    }

    public long getIdOfLatestDataInTODO(){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + TODO_TABLE + " ORDER BY _id DESC LIMIT 1";
        long id = 0;
        Cursor cs = database.rawQuery(cmd,null);
        while(cs.moveToNext()){
            id = cs.getInt(cs.getColumnIndex(ID));
        }
        cs.close();
        return id;
    }

    public void deleteTheLastCoupleOnesFromHistory(int count){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + HISTORY_TABLE + " ORDER BY _id DESC LIMIT " + Integer.toString(count);
        Cursor cs = database.rawQuery(cmd,null);
        while(cs.moveToNext()){
            database.delete(HISTORY_TABLE,ID + " = ?",new String[]{Integer.toString(cs.getInt(cs.getColumnIndex(ID)))});
        }
        cs.close();
        database.close();
    }

    public void deleteTheLastCoupleOnesFromToDo(int count){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + TODO_TABLE + " ORDER BY _id DESC LIMIT " + Integer.toString(count);
        Cursor cs = database.rawQuery(cmd,null);
        while(cs.moveToNext()){
            database.delete(TODO_TABLE,ID + " = ?",new String[]{Integer.toString(cs.getInt(cs.getColumnIndex(ID)))});
        }
        cs.close();
        database.close();
    }

    public long getIdOfLatestDataInHistory(){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + HISTORY_TABLE + " ORDER BY _id DESC LIMIT 1";
        long id = 0;
        Cursor cs = database.rawQuery(cmd,null);
        while (cs.moveToNext()){
            id = cs.getInt(cs.getColumnIndex(ID));
        }
        cs.close();
        return id;
    }

    /*public String getDeletedTimeFromDB(long id){

        Cursor cs = database.rawQuery("SELECT rowid _id,* FROM "+ HISTORY_TABLE + " WHERE "+ ID + " = " + id, null);
        String time="";
        while(cs.moveToNext()){
            time = cs.getString(cs.getColumnIndex(DELETED_TIMESTAMP));
        }
        cs.close();
        return time;
    }*/

    long diff = 0;
    public long getTimeDifference(String timestampstr){
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.US);
            Date delTime = dateFormat.parse(timestampstr);
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

    public ArrayList<Integer> getAllTagColors(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT _id," + TAG_COLOR + " FROM " + TAGS_TABLE ,null);
        ArrayList<Integer> allColors = new ArrayList<Integer>();
        if(cs.getCount() != 0){
            while(cs.moveToNext()){
                allColors.add(Color.parseColor(cs.getString(cs.getColumnIndex(TAG_COLOR))));
            }
        }
        cs.close();
        return allColors;
    }

    public ArrayList<String> getAllTags(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT _id," + TAG + " FROM " + TAGS_TABLE ,null);
        ArrayList<String> allTags = new ArrayList<String>();
        if(cs.getCount() != 0){
            while(cs.moveToNext()){
                allTags.add(cs.getString(cs.getColumnIndex(TAG)));
            }
        }
        cs.close();
        return allTags;
    }

    public ArrayList<String> getTagsForNavMenu(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT _id," + TAG + " FROM " + TAGS_TABLE + " order by _id desc LIMIT 5",null);
        ArrayList<String> allTags = new ArrayList<String>();
        if(cs.getCount() != 0){
            while(cs.moveToNext()){
                allTags.add(cs.getString(cs.getColumnIndex(TAG)));
            }
        }
        cs.close();
        return allTags;
    }

    public ArrayList<String> getTagColorsForNavMenu(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT _id," + TAG_COLOR + " FROM " + TAGS_TABLE + " LIMIT 5",null);
        ArrayList<String> allTagColors = new ArrayList<>();
        if(cs.getCount() != 0){
            while(cs.moveToNext()){
                allTagColors.add(cs.getString(cs.getColumnIndex(TAG_COLOR)));
            }
        }
        cs.close();
        return allTagColors;
    }

    public String getTagColor(String tag){//see if tag exists in the tag database
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.query(false,TAGS_TABLE, new String[]{TAG,TAG_COLOR},TAG + " LIKE ?",new String[]{""+ tag+ ""},null,null,"_id desc",null );//search for the tag
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

    public boolean isTagInUse(String tag){//see if tag is in use in the displaying, active notes
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.query(false,TODO_TABLE, new String[]{ID,TITLE},TITLE + " LIKE ? OR ? OR ?",new String[]{"%"+ tag+ "", "%"+ tag+ " %", "%" + tag + "\n%"},null,null,"_id desc",null );
        if(!(cs.getCount() == 0)) {
            while(cs.moveToNext()){//confirm that the tag still is in use again
                String todoText = cs.getString(cs.getColumnIndex(TITLE));
                if(todoText.contains(tag)){
                    if(todoText.indexOf(tag) == 0){//starts with the tag
                        cs.close();
                        return true;
                    }
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

    public long getTagId(String tag){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.query(false,TAGS_TABLE, new String[]{ID,TAG},TAG + " LIKE ?",new String[]{"" + tag + ""},null,null,"_id desc",null );
        cs.moveToNext();
        long id = cs.getInt(cs.getColumnIndex(ID));
        cs.close();
        return id;
    }

    public void createNewTag(String tag, String tagColor){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TAG,tag);
        cv.put(TAG_COLOR,tagColor);
        database.insert(TAGS_TABLE,null,cv);
    }

    public void pinNote(long id){
        SQLiteDatabase database = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.US);
        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(IMPORTANCE,true);
        Date nowTime = Calendar.getInstance().getTime();//get now time
        cv.put(IMPORTANCE_TIMESTAMP, dateFormat.format(nowTime));
        database.update(TODO_TABLE, cv, ID + " = ?", new String[] { String.valueOf(id) });
    }

    public void unpinNote(long id){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(IMPORTANCE,false);
        cv.put(IMPORTANCE_TIMESTAMP,(String)null);
        database.update(TODO_TABLE, cv, ID + " = ?", new String[] { String.valueOf(id) });
    }

    public int getPinnedNotesCount(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.query(false,TODO_TABLE, new String[]{ID,IMPORTANCE},IMPORTANCE + " = ?",new String[]{"1"},null,null,"_id desc",null );//filter for pinned tag
        int count = cs.getCount();
        cs.close();
        return count;
    }

    public int getRecentReminderCount(){
        SQLiteDatabase database = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.US);
        Calendar recentTime = Calendar.getInstance();
        recentTime.add(Calendar.WEEK_OF_YEAR,2);
        String currentTimeStr = MainActivity.getCurrentTimeString();
        String recentTimeStr = dateFormat.format(recentTime.getTime());
        Cursor cs = database.query(false,TODO_TABLE, new String[]{ID, RECENT_REMIND_TIME}, RECENT_REMIND_TIME + " BETWEEN ? AND ?",new String[]{currentTimeStr,recentTimeStr},null,null,"_id desc",null );//filter for recent reminders
        int recentReminderCount = cs.getCount();
        cs.close();
        return recentReminderCount;
    }

    public int getReminderCount(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.query(false,TODO_TABLE, new String[]{ID, REMIND_TIMES}, REMIND_TIMES + " IS NOT NULL", null,null,null,null,null );//filter for recent reminders
        int recentReminderCount = cs.getCount();
        cs.close();
        return recentReminderCount;
    }

    public void removeExpiredReminderDates(){
        SQLiteDatabase database = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.US);
        Cursor cs = database.query(false,TODO_TABLE, new String[]{ID, REMIND_TIMES}, REMIND_TIMES + " IS NOT NULL", null,null,null,null,null );//filter for recent reminders
        Date currentTime = MainActivity.getCurrentTime();
        String oldRemindTimeStr = "";
        Gson remindGson = new Gson();
        Type type = new TypeToken<ArrayList<Date>>() {}.getType();
        int id;
        while(cs.moveToNext()){
            id = cs.getInt(cs.getColumnIndex(ID));
            oldRemindTimeStr = cs.getString(cs.getColumnIndex(REMIND_TIMES));
            ArrayList<Date> remindTimeOutput = remindGson.fromJson(oldRemindTimeStr,type);
            for(Date remindTime : remindTimeOutput){
                if(remindTime.compareTo(currentTime) <= 0){//remind time before or equals current time
                    remindTimeOutput.remove(remindTime);
                }
            }
            String newRemindTimeStr = remindGson.toJson(remindTimeOutput);
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID,id);
            contentValues.put(REMIND_TIMES, newRemindTimeStr);
            contentValues.put(RECENT_REMIND_TIME, dateFormat.format(remindTimeOutput.get(0)));
            database.update(TODO_TABLE, contentValues, ID + " = ?", new String[] { String.valueOf(id) });
        }
    }

    /* OLD METHOD
    public boolean updateData(String id, String title){

        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(TITLE,title);
        database.update(TODO_TABLE, cv, ID + " = ?", new String[] { id });
        return true;
    }*/

    public void stopService(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.close();
    }

    public boolean isOpen(){
        SQLiteDatabase database = this.getWritableDatabase();
        return (database.isOpen());
    }
}
