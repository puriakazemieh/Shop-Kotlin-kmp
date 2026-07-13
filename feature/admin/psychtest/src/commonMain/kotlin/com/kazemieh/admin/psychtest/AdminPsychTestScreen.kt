package com.kazemieh.admin.psychtest

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.domain.psychtest.AdminScoreRange
import com.kazemieh.domain.psychtest.AdminTestQuestion
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.kazemieh.domain.psychtest.TestResultMode
import com.kazemieh.domain.psychtest.UserPsychTest
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPsychTestScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminPsychTestViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()
    var tab by remember { mutableStateOf(0) } // 0 = تست‌ها، 1 = تفسیرها

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AdminPsychTestEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminPsychTestEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text("مدیریتِ تست‌ها", fontSize = FontSize.LARGE, color = colors.onSurface) },
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
            Column(Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TabChip("تست‌ها", tab == 0) { tab = 0 }
                    TabChip("تفسیرها (${state.pending.size})", tab == 1) { tab = 1 }
                }
                Box(Modifier.fillMaxSize()) {
                    if (tab == 0) TestsTab(state, viewModel) else InterpretTab(state.pending, viewModel::interpret)
                }
            }
        }
    }
}

@Composable
private fun TestsTab(state: AdminPsychTestState, viewModel: AdminPsychTestViewModel) {
    val colors = AppTheme.colors
    var showCreate by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "مدیریتِ تست‌های روان‌شناسی",
                    fontWeight = FontWeight.Bold, fontSize = FontSize.EXTRA_REGULAR, color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(colors.primary)
                        .clickable { showCreate = true }.padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
                    Text("ساختِ تستِ جدید", color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (state.tests.isEmpty()) {
            item { Text("هنوز تستی ساخته نشده. با دکمه‌ی «ساختِ تستِ جدید» شروع کن.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, modifier = Modifier.padding(top = 8.dp)) }
        }
        items(state.tests.size) { idx ->
            TestCard(
                state.tests[idx],
                onClick = { viewModel.openDetail(state.tests[idx].slug) },
                onEdit = { viewModel.openDetail(state.tests[idx].slug) },
                onDelete = { viewModel.deleteTest(state.tests[idx].id) }
            )
        }
    }

    if (showCreate) {
        PsychTestSheet(onDismiss = { showCreate = false }) {
            CreateTestForm(viewModel, onDone = { showCreate = false })
        }
    }

    if (state.detailLoadingSlug != null || state.selectedDetail != null) {
        PsychTestSheet(onDismiss = { viewModel.closeDetail() }) {
            TestDetailContent(loading = state.detailLoadingSlug != null, detail = state.selectedDetail)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PsychTestSheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().responsiveMaxWidth(com.kazemieh.designsystem.ContentWidth.readable)
                .padding(horizontal = 20.dp).padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()).imePadding()
        ) { content() }
    }
}

// =============================== لیستِ تست‌ها (کارت) ===============================

@Composable
private fun TestCard(test: PsychTestSummary, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            Text(test.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.EXTRA_REGULAR)
            Spacer(Modifier.height(6.dp))
            Text(
                "${test.questionCount} سؤال · ${formatToman(test.price)}",
                color = colors.onSurfaceVariant, fontSize = FontSize.SMALL
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SquareIcon(Icons.Default.Edit, colors.primary) { onEdit() }
                SquareIcon(Icons.Default.Delete, colors.sale) { onDelete() }
            }
        }
        Spacer(Modifier.width(10.dp))
        ResultBadge(test.resultMode)
    }
}

@Composable
private fun ResultBadge(mode: TestResultMode) {
    val colors = AppTheme.colors
    val label = if (mode == TestResultMode.AUTO) "نتیجه‌ی آنی" else "تفسیرِ مشاور"
    Text(
        label, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold, color = colors.primary,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(colors.accentSoft).padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun SquareIcon(icon: ImageVector, tint: Color, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
            .border(1.dp, colors.line, RoundedCornerShape(10.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp)) }
}

/** نمایشِ محتوایِ کاملِ یک تست (عنوان/توضیح/سؤال‌ها) هنگامِ کلیک روی ردیف. */
@Composable
private fun TestDetailContent(loading: Boolean, detail: com.kazemieh.domain.psychtest.PsychTestDetail?) {
    val colors = AppTheme.colors
    when {
        loading -> {
            Text("در حالِ بارگذاریِ محتوا…", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, modifier = Modifier.padding(vertical = 24.dp))
        }
        detail != null -> {
            Text(detail.title, fontWeight = FontWeight.ExtraBold, color = colors.onSurface, fontSize = FontSize.MEDIUM)
            if (!detail.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(detail.description!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${detail.questions.size} سؤال · ${if (detail.resultMode == TestResultMode.AUTO) "نتیجه‌ی خودکار" else "تفسیرِ مشاور"}",
                color = colors.primary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            detail.questions.forEachIndexed { i, q ->
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text("${i + 1}. ${q.text}", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL)
                    q.options.forEach { opt ->
                        Text("• ${opt.text}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL, modifier = Modifier.padding(start = 10.dp, top = 2.dp))
                    }
                }
            }
            if (detail.questions.isEmpty()) {
                Text("این تست هنوز سؤالی ندارد.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            }
        }
    }
}

// =============================== فرمِ ساخت/ویرایشِ تست ===============================

/** پیش‌نویسِ یک سؤال در فرم. */
private class QuestionDraftUi {
    var text by mutableStateOf("")
    val options = mutableStateListOf("", "", "", "")
}

/** پیش‌نویسِ یک بازه‌ی امتیاز→تفسیر (فقط حالتِ خودکار). */
private class RangeDraftUi {
    var min by mutableStateOf("")
    var max by mutableStateOf("")
    var text by mutableStateOf("")
}

@Composable
private fun CreateTestForm(viewModel: AdminPsychTestViewModel, onDone: () -> Unit) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var resultMode by remember { mutableStateOf("AUTO") }
    val questions = remember { mutableStateListOf(QuestionDraftUi()) }
    val ranges = remember { mutableStateListOf<RangeDraftUi>() }

    Text("ساخت / ویرایشِ تستِ روان‌شناسی", fontWeight = FontWeight.ExtraBold, color = colors.onSurface, fontSize = FontSize.MEDIUM)
    Spacer(Modifier.height(18.dp))

    LabeledField("عنوانِ تست", title, { title = it }, "مثلاً تستِ شخصیت‌شناسی")
    Spacer(Modifier.height(14.dp))
    LabeledField("قیمت (تومان)", price, { v -> price = v.filter { it.isDigit() } }, "350000", numeric = true)
    Spacer(Modifier.height(14.dp))
    LabeledField("توضیحِ کوتاه", description, { description = it }, "توضیحِ کوتاه درباره‌ی تست")
    Spacer(Modifier.height(18.dp))

    Text("نحوه‌ی ارائه‌ی نتیجه", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ModeChip("نتیجه‌ی آنی (خودکار)", resultMode == "AUTO", Modifier.weight(1f)) { resultMode = "AUTO" }
        ModeChip("تفسیر توسط مشاور", resultMode == "COUNSELOR", Modifier.weight(1f)) { resultMode = "COUNSELOR" }
    }
    Spacer(Modifier.height(18.dp))

    SectionHeader("سؤالاتِ تست", "+ افزودنِ سؤال") { questions.add(QuestionDraftUi()) }
    Spacer(Modifier.height(10.dp))
    questions.forEachIndexed { i, q ->
        QuestionEditor(q, canRemove = questions.size > 1) { questions.removeAt(i) }
        Spacer(Modifier.height(10.dp))
    }

    if (resultMode == "AUTO") {
        Spacer(Modifier.height(6.dp))
        SectionHeader("بازه‌ی امتیاز ← تفسیر", "+ افزودنِ بازه") { ranges.add(RangeDraftUi()) }
        Spacer(Modifier.height(10.dp))
        ranges.forEachIndexed { i, r ->
            RangeEditor(r) { ranges.removeAt(i) }
            Spacer(Modifier.height(10.dp))
        }
    }

    Spacer(Modifier.height(20.dp))
    val canSave = title.isNotBlank() &&
        questions.any { it.text.isNotBlank() && it.options.count { o -> o.isNotBlank() } >= 2 }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BigButton("ذخیرهٔ تست", filled = true, enabled = canSave, modifier = Modifier.weight(1f)) {
            val qs = questions
                .filter { it.text.isNotBlank() && it.options.count { o -> o.isNotBlank() } >= 2 }
                .map { q ->
                    AdminTestQuestion(
                        q.text.trim(),
                        q.options.mapIndexedNotNull { idx, o -> if (o.isNotBlank()) o.trim() to idx else null }
                    )
                }
            val rs = ranges
                .filter { it.text.isNotBlank() }
                .map { AdminScoreRange(it.min.toIntOrNull() ?: 0, it.max.toIntOrNull() ?: 0, it.text.trim()) }
            val slug = "test-" + kotlin.random.Random.nextInt(100_000, 999_999)
            viewModel.createTest(title.trim(), slug, description.trim(), price.trim(), "", resultMode, qs, rs)
            onDone()
        }
        BigButton("انصراف", filled = false, enabled = true, modifier = Modifier.weight(1f)) { onDone() }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
        Text(
            action, color = colors.primary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onAction() }.padding(horizontal = 4.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun QuestionEditor(q: QuestionDraftUi, canRemove: Boolean, onRemove: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.line, RoundedCornerShape(14.dp)).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { FilledField(q.text, { q.text = it }, "متنِ سؤال") }
            if (canRemove) RemoveButton(onRemove)
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { FilledField(q.options[0], { q.options[0] = it }, "گزینه") }
            Box(Modifier.weight(1f)) { FilledField(q.options[1], { q.options[1] = it }, "گزینه") }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { FilledField(q.options[2], { q.options[2] = it }, "گزینه") }
            Box(Modifier.weight(1f)) { FilledField(q.options[3], { q.options[3] = it }, "گزینه") }
        }
    }
}

@Composable
private fun RangeEditor(r: RangeDraftUi, onRemove: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.line, RoundedCornerShape(14.dp)).padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { FilledField(r.min, { v -> r.min = v.filter { it.isDigit() } }, "از", numeric = true) }
            Box(Modifier.weight(1f)) { FilledField(r.max, { v -> r.max = v.filter { it.isDigit() } }, "تا", numeric = true) }
            RemoveButton(onRemove)
        }
        Spacer(Modifier.height(8.dp))
        FilledField(r.text, { r.text = it }, "متنِ تفسیرِ این بازه")
    }
}

@Composable
private fun RemoveButton(onRemove: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
            .border(1.dp, colors.line, RoundedCornerShape(10.dp)).clickable { onRemove() },
        contentAlignment = Alignment.Center
    ) { Icon(Icons.Default.Close, contentDescription = null, tint = colors.sale, modifier = Modifier.size(16.dp)) }
}

@Composable
private fun LabeledField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, numeric: Boolean = false) {
    val colors = AppTheme.colors
    Text(label, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
    Spacer(Modifier.height(8.dp))
    FilledField(value, onValueChange, placeholder, numeric)
}

@Composable
private fun FilledField(value: String, onValueChange: (String) -> Unit, placeholder: String, numeric: Boolean = false) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
        singleLine = true, shape = RoundedCornerShape(12.dp),
        keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant, unfocusedContainerColor = colors.surfaceVariant,
            focusedBorderColor = colors.primary, unfocusedBorderColor = Color.Transparent,
            cursorColor = colors.primary, focusedTextColor = colors.onSurface, unfocusedTextColor = colors.onSurface
        )
    )
}

@Composable
private fun ModeChip(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
        color = if (active) colors.onPrimary else colors.onSurfaceVariant,
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(if (active) colors.primary else colors.surface)
            .border(1.dp, if (active) colors.primary else colors.line, RoundedCornerShape(12.dp))
            .clickable { onClick() }.padding(vertical = 13.dp)
    )
}

@Composable
private fun BigButton(label: String, filled: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR,
        color = if (filled) colors.onPrimary else colors.primary,
        modifier = modifier.clip(RoundedCornerShape(13.dp))
            .background(if (filled) (if (enabled) colors.primary else colors.line) else colors.surface)
            .then(if (filled) Modifier else Modifier.border(1.dp, colors.line, RoundedCornerShape(13.dp)))
            .clickable(enabled = enabled) { onClick() }.padding(vertical = 14.dp)
    )
}

// =============================== تبِ تفسیرها ===============================

@Composable
private fun InterpretTab(pending: List<UserPsychTest>, onInterpret: (Long, String) -> Unit) {
    val colors = AppTheme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pending.isEmpty()) {
            item { Text("موردی برای تفسیر نیست.", color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR, modifier = Modifier.padding(top = 24.dp)) }
        }
        items(pending.size) { idx ->
            InterpretCard(pending[idx], onInterpret)
        }
    }
}

@Composable
private fun InterpretCard(userTest: UserPsychTest, onInterpret: (Long, String) -> Unit) {
    val colors = AppTheme.colors
    var text by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Text(userTest.testTitle, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        userTest.totalScore?.let {
            Spacer(Modifier.height(4.dp))
            Text("امتیازِ کاربر: $it", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
        Spacer(Modifier.height(8.dp))
        FilledField(text, { text = it }, "متنِ تفسیر")
        Spacer(Modifier.height(8.dp))
        BigButton("ثبتِ تفسیر", filled = true, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            onInterpret(userTest.id, text.trim()); text = ""
        }
    }
}

@Composable
private fun TabChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold,
        color = if (active) colors.onPrimary else colors.onSurfaceVariant,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(if (active) colors.primary else colors.surfaceVariant)
            .clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
