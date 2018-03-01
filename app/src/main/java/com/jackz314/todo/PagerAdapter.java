package com.jackz314.todo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by jack on 2/26/18.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs = 3;
    Context mContext;

    public PagerAdapter(FragmentManager fragmentManager, Context context){
        super(fragmentManager);
        mContext = context;
        //this.mNumOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args;
        Fragment fragment;
        switch (position) {
            case 0:
                //fragment = new ImportantFragment();
                //args = new Bundle();
                //args.putInt("object", position + 1);
                //fragment.setArguments(args);
                //return fragment;
                return ImportantFragment.newInstance(position);
            case 1:

                return MainFragment.newInstance(position);
            case 2:

                return ClipboardFragment.newInstance(position);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return mContext.getString(R.string.important_tab_title);
            case 1:
                return mContext.getString(R.string.main_tab_title);
            case 2:
                return mContext.getString(R.string.clipboard_tab_title);
            default:
                return super.getPageTitle(position);
        }
    }

    public void changeTabCount(int newCount){
        mNumOfTabs = newCount;
    }
}
