package com.kazemieh.home.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    navigateBack: () -> Unit
) {
    val currentLanguage by viewModel.language.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.Settings),
                        fontFamily = AppFont()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackDesc)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Resources.String.Language),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = AppFont()
            )
            Spacer(modifier = Modifier.height(8.dp))

            AppLanguage.entries.forEach { language ->
                SettingsItem(
                    label = language.label,
                    isSelected = currentLanguage == language,
                    onSelect = { viewModel.onLanguageSelected(language) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Resources.String.Theme),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = AppFont()
            )
            Spacer(modifier = Modifier.height(8.dp))

            AppThemeMode.entries.forEach { mode ->
                SettingsItem(
                    label = stringResource(mode.toResource()),
                    isSelected = currentThemeMode == mode,
                    onSelect = { viewModel.onThemeModeSelected(mode) }
                )
            }
        }
    }
}

fun AppThemeMode.toResource(): StringResource {
    return when (this) {
        AppThemeMode.LIGHT -> Resources.String.Light
        AppThemeMode.DARK -> Resources.String.Dark
        AppThemeMode.SYSTEM -> Resources.String.SystemDefault
    }
}

@Composable
fun SettingsItem(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            fontFamily = AppFont()
        )
    }
}
