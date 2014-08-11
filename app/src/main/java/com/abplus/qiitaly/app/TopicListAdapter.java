package com.abplus.qiitaly.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 投稿リストのアダプタ
 *
 * Created by kazhida on 2014/08/05.
 */
public class TopicListAdapter extends BaseAdapter {

    protected List<Item> items;
    protected List<Item> topics = new ArrayList<>();
    protected LayoutInflater inflater;
    protected Context context;
    protected String title;

    TopicListAdapter(Activity activity) {
        super();
        inflater = activity.getLayoutInflater();
        context = activity;
    }

    @Override
    public int getCount() {
        return topics.size();
    }

    @Override
    public Object getItem(int position) {
        return topics.get(position);
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

        Item item = topics.get(position);

        holder.titleText.setText(item.getTitle());
        holder.stockCount.setText(context.getString(R.string.stoked, item.getStockCount()));

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
        this.items = items;
        topics.clear();
        topics.addAll(items);
        notifyDataSetChanged();
    }

    private String updatedDescription(Item item) {
        return context.getString(R.string.updated, item.getUser().getUrlName(), item.getUpdatedAtInWords());
    }

    private String postedDescription(Item item) {
        return context.getString(R.string.posted, item.getUser().getUrlName(), item.getCreatedAtInWords());
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

    private abstract class CommonItemsCallback implements Backend.Callback<List<Item>> {
        @Override
        public void onException(Throwable throwable) {
            throwable.printStackTrace();
            Dialogs.errorMessage(context, R.string.err_response, throwable.getLocalizedMessage());
        }

        @Override
        public void onError(String errorReason) {
            Dialogs.errorMessage(context, R.string.err_response, errorReason);
        }
    }

    public static class ForWhatsNew extends TopicListAdapter {

        ForWhatsNew(Activity activity) {
            super(activity);

            title = activity.getString(R.string.home_whats_new);

            Backend.sharedInstance().items(false, new CommonItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                }
            });
        }
    }

    public static class ForContributes extends TopicListAdapter {

        ForContributes(Activity activity) {
            super(activity);

            title = activity.getString(R.string.home_self_topic);

            Backend.sharedInstance().items(true, new CommonItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                }
            });
        }
    }

    public static class ForStocks extends TopicListAdapter {

        ForStocks(Activity activity) {
            super(activity);

            title = activity.getString(R.string.home_stocks);

            Backend.sharedInstance().stocks(new CommonItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                }
            });
        }
    }

    public static class ByUser extends TopicListAdapter {

        ByUser(Activity activity, User user) {
            super(activity);

            title = user.getName();

            Backend.sharedInstance().userItems(user.getUrlName(), new CommonItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                }
            });
        }
    }

    public static class ByTag extends TopicListAdapter {

        ByTag(Activity activity, Tag tag) {
            super(activity);

            title = tag.getName();

            Backend.sharedInstance().tagItems(tag.getUrlName(), new CommonItemsCallback() {
                @Override
                public void onSuccess(List<Item> items) {
                    initItems(items);
                }
            });
        }
    }
//
//    public static class BySearch extends TopicListAdapter {
//
//        BySearch(Activity activity, String query) {
//            super(activity);
//
//            title = query;
//
//            Backend.sharedInstance().search(query, new CommonItemsCallback() {
//                @Override
//                public void onSuccess(List<Item> items) {
//                    initItems(items);
//                }
//            });
//        }
//    }
}
