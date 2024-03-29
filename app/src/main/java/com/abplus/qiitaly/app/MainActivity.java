package com.abplus.qiitaly.app;


import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.*;
import android.support.v4.widget.DrawerLayout;
import android.widget.SearchView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.Backend;
import com.abplus.qiitaly.app.api.models.Item;
import com.abplus.qiitaly.app.api.models.Tag;
import com.abplus.qiitaly.app.api.models.User;
import com.abplus.qiitaly.app.utils.DatabaseHelper;
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
    private static final String CURRENT_PAGE   = "CURRENT_PAGE";
    private static final int LOGIN_REQUEST_CODE = 15449;    //  適当
    private static final int MEMORY_CACHE_SIZE = 8 * 1024 * 1024;

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
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        DatabaseHelper.initInstance(this);
        DatabaseHelper.sharedInstance().executeRead(new DatabaseHelper.Reader() {
            @Override
            public void onRead(@NotNull SQLiteDatabase db) {
                Item.Cache.getHolder().load(db);
            }
        });

        drawer = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
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

    @Override
    public void onResume() {
        super.onResume();

        if (! Backend.sharedInstance().isLoggedIn()) {
            startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST_CODE, null);
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
        editor.putInt(CURRENT_PAGE,   pager.getCurrentItem());
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
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
                setTitle(R.string.title_users);
                break;
            case Tags:
                currentScreen = Screen.Tags;
                if (adapters[currentScreen.ordinal()] == null) {
                    adapters[currentScreen.ordinal()] = ListPagerAdapter.forTags(this, getSupportFragmentManager());
                }
                setTitle(R.string.title_tags);
                break;
            default:
                currentScreen = Screen.Home;
                if (adapters[currentScreen.ordinal()] == null) {
                    adapters[currentScreen.ordinal()] = ListPagerAdapter.forHome(this, getSupportFragmentManager());
                }
                setTitle(R.string.title_home);
                break;
        }
        pager.setAdapter(adapters[currentScreen.ordinal()]);
        pager.setOffscreenPageLimit(adapters[currentScreen.ordinal()].getCount() - 1);
        drawerLayout.closeDrawer(fragmentContainer);
    }

    @Override
    public void onLogout() {
        Dialogs.confirm(this, R.string.confirm, R.string.sure_logout, new Runnable() {
            @Override
            public void run() {
                Backend.sharedInstance().logout(MainActivity.this);
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOGIN_REQUEST_CODE, null);
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupPages() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        drawer.selectItem(preferences.getInt(CURRENT_SCREEN, 0));
        pager.setCurrentItem(preferences.getInt(CURRENT_PAGE, 0));
    }

    private void setupCurrentUser() {
        final ProgressDialog dialog = Dialogs.startLoading(this);

        Backend.sharedInstance().user(new ApiCallback<User>(getActivity(), dialog) {
            @Override
            public void onSuccess(final User user, String nextUrl, int rateLimitRemain) {

                Backend.sharedInstance().usersFollowingUsers(user.getUrlName(), new ApiCallback<List<User>>(getActivity(), dialog) {
                    @Override
                    public void onSuccess(List<User> users, String nextUrl, int rateLimitRemain) {
                        user.addFollowingUsers(users);

                        Backend.sharedInstance().usersFollowingTags(user.getUrlName(), new ApiCallback<List<Tag>>(getActivity(), dialog) {
                            @Override
                            public void onSuccess(List<Tag> tags, String nextUrl, int rateLimitRemain) {
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

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivity(SearchResultActivity.searchIntent(MainActivity.this, query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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
}
