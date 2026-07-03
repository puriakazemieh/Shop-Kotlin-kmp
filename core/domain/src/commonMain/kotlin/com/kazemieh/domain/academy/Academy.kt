package com.kazemieh.domain.academy

/** نوعِ فعالیت و شکلِ برگزاری (هم‌ارز با enumهای سرور). */
enum class CourseType { COURSE, SEMINAR, WORKSHOP;
    companion object { fun from(v: String?) = entries.firstOrNull { it.name == v } ?: COURSE }
    val label: String get() = when (this) {
        COURSE -> "دوره"; SEMINAR -> "سمینار"; WORKSHOP -> "کارگاه"
    }
}

enum class CourseFormat { ONLINE_RECORDED, ONLINE_LIVE, IN_PERSON, OFFLINE;
    companion object { fun from(v: String?) = entries.firstOrNull { it.name == v } ?: ONLINE_RECORDED }
    val isOnline: Boolean get() = this == ONLINE_RECORDED || this == ONLINE_LIVE
    val label: String get() = when (this) {
        ONLINE_RECORDED -> "آنلاین (ضبط‌شده)"; ONLINE_LIVE -> "آنلاین (زنده)"
        IN_PERSON -> "حضوری"; OFFLINE -> "آفلاین"
    }
}

data class CourseSummary(
    val id: Long,
    val title: String,
    val slug: String,
    val thumbnailUrl: String?,
    val instructor: String?,
    val price: Double,
    val discountedPrice: Double?,
    val lessonCount: Int,
    val enrolled: Boolean,
    val courseType: CourseType = CourseType.COURSE,
    val format: CourseFormat = CourseFormat.ONLINE_RECORDED,
    val isOnline: Boolean = true,
    val level: String? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false
)

data class Lesson(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val isFreePreview: Boolean,
    val videoUrl: String?,
    val completed: Boolean,
    val lastPositionSeconds: Int
)

data class CourseSection(
    val id: Long,
    val title: String,
    val lessons: List<Lesson>
)

data class CourseDetail(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?,
    val instructor: String?,
    val price: Double,
    val discountedPrice: Double?,
    val enrolled: Boolean,
    val progressPercent: Int,
    val sections: List<CourseSection>,
    val courseType: CourseType = CourseType.COURSE,
    val format: CourseFormat = CourseFormat.ONLINE_RECORDED,
    val isOnline: Boolean = true,
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val seatsTaken: Int = 0,
    val seatsRemaining: Int? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val instructorBio: String? = null,
    val instructorSkills: List<String> = emptyList()
)

data class CourseProgress(
    val courseId: Long,
    val totalLessons: Int,
    val completedLessons: Int,
    val progressPercent: Int
)
