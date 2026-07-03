package com.kazemieh.domain.academy

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
        isFreePreview: Boolean = false
    ) = repository.addLesson(courseId, sectionId, title, videoUrl, durationSeconds, sortOrder, isFreePreview)
}
