package com.jackz314.todo;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Locale;
import java.util.prefs.PreferenceChangeListener;

import static com.jackz314.todo.MainActivity.DELETE_CONTEXT_ID;
import static com.jackz314.todo.R.color.colorPrimary;
import static com.jackz314.todo.dtb.CONTENT;
import static com.jackz314.todo.dtb.DELETED_TIMESTAMP;
import static com.jackz314.todo.dtb.ID;

public class HistoryActivity extends AppCompatActivity {
    dtb todosql;
    TextView emptyHistory, selectionTitle;
    ListView historyList;
    ActionBar toolbar;
    int themeColor,textColor,backgroundColor,textSize;
    SharedPreferences ps,sharedPreferencesCustom;
    private FirebaseAnalytics mFirebaseAnalytics;
    ColorUtils colorUtils;
    boolean isInSearchMode =false, isInSelectionMode = false;
    Toolbar selectionToolBar;
    ArrayList<Long> selectedId = new ArrayList<>();
    ArrayList<String> selectedContent = new ArrayList<>();
    public ArrayList<String> CLONESelectedContent = new ArrayList<>();
    public String searchText;
    MenuItem searchViewItem;
    CheckBox selectAllBox, multiSelectionBox;
    public static int RESTORE_CONTEXT_ID = 1;
    ConstraintLayout historyView;
    public static int DELETE_HISTORY_CONTEXT_ID = 2;
    MainActivity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ps = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        /*sharedPreferencesCustom = getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor=sharedPreferencesCustom.getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorPrimary));
        textColor=sharedPreferencesCustom.getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColor=sharedPreferencesCustom.getInt(getString(R.string.theme_color_key),Color.WHITE);*/
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_history);
        todosql = new dtb(this);
        selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
        setSupportActionBar(selectionToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        historyList = (ListView)findViewById(R.id.historyList);
        emptyHistory = (TextView)findViewById(R.id.emptyHistory);
        LayoutInflater inflater =this.getLayoutInflater();
        historyView = (ConstraintLayout)findViewById(R.id.historyView);
        selectionTitle = (TextView)selectionToolBar.findViewById(R.id.history_selection_toolbar_title);
        selectionTitle.setText(R.string.history_name);
       //View toolbarView = inflater.inflate(R.layout.app_bar_main,null);
        toolbar = getSupportActionBar();
        //toolbar.setBackgroundColor(themeColor);// not working yet!
        setHistoryColorsPreferences();
        deleteExpiredNotes();
        displayAllNotes();
        /*historyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(25);
                //vibrator.cancel();
                deleteNote(String.valueOf(id));
                return false;
            }
        });*/
        historyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(isInSelectionMode){
                    //do nothing
                }else {
                    isInSelectionMode = true;
                    //multiSelectionBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
                    //multiSelectionBox.setChecked(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
                    selectionToolBar.getMenu().clear();
                    displayAllNotes();
                    selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
                    selectionTitle = (TextView)selectionToolBar.findViewById(R.id.history_selection_toolbar_title);
                    //toolbar.hide();
                    selectionToolBar.setVisibility(View.VISIBLE);
                    selectionTitle.setText(getString(R.string.selection_mode_title));
                    //Drawable backArrow = getDrawable(R.drawable.ic_close_black_24dp);
                    //selectionToolBar.setNavigationIcon(backArrow);
                    selectionToolBar.setBackgroundColor(themeColor);
                    selectAllBox = (CheckBox)selectionToolBar.findViewById(R.id.history_select_all_box);
                    selectAllBox.setVisibility(View.VISIBLE);
                    ColorStateList colorStateList = new ColorStateList(
                            new int[][]{
                                    new int[]{-android.R.attr.state_checked}, //disabled
                                    new int[]{android.R.attr.state_checked}, //enabled
                                    new int[]{android.R.attr.background}
                            },
                            new int[] {
                                    Color.WHITE//disabled
                                    ,ColorUtils.lighten(themeColor,0.32) //enabled
                                    ,Color.WHITE
                            }
                    );
                    //selectAllBox.setBackground(new ColorDrawable(Color.WHITE));
                    selectAllBox.setButtonTintList(colorStateList);//set the color tint list
                    //selectAllBox.getButtonDrawable().setColorFilter(themeColor, PorterDuff.Mode.DST); //API>=23 (Android 6.0)
                    selectAllBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!selectAllBox.isChecked()){//uncheck all
                                selectAllBox.setChecked(false);
                                selectedId.clear();
                                selectedContent.clear();
                                for(int i = 0; i < historyList.getCount(); i++){
                                    multiSelectionBox = (CheckBox)historyList.getChildAt(i).findViewById(R.id.multiSelectionBox);
                                    multiSelectionBox.setChecked(false);
                                }
                                selectionTitle.setText(getString(R.string.selection_mode_empty_title));
                                selectionToolBar.getMenu().clear();
                            }else if(selectAllBox.isChecked()){//check all
                                selectAllBox.setChecked(true);
                                selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
                                selectionToolBar.inflateMenu(R.menu.history_selection_mode_menu);
                                selectionToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        if(item.getItemId() == R.id.history_selection_menu_restore){
                                            restoreSetOfData();
                                        }else if(item.getItemId() == R.id.history_selection_menu_delete){
                                            deleteSetOfData();
                                        }
                                        return false;
                                    }
                                });
                                Long id;
                                selectedId.clear();
                                selectedContent.clear();
                                for(int i = 0; i < historyList.getAdapter().getCount(); i++){
                                    multiSelectionBox = (CheckBox)historyList.getChildAt(i).findViewById(R.id.multiSelectionBox);
                                    multiSelectionBox.setChecked(true);
                                    id = historyList.getAdapter().getItemId(i);
                                    selectedId.add(0,id);
                                    String data = todosql.getOneDataInTODO(Long.toString(id));
                                    selectedContent.add(0,data);
                                }
                                String count = Integer.toString(selectedId.size());
                                selectionTitle.setText(count + getString(R.string.selection_mode_title));
                            }
                        }
                    });
                    /*selectionToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setOutOfSelectionMode();
                            //What to do on back clicked
                        }
                    });*/
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                }
                return false;
            }
        });


        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                if (isInSelectionMode) {
                    multiSelectionBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
                    if(multiSelectionBox.isChecked()){
                        removeSelectedId(id);
                        System.out.println("false" + id);
                        multiSelectionBox.setChecked(false);
                    }else {
                        addSelectedId(id);
                        multiSelectionBox.setChecked(true);
                        System.out.println("true" + id);

                    }
                    /*if(selectedId.contains(id)){
                        selectedId.remove(selectedId.indexOf(id));
                    }else {
                        selectedId.add(0,id);
                    }*/
                    // Toast.makeText(getApplicationContext(),selectedId.toString(),Toast.LENGTH_SHORT).show();
                }else {
                    final String restoredContent = todosql.getOneDataInHISTORY(String.valueOf(id));
                    restoreNote(String.valueOf(id));
                    Snackbar.make(historyView, getString(R.string.note_restored_snack_text), Snackbar.LENGTH_SHORT).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            todosql.insertDataToHistory(restoredContent);
                            todosql.deleteNote(todosql.getIdOfLatestDataInTODO());
                            displayAllNotes();
                        }
                    }).show();
                }
            }
        });

    }

    public void onResume(){
        setHistoryColorsPreferences();
        deleteExpiredNotes();
        displayAllNotes();
        super.onResume();
    }

    public void setHistoryColorsPreferences(){
        //get colors
        sharedPreferencesCustom = getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor=sharedPreferencesCustom.getInt(getString(R.string.theme_color_key),getResources().getColor(colorPrimary));
        textColor=sharedPreferencesCustom.getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColor=sharedPreferencesCustom.getInt(getString(R.string.background_color_key),Color.WHITE);
        textSize=sharedPreferencesCustom.getInt(getString(R.string.text_size_key),24);
        //set colors
        Window window = this.getWindow();
        window.setStatusBarColor(themeColor);
        window.setNavigationBarColor(themeColor);
        ActionBar actionBar = getSupportActionBar();
        Drawable actionBarColor = new ColorDrawable(themeColor);
        actionBarColor.setColorFilter(themeColor, PorterDuff.Mode.DST);
        actionBar.setBackgroundDrawable(actionBarColor);
        historyList.setBackgroundColor(backgroundColor);
        emptyHistory.setTextColor(ColorUtils.lighten(textColor,0.65));
        historyView.setBackgroundColor(backgroundColor);
        int[] colors = {0,colorUtils.lighten(textColor,0.3),0};
        historyList.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        historyList.setDividerHeight(2);
    }

    public void displayAllNotes(){
        if(isInSearchMode && searchText != null){
            displaySearchResults(searchText);
        }else {
            setHistoryColorsPreferences();
            int[] colors = {0,colorUtils.lighten(textColor,0.3),0};
            historyList.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
            historyList.setDividerHeight(2);
            sharedPreferencesCustom = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
            boolean isOrderReguar = sharedPreferencesCustom.getBoolean(getString(R.string.order_key),true);
            Cursor cs;
            if(!isOrderReguar){
                cs = todosql.getHistory();
            }
            else {
                cs = todosql.getHistoryDesc();
            }if(cs.getCount()==0){//if database is empty, then clears the listView too
                System.out.println("empty history!");
                emptyHistory.setVisibility(View.VISIBLE);
                emptyHistory.setText(R.string.empty_history);
                historyList.removeAllViewsInLayout();//remove all items
            } else {
                emptyHistory.setVisibility(View.INVISIBLE);
                emptyHistory.setText("");
            }
            historyList.setAdapter(new TodoListAdapter(this,R.layout.todolist,cs,new String[] {todosql.TITLE},new int[]{R.id.titleText}){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View todoView = super.getView(position,convertView,parent);
                    CheckBox multiSelectionBox = (CheckBox)todoView.findViewById(R.id.multiSelectionBox);
                    ColorStateList colorStateList = new ColorStateList(
                            new int[][]{
                                    new int[]{-android.R.attr.state_checked}, //disabled
                                    new int[]{android.R.attr.state_checked} //enabled
                            },
                            new int[] {
                                    Color.DKGRAY//disabled
                                    ,themeColor //enabled
                            }
                    );
                    if(isInSelectionMode){
                        multiSelectionBox.setVisibility(View.VISIBLE);
                        multiSelectionBox.setBackgroundColor(backgroundColor);
                        multiSelectionBox.setButtonTintList(colorStateList);
                        multiSelectionBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                returnSelected();
                            }
                        });
                    }else {
                        multiSelectionBox.setVisibility(View.GONE);
                    }
                    TextView todoText = (TextView)todoView.findViewById(R.id.titleText);
                    todoText.setTextColor(textColor);
                    todoText.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
                    return todoView;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (isInSelectionMode || isInSearchMode) {
            if (isInSelectionMode) {
                setOutOfSelectionMode();
            } else {
                setOutOfSearchMode();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void setOutOfSearchMode(){
        isInSearchMode = false;
        displayAllNotes();
    }

    public void setOutOfSelectionMode(){
        isInSelectionMode = false;
        selectedId.clear();
        selectedContent.clear();
        selectAllBox.setVisibility(View.GONE);
        selectionTitle.setText(R.string.history_name);
        selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
        selectionToolBar.getMenu().clear();
        selectionToolBar.inflateMenu(R.menu.search_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        displayAllNotes();
        selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
        //selectionToolBar.setVisibility(View.GONE);
        if(selectAllBox != null){
            selectAllBox.setChecked(false);
        }
        //toolbar.show();
        historyList.requestFocus();
    }

    public void restoreSetOfData(){
        CLONESelectedContent.clear();
        for(long id : selectedId){
            todosql.restoreDataHToM(String.valueOf(id));
        }
        final int size = selectedId.size();
        CLONESelectedContent = new ArrayList<>(selectedContent);
        setOutOfSelectionMode();
        displayAllNotes();
        Snackbar.make(historyView, getString(R.string.note_restored_snack_text), Snackbar.LENGTH_SHORT).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                todosql.deleteTheLastCoupleOnesFromToDo(CLONESelectedContent.size());
                for(String str : CLONESelectedContent){
                    todosql.insertDataToHistory(str);
                }
                displayAllNotes();
            }
        }).show();
    }

    public void deleteSetOfData(){
        CLONESelectedContent.clear();
        for(long id : selectedId){
            todosql.deleteFromHistory(String.valueOf(id));
        }
        CLONESelectedContent = new ArrayList<>(selectedContent);
        setOutOfSelectionMode();
        displayAllNotes();
        Snackbar.make(historyView, getString(R.string.note_deleted_snack_text), Snackbar.LENGTH_SHORT).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String content : CLONESelectedContent){
                    todosql.insertDataToHistory(content);
                }
                displayAllNotes();
            }
        }).show();
    }

    public void addSelectedId(long id){
        selectedId.add(0,id);
        String data = todosql.getOneDataInHISTORY(Long.toString(id));
        selectedContent.add(0,data);
        selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
        if(selectedId.size() == 1){
            selectionToolBar.inflateMenu(R.menu.history_selection_mode_menu);
            selectionToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.history_selection_menu_restore){
                        restoreSetOfData();
                    }else if(item.getItemId() == R.id.history_selection_menu_delete){
                        deleteSetOfData();
                    }
                    return false;
                }
            });
        }if(selectedId.size() == historyList.getCount()){
            selectAllBox.setChecked(true);
        }
        String count = Integer.toString(selectedId.size());
        selectionTitle.setText(count + getString(R.string.selection_mode_title));
    }

    public void removeSelectedId(long id){
        selectedId.remove(selectedId.indexOf(id));
        selectAllBox.setChecked(false);
        String data = todosql.getOneDataInHISTORY(Long.toString(id));
        selectedContent.remove(selectedContent.indexOf(data));
        if (selectedId.size() == 0) {
            selectionTitle.setText(getString(R.string.selection_mode_empty_title));
            selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
            selectionToolBar.getMenu().clear();
        }else {
            selectionToolBar = (Toolbar)findViewById(R.id.selection_toolbar);
            String count = Integer.toString(selectedId.size());
            selectionTitle.setText(count + getString(R.string.selection_mode_title));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.todo_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        Spannable hintText = new SpannableString(getString(R.string.search_hint));
        hintText.setSpan( new ForegroundColorSpan(ColorUtils.darken(Color.WHITE,0.5)), 0, hintText.length(), 0 );
        searchView.setQueryHint(hintText);
        MenuItem  searchMenuIem = menu.findItem(R.id.todo_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuIem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isInSearchMode = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if(isInSelectionMode){
                    setOutOfSelectionMode();
                    return false;
                }else {
                    setOutOfSearchMode();
                }
                return true;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInSearchMode = true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                displaySearchResults(newText);
                return false;
            }
        });
        return true;
    }

    public void displaySearchResults(final String filter){
        final Cursor cursor = todosql.getHistorySearchResults(filter);
        if(cursor.getCount() == 0){
            emptyHistory.setVisibility(View.VISIBLE);
            emptyHistory.setText(R.string.empty_search_result);
            historyList.removeAllViewsInLayout();//remove all items
            historyList.setAdapter(null);
        } else {
            emptyHistory.setVisibility(View.GONE);
            emptyHistory.setText("");
            final TodoListAdapter historyListAdapter = new TodoListAdapter(this,R.layout.todolist,cursor,new String[] {todosql.TITLE},new int[]{R.id.titleText});
            historyList.setAdapter(new TodoListAdapter(this,R.layout.todolist,cursor,new String[] {todosql.TITLE},new int[]{R.id.titleText}){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View todoView = super.getView(position,convertView,parent);
                    CheckBox multiSelectionBox = (CheckBox)todoView.findViewById(R.id.multiSelectionBox);
                    TextView todoText = (TextView)todoView.findViewById(R.id.titleText);
                    String cursorText = cursor.getString(cursor.getColumnIndex(dtb.TITLE));
                    int startPos = cursorText.toLowerCase(Locale.US).indexOf(filter.toLowerCase(Locale.US));
                    int endPos = startPos + filter.length();
                    todoText.setTextColor(textColor);
                    todoText.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
                    if (startPos != -1) // This should always be true, just a sanity check
                    {
                        Spannable spannable = new SpannableString(cursorText);
                        ColorStateList highlightColor = new ColorStateList(new int[][] { new int[] {}}, new int[] { Color.RED });
                        TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, highlightColor, null);
                        spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        todoText.setText(spannable);
                    }
                    ColorStateList colorStateList = new ColorStateList(
                            new int[][]{
                                    new int[]{-android.R.attr.state_checked}, //disabled
                                    new int[]{android.R.attr.state_checked} //enabled
                            },
                            new int[] {
                                    Color.DKGRAY//disabled
                                    ,themeColor //enabled
                            }
                    );
                    if(isInSelectionMode){
                        multiSelectionBox.setVisibility(View.VISIBLE);
                        multiSelectionBox.setBackgroundColor(backgroundColor);
                        multiSelectionBox.setButtonTintList(colorStateList);
                        multiSelectionBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                returnSelected();
                            }
                        });
                    }else {
                        multiSelectionBox.setVisibility(View.GONE);
                    }

                    return todoView;
                }
            });
        }
    }

    String timestr = "";
    int expireTime = 60*24;
    public void deleteExpiredNotes(){
        if(!ps.getBoolean("clear_history_switch_key",false)){//see auto delete state /off
            displayAllNotes();
        }
        else {//on
            Cursor cs = todosql.getHistory();
            if(cs.getCount()==0) System.out.println();
            else {
                expireTime=ps.getInt(getString(R.string.clear_interval_value_key),60*24);
                while(cs.moveToNext()){
                    timestr = cs.getString(cs.getColumnIndex("datetime(deleted_timestamp,'localtime')"));
                    //System.out.println(String.valueOf(todosql.getTimeDifference(timestr)));
                    if(todosql.getTimeDifference(timestr)>=expireTime){//if bigger than set value, delete it!
                        deleteNote(String.valueOf(cs.getInt(cs.getColumnIndex(todosql.ID))));
                    }
                }
            }
            cs.close();
            displayAllNotes();
        }
    }

    public void deleteNote(String id){
        Integer delRows = todosql.deleteFromHistory(id);
        displayAllNotes();
        if(delRows==0) Toast.makeText(this,R.string.error_message,Toast.LENGTH_SHORT).show();
    }

    public void restoreNote(String id){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "restore_notes");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "restore notes");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        todosql.restoreDataHToM(id);
        displayAllNotes();
    }

}
