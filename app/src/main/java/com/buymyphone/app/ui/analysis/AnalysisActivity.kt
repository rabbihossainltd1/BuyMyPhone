package com.buymyphone.app.ui.analysis

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.buymyphone.app.databinding.ActivityAnalysisBinding
import com.buymyphone.app.ui.result.ResultActivity
import com.buymyphone.app.utils.Constants
import com.google.gson.Gson

class AnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisBinding
    private val viewModel: AnalysisViewModel by viewModels()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        viewModel.startAnalysis()
    }

    private fun setupObservers() {
        viewModel.progress.observe(this) { progress ->
            binding.progressBar.setProgressCompat(progress, true)
            binding.tvPercent.text = "$progress%"
        }

        viewModel.statusText.observe(this) { status ->
            binding.tvStatus.text = status
        }

        viewModel.result.observe(this) { result ->
            if (result != null) {
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra(Constants.EXTRA_ANALYSIS_RESULT, gson.toJson(result))
                }
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }

        viewModel.error.observe(this) { err ->
            if (!err.isNullOrBlank()) {
                binding.tvStatus.text = err
            }
        }
    }
}
