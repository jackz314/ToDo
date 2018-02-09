package com.jackz314.todo;

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
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.jackz314.todo.dtb.ID;
import static com.jackz314.todo.dtb.TITLE;

public class TagsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public ArrayList<Long> selectedId = new ArrayList<>();
    public ArrayList<String> selectedContent = new ArrayList<>();
    public ArrayList<String> CLONESelectedContent = new ArrayList<>();
    TodoListAdapter tagListAdapter;
    RecyclerView tagList;
    int themeColor,textColor,backgroundColor,textSize;
    SharedPreferences sharedPreferences;
    FloatingActionButton fab;
    EditText input;
    CheckBox selectAllBox, multiSelectionBox;
    Toolbar toolbar, selectionToolBar;
    TextView modifyId, selectionTitle;
    CoordinatorLayout main;
    dtb todosql;
    String tagName = "";
    boolean isAdd = true;
    boolean selectAll = false, unSelectAll = false, isInSelectionMode = false, isInSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        toolbar = (Toolbar)findViewById(R.id.tags_toolbar);
        setSupportActionBar(toolbar);
        displayAllNotes();
        todosql = new dtb(this);
        fab = (FloatingActionButton)findViewById(R.id.tags_fab);
        input = (EditText)findViewById(R.id.tags_input);
        tagList = (RecyclerView)findViewById(R.id.taglist);
        main = (CoordinatorLayout)findViewById(R.id.tags_main);
        modifyId = (TextView)findViewById(R.id.motify_tag_id);
        tagName = determineTag();//determine tag name
        try{
            getSupportActionBar().setTitle(tagName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException ignored){
            //ignored
        }
        setColorPreferences();
        ItemClickSupport.addTo(tagList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, final int position, final View view) {
                long id = tagListAdapter.getItemId(position);
                unSelectAll = false;
                selectAll = false;
                if (isInSelectionMode) {
                    multiSelectionBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
                    if(multiSelectionBox.isChecked()){
                        removeSelectedId(id);
                        multiSelectionBox.setChecked(false);
                        //System.out.println("false" + id);
                    }else {
                        addSelectedId(id);
                        multiSelectionBox.setChecked(true);
                        //System.out.println("true" + id);

                    }
                }else {
                    if(isInSearchMode){
                        setOutOfSearchMode();
                    }
                    modifyId.setText(String.valueOf(id));
                    if(isAdd){
                        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_plus_to_send); // Insert your AnimatedVectorDrawable resource identifier
                        fab.setImageDrawable(d);
                        isAdd = false;
                        d.start();
                    }
                    input.setVisibility(View.VISIBLE);
                    input.setText(todosql.getOneDataInTODO(String.valueOf(id)));
                    input.requestFocus();
                    input.setSelection(input.getText().length());
                    showKeyboard();
                    int top = view.getTop();
                    tagList.smoothScrollBy(0,top);//scroll the clicked item to top
                    //Toast.makeText(getApplicationContext(),String.valueOf(position),Toast.LENGTH_LONG).show();
                    //handler.postDelayed(r,250);//double click interval
                    //(new Handler()).postDelayed(new Runnable() {
                    //  @Override
                    //   public void run() {
                    //  if(!justDoubleClicked){

                    //   }]
                    // }
                    //}, 250);
                    //justDoubleClicked = false;
                    // }
                }
            }
        });
        ItemClickSupport.addTo(tagList).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, final View view) {
                long id = tagListAdapter.getItemId(position);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(30);
                if(isInSelectionMode){
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("ToDo", todosql.getOneDataInTODO(String.valueOf(id)));
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(main,getString(R.string.todo_copied),Snackbar.LENGTH_LONG).show();
                }else {
                    setOutOfSelectionMode();
                    fab.setVisibility(View.INVISIBLE);
                    input.setVisibility(View.GONE);
                    isInSelectionMode = true;
                    getSupportLoaderManager().restartLoader(123,null,TagsActivity.this);
                    //multiSelectionBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
                    //multiSelectionBox.setChecked(true);
                    displayAllNotes();
                    selectionToolBar = (Toolbar)findViewById(R.id.selection_toolbar);
                    selectionTitle = (TextView)selectionToolBar.findViewById(R.id.selection_toolbar_title);
                    toolbar = (Toolbar) findViewById(R.id.toolbar);
                    toolbar.setVisibility(View.GONE);
                    selectionToolBar.setVisibility(View.VISIBLE);
                    selectionTitle.setText(getString(R.string.selection_mode_title));
                    //Drawable backArrow = getDrawable(R.drawable.ic_close_black_24dp);
                    //selectionToolBar.setNavigationIcon(backArrow);
                    selectionToolBar.setBackgroundColor(themeColor);
                    selectAllBox = (CheckBox)selectionToolBar.findViewById(R.id.select_all_box);
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
                            unSelectAll = false;
                            selectAll = false;
                            if(!selectAllBox.isChecked()){//uncheck all
                                selectAllBox.setChecked(false);
                                selectedId.clear();
                                selectedContent.clear();
                                selectionTitle.setText(getString(R.string.selection_mode_empty_title));
                                selectionToolBar.getMenu().clear();
                                unSelectAll = true;
                                tagList.getAdapter().notifyDataSetChanged();
                            }else if(selectAllBox.isChecked()){//check all
                                selectAllBox.setChecked(true);
                                if(selectedId.size()==0){
                                    selectionToolBar.inflateMenu(R.menu.selection_mode_menu);
                                    selectionToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            if(item.getItemId() == R.id.selection_menu_finish){
                                                finishSetOfData();
                                            }else if(item.getItemId() == R.id.selection_menu_delete){
                                                deleteSetOfData();
                                            }else if (item.getItemId() == R.id.selection_menu_share){
                                                shareSetOfData();
                                            }else if (item.getItemId() == R.id.selection_menu_export){
                                                boolean succ = exportOrPrint();
                                                if (succ){
                                                    Toast.makeText(getApplicationContext(),getString(R.string.export_succeed),Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(getApplicationContext(),getString(R.string.export_failed),Toast.LENGTH_LONG).show();
                                                }
                                            }
                                            return false;
                                        }
                                    });
                                }
                                long id;
                                selectedId.clear();
                                selectedContent.clear();
                                selectAll = true;
                                tagList.getAdapter().notifyDataSetChanged();
                                Cursor cursor = todosql.getData();
                                cursor.moveToFirst();
                                do{
                                    id = cursor.getInt(cursor.getColumnIndex(ID));
                                    selectedId.add(0,id);
                                    String data = todosql.getOneDataInTODO(Long.toString(id));
                                    selectedContent.add(0,data);
                                }while (cursor.moveToNext());
                                String count = Integer.toString(selectedId.size());
                                selectionTitle.setText(count + getString(R.string.selection_mode_title));
                            }
                        }
                    });
                    addSelectedId(id);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            multiSelectionBox =(CheckBox)view.findViewById(R.id.multiSelectionBox);
                            multiSelectionBox.setChecked(true);
                        }
                    }, 1);//to solve the problem that the checkbox is not checked with no delay

                    /*selectionToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setOutOfSelectionMode();
                            //What to do on back clicked
                        }
                    });*/
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                }
                return true;
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {//draw the options after swipe left

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
                unSelectAll = false;
                selectAll = false;
                if(isInSelectionMode && selectedId.contains(viewHolder.getItemId())){
                    removeSelectedId(viewHolder.getItemId());
                }
                final String finishedContent = todosql.getOneDataInTODO(String.valueOf(viewHolder.getItemId()));
                finishData(viewHolder.getItemId());
                Snackbar.make(main, getString(R.string.note_finished_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        insertData(finishedContent);
                        long lastHistoryId = todosql.getIdOfLatestDataInHistory();
                        todosql.deleteFromHistory(String.valueOf(lastHistoryId));
                        displayAllNotes();
                    }
                }).show();
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
                textPaint.getTextBounds(getString(R.string.finish),0,getString(R.string.finish).length(), bounds);
                Drawable finishIcon = ContextCompat.getDrawable(TagsActivity.this, R.drawable.ic_done_black_24dp);//draw finish icon
                finishIcon.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
                int finishIconMargin = 40;
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = finishIcon.getIntrinsicWidth();
                int intrinsicHeight = finishIcon.getIntrinsicWidth();
                int finishIconLeft = itemView.getRight() - finishIconMargin - intrinsicWidth - bounds.width();
                int finishIconRight = itemView.getRight() - finishIconMargin - bounds.width();
                int finishIconTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int finishIconBottom = finishIconTop + intrinsicHeight;
                finishIcon.setBounds(finishIconLeft, finishIconTop, finishIconRight, finishIconBottom);
                finishIcon.draw(c);
                //fade out the view
                final float alpha = 1.0f - Math.abs(dX) / (float) viewHolder.itemView.getWidth();//1.0f == ALPHA FULL
                viewHolder.itemView.setAlpha(alpha);
                viewHolder.itemView.setTranslationX(dX);
                c.drawText(getString(R.string.finish),(float) itemView.getRight() - 34 - bounds.width() ,(((finishIconTop+finishIconBottom)/2) - (textPaint.descent()+textPaint.ascent())/2), textPaint);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
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

    public void addSelectedId(long id){
        selectedId.add(0,id);
        String data = todosql.getOneDataInTODO(Long.toString(id));
        selectedContent.add(0,data);
        selectionToolBar = (Toolbar)findViewById(R.id.selection_toolbar);
        if(selectedId.size() == 1){
            selectionToolBar.inflateMenu(R.menu.selection_mode_menu);
            selectionToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.selection_menu_finish){
                        finishSetOfData();
                    }else if(item.getItemId() == R.id.selection_menu_delete){
                        deleteSetOfData();
                    }else if (item.getItemId() == R.id.selection_menu_share){
                        shareSetOfData();
                    }else if (item.getItemId() == R.id.selection_menu_export){
                        exportOrPrint();
                    }
                    return false;
                }
            });
        }if(selectedId.size() == todosql.getData().getCount()){
            selectAllBox.setChecked(true);
        }
        String count = Integer.toString(selectedId.size());
        selectionTitle.setText(count + getString(R.string.selection_mode_title));
    }

    public void removeSelectedId(long id){
        selectedId.remove(selectedId.indexOf(id));
        String data = todosql.getOneDataInTODO(Long.toString(id));
        selectedContent.remove(selectedContent.indexOf(data));
        if(selectedId.size() < todosql.getData().getCount()){
            selectAllBox.setChecked(false);
        }
        if (selectedId.size() == 0) {
            selectionTitle.setText(getString(R.string.selection_mode_empty_title));
            selectionToolBar.getMenu().clear();
        }else {
            selectionToolBar = (Toolbar)findViewById(R.id.selection_toolbar);
            String count = Integer.toString(selectedId.size());
            selectionTitle.setText(count + getString(R.string.selection_mode_title));
        }
    }

    public void finishSetOfData(){

        CLONESelectedContent.clear();

        for(long id : selectedId){
            finishData(id);
        }
        final int size = selectedId.size();
        CLONESelectedContent = new ArrayList<>(selectedContent);
        setOutOfSelectionMode();
        displayAllNotes();
        Snackbar.make(main, String.valueOf(CLONESelectedContent.size()) + " "  + getString(R.string.notes_finished_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String content : CLONESelectedContent){
                    insertData(content);
                }
                todosql.deleteTheLastCoupleOnesFromHistory(size);
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
        displayAllNotes();
        Snackbar.make(main, String.valueOf(CLONESelectedContent.size()) + " " + getString(R.string.notes_deleted_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String content : CLONESelectedContent){
                    insertData(content);
                }
                displayAllNotes();
            }
        }).show();
    }

    public Uri insertData(String title) {
        if (!title.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(TITLE, title);
            return getContentResolver().insert(AppContract.Item.TODO_URI, values);
        } else return null;
    }

    public void finishData(long id){
        ContentValues cv = new ContentValues();
        String data = todosql.getOneDataInTODO(Long.toString(id));
        cv.put(TITLE,data);
        //System.out.println("finish data" + id);
        deleteData(id);
        getContentResolver().insert(AppContract.Item.HISTORY_URI, cv);
    }

    public void deleteData(long id){
        Uri uri = ContentUris.withAppendedId(AppContract.Item.TODO_URI, id);
        //System.out.println("delete data" + id);
        getContentResolver().delete(uri, null, null);
        displayAllNotes();
    }

    public void shareSetOfData(){//share note function
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = null;
        StringBuilder shareBodyBuilder = new StringBuilder();
        if (selectedContent.size() == 1){
            shareBodyBuilder.append(selectedContent.get(0));
        }else {
            for(String data : selectedContent){
                shareBodyBuilder.append("\n\n");//empty line after each note
                shareBodyBuilder.append(data);
            }
            shareBodyBuilder.deleteCharAt(0);//remove the extra final empty lines.
            shareBodyBuilder.deleteCharAt(0);//remove the extra final empty lines.
        }
        shareBody = shareBodyBuilder.toString();
        String shareSub = getString(R.string.note_share_subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }

    public String determineTag(){
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            return extras.getString("TAG_VALUE");
        }
        return "";
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class ExportPrintAdapter extends PrintDocumentAdapter//print adapter for exportation
    {
        public PdfDocument myPdfDocument;
        public int totalpages = 0;
        Context context;
        private int pageHeight;
        private int pageWidth;

        public ExportPrintAdapter(Context context)
        {
            this.context = context;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle metadata) {
            myPdfDocument = new PrintedPdfDocument(context, newAttributes);//create new PDF document

            pageHeight = newAttributes.getMediaSize().getHeightMils()/1000 * 72;
            pageWidth = newAttributes.getMediaSize().getWidthMils()/1000 * 72;//calculate page height/width

            if (cancellationSignal.isCanceled() ) {//handle cancellation requests
                callback.onLayoutCancelled();
                return;
            }
            totalpages = computePageCount(newAttributes);//get total page number
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                    .Builder(getString(R.string.export_file_name) + dateFormat.format(Calendar.getInstance().getTime()) + ".pdf")//exported file name
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalpages);//set page number
            PrintDocumentInfo info = builder.build();
            callback.onLayoutFinished(info, true);
        }


        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal cancellationSignal,
                            final PrintDocumentAdapter.WriteResultCallback callback) {//render final export document
            for (int i = 0; i < totalpages; i++) {
                if (pageInRange(pageRanges, i))
                {
                    PdfDocument.PageInfo newPage = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i).create();

                    PdfDocument.Page page = myPdfDocument.startPage(newPage);

                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        myPdfDocument.close();
                        myPdfDocument = null;
                        return;
                    }
                    //drawPage(page, i);//DEPRECIATED METHOD
                    Canvas canvas = page.getCanvas();
                    int titleBaseLine = 72;
                    int leftMargin = 54;
                    int verticalMargin = 16;
                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(40);//set title font                    canvas.drawText("This is some test content to verify that custom document printing works", leftMargin, titleBaseLine + 35, paint);
                    canvas.drawText(getString(R.string.export_title), leftMargin, titleBaseLine + 35, paint);
                    paint.setTextSize(14);
                    for(String text : selectedContent){
                        canvas.drawText(
                                text,
                                leftMargin,
                                titleBaseLine,
                                paint);
                        if(canvas.getClipBounds().height()-verticalMargin >= pageHeight) return;
                    }
                    myPdfDocument.finishPage(page);
                }
            }

            try {
                myPdfDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                myPdfDocument.close();
                myPdfDocument = null;
            }
            callback.onWriteFinished(pageRanges);
            setOutOfSelectionMode();
        }

        private void drawPage(PdfDocument.Page page, int pagenumber) {//DEPRECIATED method, see onWrite() part
            Canvas canvas = page.getCanvas();

            pagenumber++; // Make sure page numbers start at 1

            int titleBaseLine = 72;
            int leftMargin = 54;
            int dynamicTextSize = 0;//determine text size based on the content size
            int textCount = selectedContent.toArray().length;
            if (textCount <= 150){
                dynamicTextSize = 30;
            }else if(textCount < 500 && textCount > 150){
                dynamicTextSize = 22;
            }else {
                dynamicTextSize = 18;
            }
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(dynamicTextSize);//set title font
            canvas.drawText(
                    "Test Print Document Page " + pagenumber,
                    leftMargin,
                    titleBaseLine,
                    paint);

            paint.setTextSize(14);
            canvas.drawText("This is some test content to verify that custom document printing works", leftMargin, titleBaseLine + 35, paint);

            //PdfDocument.PageInfo pageInfo = page.getInfo();

            /*canvas.drawCircle(pageInfo.getPageWidth()/2,
                    pageInfo.getPageHeight()/2,
                    150,
                    paint);*///draw circle todo mark on the page
        }

        private boolean pageInRange(PageRange[] pageRanges, int page)
        {
            for (int i = 0; i<pageRanges.length; i++)
            {
                if ((page >= pageRanges[i].getStart()) &&
                        (page <= pageRanges[i].getEnd()))
                    return true;
            }
            return false;
        }

        private int computePageCount(PrintAttributes printAttributes) { //calculate total page number todo improve this algorithm
            int itemsPerPage = 4; // default item count for portrait mode

            PrintAttributes.MediaSize pageSize = printAttributes.getMediaSize();
            if (!pageSize.isPortrait()) {
                // Six items per page in landscape orientation
                itemsPerPage = 6;
            }

            // Determine number of print items
            int finalPageNumber = 0;
            int printItemCount = selectedContent.size();
            int pageHeight = pageSize.getHeightMils();
            int dynamicTextSize = 0;//determine text size based on the content
            int textCount = selectedContent.toArray().length;
            if (textCount <= 150){
                dynamicTextSize = 30;
            }else if(textCount < 500 && textCount > 150){
                dynamicTextSize = 22;
            }else {
                dynamicTextSize = 18;
            }
            Toast.makeText(getApplicationContext(),printItemCount,Toast.LENGTH_LONG).show();
            Paint fontPaint = new Paint();//determine content size'
            Rect fontRect = new Rect();
            fontPaint.setStyle(Paint.Style.FILL);
            fontPaint.setColor(Color.BLACK);
            fontPaint.setTextSize(dynamicTextSize);
            fontPaint.getTextBounds(selectedContent.toString(),0,selectedContent.size(),fontRect);
            int textTotalHeight = fontRect.height();
            if (pageHeight >= textTotalHeight) finalPageNumber = 1;//in case the content is less than one page
            else finalPageNumber = textTotalHeight / pageHeight + 1;//calculate the total page number needed
            return finalPageNumber; //todo temporary, change later
            //return (int) Math.ceil(printItemCount / itemsPerPage);
        }


    }

    public boolean exportOrPrint(){//print list or export as pdf
        //todo print list or export as pdf
        Toast.makeText(getApplicationContext(),getString(R.string.exporting),Toast.LENGTH_SHORT).show();
        String exportBody = null;
        StringBuilder exportBodyBuilder = new StringBuilder();
        exportBody = getString(R.string.note_export_content_header);
        for(String data : selectedContent){
            exportBodyBuilder.append(data);
            exportBodyBuilder.append("\n\n");//empty line after each note
        }
        exportBody = exportBodyBuilder.toString();//this is the final string for export
        PrintManager printManager = (PrintManager) TagsActivity.this
                .getSystemService(Context.PRINT_SERVICE);
        String jobName = TagsActivity.this.getString(R.string.app_name) + " Document";
        try{
            printManager.print(jobName, new TagsActivity.ExportPrintAdapter(TagsActivity.this),null);//print with print adapter
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        //PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(new Rect(0, 0, 100, 100), 1).create();
        //PdfDocument.Page page = document.startPage(pageInfo);
        //document.writeTo();
        return true;
    }

    public void setOutOfSearchMode(){
        fab.setVisibility(View.VISIBLE);
        isInSearchMode = false;
        getSupportLoaderManager().restartLoader(123,null,this);
        displayAllNotes();
        hideKeyboard();
    }

    public void setOutOfSelectionMode(){
        isInSelectionMode = false;
        fab.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(123,null,this);
        selectedId.clear();
        selectedContent.clear();
        selectAll = false;
        unSelectAll = false;
        displayAllNotes();
        selectionToolBar = (Toolbar)findViewById(R.id.selection_toolbar);
        selectionToolBar.getMenu().clear();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        selectionToolBar.setVisibility(View.GONE);
        if(selectAllBox != null){
            selectAllBox.setChecked(false);
        }
        toolbar.setVisibility(View.VISIBLE);
        tagList.requestFocus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(isInSearchMode && isInSelectionMode){
            setOutOfSelectionMode();
        }else if(isInSearchMode){
            setOutOfSearchMode();
        }else {
            setOutOfSelectionMode();
        }
        return true;
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
                    String text = cursor.getString(cursor.getColumnIndex(dtb.TITLE));//get the text of the note
                    holder.todoText.setTextColor(textColor);
                    holder.cardView.setCardBackgroundColor(ColorUtils.darken(backgroundColor,0.01));
                    holder.todoText.setTextSize(textSize);
                }
            });
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
         //   EmptextView.setTextColor(Color.parseColor("#7FFFFFFF"));
        } else {//bright
       //     EmptextView.setTextColor(Color.parseColor("#61000000"));

        }
        tagList.setBackgroundColor(backgroundColor);
      //  navigationView.setBackgroundColor(backgroundColor);
        View listView = LayoutInflater.from(TagsActivity.this).inflate(R.layout.todolist, null);
        if (ColorUtils.determineBrightness(backgroundColor) < 0.5) {// dark
           // input.setHintTextColor(ColorUtils.makeTransparent(textColor, 0.5));
        } else {
          //  input.setHintTextColor(ColorUtils.makeTransparent(textColor, 0.38));
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(),0);
    }

    public void showKeyboard() {
        InputMethodManager imManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imManager.showSoftInput(input,InputMethodManager.SHOW_IMPLICIT);
    }

}
