package com.example

import android.app.Application
import com.example.data.local.BasluneDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.repository.BasluneRepository
import com.example.data.repository.LocalBasluneRepository
import com.example.data.seed.StarterDataSeeder

class BasluneApp : Application() {
    val database: BasluneDatabase by lazy { BasluneDatabase.getInstance(this) }
    val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(this) }
    val repository: BasluneRepository by lazy {
        LocalBasluneRepository(
            db = database,
            dao = database.basluneDao(),
            settingsStore = settingsDataStore,
            seeder = StarterDataSeeder()
        )
    }
}
