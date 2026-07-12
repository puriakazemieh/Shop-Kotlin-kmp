package com.kazemieh.admin.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.CarmillaFilterChip
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.domain.admin.AdminOrderSummary
import com.kazemieh.domain.admin.AdminOrderDetail
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminOrderViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val statusUpdatedSuccessMessage = stringResource(Resources.String.StatusUpdatedSuccessfully)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminOrderEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminOrderEffect.StatusUpdated -> messageBarState.addSuccess(statusUpdatedSuccessMessage)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (!embedded) TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.ManageOrders),
                        fontFamily = AppFont(),
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                StatusFilterRow(
                    selectedStatus = state.selectedStatus,
                    onStatusSelected = { viewModel.handleIntent(AdminOrderIntent.FilterByStatus(it)) }
                )

                when (val result = state.ordersState) {
                    is AppResult.Loading -> LoadingCard(Modifier.fillMaxSize())
                    is AppResult.Error -> InfoCard(
                        title = stringResource(Resources.String.Oops),
                        subtitle = result.message,
                        image = Resources.Image.Cat
                    )
                    is AppResult.Success -> {
                        val orders = result.data.items
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
                                    AdminOrderCard(
                                        order = order,
                                        onClick = { viewModel.handleIntent(AdminOrderIntent.ShowOrderDetail(order.id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showDetailDialog) {
        OrderDetailDialog(
            state = state,
            onDismiss = { viewModel.handleIntent(AdminOrderIntent.DismissOrderDetail) },
            onUpdateStatus = { status ->
                (state.orderDetailState as? AppResult.Success)?.data?.let {
                    viewModel.handleIntent(AdminOrderIntent.UpdateStatus(it.id, status))
                }
            }
        )
    }
}

@Composable
fun StatusFilterRow(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    val statuses = listOf(null, "PLACED", "PROCESSING", "SHIPPING", "COMPLETED", "CANCELLED")
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statuses) { status ->
            CarmillaFilterChip(
                text = when (status) {
                    null -> stringResource(Resources.String.AllLabel)
                    "PLACED" -> stringResource(Resources.String.OrderStatusPlaced)
                    "PROCESSING" -> stringResource(Resources.String.OrderStatusProcessing)
                    "SHIPPING" -> stringResource(Resources.String.OrderStatusShipping)
                    "COMPLETED" -> stringResource(Resources.String.OrderStatusCompleted)
                    "CANCELLED" -> stringResource(Resources.String.OrderStatusCancelled)
                    else -> status
                },
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) }
            )
        }
    }
}

@Composable
fun AdminOrderCard(
    order: AdminOrderSummary,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Resources.String.OrderIdPrefix, order.id),
                fontWeight = FontWeight.Bold,
                fontSize = FontSize.REGULAR,
                fontFamily = AppFont(),
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${order.userEmail} · ${order.createdAt?.take(10) ?: ""}",
                fontSize = FontSize.SMALL,
                color = colors.onSurfaceVariant,
                fontFamily = AppFont(),
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(Resources.String.PriceFormat, formatToman(order.totalPrice)),
            fontWeight = FontWeight.ExtraBold,
            fontSize = FontSize.SMALL,
            color = colors.onSurface,
            fontFamily = AppFont()
        )
        Spacer(modifier = Modifier.width(10.dp))
        StatusBadge(status = order.status)
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            painter = painterResource(Resources.Icon.RightArrow),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp).graphicsLayer { rotationY = if (isRtl) 180f else 0f }
        )
    }
}

@Composable
fun StatusBadge(status: String) {
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
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = FontSize.EXTRA_SMALL,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontFamily = AppFont()
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderDetailDialog(
    state: AdminOrderState,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().responsiveMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Resources.String.OrderDetail),
                            fontFamily = AppFont(),
                            fontSize = FontSize.LARGE,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Resources.String.CloseDesc),
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
                when (val result = state.orderDetailState) {
                    is AppResult.Loading -> LoadingCard(Modifier.fillMaxSize())
                    is AppResult.Error -> InfoCard(
                        title = stringResource(Resources.String.Oops),
                        subtitle = result.message,
                        image = Resources.Image.Cat
                    )
                    is AppResult.Success -> {
                        val order = result.data
                        val colors = AppTheme.colors
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Resources.String.OrderIdPrefix, order.id),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = FontSize.REGULAR,
                                    color = colors.onSurface,
                                    fontFamily = AppFont()
                                )
                                StatusBadge(status = order.status)
                            }
                            Spacer(Modifier.height(16.dp))

                            OrderCard(title = stringResource(Resources.String.CustomerInformation)) {
                                OrderLine(stringResource(Resources.String.EmailLabelFormat, order.userEmail))
                                OrderLine(stringResource(Resources.String.UserIdLabelFormat, order.userId))
                            }
                            Spacer(Modifier.height(14.dp))

                            OrderCard(title = stringResource(Resources.String.ShippingAddress)) {
                                Text(
                                    text = "${order.addressSnapshot.receiverName} — ${order.addressSnapshot.receiverPhone}",
                                    fontFamily = AppFont(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = FontSize.SMALL,
                                    color = colors.onSurface
                                )
                                Spacer(Modifier.height(3.dp))
                                OrderLine(stringResource(Resources.String.CityProvinceFormat, order.addressSnapshot.province, order.addressSnapshot.city))
                                OrderLine(order.addressSnapshot.addressLine1)
                                order.addressSnapshot.addressLine2?.let { OrderLine(it) }
                            }
                            Spacer(Modifier.height(14.dp))

                            OrderCard(title = stringResource(Resources.String.ItemsLabel)) {
                                order.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.titleSnapshot, fontWeight = FontWeight.SemiBold, fontSize = FontSize.SMALL, color = colors.onSurface, fontFamily = AppFont())
                                            if (item.optionsSnapshot.isNotEmpty()) {
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = item.optionsSnapshot.entries.joinToString("، ") { "${it.key}: ${it.value}" },
                                                    fontSize = FontSize.EXTRA_SMALL,
                                                    color = colors.onSurfaceVariant,
                                                    fontFamily = AppFont()
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(Resources.String.QtyXPriceFormat, item.qty, stringResource(Resources.String.PriceFormat, formatToman(item.unitPriceSnapshot))),
                                            fontSize = FontSize.SMALL,
                                            color = colors.onSurface,
                                            fontFamily = AppFont()
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                                Spacer(Modifier.height(10.dp))
                                OrderSummaryRow(stringResource(Resources.String.SubtotalLabel), stringResource(Resources.String.PriceFormat, formatToman(order.subtotalPrice)), colors.onSurface)
                                Spacer(Modifier.height(7.dp))
                                OrderSummaryRow(stringResource(Resources.String.ShippingLabel), stringResource(Resources.String.PriceFormat, formatToman(order.shippingPrice)), colors.onSurface)
                                Spacer(Modifier.height(7.dp))
                                OrderSummaryRow(stringResource(Resources.String.TotalLabelSimple), stringResource(Resources.String.PriceFormat, formatToman(order.totalPrice)), colors.primary, bold = true)
                            }
                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = stringResource(Resources.String.UpdateStatus),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = FontSize.REGULAR,
                                color = colors.onSurface,
                                fontFamily = AppFont()
                            )
                            Spacer(Modifier.height(12.dp))
                            val statuses = listOf("PLACED", "PROCESSING", "SHIPPING", "COMPLETED", "CANCELLED")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                statuses.forEach { status ->
                                    val isCurrent = order.status.equals(status, ignoreCase = true)
                                    Text(
                                        text = statusLabel(status),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(11.dp))
                                            .then(
                                                if (isCurrent) Modifier.background(colors.primary)
                                                else Modifier.background(colors.surfaceVariant).border(1.dp, colors.line, RoundedCornerShape(11.dp))
                                            )
                                            .clickable(enabled = !state.isUpdatingStatus && !isCurrent) { onUpdateStatus(status) }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        fontSize = FontSize.SMALL,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) colors.onPrimary else colors.onSurfaceVariant,
                                        fontFamily = AppFont()
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

/** کارتِ بخشِ جزئیات سفارش — عنوان + محتوای کارتِ سفید با حاشیه. */
@Composable
private fun OrderCard(title: String, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = FontSize.REGULAR,
        color = colors.onSurface,
        fontFamily = AppFont(),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) { content() }
}

@Composable
private fun OrderLine(text: String) {
    Text(
        text = text,
        fontFamily = AppFont(),
        fontSize = FontSize.SMALL,
        color = AppTheme.colors.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

@Composable
private fun OrderSummaryRow(label: String, value: String, valueColor: Color, bold: Boolean = false) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = valueColor, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

@Composable
private fun statusLabel(status: String): String = when (status.uppercase()) {
    "PLACED" -> stringResource(Resources.String.OrderStatusPlaced)
    "PROCESSING" -> stringResource(Resources.String.OrderStatusProcessing)
    "SHIPPING" -> stringResource(Resources.String.OrderStatusShipping)
    "COMPLETED" -> stringResource(Resources.String.OrderStatusCompleted)
    "CANCELLED" -> stringResource(Resources.String.OrderStatusCancelled)
    else -> status
}
