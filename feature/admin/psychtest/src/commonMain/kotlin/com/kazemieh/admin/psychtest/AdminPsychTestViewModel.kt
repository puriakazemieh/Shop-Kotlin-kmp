package com.kazemieh.admin.psychtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.psychtest.AdminScoreRange
import com.kazemieh.domain.psychtest.AdminTestQuestion
import com.kazemieh.domain.psychtest.CreatePsychTestUseCase
import com.kazemieh.domain.psychtest.DeletePsychTestUseCase
import com.kazemieh.domain.psychtest.GetAdminPsychTestsUseCase
import com.kazemieh.domain.psychtest.GetPendingInterpretationsUseCase
import com.kazemieh.domain.psychtest.InterpretTestUseCase
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.kazemieh.domain.psychtest.UserPsychTest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminPsychTestState(
    val isLoading: Boolean = false,
    val tests: List<PsychTestSummary> = emptyList(),
    val pending: List<UserPsychTest> = emptyList(),
    /** سؤالاتِ در حالِ ساختِ همین سشن (پیش از ثبتِ تست). */
    val draftQuestions: List<AdminTestQuestion> = emptyList(),
    val draftRanges: List<AdminScoreRange> = emptyList()
)

sealed interface AdminPsychTestEffect {
    data class ShowError(val message: Any) : AdminPsychTestEffect
    data class ShowSuccess(val message: Any) : AdminPsychTestEffect
}

class AdminPsychTestViewModel(
    private val getAdminPsychTestsUseCase: GetAdminPsychTestsUseCase,
    private val createPsychTestUseCase: CreatePsychTestUseCase,
    private val deletePsychTestUseCase: DeletePsychTestUseCase,
    private val getPendingInterpretationsUseCase: GetPendingInterpretationsUseCase,
    private val interpretTestUseCase: InterpretTestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminPsychTestState())
    val state: StateFlow<AdminPsychTestState> = _state.asStateFlow()

    private val _effect = Channel<AdminPsychTestEffect>()
    val effect: Flow<AdminPsychTestEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val r = getAdminPsychTestsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, tests = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(AdminPsychTestEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
        loadPending()
    }

    fun loadPending() {
        viewModelScope.launch {
            when (val r = getPendingInterpretationsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(pending = r.data) }
                else -> {}
            }
        }
    }

    fun addDraftQuestion(text: String, options: List<Pair<String, Int>>) {
        _state.update { it.copy(draftQuestions = it.draftQuestions + AdminTestQuestion(text, options.filter { o -> o.first.isNotBlank() })) }
    }

    fun addDraftRange(min: String, max: String, interpretation: String) {
        val range = AdminScoreRange(min.toIntOrNull() ?: 0, max.toIntOrNull() ?: 0, interpretation)
        _state.update { it.copy(draftRanges = it.draftRanges + range) }
    }

    fun createTest(title: String, slug: String, description: String, price: String, productId: String, resultMode: String) {
        viewModelScope.launch {
            val result = createPsychTestUseCase(
                title = title, slug = slug, description = description.ifBlank { null },
                price = price.toDoubleOrNull() ?: 0.0, productId = productId.toLongOrNull(),
                resultMode = resultMode, questions = _state.value.draftQuestions, ranges = _state.value.draftRanges
            )
            when (result) {
                is AppResult.Success -> {
                    _effect.send(AdminPsychTestEffect.ShowSuccess("تست ساخته شد."))
                    _state.update { it.copy(draftQuestions = emptyList(), draftRanges = emptyList()) }
                    load()
                }
                is AppResult.Error -> _effect.send(AdminPsychTestEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun deleteTest(id: Long) {
        viewModelScope.launch {
            when (val r = deletePsychTestUseCase(id)) {
                is AppResult.Success -> { _effect.send(AdminPsychTestEffect.ShowSuccess("تست حذف شد.")); load() }
                is AppResult.Error -> _effect.send(AdminPsychTestEffect.ShowError(r.message))
                else -> {}
            }
        }
    }

    fun interpret(userTestId: Long, interpretation: String) {
        viewModelScope.launch {
            when (val r = interpretTestUseCase(userTestId, interpretation)) {
                is AppResult.Success -> { _effect.send(AdminPsychTestEffect.ShowSuccess("تفسیر ثبت شد.")); loadPending() }
                is AppResult.Error -> _effect.send(AdminPsychTestEffect.ShowError(r.message))
                else -> {}
            }
        }
    }
}
