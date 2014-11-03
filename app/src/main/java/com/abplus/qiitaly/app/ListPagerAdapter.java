package com.abplus.qiitaly.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.Backend;
import com.abplus.qiitaly.app.api.models.Tag;
import com.abplus.qiitaly.app.api.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * ページャのアダプタ
 *
 * Created by kazhida on 2014/08/11.
 */
public class ListPagerAdapter extends FragmentPagerAdapter {

    private static final String POSITION = "POSITION";

    private List<ArticleListAdapter> adapters = new ArrayList<>();
    private long idOffset = 0;

    public ListPagerAdapter(FragmentManager manager) {
        super(manager);
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

    @Override
    public long getItemId(int position) {
        return idOffset + position;
    }

    static ListPagerAdapter forHome(Activity activity, FragmentManager manager) {
        ListPagerAdapter adapter = new ListPagerAdapter(manager);
        adapter.adapters.add(new ArticleListAdapter.ForWhatsNew(activity));
        adapter.adapters.add(new ArticleListAdapter.ForStocks(activity));
        adapter.adapters.add(new ArticleListAdapter.ForContributes(activity));
        adapter.idOffset = 1L << 32;
        return adapter;
    }

    static ListPagerAdapter forUsers(Activity activity, FragmentManager manager) {
        ListPagerAdapter adapter = new ListPagerAdapter(manager);

        for (User user: Backend.sharedInstance().getCurrent().getFollowings().getUsers()) {
            adapter.adapters.add(new ArticleListAdapter.ByUser(activity, user));
        }
        adapter.idOffset = 2L << 32;

        return adapter;
    }

    static ListPagerAdapter forTags(Activity activity, FragmentManager manager) {
        ListPagerAdapter adapter = new ListPagerAdapter(manager);

        for (Tag tag: Backend.sharedInstance().getCurrent().getFollowings().getTags()) {
            adapter.adapters.add(new ArticleListAdapter.ByTag(activity, tag));
        }
        adapter.idOffset = 3L << 32;

        return adapter;
    }

    @SuppressLint("ValidFragment")
    public class TopicListFragment extends Fragment {
        @InjectView(R.id.swipe_layout)
        SwipeRefreshLayout swipeLayout;
        @InjectView(R.id.remain_text)
        TextView remainText;
        @InjectView(R.id.list_view)
        ListView listView;
        View listFooter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_article_list, container, false);
            ButterKnife.inject(this, view);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final ArticleListAdapter adapter = adapters.get(getArguments().getInt(POSITION));

            listFooter = getActivity().getLayoutInflater().inflate(R.layout.item_loading, listView, false);
            listView.setAdapter(adapter);
            listView.addFooterView(listFooter);
            listFooter.setVisibility(View.GONE);

            remainText.setText(getString(R.string.rate_limit_remain, adapter.getRateLimitRemain()));

            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    listFooter.setVisibility(View.VISIBLE);
                    adapter.reload(new Runnable() {
                        @Override
                        public void run() {
                            postRefresh(adapter.getRateLimitRemain());
                        }
                    });
                }
            });

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {}

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount == firstVisibleItem + visibleItemCount) {
                        if (listFooter.getVisibility() != View.VISIBLE) {
                            if (adapter.getNextUrl() != null && ! adapter.isEmpty()) {
                                listFooter.setVisibility(View.VISIBLE);
                                adapter.readMore(new Runnable() {
                                    @Override
                                    public void run() {
                                        postRefresh(adapter.getRateLimitRemain());
                                    }
                                });
                            }
                        }
                    }
                }
            });

            listFooter.setVisibility(View.VISIBLE);
            adapter.reload(new Runnable() {
                @Override
                public void run() {
                    postRefresh(adapter.getRateLimitRemain());
                }
            });
        }

        private void postRefresh(int rateLimitRemain) {
            swipeLayout.setRefreshing(false);
            listFooter.setVisibility(View.GONE);
            remainText.setText(getString(R.string.rate_limit_remain, rateLimitRemain));

            remainText.setVisibility(View.VISIBLE);

            if (rateLimitRemain > 0) {
                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(1500);
                animation.setFillAfter(true);
                animation.setFillEnabled(true);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        remainText.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                remainText.startAnimation(animation);
            }
        }
    }
}
