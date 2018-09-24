package com.jackz314.todo;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FontSpinnerAdapter extends ArrayAdapter<String>{

    private TextView mTextView = null;
    private Typeface mTypeface = null;

    public FontSpinnerAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
         mTextView = (TextView) super.getView(position, convertView, parent);
         if(mTypeface != null){
             mTextView.setTypeface(mTypeface);
         }
         return mTextView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        mTextView = (TextView) super.getDropDownView(position, convertView, parent);
        if(mTypeface != null){
            mTextView.setTypeface(mTypeface);
        }
        return mTextView;
    }

    public void setTypeface(Typeface typeface){
        mTypeface = typeface;
        notifyDataSetChanged();
    }
}
