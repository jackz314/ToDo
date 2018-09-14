package com.jackz314.todo;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jackz314.todo.utils.ColorUtils;

import java.util.ArrayList;

import static com.jackz314.todo.DatabaseManager.ID;
import static com.jackz314.todo.DatabaseManager.TITLE;
import static com.jackz314.todo.MainActivity.removeCharAt;
import static com.jackz314.todo.R.color.colorActualPrimary;
import static com.jackz314.todo.SetEdgeColor.setEdgeColor;

public class HistoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    DatabaseManager todosql;
    TextView emptyHistory, selectionTitle;
    RecyclerView historyList;
    int themeColor,textColor,backgroundColor,textSize;
    int doubleClickCount = 0;
    SharedPreferences sharedPreferences;
    private FirebaseAnalytics mFirebaseAnalytics;
    ColorUtils colorUtils;
    boolean isInSearchMode =false, isInSelectionMode = false;
    Toolbar toolbar;
    long selectedItemID;
    boolean selectAll = false, unSelectAll = false;
    ArrayList<Long> selectedId = new ArrayList<>();
    ArrayList<String> selectedContent = new ArrayList<>();
    public ArrayList<String> CLONESelectedContent = new ArrayList<>();
    public String searchText;
    MenuItem searchViewItem;
    SearchView searchView;
    CheckBox selectAllBox, multiSelectionBox;
    private static final String[] PROJECTION = new String[]{ID, TITLE};
    private static final String SELECTION = "REPLACE (title, '*', '')" + " LIKE ?";
    public static int RESTORE_CONTEXT_ID = 1;
    ConstraintLayout historyView;
    public static int DELETE_HISTORY_CONTEXT_ID = 2;
    TodoListAdapter historyListAdapter;
    //todo fix history restore as blank note problem
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*sharedPreferences) = sharedPreferences("settings_data",MODE_PRIVATE);
        themeColorSetting=sharedPreferences).getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorActualPrimary));
        textColorSetting=sharedPreferences).getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColorSetting=sharedPreferences).getInt(getString(R.string.theme_color_key),Color.WHITE);*/
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_history);
        todosql = new DatabaseManager(this);
        toolbar = findViewById(R.id.history_selection_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        historyList = findViewById(R.id.historyList);
        historyList.setHasFixedSize(true);
        emptyHistory = findViewById(R.id.emptyHistory);
        LayoutInflater inflater =this.getLayoutInflater();
        historyView = findViewById(R.id.historyView);
        selectionTitle = toolbar.findViewById(R.id.history_selection_toolbar_title);
        selectionTitle.setText(R.string.history_name);
       //View toolbarView = inflater.inflate(R.layout.app_bar_main,null);
        //toolbar.setBackgroundColor(themeColorSetting);// not working yet!
        setHistoryColorsPreferences();
        deleteExpiredNotes();
        displayAllNotes();
        historyList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                setEdgeColor(historyList,themeColor);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                setEdgeColor(historyList,themeColor);
            }
        });
        ItemClickSupport.addTo(historyList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                long id = historyList.getAdapter().getItemId(position);
                if(isInSearchMode){
                    setOutOfSearchMode();
                }
                unSelectAll = false;
                selectAll = false;
                if (isInSelectionMode) {
                    multiSelectionBox = v.findViewById(R.id.multiSelectionBox);
                    if(multiSelectionBox.isChecked()){
                        removeSelectedId(id);
                        ////System.out.println("false" + id);
                        multiSelectionBox.setChecked(false);
                    }else {
                        addSelectedId(id);
                        multiSelectionBox.setChecked(true);
                        ////System.out.println("true" + id);
                    }
                    /*if(selectedId.contains(id)){
                        selectedId.remove(selectedId.indexOf(id));
                    }else {
                        selectedId.add(0,id);
                    }*/
                    // Toast.makeText(getApplicationContext(),selectedId.toString(),Toast.LENGTH_SHORT).show();
                }else {
                    final String restoredContent = todosql.getOneDataInHISTORY(String.valueOf(id));
                    restoreData(id);
                    //historyListAdapter.notifyItemChanged(position);
                    //historyListAdapter.notifyItemRangeChanged(position, historyListAdapter.getItemCount());
                    Snackbar.make(historyView, getString(R.string.note_restored_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            insertData(restoredContent);
                            todosql.deleteNote(todosql.getIdOfLatestDataInTODO());
                            displayAllNotes();
                        }
                    }).show();
                }
            }
        });

        ItemClickSupport.addTo(historyList).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, final View view) {
                long id = historyListAdapter.getItemId(position);
                unSelectAll = false;
                selectAll = false;
                if(isInSelectionMode){
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("ToDo", todosql.getOneDataInHISTORY(String.valueOf(id)));
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(view,getString(R.string.todo_copied),Snackbar.LENGTH_SHORT).show();
                }else {
                    if(isInSearchMode){
                        setOutOfSearchMode();
                    }
                    setOutOfSelectionMode();
                    //multiSelectionBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
                    //multiSelectionBox.setChecked(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toolbar = findViewById(R.id.history_selection_toolbar);
                    toolbar.getMenu().clear();
                    toolbar = findViewById(R.id.history_selection_toolbar);
                    selectionTitle = toolbar.findViewById(R.id.history_selection_toolbar_title);
                    //toolbar.hide();
                    toolbar.setVisibility(View.VISIBLE);
                    selectionTitle.setText(getString(R.string.selection_mode_title));
                    //Drawable backArrow = getDrawable(R.drawable.ic_close_black_24dp);
                    //toolbar.setNavigationIcon(backArrow);
                    toolbar.setBackgroundColor(themeColor);
                    selectAllBox = toolbar.findViewById(R.id.history_select_all_box);
                    selectAllBox.setVisibility(View.VISIBLE);
                    isInSelectionMode = true;
                    ////System.out.println(isInSelectionMode + "isInselectionmode");
                    selectedItemID = id;
                    getSupportLoaderManager().restartLoader(234,null,HistoryActivity.this);
                    displayAllNotes();
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
                    //selectAllBox.getButtonDrawable().setColorFilter(themeColorSetting, PorterDuff.Mode.DST); //API>=23 (Android 6.0)
                    selectAllBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!selectAllBox.isChecked()){//uncheck all
                                selectAllBox.setChecked(false);
                                selectedId.clear();
                                selectedContent.clear();
                                unSelectAll = true;
                                selectAll = false;
                                //historyList.getAdapter().notifyDataSetChanged();
                                selectionTitle.setText(getString(R.string.selection_mode_empty_title));
                                toolbar.getMenu().clear();
                                historyList.getAdapter().notifyDataSetChanged();
                            }else if(selectAllBox.isChecked()){//check all
                                selectAllBox.setChecked(true);
                                selectAll = true;
                                unSelectAll = false;
                                toolbar = findViewById(R.id.history_selection_toolbar);
                                if(selectedId.size() == 0){
                                    toolbar.inflateMenu(R.menu.history_selection_mode_menu);
                                    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
                                }

                                long id;
                                selectedId.clear();
                                selectedContent.clear();
                                //historyList.getAdapter().notifyDataSetChanged();
                                Cursor cursor = todosql.getHistory();
                                cursor.moveToFirst();
                                do{
                                    id = cursor.getInt(cursor.getColumnIndex(ID));
                                    selectedId.add(0,id);
                                    String data = todosql.getOneDataInHISTORY(Long.toString(id));
                                    selectedContent.add(0,data);
                                }while (cursor.moveToNext());
                                String count = Integer.toString(selectedId.size());
                                selectionTitle.setText(count + getString(R.string.selection_mode_title));
                                historyList.getAdapter().notifyDataSetChanged();
                            }
                        }
                    });
                    /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setOutOfSelectionMode();
                            //What to do on back clicked
                        }
                    });*/
                    addSelectedId(id);
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                }
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            historyList.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    setEdgeColor(historyList,themeColor);
                }
            });
        }else {
            historyList.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    setEdgeColor(historyList,themeColor);
                }
            });
        }


        doubleClickCount = 0;
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doubleClickCount++;
                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        doubleClickCount = 0;
                    }
                };
                handler.postDelayed(r,250);
                if (doubleClickCount == 2) {//double clicked
                    doubleClickCount = 0;
                    historyList.smoothScrollToPosition(0);//smooth scroll to top
                }
            }
        });
    }

    public void onResume(){
        setHistoryColorsPreferences();
        deleteExpiredNotes();
        setEdgeColor(historyList,themeColor);
        displayAllNotes();
        super.onResume();
    }

    public void setHistoryColorsPreferences(){
        //get colors
        sharedPreferences = getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor=sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(colorActualPrimary));
        textColor=sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColor=sharedPreferences.getInt(getString(R.string.background_color_key),Color.WHITE);
        textSize=sharedPreferences.getInt(getString(R.string.text_size_key),24);
        //set colors
        Window window = this.getWindow();
        window.setStatusBarColor(themeColor);
        window.setNavigationBarColor(themeColor);
        ActionBar actionBar = getSupportActionBar();
        Drawable actionBarColor = new ColorDrawable(themeColor);
        actionBarColor.setColorFilter(themeColor, PorterDuff.Mode.DST);
        actionBar.setBackgroundDrawable(actionBarColor);
        historyList.setBackgroundColor(backgroundColor);
        emptyHistory.setTextColor(ColorUtils.lighten(textColor,0.6));
        historyView.setBackgroundColor(backgroundColor);
        int[] colors = {0, ColorUtils.lighten(textColor,0.3),0};
        //divider
    }

    public void displayAllNotes(){
        if(historyList.getAdapter() == null){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            historyList.setLayoutManager(linearLayoutManager);
            historyListAdapter = (new TodoListAdapter(){
                @Override
                public void onBindViewHolder(TodoViewHolder holder, Cursor cursor) {
                    super.onBindViewHolder(holder, cursor);
                    final long id = cursor.getInt(cursor.getColumnIndex(DatabaseManager.ID));
                    String text = cursor.getString(cursor.getColumnIndex(DatabaseManager.TITLE));
                    holder.todoText.setTextColor(textColor);
                    holder.cardView.setCardBackgroundColor(ColorUtils.darken(backgroundColor,0.01));
                    holder.todoText.setTextSize(textSize);
                    SpannableStringBuilder spannable = new SpannableStringBuilder(text);

                    //bold section
                    int boldStartPos = text.indexOf("*");
                    if(boldStartPos >= 0){//contains bold markdown
                        while(boldStartPos < text.length() && boldStartPos >= 0){
                            int boldEndPos = text.indexOf("*",boldStartPos + 1);
                            if(boldEndPos < 0){
                                break;
                            }else {
                                spannable.delete(boldStartPos, boldStartPos + 1);//delete "*" after marked
                                spannable.delete(boldEndPos - 1, boldEndPos);//this doesn't work....
                                text = removeCharAt(text, boldStartPos);
                                text = removeCharAt(text, boldEndPos - 1);
                                System.out.println(text);
                                spannable.setSpan(new StyleSpan(Typeface.BOLD), boldStartPos, boldEndPos - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//set marked text to bold
                                boldStartPos = text.indexOf("*", boldEndPos - 2);
                            }
                        }
                    }

                    //tag section
                    int tagStartPos = text.indexOf("#",0);//find the position of the start point of the tag
                    if(tagStartPos >= 0){//if potentially contains tags
                        while(tagStartPos < text.length() - 1 && tagStartPos >= 0){//search and set color for all tags
                            int tagEndPos = -1;//assume neither enter nor space exists
                            if(text.indexOf(" ",tagStartPos) >= 0&& text.indexOf("\n",tagStartPos) >= 0){//contains both enter and space
                                tagEndPos = Math.min(text.indexOf(" ",tagStartPos),text.indexOf("\n",tagStartPos));//find the position of end point of the tag: space or line break
                            }else if(text.indexOf(" ",tagStartPos) < 0){//contains only enter
                                tagEndPos = text.indexOf("\n",tagStartPos);
                            }else {//contains only space
                                tagEndPos = text.indexOf(" ",tagStartPos);
                            }
                            if(tagEndPos < 0){//if the tag is the last section of the note
                                tagEndPos = text.length() - 1;
                            }else if(tagEndPos == tagStartPos + 1){//if only one #, skip to next loop
                                continue;
                            }
                            //System.out.println(tagStartPos + " AND " + tagEndPos);
                            String tag = text.toLowerCase().substring(tagStartPos,tagEndPos + 1);//ignore case in tags//REMEMBER: SUBSTRING SECOND VARIABLE DOESN'T CONTAIN THE CHARACTER AT THAT POSITION
                            //System.out.println("TEXT: " + text + "****" + tag + "********");
                            String tagColor = todosql.getTagColor(tag);
                            if(tagColor.equals("")){//if tag doesn't exist
                                tagColor = "#BBBBBC";//set tag color to grey in history
                            }
                            spannable.setSpan(new TextAppearanceSpan(null,Typeface.ITALIC,-1,
                                    new ColorStateList(new int[][] {new int[] {}},
                                            new int[] {Color.parseColor(tagColor)})
                                    ,null), tagStartPos, tagEndPos + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//highlight tag text
                            tagStartPos = text.indexOf("#",tagEndPos);//set tagStartPos to the new tag start point
                            //todo performance issue
                        }
                    }

                    //search section
                    if(isInSearchMode){
                        if(cursor.getCount() == 0){
                            emptyHistory.setVisibility(View.VISIBLE);
                            emptyHistory.setText(getString(R.string.empty_search_result));
                        }else {
                            emptyHistory.setVisibility(View.GONE);
                            emptyHistory.setText("");
                            ColorStateList highlightColor = new ColorStateList(new int[][] { new int[] {}}, new int[] { Color.parseColor("#ef5350") });
                            String textLow = text.toLowerCase();
                            String searchTextLow = searchText.toLowerCase();
                            int startPos = textLow.indexOf(searchTextLow);
                            if(!(startPos <0)){
                                do{
                                    int start = Math.min(startPos, textLow.length());
                                    int end = Math.min(startPos + searchTextLow.length(), textLow.length());
                                    startPos = textLow.indexOf(searchTextLow,end);
                                    spannable.setSpan(new TextAppearanceSpan(null,Typeface.BOLD,-1,new ColorStateList(new int[][] {new int[] {}},new int[] {Color.parseColor("#ef5350")}),null), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }while (startPos > 0);
                            }
                            //spannable.setSpan(new TextAppearanceSpan(null,Typeface.BOLD,-1,new ColorStateList(new int[][] {new int[] {}},new int[] {Color.parseColor("#ef5350")}),null), startPos, startPos + searchTextLow.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                    holder.todoText.setText(spannable);
                    ////System.out.println(isInSelectionMode + "DISPLAYALLNOTES");
                    if(isInSelectionMode){
                        holder.cBox.setVisibility(View.VISIBLE);
                        if(selectAll){
                            holder.cBox.setChecked(true);
                        }
                        if (unSelectAll){
                            holder.cBox.setChecked(false);
                        }
                        if(selectedItemID == id){//process the first long press select item
                            holder.cBox.setChecked(true);
                            selectedItemID = -250;
                        }else {
                            if(!selectAll){
                                holder.cBox.setChecked(false);
                            }
                        }
                        //Toast.makeText(getApplicationContext(),"SELECTIONMODE",Toast.LENGTH_SHORT).show();
                    }else {
                        holder.cBox.setChecked(false);
                        holder.cBox.setVisibility(View.GONE);
                    }
                    ////System.out.println(text+"|cursor read");
                    holder.cBox.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            CheckBox cb = v.findViewById(R.id.multiSelectionBox);
                            unSelectAll = false;
                            selectAll = false;
                            if (cb.isChecked()) {
                                    addSelectedId(id);
                                ////System.out.println("checked " + id);
                                // do some operations here
                            } else if (!cb.isChecked()) {
                                ////System.out.println("unchecked " + id);

                                removeSelectedId(id);
                                                 // do some operations here
                            }
                        }
                    });
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
                    holder.cBox.setButtonTintList(colorStateList);
                }
            });
            historyList.setAdapter(historyListAdapter);
            getSupportLoaderManager().initLoader(234,null,this);
            ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            mItemTouchHelper.attachToRecyclerView(historyList);
        }
        if(todosql.getHistory().getCount()==0){//if database is empty, then clears the listView too
            //////System.out.println("empty history!");
            emptyHistory.setVisibility(View.VISIBLE);
            emptyHistory.setText(R.string.empty_history);
            //historyList.removeAllViewsInLayout();//remove all items
            historyList.setAdapter(null);
        } else {
            emptyHistory.setVisibility(View.GONE);
            emptyHistory.setText("");
        }
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if(isInSelectionMode) return 0; //prevent swipe in selection mode
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.RIGHT) {
                final String restoredContent = todosql.getOneDataInHISTORY(String.valueOf(viewHolder.getItemId()));
                restoreData(viewHolder.getItemId());
                Snackbar.make(historyView, getString(R.string.note_restored_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        insertData(restoredContent);
                        todosql.deleteTheLastCoupleOnesFromToDo(1);
                        displayAllNotes();
                    }
                }).show();
            }

            if (direction == ItemTouchHelper.LEFT) {
                final String deletedContent = todosql.getOneDataInHISTORY(String.valueOf(viewHolder.getItemId()));
                deleteData(viewHolder.getItemId());
                Snackbar.make(historyView, getString(R.string.note_deleted_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        insertData(deletedContent);
                        displayAllNotes();
                    }
                }).show();
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (viewHolder.getAdapterPosition() == -1) {
                return;
            }

            View itemView = viewHolder.itemView;
            int iconMargin = 34;
            int itemHeight = itemView.getBottom() - itemView.getTop();
            Paint textPaint = new Paint();
            textPaint.setStrokeWidth(2);
            textPaint.setTextSize(80);
            textPaint.setColor(themeColor);
            textPaint.setTextAlign(Paint.Align.LEFT);

            if(dX < 0 && actionState == ItemTouchHelper.ACTION_STATE_SWIPE){//swiped left
                Rect deleteTextBond = new Rect();
                textPaint.getTextBounds(getString(R.string.delete),0,getString(R.string.delete).length(), deleteTextBond);
                Drawable deleteIcon = ContextCompat.getDrawable(HistoryActivity.this, R.drawable.ic_delete_black_24dp);//draw finish icon
                deleteIcon.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                int intrinsicHeight = deleteIcon.getIntrinsicWidth();
                int deleteIconLeft = itemView.getRight() - iconMargin - intrinsicWidth - deleteTextBond.width();
                int deleteIconRight = itemView.getRight() - iconMargin - deleteTextBond.width();
                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);

                deleteIcon.draw(c);
                c.drawText(getString(R.string.delete),(float) itemView.getRight() - iconMargin - deleteTextBond.width() ,(((deleteIconTop + deleteIconBottom)/2) - (textPaint.descent()+textPaint.ascent())/2), textPaint);
            }
            if(dX > 0 && actionState == ItemTouchHelper.ACTION_STATE_SWIPE){//swiped right
                Drawable restoreIcon = ContextCompat.getDrawable(HistoryActivity.this,R.drawable.ic_restore_black_24dp);
                restoreIcon.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
                int intrinsicWidthRestore = restoreIcon.getIntrinsicWidth();
                int intrinsicHeighthRestore = restoreIcon.getIntrinsicHeight();
                int restoreIconLeft = itemView.getLeft() + iconMargin;
                int restoreIconRight = itemView.getLeft() + iconMargin + intrinsicWidthRestore;
                int restoreIconTop = itemView.getTop() + (itemHeight - intrinsicHeighthRestore)/2;
                int restoreIconBottom = restoreIconTop + intrinsicHeighthRestore;
                restoreIcon.setBounds(restoreIconLeft,restoreIconTop,restoreIconRight,restoreIconBottom);
                restoreIcon.draw(c);
                c.drawText(getString(R.string.restore),itemView.getLeft() + iconMargin +restoreIconRight,(((restoreIconTop + restoreIconBottom)/2) - (textPaint.descent()+textPaint.ascent())/2), textPaint );

            }
            //fade out the view
            final float alpha = 1.0f - Math.abs(dX) / (float) viewHolder.itemView.getWidth();//1.0f == ALPHA FULL
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    /*public static void setEdgeEffect(final RecyclerView recyclerView, final int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                final Class<?> clazz = RecyclerView.class;
                for (final String name : new String[] {"ensureTopGlow", "ensureBottomGlow"}) {
                    Method method = clazz.getDeclaredMethod(name);
                    method.setAccessible(true);
                    method.invoke(recyclerView);
                }
                for (final String name : new String[] {"mTopGlow", "mBottomGlow"}) {
                    final Field field = clazz.getDeclaredField(name);
                    field.setAccessible(true);
                    final Object edge = field.get(recyclerView); // android.support.v4.widget.EdgeEffectCompat
                    final Field fEdgeEffect = edge.getClass().getDeclaredField("mEdgeEffect");
                    fEdgeEffect.setAccessible(true);
                    ((EdgeEffect) fEdgeEffect.get(edge)).setColor(color);
                }
            } catch (final Exception ignored) {}
        }
    }*/

    @Override
    public void onBackPressed() {
        ////System.out.println("BACKPRESSED");
        if (isInSelectionMode || isInSearchMode) {
            if (isInSelectionMode) {
                setOutOfSelectionMode();
                ////System.out.println("set1out back");
            } else {
                setOutOfSearchMode();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void setOutOfSearchMode(){
        isInSearchMode = false;
        ////System.out.println("setfase search");
        getSupportLoaderManager().restartLoader(234,null,HistoryActivity.this);
        displayAllNotes();
    }

    public void setOutOfSelectionMode(){
        unSelectAll = false;
        selectAll = false;
        isInSelectionMode = false;
        ////System.out.println("setfase selection");
        selectedId.clear();
        selectedContent.clear();
        if(selectAllBox != null){
            selectAllBox.setVisibility(View.GONE);
        }
        selectionTitle.setText(R.string.history_name);
        toolbar = findViewById(R.id.history_selection_toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.search_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportLoaderManager().restartLoader(234,null,this);
        displayAllNotes();
        toolbar = findViewById(R.id.history_selection_toolbar);
        //toolbar.setVisibility(View.GONE);
        if(selectAllBox != null){
            selectAllBox.setChecked(false);
        }
        //toolbar.show();
        historyList.requestFocus();
    }

    public void restoreSetOfData(){
        CLONESelectedContent.clear();
        for(long id : selectedId){
            restoreData(id);
        }
        final int size = selectedId.size();
        CLONESelectedContent = new ArrayList<>(selectedContent);
        setOutOfSelectionMode();
        ////System.out.println("set1out restore");
        displayAllNotes();
        Snackbar.make(historyView, getString(R.string.notes_restored_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                todosql.deleteTheLastCoupleOnesFromToDo(CLONESelectedContent.size());
                for(String str : CLONESelectedContent){
                    insertData(str);
                }
                displayAllNotes();
            }
        }).show();
    }

    public void deleteSetOfData(){
        CLONESelectedContent.clear();
        for(long id : selectedId){
            deleteData(id);
        }
        CLONESelectedContent = new ArrayList<>(selectedContent);
        setOutOfSelectionMode();
        ////System.out.println("set1out delete");
        displayAllNotes();
        Snackbar.make(historyView, getString(R.string.notes_deleted_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String str : CLONESelectedContent){
                    insertData(str);
                }
                displayAllNotes();
            }
        }).show();
    }

    public void addSelectedId(long id){
        selectedId.add(0,id);
        String data = todosql.getOneDataInHISTORY(Long.toString(id));
        selectedContent.add(0,data);
        toolbar = findViewById(R.id.history_selection_toolbar);
        if(selectedId.size() == 1){
            toolbar.inflateMenu(R.menu.history_selection_mode_menu);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
        }if(selectedId.size() == historyList.getAdapter().getItemCount()){
            selectAll = true;
            unSelectAll = false;
            selectAllBox.setChecked(true);
        }
        String count = Integer.toString(selectedId.size());
        selectionTitle.setText(count + getString(R.string.selection_mode_title));
    }

    public void removeSelectedId(long id){
        selectedId.remove(selectedId.indexOf(id));
        selectAllBox.setChecked(false);
        selectAll = false;
        unSelectAll = true;
        String data = todosql.getOneDataInHISTORY(Long.toString(id));
        selectedContent.remove(selectedContent.indexOf(data));
        if (selectedId.size() == 0) {
            selectionTitle.setText(getString(R.string.selection_mode_empty_title));
            toolbar = findViewById(R.id.history_selection_toolbar);
            toolbar.getMenu().clear();
        }else {
            toolbar = findViewById(R.id.selection_toolbar);
            String count = Integer.toString(selectedId.size());
            selectionTitle.setText(count + getString(R.string.selection_mode_title));
        }
    }

    private void query(String text) {
        Bundle bundle = new Bundle();
        bundle.putString("QUERY", text);
        searchText = text;
        getSupportLoaderManager().restartLoader(234, bundle, HistoryActivity.this);
    }

    public void handleVoiceSearch(Intent intent){
        //onSearchRequested();
        final String query = intent.getStringExtra(SearchManager.QUERY);
        if(query == null){
            return;
        }
        if(query.trim().equals("")){
            return;
        }else {
            //Toast.makeText(getApplicationContext(),query,Toast.LENGTH_SHORT).show();
            searchView.setQuery(query,false);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    query(query);
                }
            },1);// MAGIC TRICK THAT AVOIDS PROBLEMS, I assume the voice function paused the main thread for a little while, so my restartLoader didn't work? Fucking android, I spent 3 fucking hours for this shitty bug and now this magic trick saved me again, WOOHOO!
        }
    }

    /*@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleVoiceSearch(intent);
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.todo_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconifiedByDefault(true);
        Spannable hintText = new SpannableString(getString(R.string.search_hint));
        if(ColorUtils.determineBrightness(themeColor) < 0.5){//dark themeColorSetting
            hintText.setSpan( new ForegroundColorSpan(Color.parseColor("#7FFFFFFF")), 0, hintText.length(), 0 );
        }else {//light themeColorSetting
            hintText.setSpan( new ForegroundColorSpan(Color.parseColor("#61000000")), 0, hintText.length(), 0 );
        }
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
                    ////System.out.println("set1out menu collapse");
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
                query(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                query(newText);
                return false;
            }

        });
        return true;
    }

    /*
    public void displaySearchResults(final String filter){
        final Cursor cursor = databaseManager.getHistorySearchResults(filter);
        if(cursor.getCount() == 0){
            emptyHistory.setVisibility(View.VISIBLE);
            emptyHistory.setText(R.string.empty_search_result);
            historyList.removeAllViewsInLayout();//remove all items
            historyList.setAdapter(null);
        } else {
            emptyHistory.setVisibility(View.GONE);
            emptyHistory.setText("");
            final TodoListAdapter historyListAdapter = (new TodoListAdapter(cursor){
                @Override
                public void onBindViewHolder(TodoViewHolder holder, Cursor cursor) {
                    super.onBindViewHolder(holder, cursor);
                    CheckBox multiSelectionBox = holder.cBox;
                    TextView todoText = holder.todoText;
                    String cursorText = cursor.getString(cursor.getColumnIndex(TITLE));
                    int startPos = cursorText.toLowerCase(Locale.US).indexOf(filter.toLowerCase(Locale.US));
                    int endPos = startPos + filter.length();
                    todoText.setTextColor(textColorSetting);
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
                                    ,themeColorSetting //enabled
                            }
                    );
                    ////System.out.println("null called");
                    if(isInSelectionMode){
                        multiSelectionBox.setVisibility(View.VISIBLE);
                        multiSelectionBox.setBackgroundColor(backgroundColorSetting);
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
                }
            });
            historyList.setAdapter(historyListAdapter);
        }
    }*/

    String timestr = "";
    int expireTime = 60*24;
    public void deleteExpiredNotes(){
        if(!sharedPreferences.getBoolean("clear_history_switch_key",false)){//see auto delete state /off
            displayAllNotes();
        }
        else {//on
            Cursor cs = todosql.getHistory();
            if(cs.getCount()==0) {
                ////System.out.println();
            }
            else {
                expireTime=sharedPreferences.getInt(getString(R.string.clear_interval_value_key),60*24);
                while(cs.moveToNext()){
                    timestr = cs.getString(cs.getColumnIndex("datetime(deleted_timestamp,'localtime')"));
                    //////System.out.println(String.valueOf(databaseManager.getTimeDifference(timestr)));
                    if(todosql.getTimeDifference(timestr)>=expireTime){//if bigger than set value, delete it!
                        deleteData(cs.getInt(cs.getColumnIndex(ID)));
                    }
                }
            }
            cs.close();
            displayAllNotes();
        }
    }

    public void insertData(String title){
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE,title);
        getContentResolver().insert(DatabaseContract.Item.HISTORY_URI,contentValues);
    }

    public void restoreData(long id){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "restore_notes");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "restore notes");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        ContentValues cv = new ContentValues();
        String data = todosql.getOneDataInHISTORY(Long.toString(id));
        cv.put(TITLE,data);
        deleteData(id);
        getContentResolver().insert(DatabaseContract.Item.TODO_URI, cv);
    }

    public void deleteData(long id){
        Uri uri = ContentUris.withAppendedId(DatabaseContract.Item.HISTORY_URI, id);
        getContentResolver().delete(uri, null, null);
        //historyList.getAdapter().notifyDataSetChanged();
        getSupportLoaderManager().restartLoader(234,null,this);
        displayAllNotes();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "delete_history");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "delete history");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort = null;
        setHistoryColorsPreferences();
        if(sharedPreferences.getBoolean(getString(R.string.order_key),true)){
            sort = "_id DESC";
        }
        if (args != null) {
            String[] selectionArgs = new String[]{"%" + args.getString("QUERY") + "%"};
            return new CursorLoader(this, DatabaseContract.Item.HISTORY_URI, PROJECTION, SELECTION, selectionArgs, sort);
        }
        return new CursorLoader(this, DatabaseContract.Item.HISTORY_URI, PROJECTION, null, null, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        emptyHistory.setVisibility(View.VISIBLE);
        //Toast.makeText(getApplicationContext(),String.valueOf(data.getCount()) + String.valueOf(isInSearchMode),Toast.LENGTH_LONG).show();
        if(data.getCount() == 0 && isInSearchMode){
            emptyHistory.setVisibility(View.VISIBLE);
            emptyHistory.setText(getString(R.string.empty_search_result));
        }else if(data.getCount() == 0 && !isInSearchMode){
            emptyHistory.setVisibility(View.VISIBLE);
            emptyHistory.setText(R.string.empty_history);
        }else {
            emptyHistory.setVisibility(View.GONE);
            emptyHistory.setText("");
        }
        historyListAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        historyListAdapter.changeCursor(null);
    }
}
