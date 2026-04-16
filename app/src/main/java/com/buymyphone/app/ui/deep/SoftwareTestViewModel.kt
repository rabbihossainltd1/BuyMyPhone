package com.buymyphone.app.ui.deep

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.BuyMyPhoneApplication
import com.buymyphone.app.data.database.entities.ReportEntity
import com.buymyphone.app.data.models.SoftwareCheckResult
import com.buymyphone.app.managers.PdfExportManager
import com.buymyphone.app.managers.SoftwareAnalysisManager
import com.buymyphone.app.utils.Constants
import kotlinx.coroutines.launch

class SoftwareTestViewModel(application: Application) : AndroidViewModel(application) {

    private val app          = application as BuyMyPhoneApplication
    private val manager      = SoftwareAnalysisManager(application)
    private val pdfManager   = PdfExportManager(application)

    private val _progress   = MutableLiveData(0)
    private val _statusText = MutableLiveData("Preparing analysis…")
    private val _result     = MutableLiveData<SoftwareCheckResult?>()
    private val _pdfPath    = MutableLiveData<String?>()
    private val _saveId     = MutableLiveData<Long?>()

    val progress:   LiveData<Int>                   = _progress
    val statusText: LiveData<String>                = _statusText
    val result:     LiveData<SoftwareCheckResult?>  = _result
    val pdfPath:    LiveData<String?>               = _pdfPath
    val saveId:     LiveData<Long?>                 = _saveId

    fun runAnalysis() {
        viewModelScope.launch {
            try {
                val softwareResult = manager.runSoftwareAnalysis { prog, status ->
                    _progress.postValue(prog)
                    _statusText.postValue(status)
                }
                _result.postValue(softwareResult)
                autoSaveReport(softwareResult)
            } catch (e: Exception) {
                _statusText.postValue("Error: ${e.message}")
            }
        }
    }

    private suspend fun autoSaveReport(result: SoftwareCheckResult) {
        try {
            val json = app.reportRepository.toJson(result)
            val entity = ReportEntity(
                deviceName       = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL,
                deviceModel      = android.os.Build.MODEL,
                reportType       = Constants.REPORT_TYPE_SOFTWARE,
                score            = when (result.overallRisk.name) {
                    "LOW"    -> 90
                    "MEDIUM" -> 60
                    else     -> 25
                },
                performanceClass = result.overallRisk.label,
                bestUsage        = "Software Analysis",
                timestamp        = System.currentTimeMillis(),
                analysisJson     = json
            )
            val id = app.reportRepository.insertReport(entity)
            _saveId.postValue(id)
        } catch (_: Exception) {}
    }

    fun exportPdf(result: SoftwareCheckResult) {
        viewModelScope.launch {
            val deviceInfo = com.buymyphone.app.data.models.DeviceInfo(
                manufacturer  = android.os.Build.MANUFACTURER,
                model         = android.os.Build.MODEL,
                deviceName    = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                androidVersion= android.os.Build.VERSION.RELEASE
            )
            val path = pdfManager.exportSoftwareReport(result, deviceInfo)
            _pdfPath.postValue(path)
        }
    }
}
