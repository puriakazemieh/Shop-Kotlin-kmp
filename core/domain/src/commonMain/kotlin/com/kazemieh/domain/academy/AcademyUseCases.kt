package com.kazemieh.domain.academy

class GetCoursesUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke() = repository.getCourses()
}

class GetCourseDetailUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(slug: String) = repository.getCourse(slug)
}

class GetMyCoursesUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke() = repository.getMyCourses()
}

class EnrollCourseUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.enroll(courseId)
}

class UpdateLessonProgressUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(lessonId: Long, completed: Boolean? = null, lastPositionSeconds: Int? = null) =
        repository.updateLessonProgress(lessonId, completed, lastPositionSeconds)
}

class GetQuizUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.getQuiz(courseId)
}

class SubmitQuizUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long, answers: Map<Int, Int>) = repository.submitQuiz(courseId, answers)
}

class GetCertificatesUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke() = repository.getCertificates()
}

class JoinWaitlistUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.joinWaitlist(courseId)
}

class GetLessonQuizUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(lessonId: Long) = repository.getLessonQuiz(lessonId)
}

class SubmitLessonQuizUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(lessonId: Long, answers: Map<Int, Int>) = repository.submitLessonQuiz(lessonId, answers)
}

class SubmitProjectUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long, fileBytes: ByteArray, fileName: String, note: String? = null) =
        repository.submitProject(courseId, fileBytes, fileName, note)
}

class SubmitProjectByLinkUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long, fileUrl: String, note: String? = null) =
        repository.submitProjectByLink(courseId, fileUrl, note)
}

class GetMyProjectUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.getMyProject(courseId)
}

class GetLessonQuestionsUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(lessonId: Long) = repository.getLessonQuestions(lessonId)
}

class CreateLessonQuestionUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(lessonId: Long, content: String, parentId: Long? = null) =
        repository.createLessonQuestion(lessonId, content, parentId)
}

class MarkCourseUpdateSeenUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.markCourseUpdateSeen(courseId)
}

class GetPeerSubmissionsUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long) = repository.getPeerSubmissions(courseId)
}

class GetPeerCommentsUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(submissionId: Long) = repository.getPeerComments(submissionId)
}

class AddPeerCommentUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(submissionId: Long, comment: String) = repository.addPeerComment(submissionId, comment)
}

class VerifyCertificateUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(certNumber: String) = repository.verifyCertificate(certNumber)
}

class GetPlacementQuizUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke() = repository.getPlacementQuiz()
}

class SubmitPlacementQuizUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(answers: List<Int>) = repository.submitPlacementQuiz(answers)
}

class RequestCourseRefundUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke(courseId: Long, reason: String? = null) = repository.requestRefund(courseId, reason)
}

class GetMyRefundRequestsUseCase(private val repository: AcademyRepository) {
    suspend operator fun invoke() = repository.getMyRefundRequests()
}
