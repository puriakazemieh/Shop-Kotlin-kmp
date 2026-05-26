package com.kazemieh.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.AddressBottomSheet
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.component.ProfileForm
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.address.Address
import com.kazemieh.designsystem.util.anyToString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var showAddressDialog by remember { mutableStateOf(false) }
    var addressToEdit by remember { mutableStateOf<Address?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowSuccess -> {
                    messageBarState.addSuccess(effect.message)
                }

                is ProfileEffect.ShowError -> {
                    messageBarState.addError(effect.message)
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
                        text = stringResource(Resources.String.MyProfile),
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
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            messageBarState = messageBarState,
            errorMaxLines = 2,
            errorContainerColor = MaterialTheme.colorScheme.error,
            errorContentColor = MaterialTheme.colorScheme.primaryContainer,
            successContainerColor = MaterialTheme.colorScheme.primary,
            successContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 24.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                when (val displayState = state.displayState) {
                    is AppResult.Loading -> {
                        LoadingCard(modifier = Modifier.fillMaxWidth().height(200.dp))
                    }

                    is AppResult.Error -> {
                        InfoCard(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            image = Resources.Image.Cat,
                            title = stringResource(Resources.String.Oops),
                            subtitle = displayState.message
                        )
                    }

                    is AppResult.Success -> {
                        state.profile?.let { profile ->
                            ProfileForm(
                                modifier = Modifier.fillMaxWidth(),
                                firstName = profile.firstName,
                                onFirstNameChange = { value ->
                                    viewModel.handleIntent(
                                        ProfileIntent.UpdateFirstName(value)
                                    )
                                },
                                lastName = profile.lastName,
                                onLastNameChange = { value ->
                                    viewModel.handleIntent(
                                        ProfileIntent.UpdateLastName(value)
                                    )
                                },
                                email = profile.email,
                                phoneNumber = profile.phone,
                                onPhoneNumberChange = { value ->
                                    viewModel.handleIntent(
                                        ProfileIntent.UpdatePhoneNumber(value)
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PrimaryButton(
                                text = if (state.isSaving) stringResource(Resources.String.Saving) else stringResource(Resources.String.UpdateProfile),
                                icon = Resources.Icon.Checkmark,
                                enabled = state.isFormValid && !state.isSaving,
                                onClick = {
                                    viewModel.handleIntent(ProfileIntent.SaveProfile)
                                }
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Resources.String.MyAddresses),
                                    fontFamily = AppFont(),
                                    fontSize = FontSize.LARGE,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                IconButton(onClick = {
                                    addressToEdit = null
                                    showAddressDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(Resources.String.AddAddress),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.addressLoading) {
                                LoadingCard(modifier = Modifier.fillMaxWidth().height(100.dp))
                            } else if (state.addresses.isEmpty()) {
                                Text(
                                    text = stringResource(Resources.String.NoAddressesFound),
                                    fontFamily = AppFont(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                state.addresses.forEach { address ->
                                    AddressItem(
                                        address = address,
                                        onSetDefault = {
                                            viewModel.handleIntent(
                                                ProfileIntent.SetDefaultAddress(address.id)
                                            )
                                        },
                                        onEdit = {
                                            addressToEdit = address
                                            showAddressDialog = true
                                        },
                                        onDelete = {
                                            viewModel.handleIntent(
                                                ProfileIntent.DeleteAddress(address.id)
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddressDialog) {
        AddressBottomSheet(
            initialReceiverName = addressToEdit?.receiverName ?: "",
            initialReceiverPhone = addressToEdit?.receiverPhone ?: "",
            initialProvince = addressToEdit?.province ?: "",
            initialCity = addressToEdit?.city ?: "",
            initialAddressLine1 = addressToEdit?.addressLine1 ?: "",
            initialAddressLine2 = addressToEdit?.addressLine2,
            initialPostalCode = addressToEdit?.postalCode,
            onDismiss = { showAddressDialog = false },
            onConfirm = { receiverName, receiverPhone, province, city, addressLine1, addressLine2, postalCode ->
                if (addressToEdit == null) {
                    viewModel.handleIntent(
                        ProfileIntent.AddAddress(
                            receiverName = receiverName,
                            receiverPhone = receiverPhone,
                            country = "IR",
                            province = province,
                            city = city,
                            addressLine1 = addressLine1,
                            addressLine2 = addressLine2,
                            postalCode = postalCode,
                            setAsDefault = false
                        )
                    )
                } else {
                    viewModel.handleIntent(
                        ProfileIntent.UpdateUserAddress(
                            id = addressToEdit!!.id,
                            receiverName = receiverName,
                            receiverPhone = receiverPhone,
                            country = "IR",
                            province = province,
                            city = city,
                            addressLine1 = addressLine1,
                            addressLine2 = addressLine2,
                            postalCode = postalCode
                        )
                    )
                }
                showAddressDialog = false
            }
        )
    }
}

@Composable
fun AddressItem(
    address: Address,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (address.isDefault) Icons.Default.Star else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (address.isDefault) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = address.receiverName,
                        fontFamily = AppFont(),
                        fontWeight = FontWeight.Bold
                    )
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(Resources.String.Default),
                                fontSize = FontSize.SMALL,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = AppFont()
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Resources.String.Edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Resources.String.Delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = address.receiverPhone,
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Resources.String.AddressFormat, address.province, address.city, address.addressLine1),
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM
            )
            address.addressLine2?.let {
                Text(
                    text = it,
                    fontFamily = AppFont(),
                    fontSize = FontSize.MEDIUM
                )
            }
            address.postalCode?.let {
                Text(
                    text = stringResource(Resources.String.PostalCodeLabel, it),
                    fontFamily = AppFont(),
                    fontSize = FontSize.MEDIUM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!address.isDefault) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onSetDefault,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(Resources.String.SetAsDefault),
                        fontFamily = AppFont(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}



