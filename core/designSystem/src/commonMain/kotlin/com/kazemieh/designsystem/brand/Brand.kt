package com.kazemieh.designsystem.brand

import androidx.compose.ui.graphics.Color
import com.kazemieh.designsystem.AccentLight
import com.kazemieh.designsystem.Accent2Light
import com.kazemieh.designsystem.AccentSoftLight
import com.kazemieh.designsystem.GoldLight
import com.kazemieh.designsystem.GoldSoftLight
import com.kazemieh.designsystem.BgLight
import com.kazemieh.designsystem.SurfaceLight
import com.kazemieh.designsystem.SurfaceVariantLight
import com.kazemieh.designsystem.LineLight
import com.kazemieh.designsystem.OutlineVariantLight
import com.kazemieh.designsystem.InkLight
import com.kazemieh.designsystem.InkSoftLight
import com.kazemieh.designsystem.AccentDark
import com.kazemieh.designsystem.Accent2Dark
import com.kazemieh.designsystem.AccentSoftDark
import com.kazemieh.designsystem.GoldDark
import com.kazemieh.designsystem.GoldSoftDark
import com.kazemieh.designsystem.BgDark
import com.kazemieh.designsystem.SurfaceDark
import com.kazemieh.designsystem.SurfaceVariantDark
import com.kazemieh.designsystem.LineDark
import com.kazemieh.designsystem.OutlineVariantDark
import com.kazemieh.designsystem.InkDark
import com.kazemieh.designsystem.InkSoftDark
import com.kazemieh.designsystem.SaleLight
import com.kazemieh.designsystem.SaleDark
import com.kazemieh.designsystem.StarLight
import com.kazemieh.designsystem.StarDark
import com.kazemieh.designsystem.OkLight
import com.kazemieh.designsystem.OkDark

/**
 * پالتِ رنگیِ یک برند برای یک تم (روشن یا تیره). این همان بذرهایی است که
 * از رویشان هم `MaterialTheme.colorScheme` و هم `AppColors` ساخته می‌شود.
 */
data class BrandPalette(
    val accent: Color,
    val accent2: Color,
    val accentSoft: Color,
    val gold: Color,
    val goldSoft: Color,
    val bg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val line: Color,
    val outlineVariant: Color,
    val ink: Color,
    val inkSoft: Color,
    val sale: Color,
    val star: Color,
    val ok: Color,
    val onAccent: Color = Color.White,
    val onGold: Color = Color.White
)

/** پالتِ روشن + تیرهٔ یک برند. */
data class BrandColors(
    val light: BrandPalette,
    val dark: BrandPalette
)

/**
 * پرچم‌های فعال/غیرفعالِ قابلیت‌ها per brand (برای مخفی/نمایشِ بخش‌ها).
 * عمودی‌های اختیاری (آموزشگاه/مشاوره/تست روان‌شناسی/مقایسه‌ی محصول) هرکدام
 * مستقل روشن/خاموش می‌شوند تا هر برند فقط فیچرهایی را که نیاز دارد نشان دهد
 * (مثلاً فروشگاه ساعت هیچ‌کدام را نمی‌خواهد، آموزشگاه فقط `academy` را می‌خواهد،
 * مرکز مشاوره هر سه‌ی `academy`/`clinic`/`psychTests` را می‌خواهد).
 */
data class BrandFeatures(
    val wallet: Boolean = true,
    val blog: Boolean = true,
    val stories: Boolean = true,
    val support: Boolean = true,
    val customerClub: Boolean = true,
    /** عمودیِ آموزشگاه (دوره/سمینار/کارگاه + پخش‌کننده). */
    val academy: Boolean = false,
    /** عمودیِ مشاوره/نوبت‌دهی (درمانگرها + رزرو + اعتبار جلسه). */
    val clinic: Boolean = false,
    /** عمودیِ تست‌های روان‌شناسیِ قابل‌خرید (تفسیر خودکار یا توسط مشاور). */
    val psychTests: Boolean = false,
    /** مقایسه‌ی محصولات (فیچرِ عمومیِ فروشگاهی، مستقل از عمودی‌ها). */
    val productComparison: Boolean = false,
    // ---- زیرفیچرهای مستقلِ آموزشگاه (هرکدام جدا از خودِ `academy` روشن/خاموش می‌شوند) ----
    /** آزمونِ پایانِ دوره + گواهی (کوییز/چک‌پوینتِ درس هم همین پرچم را دارد). */
    val academyQuiz: Boolean = false,
    /** تبِ «دوره‌های رایگان» در فهرستِ دوره‌ها. */
    val academyFreeCoursesTab: Boolean = false,
    /** صفحه‌ی عمومیِ پروفایلِ مدرس (فهرستِ دوره‌های یک مدرسِ خاص). */
    val academyInstructorProfiles: Boolean = false,
    /** ارزیابیِ پروژه‌محورِ پایانِ دوره (کنارِ/به‌جایِ آزمونِ چندگزینه‌ای). */
    val academyProjectAssessment: Boolean = false,
    /** فایل‌های ضمیمه‌ی درس (جزوه/کدِ نمونه) کنارِ ویدیو. */
    val academyLessonResources: Boolean = false,
    // ---- زیرفیچرِ فروشگاهی ----
    /** باندل/پکیجِ ترکیبیِ محصول. */
    val productBundles: Boolean = false,
    // ---- زیرفیچرهای مستقلِ مشاوره ----
    /** پرونده‌ی کاملِ مراجع (نوبت‌ها + یادداشت‌ها + نتایجِ تست در یک نما). */
    val clinicPatientFile: Boolean = false,
    /** CRMِ سبکِ مراجعان (فهرستِ مراجعانِ هر درمانگر + برچسب). */
    val clinicCrm: Boolean = false
)

/**
 * پیکربندیِ کاملِ یک برند/فروشگاه. نمونه‌ی فعال در زمانِ اجرا از طریق Koin تزریق می‌شود
 * و در زمانِ build (فلِیورِ اندروید / آرگومان اجرا) انتخاب می‌شود.
 */
data class BrandConfig(
    val id: String,
    val appName: String,
    val colors: BrandColors,
    val currency: String = "تومان",
    /** اگر مقدار داشته باشد، BASE_URL شبکه را override می‌کند؛ در غیر این صورت PlatformConfig. */
    val apiBaseUrl: String? = null,
    val features: BrandFeatures = BrandFeatures()
)

// =====================================================================================
//  برندهای از پیش تعریف‌شده
// =====================================================================================

/** برندِ پیش‌فرض: کارمیلا (پالتِ فعلیِ دیزاین‌سیستم — بدون تغییرِ بصری). */
val CarmilaBrandColors = BrandColors(
    light = BrandPalette(
        accent = AccentLight, accent2 = Accent2Light, accentSoft = AccentSoftLight,
        gold = GoldLight, goldSoft = GoldSoftLight,
        bg = BgLight, surface = SurfaceLight, surfaceVariant = SurfaceVariantLight,
        line = LineLight, outlineVariant = OutlineVariantLight,
        ink = InkLight, inkSoft = InkSoftLight,
        sale = SaleLight, star = StarLight, ok = OkLight,
        onAccent = Color.White, onGold = Color.White
    ),
    dark = BrandPalette(
        accent = AccentDark, accent2 = Accent2Dark, accentSoft = AccentSoftDark,
        gold = GoldDark, goldSoft = GoldSoftDark,
        bg = BgDark, surface = SurfaceDark, surfaceVariant = SurfaceVariantDark,
        line = LineDark, outlineVariant = OutlineVariantDark,
        ink = InkDark, inkSoft = InkSoftDark,
        sale = SaleDark, star = StarDark, ok = OkDark,
        onAccent = Color(0xFF0F1320), onGold = Color(0xFF2A2113)
    )
)

val CarmilaBrand = BrandConfig(
    id = "carmila",
    appName = "کارمیلا",
    colors = CarmilaBrandColors,
    currency = "تومان",
    // برندِ نمایشیِ اصلی؛ همه‌ی فیچرها برای دمو روشن‌اند تا هر قابلیتی قابلِ بررسی باشد.
    features = BrandFeatures(
        academy = true, clinic = true, psychTests = true, productComparison = true,
        academyQuiz = true, academyFreeCoursesTab = true, academyInstructorProfiles = true,
        academyProjectAssessment = true, academyLessonResources = true,
        productBundles = true, clinicPatientFile = true, clinicCrm = true
    )
)

/** نمونه‌ی برندِ دومِ عطرفروشی «آتریس» — پالتِ گرم/طلایی. */
val AtrisBrandColors = BrandColors(
    light = BrandPalette(
        accent = Color(0xFF6B4E2E), accent2 = Color(0xFF8A6A3F), accentSoft = Color(0xFFF1E7D8),
        gold = Color(0xFFB8912F), goldSoft = Color(0xFFF3ECDA),
        bg = Color(0xFFF8F4EC), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFF1EBDE),
        line = Color(0xFFE6DCC9), outlineVariant = Color(0xFFD8CBB2),
        ink = Color(0xFF2C2417), inkSoft = Color(0xFF7A6E58),
        sale = Color(0xFFC24B3A), star = Color(0xFFE0A93B), ok = Color(0xFF3E9B6B),
        onAccent = Color.White, onGold = Color.White
    ),
    dark = BrandPalette(
        accent = Color(0xFFC7A16A), accent2 = Color(0xFFD6B681), accentSoft = Color(0xFF33291A),
        gold = Color(0xFFD8B25A), goldSoft = Color(0xFF3A2F1C),
        bg = Color(0xFF17130D), surface = Color(0xFF201A12), surfaceVariant = Color(0xFF2B2419),
        line = Color(0xFF382F22), outlineVariant = Color(0xFF463A29),
        ink = Color(0xFFF1EADA), inkSoft = Color(0xFFB3A88F),
        sale = Color(0xFFE0654E), star = Color(0xFFE0A93B), ok = Color(0xFF43B57F),
        onAccent = Color(0xFF17130D), onGold = Color(0xFF2A2113)
    )
)

val AtrisBrand = BrandConfig(
    id = "atris",
    appName = "آتریس",
    colors = AtrisBrandColors,
    currency = "تومان",
    features = BrandFeatures(productBundles = true)
)

/** نمونه‌ی برندِ سومِ ساعت‌فروشی «کرونوس» — پالتِ سرد/مشکی-نقره‌ای. */
val ChronosBrandColors = BrandColors(
    light = BrandPalette(
        accent = Color(0xFF1F2A33), accent2 = Color(0xFF35434E), accentSoft = Color(0xFFE6EAED),
        gold = Color(0xFF8A8F98), goldSoft = Color(0xFFECEEF0),
        bg = Color(0xFFF4F5F6), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFECEEF0),
        line = Color(0xFFDFE3E6), outlineVariant = Color(0xFFCBD1D6),
        ink = Color(0xFF12181D), inkSoft = Color(0xFF61707A),
        sale = Color(0xFFC33B3B), star = Color(0xFFD9A441), ok = Color(0xFF2E9E6B),
        onAccent = Color.White, onGold = Color.White
    ),
    dark = BrandPalette(
        accent = Color(0xFF9FB4C2), accent2 = Color(0xFFB4C6D1), accentSoft = Color(0xFF212B32),
        gold = Color(0xFFB9BEC6), goldSoft = Color(0xFF2A2F35),
        bg = Color(0xFF0E1216), surface = Color(0xFF161B20), surfaceVariant = Color(0xFF1F262C),
        line = Color(0xFF2A323A), outlineVariant = Color(0xFF37414A),
        ink = Color(0xFFECEFF2), inkSoft = Color(0xFF9AA6AE),
        sale = Color(0xFFDE5A5A), star = Color(0xFFD9A441), ok = Color(0xFF3EB07E),
        onAccent = Color(0xFF0E1216), onGold = Color(0xFF161B20)
    )
)

val ChronosBrand = BrandConfig(
    id = "chronos",
    appName = "کرونوس",
    colors = ChronosBrandColors,
    currency = "تومان",
    features = BrandFeatures(productBundles = true)
)

/** برندِ آموزشگاه «کاظمیه» — پالتِ آبی، فقط عمودیِ آموزشگاه روشن است. */
val AcademyBrandColors = BrandColors(
    light = BrandPalette(
        accent = Color(0xFF1E3A6E), accent2 = Color(0xFF2E5090), accentSoft = Color(0xFFE7EDF7),
        gold = Color(0xFFC9A45C), goldSoft = Color(0xFFF3ECDA),
        bg = Color(0xFFF5F7FA), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFEDF1F7),
        line = Color(0xFFDEE5EF), outlineVariant = Color(0xFFCBD5E3),
        ink = Color(0xFF16223A), inkSoft = Color(0xFF66748F),
        sale = Color(0xFFD8453B), star = Color(0xFFE7A93B), ok = Color(0xFF1F9D6B),
        onAccent = Color.White, onGold = Color.White
    ),
    dark = BrandPalette(
        accent = Color(0xFF7C97D6), accent2 = Color(0xFF93AAE0), accentSoft = Color(0xFF1C2740),
        gold = Color(0xFFD8B872), goldSoft = Color(0xFF352C1C),
        bg = Color(0xFF0E1420), surface = Color(0xFF161E30), surfaceVariant = Color(0xFF1E2A40),
        line = Color(0xFF2A374F), outlineVariant = Color(0xFF37455E),
        ink = Color(0xFFE8ECF5), inkSoft = Color(0xFF9BA8C2),
        sale = Color(0xFFE06B62), star = Color(0xFFE7A93B), ok = Color(0xFF3EB07E),
        onAccent = Color(0xFF0E1420), onGold = Color(0xFF2A2113)
    )
)

val AcademyBrand = BrandConfig(
    id = "academy",
    appName = "آموزشگاه کاظمیه",
    colors = AcademyBrandColors,
    currency = "تومان",
    features = BrandFeatures(
        // آموزشگاه علاوه بر دوره‌ها، نوبت‌دهیِ مشاوره و تست‌های روان‌شناسی را هم دارد،
        // بنابراین این عمودی‌ها روی صفحه‌ی اصلی نمایش داده می‌شوند.
        academy = true, clinic = true, psychTests = true, productComparison = false,
        academyQuiz = true, academyFreeCoursesTab = true, academyInstructorProfiles = true,
        academyProjectAssessment = true, academyLessonResources = true,
        clinicPatientFile = true, clinicCrm = true
    )
)

/** برندِ مرکزِ مشاوره «مهرجو» — پالتِ زرشکی/طلایی، عمودی‌های آموزشگاه+مشاوره+تست روشن‌اند. */
val PsychBrandColors = BrandColors(
    light = BrandPalette(
        accent = Color(0xFF6E1E2E), accent2 = Color(0xFF8A2E40), accentSoft = Color(0xFFF3E4E7),
        gold = Color(0xFFC9A227), goldSoft = Color(0xFFF6EFD6),
        bg = Color(0xFFFAF6F1), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFF2E9E7),
        line = Color(0xFFE7DAD6), outlineVariant = Color(0xFFD8C6C1),
        ink = Color(0xFF2C1418), inkSoft = Color(0xFF7A5E62),
        sale = Color(0xFFC1392B), star = Color(0xFFE0B93B), ok = Color(0xFF2E8B57),
        onAccent = Color.White, onGold = Color(0xFF2C1418)
    ),
    dark = BrandPalette(
        accent = Color(0xFFD68A9A), accent2 = Color(0xFFE0A0AD), accentSoft = Color(0xFF33181E),
        gold = Color(0xFFE0C25A), goldSoft = Color(0xFF3A311C),
        bg = Color(0xFF1A1114), surface = Color(0xFF23181C), surfaceVariant = Color(0xFF2D2024),
        line = Color(0xFF3A2A2F), outlineVariant = Color(0xFF48353B),
        ink = Color(0xFFF3E7E9), inkSoft = Color(0xFFC2A6AC),
        sale = Color(0xFFE0654E), star = Color(0xFFE0C25A), ok = Color(0xFF43B57F),
        onAccent = Color(0xFF1A1114), onGold = Color(0xFF2A2113)
    )
)

val PsychBrand = BrandConfig(
    id = "psych",
    appName = "مرکز مشاوره مهرجو",
    colors = PsychBrandColors,
    currency = "تومان",
    features = BrandFeatures(
        academy = true, clinic = true, psychTests = true, productComparison = false,
        academyQuiz = true, academyFreeCoursesTab = true, academyInstructorProfiles = true,
        academyProjectAssessment = true, academyLessonResources = true,
        clinicPatientFile = true, clinicCrm = true
    )
)

/**
 * برندِ «وردپرس» — همان اپِ کارمیلا که به‌جای سرورِ Spring Boot به یک سایتِ
 * وردپرس (پلاگینِ Carmilla Bridge) وصل می‌شود. تنها تفاوت با CarmilaBrand،
 * مقدارِ `apiBaseUrl` است که BASE_URL شبکه را به `/wp-json/carmilla/v1/` سایت
 * تغییر می‌دهد (از طریقِ ApiConfig.baseUrlOverride در initKoin). صاحبِ هر سایت
 * فقط همین یک آدرس را به دامنه‌ی خودش تغییر می‌دهد (الگویِ White-Label).
 */
val WpBrand = BrandConfig(
    id = "wp",
    appName = "کارمیلا",
    colors = CarmilaBrandColors,
    currency = "تومان",
    // آدرسِ سایتِ وردپرس را اینجا تنظیم کنید (باید به «/» ختم شود).
    apiBaseUrl = "https://example.com/wp-json/carmilla/v1/",
    features = BrandFeatures(
        academy = true, clinic = true, psychTests = true, productComparison = true,
        academyQuiz = true, academyFreeCoursesTab = true, academyInstructorProfiles = true,
        academyProjectAssessment = true, academyLessonResources = true,
        productBundles = true, clinicPatientFile = true, clinicCrm = true
    )
)

/**
 * رجیستریِ برندها. انتخابِ برندِ فعال در زمانِ اجرا بر اساسِ شناسه (از فلِیور/آرگومان).
 */
object BrandRegistry {
    val default: BrandConfig = CarmilaBrand

    private val all: List<BrandConfig> =
        listOf(CarmilaBrand, AtrisBrand, ChronosBrand, AcademyBrand, PsychBrand, WpBrand)

    fun byId(id: String?): BrandConfig =
        all.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: default
}
