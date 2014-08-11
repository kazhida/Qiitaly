package com.abplus.qiitaly.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
public class TopicActivity extends Activity {

    public static final String UUID = "UUID";

    public static Intent startIntent(Context context, Item item) {
        Intent intent = new Intent(context, TopicActivity.class);
        intent.putExtra(UUID, item.getUuid());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        String uuid = getIntent().getStringExtra(UUID);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, TopicFragment.newInstance(uuid))
                    .commit();
        }
    }

    public static class TopicFragment extends Fragment {
        @InjectView(R.id.content_text)
        TextView contentText;

        static TopicFragment newInstance(@NotNull String uuid) {
            TopicFragment fragment = new TopicFragment();

            Bundle bundle = new Bundle();
            bundle.putString(UUID, uuid);
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_topic, container, false);
            ButterKnife.inject(this, view);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final Context context = getActivity();

            Backend.sharedInstance().item(getArguments().getString(UUID), new Backend.Callback<Item>() {
                @Override
                public void onSuccess(Item result) {
                    contentText.setText(result.getBody());
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                    Dialogs.errorMessage(context, R.string.err_response, throwable.getLocalizedMessage());
                }

                @Override
                public void onError(String errorReason) {
                    Dialogs.errorMessage(context, R.string.err_response, errorReason);
                }
            });
        }
    }
}
