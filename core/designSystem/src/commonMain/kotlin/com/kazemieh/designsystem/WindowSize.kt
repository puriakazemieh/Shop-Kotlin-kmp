package com.kazemieh.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =====================================================================================
//  Responsive / adaptive window-size system
//  سیستمِ ریسپانسیو مطابقِ نقاطِ شکستِ Material 3 (Compact / Medium / Expanded)
//  بر پایه‌ی BoxWithConstraints تا روی همه‌ی پلتفرم‌ها (Android/iOS/Web/Desktop) کار کند
//  و به وابستگیِ اضافه (material3-window-size-class) نیاز نداشته باشد.
// =====================================================================================

/** رده‌بندیِ عرضِ پنجره مطابقِ Material 3. */
enum class WindowWidthClass {
    /** موبایل — عرض < 600dp */
    Compact,
    /** تبلتِ عمودی / پنجره‌ی کوچکِ دسکتاپ — 600dp ≤ عرض < 840dp */
    Medium,
    /** تبلتِ افقی / لپ‌تاپ / دسکتاپ / وب — عرض ≥ 840dp */
    Expanded,
}

/** رده‌بندیِ ارتفاعِ پنجره مطابقِ Material 3. */
enum class WindowHeightClass {
    Compact,
    Medium,
    Expanded,
}

data class WindowSizeClass(
    val widthClass: WindowWidthClass,
    val heightClass: WindowHeightClass,
    val widthDp: Dp,
    val heightDp: Dp,
) {
    /** موبایل — نوارِ پایین و چیدمانِ تک‌ستونه. */
    val isCompact: Boolean get() = widthClass == WindowWidthClass.Compact

    /** تبلتِ عمودی. */
    val isMedium: Boolean get() = widthClass == WindowWidthClass.Medium

    /** لپ‌تاپ/دسکتاپ/وب. */
    val isExpanded: Boolean get() = widthClass == WindowWidthClass.Expanded

    /** هر چیزی بزرگ‌تر از موبایل — نوارِ کناری به‌جای نوارِ پایین. */
    val isLarge: Boolean get() = widthClass != WindowWidthClass.Compact
}

val LocalWindowSizeClass = staticCompositionLocalOf {
    WindowSizeClass(
        widthClass = WindowWidthClass.Compact,
        heightClass = WindowHeightClass.Medium,
        widthDp = 360.dp,
        heightDp = 800.dp,
    )
}

/**
 * محاسبه‌ی رده‌ی اندازه‌ی پنجره از روی ابعادِ واقعیِ ریشه و ارائه‌ی آن به‌صورتِ CompositionLocal.
 * باید یک بار در ریشه‌ی اپ (`App()`) دورِ کلِ محتوا قرار گیرد.
 */
@Composable
fun ProvideWindowSizeClass(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        val widthClass = when {
            w < 600.dp -> WindowWidthClass.Compact
            w < 840.dp -> WindowWidthClass.Medium
            else -> WindowWidthClass.Expanded
        }
        val heightClass = when {
            h < 480.dp -> WindowHeightClass.Compact
            h < 900.dp -> WindowHeightClass.Medium
            else -> WindowHeightClass.Expanded
        }
        CompositionLocalProvider(
            LocalWindowSizeClass provides WindowSizeClass(widthClass, heightClass, w, h)
        ) {
            content()
        }
    }
}

/** میان‌بر برای خواندنِ رده‌ی فعلیِ اندازه‌ی پنجره در هر Composable. */
@Composable
fun windowSizeClass(): WindowSizeClass = LocalWindowSizeClass.current

/**
 * تعدادِ ستونِ گرید بر اساسِ عرضِ پنجره — پیش‌فرض ۲ ستون روی موبایل،
 * ۳ روی تبلت و ۴ روی دسکتاپ/وب.
 */
@Composable
fun adaptiveGridColumns(
    compact: Int = 2,
    medium: Int = 3,
    expanded: Int = 4,
): Int = when (LocalWindowSizeClass.current.widthClass) {
    WindowWidthClass.Compact -> compact
    WindowWidthClass.Medium -> medium
    WindowWidthClass.Expanded -> expanded
}

/**
 * انتخابِ یک مقدار بر اساسِ رده‌ی عرض — برای پارامترهایی مثل padding افقی یا اندازه‌ی فونت.
 */
@Composable
fun <T> adaptiveValue(compact: T, medium: T, expanded: T): T =
    when (LocalWindowSizeClass.current.widthClass) {
        WindowWidthClass.Compact -> compact
        WindowWidthClass.Medium -> medium
        WindowWidthClass.Expanded -> expanded
    }

/** بیشینه‌ی عرضِ محتوا برای جلوگیری از کش‌آمدنِ افقیِ صفحات روی نمایشگرهای پهن. */
object ContentWidth {
    /** فرم‌ها و متنِ خوانا (جزئیات، تنظیمات، احراز هویت). */
    val readable: Dp = 640.dp

    /** محتوای متوسط (لیست‌های تک‌ستونه، سبد خرید، پروفایل). */
    val medium: Dp = 840.dp

    /** گریدها و داشبوردها. */
    val wide: Dp = 1200.dp
}

/**
 * محتوا را روی نمایشگرهای پهن در وسط قرار می‌دهد و عرضش را محدود می‌کند، ولی روی موبایل
 * دست‌نخورده تمام‌عرض می‌ماند. با این کار همه‌ی صفحات روی دسکتاپ/تبلت خوانا و متمرکز می‌مانند.
 *
 * @param maxWidth بیشینه‌ی عرضِ محتوا (پیش‌فرض [ContentWidth.medium]).
 * @param horizontalPadding فاصله‌ی افقیِ اضافی روی صفحاتِ بزرگ.
 */
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = ContentWidth.medium,
    horizontalPadding: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val isLarge = LocalWindowSizeClass.current.isLarge
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .then(if (isLarge && horizontalPadding > 0.dp) Modifier.padding(horizontal = horizontalPadding) else Modifier),
        ) {
            content()
        }
    }
}
