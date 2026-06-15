package com.kazemieh.orders.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.common.util.formatDateTime
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.orders.list.UserStatusBadge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    navigateBack: () -> Unit,
    navigateToTracking: (Long) -> Unit
) {
    val viewModel = koinViewModel<OrderDetailViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(orderId) {
        viewModel.handleIntent(OrderDetailIntent.LoadOrderDetail(orderId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OrderDetailEffect.OrderCancelled -> messageBarState.addSuccess("Order cancelled successfully")
                is OrderDetailEffect.NavigateToTracking -> navigateToTracking(effect.id)
                is OrderDetailEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
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
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Resources.String.OrderIdPrefix, order.id),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFont()
                                )
                                UserStatusBadge(status = order.status)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(Resources.String.ShippingAddress), fontWeight = FontWeight.Bold, fontFamily = AppFont())
                            order.address?.let { address ->
                                Text(
                                    text = address.receiverName,
                                    fontFamily = AppFont(),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = address.receiverPhone,
                                    fontFamily = AppFont(),
                                    fontSize = FontSize.SMALL
                                )
                                Text(
                                    text = stringResource(Resources.String.AddressFormat, address.province, address.city, address.addressLine1),
                                    fontFamily = AppFont()
                                )
                                address.addressLine2?.let { Text(it, fontFamily = AppFont()) }
                                address.postalCode?.let {
                                    Text(
                                        text = stringResource(Resources.String.PostalCodeLabel, it),
                                        fontFamily = AppFont(),
                                        fontSize = FontSize.SMALL
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(Resources.String.ItemsLabel), fontWeight = FontWeight.Bold, fontFamily = AppFont())
                            order.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontWeight = FontWeight.Medium, fontFamily = AppFont())
                                        Text(
                                            text = item.options.entries.joinToString(", ") { "${it.key}: ${it.value}" },
                                            fontSize = FontSize.SMALL,
                                            fontFamily = AppFont()
                                        )
                                    }
                                    Text(
                                        stringResource(Resources.String.QtyXPriceFormat, item.qty, stringResource(Resources.String.PriceFormat, item.unitPrice)),
                                        fontFamily = AppFont()
                                    )
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
                                Text(
                                    stringResource(Resources.String.PriceFormat, order.totalPrice),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = AppFont()
                                )
                            }

                            if ((order.walletPaidAmount ?: 0.0) > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stringResource(Resources.String.WalletPaidAmount), fontSize = FontSize.SMALL, fontFamily = AppFont())
                                    Text(stringResource(Resources.String.PriceFormat, order.walletPaidAmount ?: 0.0), fontSize = FontSize.SMALL, fontFamily = AppFont())
                                }
                            }
                            if ((order.gatewayPaidAmount ?: 0.0) > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stringResource(Resources.String.GatewayPaidAmount), fontSize = FontSize.SMALL, fontFamily = AppFont())
                                    Text(stringResource(Resources.String.PriceFormat, order.gatewayPaidAmount ?: 0.0), fontSize = FontSize.SMALL, fontFamily = AppFont())
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            if (order.status == "PLACED" || order.status == "PROCESSING") {
                                PrimaryButton(
                                    text = stringResource(Resources.String.Cancel),
                                    onClick = { viewModel.handleIntent(OrderDetailIntent.CancelOrder(order.id)) },
                                    enabled = !state.isCancelling,
                                    secondary = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            PrimaryButton(
                                text = stringResource(Resources.String.TrackOrder),
                                onClick = { viewModel.handleIntent(OrderDetailIntent.TrackOrder(order.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
