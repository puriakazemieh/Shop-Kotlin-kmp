package com.kazemieh.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.kazemieh.designsystem.component.CustomTextField
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
        contentBackgroundColor = colors.background,
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = stringResource(Resources.String.Cart),
                            fontSize = FontSize.EXTRA_MEDIUM,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onBackground
                        )
                        Spacer(Modifier.height(20.dp))

                        if (cart.items.isEmpty() && cart.savedForLater.isEmpty()) {
                            EmptyCart()
                        }

                        // ---- اقلام سبد ----
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

                        // ---- خلاصه سفارش ----
                        if (cart.items.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            OrderSummaryCard(
                                subtotal = cart.subtotal,
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

                        // ---- سبد خرید بعدی ----
                        if (cart.savedForLater.isNotEmpty()) {
                            Spacer(Modifier.height(28.dp))
                            Text(
                                text = stringResource(Resources.String.SavedForLater),
                                fontSize = FontSize.MEDIUM,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.onBackground
                            )
                            Spacer(Modifier.height(14.dp))
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
            label = stringResource(Resources.String.SubtotalLabel),
            value = stringResource(Resources.String.PriceFormat, subtotal),
            valueColor = colors.onSurface
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

        // کد تخفیف
        CustomTextField(
            value = if (appliedDiscountCode.isNullOrBlank()) discountCode else appliedDiscountCode,
            onValueChange = onDiscountCodeChange,
            placeholder = Resources.String.DiscountCode,
            enabled = appliedDiscountCode.isNullOrBlank()
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = if (!appliedDiscountCode.isNullOrBlank()) stringResource(Resources.String.Remove) else stringResource(Resources.String.Apply),
            onClick = { if (!appliedDiscountCode.isNullOrBlank()) onRemove() else onApply() },
            enabled = !isApplyingDiscount,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        )

        Spacer(Modifier.height(15.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
        Spacer(Modifier.height(15.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Resources.String.TotalLabel),
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
