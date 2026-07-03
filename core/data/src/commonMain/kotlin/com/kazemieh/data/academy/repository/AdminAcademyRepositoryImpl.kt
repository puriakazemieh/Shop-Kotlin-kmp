package com.kazemieh.data.academy.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.AdminAcademyRepository
import com.kazemieh.domain.academy.AdminCourseParams
import com.kazemieh.domain.academy.AdminCourseUpdateParams
import com.kazemieh.domain.academy.AdminQuizQuestion
import com.kazemieh.domain.academy.AdminWaitlistEntry
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.CourseFormat
import com.kazemieh.domain.academy.CourseSection
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.academy.CourseType
import com.kazemieh.domain.academy.Lesson
import com.kazemieh.network.academy.AdminAcademyApi
import com.kazemieh.network.academy.dto.AdminCreateCourseRequestDto
import com.kazemieh.network.academy.dto.AdminCreateLessonRequestDto
import com.kazemieh.network.academy.dto.AdminCreateSectionRequestDto
import com.kazemieh.network.academy.dto.AdminUpdateCourseRequestDto
import com.kazemieh.network.academy.dto.AdminUpsertQuizRequestDto
import com.kazemieh.network.academy.dto.AdminWaitlistEntryResponse
import com.kazemieh.network.academy.dto.CourseDetailResponse
import com.kazemieh.network.academy.dto.CourseSummaryResponse
import com.kazemieh.network.academy.dto.LessonResponse
import com.kazemieh.network.academy.dto.QuizOptionResponse
import com.kazemieh.network.academy.dto.QuizQuestionResponse
import com.kazemieh.network.academy.dto.SectionResponse
import com.kazemieh.network.common.safeApiCall

class AdminAcademyRepositoryImpl(
    private val api: AdminAcademyApi
) : AdminAcademyRepository {

    override suspend fun listCourses(): AppResult<List<CourseSummary>> = safeApiCall {
        api.listCourses().map { it.toDomain() }
    }

    override suspend fun getCourseDetail(id: Long): AppResult<CourseDetail> = safeApiCall {
        api.getCourseDetail(id).toDomain()
    }

    override suspend fun createCourse(params: AdminCourseParams): AppResult<Long> = safeApiCall {
        api.createCourse(
            AdminCreateCourseRequestDto(
                title = params.title,
                slug = params.slug,
                description = params.description,
                thumbnailUrl = params.thumbnailUrl,
                instructor = params.instructor,
                price = params.price,
                discountedPrice = params.discountedPrice,
                productId = params.productId,
                isPublished = params.isPublished,
                courseType = params.courseType,
                format = params.format,
                level = params.level,
                location = params.location,
                capacity = params.capacity,
                jobMarketBadge = params.jobMarketBadge,
                freeUpdateBadge = params.freeUpdateBadge,
                instructorBio = params.instructorBio,
                instructorSkills = params.instructorSkills
            )
        )
    }

    override suspend fun updateCourse(id: Long, params: AdminCourseUpdateParams): AppResult<Unit> = safeApiCall {
        api.updateCourse(
            id,
            AdminUpdateCourseRequestDto(
                title = params.title,
                description = params.description,
                thumbnailUrl = params.thumbnailUrl,
                instructor = params.instructor,
                price = params.price,
                discountedPrice = params.discountedPrice,
                isPublished = params.isPublished
            )
        )
    }

    override suspend fun deleteCourse(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteCourse(id)
    }

    override suspend fun addSection(courseId: Long, title: String, sortOrder: Int): AppResult<Long> = safeApiCall {
        api.addSection(courseId, AdminCreateSectionRequestDto(title = title, sortOrder = sortOrder))
    }

    override suspend fun addLesson(
        courseId: Long,
        sectionId: Long,
        title: String,
        videoUrl: String?,
        durationSeconds: Int,
        sortOrder: Int,
        isFreePreview: Boolean
    ): AppResult<Long> = safeApiCall {
        api.addLesson(
            courseId, sectionId,
            AdminCreateLessonRequestDto(
                title = title, videoUrl = videoUrl, durationSeconds = durationSeconds,
                sortOrder = sortOrder, isFreePreview = isFreePreview
            )
        )
    }

    override suspend fun upsertQuiz(
        courseId: Long,
        title: String,
        passScore: Int,
        questions: List<AdminQuizQuestion>
    ): AppResult<Unit> = safeApiCall {
        api.upsertQuiz(
            courseId,
            AdminUpsertQuizRequestDto(
                title = title,
                passScore = passScore,
                questions = questions.mapIndexed { i, q ->
                    QuizQuestionResponse(
                        index = i,
                        text = q.text,
                        options = q.options.mapIndexed { oi, opt ->
                            QuizOptionResponse(text = opt, correct = oi == q.correctIndex)
                        }
                    )
                }
            )
        )
    }

    override suspend fun listWaitlist(courseId: Long): AppResult<List<AdminWaitlistEntry>> = safeApiCall {
        api.listWaitlist(courseId).map { it.toDomain() }
    }

    override suspend fun notifyNextInWaitlist(courseId: Long): AppResult<AdminWaitlistEntry?> = safeApiCall {
        api.notifyNextInWaitlist(courseId).entry?.toDomain()
    }

    private fun AdminWaitlistEntryResponse.toDomain() = AdminWaitlistEntry(
        id = id, userId = userId, notified = notified, createdAt = createdAt, notifiedAt = notifiedAt
    )

    private fun CourseSummaryResponse.toDomain() = CourseSummary(
        id = id, title = title, slug = slug, thumbnailUrl = thumbnailUrl, instructor = instructor,
        price = price, discountedPrice = discountedPrice, lessonCount = lessonCount, enrolled = enrolled,
        courseType = CourseType.from(courseType), format = CourseFormat.from(format), isOnline = isOnline,
        level = level, jobMarketBadge = jobMarketBadge, freeUpdateBadge = freeUpdateBadge
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
        enrolled = enrolled, progressPercent = progressPercent, sections = sections.map { it.toDomain() },
        courseType = CourseType.from(courseType), format = CourseFormat.from(format), isOnline = isOnline,
        level = level, location = location, capacity = capacity, seatsTaken = seatsTaken,
        seatsRemaining = seatsRemaining, jobMarketBadge = jobMarketBadge, freeUpdateBadge = freeUpdateBadge,
        instructorBio = instructorBio, instructorSkills = instructorSkills,
        isFull = isFull, onWaitlist = onWaitlist, productId = productId
    )
}
