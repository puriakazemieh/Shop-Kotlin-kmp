package com.kazemieh.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode

@Composable
fun AppTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    language: AppLanguage = AppLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkAppColorScheme else LightAppColorScheme
    val appColors = provideAppColors(darkTheme)
    val layoutDirection = if (language == AppLanguage.PERSIAN) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalLayoutDirection provides layoutDirection,
        AppLocale provides language.code
    ) {
        androidx.compose.runtime.key(language.code) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = AppTypography(),
                shapes = AppShapes,
                content = content
            )
        }
    }
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}