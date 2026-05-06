package com.abplus.qiitaly.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val rendered_body: String,
    // HTML形式の本文 Example: "<h1>Example</h1>"
    val body: String,
    // Markdown形式の本文 Example: "# Example"
    val coediting: Boolean = false,
    // この記事が共同更新状態かどうか (Qiita Teamでのみ有効) Example: false
    val comments_count: Int = 0,
    // この記事へのコメントの数 Example: 100
    val created_at: String = "2000-01-01T00:00:00+00:00",
    // データが作成された日時 Example: "2000-01-01T00:00:00+00:00"
    // val group: String?,
    // Qiita Teamのグループを表します。
    val id: String = "",
    // 記事の一意なID Example: "c686397e4a0f4f11683d" Pattern: /^[0-9a-f]{20}$/
    val likes_count: Int = 0,
    // この記事への「いいね」の数（Qiitaでのみ有効） Example: 100
    val private: Boolean = false,
    // 限定共有状態かどうかを表すフラグ (Qiita Teamでは無効) Example: false
    val reactions_count: Int = 0,
    // 絵文字リアクションの数（Qiita Teamでのみ有効） Example: 100
    val stocks_count: Int = 0,
    // この記事がストックされた数 Example: 100
    val tags: List<Tag> = emptyList(),
    // 記事に付いたタグ一覧 Example: [{"name"=>"Ruby", "versions"=>["0.0.1"]}]
    val title: String,
    // 記事のタイトル Example: "Example title"
    val updated_at: String = "2000-01-01T00:00:00+00:00",
    // データが最後に更新された日時 Example: "2000-01-01T00:00:00+00:00"
    val url: String,
    // 記事のURL Example: "https://qiita.com/Qiita/items/c686397e4a0f4f11683d"
    val user: User,
    // Qiita上のユーザーを表します。
    val page_views_count: Int? = null,
    // 閲覧数 Example: 100
//    team_membership
//    Qiita Team のチームメンバー情報を表します。
//    organization_url_name
//    記事のOrganization の url_name を表します。
//    Example: "qiita-inc"
//    Type: string, null
    val slide: Boolean = false,
    // スライドモードが有効を表すフラグ Example: false
)

@Serializable
data class Tag(
    val name: String,
    val versions: List<String> = emptyList(),
)
