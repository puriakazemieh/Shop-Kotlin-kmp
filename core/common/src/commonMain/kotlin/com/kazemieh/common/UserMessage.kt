package com.kazemieh.common

fun String.toUserMessage(): String {
    return when (this) {
        // Auth & User
        "USER_NOT_FOUND" -> "کاربر یافت نشد"
        "EMAIL_ALREADY_EXISTS" -> "این ایمیل قبلاً ثبت شده است"
        "USER_INACTIVE" -> "حساب کاربری غیرفعال است"
        "INVALID_CREDENTIALS" -> "ایمیل یا رمز عبور اشتباه است"
        "ACCESS_DENIED" -> "دسترسی غیرمجاز"
        "INVALID_CURRENT_PASSWORD" -> "رمز عبور فعلی اشتباه است"
        "SAME_AS_OLD_PASSWORD" -> "رمز عبور جدید باید متفاوت باشد"

        // Category
        "CATEGORY_NOT_FOUND" -> "دسته‌بندی یافت نشد"
        "CATEGORY_SLUG_EXISTS" -> "این نام دسته‌بندی تکراری است"
        "CATEGORY_CYCLE" -> "ساختار دسته‌بندی نامعتبر است"

        // Product
        "PRODUCT_NOT_FOUND" -> "محصول یافت نشد"
        "PRODUCT_SLUG_EXISTS" -> "این نام محصول تکراری است"
        "PRODUCT_INACTIVE" -> "محصول غیرفعال است"
        "INVALID_PRODUCT_PRICE" -> "قیمت محصول نامعتبر است"

        // Size & Color
        "SIZE_NOT_FOUND" -> "سایز یافت نشد"
        "SIZE_EXISTS" -> "این سایز قبلاً ثبت شده"
        "COLOR_NOT_FOUND" -> "رنگ یافت نشد"
        "COLOR_EXISTS" -> "این رنگ قبلاً ثبت شده"
        "INVALID_COLOR_HEX" -> "کد رنگ نامعتبر است"

        // Variant
        "VARIANT_NOT_FOUND" -> "تنوع محصول یافت نشد"
        "SKU_EXISTS" -> "کد محصول تکراری است"
        "VARIANT_COMBO_EXISTS" -> "این ترکیب رنگ و سایز قبلاً ثبت شده"
        "VARIANT_INACTIVE" -> "این تنوع محصول غیرفعال است"
        "INVALID_VARIANT_PRICE" -> "قیمت نامعتبر است"

        // Inventory
        "INVENTORY_NOT_FOUND" -> "موجودی یافت نشد"
        "INSUFFICIENT_STOCK" -> "موجودی کافی نیست"
        "INVENTORY_CONFLICT" -> "خطا در به‌روزرسانی موجودی"
        "INVALID_INVENTORY" -> "مقدار موجودی نامعتبر است"

        // Address
        "ADDRESS_NOT_FOUND" -> "آدرس یافت نشد"
        "ADDRESS_ACCESS_DENIED" -> "دسترسی به آدرس غیرمجاز است"
        "DEFAULT_ADDRESS_CONFLICT" -> "خطا در تنظیم آدرس پیش‌فرض"
        "INVALID_ADDRESS" -> "آدرس نامعتبر است"

        // Order
        "ORDER_NOT_FOUND" -> "سفارش یافت نشد"
        "ORDER_ACCESS_DENIED" -> "دسترسی به سفارش غیرمجاز است"
        "EMPTY_ORDER" -> "سفارش خالی است"
        "INVALID_ORDER_STATUS" -> "وضعیت سفارش نامعتبر است"
        "ORDER_STATUS_TRANSITION" -> "تغییر وضعیت سفارش امکان‌پذیر نیست"
        "ORDER_FINALIZED" -> "سفارش نهایی شده و قابل تغییر نیست"
        "ORDER_PRICE_MISMATCH" -> "مبلغ سفارش مطابقت ندارد"

        // Order Item
        "ORDER_ITEM_NOT_FOUND" -> "آیتم سفارش یافت نشد"
        "INVALID_QUANTITY" -> "تعداد نامعتبر است"
        "INVALID_ORDER_ITEM_PRICE" -> "قیمت آیتم نامعتبر است"
        "VARIANT_NOT_AVAILABLE" -> "این محصول در حال حاضر موجود نیست"

        // DB & Generic
        "DATA_INTEGRITY_VIOLATION" -> "خطا در ذخیره اطلاعات"
        "FOREIGN_KEY_VIOLATION" -> "ارجاع نامعتبر"
        "UNIQUE_VIOLATION" -> "این مقدار تکراری است"
        "CHECK_VIOLATION" -> "داده نامعتبر است"
        "VALIDATION_ERROR" -> "اطلاعات وارد شده نامعتبر است"
        "INVALID_JSON" -> "فرمت درخواست نامعتبر است"
        "INTERNAL_ERROR" -> "خطای سرور رخ داد"

        "UNKNOWN_ERROR" -> "خطای نامشخص رخ داد"
        else -> this
    }
}
