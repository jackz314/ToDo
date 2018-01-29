package com.jackz314.todo;


import android.Manifest;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
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
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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



// the great alpaca that saves me from the bugs

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener{
    dtb todosql;
    EditText input;
    FloatingActionButton fab;
    TextView modifyId;
    RecyclerView todoList;
    private FirebaseAnalytics mFirebaseAnalytics;
    IabHelper mHelper;
    private static final String REMOVE_AD_SKU = "todo_iap_remove_ad";
    int exit=0,doubleClickCout = 0;
    private static final String[] PROJECTION = new String[]{ID, TITLE};
    private static final String SELECTION = TITLE + " LIKE ?";
    boolean justex = false;
    public boolean isInSearchMode = false, isInSelectionMode = false;
    public ArrayList<Long> selectedId = new ArrayList<>();
    public ArrayList<String> selectedContent = new ArrayList<>();
    public ArrayList<String> CLONESelectedContent = new ArrayList<>();
    boolean isConnected = false;
    boolean justDoubleClicked = false;
    boolean selectAll = false, unSelectAll = false;
    SharedPreferences sharedPreferences;
    int resultCount = 0, cursorPos = 0;
    public String searchText;
    String oldResult = "";
    int themeColor,textColor,backgroundColor,textSize;
    CoordinatorLayout main;
    SearchView searchView;
    Boolean noInterruption = true;
    DrawerLayout mDrawerLayout;
    TodoListAdapter todoListAdapter;
    ActionBarDrawerToggle mDrawerToggle;
    TextView EmptextView, selectionTitle;
    CheckBox multiSelectionBox;
    SpeechRecognizer speechRecognizer;
    AdView adView;
    RecognitionProgressView recognitionProgressView;
    boolean isAdd = true;
    NavigationView navigationView;
    ProgressFloatingActionButton proFab;
    ProgressBar fabProgressBar;
    Menu menuNav;
    MenuItem navRemoveAD;
    BroadcastReceiver receiver;
    IInAppBillingService mService;
    Toolbar selectionToolBar, toolbar;
    ServiceConnection mServiceConn;
    CheckBox selectAllBox;
    private String todoTableId = "HAHA! this is the real one, gotcha";
    //IabBroadcastReceiver mBroadcastReceiver;
    //public static int MODIFY_CONTEXT_ID = 1;
    //public static int DELETE_CONTEXT_ID = 2;
    public boolean iapsetup = true;
    public boolean isAdRemoved = false;
    ProgressDialog purchaseProgressDialog;
    static int REMOVE_REQUEST_ID =1022;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //System.out.println("oncreatecalled");
        adView= (AdView)findViewById(R.id.bannerAdView);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        //layoutInflater.inflate(R.layout.nav_header_main,null);
        //setLauncherIcon();
       // FirebaseCrash.report(new Exception("MainActivity created"));
        //FirebaseCrash.log("MainActivity created log");
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        input = (EditText)findViewById(R.id.input);
        modifyId = (TextView)findViewById(R.id.modifyId);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        EmptextView = (TextView)findViewById(R.id.emptyText);
        todoList = (RecyclerView) findViewById(R.id.todolist);
        todoList.setHasFixedSize(true);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        proFab = (ProgressFloatingActionButton)findViewById(R.id.progress_fab);
        fabProgressBar = (ProgressBar)findViewById(R.id.fab_progress_bar);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //todoList.addOnScrollListener(new RecyclerViewListener());
        //speechRecognizer.setRecognitionListener(new speechListener());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        todosql = new dtb(this);
        todoTableId = "0x397821dc97276";
        setSupportActionBar(toolbar);
        // navheadText = (TextView)navigationView.findViewById(R.id.navHeadText);
        main = (CoordinatorLayout)findViewById(R.id.total_main_bar);
        //get colors

        input.setTextIsSelectable(true);
        input.setFocusable(true);
        todoList.setFocusable(true);
        todoList.setFocusableInTouchMode(true);
        String historySettingPref = "MII";
        String bep = "ANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiZZobdX3yEuQtssAfZ2AE69Agvit3KuCfR6ywZRlrcpjWKb5+aKBT72hEawKFwDCsFquccZvt6R8nKBD1ucbl4PCgZvrUie9EFQR4YKxlp9iPogdreu8ifIjR/un9sFsiRGndmjhgJHMx66uKlDX7gyu9/EzuxFVajPCdbw7nQdK9XJzBripYLKY0w5/BLbKaOo7kmhSwiOlsRQwayIbXvUiYQb5ij17eFO/n4sebKNvixdIsaU3YaFlh/CbEpy/3P0UEHtrtb3B27pBa4+3kEriVc7uVBN+kYHmMQRMBgyjzKNwITDhHrP12qjlmrVk4LKehQVVDmPymB/C1/qTuwIDAQAB";
        historySettingPref += "BIjAN" + bep.substring(2,bep.length());
        input.setVisibility(View.GONE);
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
        navRemoveAD = menuNav.findItem(R.id.unlock);
        int size = menuNav.size();
        for (int i = 0; i < size; i++) {
            menuNav.getItem(i).setChecked(false);
        }
        mHelper = new IabHelper(this, historySettingPref);
        mHelper.enableDebugLogging(true);
        if (mHelper != null) mHelper.flagEndAsync();
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
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
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                    //System.out.println("qazwsx"+5);
                    iapsetup = false;
                }
            }
        });
        setColorPreferences();
        displayAllNotes();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isAdRemoved && navRemoveAD != null){
                    adView.destroy();
                    displayAllNotes();
                    menuNav.removeItem(R.id.unlock);
                }else {
                    adView.loadAd(adRequest);
                }
            }
        },500);
        if(navRemoveAD != null && !iapsetup){
            //navRemoveAD.setEnabled(false);
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
        todoList.setOnTouchListener(new View.OnTouchListener() {
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
        });
        recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);
        recognitionProgressView.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        proFab.setVisibility(View.VISIBLE);
        recognitionProgressView.setSpeechRecognizer(speechRecognizer);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onBeginningOfSpeech() {

            }
            public void onReadyForSpeech(Bundle params)
            {
            }
            public void onRmsChanged(float rmsdB)
            {
            }
            public void onBufferReceived(byte[] buffer)
            {
            }
            public void onEndOfSpeech()
            {

            }
            public void onError(int error)
            {
                if(error == SpeechRecognizer.ERROR_NO_MATCH){
                    //Toast.makeText(getApplicationContext(),String.valueOf(error),Toast.LENGTH_SHORT).show();
                    recognitionProgressView.stop();
                    recognitionProgressView.play();
                    recognitionProgressView.setSpeechRecognizer(speechRecognizer);

                }else {
                    recognitionProgressView.setVisibility(View.GONE);
                    proFab.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                    input.setEnabled(true);
                    //proFab.setVisibility(View.VISIBLE);
                    recognitionProgressView.stop();
                    recognitionProgressView.play();
                    if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS){
                        Toast.makeText(getApplicationContext(),getString(R.string.voice_permission_request),Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},0);
                    }else if(error == SpeechRecognizer.ERROR_AUDIO){
                        Toast.makeText(getApplicationContext(),getString(R.string.voice_recon_audio_record_err),Toast.LENGTH_SHORT).show();
                    }else if(error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT || error == SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_SERVER ){
                        Toast.makeText(getApplicationContext(),getString(R.string.voice_recon_internet_err),Toast.LENGTH_SHORT).show();
                    }else if(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY){
                        Toast.makeText(getApplicationContext(),getString(R.string.voice_recon_busy_err),Toast.LENGTH_SHORT).show();
                    }else if(error == SpeechRecognizer.ERROR_CLIENT){

                    }
                }
                //Toast.makeText(getApplicationContext(),getString(R.string.speech_to_text_failed) + String.valueOf(error), Toast.LENGTH_LONG).show();
            }
            public void onResults(Bundle results)
            {
                String str = "";
                input.setEnabled(false);
                ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null) {
                    input.setEnabled(false);
                    if(oldResult.isEmpty()){
                        String originalText = input.getText().toString();
                        if(!originalText.isEmpty()){
                            Log.i("VOICERECOGNITION", "|LAST CHARACTER|"+originalText.substring(originalText.length() - 1)+"|");
                            if( input.getSelectionStart() == -1 || input.getSelectionStart()-1 == input.getText().toString().length() - 1){//cursor at last or not exist
                                if(!Character.isWhitespace(originalText.charAt(originalText.length() - 1)) || Pattern.matches("\\p{Punct}", String.valueOf(originalText.charAt(originalText.length() - 1)))){
                                    input.append(" ");
                                }
                            }else {
                                if(!Character.isWhitespace(originalText.charAt(input.getSelectionStart()-1)) || Pattern.matches("\\p{Punct}", String.valueOf(originalText.charAt(input.getSelectionStart()-1)))){
                                    input.getText().insert(input.getSelectionStart()," ");
                                }
                            }
                        }
                    }
                    str = (String) data.get(0);
                    Log.i("VOICERECOGNITION", ">"+str+"<");
                    if(!oldResult.isEmpty()){
                        String originalText = input.getText().toString();
                        if(oldResult.length() <= str.length()){
                            if(originalText.contains(oldResult)){
                                input.getText().replace(originalText.indexOf(oldResult),originalText.indexOf(oldResult) + oldResult.length(),str);
                            }
                            //String newText = originalText.replace(oldResult,str);
                        }else {
                            input.append(str);
                        }
                    }else {
                        input.getText().insert(input.getSelectionStart(),str);
                    }
                    input.setSelection(input.getSelectionStart());
                    if(input.getSelectionStart() < input.getText().length()-1){
                        if(!Character.isWhitespace(input.getText().charAt(input.getSelectionStart())) || Pattern.matches("\\p{Punct}", String.valueOf(input.getText().charAt(input.getSelectionStart())))){
                            input.getText().insert(input.getSelectionStart()," ");
                        }
                    }
                }
                oldResult = "";
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recognitionProgressView.setVisibility(View.GONE);
                        proFab.setVisibility(View.VISIBLE);
                        recognitionProgressView.stop();
                        recognitionProgressView.play();
                        Handler delayHandler = new Handler();
                        noInterruption = true;
                        fabProgressBar.setVisibility(View.VISIBLE);
                        fabProgressBar.getProgressDrawable().setColorFilter(ColorUtils.lighten(themeColor,0.4), PorterDuff.Mode.MULTIPLY);
                        //fabProgressBar.getIndeterminateDrawable().setColorFilter(ColorUtils.lighten(themeColor,0.4), PorterDuff.Mode.MULTIPLY);
                        fabProgressBar.setProgress(0);
                        fab.setVisibility(View.VISIBLE);
                        //fabProgressBar.setSecondaryProgress(100);
                        fakeProgress(1200);//fake progress bar for 2000ms
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(noInterruption && fab.getVisibility() == View.VISIBLE){
                                    fab.performClick();
                                    fabProgressBar.clearAnimation();
                                    fabProgressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        },1201);
                        input.setEnabled(true);
                    }
                },500);

            }
            public void onPartialResults(Bundle partialResults)
            {
                ArrayList<String> partialResultsList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String result = partialResultsList.get(0);

                if (result.isEmpty()) {
                } else {
                    input.setEnabled(false);
                    if(oldResult.isEmpty()){
                        String originalText = input.getText().toString();
                        if(!originalText.isEmpty()){
                            Log.i("VOICERECOGNITION", "|LAST CHARACTER|"+originalText.substring(originalText.length() - 1)+"|");
                            if( input.getSelectionStart() == -1 || input.getSelectionStart() == input.getText().toString().length() - 1){//cursor at last or not exist
                                if(!Character.isWhitespace(originalText.charAt(originalText.length() - 1)) || Pattern.matches("\\p{Punct}", String.valueOf(originalText.charAt(originalText.length() - 1)))){
                                    input.append(" ");
                                }
                            }else {
                                if (input.getSelectionStart() == -1){
                                    input.append(" ");
                                }else{
                                    if(!Character.isWhitespace(originalText.charAt(input.getSelectionStart()-1)) || Pattern.matches("\\p{Punct}", String.valueOf(originalText.charAt(input.getSelectionStart()-1)))){
                                        input.getText().insert(input.getSelectionStart()," ");
                                    }
                                }
                            }
                        }
                    }
                    // resultCount++;
                    Log.i("VOICERECOGNITION", "|"+result+"|");
                    //Handler handler = new Handler();
                    //   handler.post(new Runnable() {
                    //   @Override
                    // public void run() {
                    //int currentCursorPos = input.getSelectionStart()-1;
                    // //System.out.println(currentCursorPos);
                    if(input.getSelectionStart() == -1){
                        if(result.toLowerCase().contains(oldResult.toLowerCase())){
                            input.append(result.substring(oldResult.length()));
                        }else {
                            input.getText().replace(input.getText().length()-oldResult.length(),input.getText().length(),result);
                        }
                        input.setSelection(input.getText().length()-1);
                    }else {
                        if(result.toLowerCase().contains(oldResult.toLowerCase())){
                            input.getText().insert(input.getSelectionStart(),result.substring(oldResult.length()));
                            //input.setSelection(input.getSelectionStart() + result.length()-1-oldResult.length()-1);

                        }else {
                            input.getText().replace(input.getSelectionStart()-oldResult.length(),input.getSelectionStart(),result);
                            //input.setSelection(input.getSelectionStart()+result.length()-1);
                        }
                    }
                    //input.moveCursorToVisibleOffset();
                    //  }
                    //  });
                    oldResult = result;
                    //resultCount = 0;
                }
            }
            public void onEvent(int eventType, Bundle params)
            {
            }
        });
        recognitionProgressView.play();
        doubleClickCout = 0;
        ItemClickSupport.addTo(todoList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, final int position, final View view) {
                long id = todoListAdapter.getItemId(position);
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
                    /*if(selectedId.contains(id)){
                        selectedId.remove(selectedId.indexOf(id));
                    }else {
                        selectedId.add(0,id);
                    }*/
                    // Toast.makeText(getApplicationContext(),selectedId.toString(),Toast.LENGTH_SHORT).show();
                }else {
                    /*doubleClickCout++;
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
                        finishData(id);
                        if(!modifyId.getText().toString().equals("")){
                            if(modifyId.getText().toString().equals(String .valueOf(id))){
                                modifyId.setText("");
                                input.setText("");
                                input.setVisibility(View.GONE);
                                hideKeyboard();
                                todoList.clearFocus();
                                adView.requestFocus();
                                fab.setImageResource(R.drawable.ic_add_black_24dp);
                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "finish_notes");
                                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "finished note");
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                            }
                        }
                        Snackbar.make(main, getString(R.string.note_finished_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                insertData(finishedContent);
                                long lastHistoryId = todosql.getIdOfLatestDataInHistory();
                                todosql.deleteFromHistory(String.valueOf(lastHistoryId));
                                displayAllNotes();
                            }
                        }).show();
                        justDoubleClicked = true;
                        doubleClickCout = 0;
                    }*/
                    //else if (doubleClickCout == 1) {
                        //Single click
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
                        todoList.smoothScrollBy(0,top);//scroll the clicked item to top
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
        ItemClickSupport.addTo(todoList).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, final View view) {
                long id = todoListAdapter.getItemId(position);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(30);
                if(isInSelectionMode){
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("ToDo", todosql.getOneDataInTODO(String.valueOf(id)));
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(main,getString(R.string.todo_copied),Snackbar.LENGTH_LONG).show();
                }else {
                    setOutOfSelectionMode();
                    proFab.setVisibility(View.INVISIBLE);
                    input.setVisibility(View.GONE);
                    isInSelectionMode = true;
                    getSupportLoaderManager().restartLoader(123,null,MainActivity.this);
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
                                todoList.getAdapter().notifyDataSetChanged();
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
                                            }
                                            return false;
                                        }
                                    });
                                }
                                long id;
                                selectedId.clear();
                                selectedContent.clear();
                                selectAll = true;
                                todoList.getAdapter().notifyDataSetChanged();
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


        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"clicked",Toast.LENGTH_SHORT).show();
                input.requestFocus();
                interruptAutoSend();
                showKeyboard();
                if(isAdd){
                    AnimatedVectorDrawable d = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_plus_to_send); // Insert your AnimatedVectorDrawable resource identifier
                    fab.setImageDrawable(d);
                    isAdd = false;
                    d.start();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            todoList.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    setEdgeEffect(todoList,themeColor);

                }
            });
        }else {
            todoList.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    setEdgeEffect(todoList,themeColor);
                }
            });
        }

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                interruptAutoSend();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //interruptAutoSend();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setOutOfSelectionMode();
                String inputText=input.getText().toString().trim();
                interruptAutoSend();
                if(inputText.isEmpty()||inputText.equals("")||input.getText()==null){
                    if(isAdd){
                        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_plus_to_send); // Insert your AnimatedVectorDrawable resource identifier
                        fab.setImageDrawable(d);
                        isAdd = false;
                        d.start();
                    }
                    input.setVisibility(View.VISIBLE);
                    showKeyboard();
                    input.requestFocus();
                }
                else{
                    int successModify=-1;
                    Uri success = null;
                    if (!modifyId.getText().toString().equals("")){
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "update_notes");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "updated notes"+input.getText().toString());
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                        successModify = updateData(Long.valueOf(modifyId.getText().toString()),input.getText().toString());
                    } else {
                        success = insertData(input.getText().toString());
                        int[] colors = {0, ColorUtils.lighten(textColor,0.6), 0};
                        //todoList.setDivider(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors));
                        //todoList.setDividerHeight(2);
                        todoList.smoothScrollToPosition(0);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "new_notes");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "new notes"+input.getText().toString());
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "function");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    }if(success != null || successModify != -1){
                        hideKeyboard();
                        displayAllNotes();
                        if(!isAdd){
                            AnimatedVectorDrawable d = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_send_to_plus); // Insert your AnimatedVectorDrawable resource identifier
                            fab.setImageDrawable(d);
                            isAdd = true;
                            d.start();
                        }
                        //input.clearFocus();
                        input.setVisibility(View.GONE);
                        input.setText("");
                        modifyId.setText("");
                    } else{
                        Toast.makeText(getApplicationContext(),getString(R.string.error_message),Toast.LENGTH_SHORT).show();
                        input.requestFocus();
                    }
                }
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();*/

            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(20);
                //speechRecognizer.stopListening();
                interruptAutoSend();
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault().getLanguage().trim());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_prompt));
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                //if(input.getVisibility() != View.VISIBLE){
                    //fab.setImageResource(le(R.drawable.avd_plus_to_sed));
                //}
                proFab.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.INVISIBLE);
                recognitionProgressView.setVisibility(View.VISIBLE);
                input.setVisibility(View.VISIBLE);
                input.setEnabled(false);
                if(isAdd){
                    AnimatedVectorDrawable d = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_plus_to_send); // Insert your AnimatedVectorDrawable resource identifier
                    fab.setImageDrawable(d);
                    isAdd = false;
                    d.start();
                }
                speechRecognizer.startListening(intent);
                return true;
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

        recognitionProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognitionProgressView.setVisibility(View.GONE);
                proFab.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                speechRecognizer.stopListening();
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        input.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                interruptAutoSend();
                return false;
            }
        });

        input.setOnFocusChangeListener(new View.OnFocusChangeListener(){
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
                            if(!isAdd){
                                AnimatedVectorDrawable d = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_send_to_plus); // Insert your AnimatedVectorDrawable resource identifier
                                fab.setImageDrawable(d);
                                isAdd = true;
                                d.start();
                            }
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

        IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener(){
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            //Toast.makeText(getApplicationContext(),"dsada",Toast.LENGTH_SHORT).show();
            /*if(consume){
                try {
                    mHelper.consumeAsync(inv.getPurchase(REMOVE_AD_SKU),mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }*/
            if(result.isFailure()||(!result.isSuccess())|| mHelper == null){
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_SHORT).show();
                    purchaseProgressDialog.dismiss();
                }
                adView= (AdView)findViewById(R.id.bannerAdView);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
                adView.setVisibility(View.VISIBLE);
                isAdRemoved = false;
                //iapsetup = false;
                return;
            }
            if(result.isSuccess()){
                //Toast.makeText(getApplicationContext(),"dsds",Toast.LENGTH_LONG).show();
                iapsetup = true;
                Purchase unlockPurchase = inv.getPurchase(REMOVE_AD_SKU);
                isAdRemoved = (unlockPurchase != null && verifyDeveloperPayload(unlockPurchase) && inv.hasPurchase(REMOVE_AD_SKU));
                if(unlockPurchase != null && verifyDeveloperPayload(unlockPurchase) && inv.hasPurchase(REMOVE_AD_SKU)){
                    removeAd();
                    return;
                }else {
                    adView= (AdView)findViewById(R.id.bannerAdView);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView.loadAd(adRequest);
                    adView.setVisibility(View.VISIBLE);
                    isAdRemoved = false;
                    if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                        Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(),"3",Toast.LENGTH_SHORT).show();
                        purchaseProgressDialog.dismiss();
                    }
                }
            }else {
                adView= (AdView)findViewById(R.id.bannerAdView);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
                adView.setVisibility(View.VISIBLE);
                isAdRemoved = false;
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"4",Toast.LENGTH_SHORT).show();
                    purchaseProgressDialog.dismiss();
                }
            }
        }
    };

    public boolean restorePurchase(){
        try {
            if (mHelper != null) mHelper.flagEndAsync();
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return isAdRemoved;
    }

    public void fakeProgress(int ms){
        ObjectAnimator animator = ObjectAnimator.ofInt(fabProgressBar,"progress",0,1000);
        animator.setDuration(ms);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();

    }
    // TODO: 2017/9/29 Apply the margin setting to real ad remove situation(onCreate, ad loaded, resume and etc.

    public void removeAd(){
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
    }

    public void setOutOfSearchMode(){
        proFab.setVisibility(View.VISIBLE);
        isInSearchMode = false;
        getSupportLoaderManager().restartLoader(123,null,this);
        displayAllNotes();
        hideKeyboard();
    }

    /*
    public View getViewByPosition(int pos) {
        //final int firstListItemPosition = todoList.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + todoList.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return todoList.getAdapter().getView(pos, null, todoList);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return todoList.getChildAt(childIndex);
        }
    }*/

    public void setOutOfSelectionMode(){
        isInSelectionMode = false;
        proFab.setVisibility(View.VISIBLE);
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
        todoList.requestFocus();
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

    /*public void setLauncherIcon(){
        Intent launcherIntent = new Intent();
        launcherIntent.setClassName("com.jackz314.todAAAo","MainActivity");
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
    }*/

    boolean verifyDeveloperPayload(Purchase p) {
        if(p.getDeveloperPayload() != null && p.getDeveloperPayload().contains("0x397821dc97276")){
            if(p.getDeveloperPayload().equals(
                    "0x397821dc97276"+
                    "CPMFnxQ5s0" +
                    "NBVs3kWNgN" +
                    "ivr1zfRbfk" +
                    "U1lCak93su" +
                    "RlMWFgHQMj" +
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

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            //Toast.makeText(getApplicationContext(),"finished",Toast.LENGTH_SHORT).show();
            if (mHelper == null || result.isFailure() || !verifyDeveloperPayload(purchase)) {
                Toast.makeText(getApplicationContext(),getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"5 " + String.valueOf(mHelper == null) + String.valueOf(result.isFailure()) + String.valueOf(!verifyDeveloperPayload(purchase)),Toast.LENGTH_SHORT).show();
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    purchaseProgressDialog.dismiss();
                }
                return;
            }
            if(purchase.getSku().equals(REMOVE_AD_SKU)){
                try {
                    //Toast.makeText(getApplicationContext(),"SUCCESS",Toast.LENGTH_SHORT).show();
                    if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                        purchaseProgressDialog.setMessage(getString(R.string.verifying));
                    }
                    if (mHelper != null) mHelper.flagEndAsync();
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                    return;
                    //iapsetup = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"6",Toast.LENGTH_SHORT).show();
                    if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                        purchaseProgressDialog.dismiss();
                    }
                    //System.out.println("qazwsx"+5);
                    iapsetup = false;
                    return;
                }
            }else {
                Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"7",Toast.LENGTH_SHORT).show();
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    purchaseProgressDialog.dismiss();
                }
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

    public static void setCursorColor(EditText view, int color) {
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

    public void setColorPreferences(){
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor = sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorActualPrimary));
        textColor = sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        textSize = sharedPreferences.getInt(getString(R.string.text_size_key),24);
        backgroundColor = sharedPreferences.getInt(getString(R.string.background_color_key),Color.WHITE);

        /*
        set colors
         */
        int[] colorsBACKUP = {// logo color
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };

        int[] colors = {// logo color
                 ColorUtils.lighten(themeColor, 0.3),
                 ColorUtils.lighten(themeColor, 0.2),
                 ColorUtils.lighten(themeColor, 0.15),
                 ColorUtils.lighten(themeColor, 0.25),
                 ColorUtils.lighten(themeColor, 0.4)
        };

        //int[] heights = { 20, 24, 18, 23, 16 };
        int[] heights = { 30, 36, 27, 35, 24 };

        recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);
        recognitionProgressView.setColors(colors);
        recognitionProgressView.setBarMaxHeightsInDp(heights);
        recognitionProgressView.setCircleRadiusInDp(3);
        recognitionProgressView.setSpacingInDp(3);
        recognitionProgressView.setIdleStateAmplitudeInDp(2);
        recognitionProgressView.setRotationRadiusInDp(12);
        recognitionProgressView.play();
        LayoutInflater inflater = LayoutInflater.from(this);
        //View navMainView = inflater.inflate(R.layout.nav_header_main,null);
        if(ColorUtils.determineBrightness(backgroundColor) < 0.5){
            navigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#fafafa")));
        }else {
            navigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#212121")));
        }
        navigationView.setItemIconTintList(ColorStateList.valueOf(themeColor));
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
        fab.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        toolbar.setBackgroundColor(themeColor);
        input.setTextColor(textColor);
        input.setTextSize(24);
        setCursorColor(input,themeColor);
        main.setBackgroundColor(backgroundColor);
        Window window = this.getWindow();
        window.setStatusBarColor(themeColor);
        window.setNavigationBarColor(themeColor);
        if(ColorUtils.determineBrightness(backgroundColor) < 0.5){// dark
            EmptextView.setTextColor(Color.parseColor("#7FFFFFFF"));
        }else {//bright
            EmptextView.setTextColor(Color.parseColor("#61000000"));

        }
        todoList.setBackgroundColor(backgroundColor);
        navigationView.setBackgroundColor(backgroundColor);
        View listView= LayoutInflater.from(MainActivity.this).inflate(R.layout.todolist,null);
        if(ColorUtils.determineBrightness(backgroundColor) < 0.5){// dark
            input.setHintTextColor(ColorUtils.makeTransparent(textColor,0.5));
        }else {
            input.setHintTextColor(ColorUtils.makeTransparent(textColor,0.38));
        }
        input.setLinkTextColor(themeColor);
        input.setHighlightColor(ColorUtils.lighten(themeColor,0.3));
        input.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        //todoList.setDivider(new GradientDrawable(GradientDrawable.Orientation.TR_BL, colors));
        //todoList.setDividerHeight(2);
}

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 0:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),getString(R.string.thanks_for_corporation),Toast.LENGTH_SHORT).show();
                    fab.performLongClick();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(getApplicationContext(),getString(R.string.voice_permission_request),Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
        }
    }

    public void purchaseRemoveAds(){
        try {
            new ConnectionDetector().execute().get(1000, TimeUnit.MILLISECONDS);
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
                mHelper.launchPurchaseFlow(this, REMOVE_AD_SKU, REMOVE_REQUEST_ID, mPurchaseFinishedListener, todoTableId);
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

    /*
    public void displaySearchResults(final String filter){
        final Cursor cursor = todosql.getSearchResults(filter);
        if(cursor.getCount() == 0){
            EmptextView.setVisibility(View.VISIBLE);
            EmptextView.setText(R.string.empty_search_result);
            todoList.removeAllViewsInLayout();//remove all items
            todoList.setAdapter(null);
        } else {
            EmptextView.setVisibility(View.GONE);
            EmptextView.setText("");
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

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {

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
            Drawable finishIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_done_black_24dp);//draw finish icon
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

        public void displayAllNotes(){
        if(todoList.getAdapter() == null){
            ////System.out.println("null called");
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            todoList.setLayoutManager(linearLayoutManager);
            todoListAdapter = (new TodoListAdapter(null){
                @Override
                public void onBindViewHolder(TodoViewHolder holder, Cursor cursor) {
                    super.onBindViewHolder(holder, cursor);
                    final long id = cursor.getInt(cursor.getColumnIndex(dtb.ID));
                    String text = cursor.getString(cursor.getColumnIndex(dtb.TITLE));
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
                                spannable.setSpan(new TextAppearanceSpan(null,Typeface.BOLD,-1,new ColorStateList(new int[][] {new int[] {}},new int[] {Color.parseColor("#ef5350")}),null), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }while (startPos > 0);
                            holder.todoText.setText(spannable);
                        }
                    }else {
                        holder.todoText.setText(text);
                    }
                    //System.out.println("null called");
                    if(isInSelectionMode){
                        holder.cBox.setVisibility(View.VISIBLE);
                        if(selectAll){
                            holder.cBox.setChecked(true);
                        }
                        if(unSelectAll){
                            holder.cBox.setChecked(false);
                        }
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
                                if(mContext.toString().contains("MainActivity")){
                                    ((MainActivity)mContext).removeSelectedId(id);
                                }else if(mContext.toString().contains("HistoryActivity")){
                                    ((HistoryActivity)mContext).removeSelectedId(id);
                                }                    // do some operations here
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
            todoList.setAdapter(todoListAdapter);
            getSupportLoaderManager().initLoader(123, null, this);
            ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            mItemTouchHelper.attachToRecyclerView(todoList);
        }
        getSupportLoaderManager().restartLoader(123,null,this);
    }

    public void interruptAutoSend(){
        noInterruption = false;
        fabProgressBar.setVisibility(View.INVISIBLE);
        //stop circle
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

    public void shareSetOfData(){//share note function
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = null;
        StringBuilder shareBodyBuilder = new StringBuilder();
        shareBody = getString(R.string.note_share_content_header);
        for(String data : selectedContent){
            shareBodyBuilder.append(data);
            shareBodyBuilder.append("\n\n");//empty line after each note
        }
        shareBody = shareBodyBuilder.toString();
        String shareSub = getString(R.string.note_share_subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
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
            //todoList.setSelectionFromTop(firstVisibleItem,firstItemDiff);
            if(!input.getText().toString().equals("")){
                fab.setImageResource(R.drawable.ic_send_black_24dp);
                isAdd = false;
                input.setVisibility(View.VISIBLE);
                showKeyboard();
            }
            //System.out.println("Orientation Changed!");
        }
        super.onConfigurationChanged(newConfig);
     }

    @Override
    public void onBackPressed() {
        interruptAutoSend();
        if(recognitionProgressView != null && recognitionProgressView.getVisibility() == View.VISIBLE){
            recognitionProgressView.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            proFab.setVisibility(View.VISIBLE);
        }
        speechRecognizer.stopListening();
        if(isInSelectionMode || isInSearchMode){
            if(isInSelectionMode){
                setOutOfSelectionMode();
            }
            if (isInSearchMode){
                setOutOfSearchMode();
            }
        }else {
            //System.out.println(String.valueOf(exit));
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            //main.requestFocus();
            //input.clearFocus();
            hideKeyboard();
            displayAllNotes();
            if (input.getVisibility() == View.GONE){
                justex = true;
            }else {
                if(input.getText().toString().equals("")){
                    if(!isAdd){
                        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_send_to_plus); // Insert your AnimatedVectorDrawable resource identifier
                        fab.setImageDrawable(d);
                        isAdd = true;
                        d.start();
                    }
                    input.setVisibility(View.GONE);
                    justex = false;
                    modifyId.setText("");
                    hideKeyboard();
                } else {
                    input.setText("");
                    modifyId.setText("");
                    justex=false;
                    hideKeyboard();
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit=0;
                }
            }, 1500);
            if(justex&&!drawer.isDrawerOpen(GravityCompat.START)){
                exit++;
                Toast.makeText(getApplicationContext(),R.string.press_again_to_exit,Toast.LENGTH_SHORT).show();
            }
            //justex = true;
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

    //TODO FIX THEMESELECTOR SUMMMARY TEXT COLOR
    //TODO SET SEARCHVIEW ANIMATION
    //TODO OPTIMIZE ALL CODE
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            hintText.setSpan( new ForegroundColorSpan(Color.parseColor("#7FFFFFFF")), 0, hintText.length(), 0 );
        }else {//light themeColor
            hintText.setSpan( new ForegroundColorSpan(Color.parseColor("#61000000")), 0, hintText.length(), 0 );
        }
        searchView.setQueryHint(hintText);
        MenuItem searchMenuIem = menu.findItem(R.id.todo_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuIem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isInSearchMode = true;
                proFab.setVisibility(View.INVISIBLE);
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
                proFab.setVisibility(View.INVISIBLE);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //System.out.println(intent.getAction()+" IDENTIFIII");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleVoiceSearch(intent);
        }

    }

    public void query(String text) {
        Bundle bundle = new Bundle();
        bundle.putString("QUERY", text);
        searchText = text;
        //System.out.println("calledquery" + " " + text);
        getSupportLoaderManager().restartLoader(123, bundle, MainActivity.this);
    }

    public void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            float d = getApplicationContext().getResources().getDisplayMetrics().density;
            p.setMargins((int)(l*d), (int)(t*d), (int)(r*d), (int)(b*d));//dp to pixels
            v.requestLayout();
        }
    }

    public void handleGoogleNowCall(Intent intent){
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
                    insertData(finalNote);
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
        if(!input.getText().toString().equals("") && input.getVisibility()==View.VISIBLE) showKeyboard();
        displayAllNotes();
        setColorPreferences();
        int size = menuNav.size();
        String sort = null;
        Intent voiceIntent = getIntent();
        String historySettingPref = "MII";
        String bep = "ANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiZZobdX3yEuQtssAfZ2AE69Agvit3KuCfR6ywZRlrcpjWKb5+P2oT72hEaw5FwDCsFquccZvt6R8nKBD1ucbl4PCgZvrUie9EFQR4YKxlp9iPogdreu8ifIjR/un9sFsiRGndmjhgJHMx66uKlDX7gyu9/EzuxFVajPCdbw7nQdK9XJzBripYLKY0w5/BLbKaOo7kmhSwiOlsRQwayIbXvUiYQb5ij17eFO/n4sebKNvixdIsaU3YaFlh/CbEpy/3P0UEHtrtb3B27pBa4+3kEriVc7uVBN+kYHmMQRMBgyjzKNwITDhHrP12qjlmrVk4LKehQVVDmPymB/C1/qTuwIDAQAB";
        historySettingPref += "BIjAN" + bep.substring(2,bep.length());
        adView.setAdListener(new AdListener(){//resume ad when got internet
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
        });
        try {
            if (voiceIntent != null && voiceIntent.getAction() != null){
                if(voiceIntent.getAction().equals(getString(R.string.google_now_request_code)) && voiceIntent.getStringExtra(Intent.EXTRA_TEXT) != null) {
                    //System.out.println("fucking text: ");
                    handleGoogleNowCall(voiceIntent);
                }if(voiceIntent.getAction().equals(Intent.ACTION_SEND) && voiceIntent.getStringExtra(Intent.EXTRA_TEXT) != null){
                    if(!voiceIntent.getStringExtra(Intent.EXTRA_TEXT).trim().equals("")){
                        insertData(voiceIntent.getStringExtra(Intent.EXTRA_TEXT));
                        Toast.makeText(getApplicationContext(),getString(R.string.note_added),Toast.LENGTH_LONG).show();
                        finish();
                    }
                    getIntent().removeExtra(Intent.EXTRA_TEXT);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(sharedPreferences.getBoolean(getString(R.string.order_key),true)){
            sort = "_id DESC";
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        getSupportLoaderManager().restartLoader(123,null,this);
        for (int i = 0; i < size; i++) {
            menuNav.getItem(i).setChecked(false);
        }
        if(isAdRemoved){
            adView.destroy();
            displayAllNotes();
            Log.i("unlock","skipped ad");
        }
        else{
            adView= (AdView)findViewById(R.id.bannerAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
        if(!sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) != null){
            navigationView.getMenu().removeItem(R.id.history);
        }if(sharedPreferences.getBoolean(getString(R.string.main_history_switch),true) && menuNav.findItem(R.id.history) == null){
            //Toast.makeText(getApplicationContext(),"f",Toast.LENGTH_LONG).show();
            menuNav = navigationView.getMenu();
            menuNav.add(R.id.nav_category_main,R.id.history,0,getString(R.string.nav_history)).setIcon(R.drawable.ic_history_black_24dp);
        }
        if(sharedPreferences.getBoolean("first_run",true)){
            Cursor cs = todosql.getData();
            if (cs.getCount()==0){
                //first run codes
                insertData(getString(R.string.tutorial_6));
                insertData(getString(R.string.tutorial_5));
                insertData(getString(R.string.tutorial_4));
                insertData(getString(R.string.tutorial_3));
                insertData(getString(R.string.tutorial_2));
                insertData(getString(R.string.tutorial_1));
                insertData(getString(R.string.welcome_note));
                displayAllNotes();
                sharedPreferences.edit().putBoolean("first_run",false).apply();
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

    /*private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }*/

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
            if(iapsetup){
                hideKeyboard();
                purchaseRemoveAds();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REMOVE_REQUEST_ID){
            if(resultCode == RESULT_OK){
                if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                    // not handled, so handle it ourselves (here's where you'd
                    // perform any handling of activity results not related to in-app
                    // billing...
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
            if(resultCode == RESULT_CANCELED){
                if(purchaseProgressDialog != null && purchaseProgressDialog.isShowing()){
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                    purchaseProgressDialog.dismiss();
                }
                isAdRemoved = false;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort = null;
        setColorPreferences();
        if(sharedPreferences.getBoolean(getString(R.string.order_key),true)){
            sort = "_id DESC";
        }
        if (args != null) {
            String[] selectionArgs = new String[]{"%" + args.getString("QUERY") + "%"};
            return new CursorLoader(this, AppContract.Item.TODO_URI, PROJECTION, SELECTION, selectionArgs, sort);
        }
        return new CursorLoader(this, AppContract.Item.TODO_URI, PROJECTION, null, null, sort);
    }

    public static String getThisPackageName(){
        return MainActivity.class.getPackage().getName();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ////System.out.println("dataCount" + data.getCount() + " " + isInSearchMode);
        if(data.getCount() == 0 && isInSearchMode){
            EmptextView.setVisibility(View.VISIBLE);
            EmptextView.setText(getString(R.string.empty_search_result));
        }else if(data.getCount() == 0 && !isInSearchMode){
            EmptextView.setVisibility(View.VISIBLE);
            EmptextView.setText(R.string.empty_todolist);
        }else {
            EmptextView.setVisibility(View.GONE);
            EmptextView.setText("");
        }
        todoListAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        todoListAdapter.changeCursor(null);
    }

    //database handling
    public int updateData(long id, String title){
        if (!title.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(TITLE, title);
            Uri uri = ContentUris.withAppendedId(AppContract.Item.TODO_URI, id);
            return getContentResolver().update(uri, values, null, null);
        }else return -1;

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
}

