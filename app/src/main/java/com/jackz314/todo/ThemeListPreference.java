package com.jackz314.todo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.StateListDrawable;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

/**
 * Created by zhang on 2017/9/6.
 */

public class ThemeListPreference extends ListPreference implements AdapterView.OnItemClickListener {

    Context mContext;
    public ThemeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public ThemeListPreference(Context context) {
        super(context);
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
            Resources resources = mContext.getResources();
            int darkblue = resources.getColor(R.color.colorPrimary);
            int red = resources.getColor(R.color.red);
            int green = resources.getColor(R.color.green);
            int cyan = resources.getColor(R.color.cyan);
            int orange = resources.getColor(R.color.cyan);
            int yellow = resources.getColor(R.color.yellow);
            int blue = resources.getColor(R.color.blue);
            int pink = resources.getColor(R.color.pink);
            int brown = resources.getColor(R.color.brown);
            int cyan_dark = resources.getColor(R.color.cyan_dark);
            int brown_dark = resources.getColor(R.color.brown_dark);
            int purple = resources.getColor(R.color.purple);
            int black = resources.getColor(R.color.black);


            @Override
            public View getView(int position, View row, ViewGroup parent) {
                if (row == null) {
                    row = LayoutInflater.from(mContext).inflate(R.layout.theme_selector_layout, parent, false);
                }

                RadioButton button = (RadioButton) row.findViewById(R.id.theme_list_row_radio);
                if (position == findIndexOfValue(PreferenceManager.getDefaultSharedPreferences(mContext).getString(getKey(), ""))) {
                    button.setChecked(true);
                }

                StateListDrawable drawable = (StateListDrawable) button.getCompoundDrawables()[0];

                switch (position) {
                    case 0:
                        drawable.setColorFilter(darkblue, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 1:
                        drawable.setColorFilter(red, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 2:
                        drawable.setColorFilter(green, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 3:
                        drawable.setColorFilter(cyan, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 4:
                        drawable.setColorFilter(orange, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 5:
                        drawable.setColorFilter(yellow, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 6:
                        drawable.setColorFilter(blue, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 7:
                        drawable.setColorFilter(pink, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 8:
                        drawable.setColorFilter(brown, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 9:
                        drawable.setColorFilter(cyan_dark, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 10:
                        drawable.setColorFilter(brown_dark, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 11:
                        drawable.setColorFilter(purple, PorterDuff.Mode.MULTIPLY);
                        break;
                    case 12:
                        drawable.setColorFilter(black, PorterDuff.Mode.MULTIPLY);
                        break;
                    default:
                        throw new IllegalStateException("Undefined theme");
                }

                return super.getView(position, row, parent);
            }
        };
        builder.setAdapter(adapter, this);

        super.onPrepareDialogBuilder(builder);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
