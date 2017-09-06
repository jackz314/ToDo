package com.jackz314.todo;


import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.IntDef;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.provider.DocumentFile;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DrawableUtils;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.luminance;
import static com.jackz314.todo.R.color.colorPrimary;
import static com.jackz314.todo.dtb.DATABASE_NAME;
import static com.jackz314.todo.dtb.TODO_TABLE;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    static dtb dtb;
    ColorUtils colorUtils;
    static ColorPickerDialog colorPickerDialog;
    static AlertDialog backupDialog, restoreDialog;
    public static int themeColorNum;
    public static int textColorNum;
    public static int backgroundColorNum;
    public static final int BACKUP_PERMISSION_REQUEST = 1, RESTORE_PERMISSION_REQUEST = 2;
    public static final int PATH_SELECTOR_REQUEST_CODE = 3, FILE_SELECTOR_REQUEST_CODE = 4;
    private FirebaseAnalytics mFirebaseAnalytics;
    public SharedPreferences sharedPreferences;
    public boolean storageBackupPerDenied = false, storageRestorePerDenied = false;
    public String pathSelectorPath = "", fileSelected = "";
    SwitchPreference autoClearSwitch,orderSwitch,mainHistorySwitch;
    ListPreference themeSelector;
    Preference wipeButton,themeColor,textColor,backgroundColor,resetAppearanceData,textSize,backupData,restoreData,chooseClrFrequency;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences,     look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    //preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public void setColorPreferencesSettings(){
        sharedPreferences = getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColorNum=sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(colorPrimary));
        textColorNum=sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColorNum=sharedPreferences.getInt(getString(R.string.background_color_key),Color.parseColor("#fafafa"));
        themeSelector = (ListPreference)findPreference(getString(R.string.theme_selector_key));
        mainHistorySwitch = (SwitchPreference)findPreference(getString(R.string.main_history_switch));
        chooseClrFrequency = findPreference(getString(R.string.clear_frequency_choose_key));
        autoClearSwitch = (SwitchPreference)findPreference(getString(R.string.clear_history_switch_key));
        wipeButton = findPreference(getString(R.string.wipe_history_key));
        themeColor = findPreference(getString(R.string.theme_color_key));
        textColor = findPreference(getString(R.string.text_color_key));
        backgroundColor = findPreference(getString(R.string.background_color_key));
        resetAppearanceData = findPreference(getString(R.string.reset_appearance_key));
        orderSwitch = (SwitchPreference)findPreference(getString(R.string.order_key));
        textSize = findPreference(getString(R.string.text_size_key));
        backupData = findPreference(getString(R.string.backup_data_key));
        restoreData = findPreference(getString(R.string.restore_data_key));
        chooseClrFrequency.setSummary(sharedPreferences.getString(getString(R.string.clear_interval_summary_key),getString(R.string.disabled)));
        themeSelector.setSummary(sharedPreferences.getString(getString(R.string.theme_selector_summary_key),getString(R.string.custom)));
        setColorForPref(mainHistorySwitch);
        setColorForPref(chooseClrFrequency);
        setColorForPref(autoClearSwitch);
        setColorForPref(wipeButton);
        setColorForPref(themeSelector);
        setColorForPref(backupData);
        setColorForPref(restoreData);
        setColorForPref(themeColor);
        setColorForPref(textColor);
        setColorForPref(backgroundColor);
        setColorForPref(resetAppearanceData);
        setColorForPref(orderSwitch);
        setColorForPref(textSize);
        Drawable themeColorD = getDrawable(R.drawable.ic_format_color_fill_black_24dp);
        themeColorD.setColorFilter(themeColorNum, PorterDuff.Mode.SRC);
        Drawable textColorD = getDrawable(R.drawable.ic_text_format_black_24dp);
        textColorD.setColorFilter(textColorNum, PorterDuff.Mode.SRC);
        Drawable backgroundColorD = getDrawable(R.drawable.ic_aspect_ratio_black_24dp);
        backgroundColorD.setColorFilter(backgroundColorNum, PorterDuff.Mode.SRC);
        themeColor.setIcon(themeColorD);
        textColor.setIcon(textColorD);
        backgroundColor.setIcon(backgroundColorD);
        orderSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.order_key),true));
        getListView().setBackgroundColor(Color.TRANSPARENT);
        getListView().setBackgroundColor(backgroundColorNum);
        Window window = this.getWindow();
        window.setStatusBarColor(themeColorNum);
        window.setNavigationBarColor(themeColorNum);
        setupActionBar();
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColorNum = sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorPrimary));
        textColorNum = sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColorNum = sharedPreferences.getInt(getString(R.string.background_color_key),Color.parseColor("#fafafa"));
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Window window = this.getWindow();
        window.setStatusBarColor(themeColorNum);
        window.setNavigationBarColor(themeColorNum);
        setColorPreferencesSettings();
        final LayoutInflater inf = LayoutInflater.from(this);
        setupActionBar();
        /********************/
        dtb = new dtb(getApplicationContext());
        if(!sharedPreferences.getBoolean(getString(R.string.clear_history_switch_key),false)){
            chooseClrFrequency.setEnabled(false);

        }else{
           chooseClrFrequency.setEnabled(true);
        }
        if(!sharedPreferences.getBoolean(getString(R.string.main_history_switch),true)){
            autoClearSwitch.setEnabled(false);
            chooseClrFrequency.setEnabled(false);
            wipeButton.setEnabled(false);
        }else{
            //chooseClrFrequency.setEnabled(true);
            wipeButton.setEnabled(true);
            autoClearSwitch.setEnabled(true);
        }
        themeSelector = (ListPreference)findPreference(getString(R.string.theme_selector_key));
        setColorForPref(themeSelector);
        themeSelector.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String themeSummary;
                int pos = Arrays.asList(themeSelector.getEntryValues()).indexOf(newValue);
                themeSummary = themeSelector.getEntries()[pos].toString();
                editor.putString(getString(R.string.theme_selector_summary_key),themeSummary);
                editor.putInt(getString(R.string.theme_selector_value_key), Color.parseColor(newValue.toString()));
                editor.putInt(getString(R.string.theme_color_key), Color.parseColor(newValue.toString()));
                if(themeSummary.contains("(")){// dark theme
                    editor.putInt(getString(R.string.text_color_key),Color.parseColor("#eeeeee"));
                    editor.putInt(getString(R.string.background_color_key),Color.parseColor("#616161"));
                    textColorNum = Color.parseColor("#eeeeee");
                    backgroundColorNum = Color.parseColor("#616161");
                }else {// bright theme
                    editor.putInt(getString(R.string.text_color_key),Color.parseColor("#212121"));
                    editor.putInt(getString(R.string.background_color_key),Color.parseColor("#fafafa"));
                    textColorNum = Color.parseColor("#212121");
                    backgroundColorNum = Color.parseColor("#fafafa");
                }
                themeColorNum = Color.parseColor(newValue.toString());
                editor.commit();
                String themeSelectorSummary = sharedPreferences.getString(getString(R.string.theme_selector_summary_key),getString(R.string.custom));
                themeSelector.setSummary(themeSelectorSummary);
                setColorPreferencesSettings();
                setColorForPref(themeSelector);
                return false;
            }
        });

        mainHistorySwitch = (SwitchPreference)findPreference(getString(R.string.main_history_switch));
        setColorForPref(mainHistorySwitch);
        mainHistorySwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.main_history_switch),(boolean)newValue);
                editor.commit();
                mainHistorySwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.main_history_switch),true));
                if(!sharedPreferences.getBoolean(getString(R.string.main_history_switch),true)){
                    autoClearSwitch.setEnabled(false);
                    chooseClrFrequency.setEnabled(false);
                    wipeButton.setEnabled(false);
                }else{
                    chooseClrFrequency.setEnabled(true);
                    wipeButton.setEnabled(true);
                    autoClearSwitch.setEnabled(true);
                }
                setColorPreferencesSettings();
                return false;
            }
        });
        chooseClrFrequency = findPreference(getString(R.string.clear_frequency_choose_key));
        setColorForPref(chooseClrFrequency);
        chooseClrFrequency.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                LayoutInflater inflater = LayoutInflater.from(SettingsActivity.this);
                final View dialogView = inflater.inflate(R.layout.time_interval_choose_dialog, null);
                final Spinner unitChooser = (Spinner) dialogView.findViewById(R.id.unit_chooser);
                final NumberPicker intervalChooser = (NumberPicker)dialogView.findViewById(R.id.interval_num_picker);
                intervalChooser.setValue(sharedPreferences.getInt(getString(R.string.clear_interval_num_key),1));
                unitChooser.setSelection(sharedPreferences.getInt(getString(R.string.clear_interval_unit_num_key),2),true);
                final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this).setView(dialogView)
                        .setTitle(getString(R.string.clr_interval_dialog_title))
                        .setMessage(getString(R.string.clr_interval_dialog_Message))
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int intervalValue = 60;
                                if(unitChooser.getSelectedItemPosition()==0){//min
                                    intervalValue = intervalChooser.getValue();
                                }else if(unitChooser.getSelectedItemPosition()==1){//hour
                                    intervalValue = intervalChooser.getValue()*60;
                                }else if(unitChooser.getSelectedItemPosition()==2){//day
                                    intervalValue = intervalChooser.getValue()*60*24;
                                }else if(unitChooser.getSelectedItemPosition()==3){//week
                                    intervalValue = intervalChooser.getValue()*60*24*7;
                                }else if(unitChooser.getSelectedItemPosition()==4){//month
                                    intervalValue = intervalChooser.getValue()*60*24*30;
                                }else if(unitChooser.getSelectedItemPosition()==5){//year
                                    intervalValue = intervalChooser.getValue()*60*24*365;
                                }
                                editor.putInt(getString(R.string.clear_interval_value_key),intervalValue);
                                editor.putInt(getString(R.string.clear_interval_num_key),intervalChooser.getValue());
                                editor.putString(getString(R.string.clear_interval_unit_key),unitChooser.getSelectedItem().toString());
                                editor.putInt(getString(R.string.clear_interval_unit_num_key),unitChooser.getSelectedItemPosition());
                                String summary = "";
                                summary = String.valueOf(intervalChooser.getValue()) + " " + unitChooser.getSelectedItem().toString();
                                editor.putString(getString(R.string.clear_interval_summary_key),summary);
                                editor.commit();
                                chooseClrFrequency.setSummary(sharedPreferences.getString(getString(R.string.clear_interval_summary_key),getString(R.string.disabled)));
                                setColorForPref(chooseClrFrequency);
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setCancelable(true).show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                intervalChooser.setMinValue(1);
                intervalChooser.setMaxValue(500);
                intervalChooser.setValue(sharedPreferences.getInt(getString(R.string.clear_interval_num_key),1));
                intervalChooser.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        if(newVal != oldVal && newVal != sharedPreferences.getInt(getString(R.string.clear_interval_num_key),1)){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                        intervalChooser.setValue(newVal);
                        intervalChooser.setSelected(true);
                    }
                });
                unitChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(position != sharedPreferences.getInt(getString(R.string.clear_interval_unit_num_key),2)){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                        if(position == 0){//min

                        }else if(position == 1){//hour

                        }else if(position == 2){//day

                        }else if(position == 3){//week
                            if(intervalChooser.getValue()>100){
                                intervalChooser.setValue(100);
                            }
                            intervalChooser.setMaxValue(100);
                        }else if(position == 4){//month
                            if(intervalChooser.getValue()>50){
                                intervalChooser.setValue(50);
                            }
                            intervalChooser.setMaxValue(50);
                        }else if(position == 5){//year
                            if(intervalChooser.getValue()>10){
                                intervalChooser.setValue(10);
                            }
                            intervalChooser.setMaxValue(10);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        return;
                    }
                });
                return false;
            }
        });
        autoClearSwitch = (SwitchPreference)findPreference(getString(R.string.clear_history_switch_key));
        setColorForPref(autoClearSwitch);
        autoClearSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.clear_history_switch_key),(boolean)newValue);
                editor.commit();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "auto_clear_history");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Auto clear status:"+sharedPreferences.getBoolean(getString(R.string.clear_history_switch_key),false));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "switch");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                autoClearSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.clear_history_switch_key),false));
                if(!sharedPreferences.getBoolean(getString(R.string.clear_history_switch_key),false)){
                    chooseClrFrequency.setEnabled(false);
                }else{
                    chooseClrFrequency.setEnabled(true);
                }
                setColorPreferencesSettings();
                return false;
            }
        });
        textSize = findPreference(getString(R.string.text_size_key));
        setColorForPref(textSize);
        textSize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                LayoutInflater inflater = LayoutInflater.from(SettingsActivity.this);
                final View dialogView = inflater.inflate(R.layout.editnum_dialog, null);
                //final EditText edt = (EditText) dialogView.findViewById(R.id.num1);
                final NumberPicker numberPicker = (NumberPicker)dialogView.findViewById(R.id.numberPicker);
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(60);
                numberPicker.setValue(sharedPreferences.getInt("text_size_key",24));

                //edt.setText(String.valueOf(sharedPreferences.getInt("text_size_key",24)));
                //edt.selectAll();
                final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getString(R.string.txt_size_title))
                        .setMessage(getString(R.string.txt_size_message))
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if((numberPicker.getValue() > 0)){
                                    final int textSize = numberPicker.getValue();
                                    if(numberPicker.getValue()>=50||numberPicker.getValue()<=10){
                                        new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)//alert title
                                                .setMessage(R.string.text_size_not_suitable)//alert content
                                                .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {//YES
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {//YES
                                                        editor.putInt(getString(R.string.text_size_key),numberPicker.getValue());
                                                        editor.commit();
                                                        setColorPreferencesSettings();
                                                    }
                                                }).setNegativeButton(R.string.reconsider, new DialogInterface.OnClickListener() {//NO
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {//NO
                                                Log.i("information","canceled");
                                            }
                                        }).show();
                                    }else {
                                        editor.putInt(getString(R.string.text_size_key),numberPicker.getValue());
                                        editor.commit();
                                    }
                                }else {
                                    return;
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;//cancel
                            }
                        }).setCancelable(true).setView(dialogView).show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        if(newVal != oldVal && newVal != sharedPreferences.getInt("text_size_key",24)){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }else {
                            //dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        }
                        numberPicker.setSelected(true);
                        numberPicker.setValue(newVal);
                    }
                });
                /*edt.addTextChangedListener(new TextWatcher() {
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
                });*/
                dialog.show();
                //dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                return false;
            }
        });
        wipeButton = findPreference(getString(R.string.wipe_history_key));
        setColorForPref(wipeButton);
        wipeButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)//alert title
                        .setMessage(R.string.warning_content)//alert content
                        .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {//YES
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//YES
                                dtb.wipeHistory();
                            }
                        }).setNegativeButton(R.string.reconsider, new DialogInterface.OnClickListener() {//NO
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//NO
                        Log.i("information","canceled");
                    }
                }).show();
                return false;
            }
        });
        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        //bindPreferenceSummaryToValue((ListPreference)findPreference(getString(R.string.theme_selector_key)));
        themeColor = findPreference(getString(R.string.theme_color_key));
        setColorForPref(themeColor);
        themeColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {// change theme color
            @Override
            public boolean onPreferenceClick(Preference preference) {
                colorPickerDialog = new ColorPickerDialog(SettingsActivity.this, themeColorNum, getString(R.string.color_picker_dialog_title), new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(final int color) {// set color
                        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(getString(R.string.theme_color_key),color);
                        editor.commit();
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "themeColor");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Theme color:"+Integer.toString(color));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Color int");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                        themeColorNum = color;
                        //Toast.makeText(getApplicationContext(),String.valueOf(color),Toast.LENGTH_SHORT).show();
                        setColorPreferencesSettings();
                    }
                });
                colorPickerDialog.setTitle(getString(R.string.theme_color_selector));
                colorPickerDialog.show();
                return false;
            }
        });
        textColor = findPreference(getString(R.string.text_color_key));
        setColorForPref(textColor);
        textColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {// change text color
            @Override
            public boolean onPreferenceClick(Preference preference) {
                colorPickerDialog = new ColorPickerDialog(SettingsActivity.this, textColorNum, getString(R.string.color_picker_dialog_title), new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(final int color) {
                        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                        if(sharedPreferences.getInt(getString(R.string.background_color_key),backgroundColorNum) == color){
                            new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)
                                    .setMessage(R.string.color_conflict)
                                    .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt(getString(R.string.text_color_key),color);
                                            editor.commit();
                                            Bundle bundle = new Bundle();
                                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "textColor");
                                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Text color:"+Integer.toString(color));
                                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Color int");
                                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                            textColorNum = color;
                                            setColorPreferencesSettings();
                                        }
                                    }).setNegativeButton(R.string.reconsider, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            }).show();
                        }else{
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(getString(R.string.text_color_key),color);
                            editor.commit();
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "textColor");
                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Text color:"+Integer.toString(color));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Color int");
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                            textColorNum = color;
                            setColorPreferencesSettings();
                        }
                    }
                });
                colorPickerDialog.setTitle(getString(R.string.text_color_selector));
                colorPickerDialog.show();
                return false;
            }
        });
        backgroundColor = findPreference(getString(R.string.background_color_key));
        setColorForPref(backgroundColor);
        backgroundColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                colorPickerDialog = new ColorPickerDialog(SettingsActivity.this, backgroundColorNum, getString(R.string.color_picker_dialog_title), new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(final int color) {
                        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                        if(sharedPreferences.getInt(getString(R.string.text_color_key),textColorNum) ==  color){
                            new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)
                                    .setMessage(R.string.color_conflict)
                                    .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt(getString(R.string.background_color_key),color);
                                            editor.commit();
                                            Bundle bundle = new Bundle();
                                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "backgroundColor");
                                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Background color:"+Integer.toString(color));
                                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Color int");
                                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                            backgroundColorNum = color;
                                            setColorPreferencesSettings();
                                        }
                                    }).setNegativeButton(R.string.reconsider, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            }).show();
                        }else{
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(getString(R.string.background_color_key),color);
                            editor.commit();
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "backgroundColor");
                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Background color:"+Integer.toString(color));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Color int");
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                            backgroundColorNum = color;
                            setColorPreferencesSettings();
                        }
                    }
                });
                colorPickerDialog.setTitle(getString(R.string.background_color_selector));
                colorPickerDialog.show();
                return false;
            }
        });
        resetAppearanceData = findPreference(getString(R.string.reset_appearance_key));
        setColorForPref(resetAppearanceData);
        resetAppearanceData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)//alert title
                        .setMessage(R.string.warning_content)//alert content
                        .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {//YES
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//YES
                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(getString(R.string.background_color_key),Color.parseColor("#fafafa"));
                                editor.putInt(getString(R.string.text_color_key),Color.BLACK);
                                editor.putInt(getString(R.string.theme_color_key),getResources().getColor(colorPrimary));
                                editor.putBoolean(getString(R.string.order_key),true);
                                editor.putInt(getString(R.string.text_size_key),24);
                                editor.apply();
                                setColorPreferencesSettings();
                            }
                        }).setNegativeButton(R.string.reconsider, new DialogInterface.OnClickListener() {//NO
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//NO
                        Log.i("information","canceled");
                    }
                }).show();

                return false;
            }
        });
        orderSwitch = (SwitchPreference)findPreference(getString(R.string.order_key));
        setColorForPref(orderSwitch);
        orderSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.order_key),(boolean)newValue);
                editor.commit();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "order_of_items");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Order(true == reversely, false == normally):"+sharedPreferences.getBoolean(getString(R.string.order_key),false));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "switch");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                orderSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.order_key),false));
                return false;
            }
        });
        backupData = findPreference(getString(R.string.backup_data_key));
        setColorForPref(backupData);
        backupData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LayoutInflater inflater = LayoutInflater.from(SettingsActivity.this);
                View dialogView = inflater.inflate(R.layout.backup_dialog,null);
                final EditText edt = (EditText)dialogView.findViewById(R.id.tagText);
                Button editPath =(Button)dialogView.findViewById(R.id.path_selector);
                editPath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        //i.putExtra(Intent.EXTRA_INITIAL_INTENTS,"/To/Do/backup");
                        startActivityForResult(i,PATH_SELECTOR_REQUEST_CODE);
                    }
                });
                final SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmssSSS");
                final File storageRoot = Environment.getExternalStorageDirectory();
                final String backupPathStr = Environment.getExternalStorageDirectory().getPath();
                pathSelectorPath = backupPathStr + "/ToDo/backup";
                backupDialog = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getString(R.string.backup_dialog_title))
                        .setMessage(getString(R.string.backup_dialog_content1) + " " + backupPathStr + getString(R.string.backup_dialog_content2))
                        .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String timeStampName = timeStamp.format(Calendar.getInstance().getTime());
                                File databasePath = getDatabasePath(DATABASE_NAME);
                                String backupFileName = "";
                                if(edt.getText().toString().trim().isEmpty()){
                                    backupFileName = "ToDo_BACKUP_ " + edt.getText().toString().trim() + "_" + timeStampName + ".db";
                                }else {
                                    backupFileName = "ToDo_BACKUP_" + timeStampName + ".db";
                                }
                                String destinationPathStr = pathSelectorPath + "/" + backupFileName;
                                File destinationPath = new File(destinationPathStr);
                                File dstDir = new File(pathSelectorPath);
                                if(pathSelectorPath.equals(backupPathStr + "/ToDo/backup") && !dstDir.exists() && !dstDir.isDirectory()){
                                    dstDir.mkdirs();
                                }
                                try{
                                    FileChannel src = new FileInputStream(databasePath).getChannel();
                                    FileChannel dst = new FileOutputStream(destinationPath).getChannel();
                                    dst.transferFrom(src,0,src.size());
                                    src.close();
                                    dst.close();
                                    Toast.makeText(getApplicationContext(),getString(R.string.backup_finished),Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(),getString(R.string.backup_error) + "\n" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                backupDialog.dismiss();
                            }
                        }).setCancelable(true).setView(dialogView).show();
                if(storageRoot.canWrite()){

                }else{
                    backupDialog.dismiss();
                    if(storageBackupPerDenied){
                        Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_refused_forever),Toast.LENGTH_LONG).show();
                    }else {
                        int permissionCheck = ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
                            if(ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.storage_permission_refused_explanation_bk), Toast.LENGTH_LONG).show();
                                ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},BACKUP_PERMISSION_REQUEST);
                            }else{
                                Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_hint_bk),Toast.LENGTH_LONG).show();
                                ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},BACKUP_PERMISSION_REQUEST);
                            }
                        }
                    }
                    //Toast.makeText(getApplicationContext(),getString(R.string.backup_no_write_permission),Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        restoreData = findPreference(getString(R.string.restore_data_key));
        setColorForPref(restoreData);
        restoreData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final File storageRoot = Environment.getExternalStorageDirectory();
                LayoutInflater inflater = LayoutInflater.from(SettingsActivity.this);
                View dialogView = inflater.inflate(R.layout.restore_dialog,null);
                final CheckBox replaceCheck = (CheckBox)dialogView.findViewById(R.id.replace_current_checkbox);
                Button selectBackup = (Button)dialogView.findViewById(R.id.select_backup_file_btn);
                selectBackup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/octet-stream");
                        //intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent,FILE_SELECTOR_REQUEST_CODE);
                    }
                });
                restoreDialog = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getString(R.string.restore_dialog_title))
                        .setMessage(getString(R.string.restore_dialog_content_before))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.restore_data_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(replaceCheck.isChecked()){
                                    restoreDataFromBackupReplace(fileSelected);
                                }else {
                                    restoreDataFromBackup(fileSelected);
                                }
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                restoreDialog.dismiss();
                            }
                        }).setView(dialogView).show();
                restoreDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                if(storageRoot.canRead() && storageRoot.canWrite()){//permission all good to go

                }else{
                    restoreDialog.dismiss();
                    if(storageRestorePerDenied){
                        Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_refused_forever),Toast.LENGTH_LONG).show();
                    }else {
                        if(ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                            Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_refused_explanation_rs),Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_hint_rs),Toast.LENGTH_LONG).show();
                        }
                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},RESTORE_PERMISSION_REQUEST);
                    }
                }
                return false;
            }
        });
    }

    public boolean restoreDataFromBackup(String filePath){
        if(dtb.validateBackup(filePath)){
            String result =dtb.mergeBackup(filePath) ;
            if(result == null){
                Toast.makeText(getApplicationContext(),getString(R.string.restore_finished),Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(),getString(R.string.restore_error) + "\n" + result,Toast.LENGTH_LONG).show();
                System.out.println("Error restoring: " + result);
            }
        }else {
            Toast.makeText(getApplicationContext(),getString(R.string.not_validate_backup),Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
    }

    public boolean restoreDataFromBackupReplace(String filePath){
        File backupFilePath = new File(filePath);
        File destinationPath = getDatabasePath(DATABASE_NAME);
        if(dtb.validateBackup(filePath)){
            try{
                FileChannel src = new FileInputStream(backupFilePath).getChannel();
                FileChannel dst = new FileOutputStream(destinationPath).getChannel();
                dst.transferFrom(src,0,src.size());
                src.close();
                dst.close();
                Toast.makeText(getApplicationContext(),getString(R.string.restore_finished),Toast.LENGTH_SHORT).show();
                return true;
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),getString(R.string.restore_error) + "\n" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),getString(R.string.not_validate_backup),Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if (resultCode != RESULT_OK) return;
        if(data == null){
            return;
        }
            if (requestCode == PATH_SELECTOR_REQUEST_CODE) {
                Uri uri = data.getData();
                //DocumentFile pathSelected = DocumentFile.fromTreeUri(this,uri);
                Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
                String path = uri.getPath();
                int posOfCol = 0;
                for(int i = 0; i<path.length();i++){
                    if (path.substring(i,i+1).equals(":")){
                        posOfCol = i;
                    }
                }
                if(path.contains("primary")){// main sd card
                    pathSelectorPath = Environment.getExternalStorageDirectory().getPath() + "/" + path.substring(posOfCol+1);
                }else {//others
                    //Toast.makeText(getApplicationContext(),getString(R.string.selected_wrong_path),Toast.LENGTH_LONG).show();
                    //pathSelectorPath = Environment.getExternalStorageDirectory().getPath() + "/To/Do/backup";
                    File[] extRoot = getExternalFilesDirs(null);
                    pathSelectorPath = extRoot[1].getPath() + "/" + path.substring(posOfCol+1);
                }
                backupDialog.setMessage(getString(R.string.backup_dialog_content1) + "\n" + pathSelectorPath);
                //Toast.makeText(getApplicationContext(),pathSelectorPath,Toast.LENGTH_LONG).show();
            }else if (requestCode == FILE_SELECTOR_REQUEST_CODE) {
                Uri uri = data.getData();
                String processFile = uri.getPath();
                if(!processFile.endsWith(".db")){
                    Toast.makeText(getApplicationContext(),getString(R.string.not_validate_backup),Toast.LENGTH_LONG).show();
                }else {
                    int posOfCol = 0;
                    for(int i = 0; i<processFile.length();i++){
                        if (processFile.substring(i,i+1).equals(":")){
                            posOfCol = i;
                        }
                    }
                    fileSelected = Environment.getExternalStorageDirectory().getPath() + "/" + processFile.substring(posOfCol+1);
                    File file = new File(fileSelected);
                    Date lastModDate = new Date(file.lastModified());
                    String fileModDateStr = lastModDate.toString();
                    if(restoreDialog != null){
                        restoreDialog.setMessage(getString(R.string.restore_dialog_content1) + " " + fileSelected + "\n" + getString(R.string.restore_dialog_content2) + " " + fileModDateStr);
                        restoreDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }
    }

    /*public String getRealPathFromURI(Uri contentUri) {
        String [] proj      = {MediaStore.Images.Media.DATA};
        Cursor cursor       = getContentResolver().query( contentUri, proj, null, null,null);
        if (cursor == null) return null;
        int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults){
             if(BACKUP_PERMISSION_REQUEST == requestCode)  {
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){//granted
                    backupDialog.show();
                    Toast.makeText(getApplicationContext(),getString(R.string.thanks_for_corporation),Toast.LENGTH_SHORT).show();
                }else {//denied
                    if(ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_refused_explanation_bk),Toast.LENGTH_LONG).show();
                    }else{
                        storageBackupPerDenied = true;
                        Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_refused_forever),Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }else if (RESTORE_PERMISSION_REQUEST == requestCode) {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    restoreDialog.show();
                    Toast.makeText(getApplicationContext(),getString(R.string.thanks_for_corporation),Toast.LENGTH_SHORT).show();
                }else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                        Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_refused_explanation_rs),Toast.LENGTH_LONG).show();
                    }else {
                        storageRestorePerDenied = true;
                        Toast.makeText(getApplicationContext(),getString(R.string.storage_permission_refused_forever),Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        Drawable actionBarColor = new ColorDrawable(themeColorNum);
        actionBarColor.setColorFilter(themeColorNum, PorterDuff.Mode.DST);
        actionBar.setBackgroundDrawable(actionBarColor);
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */

    public void setColorForPref(Preference pref){
        String title = pref.getTitle().toString();
        String summary = pref.getSummary().toString();
        Spannable coloredTitle = new SpannableString (title);
        Spannable coloredSummary = new SpannableString (summary);
        if(pref.isEnabled()){
            coloredTitle.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredTitle.length(), 0 );
            coloredSummary.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredSummary.length(), 0 );
        }else {
            coloredTitle.setSpan( new ForegroundColorSpan(colorUtils.lighten(textColorNum,0.5)), 0, coloredTitle.length(), 0 );
            coloredSummary.setSpan( new ForegroundColorSpan(colorUtils.lighten(textColorNum,0.5)), 0, coloredSummary.length(), 0 );
        }
        pref.setTitle(coloredTitle);
        pref.setSummary(coloredSummary);
    }
    public void setColorForPref(EditTextPreference pref){
        String title = pref.getTitle().toString();
        String summary = pref.getSummary().toString();
        Spannable coloredTitle = new SpannableString (title);
        Spannable coloredSummary = new SpannableString (summary);
        coloredTitle.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredTitle.length(), 0 );
        coloredSummary.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredSummary.length(), 0 );
        pref.setTitle(coloredTitle);
        pref.setSummary(coloredSummary);
    }
    public void setColorForPref(SwitchPreference pref){
        String title = pref.getTitle().toString();
        Spannable coloredTitle = new SpannableString (title);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, //disabled
                        new int[]{android.R.attr.state_checked}, //enabled
                },
                new int[] {
                        Color.parseColor("#fafafa")//disabled
                        ,ColorUtils.lighten(themeColorNum,0.32) //enabled
                }
        );
        //// TODO: 2017/8/29 CHANGE COLOR OF SWITCHPREFERENCE DYNAMICALLY
        if(pref.isEnabled()){
            coloredTitle.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredTitle.length(), 0 );
            if(pref.getSummary()!= null){
                String summary = pref.getSummary().toString();
                Spannable coloredSummary = new SpannableString (summary);
                coloredSummary.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredSummary.length(), 0 );
                pref.setSummary(coloredSummary);
            }
        }else {
            coloredTitle.setSpan( new ForegroundColorSpan(colorUtils.lighten(textColorNum,0.5)), 0, coloredTitle.length(), 0 );
            if(pref.getSummary()!= null){
                String summary = pref.getSummary().toString();
                Spannable coloredSummary = new SpannableString (summary);
                coloredSummary.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredSummary.length(), 0 );
                pref.setSummary(coloredSummary);
            }
        }
        pref.setTitle(coloredTitle);

    }
    public void setColorForPref(ListPreference pref){
        String title = pref.getTitle().toString();

        ArrayList<CharSequence> entries = new ArrayList<>(Arrays.asList(pref.getEntries()));
        ArrayList<CharSequence> coloredEntries = new ArrayList<>();
        String[] colors = getResources().getStringArray(R.array.theme_entries_value);
        for(int i = 0; i < entries.size(); i++){
            Spannable coloredSummary = new SpannableString (entries.get(i));
            coloredSummary.setSpan( new ForegroundColorSpan(Color.parseColor(colors[i])), 0, coloredSummary.length(), 0 );
            coloredEntries.add(coloredSummary);
        }
        pref.getIcon().setColorFilter(themeColorNum, PorterDuff.Mode.SRC_IN);
        CharSequence[] coloredEntriesArray = coloredEntries.toArray(new CharSequence[coloredEntries.size()]);
        Spannable coloredTitle = new SpannableString (title);
        coloredTitle.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredTitle.length(), 0 );
        pref.setTitle(coloredTitle);
        pref.setEntries(coloredEntriesArray);
    }
}
