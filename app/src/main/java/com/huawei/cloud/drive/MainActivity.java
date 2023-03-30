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

package com.huawei.cloud.drive;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.huawei.cloud.drive.adapter.TabsAdapter;
import com.huawei.cloud.drive.fragment.InterfaceFragment;
import com.huawei.cloud.drive.log.Logger;
import com.huawei.cloud.drive.model.TabInfo;
import com.huawei.cloud.drive.utils.ViewUtil;
import com.huawei.cloud.drive.view.FileViewPager;
import com.huawei.cloud.drive.R;

/**
 * Main Activity
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    public FileViewPager mViewPager;

    public TabsAdapter mTabsAdapter;

    private Toolbar mHwToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initActionBar();
        initMainView();
    }

    /**
     * Init ActionBar
     */
    protected void initActionBar() {
        mHwToolbar = ViewUtil.findViewById(this, R.id.hwtoolbar);
    }

    /**
     * Init main view
     */
    private void initMainView() {
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(1);

        initTabsAdapter();
        setEnableScroll(false);
    }

    /**
     * Init tabs adapter
     */
    private void initTabsAdapter() {
        mTabsAdapter = new TabsAdapter(this, mViewPager, getFragmentManager());
        mTabsAdapter.addTab(InterfaceFragment.class, null);
        mTabsAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (mViewPager == null) {
            Logger.i(TAG, "onOptionsItemSelected viewpager is null");
            return false;
        }
        if (mTabsAdapter == null) {
            Logger.i(TAG, "onOptionsItemSelected mTabsAdapter is null");
            return false;
        }
        int currentItem = mViewPager.getCurrentItem();
        TabInfo tabInfo = mTabsAdapter.getTabInfo(currentItem);
        Fragment fragment = tabInfo.getFragment();

        if (fragment == null) {
            Logger.i(TAG, "onOptionsItemSelected fragment is null");
            return false;
        }

        return true;
    }

    /**
     * Set enableScroll
     */
    public void setEnableScroll(boolean enableScroll) {
        if (mViewPager != null) {
            mViewPager.setEnableScroll(enableScroll);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public Toolbar getToolbar() {
        return mHwToolbar;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mTabsAdapter == null || null == mViewPager) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
