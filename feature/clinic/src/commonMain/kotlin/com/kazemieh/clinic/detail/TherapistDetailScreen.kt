package com.kazemieh.clinic.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.clinic.AvailabilitySlot
import com.seiko.imageloader.rememberImagePainter
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistDetailScreen(
    slug: String,
    navigateBack: () -> Unit,
    navigateToMyAppointments: () -> Unit,
    navigateToProduct: (String) -> Unit = {},
    navigateToMessaging: (Long) -> Unit = {}
) {
    val viewModel = koinViewModel<TherapistDetailViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(slug) { viewModel.load(slug) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TherapistDetailEffect.Booked -> {
                    messageBarState.addSuccess("نوبتِ شما ثبت شد.")
                    navigateToMyAppointments()
                }
                is TherapistDetailEffect.SwitchRequested -> messageBarState.addSuccess("درخواستِ تعویضِ درمانگر ثبت شد؛ به‌زودی بررسی می‌شود.")
                is TherapistDetailEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("جزئیات درمانگر", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.fillMaxSize().padding(padding),
            messageBarState = messageBarState
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val therapist = state.therapist
                when {
                    state.isLoading && therapist == null ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                    therapist != null -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(72.dp).clip(CircleShape).background(colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!therapist.photoUrl.isNullOrBlank()) {
                                        Image(
                                            painter = rememberImagePainter(therapist.photoUrl!!),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text("🧑‍⚕️", fontSize = FontSize.LARGE)
                                    }
                                }
                                Spacer(Modifier.size(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(therapist.name, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.EXTRA_REGULAR, color = colors.onSurface)
                                    if (!therapist.specialty.isNullOrBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(therapist.specialty!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("هر جلسه ${therapist.sessionDurationMinutes} دقیقه · ${therapist.modeLabel}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                                }
                            }
                        }

                        // ---- جلسه‌ی حضوری: نشانیِ محلِ برگزاری ----
                        if (therapist.isInPerson && !therapist.location.isNullOrBlank()) {
                            item {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text("محلِ برگزاری: ", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL)
                                    Text(therapist.location!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                                }
                            }
                        }

                        if (!therapist.bio.isNullOrBlank()) {
                            item { Text(therapist.bio!!, color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR) }
                        }

                        if (therapist.requiresPurchase) {
                            item {
                                PurchaseGateCard(
                                    sessionCredits = therapist.sessionCredits,
                                    productSlug = therapist.productSlug,
                                    onBuyClick = { therapist.productSlug?.let(navigateToProduct) }
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("انتخابِ زمانِ نوبت", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                        }

                        if (therapist.slots.isEmpty()) {
                            item {
                                Text(
                                    "در حالِ حاضر زمانِ آزادی برای رزرو وجود ندارد.",
                                    color = colors.onSurfaceVariant, fontSize = FontSize.SMALL,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(therapist.slots.size) { idx ->
                                val slot = therapist.slots[idx]
                                SlotRow(
                                    slot = slot,
                                    booking = state.bookingSlotId == slot.id,
                                    enabled = state.bookingSlotId == null && therapist.canBook,
                                    onBook = { viewModel.book(slot.id) }
                                )
                            }
                        }

                        // ---- نظرِ مراجعان (فقط اگر خدمت به محصول لینک شده باشد) ----
                        therapist.productId?.let { pid ->
                            item {
                                Spacer(Modifier.height(16.dp))
                                com.kazemieh.details.component.ProductReviewsSection(productId = pid, title = "نظرِ مراجعان")
                            }
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "چت با درمانگر",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Radius.button))
                                    .background(colors.surfaceVariant)
                                    .clickable { navigateToMessaging(therapist.id) }
                                    .padding(vertical = 12.dp),
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = FontSize.REGULAR,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (state.isRequestingSwitch) "در حالِ ثبتِ درخواست…" else "درخواستِ تعویضِ درمانگر",
                                modifier = Modifier
                                    .clickable(enabled = !state.isRequestingSwitch) { viewModel.requestSwitch(null) }
                                    .padding(vertical = 4.dp),
                                color = colors.onSurfaceVariant,
                                fontSize = FontSize.SMALL,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        }
                    }
                }
            }
        }
    }
}

/** بنرِ وضعیتِ خرید: اگر این درمانگر نیازمندِ بسته‌ی خریداری‌شده باشد و اعتباری نمانده، دکمه‌ی خرید را نشان بده. */
@Composable
private fun PurchaseGateCard(
    sessionCredits: Int,
    productSlug: String?,
    onBuyClick: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.accentSoft)
            .padding(14.dp)
    ) {
        if (sessionCredits > 0) {
            Text(
                "شما $sessionCredits جلسه‌ی خریداری‌شده برای این مشاور دارید.",
                color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = FontSize.SMALL
            )
        } else {
            Text(
                "برای رزروِ نوبت با این مشاور، ابتدا باید بسته‌ی جلسه را از فروشگاه خریداری کنید.",
                color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = FontSize.SMALL
            )
            if (productSlug != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "خریدِ بسته و رزرو",
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.primary)
                        .clickable { onBuyClick() }
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.SMALL
                )
            }
        }
    }
}

@Composable
private fun SlotRow(
    slot: AvailabilitySlot,
    booking: Boolean,
    enabled: Boolean,
    onBook: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(slot.dayLabel, fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            Spacer(Modifier.height(2.dp))
            Text(slot.timeLabel, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            if (slot.isGroupSession) {
                Spacer(Modifier.height(2.dp))
                Text("جلسه‌ی گروهی · ${slot.remainingCapacity} از ${slot.capacity} جا خالی", color = colors.primary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold)
            }
        }
        Text(
            text = if (booking) "در حالِ رزرو…" else "رزرو",
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.button))
                .background(if (enabled) colors.primary else colors.surfaceVariant)
                .clickable(enabled = enabled) { onBook() }
                .padding(horizontal = 18.dp, vertical = 9.dp),
            color = if (enabled) colors.onPrimary else colors.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = FontSize.SMALL
        )
    }
}
