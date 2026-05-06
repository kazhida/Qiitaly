package com.abplus.qiitaly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abplus.qiitaly.data.models.Article
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemList(
    articleFlow: StateFlow<List<Article>>,
    isRefreshingFlow: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onArticleClick: (Article) -> Unit = {},
) {
    val items by articleFlow.collectAsState()
    val isRefreshing by isRefreshingFlow.collectAsState()
    val gridState = rememberLazyStaggeredGridState()

    LaunchedEffect(gridState, items.size) {
        snapshotFlow {
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.maxOfOrNull { it.index }
                ?: return@snapshotFlow false
            lastVisibleIndex >= items.lastIndex - LOAD_MORE_THRESHOLD
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                onLoadMore()
            }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
        ) {
            items(
                items = items,
                key = { item -> item.id },
            ) { item ->
                ItemItem(
                    article = item,
                    onClick = onArticleClick,
                )
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 3
