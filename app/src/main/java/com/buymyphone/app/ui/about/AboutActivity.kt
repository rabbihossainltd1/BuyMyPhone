package com.buymyphone.app.ui.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.buymyphone.app.BuildConfig
import com.buymyphone.app.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "About" }

        binding.tvVersion.text  = "Version ${BuildConfig.VERSION_NAME}"
        binding.tvBuildCode.text= "Build ${BuildConfig.VERSION_CODE}"
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
