package com.kazemieh.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.kazemieh.catalog.MainProductCard
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.adaptiveGridColumns
import com.kazemieh.designsystem.component.AddressBottomSheet
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.component.ProfileEditBottomSheet
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.MessageBarState
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.util.anyToString
import com.kazemieh.domain.address.Address
import com.kazemieh.domain.order.Order
import com.kazemieh.domain.wallet.WalletBalance
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateBack: () -> Unit,
    navigateToDetail: (String) -> Unit = {},
    navigateToOrderDetail: (Long) -> Unit = {},
    onSignedOut: () -> Unit = {}
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsState()
    val favoriteColumns = adaptiveGridColumns(compact = 2, medium = 3, expanded = 4)
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var showAddressDialog by remember { mutableStateOf(false) }
    var addressToEdit by remember { mutableStateOf<Address?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowSuccess -> {
                    messageBarState.addSuccess(effect.message)
                }

                is ProfileEffect.ShowError -> {
                    messageBarState.addError(effect.message)
                }

                is ProfileEffect.SignedOut -> onSignedOut()
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
                            modifier = Modifier.graphicsLayer {
                                rotationY = if (isRtl) 180f else 0f
                            },
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
                            subtitle = anyToString(displayState.message)
                        )
                    }

                    is AppResult.Success -> {
                        state.profile?.let { profile ->
                            var selectedTab by remember { mutableStateOf(0) }
                            ProfileHeader(
                                name = "${profile.firstName.orEmpty()} ${profile.lastName.orEmpty()}",
                                phone = profile.phone
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            ProfileTabs(selected = selectedTab, onSelect = { selectedTab = it })

                            Spacer(modifier = Modifier.height(20.dp))

                            when (selectedTab) {
                                // ---- اطلاعات شخصی ----
                                0 -> {
                                    // دکمه‌ی ویرایش (مثل بخش آدرس‌ها) که باتم‌شیت ویرایش را باز می‌کند
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showEditSheet = true }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "ویرایش اطلاعات",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = AppFont(),
                                            fontSize = FontSize.REGULAR,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    // آیتم‌های اطلاعات به‌صورت متنِ فقط‌خواندنی
                                    InfoField(label = "نام", value = profile.firstName)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    InfoField(label = "نام خانوادگی", value = profile.lastName)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    InfoField(label = "ایمیل", value = profile.email)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    InfoField(label = "شماره موبایل", value = profile.phone)
                                    Spacer(modifier = Modifier.height(20.dp))
                                    SignOutRow(onClick = { viewModel.handleIntent(ProfileIntent.SignOut) })
                                }

                                // ---- آدرس‌ها ----
                                1 -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                addressToEdit = null
                                                showAddressDialog = true
                                            }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(Resources.String.AddNewAddress),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = AppFont(),
                                            fontSize = FontSize.REGULAR,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
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
                                                onSetDefault = { viewModel.handleIntent(ProfileIntent.SetDefaultAddress(address.id)) },
                                                onEdit = {
                                                    addressToEdit = address
                                                    showAddressDialog = true
                                                },
                                                onDelete = { viewModel.handleIntent(ProfileIntent.DeleteAddress(address.id)) }
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }

                                // ---- کیف پول (موجودی و تراکنش‌ها به‌صورت درجا) ----
                                2 -> {
                                    ProfileWalletTab(messageBarState = messageBarState)
                                }

                                // ---- سفارش‌ها (فهرست درجا) ----
                                3 -> {
                                    if (state.ordersLoading) {
                                        LoadingCard(modifier = Modifier.fillMaxWidth().height(120.dp))
                                    } else if (state.orders.isEmpty()) {
                                        EmptyTabHint(text = "هنوز سفارشی ثبت نکرده‌اید.")
                                    } else {
                                        state.orders.forEach { order ->
                                            OrderRow(order = order, onClick = { navigateToOrderDetail(order.id) })
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }

                                // ---- علاقه‌مندی‌ها (شبکه‌ی درجا) ----
                                else -> {
                                    if (state.favoritesLoading) {
                                        LoadingCard(modifier = Modifier.fillMaxWidth().height(120.dp))
                                    } else if (state.favorites.isEmpty()) {
                                        EmptyTabHint(text = stringResource(Resources.String.FavoritesEmpty))
                                    } else {
                                        state.favorites.chunked(favoriteColumns).forEach { rowItems ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                rowItems.forEach { product ->
                                                    MainProductCard(
                                                        modifier = Modifier.weight(1f),
                                                        product = product,
                                                        onClick = navigateToDetail,
                                                        onFavoriteClick = { viewModel.handleIntent(ProfileIntent.ToggleFavorite(product)) }
                                                    )
                                                }
                                                repeat(favoriteColumns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
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

    if (showEditSheet) {
        state.profile?.let { profile ->
            ProfileEditBottomSheet(
                initialFirstName = profile.firstName.orEmpty(),
                initialLastName = profile.lastName.orEmpty(),
                email = profile.email ?: "",
                initialPhone = profile.phone ?: "",
                isSaving = state.isSaving,
                onDismiss = { showEditSheet = false },
                onConfirm = { firstName, lastName, phone ->
                    viewModel.handleIntent(ProfileIntent.UpdateFirstName(firstName))
                    viewModel.handleIntent(ProfileIntent.UpdateLastName(lastName))
                    viewModel.handleIntent(ProfileIntent.UpdatePhoneNumber(phone))
                    viewModel.handleIntent(ProfileIntent.SaveProfile)
                    showEditSheet = false
                }
            )
        }
    }
}

/** یک ردیفِ اطلاعاتِ فقط‌خواندنی: برچسب + مقدار. */
@Composable
private fun InfoField(label: String, value: String?) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = label,
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            fontFamily = AppFont(),
            fontSize = FontSize.REGULAR,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
    }
}

/** پیام خالی‌بودنِ یک تب. */
@Composable
private fun EmptyTabHint(text: String) {
    Text(
        text = text,
        fontFamily = AppFont(),
        fontSize = FontSize.REGULAR,
        color = AppTheme.colors.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

/** کارتِ خلاصه‌ی سفارش برای نمایشِ درجا در تب سفارش‌ها. */
@Composable
private fun OrderRow(order: Order, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "سفارش #${order.id}",
                fontFamily = AppFont(),
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${orderStatusLabel(order.status)} • ${order.createdAt.take(10)}",
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                color = colors.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(Resources.String.PriceFormat, order.totalPrice),
            fontFamily = AppFont(),
            fontSize = FontSize.REGULAR,
            fontWeight = FontWeight.ExtraBold,
            color = colors.primary
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(Resources.Icon.RightArrow),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp).graphicsLayer { rotationY = if (isRtl) 180f else 0f }
        )
    }
}

private fun orderStatusLabel(status: String): String = when (status.uppercase()) {
    "PENDING", "AWAITING_PAYMENT" -> "در انتظار پرداخت"
    "PAID", "PROCESSING" -> "در حال پردازش"
    "SHIPPED" -> "ارسال‌شده"
    "DELIVERED" -> "تحویل‌شده"
    "CANCELLED", "CANCELED" -> "لغوشده"
    else -> status
}

/** محتوای تب کیف پول به‌صورت درجا: موجودی + شارژ/برداشت + تراکنش‌ها. */
@Composable
private fun ProfileWalletTab(messageBarState: MessageBarState) {
    val vm = koinViewModel<WalletViewModel>()
    val st by vm.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    var showTopUp by remember { mutableStateOf(false) }
    var showWithdraw by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is WalletEffect.ShowError -> messageBarState.addError(effect.message)
                is WalletEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
                is WalletEffect.NavigateToPayment -> uriHandler.openUri(effect.url)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        BalanceCard(
            balanceState = st.balanceState,
            onTopUp = { showTopUp = true },
            onWithdraw = { showWithdraw = true }
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Resources.String.Transactions),
            fontFamily = AppFont(),
            fontSize = FontSize.EXTRA_REGULAR,
            fontWeight = FontWeight.ExtraBold,
            color = AppTheme.colors.onSurface
        )
        Spacer(Modifier.height(12.dp))
        when (val ts = st.transactionsState) {
            is AppResult.Loading -> LoadingCard(Modifier.fillMaxWidth().height(120.dp))
            is AppResult.Error -> InfoCard(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                title = stringResource(Resources.String.Oops),
                subtitle = anyToString(ts.message),
                image = Resources.Image.Cat
            )
            is AppResult.Success -> {
                val txs = ts.data.items
                if (txs.isEmpty()) {
                    EmptyTabHint(text = stringResource(Resources.String.WalletEmpty))
                } else {
                    txs.forEach { tx ->
                        TransactionItem(tx)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showTopUp) {
        TopUpDialog(
            isLoading = st.isTopUpLoading,
            onDismiss = { showTopUp = false },
            onConfirm = { amount ->
                vm.handleIntent(WalletIntent.TopUp(amount))
                showTopUp = false
            }
        )
    }
    if (showWithdraw) {
        WithdrawDialog(
            isLoading = st.isWithdrawLoading,
            onDismiss = { showWithdraw = false },
            onConfirm = { amount, iban ->
                vm.handleIntent(WalletIntent.Withdraw(amount, iban))
                showWithdraw = false
            }
        )
    }
}

@Composable
private fun ProfileHeader(name: String, phone: String?) {
    val colors = AppTheme.colors
    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(colors.primary, colors.accent2)))
            .padding(22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.ifBlank { "؟" },
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.trim().ifBlank { stringResource(Resources.String.MyProfile) },
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            if (!phone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phone,
                    fontFamily = AppFont(),
                    fontSize = FontSize.REGULAR,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/** ردیفِ تب‌های پروفایل (مثل پنل ادمین): اطلاعات شخصی / آدرس‌ها / کیف پول / … */
@Composable
private fun ProfileTabs(selected: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("اطلاعات شخصی", "آدرس‌ها", "کیف پول", "سفارش‌ها", "علاقه‌مندی‌ها")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            ProfileTabChip(
                label = label,
                selected = selected == index,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun ProfileTabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) Modifier.background(colors.primary)
                else Modifier.background(colors.surface).border(1.dp, colors.line, RoundedCornerShape(12.dp))
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        fontFamily = AppFont(),
        fontSize = FontSize.REGULAR,
        fontWeight = FontWeight.Bold,
        color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        maxLines = 1
    )
}

/** دکمهٔ خروج از حساب — داخل بخش مشخصاتِ پروفایل (مطابق درخواست). */
@Composable
private fun SignOutRow(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.sale.copy(alpha = 0.08f))
            .border(BorderStroke(1.dp, colors.sale.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Resources.Icon.SignOut),
            contentDescription = null,
            tint = colors.sale,
            modifier = Modifier.size(19.dp).graphicsLayer { rotationY = if (isRtl) 180f else 0f }
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = stringResource(Resources.String.SignOut),
            fontFamily = AppFont(),
            fontSize = FontSize.REGULAR,
            fontWeight = FontWeight.Bold,
            color = colors.sale
        )
    }
}

/** ردیفِ منوی پروفایل — مطابق اسپک: کاشیِ آیکن + عنوان + زیرعنوان + فلش. */
@Composable
private fun MenuRow(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontFamily = AppFont(), fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(3.dp))
            Text(text = subtitle, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
        }
        Icon(
            painter = painterResource(Resources.Icon.RightArrow),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp).graphicsLayer { rotationY = if (isRtl) 180f else 0f }
        )
    }
}

@Composable
fun WalletBalanceCard(
    state: AppResult<WalletBalance>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, AppTheme.colors.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(AppTheme.colors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Resources.Icon.Dollar),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(Resources.String.MyWallet),
                        fontFamily = AppFont(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    when (state) {
                        is AppResult.Loading -> {
                            Text(
                                text = stringResource(Resources.String.Loading),
                                fontSize = FontSize.SMALL,
                                fontFamily = AppFont(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        is AppResult.Success -> {
                            Text(
                                text = stringResource(
                                    Resources.String.PriceFormat,
                                    state.data.balance
                                ),
                                fontSize = FontSize.MEDIUM,
                                fontFamily = AppFont(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        is AppResult.Error -> {
                            Text(
                                text = anyToString(state.message),
                                fontSize = FontSize.EXTRA_SMALL,
                                fontFamily = AppFont(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            Icon(
                painter = painterResource(Resources.Icon.RightArrow),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
                    .graphicsLayer { rotationY = if (isRtl) 180f else 0f }
            )
        }
    }
}

@Composable
fun AddressItem(
    address: Address,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(
                width = if (address.isDefault) 1.5.dp else 1.dp,
                color = if (address.isDefault) colors.primary else colors.line,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = address.receiverName,
                    fontFamily = AppFont(),
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                if (address.isDefault) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Resources.String.Default),
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(colors.accentSoft)
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                        fontSize = FontSize.EXTRA_SMALL,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        fontFamily = AppFont()
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.accentSoft)
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(Resources.String.Edit), tint = colors.primary, modifier = Modifier.size(15.dp))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.sale.copy(alpha = 0.1f))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(Resources.String.Delete), tint = colors.sale, modifier = Modifier.size(15.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${address.receiverName} — ${address.receiverPhone}",
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "${address.province}، ${address.city}",
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            color = colors.onSurfaceVariant
        )
        Text(
            text = address.addressLine1,
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            color = colors.onSurfaceVariant
        )
        address.addressLine2?.let {
            Text(it, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
        }
        address.postalCode?.let {
            Text(
                text = stringResource(Resources.String.PostalCodeLabel, it),
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                color = colors.onSurfaceVariant
            )
        }

        if (!address.isDefault) {
            Spacer(modifier = Modifier.height(11.dp))
            Text(
                text = stringResource(Resources.String.SetAsDefault),
                modifier = Modifier.clickable { onSetDefault() },
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
        }
    }
}



