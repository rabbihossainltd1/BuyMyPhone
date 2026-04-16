package com.buymyphone.app.data.repository

import androidx.lifecycle.LiveData
import com.buymyphone.app.data.database.ReportDao
import com.buymyphone.app.data.database.entities.ReportEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportRepository(
    private val reportDao: ReportDao,
    private val externalScope: CoroutineScope
) {
    private val gson = Gson()

    val allReports: LiveData<List<ReportEntity>> = reportDao.getAllReports()

    suspend fun insertReport(report: ReportEntity): Long {
        return withContext(Dispatchers.IO) {
            reportDao.insertReport(report)
        }
    }

    suspend fun getReportById(id: Long): ReportEntity? {
        return withContext(Dispatchers.IO) {
            reportDao.getReportById(id)
        }
    }

    suspend fun deleteReport(report: ReportEntity) {
        withContext(Dispatchers.IO) {
            reportDao.deleteReport(report)
        }
    }

    suspend fun deleteReportById(id: Long) {
        withContext(Dispatchers.IO) {
            reportDao.deleteReportById(id)
        }
    }

    suspend fun deleteAllReports() {
        withContext(Dispatchers.IO) {
            reportDao.deleteAllReports()
        }
    }

    suspend fun getReportCount(): Int {
        return withContext(Dispatchers.IO) {
            reportDao.getReportCount()
        }
    }

    suspend fun updatePdfPath(reportId: Long, pdfPath: String) {
        withContext(Dispatchers.IO) {
            val report = reportDao.getReportById(reportId) ?: return@withContext
            reportDao.updateReport(report.copy(pdfPath = pdfPath))
        }
    }

    fun <T> toJson(obj: T): String = gson.toJson(obj)

    inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, T::class.java)
}
