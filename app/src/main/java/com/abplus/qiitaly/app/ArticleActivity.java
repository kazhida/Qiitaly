package com.abplus.qiitaly.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.Backend;
import com.abplus.qiitaly.app.api.models.Item;
import com.abplus.qiitaly.app.utils.Dialogs;
import org.jetbrains.annotations.NotNull;

/**
 * 投稿を表示するアクティビティ
 *
 * Created by kazhida on 2014/08/11.
 */
public class ArticleActivity extends Activity {

    public static final String UUID = "UUID";
    public static final String URL_NAME = "URL_NAME";

    public static Intent startIntent(Context context, Item item) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(UUID,     item.getUuid());
        intent.putExtra(URL_NAME, item.getUser().getUrlName());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        String uuid = getIntent().getStringExtra(UUID);
        String urlName = getIntent().getStringExtra(URL_NAME);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ArticleFragment.newInstance(urlName, uuid))
                    .commit();
        }
    }

    public static class ArticleFragment extends Fragment {
        @InjectView(R.id.web_view)
        WebView webView;

        static ArticleFragment newInstance(@NotNull String urlName, @NotNull String uuid) {
            ArticleFragment fragment = new ArticleFragment();

            Bundle bundle = new Bundle();
            bundle.putString(URL_NAME,  urlName);
            bundle.putString(UUID,      uuid);
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_article, container, false);
            ButterKnife.inject(this, view);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final Context context = getActivity();
            final ProgressDialog dialog = Dialogs.startLoading(context);

            final String urlName = getArguments().getString(URL_NAME);
            final String uuid = getArguments().getString(UUID);

            Backend.sharedInstance().item(getArguments().getString(UUID), new Backend.Callback<Item>() {
                @Override
                public void onSuccess(Item result, String nextUrl) {
                    dialog.dismiss();
//                    HtmlBuilder builder = new HtmlBuilder(result);
//                    webView.loadDataWithBaseURL(null, builder.build(), "text/html", "UTF-8", null);
                    webView.loadUrl("http://qiita.com/" + urlName + "/items/" + uuid);
                }

                @Override
                public void onException(Throwable throwable) {
                    dialog.dismiss();
                    throwable.printStackTrace();
                    Dialogs.errorMessage(context, R.string.err_response, throwable.getLocalizedMessage());
                }

                @Override
                public void onError(String errorReason) {
                    dialog.dismiss();
                    Dialogs.errorMessage(context, R.string.err_response, errorReason);
                }
            });
        }
    }
}
