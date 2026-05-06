package com.abplus.qiitaly.utils

import com.abplus.qiitaly.data.models.Article
import com.abplus.qiitaly.data.models.Tag
import com.abplus.qiitaly.data.models.User

class HtmlBuilder(
    private val article: Article?,
) {
    fun build(): String = buildString {
        append(header())

        article?.let { item ->
            append(articleHeader(item))
            append("<div class=\"container\">")
            append(item.rendered_body)
            append("</div>")
        }

        append(footer())
    }

    private fun header(): String =
        """
        <!doctype html>
        <html>
        <head>
          <meta http-equiv="content-type" content="text/html;charset=UTF-8">
          <meta name="viewport" content="width=device-width,initial-scale=1.0">
          <style>
            body {
              -webkit-text-size-adjust: 100%;
              margin: 0;
              padding: 0;
              background-color: #fffef2;
              color: #222;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Noto Sans", Helvetica, Arial, sans-serif;
              font-size: 14px;
              line-height: 1.5;
            }

            a {
              color: #2e7d32;
              overflow-wrap: anywhere;
            }

            img {
              max-width: 100%;
              height: auto;
            }

            pre {
              overflow-x: auto;
              padding: 12px;
              border-radius: 6px;
              background: #f6f8fa;
            }

            code {
              font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
              font-size: 0.92em;
            }

            blockquote {
              margin-left: 0;
              padding-left: 12px;
              border-left: 4px solid #d4d4d4;
              color: #555;
            }

            table {
              max-width: 100%;
              border-collapse: collapse;
              overflow-x: auto;
              display: block;
            }

            th,
            td {
              padding: 6px 8px;
              border: 1px solid #d4d4d4;
            }

            .container {
              padding: 8px;
            }

            .article-header {
              width: 100%;
              margin: 0 0 8px;
              padding: 4px 0 0;
              background-color: #eef2ea;
            }

            .article-title-row {
              display: flex;
              gap: 8px;
              align-items: flex-start;
              padding: 8px;
            }

            .profile-icon {
              flex: 0 0 auto;
              width: 64px;
              height: 64px;
              border-radius: 8px;
              object-fit: cover;
            }

            .item-title {
              margin: 0;
              padding: 4px;
              background-color: transparent;
              font-size: 18px;
              font-weight: 600;
              line-height: 1.2;
            }

            .article-description {
              clear: left;
              margin: 0 8px;
              padding-bottom: 8px;
              color: #777;
              font-size: 12px;
            }

            .tags {
              display: flex;
              flex-wrap: wrap;
              gap: 6px;
              margin: 0;
              padding: 0 8px 8px;
            }

            .tag {
              display: inline-flex;
              align-items: center;
              min-height: 22px;
              padding: 1px 8px;
              border-radius: 4px;
              background: #d9ded6;
              color: #334033;
              font-size: 12px;
            }

            h1 {
              padding: 3px 10px;
              border-radius: 4px;
              background-color: rgba(0, 0, 0, 0.1);
              font-size: 20px;
              font-weight: 600;
              line-height: 1.2;
            }

            h2 {
              border-bottom: 1px solid #d4d4d4;
              font-size: 18px;
              font-weight: 600;
              line-height: 1.2;
            }

            h3 {
              font-size: 16px;
              font-weight: 600;
              line-height: 1.2;
            }

            h4 {
              font-size: 14px;
              font-weight: 600;
              line-height: 1.2;
            }
          </style>
        </head>
        <body>
        """.trimIndent()

    private fun footer(): String = "</body></html>"

    private fun articleHeader(article: Article): String = buildString {
        append("<header class=\"article-header\">")
        append("<div class=\"article-title-row\">")
        append("<img class=\"profile-icon\" src=\"")
        append(article.user.profile_image_url.escapeHtmlAttribute())
        append("\" alt=\"\">")
        append("<h1 class=\"item-title\">")
        append(article.title.escapeHtml())
        append("</h1>")
        append("</div>")

        if (article.tags.isNotEmpty()) {
            append("<p class=\"tags\">")
            article.tags.forEach { tag ->
                append(tag.toHtml())
            }
            append("</p>")
        }

        append("<p class=\"article-description\">")
        append(article.user.displayName.escapeHtml())
        append(" posted at ")
        append(article.created_at.take(10).escapeHtml())
        append("</p>")
        append("</header>")
    }

    private fun Tag.toHtml(): String =
        "<span class=\"tag\">${name.escapeHtml()}</span>"

    private val User.displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: id

    private fun String.escapeHtml(): String = buildString(length) {
        for (char in this@escapeHtml) {
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(char)
            }
        }
    }

    private fun String.escapeHtmlAttribute(): String = escapeHtml()
}
