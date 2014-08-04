package com.abplus.qiitaly.app;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.Backend;
import com.abplus.qiitaly.app.api.models.User;
import com.abplus.qiitaly.app.utils.Dialogs;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    @InjectView(R.id.indicator)
    TabPageIndicator indicator;
    @InjectView(R.id.pager)
    ViewPager pager;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private enum Screen {
        Home,
        Users,
        Tags
    }

    private NavigationDrawerFragment drawer;
    private Screen currentScreen = Screen.Home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

        drawer = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        drawer.setUp(R.id.navigation_drawer, drawerLayout);

        pager.setAdapter(new ListPagerAdapter());
        indicator.setViewPager(pager);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (! Backend.sharedInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (Backend.sharedInstance().getCurrent() != null) {
            setupPages();
        } else {
            Backend.sharedInstance().user(new Backend.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    setupPages();
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                    Dialogs.errorMessage(MainActivity.this, R.string.err_login, throwable.getLocalizedMessage());
                }

                @Override
                public void onError(String errorReason) {
                    Dialogs.errorMessage(MainActivity.this, R.string.err_login, errorReason);
                }
            });
        }
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
    }

    private void setupPages() {
        ListPagerAdapter adapter = new HomePagerAdapter();
        pager.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!drawer.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ListPagerAdapter extends FragmentPagerAdapter {

        List<TopicListFragment> fragments = new ArrayList<TopicListFragment>();

        public ListPagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private class HomePagerAdapter extends ListPagerAdapter {

        HomePagerAdapter() {
            super();
            fragments.add(new TopicListFragment().forWhatsNew());
            fragments.add(new TopicListFragment().forStocks());
            fragments.add(new TopicListFragment().forUser(Backend.sharedInstance().getUrlName()));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.home_whats_new);
                case 1:
                    return getString(R.string.home_stocks);
                case 2:
                    return getString(R.string.home_self_topic);
                default:
                    return null;
            }
        }
    }
}
