package com.kazemieh.admin.clinic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.AddPatientNoteUseCase
import com.kazemieh.domain.clinic.AddSlotUseCase
import com.kazemieh.domain.clinic.AdminAppointment
import com.kazemieh.domain.clinic.AdminPatientSummary
import com.kazemieh.domain.clinic.AdminSlot
import com.kazemieh.domain.clinic.AdminTherapistParams
import com.kazemieh.domain.clinic.CompleteAppointmentUseCase
import com.kazemieh.domain.clinic.ConfirmAppointmentUseCase
import com.kazemieh.domain.clinic.CreateTherapistUseCase
import com.kazemieh.domain.clinic.DeleteTherapistUseCase
import com.kazemieh.domain.clinic.GenerateSlotsUseCase
import com.kazemieh.domain.clinic.GetAdminAppointmentsUseCase
import com.kazemieh.domain.clinic.GetAdminPatientsUseCase
import com.kazemieh.domain.clinic.GetAdminSlotsUseCase
import com.kazemieh.domain.clinic.GetAdminTherapistsUseCase
import com.kazemieh.domain.clinic.GetPatientFileUseCase
import com.kazemieh.domain.clinic.GetPatientNotesUseCase
import com.kazemieh.domain.clinic.PatientFile
import com.kazemieh.domain.clinic.PatientNote
import com.kazemieh.domain.clinic.SetPatientTagsUseCase
import com.kazemieh.domain.clinic.TherapistSummary
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminClinicState(
    val isLoading: Boolean = false,
    val therapists: List<TherapistSummary> = emptyList(),
    val expandedTherapistId: Long? = null,
    val expandedSlots: List<AdminSlot> = emptyList(),
    val loadingSlots: Boolean = false,
    val appointments: List<AdminAppointment> = emptyList(),
    val loadingAppointments: Boolean = false,
    /** یادداشت‌های محرمانه، per appointmentId (فقط برای نوبتِ باز‌شده). */
    val notesByAppointment: Map<Long, List<PatientNote>> = emptyMap(),
    val expandedNotesAppointmentId: Long? = null,
    val loadingNotes: Boolean = false,
    // ---- CRMِ سبکِ مراجعان + پرونده‌ی کاملِ مراجع ----
    val crmTherapistId: Long? = null,
    val patients: List<AdminPatientSummary> = emptyList(),
    val loadingPatients: Boolean = false,
    val expandedPatientUserId: Long? = null,
    val patientFile: PatientFile? = null,
    val loadingPatientFile: Boolean = false
)

sealed interface AdminClinicEffect {
    data class ShowError(val message: Any) : AdminClinicEffect
    data class ShowSuccess(val message: Any) : AdminClinicEffect
}

class AdminClinicViewModel(
    private val getAdminTherapistsUseCase: GetAdminTherapistsUseCase,
    private val createTherapistUseCase: CreateTherapistUseCase,
    private val deleteTherapistUseCase: DeleteTherapistUseCase,
    private val addSlotUseCase: AddSlotUseCase,
    private val generateSlotsUseCase: GenerateSlotsUseCase,
    private val getAdminSlotsUseCase: GetAdminSlotsUseCase,
    private val getAdminAppointmentsUseCase: GetAdminAppointmentsUseCase,
    private val confirmAppointmentUseCase: ConfirmAppointmentUseCase,
    private val completeAppointmentUseCase: CompleteAppointmentUseCase,
    private val getPatientNotesUseCase: GetPatientNotesUseCase,
    private val addPatientNoteUseCase: AddPatientNoteUseCase,
    private val getAdminPatientsUseCase: GetAdminPatientsUseCase,
    private val setPatientTagsUseCase: SetPatientTagsUseCase,
    private val getPatientFileUseCase: GetPatientFileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminClinicState())
    val state: StateFlow<AdminClinicState> = _state.asStateFlow()

    private val _effect = Channel<AdminClinicEffect>()
    val effect: Flow<AdminClinicEffect> = _effect.receiveAsFlow()

    fun loadTherapists() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getAdminTherapistsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, therapists = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(AdminClinicEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun loadAppointments() {
        _state.update { it.copy(loadingAppointments = true) }
        viewModelScope.launch {
            when (val result = getAdminAppointmentsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(loadingAppointments = false, appointments = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(loadingAppointments = false) }
                    _effect.send(AdminClinicEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun toggleExpand(therapistId: Long) {
        if (_state.value.expandedTherapistId == therapistId) {
            _state.update { it.copy(expandedTherapistId = null, expandedSlots = emptyList()) }
            return
        }
        _state.update { it.copy(expandedTherapistId = therapistId, expandedSlots = emptyList(), loadingSlots = true) }
        viewModelScope.launch {
            when (val result = getAdminSlotsUseCase(therapistId)) {
                is AppResult.Success -> _state.update { it.copy(loadingSlots = false, expandedSlots = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(loadingSlots = false) }
                    _effect.send(AdminClinicEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun createTherapist(
        name: String, slug: String, sessionPrice: String, durationMinutes: String, productId: String,
        mode: String = "ONLINE", location: String = ""
    ) {
        viewModelScope.launch {
            val params = AdminTherapistParams(
                name = name,
                slug = slug,
                sessionPrice = sessionPrice.toDoubleOrNull() ?: 0.0,
                sessionDurationMinutes = durationMinutes.toIntOrNull() ?: 45,
                productId = productId.toLongOrNull(),
                mode = mode,
                location = location.ifBlank { null }
            )
            when (val result = createTherapistUseCase(params)) {
                is AppResult.Success -> {
                    _effect.send(AdminClinicEffect.ShowSuccess("درمانگر ساخته شد."))
                    loadTherapists()
                }
                is AppResult.Error -> _effect.send(AdminClinicEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun deleteTherapist(id: Long) {
        viewModelScope.launch {
            when (val result = deleteTherapistUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminClinicEffect.ShowSuccess("درمانگر حذف شد."))
                    if (_state.value.expandedTherapistId == id) {
                        _state.update { it.copy(expandedTherapistId = null, expandedSlots = emptyList()) }
                    }
                    loadTherapists()
                }
                is AppResult.Error -> _effect.send(AdminClinicEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun addSlot(therapistId: Long, startTime: String, endTime: String) {
        viewModelScope.launch {
            when (val result = addSlotUseCase(therapistId, startTime, endTime)) {
                is AppResult.Success -> {
                    _effect.send(AdminClinicEffect.ShowSuccess("بازه اضافه شد."))
                    refreshSlots(therapistId)
                }
                is AppResult.Error -> _effect.send(AdminClinicEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun generateSlots(therapistId: Long, windowStart: String, windowEnd: String, slotMinutes: String) {
        viewModelScope.launch {
            when (val result = generateSlotsUseCase(therapistId, windowStart, windowEnd, slotMinutes.toIntOrNull())) {
                is AppResult.Success -> {
                    _effect.send(AdminClinicEffect.ShowSuccess("${result.data} بازه ساخته شد."))
                    refreshSlots(therapistId)
                }
                is AppResult.Error -> _effect.send(AdminClinicEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun refreshSlots(therapistId: Long) {
        viewModelScope.launch {
            when (val result = getAdminSlotsUseCase(therapistId)) {
                is AppResult.Success -> _state.update { it.copy(expandedSlots = result.data) }
                else -> {}
            }
        }
    }

    fun confirmAppointment(id: Long, videoRoomUrl: String) {
        viewModelScope.launch {
            when (val result = confirmAppointmentUseCase(id, videoRoomUrl)) {
                is AppResult.Success -> {
                    _effect.send(AdminClinicEffect.ShowSuccess("نوبت تأیید شد."))
                    loadAppointments()
                }
                is AppResult.Error -> _effect.send(AdminClinicEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun completeAppointment(id: Long) {
        viewModelScope.launch {
            when (val result = completeAppointmentUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminClinicEffect.ShowSuccess("نوبت به‌عنوانِ برگزارشده علامت خورد."))
                    loadAppointments()
                }
                is AppResult.Error -> _effect.send(AdminClinicEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    /** یادداشتِ محرمانه (فقط ادمین/مشاور می‌بینند) — باز/بسته‌کردنِ پنلِ یک نوبت. */
    fun toggleNotes(appointmentId: Long) {
        if (_state.value.expandedNotesAppointmentId == appointmentId) {
            _state.update { it.copy(expandedNotesAppointmentId = null) }
            return
        }
        _state.update { it.copy(expandedNotesAppointmentId = appointmentId) }
        loadNotes(appointmentId)
    }

    private fun loadNotes(appointmentId: Long) {
        _state.update { it.copy(loadingNotes = true) }
        viewModelScope.launch {
            when (val result = getPatientNotesUseCase(appointmentId)) {
                is AppResult.Success -> _state.update {
                    it.copy(loadingNotes = false, notesByAppointment = it.notesByAppointment + (appointmentId to result.data))
                }
                is AppResult.Error -> {
                    _state.update { it.copy(loadingNotes = false) }
                    _effect.send(AdminClinicEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun addNote(appointmentId: Long, note: String) {
        viewModelScope.launch {
            when (val result = addPatientNoteUseCase(appointmentId, note)) {
                is AppResult.Success -> {
                    _effect.send(AdminClinicEffect.ShowSuccess("یادداشت ثبت شد."))
                    loadNotes(appointmentId)
                }
                is AppResult.Error -> _effect.send(AdminClinicEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    // ---- CRMِ سبکِ مراجعان + پرونده‌ی کاملِ مراجع ----
    fun selectCrmTherapist(therapistId: Long) {
        _state.update { it.copy(crmTherapistId = therapistId, expandedPatientUserId = null, patientFile = null) }
        loadPatients(therapistId)
    }

    fun loadPatients(therapistId: Long) {
        _state.update { it.copy(loadingPatients = true) }
        viewModelScope.launch {
            when (val result = getAdminPatientsUseCase(therapistId)) {
                is AppResult.Success -> _state.update { it.copy(loadingPatients = false, patients = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(loadingPatients = false) }
                    _effect.send(AdminClinicEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun togglePatientFile(userId: Long) {
        val therapistId = _state.value.crmTherapistId ?: return
        if (_state.value.expandedPatientUserId == userId) {
            _state.update { it.copy(expandedPatientUserId = null, patientFile = null) }
            return
        }
        _state.update { it.copy(expandedPatientUserId = userId, patientFile = null, loadingPatientFile = true) }
        viewModelScope.launch {
            when (val result = getPatientFileUseCase(therapistId, userId)) {
                is AppResult.Success -> _state.update { it.copy(loadingPatientFile = false, patientFile = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(loadingPatientFile = false) }
                    _effect.send(AdminClinicEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun setPatientTags(userId: Long, tags: List<String>) {
        val therapistId = _state.value.crmTherapistId ?: return
        viewModelScope.launch {
            when (val result = setPatientTagsUseCase(therapistId, userId, tags)) {
                is AppResult.Success -> {
                    _effect.send(AdminClinicEffect.ShowSuccess("برچسب‌ها به‌روزرسانی شد."))
                    loadPatients(therapistId)
                }
                is AppResult.Error -> _effect.send(AdminClinicEffect.ShowError(result.message))
                else -> {}
            }
        }
    }
}
