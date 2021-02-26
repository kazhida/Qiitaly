package com.abplus.qiitaly.data.models

import com.abplus.qiitaly.domain.models.*
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitItem(
    val id: String,
    val title: String,
    val body: String,
    val rendered_body: String,
    val url: String,
    val tags: List<RetrofitTag>,
    val user: RetrofitUser,
    val group: RetrofitGroup,
    val coediting: Boolean,
    @Json(name = "private")
    val privateItem: Boolean,
    val comments_count: Int,
    val likes_count: Int,
    val reactions_count: Int,
    val page_views_count: Int?,
    val created_at: String,
    val updated_at: String
) {
    fun toArticle() = Article(
        id = Article.ID(id),
        title = title,
        body = Body(
            markdown = body,
            html = rendered_body
        ),
        url = Url(url),
        tags = tags.map { it.toTag() },
        user = user.toUser(),
        group = group.toGroup(),
        coediting = coediting,
        counts = Article.Counts(
            comments = comments_count,
            likes = likes_count,
            reactions = reactions_count,
            pageViews = page_views_count
        ),
        privateItem = privateItem,
        timeStamp = TimeStamp.from(
            createdAt = created_at,
            updatedAt = updated_at
        )
    )
}