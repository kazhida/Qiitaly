package com.abplus.qiitaly.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.jetbrains.annotations.NotNull;

/**
 * 投稿リストのフラグメント
 *
 * Created by kazhida on 2014/08/03.
 */
public class TopicListFragment extends Fragment {
    @InjectView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.list_view)
    ListView listView;

    private static final String LIST_SOURCE = "LIST_SOURCE";
    private static final int LIST_SOURCE_WHATS_NEW = 1;
    private static final int LIST_SOURCE_STOCKS    = 2;
    private static final int LIST_SOURCE_USER      = 3;
    private static final int LIST_SOURCE_TAG       = 4;
    private static final int LIST_SOURCE_SEARCH    = 5;

    private static final String TITLE    = "TITLE";
    private static final String URL_NAME = "URL_NAME";
    private static final String TAG      = "TAG";
    private static final String QUERY    = "QUERY";

    private Bundle sourceBundle(String title, int source, String key, String value) {
        Bundle bundle = new Bundle();

        bundle.putString(TITLE, title);
        bundle.putInt(LIST_SOURCE, source);

        if (key != null) {
            bundle.putString(key, value);
        }
        return bundle;
    }

    private Bundle sourceBundle(String title, int source) {
        return sourceBundle(title, source, null, null);
    }

    public TopicListFragment forWhatsNew(String title) {
        setArguments(sourceBundle(title, LIST_SOURCE_WHATS_NEW));
        return this;
    }

    public TopicListFragment forStocks(String title) {
        setArguments(sourceBundle(title, LIST_SOURCE_STOCKS));
        return this;
    }

    public TopicListFragment byUser(String title, String urlName) {
        setArguments(sourceBundle(title, LIST_SOURCE_USER, URL_NAME, urlName));
        return this;
    }

    @SuppressWarnings("unused")
    public TopicListFragment byTag(String title, String tag) {
        setArguments(sourceBundle(title, LIST_SOURCE_TAG, TAG, tag));
        return this;
    }

    @SuppressWarnings("unused")
    public TopicListFragment bySearch(String query) {
        setArguments(sourceBundle(query, LIST_SOURCE_SEARCH, QUERY, query));
        return this;
    }

    public String title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_list, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        title = getArguments().getString(TITLE);

        if (listView.getAdapter() == null) {
            switch (getArguments().getInt(LIST_SOURCE)) {
                case LIST_SOURCE_WHATS_NEW:
                    listView.setAdapter(new TopicListAdapter.ForWhatsNew(getActivity()));
                    break;
                case LIST_SOURCE_STOCKS:
                    listView.setAdapter(new TopicListAdapter.ForStocks(getActivity()));
                    break;
                case LIST_SOURCE_USER:
                    listView.setAdapter(new TopicListAdapter.ByUser(getActivity(), getArguments().getString(URL_NAME)));
                    break;
                case LIST_SOURCE_TAG:
                    listView.setAdapter(new TopicListAdapter.ByTag(getActivity(), getArguments().getString(TAG)));
                    break;
                case LIST_SOURCE_SEARCH:
                    listView.setAdapter(new TopicListAdapter.BySearch(getActivity(), getArguments().getString(QUERY)));
                    break;
                default:
                    //  こないはず
                    listView.setAdapter(new TopicListAdapter(getActivity()));
                    break;
            }
        }

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
