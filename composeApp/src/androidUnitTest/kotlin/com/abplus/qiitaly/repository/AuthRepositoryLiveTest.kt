package com.abplus.qiitaly.repository

import com.abplus.qiitaly.data.repositories.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assume.assumeTrue
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryLiveTest {

    @Test
    fun authenticateAsync_exchangesActualAuthorizationCode() = runTest {
        val authorizationCode = System.getenv("QIITA_AUTH_CODE")
        assumeTrue(
            "Set QIITA_AUTH_CODE to a fresh code returned from qiitare://oauth/callback",
            !authorizationCode.isNullOrBlank()
        )

        val repository = AuthRepository(
            scope = this,
            authorizationCodeProvider = { _, request ->
                AuthCodeResult(code = authorizationCode, state = request.state)
            },
        )

        val flow = repository.authenticateAsync()

        val terminalState = awaitTerminalState(flow)
        assertTrue(
            terminalState is AuthRepository.QiitaAuthState.Authenticated,
            "Expected Authenticated but was $terminalState"
        )
    }

    private suspend fun awaitTerminalState(flow: StateFlow<AuthRepository.QiitaAuthState>): AuthRepository.QiitaAuthState {
        withTimeout(10_000.milliseconds) {
            while (true) {
                when (flow.value) {
                    is AuthRepository.QiitaAuthState.Authenticated,
                    is AuthRepository.QiitaAuthState.Failed -> return@withTimeout
                    else -> yield()
                }
            }
        }
        return flow.value
    }
}
