package com.abplus.qiitaly.app;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.support.v4.widget.DrawerLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.Backend;
import com.abplus.qiitaly.app.api.models.Tag;
import com.abplus.qiitaly.app.api.models.User;
import com.abplus.qiitaly.app.utils.Dialogs;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.viewpagerindicator.TitlePageIndicator;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class MainActivity extends FragmentActivity implements NavigationDrawerFragment.Callback {
    @InjectView(R.id.indicator)
    TitlePageIndicator indicator;
    @InjectView(R.id.pager)
    ViewPager pager;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.navigation_drawer)
    View fragmentContainer;

    private static final String CURRENT_SCREEN = "CURRENT_SCREEN";

    private enum Screen {
        Home,
        Users,
        Tags
    }

    private NavigationDrawerFragment drawer;
    private Screen currentScreen = Screen.Home;
    private ListPagerAdapter[] adapters = new ListPagerAdapter[Screen.values().length];
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        drawer = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        pager.setAdapter(new ListPagerAdapter(getSupportFragmentManager()));
        indicator.setViewPager(pager);

        Backend.sharedInstance().restoreAuth(this);

        initImageLoader();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (! Backend.sharedInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (Backend.sharedInstance().getCurrent() == null) {
            setupCurrentUser();
        } else {
            setupPages();
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(CURRENT_SCREEN, currentScreen.ordinal());
        editor.commit();
    }


    @Override
    public void onItemSelected(int position) {
        currentScreen = Screen.values()[position];

        switch (currentScreen) {
            case Users:
                currentScreen = Screen.Users;
                if (adapters[currentScreen.ordinal()] == null) {
                    adapters[currentScreen.ordinal()] = ListPagerAdapter.forUsers(this, getSupportFragmentManager());
                }
                break;
            case Tags:
                currentScreen = Screen.Tags;
                if (adapters[currentScreen.ordinal()] == null) {
                    adapters[currentScreen.ordinal()] = ListPagerAdapter.forTags(this, getSupportFragmentManager());
                }
                break;
            default:
                currentScreen = Screen.Home;
                if (adapters[currentScreen.ordinal()] == null) {
                    adapters[currentScreen.ordinal()] = ListPagerAdapter.forHome(this, getSupportFragmentManager());
                }
                break;
        }
        pager.setAdapter(adapters[currentScreen.ordinal()]);
        drawerLayout.closeDrawer(fragmentContainer);
    }

    @Override
    public void onLogout() {
        //todo logout
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupPages() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        drawer.selectItem(preferences.getInt(CURRENT_SCREEN, 0));
    }

    private void setupCurrentUser() {
        final ProgressDialog dialog = Dialogs.startLoading(this);

        Backend.sharedInstance().user(new ApiCallback<User>(getActivity(), dialog) {
            @Override
            public void onSuccess(final User user) {
                Backend.sharedInstance().usersFollowingUsers(user.getUrlName(), new ApiCallback<List<User>>(getActivity(), dialog) {
                    @Override
                    public void onSuccess(List<User> users) {
                        user.addFollowingUsers(users);

                        Backend.sharedInstance().usersFollowingTags(user.getUrlName(), new ApiCallback<List<Tag>>(getActivity(), dialog) {
                            @Override
                            public void onSuccess(List<Tag> tags) {
                                user.addFollowingTags(tags);

                                dialog.dismiss();
                                setupPages();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private Activity getActivity() {
        return this;
    }

    private static abstract class ApiCallback<T> implements Backend.Callback<T> {

        private ProgressDialog dialog;
        private Context context;

        ApiCallback(Context context, ProgressDialog dialog) {
            this.dialog = dialog;
            this.context = context;
        }

        @Override
        public void onException(Throwable throwable) {
            throwable.printStackTrace();
            dialog.dismiss();
            Dialogs.errorMessage(context, R.string.err_response, throwable.getLocalizedMessage());
        }

        @Override
        public void onError(String errorReason) {
            dialog.dismiss();
            Dialogs.errorMessage(context, R.string.err_response, errorReason);
        }
    }

    private static final int MEMORY_CACHE_SIZE = 8 * 1024 * 1024;

    private void initImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCache(new LruMemoryCache(MEMORY_CACHE_SIZE))
                .memoryCacheSize(MEMORY_CACHE_SIZE)
                .diskCacheFileCount(200)
                .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
