package com.abplus.qiitaly

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.abplus.qiitaly.data.models.AuthenticatedUser
import com.abplus.qiitaly.data.models.Article
import com.abplus.qiitaly.data.repositories.AuthRepository
import com.abplus.qiitaly.data.repositories.QiitaRepository
import com.abplus.qiitaly.ui.screens.ArticleScreen
import com.abplus.qiitaly.ui.screens.LicenseScreen
import com.abplus.qiitaly.ui.screens.QUERY_OWNERS
import com.abplus.qiitaly.ui.screens.QUERY_STOCKED
import com.abplus.qiitaly.ui.screens.TimelineScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.appsupport.IosCodeAuthFlowFactory

fun MainViewController() = ComposeUIViewController { IosApp() }

private enum class Route {
    TIMELINE,
    ARTICLE,
    LICENSES,
}

@Composable
private fun IosApp() {
    val appState = remember { IosAppState() }
    var route by remember { mutableStateOf(Route.TIMELINE) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }

    LaunchedEffect(appState) {
        appState.start()
    }

    DisposableEffect(appState) {
        onDispose {
            appState.close()
        }
    }

    MaterialTheme {
        when (route) {
            Route.TIMELINE -> TimelineScreen(
                articleFlow = appState.items,
                authenticatedUserFlow = appState.authenticatedUser,
                isRefreshingFlow = appState.isRefreshingItems,
                onRefresh = appState::refreshItems,
                onLoadMore = appState::loadNextItemPage,
                onShowFolloweeItems = appState::showFolloweeItems,
                onShowFollowingTagItems = appState::showFollowingTagItems,
                onShowQueryItems = appState::showQueryItems,
                onLicensesClick = { route = Route.LICENSES },
                onArticleClick = { article ->
                    selectedArticle = article
                    route = Route.ARTICLE
                },
                onShowStockItems = appState::showStockItems,
                onShowOwnItems = appState::showOwnItems,
            )

            Route.ARTICLE -> {
                val article = selectedArticle
                if (article == null) {
                    route = Route.TIMELINE
                } else {
                    ArticleScreen(
                        article = article,
                        onBackClick = { route = Route.TIMELINE },
                    )
                }
            }

            Route.LICENSES -> LicenseScreen(
                onBackClick = { route = Route.TIMELINE },
            )
        }
    }
}

private class IosAppState(
    private val scope: CoroutineScope = MainScope(),
    private val tokenPreferences: QiitaTokenPreferences = QiitaTokenPreferences(),
    private val authRepository: AuthRepository = AuthRepository(
        codeAuthFlowFactory = IosCodeAuthFlowFactory(),
    ),
    private val qiitaRepository: QiitaRepository = QiitaRepository(),
) {
    private val _items = MutableStateFlow<List<Article>>(emptyList())
    val items = _items.asStateFlow()

    private val _authenticatedUser = MutableStateFlow<AuthenticatedUser?>(null)
    val authenticatedUser = _authenticatedUser.asStateFlow()

    private val _isRefreshingItems = MutableStateFlow(false)
    val isRefreshingItems = _isRefreshingItems.asStateFlow()

    private val _itemQuery = MutableStateFlow<String?>(null)
    private val itemQuery = _itemQuery.asStateFlow()

    private var started = false
    private var followeeItemQuery: String? = null
    private var followingTagItemQuery: String? = null
    private var nextItemPage = 1
    private var isLoadingItems = false
    private var isItemLastPage = true
    private var authenticationJob: Job? = null

    fun start() {
        if (started) return
        started = true

        observeItemQuery()

        val accessToken = tokenPreferences.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            observeAuthentication()
        } else {
            loadItems(accessToken)
        }
    }

    fun close() {
        scope.cancel()
    }

    fun showFolloweeItems() {
        _itemQuery.value = followeeItemQuery
    }

    fun showFollowingTagItems() {
        _itemQuery.value = followingTagItemQuery
    }

    fun showStockItems(userId: String) {
        _itemQuery.value = "$QUERY_STOCKED;$userId"
    }

    fun showOwnItems(userId: String) {
        _itemQuery.value = "$QUERY_OWNERS;$userId"
    }



    fun showQueryItems(query: String) {
        _itemQuery.value = query.ifBlank { null }
    }

    fun loadNextItemPage() {
        scope.launch {
            loadNextItemPage(itemQuery.value)
        }
    }

    fun refreshItems() {
        if (isLoadingItems) return

        scope.launch {
            _isRefreshingItems.value = true
            nextItemPage = 1
            isItemLastPage = false
            _items.value = emptyList()

            try {
                loadNextItemPage(itemQuery.value)
            } finally {
                _isRefreshingItems.value = false
            }
        }
    }

    private fun observeAuthentication() {
        if (authenticationJob?.isActive == true) return

        authenticationJob = scope.launch {
            val state = authRepository.authenticateAsync().first {
                it is AuthRepository.QiitaAuthState.Authenticated ||
                    it is AuthRepository.QiitaAuthState.Failed
            }
            authenticationJob = null

            if (state is AuthRepository.QiitaAuthState.Authenticated) {
                tokenPreferences.saveAccessToken(state.accessToken)
                loadItems(state.accessToken)
            }
        }
    }

    private fun observeItemQuery() {
        scope.launch {
            itemQuery.collectLatest { query ->
                nextItemPage = 1
                isLoadingItems = false
                isItemLastPage = false
                _items.value = emptyList()
                loadNextItemPage(query)
            }
        }
    }

    private fun loadItems(accessToken: String) {
        scope.launch(Dispatchers.Default) {
            runCatching {
                val authenticatedUser = qiitaRepository.getAuthenticatedUser(accessToken)
                _authenticatedUser.value = authenticatedUser
                val followeeIds = getAllFolloweeIds(authenticatedUser.id)
                val followingTagIds = getAllFollowingTagIds(authenticatedUser.id)
                followeeItemQuery = buildFolloweeItemQuery(followeeIds)
                followingTagItemQuery = buildFollowingTagItemQuery(followingTagIds)
            }.onSuccess {
                _itemQuery.value = null
            }.onFailure { error ->
                if (error is QiitaRepository.InvalidAccessTokenException) {
                    handleInvalidAccessToken(error)
                } else {
                    println("Failed to get Qiita items. ${error.message}")
                }
            }
        }
    }

    private fun handleInvalidAccessToken(error: QiitaRepository.InvalidAccessTokenException) {
        println("Stored Qiita access token is invalid. Starting authentication again. ${error.message}")
        tokenPreferences.clearAccessToken()
        _authenticatedUser.value = null
        _items.value = emptyList()
        followeeItemQuery = null
        followingTagItemQuery = null
        observeAuthentication()
    }

    private suspend fun loadNextItemPage(query: String?) {
        if (isLoadingItems || isItemLastPage) return

        isLoadingItems = true
        try {
            val newItems = qiitaRepository.getItems(
                page = nextItemPage,
                perPage = ITEMS_PER_PAGE,
                query = query,
            )
            if (itemQuery.value != query) return

            if (newItems.isEmpty()) {
                isItemLastPage = true
            } else {
                _items.value = _items.value + newItems
                nextItemPage += 1
                if (newItems.size < ITEMS_PER_PAGE) {
                    isItemLastPage = true
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            if (itemQuery.value == query) {
                if (error is QiitaRepository.InvalidAccessTokenException) {
                    handleInvalidAccessToken(error)
                } else {
                    println("Failed to get Qiita items. ${error.message}")
                }
            }
        } finally {
            if (itemQuery.value == query) {
                isLoadingItems = false
            }
        }
    }

    private suspend fun getAllFolloweeIds(userId: String): List<String> {
        val followeeIds = mutableListOf<String>()
        for (page in 1..MAX_QIITA_PAGE) {
            val followees = qiitaRepository.getFollowees(userId = userId, page = page)
            if (followees.isEmpty()) {
                break
            }
            followeeIds += followees.map { it.id }
        }
        return followeeIds
    }

    private suspend fun getAllFollowingTagIds(userId: String): List<String> {
        val followingTagIds = mutableListOf<String>()
        for (page in 1..MAX_QIITA_PAGE) {
            val followingTags = qiitaRepository.getFollowingTags(userId = userId, page = page)
            if (followingTags.isEmpty()) {
                break
            }
            followingTagIds += followingTags.map { it.id }
        }
        return followingTagIds
    }

    private fun buildFolloweeItemQuery(followeeIds: List<String>): String? {
        return followeeIds.takeIf { it.isNotEmpty() }?.joinToString(
            separator = ",",
            prefix = "user:",
        )
    }

    private fun buildFollowingTagItemQuery(followingTagIds: List<String>): String? {
        return followingTagIds.takeIf { it.isNotEmpty() }?.joinToString(
            separator = ",",
            prefix = "tag:",
        )
    }

    companion object {
        private const val MAX_QIITA_PAGE = 100
        private const val ITEMS_PER_PAGE = 20
    }
}
