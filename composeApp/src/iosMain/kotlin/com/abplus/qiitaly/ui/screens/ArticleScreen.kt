package com.abplus.qiitaly.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitView
import com.abplus.qiitaly.data.models.Article
import com.abplus.qiitaly.utils.HtmlBuilder
import platform.Foundation.NSURL
import platform.WebKit.WKWebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    article: Article,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val html = remember(article.id, article.updated_at, article.rendered_body) {
        HtmlBuilder(article).build()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "きいたり",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
    ) { innerPadding ->
        UIKitView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = {
                WKWebView().apply {
                    allowsBackForwardNavigationGestures = true
                }
            },
            update = { webView ->
                webView.loadHTMLString(
                    string = html,
                    baseURL = NSURL.URLWithString(article.url),
                )
            },
        )
    }
}
