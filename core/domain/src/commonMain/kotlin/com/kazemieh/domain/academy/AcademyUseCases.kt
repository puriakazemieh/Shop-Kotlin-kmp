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
