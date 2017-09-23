package com.jackz314.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by zhang on 2017/9/18.
 */

class ColorSwitchPreference extends SwitchPreference {
    Switch aSwitch;
    SharedPreferences sharedPreferences;

    public ColorSwitchPreference(Context context){
        super(context);
    }

    public ColorSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColorSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        aSwitch = findSwitchInChildViews((ViewGroup) view);
        if (aSwitch!=null) {
            //do change color here
            changeColor(aSwitch.isChecked(),aSwitch.isEnabled());
            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    changeColor(isChecked, aSwitch.isEnabled());
                }
            });
        }
    }

    private void changeColor(boolean checked, boolean enabled){
        try {
            sharedPreferences = getContext().getSharedPreferences("settings_data",MODE_PRIVATE);
            //apply the colors here
            int thumbCheckedColor = sharedPreferences.getInt("theme_color_key",Color.parseColor("#3F51B5"));
            int thumbUncheckedColor = Color.parseColor("#ECECEC");
            int trackCheckedColor = sharedPreferences.getInt("theme_color_key",Color.parseColor("#3F51B5"));
            int trackUncheckedColor = Color.parseColor("#B9B9B9");
            if(enabled){
                aSwitch.getThumbDrawable().setColorFilter(checked ? thumbCheckedColor : thumbUncheckedColor, PorterDuff.Mode.MULTIPLY);
                aSwitch.getTrackDrawable().setColorFilter(checked ? trackCheckedColor : trackUncheckedColor, PorterDuff.Mode.MULTIPLY);
            }else {
                aSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#B9B9B9"), PorterDuff.Mode.MULTIPLY);
                aSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#E9E9E9"), PorterDuff.Mode.MULTIPLY);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private Switch findSwitchInChildViews(ViewGroup view) {// find the Switch widget in the SwitchPreference
        for (int i=0;i<view.getChildCount();i++) {
            View thisChildview = view.getChildAt(i);
            if (thisChildview instanceof Switch) {
                return (Switch)thisChildview;
            }
            else if (thisChildview instanceof  ViewGroup) {
                Switch theSwitch = findSwitchInChildViews((ViewGroup) thisChildview);
                if (theSwitch!=null) return theSwitch;
            }
        }
        return null;
    }
}
