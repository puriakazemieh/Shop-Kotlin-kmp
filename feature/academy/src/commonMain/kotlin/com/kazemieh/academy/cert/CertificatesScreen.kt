package com.kazemieh.academy.cert

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.domain.academy.Certificate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatesScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<CertificatesViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("گواهی‌های من", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                state.certificates.isEmpty() -> Text(
                    "هنوز گواهی‌ای ندارید. با قبولی در آزمونِ پایانِ دوره، گواهی صادر می‌شود.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.certificates.size) { idx ->
                        CertificateCard(state.certificates[idx])
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateCard(cert: Certificate) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.gold, RoundedCornerShape(Radius.md))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = colors.gold, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(10.dp))
            Text("گواهیِ پایانِ دوره", fontWeight = FontWeight.ExtraBold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        }
        Spacer(Modifier.height(12.dp))
        Text(cert.courseTitle, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.EXTRA_REGULAR)
        Spacer(Modifier.height(8.dp))
        Text("شماره‌ی گواهی: ${cert.certNumber}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        Spacer(Modifier.height(4.dp))
        Text("تاریخِ صدور: ${cert.issuedAt.take(10)}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
    }
}
