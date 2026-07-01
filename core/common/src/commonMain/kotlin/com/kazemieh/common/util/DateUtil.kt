package com.kazemieh.common.util

fun formatDateTime(isoString: String?): String {
    if (isoString == null) return ""
    // Input format: 2023-10-27T10:30:00Z
    return try {
        val parts = isoString.split("T")
        if (parts.size < 2) return isoString
        
        val datePart = parts[0] // 2023-10-27
        val timePart = parts[1].take(5) // 10:30
        
        val dateParts = datePart.split("-")
        if (dateParts.size < 3) return "$datePart $timePart"
        
        "${dateParts[0]}/${dateParts[1]}/${dateParts[2]} $timePart"
    } catch (e: Exception) {
        isoString
    }
}

/** ارقام لاتین را به ارقام فارسی تبدیل می‌کند. */
fun String.toPersianDigits(): String {
    val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString {
        for (c in this@toPersianDigits) {
            if (c in '0'..'9') append(fa[c - '0']) else append(c)
        }
    }
}

/** تاریخِ کوتاهِ فارسی برای نمایش کنارِ نظرها/پرسش‌ها (بدون ساعت). */
fun formatShortDateFa(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val datePart = isoString.split("T").firstOrNull() ?: return ""
        val p = datePart.split("-")
        if (p.size < 3) datePart.toPersianDigits() else "${p[0]}/${p[1]}/${p[2]}".toPersianDigits()
    } catch (e: Exception) {
        ""
    }
}
