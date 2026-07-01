package com.kazemieh.cart

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navigateToCheckout: (Double) -> Unit,
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<CartViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    var discountCode by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CartEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    ContentWithMessageBar(
        contentBackgroundColor = colors.surface,
        messageBarState = messageBarState,
        errorContainerColor = colors.error,
        errorContentColor = colors.onError,
        successContainerColor = colors.primary,
        successContentColor = colors.onPrimary
    ) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.handleIntent(CartIntent.Refresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val cartResult = state.cartState) {
                is AppResult.Loading -> LoadingCard(modifier = Modifier.fillMaxWidth().height(200.dp))

                is AppResult.Error -> Box(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    InfoCard(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        image = Resources.Image.Cat,
                        title = stringResource(Resources.String.Oops),
                        subtitle = cartResult.message
                    )
                }

                is AppResult.Success -> {
                    val cart = cartResult.data
                    var selectedTab by remember { mutableStateOf(0) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 24.dp)
                    ) {
                        // عنوانِ صفحه از تاپ‌باری که MainGraphScreen می‌سازد می‌آید؛
                        // این‌جا فقط تب‌ها و محتوا را می‌گذاریم تا «سبد خرید» دوبار نوشته نشود.

                        // ---- تب‌ها: سبد جاری / سبد بعدی ----
                        CartTabs(
                            selected = selectedTab,
                            currentCount = cart.items.size,
                            savedCount = cart.savedForLater.size,
                            onSelect = { selectedTab = it }
                        )
                        Spacer(Modifier.height(20.dp))

                        if (selectedTab == 0) {
                            // ---- سبد خرید جاری ----
                            if (cart.items.isEmpty()) {
                                EmptyCart()
                            } else {
                                cart.items.forEach { item ->
                                    CartItemCard(
                                        cartItem = item,
                                        onPlusClick = { viewModel.handleIntent(CartIntent.AdjustQuantity(item.variantId, 1)) },
                                        onMinusClick = { newQty ->
                                            if (newQty == 0) viewModel.handleIntent(CartIntent.DeleteItem(item.id))
                                            else viewModel.handleIntent(CartIntent.AdjustQuantity(item.variantId, -1))
                                        },
                                        onDeleteClick = { viewModel.handleIntent(CartIntent.DeleteItem(item.id)) },
                                        onMoveToSaveForLaterClick = { viewModel.handleIntent(CartIntent.MoveToSaveForLater(item.id)) }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                Spacer(Modifier.height(8.dp))
                                val savings = cart.items.sumOf { item ->
                                    val cmp = item.compareAtPrice
                                    if (cmp != null && cmp > item.price) (cmp - item.price) * item.qty else 0.0
                                }
                                OrderSummaryCard(
                                    subtotal = cart.subtotal,
                                    savings = savings,
                                    discountAmount = cart.discountAmount,
                                    total = cart.total,
                                    appliedDiscountCode = cart.appliedDiscountCode,
                                    discountCode = discountCode,
                                    onDiscountCodeChange = { discountCode = it },
                                    isApplyingDiscount = state.isApplyingDiscount,
                                    onApply = { viewModel.handleIntent(CartIntent.ApplyDiscount(discountCode)) },
                                    onRemove = { viewModel.handleIntent(CartIntent.RemoveDiscount) },
                                    onCheckout = { navigateToCheckout(cart.total) }
                                )
                            }
                        } else {
                            // ---- سبد خرید بعدی ----
                            if (cart.savedForLater.isEmpty()) {
                                EmptySaved()
                            } else {
                                cart.savedForLater.forEach { item ->
                                    CartItemCard(
                                        cartItem = item,
                                        onPlusClick = {},
                                        onMinusClick = {},
                                        onDeleteClick = { viewModel.handleIntent(CartIntent.DeleteItem(item.id)) },
                                        onMoveToCartClick = { viewModel.handleIntent(CartIntent.MoveToCart(item.id)) }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** نوارِ دوتاییِ تب: سبد خرید جاری / سبد خرید بعدی (سگمنت‌کنترلِ پیل‌شکل). */
@Composable
private fun CartTabs(
    selected: Int,
    currentCount: Int,
    savedCount: Int,
    onSelect: (Int) -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.button))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.button))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CartTab(
            label = stringResource(Resources.String.CartCurrentTab),
            count = currentCount,
            selected = selected == 0,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(0) }
        )
        CartTab(
            label = stringResource(Resources.String.CartNextTab),
            count = savedCount,
            selected = selected == 1,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(1) }
        )
    }
}

@Composable
private fun CartTab(
    label: String,
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (selected) colors.surface else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 0) "$label ($count)" else label,
            fontSize = FontSize.SMALL,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptySaved() {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "موردی برای بعد ذخیره نشده است",
            fontSize = FontSize.REGULAR,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyCart() {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(90.dp).clip(CircleShape).background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Resources.Icon.ShoppingCart),
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(Resources.String.CartIsEmpty),
            fontSize = FontSize.EXTRA_REGULAR,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Resources.String.CartIsEmptySubtitle),
            fontSize = FontSize.REGULAR,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OrderSummaryCard(
    subtotal: Double,
    savings: Double,
    discountAmount: Double,
    total: Double,
    appliedDiscountCode: String?,
    discountCode: String,
    onDiscountCodeChange: (String) -> Unit,
    isApplyingDiscount: Boolean,
    onApply: () -> Unit,
    onRemove: () -> Unit,
    onCheckout: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.lg))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(Resources.String.OrderSummary),
            fontSize = FontSize.EXTRA_REGULAR,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onSurface
        )
        Spacer(Modifier.height(16.dp))

        SummaryRow(
            label = stringResource(Resources.String.ItemsPriceLabel),
            value = stringResource(Resources.String.PriceFormat, subtotal),
            valueColor = colors.onSurface
        )
        if (savings > 0) {
            Spacer(Modifier.height(11.dp))
            SummaryRow(
                label = stringResource(Resources.String.YourSavingsLabel),
                value = "- ${stringResource(Resources.String.PriceFormat, savings)}",
                valueColor = colors.sale
            )
        }
        Spacer(Modifier.height(11.dp))
        SummaryRow(
            label = stringResource(Resources.String.ShippingLabel),
            value = stringResource(Resources.String.FreeShipping),
            valueColor = colors.ok
        )
        if (discountAmount > 0) {
            Spacer(Modifier.height(11.dp))
            SummaryRow(
                label = stringResource(Resources.String.DiscountAmount),
                value = "- ${stringResource(Resources.String.PriceFormat, discountAmount)}",
                valueColor = colors.sale
            )
        }

        Spacer(Modifier.height(14.dp))

        // ---- کد تخفیف ----
        if (!appliedDiscountCode.isNullOrBlank()) {
            // چیپِ کدِ اعمال‌شده
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(colors.accentSoft)
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(Icons.Default.Check, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "کد «$appliedDiscountCode» اعمال شد",
                        fontSize = FontSize.SMALL,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Resources.String.Remove),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).clickable(enabled = !isApplyingDiscount) { onRemove() }
                )
            }
        } else {
            // فیلدِ کد تخفیف + دکمه‌ی اعمال (اینلاین، مطابق اسپک)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = discountCode,
                    onValueChange = onDiscountCodeChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(Resources.String.DiscountCode), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
                    singleLine = true,
                    shape = RoundedCornerShape(Radius.sm),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.line,
                        cursorColor = colors.primary,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface
                    )
                )
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(Radius.button))
                        .background(if (discountCode.isNotBlank() && !isApplyingDiscount) colors.primary else colors.line)
                        .clickable(enabled = discountCode.isNotBlank() && !isApplyingDiscount) { onApply() }
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Resources.String.Apply),
                        color = colors.onPrimary,
                        fontSize = FontSize.REGULAR,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(15.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
        Spacer(Modifier.height(15.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Resources.String.PayableAmountLabel),
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = stringResource(Resources.String.PriceFormat, total),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.ExtraBold,
                color = colors.primary
            )
        }

        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text = stringResource(Resources.String.Checkout),
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(Resources.String.SecurePayment),
            fontSize = FontSize.SMALL,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = FontSize.REGULAR, color = colors.onSurfaceVariant)
        Text(text = value, fontSize = FontSize.REGULAR, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
