package com.buymyphone.app.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.buymyphone.app.BuyMyPhoneApplication
import com.buymyphone.app.databinding.ActivitySettingsBinding
import com.buymyphone.app.ui.compare.CompareActivity
import com.buymyphone.app.ui.history.HistoryActivity
import com.buymyphone.app.ui.premium.PremiumActivity
import com.buymyphone.app.utils.showToast

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Settings" }

        loadCurrentSettings()
        setupObservers()
        setupClickListeners()
    }

    private fun loadCurrentSettings() {
        val prefs  = getSharedPreferences(BuyMyPhoneApplication.PREFS_NAME, MODE_PRIVATE)
        val isDark = prefs.getBoolean(BuyMyPhoneApplication.KEY_DARK_MODE, false)
        binding.switchDarkMode.isChecked = isDark
        updateThemeLabel(isDark)
    }

    private fun setupObservers() {
        viewModel.reportCount.observe(this) { count ->
            binding.tvReportCount.text = "$count reports stored"
        }
    }

    private fun setupClickListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveTheme(isChecked)
            updateThemeLabel(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else           AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.itemReportsHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.itemCompareTwoPhones.setOnClickListener {
            startActivity(Intent(this, CompareActivity::class.java))
        }

        binding.itemGoPremium.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        binding.itemClearReports.setOnClickListener {
            showClearConfirmation()
        }

        binding.itemHeavyApps.setOnClickListener {
            detectHeavyApps()
        }
    }

    private fun updateThemeLabel(isDark: Boolean) {
        binding.tvThemeLabel.text = if (isDark) "Dark Mode  ✓" else "Light Mode"
    }

    private fun saveTheme(isDark: Boolean) {
        getSharedPreferences(BuyMyPhoneApplication.PREFS_NAME, MODE_PRIVATE)
            .edit().putBoolean(BuyMyPhoneApplication.KEY_DARK_MODE, isDark).apply()
    }

    private fun showClearConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Reports")
            .setMessage("This will permanently delete all saved reports. Continue?")
            .setPositiveButton("Clear All") { _, _ ->
                viewModel.clearAllReports { showToast("All reports cleared") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun detectHeavyApps() {
        val am  = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        val procs = am.runningAppProcesses ?: emptyList()
        val memInfo = am.getMemInfo()
