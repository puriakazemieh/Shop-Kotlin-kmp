package com.kazemieh.academy.cert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import org.koin.compose.viewmodel.koinViewModel

/** صفحه‌ی عمومیِ تاییدِ گواهی — بدونِ نیازِ ورود، برایِ کارفرمایان/مؤسساتی که می‌خواهند اصالتِ گواهی را بررسی کنند. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateVerifyScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<CertificateVerifyViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    var certNumber by remember { mutableStateOf("") }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("تاییدِ گواهی", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "شماره‌ی گواهی را وارد کن تا اصالت و مشخصاتِ آن بررسی شود.",
                color = colors.onSurfaceVariant, fontSize = FontSize.SMALL
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = certNumber,
                onValueChange = { certNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("شماره‌ی گواهی", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (state.isChecking) "در حالِ بررسی…" else "بررسیِ گواهی",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.button))
                    .background(if (certNumber.isNotBlank() && !state.isChecking) colors.primary else colors.line)
                    .clickable(enabled = certNumber.isNotBlank() && !state.isChecking) { viewModel.verify(certNumber) }
                    .padding(vertical = 14.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR
            )
            Spacer(Modifier.height(20.dp))
            val result = state.result
            if (state.searched && result != null) {
                if (result.valid) {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(colors.ok.copy(alpha = 0.1f)).padding(16.dp)
                    ) {
                        Text("گواهیِ معتبر ✓", fontWeight = FontWeight.ExtraBold, color = colors.ok, fontSize = FontSize.REGULAR)
                        Spacer(Modifier.height(8.dp))
                        result.courseTitle?.let { Text("دوره: $it", color = colors.onSurface, fontSize = FontSize.SMALL) }
                        result.issuedAt?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("تاریخِ صدور: ${it.take(10)}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(colors.sale.copy(alpha = 0.1f)).padding(16.dp)
                    ) {
                        Text("گواهی‌ای با این شماره یافت نشد.", fontWeight = FontWeight.Bold, color = colors.sale, fontSize = FontSize.REGULAR)
                    }
                }
            }
        }
    }
}
