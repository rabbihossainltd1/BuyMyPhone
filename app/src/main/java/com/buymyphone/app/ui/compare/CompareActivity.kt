package com.buymyphone.app.ui.compare

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.buymyphone.app.data.database.entities.ReportEntity
import com.buymyphone.app.databinding.ActivityCompareBinding
import com.buymyphone.app.utils.gone
import com.buymyphone.app.utils.visible

class CompareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompareBinding
    private val viewModel: CompareViewModel by viewModels()
    private var selectedSlot = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Compare Phones" }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.deviceA.observe(this) { report ->
            if (report != null) {
                binding.tvDeviceAName.text  = report.deviceName
                binding.tvDeviceAScore.text = "${report.score}"
                binding.tvDeviceAClass.text = report.performanceClass
            } else {
                binding.tvDeviceAName.text  = "Select Device A"
                binding.tvDeviceAScore.text = "--"
                binding.tvDeviceAClass.text = ""
            }
            maybeShowComparison()
        }

        viewModel.deviceB.observe(this) { report ->
            if (report != null) {
                binding.tvDeviceBName.text  = report.deviceName
                binding.tvDeviceBScore.text = "${report.score}"
                binding.tvDeviceBClass.text = report.performanceClass
            } else {
                binding.tvDeviceBName.text  = "Select Device B"
                binding.tvDeviceBScore.text = "--"
                binding.tvDeviceBClass.text = ""
            }
            maybeShowComparison()
        }

        viewModel.reports.observe(this) { reports ->
            binding.tvReportCount.text = "${reports.size} saved reports available"
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectA.setOnClickListener {
            selectedSlot = 0
            showReportPicker()
        }

        binding.btnSelectB.setOnClickListener {
            selectedSlot = 1
            showReportPicker()
        }

        binding.btnClear.setOnClickListener {
            viewModel.clearSelection()
            binding.cardComparison.gone()
        }
    }

    private fun showReportPicker() {
        val reports = viewModel.reports.value ?: return
        if (reports.isEmpty()) {
            com.google.android.material.snackbar.Snackbar
                .make(binding.root, "No reports saved yet", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        val names = reports.map { "${it.deviceName} (${it.timestamp.formattedDate()})" }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Device")
            .setItems(names) { _, idx ->
                val report = reports[idx]
                if (selectedSlot == 0) viewModel.selectDeviceA(report)
                else viewModel.selectDeviceB(report)
            }
            .show()
    }

    private fun maybeShowComparison() {
        val a = viewModel.deviceA.value
        val b = viewModel.deviceB.value
        if (a != null && b != null) {
            binding.cardComparison.visible()
            val winner = if (a.score >= b.score) a.deviceName else b.deviceName
            binding.tvWinner.text = "🏆 Winner: $winner"
            binding.tvScoreDiff.text = "Score difference: ${Math.abs(a.score - b.score)} pts"
        }
    }

    private fun Long.formattedDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(this))
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
