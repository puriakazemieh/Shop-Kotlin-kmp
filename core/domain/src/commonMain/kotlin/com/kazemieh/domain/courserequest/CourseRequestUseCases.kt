package com.kazemieh.domain.courserequest

class GetCourseRequestsUseCase(private val repository: CourseRequestRepository) {
    suspend operator fun invoke() = repository.list()
}

class GetMyCourseRequestsUseCase(private val repository: CourseRequestRepository) {
    suspend operator fun invoke() = repository.mine()
}

class CreateCourseRequestUseCase(private val repository: CourseRequestRepository) {
    suspend operator fun invoke(title: String, description: String?) = repository.create(title, description)
}

class ToggleCourseRequestLikeUseCase(private val repository: CourseRequestRepository) {
    suspend operator fun invoke(id: Long) = repository.toggleLike(id)
}

class GetAdminCourseRequestsUseCase(private val repository: CourseRequestRepository) {
    suspend operator fun invoke() = repository.adminList()
}

class DeleteCourseRequestUseCase(private val repository: CourseRequestRepository) {
    suspend operator fun invoke(id: Long) = repository.adminDelete(id)
}
