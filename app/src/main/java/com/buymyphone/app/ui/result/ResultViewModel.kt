package com.buymyphone.app.ui.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.BuyMyPhoneApplication
import com.buymyphone.app.data.database.entities.ReportEntity
import com.buymyphone.app.data.models.AnalysisResult
import com.buymyphone.app.managers.PdfExportManager
import com.buymyphone.app.utils.Constants
import kotlinx.coroutines.launch

class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BuyMyPhoneApplication
    private val pdfManager = PdfExportManager(application)

    private val _saveStatus = MutableLiveData<SaveStatus?>()
    private val _pdfPath    = MutableLiveData<String?>()

    val saveStatus: LiveData<SaveStatus?> = _saveStatus
    val pdfPath:    LiveData<String?>     = _pdfPath

    fun saveReport(result: AnalysisResult) {
        viewModelScope.launch {
            try {
                val json = app.reportRepository.toJson(result)
                val entity = ReportEntity(
                    deviceName       = result.deviceInfo.deviceName,
                    deviceModel      = result.deviceInfo.model,
                    reportType       = Constants.REPORT_TYPE_STANDARD,
                    score            = result.overallScore,
                    performanceClass = result.performanceClass.label,
                    bestUsage        = result.bestUsage.label,
                    timestamp        = System.currentTimeMillis(),
                    analysisJson     = json
                )
                val id = app.reportRepository.insertReport(entity)
                _saveStatus.postValue(SaveStatus.Success(id))
            } catch (e: Exception) {
                _saveStatus.postValue(SaveStatus.Error(e.message ?: "Save failed"))
            }
        }
    }

    fun exportPdf(result: AnalysisResult) {
        viewModelScope.launch {
            try {
                val path = pdfManager.exportAnalysisReport(result)
                _pdfPath.postValue(path)
            } catch (e: Exception) {
                _pdfPath.postValue(null)
            }
        }
    }
}

sealed class SaveStatus {
    data class Success(val reportId: Long) : SaveStatus()
    data class Error(val message: String)  : SaveStatus()
}
