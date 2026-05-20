package com.kazemieh.home.cart.checkout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.AddressBottomSheet
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.model.Address
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    totalAmount: Double,
    navigateBack: () -> Unit,
    navigateToPaymentCompleted: (Boolean?, String?) -> Unit,
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<CheckoutViewModel>()
    val screenState = viewModel.screenState
    val isFormValid = viewModel.isFormValid

    var showAddressBottomSheet by remember { mutableStateOf(false) }
    val addressAddedSuccessMessage = stringResource(Resources.String.AddressAddedSuccessfully)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.Checkout),
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    Text(
                        text = stringResource(Resources.String.PriceFormat, totalAmount),
                        fontSize = FontSize.EXTRA_MEDIUM,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackArrowDesc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            contentBackgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            messageBarState = messageBarState,
            errorMaxLines = 2,
            errorContainerColor = MaterialTheme.colorScheme.errorContainer,
            errorContentColor = MaterialTheme.colorScheme.onErrorContainer,
            successContainerColor = MaterialTheme.colorScheme.primaryContainer,
            successContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            val scrollState = rememberScrollState()
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (screenState.addresses.isNotEmpty()) {
                        Text(
                            text = stringResource(Resources.String.SelectAddress),
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.MEDIUM
                        )
                        screenState.addresses.forEach { address ->
                            AddressItem(
                                address = address,
                                isSelected = screenState.selectedAddressId == address.id,
                                onClick = { viewModel.selectAddress(address.id) }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAddressBottomSheet = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Resources.String.AddNewAddress),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (!screenState.isLoading) {
                        PrimaryButton(
                            text = stringResource(Resources.String.AddYourFirstAddress),
                            icon = Resources.Icon.Plus,
                            onClick = { showAddressBottomSheet = true }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        PrimaryButton(
                            text = stringResource(Resources.String.PayWithPayPal),
                            icon = Resources.Image.PaypalLogo,
                            enabled = isFormValid,
                            onClick = {
                                viewModel.payWithPayPal(
                                    onSuccess = {

                                    },
                                    onError = { message ->
                                        messageBarState.addError(message)
                                    }
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = stringResource(Resources.String.PayOnDelivery),
                            icon = Resources.Icon.ShoppingCart,
                            secondary = true,
                            enabled = isFormValid,
                            onClick = {
                                viewModel.payOnDelivery(
                                    onSuccess = {
                                        navigateToPaymentCompleted(true, null)
                                    },
                                    onError = { message ->
                                        navigateToPaymentCompleted(null, message.toString())
                                    }
                                )
                            }
                        )
                    }
                }

                if (screenState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
    if (showAddressBottomSheet) {
        AddressBottomSheet(
            onDismiss = { showAddressBottomSheet = false },
            onConfirm = { receiverName, receiverPhone, province, city, addressLine1, addressLine2, postalCode ->
                viewModel.addNewAddress(
                    receiverName = receiverName,
                    receiverPhone = receiverPhone,
                    province = province,
                    city = city,
                    addressLine1 = addressLine1,
                    addressLine2 = addressLine2,
                    postalCode = postalCode,
                    onSuccess = {
                        showAddressBottomSheet = false
                        messageBarState.addSuccess(addressAddedSuccessMessage)
                    },
                    onError = { error ->
                        messageBarState.addError(error)
                    }
                )
            }
        )
    }
}

@Composable
fun AddressItem(
    address: Address,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = address.receiverName,
                fontWeight = FontWeight.Bold,
                fontSize = FontSize.MEDIUM
            )
            Text(
                text = stringResource(Resources.String.CityProvinceFormat, address.province, address.city),
                fontSize = FontSize.SMALL
            )
            Text(
                text = address.addressLine1,
                fontSize = FontSize.SMALL
            )
        }
    }
}
