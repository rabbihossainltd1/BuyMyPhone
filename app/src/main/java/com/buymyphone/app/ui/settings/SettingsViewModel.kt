package com.buymyphone.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.BuyMyPhoneApplication
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BuyMyPhoneApplication

    private val _reportCount = MutableLiveData(0)
    val reportCount: LiveData<Int> = _reportCount

    init { loadStats() }

    private fun loadStats() {
        viewModelScope.launch {
            _reportCount.postValue(app.reportRepository.getReportCount())
        }
    }

    fun clearAllReports(onDone: () -> Unit) {
        viewModelScope.launch {
            app.reportRepository.deleteAllReports()
            _reportCount.postValue(0)
            onDone()
        }
    }
}
