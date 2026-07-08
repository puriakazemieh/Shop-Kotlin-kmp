package com.kazemieh.admin.clinic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.clinic.AdminAppointment
import com.kazemieh.domain.clinic.AdminAppointmentStatus
import com.kazemieh.domain.clinic.AdminSlot
import com.kazemieh.domain.clinic.TherapistSummary
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClinicScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminClinicViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()
    var tab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadTherapists()
        viewModel.loadAppointments()
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AdminClinicEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminClinicEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text("مدیریتِ مشاوره", fontSize = FontSize.LARGE, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        ContentWithMessageBar(modifier = Modifier.padding(padding), messageBarState = messageBarState) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabChip("درمانگرها", tab == 0) { tab = 0 }
                    TabChip("نوبت‌ها", tab == 1) { tab = 1 }
                    TabChip("مراجعان", tab == 2) { tab = 2 }
                    TabChip("درخواستِ تعویض", tab == 3) { tab = 3; viewModel.loadSwitchRequests() }
                }
                when (tab) {
                    0 -> TherapistsTab(
                        state = state,
                        onToggle = viewModel::toggleExpand,
                        onDelete = viewModel::deleteTherapist,
                        onCreate = { n, s, p, d, pid, mode, loc -> viewModel.createTherapist(n, s, p, d, pid, mode, loc) },
                        onAddSlot = viewModel::addSlot,
                        onGenerateSlots = viewModel::generateSlots
                    )
                    1 -> AppointmentsTab(
                        state = state,
                        onConfirm = viewModel::confirmAppointment,
                        onComplete = viewModel::completeAppointment,
                        onToggleNotes = viewModel::toggleNotes,
                        onAddNote = viewModel::addNote
                    )
                    2 -> PatientsTab(
                        state = state,
                        onSelectTherapist = viewModel::selectCrmTherapist,
                        onTogglePatientFile = viewModel::togglePatientFile,
                        onSetTags = viewModel::setPatientTags
                    )
                    3 -> SwitchRequestsTab(
                        state = state,
                        onReview = viewModel::reviewSwitchRequest
                    )
                }
            }
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primary else colors.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
    )
}

@Composable
private fun TherapistsTab(
    state: AdminClinicState,
    onToggle: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onCreate: (name: String, slug: String, sessionPrice: String, durationMinutes: String, productId: String, mode: String, location: String) -> Unit,
    onAddSlot: (therapistId: Long, startTime: String, endTime: String) -> Unit,
    onGenerateSlots: (therapistId: Long, windowStart: String, windowEnd: String, slotMinutes: String) -> Unit
) {
    val colors = AppTheme.colors
    if (state.isLoading && state.therapists.isEmpty()) {
        LoadingCard(modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "درمانگر بساز، سپس با بازکردنِ آن، بازه‌های زمانیِ آزاد اضافه کن. اگر «شناسهٔ محصول» را پر کنی، رزرو نیازمندِ خریدِ آن محصول می‌شود.",
                fontSize = FontSize.SMALL, color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            AddTherapistForm(onSubmit = onCreate)
        }
        items(state.therapists) { therapist ->
            TherapistCard(
                therapist = therapist,
                expanded = state.expandedTherapistId == therapist.id,
                slots = if (state.expandedTherapistId == therapist.id) state.expandedSlots else emptyList(),
                loadingSlots = state.loadingSlots && state.expandedTherapistId == therapist.id,
                onToggle = { onToggle(therapist.id) },
                onDelete = { onDelete(therapist.id) },
                onAddSlot = { start, end -> onAddSlot(therapist.id, start, end) },
                onGenerateSlots = { ws, we, sm -> onGenerateSlots(therapist.id, ws, we, sm) }
            )
        }
    }
}

@Composable
private fun AddTherapistForm(onSubmit: (name: String, slug: String, sessionPrice: String, durationMinutes: String, productId: String, mode: String, location: String) -> Unit) {
    val colors = AppTheme.colors
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("45") }
    var productId by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("ONLINE") }
    var location by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .padding(14.dp)
    ) {
        Text("افزودنِ درمانگرِ جدید", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(10.dp))
        AdminTextField(value = name, onValueChange = { name = it }, label = "نامِ درمانگر")
        Spacer(Modifier.height(8.dp))
        AdminTextField(value = slug, onValueChange = { slug = it }, label = "اسلاگ (لاتین، یکتا)")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                AdminTextField(value = price, onValueChange = { price = it }, label = "قیمتِ هر جلسه")
            }
            Box(modifier = Modifier.weight(1f)) {
                AdminTextField(value = duration, onValueChange = { duration = it }, label = "مدتِ جلسه (دقیقه)")
            }
        }
        Spacer(Modifier.height(8.dp))
        AdminTextField(value = productId, onValueChange = { productId = it }, label = "شناسه‌ی محصول (اختیاری — برای گیتِ خرید)")
        Spacer(Modifier.height(10.dp))
        Text("نحوه‌ی برگزاری", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ModeChip("آنلاین", mode == "ONLINE") { mode = "ONLINE" }
            ModeChip("حضوری", mode == "IN_PERSON") { mode = "IN_PERSON" }
            ModeChip("تلفنی", mode == "PHONE") { mode = "PHONE" }
        }
        if (mode == "IN_PERSON") {
            Spacer(Modifier.height(8.dp))
            AdminTextField(value = location, onValueChange = { location = it }, label = "نشانیِ محلِ برگزاری")
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "ساختِ درمانگر",
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (name.isNotBlank() && slug.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = name.isNotBlank() && slug.isNotBlank()) {
                    onSubmit(name.trim(), slug.trim(), price.trim(), duration.trim(), productId.trim(), mode, location.trim())
                    name = ""; slug = ""; price = ""; duration = "45"; productId = ""; mode = "ONLINE"; location = ""
                }
                .padding(horizontal = 16.dp, vertical = 11.dp),
            color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
        )
    }
}

@Composable
private fun ModeChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold,
        color = if (active) colors.onPrimary else colors.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) colors.primary else colors.surface)
            .border(1.dp, if (active) colors.primary else colors.line, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun TherapistCard(
    therapist: TherapistSummary,
    expanded: Boolean,
    slots: List<AdminSlot>,
    loadingSlots: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onAddSlot: (startTime: String, endTime: String) -> Unit,
    onGenerateSlots: (windowStart: String, windowEnd: String, slotMinutes: String) -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(therapist.name, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                Spacer(Modifier.height(3.dp))
                val gate = if (therapist.requiresPurchase) " · نیازمندِ خرید" else " · رایگان"
                Text("اسلاگ: ${therapist.slug}$gate", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
            }
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(colors.sale.copy(alpha = 0.1f)).clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale)
            }
        }

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            when {
                loadingSlots -> Text("در حالِ بارگذاری…", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                else -> {
                    if (slots.isEmpty()) {
                        Text("بازه‌ای ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                    } else {
                        slots.forEach { slot ->
                            Text(
                                "${slot.startTime} – ${slot.endTime}" + if (slot.isBooked) " (رزروشده)" else "",
                                color = if (slot.isBooked) colors.onSurfaceVariant else colors.ok,
                                fontSize = FontSize.EXTRA_SMALL,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    AddSlotForm(onSubmit = onAddSlot)
                    Spacer(Modifier.height(10.dp))
                    GenerateSlotsForm(onSubmit = onGenerateSlots)
                }
            }
        }
    }
}

/** فرمِ تولیدِ خودکارِ بازه‌ها از یک بازه‌ی کاری. */
@Composable
private fun GenerateSlotsForm(onSubmit: (windowStart: String, windowEnd: String, slotMinutes: String) -> Unit) {
    val colors = AppTheme.colors
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant).padding(10.dp)
    ) {
        Text("تولیدِ خودکارِ بازه‌ها", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL)
        Spacer(Modifier.height(4.dp))
        Text(
            "بازه‌ی کاری را وارد کن؛ سرور آن را به بازه‌های کوچک تقسیم می‌کند. فرمتِ ISO با آفست، مثلاً 2026-07-10T09:00:00+03:30",
            fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        AdminTextField(value = start, onValueChange = { start = it }, label = "شروعِ بازه‌ی کاری")
        Spacer(Modifier.height(6.dp))
        AdminTextField(value = end, onValueChange = { end = it }, label = "پایانِ بازه‌ی کاری")
        Spacer(Modifier.height(6.dp))
        AdminTextField(value = minutes, onValueChange = { minutes = it }, label = "مدتِ هر بازه (دقیقه — خالی = مدتِ جلسه)")
        Spacer(Modifier.height(8.dp))
        Text(
            "ساختِ بازه‌ها",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (start.isNotBlank() && end.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = start.isNotBlank() && end.isNotBlank()) {
                    onSubmit(start.trim(), end.trim(), minutes.trim()); start = ""; end = ""; minutes = ""
                }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddSlotForm(onSubmit: (startTime: String, endTime: String) -> Unit) {
    val colors = AppTheme.colors
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .padding(10.dp)
    ) {
        Text(
            "شروع/پایانِ بازه به‌فرمتِ ISO با آفستِ زمانی، مثلاً 2026-07-10T14:00:00+03:30",
            fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        AdminTextField(value = start, onValueChange = { start = it }, label = "شروعِ بازه")
        Spacer(Modifier.height(6.dp))
        AdminTextField(value = end, onValueChange = { end = it }, label = "پایانِ بازه")
        Spacer(Modifier.height(8.dp))
        Text(
            "افزودنِ بازه",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (start.isNotBlank() && end.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = start.isNotBlank() && end.isNotBlank()) {
                    onSubmit(start.trim(), end.trim())
                    start = ""; end = ""
                }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AppointmentsTab(
    state: AdminClinicState,
    onConfirm: (id: Long, videoRoomUrl: String) -> Unit,
    onComplete: (Long) -> Unit,
    onToggleNotes: (Long) -> Unit,
    onAddNote: (Long, String) -> Unit
) {
    val colors = AppTheme.colors
    if (state.loadingAppointments && state.appointments.isEmpty()) {
        LoadingCard(modifier = Modifier.fillMaxSize())
        return
    }
    if (state.appointments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("نوبتی ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.appointments) { appointment ->
            AppointmentAdminCard(
                appointment = appointment,
                onConfirm = onConfirm,
                onComplete = { onComplete(appointment.id) },
                notesExpanded = state.expandedNotesAppointmentId == appointment.id,
                notes = state.notesByAppointment[appointment.id].orEmpty(),
                loadingNotes = state.loadingNotes && state.expandedNotesAppointmentId == appointment.id,
                onToggleNotes = onToggleNotes,
                onAddNote = onAddNote
            )
        }
    }
}

@Composable
private fun AppointmentAdminCard(
    appointment: AdminAppointment,
    onConfirm: (id: Long, videoRoomUrl: String) -> Unit,
    onComplete: () -> Unit,
    notesExpanded: Boolean,
    notes: List<com.kazemieh.domain.clinic.PatientNote>,
    loadingNotes: Boolean,
    onToggleNotes: (Long) -> Unit,
    onAddNote: (Long, String) -> Unit
) {
    val colors = AppTheme.colors
    var videoUrl by remember { mutableStateOf(appointment.videoRoomUrl.orEmpty()) }
    val (statusLabel, statusColor) = when (appointment.status) {
        AdminAppointmentStatus.PENDING -> "در انتظارِ تأیید" to colors.star
        AdminAppointmentStatus.CONFIRMED -> "تأییدشده" to colors.ok
        AdminAppointmentStatus.COMPLETED -> "برگزارشده" to colors.onSurfaceVariant
        AdminAppointmentStatus.CANCELLED -> "لغوشده" to colors.sale
        AdminAppointmentStatus.UNKNOWN -> "-" to colors.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(appointment.therapistName, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR, modifier = Modifier.weight(1f))
            Text(
                statusLabel,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(statusColor.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp),
                color = statusColor, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("کاربر #${appointment.userId} · ${appointment.dayLabel} ${appointment.timeLabel}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        if (!appointment.notes.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("یادداشت: ${appointment.notes}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        }

        if (appointment.status == AdminAppointmentStatus.PENDING || appointment.status == AdminAppointmentStatus.CONFIRMED) {
            Spacer(Modifier.height(10.dp))
            AdminTextField(
                value = videoUrl, onValueChange = { videoUrl = it },
                label = if (appointment.isPhone) "شماره‌ی تماس برای این جلسه" else "لینکِ اتاقِ تماس (Jitsi/Meet)"
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (appointment.isPhone) "تأیید و ثبتِ شماره" else "تأیید و ثبتِ لینک",
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (videoUrl.isNotBlank()) colors.primary else colors.line)
                        .clickable(enabled = videoUrl.isNotBlank()) { onConfirm(appointment.id, videoUrl.trim()) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
                )
                if (appointment.status == AdminAppointmentStatus.CONFIRMED) {
                    Text(
                        "علامت‌گذاریِ برگزارشده",
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                            .clickable { onComplete() }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        color = colors.onSurface, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ---- یادداشتِ محرمانه‌ی مراجع (فقط ادمین/مشاور) ----
        Spacer(Modifier.height(10.dp))
        Text(
            if (notesExpanded) "بستنِ یادداشت‌ها" else "یادداشتِ محرمانه‌ی مراجع",
            modifier = Modifier.clickable { onToggleNotes(appointment.id) }.padding(vertical = 4.dp),
            color = colors.primary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold
        )
        if (notesExpanded) {
            PatientNotesPanel(
                notes = notes,
                loading = loadingNotes,
                onAddNote = { text -> onAddNote(appointment.id, text) }
            )
        }
    }
}

@Composable
private fun PatientNotesPanel(
    notes: List<com.kazemieh.domain.clinic.PatientNote>,
    loading: Boolean,
    onAddNote: (String) -> Unit
) {
    val colors = AppTheme.colors
    var text by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            "این یادداشت‌ها محرمانه‌اند و فقط برای ادمین/مشاور قابلِ‌مشاهده‌اند.",
            color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL
        )
        Spacer(Modifier.height(8.dp))
        when {
            loading -> Text("در حالِ بارگذاری…", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
            notes.isEmpty() -> Text("یادداشتی ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
            else -> notes.forEach { note ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(note.note, color = colors.onSurface, fontSize = FontSize.SMALL)
                    Text(note.createdAt.take(19), color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        AdminTextField(value = text, onValueChange = { text = it }, label = "یادداشتِ جدید")
        Spacer(Modifier.height(6.dp))
        Text(
            "افزودنِ یادداشت",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (text.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = text.isNotBlank()) { onAddNote(text.trim()); text = "" }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
        )
    }
}

/** CRMِ سبکِ مراجعان: انتخابِ درمانگر → فهرستِ مراجعان با برچسب → پرونده‌ی کاملِ مراجع (نوبت‌ها+یادداشت‌ها+نتایجِ تست). */
@Composable
private fun PatientsTab(
    state: AdminClinicState,
    onSelectTherapist: (Long) -> Unit,
    onTogglePatientFile: (Long) -> Unit,
    onSetTags: (userId: Long, tags: List<String>) -> Unit
) {
    val colors = AppTheme.colors
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("ابتدا یک درمانگر را انتخاب کن:", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.therapists.forEach { t ->
                ModeChip(t.name, state.crmTherapistId == t.id) { onSelectTherapist(t.id) }
            }
        }
        Spacer(Modifier.height(12.dp))
        when {
            state.crmTherapistId == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("درمانگری را انتخاب کن تا فهرستِ مراجعانش دیده شود.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            }
            state.loadingPatients && state.patients.isEmpty() -> LoadingCard(modifier = Modifier.fillMaxSize())
            state.patients.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هنوز مراجعی برای این درمانگر ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            }
            else -> LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.patients) { patient ->
                    PatientCard(
                        patient = patient,
                        expanded = state.expandedPatientUserId == patient.userId,
                        file = if (state.expandedPatientUserId == patient.userId) state.patientFile else null,
                        loadingFile = state.loadingPatientFile && state.expandedPatientUserId == patient.userId,
                        onToggle = { onTogglePatientFile(patient.userId) },
                        onSetTags = { tags -> onSetTags(patient.userId, tags) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientCard(
    patient: com.kazemieh.domain.clinic.AdminPatientSummary,
    expanded: Boolean,
    file: com.kazemieh.domain.clinic.PatientFile?,
    loadingFile: Boolean,
    onToggle: () -> Unit,
    onSetTags: (List<String>) -> Unit
) {
    val colors = AppTheme.colors
    var tagsText by remember(patient.userId) { mutableStateOf(patient.tags.joinToString("، ")) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(patient.userName, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                Spacer(Modifier.height(3.dp))
                Text("${patient.appointmentCount} نوبت" + (patient.lastAppointmentAt?.let { " · آخرین: ${it.take(10)}" } ?: ""), color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                if (patient.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        patient.tags.forEach { tag ->
                            Text(
                                tag, fontSize = FontSize.EXTRA_SMALL, color = colors.primary,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(colors.accentSoft).padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            AdminTextField(value = tagsText, onValueChange = { tagsText = it }, label = "برچسب‌ها (با کاما جدا کن، مثلاً «نیازمندِ پیگیری»)")
            Spacer(Modifier.height(6.dp))
            Text(
                "ذخیره‌ی برچسب‌ها",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.primary)
                    .clickable { onSetTags(tagsText.split(",", "،").map { it.trim() }.filter { it.isNotEmpty() }) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            when {
                loadingFile -> Text("در حالِ بارگذاریِ پرونده…", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                file != null -> PatientFileView(file)
            }
        }
    }
}

@Composable
private fun PatientFileView(file: com.kazemieh.domain.clinic.PatientFile) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant).padding(12.dp)
    ) {
        Text("پرونده‌ی مراجع", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
        Spacer(Modifier.height(8.dp))
        Text("نوبت‌ها", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.EXTRA_SMALL)
        if (file.appointments.isEmpty()) {
            Text("نوبتی ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        } else {
            file.appointments.forEach { appt ->
                Spacer(Modifier.height(4.dp))
                Text("· ${appt.dayLabel} ${appt.timeLabel} — ${appt.status}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                appt.notes.forEach { note ->
                    Text("  یادداشت: ${note.note}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL, modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
        if (file.testResults.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("نتایجِ تست‌های روان‌شناسی", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.EXTRA_SMALL)
            file.testResults.forEach { t ->
                Spacer(Modifier.height(4.dp))
                Text(
                    "· ${t.testTitle}" + (t.totalScore?.let { " — نمره: $it" } ?: "") + (t.interpretation?.let { " ($it)" } ?: ""),
                    color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL
                )
            }
        }
    }
}

@Composable
private fun SwitchRequestsTab(
    state: AdminClinicState,
    onReview: (id: Long, approve: Boolean, adminNote: String?) -> Unit
) {
    val colors = AppTheme.colors
    if (state.loadingSwitchRequests && state.switchRequests.isEmpty()) {
        LoadingCard(modifier = Modifier.fillMaxSize())
        return
    }
    if (state.switchRequests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("درخواستِ تعویضی ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.switchRequests) { req ->
            SwitchRequestCard(req = req, onReview = onReview)
        }
    }
}

@Composable
private fun SwitchRequestCard(
    req: com.kazemieh.domain.clinic.AdminSwitchRequest,
    onReview: (id: Long, approve: Boolean, adminNote: String?) -> Unit
) {
    val colors = AppTheme.colors
    val (statusLabel, statusColor) = when (req.status) {
        com.kazemieh.domain.clinic.SwitchRequestStatus.PENDING -> "در انتظارِ بررسی" to colors.star
        com.kazemieh.domain.clinic.SwitchRequestStatus.APPROVED -> "تأییدشده" to colors.ok
        com.kazemieh.domain.clinic.SwitchRequestStatus.REJECTED -> "ردشده" to colors.sale
        com.kazemieh.domain.clinic.SwitchRequestStatus.UNKNOWN -> "-" to colors.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                req.userName ?: "کاربر #${req.userId}",
                fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR,
                modifier = Modifier.weight(1f)
            )
            Text(
                statusLabel,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(statusColor.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp),
                color = statusColor, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("از: ${req.fromTherapistName}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        if (!req.reason.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("دلیل: ${req.reason}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        }
        if (req.status == com.kazemieh.domain.clinic.SwitchRequestStatus.PENDING) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "تأیید",
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.primary)
                        .clickable { onReview(req.id, true, null) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
                )
                Text(
                    "ردّ",
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant)
                        .clickable { onReview(req.id, false, null) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    color = colors.sale, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AdminTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.line,
            cursorColor = colors.primary,
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface
        )
    )
}
