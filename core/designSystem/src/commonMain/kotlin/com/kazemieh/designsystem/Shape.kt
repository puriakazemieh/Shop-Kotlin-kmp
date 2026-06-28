package com.kazemieh.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// =====================================================================================
//  Carmilla Design System — Shape / Radius
//  مقیاس شعاع از پروتوتایپ: 8 · 11 · 13(دکمه) · 16 · 22 · full
// =====================================================================================

object Radius {
    val xs = 8.dp        // بَج، تگ کوچک
    val sm = 11.dp       // چیپ، آیتم لیست
    val button = 13.dp   // دکمه‌ها
    val md = 16.dp       // کارت محصول
    val lg = 22.dp       // کانتینر بزرگ، شیت
    val full = 999.dp    // دایره‌ای (استوری، اواتار)
}

// Material3 Shapes — برای کامپوننت‌هایی که شکل را از تم می‌گیرند
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.xs),
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(28.dp)
)
