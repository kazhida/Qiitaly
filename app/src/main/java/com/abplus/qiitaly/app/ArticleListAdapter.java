package com.abplus.qiitaly.app;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.Html;
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
import com.abplus.qiitaly.app.utils.DatabaseHelper;
import com.abplus.qiitaly.app.utils.Dialogs;
import com.nostra13.universalimageloader.core.ImageLoader;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 投稿リストのアダプタ
 *
 * Created by kazhida on 2014/08/05.
 */
public class ArticleListAdapter extends BaseAdapter {

    protected List<Item> items = new ArrayList<>();
    protected LayoutInflater inflater;
    protected Activity activity;
    protected String title;
    protected boolean loading;

    ArticleListAdapter(Activity activity) {
        super();
        inflater = activity.getLayoutInflater();
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return items.size() + (loading ? 1 : 0);
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
    public View getView(final int position, View convertView, @NotNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.item_article, parent, false);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder holder = (ViewHolder) view.getTag();

        final Item item = position < items.size() ? items.get(position) : null;

        if (item != null) {
            holder.titleText.setText(Html.fromHtml(item.getTitle()));
            holder.stockCount.setText(activity.getString(R.string.stoked, item.getStockCount()));

            Item.Cache cache = Item.Cache.getHolder().get(item.getUuid());
            if (cache == null) {
                holder.newItem.setVisibility(View.GONE);
                holder.updatedItem.setVisibility(View.GONE);
            } else if (cache.isUnread()) {
                holder.newItem.setVisibility(View.VISIBLE);
                holder.updatedItem.setVisibility(View.GONE);
            } else if (cache.isUpdated()) {
                holder.newItem.setVisibility(View.GONE);
                holder.updatedItem.setVisibility(View.VISIBLE);
            } else {
                holder.newItem.setVisibility(View.GONE);
                holder.updatedItem.setVisibility(View.GONE);
            }
            if (item.getUpdatedAt().compareTo(item.getCreatedAt()) > 0) {
                holder.descriptionText.setText(updatedDescription(item));
            } else {
                holder.descriptionText.setText(postedDescription(item));
            }

            holder.tagLayout.removeAllViews();
            for (Tag tag: item.getTags()) {
                TextView textView = (TextView) inflater.inflate(R.layout.tag_text, holder.tagLayout, false);
                textView.setText(tag.getName());
                holder.tagLayout.addView(textView);
            }

            ImageLoader.getInstance().displayImage(item.getUser().getProfileImageUrl(), holder.iconImage);

            if (item.getNextUrl() == null || loading) {
                holder.moreButton.setVisibility(View.GONE);
                holder.moreButton.setOnClickListener(null);
            } else {
                holder.moreButton.setVisibility(View.VISIBLE);
                holder.moreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        readMore(position, item.getNextUrl());
                    }
                });
            }
            holder.articlePanel.setVisibility(View.VISIBLE);
            holder.loadingView.setVisibility(View.GONE);
        } else  {
            holder.articlePanel.setVisibility(View.GONE);
            holder.moreButton.setVisibility(View.GONE);
            holder.loadingView.setVisibility(View.VISIBLE);
        }


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NotNull View v) {
                if (item != null && item.isSaved()) {
                    DatabaseHelper.sharedInstance().executeWrite(new DatabaseHelper.Writer() {
                        @Override
                        public void onWrite(@NotNull SQLiteDatabase db) {
                            Item.Cache.getHolder().get(item.getUuid()).checked(db);
                        }
                    });
                    notifyDataSetChanged();
                    activity.startActivity(ArticleActivity.startIntent(activity, item));
                }
            }
        });

        return view;
    }

    private String updatedDescription(Item item) {
        return activity.getString(R.string.updated, item.getUser().getUrlName(), item.getUpdatedAtInWords());
    }

    private String postedDescription(Item item) {
        return activity.getString(R.string.posted, item.getUser().getUrlName(), item.getCreatedAtInWords());
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
        @InjectView(R.id.more_button)
        View moreButton;
        @InjectView(R.id.new_item)
        View newItem;
        @InjectView(R.id.updated_item)
        View updatedItem;
        @InjectView(R.id.loading_view)
        View loadingView;
        @InjectView(R.id.article_panel)
        View articlePanel;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    protected void load(Backend.Callback<List<Item>> callback) {
        //なにもしない
    }

    public void reload(final Runnable runnable) {
        items.clear();

        setLoading(true);

        load(new Backend.Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> result, @Nullable String nextUrl) {
                items.clear();
                items.addAll(result);
                if (items.size() > 0) {
                    items.get(items.size() - 1).setNextUrl(nextUrl);
                }
                if (runnable != null) {
                    runnable.run();
                }
                save(result);
                setLoading(false);
            }

            @Override
            public void onException(Throwable throwable) {
                throwable.printStackTrace();
                setLoading(false);
                if (runnable != null) {
                    runnable.run();
                }
                Dialogs.errorMessage(activity, R.string.err_response, throwable.getLocalizedMessage());
            }

            @Override
            public void onError(String errorReason) {
                setLoading(false);
                if (runnable != null) {
                    runnable.run();
                }
                Dialogs.errorMessage(activity, R.string.err_response, errorReason);
            }
        });
    }

    public void readMore(final int position, String nextUrl) {

        setLoading(true);

        Backend.sharedInstance().moreItems(nextUrl, new Backend.Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> result, @Nullable String nextUrl) {
                items.get(position).setNextUrl(null);

                if (position < items.size() - 1) {
                    Item next = items.get(position + 1);
                    List<Item> additional = new ArrayList<>();
                    for (Item item : result) {
                        if (item.getUuid().equals(next.getUuid())) break;
                        additional.add(item);
                    }
                    if (result.size() == additional.size() && additional.size() > 0) {
                        Item last = additional.get(additional.size() - 1);
                        last.setNextUrl(nextUrl);
                    }
                    save(additional);
                    items.addAll(position + 1, additional);
                } else {
                    if (result.size() > 0) {
                        Item last = result.get(result.size() - 1);
                        last.setNextUrl(nextUrl);
                    }
                    save(result);
                    items.addAll(result);
                }
                setLoading(false);
            }

            @Override
            public void onException(Throwable throwable) {
                throwable.printStackTrace();
                setLoading(false);
                Dialogs.errorMessage(activity, R.string.err_response, throwable.getLocalizedMessage());
            }

            @Override
            public void onError(String errorReason) {
                setLoading(false);
                Dialogs.errorMessage(activity, R.string.err_response, errorReason);
            }
        });
    }

    private void save(final List<Item> items) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(@NotNull Void... params) {
                DatabaseHelper.sharedInstance().executeWrite(new DatabaseHelper.Writer() {
                    @Override
                    public void onWrite(@NotNull SQLiteDatabase db) {
                        for (Item item: items) {
                            Item.Cache.getHolder().save(db, item);
                        }
                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(@NotNull Void result) {
                notifyDataSetChanged();
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        this.loading = loading;
        notifyDataSetChanged();
    }

    public static class ForWhatsNew extends ArticleListAdapter {

        ForWhatsNew(Activity activity) {
            super(activity);
            title = activity.getString(R.string.home_whats_new);
            reload(null);
        }

        @Override
        protected void load(Backend.Callback<List<Item>> callback) {
            Backend.sharedInstance().items(false, callback);
        }
    }

    public static class ForContributes extends ArticleListAdapter {

        ForContributes(Activity activity) {
            super(activity);
            title = activity.getString(R.string.home_self_topic);
            reload(null);
        }

        @Override
        protected void load(Backend.Callback<List<Item>> callback) {
            Backend.sharedInstance().items(true, callback);
        }
    }

    public static class ForStocks extends ArticleListAdapter {

        ForStocks(Activity activity) {
            super(activity);
            title = activity.getString(R.string.home_stocks);
            reload(null);
        }

        @Override
        protected void load(Backend.Callback<List<Item>> callback) {
            Backend.sharedInstance().stocks(callback);
        }
    }

    public static class ByUser extends ArticleListAdapter {

        private User user;

        ByUser(Activity activity, User user) {
            super(activity);
            title = user.getUrlName();
            this.user = user;
            reload(null);
        }

        @Override
        protected void load(Backend.Callback<List<Item>> callback) {
            Backend.sharedInstance().userItems(user.getUrlName(), callback);
        }
    }

    public static class ByTag extends ArticleListAdapter {

        private Tag tag;

        ByTag(Activity activity, Tag tag) {
            super(activity);
            title = tag.getName();
            this.tag = tag;
            reload(null);
        }

        @Override
        protected void load(Backend.Callback<List<Item>> callback) {
            Backend.sharedInstance().tagItems(tag.getUrlName(), callback);
        }
    }

    public static class BySearch extends ArticleListAdapter {
        @Getter
        private String query;

        BySearch(Activity activity, String query) {
            super(activity);
            title = query;
            this.query = query;
            reload(null);
        }

        public void reload(String query, final Runnable runnable) {
            this.query = query;
            reload(runnable);
        }

        @Override
        protected void load(Backend.Callback<List<Item>> callback) {
            Backend.sharedInstance().search(query, callback);
        }
    }
}
