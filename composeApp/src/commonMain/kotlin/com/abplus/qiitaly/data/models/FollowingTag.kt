package com.abplus.qiitaly.data.models

import kotlinx.serialization.Serializable

@Serializable
data class FollowingTag(
    val followers_count: Int = 0,
    val icon_url: String?,
    val id: String,
    val items_count: Int = 0,
)
