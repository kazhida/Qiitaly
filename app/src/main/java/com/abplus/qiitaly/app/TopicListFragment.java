package com.abplus.qiitaly.app;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

/**
 * 投稿リストのフラグメント
 *
 * Created by kazhida on 2014/08/03.
 */
public class TopicListFragment extends ListFragment {

    private static final String LIST_SOURCE = "LIST_SOURCE";
    private static final int LIST_SOURCE_WHATS_NEW = 1;
    private static final int LIST_SOURCE_STOCKS    = 2;
    private static final int LIST_SOURCE_USER      = 3;
    private static final int LIST_SOURCE_TAG       = 4;
    private static final int LIST_SOURCE_SEARCH    = 5;

    private static final String URL_NAME = "URL_NAME";
    private static final String TAG      = "TAG";
    private static final String QUERY    = "QUERY";

    private Bundle sourceBundle(int source, String key, String value) {
        Bundle bundle = new Bundle();
        bundle.putInt(LIST_SOURCE, source);
        if (key != null) {
            bundle.putString(key, value);
        }
        return bundle;
    }

    private Bundle sourceBundle(int source) {
        return sourceBundle(source, null, null);
    }

    public TopicListFragment forWhatsNew() {
        setArguments(sourceBundle(LIST_SOURCE_WHATS_NEW));
        return this;
    }

    public TopicListFragment forStocks() {
        setArguments(sourceBundle(LIST_SOURCE_STOCKS));
        return this;
    }

    public TopicListFragment forUser(String urlName) {
        setArguments(sourceBundle(LIST_SOURCE_USER, URL_NAME, urlName));
        return this;
    }

    public TopicListFragment forTag(String tag) {
        setArguments(sourceBundle(LIST_SOURCE_TAG, TAG, tag));
        return this;
    }

    public TopicListFragment forSearch(String query) {
        setArguments(sourceBundle(LIST_SOURCE_SEARCH, QUERY, query));
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListAdapter(new TopicListAdapter());
    }
}
