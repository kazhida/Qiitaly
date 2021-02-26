package com.abplus.qiitaly.data.models

import com.abplus.qiitaly.domain.models.Reaction
import com.abplus.qiitaly.domain.models.TimeStamp
import com.abplus.qiitaly.domain.models.Url
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitReaction(
    val name: String,
    val image_url: String,
    val user: RetrofitUser,
    val created_at: String
) {

    fun toReaction() = Reaction(
        name = name,
        imageUrl = Url.ImageUrl(image_url),
        user = user.toUser(),
        timeStamp = TimeStamp.from(
            created_at,
            created_at
        )
    )
}
