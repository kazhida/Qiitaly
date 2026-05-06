package com.abplus.qiitaly.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Description
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.abplus.qiitaly.data.models.AuthenticatedUser
import com.abplus.qiitaly.data.models.Article
import com.abplus.qiitaly.ui.components.ItemList
import com.abplus.qiitaly.ui.components.ProfileHeader
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import qiitaly.composeapp.generated.resources.Res

const val QUERY_STOCKED = "stocked"
const val QUERY_OWNERS = "owners"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    articleFlow: StateFlow<List<Article>>,
    authenticatedUserFlow: StateFlow<AuthenticatedUser?>,
    isRefreshingFlow: StateFlow<Boolean>,
    isLoadingFlow: StateFlow<Boolean>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onShowFolloweeItems: () -> Unit,
    onShowFollowingTagItems: () -> Unit,
    onShowQueryItems: (String) -> Unit,
    onShowStockItems: (userId: String) -> Unit,
    onShowOwnItems: (userId: String) -> Unit,
    onLicensesClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var queryText by remember { mutableStateOf("") }

    fun closeDrawer() {
        keyboardController?.hide()
        scope.launch {
            drawerState.close()
        }
    }

    fun showQueryItems() {
        onShowQueryItems(queryText)
        closeDrawer()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f),
                drawerShape = RectangleShape,
            ) {
                ProfileHeader(authenticatedUserFlow = authenticatedUserFlow)
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Query") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = ::showQueryItems) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            showQueryItems()
                        },
                    ),
                )
                NavigationDrawerItem(
                    label = { Text("自分の投稿") },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        onShowOwnItems(authenticatedUserFlow.value?.id ?: "")
                        closeDrawer()
                    },
                )

                NavigationDrawerItem(
                    label = { Text("フォローしているユーザの投稿") },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PeopleAlt,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        onShowFolloweeItems()
                        closeDrawer()
                    },
                )
                NavigationDrawerItem(
                    label = { Text("フォローしているタグの投稿") },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        onShowFollowingTagItems()
                        closeDrawer()
                    },
                )
                NavigationDrawerItem(
                    label = { Text("ストックしている投稿") },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Bookmarks,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        onShowStockItems(authenticatedUserFlow.value?.id ?: "")
                        closeDrawer()
                    },
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("ライセンス情報") },
                    selected = false,
                    onClick = {
                        onLicensesClick()
                        closeDrawer()
                    },
                )
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = Res.getUri("files/bg.jpg"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "きいたり",
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF2E7D32),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Open drawer",
                                )
                            }
                        },
                    )
                },
            ) { paddingValues ->
                ItemList(
                    articleFlow = articleFlow,
                    isRefreshingFlow = isRefreshingFlow,
                    isLoadingFlow = isLoadingFlow,
                    modifier = Modifier.padding(paddingValues).padding(16.dp),
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore,
                    onArticleClick = onArticleClick,
                )
            }
        }
    }
}
