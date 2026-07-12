package com.kazemieh.domain.psychtest

enum class TestResultMode { AUTO, COUNSELOR;
    companion object { fun from(v: String?) = entries.firstOrNull { it.name == v } ?: AUTO }
}

enum class UserTestStatus { PURCHASED, IN_PROGRESS, AWAITING_INTERPRETATION, COMPLETED;
    companion object { fun from(v: String?) = entries.firstOrNull { it.name == v } ?: PURCHASED }
    val label: String get() = when (this) {
        PURCHASED -> "خریداری‌شده"; IN_PROGRESS -> "در حالِ انجام"
        AWAITING_INTERPRETATION -> "در انتظارِ تفسیر"; COMPLETED -> "تکمیل‌شده"
    }
}

data class PsychTestSummary(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val price: Double,
    val discountedPrice: Double?,
    val resultMode: TestResultMode,
    val questionCount: Int,
    val owned: Boolean,
    val productId: Long?,
    val productSlug: String? = null
)

data class TestOption(val text: String)

data class TestQuestion(
    val index: Int,
    val text: String,
    val options: List<TestOption>
)

data class PsychTestDetail(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val price: Double,
    val discountedPrice: Double?,
    val resultMode: TestResultMode,
    val questions: List<TestQuestion>,
    val owned: Boolean,
    val productId: Long?
)

data class UserPsychTest(
    val id: Long,
    val testId: Long,
    val testTitle: String,
    val status: UserTestStatus,
    val resultMode: TestResultMode,
    val totalScore: Int?,
    val interpretation: String?,
    val completedAt: String?
)

/** یک سؤالِ تست در فرمِ تست‌سازِ ادمین. */
data class AdminTestQuestion(
    val text: String,
    /** گزینه‌ها همراهِ امتیازِ هرکدام. */
    val options: List<Pair<String, Int>>
)

data class AdminScoreRange(
    val minScore: Int,
    val maxScore: Int,
    val interpretation: String
)
