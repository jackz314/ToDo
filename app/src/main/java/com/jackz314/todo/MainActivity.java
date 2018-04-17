package com.jackz314.todo;


import android.app.Notification;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jackz314.dateparser.DateGroup;
import com.jackz314.dateparser.Parser;
import com.jackz314.todo.iap_utils.IabHelper;
import com.jackz314.todo.iap_utils.IabResult;
import com.jackz314.todo.iap_utils.Inventory;
import com.jackz314.todo.iap_utils.Purchase;
import com.jackz314.todo.utils.ColorUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.jackz314.todo.DatabaseManager.DATE_FORMAT;
import static com.jackz314.todo.DatabaseManager.RECENT_REMIND_TIME;
import static com.jackz314.todo.DatabaseManager.RECURRENCE_STATS;
import static com.jackz314.todo.DatabaseManager.REMIND_TIMES;
import static com.jackz314.todo.DatabaseManager.TITLE;

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
//todo edge effect doesn't work at first scroll, minor problem
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ImportantFragment.OnFragmentInteractionListener, ClipboardFragment.OnFragmentInteractionListener, MainFragment.OnFragmentInteractionListener{
    //paused ad//private static final String REMOVE_AD_SKU = "todo_iap_remove_ad";
    private static final String PREMIUM_UPGRADE_SKU = "todo_iap_premium";
    //private static final String[] PROJECTION = new String[]{ID, TITLE};//"REPLACE (title, '*', '')"
    //private static final String SELECTION = "REPLACE (title, '*', '')" + " LIKE ?";
    //paused ad//static int REMOVE_REQUEST_ID =1022;
    static int PURCHASE_PREMIUM_REQUEST_ID = 1025;
    public static String REMINDER_NOTIFICATION_ID = "reminder_notification_id";
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
    protected MainFragment.OnMainBackPressedListener onMainBackPressedListener;
    protected ImportantFragment.OnImportantBackPressedListener onImportantBackPressedListener;
    protected ClipboardFragment.OnClipboardBackPressedListener onClipboardBackPressedListener;
    protected MainFragment.OnMainQueryListener onMainQueryListener;
    protected ImportantFragment.OnImportantQueryListener onImportantQueryListener;

    IabHelper mHelper;
    int exit=0;
    boolean justex = false;
    DatabaseManager todosql;
    boolean selectAll = false, unSelectAll = false;
    SharedPreferences sharedPreferences;
    String oldResult = "";
    int themeColor,textColor,backgroundColor,textSize;
    ViewPager viewPager;
    FragmentPagerAdapter pagerAdapter;
    ImportantFragment importantFragment;
    MainFragment mainFragment;
    ClipboardFragment clipboardFragment;
    CoordinatorLayout main;
    DrawerLayout mDrawerLayout;
    TodoListAdapter todoListAdapter;
    ActionBarDrawerToggle mDrawerToggle;
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
        //Fragment currentFragment = getFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem);
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        navigationView = findViewById(R.id.nav_view);
        //speechRecognizer.setRecognitionListener(new speechListener());
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabs_layout);
        todoTableId = "0x397821dc97276";
        todosql = new DatabaseManager(this);
        setSupportActionBar(toolbar);
        main = findViewById(R.id.total_main_bar);
        //set tabs
        viewPager = findViewById(R.id.pager);
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(),getApplicationContext());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);
        importantFragment = (ImportantFragment)pagerAdapter.getItem(0);
        mainFragment = (MainFragment)pagerAdapter.getItem(1);
        clipboardFragment = (ClipboardFragment) pagerAdapter.getItem(2);
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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            //Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
               // EditText input = mainFragment.input; //todo this doesn't work, fix it
//                if(input.isCursorVisible() && input.getVisibility() == View.VISIBLE){
               //     showKeyboard();
              //  }
                super.onDrawerClosed(view);
                //getActionBar().setTitle(title);
            }

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                hideKeyboard();
                menuNav.removeGroup(R.id.dynamic_tags);
                if(todosql.getTagsForNavMenu() != null){
                    final ArrayList<String> dynamicTags = todosql.getTagsForNavMenu();
                    ArrayList<String> dynamicTagColors = todosql.getTagColorsForNavMenu();
                    //todo fix navigationView dynamic expandable tag item
                    for(int i = 0; i < dynamicTags.size(); i++){
                        Spannable spannable = new SpannableString(dynamicTags.get(i));
                        spannable.setSpan(new TextAppearanceSpan(null,Typeface.ITALIC,-1,
                                new ColorStateList(new int[][] {new int[] {}},
                                        new int[] {Color.parseColor(dynamicTagColors.get(i))})
                                ,null), 0, dynamicTags.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//change tag title color
                        final int finalI = i;
                        menuNav.add(R.id.dynamic_tags,R.id.dynamic_tag_1,2,dynamicTags.get(i)).setTitle(spannable).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                String tag = dynamicTags.get(finalI);
                                Intent tagIntent = new Intent(MainActivity.this, TagsActivity.class);
                                tagIntent.putExtra("TAG_VALUE",tag);
                                startActivity(tagIntent);
                                return false;
                            }
                        });
                    }
                }
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(title);
            }
        };
        mDrawerLayout.setDrawerListener(toggle);
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
            input.setText(databaseManager.getOneTitleInTODO(String.valueOf(id)));
            input.requestFocus();
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    input.requestFocus();
                }
            }, 230);
            input.performClick();
        }if(item.getItemId() == DELETE_CONTEXT_ID) {
            final String deleteContent = databaseManager.getOneTitleInTODO(String.valueOf(id));
            databaseManager.deleteNote(id);
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

    public void setOnMainQueryListener(MainFragment.OnMainQueryListener onMainQueryListener){
        this.onMainQueryListener = onMainQueryListener;
    }

    public void setOnImportantQueryListner(ImportantFragment.OnImportantQueryListener onImportantQueryListener){
        this.onImportantQueryListener = onImportantQueryListener;
    }

    public void setOnMainBackPressedListener(MainFragment.OnMainBackPressedListener onBackPressedListener) {
        this.onMainBackPressedListener = onBackPressedListener;
    }

    public void setOnImportantBackPressedListener(ImportantFragment.OnImportantBackPressedListener onBackPressedListener) {
        this.onImportantBackPressedListener = onBackPressedListener;
    }
    public void setOnClipboardBackPressedListener(ClipboardFragment.OnClipboardBackPressedListener onBackPressedListener) {
        this.onClipboardBackPressedListener = onBackPressedListener;
    }

    boolean verifyDeveloperPayload(Purchase p) {
        if(p.getDeveloperPayload() != null && p.getDeveloperPayload().contains("0x397821dc97276")){
            return p.getDeveloperPayload().equals(
                    "0x397821dc97276" +
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
                            "GcIdL4lDdR");
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
                TextView navHeadText = navHeader.findViewById(R.id.navHeadText);
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
            setEdittextHandleColor(view, color);
        } catch (Exception ignored) {
        }
    }

    /**
     * Set the color of the handles when you select text in a
     * {@link android.widget.EditText} or other view that extends {@link TextView}.
     *
     * @param view
     *     The {@link TextView} or a {@link View} that extends {@link TextView}.
     * @param color
     *     The color to set for the text handles
     */
    public static void setEdittextHandleColor(TextView view, int color) {
        try {
            Field editorField = TextView.class.getDeclaredField("mEditor");
            if (!editorField.isAccessible()) {
                editorField.setAccessible(true);
            }

            Object editor = editorField.get(view);
            Class<?> editorClass = editor.getClass();

            String[] handleNames = {"mSelectHandleLeft", "mSelectHandleRight", "mSelectHandleCenter"};
            String[] resNames = {"mTextSelectHandleLeftRes", "mTextSelectHandleRightRes", "mTextSelectHandleRes"};

            for (int i = 0; i < handleNames.length; i++) {
                Field handleField = editorClass.getDeclaredField(handleNames[i]);
                if (!handleField.isAccessible()) {
                    handleField.setAccessible(true);
                }

                Drawable handleDrawable = (Drawable) handleField.get(editor);

                if (handleDrawable == null) {
                    Field resField = TextView.class.getDeclaredField(resNames[i]);
                    if (!resField.isAccessible()) {
                        resField.setAccessible(true);
                    }
                    int resId = resField.getInt(view);
                    handleDrawable = view.getResources().getDrawable(resId);
                }

                if (handleDrawable != null) {
                    Drawable drawable = handleDrawable.mutate();
                    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    handleField.set(editor, drawable);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        final EditText edt = dialogView.findViewById(R.id.edit1);
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
                                Drawable progressDrawable = new ProgressBar(getApplicationContext()).getIndeterminateDrawable().mutate();//set a theme colored progress drawable for the ProgressDialog
                                progressDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
                                uploadingDialog.setIndeterminateDrawable(progressDrawable);
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
        final Cursor cursor = databaseManager.getSearchResults(filter);
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
                Drawable progressDrawable = new ProgressBar(getApplicationContext()).getIndeterminateDrawable().mutate();//set a theme colored progress drawable for the ProgressDialog
                progressDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
                purchaseProgressDialog.setIndeterminateDrawable(progressDrawable);
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

    public interface OnBackPressedListener {
        void doBack();
    }

    @NonNull
    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }

    @NonNull
    public static String removeSubstring(String s, int from, int to) {
        return s.substring(0, from + 1) + s.substring(to);
    }

    //todo add "wk", "yr", and "&" (relative to "wks", "yrs", and "and") to DateLexer.g and compile a new one
    public static String getDateString(String str, String... addOnOps){//addOnOps: startPos (inclusive),endPos (exclusive),replaceWithString
        Parser parser = new Parser();
        List groups = parser.parse(str, getCurrentTime());
        if(groups.size() <= 0){
            String strLow = str.toLowerCase();
            if(strLow.contains("every sec") ||
                    strLow.contains("every min") ||
                    strLow.contains("every hr") ||
                    strLow.contains("every hour") ||
                    strLow.contains("every day") ||
                    strLow.contains("every wk") ||
                    strLow.contains("every week") ||
                    strLow.contains("every month") ||
                    strLow.contains("every yr") ||
                    strLow.contains("every year")){
                return getDateString(insertToString(str," 1",strLow.indexOf("every ") + 5), Integer.toString(strLow.indexOf("every ") + 5), Integer.toString(strLow.indexOf("every ") + 7));
            }else if(strLow.contains("everyday")){
                return getDateString(insertToString(str," 1 ",strLow.indexOf("everyday") + 5), Integer.toString(strLow.indexOf("every ") + 5), Integer.toString(strLow.indexOf("every ") + 8));
            }else if(strLow.contains("weekday")){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str);
                if(strLow.contains("weekdays")){
                    stringBuilder.replace(strLow.indexOf("weekdays"),strLow.indexOf("weekdays") + 8,"Mon and Tue and Wed and Thu and Fri");
                    return getDateString(stringBuilder.toString(), Integer.toString(strLow.indexOf("weekdays")), Integer.toString(strLow.indexOf("weekdays") + 35));//34 == weekdays string length + 1 because of exclusive
                }else {
                    stringBuilder.replace(strLow.indexOf("weekday"),strLow.indexOf("weekday") + 7,"Mon and Tue and Wed and Thu and Fri");
                    return getDateString(stringBuilder.toString(), Integer.toString(strLow.indexOf("weekday")), Integer.toString(strLow.indexOf("weekday") + 35));//34 == weekdays string length + 1 because of exclusive
                }
            }else if(strLow.contains("weekend")){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str);
                if(strLow.contains("weekends")){
                    stringBuilder.replace(strLow.indexOf("weekends"),strLow.indexOf("weekends") + 8,"Sat and Sun");
                    return getDateString(stringBuilder.toString(), Integer.toString(strLow.indexOf("weekends")), Integer.toString(strLow.indexOf("weekends") + 11));// == weekends string length + 1 because of exclusive
                }else {
                    stringBuilder.replace(strLow.indexOf("weekend"),strLow.indexOf("weekend") + 7,"Sat and Sun");
                    return getDateString(stringBuilder.toString(), Integer.toString(strLow.indexOf("weekend")), Integer.toString(strLow.indexOf("weekend") + 11));// == weekends string length + 1 because of exclusive
                }
            }else {
                if(str.replace("!","").isEmpty()){//not a date string but a action string
                    return "";
                }else {
                    return null;
                }
            }
        }
        StringBuilder dateString = new StringBuilder();
        dateString.append("");//avoid null
        for(Object groupF : groups) {
            DateGroup group = (DateGroup)groupF;
            String matchingValue = group.getText();
            dateString.append(matchingValue);
        }
        String prefix = "", suffix = "";
        DateGroup typicalGroup = (DateGroup)groups.get(0);
        prefix = typicalGroup.getPrefix(str.length());
        suffix = typicalGroup.getSuffix(str.length());
        String processedPrefix = prefix;
        if(prefix.startsWith(" ")){
            processedPrefix = prefix.substring(1);
        }
        if(!processedPrefix.replace("!","").isEmpty() && !processedPrefix.toLowerCase().replace("!","").equals("every ")){
            return null;//invalid dateString, return null
        }else {
            dateString.insert(0,prefix);
        }

        if(dateString.toString().contains("@")){
            dateString = new StringBuilder(dateString.substring(0,dateString.indexOf("@")));
        }

        if(suffix.startsWith("!")){
            int impIndicatorCount = countContinuousOccurrences(suffix,"!");
            dateString.append(suffix.substring(0,impIndicatorCount + 1));
        }

        //System.out.println("ORIGINAL STR:" + str + "\n" + "DATESTRING:" + dateString);
        if(addOnOps != null){
            if(addOnOps.length == 2){//remove
                dateString.delete(Integer.valueOf(addOnOps[0]),Integer.valueOf(addOnOps[1]));
            }else if(addOnOps.length == 3){//replace
                dateString.replace(Integer.valueOf(addOnOps[0]),Integer.valueOf(addOnOps[1]),addOnOps[2]);
            }
        }
        return dateString.toString();
    }

    public static String generateRecurrenceStr(ArrayList<String> recurStat){
        String recurFinalStr = "";
        if(recurStat != null && recurStat.size() == 3){
            String recurUnit = recurStat.get(0);
            String recurValue = recurStat.get(1);
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.getDefault());
            Date recentRecurrenceDate = null;
            try {
                recentRecurrenceDate = dateFormat.parse(recurStat.get(2));
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
            if(recurValue.equals("0")){
                recurValue = "1";
            }
            //Calendar calendar = Calendar.getInstance();
            //calendar.setTime(recentRecurrenceDate);
            switch (recurUnit){
                case "SECOND":{
                    recurFinalStr = "second";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + "second";//plural
                    }
                    break;
                }
                case "MINUTE":{
                    recurFinalStr = "minute";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " mins";//plural
                    }
                    break;
                }case "HOUR":{
                    recurFinalStr = "hour";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " hrs";//plural
                    }
                    SimpleDateFormat format = new SimpleDateFormat("mm", Locale.getDefault());
                    String exactTime = format.format(recentRecurrenceDate);
                    if(!exactTime.endsWith(":00")){
                        recurFinalStr += " (" + exactTime + "')";
                    }
                    break;
                }case "DAY":{//day
                    recurFinalStr = "day";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " days";//plural
                    }
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String exactTime = format.format(recentRecurrenceDate);
                    if(exactTime.endsWith(":00")){//whole clock, then change to XX AM/PM
                        SimpleDateFormat wholeFormat = new SimpleDateFormat("hh aa", Locale.getDefault());
                        exactTime = wholeFormat.format(recentRecurrenceDate);
                    }
                    recurFinalStr += " (" + exactTime + ")";
                    break;
                }case "WEEK":{//week use normal mon,tue,wed until it's more than every week (like every 2 weeks), then use x weeks (mon/tue/wed)
                    SimpleDateFormat format = new SimpleDateFormat("EEEEE", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " weeks";
                        format = new SimpleDateFormat("EEE", Locale.getDefault());
                        recurFinalStr += " (" + format.format(recentRecurrenceDate) + ")";
                    }
                    break;
                }case "MONTH":{//month
                    recurFinalStr = "month";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " months";//plural
                    }
                    SimpleDateFormat format = new SimpleDateFormat("d", Locale.getDefault());
                    String exactDay = addCountSuffix(format.format(recentRecurrenceDate));
                    recurFinalStr += " (" + exactDay + ")";
                    break;
                }case "YEAR":{//year
                    recurFinalStr = "year";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " years";//plural
                    }
                    SimpleDateFormat format = new SimpleDateFormat("MMM d '(day' D')'", Locale.getDefault());
                    recurFinalStr += " (" + format.format(recentRecurrenceDate) + ")";
                    break;
                }case "HOURS_OF_DAY|MINUTES_OF_HOUR":{
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String exactTime = format.format(recentRecurrenceDate);
                    if(exactTime.endsWith(":00")){//whole clock, then change to XX AM/PM
                        SimpleDateFormat wholeFormat = new SimpleDateFormat("hh aa", Locale.getDefault());
                        exactTime = wholeFormat.format(recentRecurrenceDate);
                    }
                    recurFinalStr = exactTime;
                    break;
                }case "HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE":{
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    break;
                }case "DAY_OF_WEEK":{//day of a week
                    SimpleDateFormat format = new SimpleDateFormat("EEEEE", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " " + format.format(recentRecurrenceDate) + "s";
                    }
                    SimpleDateFormat exactTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String exactTime = exactTimeFormat.format(recentRecurrenceDate);
                    if(exactTime.endsWith(":00")){//whole clock, then change to XX AM/PM
                        SimpleDateFormat wholeTimeFormat = new SimpleDateFormat("hh aa", Locale.getDefault());
                        exactTime = wholeTimeFormat.format(recentRecurrenceDate);
                    }
                    recurFinalStr += " (" + exactTime + ")";
                    break;
                }case "DAY_OF_WEEK[OF]MONTH_OF_YEAR":{//Xth WEEKDAY of MONTH
                    SimpleDateFormat format = new SimpleDateFormat("EEE 'of' MMM", Locale.getDefault());
                    if(Integer.valueOf(recurValue) > 4) recurValue = "4";//bigger than 4 would all be considered as 4 both grammatically and operationally, so change it here
                    recurValue = addCountSuffix(recurValue);
                    recurFinalStr = recurValue + " " + format.format(recentRecurrenceDate);
                    SimpleDateFormat exactTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String exactTime = exactTimeFormat.format(recentRecurrenceDate);
                    if(exactTime.endsWith(":00")){//whole clock, then change to XX AM/PM
                        SimpleDateFormat wholeTimeFormat = new SimpleDateFormat("hh aa", Locale.getDefault());
                        exactTime = wholeTimeFormat.format(recentRecurrenceDate);
                    }
                    recurFinalStr += " (" + exactTime + ")";
                    break;
                }case "MONTH_OF_YEAR":{
                    SimpleDateFormat format = new SimpleDateFormat("MMMMM", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    SimpleDateFormat exactDayFormat = new SimpleDateFormat("d", Locale.getDefault());
                    String exactDay = addCountSuffix(exactDayFormat.format(recentRecurrenceDate));
                    recurFinalStr += " (" + exactDay + ")";
                    break;
                }case "MONTH_OF_YEAR|DAY_OF_MONTH":{//day of a year
                    SimpleDateFormat format = new SimpleDateFormat("MMM d", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    SimpleDateFormat exactTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String exactTime = exactTimeFormat.format(recentRecurrenceDate);
                    if(exactTime.endsWith(":00")){//whole clock, then change to XX AM/PM
                        SimpleDateFormat wholeTimeFormat = new SimpleDateFormat("hh aa", Locale.getDefault());
                        exactTime = wholeTimeFormat.format(recentRecurrenceDate);
                    }
                    recurFinalStr += " (" + exactTime + ")";
                    break;
                }case "MONTH_OF_YEAR|DAY_OF_MONTH|HOURS_OF_DAY|MINUTES_OF_HOUR":{
                    SimpleDateFormat format = new SimpleDateFormat("MMM d", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String timeStr = timeFormat.format(recentRecurrenceDate);
                    if(timeStr.endsWith(":00")){//whole clock, then change to XX AM/PM
                        SimpleDateFormat wholeTimeFormat = new SimpleDateFormat("hh aa", Locale.getDefault());
                        timeStr = wholeTimeFormat.format(recentRecurrenceDate);
                    }
                    recurFinalStr += " " + timeStr;
                    break;
                }case "MONTH_OF_YEAR|DAY_OF_MONTH|HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE":{
                    SimpleDateFormat format = new SimpleDateFormat("MMM d HH:mm:ss", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    break;
                }default:{
                    if(recurUnit.isEmpty()){//no unit stored then use default parse method
                        SimpleDateFormat format = new SimpleDateFormat("MMM d HH:mm:ss", Locale.getDefault());
                        recurFinalStr = format.format(recentRecurrenceDate);
                    }else {//parse out holiday names
                        recurFinalStr = recurUnit.replace("_", " ").toLowerCase();
                        recurFinalStr = MainActivity.capitalize(recurFinalStr);
                        if(recurFinalStr.contains("Mothers") || recurFinalStr.contains("Fathers")){
                            recurFinalStr = insertToString(recurFinalStr,"'",5);//insert "'" to Mothers/Fathers
                        }
                        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
                        recurFinalStr += "(" + monthDayFormat.format(recentRecurrenceDate) + ")";
                    }
                    break;
                }
            }
        }
        return recurFinalStr;
    }

    public static String generateSimpleRecurrenceStr(ArrayList<String> recurStat, boolean... containWeekDayIndicator){
        String recurFinalStr = "";
        if(recurStat != null && recurStat.size() == 3){
            String recurUnit = recurStat.get(0);
            String recurValue = recurStat.get(1);
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.getDefault());
            Date recentRecurrenceDate = null;
            try {
                recentRecurrenceDate = dateFormat.parse(recurStat.get(2));
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
            if(recurValue.equals("0")){
                recurValue = "1";
            }
            //Calendar calendar = Calendar.getInstance();
            //calendar.setTime(recentRecurrenceDate);
            switch (recurUnit){
                case "SECOND":{
                    recurFinalStr = "second";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + "second";//plural
                    }
                    break;
                }
                case "MINUTE":{
                    recurFinalStr = "minute";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " mins";//plural
                    }
                    break;
                }case "HOUR":{
                    recurFinalStr = "hour";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " hrs";//plural
                    }
                    break;
                }case "DAY":{//day
                    recurFinalStr = "day";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " days";//plural
                    }
                    break;
                }case "WEEK":{//week use normal mon,tue,wed until it's more than every week (like every 2 weeks), then use x weeks (mon/tue/wed)
                    if(containWeekDayIndicator != null && !containWeekDayIndicator[0]){//don't add weekday indicator
                        recurFinalStr = "week";
                        if(!recurValue.equals("1")){
                            recurFinalStr = recurValue + " weeks";
                        }
                    }else {//add weekday indicator(default)
                        SimpleDateFormat format = new SimpleDateFormat("EEEEE", Locale.getDefault());
                        recurFinalStr = format.format(recentRecurrenceDate);
                        if(!recurValue.equals("1")){
                            recurFinalStr = recurValue + " weeks";
                        }
                        format = new SimpleDateFormat("EEE", Locale.getDefault());
                        recurFinalStr += " (" + format.format(recentRecurrenceDate) + ")";
                    }
                    break;
                }case "MONTH":{//month
                    recurFinalStr = "month";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " months";//plural
                    }
                    break;
                }case "YEAR":{//year
                    recurFinalStr = "year";
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " years";//plural
                    }
                    SimpleDateFormat format = new SimpleDateFormat("'day' D", Locale.getDefault());
                    recurFinalStr += " (" + format.format(recentRecurrenceDate) + ")";
                    break;
                }case "HOURS_OF_DAY|MINUTES_OF_HOUR":
                case "HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE":{
                    recurFinalStr = "day";
                    break;
                }case "DAY_OF_WEEK":{//day of a week
                    SimpleDateFormat format = new SimpleDateFormat("EEEEE", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    if(!recurValue.equals("1")){
                        recurFinalStr = recurValue + " " + format.format(recentRecurrenceDate) + "s";
                    }
                    break;
                }case "DAY_OF_WEEK[OF]MONTH_OF_YEAR":{//Xth WEEKDAY of MONTH
                    SimpleDateFormat format = new SimpleDateFormat("EEE 'of' MMM", Locale.getDefault());
                    if(Integer.valueOf(recurValue) > 4) recurValue = "4";//bigger than 4 would all be considered as 4 both grammatically and operationally, so change it here
                    recurValue = addCountSuffix(recurValue);
                    recurFinalStr = recurValue + " " + format.format(recentRecurrenceDate);
                    break;
                }case "MONTH_OF_YEAR":{
                    SimpleDateFormat format = new SimpleDateFormat("MMMMM", Locale.getDefault());
                    recurFinalStr = format.format(recentRecurrenceDate);
                    break;
                }case "MONTH_OF_YEAR|DAY_OF_MONTH":{//day of a year
                    recurFinalStr = "year on this day";
                    break;
                }case "MONTH_OF_YEAR|DAY_OF_MONTH|HOURS_OF_DAY|MINUTES_OF_HOUR":
                case "MONTH_OF_YEAR|DAY_OF_MONTH|HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE":{
                    recurFinalStr = "year on this day at this time";
                    break;
                }default:{
                    if(recurUnit.isEmpty()){//no unit stored then use default parse method
                        recurFinalStr = "Time";
                    }else {//parse out holiday names
                        recurFinalStr = recurUnit.replace("_", " ").toLowerCase();
                        recurFinalStr = MainActivity.capitalize(recurFinalStr);
                        if(recurFinalStr.contains("Mothers") || recurFinalStr.contains("Fathers")){
                            recurFinalStr = insertToString(recurFinalStr,"'",5);//insert "'" to Mothers/Fathers
                        }
                    }
                    break;
                }
            }
        }
        return recurFinalStr;
    }

    public static Date generateNextRecurDate(ArrayList<String> recurStat){
        if(recurStat != null && recurStat.size() == 3) {
            String recurUnit = recurStat.get(0);
            String recurValue = recurStat.get(1);
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(recurStat.get(2));
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            if(recurValue.equals("0")){
                recurValue = "1";
            }
            switch (recurUnit) {
                case "SECOND": {
                    calendar.add(Calendar.SECOND, Integer.valueOf(recurValue));
                    break;
                }case "MINUTE": {
                    calendar.add(Calendar.MINUTE, Integer.valueOf(recurValue));
                    break;
                }case "HOUR": {
                    calendar.add(Calendar.HOUR, Integer.valueOf(recurValue));
                    break;
                }case "DAY": {//day
                    calendar.add(Calendar.DAY_OF_YEAR, Integer.valueOf(recurValue));
                    break;
                }case "HOURS_OF_DAY|MINUTES_OF_HOUR":
                case "HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE":{
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                }case "WEEK":
                case "DAY_OF_WEEK":{//week
                    calendar.add(Calendar.WEEK_OF_YEAR, Integer.valueOf(recurValue));
                    break;
                }case "MONTH": {//month
                    calendar.add(Calendar.MONTH, Integer.valueOf(recurValue));
                    break;
                }case "YEAR": {//year
                    calendar.add(Calendar.YEAR, Integer.valueOf(recurValue));
                    break;
                }case "DAY_OF_WEEK[OF]MONTH_OF_YEAR":{//Xth WEEKDAY of MONTH
                    break;
                }case "MONTH_OF_YEAR":
                case "MONTH_OF_YEAR|DAY_OF_MONTH":
                case "MONTH_OF_YEAR|DAY_OF_MONTH|HOURS_OF_DAY|MINUTES_OF_HOUR":
                case "MONTH_OF_YEAR|DAY_OF_MONTH|HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE":{
                    calendar.add(Calendar.YEAR, 1);
                    break;
                }default: {
                    if(!recurUnit.isEmpty()){//most likely is a holiday/festival
                        Parser parser = new Parser();
                        //parse out the date of this holiday next year
                        List groups = parser.parse(recurUnit.replace("_", " ") + " " + Integer.toString(calendar.get(Calendar.YEAR) + 1), currentDate);
                        if(groups.size() > 0){
                            DateGroup group = (DateGroup)groups.get(0);
                            Date nextYearHolidayDate = group.getDates().get(0);
                            calendar.setTime(nextYearHolidayDate);
                        }else calendar.add(Calendar.YEAR, 1);//in case it failed to parse the next year holiday date, just add one year
                    }
                    break;
                }
            }
            return calendar.getTime();
        }else return null;
    }
//todo dateParser add support for "&"
    public static String getProperDateString(String str){//addedInfo: startPos,endPos
        String strLow = str.toLowerCase();
        if(strLow.startsWith("every sec") ||
                strLow.startsWith("every min") ||
                strLow.startsWith("every hr") ||
                strLow.startsWith("every hour") ||
                strLow.startsWith("every day") ||
                strLow.startsWith("every wk") ||
                strLow.startsWith("every week") ||
                strLow.startsWith("every month") ||
                strLow.startsWith("every yr") ||
                strLow.startsWith("every year")){
            return getDateString(insertToString(str," 1",5));
        }else if(strLow.startsWith("everyday")){
            return getDateString(insertToString(str," 1 ",5));
        }else if(strLow.startsWith("weekdays")){
            return getDateString("Mon and Tue and Wed and Thu and Fri " + str.substring(9));
        }else if (strLow.startsWith("weekday")){
            return getDateString("Mon and Tue and Wed and Thu and Fri " + str.substring(8));
        }else if(strLow.startsWith("weekends")){
            return getDateString("Sat and Sun " + str.substring(9));
        }else if (strLow.startsWith("weekend")){
            return getDateString("Sat and Sun " + str.substring(8));
        }else{
            return getDateString(str);
        }
    }

    public static ArrayList<String> updateRecurStatWithNewDate(ArrayList<String> oldRecurStat, Date newDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        ArrayList<String> newRecurStat = new ArrayList<>();
        newRecurStat.add(0,oldRecurStat.get(0));
        newRecurStat.add(1,oldRecurStat.get(1));
        newRecurStat.add(2,dateFormat.format(newDate));
        return newRecurStat;
    }

    public static int countOccurrences(String str, String sub) {
        if (str.isEmpty() || sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    public static int countContinuousOccurrences(String str, String sub){
        if (str.isEmpty() || sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while (str.substring(idx,idx + sub.length()).equals(sub)) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    public static String getCurrentTimeString(){
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        return dateFormat.format(new Date());
    }

    public static Date getCurrentTime(){
        return new Date();
    }

    public static Calendar getCurrentCalendarTime(){
        return Calendar.getInstance();
    }

    public static String[] combineStringArray(String[] A, String[] B) {
        if(A != null && B != null){
            int aLen = A.length;
            int bLen = B.length;
            String[] C= new String[aLen+bLen];
            System.arraycopy(A, 0, C, 0, aLen);
            System.arraycopy(B, 0, C, aLen, bLen);
            return C;
        }else if(A != null){
            return A;
        }else return B;
    }

    /** FROM APACHE WordUtils
     * <p>Capitalizes all the delimiter separated words in a String.
     * Only the first character of each word is changed. To convert the
     * rest of each word to lowercase at the same time.</p>
     *
     * <p>The delimiters represent a set of characters understood to separate words.
     * The first string character and the first non-delimiter character after a
     * delimiter will be capitalized. </p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * Capitalization uses the Unicode title case, normally equivalent to
     * upper case.</p>
     *
     * <pre>
     * WordUtils.capitalize(null, *)            = null
     * WordUtils.capitalize("", *)              = ""
     * WordUtils.capitalize(*, new char[0])     = *
     * WordUtils.capitalize("i am fine", null)  = "I Am Fine"
     * WordUtils.capitalize("i aM.fine", {'.'}) = "I aM.Fine"
     * WordUtils.capitalize("i am fine", new char[]{}) = "I am fine"
     * </pre>
     *
     * @param str  the String to capitalize, may be null
     * @param delimiters  set of characters to determine capitalization, null means whitespace
     * @return capitalized String, <code>null</code> if null String input
     */
    public static String capitalize(final String str, final char... delimiters) {
        if (str.isEmpty()) {
            return str;
        }
        final Set<Integer> delimiterSet = generateDelimiterSet(delimiters);
        final int strLen = str.length();
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        boolean capitalizeNext = true;
        for (int index = 0; index < strLen;) {
            final int codePoint = str.codePointAt(index);
            if (delimiterSet.contains(codePoint)) {
                capitalizeNext = true;
                newCodePoints[outOffset++] = codePoint;
            index += Character.charCount(codePoint);
            } else if (capitalizeNext) {
                final int titleCaseCodePoint = Character.toTitleCase(codePoint);
                newCodePoints[outOffset++] = titleCaseCodePoint;
                index += Character.charCount(titleCaseCodePoint);
                capitalizeNext = false;
            } else {
                newCodePoints[outOffset++] = codePoint;
                index += Character.charCount(codePoint);
            }
        }
        return new String(newCodePoints, 0, outOffset);
    }

    private static Set<Integer> generateDelimiterSet(final char[] delimiters) {
        final Set<Integer> delimiterHashSet = new HashSet<>();
        if (delimiters == null || delimiters.length == 0) {
            if (delimiters == null) {
                delimiterHashSet.add(Character.codePointAt(new char[] {' '}, 0));
            }

            return delimiterHashSet;
        }

        for (int index = 0; index < delimiters.length; index++) {
            delimiterHashSet.add(Character.codePointAt(delimiters, index));
        }
        return delimiterHashSet;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    public static boolean isTomorrow(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return isSameDay(date, cal.getTime());
    }

    public static boolean isInThisWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
        cal.setTime(date);
        int targetWeek = cal.get(Calendar.WEEK_OF_YEAR);
        return currentWeek == targetWeek;
    }

    public static boolean isInNextWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
        cal.setTime(date);
        int targetWeek = cal.get(Calendar.WEEK_OF_YEAR);
        return currentWeek + 1 == targetWeek;
    }

    public static boolean isInThisYear(Date date) {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        cal.setTime(date);
        int targetYear = cal.get(Calendar.YEAR);
        return currentYear == targetYear;
    }

    public static boolean isInNextYear(Date date) {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        cal.setTime(date);
        int targetYear = cal.get(Calendar.YEAR);
        return currentYear + 1 == targetYear;
    }

    //position should be the index of the character after the place wanted to insert
    public static String insertToString(String originalStr, CharSequence insertStr, int position){
        return originalStr.substring(0, position) + insertStr + originalStr.substring(position);
    }

    public static String addCountSuffix(String rawDateStr){
        switch (rawDateStr){//add count suffix
            case "1":{
                rawDateStr += " st";
                break;
            }case "2":{
                rawDateStr += " nd";
                break;
            }case "3":{
                rawDateStr += " rd";
                break;
            }default:{
                rawDateStr += " th";
                break;
            }
        }
        return rawDateStr;
    }

    public static boolean isStringContainAnyOfTheseWords(String string, String[] matches){
        for (String s : matches)
        {
            if (string.contains(s))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates the notification for the reminder
     *
     * TitleText: TITLE value from cursor
     * ContentText:Occur time, next up time, and then up time with recurrence indicators if they exist
     * e.g. 8:29 (Every Tue) • Next up: Tuesday (Mar 3) 23:12 • Then: Jul 4, 10:29
     *
     * @param context context of the activity/fragment/service running in, used to get string resources
     * @param cursor the cursor returned from quering a note
     * @return the reminder notification, null if failed during the process, for example, ParseException
     */
    public static Notification generateReminderNotification(Context context, Cursor cursor){
        String title = cursor.getString(cursor.getColumnIndex(TITLE));
        String recentRemindDateStr = cursor.getString(cursor.getColumnIndex(RECENT_REMIND_TIME));
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date nowRemindTime = null;
        try {
            nowRemindTime = dateFormat.parse(recentRemindDateStr);
        } catch (ParseException ignored){}
        String remindTimesStr = cursor.getString(cursor.getColumnIndex(REMIND_TIMES));
        Gson remindGson = new Gson();
        Type type = new TypeToken<ArrayList<Date>>() {}.getType();
        ArrayList<Date> remindDates = remindGson.fromJson(remindTimesStr,type);
        Date nextUpRemindTime = remindDates.get(1);
        Date thenUpRemindTime = remindDates.get(2);
        SimpleDateFormat timeNotificationDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateNotificationDateFormat = new SimpleDateFormat("MMM d",Locale.getDefault());
        SimpleDateFormat wholeTimeDateFormat = new SimpleDateFormat("hh aa",Locale.getDefault());
        StringBuilder notificationContent = new StringBuilder();
        String nowUpTimeStr = "", nextUpTimeStr = "", thenUpTimeStr = "";
        ArrayList<ArrayList<String>> nextUpRecurStats = new ArrayList<>(), thenUpRecurStats = new ArrayList<>();
        nowUpTimeStr = timeNotificationDateFormat.format(nowRemindTime);
        if(nowUpTimeStr.endsWith(":00") ){//if whole time then change display from XX:XX to XX AM/PM todo consider add 24/12 hr mode setting and add that decision logic here
            nowUpTimeStr = wholeTimeDateFormat.format(nowRemindTime);
        }
        String recurrenceStatsStr = cursor.getString(cursor.getColumnIndex(RECURRENCE_STATS));
        if(recurrenceStatsStr != null) {//has recurrences
            Type recurStatType = new TypeToken<ArrayList<ArrayList<String>>>() {}.getType();
            ArrayList<ArrayList<String>> recurrenceStats = remindGson.fromJson(recurrenceStatsStr,recurStatType);
            Date firstRecurDate = null, firstDateGenByFirst = null, secondRecurDate = null;
            ArrayList<String> firstRecurStat = recurrenceStats.get(0);

            try {
                firstRecurDate = dateFormat.parse(recurrenceStats.get(0).get(2));
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
            if(nextUpRemindTime == null){
                nextUpRemindTime = new Date(Long.MAX_VALUE);
            }
            if(thenUpRemindTime == null){
                thenUpRemindTime = new Date(Long.MAX_VALUE);
            }

            //add recur stat to now up str
            int coincideCount = 0;
            StringBuilder nowUpRecurStrBuilder = new StringBuilder();
            for(ArrayList<String> recurStat : recurrenceStats){
                Date recurDate = null;
                try {
                    recurDate = dateFormat.parse(recurStat.get(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                    continue;
                }
                if(recurDate.equals(nowRemindTime)){
                    coincideCount++;
                    if(coincideCount != 4){
                        nowUpRecurStrBuilder.append(generateSimpleRecurrenceStr(recurStat));
                        nowUpRecurStrBuilder.append(" & ");
                    }else {
                        nowUpRecurStrBuilder.replace(nowUpRecurStrBuilder.length() - 3,nowUpRecurStrBuilder.length(),"…");//replace " & " with "…"
                        break;
                    }
                }
            }
            if(!nowUpRecurStrBuilder.toString().isEmpty()){
                if(coincideCount != 4){
                    nowUpRecurStrBuilder.delete(nowUpRecurStrBuilder.length() - 3, nowUpRecurStrBuilder.length());//delete the added " & " if not deleted in above code
                }
                nowUpRecurStrBuilder.insert(0," (" + context.getString(R.string.reminder_notification_every));
                nowUpRecurStrBuilder.append(")");//wrap up
                nowUpTimeStr += nowUpRecurStrBuilder.toString();
            }
            //todo add support for until dates, add int totalRemindOccurrences (-1 for infinite (no until)) to corporate that and change below code
            //todo add determine logic after adding until time support as there can be no thenUp/nextUp dates even if there are recurring dates
            //calculate nextUp and thenUp
            for(int i = 0; i < recurrenceStats.size(); i++){
                Date recurDate = null;
                try {
                    recurDate = dateFormat.parse(recurrenceStats.get(i).get(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                    break;
                }
                Date genRecurDate = generateNextRecurDate(recurrenceStats.get(i));
                if(i < recurrenceStats.size() - 1){//if the current loop is not the last one, support nextRecurDate
                    Date recurDate2 = null;
                    try {
                        recurDate2 = dateFormat.parse(recurrenceStats.get(i + 1).get(2));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        break;
                    }
                    if(recurDate.compareTo(nextUpRemindTime) <= 0){
                        if(recurDate.equals(nowRemindTime)){
                            //since recurDate is nowRemindTime, so it can't be nextUpRemindTime, therefore compare genRecurDate with recurDate2 to determine nextUpRemindTime
                            if(genRecurDate.compareTo(recurDate2) <= 0){
                                if(genRecurDate.compareTo(nextUpRemindTime) <= 0){
                                    //nextUp must be this one
                                    nextUpRemindTime = genRecurDate;
                                    nextUpRecurStats.add(updateRecurStatWithNewDate(recurrenceStats.get(i),genRecurDate));
                                    if(genRecurDate.equals(recurDate2)){//add the other one's information
                                        nextUpRecurStats.add(recurrenceStats.get(i + 1));
                                    }
                                }
                            }else if(recurDate2.compareTo(nextUpRemindTime) <= 0){//recurDate2 < genRecurDate && (the if statement here) recurDate2 < nextUpRemindTime
                                //nextUp must be this one
                                nextUpRemindTime = recurDate2;
                                nextUpRecurStats.add(recurrenceStats.get(i + 1));
                            }
                        }else {//nextUpRemindTime is recurDate
                            //nextUp must be this one
                            nextUpRemindTime = recurDate;
                            nextUpRecurStats.add(recurrenceStats.get(i));
                        }

                    }else if(recurDate.compareTo(thenUpRemindTime) <= 0){

                    }else {//if the dates are after existing nextUp&thenUp dates, break
                        break;
                    }
                }else {//only one date left, no need to compare with other dates
                    if(recurDate.compareTo(nextUpRemindTime) <= 0){//recurDate is nowRemindTime, so only genRecurDate can be nextRemindTime
                        if(recurDate.equals(nowRemindTime)){
                            if(genRecurDate.compareTo(nextUpRemindTime) <= 0){
                               //nextUp must be this one
                                nextUpRemindTime = genRecurDate;
                                nextUpRecurStats.add(updateRecurStatWithNewDate(recurrenceStats.get(i),genRecurDate));
                            }
                        }else {//otherwise recurDate is nextRemindTime
                            //nextUp must be this one
                            nextUpRemindTime = recurDate;
                            nextUpRecurStats.add(recurrenceStats.get(i));
                        }
                    }
                }
            }

            //determine thenUpDate
            for(int i = 0; i < recurrenceStats.size(); i++){
                Date recurDate = null;
                try {
                    recurDate = dateFormat.parse(recurrenceStats.get(i).get(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                    break;
                }
                if(i < recurrenceStats.size() - 1) {//if the current loop is not the last one, support nextRecurDate
                    Date recurDate2 = null;
                    try {
                        recurDate2 = dateFormat.parse(recurrenceStats.get(i + 1).get(2));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        break;
                    }
                    if(recurDate.compareTo(thenUpRemindTime) <= 0){
                        Date genRecurDate = recurDate;
                        while (true){//use while loop to get a closest one after nextUp
                            genRecurDate = generateNextRecurDate(updateRecurStatWithNewDate(recurrenceStats.get(i),genRecurDate));
                            if(genRecurDate.after(nextUpRemindTime)){
                                break;
                            }
                        }
                        if(genRecurDate.compareTo(recurDate2) <= 0){
                            if(genRecurDate.compareTo(thenUpRemindTime) <= 0){
                                //thenUp must be this one
                                thenUpRemindTime = genRecurDate;
                                thenUpRecurStats.add(updateRecurStatWithNewDate(recurrenceStats.get(i), genRecurDate));
                                if(genRecurDate.equals(recurDate2)){//add the other one's information
                                    thenUpRecurStats.add(recurrenceStats.get(i + 1));
                                }
                            }
                        }else{
                            if(recurDate2.after(nextUpRemindTime)){//if that's after nextUp, then add it in
                                if(recurDate2.compareTo(thenUpRemindTime) <= 0) {//recurDate2 < genRecurDate && (the if statement here) recurDate2 < thenUpRemindTime
                                    //thenUp must be this one
                                    thenUpRemindTime = recurDate2;
                                    thenUpRecurStats.add(recurrenceStats.get(i + 1));
                                }
                            }else {//otherwise find the next initial recur date that's the closest after nextUp, then compare it to genRecurDate again to make the final decision
                                //use for loop to get a closest one after nextUp
                                recurDate2 = null;
                                int levelIndex = i;
                                for(int j = i; i < recurrenceStats.size(); j ++){
                                    try {
                                        recurDate2 = dateFormat.parse(recurrenceStats.get(j).get(2));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                    if(recurDate2.after(nextUpRemindTime)){
                                        levelIndex = i;
                                        break;//found!
                                    }
                                    recurDate2 = null;
                                }
                                if(recurDate2 != null){
                                    if(genRecurDate.compareTo(recurDate2) <= 0){
                                        thenUpRemindTime = genRecurDate;
                                        thenUpRecurStats.add(updateRecurStatWithNewDate(recurrenceStats.get(i),genRecurDate));
                                        if(genRecurDate.equals(recurDate2)){//add the other one's information
                                            thenUpRecurStats.add(recurrenceStats.get(levelIndex));
                                        }
                                    }else {
                                        thenUpRemindTime = recurDate2;
                                        thenUpRecurStats.add(recurrenceStats.get(levelIndex));
                                    }
                                }else {//all the initial recur dates are before nextUpdate todo even base dates should be considered in this sort, change it!
                                    //todo generate nextUp dates base by base until all of the "groups" have one date that's the closest after nextUpDate, then sort the dates, get the smallest one, use that to compare with the genRecurDate (in first group)
                                    //todo rank this and find smallest unit, then use that initial date to find the closest one after nextUpTime (which is probably the 1st or 2nd closest depends on the comparison later) then compare that date with the genRecurDate from the begining, write on from there
                                    ArrayList<ArrayList<String>> smallestRecurIntervalStats = new ArrayList<>(recurrenceStats);
                                    ArrayList<ArrayList<String>> recurStatsWithUnitLevel = new ArrayList<>(recurrenceStats);
                                    if()
                                    for (ArrayList<String> recurStat : recurStatsWithUnitLevel){
                                        String recurUnit = recurStat.get(0);
                                        switch (recurUnit) {
                                            case "SECOND": {
                                                recurStat.add("1");
                                                break;
                                            }
                                            case "MINUTE": {
                                                recurStat.add("2");
                                                break;
                                            }
                                            case "HOUR": {
                                                recurStat.add("3");
                                                break;
                                            }
                                            case "DAY": {//day
                                                recurStat.add("4");
                                                break;
                                            }
                                            case "HOURS_OF_DAY|MINUTES_OF_HOUR":
                                            case "HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE": {
                                                recurStat.add("5");
                                                break;
                                            }
                                            case "WEEK":
                                            case "DAY_OF_WEEK": {//week
                                                recurStat.add("6");
                                                break;
                                            }
                                            case "MONTH": {//month
                                                recurStat.add("7");
                                                break;
                                            }
                                            case "YEAR"://year
                                                case "DAY_OF_WEEK[OF]MONTH_OF_YEAR":
                                                case "MONTH_OF_YEAR":
                                                case "MONTH_OF_YEAR|DAY_OF_MONTH":
                                                case "MONTH_OF_YEAR|DAY_OF_MONTH|HOURS_OF_DAY|MINUTES_OF_HOUR":
                                                case "MONTH_OF_YEAR|DAY_OF_MONTH|HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE": {
                                                recurStat.add("8");
                                                break;
                                            }
                                            default: {
                                                recurStat.add("9");
                                                break;
                                            }
                                        }
                                    }
                                    final List<String> ORDER = Arrays.asList("SECOND","MINUTE","HOUR","DAY","HOURS_OF_DAY|MINUTES_OF_HOUR|SECONDS_OF_MINUTE","HOURS_OF_DAY|MINUTES_OF_HOUR","WEEK","DAY_OF_WEEK","MONTH","YEAR");
                                    Collections.sort(smallestRecurIntervalStats, new Comparator<ArrayList<String>>() {//sort a list with a given customized string order
                                        @Override
                                        public int compare(ArrayList<String> ao1, ArrayList<String> ao2) {
                                            String o1 = ao1.get(2), o2 = ao2.get(2);
                                            int pos1 = 0;
                                            int pos2 = 0;
                                            for (int i = 0; i < Math.min(o1.length(), o2.length()) && pos1 == pos2; i++) {
                                                pos1 = ORDER.indexOf(o1.charAt(i));
                                                pos2 = ORDER.indexOf(o2.charAt(i));
                                            }

                                            if (pos1 == pos2 && o1.length() != o2.length()) {
                                                return o1.length() - o2.length();
                                            }

                                            return pos1  - pos2  ;//sort recurrence status based on initial remind date
                                        }
                                    });
                                }
                            }

                        }

                    }else break;
                }else {
                    Date genRecurDate = recurDate;
                    while (true){//use while loop to get a closest one after nextUp
                        genRecurDate = generateNextRecurDate(updateRecurStatWithNewDate(recurrenceStats.get(i),genRecurDate));
                        if(genRecurDate.after(nextUpRemindTime)){
                            break;
                        }
                    }
                    if(genRecurDate.compareTo(thenUpRemindTime) <= 0){
                        thenUpRemindTime = genRecurDate;
                        thenUpRecurStats.add(updateRecurStatWithNewDate(recurrenceStats.get(i), genRecurDate));
                    }
                }
            }

            if(firstRecurDate.equals(nowRemindTime)){//the most recent remind time is recurring (the first in the arraylist of recurrenceStats)
                nowUpTimeStr += " (" + context.getString(R.string.reminder_notification_every) + generateSimpleRecurrenceStr(recurrenceStats.get(0)) + ")";//add recurrence indicator
                firstDateGenByFirst = generateNextRecurDate(firstRecurStat);
                if(recurrenceStats.size() > 1){
                    try {
                        secondRecurDate = dateFormat.parse(recurrenceStats.get(1).get(2));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return null;
                    }
                    ArrayList<String> secondRecurStat = recurrenceStats.get(1);
                    if(firstDateGenByFirst.compareTo(secondRecurDate) <= 0){
                        //if(nextUpRemindTime == null) nextUpRemindTime = firstDateGenByFirst;
                        if(firstDateGenByFirst.compareTo(nextUpRemindTime) <= 0){//closer than existing "nextUp"
                            nextUpRemindTime = firstDateGenByFirst;
                            nextUpRecurStats = updateRecurStatWithNewDate(firstRecurStat,nextUpRemindTime);
                            Date secondDateGenByFirst = generateNextRecurDate(nextUpRecurStats);
                            if(secondDateGenByFirst != null){
                                if(secondDateGenByFirst.compareTo(secondRecurDate) <= 0){
                                    //if(thenUpRemindTime == null) thenUpRemindTime = secondDateGenByFirst;
                                    if(secondDateGenByFirst.compareTo(thenUpRemindTime) <= 0){
                                        //NOW=firstRecur, NEXT=firstGenByFirst, THEN=secondGenByFirst
                                        thenUpRemindTime = secondDateGenByFirst;
                                        thenUpRecurStats = updateRecurStatWithNewDate(firstRecurStat,thenUpRemindTime);
                                    }
                                }else {//NOW=firstRecur, NEXT=firstGenByFirst, THEN=secondRecur
                                    //if(thenUpRemindTime == null) thenUpRemindTime = secondRecurDate;
                                    if(secondRecurDate.compareTo(thenUpRemindTime) <= 0){
                                        thenUpRemindTime = secondRecurDate;
                                        thenUpRecurStats = secondRecurStat;
                                    }
                                }
                            }else if(secondRecurDate.compareTo(thenUpRemindTime) <= 0){//if for some reason parsing date failed, then just assign thenUp to secondRecur if it meets requirements
                                // NOW=firstRecur, NEXT=firstGenByFirst, THEN=secondRecur
                                thenUpRemindTime = secondRecurDate;
                                thenUpRecurStats = secondRecurStat;
                            }
                        }else {
                            //if(thenUpRemindTime == null) thenUpRemindTime = firstDateGenByFirst;
                            if(firstDateGenByFirst.compareTo(thenUpRemindTime) <= 0){//closer than existing "thenUp"
                                // NOW=firstRecur, NEXT=ORIGINAL, THEN=firstGenByFirst
                                thenUpRemindTime = firstDateGenByFirst;
                                thenUpRecurStats = updateRecurStatWithNewDate(firstRecurStat,thenUpRemindTime);
                            }
                        }
                    }else{//secondRecur before firstGenByFirst
                        //if(nextUpRemindTime == null) nextUpRemindTime = secondRecurDate;
                        if(secondRecurDate.compareTo(nextUpRemindTime) <= 0){//closer than existing "nextUp"
                            nextUpRemindTime = secondRecurDate;
                            nextUpRecurStats = secondRecurStat;
                            Date firstDateGenBySecond = generateNextRecurDate(nextUpRecurStats);
                            if(firstDateGenBySecond != null){
                                if(firstDateGenBySecond.compareTo(firstDateGenByFirst) <= 0){
                                    //if(thenUpRemindTime == null) thenUpRemindTime = firstDateGenBySecond;
                                    if(firstDateGenBySecond.compareTo(thenUpRemindTime) <= 0){
                                        thenUpRemindTime = firstDateGenBySecond;
                                        thenUpRecurStats = updateRecurStatWithNewDate(firstRecurStat,thenUpRemindTime);
                                    }
                                    //NOW=firstRecur, NEXT=secondRecur, THEN=secondGenBySecond
                                }else {//NOW=firstRecur, NEXT=secondRecur, THEN=firstGenByFirst
                                    //if(thenUpRemindTime == null) thenUpRemindTime = firstDateGenByFirst;
                                    if(firstDateGenByFirst.compareTo(thenUpRemindTime) <= 0){
                                        thenUpRemindTime = firstDateGenByFirst;
                                        thenUpRecurStats = secondRecurStat;
                                    }
                                }
                            }else {
                                //if(thenUpRemindTime == null) thenUpRemindTime = secondRecurDate;
                                if(secondRecurDate.compareTo(thenUpRemindTime) <= 0){//if for some reason parsing date failed, then just assign thenUp to secondRecur if it meets requirements
                                    // NOW=firstRecur, NEXT=secondRecur, THEN=firstGenByFirst
                                    thenUpRemindTime = firstDateGenByFirst;
                                    thenUpRecurStats = secondRecurStat;
                                }
                            }
                        }else {
                            //if(thenUpRemindTime == null) thenUpRemindTime = secondRecurDate;
                            if(secondRecurDate.compareTo(thenUpRemindTime) <= 0){//closer than existing "thenUp"
                                // NOW=firstRecur, NEXT=ORIGINAL, THEN=secondRecur
                                thenUpRemindTime = secondRecurDate;
                                thenUpRecurStats = updateRecurStatWithNewDate(firstRecurStat,thenUpRemindTime);
                            }
                        }
                    }
                }else{//only one recurDate indicator exist, has to generate nextUp and thenUp all from the only one if they exist
                    //if(nextUpRemindTime == null) nextUpRemindTime = firstDateGenByFirst;
                    if(firstDateGenByFirst.compareTo(nextUpRemindTime) <= 0){
                        nextUpRemindTime = firstDateGenByFirst;
                        nextUpRecurStats = updateRecurStatWithNewDate(firstRecurStat,nextUpRemindTime);
                        Date secondDateGenByFirst = generateNextRecurDate(nextUpRecurStats);
                        //if(thenUpRemindTime == null) thenUpRemindTime = secondDateGenByFirst;
                        if(secondDateGenByFirst != null && secondDateGenByFirst.compareTo(thenUpRemindTime) <= 0){//secondGenBySecond closer than existing "thenUp"
                            //NOW=firstRecur, NEXT=firstGenByFirst, THEN=secondGenByFirst
                            thenUpRemindTime = secondDateGenByFirst;
                            thenUpRecurStats = updateRecurStatWithNewDate(firstRecurStat, thenUpRemindTime);
                        }
                    }
                }
            }else {//first recur date is not current remind time
                //if(nextUpRemindTime == null) nextUpRemindTime = firstRecurDate;
                firstDateGenByFirst = generateNextRecurDate(firstRecurStat);
                if(firstRecurDate.compareTo(nextUpRemindTime) <= 0){
                    nextUpRemindTime = firstRecurDate;
                    nextUpRecurStats = firstRecurStat;
                    if(recurrenceStats.size() > 1) {
                        try {
                            secondRecurDate = dateFormat.parse(recurrenceStats.get(1).get(2));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return null;
                        }
                        if(firstDateGenByFirst.compareTo(secondRecurDate) <= 0){
                            //if(thenUpRemindTime == null) thenUpRemindTime = firstDateGenByFirst;
                            if(firstDateGenByFirst.compareTo(thenUpRemindTime) <= 0){
                                thenUpRemindTime = firstDateGenByFirst;
                                thenUpRecurStats = updateRecurStatWithNewDate(firstRecurStat, thenUpRemindTime);
                            }
                        }else {
                            //if(thenUpRemindTime == null) thenUpRemindTime = secondRecurDate;
                            if(secondRecurDate.compareTo(thenUpRemindTime) <= 0){
                                thenUpRemindTime = secondRecurDate;
                                thenUpRecurStats = recurrenceStats.get(1);
                            }
                        }
                    }else {//only this one recur date indicator exist, then just use its next generated date to see if it's then up
                        //if(thenUpRemindTime == null) thenUpRemindTime = firstDateGenByFirst;
                        if(firstDateGenByFirst.compareTo(thenUpRemindTime) <=0){
                            thenUpRemindTime = firstDateGenByFirst;
                            thenUpRecurStats = updateRecurStatWithNewDate(firstRecurStat, thenUpRemindTime);
                        }
                    }
                }else{
                    //if(thenUpRemindTime == null) thenUpRemindTime = firstRecurDate;
                    if(firstRecurDate.before(thenUpRemindTime)){//no need to find new, closer recurrence dates, because they don't exist
                        //NOW=ORIGINAL, NEXT=ORIGINAL, THEN=firstRecur
                        thenUpRemindTime = firstRecurDate;
                        thenUpRecurStats = recurrenceStats.get(0);//store its recur status, add to final notification string later
                    }
                }
            }
        }
        notificationContent.append(nowUpTimeStr);//start with the most recent reminder time
        if(nextUpRemindTime != null && nextUpRemindTime.getTime() != Long.MAX_VALUE){
            notificationContent.append(context.getString(R.string.reminder_notification_splitter));
            notificationContent.append(context.getString(R.string.reminder_notification_next_up));
            if(isToday(nextUpRemindTime)){
                nextUpTimeStr = timeNotificationDateFormat.format(nextUpRemindTime);
            }else if(isTomorrow(nextUpRemindTime)){
                nextUpTimeStr = context.getString(R.string.tomorrow) + " " + timeNotificationDateFormat.format(nextUpRemindTime);//tomorrow + time
            }else if(isInThisWeek(nextUpRemindTime) || isInNextWeek(nextUpRemindTime)){
                SimpleDateFormat weekDayFormat = new SimpleDateFormat("EEEEE '('MMM d')' HH:mm",Locale.getDefault());
                nextUpTimeStr = weekDayFormat.format(nextUpRemindTime);
                if(isInNextWeek(nextUpRemindTime)){
                    nextUpTimeStr = context.getString(R.string.next) + nextUpTimeStr;
                }
            }else {
                nextUpTimeStr = dateNotificationDateFormat.format(nextUpRemindTime) + ", " + timeNotificationDateFormat.format(nextUpRemindTime);//date + time
            }
            if(nextUpRecurStats.size() > 0){//has recurrence status
                if (!isToday(nextUpRemindTime) && !isTomorrow(nextUpRemindTime) && (isInThisWeek(nextUpRemindTime) || isInNextWeek(nextUpRemindTime))) {
                    //already added weekday indicator above, skip it here by setting parameter containWeekdayIndicator to false
                    nextUpTimeStr += " (" + context.getString(R.string.reminder_notification_every) + generateSimpleRecurrenceStr(nextUpRecurStats, false) + ")";
                }else {
                    nextUpTimeStr += " (" + context.getString(R.string.reminder_notification_every) + generateSimpleRecurrenceStr(nextUpRecurStats) + ")";
                }
            }
            if(!isInThisYear(nextUpRemindTime)){
                if(isInNextYear(nextUpRemindTime)){
                    nextUpTimeStr += " (" + context.getString(R.string.next_year) + ")";
                }else {
                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",Locale.getDefault());
                    nextUpTimeStr += " (" + yearFormat.format(nextUpRemindTime) + ")";
                }
            }
            notificationContent.append(nextUpTimeStr);
        }
        if(thenUpRemindTime != null && thenUpRemindTime.getTime() != Long.MAX_VALUE){
            notificationContent.append(context.getString(R.string.reminder_notification_splitter));
            notificationContent.append(context.getString(R.string.reminder_notification_then));
            if(isToday(thenUpRemindTime)){
                thenUpTimeStr = timeNotificationDateFormat.format(thenUpRemindTime);
            }else if(isTomorrow(thenUpRemindTime)){
                thenUpTimeStr = context.getString(R.string.tomorrow) + " " + timeNotificationDateFormat.format(thenUpRemindTime);//tomorrow + time
            }else if(isInThisWeek(thenUpRemindTime) || isInNextWeek(thenUpRemindTime)){
                SimpleDateFormat weekDayFormat = new SimpleDateFormat("EEEEE '('MMM d')' HH:mm",Locale.getDefault());
                thenUpTimeStr = weekDayFormat.format(thenUpRemindTime);
                if(isInNextWeek(thenUpRemindTime)){
                    thenUpTimeStr = context.getString(R.string.next) + thenUpTimeStr;
                }
            }else {
                thenUpTimeStr = dateNotificationDateFormat.format(thenUpRemindTime) + " " + timeNotificationDateFormat.format(thenUpRemindTime);//date + time
            }
            if(thenUpRecurStats.size() > 0){//has recurrence status
                if (!isToday(thenUpRemindTime) && !isTomorrow(thenUpRemindTime) && (isInThisWeek(thenUpRemindTime) || isInNextWeek(thenUpRemindTime))) {
                    //already added weekday indicator above, skip it here by setting parameter containWeekdayIndicator to false
                    thenUpTimeStr += " (" + context.getString(R.string.reminder_notification_every) + generateSimpleRecurrenceStr(thenUpRecurStats, false) + ")";
                }else {
                    thenUpTimeStr += " (" + context.getString(R.string.reminder_notification_every) + generateSimpleRecurrenceStr(thenUpRecurStats) + ")";
                }
            }
            notificationContent.append(thenUpTimeStr);
        }

                /*if(recentRecurDate.compareTo(nowRemindTime) == 0 || recentRecurDate.compareTo(nextUpRemindTime) == 0 || recentRecurDate.compareTo(thenUpRemindTime) == 0){
                    notificationContent.insert(timeNotificationDateFormat.format(nowRemindTime).length() - 1, "(" + context.getString(R.string.reminder_notification_every) +
                            generateRecurrenceStr(recurrenceStat));
                }*/
        if(remindDates.get(3) != null || recurrenceStatsStr != null){
            notificationContent.append("…");
        }
        Notification.Builder reminderNotifBuilder = new Notification.Builder(context);
        reminderNotifBuilder.setSmallIcon(R.mipmap.ic_launcher);
        reminderNotifBuilder.setContentTitle(title);
        reminderNotifBuilder.setContentText(notificationContent);
        return reminderNotifBuilder.build();
    }

    public static List sortListWithStringOrder(final List ORDER, List listToBeSorted){
        Collections.sort(listToBeSorted, new Comparator<ArrayList<String>>() {//sort a list with a given customized string order
            @Override
            public int compare(ArrayList<String> ao1, ArrayList<String> ao2) {
                String o1 = ao1.get(2), o2 = ao2.get(2);
                int pos1 = 0;
                int pos2 = 0;
                for (int i = 0; i < Math.min(o1.length(), o2.length()) && pos1 == pos2; i++) {
                    pos1 = ORDER.indexOf(o1.charAt(i));
                    pos2 = ORDER.indexOf(o2.charAt(i));
                }

                if (pos1 == pos2 && o1.length() != o2.length()) {
                    return o1.length() - o2.length();
                }

                return pos1  - pos2  ;//sort recurrence status based on initial remind date
            }
        });
        return listToBeSorted;
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

    @Override
    public void onBackPressed() {
        switch (tabLayout.getSelectedTabPosition()){
            case 0:{//ImportantFragment
                if(onImportantBackPressedListener != null){
                    onImportantBackPressedListener.doBack();
                }else super.onBackPressed();
                break;
            }case 1:{//MainFragment
                if(onMainBackPressedListener != null){
                    onMainBackPressedListener.doBack();
                }else super.onBackPressed();
                break;
            }case 2:{//ClipboardFragment
                if(onClipboardBackPressedListener != null){
                    onClipboardBackPressedListener.doBack();
                }else super.onBackPressed();
                break;
            }default:{
                super.onBackPressed();
            }
        }
    }

    public void setBackToMainFragment(){
        viewPager.setCurrentItem(1);
    }

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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        /*if(tabLayout.getSelectedTabPosition() == 1){
            mainFragment.setOutOfSelectionMode();
        }else if (tabLayout.getSelectedTabPosition() == 0){

            importantFragment.setOutOfSelectionMode();
        }*/
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
            return networkInfo != null && networkInfo.isConnected();
        }

        @Override
        protected void onPostExecute(Boolean aResult) {
            //isConnected = isConn;
            super.onPostExecute(aResult);
        }
    }
}