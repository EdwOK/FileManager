package com.project.filemanager.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.project.filemanager.fragments.BrowserFragment;

public class ExplorerTabsAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_PAGES = 2;

    private static Fragment mCurrentFragment;

    public ExplorerTabsAdapter(FragmentManager fm) {
        super(fm);
    }

    public static BrowserFragment getCurrentBrowserFragment() {
        return (BrowserFragment) mCurrentFragment;
    }

    @Override
    public Fragment getItem(int pos) {
        return new BrowserFragment();
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (mCurrentFragment != object) {
            mCurrentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }
}