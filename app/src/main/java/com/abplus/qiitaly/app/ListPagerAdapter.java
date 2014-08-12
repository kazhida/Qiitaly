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
import android.widget.ListView;
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

    private List<TopicListAdapter> adapters = new ArrayList<>();
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
        adapter.adapters.add(new TopicListAdapter.ForWhatsNew(activity));
        adapter.adapters.add(new TopicListAdapter.ForStocks(activity));
        adapter.adapters.add(new TopicListAdapter.ForContributes(activity));
        adapter.idOffset = 1L << 32;
        return adapter;
    }

    static ListPagerAdapter forUsers(Activity activity, FragmentManager manager) {
        ListPagerAdapter adapter = new ListPagerAdapter(manager);

        for (User user: Backend.sharedInstance().getCurrent().getFollowings().getUsers()) {
            adapter.adapters.add(new TopicListAdapter.ByUser(activity, user));
        }
        adapter.idOffset = 2L << 32;

        return adapter;
    }

    static ListPagerAdapter forTags(Activity activity, FragmentManager manager) {
        ListPagerAdapter adapter = new ListPagerAdapter(manager);

        for (Tag tag: Backend.sharedInstance().getCurrent().getFollowings().getTags()) {
            adapter.adapters.add(new TopicListAdapter.ByTag(activity, tag));
        }
        adapter.idOffset = 3L << 32;

        return adapter;
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

            final TopicListAdapter adapter = adapters.get(getArguments().getInt(POSITION));

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(adapter);

            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    adapter.reload(new Runnable() {
                        @Override
                        public void run() {
                            swipeLayout.setRefreshing(false);
                        }
                    });
                }
            });
        }
    }
}
