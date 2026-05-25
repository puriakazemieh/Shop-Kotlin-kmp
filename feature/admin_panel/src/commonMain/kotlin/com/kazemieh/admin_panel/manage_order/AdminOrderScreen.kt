package com.kazemieh.admin_panel.manage_order

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
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.model.admin.AdminOrderSummary
import com.kazemieh.domain.model.admin.AdminOrderDetail
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderScreen(
    onBackClick: () -> Unit
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
                                modifier = Modifier.fillMaxSize(),
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
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(status ?: stringResource(Resources.String.AllLabel), fontFamily = AppFont()) }
            )
        }
    }
}

@Composable
fun AdminOrderCard(
    order: AdminOrderSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Resources.String.OrderIdPrefix, order.id),
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.MEDIUM,
                    fontFamily = AppFont()
                )
                StatusBadge(status = order.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.userEmail,
                fontSize = FontSize.SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = AppFont()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.createdAt?.take(10) ?: "",
                    fontSize = FontSize.SMALL,
                    fontFamily = AppFont()
                )
                Text(
                    text = stringResource(Resources.String.PriceFormat, order.totalPrice),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = AppFont()
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status.uppercase()) {
        "PLACED" -> Color(0xFFFFB300)
        "PROCESSING" -> Color(0xFF4CAF50)
        "SHIPPING" -> Color(0xFF2196F3)
        "COMPLETED" -> Color(0xFF8BC34A)
        "CANCELLED" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.outline
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = FontSize.EXTRA_SMALL,
            color = color,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFont()
        )
    }
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
            modifier = Modifier.fillMaxSize(),
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(stringResource(Resources.String.CustomerInformation), fontWeight = FontWeight.Bold, fontFamily = AppFont())
                            Text(stringResource(Resources.String.EmailLabelFormat, order.userEmail), fontFamily = AppFont())
                            Text(stringResource(Resources.String.UserIdLabelFormat, order.userId), fontFamily = AppFont())
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(Resources.String.ShippingAddress), fontWeight = FontWeight.Bold, fontFamily = AppFont())
                            Text(order.addressSnapshot.receiverName, fontFamily = AppFont())
                            Text(order.addressSnapshot.receiverPhone, fontFamily = AppFont())
                            Text(stringResource(Resources.String.CityProvinceFormat, order.addressSnapshot.province, order.addressSnapshot.city), fontFamily = AppFont())
                            Text(order.addressSnapshot.addressLine1, fontFamily = AppFont())
                            order.addressSnapshot.addressLine2?.let { Text(it, fontFamily = AppFont()) }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(Resources.String.ItemsLabel), fontWeight = FontWeight.Bold, fontFamily = AppFont())
                            order.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.titleSnapshot, fontWeight = FontWeight.Medium, fontFamily = AppFont())
                                        Text(
                                            text = item.optionsSnapshot.entries.joinToString(", ") { "${it.key}: ${it.value}" },
                                            fontSize = FontSize.SMALL,
                                            fontFamily = AppFont()
                                        )
                                    }
                                    Text(stringResource(Resources.String.QtyXPriceFormat, item.qty, stringResource(Resources.String.PriceFormat, item.unitPriceSnapshot)), fontFamily = AppFont())
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(Resources.String.SubtotalLabel), fontFamily = AppFont())
                                Text(stringResource(Resources.String.PriceFormat, order.subtotalPrice), fontFamily = AppFont())
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(Resources.String.ShippingLabel), fontFamily = AppFont())
                                Text(stringResource(Resources.String.PriceFormat, order.shippingPrice), fontFamily = AppFont())
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(Resources.String.TotalLabelSimple), fontWeight = FontWeight.Bold, fontFamily = AppFont())
                                Text(stringResource(Resources.String.PriceFormat, order.totalPrice), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = AppFont())
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(stringResource(Resources.String.UpdateStatus), fontWeight = FontWeight.Bold, fontFamily = AppFont())
                            val statuses = listOf("PLACED", "PROCESSING", "SHIPPING", "COMPLETED", "CANCELLED")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                statuses.forEach { status ->
                                    Button(
                                        onClick = { onUpdateStatus(status) },
                                        enabled = !state.isUpdatingStatus && order.status != status,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (order.status == status) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(status, fontSize = FontSize.EXTRA_SMALL, fontFamily = AppFont())
                                    }
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
