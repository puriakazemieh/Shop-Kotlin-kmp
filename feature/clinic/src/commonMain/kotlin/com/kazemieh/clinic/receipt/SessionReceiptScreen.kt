package com.kazemieh.clinic.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.domain.clinic.SessionReceipt
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionReceiptScreen(
    appointmentId: Long,
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<SessionReceiptViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(appointmentId) { viewModel.load(appointmentId) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("رسیدِ جلسه", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val receipt = state.receipt
            when {
                state.isLoading && receipt == null -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                receipt != null -> ReceiptCard(receipt, modifier = Modifier.align(Alignment.TopCenter).padding(20.dp))
                else -> Text(
                    "رسید یافت نشد.",
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReceiptCard(receipt: SessionReceipt, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surface)
            .padding(20.dp)
    ) {
        Text("رسیدِ جلسه‌ی مشاوره", fontWeight = FontWeight.ExtraBold, fontSize = FontSize.EXTRA_REGULAR, color = colors.onSurface)
        Spacer(Modifier.height(4.dp))
        Text("این رسید برایِ ارائه به بیمه یا سازمانِ مربوطه قابلِ‌استفاده است.", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        Spacer(Modifier.height(18.dp))
        ReceiptRow("شماره‌ی جلسه", "#${receipt.appointmentId}")
        ReceiptRow("نامِ مراجع", receipt.patientName)
        ReceiptRow("نامِ درمانگر", receipt.therapistName)
        receipt.therapistSpecialty?.let { ReceiptRow("تخصص", it) }
        ReceiptRow("نحوه‌ی برگزاری", when (receipt.sessionMode) {
            "IN_PERSON" -> "حضوری"; "PHONE" -> "تلفنی"; else -> "آنلاین"
        })
        ReceiptRow("تاریخ و ساعت", receipt.sessionDate)
        ReceiptRow("مدتِ جلسه", "${receipt.sessionDurationMinutes} دقیقه")
        Spacer(Modifier.height(8.dp))
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("مبلغِ پرداخت‌شده", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            Text("${receipt.amountPaid.toLong()} تومان", fontWeight = FontWeight.ExtraBold, color = colors.primary, fontSize = FontSize.REGULAR)
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        Text(value, color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = FontSize.SMALL)
    }
}
