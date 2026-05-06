package com.abplus.qiitaly.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abplus.qiitaly.data.models.Article
import com.abplus.qiitaly.data.models.Tag

@Composable
fun ItemItem(
    article: Article,
    onClick: (Article) -> Unit = {},
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = { onClick(article) },
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserIcon(
                    article.user.profile_image_url,
                )
                Column {
                    Text(
                        text = article.user.name ?: "",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color.Gray
                    )
                    Text(
                        text = article.updated_at.substring(0, 10),
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color.Gray,
                        fontSize = 9.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.title,
                modifier = Modifier.padding(start = 8.dp).clickable { onClick(article) },
            )

            Row {
                article.tags.forEach { tag ->
                    Tag(tag.name)
                }
            }
        }
    }
}
