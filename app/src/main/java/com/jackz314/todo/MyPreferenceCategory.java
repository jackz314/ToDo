package com.jackz314.todo;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

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
        int textColorNum= SettingsActivity.themeColor;
        //textColor=settingsActivity.getTextColor();
        //view.setBackgroundColor(Color.YELLOW);
        if(view instanceof TextView){
            TextView tv = (TextView)view;
            tv.setTextColor(textColorNum);
        }
    }

}