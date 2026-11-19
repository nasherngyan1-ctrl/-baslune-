package com.example.core.localization

import com.example.domain.model.CurrencyCode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val enSymbols = DecimalFormatSymbols(Locale.US)
    private val iqdFormat = DecimalFormat("#,##0", enSymbols)
    private val usdFormat = DecimalFormat("#,##0.00", enSymbols)
    private val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")

    private fun newDateFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }

    private fun newDateTimeFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply { isLenient = false }

    fun formatMoney(
        amount: Double,
        currency: CurrencyCode,
        language: AppLanguage = AppLanguage.KURDISH_SORANI
    ): String {
        return when (currency) {
            CurrencyCode.IQD -> {
                val formatted = synchronized(iqdFormat) { iqdFormat.format(amount) }
                when (language) {
                    AppLanguage.KURDISH_SORANI -> "$formatted د.ع"
                    AppLanguage.ARABIC -> "$formatted د.ع"
                    AppLanguage.ENGLISH -> "$formatted IQD"
                }
            }
            CurrencyCode.USD -> {
                val formatted = synchronized(usdFormat) { usdFormat.format(amount) }
                "$$formatted"
            }
        }
    }

    fun formatDate(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "—"
        return newDateFormat().format(Date(timestampMillis))
    }

    fun formatDateTime(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "—"
        return newDateTimeFormat().format(Date(timestampMillis))
    }

    fun formatPercent(value: Double): String {
        return "${value.toInt().coerceIn(0, 100)}%"
    }

    fun parseDateOrNull(dateStr: String): Long? {
        val trimmed = dateStr.trim()
        if (!dateRegex.matches(trimmed)) return null
        return try {
            newDateFormat().parse(trimmed)?.time
        } catch (_: Exception) {
            null
        }
    }
}
