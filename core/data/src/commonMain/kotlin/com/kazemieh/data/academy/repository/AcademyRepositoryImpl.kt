package com.kazemieh.data.academy.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.AcademyRepository
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.CourseProgress
import com.kazemieh.domain.academy.CourseSection
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.academy.Lesson
import com.kazemieh.network.academy.AcademyApi
import com.kazemieh.network.academy.dto.CourseDetailResponse
import com.kazemieh.network.academy.dto.CourseSummaryResponse
import com.kazemieh.network.academy.dto.LessonResponse
import com.kazemieh.network.academy.dto.ProgressResponse
import com.kazemieh.network.academy.dto.SectionResponse
import com.kazemieh.network.academy.dto.UpdateProgressRequestDto
import com.kazemieh.network.common.safeApiCall

class AcademyRepositoryImpl(
    private val api: AcademyApi
) : AcademyRepository {

    override suspend fun getCourses(): AppResult<List<CourseSummary>> = safeApiCall {
        api.getCourses().map { it.toDomain() }
    }

    override suspend fun getCourse(slug: String): AppResult<CourseDetail> = safeApiCall {
        api.getCourse(slug).toDomain()
    }

    override suspend fun getMyCourses(): AppResult<List<CourseSummary>> = safeApiCall {
        api.getMyCourses().map { it.toDomain() }
    }

    override suspend fun enroll(courseId: Long): AppResult<CourseDetail> = safeApiCall {
        api.enroll(courseId).toDomain()
    }

    override suspend fun updateLessonProgress(
        lessonId: Long,
        completed: Boolean?,
        lastPositionSeconds: Int?
    ): AppResult<CourseProgress> = safeApiCall {
        api.updateLessonProgress(lessonId, UpdateProgressRequestDto(completed, lastPositionSeconds)).toDomain()
    }

    private fun CourseSummaryResponse.toDomain() = CourseSummary(
        id = id, title = title, slug = slug, thumbnailUrl = thumbnailUrl, instructor = instructor,
        price = price, discountedPrice = discountedPrice, lessonCount = lessonCount, enrolled = enrolled
    )

    private fun LessonResponse.toDomain() = Lesson(
        id = id, title = title, durationSeconds = durationSeconds, isFreePreview = isFreePreview,
        videoUrl = videoUrl, completed = completed, lastPositionSeconds = lastPositionSeconds
    )

    private fun SectionResponse.toDomain() = CourseSection(
        id = id, title = title, lessons = lessons.map { it.toDomain() }
    )

    private fun CourseDetailResponse.toDomain() = CourseDetail(
        id = id, title = title, slug = slug, description = description, thumbnailUrl = thumbnailUrl,
        instructor = instructor, price = price, discountedPrice = discountedPrice,
        enrolled = enrolled, progressPercent = progressPercent, sections = sections.map { it.toDomain() }
    )

    private fun ProgressResponse.toDomain() = CourseProgress(
        courseId = courseId, totalLessons = totalLessons,
        completedLessons = completedLessons, progressPercent = progressPercent
    )
}
