package com.jackz314.todo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by jack on 2/26/18.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs = 3;
    private Context mContext;
    private FragmentManager mFragmentManager;

    public PagerAdapter(FragmentManager fragmentManager, Context context){
        super(fragmentManager);
        mContext = context;
        mFragmentManager = fragmentManager;
        //this.mNumOfTabs = numOfTabs;
    }

   // public Fragment getActiveFragment(ViewPager container, int position) {
   //     String name = getFragmentTag(container, position);
   //     return  mFragmentManager.findFragmentByTag(name);
   // }

    //public static String getFragmentTag(int index) {
        //return "android:switcher:" + container.getId() + ":" + index;//depreciated
    //}

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
