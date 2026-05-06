package com.abplus.qiitaly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abplus.qiitaly.data.models.AuthenticatedUser
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProfileHeader(
    authenticatedUserFlow: StateFlow<AuthenticatedUser?>,
    modifier: Modifier = Modifier,
) {
    val authenticatedUser by authenticatedUserFlow.collectAsState()

    Box(modifier = modifier.fillMaxWidth().background(Color.LightGray)) {
        Column(
            modifier = modifier.padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            authenticatedUser?.let { user ->
                UserIcon(
                    url = user.profile_image_url,
                    size = 56.dp,
                    contentDescription = "${user.id} profile image",
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = user.name?.takeIf { it.isNotBlank() } ?: user.id,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "@${user.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } ?: Text(
                text = "Qiitaly",
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier = Modifier.height(1.dp).background(Color.LightGray),
            )
        }
    }

}
