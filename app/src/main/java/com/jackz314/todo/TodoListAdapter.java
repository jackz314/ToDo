package com.jackz314.todo;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/*
 * Created by zhang on 2017/8/13.
 */

public class TodoListAdapter extends SimpleCursorAdapter {
    private Cursor c;
    private Context context;
    private ArrayList<Long> itemChecked = new ArrayList<>();
    protected int[] mFrom;
    protected int[] mTo;

    LayoutInflater inflater;
    private int mStringConversionColumn = -1;
    private CursorToStringConverter mCursorToStringConverter;
    private ViewBinder mViewBinder;
    MainActivity main;
    String[] mOriginalFrom;

// itemChecked will store the position of the checked items.

    public TodoListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to,1);
        this.c = c;
        this.context = context;
        mTo = to;
        mOriginalFrom = from;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*for (int i = 0; i < this.getCount(); i++) {
            itemChecked.add(i, false); // initializes all items value with false
        }*/
    }
  /*
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        System.out.println("BIND VIEW!");
        final ViewBinder binder = mViewBinder;
        final int count = mTo.length;
        final int[] from = mFrom;
        final int[] to = mTo;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.todolist, null);
        }
        TextView todoText = (TextView) view.findViewById(R.id.titleText);
        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, cursor, from[i]);
                }

                if (!bound) {
                    String text = cursor.getString(from[i]);
                    if (text == null) {
                        text = "";
                    }

                    if (v instanceof TextView) {
                        todoText.setText(text);
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        setViewImage((ImageView) v, text);
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " + " view that can be bounds by this SimpleCursorAdapter");
                    }
                }
            }
        }
        super.bindView(view,context,cursor);
    }
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = inflater.inflate(R.layout.todolist, parent, false);
        return v;
    }*/

    public void setCheckboxChecked(View view, boolean YN){
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
        checkBox.setChecked(YN);
    }

    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    public ArrayList returnSelected(){
        return itemChecked;
    }

    public interface MyInterface{
        public void foo();
    }

    public void clearCheckbox(){
        //final CheckBox cBox = (CheckBox)findViewById(R.id.multiSelectionBox); // your CheckBox
    }

    public void refreshCursor(Cursor cursor){
        c = cursor;
        swapCursor(c);
    }

    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    public View getView(int position, View view, ViewGroup parent){
        if(c.moveToPosition(position)){
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.todolist, null);
                newView(context,c,parent);
            }
            bindView(view,context,c);
            System.out.println("bindView!");

            final TextView todoText = (TextView) view.findViewById(R.id.titleText);
            final CheckBox cBox = (CheckBox) view.findViewById(R.id.multiSelectionBox); // your CheckBox
            System.out.println(c.getCount());
            final long id = getItemId(position);
            cBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v.findViewById(R.id.multiSelectionBox);
                    if (cb.isChecked()) {
                        if(context.toString().contains("MainActivity")){
                            ((MainActivity)context).addSelectedId(id);
                        }else if(context.toString().contains("HistoryActivity")){
                            ((HistoryActivity)context).addSelectedId(id);
                        }
                        System.out.println("checked " + id);
                        // do some operations here
                    } else if (!cb.isChecked()) {
                        System.out.println("unchecked " + id);
                        if(context.toString().contains("MainActivity")){
                            ((MainActivity)context).removeSelectedId(id);
                        }else if(context.toString().contains("HistoryActivity")){
                            ((HistoryActivity)context).removeSelectedId(id);
                        }                    // do some operations here
                    }
                }
            });
        } else {
            newView(context,c,parent);
            bindView(view,context,c);
        }
        return view;
    } //getView method that will scroll the list to the top every time you refreshes the data
/*
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.todolist, parent,false);
    }

    public void bindView(View view, final Context context, Cursor cursor){
        int position = cursor.getPosition();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.todolist, null);
        }
        System.out.println("bindView!");

        final TextView todoText = (TextView) view.findViewById(R.id.titleText);
        final CheckBox cBox = (CheckBox) view.findViewById(R.id.multiSelectionBox); // your CheckBox
        System.out.println(c.getCount());
        final long id = getItemId(position);
        cBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.findViewById(R.id.multiSelectionBox);
                if (cb.isChecked()) {
                    if(context.toString().contains("MainActivity")){
                        ((MainActivity)context).addSelectedId(id);
                    }else if(context.toString().contains("HistoryActivity")){
                        ((HistoryActivity)context).addSelectedId(id);
                    }
                    System.out.println("checked " + id);
                    // do some operations here
                } else if (!cb.isChecked()) {
                    System.out.println("unchecked " + id);
                    if(context.toString().contains("MainActivity")){
                        ((MainActivity)context).removeSelectedId(id);
                    }else if(context.toString().contains("HistoryActivity")){
                        ((HistoryActivity)context).removeSelectedId(id);
                    }                    // do some operations here
                }
            }
        });
    }*/

}
