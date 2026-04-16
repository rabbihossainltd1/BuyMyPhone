package com.buymyphone.app.ui.deep

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.buymyphone.app.adapters.SoftwareCheckAdapter
import com.buymyphone.app.data.models.SoftwareCheckResult
import com.buymyphone.app.databinding.ActivitySoftwareTestBinding
import com.buymyphone.app.ui.buysell.BuySellHelperActivity
import com.buymyphone.app.utils.Constants
import com.buymyphone.app.utils.animateFadeIn
import com.buymyphone.app.utils.gone
import com.buymyphone.app.utils.showToast
import com.buymyphone.app.utils.visible
import com.google.gson.Gson
import java.io.File

class SoftwareTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoftwareTestBinding
    private val viewModel: SoftwareTestViewModel by viewModels()
    private val adapter = SoftwareCheckAdapter()
    private val gson    = Gson()
    private var latestResult: SoftwareCheckResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoftwareTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Software Analysis" }

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        viewModel.runAnalysis()
    }

    private fun setupRecyclerView() {
        binding.recyclerChecks.apply {
            layoutManager = LinearLayoutManager(this@SoftwareTestActivity)
            adapter = this@SoftwareTestActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.progress.observe(this) { prog ->
            binding.progressBar.setProgressCompat(prog, true)
            binding.tvPercent.text = "$prog%"
        }

        viewModel.statusText.observe(this) { status ->
            binding.tvStatus.text = status
        }

        viewModel.result.observe(this) { result ->
            if (result != null) {
                latestResult = result
                showResults(result)
            }
        }

        viewModel.pdfPath.observe(this) { path ->
            if (!path.isNullOrBlank()) {
                showToast("PDF exported")
                openPdf(path)
            } else {
                showToast("PDF export failed")
            }
        }
    }

    private fun showResults(result: SoftwareCheckResult) {
        binding.layoutProgress.gone()
        binding.layoutResults.visible()
        binding.layoutResults.animateFadeIn(400)

        adapter.submitList(result.checks)

        // Risk banner
        val riskColor = Color.parseColor(result.overallRisk.colorHex)
        binding.tvRiskLevel.text       = result.overallRisk.label
        binding.tvRiskLevel.setTextColor(riskColor)
        binding.tvRiskSummary.text     = result.riskSummary
        binding.viewRiskIndicator.setBackgroundColor(riskColor)

        // Stats
        val passed   = result.checks.count { it.status.name == "PASS" }
        val warnings = result.checks.count { it.status.name == "WARNING" }
        val failed   = result.checks.count { it.status.name == "FAIL" }
        binding.tvPassCount.text    = "$passed Passed"
        binding.tvWarningCount.text = "$warnings Warnings"
        binding.tvFailCount.text    = "$failed Failed"

        binding.btnOpenBuySell.visible()
        binding.btnExportPdf.visible()
    }

    private fun setupClickListeners() {
        binding.btnOpenBuySell.setOnClickListener {
            val result = latestResult ?: return@setOnClickListener
            val intent = Intent(this, BuySellHelperActivity::class.java).apply {
                putExtra(Constants.EXTRA_SOFTWARE_RESULT, gson.toJson(result))
            }
            startActivity(intent)
        }

        binding.btnExportPdf.setOnClickListener {
            latestResult?.let { viewModel.exportPdf(it) }
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

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
