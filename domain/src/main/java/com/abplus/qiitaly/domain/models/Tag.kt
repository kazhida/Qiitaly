package com.abplus.qiitaly.domain.models

public data class Tag(
    val id: ID,
    val iconUrl: Url,
    val counts: Counts
) {
    data class ID(val id: String)

    data class Counts(
        val followers: Int,
        val items: Int
    )
}