package com.abplus.qiitaly.data.models

import com.abplus.qiitaly.domain.models.Team
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitTeam(
    val id: String,
    val name: String,
    val active: Boolean
) {

    fun toTeam() = Team(
        id = Team.ID(id),
        name = name,
        active = active
    )
}