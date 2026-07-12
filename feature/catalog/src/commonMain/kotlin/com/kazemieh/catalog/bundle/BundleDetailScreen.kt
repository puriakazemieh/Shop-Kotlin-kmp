package com.kazemieh.catalog.bundle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.catalog.ProductCard
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import org.koin.compose.viewmodel.koinViewModel

/** جزئیاتِ یک باندل: محصولِ خودِ باندل (با دکمه‌ی خرید که به صفحه‌ی محصول می‌رود) + اعضای پکیج (نمایشی). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BundleDetailScreen(
    slug: String,
    navigateBack: () -> Unit,
    navigateToProduct: (String) -> Unit
) {
    val viewModel = koinViewModel<BundleDetailViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(slug) { viewModel.load(slug) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(state.bundle?.title ?: "باندل", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
            val bundle = state.bundle
            when {
                state.isLoading && bundle == null -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                bundle != null -> LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ProductCard(product = bundle.product, onClick = navigateToProduct)
                        if (!bundle.description.isNullOrBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(bundle.description, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "مشاهده و خرید",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.button))
                                .background(colors.primary)
                                .clickable { navigateToProduct(bundle.product.slug) }
                                .padding(vertical = 14.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR
                        )
                        if (bundle.members.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text("این باندل شامل این محصولات است", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                        }
                    }
                    items(bundle.members) { member ->
                        ProductCard(product = member, onClick = navigateToProduct)
                    }
                }
            }
        }
    }
}
