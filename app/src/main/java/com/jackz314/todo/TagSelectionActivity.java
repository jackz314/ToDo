package com.jackz314.todo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.jackz314.todo.dtb.ID;
import static com.jackz314.todo.dtb.TAG;
import static com.jackz314.todo.dtb.TAG_COLOR;


public class TagSelectionActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    TodoListAdapter tagListAdapter;
    RecyclerView tagList;
    int themeColor,textColor,backgroundColor,textSize;
    SharedPreferences sharedPreferences;
    FloatingActionButton fab;
    EditText input;
    CheckBox selectAllBox, multiSelectionBox;
    Toolbar toolbar;
    CoordinatorLayout main;
    boolean isInSearchMode = false;
    private static final String[] PROJECTION = new String[]{ID, TAG};
    private static final String SELECTION = TAG + " LIKE ?";
    public String searchText;
    TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tags_selection_toolbar);
        setSupportActionBar(toolbar);
        main = (CoordinatorLayout)findViewById(R.id.tags_selection_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ItemClickSupport.addTo(tagList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, final int position, final View view) {

            }
        });
    }

    public void displayAllNotes(){
        if(!(tagList == null)){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            tagList.setLayoutManager(linearLayoutManager);
            tagListAdapter = (new TodoListAdapter(null){
                @Override
                public void onBindViewHolder(TodoViewHolder holder, Cursor cursor) {
                    super.onBindViewHolder(holder, cursor);
                    final long id = cursor.getInt(cursor.getColumnIndex(dtb.ID));
                    String text = cursor.getString(cursor.getColumnIndex(dtb.TAG));//get the text of the note
                    String tagColor = cursor.getString(cursor.getColumnIndex(dtb.TAG_COLOR));
                    holder.todoText.setTextColor(textColor);
                    holder.cardView.setCardBackgroundColor(ColorUtils.darken(backgroundColor,0.01));
                    holder.todoText.setTextSize(textSize);
                    if(isInSearchMode){
                        Spannable spannable = new SpannableString(text);
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
                                spannable.setSpan(new TextAppearanceSpan(null, Typeface.BOLD,-1,
                                        new ColorStateList(new int[][] {new int[] {}},
                                                new int[] {Color.parseColor("#ef5350")})
                                        ,null), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//highlight searched text
                            }while (startPos > 0);
                            holder.todoText.setText(spannable);
                        }
                    }
                }
            });
        }
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

    public static void setCursorColor(EditText view, int color) {//REFLECTION USED
        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(view);

            // Get the editor
            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(view);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(view.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception ignored) {

        }
    }

    public void setColorPreferences() {
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data", MODE_PRIVATE);
        themeColor = sharedPreferences.getInt(getString(R.string.theme_color_key), getResources().getColor(R.color.colorActualPrimary));
        textColor = sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        textSize = sharedPreferences.getInt(getString(R.string.text_size_key), 24);
        backgroundColor = sharedPreferences.getInt(getString(R.string.background_color_key), Color.WHITE);
        fab.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        toolbar.setBackgroundColor(themeColor);
        input.setTextColor(textColor);
        input.setTextSize(getResources().getDimension(R.dimen.default_text_size));
        setCursorColor(input, themeColor);
        main.setBackgroundColor(backgroundColor);
        Window window = this.getWindow();
        window.setStatusBarColor(themeColor);
        window.setNavigationBarColor(themeColor);
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
        String sort = null;
        setColorPreferences();
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

}
