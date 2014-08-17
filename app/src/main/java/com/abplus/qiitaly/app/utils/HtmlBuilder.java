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
        return  "<html><head><meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">\n" +
                "<style>\n" +
                "body {" +
                "  -webkit-text-size-adjust: 100%;" +
                "}\n" +
                ".article-header {" +
                "   background-color: #EEF2EA;" +
                "   margin-bottom: 8px;" +
                "}\n" +
                ".article-description {" +
                "  clear: left;" +
                "  color: #999999;" +
                "  padding: 4px;" +
                "  font-size: 12px;" +
                "}\n" +
                "img.profile-icon {" +
                "  width: 80px;" +
                "  height: 80px;" +
                "  margin: 8px;" +
                "" +
                "}\n" +
                ".article-header h1 {" +
                "  font-size: 18px;" +
                "  font-weight: 600;" +
                "  background-color: rgba(0, 0, 0, 0);" +
                "  padding: 4px;" +
                "}\n" +
                "h1 {" +
                "  font-size: 24px;" +
                "  font-weight: 600;" +
                "  border-radius: 4px;" +
                "  background-color: rgba(0, 0, 0, 0.1);" +
                "  padding: 3px 10px;" +
                "}\n" +
                "h2 {" +
                "  font-size: 22px;" +
                "  border-bottom-width: 1px;" +
                "  border-bottom-style: solid;" +
                "  border-bottom-color: #D4D4D4;" +
                "  font-weight: 600;" +
                "}\n" +
                "h3 {" +
                "  font-size: 18px;" +
                "  font-weight: 600;" +
                "}\n" +
                "h4 {" +
                "  font-size: 16px;" +
                "  font-weight: 600;" +
                "}\n" +
                "</style>" +
                "</head>";
    }

    private String footer() {
        return "</body></html>";
    }

    private String articleHeader(@NotNull Item.Cache item) {
        return "<header class=\"article-header\">" +
                "<div><img class=\"profile-icon\" src=\"" + item.getUserProfileImageUrl() + "\" align=\"left\">" +
                "<h1 class=\"item-title\">" + item.getTitle() + "</h1></div>" +
                "<div><p class=\"article-description\">" + item.getUserUrlName() + "が" + item.getCreatedAtInWords() + "前に投稿</p></div>" +
                "</header>";
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
