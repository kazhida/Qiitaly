package com.abplus.qiitaly.domain.models

public data class Group(
    val id: ID,
    val name: String,
    val urlName: String,
    val privateGroup: Boolean,
    val timeStamp: TimeStamp
) {
    data class ID(val id: Long)
}