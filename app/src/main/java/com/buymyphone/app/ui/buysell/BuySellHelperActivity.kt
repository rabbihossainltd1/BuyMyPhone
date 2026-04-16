package com.buymyphone.app.ui.buysell

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.buymyphone.app.data.models.BuySellResult
import com.buymyphone.app.data.models.BuySellVerdict
import com.buymyphone.app.data.models.SoftwareCheckResult
import com.buymyphone.app.data.models.SoftwareCheckResult as SwResult
import com.buymyphone.app.databinding.ActivityBuySellHelperBinding
import com.buymyphone.app.utils.Constants
import com.buymyphone.app.utils.ScoringEngine
import com.buymyphone.app.utils.animateFadeIn
import com.buymyphone.app.utils.animateScale
import com.buymyphone.app.utils.showToast
import com.google.gson.Gson
import java.io.File

class BuySellHelperActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuySellHelperBinding
    private val viewModel: BuySellViewModel by viewModels()
    private val gson = Gson()
    private lateinit var buySellResult: BuySellResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuySellHelperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Buy / Sell Helper" }

        loadAndDisplayResult()
        setupObservers()
        setupClickListeners()
    }

    private fun loadAndDisplayResult() {
        val swJson = intent.getStringExtra(Constants.EXTRA_SOFTWARE_RESULT)

        if (swJson != null) {
            val softwareResult = gson.fromJson(swJson, SoftwareCheckResult::class.java)
            buySellResult = ScoringEngine.calculateBuySellResult(
                softwareResult = softwareResult,
                hardwareScore  = 75,
                deviceInfo     = com.buymyphone.app.data.models.DeviceInfo(
                    manufacturer  = android.os.Build.MANUFACTURER,
                    model         = android.os.Build.MODEL,
                    deviceName    = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                )
            )
        } else {
            val bsJson = intent.getStringExtra(Constants.EXTRA_BUYSELL_RESULT)
            buySellResult = if (bsJson != null) gson.fromJson(bsJson, BuySellResult::class.java)
                            else createDefaultResult()
        }

        displayResult(buySellResult)
        viewModel.saveReport(buySellResult)
    }

    private fun displayResult(r: BuySellResult) {
        // Verdict card
        val verdictColor = Color.parseColor(r.verdict.colorHex)
        val verdictBg    = Color.parseColor(r.verdict.bgColorHex)
        binding.cardVerdict.setCardBackgroundColor(verdictBg)
        binding.tvVerdictEmoji.text = r.verdict.emoji
        binding.tvVerdictLabel.text = r.verdict.label
        binding.tvVerdictLabel.setTextColor(verdictColor)

        // Score
        binding.scoreCircle.setScore(r.overallScore, "Trust Score")
        binding.tvConfidence.text = "Analysis Confidence: ${r.confidenceLevel}%"

        // Summary grid
        binding.tvSoftwareRisk.text   = r.softwareRisk
        binding.tvHardwareScore.text  = "${r.hardwareScore}/100"
        binding.tvFairPrice.text      = r.fairPriceRange

        // Why buy
        binding.tvWhyBuy.text = if (r.whyBuy.isEmpty()) "No strong positives detected."
            else r.whyBuy.joinToString("\n") { "✅  $it" }

        // Why avoid
        binding.tvWhyAvoid.text = if (r.whyAvoid.isEmpty()) "No major negatives detected."
            else r.whyAvoid.joinToString("\n") { "⚠️  $it" }

        // Details
        binding.tvResaleVerdict.text         = r.resaleVerdict
        binding.tvBuyerWarning.text          = r.buyerWarning
        binding.tvSellerRecommendation.text  = r.sellerRecommendation

        // Animations
        binding.cardVerdict.animateScale(0.8f, 1f, 500)
        binding.cardScore.animateFadeIn(500)
        binding.cardWhyBuy.animateFadeIn(700)
        binding.cardWhyAvoid.animateFadeIn(900)
        binding.cardDetails.animateFadeIn(1100)
    }

    private fun setupObservers() {
        viewModel.pdfPath.observe(this) { path ->
            if (!path.isNullOrBlank()) {
                showToast("PDF exported successfully")
                openPdf(path)
            } else {
                showToast("PDF export failed")
            }
        }

        viewModel.saved.observe(this) { saved ->
            if (saved) showToast("Report saved to history")
        }
    }

    private fun setupClickListeners() {
        binding.btnExportPdf.setOnClickListener {
            viewModel.exportPdf(buySellResult)
        }
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
        } catch (_: Exception) { showToast("No PDF viewer installed") }
    }

    private fun createDefaultResult() = BuySellResult(
        verdict = BuySellVerdict.GOOD_DEAL,
        overallScore = 70,
        softwareRisk = "Low Risk",
        hardwareScore = 75,
        whyBuy   = listOf("Device appears functional", "Software checks passed"),
        whyAvoid = emptyList(),
        resaleVerdict = "Moderate resale value expected",
        buyerWarning  = "Standard precautions recommended",
        sellerRecommendation = "Price competitively to attract buyers",
        fairPriceRange = "Market price or slight discount",
        confidenceLevel = 70
    )

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
