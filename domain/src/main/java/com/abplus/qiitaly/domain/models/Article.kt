package com.abplus.qiitaly.domain.models

public data class Article(
    val id: ID,
    val title: String,
    val body: Body,
    val url: Url,
    val tags: List<Tag>,
    val user: User,
    val group: Group,
    val coediting: Boolean,
    val counts: Counts,
    val privateItem: Boolean,
    val timeStamp: TimeStamp,
) {
    public data class ID(val id: String);

    public data class Counts(
        val comments: Int,
        val likes: Int,
        val reactions: Int,
        val pageViews: Int?
    )
}
