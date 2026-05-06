package com.abplus.qiitaly.data.repositories

import com.abplus.qiitaly.data.models.Article
import com.abplus.qiitaly.data.models.AuthenticatedUser
import com.abplus.qiitaly.data.models.FollowingTag
import com.abplus.qiitaly.data.models.User
import com.abplus.qiitaly.ui.screens.QUERY_OWNERS
import com.abplus.qiitaly.ui.screens.QUERY_STOCKED
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class QiitaRepository(
    private val httpClient: HttpClient = defaultHttpClient(),
    private val baseUrl: String = BASE_URL,
) {

    suspend fun getItems(page: Int = 1, perPage: Int = 20, query: String? = null): List<Article> {
        validatePaging(page, perPage)

        // 特別なクエリ
        query?.split(";")?.let { queryParts ->
            val queryType = queryParts.firstOrNull()
            val userId = queryParts.getOrNull(1)
            if (queryType != null && userId != null) {
                return when (queryType) {
                    QUERY_STOCKED -> getStockedItems(userId, page, perPage)
                    QUERY_OWNERS -> getOwnedItems(userId, page, perPage)
                    else -> error("Invalid query type: $query")
                }
            }
        }

        return getPagedJson("items", "items", page, perPage) {
            if (!query.isNullOrBlank()) {
                parameter("query", query)
            }
        }
    }

    suspend fun getStockedItems(userId: String, page: Int = 1, perPage: Int = 20): List<Article> {
        validatePaging(page, perPage)
        return getUserPagedJson(userId, "stocks", "stocks", page, perPage)
    }

    suspend fun getOwnedItems(userId: String, page: Int = 1, perPage: Int = 20): List<Article> {
        validatePaging(page, perPage)
        return getUserPagedJson(userId, "items", "items", page, perPage)
    }

    suspend fun getAuthenticatedUser(accessToken: String): AuthenticatedUser {
        require(accessToken.isNotBlank()) { "accessToken must not be blank." }

        val response = httpClient.get("$baseUrl/authenticated_user") {
            accept(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        if (response.status.value !in 200..299) {
            val body = runCatching { response.body<String>() }.getOrDefault("")
            if (response.status.isAuthenticationRecoveryStatus()) {
                throw InvalidAccessTokenException(body)
            }
            throw IllegalStateException(
                "Qiita authenticated user request failed: ${response.status.value} ${response.status.description}. $body"
                    .trim()
            )
        }

        return response.body<AuthenticatedUser>()
    }

    suspend fun getFollowees(userId: String, page: Int = 1, perPage: Int = 100): List<User> {
        require(userId.isNotBlank()) { "userId must not be blank." }
        validatePaging(page, perPage)
        return getUserPagedJson(userId, "followees", "followees", page, perPage)
    }

    suspend fun getFollowingTags(userId: String, page: Int = 1, perPage: Int = 100): List<FollowingTag> {
        require(userId.isNotBlank()) { "userId must not be blank." }
        validatePaging(page, perPage)
        return getUserPagedJson(userId, "following_tags", "following tags", page, perPage)
    }

    private suspend inline fun <reified T> getPagedJson(
        path: String,
        requestName: String,
        page: Int,
        perPage: Int,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): T {
        val response = httpClient.get("$baseUrl/$path") {
            accept(ContentType.Application.Json)
            parameter("page", page)
            parameter("per_page", perPage)
            block()
        }
        response.throwIfFailed(requestName)
        return response.body()
    }

    private suspend inline fun <reified T> getUserPagedJson(
        userId: String,
        resourcePath: String,
        requestName: String,
        page: Int,
        perPage: Int,
    ): T = getPagedJson("users/$userId/$resourcePath", requestName, page, perPage)

    private fun validatePaging(page: Int, perPage: Int) {
        require(page in 1..100) { "page must be between 1 and 100." }
        require(perPage in 1..100) { "perPage must be between 1 and 100." }
    }

    private suspend fun HttpResponse.throwIfFailed(requestName: String) {
        if (status.value in 200..299) return

        val body = runCatching { body<String>() }.getOrDefault("")
        if (status.isAuthenticationRecoveryStatus()) {
            throw InvalidAccessTokenException(body)
        }
        throw IllegalStateException(
            "Qiita $requestName request failed: ${status.value} ${status.description}. $body"
                .trim()
        )
    }

    private fun HttpStatusCode.isAuthenticationRecoveryStatus(): Boolean {
        return this == HttpStatusCode.Unauthorized || this == HttpStatusCode.Forbidden
    }

    companion object {
        private const val BASE_URL = "https://qiita.com/api/v2"

        private fun defaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        }
    }

    class InvalidAccessTokenException(responseBody: String) : IllegalStateException(
        "Qiita access token is invalid. $responseBody".trim()
    )
}
