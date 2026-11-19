package com.example.core.localization

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    KURDISH_SORANI("ckb", "کوردی (سۆرانی)", true),
    ENGLISH("en", "English", false),
    ARABIC("ar", "العربية", true);

    val layoutDirection: LayoutDirection
        get() = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: KURDISH_SORANI
    }
}
