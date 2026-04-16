package com.buymyphone.app.ui.compare

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.BuyMyPhoneApplication
import com.buymyphone.app.data.database.entities.ReportEntity
import kotlinx.coroutines.launch

class CompareViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BuyMyPhoneApplication

    private val _reports     = MutableLiveData<List<ReportEntity>>()
    private val _deviceA     = MutableLiveData<ReportEntity?>()
    private val _deviceB     = MutableLiveData<ReportEntity?>()

    val reports:  LiveData<List<ReportEntity>> = _reports
    val deviceA:  LiveData<ReportEntity?>      = _deviceA
    val deviceB:  LiveData<ReportEntity?>      = _deviceB

    init { loadReports() }

    private fun loadReports() {
        viewModelScope.launch {
            _reports.postValue(app.reportRepository.getAllReportsSuspend())
        }
    }

    private suspend fun ReportRepository_getAllReportsSuspend(): List<ReportEntity> {
        return app.reportRepository.getAllReportsSuspend()
    }

    fun selectDeviceA(report: ReportEntity) { _deviceA.postValue(report) }
    fun selectDeviceB(report: ReportEntity) { _deviceB.postValue(report) }
    fun clearSelection() { _deviceA.postValue(null); _deviceB.postValue(null) }
}

// Extension to expose suspend function
private suspend fun com.buymyphone.app.data.repository.ReportRepository.getAllReportsSuspend() =
    getAllReportsSuspend()
