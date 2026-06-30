package com.kazemieh.cart.checkout

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.AddressBottomSheet
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.address.Address
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    totalAmount: Double,
    navigateBack: () -> Unit,
    navigateToPaymentCompleted: (Boolean?, String?) -> Unit,
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<CheckoutViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val uriHandler = LocalUriHandler.current

    var showAddressBottomSheet by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf(0) } // 0 = درگاه، 1 = در محل
    val addressAddedSuccessMessage = stringResource(Resources.String.AddressAddedSuccessfully)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CheckoutEffect.AddressAdded -> {
                    showAddressBottomSheet = false
                    messageBarState.addSuccess(addressAddedSuccessMessage)
                }
                is CheckoutEffect.NavigateToPaymentCompleted -> navigateToPaymentCompleted(effect.success, effect.error)
                is CheckoutEffect.OpenZarinpal -> uriHandler.openUri(effect.url)
                is CheckoutEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    val walletUsed = if (state.useWallet) min(state.walletBalance, totalAmount) else 0.0
    val remaining = (totalAmount - walletUsed).coerceAtLeast(0.0)

    Scaffold(containerColor = colors.background) { padding ->
        ContentWithMessageBar(
            contentBackgroundColor = colors.background,
            modifier = Modifier.padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
            messageBarState = messageBarState,
            errorMaxLines = 2,
            errorContainerColor = colors.error,
            errorContentColor = colors.onError,
            successContainerColor = colors.primary,
            successContentColor = colors.onPrimary
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 24.dp)
                ) {
                    // ---- هدر ----
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(Radius.button))
                                .background(colors.surface)
                                .border(1.dp, colors.line, RoundedCornerShape(Radius.button))
                                .clickable { navigateBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp).graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                                painter = painterResource(Resources.Icon.BackArrow),
                                contentDescription = stringResource(Resources.String.BackArrowDesc),
                                tint = colors.onSurface
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(Resources.String.Checkout),
                            fontSize = FontSize.EXTRA_MEDIUM,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onBackground
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ---- آدرس تحویل ----
                    SectionTitle("آدرس تحویل")
                    Spacer(Modifier.height(11.dp))
                    if (state.addresses.isNotEmpty()) {
                        state.addresses.forEach { address ->
                            AddressItem(
                                address = address,
                                isSelected = state.selectedAddressId == address.id,
                                onClick = { viewModel.handleIntent(CheckoutIntent.SelectAddress(address.id)) }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showAddressBottomSheet = true }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(Resources.String.AddNewAddress),
                                color = colors.primary,
                                fontSize = FontSize.REGULAR,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (!state.isLoading) {
                        PrimaryButton(
                            text = stringResource(Resources.String.AddYourFirstAddress),
                            icon = Resources.Icon.Plus,
                            onClick = { showAddressBottomSheet = true }
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    // ---- پرداخت از کیف پول ----
                    SectionTitle(stringResource(Resources.String.UseWalletBalance))
                    Spacer(Modifier.height(11.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(colors.surface)
                            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
                            .padding(15.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Resources.String.UseWalletBalance),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = FontSize.REGULAR,
                                    color = colors.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = stringResource(Resources.String.WalletBalance) + ": " + stringResource(Resources.String.PriceFormat, state.walletBalance),
                                    fontSize = FontSize.SMALL,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.useWallet,
                                onCheckedChange = { viewModel.handleIntent(CheckoutIntent.ToggleUseWallet(it)) }
                            )
                        }
                        if (state.useWallet) {
                            Spacer(Modifier.height(14.dp))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                            Spacer(Modifier.height(12.dp))
                            SummaryRow("از کیف پول", stringResource(Resources.String.PriceFormat, walletUsed), colors.ok)
                            Spacer(Modifier.height(7.dp))
                            SummaryRow("باقی‌مانده از درگاه", stringResource(Resources.String.PriceFormat, remaining), colors.onSurface)
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // ---- شیوه پرداخت ----
                    SectionTitle("شیوه پرداخت")
                    Spacer(Modifier.height(11.dp))
                    PaymentOption(
                        label = stringResource(Resources.String.PayWithZarinpal),
                        selected = selectedMethod == 0,
                        onClick = { selectedMethod = 0 }
                    )
                    Spacer(Modifier.height(10.dp))
                    PaymentOption(
                        label = stringResource(Resources.String.PayOnDelivery),
                        selected = selectedMethod == 1,
                        onClick = { selectedMethod = 1 }
                    )

                    Spacer(Modifier.height(22.dp))

                    // ---- خلاصه ----
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(colors.surface)
                            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("قابل پرداخت", fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
                            Text(
                                text = stringResource(Resources.String.PriceFormat, totalAmount),
                                fontSize = FontSize.MEDIUM,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    PrimaryButton(
                        text = "پرداخت و ثبت سفارش",
                        enabled = state.isFormValid,
                        onClick = {
                            if (selectedMethod == 0) viewModel.handleIntent(CheckoutIntent.PayWithZarinpal)
                            else viewModel.handleIntent(CheckoutIntent.PayOnDelivery)
                        }
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    if (showAddressBottomSheet) {
        AddressBottomSheet(
            onDismiss = { showAddressBottomSheet = false },
            onConfirm = { receiverName, receiverPhone, province, city, addressLine1, addressLine2, postalCode ->
                viewModel.handleIntent(
                    CheckoutIntent.AddNewAddress(
                        receiverName = receiverName,
                        receiverPhone = receiverPhone,
                        province = province,
                        city = city,
                        addressLine1 = addressLine1,
                        addressLine2 = addressLine2,
                        postalCode = postalCode
                    )
                )
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = FontSize.REGULAR,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.onBackground
    )
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
        Text(value, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun PaymentOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.button))
            .background(if (selected) colors.accentSoft else colors.surface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) colors.primary else colors.line,
                shape = RoundedCornerShape(Radius.button)
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = FontSize.REGULAR, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
    }
}

@Composable
fun AddressItem(
    address: Address,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(if (isSelected) colors.accentSoft else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.line,
                shape = RoundedCornerShape(Radius.md)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = address.receiverName,
                fontWeight = FontWeight.Bold,
                fontSize = FontSize.REGULAR,
                color = colors.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(Resources.String.CityProvinceFormat, address.province, address.city),
                fontSize = FontSize.SMALL,
                color = colors.onSurfaceVariant
            )
            Text(
                text = address.addressLine1,
                fontSize = FontSize.SMALL,
                color = colors.onSurfaceVariant
            )
        }
    }
}
