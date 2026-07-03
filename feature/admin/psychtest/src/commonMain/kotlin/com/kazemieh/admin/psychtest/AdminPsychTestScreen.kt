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
import androidx.compose.runtime.mutableStateListOf
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
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.psychtest.PsychTestSummary
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
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { CreateTestForm(state, viewModel) }
        items(state.tests.size) { idx ->
            TestRow(state.tests[idx], onDelete = { viewModel.deleteTest(state.tests[idx].id) })
        }
    }
}

@Composable
private fun CreateTestForm(state: AdminPsychTestState, viewModel: AdminPsychTestViewModel) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var productId by remember { mutableStateOf("") }
    var resultMode by remember { mutableStateOf("AUTO") }

    // پیش‌نویسِ سؤال
    var qText by remember { mutableStateOf("") }
    val opts = remember { mutableStateListOf("", "", "", "") }
    val scores = remember { mutableStateListOf("0", "1", "2", "3") }
    // پیش‌نویسِ بازه (فقط AUTO)
    var rMin by remember { mutableStateOf("") }
    var rMax by remember { mutableStateOf("") }
    var rText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surfaceVariant).padding(14.dp)
    ) {
        Text("افزودنِ تستِ جدید", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(10.dp))
        Field(title, { title = it }, "عنوانِ تست")
        Spacer(Modifier.height(8.dp))
        Field(slug, { slug = it }, "اسلاگ (لاتین، یکتا)")
        Spacer(Modifier.height(8.dp))
        Field(description, { description = it }, "توضیح")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { Field(price, { price = it }, "قیمت") }
            Box(Modifier.weight(1f)) { Field(productId, { productId = it }, "شناسه‌ی محصول") }
        }
        Spacer(Modifier.height(10.dp))
        Text("حالتِ نتیجه", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ModeChip("خودکار", resultMode == "AUTO") { resultMode = "AUTO" }
            ModeChip("تفسیرِ مشاور", resultMode == "COUNSELOR") { resultMode = "COUNSELOR" }
        }

        // ---- سازنده‌ی سؤال ----
        Spacer(Modifier.height(12.dp))
        Text("سؤال (${state.draftQuestions.size} سؤال افزوده شد)", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL)
        Spacer(Modifier.height(6.dp))
        Field(qText, { qText = it }, "متنِ سؤال")
        opts.forEachIndexed { i, _ ->
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(2f)) { Field(opts[i], { opts[i] = it }, "گزینه‌ی ${i + 1}") }
                Box(Modifier.weight(1f)) { Field(scores[i], { scores[i] = it }, "امتیاز") }
            }
        }
        Spacer(Modifier.height(6.dp))
        val canAddQ = qText.isNotBlank() && opts.count { it.isNotBlank() } >= 2
        SmallButton("افزودنِ سؤال", canAddQ) {
            val pairs = opts.mapIndexed { i, o -> o to (scores[i].toIntOrNull() ?: 0) }
            viewModel.addDraftQuestion(qText.trim(), pairs)
            qText = ""; opts[0] = ""; opts[1] = ""; opts[2] = ""; opts[3] = ""
        }

        // ---- سازنده‌ی بازه‌ی تفسیر (فقط AUTO) ----
        if (resultMode == "AUTO") {
            Spacer(Modifier.height(12.dp))
            Text("بازه‌ی امتیاز → تفسیر (${state.draftRanges.size})", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { Field(rMin, { rMin = it }, "از") }
                Box(Modifier.weight(1f)) { Field(rMax, { rMax = it }, "تا") }
            }
            Spacer(Modifier.height(4.dp))
            Field(rText, { rText = it }, "متنِ تفسیر")
            Spacer(Modifier.height(6.dp))
            SmallButton("افزودنِ بازه", rText.isNotBlank()) {
                viewModel.addDraftRange(rMin.trim(), rMax.trim(), rText.trim()); rMin = ""; rMax = ""; rText = ""
            }
        }

        Spacer(Modifier.height(12.dp))
        SmallButton("ساختِ تست", title.isNotBlank() && slug.isNotBlank()) {
            viewModel.createTest(title.trim(), slug.trim(), description.trim(), price.trim(), productId.trim(), resultMode)
            title = ""; slug = ""; description = ""; price = ""; productId = ""
        }
    }
}

@Composable
private fun TestRow(test: PsychTestSummary, onDelete: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(test.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            Spacer(Modifier.height(3.dp))
            Text("${test.questionCount} سؤال · ${if (test.resultMode.name == "AUTO") "خودکار" else "تفسیرِ مشاور"}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        }
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(colors.sale.copy(alpha = 0.1f)).clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale) }
    }
}

@Composable
private fun InterpretTab(pending: List<UserPsychTest>, onInterpret: (Long, String) -> Unit) {
    val colors = AppTheme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
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
        Field(text, { text = it }, "متنِ تفسیر")
        Spacer(Modifier.height(8.dp))
        SmallButton("ثبتِ تفسیر", text.isNotBlank()) { onInterpret(userTest.id, text.trim()); text = "" }
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

@Composable
private fun ModeChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold,
        color = if (active) colors.onPrimary else colors.onSurfaceVariant,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(if (active) colors.primary else colors.surface)
            .border(1.dp, if (active) colors.primary else colors.line, RoundedCornerShape(50))
            .clickable { onClick() }.padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun SmallButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(if (enabled) colors.primary else colors.line)
            .clickable(enabled = enabled) { onClick() }.padding(horizontal = 14.dp, vertical = 9.dp),
        color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Field(value: String, onValueChange: (String) -> Unit, label: String) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant) },
        singleLine = true, shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface, unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.primary, unfocusedBorderColor = colors.line,
            cursorColor = colors.primary, focusedTextColor = colors.onSurface, unfocusedTextColor = colors.onSurface
        )
    )
}
