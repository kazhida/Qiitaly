package com.abplus.qiitaly.data.repositories

import com.abplus.qiitaly.OAuthConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.PlatformCodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.preferences.clearOidcPreferences
import org.publicvalue.multiplatform.oidc.preferences.getResponseUri
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.validateState

class AuthRepository(
    private val httpClient: HttpClient = defaultHttpClient(),
    private val codeAuthFlowFactory: CodeAuthFlowFactory? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val authorizationCodeProvider: (suspend (OpenIdConnectClient, AuthCodeRequest) -> AuthCodeResult)? = null,
) {
    fun authenticateAsync(): StateFlow<QiitaAuthState> {
        val state = MutableStateFlow<QiitaAuthState>(
            QiitaAuthState.AwaitingAuthorization(oauthAuthorize())
        )

        scope.launch {
            val oidcClient = createOidcClient()
            val authCodePair = runCatching {
                if (authorizationCodeProvider != null) {
                    val authCodeRequest = oidcClient.createAuthorizationCodeRequest()
                    authCodeRequest to authorizationCodeProvider.invoke(oidcClient, authCodeRequest)
                } else {
                    requestAuthorizationCode(oidcClient)
                }
            }.getOrElse { error ->
                state.value = QiitaAuthState.Failed(
                    error.message ?: "Qiita authorization failed."
                )
                return@launch
            }

            val (authCodeRequest, authCodeResult) = authCodePair

            if (!authCodeRequest.validateState(authCodeResult.state ?: "")) {
                state.value = QiitaAuthState.Failed("Qiita redirect state mismatch.")
                return@launch
            }

            val code = authCodeResult.code
            if (code.isNullOrBlank()) {
                state.value = QiitaAuthState.Failed("Qiita redirect did not contain code.")
                return@launch
            }

            state.value = QiitaAuthState.ExchangingCode(code)

            runCatching {
                exchangeCodeForAccessToken(code)
            }.onSuccess { token ->
                state.value = QiitaAuthState.Authenticated(token)
            }.onFailure { error ->
                state.value = QiitaAuthState.Failed(
                    error.message ?: "Qiita authentication failed."
                )
            }
        }

        return state.asStateFlow()
    }

    fun oauthAuthorize(): String {
        return URLBuilder("$BASE_URL/oauth/authorize").apply {
            parameters.append("client_id", OAUTH_CLIENT_ID)
            parameters.append("scope", OAUTH_SCOPE)
            parameters.append("state", OAUTH_STATE)
        }.buildString()
    }

    private suspend fun requestAuthorizationCode(
        client: OpenIdConnectClient,
    ): Pair<AuthCodeRequest, AuthCodeResult> {
        val flowFactory = codeAuthFlowFactory
            ?: throw IllegalStateException("No CodeAuthFlowFactory configured.")
        val flow = flowFactory.createAuthFlow(client) as PlatformCodeAuthFlow
        val request = flow.startLogin()

        val responseUri = flow.preferences.getResponseUri()
            ?: throw IllegalStateException("Qiita redirect URI was not captured.")
        flow.preferences.clearOidcPreferences()

        return request to AuthCodeResult(
            code = responseUri.parameters["code"],
            state = responseUri.parameters["state"],
        )
    }

    private suspend fun exchangeCodeForAccessToken(code: String): String {
        val response = httpClient.post("$BASE_URL/access_tokens") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("client_id", OAUTH_CLIENT_ID)
                    put("client_secret", OAUTH_SECRET)
                    put("code", code)
                }
            )
        }

        if (response.status.value !in 200..299) {
            val body = runCatching { response.body<String>() }.getOrDefault("")
            throw IllegalStateException(
                "Qiita authentication failed: ${response.status.value} ${response.status.description}. $body"
                    .trim()
            )
        }

        val responseBody = response.body<JsonObject>()
        return responseBody["token"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("Qiita authentication response does not contain token.")
    }

    private fun createOidcClient(): OpenIdConnectClient {
        return OpenIdConnectClient {
            endpoints {
                authorizationEndpoint = "$BASE_URL/oauth/authorize"
                tokenEndpoint = "$BASE_URL/access_tokens"
            }
            clientId = OAUTH_CLIENT_ID
            clientSecret = OAUTH_SECRET
            scope = OAUTH_SCOPE
            redirectUri = OAUTH_REDIRECT_URL
            codeChallengeMethod = CodeChallengeMethod.off
            disableNonce = true
        }
    }

    sealed interface QiitaAuthState {
        data class AwaitingAuthorization(val authorizeUrl: String) : QiitaAuthState
        data class ExchangingCode(val code: String) : QiitaAuthState
        data class Authenticated(val accessToken: String) : QiitaAuthState
        data class Failed(val message: String) : QiitaAuthState
    }

    companion object {
        private const val BASE_URL = "https://qiita.com/api/v2"
        private val OAUTH_CLIENT_ID get() = OAuthConfig.clientId
        private val OAUTH_SECRET get() = OAuthConfig.clientSecret
        private const val OAUTH_SCOPE = "read_qiita"
        private const val OAUTH_STATE = ""
        private const val OAUTH_REDIRECT_URL = "qiitare://oauth/callback"

        private fun defaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    )
                }
            }
        }
    }
}
