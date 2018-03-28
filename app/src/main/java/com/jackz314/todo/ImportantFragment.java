package com.jackz314.todo;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitrymalkovich.android.ProgressFloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jackz314.todo.speechrecognitionview.RecognitionProgressView;
import com.jackz314.todo.speechrecognitionview.adapters.RecognitionListenerAdapter;
import com.jackz314.todo.utils.ColorUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static com.jackz314.todo.DatabaseManager.IMPORTANCE;
import static com.jackz314.todo.MainActivity.determineContainedTags;
import static com.jackz314.todo.MainActivity.removeCharAt;
import static com.jackz314.todo.MainActivity.setCursorColor;
import static com.jackz314.todo.SetEdgeColor.setEdgeColor;
import static com.jackz314.todo.DatabaseManager.DATE_FORMAT;
import static com.jackz314.todo.DatabaseManager.ID;
import static com.jackz314.todo.DatabaseManager.IMPORTANCE_TIMESTAMP;
import static com.jackz314.todo.DatabaseManager.REMIND_TIME;
import static com.jackz314.todo.DatabaseManager.TITLE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImportantFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImportantFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImportantFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    //todo got data from the wrong parameter, fix it
    private static final String ARG_PARAM = "param";
    private String mParam;
    private static final String[] PROJECTION = new String[]{"*"};//
    private static final String SELECTION = IMPORTANCE + " > 0";//ignore markdown signs when handling displaying notes //1 in sqlite means TRUE for boolean values
    private static final String SELECTION_WITH_QUERY = "REPLACE (title, '*', '')" + " LIKE ?" + " AND (" + SELECTION;//ignore markdown signs whe handling displaying notes
    public boolean isInSearchMode = false, isInSelectionMode = false;
    public ArrayList<Long> selectedId = new ArrayList<>();
    public ArrayList<String> selectedContent = new ArrayList<>();
    public ArrayList<String> CLONESelectedContent = new ArrayList<>();
    public String searchText;
    private FirebaseAnalytics mFirebaseAnalytics;
    private OnFragmentInteractionListener mListener;
    private String todoTableId = "HAHA! this is the real one, gotcha";
    DatabaseManager todosql;
    EditText input;
    FloatingActionButton fab;
    TextView modifyId;
    RecyclerView todoList;
    boolean justex = false;
    boolean selectAll = false, unSelectAll = false;
    SharedPreferences sharedPreferences;
    String oldResult = "";
    int themeColor,textColor,backgroundColor,textSize;
    int doubleClickCount = 0;
    long selectedItemID;
    ConstraintLayout main;
    Boolean noInterruption = true;
    TodoListAdapter todoListAdapter;
    TextView emptyTextView, selectionTitle;
    CheckBox multiSelectionBox;
    SpeechRecognizer speechRecognizer;
    RecognitionProgressView recognitionProgressView;
    boolean isAdd = true;
    ProgressFloatingActionButton proFab;
    ProgressBar fabProgressBar;
    Menu menuNav;
    Toolbar selectionToolBar, toolbar;
    CheckBox selectAllBox;

    public ImportantFragment() {
        // Required empty public constructor
    }

    public static ImportantFragment newInstance(int position) {
        ImportantFragment fragment = new ImportantFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM, position);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnImportantBackPressedListener {
        public void doBack();
    }

    public class ImportantBackPressedListener implements OnImportantBackPressedListener {
        private final FragmentActivity activity;

        public ImportantBackPressedListener(FragmentActivity activity) {
            this.activity = activity;
        }

        @Override
        public void doBack() {//go back to main tab
            //activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ViewPager viewPager = getActivity().findViewById(R.id.pager);
            viewPager.setCurrentItem(1);
        }
    }

    public interface OnImportantQueryListener {
        void doQuery(String query);
    }

    public class ImportantQueryListener implements OnImportantQueryListener {

        //private String query;

        public ImportantQueryListener(){
            //this.query = query;
        }

        @Override
        public void doQuery(String query) {
            query(query);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        setHasOptionsMenu(true);

        //implement onQueryListener
        ((MainActivity)getActivity()).setOnImportantQueryListner(new ImportantQueryListener() {
            @Override
            public void doQuery(String query) {
                super.doQuery(query);
            }
        });


        //implement onBackPressed
        ((MainActivity)getActivity()).setOnImportantBackPressedListener(new ImportantBackPressedListener(getActivity()) {
            @Override
            public void doBack() {
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
                    DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                    //main.requestFocus();
                    //input.clearFocus();
                    hideKeyboard();
                    displayAllNotes();
                    if (input.getVisibility() == View.GONE){
                        justex = true;
                    }else {
                        if(input.getText().toString().equals("")){
                            if(!isAdd){
                                AnimatedVectorDrawable d = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_send_to_plus); // Insert your AnimatedVectorDrawable resource identifier
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
                    //justex = true;
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    }
                    if(justex&&!drawer.isDrawerOpen(GravityCompat.START)){
                        super.doBack();
                    }
                }
            }
        });
        return inflater.inflate(R.layout.fragment_important, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        sharedPreferences = getContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        input = getView().findViewById(R.id.important_input);
        modifyId = getView().findViewById(R.id.important_modify_id);
        emptyTextView = getActivity().findViewById(R.id.important_empty_text);
        todoList = getView().findViewById(R.id.important_todolist);
        todoList.setHasFixedSize(true);
        fab = getView().findViewById(R.id.important_fab);
        proFab = getView().findViewById(R.id.important_progress_fab);
        fabProgressBar = getView().findViewById(R.id.important_fab_progress_bar);
        toolbar =  getActivity().findViewById(R.id.toolbar);
        selectionToolBar = getActivity().findViewById(R.id.selection_toolbar);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        main = getView().findViewById(R.id.importantFragmentLayout);
        todoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                setEdgeColor(todoList,themeColor);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                setEdgeColor(todoList,themeColor);
            }
        });
        //speechRecognizer.setRecognitionListener(new speechListener());
        todosql = new DatabaseManager(getContext());
        todoTableId = "0x397821dc97276";
        //set tabs
        input.setTextIsSelectable(true);
        input.setFocusable(true);
        todoList.setFocusable(true);
        todoList.setFocusableInTouchMode(true);
        setColorPreferences();
        displayAllNotes();
        if(!input.getText().toString().equals("")){
            input.setVisibility(View.VISIBLE);
            showKeyboard();
        }
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
                    //Toast.makeText(getContext(,String.valueOf(error),Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(),getString(R.string.voice_permission_request),Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.RECORD_AUDIO},0);
                    }else if(error == SpeechRecognizer.ERROR_AUDIO){
                        Toast.makeText(getContext(),getString(R.string.voice_recon_audio_record_err),Toast.LENGTH_LONG).show();
                    }else if(error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT || error == SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_SERVER ){
                        Toast.makeText(getContext(),getString(R.string.voice_recon_internet_err),Toast.LENGTH_LONG).show();
                    }else if(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                        Toast.makeText(getContext(),getString(R.string.voice_recon_busy_client_err),Toast.LENGTH_LONG).show();
                    }
                }
                //Toast.makeText(getContext(,getString(R.string.speech_to_text_failed) + String.valueOf(error), Toast.LENGTH_LONG).show();
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
                            //Log.i("VOICERECOGNITION", "|LAST CHARACTER|"+originalText.substring(originalText.length() - 1)+"|");
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
                    //Log.i("VOICERECOGNITION", ">"+str+"<");
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
                            //Log.i("VOICERECOGNITION", "|LAST CHARACTER|"+originalText.substring(originalText.length() - 1)+"|");
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
                    //Log.i("VOICERECOGNITION", "|"+result+"|");
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

        ItemClickSupport.addTo(todoList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, final int position, final View view) {
                long id = todoListAdapter.getItemId(position);
                unSelectAll = false;
                selectAll = false;
                if(isInSearchMode){
                    setOutOfSearchMode();
                }
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
                    // Toast.makeText(getContext(,selectedId.toString(),Toast.LENGTH_SHORT).show();
                }else {
                    /*doubleClickCout++; DOUBLE CLICK METHOD DEPRECIATED
                    Handler handler = new Handler();
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            doubleClickCout = 0;
                        }
                    };
                    if (doubleClickCout == 2) {
                        //Double click
                        // Toast.makeText(getContext(,"sadadadadasdasdasdassdassd",Toast.LENGTH_SHORT).show();
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
                        Snackbar.make(getView(), getString(R.string.note_finished_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
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
                    if(isAdd){//if current button displays "+" sign
                        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_plus_to_send); // Insert your AnimatedVectorDrawable resource identifier
                        fab.setImageDrawable(d);
                        isAdd = false;
                        d.start();
                    }
                    input.setVisibility(View.VISIBLE);
                    input.setText(todosql.getOneDataInTODO(id));
                    input.requestFocus();
                    input.setSelection(input.getText().length());
                    showKeyboard();
                    int top = view.getTop();
                    todoList.smoothScrollBy(0,top);//scroll the clicked item to top
                    //Toast.makeText(getContext(,String.valueOf(position),Toast.LENGTH_LONG).show();
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
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                //v.vibrate(30);
                if(isInSelectionMode){
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("ToDo", todosql.getOneDataInTODO(id));
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(getView(),getString(R.string.todo_copied),Snackbar.LENGTH_LONG).show();
                }else {
                    if(isInSearchMode){
                        setOutOfSearchMode();
                    }
                    setOutOfSelectionMode();
                    proFab.setVisibility(View.INVISIBLE);
                    input.setVisibility(View.GONE);
                    isInSelectionMode = true;
                    selectedItemID = id;
                    getLoaderManager().restartLoader(123,null,ImportantFragment.this);
                    displayAllNotes();
                    selectionTitle = (TextView)selectionToolBar.findViewById(R.id.selection_toolbar_title);
                    toolbar.setVisibility(View.GONE);
                    selectionToolBar.setVisibility(View.VISIBLE);
                    selectionTitle.setText(getString(R.string.selection_mode_title));
                    //Drawable backArrow = getDrawable(R.drawable.ic_close_black_24dp);
                    //selectionToolBar.setNavigationIcon(backArrow);
                    selectionToolBar.setBackgroundColor(themeColor);
                    selectAllBox = selectionToolBar.findViewById(R.id.select_all_box);
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
                                            }else if (item.getItemId() == R.id.selection_menu_export){
                                                boolean succ = exportOrPrint();
                                                if (succ){
                                                    Toast.makeText(getContext(),getString(R.string.export_succeed),Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(getContext(),getString(R.string.export_failed),Toast.LENGTH_LONG).show();
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
                                todoList.getAdapter().notifyDataSetChanged();
                                Cursor cursor = todosql.getImportantData();
                                cursor.moveToFirst();
                                do{
                                    id = cursor.getInt(cursor.getColumnIndex(ID));
                                    selectedId.add(0,id);
                                    String data = todosql.getOneDataInTODO(id);
                                    selectedContent.add(0,data);
                                }while (cursor.moveToNext());
                                String count = Integer.toString(selectedId.size());
                                selectionTitle.setText(count + getString(R.string.selection_mode_title));
                            }
                        }
                    });
                    addSelectedId(id);
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
                }
                return true;
            }
        });

        input.addTextChangedListener(new TextWatcher() {//todo implement when input "#", pop up choosing list of in-use tags
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.equals("#")){
                    Toast.makeText(getContext(),"TAG DETECTED",Toast.LENGTH_SHORT).show();//todo make tags into ITALIC on text change
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(,"clicked",Toast.LENGTH_SHORT).show();
                input.requestFocus();
                interruptAutoSend();
                showKeyboard();
                if(isAdd){
                    AnimatedVectorDrawable d = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_plus_to_send); // Insert your AnimatedVectorDrawable resource identifier
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
                    setEdgeColor(todoList,themeColor);
                }
            });
        }else {
            todoList.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    setEdgeColor(todoList,themeColor);
                }
            });
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setOutOfSelectionMode();
                String inputText=input.getText().toString().trim();
                interruptAutoSend();
                if(inputText.isEmpty()||inputText.equals("")||input.getText()==null){
                    if(isAdd){
                        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_plus_to_send); // Insert your AnimatedVectorDrawable resource identifier
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
                            AnimatedVectorDrawable d = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_send_to_plus); // Insert your AnimatedVectorDrawable resource identifier
                            fab.setImageDrawable(d);
                            isAdd = true;
                            d.start();
                        }
                        //input.clearFocus();
                        input.setVisibility(View.GONE);
                        input.setText("");
                        modifyId.setText("");
                    } else{
                        Toast.makeText(getContext(),getString(R.string.error_message),Toast.LENGTH_SHORT).show();
                        input.requestFocus();
                    }
                }
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
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
                    AnimatedVectorDrawable d = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_plus_to_send); // Insert your AnimatedVectorDrawable resource identifier
                    fab.setImageDrawable(d);
                    isAdd = false;
                    d.start();
                }
                speechRecognizer.startListening(intent);
                return true;
            }
        });

        recognitionProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognitionProgressView.setVisibility(View.GONE);
                proFab.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                speechRecognizer.stopListening();
            }
        });


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
                    //Toast.makeText(getContext(,"called showKeyboard!",Toast.LENGTH_SHORT).show();
                    showKeyboard();
                }
                else {
                    hideKeyboard();
                    main.requestFocus();
                    //Toast.makeText(getContext(,"focus cleared, touched, request focus",Toast.LENGTH_SHORT).show();
                    if(input.isCursorVisible()||input.isInEditMode()||input.isInputMethodTarget()||input.isFocused()||input.hasFocus()){
                        //input.clearFocus();
                        //Toast.makeText(getContext(,"focus cleared, touched, request focus",Toast.LENGTH_SHORT).show();
                        //main.requestFocus();
                        hideKeyboard();
                        if(input.getText().toString().equals("")||input.getText().toString().isEmpty()){
                            //Toast.makeText(getContext(,"3",Toast.LENGTH_SHORT).show();
                            modifyId.setText("");
                            if(!isAdd){
                                AnimatedVectorDrawable d = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_send_to_plus); // Insert your AnimatedVectorDrawable resource identifier
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

        doubleClickCount = 0;
        toolbar.setOnClickListener(new View.OnClickListener() {//double click toolbar to scroll to the top
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
                    todoList.smoothScrollToPosition(0);//smooth scroll to top
                }
            }
        });

        if(!isInSearchMode && !isInSelectionMode){//refresh important notes every minute
            final Handler handler = new Handler();
            final int delay = 1000 * 60; //every minute
            handler.postDelayed(new Runnable(){
                public void run(){
                    getLoaderManager().restartLoader(123,null,ImportantFragment.this);
                    if(getContext() != null && !isInSearchMode && !isInSelectionMode){
                        TabLayout tabLayout = getActivity().findViewById(R.id.tabs_layout);
                        if(tabLayout.getSelectedTabPosition() == 0){//continue if still displaying
                            handler.postDelayed(this, delay);
                        }
                    }
                }
            }, delay);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getString(ARG_PARAM);
        }

    }

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
            final String finishedContent = todosql.getOneDataInTODO(viewHolder.getItemId());
            finishData(viewHolder.getItemId());
            Snackbar.make(getView(), getString(R.string.note_finished_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
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
            Drawable finishIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_done_black_24dp);//draw finish icon
            finishIcon.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
            int finishIconMargin = 40;
            int itemHeight = itemView.getBottom() - itemView.getTop();
            int intrinsicWidth = finishIcon.getIntrinsicWidth();
            int intrinsicHeight = finishIcon.getIntrinsicWidth();
            int finishIconLeft = itemView.getRight() - finishIconMargin - intrinsicWidth - bounds.width() - 8;
            int finishIconRight = itemView.getRight() - finishIconMargin - bounds.width() - 8;
            int finishIconTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
            int finishIconBottom = finishIconTop + intrinsicHeight;
            finishIcon.setBounds(finishIconLeft, finishIconTop, finishIconRight, finishIconBottom);
            finishIcon.draw(c);
            //fade out the view
            final float alpha = 1.0f - Math.abs(dX) / (float) viewHolder.itemView.getWidth();//1.0f == ALPHA FULL
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
            c.drawText(getString(R.string.finish),(float) itemView.getRight() - 48 - bounds.width() ,(((finishIconTop+finishIconBottom)/2) - (textPaint.descent()+textPaint.ascent())/2), textPaint);
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    };

    public void fakeProgress(int ms){
        ObjectAnimator animator = ObjectAnimator.ofInt(fabProgressBar,"progress",0,1000);
        animator.setDuration(ms);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    public void setOutOfSearchMode(){
        proFab.setVisibility(View.VISIBLE);
        if(!(input.getText().toString().equals(""))){//if input had text before entering the search mode, set it to visible here
            input.setVisibility(View.VISIBLE);
        }
        isInSearchMode = false;
        getLoaderManager().restartLoader(123,null,this);
        displayAllNotes();
        hideKeyboard();
    }

    public void setOutOfSelectionMode(){
        isInSelectionMode = false;
        proFab.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(123,null,this);
        selectedId.clear();
        selectedContent.clear();
        selectAll = false;
        unSelectAll = false;
        displayAllNotes();
        selectionToolBar.getMenu().clear();
        selectionToolBar.setVisibility(View.GONE);
        if(selectAllBox != null){
            selectAllBox.setChecked(false);
        }
        toolbar.setVisibility(View.VISIBLE);
        todoList.requestFocus();
    }

    public void addSelectedId(long id){
        selectedId.add(0,id);
        String data = todosql.getOneDataInTODO(id);
        selectedContent.add(0,data);
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
        String data = todosql.getOneDataInTODO(id);
        selectedContent.remove(selectedContent.indexOf(data));
        if(selectedId.size() < todosql.getData().getCount()){
            selectAllBox.setChecked(false);
        }
        if (selectedId.size() == 0) {
            selectionTitle.setText(getString(R.string.selection_mode_empty_title));
            selectionToolBar.getMenu().clear();
        }else {
            String count = Integer.toString(selectedId.size());
            selectionTitle.setText(count + getString(R.string.selection_mode_title));
        }
    }

    public void setColorPreferences(){
        sharedPreferences = getContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor = sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorActualPrimary));
        textColor = sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        textSize = sharedPreferences.getInt(getString(R.string.text_size_key),24);
        backgroundColor = sharedPreferences.getInt(getString(R.string.background_color_key),Color.WHITE);

        /*
        set colors
         */
        /*int[] colorsBACKUP = {// logo color
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };*/

        int[] colors = {// logo color
                ColorUtils.lighten(themeColor, 0.3),
                ColorUtils.lighten(themeColor, 0.2),
                ColorUtils.lighten(themeColor, 0.15),
                ColorUtils.lighten(themeColor, 0.25),
                ColorUtils.lighten(themeColor, 0.4)
        };

        //int[] heights = { 20, 24, 18, 23, 16 };
        int[] heights = { 30, 36, 27, 35, 24 };
        recognitionProgressView = (RecognitionProgressView) getView().findViewById(R.id.important_recognition_view);
        recognitionProgressView.setColors(colors);
        recognitionProgressView.setBarMaxHeightsInDp(heights);
        recognitionProgressView.setCircleRadiusInDp(3);
        recognitionProgressView.setSpacingInDp(3);
        recognitionProgressView.setIdleStateAmplitudeInDp(2);
        recognitionProgressView.setRotationRadiusInDp(12);
        recognitionProgressView.play();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        //View navMainView = inflater.inflate(R.layout.nav_header_main,null);
        //setEdgeColor(todoList,themeColor);
        //int[] themeColors = {backgroundColor,themeColor};
        //Drawable drawHeadBG = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,themeColors);
        //drawHeadBG.setColorFilter(themeColor, PorterDuff.Mode.DST);
        //navHeadText.setTextSize(textSize);
        //navHeader.setBackgroundColor(Color.RED);
        fab.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        input.setTextColor(textColor);
        input.setTextSize(24);
        setCursorColor(input,themeColor);
        if(ColorUtils.determineBrightness(backgroundColor) < 0.5){// dark
            emptyTextView.setTextColor(Color.parseColor("#7FFFFFFF"));
        }else {//bright
            emptyTextView.setTextColor(Color.parseColor("#61000000"));
        }
        todoList.setBackgroundColor(backgroundColor);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 0:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(),getString(R.string.thanks_for_corporation),Toast.LENGTH_SHORT).show();
                    fab.performLongClick();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(getContext(),getString(R.string.voice_permission_request),Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
        }
    }

    public void displayAllNotes(){
        if(todoList.getAdapter() == null){
            ////System.out.println("null called");
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            todoList.setLayoutManager(linearLayoutManager);
            todoListAdapter = (new TodoListAdapter(null){
                @Override
                public void onBindViewHolder(TodoViewHolder holder, Cursor cursor) {
                    super.onBindViewHolder(holder, cursor);
                    final long id = cursor.getInt(cursor.getColumnIndex(DatabaseManager.ID));
                    String text = cursor.getString(cursor.getColumnIndex(DatabaseManager.TITLE));//get the text of the note
                    holder.todoText.setTextColor(textColor);
                    holder.cardView.setCardBackgroundColor(ColorUtils.darken(backgroundColor,0.01));
                    holder.todoText.setTextSize(textSize);
                    SpannableStringBuilder spannable = new SpannableStringBuilder(text);
                    //pin section
                    if(todosql.countPinnedNotesNumber() > 5){

                    }else {

                    }

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

                    //search section
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
                                spannable.setSpan(new BackgroundColorSpan(ColorUtils.makeTransparent(themeColor,0.2)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//set searched text to bold
                            }while (startPos > 0);
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
                                tagEndPos = text.length();
                            }else if(tagEndPos == tagStartPos + 1){//if only one #, skip to next loop
                                continue;
                            }
                            //System.out.println(tagStartPos + " AND " + tagEndPos);
                            String tag = text.toLowerCase().substring(tagStartPos, tagEndPos);//ignore case in tags//REMEMBER: SUBSTRING SECOND VARIABLE DOESN'T CONTAIN THE CHARACTER AT THAT POSITION
                            //System.out.println("TEXT: " + text + "****" + tag + "********");
                            String tagColor = todosql.returnTagColorIfExist(tag);
                            if(tagColor.equals("")){//if tag doesn't exist
                                Random random = new Random();//generate random color
                                int finalColor = random.nextInt(256*256*256);//set random limit to ffffff (HEX)
                                ArrayList<Integer> allTagColors = todosql.returnAllTagColors();
                                for(int i = 0; i < allTagColors.size(); i++){//eliminate too similar tag colors
                                    if(ColorUtils.determineSimilarColor(finalColor,allTagColors.get(i)) > 95){//compare new color to each color in tag database
                                        finalColor = random.nextInt(256*256*256);//generate a new color
                                        i = 0;//restart loop
                                    }
                                }
                                tagColor = String.format("#%06x", 0xFFFFFF & finalColor);// format it as hexadecimal string (with hashtag and leading zeros)
                                todosql.createNewTag(tag, tagColor);//add new tag
                            }
                            spannable.setSpan(new TextAppearanceSpan(null,Typeface.ITALIC,-1,
                                    new ColorStateList(new int[][] {new int[] {}},
                                            new int[] {Color.parseColor(tagColor)})
                                    ,null), tagStartPos, tagEndPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//highlight tag text
                            tagStartPos = text.indexOf("#",tagEndPos);//set tagStartPos to the new tag start point
                            //todo performance issue
                        }
                    }

                    holder.todoText.setText(spannable);
                    //System.out.println("null called");
                    if(isInSelectionMode){
                        holder.cBox.setVisibility(View.VISIBLE);
                        if(selectAll){
                            holder.cBox.setChecked(true);
                        }
                        if(unSelectAll){
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
            getLoaderManager().initLoader(123, null, this);
            ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            mItemTouchHelper.attachToRecyclerView(todoList);
        }
        getLoaderManager().restartLoader(123,null,this);
    }

    public void interruptAutoSend(){
        noInterruption = false;
        fabProgressBar.setVisibility(View.INVISIBLE);
        //stop circle
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
        Snackbar.make(getView(), String.valueOf(CLONESelectedContent.size()) + " "  + getString(R.string.notes_finished_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
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
        Snackbar.make(getView(), String.valueOf(CLONESelectedContent.size()) + " " + getString(R.string.notes_deleted_snack_text), Snackbar.LENGTH_LONG).setActionTextColor(themeColor).setAction(getString(R.string.snack_undo_text), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String content : CLONESelectedContent){
                    insertData(content);
                }
                displayAllNotes();
            }
        }).show();
    }

    public boolean exportOrPrint(){//print list or export as pdf
        //todo print list or export as pdf
        Toast.makeText(getContext(),getString(R.string.exporting),Toast.LENGTH_SHORT).show();
        String exportBody = null;
        StringBuilder exportBodyBuilder = new StringBuilder();
        exportBody = getString(R.string.note_export_content_header);
        for(String data : selectedContent){
            exportBodyBuilder.append(data);
            exportBodyBuilder.append("\n\n");//empty line after each note
        }
        exportBody = exportBodyBuilder.toString();//this is the final string for export
        PrintManager printManager = (PrintManager)getActivity().getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Document";
        try{
            printManager.print(jobName, new ImportantFragment.ExportPrintAdapter(getContext()),null);//print with print adapter
        }catch (Exception e){
            Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        //PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(new Rect(0, 0, 100, 100), 1).create();
        //PdfDocument.Page page = document.startPage(pageInfo);
        //document.writeTo();
        return true;
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

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }else {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            try{
                imm.hideSoftInputFromWindow(input.getWindowToken(),0);
            }catch (NullPointerException ignored){}
        }
    }

    public void showKeyboard() {
        View view = getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imManager.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }else{
            try{
                InputMethodManager imManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imManager.showSoftInput(input,InputMethodManager.SHOW_IMPLICIT);
            }catch (NullPointerException ignored){}
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.todo_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        EditText searchViewTextField = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        MainActivity.setCursorColor(searchViewTextField,Color.WHITE);
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
                proFab.setVisibility(View.GONE);
                input.setVisibility(View.GONE);
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
                proFab.setVisibility(View.GONE);
                input.setVisibility(View.GONE);
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
        //return true;
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void query(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("QUERY", query);
        searchText = query;
        //System.out.println("calledquery" + " " + text);
        getLoaderManager().restartLoader(123, bundle, ImportantFragment.this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        setColorPreferences();
        //determine order of the list
        String sort = IMPORTANCE + " DESC, " + REMIND_TIME + " ASC";
        String selectionAddOn = "";
        String[] selectionArgs = null;
        String[] selectionArgsCombined = null;
        if(sharedPreferences.getBoolean(getString(R.string.order_key),true)){
            sort = sort + ", _id DESC";
        }
        String currentTimeStr = MainActivity.getCurrentTimeString();
        Calendar recentTime = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        selectionAddOn = " OR " + "(DATETIME)IN json_extract(" + REMIND_TIME + ") BETWEEN ? AND ?";
        if(todosql.countRecentReminder() > 0){//has recent reminder
            recentTime.add(Calendar.WEEK_OF_YEAR,1);
            String recentTimeStr = dateFormat.format(recentTime.getTime());
            selectionArgs = new String[]{currentTimeStr,recentTimeStr};
        }else {
            if(todosql.countPinnedNotesNumber() <= 0){//if there are no pinned notes, add other notes that contains reminders to important fragment
                //selectionAddOn = " OR " + REMIND_TIME + " IS NOT NULL)";
                recentTime.add(Calendar.WEEK_OF_YEAR,3);
                String recentTimeStr = dateFormat.format(recentTime.getTime());
                selectionArgs = new String[]{currentTimeStr,recentTimeStr};
                //sort = sort + " LIMIT 5";
            }
            //" UNION SELECT * FROM " + TODO_TABLE + " WHERE " + REMIND_TIME + " IS NOT NULL ORDER BY " + REMIND_TIME + " ASC LIMIT 5";//add other notes with reminder in important if nothing includes recent reminders is present
        }
        if (args != null) {
            selectionAddOn += ")";//complete the parentheses in search mode
            String[] selectionArgsQuery = new String[]{"%" + args.getString("QUERY") + "%"};
            selectionArgsCombined = MainActivity.combineStringArray(selectionArgsQuery,selectionArgs);
            return new CursorLoader(getContext(), DatabaseContract.Item.TODO_URI, PROJECTION, SELECTION_WITH_QUERY + selectionAddOn, selectionArgsCombined, sort);
        }else {
            return new CursorLoader(getContext(), DatabaseContract.Item.TODO_URI, PROJECTION, SELECTION + selectionAddOn, selectionArgs , sort);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ////System.out.println("dataCount" + data.getCount() + " " + isInSearchMode);
        if(data.getCount() == 0 && isInSearchMode){
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(getString(R.string.empty_search_result));
        }else if(data.getCount() == 0 && !isInSearchMode){
            System.out.println("EMPTY_IMPORTANT");
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.empty_important_todolist);
        }else {
            emptyTextView.setVisibility(View.GONE);
            emptyTextView.setText("");
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
            Uri uri = ContentUris.withAppendedId(DatabaseContract.Item.TODO_URI, id);
            return getActivity().getContentResolver().update(uri, values, null, null);
        }else return -1;
    }

    public Uri insertData(String title) {
        if (!title.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(TITLE, title);
            return getActivity().getContentResolver().insert(DatabaseContract.Item.TODO_URI, values);
        } else return null;
    }

    public void finishData(long id){
        ContentValues cv = new ContentValues();
        String data = todosql.getOneDataInTODO(id);
        cv.put(TITLE,data);
        //System.out.println("finish data" + id);
        deleteData(id);
        getActivity().getContentResolver().insert(DatabaseContract.Item.HISTORY_URI, cv);
    }

    public void deleteData(long id){
        Uri uri = ContentUris.withAppendedId(DatabaseContract.Item.TODO_URI, id);
        //System.out.println("delete data" + id);
        String note = todosql.getOneDataInTODO(id);
        getActivity().getContentResolver().delete(uri, null, null);
        ArrayList<String> tags = determineContainedTags(note);
        if(!(tags == null)){//if contains tags
            for(String tag : tags){
                if(!todosql.determineIfTagInUse(tag)){//if the deleted note is the last one containing the tag, delete the tag from tag database
                    Uri tagUri = ContentUris.withAppendedId(DatabaseContract.Item.TAGS_URI,todosql.returnTagID(tag));
                    getActivity().getContentResolver().delete(tagUri,null,null);
                }
            }
        }
        displayAllNotes();
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
    public void onResume() {
        if(!(input.getText().toString().equals("")) && input.getVisibility() == View.VISIBLE) showKeyboard();
        displayAllNotes();
        setColorPreferences();
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
            Toast.makeText(getContext(),printItemCount,Toast.LENGTH_LONG).show();
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
}
