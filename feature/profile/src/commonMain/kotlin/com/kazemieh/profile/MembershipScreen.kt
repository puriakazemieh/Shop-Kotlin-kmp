package com.kazemieh.profile

import androidx.compose.foundation.background
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
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.util.formatToman
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(onBackClick: () -> Unit) {
    val viewModel = koinViewModel<MembershipViewModel>()
    val state by viewModel.state.collectAsState()
    val isSubscribing by viewModel.isSubscribing.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MembershipEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("عضویتِ ویژه", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        ContentWithMessageBar(modifier = Modifier.padding(padding), messageBarState = messageBarState) {
            when (val result = state) {
                is AppResult.Loading -> LoadingCard(modifier = Modifier.fillMaxSize())
                is AppResult.Error -> InfoCard(
                    title = stringResource(Resources.String.Oops),
                    subtitle = result.message,
                    image = Resources.Image.Cat
                )
                is AppResult.Success -> {
                    val status = result.data
                    Column(modifier = Modifier.fillMaxSize().responsiveMaxWidth(com.kazemieh.designsystem.ContentWidth.readable).padding(16.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.gold.copy(alpha = 0.12f))
                                .padding(20.dp)
                        ) {
                            Text(
                                if (status.isActive) "عضویتِ ویژه فعال است" else "عضویتِ ویژه فعال نیست",
                                fontWeight = FontWeight.ExtraBold, fontSize = FontSize.MEDIUM, color = colors.gold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${(status.discountPercent.takeIf { status.isActive } ?: 0.05) * 100}٪ تخفیفِ خودکار روی همه‌ی خریدها" +
                                    if (status.isActive && status.expiresAt != null) " · تا ${status.expiresAt?.take(10)}" else "",
                                fontSize = FontSize.SMALL, color = colors.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "هزینه‌ی هر ۳۰ روز: ${formatToman(status.price)} تومان (از کیفِ‌پول کسر می‌شود)",
                            fontSize = FontSize.SMALL, color = colors.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = if (status.isActive) "تمدیدِ ۳۰ روزِ دیگر" else "فعال‌سازیِ عضویتِ ویژه",
                            enabled = !isSubscribing,
                            onClick = { viewModel.subscribe() }
                        )
                    }
                }
            }
        }
    }
}
