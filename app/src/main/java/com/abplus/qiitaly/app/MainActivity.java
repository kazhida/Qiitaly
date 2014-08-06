package com.abplus.qiitaly.app;


import android.app.ActionBar;
import android.app.ProgressDialog;
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
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.viewpagerindicator.TitlePageIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    @InjectView(R.id.indicator)
    TitlePageIndicator indicator;
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
    private ListPagerAdapter adapter;

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

        adapter = new ListPagerAdapter();
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);

        Backend.sharedInstance().restoreAuth(this);

        initImageLoader();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (! Backend.sharedInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (Backend.sharedInstance().getCurrent() != null) {
            setupPages();
        } else {
            final ProgressDialog dialog = Dialogs.startLoading(this);
            Backend.sharedInstance().user(new Backend.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    dialog.dismiss();
                    setupPages();
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                    dialog.dismiss();
                    Dialogs.errorMessage(MainActivity.this, R.string.err_login, throwable.getLocalizedMessage());
                }

                @Override
                public void onError(String errorReason) {
                    dialog.dismiss();
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
        adapter.resetForHome();
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
            return fragments.get(position).getTitle();
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        void resetForHome() {
            fragments.add(new TopicListFragment().forWhatsNew(getString(R.string.home_whats_new)));
            fragments.add(new TopicListFragment().forStocks(getString(R.string.home_stocks)));
            fragments.add(new TopicListFragment().forUser(getString(R.string.home_self_topic), Backend.sharedInstance().getUrlName()));
            notifyDataSetChanged();
        }
    }

    private void initImageLoader() {
        File cacheDir = StorageUtils.getCacheDirectory(this);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisc()
                .build();

        ImageLoader.getInstance().init(
                new ImageLoaderConfiguration.Builder(this)
                        .discCache(new UnlimitedDiscCache(cacheDir))
                        .defaultDisplayImageOptions(options)
                        .build()
        );
    }

//    private class HomePagerAdapter extends ListPagerAdapter {
//
//        HomePagerAdapter() {
//            super();
//            fragments.add(new TopicListFragment().forWhatsNew());
//            fragments.add(new TopicListFragment().forStocks());
//            fragments.add(new TopicListFragment().forUser(Backend.sharedInstance().getUrlName()));
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return getString(R.string.home_whats_new);
//                case 1:
//                    return getString(R.string.home_stocks);
//                case 2:
//                    return getString(R.string.home_self_topic);
//                default:
//                    return null;
//            }
//        }
//    }
}
