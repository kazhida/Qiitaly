package com.abplus.qiitaly.domain.models

public data class Comment(
    val id: ID,
    val user: User,
    val body: Body,
    val timeStamp: TimeStamp
) {
    public data class ID(val id: String)
}