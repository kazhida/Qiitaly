package com.abplus.qiitaly.data.models

import com.abplus.qiitaly.domain.models.Url
import com.abplus.qiitaly.domain.models.User
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
internal data class RetrofitUser(
    val id: String,
    val name: String?,
    val description: String?,
    val organization: String?,
    val location: String?,
    val permanent_id: Int,
    val profile_image_url: String,
    val website_url: String?,
    val facebook_id: String?,
    val github_login_name: String?,
    val linkedin_id: String?,
    val twitter_screen_name: String?,
    val followees_count: Int,
    val followers_count: Int,
    val items_count: Int,
    val team_only: Boolean,
) {

    fun toUser() = User(
        id = User.ID(id),
        permanentId = User.PermanentId(permanent_id),
        teamOnly = team_only,
        profileImageUrl = Url.ImageUrl(profile_image_url),
        personalInfo = User.PersonalInformation(
            name = name,
            description = description,
            organization = organization,
            location = location,
            websiteUrl = website_url?.let { Url(it) },
        ),
        socialAccounts = User.SocialAccounts(
            facebookId = facebook_id,
            githubLoginName = github_login_name,
            linkedinId = linkedin_id,
            twitterScreenName = twitter_screen_name,
        ),
        counts = User.Counts(
            followees = followees_count,
            followers = followers_count,
            articles = items_count
        )

    )
}