package com.buymyphone.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.buymyphone.app.data.database.AppDatabase
import com.buymyphone.app.data.repository.ReportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BuyMyPhoneApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepository(database.reportDao(), applicationScope)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        applyStoredTheme()
    }

    private fun applyStoredTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isDark = prefs.getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    companion object {
        const val PREFS_NAME = "buymyphone_prefs"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_PREMIUM = "is_premium"

        lateinit var instance: BuyMyPhoneApplication
            private set
    }
}
