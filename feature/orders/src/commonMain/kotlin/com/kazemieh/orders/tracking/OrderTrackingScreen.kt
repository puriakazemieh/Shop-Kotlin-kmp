package com.kazemieh.orders.tracking

import androidx.compose.foundation.layout.*
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
import com.kazemieh.orders.list.UserStatusBadge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: Long,
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<OrderTrackingViewModel>()
    val state by viewModel.state.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(orderId) {
        viewModel.handleIntent(OrderTrackingIntent.LoadTracking(orderId))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.TrackOrder),
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state.trackingState) {
                is AppResult.Loading -> LoadingCard(Modifier.fillMaxSize())
                is AppResult.Error -> InfoCard(
                    title = stringResource(Resources.String.Oops),
                    subtitle = result.message,
                    image = Resources.Image.Cat
                )
                is AppResult.Success -> {
                    val tracking = result.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Resources.String.OrderIdPrefix, tracking.id),
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.EXTRA_LARGE,
                            fontFamily = AppFont()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        UserStatusBadge(status = tracking.status)
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        TrackingItem(label = stringResource(Resources.String.OrderedAt), value = formatDateTime(tracking.orderedAt))
                        tracking.shippedAt?.let { TrackingItem(label = stringResource(Resources.String.ShippedAt), value = formatDateTime(it)) }
                        tracking.trackingCode?.let { TrackingItem(label = stringResource(Resources.String.TrackingCode), value = it) }
                        
                        if (tracking.trackingCode == null && tracking.status != "SHIPPING" && tracking.status != "COMPLETED") {
                            Text(
                                text = stringResource(Resources.String.TrackingInfoAvailability),
                                fontSize = FontSize.SMALL,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp),
                                fontFamily = AppFont()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingItem(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL, color = MaterialTheme.colorScheme.primary, fontFamily = AppFont())
        Text(text = value, fontSize = FontSize.MEDIUM, fontFamily = AppFont())
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}
