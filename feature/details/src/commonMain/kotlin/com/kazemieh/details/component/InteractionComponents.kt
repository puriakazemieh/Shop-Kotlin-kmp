package com.kazemieh.details.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.Radius
import com.kazemieh.domain.catalog.Question
import com.kazemieh.domain.catalog.Review

@Composable
fun ReviewItem(
    review: Review,
    onReplyClick: (Long) -> Unit,
    onEditClick: (Review) -> Unit,
    onDeleteClick: (Long) -> Unit,
    depth: Int = 0
) {
    val paddingStart = if (depth < 3) (depth * 16).dp else (3 * 16).dp
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingStart, top = 8.dp, bottom = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.xs))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = review.userName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            review.rating?.let {
                RatingDisplay(rating = it)
            }
        }

        Text(
            text = review.comment,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )

        Row(modifier = Modifier.padding(top = 8.dp)) {
            TextButton(
                onClick = { onReplyClick(review.id) },
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                Text("پاسخ", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(
                onClick = { onEditClick(review) },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("ویرایش", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(
                onClick = { onDeleteClick(review.id) },
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.sale)
            ) {
                Text("حذف", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Recursive replies
        review.replies.forEach { reply ->
            ReviewItem(
                review = reply,
                onReplyClick = onReplyClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                depth = depth + 1
            )
        }
    }
}

@Composable
fun QuestionItem(
    question: Question,
    onReplyClick: (Long) -> Unit,
    onEditClick: (Question) -> Unit,
    onDeleteClick: (Long) -> Unit,
    depth: Int = 0
) {
    val paddingStart = if (depth < 3) (depth * 16).dp else (3 * 16).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingStart, top = 8.dp, bottom = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.xs))
            .padding(12.dp)
    ) {
        Text(
            text = question.userName,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = question.content,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )

        Row(modifier = Modifier.padding(top = 8.dp)) {
            TextButton(
                onClick = { onReplyClick(question.id) },
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                Text("پاسخ", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(
                onClick = { onEditClick(question) },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("ویرایش", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(
                onClick = { onDeleteClick(question.id) },
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.sale)
            ) {
                Text("حذف", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Recursive replies
        question.replies.forEach { reply ->
            QuestionItem(
                question = reply,
                onReplyClick = onReplyClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                depth = depth + 1
            )
        }
    }
}

@Composable
fun RatingDisplay(rating: Int) {
    Row {
        repeat(5) { index ->
            Text(
                text = if (index < rating) "★" else "☆",
                color = if (index < rating) AppTheme.colors.star else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(Radius.md)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ثبت نظر", style = MaterialTheme.typography.titleMedium)
                
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Text(
                                text = if (index < rating) "★" else "☆",
                                color = if (index < rating) AppTheme.colors.star else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("متن نظر") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Button(
                        onClick = { onSubmit(rating, comment) },
                        enabled = comment.isNotBlank()
                    ) {
                        Text("ثبت")
                    }
                }
            }
        }
    }
}

@Composable
fun AddQuestionDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(Radius.md)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ثبت پرسش", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("متن پرسش") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Button(
                        onClick = { onSubmit(content) },
                        enabled = content.isNotBlank()
                    ) {
                        Text("ثبت")
                    }
                }
            }
        }
    }
}

@Composable
fun EditReviewDialog(
    review: Review,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(review.rating ?: 5) }
    var comment by remember { mutableStateOf(review.comment) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(Radius.md)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ویرایش نظر", style = MaterialTheme.typography.titleMedium)
                
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Text(
                                text = if (index < rating) "★" else "☆",
                                color = if (index < rating) AppTheme.colors.star else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("متن نظر") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Button(
                        onClick = { onSubmit(rating, comment) },
                        enabled = comment.isNotBlank()
                    ) {
                        Text("ویرایش")
                    }
                }
            }
        }
    }
}

@Composable
fun EditQuestionDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var content by remember { mutableStateOf(question.content) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(Radius.md)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ویرایش پرسش", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("متن پرسش") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Button(
                        onClick = { onSubmit(content) },
                        enabled = content.isNotBlank()
                    ) {
                        Text("ویرایش")
                    }
                }
            }
        }
    }
}
