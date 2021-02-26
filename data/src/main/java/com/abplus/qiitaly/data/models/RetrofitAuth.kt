package com.abplus.qiitaly.data.models

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitAuth(
    val token: String,
    val url_name: String
) {
}