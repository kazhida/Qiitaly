package com.abplus.qiitaly.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.Backend;
import com.abplus.qiitaly.app.api.models.Item;
import com.abplus.qiitaly.app.api.models.Tag;
import com.abplus.qiitaly.app.api.models.User;
import com.abplus.qiitaly.app.utils.Dialogs;
import com.nostra13.universalimageloader.core.ImageLoader;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 投稿リストのアダプタ
 *
 * Created by kazhida on 2014/08/05.
 */
public class TopicListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    protected List<Item> items = new ArrayList<>();
    protected LayoutInflater inflater;
    protected Activity activity;
    protected String title;

    TopicListAdapter(Activity activity) {
        super();
        inflater = activity.getLayoutInflater();
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.topic_item, parent, false);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder holder = (ViewHolder) view.getTag();

        Item item = items.get(position);

        holder.titleText.setText(item.getTitle());
        holder.stockCount.setText(activity.getString(R.string.stoked, item.getStockCount()));

        if (item.getUpdatedAt().compareTo(item.getCreatedAt()) > 0) {
            holder.descriptionText.setText(updatedDescription(item));
        } else {
            holder.descriptionText.setText(postedDescription(item));
        }

        for (Tag tag: item.getTags()) {
            addTag(holder.tagLayout, tag);
        }

        ImageLoader.getInstance().displayImage(item.getUser().getProfileImageUrl(), holder.iconImage);

        return view;
    }

    protected void initItems(List<Item> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    private String updatedDescription(Item item) {
        return activity.getString(R.string.updated, item.getUser().getUrlName(), item.getUpdatedAtInWords());
    }

    private String postedDescription(Item item) {
        return activity.getString(R.string.posted, item.getUser().getUrlName(), item.getCreatedAtInWords());
    }

    private void addTag(ViewGroup layout, Tag tag) {
        TextView textView = (TextView) inflater.inflate(R.layout.tag_text, layout, false);
        textView.setText(tag.getName());
        layout.addView(textView);
    }

    protected class ViewHolder {
        @InjectView(R.id.icon_image)
        ImageView iconImage;
        @InjectView(R.id.description_text)
        TextView descriptionText;
        @InjectView(R.id.title_text)
        TextView titleText;
        @InjectView(R.id.stock_count_text)
        TextView stockCount;
        @InjectView(R.id.tag_layout)
        ViewGroup tagLayout;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    protected void load(Runnable runnable) {
        //なにもしない
    }

    public void reload(Runnable runnable) {
        items.clear();
        load(runnable);
    }

    @Override
    public void onItemClick(@NotNull AdapterView<?> parent, @NotNull View view, int position, long id) {
        Item item = items.get(position);
        activity.startActivity(TopicActivity.startIntent(activity, item));
    }

    private abstract class ItemsCallback implements Backend.Callback<List<Item>> {
        @Override
        public void onException(Throwable throwable) {
            throwable.printStackTrace();
            Dialogs.errorMessage(activity, R.string.err_response, throwable.getLocalizedMessage());
        }

        @Override
        public void onError(String errorReason) {
            Dialogs.errorMessage(activity, R.string.err_response, errorReason);
        }
    }

    public static class ForWhatsNew extends TopicListAdapter {

        ForWhatsNew(Activity activity) {
            super(activity);
            title = activity.getString(R.string.home_whats_new);
            load(null);
        }

        @Override
        protected void load(final Runnable runnable) {
            Backend.sharedInstance().items(false, new ItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
    }

    public static class ForContributes extends TopicListAdapter {

        ForContributes(Activity activity) {
            super(activity);
            title = activity.getString(R.string.home_self_topic);
            load(null);
        }

        @Override
        protected void load(final Runnable runnable) {
            Backend.sharedInstance().items(true, new ItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
    }

    public static class ForStocks extends TopicListAdapter {

        ForStocks(Activity activity) {
            super(activity);
            title = activity.getString(R.string.home_stocks);
            load(null);
        }

        @Override
        protected void load(final Runnable runnable) {
            Backend.sharedInstance().stocks(new ItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
    }

    public static class ByUser extends TopicListAdapter {

        private User user;

        ByUser(Activity activity, User user) {
            super(activity);
            title = user.getUrlName();
            this.user = user;
            load(null);
        }

        @Override
        protected void load(final Runnable runnable) {
            Backend.sharedInstance().userItems(user.getUrlName(), new ItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
    }

    public static class ByTag extends TopicListAdapter {

        private Tag tag;

        ByTag(Activity activity, Tag tag) {
            super(activity);
            title = tag.getName();
            this.tag = tag;
            load(null);
        }

        @Override
        protected void load(final Runnable runnable) {
            Backend.sharedInstance().tagItems(tag.getUrlName(), new ItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
    }

    public static class BySearch extends TopicListAdapter {
        @Getter
        private String query;

        BySearch(Activity activity, String query) {
            super(activity);
            title = query;
            this.query = query;
            load(null);
        }

        public void reload(String query, final Runnable runnable) {
            this.query = query;
            reload(runnable);
        }

        @Override
        protected void load(final Runnable runnable) {
            Backend.sharedInstance().search(query, new ItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
    }
}
