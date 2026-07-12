package com.kazemieh.clinic.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.clinic.Appointment
import com.kazemieh.domain.clinic.AppointmentStatus
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppointmentsScreen(
    navigateBack: () -> Unit,
    navigateToCatalog: () -> Unit,
    navigateToReceipt: (Long) -> Unit = {}
) {
    val viewModel = koinViewModel<MyAppointmentsViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MyAppointmentsEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("نوبت‌های من", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.fillMaxSize().padding(padding),
            messageBarState = messageBarState
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                    state.appointments.isEmpty() -> Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("هنوز نوبتی رزرو نکرده‌اید.", color = colors.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "رزرو نوبت جدید",
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.button))
                                .background(colors.primary)
                                .clickable { navigateToCatalog() }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            color = colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.REGULAR
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.appointments) { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                cancelling = state.cancellingId == appointment.id,
                                onJoin = {
                                    appointment.videoRoomUrl?.let { url ->
                                        if (appointment.isPhone) uriHandler.openUri("tel:$url") else uriHandler.openUri(url)
                                    }
                                },
                                onCancel = { viewModel.cancel(appointment.id) },
                                onReceipt = { navigateToReceipt(appointment.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    cancelling: Boolean,
    onJoin: () -> Unit,
    onCancel: () -> Unit,
    onReceipt: () -> Unit
) {
    val colors = AppTheme.colors
    val (statusLabel, statusColor) = when (appointment.status) {
        AppointmentStatus.PENDING -> "در انتظار تأیید" to colors.star
        AppointmentStatus.CONFIRMED -> "تأییدشده" to colors.ok
        AppointmentStatus.COMPLETED -> "برگزارشده" to colors.onSurfaceVariant
        AppointmentStatus.CANCELLED -> "لغوشده" to colors.sale
        AppointmentStatus.UNKNOWN -> "-" to colors.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(appointment.therapistName, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR, modifier = Modifier.weight(1f))
            Text(
                text = statusLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.full))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                color = statusColor,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("${appointment.dayLabel}  •  ${appointment.timeLabel}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        if (appointment.isPhone && !appointment.videoRoomUrl.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("شماره‌ی تماس: ${appointment.videoRoomUrl}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }

        if (appointment.canJoin || appointment.status == AppointmentStatus.PENDING || appointment.status == AppointmentStatus.CONFIRMED) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (appointment.canJoin) {
                    ActionButton(
                        text = if (appointment.isPhone) "تماس بگیر" else "ورود به جلسه",
                        bg = colors.primary, fg = colors.onPrimary, onClick = onJoin
                    )
                }
                if (appointment.status == AppointmentStatus.PENDING || appointment.status == AppointmentStatus.CONFIRMED) {
                    ActionButton(
                        text = if (cancelling) "در حالِ لغو…" else "لغو نوبت",
                        bg = colors.surfaceVariant,
                        fg = colors.sale,
                        enabled = !cancelling,
                        onClick = onCancel
                    )
                }
            }
        }

        if (appointment.status == AppointmentStatus.COMPLETED) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "دریافتِ رسیدِ جلسه",
                modifier = Modifier.clickable { onReceipt() }.padding(vertical = 4.dp),
                color = colors.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = FontSize.SMALL
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    bg: Color,
    fg: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.button))
            .background(bg)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        color = fg,
        fontWeight = FontWeight.Bold,
        fontSize = FontSize.SMALL
    )
}
