package com.abplus.qiitaly.domain.models

public data class Team(
    val id: ID,
    val name: String,
    val active: Boolean
) {
    public data class ID(val id: String)
}