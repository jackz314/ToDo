package com.jackz314.todo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by jack on 2/26/18.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;

    public PagerAdapter(FragmentManager fragmentManager, int numOfTabs){
        super(fragmentManager);
        this.mNumOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args;
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new ImportantFragment();
                // Our object is just an integer :-P
                args = new Bundle();
                args.putInt("object", position + 1);
                fragment.setArguments(args);
                return fragment;
                //return new ImportantFragment();
            case 1:
                fragment = new MainFragment();
                // Our object is just an integer :-P
                args = new Bundle();
                args.putInt("object", position + 1);
                fragment.setArguments(args);
                return fragment;
                //return new MainFragment();
            case 2:
                fragment = new ClipboardFragment();
                // Our object is just an integer :-P
                args = new Bundle();
                args.putInt("object", position + 1);
                fragment.setArguments(args);
                return fragment;
                //return new ClipboardFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    public void changeTabCount(int newCount){
        mNumOfTabs = newCount;
    }
}
