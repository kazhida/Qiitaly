package com.abplus.qiitaly.app.utils;

import com.abplus.qiitaly.app.api.models.Item;
import com.abplus.qiitaly.app.api.models.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 投稿表示用のHTMLを生成するユーティリティ
 *
 * Created by kazhida on 2014/08/12.
 */
public class HtmlBuilder {

    private Item item;

    public HtmlBuilder(@Nullable Item item) {
        this.item = item;
    }

    private String header() {
        return "<html><head><meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"></head>";
    }

    private String footer() {
        return "</body></html>";
    }

    private String articleHeader(@NotNull Item item, @Nullable User user) {
        StringBuilder builder = new StringBuilder();

        builder.append("<header class=\"article-header\"><div class=\"container\">");
        if (user != null) {
            builder.append("<img src=\"").append(user.getProfileImageUrl()).append("\">");
        }
        builder.append("<div>");
        builder.append("<h1 class=\"item-title\">").append(item.getTitle()).append("</h1>");
        if (user != null) {
            builder.append(user.getUrlName()).append("が").append(item.getCreatedAtInWords()).append("前に投稿");
        }
        builder.append("</div></header>");

        return builder.toString();
    }

    public String build() {
        StringBuilder builder = new StringBuilder();

        builder.append(header());

        if (item != null) {
            builder.append(articleHeader(item, item.getUser()));
            builder.append("<div class=\"container\">");
            builder.append(item.getBody());
            builder.append("</div>");
        }

        builder.append(footer());

        return builder.toString();
    }
}
