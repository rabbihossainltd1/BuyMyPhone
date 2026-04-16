package com.buymyphone.app.ui.deep

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.buymyphone.app.databinding.ActivityDeepAnalysisBinding
import com.buymyphone.app.utils.animateFadeIn
import com.buymyphone.app.utils.animateScale

class DeepAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeepAnalysisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeepAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Deep Analysis"
        }

        setupClickListeners()
        animateEntrance()
    }

    private fun setupClickListeners() {
        binding.btnSoftwareTest.setOnClickListener {
            it.animateScale(0.93f, 1f, 150)
            startActivity(Intent(this, SoftwareTestActivity::class.java))
        }

        binding.btnHardwareTest.setOnClickListener {
            it.animateScale(0.93f, 1f, 150)
            startActivity(Intent(this, HardwareTestActivity::class.java))
        }
    }

    private fun animateEntrance() {
        binding.cardSoftware.animateFadeIn(400)
        binding.cardHardware.animateFadeIn(600)
        binding.tvDeepDesc.animateFadeIn(300)
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
