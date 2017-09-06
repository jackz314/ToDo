package com.jackz314.todo;


import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.jackz314.todo.util.IabBroadcastReceiver;
import com.jackz314.todo.util.IabHelper;
import com.jackz314.todo.util.IabResult;
import com.jackz314.todo.util.Inventory;
import com.jackz314.todo.util.Purchase;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//   ┏┓　　　┏┓
//┏┛┻━━━┛┻┓
//┃　　　　　　　┃
//┃　　　━　　　┃
//┃　┳┛　┗┳　┃
//┃　　　　　　　┃
//┃　　　┻　　　┃
//┃　　　　　　　┃
//┗━┓　　　┏━┛
//    ┃　　　┃
//    ┃　　　┃
//    ┃　　　┗━━━━┓
//    ┃　　BY 　　　   ┣┓
//    ┃　　　Jack 　  ┏┛
//    ┗┓┓┏━┳┓┏┛
//     ┃┫┫　┃┫┫
//    ┗┻┛　┗┻┛

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    dtb todosql;
    EditText input;
    FloatingActionButton fab;
    TextView modifyId;
    ListView todolist;
    private FirebaseAnalytics mFirebaseAnalytics;
    IabHelper mHelper;
    private static final String REMOVE_AD_SKU = "todo_iap_remove_ad";
    int exit=0,doubleClickCout = 0;
    boolean justex = true;
    public boolean isInSearchMode = false, isInSelectionMode = false;
    public ArrayList<Long> selectedId = new ArrayList<>();
    public ArrayList<String> selectedContent = new ArrayList<>();
    public ArrayList<String> CLONESelectedContent = new ArrayList<>();
    boolean etr = false;
    boolean isConnected = false;
    boolean isEmpty = false;
    SharedPreferences sharedPreferences;
    public String searchText;
    int themeColor,textColor,backgroundColor,textSize;
    int firstVisibleItem,firstItemDiff;
    CoordinatorLayout main;
    SearchView searchView;
    ColorUtils colorUtils;
    DrawerLayout mDrawerLayout;
    TodoListAdapter todoListAdapter;
    ActionBarDrawerToggle mDrawerToggle;
    TextView EmptextView, selectionTitle;
    CheckBox multiSelectionBox;
    AdView adView;
    NavigationView navigationView;
    Menu menuNav;
    MenuItem navRemoveAD;
    IInAppBillingService mService;
    Toolbar selectionToolBar, toolbar;
    ServiceConnection mServiceConn;
    CheckBox selectAllBox;
    private String payload = "HAHA! this is the real one, fuck you motherfucker";
    IabBroadcastReceiver mBroadcastReceiver;
    public static int MODIFY_CONTEXT_ID = 1;
    public static int DELETE_CONTEXT_ID = 2;
    public boolean iapsetup = false;
    public boolean isAdRemoved = false;
    View todoView;
    static int REMOVE_REQUEST_ID =1022;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adView= (AdView)findViewById(R.id.bannerAdView);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        AdRequest adRequest = new AdRequest.Builder().build();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        //layoutInflater.inflate(R.layout.nav_header_main,null);
        //setLauncherIcon();
       // FirebaseCrash.report(new Exception("MainActivity created"));
        //FirebaseCrash.log("MainActivity created log");
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        View todoView = layoutInflater.inflate(R.layout.todolist,null);
        input = (EditText)findViewById(R.id.input);
        modifyId = (TextView)findViewById(R.id.modifyId);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        EmptextView = (TextView)findViewById(R.id.emptyText);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        todosql = new dtb(this);
        payload = "0x397821dc97276";
        setSupportActionBar(toolbar);
        // navheadText = (TextView)navigationView.findViewById(R.id.navHeadText);
        main = (CoordinatorLayout)findViewById(R.id.total_main_bar);
        //get colors
        setColorPreferences();
        displayAllNotes();
        input.setTextIsSelectable(true);
        input.setFocusable(true);
        todolist.setFocusable(true);
        todolist.setFocusableInTouchMode(true);
        //todolist.performLongClick();
        //setOutOfSelectionMode();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                todolist.performLongClick();
                // Actions to do after 10 seconds
            }
        },100);
        String base64EncodedPublicKey = "MII";
        String bep = "ANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiZZobdX3yEuQtssAfZ2AE69Agvit3KuCfR6ywZRlrcpjWKb5+aKBT72hEawKFwDCsFquccZvt6R8nKBD1ucbl4PCgZvrUie9EFQR4YKxlp9iPogdreu8ifIjR/un9sFsiRGndmjhgJHMx66uKlDX7gyu9/EzuxFVajPCdbw7nQdK9XJzBripYLKY0w5/BLbKaOo7kmhSwiOlsRQwayIbXvUiYQb5ij17eFO/n4sebKNvixdIsaU3YaFlh/CbEpy/3P0UEHtrtb3B27pBa4+3kEriVc7uVBN+kYHmMQRMBgyjzKNwITDhHrP12qjlmrVk4LKehQVVDmPymB/C1/qTuwIDAQAB";
        base64EncodedPublicKey += "BIjAN" + bep.substring(2,bep.length()-1);
        input.setVisibility(View.GONE);
        menuNav = navigationView.getMenu();
        navRemoveAD = menuNav.findItem(R.id.unlock);
        if(navRemoveAD != null){
            navRemoveAD.setEnabled(false);
        }
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(false);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener(){
            public void onIabSetupFinished(IabResult result) {
                if(!result.isSuccess()||result.isFailure()){
                    iapsetup = false;
                    return;
                }
                if(mHelper == null){
                    iapsetup = false;
                    return;
                }
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });
        if(isAdRemoved && navRemoveAD != null){
            menuNav.removeItem(R.id.unlock);
        }else {
            adView.loadAd(adRequest);
        }
        if(!sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) != null){
            navigationView.getMenu().removeItem(R.id.history);
        }else if(sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) == null){
            menuNav.add(R.id.nav_category_main,R.id.history,0,getString(R.string.nav_history));
        }
        todolist.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                todolist.requestFocus();
                if(input.isCursorVisible()||input.isInEditMode()||input.isInputMethodTarget()||input.isFocused()||input.hasFocus()){
                    hideKeyboard();
                    if(input.getText().toString().equals("")&&input.getText().toString().isEmpty()){
                        fab.setImageResource(R.drawable.ic_add_black_24dp);
                        hideKeyboard();
                        input.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        int[] colors = {0, colorUtils.lighten(textColor,0.6), 0}; // red for the example
        todolist.setDivider(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors));
        todolist.setDividerHeight(2);
        //todolist.setDivider(new ColorDrawable(colorUtils.lighten(textColor,0.5)));
        //long click listener replaced by context menu due to suitable design concern
        /*todolist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteNote(String.valueOf(id),id);
                if(!modifyId.getText().toString().equals("")){
                    if(modifyId.getText().toString().equals(String .valueOf(id))){
                        modifyId.setText("");
                        input.setText("");
                        input.setVisibility(View.GONE);
                        hideKeyboard();
                        todolist.clearFocus();
                        adView.requestFocus();
                        fab.setImageResource(R.drawable.ic_add_black_24dp);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "delete_notes");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "delete notes");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    }
                }
                return false;
            }
        });*/
        doubleClickCout = 0;
        todolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, final long id) {
                if (isInSelectionMode) {
                    multiSelectionBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
                    if(multiSelectionBox.isChecked()){
                        removeSelectedId(id);
                        multiSelectionBox.setChecked(false);
                        System.out.println("false" + id);
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
                    doubleClickCout++;
                    Handler handler = new Handler();
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            doubleClickCout = 0;
                        }
                    };
                    if (doubleClickCout == 2) {
                        //Double click
                        // Toast.makeText(getApplicationContext(),"sadadadadasdasdasdassdassd",Toast.LENGTH_SHORT).show();
                        final String finishedContent = todosql.getOneDataInTODO(String.valueOf(id));
                        finishNote(id);
                        if(!modifyId.getText().toString().equals("")){
                            if(modifyId.getText().toString().equals(String .valueOf(id))){
                                modifyId.setText("");
                                input.setText("");
                                input.setVisibility(View.GONE);
                                hideKeyboard();
                                todolist.clearFocus();
                                adView= (AdView)findViewById(R.id.bannerAdView);
                                adView.requestFocus();
                                fab.setImageResource(R.drawable.ic_add_black_24dp);
                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "finish_notes");
                                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "finished note");
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                            }
                        }
                        Snackbar.make(main, getString(R.string.note_finished_snack_text), Snackbar.LENGTH_SHORT).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                todosql.insertData(finishedContent);
                                long lastHistoryId = todosql.getIdOfLatestDataInHistory();
                                todosql.deleteFromHistory(String.valueOf(lastHistoryId));
                                displayAllNotes();
                            }
                        }).show();
                        doubleClickCout = 0;
                    }else if (doubleClickCout == 1) {
                        //Single click
                        modifyId.setText(String.valueOf(id));
                        fab.setImageResource(R.drawable.ic_send_black_24dp);
                        input.setVisibility(View.VISIBLE);
                        input.setText(todosql.getOneDataInTODO(String.valueOf(id)));
                        handler.postDelayed(r,250);//double click interval
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                input.requestFocus();
                                todolist.smoothScrollBy(view.getTop(),300);
                            }
                        }, 250);
                    }
                }
            }
        });

        todolist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                if(isInSelectionMode){
                    //do nothing
                }else {
                    setOutOfSelectionMode();
                    isInSelectionMode = true;
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
                            if(!selectAllBox.isChecked()){//uncheck all
                                selectAllBox.setChecked(false);
                                selectedId.clear();
                                selectedContent.clear();
                                for(int i = 0; i < todolist.getCount(); i++){
                                    multiSelectionBox = (CheckBox)getViewByPosition(i).findViewById(R.id.multiSelectionBox);
                                    multiSelectionBox.setChecked(false);
                                }
                                selectionTitle.setText(getString(R.string.selection_mode_empty_title));
                                selectionToolBar.getMenu().clear();
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
                                            }
                                            return false;
                                        }
                                    });
                                }
                                Long id;
                                selectedId.clear();
                                selectedContent.clear();
                                for(int i = 0; i < todolist.getAdapter().getCount(); i++){
                                    multiSelectionBox = (CheckBox)getViewByPosition(i).findViewById(R.id.multiSelectionBox);
                                    multiSelectionBox.setChecked(true);
                                    id = todolist.getAdapter().getItemId(i);
                                    selectedId.add(0,id);
                                    String data = todosql.getOneDataInTODO(Long.toString(id));
                                    selectedContent.add(0,data);
                                }
                                String count = Integer.toString(selectedId.size());
                                selectionTitle.setText(count + getString(R.string.selection_mode_title));
                            }
                        }
                    });
                    addSelectedId(id);
                    //todoListAdapter.setCheckboxChecked(view,true);
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

        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input.requestFocus();
                showKeyboard();
                fab.setImageResource(R.drawable.ic_send_black_24dp);
            }
        });

        todolist.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: //stopped scroll
                        firstVisibleItem = view.getFirstVisiblePosition();
                        firstItemDiff = view.getTop();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://scrolling
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING://fleeing
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOutOfSelectionMode();
                String inputText=input.getText().toString().trim();
                if(inputText.isEmpty()||inputText.equals("")||input.getText()==null){
                    fab.setImageResource(R.drawable.ic_send_black_24dp);
                    input.setVisibility(View.VISIBLE);
                    showKeyboard();
                    input.requestFocus();
                }
                else{
                    boolean successModify=false,success=false;
                    if (!modifyId.getText().toString().equals("")){
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "update_notes");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "updated notes"+input.getText().toString());
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                        successModify = updateData(modifyId.getText().toString(),input.getText().toString());
                    } else {
                        success = todosql.insertData(input.getText().toString());
                        int[] colors = {0, colorUtils.lighten(textColor,0.6), 0};
                        todolist.setDivider(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors));
                        todolist.setDividerHeight(2);
                        todolist.smoothScrollToPosition(0);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "new_notes");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "new notes"+input.getText().toString());
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    }if(success||successModify){
                        hideKeyboard();
                        displayAllNotes();
                        fab.setImageResource(R.drawable.ic_add_black_24dp);
                        //input.clearFocus();
                        input.setVisibility(View.GONE);
                        input.setText("");
                        modifyId.setText("");
                    } else{
                        Toast.makeText(getApplicationContext(),getString(R.string.error_message),Toast.LENGTH_SHORT).show();
                        input.requestFocus();
                    }
                }
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT).setAction("Action", null).show();*/
            }
        });


        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                if(input.isCursorVisible()){
                    showKeyboard();
                }
                super.onDrawerClosed(view);
                //getActionBar().setTitle(title);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                hideKeyboard();
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(title);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        input.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if (hasFocus){
                    //if(!input.getText().toString().equals("")) clearEditText.setDrawableVisible(true);
                    //Toast.makeText(getApplicationContext(),"called showKeyboard!",Toast.LENGTH_SHORT).show();
                    showKeyboard();
                }
                else {
                    hideKeyboard();
                    main.requestFocus();
                    //Toast.makeText(getApplicationContext(),"focus cleared, touched, request focus",Toast.LENGTH_SHORT).show();
                    if(input.isCursorVisible()||input.isInEditMode()||input.isInputMethodTarget()||input.isFocused()||input.hasFocus()){
                        //input.clearFocus();
                        //Toast.makeText(getApplicationContext(),"focus cleared, touched, request focus",Toast.LENGTH_SHORT).show();
                        //main.requestFocus();
                        hideKeyboard();
                        if(input.getText().toString().equals("")||input.getText().toString().isEmpty()){
                            //Toast.makeText(getApplicationContext(),"3",Toast.LENGTH_SHORT).show();
                            modifyId.setText("");
                            fab.setImageResource(R.drawable.ic_add_black_24dp);
                            //input.clearFocus();
                            hideKeyboard();
                            input.setVisibility(View.GONE);
                        }
                    }
                    //displayAllNotes();
                }
            }
        });
    }//--------end of onCreate!

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener(){
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if(mHelper == null){
                iapsetup = false;
                return;
            }
            if(result.isFailure()||!result.isSuccess()){
                iapsetup = false;
                return;
            }
            if(result.isSuccess()){
                Purchase unlockPurchase = inv.getPurchase(REMOVE_AD_SKU);
                isAdRemoved = (unlockPurchase != null && verifyDeveloperPayload(unlockPurchase));
                removeAd();
            }
        }
    };


    public void removeAd(){
        if(isAdRemoved){
            adView.destroy();
            adView.setEnabled(false);
            adView.setVisibility(View.GONE);
            menuNav.removeItem(R.id.unlock);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)fab.getLayoutParams();
            params.bottomMargin = 80;
        }
        else{
            return;
        }
    }

    public void setOutOfSearchMode(){
        isInSearchMode = false;
        displayAllNotes();
        hideKeyboard();
    }

    public View getViewByPosition(int pos) {
        final int firstListItemPosition = todolist.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + todolist.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return todolist.getAdapter().getView(pos, null, todolist);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return todolist.getChildAt(childIndex);
        }
    }

    public void setOutOfSelectionMode(){
        isInSelectionMode = false;
        selectedId.clear();
        selectedContent.clear();
        displayAllNotes();
        if(!isEmpty){
            for(int i = 0; i < todolist.getCount(); i++) {
                multiSelectionBox = (CheckBox) getViewByPosition(i).findViewById(R.id.multiSelectionBox);
                multiSelectionBox.setChecked(false);
            }
        }
        selectionToolBar = (Toolbar)findViewById(R.id.selection_toolbar);
        selectionToolBar.getMenu().clear();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        selectionToolBar.setVisibility(View.GONE);
        if(selectAllBox != null){
            selectAllBox.setChecked(false);
        }
        toolbar.setVisibility(View.VISIBLE);
        todolist.requestFocus();
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

    public void setLauncherIcon(){
        Intent launcherIntent = new Intent();
        launcherIntent.setClassName("com.jackz314.todo","MainActivity");
        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,launcherIntent);
        intent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(
                    getApplicationContext(),
                    R.drawable.common_google_signin_btn_icon_light_normal
                )
        );
        getApplicationContext().sendBroadcast(intent);
    }

    boolean verifyDeveloperPayload(Purchase p) {

        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null){
                return;
            }if(result.isFailure()){
                Toast.makeText(getApplicationContext(),getString(R.string.purchase_failed),Toast.LENGTH_LONG).show();
                return;
            }if(!verifyDeveloperPayload(purchase)){
                Toast.makeText(getApplicationContext(),getString(R.string.purchase_failed),Toast.LENGTH_LONG).show();
                return;
            }
            if(purchase.getSku().equals(REMOVE_AD_SKU)){
                isAdRemoved = true;
                removeAd();
            }
        }
    };

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MODIFY_CONTEXT_ID, 0, R.string.modify_menu_text);
        menu.add(0,DELETE_CONTEXT_ID,0,R.string.delete_menu_text);
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(25);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long id = adapterContextMenuInfo.id;
        if (item.getItemId() == MODIFY_CONTEXT_ID) {
            main.requestFocus();
            View top = adapterContextMenuInfo.targetView;
            todolist.scrollListBy(top.getTop());
            modifyId.setText(String.valueOf(id));
            fab.setImageResource(R.drawable.ic_send_black_24dp);
            input.setVisibility(View.VISIBLE);
            input.setText(todosql.getOneDataInTODO(String.valueOf(id)));
            input.requestFocus();
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    input.requestFocus();
                }
            }, 230);
            input.performClick();
        }if(item.getItemId() == DELETE_CONTEXT_ID) {
            final String deleteContent = todosql.getOneDataInTODO(String.valueOf(id));
            todosql.deleteNote(id);
            displayAllNotes();
            if(!modifyId.getText().toString().equals("")){
                if(modifyId.getText().toString().equals(String .valueOf(id))){
                    modifyId.setText("");
                    input.setText("");
                    input.setVisibility(View.GONE);
                    hideKeyboard();
                    todolist.clearFocus();
                    adView= (AdView)findViewById(R.id.bannerAdView);
                    adView.requestFocus();
                    fab.setImageResource(R.drawable.ic_add_black_24dp);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "finish_notes");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "finished note");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }
            }
            Snackbar.make(main, getString(R.string.note_deleted_snack_text), Snackbar.LENGTH_SHORT).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    todosql.insertData(deleteContent);
                    displayAllNotes();
                }
            }).show();
            if(!modifyId.getText().toString().equals("")){
                if(modifyId.getText().toString().equals(String .valueOf(id))){
                    modifyId.setText("");
                    input.setText("");
                    input.setVisibility(View.GONE);
                    hideKeyboard();
                    todolist.clearFocus();
                    adView= (AdView)findViewById(R.id.bannerAdView);
                    adView.requestFocus();
                    fab.setImageResource(R.drawable.ic_add_black_24dp);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "delete_notes");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "deleted note");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }
            }
        }
        return super.onContextItemSelected(item);
    }*/

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
                    }
                    return false;
                }
            });
        }if(selectedId.size() == todolist.getCount()){
            selectAllBox.setChecked(true);
        }
        String count = Integer.toString(selectedId.size());
        selectionTitle.setText(count + getString(R.string.selection_mode_title));
    }

    public void removeSelectedId(long id){
        selectedId.remove(selectedId.indexOf(id));
        selectAllBox.setChecked(false);
        String data = todosql.getOneDataInTODO(Long.toString(id));
        selectedContent.remove(selectedContent.indexOf(data));
        if (selectedId.size() == 0) {
            selectionTitle.setText(getString(R.string.selection_mode_empty_title));
            selectionToolBar.getMenu().clear();
        }else {
            selectionToolBar = (Toolbar)findViewById(R.id.selection_toolbar);
            String count = Integer.toString(selectedId.size());
            selectionTitle.setText(count + getString(R.string.selection_mode_title));
        }
    }

    public void setColorPreferences(){
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor = sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorPrimary));
        textColor = sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        textSize = sharedPreferences.getInt(getString(R.string.text_size_key),24);
        backgroundColor = sharedPreferences.getInt(getString(R.string.background_color_key),Color.WHITE);
        todolist = (ListView)findViewById(R.id.todolist);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        /*
        set colors
         */
        LayoutInflater inflater = LayoutInflater.from(this);
        View navMainView = inflater.inflate(R.layout.nav_header_main,null);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationView.setItemTextColor(ColorStateList.valueOf(textColor));
        navigationView.setItemIconTintList(ColorStateList.valueOf(textColor));
        int[] themeColors = {backgroundColor,themeColor};
        Drawable drawHeadBG = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,themeColors);
        drawHeadBG.setColorFilter(themeColor, PorterDuff.Mode.DST);
        View navHeader = navigationView.getHeaderView(0);
        TextView navHeadText = (TextView)navHeader.findViewById(R.id.navHeadText);
        navHeadText.setTextColor(textColor);
        //navHeadText.setTextSize(textSize);
        navHeader.setBackground(drawHeadBG);
        //navHeader.setBackgroundColor(Color.RED);
        fab.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        toolbar.setBackgroundColor(themeColor);
        input.setTextColor(textColor);
        main.setBackgroundColor(backgroundColor);
        Window window = this.getWindow();
        window.setStatusBarColor(themeColor);
        window.setNavigationBarColor(themeColor);
        EmptextView.setTextColor(colorUtils.lighten(textColor,0.4));
        todolist.setBackgroundColor(backgroundColor);
        navigationView.setBackgroundColor(backgroundColor);
        View listView= LayoutInflater.from(MainActivity.this).inflate(R.layout.todolist,null);
        input.setLinkTextColor(themeColor);
        input.setHintTextColor(colorUtils.lighten(textColor,0.5));
        input.setLinkTextColor(textColor);
        input.setHighlightColor(colorUtils.lighten(themeColor,0.2));
        input.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        int[] colors = {0, colorUtils.lighten(textColor,0.6), 0};
        todolist.setDivider(new GradientDrawable(GradientDrawable.Orientation.TR_BL, colors));
        todolist.setDividerHeight(2);
}

    public void showFeedBackDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.feedback_dialog, null);
        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
        edt.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        edt.setHighlightColor(colorUtils.lighten(themeColor,0.2));
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.feedback_title))
                .setMessage(getString(R.string.feedback_message))
                .setPositiveButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(!edt.getText().toString().equals("")){
                            FirebaseCrash.report(new Exception(edt.getText().toString()));
                            FirebaseCrash.log(edt.getText().toString());
                            Toast.makeText(getApplicationContext(),getString(R.string.thx_for_feed),Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //cancel
                    }
                }).setCancelable(true).setView(dialogView).create();
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!edt.getText().toString().trim().isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                if(edt.getText().toString().trim().isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!edt.getText().toString().trim().isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                if(edt.getText().toString().trim().isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private class ConnectionDetector extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            if (networkConnectivity()) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
                    urlc.setRequestProperty("User-Agent", "Android");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.setReadTimeout(2000);
                    urlc.connect();
                    // networkcode2 = urlc.getResponseCode();
                    isConnected = (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
                } catch (IOException e) {
                    isConnected = false;
                }
            } else{
                isConnected = false;
            }
            return null;
        }
        private boolean networkConnectivity() {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //isConnected = isConn;
            super.onPostExecute(aVoid);
        }
    }

    public void purchaseRemoveAds(){
        try {
            new ConnectionDetector().execute().get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        if(isConnected){
            try{
                //Toast.makeText(getApplicationContext(),"ddd",Toast.LENGTH_LONG).show();
                mHelper.launchPurchaseFlow(this, REMOVE_AD_SKU, REMOVE_REQUEST_ID, mPurchaseFinishedListener, payload);
            }
            catch (IabHelper.IabAsyncInProgressException e) {
                Toast.makeText(getApplicationContext(),getString(R.string.purchase_failed),Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }else{
            //Toast.makeText(getApplicationContext(),"NO INTERNET",Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),getString(R.string.purchase_failed),Toast.LENGTH_LONG).show();
        }
    }

    public void displaySearchResults(final String filter){
        final Cursor cursor = todosql.getSearchResults(filter);
        if(cursor.getCount() == 0){
            EmptextView.setVisibility(View.VISIBLE);
            EmptextView.setText(R.string.empty_search_result);
            todolist.removeAllViewsInLayout();//remove all items
            todolist.setAdapter(null);
        } else {
            EmptextView.setVisibility(View.GONE);
            EmptextView.setText("");
            final TodoListAdapter todoListAdapter = new TodoListAdapter(this,R.layout.todolist,cursor,new String[] {todosql.TITLE},new int[]{R.id.titleText});
            todolist.setAdapter(new TodoListAdapter(this,R.layout.todolist,cursor,new String[] {todosql.TITLE},new int[]{R.id.titleText}){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View todoView = super.getView(position,convertView,parent);
                    //super.bindView(convertView,super.mContext,cursor);
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

    public void displayAllNotes(){
        if(isInSearchMode && searchText != null){
            displaySearchResults(searchText);
        }else {
            setColorPreferences();
            sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
            boolean isOrderReguar = sharedPreferences.getBoolean(getString(R.string.order_key),true);
            Cursor cs = null;
            if(!isOrderReguar){
                cs = todosql.getData();
            } else{
                cs = todosql.getDataDesc();
            }
            //todolist = (ListView)findViewById(R.id.todolist);
            EmptextView = (TextView)findViewById(R.id.emptyText);
            if(cs.getCount()==0){//if database is empty, then clears the listView too
                isEmpty =true;
                System.out.println("empty database!");
                EmptextView.setVisibility(View.VISIBLE);
                EmptextView.setText(R.string.empty_todolist);
                todolist.removeAllViewsInLayout();//remove all items
                todolist.setAdapter(null);
            } else {
                EmptextView.setVisibility(View.GONE);
                EmptextView.setText("");
                isEmpty =false;
                if(todolist.getAdapter() == null){
                    //Toast.makeText(getApplicationContext(),"null",Toast.LENGTH_SHORT).show();
                    final Cursor finalCs = cs;
                    todoListAdapter = (new TodoListAdapter(this,R.layout.todolist, finalCs,new String[] {todosql.TITLE},new int[]{R.id.titleText}){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent){
                           // super.newView(super.mContext, finalCs,parent);
                           // super.bindView(convertView,super.mContext, finalCs);
                            todoView = super.getView(position,convertView,parent);
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
                                multiSelectionBox.setChecked(false);
                            }
                            TextView todoText = (TextView)todoView.findViewById(R.id.titleText);
                            todoText.setTextColor(textColor);
                            todoText.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
                            return todoView;
                        }
                    });
                    todolist.setAdapter(todoListAdapter);
                }else {
                    final Cursor finalCs = cs;
                    Runnable runnable =new Runnable() {
                        @Override
                        public void run() {
                            todoListAdapter.refreshCursor(finalCs);
                            todoListAdapter.notifyDataSetChanged();
                        }
                    };
                    runOnUiThread(runnable);
                    //cs.close();
                }
            }
        }
        //registerForContextMenu(todolist);
    }

    public void finishSetOfData(){

        CLONESelectedContent.clear();

        for(long id : selectedId){
            todosql.finishData(id);
        }
        final int size = selectedId.size();
        CLONESelectedContent = new ArrayList<>(selectedContent);
        setOutOfSelectionMode();
        displayAllNotes();
        Snackbar.make(main, getString(R.string.notes_finished_snack_text), Snackbar.LENGTH_SHORT).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String content : CLONESelectedContent){
                    todosql.insertData(content);
                }
                todosql.deleteTheLastCoupleOnesFromHistory(size);
                displayAllNotes();
            }
        }).show();
    }

    public void deleteSetOfData(){
        CLONESelectedContent.clear();
        for(long id : selectedId){
            todosql.deleteNote(id);
        }
        CLONESelectedContent = new ArrayList<>(selectedContent);
        setOutOfSelectionMode();
        displayAllNotes();
        Snackbar.make(main, getString(R.string.notes_finished_snack_text), Snackbar.LENGTH_SHORT).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String content : CLONESelectedContent){
                    todosql.insertData(content);
                }
                displayAllNotes();
            }
        }).show();
    }

    public boolean updateData(String id, String title){
        boolean isUpdated = todosql.updateData(id, title);
        displayAllNotes();
        return isUpdated;
    }

    public void finishNote(final Long id){
        Integer delRows = todosql.finishData(id);
        displayAllNotes();
        if(delRows==0) Toast.makeText(this,R.string.error_message,Toast.LENGTH_SHORT).show();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(),0);
    }

    public void showKeyboard() {
        InputMethodManager imManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imManager.showSoftInput(input,InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            todolist.setSelectionFromTop(firstVisibleItem,firstItemDiff);
            if(!input.getText().toString().equals("")){
                fab.setImageResource(R.drawable.ic_send_black_24dp);
                input.setVisibility(View.VISIBLE);
                showKeyboard();
            }
            System.out.println("Orientation Changed!");
        }
        super.onConfigurationChanged(newConfig);
     }

    @Override
    public void onBackPressed() {
        if(isInSelectionMode || isInSearchMode){
            if(isInSelectionMode){
                setOutOfSelectionMode();
            }
            if (isInSearchMode){
                setOutOfSearchMode();
            }
        }else {
            System.out.println(String.valueOf(exit));
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            main.requestFocus();
            input.clearFocus();
            hideKeyboard();
            displayAllNotes();
            if(input.getText().toString().equals("")){
                fab.setImageResource(R.drawable.ic_add_black_24dp);
                input.setVisibility(View.GONE);
                justex = true;
                modifyId.setText("");
                etr = true;
                hideKeyboard();
            } else {
                input.setText("");
                modifyId.setText("");
                justex=false;
                etr=false;
                hideKeyboard();
            }if(etr){
                fab.setImageResource(R.drawable.ic_add_black_24dp);
                input.setVisibility(View.GONE);
                input.setText("");
                modifyId.setText("");
                justex = true;
                hideKeyboard();
                etr = true;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit=0;
                }
            }, 2000);
            if(justex&&!drawer.isDrawerOpen(GravityCompat.START)){
                exit++;
                Toast.makeText(getApplicationContext(),R.string.press_again_to_exit,Toast.LENGTH_SHORT).show();
            }
            justex = true;
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            else {
                if(exit>=2){
                    super.onBackPressed();
                }
            }
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
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.todo_search) {
            isInSearchMode = true;

            //todoSearch.setForeground(new ColorDrawable(themeColor));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    public void onResume(){
        if(!input.getText().toString().equals("") && input.getVisibility()==View.VISIBLE) showKeyboard();
        displayAllNotes();
        if(isAdRemoved){
            Log.i("unlock","skipped ad");
        }
        else{
            adView= (AdView)findViewById(R.id.bannerAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
        if(!sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) != null){
            navigationView.getMenu().removeItem(R.id.history);
        }else if(sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) == null){
            menuNav.add(R.id.nav_category_main,R.id.history,0,getString(R.string.nav_history));
        }
        if(sharedPreferences.getBoolean("first_run",true)){
            Cursor cs = todosql.getData();
            if (cs.getCount()==0){
                //first run codes
                todosql.insertData(getString(R.string.tutorial_5));
                todosql.insertData(getString(R.string.tutorial_4));
                todosql.insertData(getString(R.string.tutorial_3));
                todosql.insertData(getString(R.string.tutorial_2));
                todosql.insertData(getString(R.string.tutorial_1));
                todosql.insertData(getString(R.string.welcome_note));
                displayAllNotes();
                sharedPreferences.edit().putBoolean("first_run",false).commit();
            }else {
                setOutOfSelectionMode();
            }
        }
        super.onResume();
    }

    public void onDestroy(){
        if(todosql.isOpen()) todosql.stopService();
        hideKeyboard();
        adView= (AdView)findViewById(R.id.bannerAdView);
        adView.destroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        if(mHelper != null){
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
        super.onDestroy();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        setOutOfSelectionMode();
        if (id == R.id.history) {
            hideKeyboard();
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
            return true;
        }

        else if (id == R.id.settings){
            hideKeyboard();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = getString(R.string.share_content);
            String shareSub = getString(R.string.share_subject);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
        }
        else if (id == R.id.about){
            hideKeyboard();
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.unlock){
            hideKeyboard();
            //purchaseRemoveAds();
            //TEMPORARY CHANGE, CHANGE BACK BEFORE PUBLISH!!!$$$
            isAdRemoved = true;
            removeAd();
        }
        else if (id == R.id.feedback){
            showFeedBackDialog();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
