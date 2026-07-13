package com.kazemieh.domain.psychtest

class GetPsychTestsUseCase(private val repository: PsychTestRepository) {
    suspend operator fun invoke() = repository.getTests()
}

class GetPsychTestDetailUseCase(private val repository: PsychTestRepository) {
    suspend operator fun invoke(slug: String) = repository.getTest(slug)
}

class GetMyPsychTestsUseCase(private val repository: PsychTestRepository) {
    suspend operator fun invoke() = repository.getMyTests()
}

class GetUserTestQuestionsUseCase(private val repository: PsychTestRepository) {
    suspend operator fun invoke(userTestId: Long) = repository.getUserTestQuestions(userTestId)
}

class SubmitPsychTestUseCase(private val repository: PsychTestRepository) {
    suspend operator fun invoke(userTestId: Long, answers: Map<Int, Int>) = repository.submit(userTestId, answers)
}

// ---------- Admin ----------
class GetAdminPsychTestsUseCase(private val repository: AdminPsychTestRepository) {
    suspend operator fun invoke() = repository.list()
}

class GetAdminPsychTestDetailUseCase(private val repository: AdminPsychTestRepository) {
    suspend operator fun invoke(id: Long) = repository.detail(id)
}

class CreatePsychTestUseCase(private val repository: AdminPsychTestRepository) {
    suspend operator fun invoke(
        title: String,
        slug: String,
        description: String?,
        price: Double,
        productId: Long?,
        resultMode: String,
        questions: List<AdminTestQuestion>,
        ranges: List<AdminScoreRange>
    ) = repository.create(title, slug, description, price, productId, resultMode, questions, ranges)
}

class UpdatePsychTestUseCase(private val repository: AdminPsychTestRepository) {
    suspend operator fun invoke(
        id: Long,
        title: String?,
        description: String?,
        price: Double?,
        productId: Long?,
        resultMode: String?,
        questions: List<AdminTestQuestion>?,
        ranges: List<AdminScoreRange>?
    ) = repository.update(id, title, description, price, productId, resultMode, questions, ranges)
}

class DeletePsychTestUseCase(private val repository: AdminPsychTestRepository) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}

class GetPendingInterpretationsUseCase(private val repository: AdminPsychTestRepository) {
    suspend operator fun invoke() = repository.pendingInterpretations()
}

class InterpretTestUseCase(private val repository: AdminPsychTestRepository) {
    suspend operator fun invoke(userTestId: Long, interpretation: String) = repository.interpret(userTestId, interpretation)
}
