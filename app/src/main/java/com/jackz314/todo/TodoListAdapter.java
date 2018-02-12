package com.jackz314.todo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;


/*
 * Created by zhang on 2017/8/13.
 */

 class TodoListAdapter extends CursorRecyclerAdapter<TodoListAdapter.TodoViewHolder> {
    private Cursor c;
    //Context mContext;
    private ArrayList<Long> itemChecked = new ArrayList<>();
    protected int[] mFrom;
    protected int[] mTo;

    LayoutInflater inflater;


     TodoListAdapter(Cursor cursor){
        super(cursor);
        c = cursor;
    }

    @Override
    public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todolist,parent,false);
        //System.out.println("|cursor created");
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TodoViewHolder holder, final Cursor cursor) {
        final long id = cursor.getInt(cursor.getColumnIndex(dtb.ID));
        //String text = cursor.getString(cursor.getColumnIndex(dtb.TITLE));
        //holder.todoText.setText(text);
        //holder.todoText.setTextColor(Color.BLACK);
        //System.out.println(text+"|cursor read");
        /*holder.cBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.findViewById(R.id.multiSelectionBox);
                if (cb.isChecked()) {
                    if(mContext.toString().contains("MainActivity")){
                        ((MainActivity)mContext).addSelectedId(id);
                    }else if(mContext.toString().contains("HistoryActivity")){
                        ((HistoryActivity)mContext).addSelectedId(id);
                    }else if (mContext.toString().contains("TagsActivity")){
                        ((TagsActivity)mContext).addSelectedId(id);
                    }
                    //System.out.println("checked " + id);
                    // do some operations here
                } else if (!cb.isChecked()) {
                    //System.out.println("unchecked " + id);
                    if(mContext.toString().contains("MainActivity")){
                        ((MainActivity)mContext).removeSelectedId(id);
                    }else if(mContext.toString().contains("HistoryActivity")){
                        ((HistoryActivity)mContext).removeSelectedId(id);
                    }else if (mContext.toString().contains("TagsActivity")){
                        ((TagsActivity)mContext).removeSelectedId(id);
                    }
                }
            }
        });*/
        //override in main/history activity?
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView todoText;
        TextView tagText;
        CardView cardView;
        View tagDot;
        CheckBox cBox;
        TodoViewHolder(View view){
            super(view);
            todoText = (TextView) view.findViewById(R.id.titleText);
            tagText = (TextView) view.findViewById(R.id.tags_selection_text);
            cBox = (CheckBox) view.findViewById(R.id.multiSelectionBox);
            cardView = (CardView) view.findViewById(R.id.cardView);
            tagDot = view.findViewById(R.id.tag_dot);
            /*final long id = getItemId(position);
            cBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v.findViewById(R.id.multiSelectionBox);
                    if (cb.isChecked()) {
                        if(context.toString().contains("MainActivity")){
                            ((MainActivity)context).addSelectedId(id);
                        }else if(context.toString().contains("HistoryActivity")){
                            ((HistoryActivity)context).addSelectedId(id);
                        }
                        //System.out.println("checked " + id);
                        // do some operations here
                    } else if (!cb.isChecked()) {
                        //System.out.println("unchecked " + id);
                        if(context.toString().contains("MainActivity")){
                            ((MainActivity)context).removeSelectedId(id);
                        }else if(context.toString().contains("HistoryActivity")){
                            ((HistoryActivity)context).removeSelectedId(id);
                        }                    // do some operations here
                    }
                }
            });*/
        }
    }

    private int mStringConversionColumn = -1;
    MainActivity main;
    String[] mOriginalFrom;

// itemChecked will store the position of the checked items.



    public void setCheckboxChecked(View view, boolean YN){
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.multiSelectionBox);
        checkBox.setChecked(YN);
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
/*
    public View getView(int position, View view, ViewGroup parent){
        if(c.moveToPosition(position)){
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.todolist, null);
                newView(context,c,parent);
            }
            bindView(view,context,c);
            ////System.out.println("bindView!");

            final TextView todoText = (TextView) view.findViewById(R.id.titleText);
            final CheckBox cBox = (CheckBox) view.findViewById(R.id.multiSelectionBox); // your CheckBox
            //System.out.println(c.getCount());
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
                        //System.out.println("checked " + id);
                        // do some operations here
                    } else if (!cb.isChecked()) {
                        //System.out.println("unchecked " + id);
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
    }*/ //getView method that will scroll the list to the top every time you refreshes the data
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
        //System.out.println("bindView!");

        final TextView todoText = (TextView) view.findViewById(R.id.titleText);
        final CheckBox cBox = (CheckBox) view.findViewById(R.id.multiSelectionBox); // your CheckBox
        //System.out.println(c.getCount());
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
                    //System.out.println("checked " + id);
                    // do some operations here
                } else if (!cb.isChecked()) {
                    //System.out.println("unchecked " + id);
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
