package com.abplus.qiitaly.app.utils;

import com.abplus.qiitaly.app.api.models.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 投稿表示用のHTMLを生成するユーティリティ
 *
 * Created by kazhida on 2014/08/12.
 */
public class HtmlBuilder {

    private Item.Cache item;

    public HtmlBuilder(@Nullable Item.Cache item) {
        this.item = item;
    }

    private String header() {
        return "<html><head><meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"></head>";
    }

    private String footer() {
        return "</body></html>";
    }

    private String articleHeader(@NotNull Item.Cache item) {
        return "<header class=\"article-header\"><div class=\"container\">" +
                "<img src=\"" + item.getUserProfileImageUrl() + "\">" +
                "<div>" + "<h1 class=\"item-title\">" + item.getTitle() + "</h1>" +
                item.getUserUrlName() + "が" + item.getCreatedAtInWords() + "前に投稿" +
                "</div></header>";
    }

    public String build() {
        StringBuilder builder = new StringBuilder();

        builder.append(header());

        if (item != null) {
            builder.append(articleHeader(item));
            builder.append("<div class=\"container\">");
            builder.append(item.getBody());
            builder.append("</div>");
        }

        builder.append(footer());

        return builder.toString();
    }
}
