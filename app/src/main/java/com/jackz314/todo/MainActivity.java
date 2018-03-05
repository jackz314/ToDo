package com.jackz314.todo;


import android.Manifest;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.dmitrymalkovich.android.ProgressFloatingActionButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jackz314.todo.speechrecognitionview.RecognitionProgressView;
import com.jackz314.todo.speechrecognitionview.adapters.RecognitionListenerAdapter;
import com.jackz314.todo.util.IabHelper;
import com.jackz314.todo.util.IabResult;
import com.jackz314.todo.util.Inventory;
import com.jackz314.todo.util.Purchase;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.jackz314.todo.SetEdgeColor.setEdgeColor;
import static com.jackz314.todo.dtb.DATE_FORMAT;
import static com.jackz314.todo.dtb.ID;
import static com.jackz314.todo.dtb.TITLE;

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



// the great alpaca that saves me from the bugs, hopefully...
//todo pause ad function but preserve iap functions for good
//todo edge effect doesn't work at first scroll, minor problem
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ImportantFragment.OnFragmentInteractionListener, ClipboardFragment.OnFragmentInteractionListener, MainFragment.OnFragmentInteractionListener{
    //paused ad//private static final String REMOVE_AD_SKU = "todo_iap_remove_ad";
    private static final String PREMIUM_UPGRADE_SKU = "todo_iap_premium";
    private static final String[] PROJECTION = new String[]{ID, TITLE};//"REPLACE (title, '*', '')"
    private static final String SELECTION = "REPLACE (title, '*', '')" + " LIKE ?";
    //paused ad//static int REMOVE_REQUEST_ID =1022;
    static int PURCHASE_PREMIUM_REQUEST_ID = 1025;
    public boolean isInSearchMode = false, isInSelectionMode = false;
    public ArrayList<Long> selectedId = new ArrayList<>();
    public ArrayList<String> selectedContent = new ArrayList<>();
    public ArrayList<String> CLONESelectedContent = new ArrayList<>();
    public String searchText;
    public boolean iapsetup = true;
    //paused ad//public boolean isAdRemoved = false;
    public boolean isPremium = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String todoTableId = "HAHA! this is the real one, gotcha";
    IabHelper mHelper;
    int exit=0;
    boolean justex = false;
    dtb todosql;
    boolean selectAll = false, unSelectAll = false;
    SharedPreferences sharedPreferences;
    String oldResult = "";
    int themeColor,textColor,backgroundColor,textSize;
    int doubleClickCount = 0;
    ImportantFragment importantFragment;
    MainFragment mainFragment;
    ClipboardFragment clipboardFragment;
    CoordinatorLayout main;
    Boolean noInterruption = true;
    DrawerLayout mDrawerLayout;
    TodoListAdapter todoListAdapter;
    ActionBarDrawerToggle mDrawerToggle;
    TextView selectionTitle;
    CheckBox multiSelectionBox;
    //paused ad//AdView adView;
    boolean isAdd = true;
    NavigationView navigationView;
    Menu menuNav;
    MenuItem navPurchasePremium;
    BroadcastReceiver receiver;
    IInAppBillingService mService;
    Toolbar selectionToolBar, toolbar;
    ServiceConnection mServiceConn;
    CheckBox selectAllBox;
    TabLayout tabLayout;
    ProgressDialog purchaseProgressDialog;
    //todo FIX change fragment issue
        IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener(){
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if(result.isFailure()||(!result.isSuccess())|| mHelper == null){//not premium
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    purchaseProgressDialog.dismiss();
                }
                isPremium = false;
                //paused ad//adView= (AdView)findViewById(R.id.bannerAdView);
                //paused ad//AdRequest adRequest = new AdRequest.Builder().build();
                //paused ad//adView.loadAd(adRequest);
                //paused ad//adView.setVisibility(View.VISIBLE);
                //paused ad//isAdRemoved = false;
                iapsetup = false;
                return;
            }if(result.isSuccess()){//first step succeed
                iapsetup = true;
                Purchase premiumPurchase = inv.getPurchase(PREMIUM_UPGRADE_SKU);
                isPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase) && inv.hasPurchase(PREMIUM_UPGRADE_SKU));
                if(premiumPurchase != null && verifyDeveloperPayload(premiumPurchase) && inv.hasPurchase(PREMIUM_UPGRADE_SKU)){//purchased premium, unlock features here
                    //unlock features here
                    unlockPremium();
                //paused ad//    removeAd();
                }else {//not premium
                //paused ad//    adView= (AdView)findViewById(R.id.bannerAdView);
                //paused ad//    AdRequest adRequest = new AdRequest.Builder().build();
                //paused ad//    adView.loadAd(adRequest);
                //paused ad//    adView.setVisibility(View.VISIBLE);
                //paused ad//    isAdRemoved = false;
                    if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                        Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                        purchaseProgressDialog.dismiss();
                    }
                }
            }else {//not premium
            //paused ad//adView= (AdView)findViewById(R.id.bannerAdView);
            //paused ad//AdRequest adRequest = new AdRequest.Builder().build();
            //paused ad//adView.loadAd(adRequest);
            //paused ad//adView.setVisibility(View.VISIBLE);
            //paused ad//isAdRemoved = false;
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"4",Toast.LENGTH_SHORT).show();
                    purchaseProgressDialog.dismiss();
                }
            }
        }
    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null || result.isFailure() || !verifyDeveloperPayload(purchase)) {
                Toast.makeText(getApplicationContext(),getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"5 " + String.valueOf(mHelper == null) + String.valueOf(result.isFailure()) + String.valueOf(!verifyDeveloperPayload(purchase)),Toast.LENGTH_SHORT).show();
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    purchaseProgressDialog.dismiss();
                }
                return;
            }
            if(purchase.getSku().equals(PREMIUM_UPGRADE_SKU)){
                try {
                    if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                        purchaseProgressDialog.setMessage(getString(R.string.verifying));
                    }
                    if (mHelper != null) mHelper.flagEndAsync();
                    mHelper.queryInventoryAsync(mGotInventoryListener);//continue verify purchase with query inventory
                    return;
                } catch (Exception e) {//failed
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"6",Toast.LENGTH_SHORT).show();
                    if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                        purchaseProgressDialog.dismiss();
                    }
                    iapsetup = false;
                    return;
                }
            }else {//failed
                Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"7",Toast.LENGTH_SHORT).show();
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    purchaseProgressDialog.dismiss();
                }
            }
        }
    };


    /*IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Toast.makeText(getApplicationContext(),"consumed",Toast.LENGTH_LONG).show();
            }
            else {
            }

        }
    };*/

    public static void dataUpload(String data){// refresh firebase token
        if(data.equals("")) {
            data = FirebaseInstanceId.getInstance().getToken();
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();// Create a storage reference from our app
        try {
            String systemInfo ="";
            String macAddress = getMacAddr().replace(":","-");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                systemInfo = "System Info: " + "\n" + "("+ Build.MANUFACTURER + "||\n" + Build.BRAND + "||\n" + Build.DEVICE + "||\n" + Build.MODEL + "||\n"+ Build.HARDWARE + "||\n" + Build.VERSION.RELEASE + "||\n" + Build.VERSION.CODENAME + "||\n" + Build.VERSION.SDK_INT + "||\n" +  Build.VERSION.INCREMENTAL + "||\n" + Build.VERSION.SECURITY_PATCH + "||\n" + macAddress + ")";
            }else {
                systemInfo = "System Info: " + "\n" + "(" + Build.MANUFACTURER + "||\n"+ Build.BRAND + "||\n"+ Build.DEVICE + "||\n"+ Build.MODEL + "||\n" + Build.HARDWARE + "||\n" + Build.VERSION.SDK_INT + "||\n" + Build.VERSION.RELEASE + "||\n" + Build.VERSION.INCREMENTAL + "||\n" + macAddress + ") ";
            }
            String token =  data + systemInfo + "\n" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date());
            byte[] feedbackBytes =token.getBytes("UTF-8");
            String uniqueID = UUID.randomUUID().toString();
            String timeStr = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date());
            StorageReference feedbackRef = storageRef.child("firebase_token/" + " " + Build.DEVICE + " " + macAddress + " " + timeStr + " " + data + " " + uniqueID +".txt");
            UploadTask uploadTask = feedbackRef.putBytes(feedbackBytes);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //Toast.makeText(getApplicationContext(), getString(R.string.error_message) + "\n" + exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(getApplicationContext(), getString(R.string.thx_for_feed), Toast.LENGTH_SHORT).show();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                }
            });
        } catch (UnsupportedEncodingException e) {
            //Toast.makeText(getApplicationContext(), getString(R.string.error_message) + "\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ////System.out.println("ex eoiii" + ex.getLocalizedMessage());
        }
        return "(Can't retrieve mac address)";
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
                //for (final String name : new String[] {"mEdgeGlowTop", "mEdgeGlowBottom"}) {
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

    public static String getThisPackageName(){
        return MainActivity.class.getPackage().getName();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //paused ad//adView= (AdView)findViewById(R.id.bannerAdView);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //paused ad//final AdRequest adRequest = new AdRequest.Builder().build();
        mDrawerLayout =  findViewById(R.id.drawer_layout);
        //LayoutInflater layoutInflater = LayoutInflater.from(this);
        //layoutInflater.inflate(R.layout.nav_header_main,null);
        //setLauncherIcon();
       // FirebaseCrash.report(new Exception("MainActivity created"));
        //FirebaseCrash.log("MainActivity created log");
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        importantFragment = (ImportantFragment)getSupportFragmentManager().findFragmentById(R.id.importantFragment);
        mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        clipboardFragment = (ClipboardFragment) getSupportFragmentManager().findFragmentById(R.id.clipboardFragment);
        //speechRecognizer.setRecognitionListener(new speechListener());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabs_layout);
        todoTableId = "0x397821dc97276";
        todosql = new dtb(this);
        setSupportActionBar(toolbar);
        main = (CoordinatorLayout)findViewById(R.id.total_main_bar);
        //set tabs
        final ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        final PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(),getApplicationContext());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);
        String historySettingPref = "MII";
        String bep = "ANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiZZobdX3yEuQtssAfZ2AE69Agvit3KuCfR6ywZRlrcpjWKb5+aKBT72hEawKFwDCsFquccZvt6R8nKBD1ucbl4PCgZvrUie9EFQR4YKxlp9iPogdreu8ifIjR/un9sFsiRGndmjhgJHMx66uKlDX7gyu9/EzuxFVajPCdbw7nQdK9XJzBripYLKY0w5/BLbKaOo7kmhSwiOlsRQwayIbXvUiYQb5ij17eFO/n4sebKNvixdIsaU3YaFlh/CbEpy/3P0UEHtrtb3B27pBa4+3kEriVc7uVBN+kYHmMQRMBgyjzKNwITDhHrP12qjlmrVk4LKehQVVDmPymB/C1/qTuwIDAQAB";
        historySettingPref += "BIjAN" + bep.substring(2,bep.length());
        //setMargins(fab,8,16,16,16);
        menuNav = navigationView.getMenu();
        todoTableId = todoTableId +
                "CPMFnxQ5s0" +
                "NBVs3kWNgN" +
                "ivr1zfRbfk" +
                "U1lCak93su" +
                "RlMWFgHQMj" +
                "ZWYDiMVeak" +
                "rZ3bRGzfzz" +
                "9IMuplWteD" +
                "rBMyPRIDUm" +
                "GcIdL4lDdR";
        navPurchasePremium = menuNav.findItem(R.id.unlock);
        int size = menuNav.size();
        for (int i = 0; i < size; i++) {
            menuNav.getItem(i).setChecked(false);
        }
        mHelper = new IabHelper(this, historySettingPref);
        mHelper.enableDebugLogging(true);
        if(mHelper != null) mHelper.flagEndAsync();
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if((!result.isSuccess())||result.isFailure()){
                    iapsetup = false;
                    return;
                }
                if(mHelper == null){
                    iapsetup = false;
                    return;
                }
                try {
                    if (mHelper != null) mHelper.flagEndAsync();
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                    iapsetup = false;
                }
            }
        });
        setColorPreferences();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isPremium && navPurchasePremium != null){
                    //paused ad//adView.destroy();
                    menuNav.removeItem(R.id.unlock);
                }else {
                    //paused ad//adView.loadAd(adRequest);
                }
            }
        },700);
        if(navPurchasePremium != null && !iapsetup){
            navPurchasePremium.setEnabled(false);
        }
        if(!sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) != null){
            navigationView.getMenu().removeItem(R.id.history);
        }else if(sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) == null){
            menuNav.add(R.id.nav_category_main,R.id.history,0,getString(R.string.nav_history));
        }
        //proFab.performClick();
        //fabProgressBar.setProgressDrawable(getDrawable(R.drawable.circular));
        //interruptAutoSend();
        ///input.setVisibility(View.VISIBLE);
      //  Handler handler = new Handler();
      //  handler.postDelayed(new Runnable() {
       //     @Override
      //      public void run() {
         //       fab.setVisibility(View.VISIBLE);
       //         fab.performClick();
       //         input.setVisibility(View.GONE);
      //      }
      //  },5000);
        /*todoList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                todoList.requestFocus();
                if(input.isCursorVisible()||input.isInEditMode()||input.isInputMethodTarget()||input.isFocused()||input.hasFocus()){
                    hideKeyboard();
                    if(input.getText().toString().equals("")&&input.getText().toString().isEmpty()){
                        if(!isAdd){
                            AnimatedVectorDrawable d = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_send_to_plus); // Insert your AnimatedVectorDrawable resource identifier
                            fab.setImageDrawable(d);
                            isAdd = true;
                            d.start();
                        }
                        hideKeyboard();
                        input.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });*/

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                MainFragment mainFragment = (MainFragment)getSupportFragmentManager().findFragmentById(R.id.mainFragment);
                EditText input = mainFragment.input;
                if(input.isCursorVisible() && input.getVisibility() == View.VISIBLE){
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

    }//--------end of onCreate!

    public boolean determineIfPurchased(){
        try {
            if (mHelper != null) mHelper.flagEndAsync();
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return isPremium;
    }

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
            todoList.scrollListBy(top.getTop());
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
                    todoList.clearFocus();
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
            Snackbar.make(main, getString(R.string.note_deleted_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertData()(deleteContent);
                    displayAllNotes();
                }
            }).show();
            if(!modifyId.getText().toString().equals("")){
                if(modifyId.getText().toString().equals(String .valueOf(id))){
                    modifyId.setText("");
                    input.setText("");
                    input.setVisibility(View.GONE);
                    hideKeyboard();
                    todoList.clearFocus();
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

    private void unlockPremium(){
        menuNav.removeItem(R.id.unlock);
        if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
            purchaseProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(),getString(R.string.thanks_for_purchase),Toast.LENGTH_SHORT).show();
        }
        //todo unlock premium features here
    }
    
    //paused ad//
    /*public void removeAd(){
        if(isAdRemoved){
            adView.destroy();
            adView.setEnabled(false);
            adView.setVisibility(View.GONE);
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout)findViewById(R.id.fab_coordinator_layout);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)coordinatorLayout.getLayoutParams();
            params.bottomMargin = 0;
            //setMargins(coordinatorLayout,0,0,0,0);
            //setMargins(fab,16,16,16,btmMargin);
            menuNav.removeItem(R.id.unlock);
            if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                purchaseProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(),getString(R.string.thanks_for_purchase),Toast.LENGTH_SHORT).show();
            }
            //ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)fab.getLayoutParams();
            //params.bottomMargin = 80;
        }
        else{
            return;
        }
    }*/

    boolean verifyDeveloperPayload(Purchase p) {
        if(p.getDeveloperPayload() != null && p.getDeveloperPayload().contains("0x397821dc97276")){
            if(p.getDeveloperPayload().equals(
                    "0x397821dc97276"+
                    "CPMFnxQ5s0" +
                    "NBVs3kWNgN" +
                    "ivr1zfRbfk" +
                    "U1lCak93su" +
                    "RlMWFg" +
                    "HQMj" +
                    "ZWYDiMVeak" +
                    "rZ3bRGzfzz" +
                    "9IMuplWteD" +
                    "rBMyPRIDUm" +
                    "GcIdL4lDdR"))
            return true;
        }
        /*
         *  verify that the developer payload of the purchase is correct. It will be
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

        return false;
    }

    public void setColorPreferences(){
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor = sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorActualPrimary));
        textColor = sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        textSize = sharedPreferences.getInt(getString(R.string.text_size_key),24);
        backgroundColor = sharedPreferences.getInt(getString(R.string.background_color_key),Color.WHITE);
        if(ColorUtils.determineBrightness(backgroundColor) < 0.5){
            navigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#fafafa")));
        }else {
            navigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#212121")));
        }
        //setEdgeColor(todoList,themeColor);
        navigationView.setItemIconTintList(ColorStateList.valueOf(themeColor));
        tabLayout.setBackgroundColor(themeColor);
        if(ColorUtils.determineBrightness(themeColor) > 0.9){//if the tab background color is to bright, change tab text color
            tabLayout.setTabTextColors(ColorUtils.makeTransparent(Color.BLACK,0.7),Color.BLACK);
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.tab_white));
        }else {
            tabLayout.setTabTextColors(ColorUtils.makeTransparent(getResources().getColor(R.color.tab_white),0.7),getResources().getColor(R.color.tab_white));
            tabLayout.setSelectedTabIndicatorColor(ColorUtils.lighten(themeColor,0.7));
        }
        //int[] themeColors = {backgroundColor,themeColor};
        //Drawable drawHeadBG = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,themeColors);
        //drawHeadBG.setColorFilter(themeColor, PorterDuff.Mode.DST);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Drawable navHeadImage = getDrawable(R.drawable.nav_header);
                navHeadImage.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
                View navHeader = navigationView.getHeaderView(0);
                TextView navHeadText = (TextView)navHeader.findViewById(R.id.navHeadText);
                navHeadText.setTextColor(Color.WHITE);
                navHeader.setBackground(navHeadImage);
            }
        });
        //navHeadText.setTextSize(textSize);
        //navHeader.setBackgroundColor(Color.RED);
        toolbar.setBackgroundColor(themeColor);
        main.setBackgroundColor(backgroundColor);
        Window window = this.getWindow();
        window.setStatusBarColor(themeColor);
        window.setNavigationBarColor(themeColor);
        navigationView.setBackgroundColor(backgroundColor);
    }

    public static void setCursorColor(EditText view, int color) {//REFLECTION METHOD USED
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

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }else {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            try{
                imm.hideSoftInputFromWindow(main.getWindowToken(),0);
            }catch (NullPointerException ignored){}
        }
    }

    public void showKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imManager.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }else{
            try{
                InputMethodManager imManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imManager.showSoftInput(main,InputMethodManager.SHOW_IMPLICIT);
            }catch (NullPointerException ignored){}
        }
    }


    public void showFeedBackDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.feedback_dialog, null);
        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
        edt.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        edt.setTextColor(textColor);
        edt.setHighlightColor(ColorUtils.lighten(themeColor,0.2));
        setCursorColor(edt,themeColor);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.feedback_title))
                .setMessage(getString(R.string.feedback_message))
                .setPositiveButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        if(!edt.getText().toString().equals("")) {
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();// Create a storage reference from our app
                            //FirebaseCrash.report(new Exception(edt.getText().toString()));
                            FirebaseCrash.log(edt.getText().toString());
                            ProgressDialog uploadingDialog = null;
                            try {
                                String systemInfo ="";
                                String macAddress = getMacAddr().replace(":","-");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    systemInfo = "System Info: " + "\n" + "("+ Build.MANUFACTURER + "||\n" + Build.BRAND + "||\n" + Build.DEVICE + "||\n" + Build.MODEL + "||\n"+ Build.HARDWARE + "||\n" + Build.VERSION.RELEASE + "||\n" + Build.VERSION.CODENAME + "||\n" + Build.VERSION.SDK_INT + "||\n" +  Build.VERSION.INCREMENTAL + "||\n" + Build.VERSION.SECURITY_PATCH + "||\n" + macAddress + ")";
                                }else {
                                    systemInfo = "System Info: " + "\n" + "(" + Build.MANUFACTURER + "||\n"+ Build.BRAND + "||\n"+ Build.DEVICE + "||\n"+ Build.MODEL + "||\n" + Build.HARDWARE + "||\n" + Build.VERSION.SDK_INT + "||\n" + Build.VERSION.RELEASE + "||\n" + Build.VERSION.INCREMENTAL + "||\n" + macAddress + ") ";
                                }
                                String feedback =  edt.getText().toString() + "---------------" + systemInfo + "\n" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date());
                                byte[] feedbackBytes =feedback.getBytes("UTF-8");
                                uploadingDialog = new ProgressDialog(MainActivity.this);
                                uploadingDialog.setTitle(getString(R.string.reporting_feedback_title));
                                uploadingDialog.setMessage(getString(R.string.please_wait));
                                uploadingDialog.setCancelable(false);
                                uploadingDialog.show();
                                String uniqueID = UUID.randomUUID().toString();
                                String timeStr = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date());
                                String msg = "";
                                if(edt.getText().toString().length() > 20){
                                    msg = edt.getText().toString().substring(0,20);
                                }else{
                                    msg = edt.getText().toString();
                                }
                                if(!msg.equals(edt.getText().toString())){
                                    msg = msg + "...";
                                }

                                StorageReference feedbackRef = storageRef.child("feedback/"+ msg + " " + Build.DEVICE + " " + macAddress + " " + timeStr + " " + uniqueID +".txt");
                                UploadTask uploadTask = feedbackRef.putBytes(feedbackBytes);
                                final ProgressDialog finalUploadingDialog = uploadingDialog;
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        dialog.dismiss();
                                        finalUploadingDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_message) + "\n" + exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                        // Handle unsuccessful uploads
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        dialog.dismiss();
                                        finalUploadingDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), getString(R.string.thx_for_feed), Toast.LENGTH_SHORT).show();
                                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    }
                                });
                            } catch (UnsupportedEncodingException e) {
                                dialog.dismiss();
                                uploadingDialog.dismiss();
                                Toast.makeText(getApplicationContext(), getString(R.string.error_message) + "\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }

                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //cancel
                    }
                }).setCancelable(true).setView(dialogView).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColor);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColor);
            }
        });
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

    /*
    public void displaySearchResults(final String filter){
        final Cursor cursor = todosql.getSearchResults(filter);
        if(cursor.getCount() == 0){
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.empty_search_result);
            todoList.removeAllViewsInLayout();//remove all items
            todoList.setAdapter(null);
        } else {
            emptyTextView.setVisibility(View.GONE);
            emptyTextView.setText("");
            final TodoListAdapter todoListAdapter = (new TodoListAdapter(cursor){
                @Override
                public void onBindViewHolder(TodoViewHolder holder, Cursor cursor) {
                    CheckBox multiSelectionBox = (CheckBox)todoView.findViewById(R.id.multiSelectionBox);
                    TextView todoText = (TextView)todoView.findViewById(R.id.titleText);
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
                    super.onBindViewHolder(holder, cursor);
                }
            });
            todoList.setAdapter(todoListAdapter);
        }
    }*/

    public void purchasePremium(){
        boolean isConnected = true;
        try {
            isConnected = new ConnectionDetector().execute().get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),getString(R.string.voice_recon_internet_err),Toast.LENGTH_SHORT).show();
        }
        if(isConnected){
            try{
                //Toast.makeText(getApplicationContext(),"ddd",Toast.LENGTH_LONG).show();
                purchaseProgressDialog =  new ProgressDialog(MainActivity.this);
                purchaseProgressDialog.setTitle(getString(R.string.please_wait));
                purchaseProgressDialog.setMessage(getString(R.string.purchasing));
                purchaseProgressDialog.setCancelable(false);
                purchaseProgressDialog.show();
                if (mHelper != null) mHelper.flagEndAsync();
                mHelper.launchPurchaseFlow(this, PREMIUM_UPGRADE_SKU, PURCHASE_PREMIUM_REQUEST_ID, mPurchaseFinishedListener, todoTableId);
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(),getString(R.string.purchase_failed),Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"8" +e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                //insertData(e.getLocalizedMessage());
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    purchaseProgressDialog.dismiss();
                }
                e.printStackTrace();
            }
        }else{
            //Toast.makeText(getApplicationContext(),"NO INTERNET",Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),getString(R.string.purchase_failed),Toast.LENGTH_LONG).show();
            //=Toast.makeText(getApplicationContext(),"9",Toast.LENGTH_SHORT).show();
        }
    }

    public static ArrayList<String> determineContainedTags(String text){
        int tagStartPos = text.indexOf("#",0);//find the position of the start point of the tag
        if(tagStartPos >= 0){//if contains tags
            ArrayList<String> tags = new ArrayList<String>();
            boolean isTagAtTheEnd = false;
            while(tagStartPos < text.length() - 1 && tagStartPos >= 0){//search and set color for all tags
                int tagEndPos = -1;//assume neither enter nor space exists
                if(text.indexOf(" ",tagStartPos) >= 0 && text.indexOf("\n",tagStartPos) >= 0){//contains both enter and space
                    tagEndPos = Math.min(text.indexOf(" ",tagStartPos),text.indexOf("\n",tagStartPos));//find the position of end point of the tag: space or line break
                }else if(text.indexOf(" ",tagStartPos) < 0){//contains only enter
                    tagEndPos = text.indexOf("\n",tagStartPos);
                }else {//contains only space
                    tagEndPos = text.indexOf(" ",tagStartPos);
                }
                if(tagEndPos < 0){//if the tag is the last section of the note
                    tagEndPos = text.length();
                    isTagAtTheEnd = true;
                }else if(tagEndPos == tagStartPos + 1){//if only one #, skip to next loop
                    continue;
                }
                String tag = text.toLowerCase().substring(tagStartPos,tagEndPos);//ignore case in tags//REMEMBER: SUBSTRING SECOND VARIABLE DOESN'T CONTAIN THE CHARACTER AT THAT POSITION
                tags.add(tag);
                if(isTagAtTheEnd){
                    break;
                }else {
                    tagStartPos = text.indexOf("#",tagEndPos);//set tagStartPos to the new tag start point
                }
            }
            return tags;
        }else return null;
    }

    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }

    public static String getCurrentTimeString(){
        Date nowTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        return dateFormat.format(nowTime);
    }

    public static String[] combineStringArray(String[] A, String[] B) {
        int aLen = A.length;
        int bLen = B.length;
        String[] C= new String[aLen+bLen];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }

    public static Canvas generatePDFCanvas(Canvas canvas){//not in use for now
        return null;
    }

    /*@Override
    public void onBackPressed() {
        switch (tabLayout.getSelectedTabPosition()){
            case 0:{
                importantFragment.onBackPressed();
            }
            case 1:{
                //mainFragment.onBackPressed();
            }
            case 2:{
                //clipboardFragment.onBackPressed();
            }
        }
    }*/

    //TODO FIX THEME SELECTOR SUMMARY TEXT COLOR
    //TODO SET SEARCHVIEW ANIMATION
    //TODO OPTIMIZE ALL CODE

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //System.out.println(intent.getAction()+" IDENTIFIII");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleVoiceSearch(intent);
        }
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

    public void query(String text) {//launch search
        if(tabLayout.getSelectedTabPosition() == 0){
            importantFragment.query(text);
        }else if(tabLayout.getSelectedTabPosition() == 1){
            mainFragment.query(text);
        }
        //System.out.println("calledquery" + " " + text);
    }

    /*public void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            float d = getApplicationContext().getResources().getDisplayMetrics().density;
            p.setMargins((int)(l*d), (int)(t*d), (int)(r*d), (int)(b*d));//dp to pixels
            v.requestLayout();
        }
    }*/

    public void handleGoogleAssistantCall(Intent intent){
        String note = "";
        if(intent.getStringExtra(Intent.EXTRA_TEXT) != null){
            note = intent.getStringExtra(Intent.EXTRA_TEXT);
            //System.out.println("not null");
        }
        if(note != null){
            Handler handler = new Handler();
            final String finalNote = note;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainFragment.insertData(finalNote);
                    Toast.makeText(getApplicationContext(),getString(R.string.note_added),Toast.LENGTH_LONG).show();
                }
            }, 450);
        }
        //System.out.println("intent real info: " + intent.getStringExtra(Intent.EXTRA_TEXT) + "{{" + note + "}}");
        getIntent().removeExtra(Intent.EXTRA_TEXT);
    }

    public void handleVoiceSearch(Intent intent){
        //onSearchRequested();

        final String query = intent.getStringExtra(SearchManager.QUERY);
        if(query == null){
            Toast.makeText(getApplicationContext(),getString(R.string.speech_to_text_failed),Toast.LENGTH_LONG).show();
            return;
        }
        SearchView searchView = (SearchView)toolbar.getMenu().findItem(R.id.todo_search).getActionView();
        if(query.trim().equals("")){
            Toast.makeText(getApplicationContext(),getString(R.string.speech_to_text_failed),Toast.LENGTH_LONG).show();
            return;
        }else {
            //Toast.makeText(getApplicationContext(),query,Toast.LENGTH_SHORT).show();
            hideKeyboard();
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

    public void onResume(){
        //setEdgeColor(todoList,themeColor);
        //todoList.setVisibility(View.VISIBLE);
        setColorPreferences();
        int size = menuNav.size();
        Intent voiceIntent = getIntent();
        String historySettingPref = "MII";
        String bep = "ANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiZZobdX3yEuQtssAfZ2AE69Agvit3KuCfR6ywZRlrcpjWKb5+P2oT72hEaw5FwDCsFquccZvt6R8nKBD1ucbl4PCgZvrUie9EFQR4YKxlp9iPogdreu8ifIjR/un9sFsiRGndmjhgJHMx66uKlDX7gyu9/EzuxFVajPCdbw7nQdK9XJzBripYLKY0w5/BLbKaOo7kmhSwiOlsRQwayIbXvUiYQb5ij17eFO/n4sebKNvixdIsaU3YaFlh/CbEpy/3P0UEHtrtb3B27pBa4+3kEriVc7uVBN+kYHmMQRMBgyjzKNwITDhHrP12qjlmrVk4LKehQVVDmPymB/C1/qTuwIDAQAB";
        historySettingPref += "BIjAN" + bep.substring(2,bep.length());
        /*adView.setAdListener(new AdListener(){//resume ad when got internet
            @Override
            public void onAdFailedToLoad(int i) {
                adView.destroy();
                adView.setVisibility(View.GONE);
                //Toast.makeText(getApplicationContext(),getString(R.string.voice_recon_internet_err),Toast.LENGTH_SHORT).show();
                receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            new ConnectionDetector().execute().get(1000, TimeUnit.MILLISECONDS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(isConnected){
                                    try {
                                        try {
                                            if (mHelper != null) mHelper.flagEndAsync();
                                            mHelper.queryInventoryAsync(mGotInventoryListener);
                                        }catch (Exception  e){
                                            //Toast.makeText(getApplicationContext(),"fis" + e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                            if (mHelper != null) mHelper.flagEndAsync();
                                            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                                                @Override
                                                public void onIabSetupFinished(IabResult result) {
                                                    //Toast.makeText(getApplicationContext(),String.valueOf(isConnected),Toast.LENGTH_SHORT).show();
                                                    //Toast.makeText(getApplicationContext(),"Finished",Toast.LENGTH_SHORT).show();
                                                    //System.out.println("finisheddd");
                                                    if((!result.isSuccess())||result.isFailure()){
                                                        //System.out.println("qazwsx"+3);
                                                        iapsetup = false;
                                                        return;
                                                    }
                                                    if(mHelper == null){
                                                        //System.out.println("qazwsx"+4);
                                                        //System.out.println("finisheddd");

                                                        iapsetup = false;
                                                        return;
                                                    }
                                                    try {
                                                        if (mHelper != null) mHelper.flagEndAsync();
                                                        mHelper.queryInventoryAsync(mGotInventoryListener);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        //System.out.println("qazwsx"+5);
                                                        iapsetup = false;
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        //Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            }
                        },1005);
                    }
                };
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                registerReceiver(receiver,filter);
                super.onAdFailedToLoad(i);
            }
        });*///ad function paused!
        try {
            if (voiceIntent != null && voiceIntent.getAction() != null){
                if(voiceIntent.getAction().equals(getString(R.string.google_now_request_code)) && voiceIntent.getStringExtra(Intent.EXTRA_TEXT) != null) {
                    //System.out.println("fucking text: ");
                    handleGoogleAssistantCall(voiceIntent);
                }if(voiceIntent.getAction().equals(Intent.ACTION_SEND) && voiceIntent.getStringExtra(Intent.EXTRA_TEXT) != null){
                    if(!voiceIntent.getStringExtra(Intent.EXTRA_TEXT).trim().equals("")){
                        mainFragment.insertData(voiceIntent.getStringExtra(Intent.EXTRA_TEXT));
                        Toast.makeText(getApplicationContext(),getString(R.string.note_added),Toast.LENGTH_LONG).show();
                        finish();
                    }
                    getIntent().removeExtra(Intent.EXTRA_TEXT);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        /*if(sharedPreferences.getBoolean(getString(R.string.order_key),true)){
            sort = "_id DESC";
        }*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        for (int i = 0; i < size; i++) {
            menuNav.getItem(i).setChecked(false);
        }
        if(isPremium){
            unlockPremium();
            Log.i("IAP","PREMIUM ALREADY PURCHASED, UNLOCK FEATURES");
        }
        //else{
            //paused ad//adView= (AdView)findViewById(R.id.bannerAdView);
            //paused ad//AdRequest adRequest = new AdRequest.Builder().build();
            //paused ad//adView.loadAd(adRequest);
        //}
        if(!sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) != null){
            navigationView.getMenu().removeItem(R.id.history);
        }if(sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) == null){
            //Toast.makeText(getApplicationContext(),"f",Toast.LENGTH_LONG).show();
            menuNav = navigationView.getMenu();
            menuNav.add(R.id.nav_category_main,R.id.history,0,getString(R.string.nav_history)).setIcon(R.drawable.ic_history_black_24dp);
        }
        //set dynamic tag columns in the navigation menu
        if(todosql.returnTagsForNavMenu() != null){
            final ArrayList<String> dynamicTags = todosql.returnTagsForNavMenu();
            ArrayList<String> dynamicTagColors = todosql.returnTagColorsForNavMenu();
            //todo add navigationView dynamic expandable tag item
            for(int i = 0; i < dynamicTags.size(); i++){
                menuNav.add(R.id.nav_category_main,R.id.dynamic_tag_1,4,dynamicTags.get(i));
                Spannable spannable = new SpannableString(dynamicTags.get(i));
                spannable.setSpan(new TextAppearanceSpan(null,Typeface.ITALIC,-1,
                        new ColorStateList(new int[][] {new int[] {}},
                                new int[] {Color.parseColor(dynamicTagColors.get(i))})
                        ,null), 0, dynamicTags.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//change tag title color
                menuNav.getItem(4).setTitle(spannable);
                final int finalI = i;
                menuNav.getItem(4).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent tagIntent = new Intent(MainActivity.this, TagsActivity.class);
                        tagIntent.putExtra("TAG_VALUE",dynamicTags.get(finalI));
                        startActivity(tagIntent);
                        return false;
                    }
                });
            }
        }
        super.onResume();
    }

    public void onDestroy(){
        if(todosql.isOpen()) todosql.stopService();
        hideKeyboard();
        //paused ad//adView= (AdView)findViewById(R.id.bannerAdView);
        //paused ad//adView.destroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        if (mService != null) {
            unbindService(mServiceConn);
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(tabLayout.getSelectedTabPosition() == 1){
            mainFragment.setOutOfSelectionMode();
        }else if (tabLayout.getSelectedTabPosition() == 0){
            importantFragment.setOutOfSelectionMode();
        }
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

        else if (id == R.id.tags){
            hideKeyboard();
            Intent intent = new Intent(MainActivity.this, TagSelectionActivity.class);
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
            if(iapsetup){
                hideKeyboard();
                purchasePremium();
                //TEMPORARY CHANGE, CHANGE BACK BEFORE PUBLISH!!!$$$
                //isAdRemoved = true;//
                //removeAd();//
            }else {
                Toast.makeText(getApplicationContext(),getString(R.string.purchase_unavailable),Toast.LENGTH_LONG).show();
            }

        }
        else if (id == R.id.feedback){
            showFeedBackDialog();
        }
        /*else if(id == R.id.consume){
            try {
                consume =true;
                mHelper.queryInventoryAsync(mGotInventoryListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PURCHASE_PREMIUM_REQUEST_ID){
            if(resultCode == RESULT_OK){
                if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                    // not handled, so handle it ourselves (here's where you'd
                    // perform any handling of activity results not related to in-app
                    // billing...
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
            if(resultCode == RESULT_CANCELED){//purchase cancelled
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                    purchaseProgressDialog.dismiss();
                }
                isPremium = false;
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //todo empty for now, handles calls to new fragment
    }

    public class ConnectionDetector extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {
            if (networkConnectivity()) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
                    urlc.setRequestProperty("User-Agent", "Android");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.setReadTimeout(2000);
                    urlc.connect();
                    // networkcode2 = urlc.getResponseCode();
                    return (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
                } catch (IOException e) {
                    return false;
                }
            } else{
                return false;
            }
        }
        private boolean networkConnectivity() {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aResult) {
            //isConnected = isConn;
            super.onPostExecute(aResult);
        }
    }
}

