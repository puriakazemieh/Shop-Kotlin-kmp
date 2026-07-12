package com.kazemieh.orders.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.common.util.formatDateTime
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.domain.order.Order
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    navigateBack: () -> Unit,
    navigateToDetail: (Long) -> Unit
) {
    val viewModel = koinViewModel<OrderListViewModel>()
    val state by viewModel.state.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OrderListEffect.NavigateToDetail -> navigateToDetail(effect.orderId)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.ManageOrders),
                        fontFamily = AppFont(),
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackDesc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state.ordersState) {
                is AppResult.Loading -> LoadingCard(Modifier.fillMaxSize())
                is AppResult.Error -> InfoCard(
                    title = stringResource(Resources.String.Oops),
                    subtitle = result.message,
                    image = Resources.Image.Cat
                )
                is AppResult.Success -> {
                    val orders = result.data
                    if (orders.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(Resources.String.NoOrdersFound), fontFamily = AppFont())
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().responsiveMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = { viewModel.handleIntent(OrderListIntent.OnOrderClick(order.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Resources.String.OrderIdPrefix, order.id),
                fontWeight = FontWeight.Bold,
                fontSize = FontSize.REGULAR,
                fontFamily = AppFont(),
                color = colors.onSurface
            )
            UserStatusBadge(status = order.status)
        }
        Spacer(modifier = Modifier.height(13.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 56.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(13.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = formatDateTime(order.createdAt),
                fontSize = FontSize.SMALL,
                fontFamily = AppFont(),
                color = colors.onSurfaceVariant
            )
            Text(
                text = stringResource(Resources.String.PriceFormat, formatToman(order.totalPrice)),
                fontWeight = FontWeight.ExtraBold,
                fontSize = FontSize.EXTRA_REGULAR,
                color = colors.onSurface,
                fontFamily = AppFont()
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
        Spacer(modifier = Modifier.height(13.dp))
        Text(
            text = "جزئیات و رهگیری",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(colors.accentSoft)
                .clickable { onClick() }
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            fontFamily = AppFont()
        )
    }
}

@Composable
fun UserStatusBadge(status: String) {
    val color = when (status.uppercase()) {
        "PLACED" -> AppTheme.colors.star
        "PROCESSING" -> AppTheme.colors.accent2
        "SHIPPING" -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> AppTheme.colors.ok
        "CANCELLED" -> AppTheme.colors.sale
        else -> MaterialTheme.colorScheme.outline
    }
    val label = when (status.uppercase()) {
        "PLACED" -> stringResource(Resources.String.OrderStatusPlaced)
        "PROCESSING" -> stringResource(Resources.String.OrderStatusProcessing)
        "SHIPPING" -> stringResource(Resources.String.OrderStatusShipping)
        "COMPLETED" -> stringResource(Resources.String.OrderStatusCompleted)
        "CANCELLED" -> stringResource(Resources.String.OrderStatusCancelled)
        else -> status
    }
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(color)
            .padding(horizontal = 11.dp, vertical = 4.dp),
        fontSize = FontSize.EXTRA_SMALL,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontFamily = AppFont()
    )
}
