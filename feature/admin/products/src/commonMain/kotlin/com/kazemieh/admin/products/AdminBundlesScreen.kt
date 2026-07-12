package com.kazemieh.admin.products

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
import androidx.compose.foundation.lazy.items
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
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.bundle.AdminBundle
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

/** مدیریتِ باندل/پکیجِ ترکیبیِ محصول. باندل خودش یک محصولِ واقعی است (productId)؛ اعضا فقط نمایشی‌اند. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBundlesScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminBundlesViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AdminBundlesEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminBundlesEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text("مدیریتِ باندل‌ها", fontSize = FontSize.LARGE, color = colors.onSurface) },
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
            if (state.isLoading && state.bundles.isEmpty()) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("باندل/پکیجِ ترکیبی", fontSize = FontSize.EXTRA_REGULAR, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "اول یک محصولِ واقعی برای این باندل بساز (در تبِ محصولات)، سپس اینجا شناسه‌ی آن را وارد کن. " +
                                "اعضا فقط برای نمایشِ «شاملِ چه چیزهایی است» هستند؛ خرید از طریقِ خودِ محصولِ باندل انجام می‌شود.",
                            fontSize = FontSize.SMALL, color = colors.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))
                        AddBundleForm(onSubmit = viewModel::createBundle)
                    }
                    items(state.bundles) { bundle ->
                        BundleRow(
                            bundle = bundle,
                            onDelete = { viewModel.deleteBundle(bundle.id) },
                            onToggleActive = { viewModel.setBundleActive(bundle.id, !bundle.isActive) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddBundleForm(onSubmit: (title: String, slug: String, description: String, productId: String, memberProductIds: String) -> Unit) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var productId by remember { mutableStateOf("") }
    var memberIds by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surfaceVariant).padding(14.dp)
    ) {
        Text("افزودنِ باندلِ جدید", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(10.dp))
        BundleTextField(value = title, onValueChange = { title = it }, label = "عنوانِ باندل")
        Spacer(Modifier.height(8.dp))
        BundleTextField(value = slug, onValueChange = { slug = it }, label = "اسلاگ (لاتین، یکتا)")
        Spacer(Modifier.height(8.dp))
        BundleTextField(value = description, onValueChange = { description = it }, label = "توضیح (اختیاری)")
        Spacer(Modifier.height(8.dp))
        BundleTextField(value = productId, onValueChange = { productId = it }, label = "شناسه‌ی محصولِ باندل")
        Spacer(Modifier.height(8.dp))
        BundleTextField(value = memberIds, onValueChange = { memberIds = it }, label = "شناسه‌ی اعضا (با کاما جدا کن)")
        Spacer(Modifier.height(10.dp))
        val canSubmit = title.isNotBlank() && slug.isNotBlank() && productId.toLongOrNull() != null
        Text(
            "ساختِ باندل",
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (canSubmit) colors.primary else colors.line)
                .clickable(enabled = canSubmit) {
                    onSubmit(title.trim(), slug.trim(), description.trim(), productId.trim(), memberIds.trim())
                    title = ""; slug = ""; description = ""; productId = ""; memberIds = ""
                }
                .padding(horizontal = 16.dp, vertical = 11.dp),
            color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
        )
    }
}

@Composable
private fun BundleRow(bundle: AdminBundle, onDelete: () -> Unit, onToggleActive: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(bundle.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            Spacer(Modifier.height(3.dp))
            Text(
                "محصول #${bundle.productId} · ${bundle.memberProductIds.size} عضو" + if (!bundle.isActive) " · غیرفعال" else "",
                color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL
            )
        }
        Text(
            if (bundle.isActive) "غیرفعال کردن" else "فعال کردن",
            color = colors.primary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(colors.accentSoft)
                .clickable { onToggleActive() }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
        Spacer(Modifier.size(8.dp))
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(colors.sale.copy(alpha = 0.1f)).clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale)
        }
    }
}

@Composable
private fun BundleTextField(value: String, onValueChange: (String) -> Unit, label: String) {
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
