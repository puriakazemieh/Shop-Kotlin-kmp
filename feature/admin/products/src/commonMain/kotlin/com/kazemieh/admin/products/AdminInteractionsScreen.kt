package com.kazemieh.admin.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import com.kazemieh.designsystem.component.CarmillaFilterChip
import com.kazemieh.designsystem.responsiveMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.admin.AdminInteraction
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInteractionsScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<AdminInteractionsViewModel>()
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت نظرات و پرسش‌ها") },
                navigationIcon = {
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.handleIntent(AdminInteractionsIntent.SelectTab(0)) },
                    text = { Text("نظرات") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.handleIntent(AdminInteractionsIntent.SelectTab(1)) },
                    text = { Text("پرسش‌ها") }
                )
            }

            FilterSection(
                isNewFilter = state.isNewFilter,
                onNewFilterChange = { viewModel.handleIntent(AdminInteractionsIntent.SetNewFilter(it)) }
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val list = if (state.selectedTab == 0) state.reviews else state.questions
                LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list) { interaction ->
                        AdminInteractionItem(interaction)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    isNewFilter: Boolean?,
    onNewFilterChange: (Boolean?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.FilterList, contentDescription = null)
        CarmillaFilterChip(
            text = "همه",
            selected = isNewFilter == null,
            onClick = { onNewFilterChange(null) }
        )
        CarmillaFilterChip(
            text = "جدید",
            selected = isNewFilter == true,
            onClick = { onNewFilterChange(true) }
        )
        CarmillaFilterChip(
            text = "بررسی شده",
            selected = isNewFilter == false,
            onClick = { onNewFilterChange(false) }
        )
    }
}

@Composable
fun AdminInteractionItem(interaction: AdminInteraction) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, if (interaction.isNew) colors.primary else colors.line, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = interaction.userName.take(1).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primary,
                    fontSize = FontSize.SMALL
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = interaction.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = FontSize.SMALL,
                        color = colors.onSurface
                    )
                    if (interaction.isNew) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "جدید",
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(colors.primary)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = FontSize.EXTRA_SMALL,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = interaction.createdAt,
                    fontSize = FontSize.EXTRA_SMALL,
                    color = colors.onSurfaceVariant
                )
            }
        }

        Text(
            text = "محصول: ${interaction.productTitle}",
            fontSize = FontSize.EXTRA_SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            modifier = Modifier.padding(top = 10.dp)
        )

        interaction.rating?.let {
            Row(modifier = Modifier.padding(top = 6.dp)) {
                repeat(5) { index ->
                    Text(
                        text = if (index < it) "★" else "☆",
                        color = if (index < it) colors.star else colors.onSurfaceVariant,
                        fontSize = FontSize.REGULAR
                    )
                }
            }
        }

        Text(
            text = interaction.content,
            modifier = Modifier.padding(top = 8.dp),
            fontSize = FontSize.SMALL,
            color = colors.onSurface
        )
    }
}
