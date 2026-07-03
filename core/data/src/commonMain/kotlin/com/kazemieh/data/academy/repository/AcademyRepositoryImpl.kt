package com.kazemieh.data.academy.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.AcademyRepository
import com.kazemieh.domain.academy.Certificate
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.CourseFormat
import com.kazemieh.domain.academy.CourseProgress
import com.kazemieh.domain.academy.CourseSection
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.academy.CourseType
import com.kazemieh.domain.academy.Lesson
import com.kazemieh.domain.academy.Quiz
import com.kazemieh.domain.academy.QuizOption
import com.kazemieh.domain.academy.QuizQuestion
import com.kazemieh.domain.academy.QuizResult
import com.kazemieh.domain.academy.VideoVariant
import com.kazemieh.domain.academy.WaitlistResult
import com.kazemieh.network.academy.AcademyApi
import com.kazemieh.network.academy.dto.CertificateResponse
import com.kazemieh.network.academy.dto.CourseDetailResponse
import com.kazemieh.network.academy.dto.CourseSummaryResponse
import com.kazemieh.network.academy.dto.LessonResponse
import com.kazemieh.network.academy.dto.ProgressResponse
import com.kazemieh.network.academy.dto.QuizResponse
import com.kazemieh.network.academy.dto.QuizResultResponse
import com.kazemieh.network.academy.dto.SectionResponse
import com.kazemieh.network.academy.dto.SubmitQuizRequestDto
import com.kazemieh.network.academy.dto.UpdateProgressRequestDto
import com.kazemieh.network.academy.dto.WaitlistResponse
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

    override suspend fun getQuiz(courseId: Long): AppResult<Quiz> = safeApiCall {
        api.getQuiz(courseId).toDomain()
    }

    override suspend fun submitQuiz(courseId: Long, answers: Map<Int, Int>): AppResult<QuizResult> = safeApiCall {
        api.submitQuiz(courseId, SubmitQuizRequestDto(answers)).toDomain()
    }

    override suspend fun getCertificates(): AppResult<List<Certificate>> = safeApiCall {
        api.getCertificates().map { it.toDomain() }
    }

    override suspend fun joinWaitlist(courseId: Long): AppResult<WaitlistResult> = safeApiCall {
        api.joinWaitlist(courseId).toDomain()
    }

    private fun CourseSummaryResponse.toDomain() = CourseSummary(
        id = id, title = title, slug = slug, thumbnailUrl = thumbnailUrl, instructor = instructor,
        price = price, discountedPrice = discountedPrice, lessonCount = lessonCount, enrolled = enrolled,
        courseType = CourseType.from(courseType), format = CourseFormat.from(format), isOnline = isOnline,
        level = level, jobMarketBadge = jobMarketBadge, freeUpdateBadge = freeUpdateBadge
    )

    private fun LessonResponse.toDomain() = Lesson(
        id = id, title = title, durationSeconds = durationSeconds, isFreePreview = isFreePreview,
        videoUrl = videoUrl, completed = completed, lastPositionSeconds = lastPositionSeconds,
        videoVariants = videoVariants.map { VideoVariant(it.quality, it.url) }
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

    private fun WaitlistResponse.toDomain() = WaitlistResult(courseId = courseId, joined = joined, position = position)

    private fun ProgressResponse.toDomain() = CourseProgress(
        courseId = courseId, totalLessons = totalLessons,
        completedLessons = completedLessons, progressPercent = progressPercent
    )

    private fun QuizResponse.toDomain() = Quiz(
        courseId = courseId, title = title, passScore = passScore, alreadyPassed = alreadyPassed,
        questions = questions.map { q ->
            QuizQuestion(index = q.index, text = q.text, options = q.options.map { QuizOption(it.text) })
        }
    )

    private fun QuizResultResponse.toDomain() = QuizResult(
        courseId = courseId, score = score, passed = passed, passScore = passScore,
        certificateNumber = certificateNumber
    )

    private fun CertificateResponse.toDomain() = Certificate(
        id = id, courseId = courseId, courseTitle = courseTitle, certNumber = certNumber,
        issuedAt = issuedAt, userName = userName
    )
}
