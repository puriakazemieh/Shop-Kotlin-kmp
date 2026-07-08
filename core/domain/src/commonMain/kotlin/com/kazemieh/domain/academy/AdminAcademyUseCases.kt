package com.kazemieh.domain.academy

class UploadCourseMediaUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(fileBytes: ByteArray, fileName: String) = repository.uploadMedia(fileBytes, fileName)
}

class GetAdminCoursesUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke() = repository.listCourses()
}

class GetAdminCourseDetailUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(id: Long) = repository.getCourseDetail(id)
}

class CreateCourseUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(params: AdminCourseParams) = repository.createCourse(params)
}

class UpdateCourseUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(id: Long, params: AdminCourseUpdateParams) = repository.updateCourse(id, params)
}

class DeleteCourseUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteCourse(id)
}

class AddCourseSectionUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long, title: String, sortOrder: Int = 0) =
        repository.addSection(courseId, title, sortOrder)
}

class AddCourseLessonUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(
        courseId: Long,
        sectionId: Long,
        title: String,
        videoUrl: String? = null,
        durationSeconds: Int = 0,
        sortOrder: Int = 0,
        isFreePreview: Boolean = false,
        subtitleLanguage: String? = null,
        subtitleUrl: String? = null
    ) = repository.addLesson(courseId, sectionId, title, videoUrl, durationSeconds, sortOrder, isFreePreview, subtitleLanguage, subtitleUrl)
}

class UpsertCourseQuizUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long, title: String, passScore: Int, questions: List<AdminQuizQuestion>) =
        repository.upsertQuiz(courseId, title, passScore, questions)
}

class GetAdminWaitlistUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.listWaitlist(courseId)
}

class NotifyNextInWaitlistUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.notifyNextInWaitlist(courseId)
}

class AddLessonFileUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long, lessonId: Long, fileBytes: ByteArray, fileName: String, displayName: String) =
        repository.addLessonFile(courseId, lessonId, fileBytes, fileName, displayName)
}

class AddLessonFileByLinkUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long, lessonId: Long, name: String, url: String, sizeLabel: String? = null) =
        repository.addLessonFileByLink(courseId, lessonId, name, url, sizeLabel)
}

class DeleteLessonFileUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long, lessonId: Long, index: Int) = repository.deleteLessonFile(courseId, lessonId, index)
}

class GetAdminLessonQuizUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long, lessonId: Long) = repository.getLessonQuiz(courseId, lessonId)
}

class UpsertLessonQuizUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long, lessonId: Long, title: String, passScore: Int, questions: List<AdminQuizQuestion>) =
        repository.upsertLessonQuiz(courseId, lessonId, title, passScore, questions)
}

class ListProjectSubmissionsUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.listProjectSubmissions(courseId)
}

class ReviewProjectSubmissionUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(submissionId: Long, status: String, mentorFeedback: String? = null) =
        repository.reviewProjectSubmission(submissionId, status, mentorFeedback)
}

class ListOrganizationsUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke() = repository.listOrganizations()
}

class CreateOrganizationUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(name: String, contactEmail: String? = null) = repository.createOrganization(name, contactEmail)
}

class BuySeatsUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(organizationId: Long, courseId: Long, count: Int) = repository.buySeats(organizationId, courseId, count)
}

class ListSeatsUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(organizationId: Long) = repository.listSeats(organizationId)
}

class AssignSeatUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(organizationId: Long, courseId: Long, email: String) = repository.assignSeat(organizationId, courseId, email)
}

class ListRefundRequestsUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke() = repository.listRefundRequests()
}

class ReviewRefundRequestUseCase(private val repository: AdminAcademyRepository) {
    suspend operator fun invoke(id: Long, approve: Boolean, adminNote: String? = null) = repository.reviewRefundRequest(id, approve, adminNote)
}
