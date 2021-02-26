package com.abplus.qiitaly.domain.models

data class User(
    val id: ID,
    val permanentId: PermanentId,
    val profileImageUrl: Url.ImageUrl,
    val teamOnly: Boolean,
    val personalInfo: PersonalInformation,
    val socialAccounts: SocialAccounts,
    val counts: Counts
) {

    data class ID(val id: String)
    data class PermanentId(val id: Int)

    data class PersonalInformation(
        val name: String?,
        val description: String?,
        val organization: String?,
        val location: String?,
        val websiteUrl: Url?,
    )

    data class SocialAccounts(
        val facebookId: String?,
        val githubLoginName: String?,
        val linkedinId: String?,
        val twitterScreenName: String?,
    )

    data class Counts(
        val followees: Int,
        val followers: Int,
        val articles: Int
    )

//    class Current: User() {
//
//    }

}