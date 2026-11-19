package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.localization.AppLanguage
import com.example.domain.model.CurrencyCode
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "baslune_settings")

data class BusinessSettings(
    val language: AppLanguage = AppLanguage.KURDISH_SORANI,
    val darkTheme: Boolean = false,
    val defaultCurrency: CurrencyCode = CurrencyCode.IQD,
    val usdToIqdRate: Double = 1500.0,
    val activeRole: UserRole = UserRole.OWNER,
    val activeUserName: String = "BASLUNE Owner",
    val businessName: String = "BASLUNE Studio",
    val tagline: String = "Your Vision. Our Digital Craft.",
    val location: String = "Erbil, Kurdistan Region, Iraq",
    val contactEmail: String = "baslunestudio@gmail.com",
    val instagram: String = "baslunestudio",
    val tiktok: String = "@baslunestudio",
    val streetAddress: String = "",
    val phone: String = "",
    val taxId: String = "",
    val bankDetails: String = "",
    val lastBackupTimestamp: Long = 0L,
    val starterSeeded: Boolean = false,
    val sampleDataActive: Boolean = false
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val USD_TO_IQD_RATE = doublePreferencesKey("usd_to_iqd_rate")
        val ACTIVE_ROLE = stringPreferencesKey("active_role")
        val ACTIVE_USER_NAME = stringPreferencesKey("active_user_name")
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val TAGLINE = stringPreferencesKey("tagline")
        val LOCATION = stringPreferencesKey("location")
        val CONTACT_EMAIL = stringPreferencesKey("contact_email")
        val INSTAGRAM = stringPreferencesKey("instagram")
        val TIKTOK = stringPreferencesKey("tiktok")
        val STREET_ADDRESS = stringPreferencesKey("street_address")
        val PHONE = stringPreferencesKey("phone")
        val TAX_ID = stringPreferencesKey("tax_id")
        val BANK_DETAILS = stringPreferencesKey("bank_details")
        val LAST_BACKUP = longPreferencesKey("last_backup_timestamp")
        val STARTER_SEEDED = booleanPreferencesKey("starter_seeded")
        val SAMPLE_ACTIVE = booleanPreferencesKey("sample_data_active")
    }

    val settingsFlow: Flow<BusinessSettings> = context.dataStore.data.map { prefs ->
        BusinessSettings(
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
            darkTheme = prefs[Keys.DARK_THEME] ?: false,
            defaultCurrency = prefs[Keys.DEFAULT_CURRENCY]?.let {
                runCatching { CurrencyCode.valueOf(it) }.getOrNull()
            } ?: CurrencyCode.IQD,
            usdToIqdRate = prefs[Keys.USD_TO_IQD_RATE] ?: 1500.0,
            activeRole = prefs[Keys.ACTIVE_ROLE]?.let {
                runCatching { UserRole.valueOf(it) }.getOrNull()
            } ?: UserRole.OWNER,
            activeUserName = prefs[Keys.ACTIVE_USER_NAME] ?: "BASLUNE Owner",
            businessName = prefs[Keys.BUSINESS_NAME] ?: "BASLUNE Studio",
            tagline = prefs[Keys.TAGLINE] ?: "Your Vision. Our Digital Craft.",
            location = prefs[Keys.LOCATION] ?: "Erbil, Kurdistan Region, Iraq",
            contactEmail = prefs[Keys.CONTACT_EMAIL] ?: "baslunestudio@gmail.com",
            instagram = prefs[Keys.INSTAGRAM] ?: "baslunestudio",
            tiktok = prefs[Keys.TIKTOK] ?: "@baslunestudio",
            streetAddress = prefs[Keys.STREET_ADDRESS] ?: "",
            phone = prefs[Keys.PHONE] ?: "",
            taxId = prefs[Keys.TAX_ID] ?: "",
            bankDetails = prefs[Keys.BANK_DETAILS] ?: "",
            lastBackupTimestamp = prefs[Keys.LAST_BACKUP] ?: 0L,
            starterSeeded = prefs[Keys.STARTER_SEEDED] ?: false,
            sampleDataActive = prefs[Keys.SAMPLE_ACTIVE] ?: false
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setExchangeRate(usdToIqd: Double) {
        if (usdToIqd > 0.0) {
            context.dataStore.edit { it[Keys.USD_TO_IQD_RATE] = usdToIqd }
        }
    }

    suspend fun setDefaultCurrency(currency: CurrencyCode) {
        context.dataStore.edit { it[Keys.DEFAULT_CURRENCY] = currency.name }
    }

    suspend fun setActiveRole(role: UserRole, userName: String? = null) {
        context.dataStore.edit {
            it[Keys.ACTIVE_ROLE] = role.name
            if (!userName.isNullOrBlank()) {
                it[Keys.ACTIVE_USER_NAME] = userName
            }
        }
    }

    suspend fun updateBusinessProfile(
        businessName: String,
        tagline: String,
        location: String,
        email: String,
        instagram: String,
        tiktok: String,
        streetAddress: String,
        phone: String,
        taxId: String,
        bankDetails: String
    ) {
        context.dataStore.edit {
            it[Keys.BUSINESS_NAME] = businessName.trim()
            it[Keys.TAGLINE] = tagline.trim()
            it[Keys.LOCATION] = location.trim()
            it[Keys.CONTACT_EMAIL] = email.trim()
            it[Keys.INSTAGRAM] = instagram.trim()
            it[Keys.TIKTOK] = tiktok.trim()
            it[Keys.STREET_ADDRESS] = streetAddress.trim()
            it[Keys.PHONE] = phone.trim()
            it[Keys.TAX_ID] = taxId.trim()
            it[Keys.BANK_DETAILS] = bankDetails.trim()
        }
    }

    suspend fun setStarterSeeded(seeded: Boolean) {
        context.dataStore.edit { it[Keys.STARTER_SEEDED] = seeded }
    }

    suspend fun setSampleDataActive(active: Boolean) {
        context.dataStore.edit { it[Keys.SAMPLE_ACTIVE] = active }
    }

    suspend fun setLastBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_BACKUP] = timestamp }
    }
}
