package com.abplus.qiitaly.data.models

import com.abplus.qiitaly.domain.models.Group
import com.abplus.qiitaly.domain.models.TimeStamp
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitGroup(
    val id: Long,
    val name: String,
    val url_name: String,
    @Json(name = "private")
    val privateGroup: Boolean,
    val created_at: String,
    val updated_at:String
) {
    fun toGroup() = Group(
        id = Group.ID(id),
        name = name,
        urlName = url_name,
        privateGroup = privateGroup,
        timeStamp = TimeStamp.from(
            createdAt = created_at,
            updatedAt = updated_at
        )
    )
}
