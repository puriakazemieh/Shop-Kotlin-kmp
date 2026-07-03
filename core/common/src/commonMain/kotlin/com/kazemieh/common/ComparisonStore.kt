package com.kazemieh.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * فهرستِ محصولاتِ انتخاب‌شده برای مقایسه (session-scoped، در حافظه).
 * فقط اسلاگِ محصول نگه‌داری می‌شود؛ صفحه‌ی مقایسه جزئیاتِ هرکدام را جداگانه می‌گیرد.
 * برای سادگی به‌صورتِ object جهانی است تا هم صفحه‌ی محصول و هم صفحه‌ی مقایسه بدونِ DI به آن دسترسی داشته باشند.
 */
object ComparisonStore {
    /** بیشترین تعدادِ محصولِ قابلِ مقایسه‌ی هم‌زمان. */
    const val MAX_ITEMS = 4

    private val _slugs = MutableStateFlow<List<String>>(emptyList())
    val slugs: StateFlow<List<String>> = _slugs.asStateFlow()

    fun contains(slug: String): Boolean = _slugs.value.contains(slug)

    /** افزودن/حذفِ یک محصول از فهرستِ مقایسه. اگر به سقف رسیده باشد، افزودنِ جدید نادیده گرفته می‌شود. */
    fun toggle(slug: String) {
        val current = _slugs.value
        _slugs.value = when {
            current.contains(slug) -> current - slug
            current.size >= MAX_ITEMS -> current
            else -> current + slug
        }
    }

    fun remove(slug: String) {
        _slugs.value = _slugs.value - slug
    }

    fun clear() {
        _slugs.value = emptyList()
    }
}
