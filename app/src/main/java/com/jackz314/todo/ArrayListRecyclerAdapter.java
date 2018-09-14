package com.jackz314.todo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

 class ArrayListRecyclerAdapter extends RecyclerView.Adapter<ArrayListRecyclerAdapter.ArrayRecyclerViewHolder> {

    private ArrayList<String> arrayList;
    private Context context;

    ArrayListRecyclerAdapter(ArrayList<String> arrayList){
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ArrayRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //can override at any time to inflate different layouts
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.font_list_item, parent, false);
        return new ArrayRecyclerViewHolder(view);
    }

     @Override
     public void onBindViewHolder(@NonNull final ArrayRecyclerViewHolder holder, int position) {
         holder.mainText.setText(arrayList.get(position));
     }

    static class ArrayRecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView mainText;
        ArrayRecyclerViewHolder(View itemView) {
            super(itemView);
            mainText = itemView.findViewById(R.id.font_name_text);//can override in the future at any time to adapt for other resources
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public String getItemAtPos(int position){
        return arrayList.get(position);
    }

    public void swapNewDataSet(ArrayList<String> newArrayList)
    {
        if(newArrayList == null || newArrayList.size()==0) return;
        if (arrayList != null && arrayList.size()>0) arrayList.clear();
        arrayList = newArrayList;
        notifyDataSetChanged();
    }
}
