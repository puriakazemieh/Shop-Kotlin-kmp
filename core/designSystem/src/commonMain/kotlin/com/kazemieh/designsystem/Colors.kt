package com.kazemieh.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.kazemieh.designsystem.brand.BrandPalette

// =====================================================================================
//  Carmilla Design System — Color Tokens
//  منبع حقیقت: DesignSystem-Carmilla.dc.html (پالت روشن + متغیرهای دارک)
//  این توکن‌ها یک‌به‌یک با پروتوتایپ هم‌تراز شده‌اند.
// =====================================================================================

// --- توکن‌های پایه‌ی تم روشن (Light) ---
val AccentLight = Color(0xFF20305C)        // --accent  · رنگ برند/اکشن اصلی (سرمه‌ای)
val Accent2Light = Color(0xFF34487E)       // --accent-2 · گرادیان/حالت ثانویه‌ی برند
val AccentSoftLight = Color(0xFFEAEDF6)    // --accent-soft · کانتینر ملایم برند
val GoldLight = Color(0xFFB08D57)          // --gold · لهجه‌ی لاکچری
val GoldSoftLight = Color(0xFFF3ECE0)      // کانتینر ملایم طلایی (از گرادیان استوری)
val BgLight = Color(0xFFF6F5F1)            // --bg · پس‌زمینه‌ی گرم کِرِم
val SurfaceLight = Color(0xFFFFFFFF)       // --surface · سطح کارت
val SurfaceVariantLight = Color(0xFFF1EFE9) // --surface-2 · سطح فرورفته
val LineLight = Color(0xFFE7E4DD)          // --line · خط/بوردر
val OutlineVariantLight = Color(0xFFD9D5CC) // نسخه‌ی پررنگ‌تر خط
val InkLight = Color(0xFF192038)           // --ink · متن اصلی
val InkSoftLight = Color(0xFF6B7184)       // --ink-soft · متن ثانویه

// --- توکن‌های پایه‌ی تم تاریک (Dark) — از darkVars پروتوتایپ ---
val AccentDark = Color(0xFF5E73AD)         // --accent
val Accent2Dark = Color(0xFF7689BE)        // --accent-2
val AccentSoftDark = Color(0xFF262C44)     // --accent-soft
val GoldDark = Color(0xFFC9A86A)           // --gold
val GoldSoftDark = Color(0xFF3A2F1C)       // کانتینر طلایی دارک
val BgDark = Color(0xFF13151D)             // --bg
val SurfaceDark = Color(0xFF1B1E29)        // --surface
val SurfaceVariantDark = Color(0xFF242837) // --surface-2
val LineDark = Color(0xFF2E3242)           // --line
val OutlineVariantDark = Color(0xFF3A3F52)
val InkDark = Color(0xFFECEDF2)            // --ink
val InkSoftDark = Color(0xFF9AA0B2)        // --ink-soft

// --- رنگ‌های معنایی (Semantic) — مشترک با شدت متفاوت در دو تم ---
val SaleLight = Color(0xFFD8453B)          // --sale · حراج/خطا
val SaleDark = Color(0xFFE0584E)
val StarLight = Color(0xFFE7A93B)          // --star · امتیاز/ستاره
val StarDark = Color(0xFFE7A93B)
val OkLight = Color(0xFF1F9D6B)            // --ok · موفق/موجود
val OkDark = Color(0xFF33B57F)

// --- رنگ‌های دسته‌بندی (حفظ‌شده برای سازگاری با کد فعلی) ---
val CategoryYellow = Color(0xFFF5B041)
val CategoryBlue = Color(0xFF4EA8DE)
val CategoryGreen = Color(0xFF4895EF)
val CategoryPurple = Color(0xFF9B5DE5)
val CategoryRed = Color(0xFFF25C54)

// =====================================================================================
//  Material3 ColorScheme — مصرف‌کننده‌های فعلی از طریق MaterialTheme.colorScheme
// =====================================================================================

val LightAppColorScheme = lightColorScheme(
    primary = AccentLight,
    onPrimary = Color.White,
    primaryContainer = AccentSoftLight,
    onPrimaryContainer = AccentLight,

    secondary = GoldLight,
    onSecondary = Color.White,
    secondaryContainer = GoldSoftLight,
    onSecondaryContainer = Color(0xFF5C4325),

    tertiary = Accent2Light,
    onTertiary = Color.White,
    tertiaryContainer = AccentSoftLight,
    onTertiaryContainer = AccentLight,

    background = BgLight,
    onBackground = InkLight,

    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = InkSoftLight,

    outline = LineLight,
    outlineVariant = OutlineVariantLight,

    error = SaleLight,
    onError = Color.White,
    errorContainer = Color(0xFFFDEAE8),
    onErrorContainer = Color(0xFF5C1411)
)

val DarkAppColorScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = Color(0xFF0F1320),
    primaryContainer = AccentSoftDark,
    onPrimaryContainer = Color(0xFFDCE3F6),

    secondary = GoldDark,
    onSecondary = Color(0xFF2A2113),
    secondaryContainer = GoldSoftDark,
    onSecondaryContainer = Color(0xFFF3E6CF),

    tertiary = Accent2Dark,
    onTertiary = Color(0xFF0F1320),
    tertiaryContainer = AccentSoftDark,
    onTertiaryContainer = Color(0xFFDCE3F6),

    background = BgDark,
    onBackground = InkDark,

    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = InkSoftDark,

    outline = LineDark,
    outlineVariant = OutlineVariantDark,

    error = SaleDark,
    onError = Color(0xFF2A0A08),
    errorContainer = Color(0xFF4A0E0A),
    onErrorContainer = Color(0xFFFFD9D5)
)

// =====================================================================================
//  AppColors — توکن‌های گسترده‌ی برند (شامل معنایی‌هایی که در Material نمی‌گنجند)
//  برای کامپوننت‌های کارمیلا (Badge/Chip/ProductCard/...) در فازهای بعد استفاده می‌شود.
// =====================================================================================

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

    // توکن‌های برند کارمیلا
    val accent2: Color,
    val accentSoft: Color,
    val gold: Color,
    val line: Color,

    // معنایی
    val sale: Color,
    val star: Color,
    val ok: Color,

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

// =====================================================================================
//  سازنده‌های پالت‌محور (White-label) — از یک BrandPalette هم ColorScheme و هم AppColors می‌سازند
// =====================================================================================

fun buildColorScheme(p: BrandPalette, dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = p.accent,
        onPrimary = p.onAccent,
        primaryContainer = p.accentSoft,
        onPrimaryContainer = p.accent,
        secondary = p.gold,
        onSecondary = p.onGold,
        secondaryContainer = p.goldSoft,
        tertiary = p.accent2,
        onTertiary = p.onAccent,
        tertiaryContainer = p.accentSoft,
        onTertiaryContainer = p.accent,
        background = p.bg,
        onBackground = p.ink,
        surface = p.surface,
        onSurface = p.ink,
        surfaceVariant = p.surfaceVariant,
        onSurfaceVariant = p.inkSoft,
        outline = p.line,
        outlineVariant = p.outlineVariant,
        error = p.sale,
        onError = Color.White
    )
}

fun buildAppColors(p: BrandPalette): AppColors = AppColors(
    primary = p.accent,
    onPrimary = p.onAccent,
    primaryContainer = p.accentSoft,
    onPrimaryContainer = p.accent,
    secondary = p.gold,
    onSecondary = p.onGold,
    secondaryContainer = p.goldSoft,
    onSecondaryContainer = p.ink,
    background = p.bg,
    onBackground = p.ink,
    surface = p.surface,
    onSurface = p.ink,
    surfaceVariant = p.surfaceVariant,
    onSurfaceVariant = p.inkSoft,
    outline = p.line,
    error = p.sale,
    onError = Color.White,
    accent2 = p.accent2,
    accentSoft = p.accentSoft,
    gold = p.gold,
    line = p.line,
    sale = p.sale,
    star = p.star,
    ok = p.ok,
    categoryYellow = CategoryYellow,
    categoryBlue = CategoryBlue,
    categoryGreen = CategoryGreen,
    categoryPurple = CategoryPurple,
    categoryRed = CategoryRed
)

@Composable
fun provideAppColors(darkTheme: Boolean): AppColors {
    val colors = if (darkTheme) DarkAppColorScheme else LightAppColorScheme
    return remember(darkTheme) {
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
            // توکن‌های برند کارمیلا
            accent2 = if (darkTheme) Accent2Dark else Accent2Light,
            accentSoft = if (darkTheme) AccentSoftDark else AccentSoftLight,
            gold = if (darkTheme) GoldDark else GoldLight,
            line = if (darkTheme) LineDark else LineLight,
            // معنایی
            sale = if (darkTheme) SaleDark else SaleLight,
            star = if (darkTheme) StarDark else StarLight,
            ok = if (darkTheme) OkDark else OkLight,
            // دسته‌بندی‌ها
            categoryYellow = CategoryYellow,
            categoryBlue = CategoryBlue,
            categoryGreen = CategoryGreen,
            categoryPurple = CategoryPurple,
            categoryRed = CategoryRed
        )
    }
}
