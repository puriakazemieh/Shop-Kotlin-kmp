package com.kazemieh.common

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    PERSIAN("fa", "فارسی");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: PERSIAN
        }
    }
}
