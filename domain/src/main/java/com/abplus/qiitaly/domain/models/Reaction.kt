package com.abplus.qiitaly.domain.models

class Reaction(
    val name: String,
    val imageUrl: Url.ImageUrl,
    val user: User,
    val timeStamp: TimeStamp
)
