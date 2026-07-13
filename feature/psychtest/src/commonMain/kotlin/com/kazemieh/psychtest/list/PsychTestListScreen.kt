package com.kazemieh.psychtest.list

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
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.kazemieh.domain.psychtest.TestResultMode
import com.kazemieh.domain.psychtest.UserPsychTest
import com.kazemieh.domain.psychtest.UserTestStatus
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsychTestListScreen(
    navigateBack: () -> Unit,
    navigateToProduct: (String) -> Unit,
    navigateToTakeTest: (Long) -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<PsychTestListViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) { viewModel.load() }

    if (embedded) {
        PsychTestListBody(
            state = state,
            navigateToProduct = navigateToProduct, navigateToTakeTest = navigateToTakeTest,
            colors = colors, modifier = Modifier.fillMaxWidth().height(560.dp)
        )
        return
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("تست‌های روان‌شناسی", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        PsychTestListBody(
            state = state,
            navigateToProduct = navigateToProduct, navigateToTakeTest = navigateToTakeTest,
            colors = colors, modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}

@Composable
private fun PsychTestListBody(
    state: PsychTestListState,
    navigateToProduct: (String) -> Unit,
    navigateToTakeTest: (Long) -> Unit,
    colors: com.kazemieh.designsystem.AppColors,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.myTests.isNotEmpty()) {
                    item { SectionTitle("تست‌های من") }
                    items(state.myTests.size) { idx ->
                        MyTestCard(state.myTests[idx], onTake = { navigateToTakeTest(it) })
                    }
                }
                item { SectionTitle("همه‌ی تست‌ها") }
                if (state.tests.isEmpty()) {
                    item {
                        Text(
                            "تستی برای نمایش وجود ندارد.",
                            color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                items(state.tests.size) { idx ->
                    ShopTestCard(state.tests[idx], onBuy = { slug -> navigateToProduct(slug) })
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    val colors = AppTheme.colors
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = FontSize.EXTRA_REGULAR,
        color = colors.onSurface,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
    )
}

@Composable
private fun ResultBadge(mode: TestResultMode) {
    val colors = AppTheme.colors
    val label = if (mode == TestResultMode.AUTO) "نتیجه‌ی آنی" else "تفسیر توسط مشاور"
    Text(
        label,
        fontSize = FontSize.EXTRA_SMALL,
        fontWeight = FontWeight.SemiBold,
        color = colors.primary,
        modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(colors.accentSoft).padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun ShopTestCard(test: PsychTestSummary, onBuy: (String) -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(test.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.EXTRA_REGULAR, modifier = Modifier.weight(1f))
            Spacer(Modifier.height(8.dp))
            ResultBadge(test.resultMode)
        }
        if (!test.description.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(test.description!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, maxLines = 2)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formatToman(test.price), fontWeight = FontWeight.Bold, color = colors.primary, fontSize = FontSize.REGULAR)
            val productSlug = test.productSlug
            when {
                test.owned -> Text(
                    "خریداری‌شده — در «تست‌های من»",
                    color = colors.ok, fontWeight = FontWeight.SemiBold, fontSize = FontSize.EXTRA_SMALL
                )
                productSlug != null -> Text(
                    "خرید",
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.primary)
                        .clickable { onBuy(productSlug) }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
                )
                else -> Text(
                    "به‌زودی",
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.line)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
                )
            }
        }
    }
}

@Composable
private fun MyTestCard(userTest: UserPsychTest, onTake: (Long) -> Unit) {
    val colors = AppTheme.colors
    val canStart = userTest.status == UserTestStatus.PURCHASED || userTest.status == UserTestStatus.IN_PROGRESS
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(userTest.testTitle, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.EXTRA_REGULAR)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (userTest.status == UserTestStatus.PURCHASED) "شروع‌نشده" else userTest.status.label,
                    fontSize = FontSize.EXTRA_SMALL,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(colors.surfaceVariant).padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            if (canStart) {
                Text(
                    "شروع",
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.primary)
                        .clickable { onTake(userTest.id) }
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
                )
            }
        }
        if (userTest.status == UserTestStatus.COMPLETED && !userTest.interpretation.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text("نتیجه:", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL)
            Spacer(Modifier.height(4.dp))
            Text(userTest.interpretation!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
    }
}
