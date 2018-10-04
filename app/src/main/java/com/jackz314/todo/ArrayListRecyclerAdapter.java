package com.jackz314.todo;

import android.content.Context;
import android.graphics.Typeface;
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

    private ArrayList<String> arrayList;
    private ArrayList<String> arrayListFiltered;
    private Context context;

     ArrayListRecyclerAdapter(ArrayList<String> arrayList, Context context){
        this.arrayList = arrayList;
        arrayListFiltered = arrayList;
        this.context = context;
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
         if(arrayListFiltered.size() > 0 && arrayListFiltered.size() > position){
             final String fontName = arrayListFiltered.get(position);
             holder.mainText.setText(fontName);
             //set font here
             //run font requests in separated threads
             GetGoogleFont.GoogleFontCallback googleFontCallback = new GetGoogleFont.GoogleFontCallback() {
                 @Override
                 public void onFontRetrieved(Typeface typeface) {
                     if(typeface != null){
                         holder.mainText.setTypeface(typeface);
                         System.out.println("Font Set: " + holder.mainText.getText());
                     }else {
                         System.out.println("TYPEFACE IS NULL");
                     }
                 }
                 @Override
                 public void onFontRequestError(int errorCode) {
                     System.out.println("Font request failed, error code: " + errorCode);
                     //Toast.makeText(SettingsActivity.this, SettingsActivity.this.getString(R.string.font_list_font_load_failed), Toast.LENGTH_LONG).show();
                 }
             };
             GetGoogleFont.requestGoogleFont(googleFontCallback, fontName,400,false,false, context);
         }
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

    public int getFilteredItemCount(){
         return arrayListFiltered.size();
    }

    @Override
    public Filter getFilter(){
         System.out.println("initial count"+ getItemCount());
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                ArrayList<String> arrTemp = new ArrayList<>();
                if (charString.isEmpty()) {
                    arrTemp = arrayList;
                } else {
                    //arrayListFiltered.clear();//clear previous stuff in it
                    for (String string : arrayList) {
                        //matching...
                        if (string.toLowerCase().contains(charString.toLowerCase())) {
                            arrTemp.add(string);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = arrTemp;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                arrayListFiltered = (ArrayList<String>) results.values;//it's fine, ignore the warning, I had to it this way, and the way that make sure the warning goes away slows down the performance, so no... maybe find out more about it in the future
                System.out.println(" Filtered Result Size: "+arrayListFiltered.size() + " Original Size: " + arrayList.size());
                notifyDataSetChanged();
            }
        };
    }

    public String getItemAtPos(int position){
        return arrayListFiltered.get(position);
    }

    public void swapNewDataSet(ArrayList<String> newArrayList)
    {
        if(newArrayList == null || newArrayList.size()==0) return;
        if (arrayList != null && arrayList.size()>0) arrayList.clear();
        arrayList = newArrayList;
        notifyDataSetChanged();
    }

}
