package com.abplus.qiitaly.repository

import com.abplus.qiitaly.data.repositories.QiitaRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QiitaRepositoryTest {

    @Test
    fun getItems_requestsItemsWithPagingAndQuery() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine { request ->
                    assertEquals("https://qiita.com/api/v2/items?page=2&per_page=50&query=qiita+user%3AQiita", request.url.toString())
                    assertEquals("GET", request.method.value)
                    respond(
                        content = itemListJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val items = repository.getItems(page = 2, perPage = 50, query = "qiita user:Qiita")

        assertEquals(1, items.size)
        assertEquals("Example title", items.first().title)
        assertEquals("Ruby", items.first().tags.first().name)
        assertEquals(listOf("0.0.1"), items.first().tags.first().versions)
    }

    @Test
    fun getItems_omitsBlankQuery() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine { request ->
                    assertEquals("https://qiita.com/api/v2/items?page=1&per_page=20", request.url.toString())
                    respond(
                        content = "[]",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        assertEquals(emptyList(), repository.getItems(query = " "))
    }

    @Test
    fun getItems_throwsWhenQiitaReturnsError() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine {
                    respond(
                        content = """{"message":"Rate limit exceeded","type":"rate_limit_exceeded"}""",
                        status = HttpStatusCode.TooManyRequests,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val error = assertFailsWith<IllegalStateException> {
            repository.getItems()
        }

        assertEquals(
            """Qiita items request failed: 429 Too Many Requests. {"message":"Rate limit exceeded","type":"rate_limit_exceeded"}""",
            error.message,
        )
    }

    @Test
    fun getItems_throwsInvalidAccessTokenWhenQiitaReturnsForbidden() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine {
                    respond(
                        content = """{"message":"Forbidden","type":"forbidden"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val error = assertFailsWith<QiitaRepository.InvalidAccessTokenException> {
            repository.getItems()
        }

        assertEquals(
            """Qiita access token is invalid. {"message":"Forbidden","type":"forbidden"}""",
            error.message,
        )
    }

    @Test
    fun getItems_validatesPagingRange() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(MockEngine {
                error("Invalid paging should not call HttpClient")
            })
        )

        assertFailsWith<IllegalArgumentException> {
            repository.getItems(page = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getItems(perPage = 101)
        }
    }

    @Test
    fun getAuthenticatedUser_requestsAuthenticatedUserWithBearerToken() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine { request ->
                    assertEquals("https://qiita.com/api/v2/authenticated_user", request.url.toString())
                    assertEquals("GET", request.method.value)
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    respond(
                        content = authenticatedUserJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val user = repository.getAuthenticatedUser("access-token")

        assertEquals("qiita", user.id)
        assertEquals(1048576, user.image_monthly_upload_limit)
        assertEquals(524288, user.image_monthly_upload_remaining)
    }

    @Test
    fun getAuthenticatedUser_throwsInvalidAccessTokenWhenQiitaReturnsUnauthorized() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine {
                    respond(
                        content = """{"message":"Unauthorized","type":"unauthorized"}""",
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val error = assertFailsWith<QiitaRepository.InvalidAccessTokenException> {
            repository.getAuthenticatedUser("invalid-token")
        }

        assertEquals(
            """Qiita access token is invalid. {"message":"Unauthorized","type":"unauthorized"}""",
            error.message,
        )
    }

    @Test
    fun getAuthenticatedUser_throwsInvalidAccessTokenWhenQiitaReturnsForbidden() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine {
                    respond(
                        content = """{"message":"Forbidden","type":"forbidden"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val error = assertFailsWith<QiitaRepository.InvalidAccessTokenException> {
            repository.getAuthenticatedUser("forbidden-token")
        }

        assertEquals(
            """Qiita access token is invalid. {"message":"Forbidden","type":"forbidden"}""",
            error.message,
        )
    }

    @Test
    fun getAuthenticatedUser_throwsWhenQiitaReturnsNonUnauthorizedError() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine {
                    respond(
                        content = """{"message":"Rate limit exceeded","type":"rate_limit_exceeded"}""",
                        status = HttpStatusCode.TooManyRequests,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val error = assertFailsWith<IllegalStateException> {
            repository.getAuthenticatedUser("access-token")
        }

        assertEquals(
            """Qiita authenticated user request failed: 429 Too Many Requests. {"message":"Rate limit exceeded","type":"rate_limit_exceeded"}""",
            error.message,
        )
    }

    @Test
    fun getAuthenticatedUser_validatesAccessToken() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(MockEngine {
                error("Blank access token should not call HttpClient")
            })
        )

        assertFailsWith<IllegalArgumentException> {
            repository.getAuthenticatedUser(" ")
        }
    }

    @Test
    fun getFollowees_requestsUserFolloweesWithPaging() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine { request ->
                    assertEquals("https://qiita.com/api/v2/users/qiita/followees?page=2&per_page=50", request.url.toString())
                    assertEquals("GET", request.method.value)
                    respond(
                        content = followeesJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val followees = repository.getFollowees(userId = "qiita", page = 2, perPage = 50)

        assertEquals(listOf("followee1", "followee2"), followees.map { it.id })
    }

    @Test
    fun getFollowees_throwsWhenQiitaReturnsError() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine {
                    respond(
                        content = """{"message":"Not found","type":"not_found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val error = assertFailsWith<IllegalStateException> {
            repository.getFollowees(userId = "unknown")
        }

        assertEquals(
            """Qiita followees request failed: 404 Not Found. {"message":"Not found","type":"not_found"}""",
            error.message,
        )
    }

    @Test
    fun getFollowees_validatesParameters() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(MockEngine {
                error("Invalid followees parameters should not call HttpClient")
            })
        )

        assertFailsWith<IllegalArgumentException> {
            repository.getFollowees(userId = " ")
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getFollowees(userId = "qiita", page = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getFollowees(userId = "qiita", perPage = 101)
        }
    }

    @Test
    fun getFollowingTags_requestsUserFollowingTagsWithPaging() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine { request ->
                    assertEquals("https://qiita.com/api/v2/users/qiita/following_tags?page=2&per_page=50", request.url.toString())
                    assertEquals("GET", request.method.value)
                    respond(
                        content = followingTagsJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val followingTags = repository.getFollowingTags(userId = "qiita", page = 2, perPage = 50)

        assertEquals(listOf("Kotlin", "Compose"), followingTags.map { it.id })
    }

    @Test
    fun getFollowingTags_throwsWhenQiitaReturnsError() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(
                MockEngine {
                    respond(
                        content = """{"message":"Not found","type":"not_found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            )
        )

        val error = assertFailsWith<IllegalStateException> {
            repository.getFollowingTags(userId = "unknown")
        }

        assertEquals(
            """Qiita following tags request failed: 404 Not Found. {"message":"Not found","type":"not_found"}""",
            error.message,
        )
    }

    @Test
    fun getFollowingTags_validatesParameters() = runTest {
        val repository = QiitaRepository(
            httpClient = testHttpClient(MockEngine {
                error("Invalid following tag parameters should not call HttpClient")
            })
        )

        assertFailsWith<IllegalArgumentException> {
            repository.getFollowingTags(userId = " ")
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getFollowingTags(userId = "qiita", page = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getFollowingTags(userId = "qiita", perPage = 101)
        }
    }

    private fun testHttpClient(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    private val itemListJson = """
        [
          {
            "rendered_body": "<h1>Example</h1>",
            "body": "# Example",
            "coediting": false,
            "comments_count": 100,
            "created_at": "2000-01-01T00:00:00+00:00",
            "id": "c686397e4a0f4f11683d",
            "likes_count": 100,
            "private": false,
            "reactions_count": 100,
            "stocks_count": 100,
            "tags": [
              {
                "name": "Ruby",
                "versions": ["0.0.1"]
              }
            ],
            "title": "Example title",
            "updated_at": "2000-01-01T00:00:00+00:00",
            "url": "https://qiita.com/Qiita/items/c686397e4a0f4f11683d",
            "user": {
              "description": "Hello, world.",
              "facebook_id": "qiita",
              "followees_count": 100,
              "followers_count": 200,
              "github_login_name": "qiitan",
              "id": "qiita",
              "items_count": 300,
              "linkedin_id": "qiita",
              "location": "Tokyo, Japan",
              "name": "Qiita キータ",
              "organization": "Qiita Inc.",
              "permanent_id": 1,
              "profile_image_url": "https://example.com/image.png",
              "team_only": false,
              "twitter_screen_name": "qiita",
              "website_url": "https://qiita.com"
            },
            "page_views_count": null,
            "organization_url_name": null,
            "slide": false
          }
        ]
    """.trimIndent()

    private val authenticatedUserJson = """
        {
          "description": "Hello, world.",
          "facebook_id": "qiita",
          "followees_count": 100,
          "followers_count": 200,
          "github_login_name": "qiitan",
          "id": "qiita",
          "items_count": 300,
          "linkedin_id": "qiita",
          "location": "Tokyo, Japan",
          "name": "Qiita キータ",
          "organization": "Qiita Inc.",
          "permanent_id": 1,
          "profile_image_url": "https://example.com/image.png",
          "team_only": false,
          "twitter_screen_name": "qiita",
          "website_url": "https://qiita.com",
          "image_monthly_upload_limit": 1048576,
          "image_monthly_upload_remaining": 524288
        }
    """.trimIndent()

    private val followeesJson = """
        [
          {
            "description": "Followee 1",
            "facebook_id": null,
            "followees_count": 10,
            "followers_count": 20,
            "github_login_name": null,
            "id": "followee1",
            "items_count": 30,
            "linkedin_id": null,
            "location": null,
            "name": "Followee One",
            "organization": null,
            "permanent_id": 2,
            "profile_image_url": "https://example.com/followee1.png",
            "team_only": false,
            "twitter_screen_name": null,
            "website_url": null
          },
          {
            "description": "Followee 2",
            "facebook_id": null,
            "followees_count": 11,
            "followers_count": 21,
            "github_login_name": null,
            "id": "followee2",
            "items_count": 31,
            "linkedin_id": null,
            "location": null,
            "name": "Followee Two",
            "organization": null,
            "permanent_id": 3,
            "profile_image_url": "https://example.com/followee2.png",
            "team_only": false,
            "twitter_screen_name": null,
            "website_url": null
          }
        ]
    """.trimIndent()

    private val followingTagsJson = """
        [
          {
            "followers_count": 100,
            "icon_url": "https://example.com/kotlin.png",
            "id": "Kotlin",
            "items_count": 200
          },
          {
            "followers_count": 90,
            "icon_url": null,
            "id": "Compose",
            "items_count": 150
          }
        ]
    """.trimIndent()
}
