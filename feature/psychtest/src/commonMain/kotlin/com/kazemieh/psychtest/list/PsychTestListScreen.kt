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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.domain.psychtest.PsychTestSummary
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
    var tab by remember { mutableStateOf(if (embedded) 1 else 0) } // 0 = فروشگاه، 1 = تست‌های من

    LaunchedEffect(Unit) { viewModel.load() }

    if (embedded) {
        PsychTestListBody(
            state = state, tab = tab, onTabChange = { tab = it },
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
            state = state, tab = tab, onTabChange = { tab = it },
            navigateToProduct = navigateToProduct, navigateToTakeTest = navigateToTakeTest,
            colors = colors, modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}

@Composable
private fun PsychTestListBody(
    state: PsychTestListState,
    tab: Int,
    onTabChange: (Int) -> Unit,
    navigateToProduct: (String) -> Unit,
    navigateToTakeTest: (Long) -> Unit,
    colors: com.kazemieh.designsystem.AppColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabChip("فروشگاهِ تست", tab == 0) { onTabChange(0) }
            TabChip("تست‌های من", tab == 1) { onTabChange(1) }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                tab == 0 -> LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.tests.size) { idx ->
                        ShopTestCard(state.tests[idx], onBuy = { slug -> navigateToProduct(slug) })
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.myTests.isEmpty()) {
                        item {
                            Text(
                                "هنوز تستی نخریده‌اید. از فروشگاهِ تست خرید کنید.",
                                color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR,
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    }
                    items(state.myTests.size) { idx ->
                        MyTestCard(state.myTests[idx], onTake = { navigateToTakeTest(it) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TabChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label,
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.SemiBold,
        color = if (active) colors.onPrimary else colors.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(if (active) colors.primary else colors.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
        Text(test.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        if (!test.description.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(test.description!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, maxLines = 2)
        }
        Spacer(Modifier.height(8.dp))
        Text("${test.questionCount} سؤال", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        Spacer(Modifier.height(12.dp))
        if (test.owned) {
            Text("خریداری‌شده — از «تست‌های من» انجام دهید", color = colors.ok, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold)
        } else {
            val productId = test.productId
            Text(
                if (productId != null) "خریدِ تست" else "برای خرید به‌زودی",
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.button))
                    .background(if (productId != null) colors.primary else colors.line)
                    .clickable(enabled = productId != null) { productId?.let { onBuy(it.toString()) } }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
            )
        }
    }
}

@Composable
private fun MyTestCard(userTest: UserPsychTest, onTake: (Long) -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(userTest.testTitle, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR, modifier = Modifier.weight(1f))
            Text(
                userTest.status.label,
                fontSize = FontSize.EXTRA_SMALL,
                color = colors.primary,
                modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(colors.accentSoft).padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        if (userTest.status == UserTestStatus.COMPLETED && !userTest.interpretation.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text("نتیجه:", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL)
            Spacer(Modifier.height(4.dp))
            Text(userTest.interpretation!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
        if (userTest.status == UserTestStatus.PURCHASED || userTest.status == UserTestStatus.IN_PROGRESS) {
            Spacer(Modifier.height(12.dp))
            Text(
                "انجامِ تست",
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.button))
                    .background(colors.primary)
                    .clickable { onTake(userTest.id) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
            )
        }
    }
}
