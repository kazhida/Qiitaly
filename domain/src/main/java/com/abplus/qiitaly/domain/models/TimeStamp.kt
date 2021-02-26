package com.abplus.qiitaly.domain.models

public data class TimeStamp(
    val createdAt: Long,
    val updatedAt: Long
) {

    companion object {

        public fun from(createdAt: String, updatedAt: String): TimeStamp {

        }
    }
}
