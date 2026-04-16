package com.buymyphone.app.ui.premium

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.buymyphone.app.BuyMyPhoneApplication
import com.buymyphone.app.databinding.ActivityPremiumBinding
import com.buymyphone.app.utils.showToast

class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPremiumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Go Premium" }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPurchase.setOnClickListener {
            // Placeholder for actual billing integration
            getSharedPreferences(BuyMyPhoneApplication.PREFS_NAME, MODE_PRIVATE)
                .edit().putBoolean(BuyMyPhoneApplication.KEY_PREMIUM, true).apply()
            showToast("Premium activated! (Demo mode)")
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
