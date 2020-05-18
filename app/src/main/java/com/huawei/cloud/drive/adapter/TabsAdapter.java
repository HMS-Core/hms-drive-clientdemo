/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.cloud.drive.adapter;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import androidx.legacy.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;

import com.huawei.cloud.drive.model.TabInfo;
import com.huawei.cloud.drive.view.FileViewPager;

import java.util.ArrayList;

/**
 * Tabs Adapter
 */
public class TabsAdapter extends FragmentStatePagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
    private static final String TAG = "TabsAdapter";

    private final ViewPager mViewPager;

    private Context mContext;

    /**
     * Tab list
     */
    public final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    public TabsAdapter(Context context, FileViewPager viewPager, FragmentManager fm) {
        super(fm);
        mContext = context;
        mViewPager = viewPager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    public Fragment getItem(int position) {
        TabInfo info = mTabs.get(position);
        info.fragment = Fragment.instantiate(mContext,
                info.loadedClass.getName(), info.args);
        return info.fragment;
    }

    /**
     * Add tab
     *
     * @param _Class class
     * @param args   other args
     */
    public void addTab(Class<?> _Class, Bundle args) {
        TabInfo info = new TabInfo(_Class, args);
        mTabs.add(info);
    }

    @Override
    public int getCount() {
        return mTabs == null ? 0 : mTabs.size();
    }

    public TabInfo getTabInfo(int position) {
        TabInfo info = mTabs.get(position);
        if (info == null) {
            Log.w(TAG, "getTabInfo tabInfo is null");
            return null;
        }

        return info;
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Object tag = tab.getTag();
        for (int i = 0; i < mTabs.size(); i++) {
            if (mTabs.get(i) == tag) {
                mViewPager.setCurrentItem(i);
            }
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
