package com.buymyphone.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.buymyphone.app.R
import com.buymyphone.app.databinding.ActivityHomeBinding
import com.buymyphone.app.ui.about.AboutActivity
import com.buymyphone.app.ui.analysis.AnalysisActivity
import com.buymyphone.app.ui.deep.DeepAnalysisActivity
import com.buymyphone.app.ui.premium.PremiumActivity
import com.buymyphone.app.ui.settings.SettingsActivity
import com.buymyphone.app.utils.animateFadeIn
import com.buymyphone.app.utils.animateScale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupObservers()
        setupClickListeners()
        animateEntrance()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun setupObservers() {
        viewModel.reportCount.observe(this) { count ->
            binding.tvReportBadge.text = "$count Reports Saved"
        }
    }

    private fun setupClickListeners() {
        binding.btnStartAnalysis.setOnClickListener {
            hapticFeedback()
            it.animateScale(0.9f, 1f, 150)
            startActivity(Intent(this, AnalysisActivity::class.java))
        }

        binding.btnDeepAnalysis.setOnClickListener {
            hapticFeedback()
            it.animateScale(0.9f, 1f, 150)
            startActivity(Intent(this, DeepAnalysisActivity::class.java))
        }
    }

    private fun animateEntrance() {
        binding.cardMain.animateFadeIn(600)
        binding.cardDeep.animateFadeIn(800)
        binding.tvTagline.animateFadeIn(400)
    }

    private fun hapticFeedback() {
        try {
            @Suppress("DEPRECATION")
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about    -> { startActivity(Intent(this, AboutActivity::class.java));   true }
            R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
            R.id.action_premium  -> { startActivity(Intent(this, PremiumActivity::class.java));  true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
