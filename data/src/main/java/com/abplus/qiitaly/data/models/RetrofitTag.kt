package com.abplus.qiitaly.data.models

import com.abplus.qiitaly.domain.models.Url
import com.abplus.qiitaly.domain.models.Tag
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitTag(
    val id: String,
    val icon_url: String,
    val items_count: Int,
    val followers_count: Int
) {

    fun toTag(): Tag = Tag(
        id = Tag.ID(id),
        iconUrl = Url(icon_url),
        counts = Tag.Counts(
            items = items_count,
            followers = followers_count
        )
    )
}