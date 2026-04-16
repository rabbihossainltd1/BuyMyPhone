package com.buymyphone.app.ui.deep

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.buymyphone.app.adapters.HardwareTestAdapter
import com.buymyphone.app.data.models.HardwareTestItem
import com.buymyphone.app.data.models.HardwareTestStatus
import com.buymyphone.app.databinding.ActivityHardwareTestBinding
import com.buymyphone.app.utils.animateFadeIn
import com.buymyphone.app.utils.showToast

class HardwareTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHardwareTestBinding
    private val viewModel: HardwareTestViewModel by viewModels()
    private lateinit var adapter: HardwareTestAdapter
    private var pendingTestItem: HardwareTestItem? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            pendingTestItem?.let { executeTest(it) }
        } else {
            showToast("Permission denied — test skipped")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHardwareTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Hardware Tests" }

        setupAdapter()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupAdapter() {
        adapter = HardwareTestAdapter { item -> handleTestAction(item) }
    }

    private fun setupRecyclerView() {
        binding.recyclerTests.apply {
            layoutManager = LinearLayoutManager(this@HardwareTestActivity)
            adapter = this@HardwareTestActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.tests.observe(this) { tests ->
            adapter.submitList(tests.toList())
        }

        viewModel.passCount.observe(this) {
            binding.tvPassCount.text = "$it Passed"
        }

        viewModel.failCount.observe(this) {
            binding.tvFailCount.text = "$it Failed"
        }

        viewModel.totalScore.observe(this) { score ->
            binding.tvHardwareScore.text = "Score: $score%"
            binding.scoreProgress.setProgressCompat(score, true)
        }
    }

    private fun setupClickListeners() {
        binding.btnRunAll.setOnClickListener {
            viewModel.runAllAutoTests()
            showToast("Running all automatic tests…")
        }
    }

    private fun handleTestAction(item: HardwareTestItem) {
        if (item.requiresPermission) {
            val perm = item.permissionNeeded
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                pendingTestItem = item
                permissionLauncher.launch(arrayOf(perm))
                return
            }
        }

        if (item.isManual) {
            showManualTestDialog(item)
        } else {
            executeTest(item)
        }
    }

    private fun executeTest(item: HardwareTestItem) {
        viewModel.runAutoTest(item)
    }

    private fun showManualTestDialog(item: HardwareTestItem) {
        AlertDialog.Builder(this)
            .setTitle(item.title)
            .setMessage("${item.description}\n\nDid this test pass?")
            .setPositiveButton("✅ Pass")  { _, _ -> viewModel.markManual(item, true) }
            .setNegativeButton("❌ Fail")  { _, _ -> viewModel.markManual(item, false) }
            .setNeutralButton("Skip")     { _, _ -> /* skip silently */ }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
