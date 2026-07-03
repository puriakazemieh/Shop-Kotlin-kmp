package com.kazemieh.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.domain.catalog.CreateReviewRequest
import com.kazemieh.domain.catalog.GetReviewsUseCase
import com.kazemieh.domain.catalog.PostReviewUseCase
import com.kazemieh.domain.catalog.Review
import com.kazemieh.domain.catalog.ToggleReviewHelpfulUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.runtime.rememberCoroutineScope

/**
 * بخشِ نظرات، بازاستفاده‌شده در هر جایی که یک محصولِ لینک‌شده دارد (صفحه‌ی محصول، دوره، خدمتِ مشاوره).
 * از همان ProductReviewEntity/endpointِ سرور استفاده می‌کند؛ فقط عنوان per زمینه فرق می‌کند
 * (مثلاً «نظرِ شاگردان» برای دوره، «نظرِ مراجعان» برای مشاوره). چون سیستمِ نظرات کاملاً
 * محصول‌محور است، این بخش فقط وقتی قابلِ‌نمایش است که آیتم به یک محصول لینک شده باشد.
 */
@Composable
fun ProductReviewsSection(
    productId: Long,
    title: String = "دیدگاه خریداران"
) {
    val colors = AppTheme.colors
    val getReviewsUseCase = koinInject<GetReviewsUseCase>()
    val postReviewUseCase = koinInject<PostReviewUseCase>()
    val toggleHelpfulUseCase = koinInject<ToggleReviewHelpfulUseCase>()
    val scope = rememberCoroutineScope()

    var isLoading by remember(productId) { mutableStateOf(true) }
    var reviews by remember(productId) { mutableStateOf<List<Review>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        isLoading = true
        when (val result = getReviewsUseCase(productId)) {
            is AppResult.Success -> { reviews = result.data; errorMessage = null }
            is AppResult.Error -> errorMessage = result.message.toString()
            else -> {}
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$title (${reviews.size})", fontSize = FontSize.MEDIUM, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
            Text(
                "ثبتِ نظر",
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(colors.primary)
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(12.dp))
        when {
            isLoading -> Box(Modifier.fillMaxWidth().padding(16.dp)) { CircularProgressIndicator(color = colors.primary) }
            reviews.isEmpty() -> Text("هنوز نظری ثبت نشده. اولین نفر باشید.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            else -> reviews.forEach { review ->
                ReviewItem(
                    review = review,
                    onReplyClick = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onHelpfulClick = { reviewId ->
                        scope.launch {
                            when (val result = toggleHelpfulUseCase(reviewId)) {
                                is AppResult.Success -> reviews = reviews.map { if (it.id == reviewId) result.data else it }
                                else -> {}
                            }
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddReviewDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { rating, comment ->
                scope.launch {
                    when (val result = postReviewUseCase(CreateReviewRequest(productId = productId, rating = rating, comment = comment))) {
                        is AppResult.Success -> {
                            reviews = listOf(result.data) + reviews
                            showAddDialog = false
                        }
                        else -> {}
                    }
                }
            }
        )
    }
}
