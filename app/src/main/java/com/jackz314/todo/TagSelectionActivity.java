package com.jackz314.todo;

import android.*;
import android.animation.LayoutTransition;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.EdgeEffect;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.jackz314.todo.dtb.ID;
import static com.jackz314.todo.dtb.TAG;
import static com.jackz314.todo.dtb.TAG_COLOR;
import static com.jackz314.todo.dtb.TODO_TABLE;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_selection);
        toolbar = (Toolbar) findViewById(R.id.tags_selection_toolbar);
        tagList = (RecyclerView)findViewById(R.id.tag_selection_list);
        emptyTextView = (TextView)findViewById(R.id.emptyTagSelection);
        setSupportActionBar(toolbar);
        main = (CoordinatorLayout)findViewById(R.id.tags_selection_main);

        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException ignored){
            //ignore
        }
        setColorPreferences();
        displayAllNotes();
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
    }

    public void displayAllNotes(){
        if(tagList.getAdapter() == null){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            tagList.setLayoutManager(linearLayoutManager);
            tagListAdapter = (new TodoListAdapter(null){

                @Override
                public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {//override creating method to inflate from a different layout
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectionlist,parent,false);
                    //System.out.println("|cursor created");
                    return new TodoViewHolder(view);
                }

                @Override
                public void onBindViewHolder(TodoViewHolder holder, Cursor cursor) {
                    super.onBindViewHolder(holder, cursor);
                    //Toast.makeText(getApplicationContext(),"SD",Toast.LENGTH_LONG).show();
                    final long id = cursor.getInt(cursor.getColumnIndex(dtb.ID));
                    String text = cursor.getString(cursor.getColumnIndex(dtb.TAG));//get the text of the note
                    String tagColor = cursor.getString(cursor.getColumnIndex(dtb.TAG_COLOR));
                    holder.tagText.setTextColor(textColor);
                    ColorFilter tagDotColorFilter = new PorterDuffColorFilter(Color.parseColor(tagColor), PorterDuff.Mode.MULTIPLY);
                    holder.tagDot.getBackground().setColorFilter(tagDotColorFilter);
                    holder.tagText.setTextSize(textSize);
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

    public static void setEdgeEffect(final RecyclerView recyclerView, final int color) {
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
    }

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
        //  navigationView.setBackgroundColor(backgroundColor);
        //View listView = LayoutInflater.from(TagsActivity.this).inflate(R.layout.todolist, null);
        if (ColorUtils.determineBrightness(backgroundColor) < 0.5) {// dark
            // input.setHintTextColor(ColorUtils.makeTransparent(textColor, 0.5));
        } else {
            //  input.setHintTextColor(ColorUtils.makeTransparent(textColor, 0.38));
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
            return new CursorLoader(this, AppContract.Item.TAGS_URI,PROJECTION, SELECTION, selectionArgs, sort);
        }
        return new CursorLoader(this, AppContract.Item.TAGS_URI, PROJECTION, null, null, sort);    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() == 0 && isInSearchMode){
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(getString(R.string.empty_search_result));
        }else if(data.getCount() == 0 && !isInSearchMode){
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.empty_todolist);
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
        LinearLayout searchBar = (LinearLayout) searchView.findViewById(R.id.search_bar);
        searchBar.setLayoutTransition(new LayoutTransition());
        Spannable hintText = new SpannableString(getString(R.string.search_hint));
        if(ColorUtils.determineBrightness(themeColor) < 0.5){//dark themeColor
            hintText.setSpan( new ForegroundColorSpan(Color.parseColor("#7FFFFFFF")), 0, hintText.length(), 0 );//material design standard hint text color in dark themed background
        }else {//light themeColor
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
