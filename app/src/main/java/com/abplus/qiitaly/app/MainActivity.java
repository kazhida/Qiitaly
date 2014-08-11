package com.abplus.qiitaly.app;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.*;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    @InjectView(R.id.indicator)
    TitlePageIndicator indicator;
    @InjectView(R.id.pager)
    ViewPager pager;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private static final String CURRENT_SCREEN = "CURRENT_SCREEN";

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
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
    }

    private void setupPages() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentScreen = Screen.values()[preferences.getInt(CURRENT_SCREEN, 0)];

        switch (currentScreen) {
            case Users:
                adapter.resetForUsers();
                break;
            case Tags:
                adapter.resetForTags();
                break;
            default:
                adapter.resetForHome();
                break;
        }
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

    private Activity getActivity() {
        return this;
    }

    private static final String POSITION = "POSITION";

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

    private class ListPagerAdapter extends FragmentPagerAdapter {

        List<TopicListAdapter> adapters = new ArrayList<>();

        public ListPagerAdapter() {
            super(getSupportFragmentManager());
        }

        public TopicListAdapter getAdapter(int position) {
            return adapters.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            TopicListFragment fragment = new TopicListFragment();

            Bundle bundle = new Bundle();
            bundle.putInt(POSITION, position);
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return adapters.get(position).title;
        }

        @Override
        public int getCount() {
            return adapters.size();
        }

        void resetForHome() {
            currentScreen = Screen.Home;
            adapters.clear();
            adapters.add(new TopicListAdapter.ForWhatsNew(getActivity()));
            adapters.add(new TopicListAdapter.ForStocks(getActivity()));
            adapters.add(new TopicListAdapter.ForContributes(getActivity()));
            notifyDataSetChanged();
        }

        @SuppressWarnings("unused")
        void resetForUsers() {
            currentScreen = Screen.Users;
            adapters.clear();

            for (User user: Backend.sharedInstance().getCurrent().getFollowings().getUsers()) {
                adapters.add(new TopicListAdapter.ByUser(getActivity(), user));
            }

            notifyDataSetChanged();
        }

        @SuppressWarnings("unused")
        void resetForTags() {
            currentScreen = Screen.Tags;
            adapters.clear();

            for (Tag tag: Backend.sharedInstance().getCurrent().getFollowings().getTags()) {
                adapters.add(new TopicListAdapter.ByTag(getActivity(), tag));
            }

            notifyDataSetChanged();
        }
    }

    @SuppressLint("ValidFragment")
    public class TopicListFragment extends Fragment {
        @InjectView(R.id.swipe_layout)
        SwipeRefreshLayout swipeLayout;
        @InjectView(R.id.list_view)
        ListView listView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_topic_list, container, false);
            ButterKnife.inject(this, view);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            listView.setAdapter(adapter.getAdapter(getArguments().getInt(POSITION)));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(@NotNull AdapterView<?> adapterView, @NotNull View view, int i, long l) {
                    //todo: 詳細表示
                }
            });

            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //todo: 新規読み込み
                }
            });
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
