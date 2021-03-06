package com.jackz314.todo;

import android.animation.LayoutTransition;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jackz314.colorpicker.ColorPickerView;
import com.jackz314.colorpicker.builder.ColorPickerClickListener;
import com.jackz314.colorpicker.builder.ColorPickerDialogBuilder;
import com.jackz314.todo.utils.ColorUtils;

import static com.jackz314.todo.DatabaseManager.ID;
import static com.jackz314.todo.DatabaseManager.TAG;
import static com.jackz314.todo.DatabaseManager.TAG_COLOR;
import static com.jackz314.todo.SetEdgeColor.setEdgeColor;


public class TagSelectionActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    TodoListAdapter tagListAdapter;
    RecyclerView tagList;
    int themeColor,textColor,backgroundColor,textSize;
    SharedPreferences sharedPreferences;
    Toolbar toolbar;
    CoordinatorLayout main;
    boolean isInSearchMode = false;
    private static final String[] PROJECTION = new String[]{ID, TAG, TAG_COLOR};
    private static final String SELECTION = TAG + " LIKE ?";
    public String searchText;
    TextView emptyTextView;
    int doubleClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_selection);
        toolbar = findViewById(R.id.tags_sel_toolbar);
        tagList = findViewById(R.id.tag_sel_list);
        emptyTextView = findViewById(R.id.emptyTagSelection);
        setSupportActionBar(toolbar);
        main = findViewById(R.id.tagsel_main);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException ignored){
            //ignore
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
                    tagList.smoothScrollToPosition(0);//smooth scroll to top
                }
            }
        });
        setColorPreferences();
        displayAllNotes();
        setEdgeColor(tagList,themeColor);
        ItemClickSupport.addTo(tagList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, final int position, final View view) {
                //go to the specific tag, pass on the tag value here
                String tag = tagListAdapter.getItemContent(position,TAG);
                Intent tagIntent = new Intent(TagSelectionActivity.this, TagsActivity.class);
                tagIntent.putExtra("TAG_VALUE",tag);
                startActivity(tagIntent);
            }
        });

        tagList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                setEdgeColor(tagList,themeColor);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                setEdgeColor(tagList,themeColor);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tagList.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    setEdgeColor(tagList,themeColor);
                }
            });
        }else {
            tagList.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    setEdgeColor(tagList,themeColor);
                }
            });
        }
    }


    public void displayAllNotes(){
        if(tagList.getAdapter() == null){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            tagList.setLayoutManager(linearLayoutManager);
            tagListAdapter = (new TodoListAdapter(){

                @NonNull
                @Override
                public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {//override creating method to inflate from a different layout
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_selection_list_item,parent,false);
                    //System.out.println("|cursor created");
                    return new TodoViewHolder(view);
                }

                @Override
                public void onBindViewHolder(final TodoViewHolder holder, Cursor cursor) {
                    super.onBindViewHolder(holder, cursor);
                    //Toast.makeText(getApplicationContext(),"SD",Toast.LENGTH_LONG).show();
                    final long id = cursor.getInt(cursor.getColumnIndex(DatabaseManager.ID));
                    String text = cursor.getString(cursor.getColumnIndex(DatabaseManager.TAG));//get the text of the note
                    final String tagColor = cursor.getString(cursor.getColumnIndex(DatabaseManager.TAG_COLOR));
                    holder.tagText.setTextColor(textColor);
                    ColorFilter tagDotColorFilter = new PorterDuffColorFilter(Color.parseColor(tagColor), PorterDuff.Mode.MULTIPLY);
                    holder.tagDot.getBackground().setColorFilter(tagDotColorFilter);
                    holder.tagText.setTextSize(textSize);
                    holder.tagDot.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog colorPicker = ColorPickerDialogBuilder
                                    .with(TagSelectionActivity.this)
                                    .setTitle(getString(R.string.tag_color_selector))
                                    .initialColor(Color.parseColor(tagColor))
                                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                                    .density(12)
                                    .setPositiveButton(getString(R.string.finish), new ColorPickerClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, final int color, Integer[] integers) {
                                            if(ColorUtils.determineSimilarColor(color,textColor)>0.9){//if newly selected tag color is similar to text color, ask again to confirm the choice
                                                final AlertDialog dialog = new AlertDialog.Builder(TagSelectionActivity.this).setTitle(R.string.warning_title)
                                                        .setMessage(R.string.color_conflict)
                                                        .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                ContentValues values = new ContentValues();
                                                                String hexColor = String.format("#%06x", 0xFFFFFF & color);// format it as hexadecimal string (with hashtag and leading zeros)
                                                                values.put(TAG_COLOR, hexColor);
                                                                Uri uri = ContentUris.withAppendedId(DatabaseContract.Item.TODO_URI, id);
                                                                getContentResolver().update(uri, values, null, null);
                                                            }
                                                        }).setNegativeButton(R.string.reconsider, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                //empty
                                                            }
                                                        }).show();
                                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColor);
                                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColor);
                                            }else{
                                                ContentValues values = new ContentValues();
                                                String hexColor = String.format("#%06x", 0xFFFFFF & color);// format it as hexadecimal string (with hashtag and leading zeros)
                                                values.put(TAG_COLOR, hexColor);
                                                Uri uri = ContentUris.withAppendedId(DatabaseContract.Item.TAGS_URI, id);
                                                getContentResolver().update(uri, values, null, null);
                                            }
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).showColorEdit(true).showColorPreview(true).lightnessSliderOnly().setColorEditTextColor(Color.parseColor(tagColor)).build();
                            colorPicker.show();
                            colorPicker.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(themeColor);
                            colorPicker.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(themeColor);
                            holder.tagText.setTextColor(textColor);//refresh the tag selection list colors
                            ColorFilter tagDotColorFilter = new PorterDuffColorFilter(Color.parseColor(tagColor), PorterDuff.Mode.MULTIPLY);
                            holder.tagDot.getBackground().setColorFilter(tagDotColorFilter);
                        }
                    });
                    Spannable spannable = new SpannableString(text);
                    //Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
                    if(isInSearchMode){
                        //ColorStateList highlightColor = new ColorStateList(new int[][] { new int[] {}}, new int[] { Color.parseColor("#ef5350") });
                        String textLow = text.toLowerCase();
                        String searchTextLow = searchText.toLowerCase();
                        int startPos = textLow.indexOf(searchTextLow);
                        if(startPos <0){
                            return;
                        }
                        if(!(startPos <0)){
                            do{
                                int start = Math.min(startPos, textLow.length());
                                int end = Math.min(startPos + searchTextLow.length(), textLow.length());
                                startPos = textLow.indexOf(searchTextLow,end);
                                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//set searched text to bold
                            }while (startPos > 0);
                        }
                    }
                    spannable.setSpan(new TextAppearanceSpan(null,Typeface.NORMAL,-1,new ColorStateList(new int[][] {new int[] {}},
                            new int[] {Color.parseColor(tagColor)})
                            ,null), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.tagText.setText(spannable);
                }
            });
            tagList.setAdapter(tagListAdapter);
            getSupportLoaderManager().initLoader(1010, null, this);
            //ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            //mItemTouchHelper.attachToRecyclerView(tagList);
        }
        getSupportLoaderManager().restartLoader(1010,null,this);
    }

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

    public void setColorPreferences() {
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data", MODE_PRIVATE);
        themeColor = sharedPreferences.getInt(getString(R.string.theme_color_key), getResources().getColor(R.color.colorActualPrimary));
        textColor = sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        textSize = sharedPreferences.getInt(getString(R.string.text_size_key), 24);
        backgroundColor = sharedPreferences.getInt(getString(R.string.background_color_key), Color.WHITE);
        toolbar.setBackgroundColor(themeColor);
        main.setBackgroundColor(backgroundColor);
        Window window = this.getWindow();
        window.setStatusBarColor(themeColor);
        window.setNavigationBarColor(themeColor);
        toolbar.setBackgroundColor(themeColor);
        if (ColorUtils.determineBrightness(backgroundColor) < 0.5) {// dark
            //   emptyTextView.setTextColor(Color.parseColor("#7FFFFFFF"));
        } else {//bright
            //     emptyTextView.setTextColor(Color.parseColor("#61000000"));

        }
        tagList.setBackgroundColor(backgroundColor);
        //  navigationView.setBackgroundColor(backgroundColorSetting);
        //View listView = LayoutInflater.from(TagsActivity.this).inflate(R.layout.todo_list_item, null);
        if (ColorUtils.determineBrightness(backgroundColor) < 0.5) {// dark
            // input.setHintTextColor(ColorUtils.makeTransparent(textColorSetting, 0.5));
        } else {
            //  input.setHintTextColor(ColorUtils.makeTransparent(textColorSetting, 0.38));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        setColorPreferences();
        //determine order of the list
        String sort = null;
        if(sharedPreferences.getBoolean(getString(R.string.order_key),true)){
            sort = "_id DESC";
        }
        if (bundle != null) {
            String[] selectionArgs = new String[]{"%" + bundle.getString("QUERY") + "%"};
            return new CursorLoader(this, DatabaseContract.Item.TAGS_URI,PROJECTION, SELECTION, selectionArgs, sort);
        }
        return new CursorLoader(this, DatabaseContract.Item.TAGS_URI, PROJECTION, null, null, sort);    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() == 0 && isInSearchMode){
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(getString(R.string.empty_search_result));
        }else if(data.getCount() == 0 && !isInSearchMode){
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.empty_tag_selection);
        }else {
            emptyTextView.setVisibility(View.GONE);
            emptyTextView.setText("");
        }
        tagListAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        tagListAdapter.changeCursor(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.todo_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        LinearLayout searchBar = searchView.findViewById(R.id.search_bar);
        searchBar.setLayoutTransition(new LayoutTransition());
        Spannable hintText = new SpannableString(getString(R.string.search_hint));
        if(ColorUtils.determineBrightness(themeColor) < 0.5){//dark themeColorSetting
            hintText.setSpan( new ForegroundColorSpan(Color.parseColor("#7FFFFFFF")), 0, hintText.length(), 0 );//material design standard hint text color in dark themed background
        }else {//light themeColorSetting
            hintText.setSpan( new ForegroundColorSpan(Color.parseColor("#61000000")), 0, hintText.length(), 0 );//material design standard hint text color in light themed background
        }
        searchView.setQueryHint(hintText);
        MenuItem searchMenuIem = menu.findItem(R.id.todo_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuIem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isInSearchMode = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                setOutOfSearchMode();
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

    public void setOutOfSearchMode(){
        isInSearchMode = false;
        getSupportLoaderManager().restartLoader(1010,null,this);
        displayAllNotes();
        hideKeyboard();
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public void showKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imManager.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void query(String text) {//launch search
        Bundle bundle = new Bundle();
        bundle.putString("QUERY", text);
        searchText = text;
        //System.out.println("calledquery" + " " + text);
        getSupportLoaderManager().restartLoader(1010, bundle, TagSelectionActivity.this);
    }

}
