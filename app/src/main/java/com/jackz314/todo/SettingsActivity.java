package com.jackz314.todo;


import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.jackz314.colorpicker.ColorPickerView;
import com.jackz314.colorpicker.builder.ColorPickerClickListener;
import com.jackz314.colorpicker.builder.ColorPickerDialogBuilder;
import com.jackz314.todo.utils.ColorUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.graphics.Color.luminance;
import static com.jackz314.todo.MainActivity.setCursorColor;
import static com.jackz314.todo.R.color.colorActualPrimary;
import static com.jackz314.todo.R.color.colorPrimary;
import static com.jackz314.todo.R.color.dark_theme_background_default_color;
import static com.jackz314.todo.R.color.dark_theme_text_default_color;
import static com.jackz314.todo.R.color.normal_theme_background_default_color;
import static com.jackz314.todo.R.color.normal_theme_text_default_color;
import static com.jackz314.todo.DatabaseManager.DATABASE_NAME;

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
//todo change color picker editText color
//todo migrate the entire settings activity to PreferenceFragment based
//todo add parse date interpretation settings (next XX (week, month...) e.g. as next week this time or monday...) and date interpretation rules
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    static DatabaseManager DatabaseManager;
    ColorUtils colorUtils;
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
    SwitchPreference autoClearSwitch, orderSwitch, mainHistorySwitch, darkThemeSwitch, mainOverdueSwitch, normalOverdueSwitch;
    ThemeListPreference themeSelector;
    Preference wipeButton, themeColor, textColor, backgroundColor, notificationSettings,
        resetAppearanceData, textSize, backupData, restoreData, chooseClrFrequency, restorePurchase;
    MainActivity mainActivity;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ThemeListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ThemeListPreference listPreference = (ThemeListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

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
               // preference.setSummary(stringValue);
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
        themeColorNum = sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(colorActualPrimary));
        textColorNum = sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColorNum = sharedPreferences.getInt(getString(R.string.background_color_key), getResources().getColor(normal_theme_background_default_color));
        chooseClrFrequency.setSummary(sharedPreferences.getString(getString(R.string.clear_interval_summary_key),getString(R.string.disabled)));
        themeSelector.setSummary(sharedPreferences.getString(getString(R.string.theme_selector_summary_key),getString(R.string.custom)));
        if(sharedPreferences.getBoolean(getString(R.string.custom_theme_key),true)){
            themeSelector.setSummary(getString(R.string.custom));
        }
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
        setColorForPref(restorePurchase);
        setColorForPref(darkThemeSwitch);
        setColorForPref(mainOverdueSwitch);
        setColorForPref(normalOverdueSwitch);
        setColorForPref(notificationSettings);
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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        themeSelector = (ThemeListPreference) findPreference(getString(R.string.theme_selector_key));
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
        restorePurchase = findPreference(getString(R.string.restore_purchase_key));
        darkThemeSwitch = (SwitchPreference)findPreference(getString(R.string.dark_theme_key));
        mainOverdueSwitch = (SwitchPreference)findPreference(getString(R.string.main_overdue_switch));
        normalOverdueSwitch = (SwitchPreference)findPreference(getString(R.string.normal_overdue_switch));
        notificationSettings = findPreference(getString(R.string.notification_settings));
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setColorPreferencesSettings();
        Window window = this.getWindow();
        window.setStatusBarColor(themeColorNum);
        window.setNavigationBarColor(themeColorNum);
        final LayoutInflater inf = LayoutInflater.from(this);
        setupActionBar();
        mainActivity = new MainActivity();
        /********************/
        DatabaseManager = new DatabaseManager(getApplicationContext());
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

        restorePurchase = findPreference(getString(R.string.restore_purchase_key));
        setColorForPref(restorePurchase);
        restorePurchase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final boolean purchased = mainActivity.determineIfPurchased();
                if(purchased){
                    Toast.makeText(getApplicationContext(),getString(R.string.purchase_updated),Toast.LENGTH_SHORT).show();
                } else {
                    final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this).setMessage(getString(R.string.restore_purchase_failed)).setTitle(R.string.restore_error)
                            .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText(getString(R.string.err_msg),Build.MANUFACTURER + "\n" + Build.BRAND + "\n" + Build.DEVICE + "\n" + Build.MODEL + "\n"+ Build.HARDWARE + "\n" + Build.VERSION.RELEASE + "\n" + Build.VERSION.CODENAME + "\n" + Build.VERSION.SDK_INT + "\n" +  Build.VERSION.INCREMENTAL + "\n" + Build.VERSION.SECURITY_PATCH );
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getApplicationContext(),getString(R.string.err_msg_copied),Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
                }
                return false;
            }
        });
        themeSelector = (ThemeListPreference) findPreference(getString(R.string.theme_selector_key));
        setColorForPref(themeSelector);
        themeSelector.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                System.out.println("newVal" + newValue.toString());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String themeSummary;
                int pos = Arrays.asList(themeSelector.getEntryValues()).indexOf(String.valueOf(newValue));
                themeSummary = themeSelector.getEntries()[pos].toString();
                editor.putString(getString(R.string.theme_selector_summary_key),themeSummary);
                editor.putInt(getString(R.string.theme_selector_value_key), Color.parseColor(newValue.toString()));
                editor.putInt(getString(R.string.theme_color_key), Color.parseColor(newValue.toString()));
                editor.putString(getString(R.string.theme_selector_key),String.valueOf(newValue));
                editor.putInt(getString(R.string.theme_selector_position_key),themeSelector.findIndexOfValue(newValue.toString()));
                editor.putBoolean(getString(R.string.custom_theme_key),false);
                if(themeSummary.contains("(")){// dark theme
                    editor.putInt(getString(R.string.text_color_key), getResources().getColor(dark_theme_text_default_color));
                    editor.putInt(getString(R.string.background_color_key), getResources().getColor(dark_theme_background_default_color));
                    textColorNum = getResources().getColor(dark_theme_text_default_color);
                    backgroundColorNum = getResources().getColor(dark_theme_background_default_color);
                }else {// normal theme
                    editor.putInt(getString(R.string.text_color_key), getResources().getColor(normal_theme_text_default_color));
                    editor.putInt(getString(R.string.background_color_key), getResources().getColor(normal_theme_background_default_color));
                    textColorNum = getResources().getColor(normal_theme_text_default_color);
                    backgroundColorNum = getResources().getColor(normal_theme_background_default_color);
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

        darkThemeSwitch = (SwitchPreference)findPreference(getString(R.string.dark_theme_key));
        setColorForPref(darkThemeSwitch);
        darkThemeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.dark_theme_key),(boolean)o);
                editor.commit();
                darkThemeSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.dark_theme_key),false));
                if(sharedPreferences.getBoolean(getString(R.string.dark_theme_key),false)){//if changed to dark theme
                    editor.putInt(getString(R.string.text_color_key), getResources().getColor(dark_theme_text_default_color));
                    editor.putInt(getString(R.string.background_color_key), getResources().getColor(dark_theme_background_default_color));
                    textColorNum = getResources().getColor(dark_theme_text_default_color);
                    backgroundColorNum = getResources().getColor(dark_theme_background_default_color);
                }else {//if changed to normal theme
                    editor.putInt(getString(R.string.text_color_key), getResources().getColor(normal_theme_text_default_color));
                    editor.putInt(getString(R.string.background_color_key), getResources().getColor(normal_theme_background_default_color));
                    textColorNum = getResources().getColor(normal_theme_text_default_color);
                    backgroundColorNum = getResources().getColor(normal_theme_background_default_color);
                }
                setColorPreferencesSettings();//refresh UI with new setting
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
                final Spinner unitChooser = dialogView.findViewById(R.id.unit_chooser);
                final NumberPicker intervalChooser = dialogView.findViewById(R.id.interval_num_picker);
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
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
                final NumberPicker numberPicker = dialogView.findViewById(R.id.numberPicker);
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(60);
                numberPicker.setValue(sharedPreferences.getInt("text_size_key",20));

                //edt.setText(String.valueOf(sharedPreferences.getInt("text_size_key",20)));
                //edt.selectAll();
                final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getString(R.string.txt_size_title))
                        .setMessage(getString(R.string.txt_size_message))
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if((numberPicker.getValue() > 0)){
                                    final int textSize = numberPicker.getValue();
                                    if(numberPicker.getValue()>=50||numberPicker.getValue()<=10){
                                        final AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)//alert title
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
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        if(newVal != oldVal && newVal != sharedPreferences.getInt("text_size_key",20)){
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
                final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)//alert title
                        .setMessage(R.string.warning_content)//alert content
                        .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {//YES
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//YES
                                DatabaseManager.wipeHistory();
                            }
                        }).setNegativeButton(R.string.reconsider, new DialogInterface.OnClickListener() {//NO
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//NO
                        Log.i("information","canceled");
                    }
                }).show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
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
               AlertDialog colorPicker = ColorPickerDialogBuilder.with(SettingsActivity.this)
                        .setTitle(getString(R.string.theme_color_selector))
                        .initialColor(themeColorNum)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setPositiveButton(getString(R.string.finish), new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int color, Integer[] integers) {
                                final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(getString(R.string.theme_color_key),color);
                                editor.putBoolean(getString(R.string.custom_theme_key),true);
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
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).showColorEdit(true).showColorPreview(true)
                        .lightnessSliderOnly().setColorEditTextColor(themeColorNum).build();
                colorPicker.show();
                colorPicker.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(themeColorNum);
                colorPicker.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(themeColorNum);
                return false;
            }
        });
        textColor = findPreference(getString(R.string.text_color_key));
        setColorForPref(textColor);
        textColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {// change text color
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog colorPicker = ColorPickerDialogBuilder.with(SettingsActivity.this)
                        .setTitle(getString(R.string.text_color_selector))
                        .initialColor(textColorNum)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setPositiveButton(getString(R.string.finish), new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int color, Integer[] integers) {
                                final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                                if(ColorUtils.colorIsSimilar(sharedPreferences.getInt(getString(R.string.background_color_key),backgroundColorNum),color)){
                                    final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)
                                            .setMessage(R.string.color_conflict)
                                            .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putInt(getString(R.string.text_color_key),color);
                                                    editor.putBoolean(getString(R.string.custom_theme_key),true);
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
                                                    //empty
                                                }
                                            }).show();
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
                                }else{
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt(getString(R.string.text_color_key),color);
                                    editor.putBoolean(getString(R.string.custom_theme_key),true);
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
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).showColorEdit(true).showColorPreview(true).lightnessSliderOnly().setColorEditTextColor(textColorNum).build();
                colorPicker.show();
                colorPicker.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(themeColorNum);
                colorPicker.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(themeColorNum);
                return false;
            }
        });
        backgroundColor = findPreference(getString(R.string.background_color_key));
        setColorForPref(backgroundColor);
        backgroundColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

               AlertDialog colorPicker = ColorPickerDialogBuilder.with(SettingsActivity.this)
                        .setTitle(getString(R.string.background_color_selector))
                        .initialColor(backgroundColorNum)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setPositiveButton(getString(R.string.finish), new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int color, Integer[] integers) {
                                final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                                if(ColorUtils.colorIsSimilar(sharedPreferences.getInt(getString(R.string.text_color_key),textColorNum), color)){
                                    final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)
                                            .setMessage(R.string.color_conflict)
                                            .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putInt(getString(R.string.background_color_key),color);
                                                    editor.putBoolean(getString(R.string.custom_theme_key),true);
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
                                                }
                                            }).show();
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
                                }else{
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt(getString(R.string.background_color_key),color);
                                    editor.putBoolean(getString(R.string.custom_theme_key),true);
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
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).showColorEdit(true).showColorPreview(true).lightnessSliderOnly().setColorEditTextColor(backgroundColorNum).build();
                colorPicker.show();
                colorPicker.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(themeColorNum);
                colorPicker.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(themeColorNum);
                return false;
            }
        });
        resetAppearanceData = findPreference(getString(R.string.reset_appearance_key));
        setColorForPref(resetAppearanceData);
        resetAppearanceData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.warning_title)//alert title
                        .setMessage(R.string.warning_content)//alert content
                        .setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {//YES
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//YES
                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(getString(R.string.background_color_key), getResources().getColor(normal_theme_background_default_color));
                                editor.putInt(getString(R.string.text_color_key),Color.BLACK);
                                editor.putInt(getString(R.string.theme_color_key),getResources().getColor(colorPrimary));
                                editor.putBoolean(getString(R.string.order_key),true);
                                editor.putInt(getString(R.string.text_size_key),20);
                                editor.apply();
                                setColorPreferencesSettings();
                            }
                        }).setNegativeButton(R.string.reconsider, new DialogInterface.OnClickListener() {//NO
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//NO
                        Log.i("information","canceled");
                    }
                }).show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
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
                final EditText edt = dialogView.findViewById(R.id.tagText);
                edt.setBackgroundTintList(ColorStateList.valueOf(themeColorNum));
                setCursorColor(edt,themeColorNum);
                Button editPath = dialogView.findViewById(R.id.path_selector);
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
                                    backupFileName = "ToDo_BACKUP_ " + edt.getText().toString().trim() + "_" + timeStampName + ".database";
                                }else {
                                    backupFileName = "ToDo_BACKUP_" + timeStampName + ".database";
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
                backupDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                backupDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
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
                final CheckBox replaceCheck = dialogView.findViewById(R.id.replace_current_checkbox);
                Button selectBackup = dialogView.findViewById(R.id.select_backup_file_btn);
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
                restoreDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColorNum);
                restoreDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColorNum);
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
     mainOverdueSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
         @Override
         public boolean onPreferenceChange(Preference preference, Object newValue) {
             SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings_data",MODE_PRIVATE);
             SharedPreferences.Editor editor = sharedPreferences.edit();
             editor.putBoolean(getString(R.string.main_overdue_switch),(boolean)newValue);
             editor.commit();
             Bundle bundle = new Bundle();
             bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "main_overdue_switch");
             bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Main Overdue Switch: " + sharedPreferences.getBoolean(getString(R.string.main_overdue_switch),true));
             bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "switch");
             mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
             mainOverdueSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.main_overdue_switch),false));
             PreferenceScreen screen = getPreferenceScreen();
             if(mainOverdueSwitch.isChecked()){//remove/add normalOverdueSwitch based on status of mainOverdueSwitch
                 screen.addPreference(normalOverdueSwitch);
             }else {
                 screen.removePreference(normalOverdueSwitch);
             }
             return false;
         }
     });

     notificationSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
         @Override
         public boolean onPreferenceClick(Preference preference) {
             Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
             }else {
                 intent.putExtra("app_package", getPackageName());
                 intent.putExtra("app_uid", getApplicationInfo().uid);
             }
             startActivity(intent);
             return false;
         }
     });

    }

    public boolean restoreDataFromBackup(String filePath){
        if(DatabaseManager.validateBackup(filePath)){
            String result = DatabaseManager.mergeBackup(filePath) ;
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
        if(DatabaseManager.validateBackup(filePath)){
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
                if(!processFile.endsWith(".database")){
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
            coloredTitle.setSpan( new ForegroundColorSpan(ColorUtils.lighten(textColorNum,0.5)), 0, coloredTitle.length(), 0 );
            coloredSummary.setSpan( new ForegroundColorSpan(ColorUtils.lighten(textColorNum,0.5)), 0, coloredSummary.length(), 0 );
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
                        getResources().getColor(normal_theme_background_default_color)//disabled
                        ,ColorUtils.lighten(themeColorNum,0.32) //enabled
                }
        );
        if(pref.isEnabled()){
            //pref.setWidgetLayoutResource(R.layout.custom_switchpreference);
            Switch customSwitch = (Switch)getLayoutInflater().inflate(R.layout.custom_switchpreference,null);
            Switch customRealSwitch = customSwitch.findViewById(R.id.custom_switch_item);
            ColorStateList buttonStates = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            Color.BLUE,
                            Color.RED,
                            Color.GREEN
                    }
            );
            customRealSwitch.setButtonTintList(buttonStates);
            coloredTitle.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredTitle.length(), 0 );
            if(pref.getSummary()!= null){
                String summary = pref.getSummary().toString();
                Spannable coloredSummary = new SpannableString (summary);
                coloredSummary.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredSummary.length(), 0 );
                pref.setSummary(coloredSummary);
            }
        }else {
            coloredTitle.setSpan( new ForegroundColorSpan(ColorUtils.lighten(textColorNum,0.5)), 0, coloredTitle.length(), 0 );
            if(pref.getSummary()!= null){
                String summary = pref.getSummary().toString();
                Spannable coloredSummary = new SpannableString (summary);
                coloredSummary.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredSummary.length(), 0 );
                pref.setSummary(coloredSummary);
            }
        }
        pref.setTitle(coloredTitle);

    }

    public int getListPos(){
        return sharedPreferences.getInt(getString(R.string.theme_selector_position_key),0);
    }

    public void manuallySetPreferenceChange(String newValue){
        //System.out.println("NEW " + newValue);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String themeSummary;
        int pos = Arrays.asList(themeSelector.getEntryValues()).indexOf(String.valueOf(newValue));
        themeSummary = themeSelector.getEntries()[pos].toString();
        editor.putString(getString(R.string.theme_selector_summary_key),themeSummary);
        editor.putInt(getString(R.string.theme_selector_value_key), Color.parseColor(newValue));
        editor.putInt(getString(R.string.theme_color_key), Color.parseColor(newValue));
        editor.putString(getString(R.string.theme_selector_key),String.valueOf(newValue));
        editor.putInt(getString(R.string.theme_selector_position_key),themeSelector.findIndexOfValue(newValue));
        editor.putBoolean(getString(R.string.custom_theme_key),false);
        if(themeSummary.contains("(")){// dark theme
            editor.putInt(getString(R.string.text_color_key), getResources().getColor(dark_theme_text_default_color));
            editor.putInt(getString(R.string.background_color_key), getResources().getColor(dark_theme_background_default_color));
            textColorNum = getResources().getColor(dark_theme_text_default_color);
            backgroundColorNum = getResources().getColor(dark_theme_background_default_color);
        }else {// normal theme
            editor.putInt(getString(R.string.text_color_key), getResources().getColor(normal_theme_text_default_color));
            editor.putInt(getString(R.string.background_color_key), getResources().getColor(normal_theme_background_default_color));
            textColorNum = getResources().getColor(normal_theme_text_default_color);
            backgroundColorNum = getResources().getColor(normal_theme_background_default_color);
        }
        themeColorNum = Color.parseColor(newValue);
        editor.commit();
        String themeSelectorSummary = sharedPreferences.getString(getString(R.string.theme_selector_summary_key),getString(R.string.custom));
        themeSelector.setSummary(themeSelectorSummary);
        setColorPreferencesSettings();
        setColorForPref(themeSelector);
    }

    public void setColorForPref(ThemeListPreference pref){
        String title = pref.getTitle().toString();
        Spannable coloredCancelText = new SpannableString(pref.getNegativeButtonText());
        coloredCancelText.setSpan(new ForegroundColorSpan(themeColorNum),0,coloredCancelText.length(),0);
        pref.setNegativeButtonText(coloredCancelText);
        ArrayList<CharSequence> entries = new ArrayList<>(Arrays.asList(pref.getEntries()));
        ArrayList<CharSequence> valaues = new ArrayList<>(Arrays.asList(pref.getEntryValues()));
        //pref.setDefaultValue(valaues.indexOf(sharedPreferences.getString(getString(R.string.theme_selector_position_key),"#3F51B5")));
        ArrayList<CharSequence> coloredEntries = new ArrayList<>();
        String[] colors = getResources().getStringArray(R.array.theme_entries_value);
        for(int i = 0; i < entries.size(); i++){
            Spannable coloredSummary = new SpannableString (entries.get(i));
            coloredSummary.setSpan( new ForegroundColorSpan(Color.parseColor(colors[i])), 0, coloredSummary.length(), 0 );
            coloredEntries.add(coloredSummary);
        }

        CharSequence[] coloredEntriesArray = coloredEntries.toArray(new CharSequence[coloredEntries.size()]);
        Spannable coloredTitle = new SpannableString (title);
        coloredTitle.setSpan( new ForegroundColorSpan(textColorNum), 0, coloredTitle.length(), 0 );
        pref.setTitle(coloredTitle);
        pref.setEntries(coloredEntriesArray);
    }
}
