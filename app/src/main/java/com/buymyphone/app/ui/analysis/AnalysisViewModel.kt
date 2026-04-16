package com.buymyphone.app.ui.analysis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.BuyMyPhoneApplication
import com.buymyphone.app.data.models.AnalysisResult
import com.buymyphone.app.managers.DeviceAnalysisManager
import com.buymyphone.app.utils.ScoringEngine
import kotlinx.coroutines.launch

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val analysisManager = DeviceAnalysisManager(application)

    private val _progress    = MutableLiveData(0)
    private val _statusText  = MutableLiveData("Initializing…")
    private val _result      = MutableLiveData<AnalysisResult?>()
    private val _error       = MutableLiveData<String?>()

    val progress:   LiveData<Int>              = _progress
    val statusText: LiveData<String>           = _statusText
    val result:     LiveData<AnalysisResult?>  = _result
    val error:      LiveData<String?>          = _error

    fun startAnalysis() {
        viewModelScope.launch {
            try {
                _statusText.postValue("Scanning CPU & SoC…")
                _progress.postValue(10)

                val deviceInfo = analysisManager.analyzeDevice()

                _statusText.postValue("Analyzing display…")
                _progress.postValue(35)

                _statusText.postValue("Checking battery…")
                _progress.postValue(55)

                _statusText.postValue("Inspecting cameras…")
                _progress.postValue(70)

                _statusText.postValue("Running scoring engine…")
                _progress.postValue(88)

                val analysisResult = ScoringEngine.calculateAnalysisResult(deviceInfo)

                _statusText.postValue("Finalizing report…")
                _progress.postValue(100)

                _result.postValue(analysisResult)
            } catch (e: Exception) {
                _error.postValue("Analysis failed: ${e.message}")
            }
        }
    }
}
