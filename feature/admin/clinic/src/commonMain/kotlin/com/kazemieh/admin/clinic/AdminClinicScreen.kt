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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import com.kazemieh.designsystem.responsiveMaxWidth
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabChip("درمانگرها", tab == 0) { tab = 0 }
                    TabChip("نوبت‌ها", tab == 1) { tab = 1 }
                    TabChip("مراجعان", tab == 2) { tab = 2 }
                    TabChip("درخواستِ تعویض", tab == 3) { tab = 3; viewModel.loadSwitchRequests() }
                    TabChip("پرسشنامه‌ی تطبیق", tab == 4) { tab = 4; viewModel.loadMatchQuestions() }
                }
                // محتوای هر تب باید فضایِ باقی‌مانده را پر کند (نه اینکه ته صفحه کوچک دیده شود).
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        0 -> TherapistsTab(
                            state = state,
                            onToggle = viewModel::toggleExpand,
                            onDelete = viewModel::deleteTherapist,
                            onCreate = { n, s, p, d, pid, mode, loc, mpid -> viewModel.createTherapist(n, s, p, d, pid, mode, loc, mpid) },
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
                        4 -> MatchQuestionsTab(
                            state = state,
                            onCreate = viewModel::createMatchQuestion,
                            onDelete = viewModel::deleteMatchQuestion
                        )
                    }
                }
            }
        }
    }
}

/** لینکِ «بازگشت به فهرست» — وقتی یک آیتم برای مدیریت به‌صورتِ صفحه‌ی جدا باز شده است. */
@Composable
private fun BackToListRow(label: String, onBack: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .clickable { onBack() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(8.dp))
        Text(label, color = colors.primary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR)
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
    onCreate: (name: String, slug: String, sessionPrice: String, durationMinutes: String, productId: String, mode: String, location: String, messagingProductId: String) -> Unit,
    onAddSlot: (therapistId: Long, startTime: String, endTime: String, capacity: String) -> Unit,
    onGenerateSlots: (therapistId: Long, windowStart: String, windowEnd: String, slotMinutes: String, capacity: String) -> Unit
) {
    val colors = AppTheme.colors
    var showAddTherapist by remember { mutableStateOf(false) }
    if (state.isLoading && state.therapists.isEmpty()) {
        LoadingCard(modifier = Modifier.fillMaxSize())
        return
    }
    val selectedId = state.expandedTherapistId
    // با انتخابِ یک درمانگر، فقط همان (باز) نشان داده می‌شود — مثلِ صفحه‌ی مدیریتِ جدا.
    val visibleTherapists = if (selectedId != null) state.therapists.filter { it.id == selectedId } else state.therapists
    LazyColumn(
        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            if (selectedId == null) {
                SectionHeader(
                    title = "درمانگرها (${state.therapists.size})",
                    addLabel = "افزودن درمانگر",
                    onAdd = { showAddTherapist = true }
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "روی هر درمانگر بزنید تا صفحه‌ی مدیریتِ آن باز شود و بازه‌های زمانیِ آزاد اضافه کنید. اگر «شناسهٔ محصول» را پر کنی، رزرو نیازمندِ خریدِ آن محصول می‌شود.",
                    fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant
                )
            } else {
                BackToListRow("بازگشت به فهرستِ درمانگرها") { onToggle(selectedId) }
            }
        }
        items(visibleTherapists) { therapist ->
            TherapistCard(
                therapist = therapist,
                expanded = state.expandedTherapistId == therapist.id,
                slots = if (state.expandedTherapistId == therapist.id) state.expandedSlots else emptyList(),
                loadingSlots = state.loadingSlots && state.expandedTherapistId == therapist.id,
                onToggle = { onToggle(therapist.id) },
                onDelete = { onDelete(therapist.id) },
                onAddSlot = { start, end, cap -> onAddSlot(therapist.id, start, end, cap) },
                onGenerateSlots = { ws, we, sm, cap -> onGenerateSlots(therapist.id, ws, we, sm, cap) }
            )
        }
    }

    if (showAddTherapist) {
        AdminSheet(onDismiss = { showAddTherapist = false }) {
            AddTherapistForm(onSubmit = { n, s, p, d, pid, mode, loc, mpid ->
                onCreate(n, s, p, d, pid, mode, loc, mpid); showAddTherapist = false
            })
        }
    }
}

/** هدرِ استانداردِ هر تب: عنوان + دکمه‌ی افزودن (هم‌الگو با تبِ محصولات). */
@Composable
private fun SectionHeader(title: String, addLabel: String, onAdd: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR, color = colors.onSurface)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(colors.primary)
                .clickable { onAdd() }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(15.dp))
            Text(addLabel, color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold)
        }
    }
}

/** پوسته‌ی مشترکِ باتم‌شیتِ فرم‌ها — اسکرول‌پذیر و سازگار با کیبورد. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .responsiveMaxWidth(com.kazemieh.designsystem.ContentWidth.readable)
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            content()
        }
    }
}

@Composable
private fun AddTherapistForm(onSubmit: (name: String, slug: String, sessionPrice: String, durationMinutes: String, productId: String, mode: String, location: String, messagingProductId: String) -> Unit) {
    val colors = AppTheme.colors
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("45") }
    var productId by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("ONLINE") }
    var location by remember { mutableStateOf("") }
    var messagingProductId by remember { mutableStateOf("") }

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
        Spacer(Modifier.height(8.dp))
        AdminTextField(value = messagingProductId, onValueChange = { messagingProductId = it }, label = "شناسه‌ی محصولِ پیام‌رسانیِ نامحدود (اختیاری)")
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
                    onSubmit(name.trim(), slug.trim(), price.trim(), duration.trim(), productId.trim(), mode, location.trim(), messagingProductId.trim())
                    name = ""; slug = ""; price = ""; duration = "45"; productId = ""; mode = "ONLINE"; location = ""; messagingProductId = ""
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
    onAddSlot: (startTime: String, endTime: String, capacity: String) -> Unit,
    onGenerateSlots: (windowStart: String, windowEnd: String, slotMinutes: String, capacity: String) -> Unit
) {
    val colors = AppTheme.colors
    var showSlotPicker by remember { mutableStateOf(false) }
    var showGenerate by remember { mutableStateOf(false) }
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
                            val capacityLabel = if (slot.capacity > 1) " (${slot.bookedCount}/${slot.capacity} گروهی)" else if (slot.isBooked) " (رزروشده)" else ""
                            Text(
                                "${slotDayLabel(slot.startTime)} — ${slotTimeLabel(slot.startTime)} تا ${slotTimeLabel(slot.endTime)}$capacityLabel",
                                color = if (slot.isBooked) colors.onSurfaceVariant else colors.ok,
                                fontSize = FontSize.EXTRA_SMALL,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.primary)
                                .clickable { showSlotPicker = true }
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(14.dp))
                            Text("افزودنِ بازه", color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "تولیدِ خودکار",
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceVariant)
                                .clickable { showGenerate = true }
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            color = colors.onSurface, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showSlotPicker) {
        SlotPickerBottomSheet(
            therapistName = therapist.name,
            onDismiss = { showSlotPicker = false },
            onSubmit = { start, end, cap -> onAddSlot(start, end, cap); showSlotPicker = false }
        )
    }
    if (showGenerate) {
        AdminSheet(onDismiss = { showGenerate = false }) {
            Text("تولیدِ خودکارِ بازه‌ها", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            Spacer(Modifier.height(10.dp))
            GenerateSlotsForm(onSubmit = { ws, we, sm, cap -> onGenerateSlots(ws, we, sm, cap); showGenerate = false })
        }
    }
}

/** فرمِ تولیدِ خودکارِ بازه‌ها از یک بازه‌ی کاری. */
@Composable
private fun GenerateSlotsForm(onSubmit: (windowStart: String, windowEnd: String, slotMinutes: String, capacity: String) -> Unit) {
    val colors = AppTheme.colors
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("1") }
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
        Spacer(Modifier.height(6.dp))
        AdminTextField(value = capacity, onValueChange = { capacity = it }, label = "ظرفیتِ هر بازه (بیش‌تر از ۱ یعنی گروهی)")
        Spacer(Modifier.height(8.dp))
        Text(
            "ساختِ بازه‌ها",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (start.isNotBlank() && end.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = start.isNotBlank() && end.isNotBlank()) {
                    onSubmit(start.trim(), end.trim(), minutes.trim(), capacity.trim()); start = ""; end = ""; minutes = ""; capacity = "1"
                }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
        )
    }
}

/**
 * باتم‌شیتِ انتخابِ بازه: تقویم (روز) + ساعتِ شروع + مدت + ظرفیت، سپس زمان‌ها را به‌صورتِ
 * ISO-8601 با آفستِ +03:30 می‌سازد و ارسال می‌کند (به‌جایِ واردکردنِ دستیِ رشته‌ی خام).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotPickerBottomSheet(
    therapistName: String,
    onDismiss: () -> Unit,
    onSubmit: (startTime: String, endTime: String, capacity: String) -> Unit
) {
    val colors = AppTheme.colors
    val datePickerState = rememberDatePickerState()
    val timeState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)
    var durationMinutes by remember { mutableStateOf(60) }
    var capacity by remember { mutableStateOf(1) }

    AdminSheet(onDismiss = onDismiss) {
        Text("افزودنِ بازه برای $therapistName", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(4.dp))
        Text("۱) روزِ بازه را از تقویم انتخاب کن:", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        DatePicker(
            state = datePickerState,
            title = null,
            headline = null,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(containerColor = colors.surface)
        )
        Spacer(Modifier.height(10.dp))
        Text("۲) ساعتِ شروع:", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TimePicker(state = timeState)
        }
        Spacer(Modifier.height(12.dp))
        Text("۳) مدتِ جلسه (دقیقه):", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(30, 45, 60, 90).forEach { d ->
                ModeChip("$d دقیقه", durationMinutes == d) { durationMinutes = d }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("۴) ظرفیت (بیش‌تر از ۱ یعنی جلسه‌ی گروهی):", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 2, 3, 5, 10).forEach { c ->
                ModeChip(if (c == 1) "تکی" else "$c نفر", capacity == c) { capacity = c }
            }
        }
        Spacer(Modifier.height(16.dp))
        val dateMillis = datePickerState.selectedDateMillis
        val enabled = dateMillis != null
        Text(
            "ثبتِ بازه",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) colors.primary else colors.line)
                .clickable(enabled = enabled) {
                    val (startIso, endIso) = buildSlotIso(dateMillis!!, timeState.hour, timeState.minute, durationMinutes)
                    onSubmit(startIso, endIso, capacity.toString())
                }
                .padding(vertical = 13.dp),
            color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
    val selectedId = state.expandedNotesAppointmentId
    // با بازکردنِ یک نوبت، فقط همان (با یادداشت‌ها) نشان داده می‌شود — صفحه‌ی مدیریتِ جدا.
    val visibleAppointments = if (selectedId != null) state.appointments.filter { it.id == selectedId } else state.appointments
    LazyColumn(
        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectedId != null) {
            item { BackToListRow("بازگشت به فهرستِ نوبت‌ها") { onToggleNotes(selectedId) } }
        }
        items(visibleAppointments) { appointment ->
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
            else -> {
                val selectedUserId = state.expandedPatientUserId
                val visiblePatients = if (selectedUserId != null) state.patients.filter { it.userId == selectedUserId } else state.patients
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (selectedUserId != null) {
                        item { BackToListRow("بازگشت به فهرستِ مراجعان") { onTogglePatientFile(selectedUserId) } }
                    }
                    items(visiblePatients) { patient ->
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
private fun MatchQuestionsTab(
    state: AdminClinicState,
    onCreate: (questionText: String, tag: String, displayOrder: String) -> Unit,
    onDelete: (Long) -> Unit
) {
    val colors = AppTheme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "سؤال‌هایِ پرسشنامه‌ی تطبیقِ درمانگر را مدیریت کن. هر سؤال به یک «تگ» متصل است که با تخصصِ درمانگرها مقایسه می‌شود.",
                fontSize = FontSize.SMALL, color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            AddMatchQuestionForm(onSubmit = onCreate)
        }
        if (state.loadingMatchQuestions && state.matchQuestions.isEmpty()) {
            item { LoadingCard(modifier = Modifier.fillMaxWidth().height(120.dp)) }
        } else if (state.matchQuestions.isEmpty()) {
            item { Text("هنوز سؤالی تعریف نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL) }
        } else {
            items(state.matchQuestions) { q ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.line, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(q.questionText, color = colors.onSurface, fontSize = FontSize.SMALL)
                        Spacer(Modifier.height(3.dp))
                        Text("تگ: ${q.tag}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                    }
                    Box(
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(colors.sale.copy(alpha = 0.1f)).clickable { onDelete(q.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = colors.sale)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMatchQuestionForm(onSubmit: (questionText: String, tag: String, displayOrder: String) -> Unit) {
    val colors = AppTheme.colors
    var questionText by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var order by remember { mutableStateOf("0") }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surfaceVariant).padding(14.dp)
    ) {
        Text("افزودنِ سؤالِ جدید", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(10.dp))
        AdminTextField(value = questionText, onValueChange = { questionText = it }, label = "متنِ سؤال")
        Spacer(Modifier.height(8.dp))
        AdminTextField(value = tag, onValueChange = { tag = it }, label = "تگ (باید در تخصصِ درمانگر ظاهر شود، مثلاً «اضطراب»)")
        Spacer(Modifier.height(8.dp))
        AdminTextField(value = order, onValueChange = { order = it }, label = "ترتیبِ نمایش")
        Spacer(Modifier.height(10.dp))
        Text(
            "افزودنِ سؤال",
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (questionText.isNotBlank() && tag.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = questionText.isNotBlank() && tag.isNotBlank()) {
                    onSubmit(questionText.trim(), tag.trim(), order.trim())
                    questionText = ""; tag = ""; order = "0"
                }
                .padding(horizontal = 16.dp, vertical = 11.dp),
            color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
        )
    }
}

// ---------- کمک‌تابع‌های زمان/تاریخ (بدونِ وابستگیِ خارجی) ----------

/** برچسبِ کوتاهِ ساعت از رشته‌ی ISO (مثلاً «06:30» از «2026-07-14T06:30:00Z»). */
private fun slotTimeLabel(iso: String): String = iso.substringAfter('T', "").take(5)

/** برچسبِ کوتاهِ روز از رشته‌ی ISO (بخشِ تاریخِ «YYYY-MM-DD»). */
private fun slotDayLabel(iso: String): String = iso.take(10)

private fun pad2(n: Int): String = n.toString().padStart(2, '0')
private fun pad4(n: Int): String = n.toString().padStart(4, '0')

/** تبدیلِ «روزِ اپاک» به (سال، ماه، روز) — الگوریتمِ تقویمِ میلادیِ Howard Hinnant. */
private fun civilFromEpochDays(epochDay: Long): Triple<Int, Int, Int> {
    val z = epochDay + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
    val year = (if (m <= 2) y + 1 else y).toInt()
    return Triple(year, m, d)
}

private fun isoOf(epochDay: Long, hour: Int, minute: Int): String {
    val (y, m, d) = civilFromEpochDays(epochDay)
    return "${pad4(y)}-${pad2(m)}-${pad2(d)}T${pad2(hour)}:${pad2(minute)}:00+03:30"
}

/**
 * از تاریخِ انتخابیِ تقویم (میلی‌ثانیه‌ی UTCِ نیمه‌شب) + ساعتِ شروع + مدت،
 * جفتِ (شروع، پایان) را به‌صورتِ ISO با آفستِ +03:30 می‌سازد.
 */
private fun buildSlotIso(dateMillis: Long, startHour: Int, startMinute: Int, durationMinutes: Int): Pair<String, String> {
    val epochDay = dateMillis / 86_400_000L
    val startTotal = startHour * 60 + startMinute
    val endTotal = startTotal + durationMinutes
    val endEpochDay = epochDay + endTotal / 1440
    val endMinuteOfDay = endTotal % 1440
    val startIso = isoOf(epochDay, startHour, startMinute)
    val endIso = isoOf(endEpochDay, endMinuteOfDay / 60, endMinuteOfDay % 60)
    return startIso to endIso
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
