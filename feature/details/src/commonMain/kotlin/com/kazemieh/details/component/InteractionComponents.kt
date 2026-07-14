package com.kazemieh.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.util.formatShortDateFa
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.catalog.Question
import com.kazemieh.domain.catalog.Review
import org.jetbrains.compose.resources.painterResource

/**
 * کارتِ یک دیدگاه — مطابق اسپک کارمیلا:
 * آواتار + نام + نشان (خرید تأییدشده/پشتیبانی) + امتیاز + تاریخ، متنِ نظر، و «پاسخ».
 * پاسخ‌دادن و ویرایش همان‌جا (اینلاین) انجام می‌شود؛ دیالوگی باز نمی‌شود.
 */
@Composable
fun ReviewItem(
    review: Review,
    currentUserId: Long?,
    onReplySubmit: (parentId: Long, comment: String) -> Unit,
    onEditSubmit: (reviewId: Long, rating: Int, comment: String) -> Unit,
    onDelete: (Long) -> Unit,
    depth: Int = 0
) {
    val colors = AppTheme.colors
    var replyOpen by remember { mutableStateOf(false) }
    var editOpen by remember { mutableStateOf(false) }

    val isReply = depth > 0
    val isOwn = currentUserId != null && currentUserId == review.userId
    val indent = (minOf(depth, 3) * 14).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent, top = 8.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(if (isReply) colors.surfaceVariant else colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(14.dp)
    ) {
        InteractionHeader(
            name = review.userName,
            isSupport = review.isSupport,
            verifiedPurchase = review.verifiedPurchase,
            rating = review.rating,
            createdAt = review.createdAt
        )

        Spacer(Modifier.height(9.dp))
        Text(
            text = review.comment,
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            color = colors.onSurface,
            lineHeight = FontSize.EXTRA_MEDIUM
        )

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ActionLink(label = "پاسخ", withReplyIcon = true, tint = colors.primary) {
                replyOpen = !replyOpen; editOpen = false
            }
            if (isOwn) {
                ActionLink(label = "ویرایش", tint = colors.onSurfaceVariant) {
                    editOpen = !editOpen; replyOpen = false
                }
                ActionLink(label = "حذف", tint = colors.sale) { onDelete(review.id) }
            }
        }

        if (replyOpen) {
            InlineComposer(
                initialText = "",
                placeholder = "پاسخ خود را بنویسید…",
                submitLabel = "ثبت پاسخ",
                showRating = false,
                onCancel = { replyOpen = false },
                onSubmit = { _, txt -> onReplySubmit(review.id, txt); replyOpen = false }
            )
        }
        if (editOpen) {
            InlineComposer(
                initialText = review.comment,
                placeholder = "متن نظر",
                submitLabel = "ذخیره تغییرات",
                showRating = depth == 0,
                initialRating = review.rating ?: 5,
                onCancel = { editOpen = false },
                onSubmit = { rating, txt -> onEditSubmit(review.id, rating, txt); editOpen = false }
            )
        }

        review.replies.forEach { reply ->
            ReviewItem(
                review = reply,
                currentUserId = currentUserId,
                onReplySubmit = onReplySubmit,
                onEditSubmit = onEditSubmit,
                onDelete = onDelete,
                depth = depth + 1
            )
        }
    }
}

/**
 * کارتِ یک پرسش (یا پاسخ) — مطابق اسپک؛ پاسخ‌دادن و ویرایش اینلاین انجام می‌شود.
 */
@Composable
fun QuestionItem(
    question: Question,
    currentUserId: Long?,
    onReplySubmit: (parentId: Long, content: String) -> Unit,
    onEditSubmit: (questionId: Long, content: String) -> Unit,
    onDelete: (Long) -> Unit,
    depth: Int = 0
) {
    val colors = AppTheme.colors
    var replyOpen by remember { mutableStateOf(false) }
    var editOpen by remember { mutableStateOf(false) }

    val isReply = depth > 0
    val isOwn = currentUserId != null && currentUserId == question.userId
    val indent = (minOf(depth, 3) * 14).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent, top = 8.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(if (isReply) colors.surfaceVariant else colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(14.dp)
    ) {
        InteractionHeader(
            name = question.userName,
            isSupport = question.isSupport,
            verifiedPurchase = false,
            rating = null,
            createdAt = question.createdAt,
            // پرسش با «س» و پاسخ با «ج» مشخص می‌شود (مطابق اسپک)
            avatarOverride = if (isReply) "ج" else "س"
        )

        Spacer(Modifier.height(9.dp))
        Text(
            text = question.content,
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            color = colors.onSurface,
            lineHeight = FontSize.EXTRA_MEDIUM
        )

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ActionLink(label = "پاسخ", withReplyIcon = true, tint = colors.primary) {
                replyOpen = !replyOpen; editOpen = false
            }
            if (isOwn) {
                ActionLink(label = "ویرایش", tint = colors.onSurfaceVariant) {
                    editOpen = !editOpen; replyOpen = false
                }
                ActionLink(label = "حذف", tint = colors.sale) { onDelete(question.id) }
            }
        }

        if (replyOpen) {
            InlineComposer(
                initialText = "",
                placeholder = "پاسخ خود را بنویسید…",
                submitLabel = "ثبت پاسخ",
                showRating = false,
                onCancel = { replyOpen = false },
                onSubmit = { _, txt -> onReplySubmit(question.id, txt); replyOpen = false }
            )
        }
        if (editOpen) {
            InlineComposer(
                initialText = question.content,
                placeholder = "متن پرسش",
                submitLabel = "ذخیره تغییرات",
                showRating = false,
                onCancel = { editOpen = false },
                onSubmit = { _, txt -> onEditSubmit(question.id, txt); editOpen = false }
            )
        }

        question.replies.forEach { reply ->
            QuestionItem(
                question = reply,
                currentUserId = currentUserId,
                onReplySubmit = onReplySubmit,
                onEditSubmit = onEditSubmit,
                onDelete = onDelete,
                depth = depth + 1
            )
        }
    }
}

/** سرآیندِ مشترکِ نظر/پرسش: آواتار + نام + نشان + امتیاز، و تاریخ در سمتِ مقابل. */
@Composable
private fun InteractionHeader(
    name: String,
    isSupport: Boolean,
    verifiedPurchase: Boolean,
    rating: Int?,
    createdAt: String,
    avatarOverride: String? = null
) {
    val colors = AppTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        InteractionAvatar(name = name, isSupport = isSupport, override = avatarOverride)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name.ifBlank { "کاربر کارمیلا" },
                    fontFamily = AppFont(),
                    fontSize = FontSize.SMALL,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(6.dp))
                when {
                    isSupport -> Badge(text = "پشتیبانی", color = colors.ok)
                    verifiedPurchase -> Badge(text = "خرید تأییدشده", color = colors.ok)
                }
            }
            if (rating != null) {
                Spacer(Modifier.height(4.dp))
                StarRow(rating = rating)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatShortDateFa(createdAt),
            fontFamily = AppFont(),
            fontSize = FontSize.EXTRA_SMALL,
            color = colors.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/** آواتارِ دایره‌ای با حرفِ اولِ نام (یا حرفِ سفارشی برای پرسش/پاسخ). */
@Composable
private fun InteractionAvatar(name: String, isSupport: Boolean, override: String?) {
    val colors = AppTheme.colors
    val letter = override ?: name.trim().firstOrNull()?.toString() ?: "؟"
    val bg = if (isSupport) colors.ok.copy(alpha = 0.18f) else colors.accentSoft
    val fg = if (isSupport) colors.ok else colors.primary
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

/** نشانِ کوچکِ رنگی (خرید تأییدشده / پشتیبانی). */
@Composable
private fun Badge(text: String, color: Color) {
    Text(
        text = text,
        fontFamily = AppFont(),
        fontSize = FontSize.EXTRA_SMALL,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** ردیفِ ستاره‌های امتیاز. */
@Composable
private fun StarRow(rating: Int) {
    val colors = AppTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { i ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (i < rating) colors.star else colors.line,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

/** لینکِ اکشن (پاسخ/ویرایش/حذف) با آیکنِ اختیاریِ پاسخ. */
@Composable
private fun ActionLink(
    label: String,
    tint: Color,
    withReplyIcon: Boolean = false,
    onClick: () -> Unit
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.sm))
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (withReplyIcon) {
            Icon(
                painter = painterResource(Resources.Icon.BackArrow),
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer { rotationY = if (isRtl) 180f else 0f }
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = label,
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.SemiBold,
            color = tint
        )
    }
}

/** ویرایشگرِ اینلاین برای نوشتنِ پاسخ یا ویرایشِ نظر/پرسش — به‌جای دیالوگ. */
@Composable
private fun InlineComposer(
    initialText: String,
    placeholder: String,
    submitLabel: String,
    showRating: Boolean,
    initialRating: Int = 5,
    onCancel: () -> Unit,
    onSubmit: (rating: Int, text: String) -> Unit
) {
    val colors = AppTheme.colors
    var text by remember { mutableStateOf(initialText) }
    var rating by remember { mutableStateOf(initialRating) }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        if (showRating) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(5) { i ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < rating) colors.star else colors.line,
                        modifier = Modifier.size(24.dp).clickable { rating = i + 1 }
                    )
                }
            }
            Spacer(Modifier.height(9.dp))
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
            minLines = 2,
            shape = RoundedCornerShape(Radius.sm),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.background,
                unfocusedContainerColor = colors.background,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.line,
                cursorColor = colors.primary,
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface
            )
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "انصراف",
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.button))
                    .border(1.dp, colors.line, RoundedCornerShape(Radius.button))
                    .clickable { onCancel() }
                    .padding(vertical = 11.dp),
                textAlign = TextAlign.Center,
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.Bold,
                color = colors.onSurfaceVariant
            )
            Text(
                text = submitLabel,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.button))
                    .background(if (text.isNotBlank()) colors.primary else colors.line)
                    .clickable(enabled = text.isNotBlank()) { onSubmit(rating, text) }
                    .padding(vertical = 11.dp),
                textAlign = TextAlign.Center,
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimary
            )
        }
    }
}

/**
 * دیالوگِ ثبتِ نظرِ جدید (برای زمانی که ریپلای نیست و از دکمه‌ی اصلی باز می‌شود).
 */
@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String, images: List<String>) -> Unit
) {
    val colors = AppTheme.colors
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(Radius.md))
                .background(colors.surface)
                .padding(20.dp)
        ) {
            Text(
                "ثبت نظر جدید",
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(16.dp))
            
            Text("امتیاز شما", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            StarRowClickable(rating = rating, onRatingChange = { rating = it })
            
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("تجربه خود را بنویسید…", fontSize = FontSize.SMALL) },
                minLines = 3,
                shape = RoundedCornerShape(Radius.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line
                )
            )
            
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "انصراف",
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.button))
                        .border(1.dp, colors.line, RoundedCornerShape(Radius.button))
                        .clickable { onDismiss() }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    fontSize = FontSize.SMALL,
                    color = colors.onSurfaceVariant
                )
                Text(
                    "ثبت نظر",
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.button))
                        .background(if (comment.isNotBlank()) colors.primary else colors.line)
                        .clickable(enabled = comment.isNotBlank()) { onSubmit(rating, comment, emptyList()); onDismiss() }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    fontSize = FontSize.SMALL,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimary
                )
            }
        }
    }
}

@Composable
private fun StarRowClickable(rating: Int, onRatingChange: (Int) -> Unit) {
    val colors = AppTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(5) { i ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (i < rating) colors.star else colors.line,
                modifier = Modifier.size(28.dp).clickable { onRatingChange(i + 1) }
            )
        }
    }
}
