package com.kazemieh.admin.psychtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.psychtest.AdminPsychTestDetail
import com.kazemieh.domain.psychtest.AdminScoreRange
import com.kazemieh.domain.psychtest.AdminTestQuestion
import com.kazemieh.domain.psychtest.CreatePsychTestUseCase
import com.kazemieh.domain.psychtest.GetAdminPsychTestDetailUseCase
import com.kazemieh.domain.psychtest.DeletePsychTestUseCase
import com.kazemieh.domain.psychtest.GetAdminPsychTestsUseCase
import com.kazemieh.domain.psychtest.GetPendingInterpretationsUseCase
import com.kazemieh.domain.psychtest.GetPsychTestDetailUseCase
import com.kazemieh.domain.psychtest.InterpretTestUseCase
import com.kazemieh.domain.psychtest.PsychTestDetail
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.kazemieh.domain.psychtest.UpdatePsychTestUseCase
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
    val draftRanges: List<AdminScoreRange> = emptyList(),
    /** جزئیاتِ تستِ انتخاب‌شده برای نمایش/بررسیِ محتوا (با کلیک روی ردیف). */
    val detailLoadingSlug: String? = null,
    val selectedDetail: PsychTestDetail? = null,
    /** حالتِ ویرایش: شناسه‌ی تستِ در حالِ ویرایش و جزئیاتِ بارگذاری‌شده برای پیش‌پُر کردنِ فرم. */
    val editLoadingId: Long? = null,
    val editingTestId: Long? = null,
    val editingDetail: AdminPsychTestDetail? = null
)

sealed interface AdminPsychTestEffect {
    data class ShowError(val message: Any) : AdminPsychTestEffect
    data class ShowSuccess(val message: Any) : AdminPsychTestEffect
}

class AdminPsychTestViewModel(
    private val getAdminPsychTestsUseCase: GetAdminPsychTestsUseCase,
    private val createPsychTestUseCase: CreatePsychTestUseCase,
    private val updatePsychTestUseCase: UpdatePsychTestUseCase,
    private val deletePsychTestUseCase: DeletePsychTestUseCase,
    private val getPendingInterpretationsUseCase: GetPendingInterpretationsUseCase,
    private val interpretTestUseCase: InterpretTestUseCase,
    private val getPsychTestDetailUseCase: GetPsychTestDetailUseCase,
    private val getAdminPsychTestDetailUseCase: GetAdminPsychTestDetailUseCase
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

    fun createTest(
        title: String,
        slug: String,
        description: String,
        price: String,
        productId: String,
        resultMode: String,
        questions: List<AdminTestQuestion> = _state.value.draftQuestions,
        ranges: List<AdminScoreRange> = _state.value.draftRanges
    ) {
        viewModelScope.launch {
            val result = createPsychTestUseCase(
                title = title, slug = slug, description = description.ifBlank { null },
                price = price.toDoubleOrNull() ?: 0.0, productId = productId.toLongOrNull(),
                resultMode = resultMode, questions = questions, ranges = ranges
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

    /** با کلیک روی یک تست، محتوایِ کاملِ آن (سؤال‌ها) را از سرور می‌گیرد تا در باتم‌شیت نمایش دهد. */
    fun openDetail(slug: String) {
        _state.update { it.copy(detailLoadingSlug = slug, selectedDetail = null) }
        viewModelScope.launch {
            when (val r = getPsychTestDetailUseCase(slug)) {
                is AppResult.Success -> _state.update { it.copy(detailLoadingSlug = null, selectedDetail = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(detailLoadingSlug = null) }
                    _effect.send(AdminPsychTestEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    fun closeDetail() {
        _state.update { it.copy(detailLoadingSlug = null, selectedDetail = null) }
    }

    /** ویرایشِ یک تست: جزئیاتِ کاملِ ادمین (سؤال‌ها با امتیاز + بازه‌ها) را می‌گیرد تا فرمِ ویرایش کاملاً پیش‌پُر شود. */
    fun openEdit(test: PsychTestSummary) {
        _state.update { it.copy(editLoadingId = test.id, editingTestId = test.id, editingDetail = null) }
        viewModelScope.launch {
            when (val r = getAdminPsychTestDetailUseCase(test.id)) {
                is AppResult.Success -> _state.update { it.copy(editLoadingId = null, editingDetail = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(editLoadingId = null, editingTestId = null) }
                    _effect.send(AdminPsychTestEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    fun closeEdit() {
        _state.update { it.copy(editLoadingId = null, editingTestId = null, editingDetail = null) }
    }

    fun updateTest(
        id: Long,
        title: String,
        description: String,
        price: String,
        productId: String,
        resultMode: String,
        questions: List<AdminTestQuestion>,
        ranges: List<AdminScoreRange>?
    ) {
        viewModelScope.launch {
            val result = updatePsychTestUseCase(
                id = id, title = title, description = description,
                price = price.toDoubleOrNull(), productId = productId.toLongOrNull(), resultMode = resultMode,
                questions = questions, ranges = ranges
            )
            when (result) {
                is AppResult.Success -> {
                    _effect.send(AdminPsychTestEffect.ShowSuccess("تغییرات ذخیره شد."))
                    closeEdit()
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
