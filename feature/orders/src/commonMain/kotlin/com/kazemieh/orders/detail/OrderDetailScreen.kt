package com.kazemieh.orders.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.ContentWidth
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.domain.order.OrderDetail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.orders.list.UserStatusBadge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    navigateBack: () -> Unit,
    navigateToTracking: (Long) -> Unit,
    navigateToReturnRequest: (Long, String) -> Unit = { _, _ -> }
) {
    val viewModel = koinViewModel<OrderDetailViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var showInvoice by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.handleIntent(OrderDetailIntent.LoadOrderDetail(orderId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OrderDetailEffect.OrderCancelled -> messageBarState.addSuccess("Order cancelled successfully")
                is OrderDetailEffect.NavigateToTracking -> navigateToTracking(effect.id)
                is OrderDetailEffect.ShowError -> messageBarState.addError(effect.message)
                is OrderDetailEffect.Reordered -> {
                    if (effect.skippedTitles.isEmpty()) {
                        messageBarState.addSuccess("آیتم‌ها به سبد اضافه شدند.")
                    } else {
                        messageBarState.addSuccess(
                            "آیتم‌های موجود به سبد اضافه شدند. رد شد: ${effect.skippedTitles.joinToString("، ")}"
                        )
                    }
                }
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
                        val colors = AppTheme.colors
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .responsiveMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // ---- هدر ----
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Resources.String.OrderIdPrefix, order.id),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = FontSize.MEDIUM,
                                    fontFamily = AppFont(),
                                    color = colors.onSurface
                                )
                                UserStatusBadge(status = order.status)
                            }

                            // ---- هدیه ----
                            if (order.isGift) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.gold.copy(alpha = 0.12f))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text("این سفارش به‌عنوانِ هدیه ثبت شده", fontFamily = AppFont(), fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold, color = colors.gold)
                                    order.giftMessage?.takeIf { it.isNotBlank() }?.let {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(it, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                                    }
                                }
                            }

                            // ---- کد رهگیری ----
                            order.trackingCode?.let { tracking ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.accentSoft)
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("کد رهگیری پستی", fontFamily = AppFont(), fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold, color = colors.primary)
                                    Text(tracking, fontFamily = AppFont(), fontSize = FontSize.REGULAR, fontWeight = FontWeight.ExtraBold, color = colors.primary)
                                }
                            }

                            // ---- آدرس تحویل ----
                            order.address?.let { address ->
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(stringResource(Resources.String.ShippingAddress), fontWeight = FontWeight.ExtraBold, fontFamily = AppFont(), color = colors.onSurface)
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Text("${address.receiverName} — ${address.receiverPhone}", fontFamily = AppFont(), fontWeight = FontWeight.Bold, color = colors.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${address.province}، ${address.city}", fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                                    Text(address.addressLine1, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                                    address.postalCode?.let {
                                        Text(stringResource(Resources.String.PostalCodeLabel, it), fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                                    }
                                }
                            }

                            // ---- اقلام سفارش ----
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(stringResource(Resources.String.ItemsLabel), fontWeight = FontWeight.ExtraBold, fontFamily = AppFont(), color = colors.onSurface)
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                            ) {
                                order.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 48.dp, height = 56.dp)
                                                .clip(RoundedCornerShape(11.dp))
                                                .background(colors.surfaceVariant)
                                        )
                                        Spacer(modifier = Modifier.width(13.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, fontWeight = FontWeight.SemiBold, fontFamily = AppFont(), color = colors.onSurface)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.options.entries.joinToString(" · ") { "${it.key}: ${it.value}" } + " · تعداد ${item.qty}",
                                                fontSize = FontSize.SMALL,
                                                fontFamily = AppFont(),
                                                color = colors.onSurfaceVariant
                                            )
                                            if (order.status == "COMPLETED") {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "درخواستِ مرجوعی/تعویض",
                                                    fontSize = FontSize.EXTRA_SMALL,
                                                    fontFamily = AppFont(),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.primary,
                                                    modifier = Modifier.clickable {
                                                        navigateToReturnRequest(item.id, item.title)
                                                    }
                                                )
                                            }
                                        }
                                        Text(
                                            text = stringResource(Resources.String.PriceFormat, formatToman(item.unitPrice)),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = FontSize.SMALL,
                                            fontFamily = AppFont(),
                                            color = colors.onSurface
                                        )
                                    }
                                    HorizontalDivider(color = colors.line)
                                }
                                Column(modifier = Modifier.padding(16.dp)) {
                                    OrderSummaryLine(stringResource(Resources.String.SubtotalLabel), stringResource(Resources.String.PriceFormat, formatToman(order.subtotalPrice)))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OrderSummaryLine(stringResource(Resources.String.ShippingLabel), stringResource(Resources.String.PriceFormat, formatToman(order.shippingPrice)))
                                    if ((order.walletPaidAmount ?: 0.0) > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OrderSummaryLine(stringResource(Resources.String.WalletPaidAmount), stringResource(Resources.String.PriceFormat, formatToman(order.walletPaidAmount ?: 0.0)))
                                    }
                                    if ((order.gatewayPaidAmount ?: 0.0) > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OrderSummaryLine(stringResource(Resources.String.GatewayPaidAmount), stringResource(Resources.String.PriceFormat, formatToman(order.gatewayPaidAmount ?: 0.0)))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(Resources.String.TotalLabelSimple), fontWeight = FontWeight.Bold, fontFamily = AppFont(), color = colors.onSurface)
                                        Text(
                                            stringResource(Resources.String.PriceFormat, formatToman(order.totalPrice)),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = FontSize.EXTRA_REGULAR,
                                            color = colors.primary,
                                            fontFamily = AppFont()
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

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

                            Spacer(modifier = Modifier.height(12.dp))
                            PrimaryButton(
                                text = "سفارشِ مجددِ همین اقلام",
                                onClick = { viewModel.handleIntent(OrderDetailIntent.Reorder(order.id)) },
                                enabled = !state.isReordering,
                                secondary = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            PrimaryButton(
                                text = "دانلود فاکتور",
                                onClick = { showInvoice = true },
                                secondary = true
                            )

                            if (showInvoice) {
                                InvoiceDialog(order = order, onDismiss = { showInvoice = false })
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * فاکتورِ چاپیِ سفارش که از داده‌ی موجودِ سفارش ساخته می‌شود. روی وب/دسکتاپ کاربر می‌تواند
 * از print مرورگر خروجیِ PDF بگیرد؛ روی موبایل با اسکرین‌شات ذخیره کند.
 */
@Composable
private fun InvoiceDialog(order: OrderDetail, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .responsiveMaxWidth(ContentWidth.readable)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(colors.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text("فاکتورِ فروش", fontFamily = AppFont(), fontWeight = FontWeight.ExtraBold, fontSize = FontSize.LARGE, color = colors.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("شماره سفارش: TR-${order.id}", fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                Text("تاریخ ثبت: ${order.createdAt}", fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                order.address?.let { addr ->
                    Spacer(Modifier.height(10.dp))
                    Text("خریدار: ${addr.receiverName} — ${addr.receiverPhone}", fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurface)
                    Text("${addr.province}، ${addr.city} — ${addr.addressLine1}", fontFamily = AppFont(), fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = colors.line)
                Spacer(Modifier.height(10.dp))
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.title} ×${item.qty}", fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurface, modifier = Modifier.weight(1f))
                        Text(stringResource(Resources.String.PriceFormat, formatToman(item.unitPrice * item.qty)), fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurface)
                    }
                }
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = colors.line)
                Spacer(Modifier.height(10.dp))
                OrderSummaryLine(stringResource(Resources.String.SubtotalLabel), stringResource(Resources.String.PriceFormat, formatToman(order.subtotalPrice)))
                Spacer(Modifier.height(4.dp))
                OrderSummaryLine(stringResource(Resources.String.ShippingLabel), stringResource(Resources.String.PriceFormat, formatToman(order.shippingPrice)))
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("مبلغ کل", fontFamily = AppFont(), fontWeight = FontWeight.ExtraBold, fontSize = FontSize.REGULAR, color = colors.onSurface)
                    Text(stringResource(Resources.String.PriceFormat, formatToman(order.totalPrice)), fontFamily = AppFont(), fontWeight = FontWeight.ExtraBold, fontSize = FontSize.REGULAR, color = colors.primary)
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "بستن", onClick = onDismiss, secondary = true)
            }
        }
    }
}

@Composable
private fun OrderSummaryLine(label: String, value: String) {
    val colors = AppTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
        Text(value, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurface)
    }
}
