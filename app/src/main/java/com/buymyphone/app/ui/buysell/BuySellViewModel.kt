package com.buymyphone.app.ui.buysell

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.BuyMyPhoneApplication
import com.buymyphone.app.data.database.entities.ReportEntity
import com.buymyphone.app.data.models.BuySellResult
import com.buymyphone.app.data.models.DeviceInfo
import com.buymyphone.app.managers.PdfExportManager
import com.buymyphone.app.utils.Constants
import kotlinx.coroutines.launch

class BuySellViewModel(application: Application) : AndroidViewModel(application) {

    private val app        = application as BuyMyPhoneApplication
    private val pdfManager = PdfExportManager(application)

    private val _pdfPath = MutableLiveData<String?>()
    private val _saved   = MutableLiveData(false)

    val pdfPath: LiveData<String?> = _pdfPath
    val saved:   LiveData<Boolean> = _saved

    fun saveReport(result: BuySellResult) {
        viewModelScope.launch {
            try {
                val json = app.reportRepository.toJson(result)
                val entity = ReportEntity(
                    deviceName       = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL,
                    deviceModel      = android.os.Build.MODEL,
                    reportType       = Constants.REPORT_TYPE_BUYSELL,
                    score            = result.overallScore,
                    performanceClass = result.verdict.label,
                    bestUsage        = result.verdict.label,
                    timestamp        = System.currentTimeMillis(),
                    analysisJson     = json
                )
                app.reportRepository.insertReport(entity)
                _saved.postValue(true)
            } catch (_: Exception) { _saved.postValue(false) }
        }
    }

    fun exportPdf(result: BuySellResult) {
        viewModelScope.launch {
            val deviceInfo = DeviceInfo(
                manufacturer  = android.os.Build.MANUFACTURER,
                model         = android.os.Build.MODEL,
                deviceName    = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                androidVersion= android.os.Build.VERSION.RELEASE
            )
            val path = pdfManager.exportBuySellReport(result, deviceInfo)
            _pdfPath.postValue(path)
        }
    }
}
