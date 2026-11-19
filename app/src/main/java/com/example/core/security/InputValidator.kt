package com.example.core.security

import java.net.URI

object InputValidator {
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return true // optional field unless required by form
        return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(trimmed)
    }

    fun isValidWebUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false
        return try {
            val uri = URI(trimmed)
            val scheme = uri.scheme?.lowercase()
            (scheme == "https" || scheme == "http") && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    fun isGoogleDriveUrl(url: String): Boolean {
        if (!isValidWebUrl(url)) return false
        return try {
            val host = URI(url.trim()).host?.lowercase().orEmpty()
            host.contains("drive.google.com") || host.contains("docs.google.com")
        } catch (_: Exception) {
            false
        }
    }

    fun isPositiveAmount(raw: String): Boolean {
        val value = raw.toDoubleOrNull() ?: return false
        return value > 0.0 && value.isFinite()
    }

    fun isNonNegativeAmount(raw: String): Boolean {
        val value = raw.toDoubleOrNull() ?: return false
        return value >= 0.0 && value.isFinite()
    }
}
