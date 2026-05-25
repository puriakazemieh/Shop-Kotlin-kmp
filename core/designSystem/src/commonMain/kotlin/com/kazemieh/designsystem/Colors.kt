package com.kazemieh.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color


// --- پایه‌ رنگ‌های تم لایت (روشن و شفاف) ---
val NavyBlueLight = Color(0xFF1E254C)      // سرمه‌ای عمیق برای لایت تم
val GoldLight = Color(0xFFAC8453)          // طلایی با کنتراست کافی برای متن و دکمه در لایت تم
val BgLight = Color(0xFFF8F9FA)            // پس‌زمینه لایت تم (کمی متمایل به خاکستری گرم برای کاهش خستگی چشم)
val SurfaceLight = Color(0xFFFFFFFF)       // کارت‌ها و دیالوگ‌های سفید خالص
val SurfaceVariantLight = Color(0xFFF1F3F5) // لایه‌های ثانویه
val OutlineLight = Color(0xFFE2E8F0)        // خطوط مرزی بسیار تمیز و کمرنگ

// --- پایه‌ رنگ‌های تم دارک (مدرن و ملو) ---
val NavyBlueDark = Color(0xFF7C8EFF)       // نسخه روشن‌تر و زنده‌تر سرمه‌ای برای دکمه‌های دارک تم
val GoldDark = Color(0xFFE5C49A)           // طلایی روشن و درخشان برای دارک تم
val BgDark = Color(0xFF0F111A)             // پس‌زمینه دارک (سرمه‌ای بسیار تیره و شیک، به جای مشکی مطلق)
val SurfaceDark = Color(0xFF181C2A)        // کارت‌ها در دارک تم
val SurfaceVariantDark = Color(0xFF24293E) // لایه‌های ثانویه دارک
val OutlineDark = Color(0xFF2F3652)        // خطوط مرزی دارک

// --- رنگ‌های عمومی و اصلاح شده دسته‌بندی‌ها (ملایم و شیک) ---
val CategoryYellow = Color(0xFFF5B041)
val CategoryBlue = Color(0xFF4EA8DE)
val CategoryGreen = Color(0xFF4895EF)
val CategoryPurple = Color(0xFF9B5DE5)
val CategoryRed = Color(0xFFF25C54)
val RedError = Color(0xFFE63946)

// --- پالت لایت (Light Palette) ---
val LightAppColorScheme = lightColorScheme(
    primary = NavyBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EAF6),
    onPrimaryContainer = NavyBlueLight,

    secondary = GoldLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDF6ED),
    onSecondaryContainer = Color(0xFF5C4325),

    background = BgLight,
    onBackground = Color(0xFF1A1A1A),

    surface = SurfaceLight,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF4A4A4A),

    outline = OutlineLight,
    outlineVariant = Color(0xFFCBD5E1),

    error = RedError,
    onError = Color.White,
    errorContainer = Color(0xFFFFEAEB),
    onErrorContainer = Color(0xFF600004)
)

// --- پالت دارک (Dark Palette) ---
val DarkAppColorScheme = darkColorScheme(
    primary = NavyBlueDark,
    onPrimary = Color(0xFF0A1033),
    primaryContainer = Color(0xFF1E254C),
    onPrimaryContainer = Color(0xFFE8EAF6),

    secondary = GoldDark,
    onSecondary = Color(0xFF422F18),
    secondaryContainer = Color(0xFF5C4325),
    onSecondaryContainer = Color(0xFFFDF6ED),

    background = BgDark,
    onBackground = Color(0xFFE2E8F0),

    surface = SurfaceDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),

    outline = OutlineDark,
    outlineVariant = Color(0xFF3F476C),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFF600004),
    errorContainer = Color(0xFF4A0002),
    onErrorContainer = Color(0xFFFFD8D8)
)

// --- ساختار داده‌ای AppColors ---
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val error: Color,
    val onError: Color,

    // رنگ‌های دسته‌بندی
    val categoryYellow: Color,
    val categoryBlue: Color,
    val categoryGreen: Color,
    val categoryPurple: Color,
    val categoryRed: Color
)

val LocalAppColors = compositionLocalOf<AppColors> {
    error("No AppColors provided")
}

@Composable
fun provideAppColors(darkTheme: Boolean): AppColors {
    val colors = if (darkTheme) DarkAppColorScheme else LightAppColorScheme
    return remember(colors) {
        AppColors(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            error = colors.error,
            onError = colors.onError,
            // دسته‌بندی‌ها
            categoryYellow = CategoryYellow,
            categoryBlue = CategoryBlue,
            categoryGreen = CategoryGreen,
            categoryPurple = CategoryPurple,
            categoryRed = CategoryRed
        )
    }
}