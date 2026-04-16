package com.buymyphone.app.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.BuyMyPhoneApplication
import com.buymyphone.app.data.database.entities.ReportEntity
import com.buymyphone.app.managers.PdfExportManager
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val app        = application as BuyMyPhoneApplication
    private val pdfManager = PdfExportManager(application)

    val allReports: LiveData<List<ReportEntity>> = app.reportRepository.allReports

    fun deleteReport(report: ReportEntity) {
        viewModelScope.launch { app.reportRepository.deleteReport(report) }
    }

    fun deleteAll() {
        viewModelScope.launch { app.reportRepository.deleteAllReports() }
    }

    fun getReportById(id: Long, callback: (ReportEntity?) -> Unit) {
        viewModelScope.launch {
            callback(app.reportRepository.getReportById(id))
        }
    }
}
