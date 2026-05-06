package com.abplus.qiitaly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abplus.qiitaly.data.models.AuthenticatedUser
import com.abplus.qiitaly.data.models.Article
import com.abplus.qiitaly.data.repositories.AuthRepository
import com.abplus.qiitaly.data.repositories.QiitaRepository
import com.abplus.qiitaly.ui.App
import com.abplus.qiitaly.ui.screens.ArticleScreen
import com.abplus.qiitaly.ui.screens.LicenseScreen
import com.abplus.qiitaly.ui.screens.QUERY_OWNERS
import com.abplus.qiitaly.ui.screens.QUERY_STOCKED
import com.abplus.qiitaly.ui.screens.TimelineScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private object Route {
        const val TIMELINE = "timeline"
        const val ARTICLE = "article"
        const val LICENSES = "licenses"
    }

    private val codeAuthFlowFactory = AndroidCodeAuthFlowFactory()
    private val _items = MutableStateFlow<List<Article>>(emptyList())
    private val items = _items.asStateFlow()
    private val _authenticatedUser = MutableStateFlow<AuthenticatedUser?>(null)
    private val authenticatedUser = _authenticatedUser.asStateFlow()
    private val _isRefreshingItems = MutableStateFlow(false)
    private val isRefreshingItems = _isRefreshingItems.asStateFlow()
    private val _isLoadingItems = MutableStateFlow(false)
    private val isLoadingItems = _isLoadingItems.asStateFlow()
    private val _itemQuery = MutableStateFlow<String?>(null)
    private val itemQuery = _itemQuery.asStateFlow()
    private var followeeItemQuery: String? = null
    private var followingTagItemQuery: String? = null
    private var nextItemPage = 1
    private var isLoadingNextItemPage = false
    private var loadingApiCallCount = 0
    private var isItemLastPage = true
    private var authenticationJob: Job? = null
    private lateinit var authRepository: AuthRepository
    private lateinit var qiitaRepository: QiitaRepository
    private lateinit var qiitaTokenPreferences: QiitaTokenPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        codeAuthFlowFactory.registerActivity(this)
        qiitaTokenPreferences = QiitaTokenPreferences(applicationContext)
        authRepository = AuthRepository(codeAuthFlowFactory = codeAuthFlowFactory)
        qiitaRepository = QiitaRepository()
        observeItemQuery()
        handleOAuthRedirect(intent)

        val accessToken = qiitaTokenPreferences.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            observeAuthentication()
        } else {
            loadItems(accessToken)
        }

        setContent {
            val navController = rememberNavController()
            var selectedArticle by remember { mutableStateOf<Article?>(null) }

            NavHost(
                navController = navController,
                startDestination = Route.TIMELINE,
            ) {
                composable(Route.TIMELINE) {
                    TimelineScreen(
                        articleFlow = items,
                        authenticatedUserFlow = authenticatedUser,
                        isRefreshingFlow = isRefreshingItems,
                        isLoadingFlow = isLoadingItems,
                        onRefresh = ::refreshItems,
                        onLoadMore = ::loadNextItemPage,
                        onShowFolloweeItems = ::showFolloweeItems,
                        onShowFollowingTagItems = ::showFollowingTagItems,
                        onShowQueryItems = ::showQueryItems,
                        onShowStockItems = ::showStockItems,
                        onShowOwnItems = ::showOwnItems,
                        onLicensesClick = { navController.navigate(Route.LICENSES) },
                        onArticleClick = { article ->
                            selectedArticle = article
                            navController.navigate(Route.ARTICLE)
                        },
                    )
                }
                composable(Route.ARTICLE) {
                    val article = selectedArticle
                    if (article == null) {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                        return@composable
                    }

                    ArticleScreen(
                        article = article,
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable(Route.LICENSES) {
                    LicenseScreen(
                        onBackClick = { navController.popBackStack() },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthRedirect(intent)
    }

    private fun handleOAuthRedirect(intent: Intent?) {
        val redirectUrl = intent?.dataString ?: return
        OAuthRedirectState.handleRedirectUrl(redirectUrl)
    }

    private fun observeAuthentication() {
        if (authenticationJob?.isActive == true) return

        authenticationJob = lifecycleScope.launch {
            val state = authRepository.authenticateAsync().first {
                it is AuthRepository.QiitaAuthState.Authenticated ||
                    it is AuthRepository.QiitaAuthState.Failed
            }
            authenticationJob = null

            if (state is AuthRepository.QiitaAuthState.Authenticated) {
                qiitaTokenPreferences.saveAccessToken(state.accessToken)
                loadItems(state.accessToken)
            }
        }
    }

    private fun observeItemQuery() {
        lifecycleScope.launch {
            itemQuery.collectLatest { query ->
                Log.d(TAG, "Item query changed: $query")
                nextItemPage = 1
                isLoadingNextItemPage = false
                isItemLastPage = false
                _items.value = emptyList()
                loadNextItemPage(query)
            }
        }
    }

    private fun loadItems(accessToken: String) {
        lifecycleScope.launch {
            beginApiLoading()
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
                    Log.e(TAG, "Failed to get Qiita items.", error)
                }
            }
            endApiLoading()
        }
    }

    private fun handleInvalidAccessToken(error: QiitaRepository.InvalidAccessTokenException) {
        Log.w(TAG, "Stored Qiita access token is invalid. Starting authentication again.", error)
        qiitaTokenPreferences.clearAccessToken()
        _authenticatedUser.value = null
        _items.value = emptyList()
        followeeItemQuery = null
        followingTagItemQuery = null
        observeAuthentication()
    }

    private fun showFolloweeItems() {
        _itemQuery.value = followeeItemQuery
    }

    private fun showFollowingTagItems() {
        _itemQuery.value = followingTagItemQuery
    }

    private fun showStockItems(userId: String) {
        _itemQuery.value = "$QUERY_STOCKED;$userId"
    }

    private fun showOwnItems(userId: String) {
        _itemQuery.value = "$QUERY_OWNERS;$userId"
    }

    private fun showQueryItems(query: String) {
        _itemQuery.value = query.ifBlank { null }
    }

    private fun loadNextItemPage() {
        lifecycleScope.launch {
            loadNextItemPage(itemQuery.value)
        }
    }

    private fun refreshItems() {
        if (isLoadingNextItemPage) return

        lifecycleScope.launch {
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

    private suspend fun loadNextItemPage(query: String?) {
        if (isLoadingNextItemPage || isItemLastPage) return

        isLoadingNextItemPage = true
        beginApiLoading()
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
                    Log.e(TAG, "Failed to get Qiita items.", error)
                }
            }
        } finally {
            if (itemQuery.value == query) {
                isLoadingNextItemPage = false
            }
            endApiLoading()
        }
    }

    private fun beginApiLoading() {
        loadingApiCallCount += 1
        _isLoadingItems.value = true
    }

    private fun endApiLoading() {
        loadingApiCallCount = (loadingApiCallCount - 1).coerceAtLeast(0)
        _isLoadingItems.value = loadingApiCallCount > 0
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
        private const val TAG = "MainActivity"
        private const val MAX_QIITA_PAGE = 100
        private const val ITEMS_PER_PAGE = 20
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
