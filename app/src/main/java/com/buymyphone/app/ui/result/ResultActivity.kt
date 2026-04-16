package com.buymyphone.app.ui.result

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.buymyphone.app.data.models.AnalysisResult
import com.buymyphone.app.data.models.PerformanceClass
import com.buymyphone.app.databinding.ActivityResultBinding
import com.buymyphone.app.ui.home.HomeActivity
import com.buymyphone.app.utils.Constants
import com.buymyphone.app.utils.animateFadeIn
import com.buymyphone.app.utils.showToast
import com.google.gson.Gson
import java.io.File

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private val viewModel: ResultViewModel by viewModels()
    private val gson = Gson()
    private lateinit var result: AnalysisResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = intent.getStringExtra(Constants.EXTRA_ANALYSIS_RESULT) ?: run { finish(); return }
        result = gson.fromJson(json, AnalysisResult::class.java)

        setupUI()
        setupObservers()
        setupClickListeners()
        animateEntrance()
    }

    private fun setupUI() {
        val info = result.deviceInfo

        // Score circle
        binding.scoreCircle.setScore(
            score     = result.overallScore,
            label     = "Overall Score",
            perfLabel = result.performanceClass.label
        )

        // Device name
        binding.tvDeviceName.text   = info.deviceName.ifBlank { "Unknown Device" }
        binding.tvAndroidInfo.text  = "Android ${info.androidVersion}  •  ${info.brand}"

        // Performance badge
        binding.chipPerformance.text = result.performanceClass.label
        val badgeColor = Color.parseColor(result.performanceClass.colorHex)
        binding.chipPerformance.setChipBackgroundColorResource(android.R.color.transparent)
        binding.chipPerformance.setTextColor(badgeColor)

        // Best usage
        binding.tvBestUsage.text = "${result.bestUsage.emoji}  Best for ${result.bestUsage.label}"

        // Score breakdown
        binding.tvPerfScore.text    = "${result.performanceScore}"
        binding.tvDisplayScore.text = "${result.displayScore}"
        binding.tvCameraScore.text  = "${result.cameraScore}"
        binding.tvBatteryScore.text = "${result.batteryScore}"

        setScoreColor(binding.tvPerfScore,    result.performanceScore)
        setScoreColor(binding.tvDisplayScore, result.displayScore)
        setScoreColor(binding.tvCameraScore,  result.cameraScore)
        setScoreColor(binding.tvBatteryScore, result.batteryScore)

        // Highlights & weaknesses
        val highlights = result.highlights.joinToString("\n") { "• $it" }
        val weaknesses = result.weaknesses.joinToString("\n") { "• $it" }
        binding.tvHighlights.text = highlights.ifBlank { "• N/A" }
        binding.tvWeaknesses.text = weaknesses.ifBlank  { "• N/A" }

        // Recommendations
        binding.tvRecommendations.text = result.recommendations.joinToString("\n") { "→ $it" }

        // Device specs summary
        binding.tvSpecSoc.text      = info.socName.ifBlank { "Unknown" }
        binding.tvSpecRam.text      = "${info.totalRamMB / 1024} GB"
        binding.tvSpecStorage.text  = "${info.totalStorageGB.toInt()} GB"
        binding.tvSpecDisplay.text  = "${info.screenWidthPx}×${info.screenHeightPx} @ ${info.screenRefreshRate}Hz"
        binding.tvSpecCamera.text   = if (info.rearCamerasMegapixels.isNotEmpty())
            "${info.rearCamerasMegapixels.maxOrNull()?.toInt()}MP" else "N/A"
        binding.tvSpecBattery.text  = if (info.batteryCapacityMah > 0) "${info.batteryCapacityMah}mAh" else "N/A"
    }

    private fun setScoreColor(v: android.widget.TextView, score: Int) {
        val color = when {
            score >= 85 -> Color.parseColor("#4CAF50")
            score >= 70 -> Color.parseColor("#2196F3")
            score >= 50 -> Color.parseColor("#FF9800")
            else        -> Color.parseColor("#F44336")
        }
        v.setTextColor(color)
    }

    private fun setupObservers() {
        viewModel.saveStatus.observe(this) { status ->
            when (status) {
                is SaveStatus.Success -> showToast("Report saved successfully")
                is SaveStatus.Error   -> showToast("Save failed: ${status.message}")
                null -> {}
            }
        }

        viewModel.pdfPath.observe(this) { path ->
            if (!path.isNullOrBlank()) {
                showToast("PDF saved: $path")
                openPdf(path)
            } else {
                showToast("PDF export failed")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveReport.setOnClickListener {
            viewModel.saveReport(result)
        }

        binding.btnExportPdf.setOnClickListener {
            binding.btnExportPdf.isEnabled = false
            viewModel.exportPdf(result)
        }

        binding.btnBackHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
    }

    private fun animateEntrance() {
        binding.cardScore.animateFadeIn(400)
        binding.cardScoreBreakdown.animateFadeIn(600)
        binding.cardHighlights.animateFadeIn(800)
    }

    private fun openPdf(path: String) {
        try {
            val file = File(path)
            val uri  = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: Exception) {
            showToast("No PDF viewer installed")
        }
        binding.btnExportPdf.isEnabled = true
    }
}
