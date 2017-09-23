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
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
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
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import static com.jackz314.todo.R.color.colorPrimary;
import static com.jackz314.todo.dtb.ID;
import static com.jackz314.todo.dtb.TITLE;

public class HistoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    dtb todosql;
    TextView emptyHistory, selectionTitle;
    RecyclerView historyList;
    ActionBar toolbar;
    int themeColor,textColor,backgroundColor,textSize;
    SharedPreferences sharedPreferences;
    private FirebaseAnalytics mFirebaseAnalytics;
    ColorUtils colorUtils;
    boolean isInSearchMode =false, isInSelectionMode = false;
    Toolbar selectionToolBar;
    boolean selectAll = false, unSelectAll = false;
    ArrayList<Long> selectedId = new ArrayList<>();
    ArrayList<String> selectedContent = new ArrayList<>();
    public ArrayList<String> CLONESelectedContent = new ArrayList<>();
    public String searchText;
    MenuItem searchViewItem;
    SearchView searchView;
    CheckBox selectAllBox, multiSelectionBox;
    private static final String[] PROJECTION = new String[]{ID, TITLE};
    private static final String SELECTION = TITLE + " LIKE ?";
    public static int RESTORE_CONTEXT_ID = 1;
    ConstraintLayout historyView;
    public static int DELETE_HISTORY_CONTEXT_ID = 2;
    TodoListAdapter historyListAdapter;
    MainActivity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*sharedPreferences) = sharedPreferences("settings_data",MODE_PRIVATE);
        themeColor=sharedPreferences).getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorPrimary));
        textColor=sharedPreferences).getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColor=sharedPreferences).getInt(getString(R.string.theme_color_key),Color.WHITE);*/
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_history);
        todosql = new dtb(this);
        selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
        setSupportActionBar(selectionToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        historyList = (RecyclerView) findViewById(R.id.historyList);
        historyList.setHasFixedSize(true);

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

        ItemClickSupport.addTo(historyList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                long id = historyList.getAdapter().getItemId(position);
                unSelectAll = false;
                selectAll = false;
                if (isInSelectionMode) {
                    multiSelectionBox = (CheckBox)v.findViewById(R.id.multiSelectionBox);
                    if(multiSelectionBox.isChecked()){
                        removeSelectedId(id);
                        //System.out.println("false" + id);
                        multiSelectionBox.setChecked(false);
                    }else {
                        addSelectedId(id);
                        multiSelectionBox.setChecked(true);
                        //System.out.println("true" + id);
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
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(300);
                unSelectAll = false;
                selectAll = false;
                if(isInSelectionMode){
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("ToDo", todosql.getOneDataInHISTORY(String.valueOf(id)));
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(view,getString(R.string.todo_copied),Snackbar.LENGTH_SHORT).show();
                }else {
                    setOutOfSelectionMode();
                    //multiSelectionBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
                    //multiSelectionBox.setChecked(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
                    selectionToolBar.getMenu().clear();
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
                    isInSelectionMode = true;
                    //System.out.println(isInSelectionMode + "isInselectionmode");
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
                    //selectAllBox.getButtonDrawable().setColorFilter(themeColor, PorterDuff.Mode.DST); //API>=23 (Android 6.0)
                    selectAllBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!selectAllBox.isChecked()){//uncheck all
                                selectAllBox.setChecked(false);
                                selectedId.clear();
                                selectedContent.clear();
                                unSelectAll = true;
                                //historyList.getAdapter().notifyDataSetChanged();
                                selectionTitle.setText(getString(R.string.selection_mode_empty_title));
                                selectionToolBar.getMenu().clear();
                            }else if(selectAllBox.isChecked()){//check all
                                selectAllBox.setChecked(true);
                                selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
                                if(selectedId.size() == 0){
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
                                }

                                long id;
                                selectedId.clear();
                                selectedContent.clear();
                                selectAll = true;
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
                    addSelectedId(id);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            multiSelectionBox =(CheckBox)view.findViewById(R.id.multiSelectionBox);
                            multiSelectionBox.setChecked(true);
                        }
                    }, 1);//to solve the problem that the checkbox is not checked with no delay
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                }
                return true;
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
        sharedPreferences = getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor=sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(colorPrimary));
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
        int[] colors = {0,colorUtils.lighten(textColor,0.3),0};
        //divider
    }

    public void displayAllNotes(){
        if(historyList.getAdapter() == null){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            historyList.setLayoutManager(linearLayoutManager);
            historyListAdapter = (new TodoListAdapter(null){
                @Override
                public void onBindViewHolder(TodoViewHolder holder, Cursor cursor) {
                    super.onBindViewHolder(holder, cursor);
                    final long id = cursor.getInt(cursor.getColumnIndex(dtb.ID));
                    String text = cursor.getString(cursor.getColumnIndex(dtb.TITLE));
                    holder.todoText.setTextColor(textColor);
                    holder.cardView.setCardBackgroundColor(colorUtils.darken(backgroundColor,0.01));
                    holder.todoText.setTextSize(textSize);
                    if(isInSearchMode){
                        if(cursor.getCount() == 0){
                            emptyHistory.setVisibility(View.VISIBLE);
                            emptyHistory.setText(getString(R.string.empty_search_result));
                        }else {
                            emptyHistory.setVisibility(View.GONE);
                            emptyHistory.setText("");
                            Spannable spannable = new SpannableString(text);
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
                                holder.todoText.setText(spannable);
                            }
                            //spannable.setSpan(new TextAppearanceSpan(null,Typeface.BOLD,-1,new ColorStateList(new int[][] {new int[] {}},new int[] {Color.parseColor("#ef5350")}),null), startPos, startPos + searchTextLow.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }else {
                        holder.todoText.setText(text);
                    }
                    //System.out.println(isInSelectionMode + "DISPLAYALLNOTES");
                    if(isInSelectionMode){
                        holder.cBox.setVisibility(View.VISIBLE);
                        if(selectAll){
                            holder.cBox.setChecked(true);
                        }
                        if (unSelectAll){
                            holder.cBox.setChecked(false);
                        }
                        //Toast.makeText(getApplicationContext(),"SELECTIONMODE",Toast.LENGTH_SHORT).show();
                    }else {
                        holder.cBox.setChecked(false);
                        holder.cBox.setVisibility(View.GONE);
                    }
                    //System.out.println(text+"|cursor read");
                    holder.cBox.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            CheckBox cb = (CheckBox) v.findViewById(R.id.multiSelectionBox);
                            unSelectAll = false;
                            selectAll = false;
                            if (cb.isChecked()) {
                                    addSelectedId(id);
                                //System.out.println("checked " + id);
                                // do some operations here
                            } else if (!cb.isChecked()) {
                                //System.out.println("unchecked " + id);

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
            ////System.out.println("empty history!");
            emptyHistory.setVisibility(View.VISIBLE);
            emptyHistory.setText(R.string.empty_history);
            historyList.removeAllViewsInLayout();//remove all items
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
            Paint textPaint = new Paint();
            textPaint.setStrokeWidth(2);
            textPaint.setTextSize(80);
            textPaint.setColor(themeColor);
            textPaint.setTextAlign(Paint.Align.LEFT);
            Rect bounds = new Rect();
            textPaint.getTextBounds(getString(R.string.delete),0,getString(R.string.finish).length(), bounds);
            Drawable deleteIcon = ContextCompat.getDrawable(HistoryActivity.this, R.drawable.ic_delete_black_24dp);//draw finish icon
            Drawable restoreIcon = ContextCompat.getDrawable(HistoryActivity.this,R.drawable.ic_restore_black_24dp);
            restoreIcon.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
            deleteIcon.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
            int iconMargin = 34;
            int itemHeight = itemView.getBottom() - itemView.getTop();
            int intrinsicWidthRestore = restoreIcon.getIntrinsicWidth();
            int intrinsicHeighthRestore = restoreIcon.getIntrinsicHeight();
            int restoreIconLeft = itemView.getLeft() + iconMargin;
            int restoreIconRight = itemView.getLeft() + iconMargin + intrinsicWidthRestore;
            int restoreIconTop = itemView.getTop() + (itemHeight - intrinsicHeighthRestore)/2;
            int restoreIconBottom = restoreIconTop + intrinsicHeighthRestore;
            restoreIcon.setBounds(restoreIconLeft,restoreIconTop,restoreIconRight,restoreIconBottom);
            int intrinsicWidth = deleteIcon.getIntrinsicWidth();
            int intrinsicHeight = deleteIcon.getIntrinsicWidth();
            int deleteIconLeft = itemView.getRight() - iconMargin - intrinsicWidth - bounds.width();
            int deleteIconRight = itemView.getRight() - iconMargin - bounds.width();
            int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
            int deleteIconBottom = deleteIconTop + intrinsicHeight;
            deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
            if(dX < 0){
                deleteIcon.draw(c);
                c.drawText(getString(R.string.delete),(float) itemView.getRight() - iconMargin - bounds.width() ,(((deleteIconTop + deleteIconBottom)/2) - (textPaint.descent()+textPaint.ascent())/2), textPaint);
            }
            if(dX > 0){
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

    @Override
    public void onBackPressed() {
        //System.out.println("BACKPRESSED");
        if (isInSelectionMode || isInSearchMode) {
            if (isInSelectionMode) {
                setOutOfSelectionMode();
                //System.out.println("set1out back");
            } else {
                setOutOfSearchMode();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void setOutOfSearchMode(){
        isInSearchMode = false;
        //System.out.println("setfase search");
        getSupportLoaderManager().restartLoader(234,null,HistoryActivity.this);
        displayAllNotes();
    }

    public void setOutOfSelectionMode(){
        unSelectAll = false;
        selectAll = false;
        isInSelectionMode = false;
        //System.out.println("setfase selection");
        selectedId.clear();
        selectedContent.clear();
        if(selectAllBox != null){
            selectAllBox.setVisibility(View.GONE);
        }
        selectionTitle.setText(R.string.history_name);
        selectionToolBar = (Toolbar)findViewById(R.id.history_selection_toolbar);
        selectionToolBar.getMenu().clear();
        selectionToolBar.inflateMenu(R.menu.search_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportLoaderManager().restartLoader(234,null,this);
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
            restoreData(id);
        }
        final int size = selectedId.size();
        CLONESelectedContent = new ArrayList<>(selectedContent);
        setOutOfSelectionMode();
        //System.out.println("set1out restore");
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
        //System.out.println("set1out delete");
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
        }if(selectedId.size() == historyList.getAdapter().getItemCount()){
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleVoiceSearch(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.todo_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        Spannable hintText = new SpannableString(getString(R.string.search_hint));
        if(ColorUtils.determineBrightness(themeColor) < 0.5){//dark themeColor
            hintText.setSpan( new ForegroundColorSpan(Color.parseColor("#7FFFFFFF")), 0, hintText.length(), 0 );
        }else {//light themeColor
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
                    //System.out.println("set1out menu collapse");
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
        final Cursor cursor = todosql.getHistorySearchResults(filter);
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
                    //System.out.println("null called");
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
                //System.out.println();
            }
            else {
                expireTime=sharedPreferences.getInt(getString(R.string.clear_interval_value_key),60*24);
                while(cs.moveToNext()){
                    timestr = cs.getString(cs.getColumnIndex("datetime(deleted_timestamp,'localtime')"));
                    ////System.out.println(String.valueOf(todosql.getTimeDifference(timestr)));
                    if(todosql.getTimeDifference(timestr)>=expireTime){//if bigger than set value, delete it!
                        deleteData(cs.getInt(cs.getColumnIndex(todosql.ID)));
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
        getContentResolver().insert(AppContract.Item.HISTORY_URI,contentValues);
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
        getContentResolver().insert(AppContract.Item.TODO_URI, cv);
    }

    public void deleteData(long id){
        Uri uri = ContentUris.withAppendedId(AppContract.Item.HISTORY_URI, id);
        getContentResolver().delete(uri, null, null);
        //historyList.getAdapter().notifyDataSetChanged();
        //getSupportLoaderManager().restartLoader(234,null,this);
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
            return new CursorLoader(this, AppContract.Item.HISTORY_URI, PROJECTION, SELECTION, selectionArgs, sort);
        }
        return new CursorLoader(this, AppContract.Item.HISTORY_URI, PROJECTION, null, null, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
