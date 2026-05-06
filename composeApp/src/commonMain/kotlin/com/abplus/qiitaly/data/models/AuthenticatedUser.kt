package com.abplus.qiitaly.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticatedUser(
    val description: String?,
    // 自己紹介文 Example: "Hello, world."
    val facebook_id: String?,
    // Facebook ID Example: "qiita"
    val followees_count: Int = 0,
    // このユーザーがフォローしているユーザーの数   Example: 100
    val followers_count: Int = 0,
    // このユーザーをフォローしているユーザーの数 Example: 200
    val github_login_name: String?,
    // GitHub ID Example: "qiitan"
    val id: String,
    // ユーザーID Example: "qiita"
    val items_count: Int = 0,
    // このユーザーが qiita.com 上で公開している記事の数 (Qiita Teamでの記事数は含まれません)  Example: 300
    val linkedin_id: String?,
    // LinkedIn ID Example: "qiita"
    val location: String?,
    // 居住地 Example: "Tokyo, Japan"
    val name: String?,
    // 設定している名前 Example: "Qiita キータ"
    val organization: String?,
    // 所属している組織 Example: "Qiita Inc."
    val permanent_id: Int = 0,
    // ユーザーごとに割り当てられる整数のID  Example: 1
    val profile_image_url: String,
    // 設定しているプロフィール画像のURL Example: "https://s3-ap-northeast-1.amazonaws.com/qiita-image-store/0/88/ccf90b557a406157dbb9d2d7e543dae384dbb561/large.png?1575443439"
    val team_only: Boolean = false,
    // Qiita Team専用モードに設定されているかどうか Example: false
    val twitter_screen_name: String?,
    // Twitterのスクリーンネーム Example: "qiita"
    val website_url: String?,
    // 設定しているWebサイトのURL Example: "https://qiita.com"
    val image_monthly_upload_limit: Int = 0,
    // 1ヶ月あたりにQiitaにアップロードできる画像の総容量 Example: 1048576
    val image_monthly_upload_remaining: Int = 0,
    // その月にQiitaにアップロードできる画像の残りの容量 Example: 524288
)
