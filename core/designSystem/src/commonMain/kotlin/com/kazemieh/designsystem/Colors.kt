package com.kazemieh.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color


// --- رنگ‌های اصلی و سفارشی شما ---
val GrayLighter = Color(0xFFFAFAFA)
val Gray = Color(0xFFF1F1F1)
val GrayDarker = Color(0xFFEBEBEB)

val NavyBlue = Color(0xFF2E3667) // Primary
val Gold = Color(0xFFC8A57C)     // Secondary
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Red = Color(0xFFDD0000)      // Error

val CategoryYellow = Color(0xFFFFC738)
val CategoryBlue = Color(0xFF38B3FF)
val CategoryGreen = Color(0xFF19D109)
val CategoryPurple = Color(0xFF8E5EFF)
val CategoryRed = Color(0xFFFF5E60)

//TextPrimary -> MaterialTheme.colorScheme.onPrimaryContainer
//IconPrimary -> MaterialTheme.colorScheme.onPrimaryContainer
//SurfaceError -> MaterialTheme.colorScheme.error
//TextWhite -> MaterialTheme.colorScheme.primaryContainer
//SurfaceBrand -> MaterialTheme.colorScheme.primary
//Surface -> MaterialTheme.colorScheme.background
//SurfaceLighter -> MaterialTheme.colorScheme.primaryContainer
//BorderIdle -> MaterialTheme.colorScheme.outline
//ButtonPrimary -> MaterialTheme.colorScheme.primary
//SurfaceSecondary -> MaterialTheme.colorScheme.secondary
//SurfaceDarker -> MaterialTheme.colorScheme.secondaryContainer

// --- پالت لایت ---
val LightAppColorScheme = lightColorScheme(
    primary = NavyBlue,       // رنگ اصلی دکمه‌ها، هدرها و ...
    onPrimary = Black,         // رنگ متن روی Primary

    primaryContainer = GrayLighter, // رنگ پس‌زمینه برای المان‌های اصلی
    onPrimaryContainer = Black,     // رنگ متن روی PrimaryContainer

    secondary = Gold,        // رنگ ثانویه برای عناصر تأکید شده
    onSecondary = White,       // رنگ متن روی Secondary

    secondaryContainer = Gray,     // رنگ پس‌زمینه برای المان‌های ثانویه
    onSecondaryContainer = Black,  // رنگ متن روی SecondaryContainer

    tertiary = CategoryBlue,   // رنگ سوم (اگر نیاز باشد)
    onTertiary = White,

    tertiaryContainer = CategoryGreen,
    onTertiaryContainer = Black,

    error = Red,               // رنگ خطا
    onError = White,           // رنگ متن روی Error

    errorContainer = CategoryRed, // رنگ پس‌زمینه برای المان‌های خطا
    onErrorContainer = White,

    background = White,        // رنگ اصلی پس‌زمینه صفحه
    onBackground = Black,      // رنگ متن اصلی روی پس‌زمینه

    surface = GrayLighter,     // رنگ سطوح (کارت‌ها، dialog ها)
    onSurface = Black,         // رنگ متن روی Surface

    surfaceVariant = Gray,     // رنگ جایگزین برای Surface (مثلا برای لیست‌ها)
    onSurfaceVariant = Black,  // رنگ متن روی SurfaceVariant

    outline = GrayDarker,      // رنگ خطوط دور المان‌ها
    outlineVariant = Gray,     // رنگ خطوط نازک‌تر
)

// --- پالت دارک ---
val DarkAppColorScheme = darkColorScheme(
    primary = NavyBlue,       // رنگ اصلی در حالت دارک
    onPrimary = Black,

    primaryContainer = Color(0xFF333333), // تیره‌تر برای پس‌زمینه
    onPrimaryContainer = White,

    secondary = Gold,
    onSecondary = White,

    secondaryContainer = Color(0xFF2A2A2A), // تیره‌تر
    onSecondaryContainer = White,

    tertiary = CategoryBlue,
    onTertiary = White,

    tertiaryContainer = CategoryGreen,
    onTertiaryContainer = Black,

    error = Red,
    onError = White,

    errorContainer = CategoryRed,
    onErrorContainer = White,

    background = Black,        // پس‌زمینه تیره
    onBackground = White,      // متن روشن روی پس‌زمینه تیره

    surface = Color(0xFF1E1E1E), // سطح تیره‌تر
    onSurface = White,         // متن روشن روی سطح

    surfaceVariant = Color(0xFF272727), // سطح جایگزین تیره‌تر
    onSurfaceVariant = White,

    outline = Color(0xFF404040), // خطوط تیره
    outlineVariant = Color(0xFF333333), // خطوط نازک‌تر تیره
)


data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val error: Color,
    val onError: Color,
    val outline: Color,
    val outlineVariant: Color,
    // رنگ‌های دسته‌بندی
    val categoryYellow: Color,
    val categoryBlue: Color,
    val categoryGreen: Color,
    val categoryPurple: Color,
    val categoryRed: Color,
    // رنگ‌های UI
    val grayLighter: Color,
    val gray: Color,
    val grayDarker: Color,
    val navyBlue: Color,
    val gold: Color,
    val white: Color,
    val black: Color,
    val red: Color
)

// تعریف CompositionLocal برای دسترسی به AppColors
val LocalAppColors = compositionLocalOf<AppColors> {
    error("No AppColors provided")
}

// ایجاد یک Composable برای ارائه AppColors
@Composable
fun provideAppColors(darkTheme: Boolean): AppColors {
    val colors = if (darkTheme) DarkAppColorScheme else LightAppColorScheme
    return remember(colors) {
        AppColors(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            error = colors.error,
            onError = colors.onError,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant,
            // رنگ‌های دسته‌بندی
            categoryYellow = CategoryYellow,
            categoryBlue = CategoryBlue,
            categoryGreen = CategoryGreen,
            categoryPurple = CategoryPurple,
            categoryRed = CategoryRed,
            // رنگ‌های UI
            grayLighter = GrayLighter,
            gray = Gray,
            grayDarker = GrayDarker,
            navyBlue = NavyBlue,
            gold = Gold,
            white = White,
            black = Black,
            red = Red
        )
    }
}