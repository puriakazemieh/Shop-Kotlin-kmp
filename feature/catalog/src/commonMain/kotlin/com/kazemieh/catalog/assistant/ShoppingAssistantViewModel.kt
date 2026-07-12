package com.kazemieh.catalog.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.catalog.ProductSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantMessage(
    val fromUser: Boolean,
    val text: String,
    val products: List<ProductSummary> = emptyList()
)

data class AssistantState(
    val messages: List<AssistantMessage> = listOf(
        AssistantMessage(
            fromUser = false,
            text = "سلام! می‌تونم درباره‌ی ارسال، مرجوعی، پرداخت راهنماییت کنم یا دنبالِ محصولی برات بگردم. چی می‌خوای بدونی؟"
        )
    ),
    val isSearching: Boolean = false
)

/**
 * دستیارِ خریدِ rule-based: ابتدا با چند الگویِ کلیدواژه‌ایِ ثابت (ارسال/مرجوعی/پرداخت) پاسخ می‌دهد؛
 * اگر هیچ‌کدام نخورد، همان عبارت را در کاتالوگ جست‌وجو می‌کند. اتصال به یک LLMِ خارجی عمداً انجام
 * نشده چون نیازمندِ زیرساخت/کلیدِ API جدید بود که در این کدبیس وجود ندارد.
 */
class ShoppingAssistantViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AssistantState())
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    private val faqRules: List<Pair<List<String>, String>> = listOf(
        listOf("ارسال", "تحویل", "پست") to "ارسالِ سفارش‌ها معمولاً ۲ تا ۴ روزِ کاری طول می‌کشد. کدِ رهگیریِ پستی بعدِ ارسال در صفحه‌ی سفارش نمایش داده می‌شود.",
        listOf("مرجوع", "پس دادن", "تعویض") to "برایِ کالاهایِ تحویل‌گرفته‌شده، از صفحه‌ی «سفارش‌های من» روی آیتمِ موردنظر «درخواستِ مرجوعی/تعویض» را بزن.",
        listOf("پرداخت", "کیف پول", "قسط") to "پرداخت از طریقِ درگاهِ زرین‌پال یا کیفِ‌پول ممکن است. فعلاً پرداختِ قسطی پشتیبانی نمی‌شود.",
        listOf("عضویت", "تخفیف ویژه", "پرایم") to "با «عضویتِ ویژه» (در حسابِ کاربری) روی همه‌ی خریدهایت تخفیفِ خودکار می‌گیری."
    )

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        _state.update { it.copy(messages = it.messages + AssistantMessage(fromUser = true, text = trimmed)) }

        val faqAnswer = faqRules.firstOrNull { (keywords, _) -> keywords.any { trimmed.contains(it) } }?.second
        if (faqAnswer != null) {
            _state.update { it.copy(messages = it.messages + AssistantMessage(fromUser = false, text = faqAnswer)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            when (val result = getProductsUseCase(query = trimmed, size = 6)) {
                is AppResult.Success -> {
                    val products = result.data.items
                    val reply = if (products.isEmpty()) {
                        "چیزی با «$trimmed» پیدا نکردم. می‌تونی با کلمه‌ی دیگه‌ای امتحان کنی."
                    } else {
                        "این‌ها رو برایِ «$trimmed» پیدا کردم:"
                    }
                    _state.update {
                        it.copy(
                            isSearching = false,
                            messages = it.messages + AssistantMessage(fromUser = false, text = reply, products = products)
                        )
                    }
                }
                is AppResult.Error -> _state.update {
                    it.copy(
                        isSearching = false,
                        messages = it.messages + AssistantMessage(fromUser = false, text = "مشکلی در جست‌وجو پیش اومد.")
                    )
                }
                else -> {}
            }
        }
    }
}
