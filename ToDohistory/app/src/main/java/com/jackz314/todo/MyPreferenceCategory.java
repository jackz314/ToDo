package com.jackz314.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by zhang on 2017/6/29.
 */

public class MyPreferenceCategory extends PreferenceCategory {
    SettingsActivity settingsActivity;
    public MyPreferenceCategory(Context context) {
        super(context);
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        int textColorNum=settingsActivity.themeColorNum;
        //textColorNum=settingsActivity.getTextColor();
        //view.setBackgroundColor(Color.YELLOW);
        if(view instanceof TextView){
            TextView tv = (TextView)view;
            tv.setTextColor(textColorNum);
        }
    }

}