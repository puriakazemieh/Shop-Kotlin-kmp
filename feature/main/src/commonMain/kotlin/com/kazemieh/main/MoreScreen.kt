package com.kazemieh.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.painterResource

/**
 * تبِ حساب کاربری — مطابق اسپک کارمیلا.
 * ترتیب: هدر ← آیتم‌های پروفایل ← (اگر ادمین) مدیریت فروشگاه ← باشگاه مشتریان و پشتیبانی ← تنظیمات.
 * دکمه‌ی خروج به بخش «مشخصات» داخل صفحه‌ی پروفایل منتقل شده است.
 */
@Composable
fun MoreScreen(
    isLoggedIn: Boolean,
    isAdmin: Boolean = false,
    userName: String = "",
    userPhone: String = "",
    showAcademy: Boolean = true,
    showClinic: Boolean = true,
    showPsychTests: Boolean = false,
    showComparison: Boolean = false,
    showFreeCourses: Boolean = false,
    showBundles: Boolean = false,
    onLoginClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCustomerClubClick: () -> Unit,
    onSupportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onMyCoursesClick: () -> Unit = {},
    onBrowseCoursesClick: () -> Unit = {},
    onCertificatesClick: () -> Unit = {},
    onMyAppointmentsClick: () -> Unit = {},
    onBrowseTherapistsClick: () -> Unit = {},
    onPsychTestsClick: () -> Unit = {},
    onComparisonClick: () -> Unit = {},
    onFreeCoursesClick: () -> Unit = {},
    onBundlesClick: () -> Unit = {},
    onReferralClick: () -> Unit = {},
    onRecurringOrdersClick: () -> Unit = {},
    onMembershipClick: () -> Unit = {},
    onShoppingAssistantClick: () -> Unit = {},
    onPlacementQuizClick: () -> Unit = {},
    onCertificateVerifyClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        if (isLoggedIn) {
            AccountHeader(name = userName, phone = userPhone, onEditClick = onEditProfileClick)
        } else {
            LoginPromptCard(onClick = onLoginClick)
        }

        Spacer(Modifier.height(16.dp))

        // آیتم‌های پروفایل (سفارش‌ها، علاقه‌مندی‌ها، آدرس‌ها، کیف پول) به داخل
        // صفحه‌ی «مشخصات» منتقل شده‌اند و از طریق کارت هدر بالا در دسترس‌اند.

        // ---- (اگر ادمین) پنل مدیریت فروشگاه ----
        if (isAdmin) {
            AdminPanelCard(onClick = onAdminPanelClick)
            Spacer(Modifier.height(12.dp))
        }

        // ---- باشگاه مشتریان و پشتیبانی ----
        if (isLoggedIn) {
            AccountRow(
                title = "باشگاه مشتریان",
                subtitle = "امتیازها و مزایای شما",
                onClick = onCustomerClubClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))

            AccountRow(
                title = "دعوت از دوستان",
                subtitle = "با کدِ اختصاصیِ خودت اعتبار بگیر",
                onClick = onReferralClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))

            AccountRow(
                title = "خریدهایِ تکراری",
                subtitle = "سفارشِ خودکارِ دوره‌ای برایِ کالاهایِ مصرفی",
                onClick = onRecurringOrdersClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))

            AccountRow(
                title = "عضویتِ ویژه",
                subtitle = "تخفیفِ خودکار روی همه‌ی خریدها",
                onClick = onMembershipClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))

            if (showAcademy) {
                AccountRow(
                    title = "دوره‌های من",
                    subtitle = "آموزش‌های خریداری‌شده و پیشرفت",
                    onClick = onMyCoursesClick
                ) { AccountIcon(painter = Resources.Icon.Book) }
                Spacer(Modifier.height(12.dp))

                AccountRow(
                    title = "گواهی‌های من",
                    subtitle = "گواهیِ دوره‌های تکمیل‌شده",
                    onClick = onCertificatesClick
                ) { AccountIcon(vector = Icons.Default.Star) }
                Spacer(Modifier.height(12.dp))
            }

            if (showClinic) {
                AccountRow(
                    title = "نوبت‌های من",
                    subtitle = "مشاوره‌های رزروشده و ورود به جلسه",
                    onClick = onMyAppointmentsClick
                ) { AccountIcon(painter = Resources.Icon.Clock) }
                Spacer(Modifier.height(12.dp))
            }
        }

        // ---- آموزشگاه (مرور و خرید دوره‌ها) — فقط وقتی برند این عمودی را روشن کرده ----
        if (showAcademy) {
            AccountRow(
                title = "دوره‌های آموزشی",
                subtitle = "مشاهده و خرید دوره‌ها",
                onClick = onBrowseCoursesClick
            ) { AccountIcon(painter = Resources.Icon.Book) }
            Spacer(Modifier.height(12.dp))

            if (showFreeCourses) {
                AccountRow(
                    title = "دوره‌های رایگان",
                    subtitle = "شروعِ یادگیری بدونِ هزینه",
                    onClick = onFreeCoursesClick
                ) { AccountIcon(painter = Resources.Icon.Book) }
                Spacer(Modifier.height(12.dp))
            }

            AccountRow(
                title = "آزمونِ تعیینِ سطح",
                subtitle = "پیدا کردنِ دوره‌ی مناسبِ سطحِ تو",
                onClick = onPlacementQuizClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))

            AccountRow(
                title = "تاییدِ گواهی",
                subtitle = "بررسیِ اصالتِ یک گواهیِ صادرشده",
                onClick = onCertificateVerifyClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))
        }

        // ---- پیشنهادهای ترکیبی/باندل — فیچرِ عمومیِ فروشگاهی ----
        if (showBundles) {
            AccountRow(
                title = "پیشنهادهای ترکیبی",
                subtitle = "پکیج‌های ویژه با چند محصول",
                onClick = onBundlesClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))
        }

        // ---- مشاوره و روان‌شناسی (رزرو نوبت) — فقط وقتی برند این عمودی را روشن کرده ----
        if (showClinic) {
            AccountRow(
                title = "مشاوره و روان‌شناسی",
                subtitle = "انتخابِ درمانگر و رزرو نوبت",
                onClick = onBrowseTherapistsClick
            ) { AccountIcon(painter = Resources.Icon.Person) }
            Spacer(Modifier.height(12.dp))
        }

        // ---- تست‌های روان‌شناسی — فقط وقتی برند این عمودی را روشن کرده ----
        if (showPsychTests) {
            AccountRow(
                title = "تست‌های روان‌شناسی",
                subtitle = "خرید و انجامِ تست‌های خودارزیابی",
                onClick = onPsychTestsClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))
        }

        // ---- مقایسه‌ی محصولات — فیچرِ عمومی، پشتِ پرچمِ برند ----
        if (showComparison) {
            AccountRow(
                title = "مقایسه‌ی محصولات",
                subtitle = "بررسیِ کنارِهمِ مشخصاتِ محصولات",
                onClick = onComparisonClick
            ) { AccountIcon(vector = Icons.Default.Star) }
            Spacer(Modifier.height(12.dp))
        }

        AccountRow(
            title = "دستیارِ خرید",
            subtitle = "راهنمایی و پیشنهادِ محصول",
            onClick = onShoppingAssistantClick
        ) { AccountIcon(vector = Icons.Default.Star) }
        Spacer(Modifier.height(12.dp))

        AccountRow(
            title = "پشتیبانی و سؤالات",
            subtitle = "پاسخگویی ۲۴ ساعته",
            onClick = onSupportClick
        ) { AccountIcon(painter = Resources.Icon.Edit) }
        Spacer(Modifier.height(12.dp))

        // ---- تنظیمات ----
        AccountRow(
            title = "تنظیمات",
            subtitle = "حساب، اعلان‌ها و ظاهر",
            onClick = onSettingsClick
        ) { AccountIcon(painter = Resources.Icon.Settings) }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AccountHeader(name: String, phone: String, onEditClick: () -> Unit) {
    val colors = AppTheme.colors
    val displayName = name.ifBlank { stringResourceSafe() }
    val initials = displayName.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(colors.primary, colors.accent2)))
            .clickable { onEditClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.ifBlank { "؟" },
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            if (phone.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = phone,
                    fontFamily = AppFont(),
                    fontSize = FontSize.REGULAR,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun LoginPromptCard(onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(colors.primary, colors.accent2)))
            .clickable { onClick() }
            .padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "ورود / ثبت‌نام",
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "برای دسترسی به حساب کاربری وارد شوید",
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun AdminPanelCard(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primary)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Resources.Icon.Unlock),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("پنل مدیریت فروشگاه", fontFamily = AppFont(), fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(3.dp))
            Text("داشبورد فروش، محصولات و محتوا", fontFamily = AppFont(), fontSize = FontSize.SMALL, color = Color.White.copy(alpha = 0.85f))
        }
        Icon(
            painter = painterResource(Resources.Icon.RightArrow),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp).graphicsLayer { rotationY = if (isRtl) 180f else 0f }
        )
    }
}

@Composable
private fun AccountRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = AppFont(), fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontFamily = AppFont(), fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
        }
        Icon(
            painter = painterResource(Resources.Icon.RightArrow),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp).graphicsLayer { rotationY = if (isRtl) 180f else 0f }
        )
    }
}

@Composable
private fun AccountIcon(
    painter: org.jetbrains.compose.resources.DrawableResource? = null,
    vector: ImageVector? = null
) {
    val colors = AppTheme.colors
    when {
        painter != null -> Icon(
            painter = painterResource(painter),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(21.dp)
        )
        vector != null -> Icon(
            imageVector = vector,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun stringResourceSafe(): String = "حساب کاربری"
