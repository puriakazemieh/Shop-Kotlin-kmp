package com.kazemieh.designsystem.util

import kotlin.math.roundToLong

/**
 * قیمت را با جداکننده‌ی هزارگان و بدونِ اعشارِ اضافی برمی‌گرداند.
 * نمونه: 1450000.0 -> "۱٬۴۵۰٬۰۰۰" (رقم‌ها با فونتِ فارسی نمایش داده می‌شوند).
 * فقط عدد را قالب‌بندی می‌کند؛ واحد («تومان») باید بیرون اضافه شود.
 */
fun formatToman(value: Number?): String {
    val n = (value?.toDouble() ?: 0.0).roundToLong()
    val negative = n < 0
    val s = (if (negative) -n else n).toString()
    val sb = StringBuilder()
    for ((i, c) in s.withIndex()) {
        if (i > 0 && (s.length - i) % 3 == 0) sb.append('٬')
        sb.append(c)
    }
    return if (negative) "-$sb" else sb.toString()
}
