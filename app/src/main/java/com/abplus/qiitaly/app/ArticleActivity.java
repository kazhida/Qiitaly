package com.abplus.qiitaly.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.webkit.WebView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.Backend;
import com.abplus.qiitaly.app.api.models.Item;
import com.abplus.qiitaly.app.utils.Dialogs;
import com.abplus.qiitaly.app.utils.HtmlBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 投稿を表示するアクティビティ
 *
 * Created by kazhida on 2014/08/11.
 */
public class ArticleActivity extends Activity {

    public static final String UUID = "UUID";

    public static Intent startIntent(Context context, Item item) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(UUID,     item.getUuid());
        return intent;
    }

    private String uuid;
    private Item.Cache itemCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        uuid = getIntent().getStringExtra(UUID);
        itemCache = Item.Cache.getHolder().get(uuid);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ArticleFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article, menu);
        menu.findItem(R.id.action_stock).setEnabled(! itemCache.isStocked());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_stock:
                stock(itemCache.getUuid());
                return true;
            case R.id.action_browse:
                openUrl(itemCache.getUrl());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openUrl(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void stock(String uuid) {
        final ProgressDialog dialog = Dialogs.startLoading(this);
        Backend.sharedInstance().stock(uuid, new Backend.Callback<String>() {
            @Override
            public void onSuccess(String result, @Nullable String nextUrl) {
                dialog.dismiss();
            }

            @Override
            public void onException(Throwable throwable) {
                dialog.dismiss();
                throwable.printStackTrace();
                Dialogs.errorMessage(ArticleActivity.this, R.string.err_response, throwable.getLocalizedMessage());
            }

            @Override
            public void onError(String errorReason) {
                dialog.dismiss();
                Dialogs.errorMessage(ArticleActivity.this, R.string.err_response, errorReason);
            }
        });
    }

    @SuppressLint("ValidFragment")
    public class ArticleFragment extends Fragment {
        @InjectView(R.id.web_view)
        WebView webView;

        @Override
        public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_article, container, false);
            ButterKnife.inject(this, view);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (itemCache != null) {
                HtmlBuilder builder = new HtmlBuilder(itemCache);
                webView.loadDataWithBaseURL(null, builder.build(), "text/html", "UTF-8", null);
            }

            Backend.sharedInstance().item(uuid, new Backend.Callback<Item>() {

                @Override
                public void onSuccess(Item result, @Nullable String nextUrl) {
                    HtmlBuilder builder = new HtmlBuilder(result);
                    webView.loadDataWithBaseURL(null, builder.build(), "text/html", "UTF-8", null);
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                    Dialogs.errorMessage(ArticleActivity.this, R.string.err_response, throwable.getLocalizedMessage());
                }

                @Override
                public void onError(String errorReason) {
                    Dialogs.errorMessage(ArticleActivity.this, R.string.err_response, errorReason);
                }
            });
        }
    }
}
