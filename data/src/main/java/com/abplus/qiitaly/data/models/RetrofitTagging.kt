package com.abplus.qiitaly.data.models

import com.abplus.qiitaly.domain.models.Tagging
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitTagging(
    val name: String,
    val versions: List<String>
) {
    fun toTagging() = Tagging(
        name = name,
        versions = versions.map { it }
    )
}