package com.jackz314.todo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.ListPreference;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by zhang on 2017/9/6.
 */

public class ThemeListPreference extends ListPreference implements AdapterView.OnItemClickListener {
    Context mContext;
    int textColor;
    public ThemeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext =context;
        //sharedPreferences = getContext().getSharedPreferences("settings_data",MODE_PRIVATE);
    }

    public ThemeListPreference(Context context) {
        super(context);
        //sharedPreferences = getContext().getSharedPreferences("settings_data",MODE_PRIVATE);
        //textColor = sharedPreferences.getInt("text_color_key",Color.parseColor("#121212"));
        mContext = context;
    }

    @Override
    protected void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
        if (builder == null) {
            throw new NullPointerException("Builder is null");
        }
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();

        if (entries == null || entryValues == null || entries.length != entryValues.length) {
            throw new IllegalStateException("Invalid entries array or entryValues array");
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.theme_selector_layout, R.id.theme_list_row_name, entries) {
            Resources resources = getContext().getResources();
            /*int darkblue = resources.getColor(R.color.colorPrimary);
            int red = resources.getColor(R.color.red);
            int green = resources.getColor(R.color.green);
            int cyan = resources.getColor(R.color.cyan);
            int orange = resources.getColor(R.color.orange);
            int yellow = resources.getColor(R.color.yellow);
            int blue = resources.getColor(R.color.blue);
            int pink = resources.getColor(R.color.pink);
            int brown = resources.getColor(R.color.brown);
            int cyan_dark = resources.getColor(R.color.cyan_dark);
            int brown_dark = resources.getColor(R.color.brown_dark);
            int purple = resources.getColor(R.color.purple);
            int black = resources.getColor(R.color.black);*/


            @Override
            public View getView(final int position, View row, ViewGroup parent) {
                //sharedPreferences = getContext().getSharedPreferences("settings_data",MODE_PRIVATE);
                //Spannable spannable = new SpannableString(getSummary().toString());
                //spannable.setSpan( new ForegroundColorSpan(sharedPreferences.getInt("text_color_key",Color.parseColor("#121212"))), 0, spannable.length(), 0 );
                //setSummary(spannable);
                if (row == null) {
                    row = LayoutInflater.from(getContext()).inflate(R.layout.theme_selector_layout, parent, false);
                }
                //RadioButton button = (RadioButton) row.findViewById(R.id.theme_list_row_radio);
                //StateListDrawable drawable = (StateListDrawable) button.getCompoundDrawables()[0];

                final RadioButton radioButton = (RadioButton)row.findViewById(R.id.theme_list_row_radio);
                //System.out.println("ROW "+row.getContentDescription());
                final SettingsActivity settingsActivity = (SettingsActivity)mContext;
                final int listPos = settingsActivity.getListPos();
                radioButton.setChecked(false);
                if (position == listPos) {
                    //System.out.println("asdf "+listPos);
                    //radioButton = (RadioButton)row.findViewById(R.id.theme_list_row_radio);
                    radioButton.setChecked(true);
                }
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RadioButton radioButtonSub = (RadioButton)v.findViewById(R.id.theme_list_row_radio);
                        if(radioButtonSub.isChecked()){
                            ArrayList<CharSequence> entries = new ArrayList<CharSequence>(Arrays.asList(getEntries()));
                            int itemPos = entries.indexOf(getItem(position));
                            String realValue = getEntryValues()[itemPos].toString();
                            settingsActivity.manuallySetPreferenceChange(realValue);
                        }
                        getDialog().dismiss();
                    }
                });
                ColorStateList colorStateList;
                String[] colorArray = resources.getStringArray(R.array.theme_entries_value);
                colorStateList = new ColorStateList(
                        new int[][]{

                                new int[]{-android.R.attr.state_enabled}, //disabled
                                new int[]{android.R.attr.state_enabled} //enabled
                        },
                        new int[] {
                                Color.parseColor("#fafafa")//disabled
                                ,Color.parseColor(colorArray[position]) //enabled
                        }
                );
                radioButton.setButtonTintList(colorStateList);
                return super.getView(position, row, parent);
            }
        };
        Spannable spannable = new SpannableString("fuck");
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE),0,spannable.length(),0);
        builder.setAdapter(adapter, this);
        super.onPrepareDialogBuilder(builder);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getDialog().dismiss();
    }
}
