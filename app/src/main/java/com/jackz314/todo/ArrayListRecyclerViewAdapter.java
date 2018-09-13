package com.jackz314.todo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

 class ArrayListRecyclerViewAdapter extends RecyclerView.Adapter<ArrayListRecyclerViewAdapter.ArrayRecyclerViewHolder> {

    private ArrayList arrayList;

    ArrayListRecyclerViewAdapter(ArrayList arrayList){
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ArrayRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //can override at any time to inflate different layouts
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.font_list_item, parent, false);
        return new ArrayRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArrayListRecyclerViewAdapter.ArrayRecyclerViewHolder holder, int position) {
        holder.mainText.setText((String)arrayList.get(position));
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
}
