package com.abplus.qiitaly.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.utils.Dialogs;

/**
 * 検索結果のアクティビティ
 *
 * Created by kazhida on 2014/08/13.
 */
public class SearchResultActivity extends Activity {
    @InjectView(R.id.title_text)
    TextView titleText;
    @InjectView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.list_view)
    ListView listView;

    private static final String QUERY = "QUERY";
    private ArticleListAdapter.BySearch adapter;

    public static Intent searchIntent(Context context, String query) {
        Intent intent = new Intent(context, SearchResultActivity.class);
        intent.putExtra(QUERY, query);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        ButterKnife.inject(this);

        String query = getIntent().getStringExtra(QUERY);

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        adapter = new ArticleListAdapter.BySearch(this, query);

        titleText.setText(getString(R.string.search_result_for, query));
        listView.setAdapter(adapter);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQuery(adapter.getQuery(), false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                titleText.setText(getString(R.string.search_result_for, query));

                final ProgressDialog dialog = Dialogs.startLoading(SearchResultActivity.this);
                adapter.reload(query, new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

}