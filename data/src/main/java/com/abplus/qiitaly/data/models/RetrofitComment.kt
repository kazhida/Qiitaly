package com.abplus.qiitaly.data.models

import com.abplus.qiitaly.domain.models.Body
import com.abplus.qiitaly.domain.models.Comment
import com.abplus.qiitaly.domain.models.TimeStamp
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitComment(
    val id: String,
    val body: String,
    val rendered_body: String,
    val user: RetrofitUser,
    val created_at: String,
    val updated_at: String
) {
    fun toComment() = Comment(
        id = Comment.ID(id),
        user = user.toUser(),
        body = Body(
            markdown = body,
            html = rendered_body
        ),
        timeStamp = TimeStamp.from(
            createdAt = created_at,
            updatedAt = updated_at
        )
    )
}
