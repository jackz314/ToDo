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

import static com.jackz314.todo.MainActivity.removePastDates;
import static com.jackz314.todo.MainActivity.removePastDatesRecur;

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
    public static String DATE_STRING_REFERENCES = "date_string_references";
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
        db.execSQL("create table "+ TODO_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + "" + CONTENT + " TEXT," + IMPORTANCE + " INTEGER," + IMPORTANCE_TIMESTAMP + " DATETIME, " + REMIND_TIMES + " TEXT, " + RECENT_REMIND_TIME + " DATETIME, " + RECURRENCE_STATS + " TEXT, " + DATE_STRING_REFERENCES + " TEXT, " + CREATED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        db.execSQL("create table "+ HISTORY_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + CONTENT + " TEXT," + IMPORTANCE + " INTEGER," + IMPORTANCE_TIMESTAMP + " DATETIME, " + REMIND_TIMES + " TEXT, " + RECENT_REMIND_TIME + " DATETIME, " + RECURRENCE_STATS + " TEXT, " + DATE_STRING_REFERENCES + " TEXT, " + DELETED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        //database.execSQL("create table "+ SAVED_FOR_LATER_TABLE + " (" + ID + " int PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + CONTENT + " TEXT," + IMPORTANCE + " INTEGER," + SAVED_FOR_LATER_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
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
        cv.put(TITLE,title);//
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

    Cursor getAllData(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT rowid _id,* FROM " + TODO_TABLE ,null);
    }

    Cursor getTagData(String tagName){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.query(TODO_TABLE,new String[]{"*"},"REPLACE (title, '*', '')" + " LIKE ? OR title LIKE ? OR title LIKE ?",new String[]{"%" + tagName + "", "%" + tagName + " %", "%" + tagName + "\n%"},null,null,null);
    }

    Cursor getImportantData(){
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

    void wipeHistory(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(HISTORY_TABLE,null,null);
    }

    private void wipeTodoList(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TODO_TABLE,null,null);
    }

    public Cursor getHistory(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT _id,datetime(deleted_timestamp,'localtime'),* FROM " + HISTORY_TABLE,null);
    }

    public  Cursor getHistoryDesc(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT _id,datetime(deleted_timestamp,'localtime'),* FROM " + HISTORY_TABLE + " order by _id desc",null);
    }

    public Integer finishData(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
        ContentValues cv = new ContentValues();
        String data;
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
            cv.put(TITLE,data);
        }
        database.insert(HISTORY_TABLE,null,cv);
        Integer del = database.delete(TODO_TABLE,ID + " = ?",new String[] {Integer.toString(id)});
        cs.close();
        return del;
    }

    void deleteFromHistory(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        Integer del = database.delete(HISTORY_TABLE,ID + " = ?",new String[] {Integer.toString(id)});
    }

    void deleteNote(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TODO_TABLE,ID + " = ?",new String[] {Integer.toString(id)});
    }

    public void insertDataForSpecialMsgAction(String data){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TITLE,data);
        database.insert(TODO_TABLE,null,cv);
    }

    public void restoreDataHToM(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT rowid _id,* FROM "+ HISTORY_TABLE + " WHERE "+ ID + " = " + id, null);
        ContentValues cv = new ContentValues();
        String data;
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
            cv.put(TITLE,data);
        }
        database.insert(TODO_TABLE,null,cv);
        database.delete(HISTORY_TABLE,ID + " = ?",new String[] {Integer.toString(id)});
        cs.close();
    }

    public String getOneTitleInTODO(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT rowid _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
        String data="";
        while(cs.moveToNext()){
            data = cs.getString(cs.getColumnIndex(TITLE));
        }
        cs.close();
        return data;
    }

    Cursor getOneDataInTODO(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT rowid _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
    }

    String mergeBackup(String fileLocation) {//todo fix later
        SQLiteDatabase currentDatabase = null;
        boolean attached = false;
        try {
            SQLiteDatabase backupDatabase = SQLiteDatabase.openDatabase(fileLocation, null, SQLiteDatabase.OPEN_READWRITE);
            currentDatabase = this.getWritableDatabase();
            File curPath = new File(currentDatabase.getPath());
            File bakPath = new File(backupDatabase.getPath());
            String backupPath = bakPath.getAbsolutePath();
            String currentName = curPath.getName().substring(0,curPath.getName().length()-3);
            String bakName = bakPath.getName().substring(0,bakPath.getName().length()-3);
            /*if(currentDatabase.inTransaction()){
                //currentDatabase.endTransaction();
            }*/
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

    boolean validateBackup(String fileLocation){
        SQLiteDatabase validateDatabase;
        try{
            File dbfile = new File(fileLocation);
            if(dbfile.exists()){
                validateDatabase = SQLiteDatabase.openDatabase(dbfile.getAbsolutePath(),null,SQLiteDatabase.OPEN_READWRITE);
                Cursor validateCursor = validateDatabase.rawQuery("SELECT name FROM " + "sqlite_master WHERE type = 'table'", null);
                StringBuilder combinedString = new StringBuilder();
                while (validateCursor.moveToNext()){
                    combinedString.append(validateCursor.getString(validateCursor.getColumnIndex("name")));
                }
                validateCursor.close();
                //System.out.println(combinedString);
                if(combinedString.toString().contains(TODO_TABLE)){
                    Cursor validateColumns = validateDatabase.rawQuery("SELECT * FROM " + TODO_TABLE + " ",null);
                    StringBuilder colBuilder = new StringBuilder();
                    combinedString = new StringBuilder();
                    for(String each : validateColumns.getColumnNames()){
                        colBuilder.append(",").append(each);
                    }
                    combinedString = new StringBuilder(colBuilder.deleteCharAt(0).toString());
                    //System.out.println(combinedString);
                    validateColumns.close();
                    return combinedString.toString().contains(TITLE);
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

    String getOneDataInHistory(long id){
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

    int getIdOfLatestDataInTODO(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.rawQuery("SELECT * FROM " + TODO_TABLE + " ORDER BY _id DESC LIMIT 1",null);
        int id = -1;
        while (cs.moveToNext()){
            id = cs.getInt(cs.getColumnIndex(ID));
        }
        cs.close();
        return id;
    }

    void deleteTheLastCoupleOnesFromHistory(int count){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + HISTORY_TABLE + " ORDER BY _id DESC LIMIT " + Integer.toString(count);
        Cursor cs = database.rawQuery(cmd,null);
        while(cs.moveToNext()){
            database.delete(HISTORY_TABLE,ID + " = ?",new String[]{Long.toString(cs.getInt(cs.getColumnIndex(ID)))});
        }
        cs.close();
        database.close();
    }

    void deleteTheLastCoupleOnesFromToDo(int count){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + TODO_TABLE + " ORDER BY _id DESC LIMIT " + Integer.toString(count);
        Cursor cs = database.rawQuery(cmd,null);
        while(cs.moveToNext()){
            database.delete(TODO_TABLE,ID + " = ?",new String[]{Long.toString(cs.getInt(cs.getColumnIndex(ID)))});
        }
        cs.close();
        database.close();
    }

    int getIdOfLatestDataInHistory(){
        SQLiteDatabase database = this.getWritableDatabase();
        String cmd = "SELECT * FROM " + HISTORY_TABLE + " ORDER BY _id DESC LIMIT 1";
        int id = 0;
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

    private long diff = 0;
    long getTimeDifference(String timestampstr){
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

    ArrayList<Integer> getAllTagColors(){
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

    public ArrayList<String> getTags(){
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

    public Cursor getTagsCursor(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("SELECT _id," + TAG + " FROM " + TAGS_TABLE ,null);
    }

    ArrayList<String> getTagsForNavMenu(){
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

    ArrayList<String> getTagColorsForNavMenu(){
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

    boolean isTagInUse(String tag){//see if tag is in use in the displaying, active notes
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

    int getTagId(String tag){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.query(false,TAGS_TABLE, new String[]{ID,TAG},TAG + " LIKE ?",new String[]{"" + tag + ""},null,null,"_id desc",null );
        cs.moveToFirst();
        int id = cs.getInt(cs.getColumnIndex(ID));
        cs.close();
        return id;
    }

    String getTag(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.query(false,TAGS_TABLE, new String[]{ID, TAG}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, "_id desc", null);
        cs.moveToFirst();
        String tag = cs.getString(cs.getColumnIndex(TAG));
        cs.close();
        return tag;
    }

    public Cursor getTagData(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.query(false,TAGS_TABLE, new String[]{ID, TAG}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, "_id desc", null);
    }

    void createNewTag(String tag, String tagColor){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TAG,tag);
        cv.put(TAG_COLOR,tagColor);
        database.insert(TAGS_TABLE,null,cv);
    }

    public void pinNote(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.US);
        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(IMPORTANCE,1);
        Date nowTime = Calendar.getInstance().getTime();//get now time
        cv.put(IMPORTANCE_TIMESTAMP, dateFormat.format(nowTime));
        database.update(TODO_TABLE, cv, ID + " = ?", new String[] { String.valueOf(id) });
    }

    public void unpinNote(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(IMPORTANCE,0);
        cv.put(IMPORTANCE_TIMESTAMP,(String)null);
        database.update(TODO_TABLE, cv, ID + " = ?", new String[] { String.valueOf(id) });
    }

    int getPinnedNotesCount(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cs = database.query(false,TODO_TABLE, new String[]{ID,IMPORTANCE},IMPORTANCE + " > 0", null,null,null,"_id desc",null );//filter for pinned tag
        int count = cs.getCount();
        cs.close();
        return count;
    }

    int getRecentReminderCount(){
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

    Cursor getAllToDoWithReminders(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.query(false,TODO_TABLE, new String[]{ID, REMIND_TIMES}, REMIND_TIMES + " IS NOT NULL", null,null,null,null,null );
    }

    boolean hasReminder(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT rowid _id,* FROM "+ TODO_TABLE + " WHERE "+ ID + " = " + id, null);
        boolean hasReminder = false;
        //System.out.println("HAS REMINDER ID: " + id + "count" + cursor.getCount());
        while (cursor.moveToNext()){
            hasReminder = cursor.getString(cursor.getColumnIndex(REMIND_TIMES)) != null && !cursor.getString(cursor.getColumnIndex(REMIND_TIMES)).isEmpty();
        }
        cursor.close();
        return hasReminder;
    }

    void finishReminder(int id, Date... fromDate){//todo not working
        SQLiteDatabase database = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Cursor cs = database.query(false,TODO_TABLE, new String[]{"*"}, ID + " = ?", new String[]{String.valueOf(id)},null,null,null,null );//filter for recent reminders
        String oldRemindTimeStr = null, oldRecurrenceStatsStr = null;
        while (cs.moveToNext()){
            oldRemindTimeStr = cs.getString(cs.getColumnIndex(REMIND_TIMES));
            oldRecurrenceStatsStr = cs.getString(cs.getColumnIndex(RECURRENCE_STATS));
        }
        cs.close();
        if(oldRemindTimeStr != null && !oldRemindTimeStr.isEmpty()){
            Date currentTime;
            if(fromDate != null && fromDate.length > 0){
                currentTime = fromDate[0];
            }else {
                currentTime = MainActivity.getCurrentTime();
            }

            Gson remindGson = new Gson();

            //update remind times
            Type remindTimeType = new TypeToken<ArrayList<Date>>() {}.getType();
            String newRemindTimeStr = null;
            ArrayList<Date> remindTimes = null;
            remindTimes = remindGson.fromJson(oldRemindTimeStr,remindTimeType);
            remindTimes = removePastDates(remindTimes, currentTime);//remove past times
            if(remindTimes.size() > 0){
                newRemindTimeStr = remindGson.toJson(remindTimes);
            }

            //update recurring statuses
            Type recurStatType = new TypeToken<ArrayList<ArrayList<String>>>() {}.getType();
            String newRecurStatStr = null;
            if(oldRecurrenceStatsStr != null && !oldRecurrenceStatsStr.isEmpty()){//only update if recurring statuses exist
                ArrayList<ArrayList<String>> recurrenceStats = remindGson.fromJson(oldRecurrenceStatsStr,recurStatType);
                recurrenceStats = removePastDatesRecur(recurrenceStats, currentTime);
                if(recurrenceStats.size() > 0){
                    newRecurStatStr = remindGson.toJson(recurrenceStats);
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID,id);
            contentValues.put(REMIND_TIMES, newRemindTimeStr);//could be null if no more remind times exist
            if(newRemindTimeStr != null){//could be null if no more remind times exist
                contentValues.put(RECENT_REMIND_TIME, dateFormat.format(remindTimes.get(0)));
            }else {
                contentValues.put(RECENT_REMIND_TIME, (String)null);
            }
            contentValues.put(RECURRENCE_STATS, newRecurStatStr);//could be null if no more remind times exist
            database.update(TODO_TABLE, contentValues, ID + " = ?", new String[] { String.valueOf(id) });//finally, update the value
        }
    }

    void storeSnoozedReminder(int id, long snoozeToTime){
        SQLiteDatabase database = this.getWritableDatabase();
        Date snoozeToDate = new Date(snoozeToTime);
        finishReminder(id,new Date(snoozeToTime));//update the rest of the reminder data to a status after the put off (snoozed) time, then add the snooze to time in there

        //add the snooze to time into remind data set
        Cursor cs = database.query(false,TODO_TABLE, new String[]{"*"}, ID + " = ?", new String[]{String.valueOf(id)},null,null,null,null );//filter for recent reminders
        Gson remindGson = new Gson();
        Type type = new TypeToken<ArrayList<Date>>() {}.getType();
        ArrayList<Date> remindTimes = new ArrayList<>();
        while (cs.moveToNext()){
            remindTimes = remindGson.fromJson(cs.getString(cs.getColumnIndex(REMIND_TIMES)), type);
        }
        cs.close();//close the cursor asap
        remindTimes.add(0, snoozeToDate);
        String newRemindTimesStr = remindGson.toJson(remindTimes);//pack up the remindTimes ArrayList again with the newly added snooze to time
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(REMIND_TIMES, newRemindTimesStr);
        cv.put(RECENT_REMIND_TIME, dateFormat.format(snoozeToDate));//set the most recent remind time to the snooze to time
        database.update(TODO_TABLE, cv, ID + " = ?", new String[] { String.valueOf(id) });
    }

    /* OLD METHOD
    public boolean updateData(String id, String title){

        ContentValues cv = new ContentValues();
        cv.put(ID,id);
        cv.put(TITLE,title);
        database.update(TODO_TABLE, cv, ID + " = ?", new String[] { id });
        return true;
    }*/

    void stopService(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.close();
    }

    boolean isOpen(){
        SQLiteDatabase database = this.getWritableDatabase();
        return (database.isOpen());
    }
}
