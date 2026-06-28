package com.kazemieh.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// =====================================================================================
//  Carmilla Design System — Typography
//  خانواده: Vazirmatn (typeface_fa) · مقیاس بر اساس DesignSystem-Carmilla.dc.html
//  ۸۰۰/۳۴ عنوان بزرگ · ۷۰۰/۲۴ سرصفحه · ۶۰۰/۱۷ عنوان کارت · ۴۰۰/۱۵ بدنه (lh 1.9)
//  ۵۰۰/۱۴ دکمه · ۵۰۰/۱۱ کپشن (letterSpacing 1.5)
// =====================================================================================

@Composable
fun AppTypography(): Typography {
    val family = AppFont()
    return Typography(
        // کالکشن پاییز و زمستان — بزرگ‌ترین تیتر
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.ExtraBold, // 800
            fontSize = 34.sp,
            lineHeight = 42.sp,
            letterSpacing = (-0.6).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.ExtraBold, // 800
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.4).sp
        ),
        // عنوان بخش و سرصفحه
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold, // 700
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        // نام محصول و عنوان کارت
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold, // 600
            fontSize = 17.sp,
            lineHeight = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        // متن بدنه — line-height بلند برای خوانایی فارسی
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal, // 400
            fontSize = 15.sp,
            lineHeight = 28.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 24.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp
        ),
        // متن دکمه
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium, // 500
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        // کپشن/برچسب کوچک
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.5.sp
        )
    )
}
