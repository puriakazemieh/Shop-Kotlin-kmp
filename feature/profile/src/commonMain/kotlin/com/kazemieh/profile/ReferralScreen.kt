package com.kazemieh.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(onBackClick: () -> Unit) {
    val viewModel = koinViewModel<ReferralViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("دعوت از دوستان", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state) {
                is AppResult.Loading -> LoadingCard(modifier = Modifier.fillMaxSize())
                is AppResult.Error -> InfoCard(
                    title = stringResource(Resources.String.Oops),
                    subtitle = result.message,
                    image = Resources.Image.Cat
                )
                is AppResult.Success -> {
                    val info = result.data
                    Column(modifier = Modifier.fillMaxSize().responsiveMaxWidth(com.kazemieh.designsystem.ContentWidth.readable).padding(16.dp)) {
                        Text(
                            "با هر خریدِ موفقِ دوستی که با کدِ تو ثبت‌نام کند، درصدی از مبلغِ خریدش به کیفِ پولِ تو اضافه می‌شود.",
                            fontSize = FontSize.SMALL, color = colors.onSurfaceVariant
                        )
                        Spacer(Modifier.height(20.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.accentSoft)
                                .padding(20.dp)
                        ) {
                            Text("کدِ اختصاصیِ تو", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                info.code,
                                fontSize = FontSize.LARGE,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primary
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatBox(label = "دوستانِ دعوت‌شده", value = info.referredCount.toString())
                            StatBox(label = "اعتبارِ کسب‌شده", value = "${info.totalEarned.toLong()} تومان")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(label, fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = FontSize.MEDIUM, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
    }
}
