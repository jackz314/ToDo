package com.jackz314.todo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

 class ArrayListRecyclerAdapter extends RecyclerView.Adapter<ArrayListRecyclerAdapter.ArrayRecyclerViewHolder> implements Filterable {

    private ArrayList<String> arrayList, arrayListFiltered;

     ArrayListRecyclerAdapter(ArrayList<String> arrayList){
        this.arrayList = arrayList;
        arrayListFiltered = arrayList;
    }

    @NonNull
    @Override
    public ArrayRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //can override at any time to inflate different layouts
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.font_list_item, parent, false);
        return new ArrayRecyclerViewHolder(view);
    }

     @Override
     public void onBindViewHolder(@NonNull final ArrayRecyclerViewHolder holder, int position) {
         holder.mainText.setText(arrayListFiltered.get(position));
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

    @Override
    public Filter getFilter(){
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    arrayListFiltered = arrayList;
                } else {
                    if(arrayListFiltered == null){//initiate it if not done before
                        arrayListFiltered = new ArrayList<>();
                    }
                    arrayListFiltered.clear();//clear previous stuff in it
                    for (String string : arrayList) {

                        //matching...
                        if (string.toLowerCase().contains(charString.toLowerCase())) {
                            arrayListFiltered.add(string);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = arrayListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                arrayListFiltered = (ArrayList<String>) results.values;//it's fine, ignore the warning, I had to it this way, and the way that make sure the warning goes away slows down the performance, so no... maybe find out more about it in the future
                notifyDataSetChanged();
            }
        };
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
