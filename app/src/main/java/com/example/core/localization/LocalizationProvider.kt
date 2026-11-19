package com.example.core.localization

import androidx.compose.runtime.staticCompositionLocalOf

val LocalStrings = staticCompositionLocalOf<AppStrings> { KurdishStrings }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.KURDISH_SORANI }

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.KURDISH_SORANI -> KurdishStrings
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.ARABIC -> ArabicStrings
}
