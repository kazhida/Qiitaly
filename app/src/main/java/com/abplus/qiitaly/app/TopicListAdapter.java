package com.abplus.qiitaly.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.models.Item;
import com.nostra13.universalimageloader.core.ImageLoader;

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

    TopicListAdapter(LayoutInflater inflater) {
        super();
        this.inflater = inflater;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.topic_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Item item = topics.get(position);

        holder.descriptionText.setText(item.getUser().getName());
        holder.titleText.setText(item.getTitle());
        holder.stockCount.setText("" + item.getStockCount());

        ImageLoader.getInstance().displayImage(item.getUser().getProfileImageUrl(), holder.iconImage);

        return view;
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

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    public static class ForWhatsNew extends TopicListAdapter {

        ForWhatsNew(LayoutInflater inflater) {
            super(inflater);
        }
    }

    public static class ForStocks extends TopicListAdapter {

        ForStocks(LayoutInflater inflater) {
            super(inflater);
        }
    }

    public static class ByUser extends TopicListAdapter {

        ByUser(LayoutInflater inflater, String urlName) {
            super(inflater);
        }
    }

    public static class ByTag extends TopicListAdapter {

        ByTag(LayoutInflater inflater, String tag) {
            super(inflater);
        }
    }

    public static class BySearch extends TopicListAdapter {

        BySearch(LayoutInflater inflater, String query) {
            super(inflater);
        }
    }
}
