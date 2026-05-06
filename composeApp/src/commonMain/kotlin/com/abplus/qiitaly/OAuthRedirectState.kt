package com.abplus.qiitaly

import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OAuthRedirectPayload(
    val redirectUrl: String,
    val code: String?,
    val state: String?,
)

interface OAuthRedirectPayloadSource {
    val payload: StateFlow<OAuthRedirectPayload?>
}

object OAuthRedirectState : OAuthRedirectPayloadSource {
    private val _payload = MutableStateFlow<OAuthRedirectPayload?>(null)
    override val payload: StateFlow<OAuthRedirectPayload?> = _payload.asStateFlow()

    fun clear() {
        _payload.value = null
    }

    fun handleRedirectUrl(url: String) {
        val parsed = Url(url)
        if (parsed.protocol.name != "qiitare" || parsed.host != "oauth" || parsed.encodedPath != "/callback") {
            return
        }

        _payload.value = OAuthRedirectPayload(
            redirectUrl = url,
            code = parsed.parameters["code"],
            state = parsed.parameters["state"],
        )
    }
}
