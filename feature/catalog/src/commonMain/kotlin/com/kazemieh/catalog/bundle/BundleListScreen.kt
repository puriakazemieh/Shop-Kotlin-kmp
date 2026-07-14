package com.kazemieh.catalog.bundle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.domain.bundle.BundleSummary
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

/** فهرستِ باندل‌ها/پکیج‌های ترکیبی — هر باندل خودش یک محصولِ واقعیِ قابلِ‌خرید است. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BundleListScreen(
    navigateBack: () -> Unit,
    navigateToBundle: (String) -> Unit
) {
    val viewModel = koinViewModel<BundleListViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("پیشنهادهای ترکیبی", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.bundles.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                state.bundles.isEmpty() -> Text(
                    "فعلاً هیچ باندلی ثبت نشده.", modifier = Modifier.align(Alignment.Center),
                    color = colors.onSurfaceVariant, fontSize = FontSize.SMALL
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.bundles) { bundle ->
                        BundleCard(bundle = bundle, onClick = { navigateToBundle(bundle.slug) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BundleCard(bundle: BundleSummary, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(Radius.sm)).background(colors.surfaceVariant)
        ) {
            if (bundle.product.thumbnailUrl != null) {
                androidx.compose.foundation.Image(
                    painter = rememberImagePainter(bundle.product.thumbnailUrl!!),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(bundle.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            Spacer(Modifier.height(3.dp))
            Text("${bundle.memberCount} محصول در این پکیج", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
            Spacer(Modifier.height(4.dp))
            val price = bundle.product.minDiscountedPrice ?: bundle.product.minPrice
            if (price != null) {
                Text("${formatToman(price)} تومان", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL)
            }
        }
    }
}
