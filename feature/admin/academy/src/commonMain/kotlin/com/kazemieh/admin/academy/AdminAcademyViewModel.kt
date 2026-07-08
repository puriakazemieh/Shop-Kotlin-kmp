package com.kazemieh.admin.academy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.AddCourseLessonUseCase
import com.kazemieh.domain.academy.AddCourseSectionUseCase
import com.kazemieh.domain.academy.AddLessonFileByLinkUseCase
import com.kazemieh.domain.academy.AddLessonFileUseCase
import com.kazemieh.domain.academy.UploadCourseMediaUseCase
import com.kazemieh.domain.academy.AdminCourseParams
import com.kazemieh.domain.academy.AdminCourseRefundRequest
import com.kazemieh.domain.academy.AdminCourseUpdateParams
import com.kazemieh.domain.academy.AdminQuizQuestion
import com.kazemieh.domain.academy.AdminWaitlistEntry
import com.kazemieh.domain.academy.AssignSeatUseCase
import com.kazemieh.domain.academy.BuySeatsUseCase
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.academy.CreateCourseUseCase
import com.kazemieh.domain.academy.CreateOrganizationUseCase
import com.kazemieh.domain.academy.DeleteCourseUseCase
import com.kazemieh.domain.academy.DeleteLessonFileUseCase
import com.kazemieh.domain.academy.GetAdminCourseDetailUseCase
import com.kazemieh.domain.academy.GetAdminCoursesUseCase
import com.kazemieh.domain.academy.GetAdminWaitlistUseCase
import com.kazemieh.domain.academy.ListOrganizationsUseCase
import com.kazemieh.domain.academy.ListProjectSubmissionsUseCase
import com.kazemieh.domain.academy.ListRefundRequestsUseCase
import com.kazemieh.domain.academy.ListSeatsUseCase
import com.kazemieh.domain.academy.NotifyNextInWaitlistUseCase
import com.kazemieh.domain.academy.Organization
import com.kazemieh.domain.academy.OrganizationSeat
import com.kazemieh.domain.academy.ProjectSubmission
import com.kazemieh.domain.academy.ReviewProjectSubmissionUseCase
import com.kazemieh.domain.academy.ReviewRefundRequestUseCase
import com.kazemieh.domain.academy.UpdateCourseUseCase
import com.kazemieh.domain.academy.UpsertCourseQuizUseCase
import com.kazemieh.domain.academy.UpsertLessonQuizUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAcademyState(
    val isLoading: Boolean = false,
    val courses: List<CourseSummary> = emptyList(),
    val expandedCourseId: Long? = null,
    val expandedCourseDetail: CourseDetail? = null,
    val loadingDetail: Boolean = false,
    /** سؤالاتِ آزمونِ ساخته‌شده در همین سشن، per courseId (برای نمایشِ شمارنده). */
    val quizQuestionsByCourse: Map<Long, List<AdminQuizQuestion>> = emptyMap(),
    /** لیستِ انتظارِ کلاسِ حضوریِ باز (per courseId). */
    val waitlistByCourse: Map<Long, List<AdminWaitlistEntry>> = emptyMap(),
    val loadingWaitlist: Boolean = false,
    /** سؤالاتِ آزمونِ کوتاهِ هر درس، ساخته‌شده در همین سشن (per lessonId). */
    val lessonQuizByLesson: Map<Long, List<AdminQuizQuestion>> = emptyMap(),
    /** پروژه‌های ثبت‌شده‌ی هر دوره (per courseId). */
    val projectSubmissionsByCourse: Map<Long, List<ProjectSubmission>> = emptyMap(),
    // ---- سازمان/صندلیِ سازمانی ----
    val organizations: List<Organization> = emptyList(),
    val loadingOrganizations: Boolean = false,
    val expandedOrganizationId: Long? = null,
    val seatsByOrganization: Map<Long, List<OrganizationSeat>> = emptyMap(),
    // ---- گارانتیِ بازگشتِ وجه ----
    val refundRequests: List<AdminCourseRefundRequest> = emptyList(),
    val loadingRefundRequests: Boolean = false
)

sealed interface AdminAcademyEffect {
    data class ShowError(val message: Any) : AdminAcademyEffect
    data class ShowSuccess(val message: Any) : AdminAcademyEffect
}

class AdminAcademyViewModel(
    private val getAdminCoursesUseCase: GetAdminCoursesUseCase,
    private val getAdminCourseDetailUseCase: GetAdminCourseDetailUseCase,
    private val createCourseUseCase: CreateCourseUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase,
    private val addCourseSectionUseCase: AddCourseSectionUseCase,
    private val addCourseLessonUseCase: AddCourseLessonUseCase,
    private val upsertCourseQuizUseCase: UpsertCourseQuizUseCase,
    private val getAdminWaitlistUseCase: GetAdminWaitlistUseCase,
    private val notifyNextInWaitlistUseCase: NotifyNextInWaitlistUseCase,
    private val addLessonFileByLinkUseCase: AddLessonFileByLinkUseCase,
    private val addLessonFileUseCase: AddLessonFileUseCase,
    private val uploadCourseMediaUseCase: UploadCourseMediaUseCase,
    private val deleteLessonFileUseCase: DeleteLessonFileUseCase,
    private val upsertLessonQuizUseCase: UpsertLessonQuizUseCase,
    private val listProjectSubmissionsUseCase: ListProjectSubmissionsUseCase,
    private val reviewProjectSubmissionUseCase: ReviewProjectSubmissionUseCase,
    private val listOrganizationsUseCase: ListOrganizationsUseCase,
    private val createOrganizationUseCase: CreateOrganizationUseCase,
    private val buySeatsUseCase: BuySeatsUseCase,
    private val listSeatsUseCase: ListSeatsUseCase,
    private val assignSeatUseCase: AssignSeatUseCase,
    private val listRefundRequestsUseCase: ListRefundRequestsUseCase,
    private val reviewRefundRequestUseCase: ReviewRefundRequestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminAcademyState())
    val state: StateFlow<AdminAcademyState> = _state.asStateFlow()

    private val _effect = Channel<AdminAcademyEffect>()
    val effect: Flow<AdminAcademyEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getAdminCoursesUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, courses = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(AdminAcademyEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun toggleExpand(courseId: Long) {
        if (_state.value.expandedCourseId == courseId) {
            _state.update { it.copy(expandedCourseId = null, expandedCourseDetail = null) }
            return
        }
        _state.update { it.copy(expandedCourseId = courseId, expandedCourseDetail = null, loadingDetail = true) }
        viewModelScope.launch {
            when (val result = getAdminCourseDetailUseCase(courseId)) {
                is AppResult.Success -> _state.update { it.copy(loadingDetail = false, expandedCourseDetail = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(loadingDetail = false) }
                    _effect.send(AdminAcademyEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
        loadWaitlist(courseId)
    }

    fun loadWaitlist(courseId: Long) {
        _state.update { it.copy(loadingWaitlist = true) }
        viewModelScope.launch {
            when (val result = getAdminWaitlistUseCase(courseId)) {
                is AppResult.Success -> _state.update {
                    it.copy(loadingWaitlist = false, waitlistByCourse = it.waitlistByCourse + (courseId to result.data))
                }
                is AppResult.Error -> _state.update { it.copy(loadingWaitlist = false) }
                else -> {}
            }
        }
    }

    fun notifyNext(courseId: Long) {
        viewModelScope.launch {
            when (val result = notifyNextInWaitlistUseCase(courseId)) {
                is AppResult.Success -> {
                    if (result.data != null) {
                        _effect.send(AdminAcademyEffect.ShowSuccess("نفرِ اولِ صف مطلع شد."))
                    } else {
                        _effect.send(AdminAcademyEffect.ShowSuccess("صفِ انتظار خالی است."))
                    }
                    loadWaitlist(courseId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun createCourse(
        title: String,
        slug: String,
        price: String,
        productId: String,
        courseType: String = "COURSE",
        format: String = "ONLINE_RECORDED",
        location: String = "",
        capacity: String = "",
        requiresProjectSubmission: Boolean = false,
        thumbnailUrl: String = "",
        cohortStartDate: String = ""
    ) {
        viewModelScope.launch {
            val params = AdminCourseParams(
                title = title,
                slug = slug,
                price = price.toDoubleOrNull() ?: 0.0,
                productId = productId.toLongOrNull(),
                courseType = courseType,
                format = format,
                location = location.ifBlank { null },
                capacity = capacity.toIntOrNull(),
                requiresProjectSubmission = requiresProjectSubmission,
                thumbnailUrl = thumbnailUrl.ifBlank { null },
                cohortStartDate = cohortStartDate.ifBlank { null }
            )
            when (val result = createCourseUseCase(params)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("دوره ساخته شد."))
                    load()
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun updateCourse(
        id: Long,
        title: String,
        description: String,
        instructor: String,
        price: String,
        discountedPrice: String,
        requiresProjectSubmission: Boolean,
        thumbnailUrl: String = "",
        cohortStartDate: String = ""
    ) {
        viewModelScope.launch {
            val params = AdminCourseUpdateParams(
                title = title.ifBlank { null },
                description = description.ifBlank { null },
                instructor = instructor.ifBlank { null },
                price = price.toDoubleOrNull(),
                discountedPrice = discountedPrice.toDoubleOrNull(),
                requiresProjectSubmission = requiresProjectSubmission,
                thumbnailUrl = thumbnailUrl.ifBlank { null },
                cohortStartDate = cohortStartDate.ifBlank { null }
            )
            when (val result = updateCourseUseCase(id, params)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("دوره به‌روزرسانی شد."))
                    refreshExpanded(id)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch {
            when (val result = deleteCourseUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("دوره حذف شد."))
                    if (_state.value.expandedCourseId == id) {
                        _state.update { it.copy(expandedCourseId = null, expandedCourseDetail = null) }
                    }
                    load()
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun addSection(courseId: Long, title: String) {
        viewModelScope.launch {
            when (val result = addCourseSectionUseCase(courseId, title)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("بخش اضافه شد."))
                    refreshExpanded(courseId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun addLesson(
        courseId: Long, sectionId: Long, title: String, videoUrl: String, durationMinutes: String, isFreePreview: Boolean,
        subtitleLanguage: String = "", subtitleUrl: String = ""
    ) {
        viewModelScope.launch {
            val durationSeconds = (durationMinutes.toDoubleOrNull() ?: 0.0) * 60
            when (val result = addCourseLessonUseCase(
                courseId, sectionId, title,
                videoUrl.ifBlank { null }, durationSeconds.toInt(), 0, isFreePreview,
                subtitleLanguage.ifBlank { null }, subtitleUrl.ifBlank { null }
            )) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("درس اضافه شد."))
                    refreshExpanded(courseId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    /** ذخیره/به‌روزرسانیِ آزمونِ پایانِ دوره با یک سؤالِ جدید (افزودنِ ساده). */
    fun addQuizQuestion(courseId: Long, passScore: String, text: String, options: List<String>, correctIndex: Int) {
        val existing = _state.value.quizQuestionsByCourse[courseId].orEmpty()
        val updated = existing + AdminQuizQuestion(text = text, options = options.filter { it.isNotBlank() }, correctIndex = correctIndex)
        _state.update { it.copy(quizQuestionsByCourse = it.quizQuestionsByCourse + (courseId to updated)) }
        viewModelScope.launch {
            when (val result = upsertCourseQuizUseCase(courseId, "آزمونِ پایانِ دوره", passScore.toIntOrNull() ?: 60, updated)) {
                is AppResult.Success -> _effect.send(AdminAcademyEffect.ShowSuccess("سؤال به آزمون اضافه شد."))
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    // ---- فایل‌های ضمیمه‌ی درس ----
    fun addLessonFile(courseId: Long, lessonId: Long, name: String, url: String) {
        viewModelScope.launch {
            when (val result = addLessonFileByLinkUseCase(courseId, lessonId, name, url)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("فایل اضافه شد."))
                    refreshExpanded(courseId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    /** آپلودِ مستقیمِ فایلِ ضمیمه از دستگاه (به‌جای چسباندنِ لینک). */
    fun addLessonFileFromDevice(courseId: Long, lessonId: Long, displayName: String, fileBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            when (val result = addLessonFileUseCase(courseId, lessonId, fileBytes, fileName, displayName)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("فایل آپلود شد."))
                    refreshExpanded(courseId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    /**
     * آپلودِ عمومیِ رسانه (کاورِ دوره یا ویدیویِ درس) — فقط URL برمی‌گرداند، بدونِ تغییرِ state.
     * فراخوان (Composable) خودش URLِ برگشتی را در فیلدِ محلی‌اش می‌نشاند.
     */
    suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): String? {
        return when (val result = uploadCourseMediaUseCase(fileBytes, fileName)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> {
                _effect.send(AdminAcademyEffect.ShowError(result.message))
                null
            }
            else -> null
        }
    }

    fun deleteLessonFile(courseId: Long, lessonId: Long, index: Int) {
        viewModelScope.launch {
            when (val result = deleteLessonFileUseCase(courseId, lessonId, index)) {
                is AppResult.Success -> refreshExpanded(courseId)
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    // ---- آزمونِ کوتاهِ درس ----
    fun addLessonQuizQuestion(courseId: Long, lessonId: Long, passScore: String, text: String, options: List<String>, correctIndex: Int) {
        val existing = _state.value.lessonQuizByLesson[lessonId].orEmpty()
        val updated = existing + AdminQuizQuestion(text = text, options = options.filter { it.isNotBlank() }, correctIndex = correctIndex)
        _state.update { it.copy(lessonQuizByLesson = it.lessonQuizByLesson + (lessonId to updated)) }
        viewModelScope.launch {
            when (val result = upsertLessonQuizUseCase(courseId, lessonId, "آزمونِ این درس", passScore.toIntOrNull() ?: 60, updated)) {
                is AppResult.Success -> _effect.send(AdminAcademyEffect.ShowSuccess("سؤال به آزمونِ درس اضافه شد."))
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    // ---- پروژه‌های پایانی ----
    fun loadProjectSubmissions(courseId: Long) {
        viewModelScope.launch {
            when (val result = listProjectSubmissionsUseCase(courseId)) {
                is AppResult.Success -> _state.update {
                    it.copy(projectSubmissionsByCourse = it.projectSubmissionsByCourse + (courseId to result.data))
                }
                else -> {}
            }
        }
    }

    fun reviewProject(courseId: Long, submissionId: Long, status: String, feedback: String?) {
        viewModelScope.launch {
            when (val result = reviewProjectSubmissionUseCase(submissionId, status, feedback)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("وضعیتِ پروژه ثبت شد."))
                    loadProjectSubmissions(courseId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun refreshExpanded(courseId: Long) {
        viewModelScope.launch {
            when (val result = getAdminCourseDetailUseCase(courseId)) {
                is AppResult.Success -> _state.update { it.copy(expandedCourseDetail = result.data) }
                else -> {}
            }
        }
        load()
    }

    // ---- سازمان/صندلیِ سازمانی ----
    fun loadOrganizations() {
        _state.update { it.copy(loadingOrganizations = true) }
        viewModelScope.launch {
            when (val result = listOrganizationsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(loadingOrganizations = false, organizations = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(loadingOrganizations = false) }
                    _effect.send(AdminAcademyEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun createOrganization(name: String, contactEmail: String) {
        viewModelScope.launch {
            when (val result = createOrganizationUseCase(name, contactEmail.ifBlank { null })) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("سازمان ساخته شد."))
                    loadOrganizations()
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun toggleOrganizationExpand(organizationId: Long) {
        val next = if (_state.value.expandedOrganizationId == organizationId) null else organizationId
        _state.update { it.copy(expandedOrganizationId = next) }
        if (next != null) loadSeats(next)
    }

    private fun loadSeats(organizationId: Long) {
        viewModelScope.launch {
            when (val result = listSeatsUseCase(organizationId)) {
                is AppResult.Success -> _state.update { it.copy(seatsByOrganization = it.seatsByOrganization + (organizationId to result.data)) }
                else -> {}
            }
        }
    }

    fun buySeats(organizationId: Long, courseId: Long, count: Int) {
        viewModelScope.launch {
            when (val result = buySeatsUseCase(organizationId, courseId, count)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("$count صندلی خریداری شد."))
                    loadSeats(organizationId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun assignSeat(organizationId: Long, courseId: Long, email: String) {
        viewModelScope.launch {
            when (val result = assignSeatUseCase(organizationId, courseId, email)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("صندلی به $email اختصاص یافت و او ثبت‌نام شد."))
                    loadSeats(organizationId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    // ---- گارانتیِ بازگشتِ وجه ----
    fun loadRefundRequests() {
        _state.update { it.copy(loadingRefundRequests = true) }
        viewModelScope.launch {
            when (val result = listRefundRequestsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(loadingRefundRequests = false, refundRequests = result.data) }
                else -> _state.update { it.copy(loadingRefundRequests = false) }
            }
        }
    }

    fun reviewRefundRequest(id: Long, approve: Boolean, adminNote: String?) {
        viewModelScope.launch {
            when (val result = reviewRefundRequestUseCase(id, approve, adminNote)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess(if (approve) "بازگشتِ وجه تأیید شد." else "درخواست ردّ شد."))
                    loadRefundRequests()
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }
}
