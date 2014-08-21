package com.abplus.qiitaly.app.utils;

import android.util.Log;
import com.abplus.qiitaly.app.api.models.Comment;
import com.abplus.qiitaly.app.api.models.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 投稿表示用のHTMLを生成するユーティリティ
 *
 * Created by kazhida on 2014/08/12.
 */
public class HtmlBuilder {

    private Item item;

    public HtmlBuilder(@Nullable Item.Cache cache) {
        item = cache == null ? null : cache.toItem();
    }

    public HtmlBuilder(@NotNull Item item) {
        this.item = item;
    }

    private String header() {
        return  "<html><head><meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">\n" +
                "<style>\n" +
                "body {" +
                "  -webkit-text-size-adjust: 100%;" +
                "  font-size: 14px;" +
                "  line-height: 1.5;" +
                "  padding: 0px;" +
                "  margin: 0px;" +
                "  background-color: #fff;" +
                "}\n" +
                ".container {" +
                "  padding: 8px;" +
                "}\n" +
                ".article-header {" +
                "   width: 100%;" +
                "   background-color: #EEF2EA;" +
                "   padding: 0px;" +
                "   margin-top: 0px;" +
                "   margin-left: 0px;" +
                "   margin-right: 0px;" +
                "   margin-bottom: 8px;" +
                "}\n" +
                ".article-description {" +
                "  clear: left;" +
                "  color: #999999;" +
                "  font-size: 12px;" +
                "  margin-left: 8px;" +
                "  padding-bottom: 8px;" +
                "}\n" +
                "img.profile-icon {" +
                "  width: 64px;" +
                "  height: 64px;" +
                "  margin: 8px;" +
                "" +
                "}\n" +
                ".article-header h1 {" +
                "  font-size: 18px;" +
                "  font-weight: 600;" +
                "  background-color: rgba(0, 0, 0, 0);" +
                "  padding: 4px;" +
                "  line-height: 1.2;" +
                "}\n" +
                "h1 {" +
                "  font-size: 24px;" +
                "  font-weight: 600;" +
                "  border-radius: 4px;" +
                "  background-color: rgba(0, 0, 0, 0.1);" +
                "  padding: 3px 10px;" +
                "  line-height: 1.2;" +
                "}\n" +
                "h2 {" +
                "  font-size: 22px;" +
                "  border-bottom-width: 1px;" +
                "  border-bottom-style: solid;" +
                "  border-bottom-color: #D4D4D4;" +
                "  font-weight: 600;" +
                "  line-height: 1.2;" +
                "}\n" +
                "h3 {" +
                "  font-size: 18px;" +
                "  font-weight: 600;" +
                "  line-height: 1.2;" +
                "}\n" +
                "h4 {" +
                "  font-size: 16px;" +
                "  font-weight: 600;" +
                "  line-height: 1.2;" +
                "}\n" +
                ".code-frame {" +
                "  border-width: 1px;" +
                "  border-color: #D4D4D4;" +
                "  border-style: solid;" +
                "  padding: 8px;" +
                "  margin: 8px;" +
                "  line-height: 1.2;" +
                "}\n" +
                ".code-frame .code-lang {" +
                "  border-bottom-width: 1px;" +
                "  border-bottom-style: solid;" +
                "  border-bottom-color: #D4D4D4;" +
                "  font-size: 12px;" +
                "  color: #666666;" +
                "}\n" +
                ".code-frame .kd {" +
                "  color: #0000FF;" +
                "  font-weight: bold;" +
                "}\n" +
                ".code-frame .c {" +
                "  color: #CC6699;" +
                "}\n" +
                ".code-frame .cm {" +
                "  color: #CC6699;" +
                "}\n" +
                ".comment-table {" +
                "  border-spacing: 0;" +
                "}\n" +
                ".comment-row {" +
                "  padding: 8px;" +
                "}\n" +
                ".comment-contents {" +
                "  margin: 8px;" +
                "  padding: 8px;" +
                "  background-color: #EEF2EA;" +
                "}\n" +
                "img.comment-icon {" +
                "  width: 32px;" +
                "  height: 32px;" +
                "  margin: 8px;" +
                "" +
                "}\n" +
                ".comment-name {" +
                "  clear: left;" +
                "  color: #333333;" +
                "  padding: 4px;" +
                "  margin: 0;" +
                "  font-size: 12px;" +
                "}\n" +
                ".comment-body {" +
                "  clear: left;" +
                "  color: #333333;" +
                "  background-color: #ffffff;" +
                "  padding: 4px;" +
                "  margin-top: 2px;" +
                "  margin-left: 0;" +
                "  margin-right: 0;" +
                "  margin-bottom: 4px;" +
                "  font-size: 12px;" +
                "}\n" +
                "</style>" +
                "</head>";
    }

    private String footer() {
        return "</body></html>";
    }

    private String articleHeader(@NotNull Item item) {
        return  "<header class=\"article-header\">" +
                "<div><img class=\"profile-icon\" src=\"" + item.getUser().getProfileImageUrl() + "\" align=\"left\">" +
                "<h1 class=\"item-title\">" + item.getTitle() + "</h1></div>" +
                "<div><p class=\"article-description\">" + item.getUser().getUrlName() + "が" + item.getCreatedAtInWords() + "前に投稿</p></div>" +
                "</header>";
    }

    private String comment(@NotNull Comment comment) {
        return  "<tr class=\"comment-row\"><td>" +
                "<img class=\"comment-icon\" src=\"" + comment.getUser().getProfileImageUrl() + "\" align=\"left\"></td>" +
                "<td><div class=\"comment-contents\"><div class=\"comment-name\">" + comment.getUser().getUrlName() + "</div>" +
                "<div class=\"comment-body\">" + comment.getBody() + "</div></div></td>";
    }

    public String build() {
        StringBuilder builder = new StringBuilder();

        builder.append(header());

        if (item != null) {
            builder.append(articleHeader(item));
            builder.append("<div class=\"container\">");
            builder.append(item.getBody());
            builder.append("</div>");
            if (item.getComments() != null && item.getComments().size() > 0) {
                builder.append("<hr>");
                builder.append("<table class=\"comment-table\">");
                for (Comment comment: item.getComments()) {
                    builder.append(comment(comment));
                }
                builder.append("</table>");
            }
        }

        builder.append(footer());

        Log.d("HtmlBuilder", builder.toString());

        return builder.toString();
    }
}
