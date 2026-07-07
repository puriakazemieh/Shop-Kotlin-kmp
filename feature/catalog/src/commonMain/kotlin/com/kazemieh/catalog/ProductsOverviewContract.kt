package com.kazemieh.catalog

import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.catalog.Banner
import com.kazemieh.domain.catalog.Campaign
import com.kazemieh.domain.catalog.Category
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.story.Story
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.clinic.TherapistSummary
import com.kazemieh.domain.psychtest.PsychTestSummary

data class ProductsOverviewState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val products: List<ProductSummary> = emptyList(),
    val campaign: Campaign? = null,
    val banners: List<Banner> = emptyList(),
    val stories: List<Story> = emptyList(),
    val categories: List<Category> = emptyList(),
    val blogPosts: List<Blog> = emptyList(),
    val recentlyViewed: List<ProductSummary> = emptyList(),
    val isStoriesLoading: Boolean = false,
    val isCategoriesLoading: Boolean = false,
    // ---- پیش‌نمایشِ عمودی‌های دیگر مستقیماً روی صفحه‌ی اصلی (نه فقط داخلِ پروفایل) ----
    val courses: List<CourseSummary> = emptyList(),
    val therapists: List<TherapistSummary> = emptyList(),
    val psychTests: List<PsychTestSummary> = emptyList(),
    val error: Any? = null
)

sealed interface ProductsOverviewIntent {
    data object LoadProducts : ProductsOverviewIntent
    data object Refresh : ProductsOverviewIntent
    data class OnProductClick(val slug: String) : ProductsOverviewIntent
    data class OnCategoryClick(val id: Long, val name: String) : ProductsOverviewIntent
    data class OnFavoriteClick(val product: ProductSummary) : ProductsOverviewIntent
    data class OnStoryClick(val index: Int) : ProductsOverviewIntent
    data class OnStorySeen(val id: Long) : ProductsOverviewIntent
}

sealed interface ProductsOverviewEffect {
    data class NavigateToDetails(val slug: String) : ProductsOverviewEffect
    data class NavigateToCategory(val id: Long, val name: String) : ProductsOverviewEffect
    data class NavigateToStory(val initialIndex: Int) : ProductsOverviewEffect
    data object NavigateToAuth : ProductsOverviewEffect
}
